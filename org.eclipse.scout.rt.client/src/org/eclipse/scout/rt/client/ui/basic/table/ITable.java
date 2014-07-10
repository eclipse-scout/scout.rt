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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

/**
 * The table is by default multi-select Columns are defined as inner classes for
 * every inner column class there is a generated getXYColumn method directly on
 * the table use isValueChangeTriggerEnabled() when formfieldata is being loaded
 * <p>
 * You can write html into the table cells.
 * <p>
 * You can use local urls that call back to the table itself and can be handled by overriding
 * {@link AbstractTable#execHyperlinkAction(URL, String, boolean)}. A local URL is one of the form http://local/...
 * <p>
 */
public interface ITable extends IPropertyObserver, IDNDSupport, ITypeWithClassId {

  /**
   * String
   */
  String PROP_TITLE = "title";
  /**
   * Boolean
   */
  String PROP_ENABLED = "enabled";
  /**
   * String
   */
  String PROP_DEFAULT_ICON = "defaultIcon";
  /**
   * Boolean
   */
  String PROP_MULTI_SELECT = "multiSelect";
  /**
   * Boolean
   */
  String PROP_MULTI_CHECK = "multiCheck";
  /**
   * Boolean
   */
  String PROP_MULTILINE_TEXT = "multilineText";
  /**
   * Integer default -1
   */
  String PROP_ROW_HEIGHT_HINT = "rowHeightHint";

  /**
   * Boolean
   */
  String PROP_CHECKABLE = "checkable";
  /**
   * Boolean
   */
  String PROP_HEADER_VISIBLE = "headerVisible";
  /**
   * Boolean
   */
  String PROP_KEYBOARD_NAVIGATION = "keyboardNavigation";
  /**
   * Boolean
   */
  String PROP_AUTO_RESIZE_COLUMNS = "autoResizeColumns";
  /**
   * {@link IColumn}
   */
  String PROP_CONTEXT_COLUMN = "contextColumn";

  /**
   * {@link ITableColumnFilterManager}
   */
  String PROP_COLUMN_FILTER_MANAGER = "columnFilterManger";
  /**
   * {@link List<IKeyStroke>}
   */
  String PROP_KEY_STROKES = "keyStroks";
  /**
   * Boolean
   */
  String PROP_SCROLL_TO_SELECTION = "scrollToSelection";
  /**
   * Object
   * <p>
   * Container of this table, {@link org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage IPage},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField ITableField},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox IListBox},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField IPlannerField}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  String PROP_CONTAINER = "container";

  /**
   * @since 4.0.0 {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenus";
  /**
   * Host for local urls that call back to the table itself and can be handled by overriding
   * {@link AbstractTable#execHyperlinkAction(URL, String, boolean)}.
   */
  String LOCAL_URL_HOST = "local";

  /**
   * Prefix for local urls that call back to the table itself and can be handled by overriding
   * {@link AbstractTable#execHyperlinkAction(URL, String, boolean)}.
   */
  String LOCAL_URL_PREFIX = "http://" + LOCAL_URL_HOST + "/";

  /**
   * {@link ITableCustomizer}
   */
  String PROP_TABLE_CUSTOMIZER = "tableCustomizer";

  void initTable() throws ProcessingException;

  void disposeTable();

  /**
   * set the context column and processes the url.
   * <p>
   * see {@link #setContextColumn(IColumn)}
   */
  void doHyperlinkAction(ITableRow row, IColumn<?> col, URL url) throws ProcessingException;

  List<ITableRowFilter> getRowFilters();

  /**
   * adding a filter multiple times is supported. This only adds it the first time. The other times it just calls
   * {@link #applyRowFilters()}
   */
  void addRowFilter(ITableRowFilter filter);

  void removeRowFilter(ITableRowFilter filter);

  /**
   * (Re-) applies all row filters to the table.
   * <p>
   * This can be useful when the state of a row filter was changed without adding or removing it.
   * <p>
   * {@link #addRowFilter(ITableRowFilter)} and {@link #removeRowFilter(ITableRowFilter)} automatically apply the
   * filters to the table.
   */
  void applyRowFilters();

  String getTitle();

  void setTitle(String s);

  /**
   * @return the context in which the table and column settings (order, width,
   *         visible,...) are loaded and stored from the {@link org.eclipse.scout.rt.client.ui.ClientUIPreferences
   *         ClientUIPreferences}
   */
  String getUserPreferenceContext();

  /**
   * Set the context in which the table and column settings (order, width,
   * visible,...) are loaded and stored from the {@link org.eclipse.scout.rt.client.ui.ClientUIPreferences
   * ClientUIPreferences}
   * <p>
   * Be very careful when changing this property during runtime and when the table is initialized. Use the constructor
   * argument instead.
   */
  void setUserPreferenceContext(String context);

  /**
   * true: all columns are resized so that the table never needs horizontal
   * scrolling
   */
  boolean isAutoResizeColumns();

  void setAutoResizeColumns(boolean b);

  /**
   * Convenience for getColumnSet().getColumnCount()
   */
  int getColumnCount();

  /**
   * short form for getColumnSet().getColumns()
   */
  List<IColumn<?>> getColumns();

  List<String> getColumnNames();

  /**
   * Convenience for getColumnSet().getVisibleColumnCount()
   */
  int getVisibleColumnCount();

  ColumnSet getColumnSet();

  int getRowCount();

  ITableRow getRow(int rowIndex);

  List<ITableRow> getRows(int[] rowIndexes);

  /**
   * This method is Thread-Safe.
   * <p>
   * For performance reasons, this method returns the life array. Do NOT change the array contents directly!
   */
  List<ITableRow> getRows();

  /**
   * see {@link #setRowFilter(ITableRowFilter)} and {@link #getRowFilter()} This
   * method is Thread-Safe.
   * <p>
   * For performance reasons, this method returns the life array. Do NOT change the array contents directly!
   */
  List<ITableRow> getFilteredRows();

  /**
   * see {@link #setRowFilter(ITableRowFilter)} and {@link #getRowFilter()} This
   * method is Thread-Safe.
   */
  int getFilteredRowCount();

  /**
   * see {@link #setRowFilter(ITableRowFilter)} and {@link #getRowFilter()} This
   * method is Thread-Safe.
   */
  ITableRow getFilteredRow(int index);

  /**
   * see {@link #setRowFilter(ITableRowFilter)} and {@link #getRowFilter()} This
   * method is Thread-Safe.
   */
  int getFilteredRowIndex(ITableRow row);

  /**
   * @return matrix[rowCount][columnCount] with cell values all calls (also
   *         invisible and primary key) are considered
   */
  Object[][] getTableData();

  /**
   * see {@link TableUtility#exportRowsAsCSV(List<? extends ITableRow>, List<? extends IColumn<?>>, boolean, boolean,
   * boolean)}
   */
  Object[][] exportTableRowsAsCSV(List<? extends ITableRow> rows, List<? extends IColumn> columns, boolean includeLineForColumnNames, boolean includeLineForColumnTypes, boolean includeLineForColumnFormat);

  int getInsertedRowCount();

  List<ITableRow> getInsertedRows();

  int getUpdatedRowCount();

  List<ITableRow> getUpdatedRows();

  int getDeletedRowCount();

  List<ITableRow> getDeletedRows();

  int getNotDeletedRowCount();

  List<ITableRow> getNotDeletedRows();

  List<Object> getRowKeys(int rowIndex);

  List<Object> getRowKeys(ITableRow row);

  ITableRow findRowByKey(List<?> keys);

  /*
   * Service aspect
   */

  /**
   * extract transfer data to be sent to the backend
   */
  void extractTableData(AbstractTableFieldData target) throws ProcessingException;

  /**
   * apply transfer data to this table
   */
  void updateTable(AbstractTableFieldData source) throws ProcessingException;

  /**
   * Convenience to find a menu, uses {@link org.eclipse.scout.rt.client.ui.action.ActionFinder ActionFinder}
   */
  <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException;

  List<IKeyStroke> getKeyStrokes();

  void setKeyStrokes(List<? extends IKeyStroke> keyStrokes);

  /**
   * Run a menu The menu is first prepared and only executed when it is visible
   * and enabled
   * 
   * @return true if menu was executed
   */
  boolean runMenu(Class<? extends IMenu> menuType) throws ProcessingException;

  /**
   * @see #setScrollToSelection()
   */
  boolean isScrollToSelection();

  /**
   * @param b
   *          true: advices the attached ui to make the current selection visible.
   *          The current selection will be scrolled to visible (again, whenever the table size changes).
   */
  void setScrollToSelection(boolean b);

  /**
   * May be used when {@link #isScrollToSelection()} = false for a one-time scroll. The property scrollToVisible however
   * remains untouched.
   * <p>
   * This is a one-time scroll advise to the ui
   */
  void scrollToSelection();

  /*
   * display
   */
  IHeaderCell getVisibleHeaderCell(int visibleColumnIndex);

  IHeaderCell getHeaderCell(int columnIndex);

  IHeaderCell getHeaderCell(IColumn<?> col);

  ICell getVisibleCell(ITableRow row, int visibleColumnIndex);

  ICell getVisibleCell(int rowIndex, int visibleColumnIndex);

  ICell getCell(int rowIndex, int columnIndex);

  ICell getCell(ITableRow row, IColumn<?> column);

  /**
   * @see #getSummaryCell(ITableRow)
   */
  ICell getSummaryCell(int rowIndex);

  /**
   * composite cell containing the union of all values of this rows that are in
   * a column with property summary=true when no summary column visible or there
   * are none, this defaults to the first defined visible column
   * 
   * @see IColumn#isSummary()
   */
  ICell getSummaryCell(ITableRow row);

  /**
   * @return the effective live editable state ( {@link IColumn#isCellEditable(ITableRow)} and
   *         {@link IColumn#isVisible()} )
   *         <p>
   *         Note that this is not a java bean getter and thus not thread-safe. Calls to this method must be inside a
   *         {@link org.eclipse.scout.rt.client.ClientSyncJob ClientSyncJob} resp. a job using the
   *         {@link org.eclipse.scout.rt.client.ClientRule ClientRule}.
   */
  boolean isCellEditable(int rowIndex, int columnIndex);

  /**
   * @return the effective live editable state ( {@link IColumn#isCellEditable(ITableRow)} and
   *         {@link IColumn#isVisible()} )
   *         <p>
   *         Note that this is not a java bean getter and thus not thread-safe. Calls to this method must be inside a
   *         {@link org.eclipse.scout.rt.client.ClientSyncJob ClientSyncJob} resp. a job using the
   *         {@link org.eclipse.scout.rt.client.ClientRule ClientRule}.
   */
  boolean isCellEditable(ITableRow row, int visibleColumnIndex);

  /**
   * @return the effective live editable state ( {@link IColumn#isCellEditable(ITableRow)} and
   *         {@link IColumn#isVisible()} )
   *         <p>
   *         Note that this is not a java bean getter and thus not thread-safe. Calls to this method must be inside a
   *         {@link org.eclipse.scout.rt.client.ClientSyncJob ClientSyncJob} resp. a job using the
   *         {@link org.eclipse.scout.rt.client.ClientRule ClientRule}.
   */
  boolean isCellEditable(ITableRow row, IColumn<?> column);

  /*
   * Properties observer section
   */

  Object getProperty(String name);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if you really know what you are doing. Rather use the officially provided api instead. <br>
   * Example for an unexpected behavior: setVisible() does not only set the property PROP_VISIBLE but also executes
   * additional code. This code would NOT be executed by directly setting the property PROP_VISIBLE with setProperty().
   */
  void setProperty(String name, Object value);

  boolean hasProperty(String name);

  boolean isCheckable();

  void setCheckable(boolean b);

  boolean isHeaderVisible();

  void setHeaderVisible(boolean b);

  boolean isMultilineText();

  void setMultilineText(boolean on);

  /**
   * This is a hint for the UI iff it is not capable of
   * having variable table row height based on cell contents (such as rap/rwt or swt).
   * <p>
   * This property is interpreted in different manner for each GUI port:
   * <ul>
   * <li>Swing: The property is ignored.
   * <li>SWT: Used as the maximal row height.
   * <li>rap/rwt: Used as the fixed row height in multiline tables.
   * </ul>
   * </p>
   * This hint defines the table row height in pixels being used as the row height for all table rows of this table
   * dependent of the GUI port.
   * </p>
   * 
   * @return the hint in pixels, default is -1
   */
  int getRowHeightHint();

  /**
   * see {@link #getRowHeightHint()}
   */
  void setRowHeightHint(int h);

  /**
   * other than isMultilineText this property reflects the default multiLine state of the table that is used when
   * wrapping columns become
   * visible or get hidden
   */
  boolean isInitialMultilineText();

  /**
   * see {@link #isInitialMultilineText()}
   */
  void setInitialMultilineText(boolean on);

  /**
   * this property is used to enable keyboard navigation of a table.
   * The row starting with the typed string will be selected. The considered
   * column is the context column; either the last selected column or the column
   * having a sort order.
   */
  boolean hasKeyboardNavigation();

  void setKeyboardNavigation(boolean on);

  /**
   * true if multiple rows can be selected (default false)
   */
  boolean isMultiSelect();

  void setMultiSelect(boolean on);

  /**
   * true if multiple rows can be checked (default true)
   */
  boolean isMultiCheck();

  void setMultiCheck(boolean b);

  boolean isEnabled();

  void setEnabled(boolean b);

  /**
   * This property changes the behaviour of {@link #replaceRows(List<? extends ITableRow>)} and
   * {@link #deleteRows(int[])} true
   * discards rows when deleted, false keeps
   * them in the cache for later usage in change management Default value is
   * true for {@link IPageWithTable} and false for
   * {@link org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField ITableField}
   */
  boolean isAutoDiscardOnDelete();

  /**
   * see {@link #isAutoDiscardOnDelete()}
   */
  void setAutoDiscardOnDelete(boolean on);

  String getDefaultIconId();

  void setDefaultIconId(String iconId);

  /**
   * Notify the ui factory to give focus to the table
   */
  void requestFocus();

  /**
   * Notify the ui factory to give focus to the table cell.
   * <p>
   * This will only work if the table cell is editable.
   */
  void requestFocusInCell(IColumn<?> column, ITableRow row);

  void addTableListener(TableListener listener);

  void removeTableListener(TableListener listener);

  /**
   * Add the listener at the top (front) of the listener list (so it is called as LAST listener).
   * <p>
   * This method is normally only used by the ui layer to update its state before other listeners handle them
   * <p>
   * Use {@link #addTableListener(TableListener)} in all other cases
   */
  void addUITableListener(TableListener listener);

  /**
   * @return the {@link IEventHistory} associated with this table
   *         <p>
   *         The default implementation is a {@link DefaultTableEventHistory} and created by
   *         {@link AbstractTable#createEventHistory()}
   *         <p>
   *         This method is thread safe.
   * @since 3.8
   */
  IEventHistory<TableEvent> getEventHistory();

  /**
   * when performing a batch mutation use this marker like try{
   * setTableChanging(true); ...modify columns, rows, perform sorting etc. }
   * finally{ setTableChanging(false); }
   */
  void setTableChanging(boolean b);

  boolean isTableChanging();

  boolean isTableInitialized();

  void setRowState(ITableRow row, int rowState) throws ProcessingException;

  void setRowState(Collection<? extends ITableRow> rows, int rowState) throws ProcessingException;

  void setAllRowState(int rowState) throws ProcessingException;

  void updateRow(ITableRow modifiedRow);

  void updateRows(Collection<? extends ITableRow> modifiedRows);

  void updateAllRows();

  /**
   * replace rows based on a comparison of their primary keys similar to
   * updateRows(), this method modifies existing rows the difference is that
   * newRows is a NEW set of rows whereas updateRows processes changes on
   * existing rows the idea is that a reload of table data does not delete and
   * (re)add rows but intelligently update existing rows if their primary key
   * matches the key of a new row in detail: for every argument newRow: 1. if
   * for an existingRow the check getRowKeys(newRow)==getRowKeys(existingRow) is
   * true, then existingRow is replaced by newRow when there are multiple
   * existingRow's that match, only the first one is replaced and the others are
   * removed 2. if for no existingRow the check
   * getRowKeys(newRow)==getRowKeys(existingRow) is true then the newRow is
   * added to the table as a really new row 3. all existing rows that do not
   * match any newRow are deleted
   */
  void replaceRows(List<? extends ITableRow> newRows) throws ProcessingException;

  void replaceRowsByArray(Object dataArray) throws ProcessingException;

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  void replaceRowsByMatrix(Object dataMatrix) throws ProcessingException;

  int getSelectedRowCount();

  ITableRow getSelectedRow();

  List<ITableRow> getSelectedRows();

  boolean isSelectedRow(ITableRow row);

  void selectRow(int rowIndex);

  void selectRow(ITableRow row);

  void selectRow(ITableRow row, boolean append);

  void selectRows(List<? extends ITableRow> rows);

  void selectRows(List<? extends ITableRow> rows, boolean append);

  void selectFirstRow();

  void selectPreviousRow();

  void selectNextRow();

  void selectLastRow();

  void selectAllRows();

  void selectAllEnabledRows();

  void deselectRow(ITableRow row);

  void deselectRows(List<? extends ITableRow> row);

  void deselectAllRows();

  void deselectAllEnabledRows();

  void setCheckableColumn(IBooleanColumn checkboxColumn);

  IBooleanColumn getCheckableColumn();

  Collection<ITableRow> getCheckedRows();

  void checkRow(int rowIndex, boolean value) throws ProcessingException;

  void checkRow(ITableRow row, boolean value) throws ProcessingException;

  void checkRows(Collection<? extends ITableRow> rows, boolean value) throws ProcessingException;

  void checkAllRows() throws ProcessingException;

  void uncheckAllRows() throws ProcessingException;

  /**
   * column that represented the last ui (mouse click) context
   * {@link ITableUIFacade#setContextCellFromUI(ITableRow,IColumn)} see {@link #setContextCell(ITableRow, IColumn)}
   */
  IColumn<?> getContextColumn();

  /**
   * Set the column that represents the last ui (mouse click) column context, normally
   * called by {@link ITableUIFacade#setContextColumnFromUI(IColumn)} and not by client code. see
   * {@link #getContextColumn()}
   */
  void setContextColumn(IColumn<?> col);

  /**
   * calls {@link #addRow(ITableRow, false)}
   */
  ITableRow addRow(ITableRow newRow) throws ProcessingException;

  /**
   * the newRow is added to the table. After the add succeeds the argument row
   * newRow has a valid reference to its coresponding ITableRow and the new
   * ITableRow is returned
   */
  ITableRow addRow(ITableRow newRow, boolean markAsInserted) throws ProcessingException;

  /**
   * calls {@link #addRows(List<? extends ITableRow>, false)}
   */
  List<ITableRow> addRows(List<? extends ITableRow> newRows) throws ProcessingException;

  /**
   * calls {@link #addRows(List<? extends ITableRow>, markAsInserted, null)}
   */
  List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted) throws ProcessingException;

  /**
   * all newRows are added to the table. After the add succeeds the argument
   * rows newRows have valid references to their coresponding ITableRow and the
   * new ITableRows are returned Using insertIndexes: assume the rows have been
   * added to the table; insertIndexes = what indexes should they cover
   * 
   * @return added rows in order as they were passed to the method
   */
  List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted, int[] insertIndexes) throws ProcessingException;

  /**
   * Convenience to add one row by its data
   */
  ITableRow addRowByArray(Object dataArray) throws ProcessingException;

  /**
   * Convenience to add one row by its data
   */
  List<ITableRow> addRowsByArray(Object dataArray, int rowStatus) throws ProcessingException;

  /**
   * Convenience to add multiple rows by their data arrays
   */
  List<ITableRow> addRowsByMatrix(Object dataMatrix) throws ProcessingException;

  /**
   * Convenience to add multiple rows by their data arrays
   */
  List<ITableRow> addRowsByMatrix(Object dataMatrix, int rowStatus) throws ProcessingException;

  /**
   * Convenience to add multiple rows by their single value (key value)
   */
  List<ITableRow> addRowsByArray(Object dataArray) throws ProcessingException;

  void moveRow(int sourceIndex, int targetIndex);

  void moveRowBefore(ITableRow movingRow, ITableRow targetRow);

  void moveRowAfter(ITableRow movingRow, ITableRow targetRow);

  void deleteRow(int rowIndex);

  void deleteRows(int[] rowIndexes);

  void deleteRow(ITableRow row);

  void deleteAllRows();

  void deleteRows(Collection<? extends ITableRow> rows);

  void discardRow(int rowIndex);

  void discardRows(int[] rowIndexes);

  void discardRow(ITableRow row);

  void discardAllRows();

  void discardRows(Collection<? extends ITableRow> rows);

  void discardAllDeletedRows();

  void discardDeletedRow(ITableRow deletedRow);

  void discardDeletedRows(Collection<? extends ITableRow> deletedRows);

  /**
   * @return this row if it is in fact a valid table row, null otherwise
   */
  ITableRow resolveRow(ITableRow row);

  /**
   * @return this array if all rows are valid table row, reduced array otherwise
   */
  List<ITableRow> resolveRows(Collection<? extends ITableRow> rows);

  void tablePopulated();

  void sort();

  void sort(List<? extends ITableRow> rowsInNewOrder);

  boolean isSortEnabled();

  void setSortEnabled(boolean b);

  ITableColumnFilterManager getColumnFilterManager();

  void setColumnFilterManager(ITableColumnFilterManager m);

  ITableCustomizer getTableCustomizer();

  void setTableCustomizer(ITableCustomizer c);

  /**
   * Container of this table, {@link org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage IPage},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField ITableField},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox IListBox},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField IPlannerField}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  ITypeWithClassId getContainer();

  /**
   * Initialize and reset all columns. This operation removes all columns from the table and adds them as if the table
   * was created again.
   * <p>
   * Note that this operation discards all table content by calling {@link #discardAllRows()} before rebuilding the
   * columns.
   * <p>
   * When done a {@link TableEvent#TYPE_COLUMN_STRUCTURE_CHANGED} event is sent.
   * <p>
   * This can be useful on tables with dynamic injected columns that need to be refreshed after a state change.
   */
  void resetColumnConfiguration();

  /**
   * initialize column visibility of all displayable columns
   */
  void resetColumnVisibilities();

  /**
   * initialize column sort order
   */
  void resetColumnSortOrder();

  /**
   * initialize column widths
   */
  void resetColumnWidths();

  /**
   * initialize column order
   */
  void resetColumnOrder();

  /**
   * initialize all columns: visible/invisible, order, sorting, width
   * <p>
   * same as calling {@link #resetColumns(true, true, true, true)}
   */
  void resetDisplayableColumns();

  /**
   * initialize columns
   */
  void resetColumns(boolean visibility, boolean order, boolean sorting, boolean widths);

  void decorateRow(ITableRow row);

  void decorateCell(ITableRow row, IColumn<?> col);

  /*
   * UI interface
   */
  ITableUIFacade getUIFacade();

  /**
   * Exports the contents of this table into the given {@link AbstractTableFieldBeanData}. The mapping from
   * {@link IColumn}s to {@link AbstractTableRowData} properties is based on the property name and the
   * {@link IColumn#getColumnId()}.
   * 
   * @param target
   * @throws ProcessingException
   * @since 3.10.0-M3
   */
  void exportToTableBeanData(AbstractTableFieldBeanData target) throws ProcessingException;

  /**
   * Imports the contents of the given {@link AbstractTableFieldBeanData}. The mapping from {@link AbstractTableRowData}
   * properties to {@link IColumn}s is based on the property name and the {@link IColumn#getColumnId()}.
   * 
   * @param source
   * @throws ProcessingException
   * @since 3.10.0-M3
   */
  void importFromTableBeanData(AbstractTableFieldBeanData source) throws ProcessingException;

  /**
   * Creates a {@link TableRowDataMapper} that is used for reading and writing data from the given
   * {@link AbstractTableRowData} type.
   * 
   * @param rowType
   * @return
   * @throws ProcessingException
   * @since 3.10.0-M5
   */
  ITableRowDataMapper createTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) throws ProcessingException;

  /**
   * @param menus
   */
  void setMenus(List<? extends IMenu> menus);

  /**
   * @param menu
   */
  void addMenu(IMenu menu);

  /**
   * @return the child list of {@link #getContextMenu()}
   */
  List<IMenu> getMenus();

  /**
   * @return the invisible root menu container of all table menus.
   */
  ITableContextMenu getContextMenu();
}
