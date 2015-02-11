/* global TableSpecHelper, LocaleSpecHelper */
describe("Table", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    session.locale = new LocaleSpecHelper().createLocale('de-CH');
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
        model.rows[0] = helper.createModelRowByTexts(1, ['cell1', '' , '0']);
        var table = helper.createTable(model);
        table.render(session.$entryPoint);

        var $row0 = table.$rows().eq(0);
        var $cells = $row0.find('.table-cell');
        expect($cells.eq(0).text()).toBe('cell1');
        expect($cells.eq(1).text()).toBe('');
        expect($cells.eq(2).text()).toBe('0');
      });

      it("accepts rows with text only", function() {
        var model = helper.createModelFixture(3, 1);
        model.rows[0] = helper.createModelRowByTexts(1, ['cell1', '' , '0'], true);
        var table = helper.createTable(model);
        table.render(session.$entryPoint);

        var $row0 = table.$rows().eq(0);
        var $cells = $row0.find('.table-cell');
        expect($cells.eq(0).text()).toBe('cell1');
        expect($cells.eq(1).text()).toBe('');
        expect($cells.eq(2).text()).toBe('0');
      });

    });

    /**
     * Test assumes that default values for horiz. alignment are set on cell object.
     */
    it("considers horizontal alignment", function() {
      var model = helper.createModelFixture(3, 2);
      model.columns[1].horizontalAlignment = 0;
      model.columns[2].horizontalAlignment = 1;

      model.rows[0].cells[1].horizontalAlignment = 0;
      model.rows[0].cells[2].horizontalAlignment = 1;

      model.rows[1].cells[1].horizontalAlignment = 0;
      model.rows[1].cells[2].horizontalAlignment = 1;

      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $headerItems = table.header.$container.find('.header-item');
      var $headerItem0 = $headerItems.eq(0);
      var $headerItem1 = $headerItems.eq(1);
      var $headerItem2 = $headerItems.eq(2);
      var $rows = table.$rows();
      var $cells0 = $rows.eq(0).find('.table-cell');
      var $cells1 = $rows.eq(1).find('.table-cell');

      //Default is different in every browser... (start chrome, webkit-auto phantomjs, left IE)
      expect($headerItem0.css('text-align')).toMatch('start|webkit-auto|left');
      expect($cells0.eq(0).css('text-align')).toMatch('start|webkit-auto|left');
      expect($cells1.eq(0).css('text-align')).toMatch('start|webkit-auto|left');

      expect($headerItem1.css('text-align')).toBe('center');
      expect($cells0.eq(1).css('text-align')).toBe('center');
      expect($cells1.eq(1).css('text-align')).toBe('center');

      expect($headerItem2.css('text-align')).toBe('right');
      expect($cells0.eq(2).css('text-align')).toBe('right');
      expect($cells1.eq(2).css('text-align')).toBe('right');
    });

  });

  describe("Check Rows", function() {
    it("check two rows in multicheckable table", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.multiCheck = true;
      table.checkable = true;
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = [];
      for(var i = 0; i<rows.length; i++){
        if(rows[i].checked){
          checkedRows.push(rows[i]);
        }
      }
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      table.checkRow(rows[4], true, true);

      checkedRows = [];
      for(var j = 0; j<rows.length; j++){
        if(rows[j].checked){
          checkedRows.push(rows[j]);
        }
      }
      expect(checkedRows.length).toBe(2);

      table.checkRow(rows[4], false, true);

      checkedRows = [];
      for(var z = 0; z<rows.length; z++){
        if(rows[z].checked){
          checkedRows.push(rows[z]);
        }
      }
      expect(checkedRows.length).toBe(1);
    });


    it("check two rows in multicheckable table, only one should be checked", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.multiCheck = false;
      table.checkable = true;
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = [];
      for(var i = 0; i<rows.length; i++){
        if(rows[i].checked){
          checkedRows.push(rows[i]);
        }
      }
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      table.checkRow(rows[4], true, true);

      checkedRows = [];
      for(var j = 0; j<rows.length; j++){
        if(rows[j].checked){
          checkedRows.push(rows[j]);
        }
      }
      expect(checkedRows.length).toBe(1);

      table.checkRow(rows[4], false, true);

      checkedRows = [];
      for(var z = 0; z<rows.length; z++){
        if(rows[z].checked){
          checkedRows.push(rows[z]);
        }
      }
      expect(checkedRows.length).toBe(0);
    });

    it("try to check a row in uncheckable table", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.multiCheck = false;
      table.checkable = false;
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = [];
      for(var i = 0; i<rows.length; i++){
        if(rows[i].checked){
          checkedRows.push(rows[i]);
        }
      }
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      checkedRows = [];
      for(var j = 0; j<rows.length; j++){
        if(rows[j].checked){
          checkedRows.push(rows[j]);
        }
      }
      expect(checkedRows.length).toBe(0);
    });

    it("try to check a disabled row ", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.multiCheck = false;
      table.checkable = false;
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = [];
      for(var i = 0; i<rows.length; i++){
        if(rows[i].checked){
          checkedRows.push(rows[i]);
        }
      }
      expect(checkedRows.length).toBe(0);
      rows[0].enabled = false;
      table.checkRow(rows[0], true, true);
      checkedRows = [];
      for(var j = 0; j<rows.length; j++){
        if(rows[j].checked){
          checkedRows.push(rows[j]);
        }
      }
      expect(checkedRows.length).toBe(0);
    });

    it("try to check a row in disabled table", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.multiCheck = true;
      table.checkable = true;
      table.enabled = false;
      table.render(session.$entryPoint);

      var rows = table.rows;
      var checkedRows = [];
      for(var i = 0; i<rows.length; i++){
        if(rows[i].checked){
          checkedRows.push(rows[i]);
        }
      }
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true, true);
      checkedRows = [];
      for(var j = 0; j<rows.length; j++){
        if(rows[j].checked){
          checkedRows.push(rows[j]);
        }
      }
      expect(checkedRows.length).toBe(0);
    });

  });

  describe("selectRowsByIds", function() {

    it("selects rows and unselects others", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, [model.rows[0].id, model.rows[4].id]);
      helper.selectRowsAndAssert(table, [model.rows[2].id]);
    });

    it("sends selection event containing rowIds", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rowIds = ['0', '4'];
      table.selectRowsByIds(rowIds);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);

      var event = new scout.Event(table.id, 'rowsSelected', {
        rowIds: rowIds
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updates cached model", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rowIds = [model.rows[0].id, model.rows[4].id];
      table.selectRowsByIds(rowIds);

      expect(table.selectedRowIds).toEqual(rowIds);
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
      helper.assertSelection(table, helper.getRowIds(model.rows));
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(model.rows));
    });

    it("selects none if all are selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, helper.getRowIds(model.rows));

      table.toggleSelection();
      helper.assertSelection(table, []);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, []);

      table.toggleSelection();
      helper.assertSelection(table, helper.getRowIds(model.rows));
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(model.rows));
    });
  });

  describe("resizeColumn", function() {

    it("updates column model and sends resize event ", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $colHeaders = table.header.$container.find('.header-item');
      var $header0 = $colHeaders.eq(0);

      expect(table.columns[0].width).not.toBe(100);

      table.resizeColumn($header0, 100, 150);

      expect(table.columns[0].width).toBe(100);

      sendQueuedAjaxCalls();
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

      var $colHeaders = table.header.$container.find('.header-item');
      var $header0 = $colHeaders.eq(0);

      table.resizeColumn($header0, 50, 100, true);
      table.resizeColumn($header0, 100, 150, true);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    it("sends resize event when resizing is finished", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $colHeaders = table.header.$container.find('.header-item');
      var $header0 = $colHeaders.eq(0);

      table.resizeColumn($header0, 50, 100, true);
      table.resizeColumn($header0, 100, 150, true);
      table.resizeColumn($header0, 150, 200);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(mostRecentJsonRequest().events.length).toBe(1);

      var event = new scout.Event(table.id, 'columnResized', {
        columnId: table.columns[0].id,
        width: 150
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
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
      $colHeaders = table.header.$container.find('.header-item');
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
      spyOn(scout.device, "supportsInternationalization").and.returnValue(true);
      // Make sure sorting is not executed because it does not work with phantomJS
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

    describe("sorting", function() {

      it("sorts text columns considering locale (if browser supports it)", function() {
        if (!scout.device.supportsInternationalization()) {
          return;
        }

        var model = helper.createModelSingleColumnByTexts(1, ['Österreich', 'Italien', 'Zypern']);
        var table = helper.createTable(model);
        column0 = model.columns[0];
        table.render(session.$entryPoint);

        table.sort(column0, 'desc');
        helper.assertTextsInCells(table.rows, 0, ['Zypern', 'Österreich', 'Italien']);

        table.sort(column0, 'asc');
        helper.assertTextsInCells(table.rows, 0, ['Italien', 'Österreich', 'Zypern']);

        session.locale = new LocaleSpecHelper().createLocale('sv');

        table.sort(column0, 'desc');
        helper.assertTextsInCells(table.rows, 0, ['Österreich', 'Zypern', 'Italien']);

        table.sort(column0, 'asc');
        helper.assertTextsInCells(table.rows, 0, ['Italien', 'Zypern', 'Österreich']);
      });

      it("sorts number columns", function() {
        var model = helper.createModelSingleColumnByValues([11, 1, 8], 'number');
        var table = helper.createTable(model);
        column0 = model.columns[0];
        table.render(session.$entryPoint);

        table.sort(column0, 'desc');
        helper.assertValuesInCells(table.rows, 0, [11, 8, 1]);

        table.sort(column0, 'asc');
        helper.assertValuesInCells(table.rows, 0, [1, 8, 11]);
      });

      it("sorts date columns", function() {
        var model = helper.createModelSingleColumnByValues([new Date('2012-08-10'), new Date('2014-03-01'), new Date('1999-01-10')], 'date');
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

  describe("group", function() {
    var model, table, column0, column1, rows, columns;
    var $colHeaders, $header0, $header1;

    function prepareTable() {
      columns = [helper.createModelColumn(createUniqueAdapterId(), 'col1'),
                 helper.createModelColumn(createUniqueAdapterId(), 'col2', 'number')];
      rows = helper.createModelRows(2, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
      column0 = model.columns[0];
      column1 = model.columns[1];
    }

    function render(table) {
      table.render(session.$entryPoint);
      $colHeaders = table.header.$container.find('.header-item');
      $header0 = $colHeaders.eq(0);
      $header1 = $colHeaders.eq(1);
    }

    it("creates a sum row", function() {
      prepareTable();
      render(table);

      expect(table.$sumRows().length).toBe(0);
      table.group(column0, true);
      expect(table.$sumRows().length).toBe(1);
    });

    it("creates a sum row, even if there are filtered rows", function() {
      prepareTable();
      render(table);

      expect(table.$sumRows().length).toBe(0);
      table.hideRow(table.$rows().eq(2));
      table.group(column0, true);
      expect(table.$sumRows().length).toBe(1);
    });

    it("sums up numbers in a number column", function() {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      render(table);

      table.group(column0, true);
      var $sumCell = table.$sumRows().eq(0).children().eq(1);
      expect($sumCell.text()).toBe('6');
    });

    it("sums up numbers in a number column but only on filtered rows", function() {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      render(table);

      table.hideRow(table.$rows().eq(2));
      table.group(column0, true);
      var $sumCell = table.$sumRows().eq(0).children().eq(1);
      expect($sumCell.text()).toBe('3');
    });

    it("sums up numbers in a number column and considers format pattern", function() {
      prepareTable();
      rows[0].cells[1].value = 1000;
      rows[1].cells[1].value = 1000;
      rows[2].cells[1].value = 2000;
      render(table);
      column1.format = '#.00';

      table.group(column0, true);
      var $sumCell = table.$sumRows().eq(0).children().eq(1);
      expect($sumCell.text()).toBe('4000.00');
    });

  });

  describe("row click", function() {

    function clickRowAndAssertSelection(table, $row) {
      $row.triggerClick();

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(1);

      var $selectedRow = $selectedRows.first();
      expect($selectedRow).toEqual($row);
    }

    it("selects row and unselects others", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      var $rows = table.$data.children('.table-row');
      clickRowAndAssertSelection(table, $rows.eq(1));
      clickRowAndAssertSelection(table, $rows.eq(2));

      helper.selectRowsAndAssert(table, [model.rows[0].id, model.rows[4].id]);
      clickRowAndAssertSelection(table, $rows.eq(4));
    });

    it("sends click and selection events", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$data.children('.table-row').first();
      $row.triggerClick();

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClicked', 'rowsSelected']);
    });

    it("sends only click if row already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$data.children('.table-row').first();
      clickRowAndAssertSelection(table, $row);
      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClicked', 'rowsSelected']);

      jasmine.Ajax.requests.reset();
      clickRowAndAssertSelection(table, $row);
      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClicked']);
    });

  });

  describe("row double click", function() {
    it("sends clicked, selection and row action events", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$data.children('.table-row').first();
      $row.triggerDoubleClick();

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClicked', 'rowsSelected', 'rowAction']);
    });
  });

  describe("right click on row", function() {

    it("opens context menu", function() {
      var model = helper.createModelFixture(2, 2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var menuModel = helper.createMenuModel('menu');
      //register adapter
      helper.menuHelper.createMenu(menuModel);
      table.menus = [session.getModelAdapter(menuModel.id)];
      var $row0 = table.$data.children('.table-row').eq(0);
      $row0.triggerContextMenu();

      sendQueuedAjaxCalls();

      var $menu = helper.getDisplayingContextMenu(table);
      expect($menu.length).toBeTruthy();
    });

    it("and sends aboutToShow for every menu item", function() {
      var model = helper.createModelFixture(2, 2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var menuModel = helper.createMenuModel('menu');
      //register adapter
      helper.menuHelper.createMenu(menuModel);
      table.menus = [session.getModelAdapter(menuModel.id)];
      var $row0 = table.$data.children('.table-row').eq(0);
      $row0.triggerContextMenu();

      sendQueuedAjaxCalls();

      //Again, since the previous responses are awaited before opening the context menu, see showContextMenuWithWait in menus.js
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      var event = new scout.Event(menuModel.id, 'aboutToShow');
      expect(requestData).toContainEvents(event);
    });

  });

  describe("row mouse down / move / up", function() {

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

      expect($rows).not.toHaveClass('selected');

      $row0.triggerMouseDown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.triggerMouseUp();

      expect($row0).toHaveClass('selected');
      expect($row1).toHaveClass('selected');
      expect($row2).toHaveClass('selected');
      expect($row3).not.toHaveClass('selected');
      expect($row4).not.toHaveClass('selected');
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
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.triggerMouseUp();

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly('rowsSelected');

      var event = new scout.Event(table.id, 'rowsSelected', {
        rowIds: [model.rows[0].id, model.rows[1].id, model.rows[2].id]
      });
      expect(requestData).toContainEvents(event);
    });

    it("only selects first row if mouse move selection or multi selection is disabled", function() {
      var model = helper.createModelFixture(2, 4);
      var table = helper.createTable(model);
      table.selectionHandler.mouseMoveSelectionEnabled = false;
      verifyMouseMoveSelectionIsDisabled(model, table);

      model = helper.createModelFixture(2, 4);
      table = helper.createTable(model);
      table.multiSelect = false;
      verifyMouseMoveSelectionIsDisabled(model, table);
    });

    function verifyMouseMoveSelectionIsDisabled(model, table) {
      table.render(session.$entryPoint);

      var $rows = table.$data.children('.table-row');
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);

      expect($rows).not.toHaveClass('selected');

      $row0.triggerMouseDown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.triggerMouseUp();

      expect($row0).toHaveClass('selected');
      expect($row1).not.toHaveClass('selected');
      expect($row2).not.toHaveClass('selected');
      expect($row3).not.toHaveClass('selected');

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      var event = new scout.Event(table.id, 'rowsSelected', {
        rowIds: [model.rows[0].id]
      });
      expect(requestData).toContainEvents(event);
    }

  });

  describe("moveColumn", function() {

    it("moves column from oldPos to newPos", function() {
      var model = helper.createModelFixture(3, 2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $colHeaders = table.header.$container.find('.header-item');
      var $header0 = $colHeaders.eq(0);
      var $header1 = $colHeaders.eq(1);
      var $header2 = $colHeaders.eq(2);

      expect(table.header.getColumnViewIndex($header0)).toBe(0);
      expect(table.header.getColumnViewIndex($header1)).toBe(1);
      expect(table.header.getColumnViewIndex($header2)).toBe(2);

      table.moveColumn($header0, 0, 2);

      expect(table.header.getColumnViewIndex($header1)).toBe(0);
      expect(table.header.getColumnViewIndex($header2)).toBe(1);
      expect(table.header.getColumnViewIndex($header0)).toBe(2);

      table.moveColumn($header2, 1, 0);

      expect(table.header.getColumnViewIndex($header2)).toBe(0);
      expect(table.header.getColumnViewIndex($header1)).toBe(1);
      expect(table.header.getColumnViewIndex($header0)).toBe(2);
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

      it("deletes single rows from model html document", function() {
        table.render(session.$entryPoint);

        expect(table.$rows().length).toBe(3);

        var message = {
          events: [createRowsDeletedEvent(model, [row0.id])]
        };
        session._processSuccessResponse(message);

        expect(table.$rows().length).toBe(2);
        expect(table.$data.find('#'+row0.id).length).toBe(0);
        expect(table.$data.find('#'+row1.id).length).toBe(1);
        expect(table.$data.find('#'+row2.id).length).toBe(1);

        message = {
          events: [createRowsDeletedEvent(model, [row1.id, row2.id])]
        };
        session._processSuccessResponse(message);

        expect(table.$rows().length).toBe(0);
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
        expect($rows.eq(0).attr('id')).toBe(model.rows[2].id);
        expect($rows.eq(1).attr('id')).toBe(model.rows[1].id);
        expect($rows.eq(2).attr('id')).toBe(model.rows[0].id);
      });

      it("does not animate ordering for newly inserted rows", function() {
        table.render(session.$entryPoint);
        expect(table.rows.length).toBe(3);

        var newRows = [
          helper.createModelRow('3', helper.createModelCells(2)),
          helper.createModelRow('4', helper.createModelCells(2))
        ];

        //Insert new rows and switch rows 0 and 1
        var message = {
          events: [
            createRowsInsertedEvent(model, newRows),
            createRowOrderChangedEvent(model, [row1.id, row0.id, newRows[0].id, newRows[1].id, row2.id])
          ]
        };
        session._processSuccessResponse(message);

        //Checkif rows were inserted
        expect(table.rows.length).toBe(5);

        //Check if animation is not done for the inserted rows
        //The animation should be done for the other rows (row0 and 1 are switched -> visualize)
        var $rows = table.$rows();
        $rows.each(function() {
          var $row = $(this);
          var oldTop = $row.data('old-top');
          var rowId = $row.attr('id');
          if (rowId  === newRows[0].id || rowId === newRows[1].id) {
            expect(oldTop).toBeUndefined();
          } else {
            expect(oldTop).toBeDefined();
          }
        });
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
        expect(table.columns[0]).toBe(column2);
        expect(table.columns[1]).toBe(column1);
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

      it("redraws the columns to reflect column order changes", function() {
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
          events: [createColumnStructureChangedEvent(model, [column2, column1])]
        };
        session._processSuccessResponse(message);

        //Check column header order
        $colHeaders = table.header.findHeaderItems();
        expect($colHeaders.length).toBe(2);
        expect($colHeaders.eq(0).data('column')).toBe(column2);
        expect($colHeaders.eq(1).data('column')).toBe(column1);

        //Check cells order
        $rows = table.$rows();
        $cells0 = $rows.eq(0).find('.table-cell');
        $cells1 = $rows.eq(1).find('.table-cell');
        expect($cells0.eq(0).text()).toBe('2');
        expect($cells0.eq(1).text()).toBe('1');
        expect($cells1.eq(0).text()).toBe('2');
        expect($cells1.eq(1).text()).toBe('1');
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

      //TODO CGU fails because css is not applied -> include css files in SpecRunner
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

        column1 = helper.createModelColumn(column1.id, 'newText1');
        column1.sortActive = true;
        column1.sortAscending = true;
        column2 = helper.createModelColumn(column2.id, 'newText2');

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

        column1 = helper.createModelColumn(column1.id, 'newText1');
        column1.sortActive = true;
        column1.sortAscending = true;
        column2 = helper.createModelColumn(column2.id, 'newText2');

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
          adapterData : createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {menus: [menu1.id, menu2.id]})]
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
          adapterData : createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {menus: [menu1.id, menu2.id]})]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1]);

        message = {
          events: [createPropertyChangeEvent(table, {menus: [menu2.id]})]
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
          adapterData : createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {menus: [menu1.id, menu2.id]})]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0]);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1]);

        message = {
          adapterData : createAdapterData(menu3),
          events: [createPropertyChangeEvent(table, {menus: [menu2.id, menu3.id]})]
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
