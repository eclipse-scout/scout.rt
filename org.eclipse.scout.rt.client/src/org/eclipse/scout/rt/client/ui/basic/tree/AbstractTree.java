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
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
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
  private IMenu[] m_menus;

  // enabled is defined as: enabledGranted && enabledProperty && enabledSlave
  private boolean m_enabledGranted;
  private boolean m_enabledProperty;

  private ITreeNode m_rootNode;
  private int m_treeChanging;
  private boolean m_autoDiscardOnDelete;
  private boolean m_autoTitle;
  private final HashMap<Object, ITreeNode> m_deletedNodes;
  private ArrayList<TreeEvent> m_treeEventBuffer = new ArrayList<TreeEvent>();
  private HashSet<ITreeNode> m_nodeDecorationBuffer = new HashSet<ITreeNode>();
  private HashSet<ITreeNode> m_selectedNodes = new HashSet<ITreeNode>();
  private final ArrayList<ITreeNodeFilter> m_nodeFilters;
  private final int m_uiProcessorCount = 0;
  private IKeyStroke[] m_baseKeyStrokes;
  private IEventHistory<TreeEvent> m_eventHistory;

  public AbstractTree() {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTree(this);
    }
    m_deletedNodes = new HashMap<Object, ITreeNode>();
    m_nodeFilters = new ArrayList<ITreeNodeFilter>(1);
    initConfig();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAutoTitle() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMultiSelect() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(42)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredMultiCheck() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(45)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredCheckable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredDragEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(51)
  @ConfigPropertyValue("0")
  protected int getConfiguredDragType() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(52)
  @ConfigPropertyValue("0")
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
  @ConfigPropertyValue("true")
  protected boolean getConfiguredAutoDiscardOnDelete() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredRootNodeVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(71)
  @ConfigPropertyValue("true")
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
  @ConfigPropertyValue("false")
  protected boolean getConfiguredScrollToSelection() {
    return false;
  }

  private Class<? extends IKeyStroke>[] getConfiguredKeyStrokes() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
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
  protected TransferObject execDrag(ITreeNode[] nodes) throws ProcessingException {
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
    setMultiCheck(getConfiguredMultiCheck());
    setMultiSelect(getConfiguredMultiSelect());
    setAutoDiscardOnDelete(getConfiguredAutoDiscardOnDelete());
    setDragEnabled(getConfiguredDragEnabled());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setRootNodeVisible(getConfiguredRootNodeVisible());
    setRootHandlesVisible(getConfiguredRootHandlesVisible());
    setScrollToSelection(getConfiguredScrollToSelection());
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
        }
      }
    });
    // key shortcuts
    ArrayList<IKeyStroke> ksList = new ArrayList<IKeyStroke>();
    Class<? extends IKeyStroke>[] shortcutArray = getConfiguredKeyStrokes();
    for (int i = 0; i < shortcutArray.length; i++) {
      IKeyStroke ks;
      try {
        ks = ConfigurationUtility.newInnerInstance(this, shortcutArray[i]);
        ksList.add(ks);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("keyStroke: " + shortcutArray[i].getName(), t));
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
    m_baseKeyStrokes = ksList.toArray(new IKeyStroke[ksList.size()]);
    setKeyStrokesInternal(m_baseKeyStrokes);
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] ma = getConfiguredMenus();
    for (int i = 0; i < ma.length; i++) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, ma[i]);
        menuList.add(menu);
      }
      catch (Exception e) {
        LOG.error("Exception occured while creating a new instance of " + ma[i].getName(), e);
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("Error occured while dynamically contributing menus.", e);
    }
    m_menus = menuList.toArray(new IMenu[0]);
  }

  /*
   * Runtime
   */
  @Override
  public final void initTree() throws ProcessingException {
    initTreeInternal();
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
  public IMenu[] getMenus() {
    return m_menus;
  }

  @Override
  public void setMenus(IMenu[] a) {
    m_menus = a;
  }

  @Override
  public <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException {
    return new ActionFinder().findAction(getMenus(), menuType);
  }

  @Override
  public boolean hasNodeFilters() {
    return m_nodeFilters.size() > 0;
  }

  @Override
  public ITreeNodeFilter[] getNodeFilters() {
    return m_nodeFilters.toArray(new ITreeNodeFilter[m_nodeFilters.size()]);
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
    IMenu[] menus;
    try {
      ITreeNode[] nodes = resolveVirtualNodes(getSelectedNodes());
      menus = fetchMenusForNodesInternal(nodes);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      menus = new IMenu[0];
    }

    //Compute the Keystrokes: base + keyStroke for the current Menus.
    ArrayList<IKeyStroke> ksList = new ArrayList<IKeyStroke>(Arrays.asList(m_baseKeyStrokes));
    for (IMenu menu : menus) {
      if (menu.getKeyStroke() != null) {
        IKeyStroke ks = new KeyStroke(menu.getKeyStroke(), menu);
        ksList.add(ks);
      }
    }

    //Set KeyStrokes:
    setKeyStrokesInternal(ksList.toArray(new IKeyStroke[ksList.size()]));
  }

  @Override
  public ITreeNode findNode(Object primaryKey) {
    ITreeNode[] a = findNodes(new Object[]{primaryKey});
    if (a != null && a.length > 0) {
      return a[0];
    }
    else {
      return null;
    }
  }

  @Override
  public ITreeNode[] findNodes(Object[] primaryKeys) {
    if (primaryKeys == null || primaryKeys.length <= 0) {
      return new ITreeNode[0];
    }
    final HashSet<Object> keySet = new HashSet<Object>(Arrays.asList(primaryKeys));
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
        fireNodesUpdated(node.getParentNode(), new ITreeNode[]{node});
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
        fireNodesUpdated(node.getParentNode(), new ITreeNode[]{node});
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
        fireNodesUpdated(node.getParentNode(), new ITreeNode[]{node});
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
        fireNodesUpdated(node.getParentNode(), new ITreeNode[]{node});
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
        ArrayList<ITreeNode> changedNodes = new ArrayList<ITreeNode>();
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
        fireNodesUpdated(commonParent, changedNodes.toArray(new ITreeNode[changedNodes.size()]));
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
        fireNodesUpdated(node.getParentNode(), new ITreeNode[]{node});
      }
    }
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
      ITreeNode[] children = parent.getChildNodes();
      for (int i = 0; i < children.length; i++) {
        expandAllRec(children[i], level + 1);
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
        ITreeNode[] children = parent.getChildNodes();
        for (int i = 0; i < children.length; i++) {
          fetchAllCollapsingNodesRec(children[i], level + 1, list);
        }
      }
    }
  }

  @Override
  public IKeyStroke[] getKeyStrokes() {
    IKeyStroke[] keyStrokes = (IKeyStroke[]) propertySupport.getProperty(PROP_KEY_STROKES);
    if (keyStrokes == null) {
      keyStrokes = new IKeyStroke[0];
    }
    return keyStrokes;
  }

  @Override
  public void setKeyStrokes(IKeyStroke[] keyStrokes) {
    m_baseKeyStrokes = keyStrokes;
    rebuildKeyStrokesInternal();
  }

  private void setKeyStrokesInternal(IKeyStroke[] keyStrokes) {
    propertySupport.setProperty(PROP_KEY_STROKES, keyStrokes);
  }

  /*
   * modifications
   */
  @Override
  public void addChildNode(ITreeNode parent, ITreeNode child) {
    if (child != null) {
      addChildNodes(parent, new ITreeNode[]{child});
    }
  }

  @Override
  public void addChildNode(int startIndex, ITreeNode parent, ITreeNode child) {
    if (child != null) {
      addChildNodes(startIndex, parent, new ITreeNode[]{child});
    }
  }

  @Override
  public void addChildNodes(ITreeNode parent, ITreeNode[] children) {
    addChildNodes(parent.getChildNodeCount(), parent, children);
  }

  @Override
  public void addChildNodes(int startIndex, ITreeNode parent, ITreeNode[] children) {
    if (children == null || children.length == 0) {
      return;
    }
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      ((AbstractTreeNode) parent).addChildNodesInternal(startIndex, children, true);
      // check if all children were added, or if somem were revoked using
      // visible=false in init (addNotify) phase.
      int revokeCount = 0;
      for (ITreeNode child : children) {
        if (child.getParentNode() == null) {
          revokeCount++;
        }
      }
      if (revokeCount > 0) {
        ITreeNode[] newChildren = new ITreeNode[children.length - revokeCount];
        int index = 0;
        for (ITreeNode child : children) {
          if (child.getParentNode() != null) {
            newChildren[index++] = child;
          }
        }
        children = newChildren;
      }
      // decorate
      decorateAffectedNodeCells(parent, children);
      // filter
      int level = 0;
      ITreeNode tmp = parent;
      while (tmp != null) {
        tmp = tmp.getParentNode();
        level++;
      }
      for (ITreeNode child : children) {
        applyNodeFiltersRecInternal(child, parent.isFilterAccepted(), level);
      }
      fireNodesInserted(parent, children);
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void updateNode(ITreeNode node) {
    updateChildNodes(node.getParentNode(), new ITreeNode[]{node});
  }

  @Override
  public void updateChildNodes(ITreeNode parent, ITreeNode[] children) {
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      children = resolveNodes(children);
      decorateAffectedNodeCells(parent, children);
      fireNodesUpdated(parent, children);
    }
    finally {
      setTreeChanging(false);
    }
  }

  @Override
  public void updateChildNodeOrder(ITreeNode parent, ITreeNode[] newChildren) {
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      ITreeNode[] newChildrenResolved = resolveNodes(newChildren);
      if (newChildren.length > 0 && newChildrenResolved.length == newChildren.length) {
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
    removeChildNodes(parent, new ITreeNode[]{child});
  }

  @Override
  public void removeChildNodes(ITreeNode parent, ITreeNode[] children) {
    if (children == null || children.length == 0) {
      return;
    }
    try {
      setTreeChanging(true);
      //
      parent = resolveNode(parent);
      children = resolveNodes(children);
      deselectNodes(children);
      ((AbstractTreeNode) parent).removeChildNodesInternal(children, true);
      decorateAffectedNodeCells(parent, parent.getChildNodes());
      if (!isAutoDiscardOnDelete()) {
        for (int i = 0; i < children.length; i++) {
          if (children[i].getStatus() == ITreeNode.STATUS_INSERTED) {
            // it was new and now it is gone, no further action required
          }
          else {
            children[i].setStatusInternal(ITableRow.STATUS_DELETED);
            m_deletedNodes.put(children[i].getPrimaryKey(), children[i]);
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
  public ITreeNode[] resolveVirtualNodes(ITreeNode[] nodes) throws ProcessingException {
    if (nodes == null) {
      return new ITreeNode[0];
    }
    try {
      setTreeChanging(true);
      //
      ArrayList<ITreeNode> resolvedNodes = new ArrayList<ITreeNode>(nodes.length);
      for (int i = 0; i < nodes.length; i++) {
        ITreeNode resolvedNode = resolveVirtualNode(nodes[i]);
        if (resolvedNode != null) {
          resolvedNodes.add(resolvedNode);
        }
      }
      return resolvedNodes.toArray(new ITreeNode[resolvedNodes.size()]);
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
    ITreeNode[] a = node.getChildNodes();
    for (int i = 0; i < a.length; i++) {
      // it might be that the visit of a node detached the node from the tree
      if (a[i].getTree() != null) {
        b = visitNodeRec(a[i], v);
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
        ITreeNode[] a = node.getFilteredChildNodes();
        for (int i = 0; i < a.length; i++) {
          // it might be that the visit of a node detached the node from the
          // tree
          if (a[i].getTree() != null) {
            boolean b = visitVisibleNodeRec(a[i], v, true);
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
  public ITreeNode[] getDeletedNodes() {
    return m_deletedNodes.values().toArray(new ITreeNode[0]);
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
  public ITreeNode[] getInsertedNodes() {
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
    return v.getNodes();
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
  public ITreeNode[] getUpdatedNodes() {
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
    return v.getNodes();
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
  public ITreeNode[] getSelectedNodes() {
    return m_selectedNodes.toArray(new ITreeNode[0]);
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
      selectNodes(new ITreeNode[]{node}, append);
    }
    else {
      selectNodes(new ITreeNode[0], append);
    }
  }

  @Override
  public void selectNodes(ITreeNode[] nodes, boolean append) {
    nodes = resolveNodes(nodes);
    try {
      nodes = resolveVirtualNodes(nodes);
    }
    catch (ProcessingException e) {
      LOG.warn("could not resolve virtual nodes.", e);
    }
    if (nodes == null) {
      nodes = new ITreeNode[0];
    }
    HashSet<ITreeNode> newSelection = new HashSet<ITreeNode>();
    if (append) {
      newSelection.addAll(m_selectedNodes);
      newSelection.addAll(Arrays.asList(nodes));
    }
    else {
      newSelection.addAll(Arrays.asList(nodes));
    }
    // check selection count with multiselect
    if (newSelection.size() > 1 && !isMultiSelect()) {
      ITreeNode first = newSelection.iterator().next();
      newSelection.clear();
      newSelection.add(first);
    }
    if (m_selectedNodes.equals(newSelection) && m_selectedNodes.containsAll(Arrays.asList(nodes))) {
      // ok
    }
    else {
      HashSet<ITreeNode> oldSelection = m_selectedNodes;
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
      deselectNodes(new ITreeNode[]{node});
    }
    else {
      deselectNodes(new ITreeNode[0]);
    }
  }

  @Override
  public void deselectNodes(ITreeNode[] nodes) {
    nodes = resolveNodes(nodes);
    if (nodes != null && nodes.length > 0) {
      HashSet<ITreeNode> oldSelection = new HashSet<ITreeNode>(m_selectedNodes);
      HashSet<ITreeNode> newSelection = new HashSet<ITreeNode>();
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
  public ITreeNode[] getCheckedNodes() {
    final ArrayList<ITreeNode> list = new ArrayList<ITreeNode>();
    visitTree(new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.isChecked()) {
          list.add(node);
        }
        return true;
      }
    });
    return list.toArray(new ITreeNode[list.size()]);
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

  private ITreeNode[] resolveNodes(ITreeNode[] nodes) {
    if (nodes == null) {
      return new ITreeNode[0];
    }
    int mismatchCount = 0;
    for (int i = 0; i < nodes.length; i++) {
      if (resolveNode(nodes[i]) == null) {
        mismatchCount++;
      }
    }
    if (mismatchCount > 0) {
      ITreeNode[] resolvedNodes = new ITreeNode[nodes.length - mismatchCount];
      int index = 0;
      for (int i = 0; i < nodes.length; i++) {
        if (resolveNode(nodes[i]) != null) {
          resolvedNodes[index] = nodes[i];
          index++;
        }
      }
      nodes = resolvedNodes;
    }
    return nodes;
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

  private void fireNodesInserted(ITreeNode parent, ITreeNode[] children) {
    if (children.length > 0) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_INSERTED, parent, children));
    }
  }

  private void fireNodesUpdated(ITreeNode parent, ITreeNode[] children) {
    if (children.length > 0) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_UPDATED, parent, children));
    }
  }

  private void fireNodeFilterChanged() {
    fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_FILTER_CHANGED, getRootNode()));
  }

  private void fireNodesDeleted(ITreeNode parent, ITreeNode[] children) {
    if (children.length > 0) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_DELETED, parent, children));
    }
  }

  private void fireChildNodeOrderChanged(ITreeNode parent, ITreeNode[] children) {
    if (children.length > 0) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, parent, children));
    }
  }

  private void fireBeforeNodesSelected(Set<ITreeNode> oldSelection, Set<ITreeNode> newSelection) {
    ITreeNode[] nodes = newSelection.toArray(new ITreeNode[newSelection.size()]);
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_BEFORE_NODES_SELECTED, nodes);
    HashSet<ITreeNode> deselectedNodes = new HashSet<ITreeNode>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes.toArray(new ITreeNode[deselectedNodes.size()]));
    HashSet<ITreeNode> newSelectedNodes = new HashSet<ITreeNode>(newSelection);
    newSelectedNodes.removeAll(oldSelection);
    e.setNewSelectedNodes(newSelectedNodes.toArray(new ITreeNode[newSelectedNodes.size()]));
    fireTreeEventInternal(e);
  }

  private void fireNodesSelected(Set<ITreeNode> oldSelection, Set<ITreeNode> newSelection) {
    // single observer: rebuild title
    if (isAutoTitle()) {
      rebuildTitleInternal();
    }
    ITreeNode[] nodes = newSelection.toArray(new ITreeNode[newSelection.size()]);
    // fire
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODES_SELECTED, nodes);
    HashSet<ITreeNode> deselectedNodes = new HashSet<ITreeNode>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes.toArray(new ITreeNode[deselectedNodes.size()]));
    HashSet<ITreeNode> newSelectedNodes = new HashSet<ITreeNode>(newSelection);
    newSelectedNodes.removeAll(oldSelection);
    e.setNewSelectedNodes(newSelectedNodes.toArray(new ITreeNode[newSelectedNodes.size()]));
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
  public IMenu[] fetchMenusForNodesInternal(ITreeNode[] nodes) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_POPUP, nodes);
    // single observer for tree-owned menus
    addLocalPopupMenus(e);
    fireTreeEventInternal(e);
    //separate node menus and empty space actions
    ArrayList<IMenu> nodeMenus = new ArrayList<IMenu>();
    ArrayList<IMenu> emptySpaceMenus = new ArrayList<IMenu>();
    for (IMenu menu : e.getPopupMenus()) {
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
    return nodeMenus.toArray(new IMenu[nodeMenus.size()]);
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

  private void fireRequestFocus() {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_REQUEST_FOCUS);
    fireTreeEventInternal(e);
  }

  private void addLocalPopupMenus(TreeEvent e) {
    int selectionCount = e.getNodes().length;
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

  private TransferObject fireNodesDragRequest(ITreeNode[] nodes) {
    if (nodes != null && nodes.length > 0) {
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
  private void fireTreeEventBatchInternal(TreeEvent[] batch) {
    if (batch.length == 0) {
      return;
    }
    EventListener[] listeners = m_listenerList.getListeners(TreeListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((TreeListener) listeners[i]).treeChangedBatch(batch);
      }
    }
  }

  /**
   * add cells on the path to root and on children to decoration buffer
   */
  private void decorateAffectedNodeCells(ITreeNode parent, ITreeNode[] children) {
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
        HashSet<ITreeNode> set = m_nodeDecorationBuffer;
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
        ArrayList<TreeEvent> list = m_treeEventBuffer;
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
          fireTreeEventBatchInternal(list.toArray(new TreeEvent[list.size()]));
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
    if (node != null) {
      selectNode(node);
      execHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals("local"));
    }
  }

  @Override
  public void exportTreeData(final AbstractTreeFieldData target) throws ProcessingException {
    exportTreeNodeDataRec(getRootNode().getChildNodes(), target, null);
  }

  private void exportTreeNodeDataRec(ITreeNode[] nodes, AbstractTreeFieldData treeData, TreeNodeData parentNodeData) throws ProcessingException {
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

  private abstract class P_AbstractCollectingTreeVisitor implements ITreeVisitor {
    private final ArrayList<ITreeNode> m_list = new ArrayList<ITreeNode>();

    protected void addNodeToList(ITreeNode node) {
      m_list.add(node);
    }

    public ITreeNode[] getNodes() {
      return m_list.toArray(new ITreeNode[0]);
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
    public void setNodesSelectedFromUI(ITreeNode[] nodes) {
      try {
        pushUIProcessor();
        try {
          setTreeChanging(true);
          //
          HashSet<ITreeNode> requestedNodes = new HashSet<ITreeNode>(Arrays.asList(resolveVirtualNodes(resolveNodes(nodes))));
          for (ITreeNode node : requestedNodes) {
            if (node.isChildrenLoaded()) {
              if (node.isChildrenDirty() || node.isChildrenVolatile()) {
                node.loadChildren();
              }
            }
          }
          // check filtered nodes
          // add existing selected nodes that are masked by filter
          ArrayList<ITreeNode> validNodes = new ArrayList<ITreeNode>();
          for (ITreeNode node : getSelectedNodes()) {
            if (!node.isFilterAccepted()) {
              validNodes.add(node);
            }
          }
          // remove all filtered from requested
          requestedNodes.removeAll(validNodes);
          // add remainder
          for (ITreeNode node : requestedNodes) {
            validNodes.add(node);
          }
          selectNodes(validNodes.toArray(new ITreeNode[validNodes.size()]), false);
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (ProcessingException se) {
        se.addContextMessage(Arrays.asList(nodes).toString());
        SERVICES.getService(IExceptionHandlerService.class).handleException(se);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public IMenu[] fireNodePopupFromUI() {
      try {
        pushUIProcessor();
        //
        ITreeNode[] nodes = resolveVirtualNodes(getSelectedNodes());
        return fetchMenusForNodesInternal(nodes);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        return new IMenu[0];
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public IMenu[] fireEmptySpacePopupFromUI() {
      try {
        pushUIProcessor();
        //
        return fetchMenusForNodesInternal(new ITreeNode[0]);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireNodeClickFromUI(ITreeNode node) {
      try {
        pushUIProcessor();
        //
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

    @Override
    public boolean getNodesDragEnabledFromUI() {
      return isDragEnabled();
    }

    @Override
    public TransferObject fireNodesDragRequestFromUI() {
      try {
        pushUIProcessor();
        //
        ITreeNode[] nodes = resolveVirtualNodes(getSelectedNodes());
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
