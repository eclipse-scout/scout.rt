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

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IMemoryPolicy;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ContextMap;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractPage extends AbstractTreeNode implements IPage {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPage.class);

  private IForm m_detailForm;
  private final ContextMap m_contextMap;
  private boolean m_tableVisible;
  private DataChangeListener m_internalDataChangeListener;
  private final String m_userPreferenceContext;
  private IProcessingStatus m_pagePopulateStatus;

  /**
   * use this static method to create a string based on the vargs that can be used as userPreferenceContext
   */
  public static String createUserPreferenceContext(Object... vargs) {
    StringBuffer buf = new StringBuffer();
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
    this(true, null, userPreferenceContext);
  }

  public AbstractPage(boolean callInitializer) {
    this(callInitializer, null, null);
  }

  public AbstractPage(ContextMap contextMap) {
    this(true, contextMap, null);
  }

  public AbstractPage(boolean callInitializer, String userPreferenceContext) {
    this(callInitializer, null, userPreferenceContext);
  }

  public AbstractPage(boolean callInitializer, ContextMap contextMap) {
    this(callInitializer, contextMap, null);
  }

  public AbstractPage(boolean callInitializer, ContextMap contextMap, String userPreferenceContext) {
    super(false);
    m_contextMap = contextMap;
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
  @ConfigPropertyValue("true")
  protected boolean getConfiguredTableVisible() {
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
  @ConfigPropertyValue("null")
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
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * Provides a documentation text or description of this page. The text is intended to be included in external
   * documentation. This method is typically processed by a documentation generation tool or similar.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return a documentation text, suitable to be included in external documents
   */
  @ConfigProperty(ConfigProperty.DOC)
  @Order(60)
  @ConfigPropertyValue("null")
  protected String getConfiguredDoc() {
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
   * @see #execPageActivated()
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
          desktop.activateBookmark(bm, false);
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
   * Called after this page has (re)loaded its data. This method is called after {@link #loadChildren()} has been
   * called.
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

  protected ContextMap getContextMap() {
    return m_contextMap;
  }

  @Override
  protected void initConfig() {
    setTableVisible(getConfiguredTableVisible());
    super.initConfig();
  }

  /*
   * Runtime
   */
  @Override
  public void initPage() throws ProcessingException {
    Cell cell = getCellForUpdate();
    if (cell.getText() == null && getConfiguredTitle() != null) {
      cell.setText(getConfiguredTitle());
    }
    if (cell.getIconId() == null && getConfiguredIconId() != null) {
      cell.setIconId(getConfiguredIconId());
    }
    execInitPage();
  }

  @Override
  public IProcessingStatus getPagePopulateStatus() {
    return m_pagePopulateStatus;
  }

  @Override
  public void setPagePopulateStatus(IProcessingStatus status) {
    m_pagePopulateStatus = status;
  }

  /**
   * @deprecated use {@link #getUserPreferenceContext()} instead
   */
  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  @ConfigOperation
  @Order(95)
  public String getBookmarkIdentifier() {
    return getUserPreferenceContext();
  }

  @Override
  @ConfigOperation
  @Order(95)
  public final String getUserPreferenceContext() {
    return m_userPreferenceContext;
  }

  @Override
  public IOutline getOutline() {
    return (IOutline) getTree();
  }

  @Override
  public IPage getParentPage() {
    return (IPage) getParentNode();
  }

  @Override
  public IPage getChildPage(final int childIndex) {
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
  public IPage[] getChildPages() {
    if (ClientSyncJob.isSyncClientJob()) {
      try {
        getTree().resolveVirtualNodes(getChildNodes());
      }
      catch (ProcessingException e) {
        LOG.error("failed to create the real page from the virtual page", e);
      }
    }
    ITreeNode[] a = getChildNodes();
    IPage[] b = new IPage[a.length];
    for (int i = 0; i < b.length; i++) {
      b[i] = (IPage) a[i];
    }
    return b;
  }

  @Override
  public void nodeAddedNotify() {
    try {
      initPage();
      //notify memory policy
      try {
        IMemoryPolicy policy = ClientSyncJob.getCurrentSession().getMemoryPolicy();
        if (policy != null) {
          policy.pageCreated(this);
        }
      }
      catch (Throwable t) {
        LOG.error("pageCreated " + getClass().getSimpleName(), t);
      }
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
  }

  @Override
  public void nodeRemovedNotify() {
    try {
      execDisposePage();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
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
      execPageActivated();
    }
    catch (Throwable t) {
      if (t instanceof ProcessingException && ((ProcessingException) t).isInterruption()) {
        //nop
      }
      else {
        //ticket 87361: only log a warning
        LOG.warn("Caught a failure, probably due to operation cancelation by the user", t);
      }
    }
  }

  @Override
  public void pageDeactivatedNotify() {
    try {
      execPageDeactivated();
    }
    catch (Throwable t) {
      if (t instanceof ProcessingException && ((ProcessingException) t).isInterruption()) {
        //nop
      }
      else {
        //ticket 87361: only log a warning
        LOG.warn("Caught a failure, probably due to operation cancelation by the user", t);
      }
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
          execDataChanged(innerDataTypes);
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
      execDataChanged(dataTypes);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
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
    execPageDataLoaded();
  }

  @Override
  public boolean isTableVisible() {
    return m_tableVisible;
  }

  @Override
  public void setTableVisible(boolean b) {
    m_tableVisible = b;
  }

}
