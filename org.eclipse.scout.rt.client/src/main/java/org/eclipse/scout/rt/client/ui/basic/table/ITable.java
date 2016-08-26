/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizer;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.security.CreateCustomColumnPermission;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

/**
 * The table is by default multi-select.
 * <p>
 * Columns are defined as inner classes.
 * </p>
 * For every inner column class there is a generated getXYColumn method directly on the table.
 */
public interface ITable extends IPropertyObserver, IDNDSupport, ITypeWithClassId, IStyleable, IAppLinkCapable, IContextMenuOwner {

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
  String PROP_ROW_ICON_VISIBLE = "rowIconVisible";
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
  String PROP_HEADER_ENABLED = "headerEnabled";
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
   * {@link TableUserFilterManager}
   */
  String PROP_USER_FILTER_MANAGER = "userFilterManager";
  /**
   * {@link List<IKeyStroke>}
   */
  String PROP_KEY_STROKES = "keyStrokes";
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
   * {@link org.eclipse.scout.rt.client.ui.form.fields.plannerfieldold.IPlannerFieldOld IPlannerField}
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
   * {@link ITableCustomizer}
   */
  String PROP_TABLE_CUSTOMIZER = "tableCustomizer";

  /**
   * List&lt;ITableControl&gt;
   *
   * @since 5.1.0
   */
  String PROP_TABLE_CONTROLS = "tableControls";

  /**
   * Boolean
   *
   * @since 5.1.0
   */
  String PROP_TABLE_STATUS_VISIBLE = "tableStatusVisible";

  /**
   * {@link IStatus}
   *
   * @since 5.1.0
   */
  String PROP_TABLE_STATUS = "tableStatus";

  /**
   * Boolean
   *
   * @since 5.2.0
   */
  String PROP_SORT_ENABLED = "sortEnabled";

  /**
   * type boolean
   *
   * @since 5.2.0
   */
  String PROP_UI_SORT_POSSIBLE = "uiSortPossible";

  /**
   * type boolean
   *
   * @since 5.2.0
   */
  String PROP_LOADING = "loading";

  void initTable();

  void disposeTable();

  /**
   * @since 5.1.0
   */
  AbstractEventBuffer<TableEvent> createEventBuffer();

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
   * @return the context in which the table and column settings (order, width, visible,...) are loaded and stored from
   *         the {@link org.eclipse.scout.rt.client.ui.ClientUIPreferences ClientUIPreferences}
   */
  String getUserPreferenceContext();

  /**
   * Set the context in which the table and column settings (order, width, visible,...) are loaded and stored from the
   * {@link org.eclipse.scout.rt.client.ui.ClientUIPreferences ClientUIPreferences}
   * <p>
   * Be very careful when changing this property during runtime and when the table is initialized. Use the constructor
   * argument instead.
   */
  void setUserPreferenceContext(String context);

  /**
   * true: all columns are resized so that the table never needs horizontal scrolling
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
   * Returns all rows that remain after applying all {@link ITableRowFilter}s. To get all rows (including the unaccepted
   * ones) use {@link #getRows()}.
   * <p>
   * This method is thread-safe.
   * <p>
   * <b>For performance reasons, this method returns the life array. Do NOT change the array contents directly!</b>
   */
  List<ITableRow> getFilteredRows();

  /**
   * Returns the number of filtered rows (accepted by all {@link ITableRowFilter}s).
   * <p>
   * This method is thread-safe.
   */
  int getFilteredRowCount();

  /**
   * Returns a row with its index relative to the list of filtered rows ({@link #getFilteredRows()}). Because the list
   * of filtered rows might be smaller than the total row count, this index differs from the ordinary
   * {@link ITableRow#getRowIndex()}. It can be obtained with {@link #getFilteredRowIndex(ITableRow)}.
   * <p>
   * This method is thread-safe.
   */
  ITableRow getFilteredRow(int index);

  /**
   * Returns the index of a given row relative to the list of filtered rows (instead of all rows). Because the list of
   * filtered rows might be smaller than the total row count, this index differs from the ordinary
   * {@link ITableRow#getRowIndex()}. It can be used to retrieve a row again with {@link #getFilteredRow(int)}.
   * <p>
   * This method is thread-safe.
   */
  int getFilteredRowIndex(ITableRow row);

  /**
   * @return matrix[rowCount][columnCount] with cell values all calls (also invisible and primary key) are considered
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
   *
   * @Deprecated: 'Array based TableData' are not supported by the Scout SDK in Neon. Use
   *              {@link #exportToTableBeanData(AbstractTableFieldBeanData)} instead. This method will be removed with
   *              Oxygen. See Bug 496292.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  void extractTableData(org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData target);

  /**
   * apply transfer data to this table
   *
   * @Deprecated: 'Array based TableData' are not supported by the Scout SDK in Neon. Use
   *              {@link #importFromTableBeanData(AbstractTableFieldBeanData)} instead. This method will be removed with
   *              Oxygen. See Bug 496292.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  void updateTable(org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData source);

  /**
   * Convenience to find a menu, uses {@link org.eclipse.scout.rt.client.ui.action.ActionFinder ActionFinder}
   *
   * @deprecated Use {@link #getMenuByClass(Class)} instead. This method is error-prone, because it does not throw an
   *             exception if the given menu type is ambiguous. It will be removed in the O-Release (7.0).
   */
  @Deprecated
  <T extends IMenu> T getMenu(Class<T> menuType);

  List<IKeyStroke> getKeyStrokes();

  void setKeyStrokes(List<? extends IKeyStroke> keyStrokes);

  /**
   * Run a menu The menu is first prepared and only executed when it is visible and enabled
   *
   * @return true if menu was executed
   */
  boolean runMenu(Class<? extends IMenu> menuType);

  /**
   * @see #setScrollToSelection(boolean)
   */
  boolean isScrollToSelection();

  /**
   * @param b
   *          true: advices the attached ui to make the current selection visible. The current selection will be
   *          scrolled to visible (again, whenever the table size changes).
   */
  void setScrollToSelection(boolean b);

  /**
   * May be used when {@link #isScrollToSelection()} = false for a one-time scroll. The property
   * {@value #PROP_SCROLL_TO_SELECTION} however remains untouched.
   * <p>
   * This is a one-time scroll advise to the ui. Only works if the table has already been loaded and is displayed.
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
   * composite cell containing the union of all values of this rows that are in a column with property summary=true when
   * no summary column visible or there are none, this defaults to the first defined visible column
   *
   * @see IColumn#isSummary()
   */
  ICell getSummaryCell(ITableRow row);

  /**
   * Convenience for {@link ICell#isEditable()}
   */
  boolean isCellEditable(int rowIndex, int columnIndex);

  /**
   * Convenience for {@link ICell#isEditable()}
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

  boolean isHeaderEnabled();

  void setHeaderEnabled(boolean headerEnabled);

  boolean isMultilineText();

  void setMultilineText(boolean on);

  /**
   * This is a hint for the UI iff it is not capable of having variable table row height based on cell contents.
   * <p>
   * This property is interpreted in different manner for each GUI port:
   * <ul>
   * <li>Swing: The property is ignored.
   * </ul>
   * </p>
   * <p>
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
   * wrapping columns become visible or get hidden
   */
  boolean isInitialMultilineText();

  /**
   * see {@link #isInitialMultilineText()}
   */
  void setInitialMultilineText(boolean on);

  /**
   * this property is used to enable keyboard navigation of a table. The row starting with the typed string will be
   * selected. The considered column is the context column; either the last selected column or the column having a sort
   * order.
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
   * {@link #deleteRows(int[])} true discards rows when deleted, false keeps them in the cache for later usage in change
   * management Default value is true for {@link IPageWithTable} and false for
   * {@link org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField ITableField}
   */
  boolean isAutoDiscardOnDelete();

  /**
   * see {@link #isAutoDiscardOnDelete()}
   */
  void setAutoDiscardOnDelete(boolean on);

  String getDefaultIconId();

  void setDefaultIconId(String iconId);

  boolean isRowIconVisible();

  void setRowIconVisible(boolean rowIconVisible);

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
   * when performing a batch mutation use this marker like try{ setTableChanging(true); ...modify columns, rows, perform
   * sorting etc. } finally{ setTableChanging(false); }
   */
  void setTableChanging(boolean b);

  boolean isTableChanging();

  boolean isTableInitialized();

  void setRowState(ITableRow row, int rowState);

  void setRowState(Collection<? extends ITableRow> rows, int rowState);

  void setAllRowState(int rowState);

  void updateRow(ITableRow modifiedRow);

  void updateRows(Collection<? extends ITableRow> modifiedRows);

  void updateAllRows();

  /**
   * replace rows based on a comparison of their primary keys similar to updateRows(), this method modifies existing
   * rows the difference is that newRows is a NEW set of rows whereas updateRows processes changes on existing rows the
   * idea is that a reload of table data does not delete and (re)add rows but intelligently update existing rows if
   * their primary key matches the key of a new row in detail: for every argument newRow: 1. if for an existingRow the
   * check getRowKeys(newRow)==getRowKeys(existingRow) is true, then existingRow is replaced by newRow when there are
   * multiple existingRow's that match, only the first one is replaced and the others are removed 2. if for no
   * existingRow the check getRowKeys(newRow)==getRowKeys(existingRow) is true then the newRow is added to the table as
   * a really new row 3. all existing rows that do not match any newRow are deleted
   */
  void replaceRows(List<? extends ITableRow> newRows);

  void replaceRowsByArray(Object dataArray);

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  void replaceRowsByMatrix(Object dataMatrix);

  int getSelectedRowCount();

  ITableRow getSelectedRow();

  List<ITableRow> getSelectedRows();

  boolean isSelectedRow(ITableRow row);

  boolean isCheckedRow(ITableRow row);

  void selectRow(int rowIndex);

  void selectRow(ITableRow row);

  /**
   * @param append
   *          true if the row should be appended to the existing selection, false if not. True has only an effect if
   *          {@link ITable#isMultiSelect()} is true.
   */
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

  List<ITableRow> getCheckedRows();

  /**
   * @param value
   *          true if the row should be checked, false if not
   */
  void checkRow(int rowIndex, boolean value);

  /**
   * @param value
   *          true if the row should be checked, false if not
   */
  void checkRow(ITableRow row, boolean value);

  /**
   * @param value
   *          true if the row should be checked, false if not
   */
  void checkRows(Collection<? extends ITableRow> rows, boolean value);

  void checkAllRows();

  void uncheckRow(ITableRow row);

  void uncheckRows(Collection<? extends ITableRow> rows);

  void uncheckAllEnabledRows();

  void uncheckAllRows();

  /**
   * column that represented the last ui (mouse click) context {@link ITableUIFacade#setContextColumnFromUI(IColumn)}
   * see {@link #setContextColumn(IColumn)}
   */
  IColumn<?> getContextColumn();

  /**
   * Set the column that represents the last ui (mouse click) column context, normally called by
   * {@link ITableUIFacade#setContextColumnFromUI(IColumn)} and not by client code. see {@link #getContextColumn()}
   */
  void setContextColumn(IColumn<?> col);

  /**
   * Creates and inserts a new {@link ITableRow} (with status {@link ITableRow#STATUS_INSERTED})
   */
  ITableRow addRow();

  /**
   * Adds a new {@link ITableRow}
   *
   * @param markAsInserted
   *          if <code>true</code>, the status of the new row is {@link ITableRow#STATUS_INSERTED}, otherwise the status
   *          {@link ITableRow#STATUS_NON_CHANGED}
   */
  ITableRow addRow(boolean markAsInserted);

  /**
   * calls {@link #addRow(ITableRow, false)}
   */
  ITableRow addRow(ITableRow newRow);

  /**
   * the newRow is added to the table. After the add succeeds the argument row newRow has a valid reference to its
   * coresponding ITableRow and the new ITableRow is returned
   */
  ITableRow addRow(ITableRow newRow, boolean markAsInserted);

  /**
   * calls {@link #addRows(List<? extends ITableRow>, false)}
   */
  List<ITableRow> addRows(List<? extends ITableRow> newRows);

  /**
   * calls {@link #addRows(List<? extends ITableRow>, markAsInserted, null)}
   */
  List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted);

  /**
   * all newRows are added to the table. After the add succeeds the argument rows newRows have valid references to their
   * coresponding ITableRow and the new ITableRows are returned Using insertIndexes: assume the rows have been added to
   * the table; insertIndexes = what indexes should they cover
   *
   * @return added rows in order as they were passed to the method
   */
  List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted, int[] insertIndexes);

  /**
   * Convenience to add one row by its data
   */
  ITableRow addRowByArray(Object dataArray);

  /**
   * Convenience to add one row by its data
   */
  List<ITableRow> addRowsByArray(Object dataArray, int rowStatus);

  /**
   * Convenience to add multiple rows by their data arrays
   */
  List<ITableRow> addRowsByMatrix(Object dataMatrix);

  /**
   * Convenience to add multiple rows by their data arrays
   */
  List<ITableRow> addRowsByMatrix(Object dataMatrix, int rowStatus);

  /**
   * Convenience to add multiple rows by their single value (key value)
   */
  List<ITableRow> addRowsByArray(Object dataArray);

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

  /**
   * Is a simplified ui sorting possible? If enabled, ui (e.g. web browser, presentation layer itself) may sort without
   * computing the order on a client server (e.g. application server). The ui uses a very simplified sort rule set, e.g.
   * alphabetical or numerical sorting is possible, more advanced sort rules are not implemented. Which rules to use are
   * determined by the client. To use advanced sort rules return <code>false</code>.
   * <p>
   * If set to <code>false</code> this property overrides any {@link IColumn#isUiSortPossible()} setting. If set to
   * <code>true</code> this property may be overridden by setting at least one {@link IColumn#isUiSortPossible()} to
   * <code>false</code>.
   * <p>
   * This property is currently also used for internal purposes after certain column property changes (e.g. visibility)
   * the value of this property may be changed. To adjust this behavior, see
   * {@link AbstractTable#checkIfColumnPreventsUiSortForTable(IColumn)}.
   */
  boolean isUiSortPossible();

  void setUiSortPossible(boolean uiSortPossible);

  TableUserFilterManager getUserFilterManager();

  void setUserFilterManager(TableUserFilterManager manager);

  ITableCustomizer getTableCustomizer();

  void setTableCustomizer(ITableCustomizer c);

  /**
   * Container of this table, {@link org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage IPage},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField ITableField},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox IListBox},
   * {@link org.eclipse.scout.rt.client.ui.form.fields.plannerfieldold.IPlannerFieldOld IPlannerField}
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
   * initialize column background effects
   */
  void resetColumnBackgroundEffects();

  /**
   * initialize column order
   */
  void resetColumnOrder();

  /**
   * initialize column filters
   */
  void resetColumnFilters();

  /**
   * reset all columns properties to initial state: <br>
   * visible/invisible, order, sorting, grouping, width, backgroundEffects
   */
  void resetColumns();

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
   * @since 3.10.0-M3
   */
  void exportToTableBeanData(AbstractTableFieldBeanData target);

  /**
   * Imports the contents of the given {@link AbstractTableFieldBeanData}. The mapping from {@link AbstractTableRowData}
   * properties to {@link IColumn}s is based on the property name and the {@link IColumn#getColumnId()}.
   *
   * @param source
   * @since 3.10.0-M3
   */
  void importFromTableBeanData(AbstractTableFieldBeanData source);

  /**
   * Creates a {@link TableRowDataMapper} that is used for reading and writing data from the given
   * {@link AbstractTableRowData} type.
   *
   * @param rowType
   * @return
   * @since 3.10.0-M5
   */
  ITableRowDataMapper createTableRowDataMapper(Class<? extends AbstractTableRowData> rowType);

  /**
   * @param menus
   */
  void setMenus(List<? extends IMenu> menus);

  /**
   * @param menu
   */
  void addMenu(IMenu menu);

  @Override
  ITableContextMenu getContextMenu();

  /**
   * Creates an empty table row. The created row is not added to the table yet.
   *
   * @return the created table row
   */
  ITableRow createRow();

  /**
   * Creates table rows for the given matrix of row values. The created rows are not added to the table yet. One row is
   * created for each row in the matrix. The row state of each created row is set to {@link ITableRow#STATUS_INSERTED}.
   * <p>
   * Performance note:<br>
   * Since the matrix may contain large amounts of data, the matrix can be passed as an
   * <code>new AtomicReference&lt;Object&gt;</code>(Object[][]) so that the further processing can set the content of
   * the holder to null while processing.
   * </p>
   *
   * @param dataMatrixOrReference
   *          Can be an Object[][] or an <code>AtomicReference&lt;Object&gt;</code>(that holds Object[][])
   * @return the list of the created table rows
   */
  List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference);

  /**
   * Creates table rows from the given matrix of row values. The created rows are not added to the table yet. One row is
   * created for each row in the matrix. The row state of each created row is set according to the
   * <code>rowStatus</code> parameter.
   * <p>
   * Performance note:<br>
   * Since the matrix may contain large amounts of data, the matrix can be passed as an
   * <code>new AtomicReference&lt;Object&gt;</code>(Object[][]) so that the further processing can set the content of
   * the holder to null while processing.
   * </p>
   *
   * @param dataMatrixOrReference
   * @param rowStatus
   *          The row status to be set for each created table row
   * @return the list of the created table rows
   */
  List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference, int rowStatus);

  /**
   * Creates table rows from the codes. The created rows are not added to the table yet.
   *
   * @param codes
   * @return the list of the created table rows
   */
  List<ITableRow> createRowsByCodes(Collection<? extends ICode<?>> codes);

  /**
   * Creates a new table row from the <code>rowValues</code> argument. The created row is not added to the table yet.
   *
   * @param rowValues
   *          The values to be filled into the new table row. Must be an array.
   * @return the created table row
   */
  ITableRow createRow(Object rowValues);

  /**
   * Creates new table rows from the given (one dimensional) array. The created rows are not added to the table yet.
   *
   * @param dataArray
   *          The values to be filled into the new table rows.
   * @return the list of the created table rows
   */
  List<ITableRow> createRowsByArray(Object dataArray);

  /**
   * Creates new table rows from the given (one dimensional) array. The created rows are not added to the table yet. The
   * row state of each created row is set according to the <code>rowStatus</code> parameter.
   *
   * @param dataArray
   *          The values to be filled into the new table rows.
   * @param rowStatus
   *          The row status to be set for each created table row
   * @return the list of the created table rows
   */
  List<ITableRow> createRowsByArray(Object dataArray, int rowStatus);

  /**
   * @since 5.1.0
   */
  List<ITableControl> getTableControls();

  /**
   * @since 5.1.0
   */
  <T extends ITableControl> T getTableControl(Class<T> controlClass);

  /**
   * @since 5.1.0
   */
  void addTableControl(ITableControl control);

  /**
   * @since 5.1.0
   */
  void addTableControl(int index, ITableControl control);

  /**
   * @since 5.1.0
   */
  void removeTableControl(ITableControl control);

  /**
   * @since 5.1.0
   */
  boolean isTableStatusVisible();

  /**
   * @since 5.1.0
   */
  void setTableStatusVisible(boolean visible);

  /**
   * @return the data fetching/loading status, warnings and other general messages related with data currently loaded
   *         into this table
   * @since 5.1.0
   */
  IStatus getTableStatus();

  /**
   * set the data loading status on the table
   * <p>
   * this is normally displayed in a status bar on the bottom of the table gui
   *
   * @since 5.1.0
   */
  void setTableStatus(IStatus status);

  /**
   * The reload handler is triggered when the user uses the ui tools to reload the table (reload button, reload
   * keystroke).
   * <p>
   * The existence of a reload handler controls the availability of these tools, meaning if no reload handler is set the
   * user may not reload the table.
   * <p>
   * Default is null.
   *
   * @since 5.1.0
   */
  IReloadHandler getReloadHandler();

  /**
   * @since 5.1.0
   */
  void setReloadHandler(IReloadHandler reloadHandler);

  void ensureInvalidColumnsVisible();

  /**
   * Corresponds to {@link IFormField#isValueChangeTriggerEnabled()}
   *
   * @since 5.2.0
   */
  boolean isValueChangeTriggerEnabled();

  /**
   * Corresponds to {@link IFormField#setValueChangeTriggerEnabled(boolean)}
   *
   * @since 5.2.0
   */
  void setValueChangeTriggerEnabled(boolean b);

  /**
   * @since 5.2.0
   */
  ITableOrganizer getTableOrganizer();

  /**
   * @since 5.2.0
   * @return True if the table has a table-customizer and the {@link CreateCustomColumnPermission}, false otherwise.
   */
  boolean isCustomizable();

  void setLoading(boolean loading);

  boolean isLoading();

}
