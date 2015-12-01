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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.columns.AbstractRowSummaryColumn;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.columns.IRowSummaryColumn;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableUIFacade;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;

/**
 * A table optimized for mobile devices which wraps another table.
 * <p>
 * It consists of a content column which displays the relevant information of the original table.
 *
 * @since 3.9.0
 */
public class MobileTable extends AbstractMobileTable implements IMobileTable {
  private static final int ROW_HEIGHT = 18;
  private int m_maxCellDetailColumns;
  private OptimisticLock m_selectionLock;
  private MobileTablePropertyDelegator m_propertyDelegator;
  private P_TableEventListener m_tableListener;

  public MobileTable(ITable originalTable) {
    super(false);
    Set<String> filter = new HashSet<String>();
    filter.add(ITable.PROP_AUTO_RESIZE_COLUMNS);
    filter.add(ITable.PROP_ROW_HEIGHT_HINT);
    filter.add(ITable.PROP_DEFAULT_ICON);
    filter.add(ITable.PROP_HEADER_VISIBLE);
    m_propertyDelegator = new MobileTablePropertyDelegator(originalTable, this, filter);
    callInitializer();

    try {
      m_selectionLock = new OptimisticLock();
      m_tableListener = new P_TableEventListener();
      m_tableListener.init();

      //Attach as UI listener to make sure every "business logic" listener comes first
      getOriginalTable().addUITableListener(m_tableListener);
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
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

    getContentColumn().setDefaultDrillDownStyle(getDefaultDrillDownStyle());
  }

  @Override
  protected void execDisposeTable() {
    super.execDisposeTable();
    if (m_tableListener != null) {
      getOriginalTable().removeTableListener(m_tableListener);
      m_tableListener = null;
    }
    m_propertyDelegator.dispose();
  }

  @Override
  protected boolean execIsAutoCreateTableRowForm() {
    if (getOriginalTable().hasProperty(IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM)) {
      return isAutoCreateRowForm(getOriginalTable());
    }

    if (getOriginalTable().isCheckable()) {
      return false;
    }

    return true;
  }

  @Override
  protected String execComputeDefaultDrillDownStyle() {
    if (getOriginalTable().hasProperty(PROP_DEFAULT_DRILL_DOWN_STYLE)) {
      return getDefaultDrillDownStyle(getOriginalTable());
    }

    if (getOriginalTable().isCheckable()) {
      return IRowSummaryColumn.DRILL_DOWN_STYLE_NONE;
    }

    //Check if the original table already has a selection or click behavior implemented. If yes, use the drill down button style to not break the original selection or click behavior.
    if (!(getOriginalTable() instanceof IMobileTable)) {
      if (ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "execRowsSelected", new Class[]{List.class}, getOriginalTable().getClass()) ||
          ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "execRowClick", new Class[]{ITableRow.class}, getOriginalTable().getClass())) {
        return IRowSummaryColumn.DRILL_DOWN_STYLE_BUTTON;
      }
    }

    return IRowSummaryColumn.DRILL_DOWN_STYLE_ICON;
  }

  /**
   * Makes sure page index is not greater than page count
   */
  private void updatePageIndex() {
    if (getPageIndex() >= getPageCount()) {
      setPageIndex(getOriginalTable(), getPageCount() - 1);
    }
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
  protected void execRowsSelected(List<? extends ITableRow> rows) {
    try {
      if (!m_selectionLock.acquire()) {
        //Prevent loop which could happen because delegation of selection is done from this to original table and vice versa
        return;
      }

      //Delegate to original table
      getOriginalTable().getUIFacade().setSelectedRowsFromUI(getRowMapColumn().getValues(rows));

      ITableRow originalRow = null;
      if (CollectionUtility.hasElements(rows)) {
        originalRow = getRowMapColumn().getValue(rows.get(0));
      }
      if (originalRow != null) {
        if (isAutoCreateTableRowForm() && IRowSummaryColumn.DRILL_DOWN_STYLE_ICON.equals(getDrillDownStyle(originalRow))) {
          startTableRowForm(originalRow);
        }
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  @Override
  protected void execRowClick(ITableRow row, MouseButton mouseButton) {
    //Delegate to original table
    ITableRow originalRow = getRowMapColumn().getValue(row);
    getOriginalTable().getUIFacade().fireRowClickFromUI(originalRow, mouseButton);
  }

  @Override
  protected void execAppLinkAction(String ref) {
    //Delegate to original table
    getOriginalTable().getUIFacade().fireAppLinkActionFromUI(ref);

    if (AbstractRowSummaryColumn.isDrillDownButtonUrl(ref)) {
      execDrillDownButtonAction();
    }
  }

  protected void execDrillDownButtonAction() {
    if (isAutoCreateTableRowForm()) {
      ITableRow originalRow = null;
      ITableRow selectedRow = getSelectedRow();
      if (selectedRow != null) {
        originalRow = getRowMapColumn().getValue(selectedRow);
      }
      startTableRowForm(originalRow);
    }
  }

  public ContentColumn getContentColumn() {
    return getColumnSet().getColumnByClass(ContentColumn.class);
  }

  public RowMapColumn getRowMapColumn() {
    return getColumnSet().getColumnByClass(RowMapColumn.class);
  }

  @Order(10)
  public class RowMapColumn extends AbstractColumn<ITableRow> {

    @Override
    protected boolean getConfiguredDisplayable() {
      return false;
    }

  }

  @Order(20)
  public class ContentColumn extends AbstractRowSummaryColumn {

  }

  @Override
  protected ITableUIFacade createUIFacade() {
    return new P_DispatchingMobileTableUIFacade();
  }

  private void reset() {
    discardAllRows();
  }

  private void handleWrappedTableRowsDeleted(List<? extends ITableRow> rows) {
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

  private void handleWrappedTableRowsSelected(List<? extends ITableRow> rows) {
    try {
      setTableChanging(true);
      selectRows(getRowMapColumn().findRows(rows));
    }
    finally {
      setTableChanging(false);
    }
  }

  private void handleWrappedTableRowsInserted(List<? extends ITableRow> rows) {
    try {
      setTableChanging(true);
      insertWrappedTableRows(rows);

      updatePageIndex();
    }
    finally {
      setTableChanging(false);
    }
  }

  private void insertWrappedTableRows(List<? extends ITableRow> rows) {
    if (!getContentColumn().isDecorationConfigurationInitialized()) {
      getContentColumn().initializeDecorationConfiguration(getOriginalTable(), m_maxCellDetailColumns);
    }

    for (ITableRow insertedRow : rows) {
      if (!insertedRow.isFilterAccepted()) {
        continue;
      }

      try {
        ITableRow row = addRowByArray(new Object[]{insertedRow, "", ""});
        getContentColumn().updateValue(row, insertedRow, getDrillDownStyleMap());
      }
      catch (RuntimeException exception) {
        BEANS.get(ExceptionHandler.class).handle(exception);
      }
    }
  }

  private void handleWrappedTableRowOrderChanged(List<? extends ITableRow> rows) {
    sort(getRowMapColumn().findRows(rows));
  }

  private void handleWrappedTableRowsUpdated(List<? extends ITableRow> originalRows) {
    if (getOriginalTable() == null || getOriginalTable().getRowCount() == 0) {
      return;
    }

    try {
      setTableChanging(true);
      for (ITableRow originalRow : originalRows) {
        ITableRow row = getRowMapColumn().findRow(originalRow);
        if (row != null) {
          getContentColumn().updateValue(row, originalRow, getDrillDownStyleMap());
          if (isCheckable()) {
            checkRow(row, originalRow.isChecked());
          }
        }
      }
    }
    catch (RuntimeException exception) {
      BEANS.get(ExceptionHandler.class).handle(exception);
    }
    finally {
      setTableChanging(false);
    }
  }

  private void syncSelectedRows() {
    if (getOriginalTable().getSelectedRowCount() == 0) {
      return;
    }

    selectRows(getRowMapColumn().findRows(getOriginalTable().getSelectedRows()));
  }

  private void syncCheckedRows() {
    if (!isCheckable() || getOriginalTable().getCheckedRows().isEmpty()) {
      return;
    }

    checkRows(getRowMapColumn().findRows(getOriginalTable().getCheckedRows()), true);
  }

  private void syncTableRows() {
    if (getOriginalTable().getRowCount() == 0) {
      return;
    }

    insertWrappedTableRows(getOriginalTable().getRows());
  }

  private class P_TableEventListener extends TableAdapter {

    protected void init() {
      try {
        setTableChanging(true);

        syncTableRows();
        syncSelectedRows();
        syncCheckedRows();
      }
      finally {
        setTableChanging(false);
      }
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

  /**
   * Used to directly dispatch ui events to the original table or to completely deny certain events
   */
  protected class P_DispatchingMobileTableUIFacade extends P_MobileTableUIFacade {

    //------------- pass events only to original table -------------

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

    @Override
    public void setPageIndexFromUi(int pageIndex) {
      getOriginalTable().setProperty(PROP_PAGE_INDEX, pageIndex);
    }

    //------------- do not process events --------------------------
    @Override
    public void fireRowActionFromUI(ITableRow row) {
      //nop; not allowed
    }

    @Override
    public void fireColumnMovedFromUI(IColumn c, int toViewIndex) {
      //nop; not allowed
    }

    @Override
    public void fireVisibleColumnsChangedFromUI(Collection<IColumn<?>> visibleColumns) {
      super.fireVisibleColumnsChangedFromUI(visibleColumns);
    }

    @Override
    public void setColumnWidthFromUI(IColumn c, int newWidth) {
      //nop; not allowed
    }

    @Override
    public IFormField prepareCellEditFromUI(ITableRow row, IColumn col) {
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
