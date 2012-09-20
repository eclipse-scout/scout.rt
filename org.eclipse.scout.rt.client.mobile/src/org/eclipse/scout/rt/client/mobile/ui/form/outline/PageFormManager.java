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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.desktop.ActiveOutlineObserver;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class PageFormManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageFormManager.class);

  private PageFormMap m_pageFormMap;
  private ActiveOutlineObserver m_activeOutlineObserver;
  private P_OutlineTreeListener m_outlineTreeListener;
  private P_DesktopListener m_desktopListener;

  private String m_leftPageSlotViewId;
  private String m_middlePageSlotViewId;
  private boolean m_tableStatusVisible;
  private IDesktop m_desktop;

  private List<IForm> m_blockedForms;
  private boolean m_pageSelectionRunning;

  public PageFormManager(String... pageSlotViewIds) {
    this(null, pageSlotViewIds);
  }

  public PageFormManager(IDesktop desktop, String... pageSlotViewIds) {
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create OutlineFormsMediator.");
    }

    initPageSlots(pageSlotViewIds);
    m_pageFormMap = new PageFormMap();
    m_blockedForms = new LinkedList<IForm>();
    m_activeOutlineObserver = new ActiveOutlineObserver(desktop);

    //Since the page is activated by the outline and the outline itself also listens to tree selection events,
    //a UI listener is attached to make sure this listener is called at the end and therefore after the page has been activated properly.
    m_outlineTreeListener = new P_OutlineTreeListener();
    m_activeOutlineObserver.addOutlineUITreeListener(m_outlineTreeListener);

    m_desktopListener = new P_DesktopListener();
    desktop.addDesktopListener(m_desktopListener);
  }

  private void initPageSlots(String... pageSlotViewIds) {
    if (pageSlotViewIds == null || pageSlotViewIds.length == 0 || !StringUtility.hasText(pageSlotViewIds[0])) {
      throw new IllegalArgumentException("At least one pageSlotViewId needs to be specified.");
    }

    m_leftPageSlotViewId = pageSlotViewIds[0];
    if (pageSlotViewIds.length > 1) {
      m_middlePageSlotViewId = pageSlotViewIds[1];
    }
  }

  public void setTableStatusVisible(boolean tableStatusVisible) {
    m_tableStatusVisible = tableStatusVisible;
  }

  public boolean isTableStatusVisible() {
    return m_tableStatusVisible;
  }

  public String getLeftPageSlotViewId() {
    return m_leftPageSlotViewId;
  }

  public String getMiddlePageSlotViewId() {
    return m_middlePageSlotViewId;
  }

  public boolean hasOnlyOnePageSlot() {
    return m_middlePageSlotViewId == null;
  }

  public boolean isPageSelectionRunning() {
    return m_pageSelectionRunning;
  }

  protected void setPageSelectionRunning(boolean pageSelectionRunning) {
    m_pageSelectionRunning = pageSelectionRunning;
  }

  public static boolean isDrillDownPage(IPage page) {
    return page instanceof IPageWithTable && page.getParentNode() instanceof IPageWithNodes;
  }

  private void destroy() {
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    if (m_outlineTreeListener != null) {
      m_activeOutlineObserver.removeOutlineUITreeListener(m_outlineTreeListener);
      m_outlineTreeListener = null;
    }

    m_pageFormMap.clear();
  }

  private void hidePageForms() {
    IPageForm[] pageForms = getDesktop().findForms(IPageForm.class);
    if (pageForms == null) {
      return;
    }

    for (IPageForm pageForm : pageForms) {
      getDesktop().removeForm(pageForm);
    }
  }

  private void hidePage(IPage page) throws ProcessingException {
    if (page == null) {
      return;
    }

    LOG.debug("Hiding page: " + page);
    IPageForm pageForm = m_pageFormMap.get(page);
    if (pageForm != null) {
      getDesktop().removeForm(pageForm);
    }
  }

  private void showPage(IPage page) throws ProcessingException {
    LOG.debug("Showing page: " + page);

    String displayViewId = getLeftPageSlotViewId();

    if (!hasOnlyOnePageSlot()) {
      displayViewId = findPageFormSlot(page);
    }

    showPage(page, displayViewId);
  }

  private String findPageFormSlot(IPage page) {
    if (page instanceof AutoLeafPageWithNodes) {
      return getMiddlePageSlotViewId();
    }

    IPageForm parentPageForm = m_pageFormMap.get(page.getParentPage(), true);
    if (parentPageForm == null) {
      return getLeftPageSlotViewId();
    }

    if (getLeftPageSlotViewId().equals(parentPageForm.getDisplayViewId())) {
      if (isDrillDownPage(page)) {
        return getLeftPageSlotViewId();
      }
      else {
        return getMiddlePageSlotViewId();
      }
    }

    return getLeftPageSlotViewId();
  }

  private void showPageInLeftForm(IPage page) throws ProcessingException {
    showPage(page, getLeftPageSlotViewId());
  }

  private void showPage(IPage page, String viewId) throws ProcessingException {
    IPageForm pageForm = m_pageFormMap.get(viewId, page);
    if (pageForm == null) {
      if (getLeftPageSlotViewId().equals(viewId)) {
        pageForm = createMainPageForm(page);
      }
      else {
        pageForm = createPageForm(page);
      }
      pageForm.setAutoAddRemoveOnDesktop(false);
      pageForm.setDisplayViewId(viewId);
      pageForm.start();

      m_pageFormMap.put(pageForm);
    }

    getDesktop().addForm(pageForm);
  }

  private IMainPageForm createMainPageForm(IPage page) throws ProcessingException {
    PageFormConfig config = new PageFormConfig();
    config.setTablePageAllowed(true);
    config.setTableStatusVisible(isTableStatusVisible());
    if (hasOnlyOnePageSlot()) {
      config.setDetailFormVisible(true);
    }
    else {
      config.setKeepSelection(true);
    }

    return new MainPageForm(page, this, config);
  }

  private IPageForm createPageForm(IPage page) throws ProcessingException {
    PageFormConfig config = new PageFormConfig();
    config.setNodePageSwitchEnabled(true);
    config.setDetailFormVisible(true);

    return new PageForm(page, this, config);
  }

  private IDesktop getDesktop() {
    return m_desktop;
  }

  public void pageSelectedNotify(PageForm pageForm, IPage selectedPage) throws ProcessingException {
    if (selectedPage == null) {
      return;
    }

    //If same page gets selected again, move it to the left.
    if (pageForm != null && pageForm.getPage().equals(selectedPage)) {
      showPageInLeftForm(selectedPage);
    }
    //A AutoLeafPage is not attached to a real outline. Since it already has been activated just show it.
    else if (selectedPage instanceof AutoLeafPageWithNodes) {
      showPage(selectedPage);
    }
    else if (selectedPage.isSelectedNode()) {
      showPage(selectedPage);
    }
    else {
      selectAndExpandPage(selectedPage);
    }
  }

  /**
   * Mainly a copy from AbstractTree.getUIFacade.setNodeSelectedAndExpandedFromUI() without setTreeChanging(true/false)
   * and additional outline change.
   * TreeChanging must not be set otherwise collecting node menus does not work anymore.
   */
  private void selectAndExpandPage(IPage page) throws ProcessingException {
    try {
      setPageSelectionRunning(true);

      //Make sure outline is correctly set
      IOutline outline = page.getOutline();
      if (getDesktop().getOutline() != page.getOutline()) {
        getDesktop().setOutline(page.getOutline());
      }

      //Select node
      ITreeNode node = outline.resolveVirtualNode(page);
      if (node != null) {
        if (node.isChildrenDirty() || node.isChildrenVolatile()) {
          node.loadChildren();
        }
        outline.selectNode(node, false);
        outline.setNodeExpanded(node, true);
        if (!outline.isScrollToSelection()) {
          outline.scrollToSelection();
        }
      }
    }
    finally {
      setPageSelectionRunning(false);
      showBlockedForms();
    }
  }

  private void handleTreeNodeSelected(final ITreeNode deselctedNode, final ITreeNode selectedNode) throws ProcessingException {
    LOG.debug("Tree node selected: " + selectedNode);
    if (selectedNode == null) {
      hidePageForms();
      return;
    }

    showPage((IPage) selectedNode);
  }

  private void handleTreeNodesDeleted(ITreeNode[] deletedNodes) throws ProcessingException {
    if (deletedNodes == null) {
      return;
    }

    for (ITreeNode node : deletedNodes) {
      if (node instanceof IPage) {
        IPage page = (IPage) node;
        handlePageRemoved(page);
      }
    }
  }

  public void pageRemovedNotify(PageForm pageForm, IPage page) throws ProcessingException {
    handlePageRemoved(page);
  }

  private void handlePageRemoved(IPage page) throws ProcessingException {
    if (page == null) {
      return;
    }

    try {
      IPageForm pageForm = m_pageFormMap.get(page);
      if (pageForm != null) {
        pageForm.doClose();
        hidePage(page);
      }
    }
    finally {
      m_pageFormMap.remove(page);
    }

    // Normally, when removing the selected page, the parent page gets selected and the corresponding page form shown.
    // In case of the AutoLeafPageWithNodes, the parent page already is selected, so no selection event will be fired. Therefore we need to show the page form manually.
    if (page instanceof AutoLeafPageWithNodes) {
      IPage parentPage = ((AutoLeafPageWithNodes) page).getActualParentPage();
      if (parentPage.isSelectedNode()) {
        showPage(parentPage);
      }
    }
  }

  public boolean acceptForm(IForm form) {
    //Outline forms are not used at all -> never show them.
    if (form instanceof IOutlineTreeForm || form instanceof IOutlineTableForm) {
      return false;
    }

    //Always block detail forms because they are displayed as inner forms on the page forms.
    if (form == getDesktop().getPageDetailForm()) {
      return false;
    }

    //If the page selection is running queue the opening of the form to make sure the page form is opened first.
    if (isPageSelectionRunning()) {
      if (!(form instanceof PageForm)) {
        if (!m_blockedForms.contains(form)) {
          m_blockedForms.add(form);
        }
        return false;
      }
    }

    return true;
  }

  private void showBlockedForms() {
    for (IForm form : m_blockedForms.toArray(new IForm[m_blockedForms.size()])) {
      m_blockedForms.remove(form);
      MobileDesktopUtility.addFormToDesktop(form);
    }
  }

  private void handlePageFormAdded(PageForm pageForm) throws ProcessingException {
    if (pageForm == null) {
      return;
    }

    String displayViewId = pageForm.getDisplayViewId();
    MobileDesktopUtility.removeFormsFromDesktop(IPageForm.class, displayViewId, pageForm);
    if (getLeftPageSlotViewId().equals(displayViewId)) {
      MobileDesktopUtility.removeFormsFromDesktop(IPageForm.class, getMiddlePageSlotViewId(), pageForm);
    }
    pageForm.formAddedNotify();
  }

  private void handlePageFormRemoved(PageForm pageForm) throws ProcessingException {
    if (pageForm == null) {
      return;
    }

    pageForm.formRemovedNotify();
  }

  private class P_OutlineTreeListener extends TreeAdapter {

    @Override
    public void treeChanged(TreeEvent event) {
      try {
        switch (event.getType()) {
          case TreeEvent.TYPE_NODES_SELECTED: {
            handleTreeNodeSelected(event.getDeselectedNode(), event.getNewSelectedNode());
            break;
          }
          case TreeEvent.TYPE_NODES_DELETED: {
            handleTreeNodesDeleted(event.getNodes());
            break;
          }
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent event) {
      try {
        switch (event.getType()) {
          case DesktopEvent.TYPE_FORM_ADDED: {
            handleFormAdded(event);
            break;
          }
          case DesktopEvent.TYPE_FORM_REMOVED: {
            handleFormRemoved(event);
            break;
          }
          case DesktopEvent.TYPE_DESKTOP_CLOSED: {
            destroy();
            break;
          }
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

    private void handleFormAdded(DesktopEvent event) throws ProcessingException {
      IForm form = event.getForm();
      if (form instanceof PageForm) {
        handlePageFormAdded((PageForm) form);
      }
    }

    private void handleFormRemoved(DesktopEvent event) throws ProcessingException {
      IForm form = event.getForm();
      if (form instanceof PageForm) {
        handlePageFormRemoved((PageForm) form);
      }
    }

  }

}
