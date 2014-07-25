/* global MenuSpecHelper */
var TableSpecHelper = function(session) {
  this.session = session;
  this.menuHelper = new MenuSpecHelper(session);
};

TableSpecHelper.prototype.createModel = function(id, columns, rows) {
  if (id === undefined) {
    id = createUniqueAdapterId();
  }

  return {
    "id": id,
    "columns": columns,
    "rows": rows,
    "headerVisible": true
  };
};

TableSpecHelper.prototype.createModelRow = function(id, cells) {
  return {
    "id": id,
    "cells": cells
  };
};

TableSpecHelper.prototype.createModelColumn = function(id, text) {
  return {
    "id": id,
    "text": text
  };
};

TableSpecHelper.prototype.createModelCell = function(text) {
  return {
    "text": text
  };
};

TableSpecHelper.prototype.createMenuModel = function(id, text, icon) {
  return this.menuHelper.createModel(id, text, icon, ['SingleSelection']);
};

TableSpecHelper.prototype.createModelColumns = function(count) {
  if (!count) {
    return;
  }

  var columns = [];
  for (var i = 0; i < count; i++) {
    columns[i] = this.createModelColumn(i+'', 'col' + i);
  }
  return columns;
};

TableSpecHelper.prototype.createModelCells = function(count) {
  var cells = [];
  for (var i = 0; i < count; i++) {
    cells[i] = this.createModelCell(i+'', 'cell' + i);
  }
  return cells;
};

TableSpecHelper.prototype.createModelRows = function(colCount, rowCount) {
  if (!rowCount) {
    return;
  }

  var rows = [];
  for (var i = 0; i < rowCount; i++) {
    rows[i] = this.createModelRow(i+'', this.createModelCells(colCount));
  }
  return rows;
};

TableSpecHelper.prototype.createModelFixture = function(colCount, rowCount) {
  return this.createModel(createUniqueAdapterId(), this.createModelColumns(colCount), this.createModelRows(colCount, rowCount));
};

TableSpecHelper.prototype.createTable = function(model) {
  var table = new scout.Table(model, this.session);
  table.init(model, this.session);
  this.session.registerModelAdapter(table); //FIXME CGU remove after moving to constructor
  return table;
};

TableSpecHelper.prototype.createMobileTable = function(model) {
  var table = new scout.MobileTable(model, this.session);
  table.init(model, this.session);
  this.session.registerModelAdapter(table); //FIXME CGU remove after moving to constructor
  return table;
};

TableSpecHelper.prototype.getRowIds = function(rows) {
  var rowIds = [];
  for (var i = 0; i < rows.length; i++) {
    rowIds.push(rows[i].id);
  }
  return rowIds;
};

TableSpecHelper.prototype.selectRowsAndAssert = function(table, rowIds) {
  table.selectRowsByIds(rowIds);
  this.assertSelection(table, rowIds);
};

TableSpecHelper.prototype.assertSelection = function(table, rowIds) {
  var $selectedRows = table.findSelectedRows();
  expect($selectedRows.length).toBe(rowIds.length);

  var selectedRowIds = [];
  $selectedRows.each(function() {
    selectedRowIds.push($(this).attr('id'));
  });

  expect(scout.arrays.equalsIgnoreOrder(rowIds, selectedRowIds)).toBeTruthy();
  expect(scout.arrays.equalsIgnoreOrder(rowIds, table.selectedRowIds)).toBeTruthy();
};

TableSpecHelper.prototype.assertSelectionEvent = function(id, rowIds) {
  var event = new scout.Event(scout.Table.EVENT_ROWS_SELECTED, id, {
    "rowIds": rowIds
  });
  expect(mostRecentJsonRequest()).toContainEvents(event);
};

TableSpecHelper.prototype.getDisplayingContextMenu = function(table) {
  return table.$data.find('.menu-container');
};
