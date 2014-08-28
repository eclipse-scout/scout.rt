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
      var $rows = table.$container.find('.table-row');
      var $cells0 = $rows.eq(0).find('.table-cell');
      var $cells1 = $rows.eq(1).find('.table-cell');

      expect($headerItem0.css('text-align')).toBe('start');
      expect($cells0.eq(0).css('text-align')).toBe('start');
      expect($cells1.eq(0).css('text-align')).toBe('start');
      expect($headerItem1.css('text-align')).toBe('center');
      expect($cells0.eq(1).css('text-align')).toBe('center');
      expect($cells1.eq(1).css('text-align')).toBe('center');
      expect($headerItem2.css('text-align')).toBe('end');
      expect($cells0.eq(2).css('text-align')).toBe('end');
      expect($cells1.eq(2).css('text-align')).toBe('end');
    });

  });

  describe("insertRows", function() {

    it("inserts rows at the end of the table", function() {
      var model = helper.createModelFixture(2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      expect(table.$dataScroll.children().length).toBe(0);

      var rows = helper.createModelRows(2, 5);
      table.insertRows(rows);

      expect(table.$dataScroll.children().length).toBe(5);

      rows = helper.createModelRows(2, 8);
      table.insertRows(rows);

      expect(table.$dataScroll.children().length).toBe(5 + 8);
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

  describe("onModelAction", function() {

    it("processes insertion events", function() {
      var model = helper.createModelFixture(2);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      spyOn(table, 'insertRows');

      var rows = helper.createModelRows(2, 5);
      var event = new scout.Event('rowsInserted', table.id, {
        "rows": rows
      });
      table.onModelAction(event);

      expect(table.insertRows).toHaveBeenCalledWith(rows);
    });

    it("processes selection events", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      spyOn(table, 'selectRowsByIds');

      var rowIds = ['0', '4'];
      var event = new scout.Event('rowsSelected', table.id, {
        "rowIds": rowIds
      });
      table.onModelAction(event);

      expect(table.selectRowsByIds).toHaveBeenCalledWith(rowIds);
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
