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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractPagingSupport;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.IMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.PagingTableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.9.0
 */
public class RwtPagingSupport extends AbstractPagingSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtPagingSupport.class);

  private RwtScoutList m_uiList;
  private P_TableRowSelectionFilter m_tableRowSelectionFilter;
  private P_SelectionListener m_selectionListener;
  private P_PropertyChangeListener m_propertyChangeListener;
  private boolean m_propertyLoadLock;

  public RwtPagingSupport(RwtScoutList uiList, IMobileTable table) {
    super(table);
    m_uiList = uiList;

    m_selectionListener = new P_SelectionListener();
    m_uiList.getUiField().addListener(SWT.MouseUp, m_selectionListener);
    m_tableRowSelectionFilter = new P_TableRowSelectionFilter();
    m_uiList.addTableRowSelectionFilter(m_tableRowSelectionFilter);
    m_propertyChangeListener = new P_PropertyChangeListener();
    table.addPropertyChangeListener(m_propertyChangeListener);
  }

  public void dispose() {
    m_uiList.removeTableRowSelectionFilter(m_tableRowSelectionFilter);
    m_uiList.getUiField().removeListener(SWT.MouseUp, m_selectionListener);
    getTable().removePropertyChangeListener(m_propertyChangeListener);
  }

  public boolean handleSelection(List<? extends ITableRow> rows) {
    for (ITableRow selectedRow : rows) {
      if (selectedRow == getPreviousElementsTableRow()) {
        setPageIndex(getPageIndex() - 1);
        setPageIndexFromUi(getPageIndex());

        refreshAfterPageChange();

        //Scroll to bottom
        m_uiList.getUiField().setTopIndex(m_uiList.getUiField().getItemCount() - 1);
        return true;
      }
      else if (selectedRow == getNextElementsTableRow()) {
        setPageIndex(getPageIndex() + 1);
        setPageIndexFromUi(getPageIndex());

        refreshAfterPageChange();

        //Scroll to top
        m_uiList.getUiField().setTopIndex(0);
        return true;
      }
    }

    return false;
  }

  @Override
  protected void initProperties() {
    if (m_propertyLoadLock) {
      return;
    }
    else {
      super.initProperties();
    }
  }

  @Override
  protected ITableRow createNextElementsTableRow() {
    final Holder<ITableRow> resultHolder = new Holder<ITableRow>(ITableRow.class);

    JobEx job = m_uiList.getUiEnvironment().invokeScoutLater(new Runnable() {

      @Override
      public void run() {
        resultHolder.setValue(RwtPagingSupport.super.createNextElementsTableRow());
      }

    }, 0);
    try {
      job.join(3000);
    }
    catch (InterruptedException e) {
      LOG.error("Failed to create NextPagingTableRow", e);
    }

    return resultHolder.getValue();
  }

  @Override
  protected ITableRow createPreviousElementsTableRow() {
    final Holder<ITableRow> resultHolder = new Holder<ITableRow>(ITableRow.class);

    JobEx job = m_uiList.getUiEnvironment().invokeScoutLater(new Runnable() {

      @Override
      public void run() {
        resultHolder.setValue(RwtPagingSupport.super.createPreviousElementsTableRow());
      }

    }, 0);
    try {
      job.join(3000);
    }
    catch (InterruptedException e) {
      LOG.error("Failed to create PreviousPagingTableRow", e);
    }

    return resultHolder.getValue();
  }

  /**
   * Refreshes the viewer. Also restores the model selection.
   * <p>
   * Changing a page does not clear the selection state in the model. This makes it possible to keep the selection when
   * just changing pages.
   */
  private void refreshAfterPageChange() {
    //Since setPageIndexFromUi is async, it's not sure when it's really written in the model -> lock during refresh
    m_propertyLoadLock = true;
    m_uiList.setPreventSelectionHandling(true);
    try {
      refresh();
      m_uiList.clearSelection();
      m_uiList.restoreSelection();
    }
    finally {
      m_propertyLoadLock = false;
      m_uiList.setPreventSelectionHandling(false);
    }
  }

  private void refresh() {
    m_uiList.getUiTableViewer().refresh();
  }

  private void setPageIndexFromUi(final int pageIndex) {
    m_uiList.getUiEnvironment().invokeScoutLater(new Runnable() {

      @Override
      public void run() {
        getTable().getUIFacade().setPageIndexFromUi(pageIndex);
      }

    }, 0);
  }

  private class P_SelectionListener implements Listener {

    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (event.type == SWT.MouseUp) {
        StructuredSelection selection = (StructuredSelection) m_uiList.getUiTableViewer().getSelection();
        List<ITableRow> selectedRows = RwtUtility.getItemsOfSelection(ITableRow.class, selection);
        if (handleSelection(selectedRows)) {
          event.doit = false;
        }
      }
    }

  }

  private class P_TableRowSelectionFilter implements ITableRowFilter {
    @Override
    public boolean accept(ITableRow row) {
      if (row instanceof PagingTableRow) {
        return false;
      }

      return true;
    }

  }

  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IMobileTable.PROP_PAGE_INDEX.equals(name)) {
      if (getPageIndex() != (Integer) newValue) {
        refresh();
      }
    }
    else if (IMobileTable.PROP_PAGE_SIZE.equals(name)) {
      if (getPageSize() != (Integer) newValue) {
        refresh();
      }
    }
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      m_uiList.getUiEnvironment().invokeUiLater(new Runnable() {

        @Override
        public void run() {
          handleScoutPropertyChange(evt.getPropertyName(), evt.getNewValue());
        }
      });
    }

  }

}
