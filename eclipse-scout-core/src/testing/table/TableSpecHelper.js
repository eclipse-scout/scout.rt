/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, comparators, DecimalFormat, ObjectFactory, objects, RemoteEvent, scout, Table, TableAdapter, TextColumnUserFilter} from '../../index';
import {MenuSpecHelper} from '../index';
import $ from 'jquery';

export default class TableSpecHelper {
  constructor(session) {
    this.session = session;
    this.menuHelper = new MenuSpecHelper(session);
  }

  createModel(columns, rows) {
    let model = createSimpleModel('Table', this.session);

    // Server will never send undefined -> don't create model with undefined properties.
    if (rows) {
      model.rows = rows;
    }
    if (columns) {
      model.columns = columns;
    }

    return model;
  }

  createModelRow(id, cells, parentRow) {
    return {
      id: scout.nvl(id, ObjectFactory.get().createUniqueId()),
      cells: cells,
      parentRow: parentRow
    };
  }

  /**
   *
   * @param texts array of texts for the cells in the new row or a string if only one cell should be created.
   * @param withoutCells true if only text instead of cells should be created (server only sends text without a cell object if no other properties are set)
   */
  createModelRowByTexts(id, texts, withoutCells) {
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
  createModelRowByValues(id, values) {
    values = arrays.ensure(values);
    let cells = [];
    for (let i = 0; i < values.length; i++) {
      cells[i] = this.createModelCell(null, values[i]);
    }
    return this.createModelRow(id, cells);
  }

  createModelColumn(text, type) {
    return {
      id: ObjectFactory.get().createUniqueId(),
      text: text,
      objectType: (type === undefined ? 'Column' : type),
      decimalFormat: (type === 'NumberColumn' ? new DecimalFormat(this.session.locale) : undefined),
      uiSortPossible: true
    };
  }

  createModelCell(text, value) {
    let cell = {};
    if (text !== undefined) {
      cell.text = text;
    }
    if (value !== undefined) {
      cell.value = value;
    }
    return scout.create('Cell', cell);
  }

  createMenuModel(text, icon) {
    return this.menuHelper.createModel(text, icon, ['Table.SingleSelection']);
  }

  createMenuModelWithSingleAndHeader(text, icon) {
    return this.menuHelper.createModel(text, icon, ['Table.SingleSelection', 'Table.Header']);
  }

  createModelColumns(count, columnType) {
    if (!count) {
      return;
    }

    if (!columnType) {
      columnType = 'Column';
    }

    let columns = [],
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
   * Otherwise the value is similar to 'cell0_0' if rowId is given, or 'cell0' if no rowId is given.
   */
  createModelCells(columns, rowId) {
    let cells = [];
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
   * Creates #rowCount rows where columns is either the column count or the column objects.
   * Passing the column objects allows to consider the column type for cell creation.
   */
  createModelRows(columns, rowCount, parentRow) {
    if (!rowCount) {
      return;
    }

    let rows = [];
    for (let i = 0; i < rowCount; i++) {
      rows[i] = this.createModelRow(null, this.createModelCells(columns, i), parentRow);
    }
    return rows;
  }

  createModelSingleColumnByTexts(texts) {
    let rows = [];
    for (let i = 0; i < texts.length; i++) {
      rows.push(this.createModelRowByTexts(null, texts[i]));
    }
    return this.createModel(this.createModelColumns(1), rows);
  }

  createModelSingleColumnByValues(values, columnType) {
    let rows = [];
    for (let i = 0; i < values.length; i++) {
      rows.push(this.createModelRowByValues(null, values[i]));
    }
    return this.createModel(this.createModelColumns(1, columnType), rows);
  }

  createModelFixture(colCount, rowCount) {
    return this.createModel(this.createModelColumns(colCount), this.createModelRows(colCount, rowCount));
  }

  createTableWithOneColumn() {
    let model = this.createModelFixture(1, 1);
    return this.createTable(model);
  }

  createModelSingleConfiguredCheckableColumn(rowCount) {
    let cols = this.createModelColumns(1);
    cols[0].checkable = true;
    return this.createModel(cols, this.createModelRows(1, rowCount));
  }

  createTable(model) {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    let table = new Table();
    table.init(model);
    return table;
  }

  createTableAdapter(model) {
    let tableAdapter = new TableAdapter();
    tableAdapter.init(model);
    return tableAdapter;
  }

  createColumnFilter(model) {
    let filter = new TextColumnUserFilter();
    filter.init(model);
    return filter;
  }

  createAndRegisterColumnFilter(model) {
    let filter = this.createColumnFilter(model);
    model.table.addFilter(filter);
    return filter;
  }


  createTableTextFilter(table, text) {
    return scout.create('TableTextUserFilter', {
      session: this.session,
      table: table,
      text: text
    });
  }

  createTextColumnFilter(table, column, text) {
    return scout.create('TextColumnUserFilter', {
      session: this.session,
      table: table,
      column: column,
      freeText: text
    });
  }

  createColumnStructureChangedEvent(model, columns) {
    return {
      target: model.id,
      columns: columns,
      type: 'columnStructureChanged'
    };
  }

  createRowsInsertedEvent(model, rows) {
    return {
      target: model.id,
      rows: rows,
      type: 'rowsInserted'
    };
  }

  createAllRowsDeletedEvent(model) {
    return {
      target: model.id,
      type: 'allRowsDeleted'
    };
  }

  createFiltersChangedEvent(model, filters) {
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
  applyDisplayStyle(table) {
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

  getRowIds(rows) {
    let rowIds = [];
    for (let i = 0; i < rows.length; i++) {
      rowIds.push(rows[i].id);
    }
    return rowIds;
  }

  selectRowsAndAssert(table, rows) {
    table.selectRows(rows);
    this.assertSelection(table, rows);
  }

  assertSelection(table, rows) {
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
  assertTextsInCells(rows, colIndex, texts) {
    expect(rows.length).toBe(texts.length);
    for (let i = 0; i < rows.length; i++) {
      expect(rows[i].cells[colIndex].text).toBe(texts[i]);
    }
  }

  assertValuesInCells(rows, colIndex, values) {
    expect(rows.length).toBe(values.length);
    for (let i = 0; i < rows.length; i++) {
      expect(rows[i].cells[colIndex].value).toBe(values[i]);
    }
  }

  assertDatesInCells(rows, colIndex, dates) {
    expect(rows.length).toBe(dates.length);
    for (let i = 0; i < rows.length; i++) {
      expect(rows[i].cells[colIndex].value.getTime()).toBe(dates[i].getTime());
    }
  }

  assertSelectionEvent(id, rowIds) {
    let event = new RemoteEvent(id, 'rowsSelected', {
      rowIds: rowIds
    });
    expect(mostRecentJsonRequest()).toContainEvents(event);
  }

  getDisplayingContextMenu(table) {
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
