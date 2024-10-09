/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.ComputeParentTablePageMenusChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageCalculateVisibleChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDetailFormActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitDetailFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageReloadPageChain;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.MenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper.IMenuTypeMapper;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.ITileOverviewForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("ef0d789e-dfbf-4715-9ab7-eedaefc936f3")
public abstract class AbstractPage<T extends ITable> extends AbstractTreeNode implements IPage<T>, IContributionOwner, IExtensibleObject {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPage.class);

  private static final String TABLE_VISIBLE = "TABLE_VISIBLE";
  private static final String DETAIL_FORM_VISIBLE = "DETAIL_FORM_VISIBLE";
  private static final String SHOW_TILE_OVERVIEW = "SHOW_TILE_OVERVIEW";
  private static final String NAVIGATE_BUTTONS_VISIBLE = "NAVIGATE_BUTTONS_VISIBLE";
  private static final String COMPACT_ROOT = "COMPACT_ROOT";
  private static final String PAGE_MENUS_ADDED = "PAGE_MENUS_ADDED";
  private static final String PAGE_ACTIVE = "PAGE_ACTIVE";
  private static final String PAGE_ACTIVATED = "PAGE_ACTIVATED";
  static final String SEARCH_REQUIRED = "SEARCH_REQUIRED";
  static final String SEARCH_ACTIVE = "SEARCH_ACTIVE";
  static final String LIMITED_RESULT = "LIMITED_RESULT";
  static final String ALWAYS_CREATE_CHILD_PAGE = "ALWAYS_CREATE_CHILD_PAGE";

  static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(TABLE_VISIBLE, DETAIL_FORM_VISIBLE, PAGE_MENUS_ADDED,
      LIMITED_RESULT, ALWAYS_CREATE_CHILD_PAGE, SEARCH_ACTIVE, SEARCH_REQUIRED, PAGE_ACTIVE);
  static final NamedBitMaskHelper FLAGS2_BIT_HELPER = new NamedBitMaskHelper(PAGE_ACTIVATED, SHOW_TILE_OVERVIEW, NAVIGATE_BUTTONS_VISIBLE);
  private static final IMenuTypeMapper TREE_MENU_TYPE_MAPPER = menuType -> {
    if (menuType == TreeMenuType.SingleSelection) {
      return TableMenuType.EmptySpace;
    }
    return menuType;
  };

  private T m_table;
  private IForm m_detailForm;
  private String m_overviewIconId;
  private IDataChangeListener m_internalDataChangeListener;
  private final TreeListener m_localTreeListener;
  private final String m_userPreferenceContext;
  private final Map<ITableRow, IPage> m_tableRowToPageMap = new HashMap<>();
  private final Map<IPage, ITableRow> m_pageToTableRowMap = new HashMap<>();

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #TABLE_VISIBLE}, {@link #DETAIL_FORM_VISIBLE}, {@link #PAGE_MENUS_ADDED},
   * {@link #SEARCH_REQUIRED}, {@link #SEARCH_ACTIVE}, {@link #LIMITED_RESULT}, {@link #ALWAYS_CREATE_CHILD_PAGE},
   * {@link #PAGE_ACTIVE}
   */
  byte m_flags;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #PAGE_ACTIVATED}, {@link #SHOW_TILE_OVERVIEW}, {@link #COMPACT_ROOT}
   */
  byte m_flags2;

  private final ObjectExtensions<AbstractPage, IPageExtension<? extends AbstractPage>> m_objectExtensions;

  protected IContributionOwner m_contributionHolder;

  @Override
  protected List<IMenu> lazyCreateAndInitializeMenus() {
    if (isInitializing()) {
      LOG.warn(
          "Menus in page {} are now created during page init. This is not recommended. The menus should be created lazily when the page is activated. "
              + "Use e.g. the execInitTable() callback to access the table after it has been created.",
          getClass(), new Exception("origin"));
    }
    AtomicReference<List<IMenu>> ref = new AtomicReference<>();
    createDisplayParentRunContext()
        .run(
            () -> runInExtensionContext(() -> ref.set(super.lazyCreateAndInitializeMenus())));
    return ref.get();
  }

  @Override
  public T getTable() {
    return getTable(true);
  }

  @Override
  public T getTable(boolean create) {
    if (create && m_table == null) {
      if (isInitializing()) {
        LOG.warn(
            "Table in page {} is created during page init. This is not recommended. The table should be created lazily when the page is activated. "
                + "Use e.g. the execInitTable() callback to access the table after it has been created.",
            getClass(), new Exception("origin"));
      }
      if (isDisposing()) {
        LOG.warn(
            "Table in page {} is created during page disposal. This is not recommended. Consider using getTable(false) during the disposal phase.",
            getClass(), new Exception("origin"));
      }

      createDisplayParentRunContext()
          .run(
              () -> runInExtensionContext(() -> {
                m_table = createTable();
                if (m_table != null) {
                  m_table.init(); // calls execInitTable of AbstractTable
                  firePageChanged();
                  addDefaultTableControls();
                  interceptInitTable(); // calls execInitTable of AbstractPage
                  fireAfterTableInit();
                }
              }));
    }
    return m_table;
  }

  /**
   * Creates a new {@link ClientRunContext} to be used for executing model logic in the context of a suitable display
   * parent.
   *
   * @return Returns a {@link ClientRunContext} created by {@link IOutline#createDisplayParentRunContext()} or just a
   *         copy of the current one, if {@link #getOutline()} returns <code>null</code>. Never <code>null</code>.
   * @since 7.0
   */
  protected ClientRunContext createDisplayParentRunContext() {
    final IOutline outline = getOutline();
    if (outline != null) {
      return outline.createDisplayParentRunContext();
    }
    return ClientRunContexts.copyCurrent();
  }

  protected void linkTableRowWithPage(ITableRow tableRow, IPage<?> page) {
    m_tableRowToPageMap.put(tableRow, page);
    m_pageToTableRowMap.put(page, tableRow);
  }

  protected void unlinkAllTableRowWithPage() {
    m_tableRowToPageMap.clear();
    m_pageToTableRowMap.clear();
  }

  @Override
  public ITreeNode getTreeNodeFor(ITableRow tableRow) {
    if (tableRow == null) {
      return null;
    }
    return m_tableRowToPageMap.get(tableRow);
  }

  @Override
  public IPage<?> getPageFor(ITableRow tableRow) {
    return (IPage) getTreeNodeFor(tableRow);
  }

  @Override
  public ITableRow getTableRowFor(ITreeNode childPage) {
    //noinspection SuspiciousMethodCalls
    return m_pageToTableRowMap.get(childPage);
  }

  @Override
  public List<ITableRow> getTableRowsFor(Collection<? extends ITreeNode> childPageNodes) {
    List<ITableRow> result = new ArrayList<>();
    for (ITreeNode node : childPageNodes) {
      ITableRow row = getTableRowFor(node);
      if (row != null) {
        result.add(row);
      }
    }
    return result;
  }

  protected void unlinkTableRowWithPage(ITableRow tableRow) {
    IPage<?> page = m_tableRowToPageMap.remove(tableRow);
    if (page != null) {
      m_pageToTableRowMap.remove(page);
    }
  }

  /**
   * use this static method to create a string based on the vargs that can be used as userPreferenceContext
   */
  public static String createUserPreferenceContext(Object... vargs) {
    StringBuilder buf = new StringBuilder();
    if (vargs != null) {
      for (Object o : vargs) {
        if (buf.length() > 0) {
          buf.append(",");
        }
        if (o == null) {
          buf.append("null");
        }
        else if (o instanceof Object[]) {
          buf.append(Arrays.toString((Object[]) o));
        }
        else {
          buf.append(o);
        }
      }
    }
    return buf.toString();
  }

  public AbstractPage() {
    this(true);
  }

  public AbstractPage(String userPreferenceContext) {
    this(true, userPreferenceContext);
  }

  public AbstractPage(boolean callInitializer) {
    this(callInitializer, null);
  }

  public AbstractPage(boolean callInitializer, String userPreferenceContext) {
    super(false);
    m_userPreferenceContext = userPreferenceContext;
    m_localTreeListener = createLocalTreeListener();
    m_objectExtensions = new ObjectExtensions<>(this, false);
    m_contributionHolder = new ContributionComposite(this);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void callInitializer() {
    if (!isInitialized()) {
      interceptInitConfig();
      super.callInitializer();
    }
  }
  /*
   * Configuration
   */

  /**
   * Configures the visibility of this page's table. Typical subclasses of this abstract class use a tabular structure
   * to display data, this includes {@link AbstractPageWithTable} as well as {@link AbstractPageWithNodes}. Set this
   * property to {@code false} if you want to display a detail form within this page.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this page's table is visible, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(35)
  protected boolean getConfiguredTableVisible() {
    return true;
  }

  /**
   * Configures if a configured detail form should be visible by default.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @see #getConfiguredDetailForm() on how to configure a detail form.
   * @see #setDetailFormVisible(boolean) on how to change the visibility of a detail form
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(91)
  protected boolean getConfiguredDetailFormVisible() {
    return true;
  }

  /**
   * Configures if the tile overview should be shown by default. If set to {@code true},
   * {@link #ensureDetailFormCreated()} will create the tile overview as detail form. It will only be visible if
   * {@link #getConfiguredDetailFormVisible()} is set to {@code true}.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(37)
  protected boolean getConfiguredShowTileOverview() {
    return false;
  }

  /**
   * Configures if the navigation buttons up and down should be inserted
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(38)
  protected boolean getConfiguredNavigateButtonsVisible() {
    return true;
  }

  /**
   * Configures the title of this page. The title is typically displayed on the GUI, e.g. as part of the representation
   * of this page as a tree node.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the title for this page
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(40)
  protected String getConfiguredTitle() {
    return null;
  }

  /**
   * Configures the icon for this page. The icon is typically used to represent this page in the GUI, e.g. as part of
   * the representation of this page as a tree node.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(50)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * @return the icon ID which is used for icons in the tile outline overview.
   */
  protected String getConfiguredOverviewIconId() {
    return null;
  }

  /**
   * Configures the detail form to be used with this page. The form is lazily {@linkplain #ensureDetailFormCreated()
   * created} and {@linkplain #ensureDetailFormStarted() started}.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a form type token
   * @see #startDetailForm() for details how the form gets started
   */
  @ConfigProperty(ConfigProperty.FORM)
  @Order(90)
  protected Class<? extends IForm> getConfiguredDetailForm() {
    return null;
  }

  /**
   * Called during the permission check for this page. The returned value will be used to determine the visibleGranted
   * Property of this page. Depending on the context this page is used, this method is called on initialization or
   * before it's data is loaded. Subclasses can change the default behavior (on initialization / early) by overriding
   * {@link #isCalculateVisibleLate()}.
   *
   * @see #calculateVisibleInternal()
   */
  @ConfigOperation
  @Order(100)
  protected boolean execCalculateVisible() {
    return true;
  }

  /**
   * Called after this page has been added to the outline tree. This method may set a detail form or check some
   * parameters.
   * <p>
   * Do not load table data here, this should be done lazily in
   * {@link AbstractPageWithTable#execLoadData(SearchFilter)}.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @see #interceptPageActivated()
   */
  @ConfigOperation
  @Order(40)
  protected void execInitPage() {
  }

  /**
   * Called after this page has been removed from its associated outline tree.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(50)
  protected void execDisposePage() {
  }

  /**
   * Called by the data change listener registered with this page (and the current desktop) through
   * {@link #registerDataChangeListener(Object...)}. Use this callback method to react to data change events by
   * reloading current data, or throwing away cached data etc.
   * <p>
   * Subclasses can override this method.<br/>
   * This default implementation does the following:
   * <ol>
   * <li>if this page is an ancestor of the selected page (or is selected itself) and this page is in the active
   * outline, a full re-load of the page is performed
   * <li>else the children of this page are marked dirty and the page itself is unloaded
   * </ol>
   *
   * @see IDesktop#dataChanged(Object...) and
   *      {@link IDesktop#fireDataChangeEvent(org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent)}
   */
  @ConfigOperation
  @Order(55)
  protected void execDataChanged(Object... dataTypes) {
    if (getTree() == null) {
      return;
    }
    //
    Set<ITreeNode> pathsToSelections = new HashSet<>();
    for (ITreeNode node : getTree().getSelectedNodes()) {
      ITreeNode tmp = node;
      while (tmp != null) {
        pathsToSelections.add(tmp);
        tmp = tmp.getParentNode();
      }
    }
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    final boolean isActiveOutline = desktop != null && desktop.getOutline() == this.getOutline();
    final boolean isRootNode = pathsToSelections.isEmpty() && getTree() != null && getTree().getRootNode() == this;
    if (isActiveOutline && (pathsToSelections.contains(this) || isRootNode)) {
      try {
        //TODO [7.0] fko: maybe remove when bookmarks can be done on outline level? (currently only pages)
        if (isRootNode) {
          this.reloadPage(IReloadReason.DATA_CHANGED_TRIGGER);
        }
        else if (desktop != null) { // NOSONAR
          /*
           * Ticket 77332 (deleting a node in the tree) also requires a reload So
           * the selected and its ancestor nodes require same processing
           */
          desktop.reloadPageFromRoot(this);
        }
      }
      catch (RuntimeException | PlatformError e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
    else {
      // not active outline OR not on selection path
      setChildrenDirty(true);
      if (isExpanded()) {
        setExpanded(false);
      }
      try {
        if (isChildrenLoaded()) {
          getTree().unloadNode(this);
        }
      }
      catch (RuntimeException | PlatformError e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  /**
   * Called in order to reload the page data. The default calls {@link #loadChildren()} inside a try finally block with
   * {@link #getTree()} and {@link ITree#setTreeChanging(boolean)}.
   *
   * @param reloadReason
   *          {@link IReloadReason}
   */
  @ConfigOperation
  @Order(58)
  protected void execReloadPage(String reloadReason) {
    ITree tree = getTree();
    if (tree == null) {
      return;
    }
    try {
      tree.setTreeChanging(true);
      loadChildren();
    }
    finally {
      tree.setTreeChanging(false);
    }
  }

  /**
   * Called after this page has (re)loaded its data. This method is called after {@link #loadChildren()} has been
   * called.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(60)
  protected void execPageDataLoaded() {
  }

  /**
   * Called whenever this page is selected in the outline tree.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(70)
  protected void execPageActivated() {
  }

  /**
   * Called whenever this page is de-selected in the outline tree.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(80)
  protected void execPageDeactivated() {
  }

  /**
   * Initializes the detail form associated with this page. This method is called before the detail form is used for the
   * first time.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @see #ensureDetailFormCreated()
   * @see #ensureDetailFormStarted()
   */
  @ConfigOperation
  @Order(120)
  protected void execInitDetailForm() {
  }

  /**
   * Callback executed when the {@link ITable} of this {@link IPage} is created.
   * <p>
   * This may be useful if an abstract page itself has no {@link ITable} but the sub-class has and the parent page wants
   * to be notified when the sub-class creates its table.
   * <p>
   * If this page itself already has a table the callback of the {@link ITable} itself
   * ({@link AbstractTable#execInitTable}) should be used instead.
   */
  @Order(130)
  @ConfigOperation
  protected void execInitTable() {
  }

  @Order(140)
  @ConfigOperation
  protected void execDetailFormActivated() {
    IForm detailForm = getDetailForm();
    if (detailForm != null) {
      detailForm.getUIFacade().fireFormActivatedFromUI();
    }
  }

  /**
   * The default implementation returns the single selection menus from the parent table page's table.
   * <p>
   * If this behavior is not desired return an empty list or filter the menus for your needs instead.
   *
   * @param parentTablePage
   *          Parent table page
   * @return A list (non-null) of single selection menus.
   */
  @Order(150)
  @ConfigOperation
  protected List<IMenu> execComputeParentTablePageMenus(IPageWithTable<?> parentTablePage) {
    ITableRow row = parentTablePage.getTableRowFor(this);
    if (row == null) {
      return CollectionUtility.emptyArrayList();
    }
    ITable table = parentTablePage.getTable();
    table.getUIFacade().setSelectedRowsFromUI(CollectionUtility.arrayList(row));
    return ActionUtility.getActions(table.getContextMenu().getChildActions(), ActionUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TableMenuType.SingleSelection), false));
  }

  protected abstract T createTable();

  @Override
  protected void initConfig() {
    super.initConfig();
    setTableVisible(getConfiguredTableVisible());
    setDetailFormVisible(getConfiguredDetailFormVisible());
    setShowTileOverview(getConfiguredShowTileOverview());
    setOverviewIconId(getConfiguredOverviewIconId());
    setNavigateButtonsVisible(getConfiguredNavigateButtonsVisible());
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  /*
   * Runtime
   */
  @Override
  public void initPage() {
    setInitializing(true);
    try {
      Cell cell = getCellForUpdate();
      if (cell.getText() == null && getConfiguredTitle() != null) {
        cell.setText(getConfiguredTitle());
      }
      if (cell.getIconId() == null && getConfiguredIconId() != null) {
        cell.setIconId(getConfiguredIconId());
      }
      interceptInitPage();
      // early permission check is done on initialization
      if (!doCalculateVisibleLate()) {
        calculateVisibleInternal();
      }
      fireAfterPageInit();
    }
    finally {
      setInitializing(false);
    }
  }

  /**
   * Override this method to control when permission checks for child pages should be executed. Default is {@code false}
   * to perform permission checks on initialization.
   *
   * @return {@code true} for a late (before loading data), {@code false} for an early (on initialization) permission
   *         check
   */
  protected boolean isCalculateVisibleLate() {
    return false;
  }

  protected boolean doCalculateVisibleLate() {
    IPage<?> parentPage = getParentPage();
    if (parentPage instanceof AbstractPage<?>) {
      return ((AbstractPage<?>) parentPage).isCalculateVisibleLate();
    }
    return false;
  }

  /**
   * Do not use this internal method
   */
  protected void calculateVisibleInternal() {
    setVisibleGranted(isVisible() && interceptCalculateVisible());
  }

  /**
   * Adds default table controls to the table.
   * <p>
   * Default does nothing.
   */
  protected void addDefaultTableControls() {
  }

  @Override
  public IStatus getTableStatus() {
    T table = getTable();
    if (table != null) {
      return table.getTableStatus();
    }
    return null;
  }

  @Override
  public void setTableStatus(IStatus tableStatus) {
    T table = getTable();
    if (table != null) {
      table.setTableStatus(tableStatus);
    }
  }

  @Override
  public boolean isTableStatusVisible() {
    T table = getTable();
    if (table != null) {
      return table.isTableStatusVisible();
    }
    return false;
  }

  @Override
  public void setTableStatusVisible(boolean tableStatusVisible) {
    T table = getTable();
    if (table != null) {
      table.setTableStatusVisible(tableStatusVisible);
    }
  }

  @Override
  public final String getUserPreferenceContext() {
    return m_userPreferenceContext;
  }

  @Override
  public IOutline getOutline() {
    return (IOutline) getTree();
  }

  @Override
  public IPage<?> getParentPage() {
    return (IPage) getParentNode();
  }

  @Override
  public void setTreeInternal(ITree tree, boolean includeSubtree) {
    ITree oldTree = getTree();
    if (oldTree != null) {
      oldTree.removeTreeListener(m_localTreeListener);
    }
    super.setTreeInternal(tree, includeSubtree);
    if (tree == null) {
      return;
    }
    tree.addTreeListener(m_localTreeListener, TreeEvent.TYPE_NODES_UPDATED);
  }

  @Override
  public IPage<?> getChildPage(final int childIndex) {
    return (IPage) getChildNode(childIndex);
  }

  @Override
  public List<IPage<?>> getChildPages() {
    List<IPage<?>> childPages = new ArrayList<>();
    for (ITreeNode childNode : getChildNodes()) {
      childPages.add((IPage) childNode);
    }
    return childPages;
  }

  @Override
  public void nodeAddedNotify() {
    try {

      // do also set initializing even though it is also set in initPage().
      // This ensures the page is also initializing if initPage has been overwritten.
      setInitializing(true);
      try {
        initPage();
      }
      finally {
        setInitializing(false);
      }
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void disposeInternal() {
    super.disposeInternal();
    try {
      interceptDisposePage();
      disposeDetailForm();
      disposeTable();
      fireAfterPageDispose();
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
    // automatically remove all data change listeners
    ITree tree = getTree();
    if (tree != null) {
      tree.removeTreeListener(m_localTreeListener);
    }
    unregisterDataChangeListener();
  }

  @Override
  public void pageActivatedNotify() {
    try {
      ensureDetailFormCreated();
      ensureDetailFormStarted();
      interceptDetailFormActivated();
      enhanceTableWithPageMenus();
      interceptPageActivated();
      setPageActive(true);
      firePageActivated();
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void pageDeactivatedNotify() {
    try {
      interceptPageDeactivated();
      setPageActive(false);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public boolean isPageActive() {
    return FLAGS_BIT_HELPER.isBitSet(PAGE_ACTIVE, m_flags);
  }

  protected void setPageActive(boolean active) {
    m_flags = FLAGS_BIT_HELPER.changeBit(PAGE_ACTIVE, active, m_flags);
    if (active) {
      m_flags2 = FLAGS2_BIT_HELPER.setBit(PAGE_ACTIVATED, m_flags2);
    }
  }

  @Override
  public boolean hasBeenActivated() {
    return FLAGS2_BIT_HELPER.isBitSet(PAGE_ACTIVATED, m_flags2);
  }

  private boolean isPageMenusAdded() {
    return FLAGS_BIT_HELPER.isBitSet(PAGE_MENUS_ADDED, m_flags);
  }

  private void setPageMenusAdded() {
    m_flags = FLAGS_BIT_HELPER.setBit(PAGE_MENUS_ADDED, m_flags);
  }

  protected void enhanceTableWithPageMenus() {
    if (isPageMenusAdded()) {
      return;
    }
    setPageMenusAdded();
    ITable table = getTable();
    if (table != null) {
      ITableContextMenu contextMenu = table.getContextMenu();
      List<IMenu> menus = contextMenu.getChildActions();
      for (IMenu menu : getOutline().getContextMenu().getChildActions()) {
        if (!MenuWrapper.containsWrappedMenu(table.getMenus(), menu)) {
          // mapping from TreeMenuType to TableMenuType
          menus.add(MenuWrapper.wrapMenu(menu, TREE_MENU_TYPE_MAPPER));
        }
      }
      if (!CollectionUtility.equalsCollection(menus, contextMenu.getChildActions())) {
        contextMenu.setChildActions(menus);
      }
    }
  }

  protected IForm createDetailForm() {
    if (getConfiguredDetailForm() == null) {
      return null;
    }
    return createDisplayParentRunContext()
        .call(() -> getConfiguredDetailForm().getConstructor().newInstance());
  }

  protected ITileOverviewForm createTileOverviewForm() {
    return new P_TileOverviewForm();
  }

  /**
   * Starts the form.
   * <p>
   * The default uses {@link IForm#start()} and therefore expects a form handler to be previously set. Override to call
   * a custom start method or implement a {@link IForm#start()} on the detail form.
   */
  protected void startDetailForm() {
    getDetailForm().start();
  }

  protected void ensureDetailFormCreated() {
    if (getDetailForm() != null) {
      return;
    }
    IForm form;
    if (isShowTileOverview()) {
      form = createTileOverviewForm();
    }
    else {
      form = createDetailForm();
    }
    if (form != null) {
      setDetailForm(form);
      interceptInitDetailForm();
    }
  }

  protected void ensureDetailFormStarted() {
    if (getDetailForm() == null || !getDetailForm().isFormStartable()) {
      return;
    }
    startDetailForm();
  }

  protected void disposeDetailForm() {
    if (getDetailForm() != null) {
      getDetailForm().doClose();
      setDetailForm(null);
    }
  }

  protected void disposeTable() {
    T table = getTable(false);
    if (table != null) {
      table.dispose();
      setTableStatus(null);
    }
  }

  @Override
  public IForm getDetailForm() {
    return m_detailForm;
  }

  @Override
  public void setDetailForm(IForm form) {
    m_detailForm = form;
    if (m_detailForm != null) {
      decorateDetailForm();
    }
    firePageChanged();
    if (isSelectedNode()) {
      getOutline().setDetailForm(m_detailForm);
    }
  }

  protected void decorateDetailForm() {
    IForm form = getDetailForm();
    if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
      form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
    }
    if (form.getDisplayViewId() == null) {
      form.setDisplayViewId(IForm.VIEW_ID_PAGE_DETAIL);
    }

    form.setModal(false); // TODO [7.0] bsh: do the same as in todo of WrappedFormField
    form.setShowOnStart(false);
  }

  @Override
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = event -> dataChanged(event.getDataType());
    }
    IDesktop.CURRENT.get().dataChangeDesktopInForegroundListeners().add(m_internalDataChangeListener, true, dataTypes);
  }

  @Override
  public void dataChanged(final Object... dataTypes) {
    createDisplayParentRunContext()
        .run(() -> {
          try {
            interceptDataChanged(dataTypes);
          }
          catch (Exception e) {
            BEANS.get(ExceptionHandler.class).handle(e);
          }
        });
  }

  @Override
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      IDesktop.CURRENT.get().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  @Override
  public final void reloadPage(String reloadReason) {
    interceptReloadPage(reloadReason);
  }

  @Override
  public void loadChildren() {
    if (doCalculateVisibleLate()) {
      // late permission check is done just before loading the data to avoid unnecessary load operations
      calculateVisibleInternal();
    }
    if (!isVisible()) {
      throw new VetoException(TEXTS.get("ErrorTitleSecurity"));
    }
    super.loadChildren();
    loadChildrenImpl();
    interceptPageDataLoaded();
  }

  protected void loadChildrenImpl() {
  }

  @Override
  public List<IMenu> computeParentTablePageMenus(IPageWithTable<?> parentTablePage) {
    return interceptComputeParentTablePageMenus(parentTablePage);

  }

  @Override
  public boolean isTableVisible() {
    return FLAGS_BIT_HELPER.isBitSet(TABLE_VISIBLE, m_flags);
  }

  @Override
  public void setTableVisible(boolean tableVisible) {
    if (isTableVisible() == tableVisible) {
      return; // no change
    }
    m_flags = FLAGS_BIT_HELPER.changeBit(TABLE_VISIBLE, tableVisible, m_flags);
    firePageChanged();
  }

  @Override
  public boolean isDetailFormVisible() {
    return FLAGS_BIT_HELPER.isBitSet(DETAIL_FORM_VISIBLE, m_flags);
  }

  @Override
  public void setDetailFormVisible(boolean detailFormVisible) {
    if (isDetailFormVisible() == detailFormVisible) {
      return; // no change
    }
    m_flags = FLAGS_BIT_HELPER.changeBit(DETAIL_FORM_VISIBLE, detailFormVisible, m_flags);
    firePageChanged();
  }

  @Override
  public boolean isCompactRoot() {
    return FLAGS2_BIT_HELPER.isBitSet(COMPACT_ROOT, m_flags2);
  }

  @Override
  public void setCompactRoot(boolean compactRoot) {
    m_flags2 = FLAGS2_BIT_HELPER.changeBit(COMPACT_ROOT, compactRoot, m_flags2);
  }

  @Override
  public boolean isShowTileOverview() {
    return FLAGS2_BIT_HELPER.isBitSet(SHOW_TILE_OVERVIEW, m_flags2);
  }

  @Override
  public void setShowTileOverview(boolean showTileOverview) {
    Assertions.assertNull(getDetailForm(), "Property 'showTileOverview' cannot be changed because DetailForm has already been created");
    if (isShowTileOverview() == showTileOverview) {
      return; // no change
    }
    m_flags2 = FLAGS2_BIT_HELPER.changeBit(SHOW_TILE_OVERVIEW, showTileOverview, m_flags2);
    firePageChanged();
  }

  @Override
  public boolean isNavigateButtonsVisible() {
    return FLAGS2_BIT_HELPER.isBitSet(NAVIGATE_BUTTONS_VISIBLE, m_flags2);
  }

  @Override
  public void setNavigateButtonsVisible(boolean navigateButtonsVisible) {
    if (isNavigateButtonsVisible() == navigateButtonsVisible) {
      return; // no change
    }
    m_flags2 = FLAGS2_BIT_HELPER.changeBit(NAVIGATE_BUTTONS_VISIBLE, navigateButtonsVisible, m_flags2);
    firePageChanged();
  }

  @Override
  public void setOverviewIconId(String overviewIconId) {
    if (ObjectUtility.equals(getOverviewIconId(), overviewIconId)) {
      return; // no change
    }
    m_overviewIconId = overviewIconId;
    firePageChanged();
  }

  @Override
  public String getOverviewIconId() {
    return m_overviewIconId;
  }

  /**
   * Note: set*Visible methods are called by initConfig(), at this point getTree() is still null. Tree can also be null
   * during shutdown.
   */
  protected void firePageChanged() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.firePageChanged(this);
    }
  }

  protected void fireBeforeDataLoaded() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.fireBeforeDataLoaded(this);
    }
  }

  protected void fireAfterDataLoaded() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.fireAfterDataLoaded(this);
    }
  }

  protected void fireAfterTableInit() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.fireAfterTableInit(this);
    }
  }

  protected void fireAfterPageInit() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.fireAfterPageInit(this);
    }
  }

  protected void fireAfterPageDispose() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.fireAfterPageDispose(this);
    }
  }

  protected void firePageActivated() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.firePageActivated(this);
    }
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

  @Override
  public <A> A getAdapter(Class<A> clazz) {
    return null;
  }

  private void interceptReloadPage(String reloadReason) {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageReloadPageChain chain = new PageReloadPageChain(extensions);
    chain.execReloadPage(reloadReason);
  }

  protected final void interceptPageDataLoaded() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PagePageDataLoadedChain chain = new PagePageDataLoadedChain(extensions);
    chain.execPageDataLoaded();
  }

  protected final void interceptPageActivated() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PagePageActivatedChain chain = new PagePageActivatedChain(extensions);
    chain.execPageActivated();
  }

  protected final void interceptDataChanged(Object... dataTypes) {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageDataChangedChain chain = new PageDataChangedChain(extensions);
    chain.execDataChanged(dataTypes);
  }

  protected final void interceptInitPage() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageInitPageChain chain = new PageInitPageChain(extensions);
    chain.execInitPage();
  }

  protected final void interceptPageDeactivated() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PagePageDeactivatedChain chain = new PagePageDeactivatedChain(extensions);
    chain.execPageDeactivated();
  }

  protected final void interceptDisposePage() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageDisposePageChain chain = new PageDisposePageChain(extensions);
    chain.execDisposePage();
  }

  protected final void interceptInitDetailForm() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageInitDetailFormChain chain = new PageInitDetailFormChain(extensions);
    chain.execInitDetailForm();
  }

  protected final void interceptInitTable() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageInitTableChain chain = new PageInitTableChain(extensions);
    chain.execInitTable();
  }

  protected final void interceptDetailFormActivated() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageDetailFormActivatedChain chain = new PageDetailFormActivatedChain(extensions);
    chain.execDetailFormActivated();
  }

  protected final boolean interceptCalculateVisible() {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    PageCalculateVisibleChain chain = new PageCalculateVisibleChain(extensions);
    return chain.execCalculateVisible();
  }

  protected final List<IMenu> interceptComputeParentTablePageMenus(IPageWithTable<?> parentTablePage) {
    List<? extends IPageExtension<? extends AbstractPage>> extensions = getAllExtensions();
    ComputeParentTablePageMenusChain chain = new ComputeParentTablePageMenusChain(extensions);
    return chain.execComputeParentTablePageMenus(parentTablePage);
  }

  /**
   * Adapter listener that delegates NODE_UPDATED tree events to pageChanged events
   */
  protected TreeListener createLocalTreeListener() {
    return e -> {
      AbstractPage<T> page = AbstractPage.this;
      if (TreeEvent.TYPE_NODES_UPDATED == e.getType() && e.getChildNodes().contains(page)) {
        AbstractPage.this.firePageChanged();
      }
    };
  }

  /**
   * Executes the given runnable in the extension context, in which this page object was created.
   *
   * @see ObjectExtensions#runInExtensionContext(Runnable)
   */
  protected void runInExtensionContext(Runnable runnable) {
    m_objectExtensions.runInExtensionContext(runnable);
  }

  @Override
  public <S extends IExtension<?>> S getExtension(Class<S> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public final List<? extends IPageExtension<? extends AbstractPage>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <S> List<S> getContributionsByClass(Class<S> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <S> S getContribution(Class<S> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  @Override
  public final <S> S optContribution(Class<S> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  protected static class LocalPageExtension<OWNER extends AbstractPage> extends AbstractPageExtension<OWNER> {

    public LocalPageExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execReloadPage(PageReloadPageChain chain, String reloadReason) {
      getOwner().execReloadPage(reloadReason);
    }

    @Override
    public void execPageDataLoaded(PagePageDataLoadedChain chain) {
      getOwner().execPageDataLoaded();
    }

    @Override
    public void execPageActivated(PagePageActivatedChain chain) {
      getOwner().execPageActivated();
    }

    @Override
    public void execDataChanged(PageDataChangedChain chain, Object... dataTypes) {
      getOwner().execDataChanged(dataTypes);
    }

    @Override
    public void execInitPage(PageInitPageChain chain) {
      getOwner().execInitPage();
    }

    @Override
    public void execPageDeactivated(PagePageDeactivatedChain chain) {
      getOwner().execPageDeactivated();
    }

    @Override
    public void execDisposePage(PageDisposePageChain chain) {
      getOwner().execDisposePage();
    }

    @Override
    public void execInitDetailForm(PageInitDetailFormChain chain) {
      getOwner().execInitDetailForm();
    }

    @Override
    public void execInitTable(PageInitTableChain chain) {
      getOwner().execInitTable();
    }

    @Override
    public void execDetailFormActivated(PageDetailFormActivatedChain chain) {
      getOwner().execDetailFormActivated();
    }

    @Override
    public boolean execCalculateVisible(PageCalculateVisibleChain chain) {
      return getOwner().execCalculateVisible();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IMenu> execComputeParentTablePageMenus(ComputeParentTablePageMenusChain chain, IPageWithTable<?> parentTablePage) {
      return getOwner().execComputeParentTablePageMenus(parentTablePage);
    }
  }

  protected IPageExtension<? extends AbstractPage> createLocalExtension() {
    return new LocalPageExtension<AbstractPage>(this);
  }

  @ClassId("d9e0f79c-5270-4a6e-8fad-220dc81659ac")
  protected class P_TileOverviewForm extends AbstractForm implements ITileOverviewForm {

    @Override
    protected String getConfiguredTitle() {
      return AbstractPage.this.getConfiguredTitle();
    }

    @Order(10)
    @ClassId("e3e6ee1e-df3d-4b13-b5da-23306fbc2686")
    public class MainBox extends AbstractGroupBox {

      @Override
      protected boolean execCalculateVisible() {
        return true;
      }

      @Order(2000)
      @ClassId("2eb22815-f33d-41e6-aabc-46b9935f12a2")
      public class F5KeyStroke extends AbstractKeyStroke {

        @Override
        protected String getConfiguredKeyStroke() {
          return IKeyStroke.F5;
        }

        @Override
        protected void execAction() {
          AbstractPage.this.reloadPage();
        }
      }
    }
  }
}
