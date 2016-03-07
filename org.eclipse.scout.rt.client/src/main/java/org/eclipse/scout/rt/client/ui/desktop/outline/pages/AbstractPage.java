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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.IMemoryPolicy;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitDetailFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper.IMenuTypeMapper;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPage<T extends ITable> extends AbstractTreeNode implements IPage<T> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPage.class);

  private static final IMenuTypeMapper TREE_MENU_TYPE_MAPPER = new IMenuTypeMapper() {
    @Override
    public IMenuType map(IMenuType menuType) {
      if (menuType == TreeMenuType.SingleSelection) {
        return TableMenuType.EmptySpace;
      }
      return menuType;
    }
  };

  private T m_table;
  private IForm m_detailForm;
  private boolean m_tableVisible;
  private boolean m_detailFormVisible;
  private boolean m_pageMenusAdded;
  private DataChangeListener m_internalDataChangeListener;
  private final String m_userPreferenceContext;
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
   * Called after this page has been added to the outline tree. This method may set a detail form or check some
   * parameters.
   * <p>
   * Do not load table data here, this should be done lazily in {@link AbstractPageWithTable.execLoadTableData}.
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
   * @see IDesktop#dataChanged(Object...)
   */
  @ConfigOperation
  @Order(55)
  protected void execDataChanged(Object... dataTypes) {
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
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    final boolean isActiveOutline = (desktop != null ? desktop.getOutline() == this.getOutline() : false);
    final boolean isRootNode = pathsToSelections.isEmpty() && getTree() != null && getTree().getRootNode() == this;
    if (isActiveOutline && (pathsToSelections.contains(this) || isRootNode)) {
      try {
        //TODO fko: maybe remove when bookmarks can be done on outline level? (currently only pages)
        if (isRootNode) {
          this.reloadPage();
        }
        /*
         * Ticket 77332 (deleting a node in the tree) also requires a reload So
         * the selected and its ancestor nodes require same processing
         */
        else if (desktop != null) {
          Bookmark bm = desktop.createBookmark();
          setChildrenDirty(true);
          //activate bookmark without activating the outline, since this would hide active tabs.
          desktop.activateBookmark(bm, false);
        }
      }
      catch (RuntimeException e) {
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
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
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

  protected abstract T initTable();

  @Override
  protected void initConfig() {
    super.initConfig();
    m_table = initTable();
    if (m_table != null) {
      addDefaultTableControls();
    }
    setTableVisible(getConfiguredTableVisible());
    setDetailFormVisible(getConfiguredDetailFormVisible());
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
    }
    finally {
      setInitializing(false);
    }
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
  public IPage<?> getChildPage(final int childIndex) {
    ///make it model thread safe
    // TOOD [mvi] why not always in model thread?
    if (ModelJobs.isModelThread()) {
      try {
        return (IPage) getTree().resolveVirtualNode(getChildNode(childIndex));
      }
      catch (RuntimeException e) {
        LOG.error("failed to create the real page from the virtual page", e);
      }
    }
    return (IPage) getChildNode(childIndex);
  }

  @Override
  public List<IPage<?>> getChildPages() {
    // TOOD [mvi] why not always in model thread?
    if (ModelJobs.isModelThread()) {
      try {
        getTree().resolveVirtualNodes(getChildNodes());
      }
      catch (RuntimeException e) {
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
      IMemoryPolicy policy = ClientSessionProvider.currentSession().getMemoryPolicy();
      if (policy != null) {
        policy.pageCreated(this);
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
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
    // automatically remove all data change listeners
    if (m_internalDataChangeListener != null) {
      IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
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
      execDetailFormActivated();
      enhanceTableWithPageMenus();
      interceptPageActivated();
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void pageDeactivatedNotify() {
    try {
      interceptPageDeactivated();
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  protected void enhanceTableWithPageMenus() {
    if (m_pageMenusAdded) {
      return;
    }
    m_pageMenusAdded = true;
    ITable table = getTable();
    if (table != null) {
      ITableContextMenu contextMenu = table.getContextMenu();
      List<IMenu> menus = contextMenu.getChildActions();
      for (IMenu menu : getOutline().getContextMenu().getChildActions()) {
        if (!OutlineMenuWrapper.containsWrappedMenu(table.getMenus(), menu)) {
          // mapping from TreeMenuType to TableMenuType
          menus.add(new OutlineMenuWrapper(menu, TREE_MENU_TYPE_MAPPER));
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
    return ClientRunContexts.copyCurrent()
        .withOutline(getOutline(), true)
        .call(new Callable<IForm>() {
          @Override
          public IForm call() throws Exception {
            return getConfiguredDetailForm().newInstance();
          }
        });
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
    IForm form = createDetailForm();
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

  protected void execDetailFormActivated() {
    if (getDetailForm() != null) {
      getDetailForm().getUIFacade().fireFormActivatedFromUI();
    }
  }

  protected void disposeDetailForm() {
    if (getDetailForm() != null) {
      getDetailForm().doClose();
      setDetailForm(null);
    }
  }

  protected void disposeTable() {
    if (getTable() != null) {
      getTable().disposeTable();
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
    firePageChanged(this);
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

    form.setModal(false); // TODO [5.2] dwi: do the same as WrappedFormField
    form.setShowOnStart(false);
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
        public void dataChanged(Object... innerDataTypes) {
          AbstractPage.this.dataChanged(innerDataTypes);
        }
      };
    }

    IDesktop.CURRENT.get().addDataChangeListener(m_internalDataChangeListener, dataTypes);
  }

  @Override
  public void dataChanged(final Object... dataTypes) {
    ClientRunContexts.copyCurrent()
        .withOutline(getOutline(), true)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              interceptDataChanged(dataTypes);
            }
            catch (Exception e) {
              BEANS.get(ExceptionHandler.class).handle(e);
            }
          }
        });
  }

  /**
   * Unregister the {@link DataChangeListener} from the desktop for these dataTypes<br>
   * Example:
   *
   * <pre>
   * unregisterDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      IDesktop.CURRENT.get().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  @Override
  public final void reloadPage() {
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
  public void loadChildren() {
    super.loadChildren();
    interceptPageDataLoaded();
  }

  @Override
  protected void onVirtualChildNodeResolved(ITreeNode resolvedNode) {
    super.onVirtualChildNodeResolved(resolvedNode);
    firePageChanged((IPage) resolvedNode);
  }

  @Override
  public List<IMenu> computeParentTablePageMenus(IPageWithTable<?> parentTablePage) {
    ITableRow row = parentTablePage.getTableRowFor(this);
    if (row == null) {
      return CollectionUtility.emptyArrayList();
    }

    ITable table = parentTablePage.getTable();
    table.getUIFacade().setSelectedRowsFromUI(CollectionUtility.arrayList(row));
    return ActionUtility.getActions(table.getContextMenu().getChildActions(), ActionUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TableMenuType.SingleSelection), false));
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
   * Note: set*Visible methods are called by initConfig(), at this point getTree() is still null. Tree can also be null
   * during shutdown.
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

  protected final void interceptPageDataLoaded() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PagePageDataLoadedChain chain = new PagePageDataLoadedChain(extensions);
    chain.execPageDataLoaded();
  }

  protected final void interceptPageActivated() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PagePageActivatedChain chain = new PagePageActivatedChain(extensions);
    chain.execPageActivated();
  }

  protected final void interceptDataChanged(Object... dataTypes) {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageDataChangedChain chain = new PageDataChangedChain(extensions);
    chain.execDataChanged(dataTypes);
  }

  protected final void interceptInitPage() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageInitPageChain chain = new PageInitPageChain(extensions);
    chain.execInitPage();
  }

  protected final void interceptPageDeactivated() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PagePageDeactivatedChain chain = new PagePageDeactivatedChain(extensions);
    chain.execPageDeactivated();
  }

  protected final void interceptDisposePage() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageDisposePageChain chain = new PageDisposePageChain(extensions);
    chain.execDisposePage();
  }

  protected final void interceptInitDetailForm() {
    List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions = getAllExtensions();
    PageInitDetailFormChain chain = new PageInitDetailFormChain(extensions);
    chain.execInitDetailForm();
  }

  protected static class LocalPageExtension<OWNER extends AbstractPage> extends LocalTreeNodeExtension<OWNER> implements IPageExtension<OWNER> {

    public LocalPageExtension(OWNER owner) {
      super(owner);
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
  }

  @Override
  protected IPageExtension<? extends AbstractPage> createLocalExtension() {
    return new LocalPageExtension<AbstractPage>(this);
  }

}
