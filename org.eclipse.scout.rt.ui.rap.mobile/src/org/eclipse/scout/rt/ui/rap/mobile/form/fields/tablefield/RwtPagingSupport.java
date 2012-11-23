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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractPagingSupport;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.IMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.PageChangeTableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;

/**
 * @since 3.9.0
 */
public class RwtPagingSupport extends AbstractPagingSupport {

  private RwtScoutList m_uiList;
  private P_TableRowSelectionFilter m_tableRowSelectionFilter;
  private P_SelectionListener m_selectionListener;

  public RwtPagingSupport(RwtScoutList uiList, IMobileTable table) {
    super(table);
    m_uiList = uiList;

    m_selectionListener = new P_SelectionListener();
    m_uiList.getUiTableViewer().addSelectionChangedListener(m_selectionListener);
    m_tableRowSelectionFilter = new P_TableRowSelectionFilter();
    m_uiList.addTableRowSelectionFilter(m_tableRowSelectionFilter);
  }

  public void dispose() {
    m_uiList.removeTableRowSelectionFilter(m_tableRowSelectionFilter);
    m_uiList.getUiTableViewer().removeSelectionChangedListener(m_selectionListener);
  }

  public boolean handleSelection(ITableRow[] rows) {
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

  /**
   * Refreshes the viewer. Also restores the model selection.
   * <p>
   * Changing a page does not clear the selection state in the model. This makes it possible to keep the selection when
   * just changing pages.
   */
  private void refreshAfterPageChange() {
    m_uiList.clearSelection(false);
    m_uiList.getUiTableViewer().refresh();
    m_uiList.restoreSelection();
  }

  private void setPageIndexFromUi(final int pageIndex) {
    m_uiList.getUiEnvironment().invokeScoutLater(new Runnable() {

      @Override
      public void run() {
        getTable().getUIFacade().setPageIndexFromUi(pageIndex);
      }

    }, 0);
  }

  private class P_SelectionListener implements ISelectionChangedListener {

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      StructuredSelection selection = (StructuredSelection) event.getSelection();
      ITableRow[] selectedRows = RwtUtility.getItemsOfSelection(ITableRow.class, selection);
      handleSelection(selectedRows);
    }
  }

  private class P_TableRowSelectionFilter implements ITableRowFilter {
    @Override
    public boolean accept(ITableRow row) {
      if (row instanceof PageChangeTableRow) {
        return false;
      }

      return true;
    }

  }

}
