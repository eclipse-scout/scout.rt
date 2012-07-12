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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.ui.desktop.ActiveOutlineObserver;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable.AutoTableForm;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable.MainBoxActionButton;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class MobileOutlineTableFormMediator {
  private IOutlineTableForm m_currentOutlineTableForm;
  private IForm m_currentPreviewForm;

  private P_OutlinePropertyChangeListener m_outlinePropertyChangeListener;
  private ActiveOutlineObserver m_activeOutlineObserver;
  private P_PageTableListener m_pageTableListener;

  private boolean m_detailFormEmbeddingEnabled;
  private boolean m_tableStatusVisible;

  public MobileOutlineTableFormMediator() {
    m_activeOutlineObserver = new ActiveOutlineObserver();
    m_outlinePropertyChangeListener = new P_OutlinePropertyChangeListener();
    m_activeOutlineObserver.addOutlinePropertyChangeListener(m_outlinePropertyChangeListener);
  }

  public boolean isDetailFormEmbeddingEnabled() {
    return m_detailFormEmbeddingEnabled;
  }

  public void setDetailFormEmbeddingEnabled(boolean detailFormEmbeddingEnabled) {
    m_detailFormEmbeddingEnabled = detailFormEmbeddingEnabled;
  }

  public void setTableStatusVisible(boolean tableStatusVisible) {
    m_tableStatusVisible = tableStatusVisible;
  }

  public boolean isTableStatusVisible() {
    return m_tableStatusVisible;
  }

  public void installOutlineTableForm() throws ProcessingException {
    IDesktop desktop = m_activeOutlineObserver.getDesktop();
    if (desktop.getOutline() == null || desktop.getOutline().getActivePage() == null) {
      return;
    }

    ITable pageDetailTable = desktop.getOutline().getDetailTable();
    IForm pageDetailForm = desktop.getOutline().getDetailForm();

    if (pageDetailTable != null && pageDetailForm == null) {
      ITable table = null;
      IPage parentPage = desktop.getOutline().getActivePage().getParentPage();
      if (parentPage instanceof IPageWithTable) {
        table = ((IPageWithTable) parentPage).getTable();
      }
      if (table != null) {
        pageDetailForm = createStartAutoDetailForm(table);
        desktop.getOutline().getActivePage().setDetailForm(pageDetailForm);
      }
    }

    if (m_currentOutlineTableForm != null) {
      m_currentOutlineTableForm.doClose();
      m_currentOutlineTableForm = null;
    }
    if (isDetailFormEmbeddingEnabled() && pageDetailForm != null) {
      MobileOutlineTableWithDetailForm tableForm = new MobileOutlineTableWithDetailForm(getMainButtons(pageDetailTable));
      tableForm.setDetailForm(pageDetailForm);
      tableForm.setCurrentTable(pageDetailTable);
      tableForm.getOutlineTableField().setTableStatusVisible(isTableStatusVisible());
      tableForm.startAndLinkWithDesktop();

      m_currentOutlineTableForm = tableForm;
    }
    else {
      List<IButton> mainButtons = null;
      if (pageDetailForm == null) {
        //If there is a detailform the main buttons are already placed on that form.
        mainButtons = getMainButtons(pageDetailTable);
      }
      MobileOutlineTableForm tableForm = new MobileOutlineTableForm(mainButtons);
      tableForm.setCurrentTable(pageDetailTable);
      tableForm.getOutlineTableField().setTableStatusVisible(isTableStatusVisible());
      tableForm.startAndLinkWithDesktop();

      m_currentOutlineTableForm = tableForm;
    }
  }

  private List<IButton> getMainButtons(ITable table) {
    List<IButton> buttons = new LinkedList<IButton>();
    IDesktop desktop = m_activeOutlineObserver.getDesktop();
    if (desktop.getOutline() == null || desktop.getOutline().getActivePage() == null) {
      return buttons;
    }

    IMenu[] actions = desktop.getOutline().getActivePage().getTree().getUIFacade().fireNodePopupFromUI();
    for (IMenu action : actions) {
      if (!action.isSeparator()) {
        MainBoxActionButton button = new MainBoxActionButton(action);
        buttons.add(button);
      }
    }
    return buttons;
  }

  private IForm createStartAutoDetailForm(ITable table) throws ProcessingException {
    if (table == null) {
      return null;
    }

    AutoTableForm autoCreatedDetailForm = new AutoTableForm(table.getSelectedRow());
    if (isDetailFormEmbeddingEnabled()) {
      //AutoTableForm must not be added to desktop since it will be embedded
      autoCreatedDetailForm.setAutoAddRemoveOnDesktop(false);

      if (m_currentPreviewForm != null) {
        m_currentPreviewForm.doClose();
      }
      IButton[] customProcessButtons = autoCreatedDetailForm.getRootGroupBox().getCustomProcessButtons();
      MobileOutlineTableWithDetailForm tableForm = new MobileOutlineTableWithDetailForm(Arrays.asList(customProcessButtons));
      tableForm.setDetailForm(autoCreatedDetailForm);
      tableForm.setCurrentTable(null);
      tableForm.getOutlineTableField().setTableStatusVisible(isTableStatusVisible());
      tableForm.start();

      m_currentPreviewForm = tableForm;
    }
    else {
      //Preview form is used if autoTableForm is not embedded but started separately (used for tablets)
      if (m_currentPreviewForm != null) {
        m_currentPreviewForm.doClose();
        m_currentPreviewForm = null;
      }
      m_currentPreviewForm = autoCreatedDetailForm;
    }

    autoCreatedDetailForm.start();

    return autoCreatedDetailForm;
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
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROW_CLICK: {
          handleRowClick(e);
          break;
        }
      }
    }

    private void handleRowClick(TableEvent event) {
      try {
        createStartAutoDetailForm(event.getTable());
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }
}
