var TableSpecHelper = function(session) {
  this.session = session;
};

TableSpecHelper.prototype.createModel = function(id, columns, rows) {
  return {
    "id": id,
    "columns": columns,
    "rows": rows
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

TableSpecHelper.prototype.createMenu = function(id, text, icon) {
  return {
    "id": id,
    "text": text,
    "iconId": icon,
    "objectType": "Menu"
  };
};

TableSpecHelper.prototype.createModelColumns = function(count) {
  if (!count) {
    return;
  }

  var columns = [];
  for (var i = 0; i < count; i++) {
    columns[i] = this.createModelColumn(i, 'col' + i);
  }
  return columns;
};

TableSpecHelper.prototype.createModelCells = function(count) {
  var cells = [];
  for (var i = 0; i < count; i++) {
    cells[i] = this.createModelCell(i, 'cell' + i);
  }
  return cells;
};

TableSpecHelper.prototype.createModelRows = function(colCount, rowCount) {
  if (!rowCount) {
    return;
  }

  var rows = [];
  for (var i = 0; i < rowCount; i++) {
    rows[i] = this.createModelRow(i, this.createModelCells(colCount));
  }
  return rows;
},

TableSpecHelper.prototype.createModelFixture = function(colCount, rowCount) {
  return this.createModel('1', this.createModelColumns(colCount), this.createModelRows(colCount, rowCount));
};

TableSpecHelper.prototype.createTable = function(model) {
  return new scout.Table(this.session, model);
};

TableSpecHelper.prototype.createMobileTable = function(model) {
  return new scout.MobileTable(this.session, model);
};

TableSpecHelper.prototype.selectRowsAndAssert = function(table, rowIds) {
  table.selectRowsByIds(rowIds);

  var $selectedRows = table.findSelectedRows();
  expect($selectedRows.length).toBe(rowIds.length);

  var selectedRowIds = [];
  $selectedRows.each(function() {
    selectedRowIds.push($(this).attr('id'));
  });

  expect(scout.arrays.equalsIgnoreOrder(rowIds, selectedRowIds)).toBeTruthy();
};

TableSpecHelper.prototype.getDisplayingRowMenu = function(table) {
  return table.$data.find('#RowMenu');
};
