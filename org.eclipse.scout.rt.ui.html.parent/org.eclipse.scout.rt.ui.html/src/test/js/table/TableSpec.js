/* global TableSpecHelper */
describe("Table", function() {
  var session;
  var helper;
  var menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
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

    it("draws a table header", function() {
      var model = helper.createModelFixture(2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      expect(table._header).not.toBeUndefined();
    });

    it("considers horizontal alignment", function() {
      var model = helper.createModelFixture(3, 2);
      model.columns[1].horizontalAlignment = 0;
      model.columns[2].horizontalAlignment = 1;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $headerItems = table._$header.find('.header-item');
      var $headerItem0 = $headerItems.eq(0);
      var $headerItem1 = $headerItems.eq(1);
      var $headerItem2 = $headerItems.eq(2);
      var $rows = table.findRows();
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

  describe("selectRowsByIds", function() {

    it("selects rows and unselects others", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.findSelectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, ['0', '4']);
      helper.selectRowsAndAssert(table, ['2']);
    });

    it("sends selection event containing rowIds", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rowIds = ['0', '4'];
      table.selectRowsByIds(rowIds);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);

      var event = new scout.Event('rowsSelected', table.id, {
        "rowIds": rowIds
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("updates cached model", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var rowIds = ['0', '4'];
      table.selectRowsByIds(rowIds);

      expect(table.selectedRowIds).toEqual(rowIds);
    });

  });

  describe("toggle selection", function() {
    it("selects all if not all are selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.findSelectedRows();
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

      var $selectedRows = table.findSelectedRows();
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

    it("sends resize event", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $colHeaders = table._$header.find('.header-item');
      var $header0 = $colHeaders.eq(0);

      table.resizeColumn($header0, 100, 150);

      sendQueuedAjaxCalls();

      var event = new scout.Event('columnResized', table.id, {
        "columnId": table.columns[0].id,
        "width": 100
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("does not send resize event when resizing is in progress", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $colHeaders = table._$header.find('.header-item');
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

      var $colHeaders = table._$header.find('.header-item');
      var $header0 = $colHeaders.eq(0);

      table.resizeColumn($header0, 50, 100, true);
      table.resizeColumn($header0, 100, 150, true);
      table.resizeColumn($header0, 150, 200);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(mostRecentJsonRequest().events.length).toBe(1);

      var event = new scout.Event('columnResized', table.id, {
        "columnId": table.columns[0].id,
        "width": 150
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

  describe("row click", function() {

    function clickRowAndAssertSelection(table, $row) {
      $row.triggerClick();

      var $selectedRows = table.findSelectedRows();
      expect($selectedRows.length).toBe(1);

      var $selectedRow = $selectedRows.first();
      expect($selectedRow).toEqual($row);
    }

    it("selects row and unselects others", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $selectedRows = table.findSelectedRows();
      expect($selectedRows.length).toBe(0);

      var $rows = table.$dataScroll.children();
      clickRowAndAssertSelection(table, $rows.eq(1));
      clickRowAndAssertSelection(table, $rows.eq(2));

      helper.selectRowsAndAssert(table, ['0', '4']);
      clickRowAndAssertSelection(table, $rows.eq(4));
    });

    it("sends click and selection events", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$dataScroll.children().first();
      $row.triggerClick();

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClicked', 'rowsSelected']);
    });

    it("sends only click if row already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $row = table.$dataScroll.children().first();
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

      var $row = table.$dataScroll.children().first();
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

      var menuId = createUniqueAdapterId();
      var menuModel = helper.createMenuModel(menuId, 'menu');
      //register adapter
      helper.menuHelper.createMenu(menuModel);
      table.menus = session.getOrCreateModelAdapters([menuId], table);
      var $row0 = table.$dataScroll.children().eq(0);
      $row0.triggerContextMenu();

      sendQueuedAjaxCalls();

      var $menu = helper.getDisplayingContextMenu(table);
      expect($menu.length).toBeTruthy();
    });

    it("and sends aboutToShow for every menu item", function() {
      var model = helper.createModelFixture(2, 2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var menuId = createUniqueAdapterId();
      var menuModel = helper.createMenuModel(menuId, 'menu');
      //register adapter
      helper.menuHelper.createMenu(menuModel);
      table.menus = session.getOrCreateModelAdapters([menuId], table);
      var $row0 = table.$dataScroll.children().eq(0);
      $row0.triggerContextMenu();

      sendQueuedAjaxCalls();

      //Again, since the previous responses are awaited before opening the context menu, see showContextMenuWithWait in menus.js
      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      var event = new scout.Event(scout.Menu.EVENT_ABOUT_TO_SHOW, menuModel.id);
      expect(requestData).toContainEvents(event);
    });

  });

  describe("row mouse down / move / up", function() {

    it("selects multiple rows", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$dataScroll.children();
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);
      var $row4 = $rows.eq(4);

      expect($rows).not.toHaveClass('row-selected');

      $row0.triggerMouseDown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.triggerMouseUp();

      expect($row0).toHaveClass('row-selected');
      expect($row1).toHaveClass('row-selected');
      expect($row2).toHaveClass('row-selected');
      expect($row3).not.toHaveClass('row-selected');
      expect($row4).not.toHaveClass('row-selected');
    });

    it("only sends selection event, no click", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$dataScroll.children();
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);

      expect($rows).not.toHaveClass('row-selected');

      $row0.triggerMouseDown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.triggerMouseUp();

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly('rowsSelected');

      var event = new scout.Event('rowsSelected', table.id, {
        "rowIds": ['0', '1', '2']
      });
      expect(requestData).toContainEvents(event);
    });

    it("only selects first row if mouse move selection or multi selection is disabled", function() {
      var model = helper.createModelFixture(2, 4);
      var table = helper.createTable(model);
      table.selectionHandler.mouseMoveSelectionEnabled = false;
      verifyMouseMoveSelectionIsDisabled(table);

      model = helper.createModelFixture(2, 4);
      table = helper.createTable(model);
      table.multiSelect = false;
      verifyMouseMoveSelectionIsDisabled(table);
    });

    function verifyMouseMoveSelectionIsDisabled(table) {
      table.render(session.$entryPoint);

      var $rows = table.$dataScroll.children();
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);
      var $row2 = $rows.eq(2);
      var $row3 = $rows.eq(3);

      expect($rows).not.toHaveClass('row-selected');

      $row0.triggerMouseDown();
      $row1.trigger('mousemove');
      $row2.trigger('mousemove');
      $row2.triggerMouseUp();

      expect($row0).toHaveClass('row-selected');
      expect($row1).not.toHaveClass('row-selected');
      expect($row2).not.toHaveClass('row-selected');
      expect($row3).not.toHaveClass('row-selected');

      sendQueuedAjaxCalls();

      var requestData = mostRecentJsonRequest();
      var event = new scout.Event('rowsSelected', table.id, {
        "rowIds": ['0']
      });
      expect(requestData).toContainEvents(event);
    }

  });

  describe("moveColumn", function() {

    it("moves column from oldPos to newPos", function() {
      var model = helper.createModelFixture(3, 2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$dataScroll.children();
      var $row0 = $rows.eq(0);
      var $row1 = $rows.eq(1);

      var $colHeaders = table._$header.find('.header-item');
      var $header0 = $colHeaders.eq(0);
      var $header1 = $colHeaders.eq(1);
      var $header2 = $colHeaders.eq(2);

      expect(table._header.getColumnViewIndex($header0)).toBe(0);
      expect(table._header.getColumnViewIndex($header1)).toBe(1);
      expect(table._header.getColumnViewIndex($header2)).toBe(2);

      table.moveColumn($header0, 0, 2);

      expect(table._header.getColumnViewIndex($header1)).toBe(0);
      expect(table._header.getColumnViewIndex($header2)).toBe(1);
      expect(table._header.getColumnViewIndex($header0)).toBe(2);

      table.moveColumn($header2, 1, 0);

      expect(table._header.getColumnViewIndex($header2)).toBe(0);
      expect(table._header.getColumnViewIndex($header1)).toBe(1);
      expect(table._header.getColumnViewIndex($header0)).toBe(2);
    });

  });

  describe("onModelAction", function() {

    function createRowsInsertedEvent(model, rows) {
      return {
        id: model.id,
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
      var event = new scout.Event('rowsSelected', table.id, {
        "rowIds": rowIds
      });
      table.onModelAction(event);

      expect(table._onRowsSelected).toHaveBeenCalledWith(rowIds);
    });

    describe("rowsDeleted event", function() {
      var model, table, row0, row1, row2;

      function createRowsDeletedEvent(model, rowIds) {
        return {
          id: model.id,
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

        expect(table.findRows().length).toBe(3);

        var message = {
          events: [createRowsDeletedEvent(model, [row0.id])]
        };
        session._processSuccessResponse(message);

        expect(table.findRows().length).toBe(2);
        expect(table.findRowById(row0.id).length).toBe(0);
        expect(table.findRowById(row1.id).length).toBe(1);
        expect(table.findRowById(row2.id).length).toBe(1);

        message = {
          events: [createRowsDeletedEvent(model, [row1.id, row2.id])]
        };
        session._processSuccessResponse(message);

        expect(table.findRows().length).toBe(0);
      });

    });

    describe("allRowsDeleted event", function() {
      var model, table, row0, row1, row2;

      function createAllRowsDeletedEvent(model, rowIds) {
        return {
          id: model.id,
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

        expect(table.findRows().length).toBe(3);

        var message = {
          events: [createAllRowsDeletedEvent(model)]
        };
        session._processSuccessResponse(message);

        expect(table.findRows().length).toBe(0);
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

        rows = helper.createModelRows(2, 3);
        message = {
          events: [createRowsInsertedEvent(model, rows)]
        };
        session._processSuccessResponse(message);

        expect(table.rows.length).toBe(5 + 3);
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
          id: model.id,
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

        var $rows = table.findRows();
        expect($rows.eq(0).attr('id')).toBe('2');
        expect($rows.eq(1).attr('id')).toBe('1');
        expect($rows.eq(2).attr('id')).toBe('0');
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
        var $rows = table.findRows();
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
          id: model.id,
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

        var $colHeaders = table._header.findHeaderItems();
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
        $colHeaders = table._header.findHeaderItems();
        expect($colHeaders.eq(0).text()).toBe(column0.text);
        expect($colHeaders.eq(1).text()).toBe(column1.text);
        expect($colHeaders.eq(2).text()).toBe(column2.text);
      });

      it("redraws the columns to reflect column order changes", function() {
        table.render(session.$entryPoint);

        var $colHeaders = table._header.findHeaderItems();
        expect($colHeaders.length).toBe(3);
        expect($colHeaders.eq(0).data('column')).toBe(column0);
        expect($colHeaders.eq(1).data('column')).toBe(column1);
        expect($colHeaders.eq(2).data('column')).toBe(column2);

        var $rows = table.findRows();
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
        $colHeaders = table._header.findHeaderItems();
        expect($colHeaders.length).toBe(2);
        expect($colHeaders.eq(0).data('column')).toBe(column2);
        expect($colHeaders.eq(1).data('column')).toBe(column1);

        //Check cells order
        $rows = table.findRows();
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
          id: model.id,
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

        var $colHeaders = table._header.findHeaderItems();
        expect($colHeaders.length).toBe(3);
        expect($colHeaders.eq(0).data('column')).toBe(column0);
        expect($colHeaders.eq(1).data('column')).toBe(column1);
        expect($colHeaders.eq(2).data('column')).toBe(column2);

        var $rows = table.findRows();
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
        $colHeaders = table._header.findHeaderItems();
        expect($colHeaders.length).toBe(3);
        expect($colHeaders.eq(0).data('column')).toBe(column2);
        expect($colHeaders.eq(1).data('column')).toBe(column0);
        expect($colHeaders.eq(2).data('column')).toBe(column1);

        //Check cells order
        $rows = table.findRows();
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
//        var $colHeaders = table._header.findHeaderItems();
//
//        var $clickedHeader = $colHeaders.eq(0);
//        $clickedHeader.triggerClick();
//
//        var tableHeaderMenu = table._header._tableHeaderMenu;
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
          id: model.id,
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

        var $colHeaders = table._header.findHeaderItems();
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

        $colHeaders = table._header.findHeaderItems();
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

        expect(table._header).toBeDefined();
        expect(table._$header.is(':visible')).toBe(true);

        var event = new scout.Event('property', table.id, {
          "properties": {
            "headerVisible": false
          }
        });
        table.onModelPropertyChange(event);

        expect(table._header).toBeDefined();
        expect(table._$header.is(':visible')).toBe(false);
      });

    });

  });

});
