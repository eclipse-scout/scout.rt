describe("DesktopTable", function() {
  var scout;

  beforeEach(function() {
    setFixtures(sandbox());
    scout = new Scout.Session($('#sandbox'), '1.1');
    jasmine.Ajax.installMock();
    jasmine.clock().install();
  });

  afterEach(function() {
    scout = null;
    jasmine.Ajax.uninstallMock();
    clearAjaxRequests();
    jasmine.clock().uninstall();
  });

  function createModel(id, columns, rows) {
    return {
      "table": {
        "id": id,
        "columns": columns,
        "rows": rows
      }
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

  function createDesktopTable(model) {
    return new Scout.DesktopTable(scout, model);
  }

  function selectRowsAndAssert(desktopTable, rowIds) {
    desktopTable.selectRowsByIds(rowIds);

    var $selectedRows = desktopTable._$tableDataScroll.find('.row-selected');
    expect($selectedRows.length).toBe(rowIds.length);

    var selectedRowIds = [];
    $selectedRows.each(function() {
      selectedRowIds.push($(this).attr('id'));
    });

    expect(arrays.equalsIgnoreOrder(rowIds, selectedRowIds)).toBeTruthy();
  }

  describe("render", function() {

    it("draws a table header", function() {
      var model = createModelFixture(2);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      expect(desktopTable._tableHeader).not.toBeUndefined();
    });

  });

  describe("insertRows", function() {

    it("inserts rows at the end of the table", function() {
      var model = createModelFixture(2);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      expect(desktopTable._$tableDataScroll.children().length).toBe(0);

      var rows = createModelRows(2, 5);
      desktopTable.insertRows(rows);

      expect(desktopTable._$tableDataScroll.children().length).toBe(5);

      rows = createModelRows(2, 8);
      desktopTable.insertRows(rows);

      expect(desktopTable._$tableDataScroll.children().length).toBe(5+8);
    });

  });

  describe("selectRowsByIds", function() {

    it("selects rows and unselects others", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $selectedRows = desktopTable._$tableDataScroll.find('.row-selected');
      expect($selectedRows.length).toBe(0);

      selectRowsAndAssert(desktopTable, ['0', '4']);
      selectRowsAndAssert(desktopTable, ['2']);
    });

    it("sends selection event containing rowIds", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var rowIds = ['0', '4'];
      desktopTable.selectRowsByIds(rowIds);

      jasmine.clock().tick(0);

      expect(ajaxRequests.length).toBe(1);

      var event = new Scout.Event(Scout.DesktopTable.EVENT_ROWS_SELECTED, '1', {
        "rowIds": rowIds
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updates cached model", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var rowIds = ['0', '4'];
      desktopTable.selectRowsByIds(rowIds);

      expect(desktopTable.model.table.selectedRowIds).toEqual(rowIds);
    });

  });

  describe("row click", function() {

    function clickRowAndAssertSelection(desktopTable, $row) {
      $row.mousedown();
      $row.mouseup();
      $row.click();
      jasmine.clock().tick($.DOUBLE_CLICK_DELAY_TIME+1);

      var $selectedRows = desktopTable._$tableDataScroll.find('.row-selected');
      expect($selectedRows.length).toBe(1);

      var $selectedRow = $selectedRows.first();
      expect($selectedRow).toEqual($row);
    }

    it("selects row and unselects others", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $selectedRows = desktopTable._$tableDataScroll.find('.row-selected');
      expect($selectedRows.length).toBe(0);

      var $rows = desktopTable._$tableDataScroll.children();
      clickRowAndAssertSelection(desktopTable, $rows.eq(1));
      clickRowAndAssertSelection(desktopTable, $rows.eq(2));

      selectRowsAndAssert(desktopTable, ['0', '4']);
      clickRowAndAssertSelection(desktopTable, $rows.eq(4));
    });

    it("sends click and selection events", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $row = desktopTable._$tableDataScroll.children().first();
      $row.click();

      jasmine.clock().tick($.DOUBLE_CLICK_DELAY_TIME+1);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([Scout.DesktopTable.EVENT_ROW_CLICKED, Scout.DesktopTable.EVENT_ROWS_SELECTED]);
    });

    it("sends only click if row already is selected", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $row = desktopTable._$tableDataScroll.children().first();
      clickRowAndAssertSelection(desktopTable, $row);
      jasmine.clock().tick(0);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([Scout.DesktopTable.EVENT_ROW_CLICKED, Scout.DesktopTable.EVENT_ROWS_SELECTED]);

      clearAjaxRequests();
      clickRowAndAssertSelection(desktopTable, $row);
      jasmine.clock().tick(0);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([Scout.DesktopTable.EVENT_ROW_CLICKED]);
    });

  });

  describe("row double click", function() {
    it("sends selection and row action events", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $row = desktopTable._$tableDataScroll.children().first();
      $row.click();
      $row.click();

      jasmine.clock().tick(0);

      expect(mostRecentJsonRequest()).toContainEventTypesExactly([Scout.DesktopTable.EVENT_ROWS_SELECTED, Scout.DesktopTable.EVENT_ROW_ACTION]);
    });
  });

  describe("row mouse down / move / up", function() {

    it("selects multiple rows", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $rows = desktopTable._$tableDataScroll.children();
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
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      var $rows = desktopTable._$tableDataScroll.children();
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
      expect(requestData).toContainEventTypesExactly(Scout.DesktopTable.EVENT_ROWS_SELECTED);

      var event = new Scout.Event(Scout.DesktopTable.EVENT_ROWS_SELECTED, '1', {
        "rowIds": ['0', '1', '2']
      });
      expect(requestData).toContainEvents(event);
    });

  });

  describe("onModelAction", function() {

    it("processes insertion events from model", function() {
      var model = createModelFixture(2);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      spyOn(desktopTable, 'insertRows');

      var rows = createModelRows(2, 5);
      var event = new Scout.Event(Scout.DesktopTable.EVENT_ROWS_INSERTED, '1', {
        "rows": rows
      });
      desktopTable.onModelAction(event);

      expect(desktopTable.insertRows).toHaveBeenCalledWith(rows);
    });

    it("processes selection events from model", function() {
      var model = createModelFixture(2, 5);
      var desktopTable = createDesktopTable(model);
      desktopTable.render(scout.$entryPoint);

      spyOn(desktopTable, 'selectRowsByIds');

      var rowIds = ['0', '4'];
      var event = new Scout.Event(Scout.DesktopTable.EVENT_ROWS_SELECTED, '1', {
        "rowIds": rowIds
      });
      desktopTable.onModelAction(event);

      expect(desktopTable.selectRowsByIds).toHaveBeenCalledWith(rowIds);
    });

  });

});
