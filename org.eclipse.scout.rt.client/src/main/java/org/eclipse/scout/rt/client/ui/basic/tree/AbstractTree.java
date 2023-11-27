/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension;
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
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.MouseButton;
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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.visitor.CollectingVisitor;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeTraversals;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.TreeNodeData;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("b177affd-790b-4908-b608-ac00b996b10e")
public abstract class AbstractTree extends AbstractWidget implements ITree, IContributionOwner, IExtensibleObject {

  private static final String AUTO_DISCARD_ON_DELETE = "AUTO_DISCARD_ON_DELETE";
  private static final String AUTO_TITLE = "AUTO_TITLE";
  private static final String ACTION_RUNNING = "ACTION_RUNNING";
  private static final String SAVE_AND_RESTORE_SCROLLBARS = "SAVE_AND_RESTORE_SCROLLBARS";
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTree.class);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(AUTO_DISCARD_ON_DELETE, AUTO_TITLE, ACTION_RUNNING, SAVE_AND_RESTORE_SCROLLBARS);

  private final TreeListeners m_listeners = new TreeListeners();

  private final Set<ITreeNode> m_checkedNodes;
  private final Map<Object, ITreeNode> m_deletedNodes;
  private final List<ITreeNodeFilter> m_nodeFilters;
  private final ObjectExtensions<AbstractTree, ITreeExtension<? extends AbstractTree>> m_objectExtensions;

  /**
   * In autoCheckChildren mode only the effectively checked parent nodes should cause nodesChecked Events (to minimize
   * network traffic). setNodesChecked is called recursively by the intercepter. The m_currentParentNodes list is used
   * to avoid firing events inside the recursion.
   */
  private List<ITreeNode> m_currentParentNodes;

  /**
   * Provides 4 boolean flags.<br>
   * Currently used: {@link #AUTO_DISCARD_ON_DELETE}, {@link #AUTO_TITLE},
   * {@link #ACTION_RUNNING}, {@link #SAVE_AND_RESTORE_SCROLLBARS}
   */
  private byte m_flags;

  private ITreeNode m_rootNode;
  private int m_treeChanging;
  private AbstractEventBuffer<TreeEvent> m_eventBuffer;

  private ITreeUIFacade m_uiFacade;
  private Set<ITreeNode> m_nodeDecorationBuffer = new HashSet<>();
  private Set<ITreeNode> m_selectedNodes = new HashSet<>();
  private List<IKeyStroke> m_baseKeyStrokes;
  private IEventHistory<TreeEvent> m_eventHistory;
  private ITreeNode m_lastSeenDropNode;
  private IContributionOwner m_contributionHolder;
  private List<IMenu> m_currentNodeMenus;

  public AbstractTree() {
    this(true);
  }

  public AbstractTree(boolean callInitializer) {
    super(false);
    m_checkedNodes = new HashSet<>();
    m_deletedNodes = new HashMap<>();
    m_nodeFilters = new ArrayList<>(1);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfig(createLocalExtension(), this::initConfig);
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

  @Override
  public final <T> T optContribution(Class<T> contribution) {
    return m_contributionHolder.optContribution(contribution);
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

  /**
   * Multi-select is not supported by the HTML UI yet. Therefore the configured method is final for the moment.
   */
  @Order(40)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected final boolean getConfiguredMultiSelect() {
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
   * <p>
   * Configures the drag support of this tree.
   * </p>
   * <p>
   * Method marked as final as currently only drop is implemented for this field.
   * </p>
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(51)
  protected final int getConfiguredDragType() {
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
   * this property is set to {@code true}, the tree saves its scrollbars coordinates to the {@link AbstractClientSession} upon
   * detaching the UI component from Scout. The coordinates are restored (if the coordinates are available), when the UI
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
   * @see #getConfiguredCheckable()
   * @since 5.1
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  @Deprecated
  protected boolean getConfiguredAutoCheckChildNodes() {
    return false;
  }

  /**
   * Configures the default auto-check mode of the tree. There are three modes:
   * <ul>
   * <li>NONE: No nodes are auto-checked</li>
   * <li>CHILDREN: All child nodes will be checked/unchecked together with their parent</li>
   * <li>CHILDREN_AND_PARENT: The state of the node is a representation of its children</li>
   * </ul>
   * <p>
   * Only has an effect if the tree is checkable.
   *
   * @see AutoCheckStyle
   * @see #getConfiguredCheckable()
   * @since 5.1
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(105)
  protected AutoCheckStyle getConfiguredAutoCheckStyle() {
    return AutoCheckStyle.NONE;
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
   * @see #getConfiguredToggleBreadcrumbStyleEnabled()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * Configures whether the tree should automatically switch to the bread crumb style when getting smaller and back when
   * getting bigger. The threshold is determined by the GUI.
   * <p>
   * Subclasses can override this method. The default is false.
   *
   * @see #getConfiguredDisplayStyle()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(145)
  protected boolean getConfiguredToggleBreadcrumbStyleEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(155)
  protected CheckableStyle getConfiguredCheckableStyle() {
    return CheckableStyle.CHECKBOX_TREE_NODE;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredTextFilterEnabled() {
    return true;
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
  @Deprecated
  public boolean isAutoCheckChildNodes() {
    return getAutoCheckStyle().equals(AutoCheckStyle.CHILDREN);
  }

  @Override
  @Deprecated
  public void setAutoCheckChildNodes(boolean b) {
    setAutoCheckStyle(b ? AutoCheckStyle.CHILDREN : AutoCheckStyle.NONE);
  }

  @Override
  public AutoCheckStyle getAutoCheckStyle() {
    return propertySupport.getProperty(PROP_AUTO_CHECK_STYLE, AutoCheckStyle.class);
  }

  @Override
  public void setAutoCheckStyle(AutoCheckStyle autoCheckStyle) {
    propertySupport.setProperty(PROP_AUTO_CHECK_STYLE, autoCheckStyle);
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
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(18)
  protected void execAppLinkAction(String ref) {
  }

  /**
   * this method should not be implemented if you support {@link #interceptDrag(Collection)} (drag of
   * multiple nodes), as it takes precedence
   *
   * @return a transferable object representing the given row
   */
  @ConfigOperation
  @Order(20)
  protected final TransferObject execDrag(ITreeNode node) {
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

  @Override
  protected void initConfig() {
    super.initConfig();
    m_eventHistory = createEventHistory();
    m_eventBuffer = createEventBuffer();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setTitle(getConfiguredTitle());
    setIconId(getConfiguredIconId());
    setDefaultIconId(getConfiguredDefaultIconId());
    setCssClass(getConfiguredCssClass());
    setAutoTitle(getConfiguredAutoTitle());
    setCheckable(getConfiguredCheckable());
    setCheckableStyle(getConfiguredCheckableStyle());
    setTextFilterEnabled(getConfiguredTextFilterEnabled());
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
    setAutoCheckStyle(getConfiguredAutoCheckStyle());
    setLazyExpandingEnabled(getConfiguredLazyExpandingEnabled());
    setDisplayStyle(getConfiguredDisplayStyle());
    setToggleBreadcrumbStyleEnabled(getConfiguredToggleBreadcrumbStyleEnabled());
    setRootNode(new AbstractTreeNode() {
    });
    // add Convenience observer for drag & drop callbacks and event history
    addTreeListener(
        e -> {
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
        },
        TreeEvent.TYPE_NODES_DRAG_REQUEST,
        TreeEvent.TYPE_NODE_DROP_ACTION,
        TreeEvent.TYPE_NODES_SELECTED,
        TreeEvent.TYPE_NODES_CHECKED,
        TreeEvent.TYPE_NODE_DROP_TARGET_CHANGED,
        TreeEvent.TYPE_DRAG_FINISHED);
    // key shortcuts
    List<Class<? extends IKeyStroke>> configuredKeyStrokes = getConfiguredKeyStrokes();
    List<IKeyStroke> ksList = new ArrayList<>(configuredKeyStrokes.size());
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
    ksList.addAll(contributedKeyStrokes);

    m_baseKeyStrokes = ksList;
    setKeyStrokesInternal(m_baseKeyStrokes);

    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);

    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
      menus.addOrdered(menu);
    }
    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("Error occurred while dynamically contributing menus.", e);
    }

    menus.addAllOrdered(contributedMenus);
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    TreeContextMenu contextMenu = new TreeContextMenu(this, menus.getOrderedList());
    setContextMenuInternal(contextMenu);
  }

  /*
   * Runtime
   */
  @Override
  protected final void initInternal() {
    super.initInternal();
    initTreeInternal();
    interceptInitTree();
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getMenus(), getKeyStrokesInternal());
  }

  protected void initTreeInternal() {
  }

  @Override
  protected final void disposeInternal() {
    disposeTreeInternal();
    try {
      interceptDisposeTree();
    }
    catch (Exception e) {
      LOG.warn("Exception while disposing tree", e);
    }
    super.disposeInternal();
  }

  protected void disposeTreeInternal() {
    getRootNode().dispose();
    clearDeletedNodes();
  }

  @Override
  public final List<? extends ITreeExtension<? extends AbstractTree>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ITreeExtension<? extends AbstractTree> createLocalExtension() {
    return new LocalTreeExtension<>(this);
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

  @Override
  public boolean hasNodeFilters() {
    return !m_nodeFilters.isEmpty();
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
    List<ITreeNodeFilter> rejectingFilters = new ArrayList<>();
    inode.setFilterAccepted(true);
    inode.setRejectedByUser(false);
    if (!m_nodeFilters.isEmpty()) {
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
    return BEANS.get(TreeEventBuffer.class);
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
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public boolean isAutoTitle() {
    return FLAGS_BIT_HELPER.isBitSet(AUTO_TITLE, m_flags);
  }

  @Override
  public void setAutoTitle(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(AUTO_TITLE, b, m_flags);
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
  public boolean isToggleBreadcrumbStyleEnabled() {
    return propertySupport.getPropertyBool(PROP_TOGGLE_BREADCRUMB_STYLE_ENABLED);
  }

  @Override
  public void setToggleBreadcrumbStyleEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_TOGGLE_BREADCRUMB_STYLE_ENABLED, b);
  }

  @Override
  public CheckableStyle getCheckableStyle() {
    return (CheckableStyle) propertySupport.getProperty(PROP_CHECKABLE_STYLE);
  }

  @Override
  public void setCheckableStyle(CheckableStyle checkableStyle) {
    propertySupport.setProperty(PROP_CHECKABLE_STYLE, checkableStyle);
  }

  @Override
  public boolean isTextFilterEnabled() {
    return propertySupport.getPropertyBool(PROP_TEXT_FILTER_ENABLED);
  }

  @Override
  public void setTextFilterEnabled(boolean textFilterEnabled) {
    propertySupport.setProperty(PROP_TEXT_FILTER_ENABLED, textFilterEnabled);
  }

  @Override
  public String getPathText(ITreeNode selectedNode) {
    return getPathText(selectedNode, " - ");
  }

  @Override
  public String getPathText(ITreeNode selectedNode, String delimiter) {
    // construct the path to the data
    ITreeNode root = getRootNode();
    StringBuilder pathStr = new StringBuilder();
    ITreeNode node = selectedNode;
    while (node != null) {
      if (node != root || isRootNodeVisible()) {
        if (pathStr.length() != 0) {
          pathStr.insert(0, delimiter);
        }
        pathStr.insert(0, node.getCell().toPlainText());
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
    if (a != null && !a.isEmpty()) {
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

    final Set<Object> keySet = new HashSet<>(primaryKeys);
    CollectingVisitor<ITreeNode> v = new CollectingVisitor<>() {

      @Override
      public TreeVisitResult preVisit(ITreeNode element, int level, int index) {
        super.preVisit(element, level, index);
        if (keySet.isEmpty()) {
          return TreeVisitResult.TERMINATE;
        }
        return TreeVisitResult.CONTINUE;
      }

      @Override
      protected boolean accept(ITreeNode node) {
        return keySet.remove(node.getPrimaryKey());
      }
    };
    visitNode(getRootNode(), v);
    return v.getCollection();
  }

  @Override
  public void setRootNode(ITreeNode root) {
    if (m_rootNode != null) {
      m_rootNode.setTreeInternal(null, true);
      // inform root of remove
      m_rootNode.nodeRemovedNotify();
      m_rootNode.dispose();
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
  public void setNodeExpanded(ITreeNode node, boolean expanded) {
    boolean lazy;
    if (node.isExpanded() == expanded) {
      // no state change: Keep the current "expandedLazy" state
      lazy = node.isExpandedLazy();
    }
    else if (expanded) {
      // collapsed -> expanded: Set the "expandedLazy" state to the node's "lazyExpandingEnabled" flag
      lazy = node.isLazyExpandingEnabled();
    }
    else {
      // expanded -> collapsed: Set the "expandedLazy" state to false
      lazy = false;
    }
    setNodeExpanded(node, expanded, lazy);
  }

  @Override
  public void setNodeExpanded(ITreeNode node, boolean expand, boolean lazy) {
    // Never do lazy expansion if it is disabled on the tree
    if (!isLazyExpandingEnabled()) {
      lazy = false;
    }

    node = resolveNode(node);
    if (node != null && (node.isExpanded() != expand || node.isExpandedLazy() != lazy)) {
      setNodeExpandedInternal(node, expand, lazy);
    }
  }

  @Override
  public void setNodeExpandedInternal(ITreeNode node, boolean expand, boolean lazy) {
    if (expand) {
      node.ensureChildrenLoaded();
      ensureParentExpanded(node.getParentNode());
    }
    node.setExpandedInternal(expand);
    node.setExpandedLazyInternal(lazy);
    fireNodeExpanded(node, expand);
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
    return FLAGS_BIT_HELPER.isBitSet(AUTO_DISCARD_ON_DELETE, m_flags);
  }

  @Override
  public void setAutoDiscardOnDelete(boolean on) {
    m_flags = FLAGS_BIT_HELPER.changeBit(AUTO_DISCARD_ON_DELETE, on, m_flags);
  }

  @Override
  public void setNodeEnabledPermission(ITreeNode node, Permission p) {
    node = resolveNode(node);
    if (node == null) {
      return;
    }
    boolean oldValue = node.isEnabled();
    AbstractTreeNode.setEnabledPermission(p, node);
    boolean newValue = node.isEnabled();
    if (oldValue != newValue) {
      fireNodesUpdated(node.getParentNode(), CollectionUtility.hashSet(node));
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
  public void setNodeEnabled(ITreeNode node, boolean enabled) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isEnabled();
      node.setEnabled(enabled, IDimensions.ENABLED);
      boolean newValue = node.isEnabled();
      if (oldValue != newValue) {
        fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

  @Override
  public void setNodeEnabledGranted(ITreeNode node, boolean enabled) {
    node = resolveNode(node);
    if (node != null) {
      boolean oldValue = node.isEnabled();
      node.setEnabled(enabled, IDimensions.ENABLED_GRANTED);
      boolean newValue = node.isEnabled();
      if (oldValue != newValue) {
        fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
      }
    }
  }

  @Override
  public void setNodeVisiblePermission(ITreeNode node, Permission permission) {
    node = resolveNode(node);
    if (node != null) {
      AbstractTreeNode.setVisiblePermission(permission, node);
      // don't fire observers since visibility change only has an effect when used in init method
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
  public void setNodeVisible(ITreeNode node, boolean visible) {
    node = resolveNode(node);
    if (node != null) {
      node.setVisible(visible, IDimensions.VISIBLE);
      // don't fire observers since visibility change only has an effect when used in init method
    }
  }

  @Override
  public void setNodeVisibleGranted(ITreeNode node, boolean visible) {
    node = resolveNode(node);
    if (node != null) {
      node.setVisible(visible, IDimensions.VISIBLE_GRANTED);
      // don't fire observers since visibility change only has an effect when used in init method
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
    if (node != null && node.isLeaf() != b) {
      node.setLeafInternal(b);
      fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
    }
  }

  @Override
  public boolean isNodeChecked(ITreeNode node) {
    if (node != null) {
      return m_checkedNodes.contains(node);
    }
    else {
      return false;
    }
  }

  @Override
  public void setNodeChecked(ITreeNode node, boolean checked) {
    setNodesChecked(CollectionUtility.arrayList(node), checked);
  }

  @Override
  public void setNodeChecked(ITreeNode node, boolean checked, boolean enabledNodesOnly) {
    setNodesChecked(CollectionUtility.arrayList(node), checked, enabledNodesOnly);
  }

  @Override
  public void setNodesChecked(List<ITreeNode> nodes, boolean checked) {
    setNodesChecked(nodes, checked, false);
  }

  @Override
  public void setNodesChecked(List<ITreeNode> nodes, boolean checked, boolean enabledNodesOnly) {
    if (!isCheckable()) {
      return;
    }
    List<ITreeNode> changedNodes = new ArrayList<>();
    for (ITreeNode node : nodes) {
      node = resolveNode(node);
      if (node != null && node.isChecked() != checked && (!enabledNodesOnly || node.isEnabled())) {
        if (checked) {
          m_checkedNodes.add(node);
        }
        else {
          m_checkedNodes.remove(node);
        }
        changedNodes.add(node);

        //uncheck others in single-check mode
        if (checked && !isMultiCheck()) {
          for (Iterator<ITreeNode> it = m_checkedNodes.iterator(); it.hasNext();) {
            if (it.next() != node) {
              it.remove();
            }
          }
          break;
        }
      }
    }
    if (!changedNodes.isEmpty()) {
      if (m_currentParentNodes == null) {
        fireNodesChecked(changedNodes);
      }
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
    if (node != null && node.getStatus() != status) {
      node.setStatusInternal(status);
      fireNodesUpdated(node.getParentNode(), CollectionUtility.arrayList(node));
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
      List<ITreeNode> list = new ArrayList<>();
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
      }
      List<ITreeNode> children = parent.getChildNodes();
      for (ITreeNode child : children) {
        fetchAllCollapsingNodesRec(child, level + 1, list);
      }
    }
  }

  @Override
  public List<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.arrayList(getKeyStrokesInternal());
  }

  protected List<IKeyStroke> getKeyStrokesInternal() {
    return propertySupport.getPropertyList(PROP_KEY_STROKES);
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
      if (parent == null) {
        return; // wrong parent
      }
      List<ITreeNode> newChildren = new ArrayList<>(children);
      // Fire NODES_INSERTED event before actually inserting the nodes, because during insertion, other events might occur (e.g. NODE_CHANGED in decorateCell())
      fireNodesInserted(parent, newChildren);
      //
      ((AbstractTreeNode) parent).addChildNodesInternal(startIndex, children, true);
      // check if all children were added, or if some were revoked using
      // visible=false in init (addNotify) phase.
      newChildren.removeIf(child -> child.getParentNode() == null);
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
      if (parent == null) {
        return; // wrong parent
      }
      List<ITreeNode> newChildrenResolved = resolveNodes(newChildren);
      if (!newChildren.isEmpty() && newChildrenResolved.size() == newChildren.size()) {
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
  public TreeVisitResult visitTree(IDepthFirstTreeVisitor<ITreeNode> v) {
    return TreeUtility.visitNode(getRootNode(), v);
  }

  @Override
  public TreeVisitResult visitNode(ITreeNode node, IDepthFirstTreeVisitor<ITreeNode> v) {
    return TreeUtility.visitNode(node, v);
  }

  @Override
  public TreeVisitResult visitVisibleTree(IDepthFirstTreeVisitor<ITreeNode> v) {
    Function<ITreeNode, Collection<? extends ITreeNode>> childrenSupplier = ITreeNode::getFilteredChildNodes;

    if (isRootNodeVisible()) {
      return TreeTraversals.create(v, childrenSupplier).traverse(getRootNode());
    }

    List<ITreeNode> visibleTopLevel = new ArrayList<>(childrenSupplier.apply(getRootNode()));
    return TreeUtility.visitNodes(visibleTopLevel, v, childrenSupplier);
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
      protected boolean accept(ITreeNode node) {
        return node.isStatusInserted();
      }
    };
    visitNode(getRootNode(), v);
    return v.getCount();
  }

  @Override
  public Set<ITreeNode> getInsertedNodes() {
    CollectingVisitor<ITreeNode> v = new CollectingVisitor<>() {
      @Override
      protected boolean accept(ITreeNode element) {
        return element.isStatusInserted();
      }
    };
    visitNode(getRootNode(), v);
    return CollectionUtility.hashSet(v.getCollection());
  }

  @Override
  public int getUpdatedNodeCount() {
    P_AbstractCountingTreeVisitor v = new P_AbstractCountingTreeVisitor() {
      @Override
      protected boolean accept(ITreeNode node) {
        return node.isStatusUpdated();
      }
    };
    visitNode(getRootNode(), v);
    return v.getCount();
  }

  @Override
  public Set<ITreeNode> getUpdatedNodes() {
    CollectingVisitor<ITreeNode> v = new CollectingVisitor<>() {
      @Override
      protected boolean accept(ITreeNode element) {
        return element.isStatusUpdated();
      }
    };
    visitNode(getRootNode(), v);
    return CollectionUtility.hashSet(v.getCollection());
  }

  @Override
  public int getSelectedNodeCount() {
    return m_selectedNodes.size();
  }

  @Override
  public ITreeNode getSelectedNode() {
    if (!m_selectedNodes.isEmpty()) {
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
    if (nodes == null) {
      nodes = CollectionUtility.hashSet();
    }
    Set<ITreeNode> newSelection = new HashSet<>();
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
      final Holder<ITreeNode> next = new Holder<>(ITreeNode.class);
      IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<>() {
        boolean m_foundCurrent;

        @Override
        public TreeVisitResult preVisit(ITreeNode element, int level, int index) {
          if (m_foundCurrent) {
            if (element.isFilterAccepted()) {
              next.setValue(element);
              return TreeVisitResult.TERMINATE;
            }
          }
          else {
            m_foundCurrent = element == current;
          }
          return TreeVisitResult.CONTINUE;
        }
      };
      visitVisibleTree(v);
      if (next.getValue() != null) {
        selectNode(next.getValue());
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
      final Holder<ITreeNode> foundVisited = new Holder<>(ITreeNode.class);
      IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<>() {
        @Override
        public TreeVisitResult preVisit(ITreeNode element, int level, int index) {
          if (element == current) {
            return TreeVisitResult.TERMINATE;
          }
          if (element.isFilterAccepted()) {
            foundVisited.setValue(element);
          }
          return TreeVisitResult.CONTINUE;
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
  public ITreeNode selectFirstNode() {
    if (!isRootNodeVisible()) {
      getRootNode().ensureChildrenLoaded();
    }

    final Holder<ITreeNode> foundVisited = new Holder<>(ITreeNode.class);
    IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<>() {
      @Override
      public TreeVisitResult preVisit(ITreeNode element, int level, int index) {
        if (element.isFilterAccepted()) {
          foundVisited.setValue(element);
          return TreeVisitResult.TERMINATE;
        }
        return TreeVisitResult.CONTINUE;
      }
    };
    visitVisibleTree(v);

    ITreeNode firstNode = foundVisited.getValue();
    if (firstNode != null) {
      selectNode(firstNode);
    }
    return firstNode;
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
    final Holder<ITreeNode> foundVisited = new Holder<>(ITreeNode.class);
    IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<>() {
      @Override
      public TreeVisitResult preVisit(ITreeNode element, int level, int index) {
        if (element.isFilterAccepted()) {
          foundVisited.setValue(element);
        }
        return TreeVisitResult.CONTINUE;
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
        if ((parent != getRootNode() || isRootNodeVisible()) && parent.isFilterAccepted()) {
          selectNode(parent);
          return;
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
      Set<ITreeNode> oldSelection = new HashSet<>(m_selectedNodes);
      Set<ITreeNode> newSelection = new HashSet<>();
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
    if (node == null) {
      return null;
    }
    if (node.getTree() == this) {
      return node;
    }
    return null;
  }

  /**
   * Keeps order of input.
   */
  private List<ITreeNode> resolveNodes(Collection<? extends ITreeNode> nodes) {
    if (!CollectionUtility.hasElements(nodes)) {
      return CollectionUtility.emptyArrayList();
    }
    List<ITreeNode> resolvedNodes = new ArrayList<>(nodes.size());
    for (ITreeNode node : nodes) {
      if (resolveNode(node) != null) {
        resolvedNodes.add(node);
      }
    }
    return resolvedNodes;
  }

  @Override
  public TreeListeners treeListeners() {
    return m_listeners;
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
    nodes.removeIf(node -> node != null && node.isInitializing());
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
    Set<ITreeNode> deselectedNodes = new HashSet<>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes);
    Set<ITreeNode> newSelectedNodes = new HashSet<>(newSelection);
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
    Set<ITreeNode> deselectedNodes = new HashSet<>(oldSelection);
    deselectedNodes.removeAll(newSelection);
    e.setDeselectedNodes(deselectedNodes);
    Set<ITreeNode> newSelectedNodes = new HashSet<>(newSelection);
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

  protected void updateNodeMenus(Set<ITreeNode> newSelectedNodes) {
    // remove old
    if (m_currentNodeMenus != null) {
      getContextMenu().removeChildActions(m_currentNodeMenus);
      m_currentNodeMenus = null;
    }
    // take only first node to avoid having multiple same menus due to all nodes.
    if (CollectionUtility.hasElements(newSelectedNodes)) {
      List<IMenu> nodeMenus = new ArrayList<>(CollectionUtility.firstElement(newSelectedNodes).getMenus());
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

  private void fireNodeAction(ITreeNode node) {
    if (isActionRunning()) {
      return;
    }
    if (node == null || node.isInitializing() || !node.isLeaf()) {
      return;
    }

    try {
      setActionRunning(true);
      try {
        interceptNodeAction(node);
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
    finally {
      setActionRunning(false);
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
      doFireTreeEvent(e);
    }
  }

  protected void doFireTreeEvent(TreeEvent e) {
    m_listeners.fireEvent(e);
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
    if (!m_nodeDecorationBuffer.isEmpty()) {
      Set<ITreeNode> set = m_nodeDecorationBuffer;
      m_nodeDecorationBuffer = new HashSet<>();
      try {
        setTreeChanging(true);
        for (ITreeNode node : set) {
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
        m_listeners.fireEvents(list);
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
    if (isActionRunning()) {
      return;
    }

    try {
      setActionRunning(true);
      interceptAppLinkAction(ref);
    }
    finally {
      setActionRunning(false);
    }
  }

  @Override
  public void exportTreeData(final AbstractTreeFieldData target) {
    exportTreeNodeDataRec(getRootNode().getChildNodes(), target, null);
  }

  private void exportTreeNodeDataRec(List<ITreeNode> nodes, AbstractTreeFieldData treeData, TreeNodeData parentNodeData) {
    List<TreeNodeData> nodeDataList = new ArrayList<>(nodes.size());
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
   * @return the new tree node for this node data or null to skip this node. It is the responsibility of this method to
   *         add the new node to the tree.
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

  private boolean isActionRunning() {
    return FLAGS_BIT_HELPER.isBitSet(ACTION_RUNNING, m_flags);
  }

  private void setActionRunning(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ACTION_RUNNING, b, m_flags);
  }

  @Override
  public boolean isSaveAndRestoreScrollbars() {
    return FLAGS_BIT_HELPER.isBitSet(SAVE_AND_RESTORE_SCROLLBARS, m_flags);
  }

  @Override
  public void setSaveAndRestoreScrollbars(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(SAVE_AND_RESTORE_SCROLLBARS, b, m_flags);
  }

  private abstract static class P_AbstractCountingTreeVisitor extends DepthFirstTreeVisitor<ITreeNode> {

    private int m_count;

    @Override
    public TreeVisitResult preVisit(ITreeNode element, int level, int index) {
      if (accept(element)) {
        m_count++;
      }
      return TreeVisitResult.CONTINUE;
    }

    protected boolean accept(ITreeNode node) {
      return true;
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
          nodes = resolveNodes(nodes);
          if (!nodes.isEmpty()) {
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
          msg.append(node.getCell().toPlainText());
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
          node = resolveNode(node);
          if (node != null && (node.isExpanded() != on || node.isExpandedLazy() != lazy)) {
            if (on && (node.isChildrenDirty() || node.isChildrenVolatile())) {
              node.loadChildren();
            }
            setNodeExpanded(node, on, lazy);
          }
        }
        finally {
          setTreeChanging(false);
        }
      }
      catch (RuntimeException e) {
        if (node != null) {
          throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("node", node.getCell().toPlainText());
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
        try {
          setTreeChanging(true);
          node = resolveNode(node);
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
              .withContextInfo("cell", node.getCell().toPlainText());
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

          List<ITreeNode> validNodes = resolveNodes(nodes);

          // remove filtered (invisible) nodes from selection
          validNodes.removeIf(iTreeNode -> !iTreeNode.isFilterAccepted());

          // load children for selection
          for (ITreeNode node : validNodes) {
            if (node.isChildrenLoaded() && (node.isChildrenDirty() || node.isChildrenVolatile())) {
              node.loadChildren();
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
        node = resolveNode(node);
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
        Collection<ITreeNode> nodes = getSelectedNodes();
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
        node = resolveNode(node);
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
        node = resolveNode(node);
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
