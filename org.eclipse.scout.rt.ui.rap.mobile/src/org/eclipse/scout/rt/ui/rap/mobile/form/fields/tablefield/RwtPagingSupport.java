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
  private P_PropertyChangeListener m_propertyChangeListener;
  private boolean m_propertyLoadLock;

  public RwtPagingSupport(RwtScoutList uiList, IMobileTable table) {
    super(table);
    m_uiList = uiList;

    m_selectionListener = new P_SelectionListener();
    m_uiList.getUiTableViewer().addSelectionChangedListener(m_selectionListener);
    m_tableRowSelectionFilter = new P_TableRowSelectionFilter();
    m_uiList.addTableRowSelectionFilter(m_tableRowSelectionFilter);
    m_propertyChangeListener = new P_PropertyChangeListener();
    table.addPropertyChangeListener(m_propertyChangeListener);
  }

  public void dispose() {
    m_uiList.removeTableRowSelectionFilter(m_tableRowSelectionFilter);
    m_uiList.getUiTableViewer().removeSelectionChangedListener(m_selectionListener);
    getTable().removePropertyChangeListener(m_propertyChangeListener);
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

  @Override
  protected void initProperties() {
    if (m_propertyLoadLock) {
      return;
    }
    else {
      super.initProperties();
    }
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
    try {
      m_uiList.clearSelection(false);
      refresh();
      m_uiList.restoreSelection();
    }
    finally {
      m_propertyLoadLock = false;
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
