/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeAutoCheckChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDisposeTreeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDragNodeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDragNodesChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDropTargetChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeHyperlinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeInitTreeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodeActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodeClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesSelectedChain;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITreeContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TreeContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilter;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTree extends AbstractPropertyObserver implements ITree, IContributionOwner, IExtensibleObject {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTree.class);

  private final EventListenerList m_listenerList = new EventListenerList();
  private ITreeUIFacade m_uiFacade;
  private boolean m_initialized;

  // enabled is defined as: enabledGranted && enabledProperty && enabledSlave // NOSONAR
  private boolean m_enabledGranted;
  private boolean m_enabledProperty;

  private Set<ITreeNode> m_checkedNodes = new HashSet<ITreeNode>();
  private ITreeNode m_rootNode;
  private int m_treeChanging;
  private boolean m_autoDiscardOnDelete;
  private boolean m_autoTitle;
  private final HashMap<Object, ITreeNode> m_deletedNodes;
  private AbstractEventBuffer<TreeEvent> m_eventBuffer;

  private Set<ITreeNode> m_nodeDecorationBuffer = new HashSet<ITreeNode>();
  private Set<ITreeNode> m_selectedNodes = new HashSet<ITreeNode>();
  private final List<ITreeNodeFilter> m_nodeFilters;
  private List<IKeyStroke> m_baseKeyStrokes;
  private IEventHistory<TreeEvent> m_eventHistory;
  // only do one action at a time
  private boolean m_actionRunning;
  private boolean m_saveAndRestoreScrollbars;
  private ITreeNode m_lastSeenDropNode;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractTree, ITreeExtension<? extends AbstractTree>> m_objectExtensions;
  private List<IMenu> m_currentNodeMenus;

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
    m_objectExtensions = new ObjectExtensions<AbstractTree, ITreeExtension<? extends AbstractTree>>(this);
    if (callInitialzier) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  /*
   * Configuration
   */
  /**
   * Configures the title of the tree.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  /**
   * Configures the icon of the tree.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * Configures the default iconId to be used for all tree nodes without an own icon.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(21)
  protected String getConfiguredDefaultIconId() {
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

  /**
   * Configures the drag support of this tree.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(51)
  protected int getConfiguredDragType() {
    return 0;
  }

  /**
   * Configures the drop support of this tree.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(52)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(190)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
  }

  /**
   * @return true: deleted nodes are automatically erased<br>
   *         false: deleted nodes are cached for later processing (service deletion)
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
   * Configures whether this tree should save and restore its coordinates of the vertical and horizontal scrollbars. If
   * this property is set to {@code true}, the tree saves its scrollbars coordinates to the {@link #ClientSession} upon
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

  /**
   * Checks / unchecks all visible child nodes if the parent node gets checked / unchecked.
   * <p>
   * Only has an effect if the tree is checkable.
   *
   * @see {@link #getConfiguredCheckable()}
   * @since 5.1
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredAutoCheckChildNodes() {
    return false;
  }

  /**
   * Configures whether it should be possible that child nodes may added lazily to the tree when expanding the node.
   * This property controls whether the feature is available at all. If set to true you need to define which nodes are
   * affected by using {@link ITreeNode#setLazyExpandingEnabled(boolean)}
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @see ITreeNode#isLazyExpandingEnabled()
   * @see ITreeNode#isExpandedLazy()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  protected boolean getConfiguredLazyExpandingEnabled() {
    return true;
  }

  /**
   * Configures the display style of the tree.
   * <p>
   * The available styles are:
   * <ul>
   * <li>{@link ITree#DISPLAY_STYLE_DEFAULT}</li>
   * <li>{@link ITree#DISPLAY_STYLE_BREADCRUMB}</li>
   * </ul>
   * <p>
   * Subclasses can override this method. The default is {@link ITree#DISPLAY_STYLE_DEFAULT}.
   *
   * @see #getConfiguredAutoToggleBreadcrumbStyle()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * Configures whether the outline should automatically switch to the bread crumb style when getting smaller and back
   * when getting bigger. The threshold is determined by the GUI.
   * <p>
   * Subclasses can override this method. The default is false.
   *
   * @see #getConfiguredDisplayStyle()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(145)
  protected boolean getConfiguredAutoToggleBreadcrumbStyle() {
    return false;
  }

  private List<Class<? extends IKeyStroke>> getConfiguredKeyStrokes() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IKeyStroke>> fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  public boolean isAutoCheckChildNodes() {
    return propertySupport.getPropertyBool(PROP_AUTO_CHECK_CHILDREN);
  }

  @Override
  public void setAutoCheckChildNodes(boolean b) {
    propertySupport.setPropertyBool(PROP_AUTO_CHECK_CHILDREN, b);
  }

  @ConfigOperation
  @Order(10)
  protected void execInitTree() {
  }

  @ConfigOperation
  @Order(15)
  protected void execDisposeTree() {
  }

  /**
   * The hyperlink's tree node is the selected node {@link #getSelectedNode()}
   *
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url (http://local/...)
   */
  @ConfigOperation
  @Order(18)
  protected void execHyperlinkAction(URL url, String path, boolean local) {
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(18)
  protected void execAppLinkAction(String ref) {
    //FIXME cgu: remove this code when execpHyperlinkAction has been removed
    URL url = null;
    boolean local = false;
    if (ref != null) {
      try {
        url = new URL(ref);
        local = "local".equals(url.getHost());
      }
      catch (MalformedURLException e) {
        LOG.error("Malformed URL '{}'", ref, e);
      }
    }
    execHyperlinkAction(url, ref, local);
  }

  /**
   * this method should not be implemented if you support {@link AbstractTree#interceptDrag(ITreeNode[])} (drag of
   * mulitple nodes), as it takes precedence
   *
   * @return a transferable object representing the given row
   */
  @ConfigOperation
  @Order(20)
  protected TransferObject execDrag(ITreeNode node) {
    return null;
  }

  /**
   * Drag of multiple nodes. If this method is implemented, also single drags will be handled by Scout, the method
   * {@link AbstractTree#interceptDrag(ITreeNode)} must not be implemented then.
   *
   * @return a transferable object representing the given rows
   */
  @ConfigOperation
  @Order(30)
  protected TransferObject execDrag(Collection<ITreeNode> nodes) {
    return null;
  }

  /**
   * process drop action
   */
  @ConfigOperation
  @Order(40)
  protected void execDrop(ITreeNode node, TransferObject t) {
  }

  /**
   * This method gets called when the drop node is changed, e.g. the dragged object is moved over a new drop target.
   *
   * @since 4.0-M7
   */
  @ConfigOperation
  @Order(45)
  protected void execDropTargetChanged(ITreeNode node) {
  }

  /**
   * decoration for every cell calls this method
   * <p>
   * Default delegates to {@link ITreeNode#decorateCell()}
   */
  @ConfigOperation
  @Order(50)
  protected void execDecorateCell(ITreeNode node, Cell cell) {
    if (cell.getIconId() == null && getDefaultIconId() != null) {
      cell.setIconId(getDefaultIconId());
    }
    node.decorateCell();
  }

  @ConfigOperation
  @Order(60)
  protected void execNodesSelected(TreeEvent e) {
  }

  @ConfigOperation
  @Order(70)
  protected void execNodeClick(ITreeNode node, MouseButton mouseButton) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_CLICK, node);
    fireTreeEventInternal(e);
  }

  @ConfigOperation
  @Order(80)
  protected void execNodeAction(ITreeNode node) {
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_ACTION, node);
    fireTreeEventInternal(e);
  }

  /**
   * Called when nodes get checked or unchecked.
   * <p>
   * Subclasses can override this method.
   *
   * @param nodes
   *          list of nodes which have been checked or unchecked (never null).
   */
  @ConfigOperation
  @Order(90)
  protected void execNodesChecked(List<ITreeNode> nodes) {
  }

  @ConfigOperation
  protected void execAutoCheckChildNodes(List<? extends ITreeNode> nodes) {
    for (ITreeNode node : nodes) {
      for (ITreeNode childNode : node.getFilteredChildNodes()) {
        if (childNode.isEnabled() && childNode.isVisible()) {
          childNode.setChecked(node.isChecked());
        }
        interceptAutoCheckChildNodes(CollectionUtility.arrayList(childNode));
      }
    }
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    m_enabledGranted = true;
    m_eventHistory = createEventHistory();
    m_eventBuffer = createEventBuffer();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setEnabled(true);
    setTitle(getConfiguredTitle());
    setIconId(getConfiguredIconId());
    setDefaultIconId(getConfiguredDefaultIconId());
    setAutoTitle(getConfiguredAutoTitle());
    setCheckable(getConfiguredCheckable());
    setNodeHeightHint(getConfiguredNodeHeightHint());
    setMultiCheck(getConfiguredMultiCheck());
    setMultiSelect(getConfiguredMultiSelect());
    setAutoDiscardOnDelete(getConfiguredAutoDiscardOnDelete());
    setDragEnabled(getConfiguredDragEnabled());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setRootNodeVisible(getConfiguredRootNodeVisible());
    setRootHandlesVisible(getConfiguredRootHandlesVisible());
    setScrollToSelection(getConfiguredScrollToSelection());
    setSaveAndRestoreScrollbars(getConfiguredSaveAndRestoreScrollbars());
    setAutoCheckChildNodes(getConfiguredAutoCheckChildNodes());
    setLazyExpandingEnabled(getConfiguredLazyExpandingEnabled());
    setDisplayStyle(getConfiguredDisplayStyle());
    setAutoToggleBreadcrumbStyle(getConfiguredAutoToggleBreadcrumbStyle());
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
                TransferObject transferObject = interceptDrag(e.getNode());
                if (transferObject == null) {
                  transferObject = interceptDrag(e.getNodes());
                }
                e.setDragObject(transferObject);
              }
              catch (Exception t) {
                LOG.error("Drag", t);
              }
            }
            break;
          }
          case TreeEvent.TYPE_NODE_DROP_ACTION: {
            m_lastSeenDropNode = null;
            if (e.getDropObject() != null) {
              try {
                interceptDrop(e.getNode(), e.getDropObject());
              }
              catch (Exception t) {
                LOG.error("Drop", t);
              }
            }
            break;
          }
          case TreeEvent.TYPE_NODES_SELECTED: {
            rebuildKeyStrokesInternal();
            break;
          }
          case TreeEvent.TYPE_NODES_CHECKED: {
            try {
              interceptNodesChecked(CollectionUtility.arrayList(e.getNodes()));
            }
            catch (RuntimeException ex) {
              BEANS.get(ExceptionHandler.class).handle(ex);
            }
            break;
          }
          case TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED: {
            try {
              if (m_lastSeenDropNode == null || m_lastSeenDropNode != e.getNode()) {
                m_lastSeenDropNode = e.getNode();
                interceptDropTargetChanged(e.getNode());
              }
            }
            catch (RuntimeException ex) {
              LOG.error("DropTargetChanged", ex);
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
    List<Class<? extends IKeyStroke>> configuredKeyStrokes = getConfiguredKeyStrokes();
    List<IKeyStroke> ksList = new ArrayList<IKeyStroke>(configuredKeyStrokes.size());
    for (Class<? extends IKeyStroke> keystrokeClazz : configuredKeyStrokes) {
      ksList.add(ConfigurationUtility.newInnerInstance(this, keystrokeClazz));
    }
    //ticket 87370: add ENTER key stroke when execNodeAction has an override
    if (ConfigurationUtility.isMethodOverwrite(AbstractTree.class, "execNodeAction", new Class[]{ITreeNode.class}, this.getClass())) {
      ksList.add(new KeyStroke("ENTER") {
        @Override
        protected void execAction() {
          fireNodeAction(getSelectedNode());
        }
      });
    }

    List<IKeyStroke> contributedKeyStrokes = m_contributionHolder.getContributionsByClass(IKeyStroke.class);
    contributedKeyStrokes.addAll(contributedKeyStrokes);

    m_baseKeyStrokes = ksList;
    setKeyStrokesInternal(m_baseKeyStrokes);

    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);

    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
      menus.addOrdered(menu);
    }
    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("Error occured while dynamically contributing menus.", e);
    }

    menus.addAllOrdered(contributedMenus);
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    TreeContextMenu contextMenu = new TreeContextMenu(this, menus.getOrderedList());
    setContextMenuInternal(contextMenu);
  }

  /*
   * Runtime
   */
  @Override
  public final void initTree() {
    initTreeInternal();
    ActionUtility.initActions(getMenus());
    interceptInitTree();
  }

  protected void initTreeInternal() {
  }

  @Override
  public final void disposeTree() {
    disposeTreeInternal();
    try {
      interceptDisposeTree();
    }
    catch (Exception e) {
      LOG.warn("Exception while disposing tree", e);
    }
  }

  protected void disposeTreeInternal() {
    ActionUtility.disposeActions(getMenus());
    getRootNode().dispose();
    clearDeletedNodes();
  }

  @Override
  public final List<? extends ITreeExtension<? extends AbstractTree>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ITreeExtension<? extends AbstractTree> createLocalExtension() {
    return new LocalTreeExtension<AbstractTree>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br/>
   * Used to manage menu list and add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  @Override
  public ITreeContextMenu getContextMenu() {
    return (ITreeContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  protected void setContextMenuInternal(ITreeContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public <T extends IMenu> T getMenu(Class<T> menuType) {
    return getMenuByClass(menuType);
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
    List<ITreeNodeFilter> rejectingFilters = new ArrayList<ITreeNodeFilter>();
    inode.setFilterAccepted(true);
    inode.setRejectedByUser(false);
    if (m_nodeFilters.size() > 0) {
      for (ITreeNodeFilter filter : m_nodeFilters) {
        if (!filter.accept(inode, level)) {
          inode.setFilterAccepted(false);
          rejectingFilters.add(filter);
        }
      }
    }

    // Prefer inode.isRejectedByUser to allow a filter to set this flag
    inode.setRejectedByUser(inode.isRejectedByUser()
        || (rejectingFilters.size() == 1 && rejectingFilters.get(0) instanceof IUserFilter));

    if (!inode.isFilterAccepted() && isSelectedNode(inode)) {
      // invisible nodes cannot be selected
      deselectNode(inode);
    }
    // make parent path accepted
    if ((!parentAccepted) && inode.isFilterAccepted()) {
      ITreeNode tmp = inode.getParentNode();
      while (tmp != null) {
        tmp.setFilterAccepted(true);
        tmp.setRejectedByUser(false);
        tmp = tmp.getParentNode();
      }
    }
    // children
    for (ITreeNode child : inode.getChildNodes()) {
      applyNodeFiltersRecInternal(child, inode.isFilterAccepted(), level + 1);
    }
  }

  @Override
  public AbstractEventBuffer<TreeEvent> createEventBuffer() {
    return new TreeEventBuffer();
  }

  protected AbstractEventBuffer<TreeEvent> getEventBuffer() {
    return m_eventBuffer;
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
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public String getDefaultIconId() {
    return propertySupport.getPropertyString(PROP_DEFAULT_ICON_ID);
  }

  @Override
  public void setDefaultIconId(String defaultIconId) {
    propertySupport.setPropertyString(PROP_DEFAULT_ICON_ID, defaultIconId);
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
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(PROP_DROP_MAXIMUM_SIZE);
  }

  @Override
  public void setEnabledPermission(Permission p) {
    boolean b;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
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
  public boolean isLazyExpandingEnabled() {
    return propertySupport.getPropertyBool(PROP_LAZY_EXPANDING_ENABLED);
  }

  @Override
  public void setLazyExpandingEnabled(boolean lazyExpandingEnabled) {
    propertySupport.setPropertyBool(PROP_LAZY_EXPANDING_ENABLED, lazyExpandingEnabled);
  }

  @Override
  public String getDisplayStyle() {
    return propertySupport.getPropertyString(PROP_DISPLAY_STYLE);
  }

  @Override
  public void setDisplayStyle(String style) {
    propertySupport.setPropertyString(PROP_DISPLAY_STYLE, style);
  }

  @Override
  public boolean isAutoToggleBreadcrumbStyle() {
    return propertySupport.getPropertyBool(PROP_AUTO_TOGGLE_BREADCRUMB_STYLE);
  }

  @Override
  public void setAutoToggleBreadcrumbStyle(boolean b) {
    propertySupport.setPropertyBool(PROP_AUTO_TOGGLE_BREADCRUMB_STYLE, b);
  }

  @Override
  public String getPathText(ITreeNode selectedNode) {
    return getPathText(selectedNode, " - ");
  }

  @Override
  public String getPathText(ITreeNode selectedNode, String delimiter) {
    // construct the path to the data
    ITreeNode root = getRootNode();
    StringBuilder pathStr = new StringBuilder("");
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
    setKeyStrokesInternal(m_baseKeyStrokes);
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
        catch (RuntimeException e) {
          LOG.error("expanding root node of {}", getTitle(), e);
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
            processTreeBuffers();
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
    setNodeExpanded(node, b, node.isLazyExpandingEnabled());
  }

  @Override
  public void setNodeExpanded(ITreeNode node, boolean b, boolean lazy) {
    // Never do lazy expansion if it is disabled on the tree
    if (!isLazyExpandingEnabled()) {
      lazy = false;
    }

    node = resolveNode(node);
    if (node != null) {
      if (node.isExpanded() != b || node.isExpandedLazy() != lazy) {
        setNodeExpandedInternal(node, b, lazy);
      }
    }
  }

  @Override
  public void setNodeExpandedInternal(ITreeNode node, boolean b, boolean lazy) {
    try {
      if (b) {
        node.ensureChildrenLoaded();
        ensureParentExpanded(node.getParentNode());
      }
      node.setExpandedInternal(b);
      node.setExpandedLazyInternal(lazy);
      fireNodeExpanded(node, b);
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
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
      node.setVisiblePermissionInternal(p);
      // dont fire observers since visibility change only has an effect when
      // used in init method
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
      node.setVisibleInternal(b);
      // dont fire observers since visibility change only has an effect when
      // used in init method
    }
  }

  @Override
  public void setNodeVisibleGranted(ITreeNode node, boolean b) {
    node = resolveNode(node);
    if (node != null) {
      node.setVisibleGrantedInternal(b);
      // dont fire observers since visibility change only has an effect when
      // used in init method
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
      return getCheckedNodes().contains(node);
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeChecked(ITreeNode node, boolean b) {
    setNodesChecked(CollectionUtility.arrayList(node), b);
  }

  @Override
  public void setNodesChecked(List<ITreeNode> nodes, boolean b) {
    setNodesChecked(nodes, b, false);
  }

  public void setNodesChecked(List<ITreeNode> nodes, boolean b, boolean onlyCheckEnabledNodes) {
    if (!isCheckable()) {
      return;
    }
    List<ITreeNode> changedNodes = new ArrayList<ITreeNode>();
    for (ITreeNode node : nodes) {
      node = resolveNode(node);
      if (node != null) {
        if (node.isChecked() != b && (!onlyCheckEnabledNodes || node.isEnabled())) {
          if (b) {
            m_checkedNodes.add(node);
          }
          else {
            m_checkedNodes.remove(node);
          }
          changedNodes.add(node);

          //uncheck others in single-check mode
          if (b && !isMultiCheck()) {
            List<ITreeNode> uncheckedNodes = new ArrayList<ITreeNode>();
            for (ITreeNode cn : getCheckedNodes()) {
              if (cn != node) {
                m_checkedNodes.remove(cn);
                uncheckedNodes.add(cn);
              }
            }
            break;
          }
        }
      }
    }
    if (changedNodes.size() > 0) {
      if (isAutoCheckChildNodes() && isMultiCheck()) {
        try {
          interceptAutoCheckChildNodes(nodes);
        }
        catch (RuntimeException ex) {
          BEANS.get(ExceptionHandler.class).handle(ex);
        }
      }
      fireNodesChecked(changedNodes);
    }
  }

  /**
   * Recursively checks/unchecks the subtree of <code>parent</code>.
   */
  private void uncheckAllRec(ITreeNode parent, boolean b) {
    if (parent == null) {
      return;
    }
    setNodeChecked(parent, b);
    for (ITreeNode node : parent.getChildNodes()) {
      uncheckAllRec(node, b);
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
    try {
      setTreeChanging(true);
      //
      expandAllRec(parent, 0);
      fireNodeExpandedRecursive(parent, true);
    }
    finally {
      setTreeChanging(false);
    }
  }

  private void expandAllRec(ITreeNode parent, int level) {
    setNodeExpanded(parent, true);
    // loop detection
    if (level >= 32) {
      LOG.warn("detected loop on tree node {}", parent);
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
      List<ITreeNode> list = new ArrayList<ITreeNode>();
      fetchAllCollapsingNodesRec(parent, 0, list);
      for (int n = list.size(), i = n - 1; i >= 0; i--) {
        setNodeExpanded(list.get(i), false);
      }
      fireNodeExpandedRecursive(parent, false);
    }
    finally {
      setTreeChanging(false);
    }
  }

  private void fetchAllCollapsingNodesRec(ITreeNode parent, int level, List<ITreeNode> list) {
    // loop detection
    if (level >= 32) {
      LOG.warn("detected loop on tree node {}", parent);
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
      List<ITreeNode> newChildren = new ArrayList<ITreeNode>(children);
      // Fire NODES_INSERTED event before actually inserting the nodes, because during insertion, other events might occur (e.g. NODE_CHANGED in decorateCell())
      fireNodesInserted(parent, newChildren);
      //
      ((AbstractTreeNode) parent).addChildNodesInternal(startIndex, children, true);
      // check if all children were added, or if some were revoked using
      // visible=false in init (addNotify) phase.
      for (Iterator<ITreeNode> it = newChildren.iterator(); it.hasNext();) {
        ITreeNode child = it.next();
        if (child.getParentNode() == null) {
          it.remove();
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
      // remove children from set of checked nodes
      for (ITreeNode child : children) {
        uncheckAllRec(child, false);
      }
      ((AbstractTreeNode) parent).removeChildNodesInternal(children, true, isAutoDiscardOnDelete());
      decorateAffectedNodeCells(parent, parent.getChildNodes());
      if (!isAutoDiscardOnDelete()) {
        for (ITreeNode child : children) {
          if (child.getStatus() == ITreeNode.STATUS_INSERTED) {
            // The node was new and now it is gone, so it can be disposed now
            child.dispose();
          }
          else {
            // The node will be disposed later.
            child.setStatusInternal(ITreeNode.STATUS_DELETED);
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
      if (parent.getChildNodeCount() == 0) {
        fireAllChildNodesDeleted(parent, children);
      }
      else {
        fireNodesDeleted(parent, children);
      }
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
  public void discardDeletedNode(ITreeNode node) {
    discardDeletedNodes(CollectionUtility.arrayList(node));
  }

  @Override
  public void discardDeletedNodes(Collection<ITreeNode> nodes) {
    for (ITreeNode node : nodes) {
      ITreeNode delNode = m_deletedNodes.get(node.getPrimaryKey());
      if (delNode == node) {
        m_deletedNodes.remove(node.getPrimaryKey());
      }
    }
  }

  @Override
  public void disposeDeletedNode(ITreeNode node) {
    disposeDeletedNodes(CollectionUtility.arrayList(node));
  }

  @Override
  public void disposeDeletedNodes(Collection<ITreeNode> nodes) {
    for (ITreeNode node : CollectionUtility.arrayList(nodes)) {
      ITreeNode delNode = m_deletedNodes.get(node.getPrimaryKey());
      if (delNode == node) {
        node.setTreeInternal(null, true);
        try {
          node.dispose();
        }
        catch (RuntimeException e) {
          LOG.warn("Exception while disposing node: {}.", node, e);
        }
        discardDeletedNode(node);
      }
    }
  }

  @Override
  public void clearDeletedNodes() {
    disposeDeletedNodes(m_deletedNodes.values());
  }

  @Override
  public Set<ITreeNode> resolveVirtualNodes(Collection<? extends ITreeNode> nodes) {
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
  public ITreeNode resolveVirtualNode(ITreeNode node) {
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
    return TreeUtility.visitNodeRec(getRootNode(), v);
  }

  @Override
  public boolean visitNode(ITreeNode node, ITreeVisitor v) {
    return TreeUtility.visitNode(node, v);
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
    catch (RuntimeException e) {
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
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
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
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
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
    return CollectionUtility.hashSet(m_checkedNodes);
  }

  @Override
  public int getCheckedNodesCount() {
    return m_checkedNodes.size();
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

  protected void filterInitializingTreeNodes(Collection<? extends ITreeNode> nodes) {
    if (nodes == null) {
      return;
    }
    for (Iterator<? extends ITreeNode> it = nodes.iterator(); it.hasNext();) {
      ITreeNode node = it.next();
      if (node != null && node.isInitializing()) {
        it.remove();
      }
    }
  }

  private void fireNodesInserted(ITreeNode parent, List<ITreeNode> children) {
    if (parent != null && parent.isInitializing()) {
      return;
    }
    filterInitializingTreeNodes(children);
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_INSERTED, parent, children));
    }
  }

  private void fireNodesUpdated(ITreeNode parent, Collection<ITreeNode> children) {
    if (parent != null && parent.isInitializing()) {
      return;
    }
    filterInitializingTreeNodes(children);
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_UPDATED, parent, children));
    }
  }

  private void fireNodesChecked(List<ITreeNode> nodes) {
    filterInitializingTreeNodes(nodes);
    if (CollectionUtility.hasElements(nodes)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_CHECKED, nodes));
    }
  }

  @Override
  public void fireNodeChanged(ITreeNode node) {
    if (node != null && node.isInitializing()) {
      return;
    }
    fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_CHANGED, node));
  }

  private void fireNodeFilterChanged() {
    if (getRootNode() != null && getRootNode().isInitializing()) {
      return;
    }
    fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_FILTER_CHANGED, getRootNode()));
  }

  private void fireNodesDeleted(ITreeNode parent, Collection<? extends ITreeNode> children) {
    if (parent != null && parent.isInitializing()) {
      return;
    }
    filterInitializingTreeNodes(children);
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODES_DELETED, parent, children));
    }
  }

  private void fireAllChildNodesDeleted(ITreeNode parent, Collection<? extends ITreeNode> children) {
    if (parent != null && parent.isInitializing()) {
      return;
    }
    filterInitializingTreeNodes(children);
    if (CollectionUtility.hasElements(children)) {
      fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED, parent, children));
    }
  }

  private void fireChildNodeOrderChanged(ITreeNode parent, List<? extends ITreeNode> children) {
    if (parent != null && parent.isInitializing()) {
      return;
    }
    filterInitializingTreeNodes(children);
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

    boolean emptySelection = newSelectedNodes.isEmpty();
    filterInitializingTreeNodes(newSelectedNodes);
    if (!emptySelection && newSelectedNodes.isEmpty()) {
      return;
    }

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
    Set<ITreeNode> deselectedNodes = new HashSet<ITreeNode>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes);
    Set<ITreeNode> newSelectedNodes = new HashSet<ITreeNode>(newSelection);
    newSelectedNodes.removeAll(oldSelection);

    boolean emptySelection = newSelectedNodes.isEmpty();
    filterInitializingTreeNodes(newSelectedNodes);
    if (!emptySelection && newSelectedNodes.isEmpty()) {
      return;
    }

    e.setNewSelectedNodes(newSelectedNodes);
    //single observer
    try {
      nodesSelectedInternal(deselectedNodes, newSelectedNodes);
      interceptNodesSelected(e);
    }
    catch (Exception ex) {
      BEANS.get(ExceptionHandler.class).handle(ex);
    }
    //end single observer
    fireTreeEventInternal(e);
  }

  protected void nodesSelectedInternal(Set<ITreeNode> oldSelection, Set<ITreeNode> newSelection) {
    updateNodeMenus(m_selectedNodes);
  }

  /**
   * @param newSelectedNodes
   */
  protected void updateNodeMenus(Set<ITreeNode> newSelectedNodes) {
    // remove old
    if (m_currentNodeMenus != null) {
      getContextMenu().removeChildActions(m_currentNodeMenus);
      m_currentNodeMenus = null;
    }
    List<IMenu> nodeMenus = new ArrayList<IMenu>();
    // take only first node to avoid having multiple same menus due to all nodes.
    if (CollectionUtility.hasElements(newSelectedNodes)) {
      nodeMenus.addAll(CollectionUtility.firstElement(newSelectedNodes).getMenus());
      m_currentNodeMenus = nodeMenus;
      getContextMenu().addChildActions(nodeMenus);
    }
  }

  private void fireNodeExpanded(ITreeNode node, boolean b) {
    if (node != null && !node.isInitializing()) {
      if (b) {
        fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_EXPANDED, node));
      }
      else {
        fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_COLLAPSED, node));
      }
    }
  }

  private void fireNodeExpandedRecursive(ITreeNode node, boolean b) {
    if (node != null && !node.isInitializing()) {
      if (b) {
        fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE, node));
      }
      else {
        fireTreeEventInternal(new TreeEvent(this, TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE, node));
      }
    }
  }

  private void fireNodeClick(ITreeNode node, MouseButton mouseButton) {
    if (node != null && !node.isInitializing()) {
      try {
        interceptNodeClick(node, mouseButton);
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
  }

  protected void interceptNodesChecked(List<ITreeNode> nodes) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeNodesCheckedChain chain = new TreeNodesCheckedChain(extensions);
    chain.execNodesChecked(nodes);
  }

  protected void interceptAutoCheckChildNodes(List<ITreeNode> nodes) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeAutoCheckChildNodesChain chain = new TreeAutoCheckChildNodesChain(extensions);
    chain.execAutoCheckChildNodes(nodes);
  }

  private void fireNodeAction(ITreeNode node) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (node != null && !node.isInitializing()) {
          if (node.isLeaf()) {
            try {
              interceptNodeAction(node);
            }
            catch (Exception ex) {
              BEANS.get(ExceptionHandler.class).handle(ex);
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

  private TransferObject fireNodesDragRequest(Collection<ITreeNode> nodes) {
    filterInitializingTreeNodes(nodes);
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
    if (node != null && node.isInitializing()) {
      return;
    }
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_DROP_ACTION, node);
    e.setDropObject(dropData);
    fireTreeEventInternal(e);
  }

  /**
   * This method gets called when the drop node is changed, e.g. the dragged object is moved over a new drop target.
   *
   * @since 4.0-M7
   */
  public void fireNodeDropTargetChanged(ITreeNode node) {
    if (node != null && node.isInitializing()) {
      return;
    }
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

  private void fireNodeEnsureVisible(ITreeNode node) {
    if (node != null && node.isInitializing()) {
      return;
    }
    TreeEvent e = new TreeEvent(this, TreeEvent.TYPE_NODE_ENSURE_VISIBLE, node);
    fireTreeEventInternal(e);
  }

  // main handler
  protected void fireTreeEventInternal(TreeEvent e) {
    if (isTreeChanging()) {
      // buffer the event for later batch firing
      getEventBuffer().add(e);
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(TreeListener.class);
      for (EventListener l : listeners) {
        ((TreeListener) l).treeChanged(e);
      }
    }
  }

  // batch handler
  private void fireTreeEventBatchInternal(List<? extends TreeEvent> batch) {
    if (CollectionUtility.hasElements(batch)) {
      EventListener[] listeners = m_listenerList.getListeners(TreeListener.class);
      for (EventListener l : listeners) {
        ((TreeListener) l).treeChangedBatch(batch);
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

  private int m_processTreeBufferLoopDetection;

  /**
   * affects columns with lookup calls or code types<br>
   * cells that have changed values fetch new texts/decorations from the lookup service in one single batch call lookup
   * (performance optimization)
   */
  private void processTreeBuffers() {
    //loop detection
    try {
      m_processTreeBufferLoopDetection++;
      if (m_processTreeBufferLoopDetection > 100) {
        LOG.error("LOOP DETECTION in {}. see stack trace for more details.", getClass(), new Exception("LOOP DETECTION"));
        return;
      }
      processDecorationBuffer();
      processEventBuffer();
    }
    finally {
      m_processTreeBufferLoopDetection--;
    }
  }

  /**
   * update row decorations
   */
  private void processDecorationBuffer() {
    if (m_nodeDecorationBuffer.size() > 0) {
      Set<ITreeNode> set = m_nodeDecorationBuffer;
      m_nodeDecorationBuffer = new HashSet<ITreeNode>();
      try {
        setTreeChanging(true);
        for (Iterator<ITreeNode> it = set.iterator(); it.hasNext();) {
          ITreeNode node = it.next();
          if (node.getTree() != null) {
            try {
              interceptDecorateCell(node, node.getCellForUpdate());
            }
            catch (Exception t) {
              LOG.warn("node {} ({})", node.getClass(), node.getCell().getText(), t);
            }
          }
        }
      }
      finally {
        setTreeChanging(false);
      }
    }
  }

  /**
   * fire events tree changes are finished now, fire all buffered events and call lookups
   */
  private void processEventBuffer() {
    if (!getEventBuffer().isEmpty()) {
      final List<TreeEvent> list = getEventBuffer().consumeAndCoalesceEvents();
      // fire the batch and set tree to changing, otherwise a listener might trigger another events that then are processed
      // before all other listeners received that batch
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
  public void unloadNode(ITreeNode node) {
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
  public void doAppLinkAction(String ref) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        interceptAppLinkAction(ref);
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  @Override
  public void exportTreeData(final AbstractTreeFieldData target) {
    exportTreeNodeDataRec(getRootNode().getChildNodes(), target, null);
  }

  private void exportTreeNodeDataRec(List<ITreeNode> nodes, AbstractTreeFieldData treeData, TreeNodeData parentNodeData) {
    ArrayList<TreeNodeData> nodeDataList = new ArrayList<TreeNodeData>(nodes.size());
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
  protected TreeNodeData exportTreeNodeData(ITreeNode node, AbstractTreeFieldData treeData) {
    TreeNodeData nodeData = new TreeNodeData();
    return nodeData;
  }

  @Override
  public void importTreeData(AbstractTreeFieldData source) {
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

  private void importTreeNodeDataRec(ITreeNode parentNode, AbstractTreeFieldData treeData, List<TreeNodeData> nodeDataList) {
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
   * @return the new tree node for this node data or null to skip this node It is the responsibility of this method to
   *         add the new nopde to the tree.
   */
  protected ITreeNode importTreeNodeData(ITreeNode parentNode, AbstractTreeFieldData treeData, TreeNodeData nodeData) {
    return null;
  }

  protected ITreeUIFacade createUIFacade() {
    return new P_UIFacade();
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
  protected class P_UIFacade implements ITreeUIFacade {
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
    public void setNodesCheckedFromUI(List<ITreeNode> nodes, boolean checked) {
      if (!isEnabled()) {
        return;
      }
      try {
        pushUIProcessor();
        try {
          setTreeChanging(true);
          //
          nodes = resolveNodes(nodes);
          nodes = CollectionUtility.arrayList(resolveVirtualNodes(nodes));
          if (nodes.size() > 0) {
            setNodesChecked(nodes, checked, true);
          }
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (RuntimeException e) {
        StringBuilder msg = new StringBuilder();
        for (ITreeNode node : nodes) {
          msg.append("[");
          msg.append(node.getCell().getText());
          msg.append("]");
        }
        throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
            .withContextInfo("nodes", msg.toString());
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setNodeExpandedFromUI(ITreeNode node, boolean on, boolean lazy) {
      try {
        pushUIProcessor();
        try {
          setTreeChanging(true);
          //
          node = resolveNode(node);
          node = resolveVirtualNode(node);
          if (node != null) {
            if (node.isExpanded() != on || node.isExpandedLazy() != lazy) {
              if (on) {
                if (node.isChildrenDirty() || node.isChildrenVolatile()) {
                  node.loadChildren();
                }
              }
              setNodeExpanded(node, on, lazy);
            }
          }
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (RuntimeException e) {
        if (node != null) {
          throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("node", node.getCell().getText());
        }
        throw e;
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
      catch (RuntimeException e) {
        if (node != null) {
          throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("cell", node.getCell().getText());
        }
        throw e;
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
      catch (RuntimeException e) {
        if (nodes != null) {
          throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("nodes", nodes.toString());
        }
        throw e;
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireNodeClickFromUI(ITreeNode node, MouseButton mouseButton) {
      try {
        pushUIProcessor();
        node = resolveNode(node);
        node = resolveVirtualNode(node);
        if (node != null) {
          fireNodeClick(node, mouseButton);
        }
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
      finally {
        popUIProcessor();
      }
    }

    @Override
    public TransferObject fireNodesDragRequestFromUI() {
      try {
        pushUIProcessor();
        //
        Collection<ITreeNode> nodes = resolveVirtualNodes(getSelectedNodes());
        return fireNodesDragRequest(nodes);
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
        fireNodeDropAction(node, dropData);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      try {
        pushUIProcessor();
        doAppLinkAction(ref);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setDisplayStyleFromUI(String style) {
      setDisplayStyle(style);
    }

  }// end private class

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalTreeExtension<OWNER extends AbstractTree> extends AbstractExtension<OWNER> implements ITreeExtension<OWNER> {

    public LocalTreeExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDrop(TreeDropChain chain, ITreeNode node, TransferObject t) {
      getOwner().execDrop(node, t);
    }

    @Override
    public void execInitTree(TreeInitTreeChain chain) {
      getOwner().execInitTree();
    }

    @Override
    public void execDropTargetChanged(TreeDropTargetChangedChain chain, ITreeNode node) {
      getOwner().execDropTargetChanged(node);
    }

    @Override
    public TransferObject execDrag(TreeDragNodesChain chain, Collection<ITreeNode> nodes) {
      return getOwner().execDrag(nodes);
    }

    @Override
    public void execNodeAction(TreeNodeActionChain chain, ITreeNode node) {
      getOwner().execNodeAction(node);
    }

    @Override
    public void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton) {
      getOwner().execNodeClick(node, mouseButton);
    }

    @Override
    public void execAppLinkAction(TreeHyperlinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }

    @Override
    public void execNodesSelected(TreeNodesSelectedChain chain, TreeEvent e) {
      getOwner().execNodesSelected(e);
    }

    @Override
    public void execDisposeTree(TreeDisposeTreeChain chain) {
      getOwner().execDisposeTree();
    }

    @Override
    public void execDecorateCell(TreeDecorateCellChain chain, ITreeNode node, Cell cell) {
      getOwner().execDecorateCell(node, cell);
    }

    @Override
    public TransferObject execDrag(TreeDragNodeChain chain, ITreeNode node) {
      return getOwner().execDrag(node);
    }

    @Override
    public void execNodesChecked(TreeNodesCheckedChain chain, List<ITreeNode> nodes) {
      getOwner().execNodesChecked(nodes);
    }

    @Override
    public void execAutoCheckChildNodes(TreeAutoCheckChildNodesChain chain, List<ITreeNode> nodes) {
      getOwner().execAutoCheckChildNodes(nodes);
    }
  }

  protected final void interceptDrop(ITreeNode node, TransferObject t) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeDropChain chain = new TreeDropChain(extensions);
    chain.execDrop(node, t);
  }

  protected final void interceptInitTree() {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeInitTreeChain chain = new TreeInitTreeChain(extensions);
    chain.execInitTree();
  }

  protected final void interceptDropTargetChanged(ITreeNode node) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeDropTargetChangedChain chain = new TreeDropTargetChangedChain(extensions);
    chain.execDropTargetChanged(node);
  }

  protected final TransferObject interceptDrag(Collection<ITreeNode> nodes) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeDragNodesChain chain = new TreeDragNodesChain(extensions);
    return chain.execDrag(nodes);
  }

  protected final void interceptNodeAction(ITreeNode node) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeNodeActionChain chain = new TreeNodeActionChain(extensions);
    chain.execNodeAction(node);
  }

  protected final void interceptNodeClick(ITreeNode node, MouseButton mouseButton) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeNodeClickChain chain = new TreeNodeClickChain(extensions);
    chain.execNodeClick(node, mouseButton);
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeHyperlinkActionChain chain = new TreeHyperlinkActionChain(extensions);
    chain.execHyperlinkAction(ref);
  }

  protected final void interceptNodesSelected(TreeEvent e) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeNodesSelectedChain chain = new TreeNodesSelectedChain(extensions);
    chain.execNodesSelected(e);
  }

  protected final void interceptDisposeTree() {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeDisposeTreeChain chain = new TreeDisposeTreeChain(extensions);
    chain.execDisposeTree();
  }

  protected final void interceptDecorateCell(ITreeNode node, Cell cell) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeDecorateCellChain chain = new TreeDecorateCellChain(extensions);
    chain.execDecorateCell(node, cell);
  }

  protected final TransferObject interceptDrag(ITreeNode node) {
    List<? extends ITreeExtension<? extends AbstractTree>> extensions = getAllExtensions();
    TreeDragNodeChain chain = new TreeDragNodeChain(extensions);
    return chain.execDrag(node);
  }
}
