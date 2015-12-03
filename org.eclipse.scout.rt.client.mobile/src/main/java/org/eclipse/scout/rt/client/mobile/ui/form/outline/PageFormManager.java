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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.mobile.ui.desktop.ActiveOutlineObserver;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageFormManager {
  private static final Logger LOG = LoggerFactory.getLogger(PageFormManager.class);

  private PageFormMap m_pageFormMap;
  private ActiveOutlineObserver m_activeOutlineObserver;
  private P_OutlineTreeListener m_outlineTreeListener;
  private P_DesktopListener m_desktopListener;
  private Map<IPage, String> m_selectedPageSlotMap;

  private String m_leftPageSlotViewId;
  private String m_middlePageSlotViewId;
  private IDesktop m_desktop;

  private List<IForm> m_blockedForms;
  private boolean m_pageSelectionRunning;

  public PageFormManager(String... pageSlotViewIds) {
    this(null, pageSlotViewIds);
  }

  public PageFormManager(IDesktop desktop, String... pageSlotViewIds) {
    if (desktop == null) {
      desktop = ClientSessionProvider.currentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create OutlineFormsMediator.");
    }

    initPageSlots(pageSlotViewIds);
    m_pageFormMap = new PageFormMap();
    m_blockedForms = new LinkedList<IForm>();
    m_activeOutlineObserver = new ActiveOutlineObserver(desktop);
    m_selectedPageSlotMap = new HashMap<IPage, String>();

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

  public static boolean isDrillDownPage(IPage<?> page) {
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
    for (IPageForm pageForm : getDesktop().findForms(IPageForm.class)) {
      getDesktop().hideForm(pageForm);
    }
  }

  private void hidePage(IPage<?> page) {
    if (page == null) {
      return;
    }

    LOG.debug("Hiding page: " + page);
    IPageForm pageForm = m_pageFormMap.get(page);
    if (pageForm != null) {
      getDesktop().hideForm(pageForm);
    }
  }

  private IPageForm showPage(IPage<?> page) {
    IPageForm pageForm = getPageForm(page, true);
    if (pageForm != null) {
      return pageForm;
    }
    LOG.debug("Showing page: " + page);

    String displayViewId = getLeftPageSlotViewId();

    if (!hasOnlyOnePageSlot()) {
      displayViewId = computePageFormSlot(page);
    }

    return showPage(page, displayViewId);
  }

  public IPageForm getPageForm(IPage<?> page, boolean onlyShowing) {
    return m_pageFormMap.get(page, onlyShowing);
  }

  public String computePageFormSlot(IPage<?> page) {
    if (page == null) {
      return null;
    }

    if (page instanceof AutoLeafPageWithNodes) {
      return getMiddlePageSlotViewId();
    }

    if (page.getParentNode() == null) {
      return getLeftPageSlotViewId();
    }

    //PageWithTables should be displayed on the left side
    if (isDrillDownPage(page)) {
      return getLeftPageSlotViewId();
    }

    //Special case for nested PageWithTables:
    //The right side can only contain a pageWithTable if the left side also shows a pageWithTable.
    //In that case a selection of the "Details" table row of the right side selects the right pageWithTable again.
    //This pageWithTable should now be displayed on the left side that's why the leftPageSlotViewId is returned.
    String currentViewId = m_selectedPageSlotMap.get(page);
    if (getMiddlePageSlotViewId().equals(currentViewId) && page.getParentPage() instanceof IPageWithTable) {
      return getLeftPageSlotViewId();
    }

    return getMiddlePageSlotViewId();
  }

  private IPageForm showPage(IPage<?> page, String viewId) {
    updateLeftPageIfNecessary(page, viewId);

    IPageForm pageForm = m_pageFormMap.get(viewId, page);
    if (pageForm != null && pageForm.isDirty()) {
      pageForm.doClose();
      m_pageFormMap.remove(pageForm);
      pageForm = null;
    }
    if (pageForm == null) {
      if (getLeftPageSlotViewId().equals(viewId)) {
        pageForm = createMainPageForm(page);
      }
      else {
        pageForm = createPageForm(page);
      }
      pageForm.setShowOnStart(false);
      pageForm.setDisplayViewId(viewId);
      pageForm.start();

      m_pageFormMap.put(pageForm);
    }

    if (!pageForm.isShowing()) {
      getDesktop().showForm(pageForm);
    }

    return pageForm;
  }

  /**
   * If a page gets shown on the right side it's necessary to make sure the left side displays the correct page, which
   * is the parent page.
   * <p>
   * Normally, on regular drill down, the left page gets shown first and afterwards the right one. In this situation
   * everything is fine and the method does nothing.<br>
   * The left page may be wrong if the right page gets shown first, which may happen in case of bookmark activation or a
   * node page switch. In such situations the left side needs to be updated with the correct page.
   * <p>
   * Node page switch: The node page on the right side moves to the left side when selecting a node page, happens on
   * nested PageWithNodes
   */
  private void updateLeftPageIfNecessary(IPage<?> page, String viewId) {
    if (getMiddlePageSlotViewId() == null) {
      return;
    }

    if (getMiddlePageSlotViewId().equals(viewId) && page.getParentPage() != null && !(page instanceof AutoLeafPageWithNodes)) {
      IPageForm parentPageForm = m_pageFormMap.get(page.getParentPage(), true);
      if (parentPageForm == null || getMiddlePageSlotViewId().equals(parentPageForm.getDisplayViewId())) {

        //Make sure there is always the correct page on the left side which is the parent page
        showPage(page.getParentPage(), getLeftPageSlotViewId());
      }
    }
  }

  protected IMainPageForm createMainPageForm(IPage<?> page) {
    PageFormConfig config = createMainPageFormConfig(page);
    return new MainPageForm(page, this, config);
  }

  protected IPageForm createPageForm(IPage<?> page) {
    PageFormConfig config = createPageFormConfig(page);
    return new PageForm(page, this, config);
  }

  protected PageFormConfig createMainPageFormConfig(IPage<?> page) {
    PageFormConfig config = new PageFormConfig();
    config.setTablePageAllowed(true);
    config.setTableStatusVisible(true);
    if (hasOnlyOnePageSlot()) {
      config.setDetailFormVisible(true);
    }
    else {
      config.setKeepSelection(true);
      config.setAutoSelectFirstChildPage(true);
    }
    return config;
  }

  protected PageFormConfig createPageFormConfig(IPage<?> page) {
    PageFormConfig config = new PageFormConfig();
    config.setDetailFormVisible(true);
    return config;
  }

  private IDesktop getDesktop() {
    return m_desktop;
  }

  public void pageSelectedNotify(PageForm pageForm, IPage<?> selectedPage) {
    if (selectedPage == null) {
      return;
    }

    //A AutoLeafPage is not attached to a real outline. Since it already has been activated just show it.
    if (selectedPage instanceof AutoLeafPageWithNodes) {
      showPage(selectedPage);
    }
    else {
      if (selectedPage.isSelectedNode()) {
        //Trigger selection again to move it to the left side and to make sure it's treated like a main page (see MultiPageChangeStrategy)
        selectedPage.getOutline().selectNode(null);
      }
      m_selectedPageSlotMap.put(selectedPage, pageForm.getDisplayViewId());
      try {
        selectAndExpandPage(selectedPage);
      }
      finally {
        m_selectedPageSlotMap.remove(selectedPage);
      }
    }
  }

  /**
   * Mainly a copy from AbstractTree.getUIFacade.setNodeSelectedAndExpandedFromUI() without setTreeChanging(true/false)
   * and additional outline change. TreeChanging must not be set otherwise collecting node menus does not work anymore.
   */
  private void selectAndExpandPage(IPage<?> page) {
    try {
      setPageSelectionRunning(true);

      //Make sure outline is correctly set
      IOutline outline = page.getOutline();
      if (getDesktop().getOutline() != page.getOutline()) {
        getDesktop().activateOutline(page.getOutline());
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

  private void handleTreeNodeSelected(final ITreeNode deselctedNode, final ITreeNode selectedNode) {
    LOG.debug("Tree node selected: " + selectedNode);
    if (selectedNode == null) {
      hidePageForms();
      return;
    }

    IPageForm pageForm = showPage((IPage) selectedNode);
    pageForm.pageSelectedNotify();
  }

  private void handleTreeNodesDeleted(Collection<ITreeNode> deletedNodes) {
    if (deletedNodes == null) {
      return;
    }

    for (ITreeNode node : deletedNodes) {

      //If a node gets deleted the child nodes typically get detached from the tree too, but no separate event will be fired.
      //So we need to make sure the pageForms of the child pages are properly removed as well.
      handleTreeNodesDeleted(node.getChildNodes());

      if (node instanceof IPage<?> && node.getTree() == null) {
        IPage<?> page = (IPage) node;
        handlePageRemoved(page);
      }
    }
  }

  public void pageRemovedNotify(PageForm pageForm, IPage<?> page) {
    handlePageRemoved(page);
  }

  private void handlePageRemoved(IPage<?> page) {
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
      IPage<?> parentPage = ((AutoLeafPageWithNodes) page).getActualParentPage();
      if (parentPage.isSelectedNode()) {
        showPage(parentPage);
      }
    }
  }

  public boolean acceptForm(IForm form) {
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

  private void handlePageFormAdded(PageForm pageForm) {
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

  private void handlePageFormRemoved(PageForm pageForm) {
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
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

  }

  private class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent event) {
      try {
        switch (event.getType()) {
          case DesktopEvent.TYPE_FORM_SHOW: {
            handleFormAdded(event);
            break;
          }
          case DesktopEvent.TYPE_FORM_HIDE: {
            handleFormRemoved(event);
            break;
          }
          case DesktopEvent.TYPE_DESKTOP_CLOSED: {
            destroy();
            break;
          }
        }
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    private void handleFormAdded(DesktopEvent event) {
      IForm form = event.getForm();
      if (form instanceof PageForm) {
        handlePageFormAdded((PageForm) form);
      }
    }

    private void handleFormRemoved(DesktopEvent event) {
      IForm form = event.getForm();
      if (form instanceof PageForm) {
        handlePageFormRemoved((PageForm) form);
      }
    }

  }

}
