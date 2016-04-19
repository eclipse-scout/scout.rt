/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global MenuSpecHelper */
var TableSpecHelper = function(session) {
  this.session = session;
  this.menuHelper = new MenuSpecHelper(session);
};

TableSpecHelper.prototype.createModel = function(columns, rows) {
  var model = createSimpleModel('Table', this.session);
  $.extend(model, {
    headerVisible: true,
    multiSelect: true,
    uiSortPossible: true
  });

  //Server will never send undefined -> don't create model with undefined properties.
  if (rows) {
    model.rows = rows;
  }
  if (columns) {
    model.columns = columns;
  }

  return model;
};

TableSpecHelper.prototype.createModelRow = function(id, cells) {
  return {
    id: id || scout.objectFactory.createUniqueId(),
    cells: cells,
    enabled: true
  };
};

/**
 *
 * @param texts array of texts for the cells in the new row or a string if only one cell should be created.
 * @param withoutCells true if only text instead of cells should be created (server only sends text without a cell object if no other properties are set)
 */
TableSpecHelper.prototype.createModelRowByTexts = function(id, texts, withoutCells) {
  texts = scout.arrays.ensure(texts);

  var cells = [];
  for (var i = 0; i < texts.length; i++) {
    if (!withoutCells) {
      cells[i] = this.createModelCell(texts[i]);
    } else {
      cells[i] = texts[i];
    }
  }
  return this.createModelRow(id, cells);
};

/**
 *
 * @param values array of values for the cells in the new row or a number if only one cell should be created.
 */
TableSpecHelper.prototype.createModelRowByValues = function(id, values) {
  values = scout.arrays.ensure(values);
  var cells = [];
  for (var i = 0; i < values.length; i++) {
    cells[i] = this.createModelCell(values[i] + '', values[i]);
  }
  return this.createModelRow(id, cells);
};

TableSpecHelper.prototype.createModelColumn = function(text, type) {
  var column = {
    id: scout.objectFactory.createUniqueId(),
    text: text,
    objectType: (type === undefined ? 'Column' : type),
    decimalFormat: (type === 'NumberColumn' ? new scout.DecimalFormat(this.session.locale) : undefined),
    uiSortPossible: true
  };
  scout.defaultValues.applyTo(column, 'Column');
  return column;
};

TableSpecHelper.prototype.createModelCell = function(text, value) {
  var cell = {};
  scout.defaultValues.applyTo(cell, 'Cell');
  if (text !== undefined) {
    cell.text = text;
  }
  if (value !== undefined) {
    cell.value = value;
  }
  return cell;
};

TableSpecHelper.prototype.createMenuModel = function(text, icon) {
  return this.menuHelper.createModel(text, icon, ['Table.SingleSelection']);
};

TableSpecHelper.prototype.createMenuModelWithSingleAndHeader = function(text, icon) {
  return this.menuHelper.createModel(text, icon, ['Table.SingleSelection', 'Table.Header']);
};

TableSpecHelper.prototype.createModelColumns = function(count, columnType) {
  if (!count) {
    return;
  }
  if (!columnType) {
    columnType = 'Column';
  }

  var columns = [];
  for (var i = 0; i < count; i++) {
    columns[i] = this.createModelColumn('col' + i, columnType);
    columns[i].index = i;
  }
  return columns;
};

/**
 * creates cells with value similar to 'cell0_0' if rowId is given, or 'cell0' if no rowId is given
 */
TableSpecHelper.prototype.createModelCells = function(count, rowId) {
  var cells = [];
  if (rowId === undefined) {
    rowId = '';
  } else {
    rowId = rowId + '_';
  }
  for (var i = 0; i < count; i++) {
    cells[i] = this.createModelCell(i + '', 'cell' + rowId + i);
  }
  return cells;
};

TableSpecHelper.prototype.createModelRows = function(colCount, rowCount) {
  if (!rowCount) {
    return;
  }

  var rows = [];
  for (var i = 0; i < rowCount; i++) {
    rows[i] = this.createModelRow(null, this.createModelCells(colCount, i));
  }
  return rows;
};

TableSpecHelper.prototype.createModelSingleColumnByTexts = function(texts) {
  var rows = [];
  for (var i = 0; i < texts.length; i++) {
    rows.push(this.createModelRowByTexts(null, texts[i]));
  }
  return this.createModel(this.createModelColumns(1), rows);
};

TableSpecHelper.prototype.createModelSingleColumnByValues = function(values, columnType) {
  var rows = [];
  for (var i = 0; i < values.length; i++) {
    rows.push(this.createModelRowByValues(null, values[i]));
  }
  return this.createModel(this.createModelColumns(1, columnType), rows);
};

TableSpecHelper.prototype.createModelFixture = function(colCount, rowCount) {
  return this.createModel(this.createModelColumns(colCount), this.createModelRows(colCount, rowCount));
};

TableSpecHelper.prototype.createTable = function(model) {
  var table = new scout.Table();
  table.init(model);
  return table;
};

TableSpecHelper.prototype.createColumnFilter = function(model) {
  var filter = new scout.TextColumnUserFilter();
  filter.init(model);
  return filter;
};

TableSpecHelper.prototype.createAndRegisterColumnFilter = function(model) {
  var filter = this.createColumnFilter(model);
  model.table.addFilter(filter);
  return filter;
};

/**
 * Applies display style on rows and cells so that cells are positioned correctly in a row.<br>
 * Necessary because the stylesheet is not applied when running the specs.
 */
TableSpecHelper.prototype.applyDisplayStyle = function(table) {
  table.$data.css('position', 'relative');
  table.$rows().each(function() {
    var $row = $(this);
    $row.css('display', 'table-row');
    $row.children('.table-cell').each(function() {
      var $cell = $(this);
      $cell.css('display', 'table-cell');
    });
  });
};

TableSpecHelper.prototype.getRowIds = function(rows) {
  var rowIds = [];
  for (var i = 0; i < rows.length; i++) {
    rowIds.push(rows[i].id);
  }
  return rowIds;
};

TableSpecHelper.prototype.selectRowsAndAssert = function(table, rows) {
  table.selectRows(rows);
  this.assertSelection(table, rows);
};

TableSpecHelper.prototype.assertSelection = function(table, rows) {
  var $selectedRows = table.$selectedRows();
  expect($selectedRows.length).toBe(rows.length);

  var selectedRows = [];
  $selectedRows.each(function() {
    selectedRows.push($(this).data('row'));

    if ($selectedRows.length == 1) {
      expect($(this).hasClass('select-single')).toBeTruthy();
    }
  });

  expect(scout.arrays.equalsIgnoreOrder(rows, selectedRows)).toBeTruthy();
  expect(scout.arrays.equalsIgnoreOrder(rows, table.selectedRows)).toBeTruthy();
};

/**
 * Asserts that the rows contain the given texts at column specified by colIndex
 * @param texts array with same length as rows.
 */
TableSpecHelper.prototype.assertTextsInCells = function(rows, colIndex, texts) {
  expect(rows.length).toBe(texts.length);
  for (var i = 0; i < rows.length; i++) {
    expect(rows[i].cells[colIndex].text).toBe(texts[i]);
  }
};

TableSpecHelper.prototype.assertValuesInCells = function(rows, colIndex, values) {
  expect(rows.length).toBe(values.length);
  for (var i = 0; i < rows.length; i++) {
    expect(rows[i].cells[colIndex].value).toBe(values[i]);
  }
};

TableSpecHelper.prototype.assertDatesInCells = function(rows, colIndex, dates) {
  expect(rows.length).toBe(dates.length);
  for (var i = 0; i < rows.length; i++) {
    expect(rows[i].cells[colIndex].value.getTime()).toBe(dates[i].getTime());
  }
};

TableSpecHelper.prototype.assertSelectionEvent = function(id, rowIds) {
  var event = new scout.Event(id, 'rowsSelected', {
    rowIds: rowIds
  });
  expect(mostRecentJsonRequest()).toContainEvents(event);
};

TableSpecHelper.prototype.getDisplayingContextMenu = function(table) {
  return $('body').find('.popup-body');
};

/**
 * Since scout.comparators.TEXT is a static object and only installed once,
 * we must reset the object - otherwise we could not test cases with client
 * and server side sorting.
 */
TableSpecHelper.prototype.resetIntlCollator = function() {
  scout.comparators.TEXT.installed = false;
  scout.comparators.TEXT.collator = null;
};
