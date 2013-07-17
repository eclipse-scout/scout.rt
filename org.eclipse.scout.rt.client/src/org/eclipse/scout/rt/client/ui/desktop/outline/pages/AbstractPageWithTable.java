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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.PageData;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IMemoryPolicy;
import org.eclipse.scout.rt.client.services.common.search.ISearchFilterService;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMediator;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ContextMap;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.service.SERVICES;

/**
 * A page containing a list of "menu" entries<br>
 * child pages are explicitly added
 */
public abstract class AbstractPageWithTable<T extends ITable> extends AbstractPage implements IPageWithTable<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPageWithTable.class);

  private T m_table;
  private ISearchForm m_searchForm;
  private FormListener m_searchFormListener;
  private boolean m_searchRequired;
  private boolean m_searchActive;
  private boolean m_showEmptySpaceMenus;
  private boolean m_showTableRowMenus;
  private final HashMap<ITableRow, IPage> m_tableRowToPageMap = new HashMap<ITableRow, IPage>();
  private final HashMap<IPage, ITableRow> m_pageToTableRowMap = new HashMap<IPage, ITableRow>();

  public AbstractPageWithTable() {
    this(true, null, null);
  }

  /**
   * calling the constructor with callInitializer == false means, the table won't be constructed upon init
   * but upon activation. this is a performance-optimization and especially recommended for tablepages
   * where the parent is directly another table page (and no folder- or plain page) in this case the parent page can
   * have a huge amount of child pages with a lot of tables to be constructed but never used.
   * 
   * @param callInitializer
   */
  public AbstractPageWithTable(boolean callInitializer) {
    this(callInitializer, null, null);
  }

  public AbstractPageWithTable(ContextMap contextMap) {
    this(true, contextMap, null);
  }

  public AbstractPageWithTable(String userPreferenceContext) {
    this(true, null, userPreferenceContext);
  }

  public AbstractPageWithTable(boolean callInitializer, ContextMap contextMap) {
    this(callInitializer, contextMap, null);
  }

  public AbstractPageWithTable(boolean callInitializer, String userPreferenceContext) {
    this(callInitializer, null, userPreferenceContext);
  }

  public AbstractPageWithTable(boolean callInitializer, ContextMap contextMap, String userPreferenceContext) {
    super(callInitializer, contextMap, userPreferenceContext);
    if (!callInitializer) {
      callMinimalInitializer();
    }
  }

  protected void callMinimalInitializer() {
    setChildrenDirty(true);
    setLeafInternal(getConfiguredLeaf());
    setEnabledInternal(getConfiguredEnabled());
    setExpandedInternal(getConfiguredExpanded());
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
  @ConfigPropertyValue("null")
  protected Class<? extends ISearchForm> getConfiguredSearchForm() {
    return null;
  }

  /**
   * Configures whether table data is automatically loaded (through a search with default constraints)
   * or whether loading the table data must be triggered explicitly by the user. Set this property to {@code true} if
   * you expect large amount of data for an unconstrained search.
   * <p>
   * This property is read by {@link #execPopulateTable()}, if you override that method, this configuration property
   * might not have any effect. This configuration property does not have any effect if no search form is configured for
   * this table page.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   * 
   * @return {@code true} if the table data should be loaded on explicit user interaction, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredSearchRequired() {
    return false;
  }

  /**
   * Configures the visibility of empty space menus on this page's table. Empty space menus are typically available
   * anywhere in a table field where no table rows are present (in the 'empty space'), as well as on the table header.
   * Typical empty space menus will affect no (existing) row (for example a 'New row...' menu), or all rows in the table
   * (for example a 'Clear all rows' menu).
   * <p>
   * Note that setting this property to {@code false} will effectively stop all empty space menus from being displayed
   * on the GUI. However, if this property is set to {@code true}, single menus can still individually be set to
   * invisible.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if empty space menus should generally be visible, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredShowEmptySpaceMenus() {
    return true;
  }

  /**
   * Configures the visibility of table row menus on this page's table. Table row menus are typically available
   * on each existing row. Typical table row menus will affect exactly one existing row (for example an 'Edit row...'
   * menu or a 'Delete row' menu).
   * <p>
   * Note that setting this property to {@code false} will effectively stop all table row menus from being displayed on
   * the GUI. However, if this property is set to {@code true}, single menus can still individually be set to invisible.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if table row menus should generally be visible, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(120)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredShowTableRowMenus() {
    return true;
  }

  /**
   * Fetches data and loads them into the page's table.
   * <p/>
   * Typically subclasses override this method if this table page is using a bean-based table page data (i.e. an
   * {@link PageData} annotation is present on this class):
   * 
   * <pre>
   * AbstractTablePageData pageData = service.loadPageData(...);
   * getTable().importFromTableBeanData(pageData);
   * </pre>
   * <p/>
   * This default implementation invokes {@link #execLoadTableData(SearchFilter)} to fetch the tabular data and loads it
   * into the table using {@link ITable#replaceRowsByMatrix(Object)}.
   * 
   * @param filter
   *          a search filter, guaranteed not to be {@code null}
   * @throws ProcessingException
   * @since 3.10.0-M1 (backported)
   */
  @ConfigOperation
  @Order(85)
  protected void execLoadData(SearchFilter filter) throws ProcessingException {
    //do NOT reference the result data object and warp it into a ref, so the processor is allowed to delete the contents to free up memory sooner
    getTable().replaceRowsByMatrix(new AtomicReference<Object>(execLoadTableData(filter)));
  }

  /**
   * Fetches and returns tabular data to be displayed in this page's table.
   * Typically this method will query a (backend) service for the data. Make
   * sure the returned content (including type definitions) matches the table columns.
   * <p>
   * This method is called by {@link #execPopulateTable()} and overriding this method generally is the most convenient
   * way to populate a table page. If you need more control over populating a table page, consider overriding
   * {@code execPopulateTable()} instead.
   * <p>
   * Subclasses can override this method. The default returns {@code null}.
   * 
   * @param filter
   *          a search filter, guaranteed not to be {@code null}
   * @return an {@code Object[][]} representing tabular data to be displayed in this page's table
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(90)
  protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
    return null;
  }

  /**
   * Populates this page's table.
   * <p>
   * It is good practice to populate table using {@code ITable.replaceRows} instead of {@code ITable.removeAllRows();
   * ITable.addRows} because in the former case the outline tree structure below the changing rows is not discarded but
   * only marked as dirty. The subtree is lazily reloaded when the user clicks next time on a child node.
   * <p>
   * Subclasses can override this method. In most cases it is sufficient to override {@link #execLoadData(SearchFilter)}
   * or {@link #execLoadTableData(SearchFilter)} instead.<br/>
   * This default implementation does the following: It queries methods {@link #isSearchActive()} and
   * {@link #isSearchRequired()} and then calls {@link #execLoadData(SearchFilter)} if appropriate.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(100)
  protected void execPopulateTable() throws ProcessingException {
    if (isSearchActive()) {
      SearchFilter filter = getSearchFilter();
      if (filter.isCompleted() || !isSearchRequired()) {
        // create a copy of the filter, just in case the subprocess is modifying
        // or extending the filter
        filter = (SearchFilter) filter.clone();
        execLoadData(filter);
      }
    }
    else {
      // searchFilter should never be null
      execLoadData(new SearchFilter());
    }
    //update table data status
    if (isSearchActive() && getSearchFilter() != null && (!getSearchFilter().isCompleted()) && isSearchRequired()) {
      setPagePopulateStatus(new ProcessingStatus(ScoutTexts.get("TooManyRows"), ProcessingStatus.WARNING));
    }
    else {
      setPagePopulateStatus(null);
    }
  }

  /**
   * Creates a child page for every table row that was added to this page's table. This method is called when
   * resolving a virtual tree node to a real node. Overriding this method is the recommended way to build the
   * outline tree structure.
   * <p>
   * Subclasses can override this method. The default returns {@code null}.
   * 
   * @param row
   *          a table row for which a new child page should be created
   * @return a new child page for {@code row}
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(110)
  protected IPage execCreateChildPage(ITableRow row) throws ProcessingException {
    return null;
  }

  protected IPage createChildPageInternal(ITableRow row) throws ProcessingException {
    return execCreateChildPage(row);
  }

  /**
   * Creates a virtual child page for every table row that was added to this page's table. The virtual page
   * is a place holder for a real page and is transformed (resolved) into the real page when it is activated for the
   * first time. This reduces memory consumption and improves performance for large table pages, where most of the child
   * pages are never activated, but solely displayed in the outline tree.
   * <p>
   * Subclasses can override this method. In most cases it is preferable to override
   * {@link #execCreateChildPage(ITableRow)} instead.<br/>
   * This default implementation checks whether {@code execCreateChildPage} is overridden and returns a new virtual
   * page, or {@code null} otherwise.
   * 
   * @param row
   *          a table row for which a new virtual child page should be created
   * @return a new virtual child page for {@code row}
   * @throws ProcessingException
   * @see VirtualPage
   * @see IVirtualTreeNode
   */
  @ConfigOperation
  @Order(111)
  protected IPage execCreateVirtualChildPage(ITableRow row) throws ProcessingException {
    if (ConfigurationUtility.isMethodOverwrite(AbstractPageWithTable.class, "execCreateChildPage", new Class[]{ITableRow.class}, AbstractPageWithTable.this.getClass())) {
      return new VirtualPage();
    }
    return null;
  }

  /**
   * Resolves a virtual tree node and returns the real tree node.
   * <p>
   * This implementation does the following:
   * <ul>
   * <li>returns {@code null} if no table row is linked to {@code node}
   * <li>else creates a new child page by calling {@link #execCreateChildPage(ITableRow)}, links the table row to the
   * new tree node and returns the new node.
   * </ul>
   * 
   * @param node
   *          the virtual tree node to be resolved
   * @return a new real tree node, replacing the virtual tree node
   * @throws ProcessingException
   */
  @Override
  protected ITreeNode execResolveVirtualChildNode(IVirtualTreeNode node) throws ProcessingException {
    ITableRow row = getTableRowFor(node);
    if (row == null) {
      return null;
    }
    //remove old association
    unlinkTableRowWithPage(row);
    //add new association
    IPage childPage = createChildPageInternal(row);
    if (childPage != null) {
      node.setResolvedNode(childPage);
      ICell tableCell = m_table.getSummaryCell(row);
      childPage.setFilterAccepted(row.isFilterAccepted());
      childPage.setEnabledInternal(row.isEnabled());
      childPage.getCellForUpdate().updateFrom(tableCell);
      linkTableRowWithPage(row, childPage);
    }
    return childPage;
  }

  private Class<? extends ITable> getConfiguredTable() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, ITable.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    super.initConfig();
    m_searchActive = true;
    setSearchRequired(getConfiguredSearchRequired());
    setShowEmptySpaceMenus(getConfiguredShowEmptySpaceMenus());
    setShowTableRowMenus(getConfiguredShowTableRowMenus());
    Class<? extends ITable> tableClass = getConfiguredTable();
    if (tableClass != null) {
      try {
        m_table = (T) ConfigurationUtility.newInnerInstance(this, tableClass);
        if (m_table instanceof AbstractTable) {
          ((AbstractTable) m_table).setContainerInternal(this);
        }
        m_table.addTableListener(new P_TableListener());
        m_table.setEnabled(isEnabled());
        m_table.setAutoDiscardOnDelete(true);
        m_table.setUserPreferenceContext(getUserPreferenceContext());
        m_table.initTable();
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    // legacy-support for searchform-inner classes
    if (getConfiguredSearchForm() == null) {
      Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
      Class<? extends ISearchForm> searchFormClass = ConfigurationUtility.filterClass(dca, ISearchForm.class);
      if (searchFormClass != null) {
        LOG.warn("inner searchforms are deprecated...");
        try {
          setSearchForm(ConfigurationUtility.newInnerInstance(this, searchFormClass));
        }
        catch (Exception e) {
          LOG.warn(null, e);
        }
      }
    }
  }

  /**
   * Ensures that the search form is initialized but not started, if one is defined for this table.
   * This allows lazy initialization of search forms.
   */
  protected void ensureSearchFormCreated() {
    if (m_searchForm == null) {
      try {
        setSearchForm(execCreateSearchForm());
      }
      catch (Exception e) {
        LOG.warn("unable to setSearchForm", e);
      }
    }
  }

  /**
   * creates the search form, but doesn't start it
   * called by {@link #ensureSearchFormCreated()}
   * 
   * @return {@link ISearchForm}
   * @throws ProcessingException
   * @since 3.8.2
   */
  protected ISearchForm execCreateSearchForm() throws ProcessingException {
    if (getConfiguredSearchForm() == null) {
      return null;
    }
    try {
      return getConfiguredSearchForm().newInstance();
    }
    catch (Exception e) {
      LOG.warn("creation of searchForm " + getConfiguredSearchForm() + " failed", e);
    }
    return null;
  }

  /**
   * Ensures that the search form is started (lazy starting)
   */
  protected void ensureSearchFormStarted() {
    if (m_searchForm != null && !m_searchForm.isFormOpen()) {
      try {
        m_searchForm.startSearch();
        notifyMemoryPolicyOfSearchFormStart();
      }
      catch (Exception e) {
        LOG.warn(null, e);
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
    m_searchForm.setAutoAddRemoveOnDesktop(false);
    // listen for search action
    m_searchFormListener = new FormListener() {
      @Override
      public void formChanged(FormEvent e) throws ProcessingException {
        switch (e.getType()) {
          case FormEvent.TYPE_STORE_AFTER: {
            // save navigation history
            IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
            IPage page = AbstractPageWithTable.this;
            if (desktop != null && desktop.getOutline() != null && desktop.getOutline().getActivePage() == page) {
              SERVICES.getService(INavigationHistoryService.class).addStep(0, page);
            }
            // do page reload to execute search
            try {
              reloadPage();
            }
            catch (ProcessingException ex) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
            }
            break;
          }
        }
      }
    };
    m_searchForm.addFormListener(m_searchFormListener);
    try {
      execInitSearchForm();
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }
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
  }

  private void notifyMemoryPolicyOfSearchFormStart() {
    //use memory policy to handle content caching
    try {
      IMemoryPolicy policy = ClientSyncJob.getCurrentSession().getMemoryPolicy();
      if (policy != null) {
        policy.pageSearchFormStarted(this);
      }
    }
    catch (Throwable t) {
      LOG.error("pageCreated " + getClass().getSimpleName(), t);
    }
  }

  /**
   * Initializes the search form associated with this page. This method is called before the
   * search form is used for the first time.
   * <p>
   * Legacy: If the search form is defined as inner class, this method is called when this page is initialized.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @throws ProcessingException
   * @see #ensureSearchFormCreated()
   * @see #ensureSearchFormStarted()
   */
  @ConfigOperation
  @Order(120)
  protected void execInitSearchForm() throws ProcessingException {
  }

  @Override
  public final T getTable() {
    if (m_table == null) {
      ensureInitialized();
    }
    return m_table;
  }

  @Override
  public boolean isShowEmptySpaceMenus() {
    return m_showEmptySpaceMenus;
  }

  @Override
  public void setShowEmptySpaceMenus(boolean showEmptySpaceMenus) {
    m_showEmptySpaceMenus = showEmptySpaceMenus;
  }

  @Override
  public boolean isShowTableRowMenus() {
    return m_showTableRowMenus;
  }

  @Override
  public void setShowTableRowMenus(boolean showTableRowMenus) {
    m_showTableRowMenus = showTableRowMenus;
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
  }

  @Override
  public SearchFilter getSearchFilter() {
    ensureSearchFormCreated();
    ensureSearchFormStarted();
    if (getSearchFormInternal() != null) {
      return getSearchFormInternal().getSearchFilter();
    }
    else {
      ISearchFilterService sfs = SERVICES.getService(ISearchFilterService.class);
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
    return m_searchRequired;
  }

  @Override
  public void setSearchRequired(boolean b) {
    m_searchRequired = b;
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    if (m_table != null) {
      m_table.setEnabled(isEnabled());
    }
  }

  @Override
  public boolean isSearchActive() {
    return m_searchActive;
  }

  @Override
  public void setSearchActive(boolean b) {
    m_searchActive = b;
    if (isSelectedNode()) {
      getOutline().setSearchForm(m_searchActive ? getSearchFormInternal() : null);
    }
  }

  @Override
  public void pageActivatedNotify() {
    ensureInitialized();
    ensureSearchFormCreated();
    ensureSearchFormStarted();
    super.pageActivatedNotify();
  }

  @Override
  public void setPagePopulateStatus(IProcessingStatus status) {
    super.setPagePopulateStatus(status);
    getTable().tablePopulated();
  }

  @Override
  @SuppressWarnings("deprecation")
  public IProcessingStatus getTablePopulateStatus() {
    return getPagePopulateStatus();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void setTablePopulateStatus(IProcessingStatus status) {
    setPagePopulateStatus(status);
  }

  /**
   * load table data
   */
  private void loadTableDataImpl() throws ProcessingException {
    if (m_table != null) {
      try {
        m_table.setTableChanging(true);
        //
        ensureSearchFormCreated();
        ensureSearchFormStarted();
        execPopulateTable();
      }
      catch (Throwable t) {
        m_table.discardAllRows();
        ProcessingException pe;
        if (t instanceof ProcessingException) {
          pe = (ProcessingException) t;
        }
        else {
          pe = new ProcessingException(t.getMessage(), t);
        }
        if (pe.isInterruption()) {
          setPagePopulateStatus(new ProcessingStatus(ScoutTexts.get("SearchWasCanceled"), ProcessingStatus.CANCEL));
        }
        else {
          setPagePopulateStatus(new ProcessingStatus(ScoutTexts.get("ErrorWhileLoadingData"), ProcessingStatus.CANCEL));
        }
        throw pe;
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  /**
   * load tree children<br>
   * this method delegates to the table reload<br>
   * when the table is loaded and this node is not a leaf node then the table
   * rows are mirrored in child nodes
   */
  @Override
  public final void loadChildren() throws ProcessingException {
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
      Object[] oldSelectedRowKeys = null;
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
      ClientSyncJob.getCurrentSession().getMemoryPolicy().beforeTablePageLoadData(this);
      try {
        loadTableDataImpl();
      }
      catch (ProcessingException pe) {
        if (!pe.isInterruption()) {
          throw pe;
        }
      }
      finally {
        ClientSyncJob.getCurrentSession().getMemoryPolicy().afterTablePageLoadData(this);
      }
      setChildrenLoaded(true);
      setChildrenDirty(false);
      // table events will handle automatic tree changes in case table is
      // mirrored in tree.
      // restore currently selected tree node when it was owned by our table
      // rows.
      // in case selection was lost, try to select similar index as before

      if (tree != null && oldSelectionOwned && tree.getSelectedNode() == null) {
        ITreeNode newSelectedNode = null;
        ITableRow row = getTable().getSelectedRow();
        if (row != null) {
          newSelectedNode = getTreeNodeFor(row);
        }
        else {
          row = getTable().findRowByKey(oldSelectedRowKeys);
          if (row != null) {
            newSelectedNode = getTreeNodeFor(row);
          }
          else if (oldSelectedNode != null && oldSelectedNode.getTree() == tree) {
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
          newSelectedNode = tree.resolveVirtualNode(newSelectedNode);
          tree.selectNode(newSelectedNode);
        }
      }
    }
    finally {
      if (tree != null) {
        tree.setTreeChanging(false);
      }
    }
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop != null) {
      desktop.afterTablePageLoaded(this);
    }
  }

  private void linkTableRowWithPage(ITableRow tableRow, IPage page) {
    m_tableRowToPageMap.put(tableRow, page);
    m_pageToTableRowMap.put(page, tableRow);
  }

  private void unlinkTableRowWithPage(ITableRow tableRow) {
    IPage page = m_tableRowToPageMap.remove(tableRow);
    if (page != null) {
      m_pageToTableRowMap.remove(page);
    }
  }

  @Override
  public IPage[] getUpdatedChildPagesFor(ITableRow[] tableRows) {
    return getChildPagesFor(tableRows, true);
  }

  /**
   * Computes the list of linked child pages for the given table rows. Revalidates the the pages cell
   * if <code>updateChildPageCells</code> is true. Otherwise, the cells are not updated.
   */
  private IPage[] getChildPagesFor(ITableRow[] tableRows, boolean updateChildPageCells) {
    IPage[] pages = new IPage[tableRows.length];
    int missingCount = 0;
    try {
      for (int i = 0; i < tableRows.length; i++) {
        pages[i] = m_tableRowToPageMap.get(tableRows[i]);
        if (pages[i] != null) {
          if (updateChildPageCells) {
            // update tree nodes from table rows
            ICell tableCell = getTable().getSummaryCell(tableRows[i]);
            pages[i].setEnabledInternal(tableRows[i].isEnabled());
            pages[i].getCellForUpdate().updateFrom(tableCell);
          }
        }
        else {
          missingCount++;
        }
      }
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    if (missingCount > 0) {
      IPage[] tmp = new IPage[pages.length - missingCount];
      int index = 0;
      for (IPage element : pages) {
        if (element != null) {
          tmp[index] = element;
          index++;
        }
      }
      pages = tmp;
    }
    return pages;
  }

  @Override
  public ITreeNode getTreeNodeFor(ITableRow tableRow) {
    if (tableRow == null) {
      return null;
    }
    else {
      return m_tableRowToPageMap.get(tableRow);
    }
  }

  @Override
  public ITableRow getTableRowFor(ITreeNode childPageNode) {
    return m_pageToTableRowMap.get(childPageNode);
  }

  @Override
  public ITableRow[] getTableRowsFor(ITreeNode[] childPageNodes) {
    ITableRow[] rows = new ITableRow[childPageNodes.length];
    int missingCount = 0;
    for (int i = 0; i < childPageNodes.length; i++) {
      rows[i] = m_pageToTableRowMap.get(childPageNodes[i]);
      if (rows[i] == null) {
        missingCount++;
      }
    }
    if (missingCount > 0) {
      ITableRow[] tmp = new ITableRow[rows.length - missingCount];
      int index = 0;
      for (ITableRow element : rows) {
        if (element != null) {
          tmp[index] = element;
          index++;
        }
      }
      rows = tmp;
    }
    return rows;
  }

  private OutlineMediator getOutlineMediator() {
    if (getOutline() == null) {
      return null;
    }

    return getOutline().getOutlineMediator();
  }

  /**
   * Table listener and tree controller<br>
   * the table is reflected in tree children only if the tree/page node is not
   * marked as being a leaf
   */
  private class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      OutlineMediator outlineMediator = getOutlineMediator();

      switch (e.getType()) {
        case TableEvent.TYPE_ROW_ACTION: {
          if (outlineMediator != null) {
            outlineMediator.mediateTableRowAction(e, AbstractPageWithTable.this);
          }
          break;
        }
        case TableEvent.TYPE_ALL_ROWS_DELETED:
        case TableEvent.TYPE_ROWS_DELETED: {
          if (!isLeaf()) {
            ITableRow[] tableRows = e.getRows();
            IPage[] childNodes = getChildPagesFor(tableRows, false);
            for (int i = 0; i < childNodes.length; i++) {
              unlinkTableRowWithPage(tableRows[i]);
            }

            if (outlineMediator != null) {
              outlineMediator.mediateTableRowsDeleted(childNodes, AbstractPageWithTable.this);
            }
          }
          break;
        }
        case TableEvent.TYPE_ROWS_INSERTED: {
          if (!isLeaf()) {
            ArrayList<IPage> childPageList = new ArrayList<IPage>();
            ITableRow[] tableRows = e.getRows();
            for (ITableRow element : tableRows) {
              try {
                IPage childPage = execCreateVirtualChildPage(element);
                if (childPage != null) {
                  ICell tableCell = m_table.getSummaryCell(element);
                  childPage.getCellForUpdate().updateFrom(tableCell);
                  linkTableRowWithPage(element, childPage);
                  childPageList.add(childPage);
                }
              }
              catch (ProcessingException ex) {
                SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
              }
              catch (Throwable t) {
                SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Page " + element, t));
              }
            }

            if (outlineMediator != null) {
              IPage[] childPages = childPageList.toArray(new IPage[childPageList.size()]);
              outlineMediator.mediateTableRowsInserted(tableRows, childPages, AbstractPageWithTable.this);
            }

            // check if a page was revoked
            for (ITableRow element : tableRows) {
              IPage page = m_tableRowToPageMap.get(element);
              if (page != null && page.getParentNode() == null) {
                unlinkTableRowWithPage(element);
              }
            }
          }
          break;
        }
        case TableEvent.TYPE_ROWS_UPDATED: {
          if (outlineMediator != null) {
            outlineMediator.mediateTableRowsUpdated(e, AbstractPageWithTable.this);
          }
          break;
        }
        case TableEvent.TYPE_ROW_ORDER_CHANGED: {
          if (outlineMediator != null) {
            outlineMediator.mediateTableRowOrderChanged(e, AbstractPageWithTable.this);
          }
          break;
        }
        case TableEvent.TYPE_ROWS_SELECTED: {
          break;
        }
        case TableEvent.TYPE_ROW_FILTER_CHANGED: {
          if (outlineMediator != null) {
            outlineMediator.mediateTableRowFilterChanged(AbstractPageWithTable.this);
          }
          break;
        }
      }// end switch
    }

  }

}
