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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.dto.PageData;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableComputeTableEmptySpaceMenusChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableCreateChildPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableInitSearchFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableLoadDataChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTablePopulateTableChain;
import org.eclipse.scout.rt.client.services.common.search.ISearchFilterService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AggregateTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.SearchFormTableControl;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMediator;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A page containing a list of "menu" entries<br>
 * child pages are explicitly added
 */
@ClassId("b131ace3-9d63-46d9-9659-e288ca26b367")
public abstract class AbstractPageWithTable<T extends ITable> extends AbstractPage<T> implements IPageWithTable<T> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPageWithTable.class);

  private ISearchForm m_searchForm;
  private FormListener m_searchFormListener;

  public AbstractPageWithTable() {
    this(true, null);
  }

  /**
   * calling the constructor with callInitializer == false means, the table won't be constructed upon init but upon
   * activation. this is a performance-optimization and especially recommended for tablepages where the parent is
   * directly another table page (and no folder- or plain page) in this case the parent page can have a huge amount of
   * child pages with a lot of tables to be constructed but never used.
   */
  public AbstractPageWithTable(boolean callInitializer) {
    this(callInitializer, null);
  }

  public AbstractPageWithTable(String userPreferenceContext) {
    this(true, userPreferenceContext);
  }

  public AbstractPageWithTable(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
    if (!callInitializer) {
      callMinimalInitializer();
    }
  }

  protected void callMinimalInitializer() {
    setChildrenDirty(true);
    setLeafInternal(getConfiguredLeaf());
    setEnabled(getConfiguredEnabled(), IDimensions.ENABLED);
    setExpandedInternal(getConfiguredExpanded());
  }

  /**
   * Every child page of a table page is being initialized when loading the table page. Since a table can contain large
   * numbers of children, potentially expensive permission checks must be performed late (before loading the data).
   */
  @Override
  protected boolean isCalculateVisibleLate() {
    return true;
  }

  /*
   * Configuration
   */
  /**
   * Configures the search form to be used with this table page. The search form is lazily
   * {@linkplain #ensureSearchFormCreated() created} and {@linkplain #ensureSearchFormStarted() started}.
   * <p>
   * For legacy support, the search form can also be defined as an inner class. This usage is deprecated, override this
   * method in subclasses instead.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a search form type token
   * @see ISearchForm
   */
  @ConfigProperty(ConfigProperty.SEARCH_FORM)
  @Order(90)
  protected Class<? extends ISearchForm> getConfiguredSearchForm() {
    return null;
  }

  /**
   * Configures whether table data is automatically loaded (through a search with default constraints) or whether
   * loading the table data must be triggered explicitly by the user. Set this property to {@code true} if you expect
   * large amount of data for an unconstrained search.
   * <p>
   * This property is read by {@link #interceptPopulateTable()}, if you override that method, this configuration
   * property might not have any effect. This configuration property does not have any effect if no search form is
   * configured for this table page.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the table data should be loaded on explicit user interaction, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredSearchRequired() {
    return false;
  }

  /**
   * Configures whether the table status should be visible.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @since 5.1
   * @see ITable#setTableStatusVisible(boolean)
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(130)
  protected boolean getConfiguredTableStatusVisible() {
    return true;
  }

  @Override
  protected boolean getConfiguredLazyExpandingEnabled() {
    // Override default value for all table pages
    return true;
  }

  /**
   * Configures whether a default child page should be created for each table row if no page is created by
   * {@link #execCreateChildPage(ITableRow)}.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @since 6.0
   * @see #execCreateChildPage(ITableRow)
   * @see #createDefaultChildPage(ITableRow)
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  protected boolean getConfiguredAlwaysCreateChildPage() {
    return false;
  }

  /**
   * Fetches data and loads them into the page's table.
   * <p/>
   * Typically subclasses override this method if this table page is using a bean-based table page data (i.e. an
   * {@link PageData} annotation is present on this class):
   *
   * <pre>
   * protected void execLoadData(SearchFilter filter) {
   *   //logic to initialize the service, to handle the search filter...
   *   AbstractTablePageData pageData = service.loadPageData(...);
   *   importPageData(pageData);
   * }
   * </pre>
   * <p/>
   * An other possibility is to import some data array (Object[][]):
   *
   * <pre>
   * protected void execLoadData(SearchFilter filter) {
   *   //logic to initialize the service, to handle the search filter...
   *   Object[][] data = service.loadTableData(...);
   *   importTableData(data);
   * }
   * </pre>
   * <p/>
   * This default implementation invokes {@link #interceptLoadData(SearchFilter)} to fetch the tabular data and loads it
   * into the table using {@link ITable#replaceRowsByMatrix(Object)}.
   *
   * @param filter
   *          a search filter, guaranteed not to be {@code null}
   * @since 3.10.0-M1
   */
  @ConfigOperation
  @Order(85)
  protected void execLoadData(SearchFilter filter) {
  }

  /**
   * Populates this page's table.
   * <p>
   * It is good practice to populate table using {@code ITable.replaceRows} instead of {@code ITable.removeAllRows();
   * ITable.addRows} because in the former case the outline tree structure below the changing rows is not discarded but
   * only marked as dirty. The subtree is lazily reloaded when the user clicks next time on a child node.
   * <p>
   * Subclasses can override this method. In most cases it is sufficient to override
   * {@link #interceptLoadData(SearchFilter)} or {@link #interceptLoadData(SearchFilter)} instead.<br/>
   * This default implementation does the following: It queries methods {@link #isSearchActive()} and
   * {@link #isSearchRequired()} and then calls {@link #interceptLoadData(SearchFilter)} if appropriate.
   */
  @ConfigOperation
  @Order(100)
  protected void execPopulateTable() {
    if (isSearchActive()) {
      SearchFilter filter = getSearchFilter();
      if (filter.isCompleted() || !isSearchRequired()) {
        // create a copy of the filter, just in case the subprocess is modifying
        // or extending the filter
        filter = filter.copy();
        interceptLoadData(filter);
      }
    }
    else {
      // searchFilter should never be null
      interceptLoadData(new SearchFilter());
    }
    //update table data status
    if (isSearchActive() && getSearchFilter() != null && (!getSearchFilter().isCompleted()) && isSearchRequired()) {
      setTableStatus(new Status(TEXTS.get("TooManyRows"), IStatus.INFO));
    }
    else {
      setTableStatus(null);
    }
    Status status = createPopulateTableStatus();
    if (status != null) {
      setTableStatus(status);
    }
  }

  /**
   * @since 9.0
   */
  protected Status createPopulateTableStatus() {
    T table = getTable();
    if (table == null) {
      return null;
    }
    boolean limited = isLimitedResult();
    if (!limited) {
      return null;
    }
    long estimatedRowCount = table.getEstimatedRowCount();
    String showingRowCountText = NumberFormat.getIntegerInstance(NlsLocale.get()).format(table.getRowCount());
    String estimatedRowCountText = NumberFormat.getIntegerInstance(NlsLocale.get()).format(estimatedRowCount);
    String message;
    if (UserAgentUtility.isTouchDevice()) {
      if (estimatedRowCount > 0L) {
        message = TEXTS.get("MaxOutlineRowWarningMobileWithEstimatedRowCount", showingRowCountText, estimatedRowCountText);
      }
      else {
        message = TEXTS.get("MaxOutlineRowWarningMobile", showingRowCountText);
      }
    }
    else {
      if (estimatedRowCount > 0L) {
        message = TEXTS.get("MaxOutlineRowWarningWithEstimatedRowCount", showingRowCountText, estimatedRowCountText);
      }
      else {
        message = TEXTS.get("MaxOutlineRowWarning", showingRowCountText);
      }
    }
    return new Status(message, IStatus.INFO);
  }

  /**
   * Creates a child page for every table row that was added to this page's table. This method is called when resolving
   * a virtual tree node to a real node. Overriding this method is the recommended way to build the outline tree
   * structure.
   * <p>
   * Subclasses can override this method. The default returns {@code null}.
   *
   * @param row
   *          a table row for which a new child page should be created
   * @return a new child page for {@code row}
   */
  @ConfigOperation
  @Order(110)
  protected IPage<?> execCreateChildPage(ITableRow row) {
    return null;
  }

  protected List<IMenu> execComputeTableEmptySpaceMenus() {
    T table = getTable();
    if (table == null) {
      return CollectionUtility.emptyArrayList();
    }
    return MenuUtility.filterMenusRec(table.getMenus(), MenuUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TableMenuType.EmptySpace), false));
  }

  protected IPage<?> createDefaultChildPage(ITableRow row) {
    return new AutoLeafPageWithNodes(row);
  }

  /**
   * This method must only be called in a context with the current outline set. See
   * {@link ClientRunContext#withOutline(IOutline, boolean)}
   */
  protected IPage<?> createChildPageInternalInRunContext(final ITableRow row) {
    IPage<?> childPage = interceptCreateChildPage(row);
    if (childPage == null && isAlwaysCreateChildPage()) {
      childPage = createDefaultChildPage(row);
    }
    return childPage;
  }

  private Class<? extends ITable> getConfiguredTable() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, ITable.class);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSearchActive(true);
    setSearchRequired(getConfiguredSearchRequired());
    setAlwaysCreateChildPage(getConfiguredAlwaysCreateChildPage());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T createTable() {
    List<ITable> contributedFields = m_contributionHolder.getContributionsByClass(ITable.class);
    T table = (T) CollectionUtility.firstElement(contributedFields);
    if (table == null) {
      Class<? extends ITable> tableClass = getConfiguredTable();
      if (tableClass != null) {
        table = (T) ConfigurationUtility.newInnerInstance(this, tableClass);
      }
      else {
        LOG.warn("there is no inner class of type ITable in {}", getClass().getName());
      }
    }

    if (table != null) {
      if (table instanceof AbstractTable) {
        ((AbstractTable) table).setContainerInternal(this);
      }
      table.addTableListener(
          new P_TableListener(),
          TableEvent.TYPE_ROW_ACTION,
          TableEvent.TYPE_ALL_ROWS_DELETED,
          TableEvent.TYPE_ROWS_DELETED,
          TableEvent.TYPE_ROWS_INSERTED,
          TableEvent.TYPE_ROWS_UPDATED,
          TableEvent.TYPE_ROW_ORDER_CHANGED,
          TableEvent.TYPE_ROW_FILTER_CHANGED);
      table.setEnabled(isEnabled());
      table.setAutoDiscardOnDelete(true);
      table.setUserPreferenceContext(getUserPreferenceContext());
      table.setTableStatusVisible(getConfiguredTableStatusVisible());
      table.setReloadHandler(new PageReloadHandler(this));
    }
    return table;
  }

  /**
   * Ensures that the search form is initialized but not started, if one is defined for this table. This allows lazy
   * initialization of search forms.
   */
  protected void ensureSearchFormCreated() {
    if (m_searchForm == null) {
      try {
        setSearchForm(createSearchForm());
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating search form for '" + getClass().getName() + "'.", e));
      }
    }
  }

  /**
   * creates the search form, but doesn't start it
   *
   * @return {@link ISearchForm} or <code>null</code> if the search form could not be created.
   */
  protected ISearchForm createSearchForm() {
    final Class<? extends ISearchForm> configuredSearchForm = getConfiguredSearchForm();
    if (configuredSearchForm == null) {
      return null;
    }

    try {
      return createDisplayParentRunContext()
          .call(() -> configuredSearchForm.getConstructor().newInstance());
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + configuredSearchForm.getName() + "'.", e));
    }
    return null;
  }

  /**
   * Ensures that the search form is started (lazy starting)
   */
  protected void ensureSearchFormStarted() {
    if (m_searchForm != null && m_searchForm.isFormStartable()) {
      try {
        m_searchForm.start();
        fireAfterSearchFormStart();
        ensureSearchControlSelected();
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating search form '" + m_searchForm.getClass().getName() + "' for page '" + getClass().getName() + "'.", e));
      }
    }
  }

  protected void ensureSearchControlSelected() {
    if (isSearchRequired() && !getSearchFilter().isCompleted()) {
      T table = getTable();
      if (table != null) {
        SearchFormTableControl control = table.getTableControl(SearchFormTableControl.class);
        if (control != null) {
          control.setSelected(true);
        }
      }
    }
  }

  private void attachToSearchFormInternal() {
    if (m_searchForm == null) {
      return;
    }
    m_searchForm.setDisplayHint(ISearchForm.DISPLAY_HINT_VIEW);
    if (m_searchForm.getDisplayViewId() == null) {
      m_searchForm.setDisplayViewId(IForm.VIEW_ID_PAGE_SEARCH);
    }
    m_searchForm.setShowOnStart(false);
    // listen for search action
    m_searchFormListener = e -> {
      switch (e.getType()) {
        case FormEvent.TYPE_RESET_COMPLETE: {
          // do page reload to execute search
          try {
            if (isSearchRequired()) {
              T table = getTable(false);
              if (table != null) {
                table.discardAllRows();
              }
            }
            else {
              reloadPage(IReloadReason.SEARCH);
            }
          }
          catch (RuntimeException | PlatformError ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
          break;
        }
        case FormEvent.TYPE_STORE_AFTER: {
          // do page reload to execute search
          try {
            reloadPage(IReloadReason.SEARCH);
          }
          catch (RuntimeException | PlatformError ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
          break;
        }
      }
    };
    m_searchForm.addFormListener(m_searchFormListener);
    try {
      interceptInitSearchForm();
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating search form '" + m_searchForm.getClass().getName() + "' for page '" + getClass().getName() + "'.", e));
    }
  }

  @Override
  protected void addDefaultTableControls() {
    T table = getTable();
    if (table == null) {
      return;
    }

    ITableControl control = createSearchFormTableControl();
    if (control != null) {
      table.addTableControl(control);
      linkSearchFormWithTableControl();
    }
    control = createAggregateTableControl();
    if (control != null) {
      table.addTableControl(control);
    }
  }

  protected void linkSearchFormWithTableControl() {
    T table = getTable(false);
    if (table == null) {
      return;
    }

    SearchFormTableControl tableControl = table.getTableControl(SearchFormTableControl.class);
    if (tableControl != null) {
      tableControl.setForm(m_searchForm);
    }
  }

  protected SearchFormTableControl createSearchFormTableControl() {
    return new SearchFormTableControl();
  }

  protected ITableControl createAggregateTableControl() {
    return new AggregateTableControl();
  }

  private void detachFromSearchFormInternal() {
    if (m_searchForm == null) {
      return;
    }
    // listen for search action
    if (m_searchFormListener != null) {
      m_searchForm.removeFormListener(m_searchFormListener);
      m_searchFormListener = null;
    }
    detachSearchTableControl();
  }

  private void detachSearchTableControl() {
    T table = getTable(false);
    if (table == null) {
      return;
    }

    ITableControl searchControl = table.getTableControl(SearchFormTableControl.class);
    if (searchControl != null) {
      table.removeTableControl(searchControl);
    }
  }

  protected void disposeSearchForm() {
    if (m_searchForm != null) {
      m_searchForm.doClose();
      setSearchForm(null);
    }
  }

  /**
   * Initializes the search form associated with this page. This method is called before the search form is used for the
   * first time.
   * <p>
   * Legacy: If the search form is defined as inner class, this method is called when this page is initialized.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @see #ensureSearchFormCreated()
   * @see #ensureSearchFormStarted()
   */
  @ConfigOperation
  @Order(120)
  protected void execInitSearchForm() {
  }

  @Override
  public final T getTable() {
    T table = super.getTable();
    if (table == null) {
      callInitializer(); // no effect if already initialized
    }
    return table;
  }

  @Override
  public ISearchForm getSearchFormInternal() {
    ensureSearchFormCreated();
    return m_searchForm;
  }

  public void setSearchForm(ISearchForm searchForm) {
    if (m_searchForm == searchForm) {
      return;
    }
    detachFromSearchFormInternal();
    m_searchForm = searchForm;
    attachToSearchFormInternal();
    linkSearchFormWithTableControl();
  }

  @Override
  public SearchFilter getSearchFilter() {
    ensureSearchFormCreated();
    ensureSearchFormStarted();
    if (getSearchFormInternal() != null) {
      return getSearchFormInternal().getSearchFilter();
    }
    else {
      ISearchFilterService sfs = BEANS.get(ISearchFilterService.class);
      if (sfs != null) {
        return sfs.createNewSearchFilter();
      }
      else {
        return new SearchFilter();
      }
    }
  }

  @Override
  public boolean isSearchRequired() {
    return FLAGS_BIT_HELPER.isBitSet(SEARCH_REQUIRED, m_flags);
  }

  @Override
  public void setSearchRequired(boolean searchRequired) {
    m_flags = FLAGS_BIT_HELPER.changeBit(SEARCH_REQUIRED, searchRequired, m_flags);
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    T table = getTable(false);
    if (table != null) {
      table.setEnabled(isEnabled());
    }
  }

  @Override
  public boolean isSearchActive() {
    return FLAGS_BIT_HELPER.isBitSet(SEARCH_ACTIVE, m_flags);
  }

  @Override
  public void setSearchActive(boolean searchActive) {
    m_flags = FLAGS_BIT_HELPER.changeBit(SEARCH_ACTIVE, searchActive, m_flags);
    if (isSelectedNode()) {
      getOutline().setSearchForm(searchActive ? getSearchFormInternal() : null);
    }
  }

  /**
   * Indicates if the result displayed in the table is the whole result or if there is more data in the server (that
   * wasn't sent to the client). Is set if {@link #importPageData(AbstractTablePageData)} was used.
   *
   * @since 3.10.0-M3
   */
  protected boolean isLimitedResult() {
    return FLAGS_BIT_HELPER.isBitSet(LIMITED_RESULT, m_flags);
  }

  @Override
  public boolean isAlwaysCreateChildPage() {
    return FLAGS_BIT_HELPER.isBitSet(ALWAYS_CREATE_CHILD_PAGE, m_flags);
  }

  @Override
  public void setAlwaysCreateChildPage(boolean alwaysCreateChildPage) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ALWAYS_CREATE_CHILD_PAGE, alwaysCreateChildPage, m_flags);
  }

  @Override
  public void pageActivatedNotify() {
    callInitializer(); // no effect if already initialized
    ensureSearchFormCreated();
    ensureSearchFormStarted();
    super.pageActivatedNotify();
  }

  @Override
  public void disposeInternal() {
    super.disposeInternal();
    try {
      disposeSearchForm();
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  /**
   * Import the content of the tablePageData in the table of the page.
   *
   * @since 3.10.0-M3
   */
  protected void importPageData(AbstractTablePageData tablePageData) {
    T table = getTable();
    if (table == null) {
      return;
    }

    table.importFromTableBeanData(tablePageData);
    m_flags = FLAGS_BIT_HELPER.changeBit(LIMITED_RESULT, tablePageData.isLimitedResult(), m_flags);
    table.setEstimatedRowCount(tablePageData.getEstimatedRowCount());
    table.setMaxRowCount(tablePageData.getMaxRowCount());
  }

  /**
   * Import data (Object[][]) in the table page. Object arrays are not type safe. The preferred way is to use a
   * bean-based table page data and {@link #importPageData(AbstractTablePageData)}
   *
   * @since 4.2.0 (Mars-M4)
   */
  protected void importTableData(Object[][] data) {
    //do NOT reference the result data object and wrap it into a ref, so the processor is allowed to delete the contents to free up memory sooner
    T table = getTable();
    if (table == null) {
      return;
    }
    table.replaceRowsByMatrix(new AtomicReference<Object>(data));
  }

  /**
   * load table data
   */
  protected void loadTableDataImpl() {
    T table = getTable();
    if (table == null) {
      return;
    }

    try {
      table.setTableChanging(true);
      ensureSearchFormCreated();
      ensureSearchFormStarted();
      interceptPopulateTable();
    }
    catch (ThreadInterruptedError | FutureCancelledError e) {
      table.discardAllRows();
      setTableStatus(new Status(TEXTS.get("SearchWasCanceled"), IStatus.ERROR));
      throw e;
    }
    catch (VetoException e) {
      table.discardAllRows();
      setTableStatus(new Status(ObjectUtility.nvl(e.getDisplayMessage(), TEXTS.get("ErrorWhileLoadingData")), IStatus.ERROR));
      throw e;
    }
    catch (RuntimeException | PlatformError e) {
      table.discardAllRows();
      setTableStatus(new Status(TEXTS.get("ErrorWhileLoadingData"), IStatus.ERROR));
      throw e;
    }
    finally {
      table.setTableChanging(false);
    }
  }

  /**
   * load tree children<br>
   * this method delegates to the table reload<br>
   * when the table is loaded and this node is not a leaf node then the table rows are mirrored in child nodes
   */
  @Override
  protected final void loadChildrenImpl() {
    ITree tree = getTree();
    try {
      if (tree != null) {
        tree.setTreeChanging(true);
      }
      //
      // backup currently selected tree node and its path to root
      boolean oldSelectionOwned = false;
      int oldSelectionDirectChildIndex = -1;
      ITreeNode oldSelectedNode = null;
      if (tree != null) {
        oldSelectedNode = tree.getSelectedNode();
      }
      List<Object> oldSelectedRowKeys = null;
      if (oldSelectedNode != null) {
        ITreeNode t = oldSelectedNode;
        while (t != null && t.getParentNode() != null) {
          if (t.getParentNode() == this) {
            oldSelectionOwned = true;
            oldSelectedRowKeys = getTableRowFor(t).getKeyValues();
            oldSelectionDirectChildIndex = t.getChildNodeIndex();
            break;
          }
          t = t.getParentNode();
        }
      }
      //
      setChildrenLoaded(false);
      fireBeforeDataLoaded();
      try {
        loadTableDataImpl();
      }
      catch (ThreadInterruptedError | FutureCancelledError e) { // NOSONAR
        // NOOP
      }
      finally {
        fireAfterDataLoaded();
      }
      setChildrenLoaded(true);
      setChildrenDirty(false);
      // table events will handle automatic tree changes in case table is mirrored in tree.
      // restore currently selected tree node when it was owned by our table rows.
      // in case selection was lost, try to select similar index as before

      T table = getTable();
      if (tree != null && table != null && oldSelectionOwned && tree.getSelectedNode() == null) {
        ITreeNode newSelectedNode = null;
        ITableRow row = table.getSelectedRow();
        if (row != null) {
          newSelectedNode = getTreeNodeFor(row);
        }
        else {
          row = table.getRowByKey(oldSelectedRowKeys);
          if (row != null) {
            newSelectedNode = getTreeNodeFor(row);
          }
          else if (oldSelectedNode != null && oldSelectedNode.getTree() == tree) { // NOSONAR
            newSelectedNode = oldSelectedNode;
          }
          else {
            int index = Math.max(-1, Math.min(oldSelectionDirectChildIndex, getChildNodeCount() - 1));
            if (index >= 0 && index < getChildNodeCount()) {
              newSelectedNode = getChildNode(index);
            }
            else {
              newSelectedNode = this;
            }
          }
        }
        if (newSelectedNode != null) {
          tree.selectNode(newSelectedNode);
        }
      }
    }
    finally {
      if (tree != null) {
        tree.setTreeChanging(false);
      }
    }
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    if (desktop != null) {
      desktop.afterTablePageLoaded(this);
    }
  }

  @Override
  public List<IPage<?>> getUpdatedChildPagesFor(List<? extends ITableRow> tableRows) {
    return getChildPagesFor(tableRows, true);
  }

  @Override
  public List<IMenu> computeTableEmptySpaceMenus() {
    return interceptComputeTableEmptySpaceMenus();
  }

  /**
   * Computes the list of linked child pages for the given table rows. Revalidates the the pages cell if
   * <code>updateChildPageCells</code> is true. Otherwise, the cells are not updated.
   */
  private List<IPage<?>> getChildPagesFor(List<? extends ITableRow> tableRows, boolean updateChildPageCells) {
    List<IPage<?>> result = new ArrayList<>();
    try {
      for (ITableRow row : tableRows) {
        IPage<?> page = getPageFor(row);
        if (page != null) {
          result.add(page);
          if (updateChildPageCells) {
            // update tree nodes from table rows
            page.setEnabled(row.isEnabled(), IDimensions.ENABLED);
            T table = getTable();
            if (table != null) {
              ICell tableCell = table.getSummaryCell(row);
              page.getCellForUpdate().updateFrom(tableCell);
            }
          }
        }
      }
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
    return result;
  }

  /**
   * Called when a row gets inserted.
   * <p>
   * Updates the cell belonging to the newly created page with the content of the summary cell of the inserted table
   * row.
   */
  protected void updateCellFromTableCell(Cell pageCell, ICell summaryCell) {
    pageCell.updateFrom(summaryCell);
  }

  /**
   * Table listener and tree controller<br>
   * the table is reflected in tree children only if the tree/page node is not marked as being a leaf
   */
  private class P_TableListener extends TableAdapter {

    private Optional<OutlineMediator> getOutlineMediator() {
      return Optional.ofNullable(getOutline())
          .map(IOutline::getOutlineMediator);
    }

    private void onRowsInserted(TableEvent e) {
      List<ITableRow> tableRows = e.getRows();
      List<IPage<?>> childPageList = new ArrayList<>(tableRows.size());

      for (ITableRow element : tableRows) {
        try {
          IPage<?> childPage = createChildPageInternalInRunContext(element);
          if (childPage != null) {
            childPage.setRejectedByUser(element.isRejectedByUser());
            childPage.setFilterAccepted(element.isFilterAccepted());
            childPage.setEnabled(element.isEnabled(), IDimensions.ENABLED);
            T table = getTable();
            if (table != null) {
              ICell tableCell = table.getSummaryCell(element);
              updateCellFromTableCell(childPage.getCellForUpdate(), tableCell);
            }
            linkTableRowWithPage(element, childPage);
            childPageList.add(childPage);
          }
        }
        catch (RuntimeException | PlatformError ex) {
          BEANS.get(ExceptionHandler.class).handle(ex);
        }
      }

      getOutlineMediator().ifPresent(mediator -> mediator.mediateTableRowsInserted(tableRows, childPageList, AbstractPageWithTable.this));

      // check if a page was revoked
      for (ITableRow tableRow : tableRows) {
        IPage<?> page = getPageFor(tableRow);
        if (page != null && page.getParentNode() == null) {
          unlinkTableRowWithPage(tableRow);
        }
      }
    }

    @Override
    public void tableChanged(final TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROW_ACTION: {
          getOutlineMediator().ifPresent(mediator -> mediator.mediateTableRowAction(e, AbstractPageWithTable.this));
          break;
        }
        case TableEvent.TYPE_ALL_ROWS_DELETED:
        case TableEvent.TYPE_ROWS_DELETED: {
          if (!isLeaf()) {
            List<ITableRow> tableRows = e.getRows();
            List<IPage<?>> childNodes = getChildPagesFor(e.getRows(), false);
            for (ITableRow row : tableRows) {
              unlinkTableRowWithPage(row);
            }
            getOutlineMediator().ifPresent(mediator -> mediator.mediateTableRowsDeleted(childNodes, AbstractPageWithTable.this));
          }
          break;
        }
        case TableEvent.TYPE_ROWS_INSERTED: {
          if (!isLeaf()) {
            createDisplayParentRunContext().run(() -> onRowsInserted(e));
          }
          break;
        }
        case TableEvent.TYPE_ROWS_UPDATED: {
          getOutlineMediator().ifPresent(mediator -> mediator.mediateTableRowsUpdated(e, AbstractPageWithTable.this));
          break;
        }
        case TableEvent.TYPE_ROW_ORDER_CHANGED: {
          getOutlineMediator().ifPresent(mediator -> mediator.mediateTableRowOrderChanged(e, AbstractPageWithTable.this));
          break;
        }
        case TableEvent.TYPE_ROW_FILTER_CHANGED: {
          getOutlineMediator().ifPresent(mediator -> mediator.mediateTableRowFilterChanged(AbstractPageWithTable.this));
          break;
        }
      }// end switch
    }
  }

  protected void fireAfterSearchFormStart() {
    IOutline outline = getOutline();
    if (outline != null) {
      outline.fireAfterSearchFormStart(this);
    }
  }

  protected final void interceptLoadData(SearchFilter filter) {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageWithTableLoadDataChain<T> chain = new PageWithTableLoadDataChain<>(extensions);
    chain.execLoadData(filter);
  }

  protected final IPage<?> interceptCreateChildPage(ITableRow row) {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageWithTableCreateChildPageChain<T> chain = new PageWithTableCreateChildPageChain<>(extensions);
    return chain.execCreateChildPage(row);
  }

  protected final void interceptPopulateTable() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageWithTablePopulateTableChain<T> chain = new PageWithTablePopulateTableChain<>(extensions);
    chain.execPopulateTable();
  }

  protected final void interceptInitSearchForm() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageWithTableInitSearchFormChain<T> chain = new PageWithTableInitSearchFormChain<>(extensions);
    chain.execInitSearchForm();
  }

  protected final List<IMenu> interceptComputeTableEmptySpaceMenus() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageWithTableComputeTableEmptySpaceMenusChain<T> chain = new PageWithTableComputeTableEmptySpaceMenusChain<>(extensions);
    return chain.execComputeTableEmptySpaceMenus();
  }

  protected static class LocalPageWithTableExtension<T extends ITable, OWNER extends AbstractPageWithTable<T>> extends LocalPageExtension<OWNER> implements IPageWithTableExtension<T, OWNER> {

    public LocalPageWithTableExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execLoadData(PageWithTableLoadDataChain<? extends ITable> chain, SearchFilter filter) {
      getOwner().execLoadData(filter);
    }

    @Override
    public IPage<?> execCreateChildPage(PageWithTableCreateChildPageChain<? extends ITable> chain, ITableRow row) {
      return getOwner().execCreateChildPage(row);
    }

    @Override
    public void execPopulateTable(PageWithTablePopulateTableChain<? extends ITable> chain) {
      getOwner().execPopulateTable();
    }

    @Override
    public void execInitSearchForm(PageWithTableInitSearchFormChain<? extends ITable> chain) {
      getOwner().execInitSearchForm();
    }

    @Override
    public List<IMenu> execComputeTableEmptySpaceMenus(PageWithTableComputeTableEmptySpaceMenusChain<? extends ITable> chain) {
      return getOwner().execComputeTableEmptySpaceMenus();
    }
  }

  @Override
  protected IPageWithTableExtension<T, ? extends AbstractPageWithTable<T>> createLocalExtension() {
    return new LocalPageWithTableExtension<>(this);
  }

}
