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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.mobile.ui.desktop.ActiveOutlineObserver;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable.AutoTableForm;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class OutlineFormsManager {
  private Map<IPage, OutlinePreviewForm> m_pageToPreviewFormMap;
  private IOutlineTableForm m_currentOutlineTableForm;
  private IForm m_currentPreviewForm;
  private IOutline m_currentPreviewOutline;

  private P_OutlinePropertyChangeListener m_outlinePropertyChangeListener;
  private ActiveOutlineObserver m_activeOutlineObserver;
  private P_PageTableListener m_pageTableListener;

  private boolean m_previewRowSelectionKeepingEnabled;
  private boolean m_nodePageSwitchEnabled;
  private boolean m_tableStatusVisible;
  private IDesktop m_desktop;

  public OutlineFormsManager() {
    this(null);
  }

  public OutlineFormsManager(IDesktop desktop) {
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create OutlineFormsMediator.");
    }

    m_pageToPreviewFormMap = new HashMap<IPage, OutlinePreviewForm>();
    m_activeOutlineObserver = new ActiveOutlineObserver(desktop);
    m_outlinePropertyChangeListener = new P_OutlinePropertyChangeListener();
    m_activeOutlineObserver.addOutlinePropertyChangeListener(m_outlinePropertyChangeListener);
  }

  public void setPreviewRowSelectionKeepingEnabled(boolean previewRowSelectionKeepingEnabled) {
    m_previewRowSelectionKeepingEnabled = previewRowSelectionKeepingEnabled;
  }

  /**
   * If enabled, the selected row which shows a preview form keeps the selection. Otherwise it gets cleared after
   * selecting.
   */
  public boolean isPreviewRowSelectionKeepingEnabled() {
    return m_previewRowSelectionKeepingEnabled;
  }

  public void setTableStatusVisible(boolean tableStatusVisible) {
    m_tableStatusVisible = tableStatusVisible;
  }

  public boolean isTableStatusVisible() {
    return m_tableStatusVisible;
  }

  public void setNodePageSwitchEnabled(boolean nodePageSwitchEnabled) {
    m_nodePageSwitchEnabled = nodePageSwitchEnabled;
  }

  public boolean isNodePageSwitchEnabled() {
    return m_nodePageSwitchEnabled;
  }

  private void destroy() {
    //FIXME CGU destroying??
  }

  public void installOutlineTableForm() throws ProcessingException {
    if (getDesktop().getOutline() == null || getDesktop().getOutline().getActivePage() == null) {
      return;
    }

    ITable pageTable = getDesktop().getOutline().getDetailTable();
    IForm pageDetailForm = getDesktop().getOutline().getDetailForm();
    showOutlineTableForm(pageTable, pageDetailForm);

    selectPageTableRowIfNecessary(pageTable);
  }

  private void showOutlineTableForm(ITable pageTable, IForm pageDetailForm) throws ProcessingException {
    if (m_currentOutlineTableForm != null) {
      m_currentOutlineTableForm.doClose();
      m_currentOutlineTableForm = null;
    }

    List<IButton> mainButtons = null;
    if (pageDetailForm == null) {
      //If there is a detailform the main buttons are already placed on that form.
      mainButtons = fetchAndConvertNodeActionsFromActivePage();
    }
    MobileOutlineTableForm tableForm = new MobileOutlineTableForm(mainButtons);
    tableForm.setCurrentTable(pageTable);
    tableForm.getOutlineTableField().setTableStatusVisible(isTableStatusVisible());
    tableForm.startAndLinkWithDesktop();

    m_currentOutlineTableForm = tableForm;
  }

  private void selectPageTableRowIfNecessary(ITable pageDetailTable) throws ProcessingException {
    if (pageDetailTable == null || pageDetailTable.getRowCount() == 0 || !isPreviewRowSelectionKeepingEnabled()) {
      return;
    }

    IPage activePage = getDesktop().getOutline().getActivePage();
    IPage pageToSelect = MobileDesktopUtility.getPageFor(activePage, pageDetailTable.getRow(0));
    if (pageDetailTable.getSelectedRow() == null) {
      if (!isDrillDownPage(pageToSelect)) {
        pageDetailTable.selectFirstRow();
      }
    }
    else {
      handleTableRowSelected(pageDetailTable.getSelectedRow());
    }
  }

  private List<IButton> fetchAndConvertNodeActionsFromActivePage() {
    IMenu[] treeNodeActions = fetchNodeActionsFromActivePage();
    return ActionButtonBarUtility.convertActionsToMainButtons(treeNodeActions);
  }

  private void handleTableRowSelected(final ITableRow tableRow) throws ProcessingException {
    if (tableRow == null) {
      if (isPreviewRowSelectionKeepingEnabled()) {
        setCurrentOutlinePreviewForm(null);
      }
      return;
    }

    IOutline outline = getDesktop().getOutline();
    IPage activePage = outline.getActivePage();
    IPage tableRowPage = MobileDesktopUtility.getPageFor(activePage, tableRow);
    if (tableRowPage == null) {
      OutlinePreviewLeafPage autoPage = new OutlinePreviewLeafPage(tableRow);
      activePage.getTree().addChildNode(activePage, autoPage);
      tableRowPage = autoPage;
    }

    if (isDrillDownPage(tableRowPage)) {
      tableRowPage.getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(tableRowPage);
      MobileDesktopUtility.clearTableSelection(tableRow.getTable());
    }
    else {
      showOutlinePreviewForm(tableRowPage);
    }

    if (!isPreviewRowSelectionKeepingEnabled()) {
      MobileDesktopUtility.clearTableSelection(tableRow.getTable());
    }
  }

  private boolean isDrillDownPage(IPage page) {
    return page instanceof IPageWithTable && page.getParentNode() instanceof IPageWithNodes;
  }

  private void showOutlinePreviewForm(IPage previewPage) throws ProcessingException {
    OutlinePreviewForm previewForm = m_pageToPreviewFormMap.get(previewPage);
    if (previewForm == null) {
      previewForm = new OutlinePreviewForm(previewPage);
      previewForm.setNodePageSwitchEnabled(isNodePageSwitchEnabled());
      previewForm.setAutoAddRemoveOnDesktop(false);
      previewForm.start();

      m_pageToPreviewFormMap.put(previewPage, previewForm);
    }

    setCurrentOutlinePreviewForm(previewForm);
  }

  private void setCurrentOutlinePreviewForm(OutlinePreviewForm previewForm) {
    if (m_currentPreviewForm != null) {
      getDesktop().removeForm(m_currentPreviewForm);
    }
    if (previewForm != null) {
      getDesktop().addForm(previewForm);
    }

    m_currentPreviewForm = previewForm;
  }

  private IDesktop getDesktop() {
    return m_desktop;
  }

  private IMenu[] fetchNodeActionsFromActivePage() {
    if (m_currentPreviewOutline == null && (getDesktop().getOutline() == null || getDesktop().getOutline().getActivePage() == null)) {
      return new IMenu[0];
    }

    if (m_currentPreviewOutline != null) {
      return m_currentPreviewOutline.getUIFacade().fireNodePopupFromUI();
    }
    else {
      return getDesktop().getOutline().getUIFacade().fireNodePopupFromUI();
    }
  }

  public void adaptPageDetailFormHeaderActions(IForm form, List<IMenu> menuList) {
    if (form instanceof AutoTableForm) {
      //the AutoTableForm already has the correct actions
      return;
    }

    IMenu[] nodeActions = fetchNodeActionsFromActivePage();
    List<IMenu> nodeActionList = new LinkedList<IMenu>();

    //Remove separators
    for (IMenu action : nodeActions) {
      if (!action.isSeparator()) {
        nodeActionList.add(action);
      }
    }

    if (!menuList.isEmpty() && !nodeActionList.isEmpty()) {
      //Separate detailform actions and tree node actions
      menuList.add(0, new MenuSeparator());
    }
    menuList.addAll(0, nodeActionList);
  }

  private class P_OutlinePropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (event.getPropertyName().equals(IOutline.PROP_DETAIL_TABLE)) {
        handleDetailTableChanged((ITable) event.getOldValue(), (ITable) event.getNewValue());
      }
    }

    private void handleDetailTableChanged(ITable oldTable, ITable newTable) {
      //Remove current preview form if table has changed
      setCurrentOutlinePreviewForm(null);

      if (oldTable != null) {
        oldTable.removeTableListener(m_pageTableListener);
      }
      if (newTable != null) {
        if (m_pageTableListener == null) {
          m_pageTableListener = new P_PageTableListener();
        }
        newTable.addTableListener(m_pageTableListener);
      }

      try {
        installOutlineTableForm();
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

  }

  private class P_PageTableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent event) {
      switch (event.getType()) {
        case TableEvent.TYPE_ROWS_SELECTED: {
          try {
            handleTableRowSelected(event.getFirstRow());
          }
          catch (ProcessingException e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e);
          }
          break;
        }
      }
    }

  }

}
