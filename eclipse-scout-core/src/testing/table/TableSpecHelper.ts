/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Cell, CellModel, ChildModelOf, Column, ColumnModel, comparators, DecimalFormat, Filter, InitModelOf, MenuModel, ModelAdapter, NumberColumnModel, ObjectOrChildModel, ObjectOrModel, objects, ObjectType, ObjectUuidProvider,
  Primitive, RemoteEvent, scout, Session, Table, TableModel, TableRow, TableRowModel, TableTextUserFilter, TextColumnUserFilter, Widget
} from '../../index';
import {MenuSpecHelper, SpecTable, SpecTableAdapter} from '../index';
import $ from 'jquery';

export class TableSpecHelper {
  session: Session;
  menuHelper: MenuSpecHelper;

  constructor(session: Session) {
    this.session = session;
    this.menuHelper = new MenuSpecHelper(session);
  }

  createModel(columns: ObjectOrChildModel<Column<any>>[], rows: ObjectOrModel<TableRow>[]): TableModelWithCells {
    let model = createSimpleModel('Table', this.session) as TableModelWithCells;

    // Server will never send undefined -> don't create model with undefined properties.
    if (rows) {
      model.rows = rows;
    }
    if (columns) {
      model.columns = columns;
    }

    return model;
  }

  createModelRow(id?: string, cells?: (Primitive | object | Cell)[], parentRow?: TableRow | string): TableRowModel {
    return {
      id: id || ObjectUuidProvider.createUiId(),
      cells: cells,
      parentRow: parentRow
    };
  }

  /**
   *
   * @param texts array of texts for the cells in the new row or a string if only one cell should be created.
   * @param withoutCells true if only text instead of cells should be created (server only sends text without a cell object if no other properties are set)
   */
  createModelRowByTexts(id: string, texts: string[] | string, withoutCells?: boolean): TableRowModel {
    texts = arrays.ensure(texts);

    let cells = [];
    for (let i = 0; i < texts.length; i++) {
      if (!withoutCells) {
        cells[i] = this.createModelCell(texts[i]);
      } else {
        cells[i] = texts[i];
      }
    }
    return this.createModelRow(id, cells);
  }

  /**
   *
   * @param values array of values for the cells in the new row or a number if only one cell should be created.
   */
  createModelRowByValues(id: string, values: any | any[]): TableRowModel {
    values = arrays.ensure(values);
    let cells: Cell[] = [];
    for (let i = 0; i < values.length; i++) {
      cells[i] = this.createModelCell(null, values[i]);
    }
    return this.createModelRow(id, cells);
  }

  createModelColumn<T>(text: string, type?: ObjectType<Column<T>>): ChildModelOf<Column<T>> & { uiSortPossible: boolean } {
    let model = {
      id: ObjectUuidProvider.createUiId(),
      text: text,
      objectType: (type === undefined ? 'Column' : type),
      uiSortPossible: true
    };
    if (type === 'NumberColumn') {
      (model as NumberColumnModel).decimalFormat = new DecimalFormat(this.session.locale);
    }
    return model;
  }

  createModelCell(text?: string, value?: any): Cell {
    let cell = {} as CellModel;
    if (text !== undefined) {
      cell.text = text;
    }
    if (value !== undefined) {
      cell.value = value;
    }
    return scout.create(Cell, cell);
  }

  createMenuModel(text?: string, icon?: string): MenuModel {
    return this.menuHelper.createModel(text, icon, [Table.MenuType.SingleSelection]);
  }

  createMenuModelWithSingleAndHeader(text: string, icon?: string): MenuModel {
    return this.menuHelper.createModel(text, icon, [Table.MenuType.SingleSelection, Table.MenuType.Header]);
  }

  createModelColumns(count: number, columnType?: ObjectType): ChildModelOf<Column<any>>[] {
    if (!count) {
      return;
    }

    if (!columnType) {
      columnType = 'Column';
    }

    let columns: ChildModelOf<Column<any>>[] = [],
      columnTypes = [];
    if (objects.isArray(columnType)) {
      if (columnType.length !== count) {
        throw new Error('Column count(' + count + ') does not match with columnType.length (' + columnType.length + ').');
      }
      columnTypes = columnType;
    } else {
      for (let i = 0; i < count; i++) {
        columnTypes.push(columnType);
      }
    }
    for (let j = 0; j < count; j++) {
      columns[j] = this.createModelColumn('col' + j, columnTypes[j]);
    }
    return columns;
  }

  /**
   * Creates cells with values.
   *
   * If the column is of type NumberColumn a numeric value is set.
   * Otherwise, the value is similar to 'cell0_0' if rowId is given, or 'cell0' if no rowId is given.
   */
  createModelCells(columns: ColumnModel[] | number, rowId?: string): Cell[] {
    let cells: Cell[] = [];
    if (rowId === undefined) {
      rowId = '';
    }

    if (typeof columns === 'number') {
      for (let i = 0; i < columns; i++) {
        cells[i] = this.createModelCell(rowId + '_' + i, 'cell' + rowId + '_' + i);
      }
    } else {
      for (let j = 0; j < columns.length; j++) {
        let value = 'cell' + rowId + j;
        if (columns[j].objectType === 'NumberColumn') {
          value = rowId + j;
        }
        cells[j] = this.createModelCell(rowId + '_' + j, value);
      }
    }
    return cells;
  }

  /**
   * Creates #rowCount rows where columns are either the column count or the column objects.
   * Passing the column objects allows to consider the column type for cell creation.
   */
  createModelRows(columns: number | ColumnModel<any>[], rowCount: number, parentRow?: TableRow | string): TableRowModelWithCells[] {
    if (!rowCount) {
      return;
    }

    let rows = [];
    for (let i = 0; i < rowCount; i++) {
      rows[i] = this.createModelRow(null, this.createModelCells(columns, i + ''), parentRow);
    }
    return rows;
  }

  createModelSingleColumnByTexts(texts: string[]): TableModelWithCells {
    let rows = [];
    for (let i = 0; i < texts.length; i++) {
      rows.push(this.createModelRowByTexts(null, texts[i]));
    }
    return this.createModel(this.createModelColumns(1), rows);
  }

  createModelSingleColumnByValues(values: any[], columnType: ObjectType<Column>): TableModelWithCells {
    let rows = [];
    for (let i = 0; i < values.length; i++) {
      rows.push(this.createModelRowByValues(null, values[i]));
    }
    return this.createModel(this.createModelColumns(1, columnType), rows);
  }

  createModelFixture(colCount: number, rowCount?: number): TableModelWithCells {
    return this.createModel(this.createModelColumns(colCount), this.createModelRows(colCount, rowCount));
  }

  createTableWithOneColumn(): Table {
    let model = this.createModelFixture(1, 1);
    return this.createTable(model);
  }

  createModelSingleConfiguredCheckableColumn(rowCount: number): TableModelWithCells {
    let cols = this.createModelColumns(1);
    cols[0].checkable = true;
    return this.createModel(cols, this.createModelRows(1, rowCount));
  }

  createTable(model: TableModel): SpecTable {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create(SpecTable, model as InitModelOf<Table>);
  }

  createTableAdapter(model: InitModelOf<ModelAdapter> | TableModel & { session: Session; id: string }): SpecTableAdapter {
    let tableAdapter = new SpecTableAdapter();
    tableAdapter.init(model as InitModelOf<SpecTableAdapter>);
    return tableAdapter;
  }

  createColumnFilter(model: InitModelOf<TextColumnUserFilter>): TextColumnUserFilter {
    let filter = new TextColumnUserFilter();
    filter.init(model);
    return filter;
  }

  createAndRegisterColumnFilter(model: InitModelOf<TextColumnUserFilter>): TextColumnUserFilter {
    let filter = this.createColumnFilter(model);
    model.table.addFilter(filter);
    return filter;
  }

  createTableTextFilter(table: Table, text: string): TableTextUserFilter {
    return scout.create(TableTextUserFilter, {
      session: this.session,
      table: table,
      text: text
    });
  }

  createTextColumnFilter(table: Table, column: Column, text: string): TextColumnUserFilter {
    return scout.create(TextColumnUserFilter, {
      session: this.session,
      table: table,
      column: column,
      freeText: text
    });
  }

  createColumnStructureChangedEvent(model: { id: string }, columns: ColumnModel[]): RemoteEvent {
    return {
      target: model.id,
      columns: columns,
      type: 'columnStructureChanged'
    };
  }

  createRowsInsertedEvent(model: { id: string }, rows: TableRowModel[]): RemoteEvent {
    return {
      target: model.id,
      rows: rows,
      type: 'rowsInserted'
    };
  }

  createAllRowsDeletedEvent(model: { id: string }): RemoteEvent {
    return {
      target: model.id,
      type: 'allRowsDeleted'
    };
  }

  createFiltersChangedEvent(model: { id: string }, filters: Filter<TableRow>[]): RemoteEvent {
    return {
      target: model.id,
      filters: filters,
      type: 'filtersChanged'
    };
  }

  /**
   * Applies display style on rows and cells so that cells are positioned correctly in a row.<br>
   * Necessary because the stylesheet is not applied when running the specs.
   */
  applyDisplayStyle(table: Table) {
    table.$data.css('position', 'relative');
    table.$rows().each(function() {
      let $row = $(this);
      $row.css('display', 'table-row');
      $row.children('.table-cell').each(function() {
        let $cell = $(this);
        $cell.css('display', 'table-cell');
      });
    });
  }

  getRowIds(rows: ObjectOrModel<TableRow>[]): string[] {
    let rowIds = [];
    for (let i = 0; i < rows.length; i++) {
      rowIds.push(rows[i].id);
    }
    return rowIds;
  }

  selectRowsAndAssert(table: Table, rows: TableRow[]) {
    table.selectRows(rows);
    this.assertSelection(table, rows);
  }

  assertSelection(table: Table, rows: TableRow[]) {
    let $selectedRows = table.$selectedRows();
    expect($selectedRows.length).toBe(rows.length);

    let selectedRows = [];
    $selectedRows.each(function() {
      selectedRows.push($(this).data('row'));

      if ($selectedRows.length === 1) {
        expect($(this).hasClass('select-single')).toBeTruthy();
      }
    });

    expect(arrays.equalsIgnoreOrder(rows, selectedRows)).toBeTruthy();
    expect(arrays.equalsIgnoreOrder(rows, table.selectedRows)).toBeTruthy();
  }

  /**
   * Asserts that the rows contain the given texts at column specified by colIndex
   * @param texts array with same length as rows.
   */
  assertTextsInCells(rows: TableRow[], colIndex: number, texts: string[]) {
    expect(rows.length).toBe(texts.length);
    for (let i = 0; i < rows.length; i++) {
      expect(rows[i].cells[colIndex].text).toBe(texts[i]);
    }
  }

  assertValuesInCells(rows: TableRow[], colIndex: number, values: any[]) {
    expect(rows.length).toBe(values.length);
    for (let i = 0; i < rows.length; i++) {
      expect(rows[i].cells[colIndex].value).toBe(values[i]);
    }
  }

  assertDatesInCells(rows: TableRow[], colIndex: number, dates: Date[]) {
    expect(rows.length).toBe(dates.length);
    for (let i = 0; i < rows.length; i++) {
      expect(rows[i].cells[colIndex].value.getTime()).toBe(dates[i].getTime());
    }
  }

  assertSelectionEvent(id: string, rowIds: string[]) {
    let event = new RemoteEvent(id, 'rowsSelected', {
      rowIds: rowIds
    });
    expect(mostRecentJsonRequest()).toContainEvents(event);
  }

  getDisplayingContextMenu(table: Table): JQuery {
    return $('body').find('.context-menu');
  }

  /**
   * Since scout.comparators.TEXT is a static object and only installed once,
   * we must reset the object - otherwise we could not test cases with client
   * and server side sorting.
   */
  resetIntlCollator() {
    comparators.TEXT.installed = false;
    comparators.TEXT.collator = null;
  }
}

export type TableRowModelWithCells = TableRowModel & { cells: Cell[] };
export type TableModelWithCells = TableModel & { id: string; parent: Widget; session: Session; objectType: ObjectType<Table> };
