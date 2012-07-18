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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
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
  private IOutlineTableForm m_currentOutlineTableForm;
  private IForm m_currentPreviewForm;
  private IOutline m_currentPreviewOutline;

  private P_OutlinePropertyChangeListener m_outlinePropertyChangeListener;
  private ActiveOutlineObserver m_activeOutlineObserver;
  private P_PageTableListener m_pageTableListener;

  private boolean m_rowSelectionOnTableChangeEnabled;
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

    m_activeOutlineObserver = new ActiveOutlineObserver(desktop);
    m_outlinePropertyChangeListener = new P_OutlinePropertyChangeListener();
    m_activeOutlineObserver.addOutlinePropertyChangeListener(m_outlinePropertyChangeListener);
  }

  public boolean isRowSelectionOnTableChangeEnabled() {
    return m_rowSelectionOnTableChangeEnabled;
  }

  public void setRowSelectionOnTableChangeEnabled(boolean rowSelectionOnTableChangeEnabled) {
    m_rowSelectionOnTableChangeEnabled = rowSelectionOnTableChangeEnabled;
  }

  public void setTableStatusVisible(boolean tableStatusVisible) {
    m_tableStatusVisible = tableStatusVisible;
  }

  public boolean isTableStatusVisible() {
    return m_tableStatusVisible;
  }

  private void destroy() {
    //FIXME CGU destroying??
  }

  public void installOutlineTableForm() throws ProcessingException {
    if (getDesktop().getOutline() == null || getDesktop().getOutline().getActivePage() == null) {
      return;
    }

    ITable pageDetailTable = getDesktop().getOutline().getDetailTable();
    IForm pageDetailForm = getDesktop().getOutline().getDetailForm();
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
    tableForm.setCurrentTable(pageDetailTable);
    tableForm.getOutlineTableField().setTableStatusVisible(isTableStatusVisible());
    tableForm.startAndLinkWithDesktop();

    m_currentOutlineTableForm = tableForm;

    if (pageDetailTable != null && pageDetailTable.getRowCount() > 0 && isRowSelectionOnTableChangeEnabled()) {
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
  }

  private List<IButton> fetchAndConvertNodeActionsFromActivePage() {
    IMenu[] treeNodeActions = fetchNodeActionsFromActivePage();
    return ActionButtonBarUtility.convertActionsToMainButtons(treeNodeActions);
  }

  private void handleTableRowSelected(final ITableRow tableRow) throws ProcessingException {
    if (tableRow == null) {
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
      clearSelection(tableRow.getTable());
    }
    else {
      showPreviewForm(tableRowPage);
    }

    if (!isRowSelectionOnTableChangeEnabled()) {
      clearSelection(tableRow.getTable());
    }
  }

  private boolean isDrillDownPage(IPage page) {
    return page instanceof IPageWithTable && page.getParentNode() instanceof IPageWithNodes;
  }

  private void clearSelection(final ITable table) {
    ClientSyncJob job = new ClientSyncJob("Clearing selection", ClientJob.getCurrentSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        table.selectRow(null);
      }
    };
    job.schedule();
  }

  private void showPreviewForm(IPage previewPage) throws ProcessingException {

    OutlinePreviewForm detailFormWrapper = new OutlinePreviewForm(previewPage);
    detailFormWrapper.start();

    //Close existing form after starting the new one to prevent flickering
    if (m_currentPreviewForm != null) {
      m_currentPreviewForm.doClose();
      m_currentPreviewForm = null;
    }

    m_currentPreviewForm = detailFormWrapper;
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
        if (m_currentPreviewForm != null) {
          m_currentPreviewForm.doClose();
          m_currentPreviewForm = null;
        }
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
