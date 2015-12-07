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
/* global TableSpecHelper, LocaleSpecHelper */
describe("Table", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("render", function() {

    it("renders a table header", function() {
      var model = helper.createModelFixture(2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      expect(table.header).not.toBeUndefined();
    });

    describe("renders table rows", function() {

      it("accepts rows with cells", function() {
        var model = helper.createModelFixture(3, 1);
        model.rows[0] = helper.createModelRowByTexts(1, ['cell1', '', '0']);
        var table = helper.createTable(model);
        table.render(session.$entryPoint);

        var $row0 = table.$rows().eq(0);
        var $cells = $row0.find('.table-cell');
        expect($cells.eq(0).text()).toBe('cell1');
        expect($cells.eq(1).html()).toBe('&nbsp;');
        expect($cells.eq(2).text()).toBe('0');
      });

      it("accepts rows with text only", function() {
        var model = helper.createModelFixture(3, 1);
        model.rows[0] = helper.createModelRowByTexts(1, ['cell1', '', '0'], true);
        var table = helper.createTable(model);
        table.render(session.$entryPoint);

        var $row0 = table.$rows().eq(0);
        var $cells = $row0.find('.table-cell');
        expect($cells.eq(0).text()).toBe('cell1');
        expect($cells.eq(1).html()).toBe('&nbsp;');
        expect($cells.eq(2).text()).toBe('0');
      });

    });

  });

  describe("checkRow", function() {

    function findCheckedRows(rows) {
      var checkedRows = [];
      for (var i = 0; i < rows.length; i++) {
        if (rows[i].checked) {
          checkedRows.push(rows[i]);
        }
      }
      return checkedRows;
    }

    it("checks the row, does not uncheck others if multiCheck is set to true", function() {
      var model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      table.checkRow(rows[4], true, true);

      checkedRows = checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(2);

      table.checkRow(rows[4], false, true);

      checkedRows = [];
      for (var z = 0; z < rows.length; z++) {
        if (rows[z].checked) {
          checkedRows.push(rows[z]);
        }
      }
      expect(checkedRows.length).toBe(1);
    });

    it("unchecks other rows if multiCheck is set to false", function() {
      var model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = false;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      table.checkRow(rows[4], true, true);

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(1);

      table.checkRow(rows[4], false, true);

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it("does not check the row if checkable is set to false", function() {
      var model = helper.createModelFixture(2, 5);
      model.checkable = false;
      model.multiCheck = false;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it("does not check the row if the row is disabled", function() {
      var model = helper.createModelFixture(2, 5);
      model.multiCheck = false;
      model.checkable = false;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
      rows[0].enabled = false;
      table.checkRow(rows[0], true, true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it("does not check the row if the table is disabled", function() {
      var model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = true;
      var table = helper.createTable(model);
      table.enabled = false;
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it("considers view ragne", function() {
      //FIXME CGU implement
    });

  });

  describe("selectRows", function() {

    it("updates model", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rows = [table.rows[0], model.rows[4]];
      table.selectRows(rows);

      expect(table.selectedRows).toEqual(rows);
    });

    it("selects rendered rows and unselects others", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, [model.rows[0], model.rows[4]]);
      helper.selectRowsAndAssert(table, [model.rows[2]]);
    });


    it("considers view ragne", function() {
      //FIXME CGU implement
    });

    it("sends selection event containing rowIds", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rows = [table.rows[0], table.rows[4]];
      table.selectRows(rows, true);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);

      var event = new scout.Event(table.id, 'rowsSelected', {
        rowIds: helper.getRowIds(rows)
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

  describe("toggle selection", function() {
    it("selects all if not all are selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      table.toggleSelection();
      helper.assertSelection(table, model.rows);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(model.rows));
    });

    it("selects none if all are selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, table.rows);

      table.toggleSelection();
      helper.assertSelection(table, []);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, []);

      table.toggleSelection();
      helper.assertSelection(table, table.rows);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(table.rows));
    });
  });

  describe("selectAll", function() {
    it("selects all rows", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      table.toggleSelection();
      helper.assertSelection(table, model.rows);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(model.rows));
    });

    it("considers view range -> renders selection only for rendered rows", function() {
      // FIXME CGU tests
    });
  });

  describe("resizeColumn", function() {

    it("updates column model and sends resize event ", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      expect(table.columns[0].width).not.toBe(100);
      table.resizeColumn(table.columns[0], 100);
      expect(table.columns[0].width).toBe(100);

      sendQueuedAjaxCalls('', 1000);
      var event = new scout.Event(table.id, 'columnResized', {
        columnId: table.columns[0].id,
        width: 100
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("does not send resize event when resizing is in progress", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      table.resizeColumn(table.columns[0], 50);
      table.resizeColumn(table.columns[0], 100);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    it("sends resize event when resizing is finished", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      table.resizeColumn(table.columns[0], 50);
      table.resizeColumn(table.columns[0], 100);
      table.resizeColumn(table.columns[0], 150);

      sendQueuedAjaxCalls('', 1000);

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(mostRecentJsonRequest().events.length).toBe(1);

      var event = new scout.Event(table.id, 'columnResized', {
        columnId: table.columns[0].id,
        width: 150
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

  describe("autoResizeColumns", function() {

    it("distributes the table columns using initialWidth as weight", function() {
      var model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 100;
      model.columns[1].initialWidth = 200;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);
      table.$data.width(450);

      var event = createPropertyChangeEvent(table, {
        "autoResizeColumns": true
      });
      table.onModelPropertyChange(event);

      // Triggers TableLayout._layoutColumns()
      session.layoutValidator.validate();

      expect(table.columns[0].width).toBe(150);
      expect(table.columns[1].width).toBe(300);
    });

    it("excludes columns with fixed width", function() {
      var model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 100;
      model.columns[0].width = model.columns[0].initialWidth;
      model.columns[0].fixedWidth = true;
      model.columns[1].initialWidth = 200;
      model.columns[1].width = model.columns[1].initialWidth;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);
      table.$data.width(450);

      var event = createPropertyChangeEvent(table, {
        "autoResizeColumns": true
      });
      table.onModelPropertyChange(event);

      // Triggers TableLayout._layoutColumns()
      session.layoutValidator.validate();

      expect(table.columns[0].width).toBe(100);
      expect(table.columns[1].width).toBe(350);
    });

    it("does not make the column smaller than a minimum size", function() {
      var model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 1000;
      model.columns[1].initialWidth = 10;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);
      table.$data.width(450);

      var event = createPropertyChangeEvent(table, {
        "autoResizeColumns": true
      });
      table.onModelPropertyChange(event);

      // Triggers TableLayout._layoutColumns()
      session.layoutValidator.validate();

      expect(table.columns[0].width).toBe(450 - scout.Column.DEFAULT_MIN_WIDTH);
      expect(table.columns[1].width).toBe(scout.Column.DEFAULT_MIN_WIDTH);
    });

  });

  describe("sort", function() {
    var model, table, column0, column1, column2;
    var $colHeaders, $header0, $header1, $header2;

    function prepareTable() {
      model = helper.createModelFixture(3, 3);
      table = helper.createTable(model);
      column0 = model.columns[0];
      column1 = model.columns[1];
      column2 = model.columns[2];
    }

    function render(table) {
      table.render(session.$entryPoint);
      $colHeaders = table.header.$container.find('.table-header-item');
      $header0 = $colHeaders.eq(0);
      $header1 = $colHeaders.eq(1);
      $header2 = $colHeaders.eq(2);
    }

    it("updates column model", function() {
      prepareTable();
      render(table);
      table.sort(column0, 'desc');

      expect(table.columns[0].sortActive).toBe(true);
      expect(table.columns[0].sortAscending).toBe(false);
      expect(table.columns[0].sortIndex).toBe(0);
    });

    describe('model update', function() {
      it("sets sortAscending according to direction param", function() {
        prepareTable();
        render(table);

        table.sort(column0, 'desc');
        expect(table.columns[0].sortAscending).toBe(false);

        table.sort(column0, 'asc');
        expect(table.columns[0].sortAscending).toBe(true);
      });

      it("resets properties on other columns", function() {
        prepareTable();
        render(table);

        // reset logic is only applied on columns used as sort-column, that's why
        // we must set the sortActive flag here.
        table.columns[1].sortActive = true;

        table.sort(column0, 'desc');
        expect(table.columns[0].sortActive).toBe(true);
        expect(table.columns[0].sortAscending).toBe(false);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortActive).toBe(false);
        expect(table.columns[1].sortIndex).toBe(-1);

        table.sort(column1, 'desc');
        expect(table.columns[0].sortActive).toBe(false);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortActive).toBe(true);
        expect(table.columns[1].sortAscending).toBe(false);
        expect(table.columns[1].sortIndex).toBe(0);
      });

      it("sets sortIndex", function() {
        prepareTable();
        render(table);

        // reset logic is only applied on columns used as sort-column, that's why
        // we must set the sortActive flag here.
        table.columns[1].sortActive = true;

        table.sort(column0, 'desc');
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(-1);

        table.sort(column1, 'desc', true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(1);

        table.sort(column1, 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(0);
      });

      it("does not remove sortIndex for columns always included at begin", function() {
        prepareTable();
        column1.initialAlwaysIncludeSortAtBegin = true;
        column1.sortActive = true;
        column1.sortIndex = 1;
        column2.initialAlwaysIncludeSortAtBegin = true;
        column2.sortActive = true;
        column2.sortIndex = 0;
        table._onColumnStructureChanged(table.columns); // (re)initialize columns, have been initialised already during init
        render(table);

        table.sort(table.columns[0], 'desc');
        expect(table.columns[0].sortIndex).toBe(2);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc', true, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[2], 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);
      });

      it("does not remove sortIndex for columns always included at end", function() {
        prepareTable();
        column1.initialAlwaysIncludeSortAtEnd = true;
        column1.sortActive = true;
        column1.sortIndex = 1;
        column2.initialAlwaysIncludeSortAtEnd = true;
        column2.sortActive = true;
        column2.sortIndex = 0;
        table._onColumnStructureChanged(table.columns); // (re)initialize columns, have been initialised already during init
        render(table);

        table.sort(table.columns[0], 'desc');
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(2);
        expect(table.columns[2].sortIndex).toBe(1);

        table.sort(table.columns[1], 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[2], 'desc', true, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);
      });

      it("does not remove sortIndex for columns always included at begin and end (combination)", function() {
        prepareTable();
        column1.initialAlwaysIncludeSortAtEnd = true;
        column1.sortActive = true;
        column1.sortIndex = 1;
        column2.initialAlwaysIncludeSortAtBegin = true;
        column2.sortActive = true;
        column2.sortIndex = 0;
        table._onColumnStructureChanged(table.columns); // (re)initialize columns, have been initialised already during init
        render(table);

        table.sort(table.columns[0], 'desc');
        expect(table.columns[0].sortIndex).toBe(1);
        expect(table.columns[1].sortIndex).toBe(2);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[2], 'desc', true, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);
      });

      it("removes column from sort columns", function() {
        prepareTable();
        render(table);

        // reset logic is only applied on columns used as sort-column, that's why
        // we must set the sortActive flag here.
        table.columns[1].sortActive = true;

        table.sort(column0, 'desc');
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(-1);

        table.sort(column1, 'desc', true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(1);

        table.sort(column2, 'desc', true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(2);

        // Remove second column -> sortIndex of 3rd column gets adjusted
        table.sort(column1, 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(-1);
        expect(table.columns[2].sortIndex).toBe(1);
      });
    });

    it("sends rowsSorted event when client side sorting is possible", function() {
      prepareTable();
      render(table);
      // Make sure sorting is not executed because it does not work with phantomJS
      spyOn(scout.device, "supportsInternationalization").and.returnValue(true);
      spyOn(table, "_sort").and.returnValue(true);

      table.sort(column0, 'desc');
      sendQueuedAjaxCalls();

      var event = new scout.Event(table.id, 'rowsSorted', {
        columnId: table.columns[0].id,
        sortAscending: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("sends sortRows event when client side sorting is not possible", function() {
      prepareTable();
      render(table);
      spyOn(scout.device, "supportsInternationalization").and.returnValue(false);

      table.sort(column0, 'desc');
      sendQueuedAjaxCalls();

      var event = new scout.Event(table.id, 'sortRows', {
        columnId: table.columns[0].id,
        sortAscending: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("sorts the data", function() {
      prepareTable();
      render(table);
      spyOn(table, '_sort');

      table.sort(column0, 'desc');

      expect(table._sort).toHaveBeenCalled();
    });

    it("regroups the data if group by column is active", function() {
      prepareTable();
      render(table);

      // Make sure sorting is not executed because it does not work with phantomJS
      spyOn(scout.device, "supportsInternationalization").and.returnValue(true);
      spyOn(table, "_sort").and.returnValue(true);

      spyOn(table, '_group');

      column0.grouped = true;
      table.sort(column0, 'desc');

      expect(table._group).toHaveBeenCalled();
    });

    describe("sorting", function() {

      it("sorts text columns considering locale (if browser supports it)", function() {
        if (!scout.device.supportsInternationalization()) {
          return;
        }

        var model = helper.createModelSingleColumnByTexts(['Österreich', 'Italien', 'Zypern']);
        var table = helper.createTable(model);
        column0 = model.columns[0];
        table.render(session.$entryPoint);

        table.sort(column0, 'desc');
        helper.assertTextsInCells(table.rows, 0, ['Zypern', 'Österreich', 'Italien']);

        table.sort(column0, 'asc');
        helper.assertTextsInCells(table.rows, 0, ['Italien', 'Österreich', 'Zypern']);

        // In order to change Collator at runtime, we must reset the "static" property
        // since it is set only once
        session.locale = new LocaleSpecHelper().createLocale('sv');
        scout.StringColumn.collator = undefined;

        table.sort(column0, 'desc');
        helper.assertTextsInCells(table.rows, 0, ['Österreich', 'Zypern', 'Italien']);

        table.sort(column0, 'asc');
        helper.assertTextsInCells(table.rows, 0, ['Italien', 'Zypern', 'Österreich']);
      });

      it("sorts number columns", function() {
        var model = helper.createModelSingleColumnByValues([100, 90, 300], 'NumberColumn');
        var table = helper.createTable(model);
        column0 = model.columns[0];
        table.render(session.$entryPoint);

        table.sort(column0, 'desc');
        helper.assertValuesInCells(table.rows, 0, [300, 100, 90]);

        table.sort(column0, 'asc');
        helper.assertValuesInCells(table.rows, 0, [90, 100, 300]);
      });

      it("sorts date columns", function() {
        var model = helper.createModelSingleColumnByValues([new Date('2012-08-10'), new Date('2014-03-01'), new Date('1999-01-10')], 'DateColumn');
        var table = helper.createTable(model);
        column0 = model.columns[0];
        table.render(session.$entryPoint);

        table.sort(column0, 'desc');
        helper.assertDatesInCells(table.rows, 0, [new Date('2014-03-01'), new Date('2012-08-10'), new Date('1999-01-10')]);

        table.sort(column0, 'asc');
        helper.assertDatesInCells(table.rows, 0, [new Date('1999-01-10'), new Date('2012-08-10'), new Date('2014-03-01')]);
      });

    });

  });

  describe("column grouping", function() {
    var model, table, column0, column1, column2, column3, column4, rows, columns;
    var $colHeaders, $header0, $header1;

    function prepareTable() {
      columns = [helper.createModelColumn('col0'),
        helper.createModelColumn('col1'),
        helper.createModelColumn('col2'),
        helper.createModelColumn('col3', 'NumberColumn'),
        helper.createModelColumn('col4', 'NumberColumn')
      ];
      columns[0].index = 0;
      columns[1].index = 1;
      columns[2].index = 2;
      columns[3].index = 3;
      columns[4].index = 4;
      rows = helper.createModelRows(5, 8);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
      column0 = model.columns[0];
      column1 = model.columns[1];
      column2 = model.columns[2];
      column3 = model.columns[3];
      column4 = model.columns[4];
      column3.setAggregationFunction('sum');
      column4.setAggregationFunction('sum');
    }

    function prepareContent() {
      var column0Values = ['a', 'b'],
        column1Values = ['c', 'd'],
        column2Values = ['e', 'f'],
        value, text, j;

      for (var i = 0; i < rows.length; i++) {
        value = column0Values[Math.floor(i / 4)];
        text = value.toString();
        rows[i].cells[0] = helper.createModelCell(text, value);

        value = column1Values[(Math.floor(i / 2)) % 2];
        text = value.toString();
        rows[i].cells[1] = helper.createModelCell(text, value);

        value = column2Values[i % 2];
        text = value.toString();
        rows[i].cells[2] = helper.createModelCell(text, value);

        j = i + 1;
        rows[i].cells[3].value = j;
        rows[i].cells[3].text = j.toString();
        rows[i].cells[4].value = j * 3;
        rows[i].cells[4].text = (j * 3).toString();
      }

    }

    function render(table) {
      table.render(session.$entryPoint);
      $colHeaders = table.header.$container.find('.table-header-item');
      $header0 = $colHeaders.eq(0);
      $header1 = $colHeaders.eq(1);
    }

    function addGrouping(table, column, multi) {
      table.groupColumn(column, multi, 'asc', false);
    }

    function removeGrouping(table, column) {
      table.groupColumn(column, "", 'asc', true);
    }

    function assertGroupingProperty(table) {
      var i, expectGrouped = scout.arrays.init(5, false);
      for (i = 1; i < arguments.length; i++) {
        expectGrouped[arguments[i]] = true;
      }

      for (i = 0; i < 5; i++) {
        if (expectGrouped[i]) {
          expect(table.columns[i].grouped).toBeTruthy();
        } else {
          expect(table.columns[i].grouped).toBeFalsy();
        }
      }
    }

    function find$aggregateRows(table) {
      return table.$data.find('.table-aggregate-row');
    }

    function assertGroupingValues(table, column, values) {
      var i, c, $sumCell;
      c = table.columns.indexOf(column);
      expect(find$aggregateRows(table).length).toBe(values.length);

      for (i = 0; i < values.length; i++) {
        $sumCell = find$aggregateRows(table).eq(i).children().eq(c);
        $sumCell.find('.table-cell-icon').remove();
        expect($sumCell.text()).toBe(values[i]);
      }
    }

    beforeEach(function() {
      // generation of sumrows is animated. leads to misleading test failures.
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });

    it("renders an aggregate row for each group", function() {
      //FIXME CGU implement
    });

    it("considers view range -> only renders an aggregate row for rendered rows", function() {
      //FIXME CGU implement
    });

    it("regroups if rows get inserted", function() {
      //FIXME CGU implement
    });

    it("regroups if rows get deleted", function() {
      //FIXME CGU implement
    });

    it("regroups if rows get updated", function() {
      //FIXME CGU implement
    });

    it("removes aggregate rows if all rows get deleted", function() {
      //FIXME CGU implement
    });

    //group only column 0
    it("groups column 0 only", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);

      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column0, false);
      expect(find$aggregateRows(table).length).toBe(2);
      assertGroupingProperty(table, 0);
      assertGroupingValues(table, column3, ['10', '26']);
      assertGroupingValues(table, column4, ['30', '78']);
      removeGrouping(table, column0);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);
    });

    //group only column 1
    it("groups column 1 only", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);

      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column1, false);
      expect(find$aggregateRows(table).length).toBe(2);
      assertGroupingProperty(table, 1);
      assertGroupingValues(table, column3, ['14', '22']);
      assertGroupingValues(table, column4, ['42', '66']);
      removeGrouping(table, column1);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);
    });

    //group columns 0 and 1
    it("groups columns 0 (avg) and 1 (sum)", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);
      column3.setAggregationFunction('avg');
      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column0, false);
      addGrouping(table, column1, true);
      expect(find$aggregateRows(table).length).toBe(4);
      assertGroupingProperty(table, 0, 1);
      assertGroupingValues(table, column3, ['1.5', '3.5', '5.5', '7.5']);
      assertGroupingValues(table, column4, ['9', '21', '33', '45']);
      removeGrouping(table, column0);
      removeGrouping(table, column1);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);

    });

    it("groups columns 0, 1 and 2", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);

      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column0, false);
      addGrouping(table, column1, true);
      addGrouping(table, column2, true);
      expect(find$aggregateRows(table).length).toBe(8);
      assertGroupingProperty(table, 0, 1, 2);
      assertGroupingValues(table, column3, ['1', '2', '3', '4', '5', '6', '7', '8']);
      assertGroupingValues(table, column4, ['3', '6', '9', '12', '15', '18', '21', '24']);
      removeGrouping(table, column0);
      removeGrouping(table, column1);
      removeGrouping(table, column2);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);

    });

    //vary order
    it("groups columns 2 and 1", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);

      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column2, false);
      addGrouping(table, column1, true);
      expect(find$aggregateRows(table).length).toBe(4);
      assertGroupingProperty(table, 2, 1);
      assertGroupingValues(table, column3, ['6', '10', '8', '12']);
      assertGroupingValues(table, column4, ['18', '30', '24', '36']);
      removeGrouping(table, column1);
      removeGrouping(table, column2);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);

    });

    //group only column 1, but group 0 first
    it("groups column 1 only", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);

      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column0, false);
      addGrouping(table, column2, true);
      addGrouping(table, column1, false);
      expect(find$aggregateRows(table).length).toBe(2);
      assertGroupingProperty(table, 1);
      assertGroupingValues(table, column3, ['14', '22']);
      assertGroupingValues(table, column4, ['42', '66']);
      removeGrouping(table, column1);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);
    });

    //group only column 1, but group 0 first
    it("groups column 1 and 2", function() {
      if (!scout.device.supportsInternationalization()) {
        return;
      }
      prepareTable();
      prepareContent();
      render(table);

      expect(find$aggregateRows(table).length).toBe(0);
      addGrouping(table, column0, false);
      addGrouping(table, column2, true);
      addGrouping(table, column1, true);
      removeGrouping(table, column0);
      expect(find$aggregateRows(table).length).toBe(4);
      assertGroupingProperty(table, 1, 2);
      assertGroupingValues(table, column3, ['6', '10', '8', '12']);
      assertGroupingValues(table, column4, ['18', '30', '24', '36']);
      removeGrouping(table, column1);
      removeGrouping(table, column2);
      expect(find$aggregateRows(table).length).toBe(0);
      assertGroupingProperty(table);
    });

  });

  describe("row click", function() {

    function clickRowAndAssertSelection(table, $row) {
      $row.triggerClick();

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(1);

      var $selectedRow = $selectedRows.first();
      expect($selectedRow).toEqual($row);

      expect($selectedRow.hasClass('selected')).toBeTruthy();
      expect($selectedRow.hasClass('select-single')).toBeTruthy();
    }

    it("selects row and unselects others", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      var $rows = table.$rows();
      clickRowAndAssertSelection(table, $rows.eq(1));
      clickRowAndAssertSelection(table, $rows.eq(2));

      helper.selectRowsAndAssert(table, [model.rows[0], model.rows[4]]);
      clickRowAndAssertSelection(table, $rows.eq(4));
    });

    it("sends selection and click events", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$rows().first();
      $row.triggerClick();

      sendQueuedAjaxCalls();

      // clicked has to be after selected otherwise it is not possible to get the selected row in execRowClick
      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowsSelected', 'rowClicked']);
    });

    it("sends only click if row already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$rows().first();
      clickRowAndAssertSelection(table, $row);
      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowsSelected', 'rowClicked']);

      // Reset internal state because there is no "sleep" in JS
      table._doubleClickSupport._lastTimestamp -= 5000; // simulate last click 5 seconds ago

      jasmine.Ajax.requests.reset();
      clickRowAndAssertSelection(table, $row);
      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClicked']);
    });

    it("sends selection, checked and click events if table is checkable and checkbox has been clicked", function() {
      var model = helper.createModelFixture(2, 5);
      model.checkable = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $checkbox = table.$rows().first().find('.check-box').first();
      $checkbox.triggerClick();

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowsSelected', 'rowsChecked', 'rowClicked']);
    });

  });

  describe("right click on row", function() {

    afterEach(function() {
      // Close context menus
      $('body').triggerClick();
    });

    it("opens context menu", function() {
      var model = helper.createModelFixture(2, 2);
      var table = helper.createTable(model);
      table.selectedRows = [table.rows[0]];
      table.render(session.$entryPoint);

      var menuModel = helper.createMenuModel('menu'),
        menu = helper.menuHelper.createMenu(menuModel);
      table.menus = [menu];
      var $row0 = table.$data.children('.table-row').eq(0);
      $row0.triggerContextMenu();

      sendQueuedAjaxCalls();

      var $menu = helper.getDisplayingContextMenu(table);
      expect($menu.length).toBeTruthy();
    });

    it("context menu only shows visible menus", function() {
      var model = helper.createModelFixture(2, 2);
      var table = helper.createTable(model);
      table.selectedRows = [table.rows[0]];
      table.render(session.$entryPoint);

      var menuModel1 = helper.createMenuModel('menu'),
        menu1 = helper.menuHelper.createMenu(menuModel1),
        menuModel2 = helper.createMenuModel('menu'),
        menu2 = helper.menuHelper.createMenu(menuModel2);
      menu2.visible = false;

      table.menus = [menu1, menu2];
      var $row0 = table.$data.children('.table-row').eq(0);
      $row0.triggerContextMenu();

      sendQueuedAjaxCalls();

      var $menu = helper.getDisplayingContextMenu(table);
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });
// TODO nbu
//    it("context menu only shows sub-menus of matching type", function() {
//      var model = helper.createModelFixture(2, 2);
//      var table = helper.createTable(model);
//      table.selectedRows = [table.rows[0]];
//      table.render(session.$entryPoint);
//
//      var menuModelTop = helper.createMenuModel('topMenu'),
//      menuTop = helper.menuHelper.createMenu(menuModelTop),
//      menuModel1 = helper.createMenuModel('singleSelectionMenu'),
//      menu1 = helper.menuHelper.createMenu(menuModel1),
//      menuModel2 = helper.createMenuModel('multiSelectionMenu'),
//      menu2 = helper.menuHelper.createMenu(menuModel2);
//      menu2.menuTypes = ['Table.MultiSelection'];
//      // TODO [5.2] nbu: enable when TODO in ContextMenuPopup.prototype._renderMenuItems is done
////      var menuModel3 = helper.createMenuModel('emptySpaceMenu'),
////      menu3 = helper.menuHelper.createMenu(menuModel3);
////      menu3.menuTypes = ['Table.EmptySpace'];
//
//      menuTop.childActions = [menu1, menu2
//      // TODO [5.2] nbu: enable when TODO in ContextMenuPopup.prototype._renderMenuItems is done
//                              //, menu3
//                              ];
//      table.menus = [menuTop];
//      table._syncMenus([menuTop]);
//      var $row0 = table.$data.children('.table-row').eq(0);
//      $row0.triggerContextMenu();
//
//      sendQueuedAjaxCalls();
//
//      var $menu = helper.getDisplayingContextMenu(table);
//      expect($menu.find('.menu-item').length).toBe(1);
//      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
//
//      var $menuTop = $menu.find('.menu-item');
//      $menuTop.triggerClick();
//      sendQueuedAjaxCalls();
//      expect($('.menu-item').find("span:contains('singleSelectionMenu')").length).toBe(1);
//      expect($('.menu-item').find("span:contains('multiSelectionMenu')").length).toBe(0);
//      // TODO [5.2] nbu: enable when TODO in ContextMenuPopup.prototype._renderMenuItems is done
////      expect($('.menu-item').find("span:contains('emptySpaceMenu')").length).toBe(0);
//    });

  });

  describe("_filterMenus", function() {
    var singleSelMenu, multiSelMenu, bothSelMenu, emptySpaceMenu, headerMenu, table;

    beforeEach(function() {
      var model = helper.createModelFixture(2, 2);
      singleSelMenu = helper.menuHelper.createMenu({
        menuTypes: ['Table.SingleSelection']
      });
      multiSelMenu = helper.menuHelper.createMenu({
        menuTypes: ['Table.MultiSelection']
      });
      emptySpaceMenu = helper.menuHelper.createMenu({
        menuTypes: ['Table.EmptySpace']
      });
      headerMenu = helper.menuHelper.createMenu({
        menuTypes: ['Table.Header']
      });
      table = helper.createTable(model);
      table.menus = [singleSelMenu, multiSelMenu, emptySpaceMenu, headerMenu];
    });

    // context menu
    it("returns no menus for contextMenu if no row is selected", function() {
      table.selectRows([]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([]);
    });

    it("returns only single selection menus for contextMenu if one row is selected", function() {
      table.selectRows(table.rows[0]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([singleSelMenu]);
    });

    it("returns only multi selection menus for contextMenu if multiple rows are selected", function() {
      table.selectRows([table.rows[0], table.rows[1]]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([multiSelMenu]);
    });

    it("returns menus with single- and multi selection set for contextMenu if one or more rows are selected", function() {
      bothSelMenu = helper.menuHelper.createMenu({
        menuTypes: ['Table.SingleSelection', 'Table.MultiSelection']
      });
      table.menus = [singleSelMenu, multiSelMenu, bothSelMenu];
      table.selectRows(table.rows[0]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([singleSelMenu, bothSelMenu]);

      table.selectRows([table.rows[0], table.rows[1]]);
      menus = table._filterMenus(table.menus, scout.MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([multiSelMenu, bothSelMenu]);

      table.selectRows([]);
      menus = table._filterMenus(table.menus, scout.MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([]);
    });

    // menuBar
    it("returns only empty space menus if no row is selected", function() {
      table.selectRows([]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.MENU_BAR);
      expect(menus).toEqual([emptySpaceMenu]);
    });

    it("returns empty space and single selection menus if one row is selected", function() {
      table.selectRows(table.rows[0]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.MENU_BAR);
      expect(menus).toEqual([singleSelMenu, emptySpaceMenu]);
    });

    it("returns empty space and multi selection menus if multiple rows are selected", function() {
      table.selectRows([table.rows[0], table.rows[1]]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.MENU_BAR);
      expect(menus).toEqual([multiSelMenu, emptySpaceMenu]);
    });

    it("returns menus with empty space, single- and multi selection set if one or more rows are selected", function() {
      bothSelMenu = helper.menuHelper.createMenu({
        menuTypes: ['Table.SingleSelection', 'Table.MultiSelection']
      });
      table.menus = [singleSelMenu, multiSelMenu, emptySpaceMenu, bothSelMenu];
      table.selectRows(table.rows[0]);
      var menus = table._filterMenus(table.menus, scout.MenuDestinations.MENU_BAR);
      expect(menus).toEqual([singleSelMenu, emptySpaceMenu, bothSelMenu]);

      table.selectRows([table.rows[0], table.rows[1]]);
      menus = table._filterMenus(table.menus, scout.MenuDestinations.MENU_BAR);
      expect(menus).toEqual([multiSelMenu, emptySpaceMenu, bothSelMenu]);

      table.selectRows([]);
      menus = table._filterMenus(table.menus, scout.MenuDestinations.MENU_BAR);
      expect(menus).toEqual([emptySpaceMenu]);
    });
  });

  describe("selection: row mouse down / move / up", function() {

    it("selects multiple rows", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);
      var $row4 = $rows.eq(4);

      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('selected');

      $row0.triggerMouseDown();
      $row1.trigger('mouseover');
      $row2.trigger('mouseover');
      $row2.triggerMouseUp();

      expect([$row0, $row1, $row2]).allToHaveClass('selected');
      expect($row0).toHaveClass('select-top');
      expect($row1).toHaveClass('select-middle');
      expect($row2).toHaveClass('select-bottom');

      expect([$row3, $row4]).not.allToHaveClass('selected');
      expect([$row1, $row2, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');
    });

    it("selection classes after sorting", function() {
      var model = helper.createModelSingleColumnByValues([5, 2, 1, 3, 4], 'NumberColumn'),
        table = helper.createTable(model),
        column0 = model.columns[0];
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);
      var $row4 = $rows.eq(4);

      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('selected');

      $row1.triggerMouseDown();
      $row2.trigger('mouseover');
      $row3.trigger('mouseover');
      $row3.triggerMouseUp();

      expect([$row0, $row4]).not.anyToHaveClass('selected');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row2, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');

      // sort table (descending)
      table.sort(column0, 'desc');

      // after sorting
      expect([$row1, $row2, $row3]).allToHaveClass('selected');
      expect($row3).toHaveClass('select-top');
      expect($row1).toHaveClass('select-middle');
      expect($row2).toHaveClass('select-bottom');

      expect([$row0, $row4]).not.allToHaveClass('selected');
      expect([$row0, $row1, $row2, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');
    });

    it("selection classes after filtering", function() {
      var model = helper.createModelSingleColumnByValues([5, 2, 1, 3, 4], 'NumberColumn'),
        table = helper.createTable(model),
        column0 = model.columns[0];
      table._animationRowLimit = 0;
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);
      var $row4 = $rows.eq(4);

      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('hidden');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('selected');

      $row1.triggerMouseDown();
      $row2.trigger('mouseover');
      $row3.trigger('mouseover');
      $row3.triggerMouseUp();

      // before filtering
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('hidden');
      expect([$row1, $row2, $row3]).allToHaveClass('selected');
      expect($row1).toHaveClass('select-top');
      expect($row2).toHaveClass('select-middle');
      expect($row3).toHaveClass('select-bottom');

      expect([$row0, $row4]).not.anyToHaveClass('selected');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row2, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');

      // filter table (descending)
      table.addFilter({
        createKey: function() {
          return 1;
        },
        accept: function(row) {
          return row.$row.text() % 2 === 0;
        }
      });
      table.filter();

      // after filtering
      expect([$row0, $row2, $row3]).allToHaveClass('hidden');
      expect($row1).allToHaveClass('selected');
      expect($row1).toHaveClass('select-single');

      expect([$row1, $row4]).not.anyToHaveClass('hidden');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('selected');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-single');
    });

    it("only sends selection event, no click", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);

      expect($rows).not.toHaveClass('selected');

      $row0.triggerMouseDown();
      $row1.trigger('mouseover');
      $row2.trigger('mouseover');
      $row2.triggerMouseUp();

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      // first selection event for first row, second selection event for remaining rows (including first row)
      expect(requestData).toContainEventTypesExactly(['rowsSelected']);

      var event = [new scout.Event(table.id, 'rowsSelected', {
        rowIds: [model.rows[0].id, model.rows[1].id, model.rows[2].id]
      })];
      expect(requestData).toContainEvents(event);
    });

    it("only send one event for mousedown and immediate mouseup on the same row", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);

      expect($rows).not.toHaveClass('selected');

      $row0.triggerMouseDown();
      $row0.triggerMouseUp();

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      // exactly only one selection event for first row
      expect(requestData).toContainEventTypesExactly(['rowsSelected', 'rowClicked']);

      var event = [new scout.Event(table.id, 'rowsSelected', {
        rowIds: [model.rows[0].id]
      })];
      expect(requestData).toContainEvents(event);
    });

    it("only selects first row if mouse move selection or multi selection is disabled", function() {
      var model = helper.createModelFixture(2, 4);
      var table = helper.createTable(model);
      table.selectionHandler.mouseMoveSelectionEnabled = false;
      verifyMouseMoveSelectionIsDisabled(model, table, false);

      model = helper.createModelFixture(2, 4);
      table = helper.createTable(model);
      table.multiSelect = false;
      verifyMouseMoveSelectionIsDisabled(model, table, true);
    });

    function verifyMouseMoveSelectionIsDisabled(model, table, selectionMovable) {
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);

      expect($rows).not.toHaveClass('selected');

      $row0.triggerMouseDown();
      $row1.trigger('mouseover');
      $row2.trigger('mouseover');
      $row2.triggerMouseUp();

      var expectedSelectedRowIndex = (selectionMovable ? 2 : 0);
      for (var i = 0; i < $rows.length; i++) {
        if (i === expectedSelectedRowIndex) {
          expect($rows.eq(i)).toHaveClass('selected');
        } else {
          expect($rows.eq(i)).not.toHaveClass('selected');
        }
      }

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      var event = new scout.Event(table.id, 'rowsSelected', {
        rowIds: [model.rows[expectedSelectedRowIndex].id]
      });
      expect(requestData).toContainEvents(event);
    }

  });

  describe("moveColumn", function() {

    it("moves column from oldPos to newPos", function() {
      var model = helper.createModelFixture(3, 2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $colHeaders = table.header.$container.find('.table-header-item');
      var $header0 = $colHeaders.eq(0);
      var $header1 = $colHeaders.eq(1);
      var $header2 = $colHeaders.eq(2);

      expect(table.columns.indexOf($header0.data('column'))).toBe(0);
      expect(table.columns.indexOf($header1.data('column'))).toBe(1);
      expect(table.columns.indexOf($header2.data('column'))).toBe(2);

      table.moveColumn($header0.data('column'), 0, 2);

      expect(table.columns.indexOf($header1.data('column'))).toBe(0);
      expect(table.columns.indexOf($header2.data('column'))).toBe(1);
      expect(table.columns.indexOf($header0.data('column'))).toBe(2);

      table.moveColumn($header2.data('column'), 1, 0);

      expect(table.columns.indexOf($header2.data('column'))).toBe(0);
      expect(table.columns.indexOf($header1.data('column'))).toBe(1);
      expect(table.columns.indexOf($header0.data('column'))).toBe(2);
    });

    it("considers view range", function() {
      //FIXME CGU does not throw exception if row which is not in view range gets updated
    });

  });

  describe("onModelAction", function() {

    function createRowsInsertedEvent(model, rows) {
      return {
        target: model.id,
        rows: rows,
        type: 'rowsInserted'
      };
    }

    it("processes selection events", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      spyOn(table, '_onRowsSelected');

      var rowIds = ['0', '4'];
      var event = new scout.Event(table.id, 'rowsSelected', {
        rowIds: rowIds
      });
      table.onModelAction(event);

      expect(table._onRowsSelected).toHaveBeenCalledWith(rowIds);
    });

    describe("rowsDeleted event", function() {
      var model, table, row0, row1, row2;

      function createRowsDeletedEvent(model, rowIds) {
        return {
          target: model.id,
          rowIds: rowIds,
          type: 'rowsDeleted'
        };
      }

      beforeEach(function() {
        model = helper.createModelFixture(2, 3);
        table = helper.createTable(model);
        row0 = model.rows[0];
        row1 = model.rows[1];
        row2 = model.rows[2];
      });

      it("deletes single rows from model", function() {
        expect(table.rows.length).toBe(3);
        expect(table.rows[0]).toBe(row0);

        var message = {
          events: [createRowsDeletedEvent(model, [row0.id])]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(2);
        expect(table.rows[0]).toBe(row1);

        message = {
          events: [createRowsDeletedEvent(model, [row1.id, row2.id])]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(0);
      });

      it("deletes single rows from model document", function() {
        table.render(session.$entryPoint);

        expect(table.$rows().length).toBe(3);

        var message = {
          events: [createRowsDeletedEvent(model, [row0.id])]
        };
        session._processSuccessResponse(message);

        expect(table.$rows().length).toBe(2);
        expect(table.$data.find('[data-rowid="' + row0.id + '"]').length).toBe(0);
        expect(table.$data.find('[data-rowid="' + row1.id + '"]').length).toBe(1);
        expect(table.$data.find('[data-rowid="' + row2.id + '"]').length).toBe(1);

        message = {
          events: [createRowsDeletedEvent(model, [row1.id, row2.id])]
        };
        session._processSuccessResponse(message);

        expect(table.$rows().length).toBe(0);
      });

      it("considers view range", function() {
        //FIXME CGU remove rendered and non rendered row without exception
      });

      it("adjusts viewRangeRendered", function() {
        //FIXME CGU remove in block, remove before, after and exactly
      });

    });

    describe("allRowsDeleted event", function() {
      var model, table, row0, row1, row2;

      function createAllRowsDeletedEvent(model, rowIds) {
        return {
          target: model.id,
          type: 'allRowsDeleted'
        };
      }

      beforeEach(function() {
        model = helper.createModelFixture(2, 3);
        table = helper.createTable(model);
        row0 = model.rows[0];
        row1 = model.rows[1];
        row2 = model.rows[2];
      });

      it("deletes all rows from model", function() {
        expect(table.rows.length).toBe(3);

        var message = {
          events: [createAllRowsDeletedEvent(model)]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(0);
      });

      it("deletes all rows from html document", function() {
        table.render(session.$entryPoint);

        expect(table.$rows().length).toBe(3);

        var message = {
          events: [createAllRowsDeletedEvent(model)]
        };
        session._processSuccessResponse(message);

        expect(table.$rows().length).toBe(0);
      });

      it("adjusts viewRangeRendered", function() {
        //FIXME CGU remove in block, remove before, after and exactly
      });

    });

    describe("rowsInserted event", function() {
      var model, table;

      beforeEach(function() {
        model = helper.createModelFixture(2);
        table = helper.createTable(model);
      });

      it("inserts rows at the end of the table", function() {
        expect(table.rows.length).toBe(0);

        var rows = helper.createModelRows(2, 5);
        var message = {
          events: [createRowsInsertedEvent(model, rows)]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(5);
        expect(Object.keys(table.rowsMap).length).toBe(5);

        rows = helper.createModelRows(2, 3);
        message = {
          events: [createRowsInsertedEvent(model, rows)]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(5 + 3);
        expect(Object.keys(table.rowsMap).length).toBe(5 + 3);
      });

      it("renders rows only if view range is not full yet", function() {
        //FIXME CGU remove rendered and non rendered row without exception
      });

      it("adjusts viewRangeRendered if new rows were rendered", function() {
        //FIXME CGU remove in block, remove before, after and exactly
      });
    });

    describe("rowOrderChanged event", function() {
      var model, table, row0, row1, row2;

      beforeEach(function() {
        model = helper.createModelFixture(2, 3);
        table = helper.createTable(model);
        row0 = model.rows[0];
        row1 = model.rows[1];
        row2 = model.rows[2];
      });

      function createRowOrderChangedEvent(model, rowIds) {
        return {
          target: model.id,
          rowIds: rowIds,
          type: 'rowOrderChanged'
        };
      }

      it("reorders the model rows", function() {
        var message = {
          events: [createRowOrderChangedEvent(model, [row2.id, row1.id, row0.id])]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(3);
        expect(table.rows[0]).toBe(row2);
        expect(table.rows[1]).toBe(row1);
        expect(table.rows[2]).toBe(row0);
      });

      it("reorders the html nodes", function() {
        table.render(session.$entryPoint);

        var message = {
          events: [createRowOrderChangedEvent(model, [row2.id, row1.id, row0.id])]
        };
        session._processSuccessResponse(message);

        var $rows = table.$rows();
        expect($rows.eq(0).attr('data-rowid')).toBe(model.rows[2].id);
        expect($rows.eq(1).attr('data-rowid')).toBe(model.rows[1].id);
        expect($rows.eq(2).attr('data-rowid')).toBe(model.rows[0].id);
      });

      it("does not animate ordering for newly inserted rows", function() {
        table.render(session.$entryPoint);
        expect(table.rows.length).toBe(3);

        var newRows = [
          helper.createModelRow(null, helper.createModelCells(2)),
          helper.createModelRow(null, helper.createModelCells(2))
        ];

        //Insert new rows and switch rows 0 and 1
        var message = {
          events: [
            createRowsInsertedEvent(model, newRows),
            createRowOrderChangedEvent(model, [row1.id, row0.id, newRows[0].id, newRows[1].id, row2.id])
          ]
        };
        session._processSuccessResponse(message);

        //Check if rows were inserted
        expect(table.rows.length).toBe(5);

        //Check if animation is not done for the inserted rows
        //The animation should be done for the other rows (row0 and 1 are switched -> visualize)
        var $rows = table.$rows();
        $rows.each(function() {
          var $row = $(this);
          var oldTop = $row.data('old-top');
          var rowId = $row.attr('data-rowid');
          if (rowId === newRows[0].id || rowId === newRows[1].id) {
            expect(oldTop).toBeUndefined();
          } else {
            expect(oldTop).toBeDefined();
          }
        });
      });

      it("considers view range", function() {
        //FIXME check if rendered rows in view range are in correct order
      });
    });

    describe("rowsUpdated event", function() {
      var model, table, row0;

      function createRowsUpdatedEvent(model, rows) {
        return {
          target: model.id,
          rows: rows,
          type: 'rowsUpdated'
        };
      }

      beforeEach(function() {
        model = helper.createModelFixture(2, 2);
        model.rows[0].cells[0].text = 'cellText0';
        model.rows[0].cells[1].text = 'cellText1';
        table = helper.createTable(model);
      });

      it("updates the model cell texts", function() {
        expect(table.rows[0].cells[0].text).toBe('cellText0');
        expect(table.rows[0].cells[1].text).toBe('cellText1');

        var row = {
          id: table.rows[0].id,
          cells: ['newCellText0', 'newCellText1']
        };
        var message = {
          events: [createRowsUpdatedEvent(model, [row])]
        };
        session._processSuccessResponse(message);

        expect(table.rows[0].cells[0].text).toBe('newCellText0');
        expect(table.rows[0].cells[1].text).toBe('newCellText1');
      });

      it("updates the html cell texts", function() {
        table.render(session.$entryPoint);
        var $rows = table.$rows();
        var $cells0 = table.$cellsForRow($rows.eq(0));
        expect($cells0.eq(0).text()).toBe('cellText0');
        expect($cells0.eq(1).text()).toBe('cellText1');

        var row = {
          id: table.rows[0].id,
          cells: ['newCellText0', 'newCellText1']
        };
        var message = {
          events: [createRowsUpdatedEvent(model, [row])]
        };
        session._processSuccessResponse(message);

        $rows = table.$rows();
        $cells0 = table.$cellsForRow($rows.eq(0));
        expect($cells0.eq(0).text()).toBe('newCellText0');
        expect($cells0.eq(1).text()).toBe('newCellText1');
      });

      it("does not destroy selection", function() {
        table.selectedRows = [table.rows[0]];
        table.render(session.$entryPoint);

        expect($('.selected').length).toBe(1);
        expect($('.selected')[0]).toBe(table.selectedRows[0].$row[0]);
        var row = {
          id: table.rows[0].id,
          cells: ['newCellText0', 'newCellText1']
        };
        var message = {
          events: [createRowsUpdatedEvent(model, [row])]
        };
        session._processSuccessResponse(message);

        expect($('.selected').length).toBe(1);
        expect($('.selected')[0]).toBe(table.selectedRows[0].$row[0]);
      });

      it("considers view range", function() {
        //FIXME CGU does not throw exception if row which is not in view range gets updated
      });

    });

    describe("columnStructureChanged event", function() {
      var model, table, column0, column1, column2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 2);
        table = helper.createTable(model);
        column0 = model.columns[0];
        column1 = model.columns[1];
        column2 = model.columns[2];
      });

      function createColumnStructureChangedEvent(model, columns) {
        return {
          target: model.id,
          columns: columns,
          type: 'columnStructureChanged'
        };
      }

      it("resets the model columns", function() {
        var message = {
          events: [createColumnStructureChangedEvent(model, [column2, column1])]
        };
        session._processSuccessResponse(message);

        expect(table.columns.length).toBe(2);
        expect(table.columns[0].id).toBe(column2.id);
        expect(table.columns[1].id).toBe(column1.id);
      });

      it("redraws the header to reflect header cell changes (text)", function() {
        table.render(session.$entryPoint);

        var $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(0).text()).toBe(column0.text);
        expect($colHeaders.eq(1).text()).toBe(column1.text);
        expect($colHeaders.eq(2).text()).toBe(column2.text);

        column0.text = 'newColText0';
        column1.text = 'newColText1';

        var message = {
          events: [createColumnStructureChangedEvent(model, [column0, column1, column2])]
        };
        session._processSuccessResponse(message);

        //Check column header text
        $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(0).text()).toBe(column0.text);
        expect($colHeaders.eq(1).text()).toBe(column1.text);
        expect($colHeaders.eq(2).text()).toBe(column2.text);
      });
    });

    describe("columnOrderChanged event", function() {
      var model, table, column0, column1, column2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 2);
        table = helper.createTable(model);
        column0 = model.columns[0];
        column1 = model.columns[1];
        column2 = model.columns[2];
      });

      function createColumnOrderChangedEvent(model, columnIds) {
        return {
          target: model.id,
          columnIds: columnIds,
          type: 'columnOrderChanged'
        };
      }

      it("reorders the model columns", function() {
        var message = {
          events: [createColumnOrderChangedEvent(model, [column2.id, column0.id, column1.id])]
        };
        session._processSuccessResponse(message);

        expect(table.columns.length).toBe(3);
        expect(table.columns[0]).toBe(column2);
        expect(table.columns[1]).toBe(column0);
        expect(table.columns[2]).toBe(column1);
      });

      it("reorders the html nodes", function() {
        table.render(session.$entryPoint);

        var $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.length).toBe(3);
        expect($colHeaders.eq(0).data('column')).toBe(column0);
        expect($colHeaders.eq(1).data('column')).toBe(column1);
        expect($colHeaders.eq(2).data('column')).toBe(column2);

        var $rows = table.$rows();
        var $cells0 = $rows.eq(0).find('.table-cell');
        var $cells1 = $rows.eq(1).find('.table-cell');

        expect($cells0.eq(0).text()).toBe('0');
        expect($cells0.eq(1).text()).toBe('1');
        expect($cells0.eq(2).text()).toBe('2');
        expect($cells1.eq(0).text()).toBe('0');
        expect($cells1.eq(1).text()).toBe('1');
        expect($cells1.eq(2).text()).toBe('2');

        var message = {
          events: [createColumnOrderChangedEvent(model, [column2.id, column0.id, column1.id])]
        };
        session._processSuccessResponse(message);

        //Check column header order
        $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.length).toBe(3);
        expect($colHeaders.eq(0).data('column')).toBe(column2);
        expect($colHeaders.eq(1).data('column')).toBe(column0);
        expect($colHeaders.eq(2).data('column')).toBe(column1);

        //Check cells order
        $rows = table.$rows();
        $cells0 = $rows.eq(0).find('.table-cell');
        $cells1 = $rows.eq(1).find('.table-cell');
        expect($cells0.eq(0).text()).toBe('2');
        expect($cells0.eq(1).text()).toBe('0');
        expect($cells0.eq(2).text()).toBe('1');
        expect($cells1.eq(0).text()).toBe('2');
        expect($cells1.eq(1).text()).toBe('0');
        expect($cells1.eq(2).text()).toBe('1');
      });

      it("considers view range", function() {
        //FIXME CGU does not throw exception if row which is not in view range gets updated
      });

      //TODO [5.2] cgu: fails because css is not applied -> include css files in SpecRunner
      //      it("moves the table header menu if it is open", function() {
      //        table.render(session.$entryPoint);
      //
      //        var $colHeaders = table.header.findHeaderItems();
      //
      //        var $clickedHeader = $colHeaders.eq(0);
      //        $clickedHeader.triggerClick();
      //
      //        var tableHeaderMenu = table.header._tableHeaderMenu;
      //        var menuLeftPosition = tableHeaderMenu.$headerMenu.position().left;
      //        expect(tableHeaderMenu.isOpen()).toBe(true);
      //
      //        var message = {
      //            events: [createColumnOrderChangedEvent(model, [column2.id, column0.id, column1.id])]
      //        };
      //        session._processSuccessResponse(message);
      //
      //        expect(tableHeaderMenu.$headerMenu.position().left > menuLeftPosition).toBe(true);
      //      });

    });

    describe("columnHeadersUpdated event", function() {
      var model, table, column0, column1, column2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 2);
        table = helper.createTable(model);
        column0 = model.columns[0];
        column1 = model.columns[1];
        column2 = model.columns[2];
      });

      function createColumnHeadersUpdatedEvent(model, columns) {
        return {
          target: model.id,
          columns: columns,
          type: 'columnHeadersUpdated'
        };
      }

      it("updates the text and sorting state of model columns", function() {
        var text0 = table.columns[0].text;

        column1 = helper.createModelColumn('newText1');
        column1.id = model.columns[1].id;
        column1.sortActive = true;
        column1.sortAscending = true;
        column2 = helper.createModelColumn('newText2');
        column2.id = model.columns[2].id;

        var message = {
          events: [createColumnHeadersUpdatedEvent(model, [column1, column2])]
        };
        session._processSuccessResponse(message);

        expect(table.columns.length).toBe(3);
        expect(table.columns[0].text).toBe(text0);
        expect(table.columns[1].text).toBe(column1.text);
        expect(table.columns[1].sortAscending).toBe(column1.sortAscending);
        expect(table.columns[1].sortActive).toBe(column1.sortActive);
        expect(table.columns[2].text).toBe(column2.text);
        expect(table.columns[2].sortAscending).toBe(column2.sortAscending);
        expect(table.columns[2].sortActive).toBe(column2.sortActive);
      });

      it("updates the text and sorting state of html table header nodes", function() {
        table.render(session.$entryPoint);

        var $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(0).text()).toBe(column0.text);
        expect($colHeaders.eq(1).text()).toBe(column1.text);
        expect($colHeaders.eq(1)).not.toHaveClass('sort-asc');
        expect($colHeaders.eq(2).text()).toBe(column2.text);

        column1 = helper.createModelColumn('newText1');
        column1.id = model.columns[1].id;
        column1.sortActive = true;
        column1.sortAscending = true;
        column2 = helper.createModelColumn('newText2');
        column2.id = model.columns[2].id;

        var message = {
          events: [createColumnHeadersUpdatedEvent(model, [column1, column2])]
        };
        session._processSuccessResponse(message);

        $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(0).text()).toBe(column0.text);
        expect($colHeaders.eq(1).text()).toBe(column1.text);
        expect($colHeaders.eq(1)).toHaveClass('sort-asc');
        expect($colHeaders.eq(2).text()).toBe(column2.text);
      });

      it("updates the custom css class of table header nodes", function() {
        table.render(session.$entryPoint);

        var $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(1)).not.toHaveClass('custom-header');

        column1 = helper.createModelColumn();
        column1.id = model.columns[1].id;
        column1.headerCssClass = 'custom-header';
        var message = {
          events: [createColumnHeadersUpdatedEvent(model, [column1])]
        };
        session._processSuccessResponse(message);

        $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(0)).not.toHaveClass('custom-header');
        expect($colHeaders.eq(1)).toHaveClass('custom-header');

        column1 = helper.createModelColumn();
        column1.id = model.columns[1].id;
        message = {
          events: [createColumnHeadersUpdatedEvent(model, [column1])]
        };
        session._processSuccessResponse(message);

        $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.eq(0)).not.toHaveClass('custom-header');
        expect($colHeaders.eq(1)).not.toHaveClass('custom-header');
      });
    });
  });

  describe("onModelPropertyChange", function() {

    describe("headerVisible", function() {

      it("hides the table header", function() {
        var model = helper.createModelFixture(2);
        var table = helper.createTable(model);
        table.render(session.$entryPoint);

        expect(table.header).toBeTruthy();

        var event = createPropertyChangeEvent(table, {
          "headerVisible": false
        });
        table.onModelPropertyChange(event);

        expect(table.header).toBeFalsy();
      });

    });

    describe("menus", function() {

      it("creates and registers menu adapters", function() {
        var model = helper.createModelFixture(2);
        var table = helper.createTable(model);
        var menu1 = helper.createMenuModel();
        var menu2 = helper.createMenuModel();

        var message = {
          adapterData: createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1]);
      });

      it("destroys the old menus", function() {
        var model = helper.createModelFixture(2);
        var table = helper.createTable(model);
        var menu1 = helper.createMenuModel();
        var menu2 = helper.createMenuModel();

        var message = {
          adapterData: createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1]);

        message = {
          events: [createPropertyChangeEvent(table, {
            menus: [menu2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(table.menus.length).toBe(1);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu1.id)).toBeFalsy();
      });

      it("destroys the old and creates the new menus if the list contains both", function() {
        var model = helper.createModelFixture(2);
        var table = helper.createTable(model);
        var menu1 = helper.createMenuModel();
        var menu2 = helper.createMenuModel();
        var menu3 = helper.createMenuModel();

        var message = {
          adapterData: createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1]);

        message = {
          adapterData: createAdapterData(menu3),
          events: [createPropertyChangeEvent(table, {
            menus: [menu2.id, menu3.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(table.menus.length).toBe(2);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu3.id)).toBe(table.menus[1]);
        expect(session.getModelAdapter(menu1.id)).toBeFalsy();
      });

    });

  });

});
