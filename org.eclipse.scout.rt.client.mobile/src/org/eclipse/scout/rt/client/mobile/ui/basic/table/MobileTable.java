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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.PropertyBucket;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableUIFacade;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * A table optimized for mobile devices which wraps another table.
 * <p>
 * It consists of a content column which displays the relevant information of the original table.
 * 
 * @since 3.9.0
 */
public class MobileTable extends AbstractMobileTable implements IMobileTable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileTable.class);
  private static final int ROW_HEIGHT = 18;
  private int m_maxCellDetailColumns;
  private OptimisticLock m_selectionLock;

  private TablePropertyDelegator<? extends ITable> m_propertyDelegator;

  private P_TableEventListener m_tableListener;

  public MobileTable(ITable originalTable) {
    super(false);
    Set<String> filter = new HashSet<String>();
    filter.add(ITable.PROP_AUTO_RESIZE_COLUMNS);
    filter.add(ITable.PROP_ROW_HEIGHT_HINT);
    filter.add(ITable.PROP_DEFAULT_ICON);
    filter.add(ITable.PROP_HEADER_VISIBLE);
    if (originalTable instanceof IMobileTable) {
      m_propertyDelegator = new MobileTablePropertyDelegator((IMobileTable) originalTable, this, filter);
    }
    else {
      m_propertyDelegator = new TablePropertyDelegator<ITable>(originalTable, this, filter);
    }
    callInitializer();

    try {
      m_selectionLock = new OptimisticLock();
      m_tableListener = new P_TableEventListener();

      m_tableListener.initalizeWith(originalTable);
      getOriginalTable().addTableListener(m_tableListener);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
    setAutoDiscardOnDelete(false);
    setAutoResizeColumns(true);
    setDefaultIconId(null);
    setHeaderVisible(false);

    m_maxCellDetailColumns = 2;
    if (getOriginalTable().getRowHeightHint() == -1) {
      //+1 stands for the cell header row
      setRowHeightHint((m_maxCellDetailColumns + 1) * ROW_HEIGHT);
    }

    getContentColumn().setDefaultDrillDownStyle(execComputeDrillDownStyle());
    setDrillDownStyleMap(getDrillDownStyleMap(getOriginalTable()));
  }

  @Override
  protected boolean execIsAutoCreateTableRowForm() {
    Boolean autoCreateRowForm = isAutoCreateRowForm(getOriginalTable());
    if (autoCreateRowForm != null) {
      return autoCreateRowForm;
    }

    if (getOriginalTable().isCheckable()) {
      return false;
    }

    return true;
  }

  protected String execComputeDrillDownStyle() {
    if (getOriginalTable().isCheckable()) {
      return IRowSummaryColumn.DRILL_DOWN_STYLE_NONE;
    }

    return IRowSummaryColumn.DRILL_DOWN_STYLE_ICON;
  }

  public void dispose() {
    if (m_tableListener == null) {
      return;
    }

    getOriginalTable().removeTableListener(m_tableListener);
    m_tableListener = null;
  }

  public ITable getOriginalTable() {
    return m_propertyDelegator.getSender();
  }

  @Override
  public String getDrillDownStyle(ITableRow tableRow) {
    String drillDownStyle = super.getDrillDownStyle(tableRow);
    if (drillDownStyle == null) {
      drillDownStyle = getContentColumn().getDefaultDrillDownStyle();
    }

    return drillDownStyle;
  }

  @Override
  protected boolean isClearingSelectionNecessary() {
    if (getSelectedRow() == null) {
      return false;
    }

    ITableRow originalRow = getRowMapColumn().getValue(getSelectedRow());
    String drillDownStyle = getDrillDownStyle(originalRow);
    return IRowSummaryColumn.DRILL_DOWN_STYLE_ICON.equals(drillDownStyle);
  }

  @Override
  protected void execRowsSelected(ITableRow[] rows) throws ProcessingException {
    try {
      if (!m_selectionLock.acquire()) {
        //Prevent loop which could happen because delegation of selection is done from this to original table and vice versa
        return;
      }

      if (rows == null || rows.length == 0) {
        getOriginalTable().getUIFacade().setSelectedRowsFromUI(rows);
        return;
      }

      ITableRow originalRow = getRowMapColumn().getValue(rows[0]);
      // TODO CGU: Attention: Drill Down style may not be accurate at this time. 
      // This may happen if the events are executed as batch and another listener sets the style on a rows inserted event (see PageForm)
      // That's why there is a double check in clearSelection
      if (IRowSummaryColumn.DRILL_DOWN_STYLE_ICON.equals(getDrillDownStyle(originalRow))) {
        if (isAutoCreateTableRowForm()) {
          startTableRowForm(originalRow);
        }
        else {
          getOriginalTable().getUIFacade().setSelectedRowsFromUI(getRowMapColumn().getValues(rows));
        }

        clearSelection();
      }
      else {
        getOriginalTable().getUIFacade().setSelectedRowsFromUI(getRowMapColumn().getValues(rows));
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  @Override
  protected void execRowClick(ITableRow row) throws ProcessingException {
    ITableRow originalRow = getRowMapColumn().getValue(row);
    if (IRowSummaryColumn.DRILL_DOWN_STYLE_ICON.equals(getDrillDownStyle(originalRow))) {
      //nop
    }
    else {
      //Drill down actions should be handled by listening to selection and not click events so click events are not fired in such a case
      getOriginalTable().getUIFacade().fireRowClickFromUI(originalRow);
    }
  }

  @Override
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    if (AbstractRowSummaryColumn.isDrillDownButtonUrl(url, path, local)) {
      execDrillDownButtonAction();
    }
    else {
      getOriginalTable().getUIFacade().fireHyperlinkActionFromUI(getRowMapColumn().getValue(getSelectedRow()), null, url);
    }
  }

  protected void execDrillDownButtonAction() throws ProcessingException {
    // nop. No default implemented yet
  }

  public ContentColumn getContentColumn() {
    return getColumnSet().getColumnByClass(ContentColumn.class);
  }

  public RowMapColumn getRowMapColumn() {
    return getColumnSet().getColumnByClass(RowMapColumn.class);
  }

  @Order(10.0)
  public class RowMapColumn extends AbstractColumn<ITableRow> {

    @Override
    protected boolean getConfiguredDisplayable() {
      return false;
    }

  }

  @Order(20.0)
  public class ContentColumn extends AbstractRowSummaryColumn {

  }

  @Override
  protected ITableUIFacade createUIFacade() {
    return new P_MobileTableUIFacade();
  }

  private void reset() {
    discardAllRows();
  }

  private void handleWrappedTableRowsDeleted(ITableRow[] rows) {
    try {
      setTableChanging(true);
      for (ITableRow deletedRow : rows) {
        ITableRow mobileTableRow = getRowMapColumn().findRow(deletedRow);
        discardRow(mobileTableRow);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  private void handleWrappedTableRowsSelected(ITableRow[] rows) {
    try {
      setTableChanging(true);
      ITableRow[] mappedRows = getRowMapColumn().findRows(rows);
      selectRows(mappedRows);
    }
    finally {
      setTableChanging(false);
    }
  }

  private void handleWrappedTableRowsInserted(ITableRow[] rows) {
    if (!getContentColumn().isInitialized()) {
      getContentColumn().initializeDecorationConfiguration(getOriginalTable(), m_maxCellDetailColumns);
    }

    try {
      setTableChanging(true);
      for (ITableRow insertedRow : rows) {
        try {
          ITableRow row = addRowByArray(new Object[]{insertedRow, "", ""});
          getContentColumn().updateValue(row, insertedRow, getDrillDownStyleMap());
        }
        catch (ProcessingException exception) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(exception);
        }
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  private void handleWrappedTableRowOrderChanged(ITableRow[] rows) {
    ITableRow[] sortedMobileRows = getRowMapColumn().findRows(rows);
    sort(sortedMobileRows);
  }

  private void handleWrappedTableRowsUpdated(ITableRow[] rows) {
    if (getOriginalTable() == null || getOriginalTable().getRowCount() == 0) {
      return;
    }

    try {
      setTableChanging(true);
      try {
        getContentColumn().updateValue(getRowMapColumn().findRows(rows), rows, getDrillDownStyleMap());

        if (isCheckable()) {
          for (ITableRow row : rows) {
            ITableRow mappedRow = getRowMapColumn().findRow(row);
            if (mappedRow != null) {
              checkRow(mappedRow, row.isChecked());
            }
          }
        }
      }
      catch (ProcessingException exception) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(exception);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  private void selectRows() {
    if (getOriginalTable().getSelectedRowCount() == 0) {
      return;
    }

    selectRows(getRowMapColumn().findRows(getOriginalTable().getSelectedRows()));
  }

  private void checkRows() throws ProcessingException {
    if (!isCheckable() || getOriginalTable().getCheckedRows().length == 0) {
      return;
    }

    checkRows(getRowMapColumn().findRows(getOriginalTable().getCheckedRows()), true);
  }

  private class P_TableEventListener extends TableAdapter {

    protected void initalizeWith(ITable originalTable) throws ProcessingException {
      reset();

      if (originalTable.getRows().length > 0) {
        //'fire' a rows inserted event with all rows
        tableChanged(new TableEvent(originalTable, TableEvent.TYPE_ROWS_INSERTED, originalTable.getRows()));
      }

      selectRows();
      checkRows();
    }

    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_SELECTED: {
          handleWrappedTableRowsSelected(e.getRows());
          break;
        }
        case TableEvent.TYPE_ALL_ROWS_DELETED: {
          reset();
          break;
        }
        case TableEvent.TYPE_ROWS_DELETED: {
          handleWrappedTableRowsDeleted(e.getRows());
          break;
        }
        case TableEvent.TYPE_ROWS_INSERTED: {
          handleWrappedTableRowsInserted(e.getRows());
          break;
        }
        case TableEvent.TYPE_ROWS_UPDATED: {
          handleWrappedTableRowsUpdated(e.getRows());
          break;
        }
        case TableEvent.TYPE_ROW_ORDER_CHANGED: {
          handleWrappedTableRowOrderChanged(e.getRows());
          break;
        }
      }
    }

  }

  public static void setAutoCreateRowForm(ITable table, Boolean autoCreateRowForm) {
    PropertyBucket.getInstance().setPropertyBoolean(table, IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM, autoCreateRowForm);
  }

  public static Boolean isAutoCreateRowForm(ITable table) {
    return PropertyBucket.getInstance().getPropertyBoolean(table, IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM);
  }

  public static void setDrillDownStyleMap(ITable table, DrillDownStyleMap drillDownStyles) {
    PropertyBucket.getInstance().setProperty(table, IMobileTable.PROP_DRILL_DOWN_STYLE_MAP, drillDownStyles);
  }

  public static DrillDownStyleMap getDrillDownStyleMap(ITable table) {
    return PropertyBucket.getInstance().getProperty(table, IMobileTable.PROP_DRILL_DOWN_STYLE_MAP);
  }

  /**
   * Used to directly dispatch ui events to the original table or to completely deny certain events
   */
  protected class P_MobileTableUIFacade extends P_TableUIFacade {

    //------------- pass events only to original table -------------
    @Override
    public IMenu[] fireRowPopupFromUI() {
      return getOriginalTable().getUIFacade().fireRowPopupFromUI();
    }

    @Override
    public IMenu[] fireEmptySpacePopupFromUI() {
      return getOriginalTable().getUIFacade().fireEmptySpacePopupFromUI();
    }

    @Override
    public IMenu[] fireHeaderPopupFromUI() {
      return getOriginalTable().getUIFacade().fireHeaderPopupFromUI();
    }

    @Override
    public TransferObject fireRowsDragRequestFromUI() {
      return getOriginalTable().getUIFacade().fireRowsDragRequestFromUI();
    }

    @Override
    public void fireRowDropActionFromUI(ITableRow row, TransferObject dropData) {
      getOriginalTable().getUIFacade().fireRowDropActionFromUI(getRowMapColumn().getValue(row), dropData);
    }

    @Override
    public boolean fireKeyTypedFromUI(String keyStrokeText, char keyChar) {
      return getOriginalTable().getUIFacade().fireKeyTypedFromUI(keyStrokeText, keyChar);
    }

    //------------- do not process events --------------------------
    @Override
    public void fireColumnMovedFromUI(IColumn<?> c, int toViewIndex) {
      //nop; not allowed
    }

    @Override
    public void fireVisibleColumnsChangedFromUI(IColumn<?>[] visibleColumns) {
      //nop; not allowed
    }

    @Override
    public void setColumnWidthFromUI(IColumn<?> c, int newWidth) {
      //nop; not allowed
    }

    @Override
    public IFormField prepareCellEditFromUI(ITableRow row, IColumn<?> col) {
      //nop; not allowed
      return null;
    }

    @Override
    public void completeCellEditFromUI() {
      //nop; not allowed
    }

    @Override
    public void cancelCellEditFromUI() {
      //nop; not allowed
    }
  }
}
