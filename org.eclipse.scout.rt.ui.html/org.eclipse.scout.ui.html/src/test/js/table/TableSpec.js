describe("Table", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    jasmine.Ajax.installMock();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstallMock();
    clearAjaxRequests();
    jasmine.clock().uninstall();
  });

  function createModel(id, columns, rows) {
    return {
      "id": id,
      "columns": columns,
      "rows": rows
    };
  }

  function createModelRow(id, cells) {
    return {
      "id": id,
      "cells": cells
    };
  }

  function createModelColumn(id, text) {
    return {
      "id": id,
      "text": text
    };
  }

  function createModelCell(text) {
    return {
      "text": text
    };
  }

  function createModelColumns(count) {
    if (!count) {
      return;
    }

    var columns = [];
    for (var i = 0; i < count; i++) {
      columns[i] = createModelColumn(i, 'col' + i);
    }
    return columns;
  }

  function createModelCells(count) {
    var cells = [];
    for (var i = 0; i < count; i++) {
      cells[i] = createModelCell(i, 'cell' + i);
    }
    return cells;
  }

  function createModelRows(colCount, rowCount) {
    if (!rowCount) {
      return;
    }

    var rows = [];
    for (var i = 0; i < rowCount; i++) {
      rows[i] = createModelRow(i, createModelCells(colCount));
    }
    return rows;
  }

  function createModelFixture(colCount, rowCount) {
    return createModel('1', createModelColumns(colCount), createModelRows(colCount, rowCount));
  }

  function createTable(model) {
    return new scout.Table(session, model);
  }

  function selectRowsAndAssert(table, rowIds) {
    table.selectRowsByIds(rowIds);

    var $selectedRows = table.findSelectedRows();
    expect($selectedRows.length).toBe(rowIds.length);

    var selectedRowIds = [];
    $selectedRows.each(function() {
      selectedRowIds.push($(this).attr('id'));
    });

    expect(scout.arrays.equalsIgnoreOrder(rowIds, selectedRowIds)).toBeTruthy();
  }

  describe("attach", function() {

    it("draws a table header", function() {
      var model = createModelFixture(2);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      expect(table._header).not.toBeUndefined();
    });

  });

  describe("insertRows", function() {

    it("inserts rows at the end of the table", function() {
      var model = createModelFixture(2);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      expect(table._$dataScroll.children().length).toBe(0);

      var rows = createModelRows(2, 5);
      table.insertRows(rows);

      expect(table._$dataScroll.children().length).toBe(5);

      rows = createModelRows(2, 8);
      table.insertRows(rows);

      expect(table._$dataScroll.children().length).toBe(5 + 8);
    });

  });

  describe("selectRowsByIds", function() {

    it("selects rows and unselects others", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $selectedRows = table.findSelectedRows();
      expect($selectedRows.length).toBe(0);

      selectRowsAndAssert(table, ['0', '4']);
      selectRowsAndAssert(table, ['2']);
    });

    it("sends selection event containing rowIds", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var rowIds = ['0', '4'];
      table.selectRowsByIds(rowIds);

      jasmine.clock().tick(0);

      expect(ajaxRequests.length).toBe(1);

      var event = new scout.Event(scout.Table.EVENT_ROWS_SELECTED, '1', {
        "rowIds": rowIds
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updates cached model", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var rowIds = ['0', '4'];
      table.selectRowsByIds(rowIds);

      expect(table.model.selectedRowIds).toEqual(rowIds);
    });

  });

  describe("row click", function() {

    function clickRowAndAssertSelection(table, $row) {
      $row.mousedown();
      $row.mouseup();
      $row.click();
      jasmine.clock().tick($.DOUBLE_CLICK_DELAY_TIME + 1);

      var $selectedRows = table.findSelectedRows();
      expect($selectedRows.length).toBe(1);

      var $selectedRow = $selectedRows.first();
      expect($selectedRow).toEqual($row);
    }

    it("selects row and unselects others", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $selectedRows = table.findSelectedRows();
      expect($selectedRows.length).toBe(0);

      var $rows = table._$dataScroll.children();
      clickRowAndAssertSelection(table, $rows.eq(1));
      clickRowAndAssertSelection(table, $rows.eq(2));

      selectRowsAndAssert(table, ['0', '4']);
      clickRowAndAssertSelection(table, $rows.eq(4));
    });

    it("sends click and selection events", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $row = table._$dataScroll.children().first();
      $row.click();

      jasmine.clock().tick($.DOUBLE_CLICK_DELAY_TIME + 1);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([scout.Table.EVENT_ROW_CLICKED, scout.Table.EVENT_ROWS_SELECTED]);
    });

    it("sends only click if row already is selected", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $row = table._$dataScroll.children().first();
      clickRowAndAssertSelection(table, $row);
      jasmine.clock().tick(0);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([scout.Table.EVENT_ROW_CLICKED, scout.Table.EVENT_ROWS_SELECTED]);

      clearAjaxRequests();
      clickRowAndAssertSelection(table, $row);
      jasmine.clock().tick(0);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([scout.Table.EVENT_ROW_CLICKED]);
    });

  });

  describe("row double click", function() {
    it("sends selection and row action events", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $row = table._$dataScroll.children().first();
      $row.click();
      $row.click();

      jasmine.clock().tick(0);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([scout.Table.EVENT_ROWS_SELECTED, scout.Table.EVENT_ROW_ACTION]);
    });
  });

  describe("row mouse down / move / up", function() {

    it("selects multiple rows", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $rows = table._$dataScroll.children();
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);
      var $row4 = $rows.eq(4);

      expect($rows).not.toHaveClass('row-selected');

      $row0.mousedown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.mouseup();

      expect($row0).toHaveClass('row-selected');
      expect($row1).toHaveClass('row-selected');
      expect($row2).toHaveClass('row-selected');
      expect($row3).not.toHaveClass('row-selected');
      expect($row4).not.toHaveClass('row-selected');
    });

    it("only sends selection event, no click", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      var $rows = table._$dataScroll.children();
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);

      expect($rows).not.toHaveClass('row-selected');

      $row0.mousedown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.mouseup();

      jasmine.clock().tick(0);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(scout.Table.EVENT_ROWS_SELECTED);

      var event = new scout.Event(scout.Table.EVENT_ROWS_SELECTED, '1', {
        "rowIds": ['0', '1', '2']
      });
      expect(requestData).toContainEvents(event);
    });

  });

  describe("onModelAction", function() {

    it("processes insertion events from model", function() {
      var model = createModelFixture(2);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      spyOn(table, 'insertRows');

      var rows = createModelRows(2, 5);
      var event = new scout.Event(scout.Table.EVENT_ROWS_INSERTED, '1', {
        "rows": rows
      });
      table.onModelAction(event);

      expect(table.insertRows).toHaveBeenCalledWith(rows);
    });

    it("processes selection events from model", function() {
      var model = createModelFixture(2, 5);
      var table = createTable(model);
      table.attach(session.$entryPoint);

      spyOn(table, 'selectRowsByIds');

      var rowIds = ['0', '4'];
      var event = new scout.Event(scout.Table.EVENT_ROWS_SELECTED, '1', {
        "rowIds": rowIds
      });
      table.onModelAction(event);

      expect(table.selectRowsByIds).toHaveBeenCalledWith(rowIds);
    });

  });

});
