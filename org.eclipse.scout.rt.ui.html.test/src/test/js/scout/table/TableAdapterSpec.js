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
/* global removePopups */
describe("TableAdapter", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new scout.LocaleSpecHelper().createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
    helper = new scout.TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    helper.resetIntlCollator();
  });


  describe("onModelAction", function() {

    function createRowsInsertedEvent(model, rows) {
      return {
        target: model.id,
        rows: rows,
        type: 'rowsInserted'
      };
    }

    describe("rowsSelected event", function() {

      function createRowsSelectedEvent(model, rowIds) {
        return {
          target: model.id,
          rowIds: rowIds,
          type: 'rowsSelected'
        };
      }

      it("calls selectRows", function() {
        var model = helper.createModelFixture(2, 5);
        var adapter = helper.createTableAdapter(model);
        var table = adapter.createWidget(model, session.desktop);
        table.render(session.$entryPoint);

        spyOn(table, 'selectRows');

        var rowIds = [table.rows[0].id, table.rows[4].id];
        var event = createRowsSelectedEvent(model, rowIds);
        adapter.onModelAction(event);
        expect(table.selectRows).toHaveBeenCalledWith([table.rows[0], table.rows[4]]);
      });
    });

    describe("rowsDeleted event", function() {
      var model, table, adapter, rows, row0, row1, row2;

      function createRowsDeletedEvent(model, rowIds) {
        return {
          target: model.id,
          rowIds: rowIds,
          type: 'rowsDeleted'
        };
      }

      beforeEach(function() {
        model = helper.createModelFixture(2, 3);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
        rows = table.rows;
        row0 = model.rows[0];
        row1 = model.rows[1];
        row2 = model.rows[2];
      });

      it("calls deleteRows", function() {
        spyOn(table, 'deleteRows');

        var rowIds = [rows[0].id, rows[2].id];
        var event = createRowsDeletedEvent(model, rowIds);
        adapter.onModelAction(event);
        expect(table.deleteRows).toHaveBeenCalledWith([rows[0], rows[2]]);
      });

    });

    describe("allRowsDeleted event", function() {
      var model, table, adapter, row0, row1, row2;

      function createAllRowsDeletedEvent(model, rowIds) {
        return {
          target: model.id,
          type: 'allRowsDeleted'
        };
      }

      beforeEach(function() {
        model = helper.createModelFixture(2, 3);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
      });

      it("calls deleteAllRows", function() {
        spyOn(table, 'deleteAllRows');

        var event = createAllRowsDeletedEvent(model);
        adapter.onModelAction(event);
        expect(table.deleteAllRows).toHaveBeenCalled();
      });

    });

    describe("rowsInserted event", function() {
      var model, table, adapter;

      beforeEach(function() {
        model = helper.createModelFixture(2);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
      });

      it("calls insertRows", function() {
        spyOn(table, 'insertRows');

        var rows = helper.createModelRows(2, 5);
        var event = createRowsInsertedEvent(model, rows);
        adapter.onModelAction(event);
        expect(table.insertRows).toHaveBeenCalledWith(rows, true);
      });
    });

    describe("rowOrderChanged event", function() {
      var model, table, adapter, row0, row1, row2;

      beforeEach(function() {
        model = helper.createModelFixture(2, 3);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
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

      it("calls updateRowOrder", function() {
        spyOn(table, 'updateRowOrder');

        var event = createRowOrderChangedEvent(model, [row2.id, row1.id, row0.id]);
        adapter.onModelAction(event);
        expect(table.updateRowOrder).toHaveBeenCalledWith([row2, row1, row0]);
      });

      it("does not animate ordering for newly inserted rows", function() {
        table.render(session.$entryPoint);
        expect(table.rows.length).toBe(3);

        var newRows = [
          helper.createModelRow(null, helper.createModelCells(2)),
          helper.createModelRow(null, helper.createModelCells(2))
        ];

        // Insert new rows and switch rows 0 and 1
        var message = {
          events: [
            createRowsInsertedEvent(model, newRows),
            createRowOrderChangedEvent(model, [row1.id, row0.id, newRows[0].id, newRows[1].id, row2.id])
          ]
        };
        session._processSuccessResponse(message);

        // Check if rows were inserted
        expect(table.rows.length).toBe(5);

        // Check if animation is not done for the inserted rows
        // The animation should be done for the other rows (row0 and 1 are switched -> visualize)
        var $rows = table.$rows();
        $rows.each(function() {
          var $row = $(this);
          var rowId = $row.data('row').id;
          if (rowId === newRows[0].id || rowId === newRows[1].id) {
            expect($row.is(':animated')).toBe(false);
          } else {
            expect($row.is(':animated')).toBe(true);
          }
        });
      });

    });

    describe("rowsUpdated event", function() {
      var model, table, adapter, row0;

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
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
      });

      it("calls updateRows", function() {
        spyOn(table, 'updateRows');

        var row = {
          id: table.rows[0].id,
          cells: ['newCellText0', 'newCellText1']
        };
        var event = createRowsUpdatedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.updateRows).toHaveBeenCalledWith([row]);
      });

    });

    describe("columnStructureChanged event", function() {
      var model, table, adapter, column0, column1, column2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 2);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
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

      it("calls updateColumnStructure", function() {
        spyOn(table, 'updateColumnStructure');
        var message = {
          events: [createColumnStructureChangedEvent(model, [column2, column1])]
        };
        session._processSuccessResponse(message);
        expect(table.updateColumnStructure).toHaveBeenCalledWith([column2, column1]);
      });
    });

    describe("columnOrderChanged event", function() {
      var model, table, adapter, column0, column1, column2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 2);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
        column0 = table.columns[0];
        column1 = table.columns[1];
        column2 = table.columns[2];
      });

      function createColumnOrderChangedEvent(model, columnIds) {
        return {
          target: model.id,
          columnIds: columnIds,
          type: 'columnOrderChanged'
        };
      }

      it("calls updateColumnOrder", function() {
        spyOn(table, 'updateColumnOrder');
        var message = {
          events: [createColumnOrderChangedEvent(model, [column2.id, column0.id, column1.id])]
        };
        session._processSuccessResponse(message);
        expect(table.updateColumnOrder).toHaveBeenCalledWith([column2, column0, column1]);
      });

    });

    describe("columnHeadersUpdated event", function() {
      var model, table, adapter, column0, column1, column2;

      beforeEach(function() {
        model = helper.createModelFixture(3, 2);
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

      it("calls updateColumnHeaders", function() {
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
        spyOn(table, 'updateColumnHeaders');

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
        expect(table.updateColumnHeaders).toHaveBeenCalledWith([column1, column2]);
      });
    });
  });

  describe("onModelPropertyChange", function() {

    describe("menus", function() {

      it("creates and registers menu adapters", function() {
        var model = helper.createModelFixture(2);
        var adapter = helper.createTableAdapter(model);
        var table = adapter.createWidget(model, session.desktop);
        var menu1 = helper.createMenuModel();
        var menu2 = helper.createMenuModel();

        var message = {
          adapterData: createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0].remoteAdapter);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1].remoteAdapter);
      });

      it("destroys the old menus", function() {
        var model = helper.createModelFixture(2);
        var adapter = helper.createTableAdapter(model);
        var table = adapter.createWidget(model, session.desktop);
        var menu1 = helper.createMenuModel();
        var menu2 = helper.createMenuModel();

        var message = {
          adapterData: createAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0].remoteAdapter);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1].remoteAdapter);

        var menu1Widget = session.getModelAdapter(menu1.id).widget;
        message = {
          events: [createPropertyChangeEvent(table, {
            menus: [menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(table.menus.length).toBe(1);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[0].remoteAdapter);
        expect(session.getModelAdapter(menu1.id)).toBeFalsy();
        expect(menu1Widget.destroyed).toBe(true);
      });

      it("destroys the old and creates the new menus if the list contains both", function() {
        var model = helper.createModelFixture(2);
        var adapter = helper.createTableAdapter(model);
        var table = adapter.createWidget(model, session.desktop);
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
        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0].remoteAdapter);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1].remoteAdapter);

        var menu1Widget = session.getModelAdapter(menu1.id).widget;
        message = {
          adapterData: createAdapterData(menu3),
          events: [createPropertyChangeEvent(table, {
            menus: [menu2.id, menu3.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(table.menus.length).toBe(2);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[0].remoteAdapter);
        expect(session.getModelAdapter(menu3.id)).toBe(table.menus[1].remoteAdapter);
        expect(session.getModelAdapter(menu1.id)).toBeFalsy();
        expect(menu1Widget.destroyed).toBe(true);
      });

    });

  });

  describe("_sendRowsFiltered", function() {

    // Test case for ticket #175700
    it("should not coalesce remove and 'add' events", function() {
      var model = helper.createModelFixture(1, 2);
      var adapter = helper.createTableAdapter(model);
      var table = adapter.createWidget(model, session.desktop);
      adapter._sendRowsFiltered(['1','2']); // should create a remove event, because number of rows is equals to the length of rowIds
      adapter._sendRowsFiltered(['1']); // should create an 'add' event
      adapter._sendRowsFiltered(['2']); // should be coalesced with previous add event
      expect(session._asyncEvents.length).toBe(2);
      expect(session._asyncEvents[0].remove).toBe(true);
      expect(session._asyncEvents[1].remove).toBe(undefined);
    });

  });

});
