/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractTree extends AbstractPropertyObserver implements ITree {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTree.class);

  private final EventListenerList m_listenerList = new EventListenerList();
  private ITreeUIFacade m_uiFacade;
  private List<IMenu> m_menus;
  private boolean m_initialized;

  // enabled is defined as: enabledGranted && enabledProperty && enabledSlave
  private boolean m_enabledGranted;
  private boolean m_enabledProperty;

  private ITreeNode m_rootNode;
  private int m_treeChanging;
  private boolean m_autoDiscardOnDelete;
  private boolean m_autoTitle;
  private final HashMap<Object, ITreeNode> m_deletedNodes;
  private List<TreeEvent> m_treeEventBuffer = new ArrayList<TreeEvent>();
  private Set<ITreeNode> m_nodeDecorationBuffer = new HashSet<ITreeNode>();
  private Set<ITreeNode> m_selectedNodes = new HashSet<ITreeNode>();
  private final List<ITreeNodeFilter> m_nodeFilters;
  private final int m_uiProcessorCount = 0;
  private List<IKeyStroke> m_baseKeyStrokes;
  private IEventHistory<TreeEvent> m_eventHistory;
  // only do one action at a time
  private boolean m_actionRunning;
  private boolean m_saveAndRestoreScrollbars;
  private ITreeNode m_lastSeenDropNode;

  public AbstractTree() {
    this(true);
  }

  public AbstractTree(boolean callInitialzier) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTree(this);
    }
    m_deletedNodes = new HashMap<Object, ITreeNode>();
    m_nodeFilters = new ArrayList<ITreeNodeFilter>(1);
    m_actionRunning = false;
    if (callInitialzier) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredAutoTitle() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredMultiSelect() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(42)
  protected boolean getConfiguredMultiCheck() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(45)
  protected boolean getConfiguredCheckable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(46)
  protected int getConfiguredNodeHeightHint() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredDragEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(51)
  protected int getConfiguredDragType() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(52)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * @return true: deleted nodes are automatically erased<br>
   *         false: deleted nodes are cached for later processing (service
   *         deletion)
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredAutoDiscardOnDelete() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredRootNodeVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(71)
  protected boolean getConfiguredRootHandlesVisible() {
    return true;
  }

  /**
   * Advices the ui to automatically scroll to the selection
   * <p>
   * If not used permanent, this feature can also used dynamically at individual occasions using
   * 
   * <pre>
   * {@link #scrollToSelection()}
   * </pre>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredScrollToSelection() {
    return false;
  }

  /**
   * Configures whether this tree should save and restore its coordinates of the vertical and horizontal scrollbars.
   * If this property is set to {@code true}, the tree saves its scrollbars coordinates to the {@link #ClientSession}
   * upon
   * detaching the UI component from Scout. The coordinates are restored (if the coordnates are available), when the UI
   * component is attached to Scout.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   * 
   * @return {@code true} if this tree should save and restore its scrollbars coordinates, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredSaveAndRestoreScrollbars() {
    return false;
  }

  private List<Class<? extends IKeyStroke>> getConfiguredKeyStrokes() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IKeyStroke>> fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  private List<Class<? extends IMenu>> getConfiguredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    List<Class<? extends IMenu>> foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  @ConfigOperation
  @Order(10)
  protected void execInitTree() throws ProcessingException {
  }

  @ConfigOperation
  @Order(15)
  protected void execDisposeTree() throws ProcessingException {
  }

  /**
   * The hyperlink's tree node is the selected node {@link #getSelectedNode()}
   * 
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(18)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
  }

  /**
   * this method should not be implemented if you support {@link AbstractTree#execDrag(ITreeNode[])} (drag of mulitple
   * nodes), as it takes precedence
   * 
   * @return a transferable object representing the given row
   */
  @ConfigOperation
  @Order(20)
  protected TransferObject execDrag(ITreeNode node) throws ProcessingException {
    return null;
  }

  /**
   * Drag of multiple nodes. If this method is implemented, also single drags will be handled by Scout,
   * the method {@link AbstractTree#execDrag(ITreeNode)} must not be implemented then.
   * 
   * @return a transferable object representing the given rows
   */
  @ConfigOperation
  @Order(30)
  protected TransferObject execDrag(Collection<ITreeNode> nodes) throws ProcessingException {
    return null;
  }

  /**
   * process drop action
   */
  @ConfigOperation
  @Order(40)
  protected void execDrop(ITreeNode node, TransferObject t) throws ProcessingException {
  }

  /**
   * This method gets called when the drop node is changed, e.g. the dragged object
   * is moved over a new drop target.
   * 
   * @since 4.0-M7
   */
  @ConfigOperation
  @Order(45)
  protected void execDropTargetChanged(ITreeNode node) throws ProcessingException {
  }

  /**
   * decoration for every cell calls this method
   * <p>
   * Default delegates to {@link ITreeNode#decorateCell()}
   */
  @ConfigOperation
  @Order(50)
  protected void execDecorateCell(ITreeNode node, Cell cell) throws ProcessingException {
    if (cell.getIconId() == null && getIconId() != null) {
      cell.setIconId(getIconId());
    }
    node.decorateCell();
  }

  @ConfigOperation
  @Order(60)
  protected void execNodesSelected(TreeEvent e) throws ProcessingException {
  }

  @ConfigOperation
  @Order(70)
  protected void execNodeClick(ITreeNode node) throws ProcessingException {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_CLICK, node);
    fireTreeEventInternal(e);
  }

  @ConfigOperation
  @Order(80)
  protected void execNodeAction(ITreeNode node) throws ProcessingException {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_ACTION, node);
    fireTreeEventInternal(e);
  }

  protected void initConfig() {
    m_enabledGranted = true;
    m_eventHistory = createEventHistory();
    m_uiFacade = new P_UIFacade();
    setTitle(getConfiguredTitle());
    setIconId(getConfiguredIconId());
    setAutoTitle(getConfiguredAutoTitle());
    setCheckable(getConfiguredCheckable());
    setNodeHeightHint(getConfiguredNodeHeightHint());
    setMultiCheck(getConfiguredMultiCheck());
    setMultiSelect(getConfiguredMultiSelect());
    setAutoDiscardOnDelete(getConfiguredAutoDiscardOnDelete());
    setDragEnabled(getConfiguredDragEnabled());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setRootNodeVisible(getConfiguredRootNodeVisible());
    setRootHandlesVisible(getConfiguredRootHandlesVisible());
    setScrollToSelection(getConfiguredScrollToSelection());
    setSaveAndRestoreScrollbars(getConfiguredSaveAndRestoreScrollbars());
    setRootNode(new AbstractTreeNode() {
    });
    // add Convenience observer for drag & drop callbacks and event history
    addTreeListener(new TreeAdapter() {

      @Override
      public void treeChanged(TreeEvent e) {
        //event history
        IEventHistory<TreeEvent> h = getEventHistory();
        if (h != null) {
          h.notifyEvent(e);
        }
        //dnd
        switch (e.getType()) {
          case TreeEvent.TYPE_NODES_DRAG_REQUEST: {
            m_lastSeenDropNode = null;
            if (e.getDragObject() == null) {
              try {
                TransferObject transferObject = execDrag(e.getNode());
                if (transferObject == null) {
                  transferObject = execDrag(e.getNodes());
                }
                e.setDragObject(transferObject);
              }
              catch (Throwable t) {
                LOG.error("Drag", t);
              }
            }
            break;
          }
          case TreeEvent.TYPE_NODE_DROP_ACTION: {
            m_lastSeenDropNode = null;
            if (e.getDropObject() != null) {
              try {
                execDrop(e.getNode(), e.getDropObject());
              }
              catch (Throwable t) {
                LOG.error("Drop", t);
              }
            }
            break;
          }
          case TreeEvent.TYPE_NODES_SELECTED: {
            rebuildKeyStrokesInternal();
            break;
          }
          case TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED: {
            try {
              if (m_lastSeenDropNode == null || m_lastSeenDropNode != e.getNode()) {
                m_lastSeenDropNode = e.getNode();
                execDropTargetChanged(e.getNode());
              }
            }
            catch (Throwable t) {
              LOG.error("DropTargetChanged", t);
            }
            break;
          }
          case TreeEvent.TYPE_DRAG_FINISHED: {
            m_lastSeenDropNode = null;
          }
        }
      }
    });
    // key shortcuts
    ArrayList<IKeyStroke> ksList = new ArrayList<IKeyStroke>();
    for (Class<? extends IKeyStroke> keystrokeClazz : getConfiguredKeyStrokes()) {
      try {
        ksList.add(ConfigurationUtility.newInnerInstance(this, keystrokeClazz));
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("keyStroke: " + keystrokeClazz.getName(), t));
      }
    }
    //ticket 87370: add ENTER key stroke when execNodeAction has an override
    if (ConfigurationUtility.isMethodOverwrite(AbstractTree.class, "execNodeAction", new Class[]{ITreeNode.class}, this.getClass())) {
      ksList.add(new KeyStroke("ENTER") {
        @Override
        protected void execAction() throws ProcessingException {
          fireNodeAction(getSelectedNode());
        }
      });
    }
    m_baseKeyStrokes = ksList;
    setKeyStrokesInternal(m_baseKeyStrokes);
    // menus
    List<IMenu> menuList = new ArrayList<IMenu>();
    for (Class<? extends IMenu> menuClazz : getConfiguredMenus()) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
        menuList.add(menu);
      }
      catch (Exception e) {
        LOG.error("Exception occured while creating a new instance of " + menuClazz.getName(), e);
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("Error occured while dynamically contributing menus.", e);
    }
    m_menus = menuList;
  }

  /*
   * Runtime
   */
  @Override
  public final void initTree() throws ProcessingException {
    initTreeInternal();
    ActionUtility.initActions(getMenus());
    execInitTree();
  }

  protected void initTreeInternal() throws ProcessingException {
  }

  @Override
  public final void disposeTree() {
    disposeTreeInternal();
    try {
      execDisposeTree();
    }
    catch (Throwable t) {
      LOG.warn(getClass().getName(), t);
    }
  }

  protected void disposeTreeInternal() {
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br/>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
  }

  @Override
  public List<IMenu> getMenus() {
    return CollectionUtility.arrayList(m_menus);
  }

  @Override
  public void setMenus(List<? extends IMenu> menues) {
    m_menus = CollectionUtility.arrayList(menues);
  }

  @Override
  public <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException {
    // ActionFinder performs instance-of checks. Hence the menu replacement mapping is not required
    return new ActionFinder().findAction(getMenus(), menuType);
  }

  @Override
  public boolean hasNodeFilters() {
    return m_nodeFilters.size() > 0;
  }

  @Override
  public List<ITreeNodeFilter> getNodeFilters() {
    return CollectionUtility.arrayList(m_nodeFilters);
  }

  @Override
  public void addNodeFilter(ITreeNodeFilter filter) {
    if (filter != null) {
      //avoid duplicate add
      boolean exists = false;
      for (ITreeNodeFilter existingFilter : m_nodeFilters) {
        if (existingFilter == filter) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        m_nodeFilters.add(filter);
      }
      applyNodeFilters();
    }
  }

  @Override
  public void removeNodeFilter(ITreeNodeFilter filter) {
    if (filter != null) {
      m_nodeFilters.remove(filter);
      applyNodeFilters();
    }
  }

  @Override
  public void applyNodeFilters() {
    applyNodeFiltersRecInternal(getRootNode(), true, 0);
    fireNodeFilterChanged();
  }

  private void applyNodeFiltersRecInternal(ITreeNode inode, boolean parentAccepted, int level) {
    if (inode == null) {
      return;
    }
    inode.setFilterAccepted(true);
    if (m_nodeFilters.size() > 0) {
      for (ITreeNodeFilter filter : m_nodeFilters) {
        if (!filter.accept(inode, level)) {
          inode.setFilterAccepted(false);
          break;
        }
      }
    }
    if (!inode.isFilterAccepted() && isSelectedNode(inode)) {
      // invisible nodes cannot be selected
      deselectNode(inode);
    }
    // make parent path accepted
    if ((!parentAccepted) && inode.isFilterAccepted()) {
      ITreeNode tmp = inode.getParentNode();
      while (tmp != null) {
        if (tmp instanceof AbstractTreeNode) {
          ((AbstractTreeNode) tmp).setFilterAccepted(true);
        }
        tmp = tmp.getParentNode();
      }
    }
    // children
    for (ITreeNode child : inode.getChildNodes()) {
      applyNodeFiltersRecInternal(child, inode.isFilterAccepted(), level + 1);
    }
  }

  @Override
  public void requestFocus() {
    fireRequestFocus();
  }

  @Override
  public ITreeNode getRootNode() {
    return m_rootNode;
  }

  @Override
  public Object getProperty(String name) {
    return propertySupport.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    propertySupport.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public boolean isAutoTitle() {
    return m_autoTitle;
  }

  @Override
  public void setAutoTitle(boolean b) {
    m_autoTitle = b;
  }

  @Override
  public String getIconId() {
    String iconId = propertySupport.getPropertyString(PROP_ICON_ID);
    if (iconId != null && iconId.length() == 0) {
      iconId = null;
    }
    return iconId;
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public boolean isCheckable() {
    return propertySupport.getPropertyBool(PROP_CHECKABLE);
  }

  @Override
  public void setCheckable(boolean b) {
    propertySupport.setPropertyBool(PROP_CHECKABLE, b);
  }

  @Override
  public int getNodeHeightHint() {
    return propertySupport.getPropertyInt(PROP_NODE_HEIGHT_HINT);
  }

  @Override
  public void setNodeHeightHint(int h) {
    propertySupport.setPropertyInt(PROP_NODE_HEIGHT_HINT, h);
  }

  @Override
  public boolean isDragEnabled() {
    return propertySupport.getPropertyBool(PROP_DRAG_ENABLED);
  }

  @Override
  public void setDragEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_DRAG_ENABLED, b);
  }

  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  @Override
  public void setEnabledPermission(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setEnabledGranted(b);
  }

  @Override
  public boolean isEnabledGranted() {
    return m_enabledGranted;
  }

  @Override
  public void setEnabledGranted(boolean b) {
    m_enabledGranted = b;
    calculateEnabled();
  }

  @Override
  public void setEnabled(boolean b) {
    m_enabledProperty = b;
    calculateEnabled();
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  private void calculateEnabled() {
    propertySupport.setPropertyBool(PROP_ENABLED, m_enabledGranted && m_enabledProperty);
  }

  @Override
  public String getPathText(ITreeNode selectedNode) {
    return getPathText(selectedNode, " - ");
  }

  @Override
  public String getPathText(ITreeNode selectedNode, String delimiter) {
    // construct the path to the data
    ITreeNode root = getRootNode();
    StringBuffer pathStr = new StringBuffer("");
    ITreeNode node = selectedNode;
    while (node != null) {
      if (node != root || isRootNodeVisible()) {
        if (pathStr.length() != 0) {
          pathStr.insert(0, delimiter);
        }
        pathStr.insert(0, node.getCell().getText());
      }
      // next
      node = node.getParentNode();
    }
    return pathStr.toString();
  }

  private void rebuildTitleInternal() {
    setTitle(getPathText(getSelectedNode()));
  }

  private void rebuildKeyStrokesInternal() {
    //Get the menus for the selected nodes
    List<IMenu> menus;
    try {
      Collection<ITreeNode> nodes = resolveVirtualNodes(getSelectedNodes());
      menus = fetchMenusForNodesInternal(nodes);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      menus = CollectionUtility.emptyArrayList();
    }

    //Compute the Keystrokes: base + keyStroke for the current Menus.
    List<IKeyStroke> ksList = new ArrayList<IKeyStroke>(m_baseKeyStrokes);
    for (IMenu menu : menus) {
      if (menu.getKeyStroke() != null) {
        try {
          IKeyStroke ks = new KeyStroke(menu.getKeyStroke(), menu);
          ks.initAction();
          ksList.add(ks);
        }
        catch (ProcessingException e) {
          LOG.error("could not initialize key stroke '" + menu.getKeyStroke() + "'", e);
        }
      }
    }

    //Set KeyStrokes:
    setKeyStrokesInternal(ksList);
  }

  @Override
  public ITreeNode findNode(Object primaryKey) {
    Collection<ITreeNode> a = findNodes(CollectionUtility.hashSet(primaryKey));
    if (a != null && a.size() > 0) {
      return CollectionUtility.firstElement(a);
    }
    else {
      return null;
    }
  }

  @Override
  public List<ITreeNode> findNodes(final Collection<?> primaryKeys) {
    if (primaryKeys == null || primaryKeys.size() <= 0) {
      return CollectionUtility.emptyArrayList();
    }

    final Set<Object> keySet = new HashSet<Object>(primaryKeys);
    P_AbstractCollectingTreeVisitor v = new P_AbstractCollectingTreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (keySet.remove(node.getPrimaryKey())) {
          addNodeToList(node);
        }
        return !keySet.isEmpty();
      }
    };
    visitNode(getRootNode(), v);
    return v.getNodes();
  }

  @Override
  public void setRootNode(ITreeNode root) {
    if (m_rootNode != null) {
      m_rootNode.setTreeInternal(null, true);
      // inform root of remove
      root.nodeRemovedNotify();
    }
    m_rootNode = root;
    if (m_rootNode != null) {
      m_rootNode.setTreeInternal(this, true);
      // inform root of add
      m_rootNode.nodeAddedNotify();
      // expand root if it is not visible
      if (!isRootNodeVisible()) {
        try {
          m_rootNode.ensureChildrenLoaded();
        }
        catch (ProcessingException e) {
          LOG.error("expanding root node of " + getTitle(), e);
        }
      }
    }
  }

  @Override
  public boolean isRootNodeVisible() {
    return propertySupport.getPropertyBool(PROP_ROOT_NODE_VISIBLE);
  }

  @Override
  public void setRootNodeVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_ROOT_NODE_VISIBLE, b);
  }

  @Override
  public boolean isRootHandlesVisible() {
    return propertySupport.getPropertyBool(PROP_ROOT_HANDLES_VISIBLE);
  }

  @Override
  public void setRootHandlesVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_ROOT_HANDLES_VISIBLE, b);
  }

  @Override
  public boolean isTreeChanging() {
    return m_treeChanging > 0;
  }

  @Override
  public void setTreeChanging(boolean b) {
    // use a stack counter because setTableChanging might be called in nested
    // loops
    if (b) {
      m_treeChanging++;
      if (m_treeChanging == 1) {
        // 0 --> 1
        propertySupport.setPropertiesChanging(true);
      }
    }
    else {
      if (m_treeChanging > 0) {
        m_treeChanging--;
        if (m_treeChanging == 0) {
          try {
            processChangeBuffer();
          }
          finally {
            propertySupport.setPropertiesChanging(false);
          }
        }
      }
    }
  }

  @Override
  public boolean isNodeExpanded(ITreeNode node) {
    if (node != null) {
      return node.isExpanded();
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeExpanded(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      if (node.isExpanded() != b) {
        try {
          if (b) {
            node.ensureChildrenLoaded();
            ensureParentExpanded(node.getParentNode());
          }
          node.setExpandedInternal(b);
          fireNodeExpanded(node, b);
        }
        catch (ProcessingException e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        }
      }
    }
  }

  @Override
  public boolean isAncestorNodeOf(ITreeNode parent, ITreeNode child) {
    ITreeNode t = child;
    while (t != null && t != parent) {
      t = t.getParentNode();
    }
    return t == parent;
  }

  @Override
  public boolean isAutoDiscardOnDelete() {
    return m_autoDiscardOnDelete;
  }

  @Override
  public void setAutoDiscardOnDelete(boolean on) {
    m_autoDiscardOnDelete = on;
  }

  @Override
  public void setNodeEnabledPermission(ITreeNode node, Permission p) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isEnabled();
      node.setEnabledPermissionInternal(p);
      boolean newValue = node.isEnabled();
      if (oldValue != newValue) {
        fireNodesUpdated(node.getParentNode(), CollectionUtility.hashSet(node));
      }
    }
  }

  @Override
  public boolean isNodeEnabled(ITreeNode node) {
    if (node != null) {
      return node.isEnabled();
    }
    else {
      return false;
    }
  }

  @Override
  public boolean isNodeEnabledGranted(ITreeNode node) {
    if (node != null) {
      return node.isEnabledGranted();
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeEnabled(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isEnabled();
      node.setEnabledInternal(b);
      boolean newValue = node.isEnabled();
      if (oldValue != newValue) {
        fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

  @Override
  public void setNodeEnabledGranted(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isEnabled();
      node.setEnabledGrantedInternal(b);
      boolean newValue = node.isEnabled();
      if (oldValue != newValue) {
        fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

  @Override
  public void setNodeVisiblePermission(ITreeNode node, Permission p) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isVisible();
      node.setVisiblePermissionInternal(p);
      boolean newValue = node.isVisible();
      if (oldValue != newValue) {
        // dont fire observers since visibility change only has an effect when
        // used in init method
      }
    }
  }

  @Override
  public boolean isNodeVisible(ITreeNode node) {
    if (node != null) {
      return node.isVisible();
    }
    else {
      return false;
    }
  }

  @Override
  public boolean isNodeVisibleGranted(ITreeNode node) {
    if (node != null) {
      return node.isVisibleGranted();
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeVisible(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isVisible();
      node.setVisibleInternal(b);
      boolean newValue = node.isVisible();
      if (oldValue != newValue) {
        // dont fire observers since visibility change only has an effect when
        // used in init method
      }
    }
  }

  @Override
  public void setNodeVisibleGranted(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isVisible();
      node.setVisibleGrantedInternal(b);
      boolean newValue = node.isVisible();
      if (oldValue != newValue) {
        // dont fire observers since visibility change only has an effect when
        // used in init method
      }
    }
  }

  @Override
  public boolean isNodeLeaf(ITreeNode node) {
    if (node != null) {
      return node.isLeaf();
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeLeaf(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      if (node.isLeaf() != b) {
        node.setLeafInternal(b);
        fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

  @Override
  public boolean isNodeChecked(ITreeNode node) {
    if (node != null) {
      return node.isChecked();
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeChecked(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      if (node.isChecked() != b) {
        List<ITreeNode> changedNodes = new ArrayList<ITreeNode>();
        node.setCheckedInternal(b);
        changedNodes.add(node);

        ITreeNode commonParent = node.getParentNode();
        //uncheck others in single-check mode
        if (b && !isMultiCheck()) {
          for (ITreeNode cn : getCheckedNodes()) {
            if (cn != node) {
              cn.setCheckedInternal(false);
              changedNodes.add(cn);
            }
          }
          commonParent = TreeUtility.findLowestCommonAncestorNode(changedNodes);
        }
        fireNodesUpdated(commonParent, changedNodes);
      }
    }
  }

  @Override
  public int getNodeStatus(ITreeNode node) {
    if (node != null) {
      return node.getStatus();
    }
    else {
      return ITreeNode.STATUS_NON_CHANGED;
    }
  }

  @Override
  public void setNodeStatus(ITreeNode node, int status) {
    node = resolveNode(node);
    if (node != null) {
      if (node.getStatus() != status) {
        node.setStatusInternal(status);
        fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

  @Override
  public Object getContainer() {
    return propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an {@link ITree}
   */
  public void setContainerInternal(Object container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  private void ensureParentExpanded(ITreeNode parent) {
    if (parent != null) {
      ensureParentExpanded(parent.getParentNode());
      if (!parent.isExpanded()) {
        setNodeExpanded(parent, true);
      }
    }
  }

  @Override
  public void ensureVisible(ITreeNode node) {
    fireNodeEnsureVisible(node);
  }

  @Override
  public void expandAll(ITreeNode parent) {
    expandAllRec(parent, 0);
  }

  private void expandAllRec(ITreeNode parent, int level) {
    setNodeExpanded(parent, true);
    // loop detection
    if (level >= 32) {
      LOG.warn("detected loop on tree node " + parent);
    }
    else {
      List<ITreeNode> children = parent.getChildNodes();
      for (ITreeNode child : children) {
        expandAllRec(child, level + 1);
      }
    }
  }

  @Override
  public void collapseAll(ITreeNode parent) {
    try {
      setTreeChanging(true);
      //
      ArrayList<ITreeNode> list = new ArrayList<ITreeNode>();
      fetchAllCollapsingNodesRec(parent, 0, list);
      for (int n = list.size(), i = n - 1; i >= 0; i--) {
        setNodeExpanded(list.get(i), false);
      }
    }
    finally {
      setTreeChanging(false);
    }
  }

  private void fetchAllCollapsingNodesRec(ITreeNode parent, int level, List<ITreeNode> list) {
    // loop detection
    if (level >= 32) {
      LOG.warn("detected loop on tree node " + parent);
    }
    else {
      if (parent.isExpanded()) {
        list.add(parent);
        List<ITreeNode> children = parent.getChildNodes();
        for (ITreeNode child : children) {
          fetchAllCollapsingNodesRec(child, level + 1, list);
        }
      }
    }
  }

  @Override
  public List<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.arrayList(propertySupport.<IKeyStroke> getPropertyList(PROP_KEY_STROKES));
  }

  @Override
  public void setKeyStrokes(List<? extends IKeyStroke> keyStrokes) {
    m_baseKeyStrokes = CollectionUtility.arrayListWithoutNullElements(keyStrokes);
    rebuildKeyStrokesInternal();
  }

  private void setKeyStrokesInternal(List<? extends IKeyStroke> keyStrokes) {
    propertySupport.setPropertyList(PROP_KEY_STROKES, keyStrokes);
  }

  /*
   * modifications
   */
  @Override
  public void addChildNode(ITreeNode parent, ITreeNode child) {
    if (child != null) {
      addChildNodes(parent, CollectionUtility.arrayList(child));
    }
  }

  @Override
  public void addChildNode(int startIndex, ITreeNode parent, ITreeNode child) {
    if (child != null) {
      addChildNodes(startIndex, parent, CollectionUtility.arrayList(child));
    }
  }

  @Override
  public void addChildNodes(ITreeNode parent, List<? extends ITreeNode> children) {
    addChildNodes(parent.getChildNodeCount(), parent, children);
  }

  @Override
  public void addChildNodes(int startIndex, ITreeNode parent, List<? extends ITreeNode> children) {
    if (!CollectionUtility.hasElements(children)) {
      return;
    }
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      ((AbstractTreeNode) parent).addChildNodesInternal(startIndex, children, true);
      // check if all children were added, or if some were revoked using
      // visible=false in init (addNotify) phase.
      List<ITreeNode> newChildren = new ArrayList<ITreeNode>();
      for (ITreeNode child : children) {
        if (child.getParentNode() != null) {
          newChildren.add(child);
        }
      }
      // decorate
      decorateAffectedNodeCells(parent, newChildren);
      // filter
      int level = 0;
      ITreeNode tmp = parent;
      while (tmp != null) {
        tmp = tmp.getParentNode();
        level++;
      }
      for (ITreeNode child : newChildren) {
        applyNodeFiltersRecInternal(child, parent.isFilterAccepted(), level);
      }
      fireNodesInserted(parent, newChildren);
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void updateNode(ITreeNode node) {
    if (node != null) {
      updateChildNodes(node.getParentNode(), CollectionUtility.hashSet(node));
    }
  }

  @Override
  public void updateChildNodes(ITreeNode parent, Collection<? extends ITreeNode> children) {
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      Collection<ITreeNode> resolvedChildren = resolveNodes(children);
      decorateAffectedNodeCells(parent, resolvedChildren);
      fireNodesUpdated(parent, resolvedChildren);
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void updateChildNodeOrder(ITreeNode parent, List<? extends ITreeNode> newChildren) {
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      List<ITreeNode> newChildrenResolved = resolveNodes(newChildren);
      if (newChildren.size() > 0 && newChildrenResolved.size() == newChildren.size()) {
        ((AbstractTreeNode) parent).setChildNodeOrderInternal(newChildrenResolved);
        decorateAffectedNodeCells(parent, newChildrenResolved);
        fireChildNodeOrderChanged(parent, newChildrenResolved);
      }
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void removeNode(ITreeNode node) {
    ITreeNode parent = node.getParentNode();
    ITreeNode child = node;
    removeChildNode(parent, child);
  }

  @Override
  public void removeChildNode(ITreeNode parent, ITreeNode child) {
    removeChildNodes(parent, CollectionUtility.hashSet(child));
  }

  @Override
  public void removeChildNodes(ITreeNode parent, Collection<? extends ITreeNode> children) {
    if (!CollectionUtility.hasElements(children)) {
      return;
    }
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      if (parent == null) {
        return;
      }
      children = resolveNodes(children);
      deselectNodes(children);
      ((AbstractTreeNode) parent).removeChildNodesInternal(children, true);
      decorateAffectedNodeCells(parent, parent.getChildNodes());
      if (!isAutoDiscardOnDelete()) {
        for (ITreeNode child : children) {
          if (child.getStatus() == ITreeNode.STATUS_INSERTED) {
            // it was new and now it is gone, no further action required
          }
          else {
            child.setStatusInternal(ITableRow.STATUS_DELETED);
            m_deletedNodes.put(child.getPrimaryKey(), child);
          }
        }
      }
      // filter
      int level = 0;
      ITreeNode tmp = parent;
      while (tmp != null) {
        tmp = tmp.getParentNode();
        level++;
      }
      for (ITreeNode child : parent.getChildNodes()) {
        applyNodeFiltersRecInternal(child, parent.isFilterAccepted(), level);
      }
      fireNodesDeleted(parent, children);
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void removeAllChildNodes(ITreeNode parent) {
    if (parent != null) {
      removeChildNodes(parent, parent.getChildNodes());
    }
  }

  @Override
  public void clearDeletedNodes() {
    for (Iterator<ITreeNode> it = m_deletedNodes.values().iterator(); it.hasNext();) {
      (it.next()).setTreeInternal(null, true);
    }
    m_deletedNodes.clear();
  }

  @Override
  public Set<ITreeNode> resolveVirtualNodes(Collection<? extends ITreeNode> nodes) throws ProcessingException {
    if (!CollectionUtility.hasElements(nodes)) {
      return CollectionUtility.hashSet();
    }
    try {
      setTreeChanging(true);
      Set<ITreeNode> resolvedNodes = new HashSet<ITreeNode>(nodes.size());
      for (ITreeNode node : nodes) {
        ITreeNode resolvedNode = resolveVirtualNode(node);
        if (resolvedNode != null) {
          resolvedNodes.add(resolvedNode);
        }
      }
      return resolvedNodes;
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public ITreeNode resolveVirtualNode(ITreeNode node) throws ProcessingException {
    if (node instanceof IVirtualTreeNode) {
      IVirtualTreeNode vnode = (IVirtualTreeNode) node;
      if (vnode.getResolvedNode() != null && vnode.getResolvedNode().getTree() == this) {
        return vnode.getResolvedNode();
      }
      if (vnode.getTree() != this) {
        return null;
      }
      ITreeNode parentNode = vnode.getParentNode();
      if (parentNode == null) {
        return null;
      }
      try {
        setTreeChanging(true);
        //
        ITreeNode resolvedNode = parentNode.resolveVirtualChildNode(vnode);
        if (resolvedNode != vnode && vnode.getResolvedNode() == null) {
          vnode.setResolvedNode(resolvedNode);
        }
        return resolvedNode;
      }
      finally {
        setTreeChanging(false);
      }
    }
    return node;
  }

  @Override
  public boolean visitTree(ITreeVisitor v) {
    return visitNodeRec(getRootNode(), v);
  }

  @Override
  public boolean visitNode(ITreeNode node, ITreeVisitor v) {
    return visitNodeRec(node, v);
  }

  private boolean visitNodeRec(ITreeNode node, ITreeVisitor v) {
    if (node == null) {
      return true;
    }
    boolean b = v.visit(node);
    if (!b) {
      return b;
    }
    List<ITreeNode> a = node.getChildNodes();
    for (ITreeNode childNode : a) {
      // it might be that the visit of a node detached the node from the tree
      if (childNode.getTree() != null) {
        b = visitNodeRec(childNode, v);
        if (!b) {
          return b;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visitVisibleTree(ITreeVisitor v) {
    return visitVisibleNodeRec(getRootNode(), v, isRootNodeVisible());
  }

  private boolean visitVisibleNodeRec(ITreeNode node, ITreeVisitor v, boolean includeParent) {
    if (node.isVisible()) {
      if (includeParent) {
        boolean b = v.visit(node);
        if (!b) {
          return b;
        }
      }
      if (node.isExpanded()) {
        List<ITreeNode> a = node.getFilteredChildNodes();
        for (ITreeNode filteredChildNode : a) {
          // it might be that the visit of a node detached the node from the
          // tree
          if (filteredChildNode.getTree() != null) {
            boolean b = visitVisibleNodeRec(filteredChildNode, v, true);
            if (!b) {
              return b;
            }
          }
        }

      }
    }
    return true;
  }

  @Override
  public int getDeletedNodeCount() {
    return m_deletedNodes.size();
  }

  @Override
  public Set<ITreeNode> getDeletedNodes() {
    return CollectionUtility.hashSet(m_deletedNodes.values());
  }

  @Override
  public int getInsertedNodeCount() {
    P_AbstractCountingTreeVisitor v = new P_AbstractCountingTreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isStatusInserted()) {
          addCount(1);
        }
        return true;
      }
    };
    visitNode(getRootNode(), v);
    return v.getCount();
  }

  @Override
  public Set<ITreeNode> getInsertedNodes() {
    P_AbstractCollectingTreeVisitor v = new P_AbstractCollectingTreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isStatusInserted()) {
          addNodeToList(node);
        }
        return true;
      }
    };
    visitNode(getRootNode(), v);
    return CollectionUtility.hashSet(v.getNodes());
  }

  @Override
  public int getUpdatedNodeCount() {
    P_AbstractCountingTreeVisitor v = new P_AbstractCountingTreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isStatusUpdated()) {
          addCount(1);
        }
        return true;
      }
    };
    visitNode(getRootNode(), v);
    return v.getCount();
  }

  @Override
  public Set<ITreeNode> getUpdatedNodes() {
    P_AbstractCollectingTreeVisitor v = new P_AbstractCollectingTreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isStatusUpdated()) {
          addNodeToList(node);
        }
        return true;
      }
    };
    visitNode(getRootNode(), v);
    return CollectionUtility.hashSet(v.getNodes());
  }

  @Override
  public int getSelectedNodeCount() {
    return m_selectedNodes.size();
  }

  @Override
  public ITreeNode getSelectedNode() {
    if (m_selectedNodes.size() > 0) {
      return m_selectedNodes.iterator().next();
    }
    else {
      return null;
    }
  }

  @Override
  public Set<ITreeNode> getSelectedNodes() {
    return CollectionUtility.hashSet(m_selectedNodes);
  }

  @Override
  public boolean isSelectedNode(ITreeNode node) {
    node = resolveNode(node);
    if (node != null) {
      return m_selectedNodes.contains(node);
    }
    else {
      return false;
    }
  }

  @Override
  public void selectNode(ITreeNode node) {
    selectNode(node, false);
  }

  @Override
  public void selectNode(ITreeNode node, boolean append) {
    if (node != null) {
      selectNodes(CollectionUtility.hashSet(node), append);
    }
    else {
      selectNodes(null, append);
    }
  }

  @Override
  public void selectNodes(Collection<? extends ITreeNode> nodes, boolean append) {
    nodes = resolveNodes(nodes);
    try {
      nodes = resolveVirtualNodes(nodes);
    }
    catch (ProcessingException e) {
      LOG.warn("could not resolve virtual nodes.", e);
    }
    if (nodes == null) {
      nodes = CollectionUtility.hashSet();
    }
    HashSet<ITreeNode> newSelection = new HashSet<ITreeNode>();
    if (append) {
      newSelection.addAll(m_selectedNodes);
      newSelection.addAll(nodes);
    }
    else {
      newSelection.addAll(nodes);
    }
    // check selection count with multiselect
    if (newSelection.size() > 1 && !isMultiSelect()) {
      ITreeNode first = newSelection.iterator().next();
      newSelection.clear();
      newSelection.add(first);
    }
    if (m_selectedNodes.equals(newSelection) && m_selectedNodes.containsAll(nodes)) {
      // ok
    }
    else {
      Set<ITreeNode> oldSelection = m_selectedNodes;
      fireBeforeNodesSelected(oldSelection, newSelection);
      m_selectedNodes = newSelection;
      fireNodesSelected(oldSelection, m_selectedNodes);
    }
  }

  @Override
  public void selectNextNode() {
    final ITreeNode current = getSelectedNode();
    if (current != null) {
      final Holder<ITreeNode> foundVisited = new Holder<ITreeNode>(ITreeNode.class);
      ITreeVisitor v = new ITreeVisitor() {
        boolean foundCurrent;

        @Override
        public boolean visit(ITreeNode node) {
          if (foundCurrent) {
            if (node.isFilterAccepted()) {
              foundVisited.setValue(node);
            }
            return foundVisited.getValue() == null;
          }
          else {
            if (node == current) {
              foundCurrent = true;
            }
            return true;
          }
        }
      };
      visitVisibleTree(v);
      if (foundVisited.getValue() != null) {
        selectNode(foundVisited.getValue());
      }
    }
    else {
      selectFirstNode();
    }
  }

  @Override
  public void selectPreviousNode() {
    final ITreeNode current = getSelectedNode();
    if (current != null) {
      final Holder<ITreeNode> foundVisited = new Holder<ITreeNode>(ITreeNode.class);
      ITreeVisitor v = new ITreeVisitor() {
        boolean foundCurrent;

        @Override
        public boolean visit(ITreeNode node) {
          if (foundCurrent) {
            return false;
          }
          if (node == current) {
            foundCurrent = true;
          }
          else if (node.isFilterAccepted()) {
            foundVisited.setValue(node);
          }
          return true;
        }
      };
      visitVisibleTree(v);
      if (foundVisited.getValue() != null) {
        selectNode(foundVisited.getValue());
      }
    }
    else {
      selectLastNode();
    }
  }

  @Override
  public void selectFirstNode() {
    if (!isRootNodeVisible()) {
      try {
        getRootNode().ensureChildrenLoaded();
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
    final Holder<ITreeNode> foundVisited = new Holder<ITreeNode>(ITreeNode.class);
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (foundVisited.getValue() != null) {
          return false;
        }
        if (node.isFilterAccepted()) {
          foundVisited.setValue(node);
        }
        return true;
      }
    };
    visitVisibleTree(v);
    if (foundVisited.getValue() != null) {
      selectNode(foundVisited.getValue());
    }
  }

  @Override
  public void selectLastNode() {
    if (!isRootNodeVisible()) {
      try {
        getRootNode().ensureChildrenLoaded();
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
    final Holder<ITreeNode> foundVisited = new Holder<ITreeNode>(ITreeNode.class);
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isFilterAccepted()) {
          foundVisited.setValue(node);
        }
        return true;
      }
    };
    visitVisibleTree(v);
    if (foundVisited.getValue() != null) {
      selectNode(foundVisited.getValue());
    }
  }

  @Override
  public void selectNextChildNode() {
    ITreeNode current = getSelectedNode();
    if (current != null) {
      current.setExpanded(true);
    }
    selectNextNode();
  }

  @Override
  public void selectPreviousParentNode() {
    ITreeNode n = getSelectedNode();
    if (n != null) {
      ITreeNode parent = n.getParentNode();
      while (parent != null) {
        if (parent != getRootNode() || isRootNodeVisible()) {
          if (parent.isFilterAccepted()) {
            selectNode(parent);
            return;
          }
        }
        //
        parent = parent.getParentNode();
      }
    }
    else {
      selectFirstNode();
    }
  }

  @Override
  public void deselectNode(ITreeNode node) {
    if (node != null) {
      deselectNodes(CollectionUtility.hashSet(node));
    }
    else {
      deselectNodes(null);
    }
  }

  @Override
  public void deselectNodes(Collection<? extends ITreeNode> nodes) {
    nodes = resolveNodes(nodes);
    if (CollectionUtility.hasElements(nodes)) {
      Set<ITreeNode> oldSelection = new HashSet<ITreeNode>(m_selectedNodes);
      Set<ITreeNode> newSelection = new HashSet<ITreeNode>();
      if (m_selectedNodes != null) {
        for (ITreeNode selChild : m_selectedNodes) {
          boolean accept = true;
          for (ITreeNode delParent : nodes) {
            if (isAncestorNodeOf(delParent, selChild)) {
              accept = false;
              break;
            }
          }
          if (accept) {
            newSelection.add(selChild);
          }
        }
      }
      if (oldSelection.size() != newSelection.size()) {
        fireBeforeNodesSelected(oldSelection, newSelection);
        m_selectedNodes = newSelection;
        fireNodesSelected(oldSelection, m_selectedNodes);
      }
    }
  }

  @Override
  public Set<ITreeNode> getCheckedNodes() {
    final List<ITreeNode> list = new ArrayList<ITreeNode>();
    visitTree(new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isChecked()) {
          list.add(node);
        }
        return true;
      }
    });
    return CollectionUtility.hashSet(list);
  }

  @Override
  public boolean isScrollToSelection() {
    return propertySupport.getPropertyBool(PROP_SCROLL_TO_SELECTION);
  }

  @Override
  public void setScrollToSelection(boolean b) {
    propertySupport.setPropertyBool(PROP_SCROLL_TO_SELECTION, b);
  }

  @Override
  public void scrollToSelection() {
    fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_SCROLL_TO_SELECTION));
  }

  private ITreeNode resolveNode(ITreeNode node) {
    if (node instanceof IVirtualTreeNode && ((IVirtualTreeNode) node).getResolvedNode() != null) {
      node = ((IVirtualTreeNode) node).getResolvedNode();
    }
    // unwrapping
    if (node == null) {
      return null;
    }
    else if (node.getTree() == this) {
      return node;
    }
    else {
      return null;
    }
  }

  /**
   * keeps order of input
   * 
   * @param nodes
   * @return
   */
  private List<ITreeNode> resolveNodes(Collection<? extends ITreeNode> nodes) {
    if (!CollectionUtility.hasElements(nodes)) {
      return CollectionUtility.emptyArrayList();
    }
    List<ITreeNode> resolvedNodes = new ArrayList<ITreeNode>(nodes.size());
    for (ITreeNode node : nodes) {
      if (resolveNode(node) != null) {
        resolvedNodes.add(node);
      }
    }
    return resolvedNodes;
  }

  /*
   * Tree Observer
   */
  @Override
  public void addTreeListener(TreeListener listener) {
    m_listenerList.add(TreeListener.class, listener);
  }

  @Override
  public void removeTreeListener(TreeListener listener) {
    m_listenerList.remove(TreeListener.class, listener);
  }

  @Override
  public void addUITreeListener(TreeListener listener) {
    m_listenerList.insertAtFront(TreeListener.class, listener);
  }

  protected IEventHistory<TreeEvent> createEventHistory() {
    return new DefaultTreeEventHistory(5000L);
  }

  @Override
  public IEventHistory<TreeEvent> getEventHistory() {
    return m_eventHistory;
  }

  private void fireNodesInserted(ITreeNode parent, List<ITreeNode> children) {
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_INSERTED, parent, children));
    }
  }

  private void fireNodesUpdated(ITreeNode parent, Collection<ITreeNode> children) {
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_UPDATED, parent, children));
    }
  }

  @Override
  public void fireNodeChanged(ITreeNode node) {
    fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_CHANGED, node));
  }

  private void fireNodeFilterChanged() {
    fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_FILTER_CHANGED, getRootNode()));
  }

  private void fireNodesDeleted(ITreeNode parent, Collection<? extends ITreeNode> children) {
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_DELETED, parent, children));
    }
  }

  private void fireChildNodeOrderChanged(ITreeNode parent, List<? extends ITreeNode> children) {
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, parent, children));
    }
  }

  private void fireBeforeNodesSelected(Set<ITreeNode> oldSelection, Set<ITreeNode> newSelection) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_BEFORE_NODES_SELECTED, newSelection);
    HashSet<ITreeNode> deselectedNodes = new HashSet<ITreeNode>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes);
    Set<ITreeNode> newSelectedNodes = new HashSet<ITreeNode>(newSelection);
    newSelectedNodes.removeAll(oldSelection);
    e.setNewSelectedNodes(newSelectedNodes);
    fireTreeEventInternal(e);
  }

  private void fireNodesSelected(Set<ITreeNode> oldSelection, Set<ITreeNode> newSelection) {
    // single observer: rebuild title
    if (isAutoTitle()) {
      rebuildTitleInternal();
    }
    // fire
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODES_SELECTED, newSelection);
    HashSet<ITreeNode> deselectedNodes = new HashSet<ITreeNode>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes);
    HashSet<ITreeNode> newSelectedNodes = new HashSet<ITreeNode>(newSelection);
    newSelectedNodes.removeAll(oldSelection);
    e.setNewSelectedNodes(newSelectedNodes);
    //single observer
    try {
      execNodesSelected(e);
    }
    catch (ProcessingException ex) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
    //end single observer
    fireTreeEventInternal(e);
  }

  private void fireNodeExpanded(ITreeNode node, boolean b) {
    if (node != null) {
      if (b) {
        fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_EXPANDED, node));
      }
      else {
        fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_COLLAPSED, node));
      }
    }
  }

  @Override
  public List<IMenu> fetchMenusForNodesInternal(Collection<? extends ITreeNode> nodes) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_POPUP, nodes);
    // single observer for tree-owned menus
    addLocalPopupMenus(e);
    fireTreeEventInternal(e);
    //separate node menus and empty space actions
    List<IMenu> popupMenus = e.getPopupMenus();
    ArrayList<IMenu> nodeMenus = new ArrayList<IMenu>(popupMenus.size());
    ArrayList<IMenu> emptySpaceMenus = new ArrayList<IMenu>();
    for (IMenu menu : popupMenus) {
      if (menu.isVisible()) {
        if (menu.isEmptySpaceAction()) {
          emptySpaceMenus.add(menu);
        }
        else {
          nodeMenus.add(menu);
        }
      }
    }
    if (nodeMenus.size() > 0 && emptySpaceMenus.size() > 0) {
      nodeMenus.add(0, new MenuSeparator());
    }
    nodeMenus.addAll(0, emptySpaceMenus);
    return nodeMenus;
  }

  private void fireNodeClick(ITreeNode node) {
    if (node != null) {
      try {
        interceptNodeClickSingleObserver(node);
        execNodeClick(node);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }
  }

  protected void interceptNodeClickSingleObserver(ITreeNode node) {
    if (isCheckable() && node.isEnabled() && isEnabled()) {
      node.setChecked(!node.isChecked());
    }
  }

  private void fireNodeAction(ITreeNode node) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (node != null) {
          if (node.isLeaf()) {
            try {
              execNodeAction(node);
            }
            catch (ProcessingException ex) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
            }
            catch (Throwable t) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
            }
          }
        }
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  private void fireRequestFocus() {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_REQUEST_FOCUS);
    fireTreeEventInternal(e);
  }

  private void addLocalPopupMenus(TreeEvent e) {
    int selectionCount = e.getNodes().size();
    ArrayList<IMenu> list = new ArrayList<IMenu>();
    for (IMenu m : this.getMenus()) {
      if ((!m.isInheritAccessibility()) || (isEnabled())) {
        m.prepareAction();
        if (m.isVisible()) {
          list.add(m);
        }
      }
    }
    if (e.getNode() != null) {
      for (IMenu m : e.getNode().getMenus()) {
        if ((!m.isInheritAccessibility()) || (e.getNode().isEnabled() && isEnabled())) {
          m.prepareAction();
          if (m.isVisible()) {
            list.add(m);
          }
        }
      }
    }
    //check single/multi select
    for (IMenu menu : list) {
      if (selectionCount > 1 && menu.isMultiSelectionAction()) {
        e.addPopupMenu(menu);
      }
      else if (selectionCount == 1 && menu.isSingleSelectionAction()) {
        e.addPopupMenu(menu);
      }
      else if (selectionCount == 0 && menu.isEmptySpaceAction()) {
        e.addPopupMenu(menu);
      }
    }
  }

  private TransferObject fireNodesDragRequest(Collection<ITreeNode> nodes) {
    if (CollectionUtility.hasElements(nodes)) {
      TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODES_DRAG_REQUEST, nodes);
      fireTreeEventInternal(e);
      return e.getDragObject();
    }
    else {
      return null;
    }
  }

  private void fireNodeDropAction(ITreeNode node, TransferObject dropData) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_DROP_ACTION, node);
    e.setDropObject(dropData);
    fireTreeEventInternal(e);
  }

  /**
   * This method gets called when the drop node is changed, e.g. the dragged object
   * is moved over a new drop target.
   * 
   * @since 4.0-M7
   */
  public void fireNodeDropTargetChanged(ITreeNode node) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED, node);
    fireTreeEventInternal(e);
  }

  /**
   * This method gets called after the drag action has been finished.
   * 
   * @since 4.0-M7
   */
  public void fireDragFinished() {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_DRAG_FINISHED);
    fireTreeEventInternal(e);
  }

  private void fireNodeRequestFocus(ITreeNode node) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_REQUEST_FOCUS, node);
    fireTreeEventInternal(e);
  }

  private void fireNodeEnsureVisible(ITreeNode node) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_ENSURE_VISIBLE, node);
    fireTreeEventInternal(e);
  }

  // main handler
  protected void fireTreeEventInternal(TreeEvent e) {
    if (isTreeChanging()) {
      // buffer the event for later batch firing
      m_treeEventBuffer.add(e);
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(TreeListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          try {
            ((TreeListener) listeners[i]).treeChanged(e);
          }
          catch (Throwable t) {
            LOG.error("fire " + e, t);
          }
        }
      }
    }
  }

  // batch handler
  private void fireTreeEventBatchInternal(List<? extends TreeEvent> batch) {
    if (CollectionUtility.hasElements(batch)) {
      EventListener[] listeners = m_listenerList.getListeners(TreeListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          ((TreeListener) listeners[i]).treeChangedBatch(batch);
        }
      }
    }
  }

  /**
   * add cells on the path to root and on children to decoration buffer
   */
  private void decorateAffectedNodeCells(ITreeNode parent, Collection<ITreeNode> children) {
    decorateAffectedNodeCellsOnPathToRoot(parent);
    for (ITreeNode child : children) {
      decorateAffectedNodeCellsOnSubtree(child);
    }
  }

  private void decorateAffectedNodeCellsOnPathToRoot(ITreeNode node) {
    ITreeNode tmp = node;
    while (tmp != null) {
      m_nodeDecorationBuffer.add(tmp);
      tmp = tmp.getParentNode();
    }
  }

  private void decorateAffectedNodeCellsOnSubtree(ITreeNode node) {
    m_nodeDecorationBuffer.add(node);
    for (ITreeNode child : node.getChildNodes()) {
      decorateAffectedNodeCellsOnSubtree(child);
    }
  }

  private int m_processChangeBufferLoopDetection;

  /**
   * affects columns with lookup calls or code types<br>
   * cells that have changed values fetch new texts/decorations from the lookup
   * service in one single batch call lookup (performance optimization)
   */
  private void processChangeBuffer() {
    //loop detection
    try {
      m_processChangeBufferLoopDetection++;
      if (m_processChangeBufferLoopDetection > 100) {
        LOG.error("LOOP DETECTION in " + getClass() + ". see stack trace for more details.", new Exception("LOOP DETECTION"));
        return;
      }
      //
      /*
       * update row decorations
       */
      if (m_nodeDecorationBuffer.size() > 0) {
        Set<ITreeNode> set = m_nodeDecorationBuffer;
        m_nodeDecorationBuffer = new HashSet<ITreeNode>();
        for (Iterator<ITreeNode> it = set.iterator(); it.hasNext();) {
          ITreeNode node = it.next();
          if (node.getTree() != null) {
            try {
              execDecorateCell(node, node.getCellForUpdate());
            }
            catch (Throwable t) {
              LOG.warn("node " + node.getClass() + " " + node.getCell().getText(), t);
            }
          }
        }
      }
      /*
       * fire events tree changes are finished now, fire all buffered events
       * and call lookups
       */
      if (m_treeEventBuffer.size() > 0) {
        List<TreeEvent> list = m_treeEventBuffer;
        m_treeEventBuffer = new ArrayList<TreeEvent>();
        // coalesce selection events
        boolean foundSelectionEvent = false;
        for (ListIterator<TreeEvent> it = list.listIterator(list.size()); it.hasPrevious();) {
          if (it.previous().getType() == TreeEvent.TYPE_NODES_SELECTED) {
            if (!foundSelectionEvent) {
              foundSelectionEvent = true;
            }
            else {
              it.remove();
            }
          }
        }
        // fire the batch and set tree to changing, otherwise a listener might trigger another events that then are processed before all other listeners received that batch
        try {
          setTreeChanging(true);
          //
          fireTreeEventBatchInternal(list);
        }
        finally {
          setTreeChanging(false);
        }
      }
    }
    finally {
      m_processChangeBufferLoopDetection--;
    }
  }

  @Override
  public boolean isMultiSelect() {
    return propertySupport.getPropertyBool(PROP_MULTI_SELECT);
  }

  @Override
  public void setMultiSelect(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_SELECT, b);
  }

  @Override
  public boolean isMultiCheck() {
    return propertySupport.getPropertyBool(PROP_MULTI_CHECK);
  }

  @Override
  public void setMultiCheck(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_CHECK, b);
  }

  @Override
  public void unloadNode(ITreeNode node) throws ProcessingException {
    try {
      setTreeChanging(true);
      //
      setNodeExpanded(node, false);
      removeAllChildNodes(node);
      node.setChildrenLoaded(false);
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void doHyperlinkAction(ITreeNode node, URL url) throws ProcessingException {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (node != null) {
          selectNode(node);
          execHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals("local"));
        }
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  @Override
  public void exportTreeData(final AbstractTreeFieldData target) throws ProcessingException {
    exportTreeNodeDataRec(getRootNode().getChildNodes(), target, null);
  }

  private void exportTreeNodeDataRec(List<ITreeNode> nodes, AbstractTreeFieldData treeData, TreeNodeData parentNodeData) throws ProcessingException {
    ArrayList<TreeNodeData> nodeDataList = new ArrayList<TreeNodeData>();
    for (ITreeNode node : nodes) {
      TreeNodeData nodeData = exportTreeNodeData(node, treeData);
      if (nodeData != null) {
        exportTreeNodeDataRec(node.getChildNodes(), treeData, nodeData);
        nodeDataList.add(nodeData);
      }
    }
    if (parentNodeData != null) {
      parentNodeData.setChildNodes(nodeDataList);
    }
    else {
      treeData.setRoots(nodeDataList);
    }
  }

  /**
   * @return a node data for this tree node or null to skip this node
   */
  protected TreeNodeData exportTreeNodeData(ITreeNode node, AbstractTreeFieldData treeData) throws ProcessingException {
    TreeNodeData nodeData = new TreeNodeData();
    return nodeData;
  }

  @Override
  public void importTreeData(AbstractTreeFieldData source) throws ProcessingException {
    if (source.isValueSet()) {
      try {
        setTreeChanging(true);
        //
        removeAllChildNodes(getRootNode());
        importTreeNodeDataRec(getRootNode(), source, source.getRoots());
      }
      finally {
        setTreeChanging(false);
      }
    }
  }

  private void importTreeNodeDataRec(ITreeNode parentNode, AbstractTreeFieldData treeData, List<TreeNodeData> nodeDataList) throws ProcessingException {
    if (nodeDataList != null) {
      for (TreeNodeData nodeData : nodeDataList) {
        ITreeNode node = importTreeNodeData(parentNode, treeData, nodeData);
        if (node != null) {
          importTreeNodeDataRec(node, treeData, nodeData.getChildNodes());
        }
      }
    }
  }

  /**
   * @return the new tree node for this node data or null to skip this node
   *         It is the responsibility of this method to add the new nopde to the tree.
   */
  protected ITreeNode importTreeNodeData(ITreeNode parentNode, AbstractTreeFieldData treeData, TreeNodeData nodeData) throws ProcessingException {
    return null;
  }

  @Override
  public ITreeUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public boolean isSaveAndRestoreScrollbars() {
    return m_saveAndRestoreScrollbars;
  }

  @Override
  public void setSaveAndRestoreScrollbars(boolean b) {
    m_saveAndRestoreScrollbars = b;
  }

  private abstract class P_AbstractCollectingTreeVisitor implements ITreeVisitor {
    private final List<ITreeNode> m_list = new ArrayList<ITreeNode>();

    protected void addNodeToList(ITreeNode node) {
      m_list.add(node);
    }

    public List<ITreeNode> getNodes() {
      return CollectionUtility.arrayList(m_list);
    }
  }// end private class

  private abstract class P_AbstractCountingTreeVisitor implements ITreeVisitor {
    private int m_count;

    protected void addCount(int n) {
      m_count += n;
    }

    public int getCount() {
      return m_count;
    }
  }// end private class

  /*
   * UI Notifications
   */
  private class P_UIFacade implements ITreeUIFacade {
    private int m_uiProcessorCount = 0;

    protected void pushUIProcessor() {
      m_uiProcessorCount++;
    }

    protected void popUIProcessor() {
      m_uiProcessorCount--;
    }

    @Override
    public boolean isUIProcessing() {
      return m_uiProcessorCount > 0;
    }

    @Override
    public void setNodeExpandedFromUI(ITreeNode node, boolean on) {
      try {
        pushUIProcessor();
        try {
          setTreeChanging(true);
          //
          node = resolveNode(node);
          node = resolveVirtualNode(node);
          if (node != null) {
            if (node.isExpanded() != on) {
              if (on) {
                if (node.isChildrenDirty() || node.isChildrenVolatile()) {
                  node.loadChildren();
                }
              }
              setNodeExpanded(node, on);
            }
          }
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (ProcessingException se) {
        se.addContextMessage(node.getCell().getText());
        SERVICES.getService(IExceptionHandlerService.class).handleException(se);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setNodeSelectedAndExpandedFromUI(ITreeNode node) {
      try {
        pushUIProcessor();
        //
        try {
          setTreeChanging(true);
          node = resolveNode(node);
          node = resolveVirtualNode(node);
          if (node != null) {
            if (node.isChildrenDirty() || node.isChildrenVolatile()) {
              node.loadChildren();
            }
            setNodeExpanded(node, true);
            selectNode(node, false);
            if (!isScrollToSelection()) {
              scrollToSelection();
            }
          }
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (ProcessingException se) {
        se.addContextMessage(node.getCell().getText());
        SERVICES.getService(IExceptionHandlerService.class).handleException(se);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setNodesSelectedFromUI(List<ITreeNode> nodes) {
      try {
        pushUIProcessor();
        try {
          setTreeChanging(true);

          Set<ITreeNode> validNodes = resolveVirtualNodes(resolveNodes(nodes));

          // remove filtered (invisible) nodes from selection
          Iterator<ITreeNode> iterator = validNodes.iterator();
          while (iterator.hasNext()) {
            if (!iterator.next().isFilterAccepted()) {
              iterator.remove();
            }
          }

          // load children for selection
          for (ITreeNode node : validNodes) {
            if (node.isChildrenLoaded()) {
              if (node.isChildrenDirty() || node.isChildrenVolatile()) {
                node.loadChildren();
              }
            }
          }

          selectNodes(validNodes, false);
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (ProcessingException se) {
        se.addContextMessage(nodes.toString());
        SERVICES.getService(IExceptionHandlerService.class).handleException(se);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public List<IMenu> fireNodePopupFromUI() {
      try {
        pushUIProcessor();
        Collection<ITreeNode> nodes = resolveVirtualNodes(getSelectedNodes());
        return fetchMenusForNodesInternal(nodes);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        return CollectionUtility.emptyArrayList();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public List<IMenu> fireEmptySpacePopupFromUI() {
      try {
        pushUIProcessor();
        Set<ITreeNode> emptySet = CollectionUtility.hashSet();
        return fetchMenusForNodesInternal(emptySet);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireNodeClickFromUI(ITreeNode node) {
      try {
        pushUIProcessor();
        node = resolveNode(node);
        node = resolveVirtualNode(node);
        if (node != null) {
          fireNodeClick(node);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireNodeActionFromUI(ITreeNode node) {
      try {
        pushUIProcessor();
        //
        node = resolveNode(node);
        node = resolveVirtualNode(node);
        if (node != null) {
          fireNodeAction(node);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      finally {
        popUIProcessor();
      }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean getNodesDragEnabledFromUI() {
      return isDragEnabled();
    }

    @Override
    public TransferObject fireNodesDragRequestFromUI() {
      try {
        pushUIProcessor();
        //
        Collection<ITreeNode> nodes = resolveVirtualNodes(getSelectedNodes());
        return fireNodesDragRequest(nodes);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        return null;
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireDragFinishedFromUI() {
      try {
        pushUIProcessor();
        fireDragFinished();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireNodeDropTargetChangedFromUI(ITreeNode node) {
      try {
        pushUIProcessor();
        //
        node = resolveNode(node);
        node = resolveVirtualNode(node);
        if (node != null) {
          fireNodeDropTargetChanged(node);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireNodeDropActionFromUI(ITreeNode node, TransferObject dropData) {
      try {
        pushUIProcessor();
        //
        node = resolveNode(node);
        node = resolveVirtualNode(node);
        if (node != null) {
          fireNodeDropAction(node, dropData);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireHyperlinkActionFromUI(ITreeNode node, URL url) {
      try {
        pushUIProcessor();
        //
        node = resolveNode(node);
        node = resolveVirtualNode(node);
        if (node != null) {
          doHyperlinkAction(node, url);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      finally {
        popUIProcessor();
      }
    }

  }// end private class

}
