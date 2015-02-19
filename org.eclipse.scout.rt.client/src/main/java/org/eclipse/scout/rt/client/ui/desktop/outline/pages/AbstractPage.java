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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IMemoryPolicy;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractPage<T extends ITable> extends AbstractTreeNode implements IPage<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPage.class);

  private T m_table;
  private IForm m_detailForm;
  private boolean m_tableVisible;
  private boolean m_detailFormVisible;
  private DataChangeListener m_internalDataChangeListener;
  private final String m_userPreferenceContext;
  private IStatus m_pagePopulateStatus;
  private final Map<ITableRow, IPage> m_tableRowToPageMap = new HashMap<ITableRow, IPage>();
  private final Map<IPage, ITableRow> m_pageToTableRowMap = new HashMap<IPage, ITableRow>();

  @Override
  public T getTable() {
    return m_table;
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
    else {
      return m_tableRowToPageMap.get(tableRow);
    }
  }

  @Override
  public IPage<?> getPageFor(ITableRow tableRow) {
    return (IPage) getTreeNodeFor(tableRow);
  }

  @Override
  public ITableRow getTableRowFor(ITreeNode childPage) {
    return m_pageToTableRowMap.get(childPage);
  }

  @Override
  public List<ITableRow> getTableRowsFor(Collection<? extends ITreeNode> childPageNodes) {
    List<ITableRow> result = new ArrayList<ITableRow>();
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
          buf.append(o.toString());
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
    if (callInitializer) {
      callInitializer();
    }
  }

  /*
   * Configuration
   */

  /**
   * Configures the visibility of this page's table. Typical subclasses of this abstract class use a tabular
   * structure to display data, this includes {@link AbstractPageWithTable} as well as {@link AbstractPageWithNodes}.
   * Set this property to {@code false} if you want to display a detail form within this page.
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
   * Configures the detail form to be used with this page. The form is lazily {@linkplain #ensureDetailFormCreated()
   * created} and {@linkplain #ensureDetailFormStarted() started}.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a form type token
   * @see {@link #startDetailForm(IForm)} for details how the form gets started
   */
  @ConfigProperty(ConfigProperty.FORM)
  @Order(90)
  protected Class<? extends IForm> getConfiguredDetailForm() {
    return null;
  }

  /**
   * Called after this page has been added to the outline tree. This method may set a detail form or check
   * some parameters.
   * <p>
   * Do not load table data here, this should be done lazily in {@link AbstractPageWithTable.execLoadTableData}.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @throws ProcessingException
   * @see #interceptPageActivated()
   */
  @ConfigOperation
  @Order(40)
  protected void execInitPage() throws ProcessingException {
  }

  /**
   * Called after this page has been removed from its associated outline tree.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(50)
  protected void execDisposePage() throws ProcessingException {
  }

  /**
   * Called by the data change listener registered with this page (and the current desktop) through
   * {@link #registerDataChangeListener(Object...)}. Use this callback method
   * to react to data change events by reloading current data, or throwing away cached data etc.
   * <p>
   * Subclasses can override this method.<br/>
   * This default implementation does the following:
   * <ol>
   * <li>if this page is an ancestor of the selected page (or is selected itself) and this page is in the active
   * outline, a full re-load of the page is performed
   * <li>else the children of this page are marked dirty and the page itself is unloaded
   * </ol>
   *
   * @see IDesktop#dataChanged(Object...)
   */
  @ConfigOperation
  @Order(55)
  protected void execDataChanged(Object... dataTypes) throws ProcessingException {
    if (getTree() == null) {
      return;
    }
    //
    HashSet<ITreeNode> pathsToSelections = new HashSet<ITreeNode>();
    for (ITreeNode node : getTree().getSelectedNodes()) {
      ITreeNode tmp = node;
      while (tmp != null) {
        pathsToSelections.add(tmp);
        tmp = tmp.getParentNode();
      }
    }
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    final boolean isActiveOutline = (desktop != null ? desktop.getOutline() == this.getOutline() : false);
    if (isActiveOutline && pathsToSelections.contains(this)) {
      try {
        /*
         * Ticket 77332 (deleting a node in the tree) also requires a reload So
         * the selected and its ancestor nodes require same processing
         */
        if (desktop != null) {
          Bookmark bm = desktop.createBookmark();
          setChildrenDirty(true);
          desktop.activateBookmark(bm);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
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
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  /**
   * Called after this page has (re)loaded its data. This method is called after {@link #loadChildren()} has
   * been called.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(60)
  protected void execPageDataLoaded() throws ProcessingException {
  }

  /**
   * Called whenever this page is selected in the outline tree.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(70)
  protected void execPageActivated() throws ProcessingException {
  }

  /**
   * Called whenever this page is de-selected in the outline tree.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(80)
  protected void execPageDeactivated() throws ProcessingException {
  }

  /**
   * Initializes the detail form associated with this page. This method is called before the
   * detail form is used for the first time.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @throws ProcessingException
   * @see #ensureDetailFormCreated()
   * @see #ensureDetailFormStarted()
   */
  @ConfigOperation
  @Order(120)
  protected void execInitDetailForm() throws ProcessingException {
  }

  protected abstract T initTable();

  @Override
  protected void initConfig() {
    super.initConfig();
    m_table = initTable();
    setTableVisible(getConfiguredTableVisible());
    setDetailFormVisible(getConfiguredDetailFormVisible());
  }

  /*
   * Runtime
   */
  @Override
  public void initPage() throws ProcessingException {
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
    }
    finally {
      setInitializing(false);
    }
  }

  @Override
  public IStatus getPagePopulateStatus() {
    return m_pagePopulateStatus;
  }

  @Override
  public void setPagePopulateStatus(IStatus status) {
    m_pagePopulateStatus = status;
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
  public IPage<?> getChildPage(final int childIndex) {
    ///make it model thread safe
    if (ClientSyncJob.isSyncClientJob()) {
      try {
        return (IPage) getTree().resolveVirtualNode(getChildNode(childIndex));
      }
      catch (ProcessingException e) {
        LOG.error("failed to create the real page from the virtual page", e);
      }
    }
    return (IPage) getChildNode(childIndex);
  }

  @Override
  public List<IPage<?>> getChildPages() {
    if (ClientSyncJob.isSyncClientJob()) {
      try {
        getTree().resolveVirtualNodes(getChildNodes());
      }
      catch (ProcessingException e) {
        LOG.error("failed to create the real page from the virtual page", e);
      }
    }
    List<IPage<?>> childPages = new ArrayList<IPage<?>>();
    for (ITreeNode childNode : getChildNodes()) {
      childPages.add((IPage) childNode);
    }
    return childPages;
  }

  @Override
  public void nodeAddedNotify() {
    try {
      initPage();
      //notify memory policy
      IMemoryPolicy policy = ClientSyncJob.getCurrentSession().getMemoryPolicy();
      if (policy != null) {
        policy.pageCreated(this);
      }
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Exception e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
    }
  }

  @Override
  public void nodeRemovedNotify() {
    try {
      interceptDisposePage();
      disposeDetailForm();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Exception e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
    }
    // automatically remove all data change listeners
    if (m_internalDataChangeListener != null) {
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null) {
        desktop.removeDataChangeListener(m_internalDataChangeListener);
      }
    }
  }

  @Override
  public void pageActivatedNotify() {
    try {
      ensureDetailFormCreated();
      ensureDetailFormStarted();
      interceptPageActivated();
    }
    catch (ProcessingException t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(t);
    }
    catch (Exception e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
    }
  }

  @Override
  public void pageDeactivatedNotify() {
    try {
      interceptPageDeactivated();
    }
    catch (ProcessingException p) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(p);
    }
    catch (Exception e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
    }
  }

  protected IForm createDetailForm() throws ProcessingException {
    if (getConfiguredDetailForm() == null) {
      return null;
    }
    try {
      return getConfiguredDetailForm().newInstance();
    }
    catch (Exception e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + getConfiguredDetailForm().getName() + "'.", e));
    }
    return null;
  }

  /**
   * Starts the form.
   * <p>
   * The default uses {@link IForm#start()} and therefore expects a form handler to be previously set. Override to call
   * a custom start method or implement a {@link IForm#start()} on the detail form.
   */
  protected void startDetailForm() throws ProcessingException {
    getDetailForm().start();
  }

  protected void ensureDetailFormCreated() throws ProcessingException {
    if (getDetailForm() != null) {
      return;
    }
    IForm form = createDetailForm();
    if (form != null) {
      setDetailForm(form);
      execInitDetailForm();
    }
  }

  protected void ensureDetailFormStarted() throws ProcessingException {
    if (getDetailForm() == null || getDetailForm().isFormOpen()) {
      return;
    }
    startDetailForm();
  }

  protected void disposeDetailForm() throws ProcessingException {
    if (getDetailForm() != null) {
      getDetailForm().doClose();
      setDetailForm(null);
    }
  }

  @Override
  public IForm getDetailForm() {
    return m_detailForm;
  }

  @Override
  public void setDetailForm(IForm form) {
    if (form != null) {
      if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
        form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
      }
      if (form.getDisplayViewId() == null) {
        form.setDisplayViewId(IForm.VIEW_ID_PAGE_DETAIL);
      }
      form.setAutoAddRemoveOnDesktop(false);
    }
    m_detailForm = form;
    if (isSelectedNode()) {
      getOutline().setDetailForm(m_detailForm);
      firePageChanged(this);
    }
  }

  /**
   * Register a {@link DataChangeListener} on the desktop for these dataTypes<br>
   * Example:
   *
   * <pre>
   * registerDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = new WeakDataChangeListener() {
        @Override
        public void dataChanged(Object... innerDataTypes) throws ProcessingException {
          interceptDataChanged(innerDataTypes);
        }
      };
    }
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getVirtualDesktop();
    }
    desktop.addDataChangeListener(m_internalDataChangeListener, dataTypes);
  }

  @Override
  public void dataChanged(Object... dataTypes) {
    try {
      interceptDataChanged(dataTypes);
    }
    catch (ProcessingException p) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(p);
    }
    catch (Exception e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
    }
  }

  /**
   * Unregister the {@link DataChangeListener} from the desktop for these
   * dataTypes<br>
   * Example:
   *
   * <pre>
   * unregisterDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      //sle Ticket 92'909: AbstractPage unregisterDataChangeListener NullPointer
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop == null) {
        desktop = ClientSyncJob.getCurrentSession().getVirtualDesktop();
      }
      desktop.removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  @Override
  public final void reloadPage() throws ProcessingException {
    ITree tree = getTree();
    if (tree != null) {
      try {
        tree.setTreeChanging(true);
        //
        // do NOT unload page, because this will clear the selection
        // //getOutline().unloadNode(this);
        loadChildren();
      }
      finally {
        tree.setTreeChanging(false);
      }
    }
  }

  @Override
  public void loadChildren() throws ProcessingException {
    super.loadChildren();
    interceptPageDataLoaded();
  }

  @Override
  protected void onVirtualChildNodeResolved(ITreeNode resolvedNode) {
    super.onVirtualChildNodeResolved(resolvedNode);
    firePageChanged((IPage) resolvedNode);
  }

  @Override
  public boolean isTableVisible() {
    return m_tableVisible;
  }

  @Override
  public void setTableVisible(boolean tableVisible) {
    if (m_tableVisible != tableVisible) {
      m_tableVisible = tableVisible;
      firePageChanged(this);
    }
  }

  @Override
  public boolean isDetailFormVisible() {
    return m_detailFormVisible;
  }

  @Override
  public void setDetailFormVisible(boolean detailFormVisible) {
    if (m_detailFormVisible != detailFormVisible) {
      m_detailFormVisible = detailFormVisible;
      firePageChanged(this);
    }
  }

  /**
   * Note: set*Visible methods are called by initConfig(), at this point getTree() is still null.
   * Tree can also be null during shutdown.
   */
  protected void firePageChanged(IPage page) {
    if (getOutline() != null) {
      getOutline().firePageChanged(page);
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

  protected final void interceptPageDataLoaded() throws ProcessingException {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PagePageDataLoadedChain chain = new PagePageDataLoadedChain(extensions);
    chain.execPageDataLoaded();
  }

  protected final void interceptPageActivated() throws ProcessingException {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PagePageActivatedChain chain = new PagePageActivatedChain(extensions);
    chain.execPageActivated();
  }

  protected final void interceptDataChanged(Object... dataTypes) throws ProcessingException {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageDataChangedChain chain = new PageDataChangedChain(extensions);
    chain.execDataChanged(dataTypes);
  }

  protected final void interceptInitPage() throws ProcessingException {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageInitPageChain chain = new PageInitPageChain(extensions);
    chain.execInitPage();
  }

  protected final void interceptPageDeactivated() throws ProcessingException {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PagePageDeactivatedChain chain = new PagePageDeactivatedChain(extensions);
    chain.execPageDeactivated();
  }

  protected final void interceptDisposePage() throws ProcessingException {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageDisposePageChain chain = new PageDisposePageChain(extensions);
    chain.execDisposePage();
  }

  protected static class LocalPageExtension<OWNER extends AbstractPage> extends LocalTreeNodeExtension<OWNER> implements IPageExtension<OWNER> {

    public LocalPageExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPageDataLoaded(PagePageDataLoadedChain chain) throws ProcessingException {
      getOwner().execPageDataLoaded();
    }

    @Override
    public void execPageActivated(PagePageActivatedChain chain) throws ProcessingException {
      getOwner().execPageActivated();
    }

    @Override
    public void execDataChanged(PageDataChangedChain chain, Object... dataTypes) throws ProcessingException {
      getOwner().execDataChanged(dataTypes);
    }

    @Override
    public void execInitPage(PageInitPageChain chain) throws ProcessingException {
      getOwner().execInitPage();
    }

    @Override
    public void execPageDeactivated(PagePageDeactivatedChain chain) throws ProcessingException {
      getOwner().execPageDeactivated();
    }

    @Override
    public void execDisposePage(PageDisposePageChain chain) throws ProcessingException {
      getOwner().execDisposePage();
    }
  }

  @Override
  protected IPageExtension<? extends AbstractPage> createLocalExtension() {
    return new LocalPageExtension<AbstractPage>(this);
  }

}
