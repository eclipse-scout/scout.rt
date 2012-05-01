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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableUIFacade;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * A table optimized for mobile devices which wraps another table.
 * <p>
 * It consists of a content column which displays the relevant information of the original table. It also provides a
 * drill down button with the ability to drill down an outline.
 * </p>
 * 
 * @since 3.8.0
 */
public class MobileTable extends AbstractTable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileTable.class);

  private ITable m_originalTable;
  private boolean m_useBoldCellHeaderText;
  private int m_maxCellDetailColumns;
  private IColumn m_cellHeaderColumn;
  private List<IColumn> m_cellDetailColumns;
  private String m_headerName;
  private static final int ROW_HEIGHT = 18;

  private final P_TableEventListener m_eventListener;

  public MobileTable() {
    m_eventListener = new P_TableEventListener();
    m_useBoldCellHeaderText = true;
    m_maxCellDetailColumns = 2;
  }

  public void setDrillDownColumnVisible(boolean drillDownColumnVisible) {
    getDrillDownColumn().setVisible(drillDownColumnVisible);
  }

  public boolean isDrillDownColumnVisible() {
    return getDrillDownColumn().isVisible();
  }

  public void installWrappedTable(ITable wrappedTable) throws ProcessingException {
    if (m_originalTable != null) {
      m_originalTable.removeTableListener(m_eventListener);
    }
    m_originalTable = wrappedTable;

    if (wrappedTable == null) {
      return;
    }

    //FIXME CGU move init to constructor, should be done like ButtonWrappingAction. Maybe create AbstractWrappingTable?

    //TODO cgu: calculate + 1 depends on summary column available (loop through columns and check if summary=true)
    if (wrappedTable.getRowHeightHint() != -1) {
      setRowHeightHint(wrappedTable.getRowHeightHint());
    }
    else {
      setRowHeightHint((m_maxCellDetailColumns + 1) * ROW_HEIGHT);
    }
    setHeaderVisible(wrappedTable.isHeaderVisible());
    if (isHeaderVisible()) {
      getContentColumn().decorateHeaderCell();
    }
    setCheckable(wrappedTable.isCheckable());
    setMultiCheck(wrappedTable.isMultiCheck());
    setEnabled(wrappedTable.isEnabled());

    m_eventListener.initalizeWith(wrappedTable);
    m_originalTable.addTableListener(m_eventListener);
  }

  public void dispose() {
    try {
      installWrappedTable(null);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  public String getHeaderName() {
    return m_headerName;
  }

  public void setHeaderName(String headerName) {
    m_headerName = headerName;
  }

  @Override
  protected boolean getConfiguredAutoDiscardOnDelete() {
    return true;
  }

  @Override
  protected boolean getConfiguredAutoResizeColumns() {
    return true;
  }

  @Override
  protected boolean getConfiguredMultilineText() {
    return true;
  }

  public ContentColumn getContentColumn() {
    return getColumnSet().getColumnByClass(ContentColumn.class);
  }

  public RowMapColumn getRowMapColumn() {
    return getColumnSet().getColumnByClass(RowMapColumn.class);
  }

  public DrillDownColumn getDrillDownColumn() {
    return getColumnSet().getColumnByClass(DrillDownColumn.class);
  }

  @Order(10.0)
  public class RowMapColumn extends AbstractColumn<ITableRow> {
    @Override
    protected boolean getConfiguredDisplayable() {
      return false;
    }
  }

  @Order(20.0)
  public class ContentColumn extends AbstractStringColumn {
    @Override
    protected boolean getConfiguredTextWrap() {
      return true;
    }

    @Override
    protected void execDecorateHeaderCell(HeaderCell cell) throws ProcessingException {
      cell.setText(m_headerName);
    }
  }

  @Order(30.0)
  public class DrillDownColumn extends AbstractStringColumn {

    @Override
    protected int getConfiguredWidth() {
      return 15;
    }

    @Override
    protected int getConfiguredHorizontalAlignment() {
      return 0;
    }

    @Override
    protected void execDecorateCell(Cell cell, ITableRow row) throws ProcessingException {
      cell.setIconId(Icons.DrillDown);

      //FIXME CGU try with new rap sources if img works to horizontal align the image. Another approach would be to position the image at the right end of the summary column
//      final StringBuilder result = new StringBuilder();
//      result.append("<div style=\"display: table; height: 100%; width: 100%\">");
//      result.append("<div style=\"display: table-cell; text-align:center; vertical-align: middle;\">");
//
//      result.append("<img src=\"drill_down.png\"/>");
//
//      result.append("</div>");
//      result.append("</div>");
//
//      cell.setText(result.toString());
    }

  }

  @Override
  protected void execRowClick(ITableRow row) throws ProcessingException {
    if (row == null) {
      return;
    }

    if (getContextColumn() == getDrillDownColumn()) {
      getUIFacade().fireRowActionFromUI(row);
    }
  }

  @Override
  protected ITableUIFacade createUIFacade() {
    return new P_MobileTableUIFacade();
  }

  private void reset() {
    m_cellHeaderColumn = null;
    m_cellDetailColumns = null;

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
    if (m_cellDetailColumns == null) {
      initializeDecorationConfiguration();
    }

    try {
      setTableChanging(true);
      for (ITableRow insertedRow : rows) {
        try {
          addRowByArray(new Object[]{insertedRow, ""});
          updateSummaryColumn(insertedRow);
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

  private void handleWrappedTableRowsUpdated(ITableRow[] rows) {
    if (m_originalTable == null || m_originalTable.getRowCount() == 0) {
      return;
    }

    try {
      setTableChanging(true);
      try {
        updateSummaryColumn(rows);

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

  private void updateSummaryColumn(ITableRow[] rows) throws ProcessingException {
    for (ITableRow row : rows) {
      updateSummaryColumn(row);
    }
  }

  private void updateSummaryColumn(ITableRow row) throws ProcessingException {
    ITableRow mobileTableRow = getRowMapColumn().findRow(row);
    if (mobileTableRow == null) {
      return;
    }

    String iconId = row.getIconId();
    if (iconId == null) {
      iconId = row.getCell(0).getIconId();
    }
    mobileTableRow.setIconId(iconId);
    getContentColumn().setValue(mobileTableRow, computeContentColumnValue(row));
  }

  /**
   * Analyzes the content of the table to find optimal columns for the displayed texts.
   */
  private void initializeDecorationConfiguration() {
    m_cellDetailColumns = new ArrayList<IColumn>(m_maxCellDetailColumns);

    for (IColumn<?> column : m_originalTable.getColumnSet().getVisibleColumns()) {
      if (m_cellDetailColumns.size() >= m_maxCellDetailColumns) {
        break;
      }

      if (m_cellHeaderColumn == null && useColumnForCellHeader(column)) {
        m_cellHeaderColumn = column;
      }
      else if (useColumnForCellDetail(column)) {
        m_cellDetailColumns.add(column);
      }

    }
  }

  private boolean useColumnForCellHeader(IColumn<?> column) {
    //Only use the given column if there are no summary columns defined
    if (m_originalTable.getColumnSet().getSummaryColumns().length == 0) {
      return true;
    }

    return false;
  }

  private boolean useColumnForCellDetail(IColumn<?> column) {
    boolean columnEmpty = true;
    int maxRowsToConsider = 10;

    for (int row = 0; row < Math.min(maxRowsToConsider, m_originalTable.getRowCount()); row++) {
      final String columnDisplayText = column.getDisplayText(m_originalTable.getRow(row));
      if (StringUtility.hasText(columnDisplayText)) {
        columnEmpty = false;

        String cellHeaderText = getCellHeaderText(row);
        if (cellHeaderText != null && cellHeaderText.contains(columnDisplayText)) {
          return false;
        }

      }

    }

    if (columnEmpty) {
      return false;
    }

    return true;
  }

  private String getCellHeaderText(int i) {
    if (m_cellHeaderColumn != null) {
      return m_cellHeaderColumn.getDisplayText(m_originalTable.getRow(i));
    }
    else {
      return m_originalTable.getSummaryCell(i).getText();
    }
  }

  private String createCellHtmlTemplate() {
    final StringBuilder result = new StringBuilder();

    result.append("<div style=\"display: table; height: 100%; \">");
    result.append("<div style=\"display: table-cell; vertical-align: middle;\">");
    result.append("#content#");
    result.append("</div>");
    result.append("</div>");

    return result.toString();
  }

  private String computeContentColumnValue(ITableRow row) {
    if (row == null) {
      return null;
    }

    String cellHeaderText = getCellHeaderText(row.getRowIndex());

    //Don't generate cell content if the only column contains html.
    //It is assumed that such a column is already optimized for mobile devices.
    if (m_cellDetailColumns.size() == 0 && cellHeaderText.contains("<html>")) {
      return cellHeaderText;
    }

    final StringBuilder content = new StringBuilder();
    if (StringUtility.hasText(cellHeaderText)) {
      if (m_useBoldCellHeaderText) {
        content.append("<b>");
        content.append(cellHeaderText);
        content.append("</b>");
      }
      else {
        content.append(cellHeaderText);
      }
      content.append("<br/>");
    }
    int col = 0;
    for (IColumn column : m_cellDetailColumns) {
      String displayText = extractCellDisplayText(column, row);
      if (StringUtility.hasText(displayText)) {
        if (isHeaderdiscriptionNeeded(row, column)) {
          content.append(extractColumnHeader(column));
          content.append(": ");
        }
        content.append(displayText);
      }
      if (col < m_cellDetailColumns.size() - 1) {
        content.append("<br/>");
      }
      col++;
    }

    String template = createCellHtmlTemplate();
    return template.replace("#content#", content);
  }

  private String extractCellDisplayText(IColumn column, ITableRow row) {
    String displayText = column.getDisplayText(row);
    displayText = StringUtility.removeNewLines(displayText);
    displayText = StringUtility.trim(displayText);
    //FIXME CGU if content consists of html code it will be interpreted (f.e. custom columns in crm with flag "show as table" set to true. -> how should this be handled?
    return displayText;
  }

  private String extractColumnHeader(IColumn column) {
    String header = column.getHeaderCell().getText();
    header = StringUtility.removeNewLines(header);
    header = StringUtility.trim(header);
    return header;
  }

  /**
   * Columns with a reasonable text don't need the header description.
   */
  private boolean isHeaderdiscriptionNeeded(ITableRow row, IColumn column) {
    if (column instanceof ISmartColumn<?>) {
      return column.getValue(row) instanceof Boolean;
    }

    if (column instanceof IStringColumn) {
      return isNumber(((IStringColumn) column).getValue(row));
    }

    return true;
  }

  private boolean isNumber(String value) {
    if (value == null) {
      return false;
    }

    try {
      Double.parseDouble(value);
    }
    catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private void selectRows() {
    if (m_originalTable.getSelectedRowCount() == 0) {
      return;
    }

    selectRows(getRowMapColumn().findRows(m_originalTable.getSelectedRows()));
  }

  private void checkRows() throws ProcessingException {
    if (!isCheckable() || m_originalTable.getCheckedRows().length == 0) {
      return;
    }

    checkRows(getRowMapColumn().findRows(m_originalTable.getCheckedRows()), true);
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
        //FIXME CGU handle more events?
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
      }
    }

  }

  /**
   * Dispatches ui events to the original table
   * Normal processed Events: fireHeaderSortFromUI; setContextColumnFromUI
   */
  protected class P_MobileTableUIFacade extends P_TableUIFacade {

    //------------- pass events to both ---------------------------
    @Override
    public void fireRowClickFromUI(ITableRow row) {
      super.fireRowClickFromUI(row);
      m_originalTable.getUIFacade().fireRowClickFromUI(getRowMapColumn().getValue(row));
    }

    @Override
    public void fireRowActionFromUI(ITableRow row) {
      super.fireRowActionFromUI(row);
      m_originalTable.getUIFacade().fireRowActionFromUI(getRowMapColumn().getValue(row));
    }

    @Override
    public void setSelectedRowsFromUI(ITableRow[] rows) {
      super.setSelectedRowsFromUI(rows);
      m_originalTable.getUIFacade().setSelectedRowsFromUI(getRowMapColumn().getValues(rows));
    }

    //------------- pass events only to original table -------------
    @Override
    public IMenu[] fireRowPopupFromUI() {
      return m_originalTable.getUIFacade().fireRowPopupFromUI();
    }

    @Override
    public IMenu[] fireEmptySpacePopupFromUI() {
      return m_originalTable.getUIFacade().fireEmptySpacePopupFromUI();
    }

    @Override
    public IMenu[] fireHeaderPopupFromUI() {
      return m_originalTable.getUIFacade().fireHeaderPopupFromUI();
    }

    @Override
    public TransferObject fireRowsDragRequestFromUI() {
      return m_originalTable.getUIFacade().fireRowsDragRequestFromUI();
    }

    @Override
    public void fireRowDropActionFromUI(ITableRow row, TransferObject dropData) {
      m_originalTable.getUIFacade().fireRowDropActionFromUI(getRowMapColumn().getValue(row), dropData);
    }

    @Override
    public boolean fireKeyTypedFromUI(String keyStrokeText, char keyChar) {
      return m_originalTable.getUIFacade().fireKeyTypedFromUI(keyStrokeText, keyChar);
    }

    @Override
    public void fireHyperlinkActionFromUI(ITableRow row, IColumn<?> column, URL url) {
      m_originalTable.getUIFacade().fireHyperlinkActionFromUI(getRowMapColumn().getValue(row), column, url);
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
