/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {defaultValues, RemoteEvent, RemoteResponse, RemoteTableOrganizer, Table, TableAdapter, TableRow, TableTextUserFilter} from '../../src/index';
import {LocaleSpecHelper, SpecTable, SpecTableAdapter, TableModelWithCells, TableSpecHelper} from '../../src/testing/index';

describe('TableAdapter', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
    helper = new TableSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    helper.resetIntlCollator();
    $.fx.off = false;
  });

  let defaults = {
    'defaults': {
      'Table': {
        'a': 123
      },
      'TableRow': {
        'b': 234
      }
    },
    'objectTypeHierarchy': {
      'Widget': {
        'Table': null
      }
    }
  };

  describe('selectRows', () => {

    it('sends rowsSelected event containing rowIds', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;

      let rows = [table.rows[0], table.rows[4]];
      table.selectRows(rows);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let event = new RemoteEvent(table.id, 'rowsSelected', {
        rowIds: helper.getRowIds(rows)
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('does not send selection event if triggered by server', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;

      let rows = [table.rows[0], table.rows[4]];
      adapter._onRowsSelected([rows[0].id, rows[1].id]);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

  describe('checkRows', () => {

    it('sends rowsChecked event containing rowIds', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;

      let rows = [table.rows[0]];
      table.checkRows(rows);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let event = new RemoteEvent(table.id, 'rowsChecked', {
        rows: [{
          rowId: rows[0].id,
          checked: true
        }]
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('does not send rowsChecked event if triggered by server', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;

      let rows = [table.rows[0]];
      adapter._onRowsChecked([{
        id: rows[0].id,
        checked: true
      }]);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

  describe('expandRows', () => {

    it('sends rowsExpanded event containing rowIds', () => {
      let rowIds = ['0', '1', '2'],
        rows = rowIds.map(id => {
          let rowData = helper.createModelRow(id, ['row' + id]);
          rowData.expanded = true;
          return rowData;
        });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;
      let tableRows = table.rows;
      table.updateRows(tableRows);
      table.render();

      table.collapseRow(tableRows[0]);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let event = new RemoteEvent(table.id, 'rowsExpanded', {
        rows: [{
          rowId: tableRows[0].id,
          expanded: false
        }]
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('does not send rowsChecked event if triggered by server', () => {
      let rowIds = ['0', '1', '2'],
        rows = rowIds.map(id => {
          let rowData = helper.createModelRow(id, ['row' + id]);
          rowData.expanded = true;
          return rowData;
        });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;
      rows[1]['parentId'] = rows[0].id;
      table.updateRows(rows);
      table.render();

      adapter._onRowsExpanded(
        [{
          id: '0',
          expanded: false
        }]
      );
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
      expect(table.rows[0].expanded).toBe(false);
    });
  });

  describe('onModelAction', () => {

    function createRowsInsertedEvent(model, rows) {
      return {
        target: model.id,
        rows: rows,
        type: 'rowsInserted'
      };
    }

    describe('rowsSelected event', () => {

      function createRowsSelectedEvent(model, rowIds) {
        return {
          target: model.id,
          rowIds: rowIds,
          type: 'rowsSelected'
        };
      }

      it('calls selectRows', () => {
        let model = helper.createModelFixture(2, 5);
        let adapter = helper.createTableAdapter(model);
        let table = adapter.createWidget(model, session.desktop) as Table;
        table.render();

        spyOn(table, 'selectRows');

        let rowIds = [table.rows[0].id, table.rows[4].id];
        let event = createRowsSelectedEvent(model, rowIds);
        adapter.onModelAction(event);
        expect(table.selectRows).toHaveBeenCalledWith([table.rows[0], table.rows[4]]);
      });
    });

    describe('rowsDeleted event', () => {
      let model: TableModelWithCells;
      let table: Table;
      let adapter: SpecTableAdapter;
      let rows: TableRow[];
      let row0, row1, row2;

      function createRowsDeletedEvent(model, rowIds) {
        return {
          target: model.id,
          rowIds: rowIds,
          type: 'rowsDeleted'
        };
      }

      beforeEach(() => {
        model = helper.createModelFixture(2, 3);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop) as Table;
        rows = table.rows;
        row0 = model.rows[0];
        row1 = model.rows[1];
        row2 = model.rows[2];
      });

      it('calls deleteRows', () => {
        spyOn(table, 'deleteRows');

        let rowIds = [rows[0].id, rows[2].id];
        let event = createRowsDeletedEvent(model, rowIds);
        adapter.onModelAction(event);
        expect(table.deleteRows).toHaveBeenCalledWith([rows[0], rows[2]]);
      });

      it('does not send rowsSelected event for the deleted rows', () => {
        let row = table.rows[0];
        table.selectedRows = [row];

        adapter._onRowsDeleted([row.id]);
        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(0);
      });
    });

    describe('allRowsDeleted event', () => {
      let model, table, adapter;

      function createAllRowsDeletedEvent(model: { id: string }) {
        return {
          target: model.id,
          type: 'allRowsDeleted'
        };
      }

      beforeEach(() => {
        model = helper.createModelFixture(2, 3);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
      });

      it('calls deleteAllRows', () => {
        spyOn(table, 'deleteAllRows');

        let event = createAllRowsDeletedEvent(model);
        adapter.onModelAction(event);
        expect(table.deleteAllRows).toHaveBeenCalled();
      });

      it('does not send rowsSelected event', () => {
        let row = table.rows[0];
        table.selectedRows = [row];

        adapter._onAllRowsDeleted();
        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(0);
      });
    });

    describe('rowsInserted event', () => {
      let model, table, adapter;

      beforeEach(() => {
        model = helper.createModelFixture(2);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
      });

      it('calls insertRows', () => {
        spyOn(table, 'insertRows');

        let rows = helper.createModelRows(2, 5);
        let event = createRowsInsertedEvent(model, rows);
        adapter.onModelAction(event);
        expect(table.insertRows).toHaveBeenCalledWith(rows);
      });

      it('may contain cells as objects', () => {
        let row = {
          cells: [{
            value: 'a value 0',
            text: 'a text 0'
          }, {
            value: 'a value 1',
            text: 'a text 1'
          }]
        };
        let event = createRowsInsertedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.rows[0].cells[0].value).toBe('a value 0');
        expect(table.rows[0].cells[0].text).toBe('a text 0');
        expect(table.rows[0].cells[1].value).toBe('a value 1');
        expect(table.rows[0].cells[1].text).toBe('a text 1');
      });

      it('may contain cells as scalars', () => {
        let row = {
          cells: ['a text 0', 'a text 1']
        };
        let event = createRowsInsertedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.rows[0].cells[0].value).toBe('a text 0');
        expect(table.rows[0].cells[0].text).toBe('a text 0');
        expect(table.rows[0].cells[1].value).toBe('a text 1');
        expect(table.rows[0].cells[1].text).toBe('a text 1');
      });

      it('respects null values', () => {
        let row = {
          cells: [{
            value: null,
            text: 'empty 0'
          }, {
            value: null,
            text: 'empty 1'
          }]
        };
        let event = createRowsInsertedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.rows[0].cells[0].value).toBe(null);
        expect(table.rows[0].cells[0].text).toBe('empty 0');
        expect(table.rows[0].cells[1].value).toBe(null);
        expect(table.rows[0].cells[1].text).toBe('empty 1');
      });

      it('uses text as value if value is not provided', () => {
        // This case is relevant for custom columns, where no JS representation exists.
        // They have values on server, but they are not sent to client. Since we expect every cell to have a value use the text as value.
        let row = {
          cells: [{
            cssClass: 'abc',
            text: 'text 0'
          }, {
            cssClass: 'abc',
            text: 'text 1'
          }]
        };
        let event = createRowsInsertedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.rows[0].cells[0].value).toBe('text 0');
        expect(table.rows[0].cells[0].text).toBe('text 0');
        expect(table.rows[0].cells[1].value).toBe('text 1');
        expect(table.rows[0].cells[1].text).toBe('text 1');
      });

      it('applies defaultValues to rows', () => {
        defaultValues.init(defaults);
        model = helper.createModelFixture(2, 1);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
        expect(table.a).toBe(123);
        expect(table.rows[0].b).toBe(234);

        table.deleteAllRows();
        expect(table.rows.length).toBe(0);

        let row = {
          cells: ['a text 0', 'a text 1']
        };
        let event = createRowsInsertedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.a).toBe(123);
        expect(table.rows[0].b).toBe(234);
      });
    });

    describe('rowOrderChanged event', () => {
      let model, table, adapter, row0, row1, row2;

      beforeEach(() => {
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

      it('calls updateRowOrder', () => {
        spyOn(table, 'updateRowOrder');

        let event = createRowOrderChangedEvent(model, [row2.id, row1.id, row0.id]);
        adapter.onModelAction(event);
        expect(table.updateRowOrder).toHaveBeenCalledWith([row2, row1, row0]);
      });

      it('correct DOM order for newly inserted rows', () => {
        table.render();
        expect(table.rows.length).toBe(3);

        let newRows = [
          helper.createModelRow(null, helper.createModelCells(2)),
          helper.createModelRow(null, helper.createModelCells(2))
        ];

        // Insert new rows and switch rows 0 and 1
        let orderedRowIds = [row1.id, row0.id, newRows[0].id, newRows[1].id, row2.id];
        let message = {
          events: [
            createRowsInsertedEvent(model, newRows),
            createRowOrderChangedEvent(model, orderedRowIds)
          ]
        };
        session._processSuccessResponse(message);

        // Check if rows were inserted
        expect(table.rows.length).toBe(5);

        // Check if order in the DOM is correct
        let $row, rowId, expectedRowId,
          i = 0,
          $rows = table.$rows();
        $rows.each(function() {
          $row = $(this);
          rowId = $row.data('row').id;
          expectedRowId = orderedRowIds[i++];
          expect(rowId).toBe(expectedRowId);
        });
      });
    });

    describe('rowsUpdated event', () => {
      let model, table, adapter;

      function createRowsUpdatedEvent(model, rows) {
        return {
          target: model.id,
          rows: rows,
          type: 'rowsUpdated'
        };
      }

      beforeEach(() => {
        model = helper.createModelFixture(2, 2);
        model.rows[0].cells[0].text = 'cellText0';
        model.rows[0].cells[1].text = 'cellText1';
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
      });

      it('calls updateRows', () => {
        spyOn(table, 'updateRows');

        let row = {
          id: table.rows[0].id,
          cells: ['newCellText0', 'newCellText1']
        };
        let event = createRowsUpdatedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.updateRows).toHaveBeenCalledWith([row]);
      });

      it('applies defaultValues to rows', () => {
        defaultValues.init(defaults);
        model = helper.createModelFixture(2, 1);
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
        expect(table.rows[0].b).toBe(234);

        table.rows[0].b = 999;
        let row = {
          id: table.rows[0].id
        };
        let event = createRowsUpdatedEvent(model, [row]);
        adapter.onModelAction(event);
        expect(table.rows[0].b).toBe(234);
      });
    });

    describe('columnStructureChanged event', () => {
      let model, table, adapter, column0, column1, column2;

      beforeEach(() => {
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

      it('calls updateColumnStructure', () => {
        spyOn(table, 'updateColumnStructure');
        let message = {
          events: [createColumnStructureChangedEvent(model, [column2, column1])]
        };
        session._processSuccessResponse(message);
        expect(table.updateColumnStructure).toHaveBeenCalledWith([column2, column1]);
      });
    });

    describe('columnOrderChanged event', () => {
      let model, table, adapter, column0, column1, column2;

      beforeEach(() => {
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

      it('calls updateColumnOrder', () => {
        spyOn(table, 'updateColumnOrder');
        let message = {
          events: [createColumnOrderChangedEvent(model, [column2.id, column0.id, column1.id])]
        };
        session._processSuccessResponse(message);
        expect(table.updateColumnOrder).toHaveBeenCalledWith([column2, column0, column1]);
      });
    });

    describe('columnHeadersUpdated event', () => {
      let model, table, adapter, column0, column1, column2;

      beforeEach(() => {
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

      it('calls updateColumnHeaders', () => {
        adapter = helper.createTableAdapter(model);
        table = adapter.createWidget(model, session.desktop);
        spyOn(table, 'updateColumnHeaders');

        column1 = helper.createModelColumn('newText1');
        column1.id = model.columns[1].id;
        column1.sortActive = true;
        column1.sortAscending = true;
        column2 = helper.createModelColumn('newText2');
        column2.id = model.columns[2].id;

        let message = {
          events: [createColumnHeadersUpdatedEvent(model, [column1, column2])]
        };
        session._processSuccessResponse(message);
        expect(table.updateColumnHeaders).toHaveBeenCalledWith([column1, column2]);
      });
    });
  });

  describe('onModelPropertyChange', () => {

    describe('menus', () => {

      it('creates and registers menu adapters', () => {
        let model = helper.createModelFixture(2);
        let adapter = helper.createTableAdapter(model);
        let table = adapter.createWidget(model, session.desktop) as Table;
        let menu1 = helper.createMenuModel();
        let menu2 = helper.createMenuModel();

        let message = {
          adapterData: mapAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0].modelAdapter);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1].modelAdapter);
      });

      it('destroys the old menus', () => {
        let model = helper.createModelFixture(2);
        let adapter = helper.createTableAdapter(model);
        let table = adapter.createWidget(model, session.desktop) as Table;
        let menu1 = helper.createMenuModel();
        let menu2 = helper.createMenuModel();

        let message: RemoteResponse = {
          adapterData: mapAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0].modelAdapter);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1].modelAdapter);

        let menu1Widget = session.getModelAdapter(menu1.id).widget;
        message = {
          events: [createPropertyChangeEvent(table, {
            menus: [menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(table.menus.length).toBe(1);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[0].modelAdapter);
        expect(session.getModelAdapter(menu1.id)).toBeFalsy();
        expect(menu1Widget.destroyed).toBe(true);
      });

      it('destroys the old and creates the new menus if the list contains both', () => {
        let model = helper.createModelFixture(2);
        let adapter = helper.createTableAdapter(model);
        let table = adapter.createWidget(model, session.desktop) as Table;
        let menu1 = helper.createMenuModel();
        let menu2 = helper.createMenuModel();
        let menu3 = helper.createMenuModel();

        let message = {
          adapterData: mapAdapterData([menu1, menu2]),
          events: [createPropertyChangeEvent(table, {
            menus: [menu1.id, menu2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(session.getModelAdapter(menu1.id)).toBe(table.menus[0].modelAdapter);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[1].modelAdapter);

        let menu1Widget = session.getModelAdapter(menu1.id).widget;
        message = {
          adapterData: mapAdapterData(menu3),
          events: [createPropertyChangeEvent(table, {
            menus: [menu2.id, menu3.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(table.menus.length).toBe(2);
        expect(session.getModelAdapter(menu2.id)).toBe(table.menus[0].modelAdapter);
        expect(session.getModelAdapter(menu3.id)).toBe(table.menus[1].modelAdapter);
        expect(session.getModelAdapter(menu1.id)).toBeFalsy();
        expect(menu1Widget.destroyed).toBe(true);
      });
    });
  });

  describe('_sendFilter', () => {

    // Test case for ticket #175700
    it('should not coalesce remove and \'add\' events', () => {
      let model = helper.createModelFixture(1, 2);
      let adapter = helper.createTableAdapter(model);
      adapter.createWidget(model, session.desktop);
      adapter._sendFilter(['1', '2']); // should create a remove event, because number of rows is equals to the length of rowIds
      adapter._sendFilter(['1']); // should create an 'add' event
      adapter._sendFilter(['2']); // should be coalesced with previous add event
      expect(session.asyncEvents.length).toBe(2);
      expect(session.asyncEvents[0].remove).toBe(true);
      expect(session.asyncEvents[1].remove).toBe(undefined);
    });
  });

  describe('_postCreateWidget', () => {

    it('should send a filter event, if a filter exists on table after widget is created.', () => {
      let model = helper.createModelFixture(2, 5);
      $.extend(model, {filters: [{objectType: TableTextUserFilter, filterType: 'text', text: '2'}]});
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;

      sendQueuedAjaxCalls(null, 250);
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let rows = [table.rows[2]];
      let event = new RemoteEvent(table.id, 'filter', {
        rowIds: helper.getRowIds(rows),
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });
  });

  describe('organizer', () => {

    it('initializes the table with RemoteTableOrganizer', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as Table;

      expect(table.organizer).toBeInstanceOf(RemoteTableOrganizer);

      expect(table.isColumnAddable(table.columns[0])).toBe(true);
      expect(table.isColumnRemovable(table.columns[0])).toBe(true);
      expect(table.isColumnModifiable(table.columns[0])).toBe(true);

      table.columnAddable = false;
      expect(table.isColumnAddable(table.columns[0])).toBe(false);
      table.columns[0].removable = false;
      table.columns[0].modifiable = false;
      expect(table.isColumnRemovable(table.columns[0])).toBe(false);
      expect(table.isColumnModifiable(table.columns[0])).toBe(false);
      expect(table.isColumnRemovable(table.columns[1])).toBe(true);
      expect(table.isColumnModifiable(table.columns[1])).toBe(true);

      table.columns.forEach(column => column.setVisible(false));
      expect(table.isColumnRemovable(table.columns[1])).toBe(true);
      expect(table.isColumnModifiable(table.columns[1])).toBe(true);
    });
  });
});
