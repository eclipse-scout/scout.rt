/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BooleanColumn, BooleanColumnUserFilterStateDo, ChildModelOf, Column, ColumnUserFilter, ColumnUserFilterStateDo, DateColumn, DateColumnUserFilter, DateColumnUserFilterStateDo, dates, IconColumn, NumberColumn, NumberColumnUserFilter,
  NumberColumnUserFilterStateDo, Range, RemoteEvent, scout, Table, TableRow, TableTextUserFilter, TableTextUserFilterStateDo, tableUiPreferences, TextColumnUserFilter, TextColumnUserFilterStateDo
} from '../../src/index';
import {SpecTable, TableSpecHelper} from '../../src/testing';

describe('TableFilter', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createColumnFilterModel(columnId, selectedValues): ChildModelOf<ColumnUserFilter> {
    return {
      objectType: TextColumnUserFilter,
      column: columnId,
      selectedValues: selectedValues
    };
  }

  function createAndRegisterColumnFilter(table, column, selectedValues): ColumnUserFilter {
    return helper.createAndRegisterColumnFilter({
      table: table,
      session: session,
      column: column,
      selectedValues: selectedValues
    });
  }

  describe('row filtering', () => {

    it('applies row filter when table gets initialized', () => {
      let model = helper.createModelFixture(2, 2);
      let filter = createColumnFilterModel(model.columns[0].id, ['1_0']);
      model.filters = [filter];
      let table = helper.createTable(model);

      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);
    });

    it('doesn\'t filter anymore if filter gets removed', () => {
      let model = helper.createModelFixture(2, 2);
      let filter = createColumnFilterModel(model.columns[0].id, ['1_0']);
      model.filters = [filter];
      let table = helper.createTable(model);

      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);

      table.removeFilterByKey((filter.column as Column).id);

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(2);
    });

    it('applies row filter if a new row gets inserted', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      createAndRegisterColumnFilter(table, column0, ['1_0']);
      table.render();
      expect(table.filteredRows().length).toBe(1);

      let rows = helper.createModelRows(2, 1);
      rows[0].cells[0].text = 'new';
      table.insertRows(rows);

      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row).toBeFalsy();

      rows = helper.createModelRows(2, 1);
      rows[0].cells[0].text = '1_0';
      table.insertRows(rows);

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(2);
      expect(filteredRows[0]).toBe(table.rows[1]);
      expect(filteredRows[1]).toBe(table.rows[3]);
    });

    it('applies row filter if a new row gets inserted, even if table is not rendered', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      createAndRegisterColumnFilter(table, column0, ['1_0']);
      expect(table.filteredRows().length).toBe(1);

      let rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'newCell';
      table.insertRows(rows);

      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row).toBeFalsy();

      table.render();
      expect(table.rows[2].$row).toBeFalsy();
    });

    // and insert rows

    it('applies row filter if a row gets updated', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      // expects 1 row to be visible
      createAndRegisterColumnFilter(table, column0, ['1_0']);
      table.render();
      expect(table.filteredRows().length).toBe(1);
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeTruthy();

      let rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].text = 'updated';
      table.updateRows(rows);

      // expects no row to be visible
      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(0);
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeFalsy();

      rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].text = '1_0';
      table.updateRows(rows);

      // expects 1 row to be visible
      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeTruthy();

      // change cell 2 of row 1, filter state should not change
      rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].text = '1_0';
      rows[0].cells[1].text = '1_1';
      table.updateRows(rows);

      // still expects 1 row to be visible
      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      // if this check fails, filteredRow cache has not been updated
      expect(filteredRows[0]).toBe(table.rows[1]);
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeTruthy();
    });

    it('applies row filter if a row gets updated, even if table is not rendered', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      // expects 1 row to be visible
      createAndRegisterColumnFilter(table, column0, ['1_0']);
      expect(table.filteredRows().length).toBe(1);

      let rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].text = 'updated';
      table.updateRows(rows);

      // expects no row to be visible
      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(0);
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeFalsy();

      table.render();
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeFalsy();
    });

    it('properly handles successive row insertion and updates', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      // expects 1 row to be visible
      createAndRegisterColumnFilter(table, column0, ['1_0']);
      table.render();
      expect(table.filteredRows().length).toBe(1);

      // insert new row -> not visible
      let rows = helper.createModelRows(2, 1);
      rows[0].cells[0].text = 'new';
      table.insertRows(rows);

      let filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row).toBeFalsy();

      // update new row -> still not visible
      rows = helper.createModelRows(2, 1);
      rows[0].id = table.rows[2].id;
      rows[0].cells[0].text = 'updated';
      table.updateRows(rows);

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row).toBeFalsy();
    });

    it('properly handles reset table case', () => {
      let model = $.extend(true, {}, helper.createModelFixture(2, 2), {textFilterEnabled: false}),
        table = helper.createTable(model),
        column0 = table.columns[0];

      // Filter active
      createAndRegisterColumnFilter(table, column0, ['1_0']);
      table.render();
      expect(table.filterCount()).toBe(1);
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(1);
      expect(table.$rows().length).toBe(1);

      // Delete all rows
      table.deleteAllRows();
      expect(table.rows.length).toBe(0);
      expect(table.filteredRows().length).toBe(0);
      expect(table.$rows().length).toBe(0);

      // Remove filters
      table.setFilters([]);
      expect(table.filterCount()).toBe(0);

      // Insert rows again
      let rows = helper.createModelRows(2, 2);
      table.insertRows(rows);
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(2);
      expect(table.$rows().length).toBe(2);
    });

    it('considers view range', () => {
      let model = helper.createModelFixture(2, 7),
        table = helper.createTable(model),
        column0 = table.columns[0];

      table.viewRangeSize = 1;

      // expects 1 row to be visible
      createAndRegisterColumnFilter(table, column0, ['1_0', '3_0']);
      table.render();

      expect(table.rows.length).toBe(7);
      expect(table.filteredRows().length).toBe(2);
      expect(table.$rows().length).toBe(1); // only one row in view range
      expect(table.$rows().eq(0).data('row')).toBe(table.filteredRows()[0]);

      // Use last filtered row as base row index
      spyOn(table, '_calculateCurrentViewRange').and.returnValue(new Range(1, 2));
      table._renderViewport();

      expect(table.$rows().length).toBe(1); // only one row in view range
      expect(table.$rows().eq(0).data('row')).toBe(table.filteredRows()[1]);
    });

    it('renders empty data if all rows are removed due to filtering', () => {
      let model = helper.createModelFixture(2, 7),
        table = helper.createTable(model),
        column0 = table.columns[0];

      table.render();
      expect(table.$emptyData).toBe(null);

      createAndRegisterColumnFilter(table, column0, ['asdf']);

      expect(table.rows.length).toBe(7);
      expect(table.filteredRows().length).toBe(0);
      expect(table.$rows().length).toBe(0);
      expect(table.$emptyData).toBeDefined();

      createAndRegisterColumnFilter(table, column0, ['1_0']);
      expect(table.$emptyData).toBe(null);

      table.resetUserFilter();
      expect(table.$emptyData).toBe(null);
      expect(table.$rows().length).toBe(7);
    });

    it('does not fail if rows are filtered while table is detached', () => {
      let model = helper.createModelFixture(2, 7);
      let table = helper.createTable(model);

      table.render();
      table.addFilter(helper.createTableTextFilter(table, 'asdf'), false);
      table.detach();
      table.filter();
      table.attach();

      expect(table.rows.length).toBe(7);
      expect(table.filteredRows().length).toBe(0);
      expect(table.viewRangeRendered.size()).toBe(0);
      expect(table.$rows().length).toBe(0);
    });
  });

  describe('selection', () => {

    it('gets removed for non visible rows after filtering', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      table.render();
      table.selectAll();
      expect(table.selectedRows.length).toBe(2);

      createAndRegisterColumnFilter(table, column0, ['1_0']);

      expect(table.selectedRows.length).toBe(1);
      expect(table.selectedRows[0]).toBe(table.rows[1]);
    });

    it('stays removed even if filtered row is selected explicitly', () => {
      let model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      // Initially, no rows are selected
      table.render();
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(2);
      expect(table.selectedRows.length).toBe(0);

      // Add a filter that hides the first row (still no rows selected)
      let filter = createAndRegisterColumnFilter(table, column0, ['1_0']);
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(1);
      expect(table.selectedRows.length).toBe(0);

      // Explicitly select the first row (currently invisible), expect still no selected row
      table.selectRows(table.rows[0]);
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(1);
      expect(table.selectedRows.length).toBe(0);

      // Remove the filter again (still no selection)
      table.removeFilter(filter);
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(2);
      expect(table.selectedRows.length).toBe(0);

      // Now select the first row again, this time the selection should change
      table.selectRows(table.rows[0]);
      expect(table.rows.length).toBe(2);
      expect(table.filteredRows().length).toBe(2);
      expect(table.selectedRows.length).toBe(1);
      expect(table.selectedRows[0]).toBe(table.rows[0]);
    });

    it('gets removed for non visible rows after filtering if a row has been updated', () => {
      let model = helper.createModelFixture(2, 3),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      createAndRegisterColumnFilter(table, column0, ['1_0', '2_0']);
      table.render();
      table.selectAll();
      expect(table.selectedRows.length).toBe(2);
      expect(table.selectedRows[0]).toBe(table.rows[1]);
      expect(table.selectedRows[1]).toBe(table.rows[2]);

      // updateRows applies filter which should consider selection removal
      let rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].text = 'updated';
      table.updateRows(rows);

      expect(table.selectedRows.length).toBe(1);
      expect(table.selectedRows[0]).toBe(table.rows[2]);
    });

    it('gets restored for visible rows after filtering', () => {
      let model = helper.createModelSingleColumnByValues([5, 2, 1, 3, 4], 'NumberColumn'),
        table = helper.createTable(model),
        rows = table.rows;
      table._animationRowLimit = 0;
      table.render();

      let $rows = table.$rows();
      let $row0 = $rows.eq(0);
      let $row1 = $rows.eq(1);
      let $row2 = $rows.eq(2);
      let $row3 = $rows.eq(3);
      let $row4 = $rows.eq(4);

      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('selected');

      table.selectRows([rows[1], rows[2], rows[3]]);

      // before filtering
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
        // @ts-expect-error
        createKey: () => 1,
        accept: row => Number(row.$row.text()) % 2 === 0
      });

      // after filtering
      $rows = table.$rows();
      expect($rows.eq(0)).toHaveClass('selected');
      expect($rows.eq(0)).toHaveClass('select-single');
      expect($rows.eq(1)).not.toHaveClass('selected');
      expect($rows.eq(1)).not.toHaveClass('select-single');
      expect($rows.eq(1)).not.toHaveClass('select-top');
    });

  });

  describe('events', () => {

    describe('filter', () => {
      let listener = {
        _onFilter: () => {
          // nop
        }
      };

      it('gets fired when table with a filter is initializing', () => {
        let model = helper.createModelFixture(2, 2);
        let table = new Table();
        let filter = createColumnFilterModel(model.columns[0].id, ['cell1_0']);
        model.filters = [filter];

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        table.init(model);
        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('does not get fired when table with no filters is initializing', () => {
        let model = helper.createModelFixture(2, 2);
        let table = new Table();

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        table.init(model);
        expect(listener._onFilter).not.toHaveBeenCalled();
      });

      it('gets fired if filter() is called', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        table.render();

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        createAndRegisterColumnFilter(table, column0, ['1_0']);

        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('gets fired if filter() is called, even if table is not rendered', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        createAndRegisterColumnFilter(table, column0, ['1_0']);

        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('gets not fired if rows are filtered again but the filtered rows have not changed', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        table.render();

        createAndRegisterColumnFilter(table, column0, ['1_0']);

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);
        table.filter();

        expect(listener._onFilter).not.toHaveBeenCalled();
      });

      it('gets fired if rows are filtered during updateRows', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        createAndRegisterColumnFilter(table, column0, ['1_0']);
        table.render();

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        let rows = helper.createModelRows(2, 1);
        rows[0].id = row1.id;
        rows[0].cells[0].text = 'updated';
        table.updateRows(rows);

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('gets fired if rows are filtered during insertRows', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        createAndRegisterColumnFilter(table, column0, ['1_0']);
        table.render();
        expect(table.filteredRows().length).toBe(1);

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        let rows = helper.createModelRows(2, 1);
        rows[0].cells[0].text = '1_0';
        table.insertRows(rows);

        expect(table.filteredRows().length).toBe(2);
        expect(listener._onFilter).toHaveBeenCalled();

        rows = helper.createModelRows(2, 1);
        rows[0].cells[0].value = 'wont accept';
        table.insertRows(rows);

        expect(table.filteredRows().length).toBe(2);
        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('gets fired if rows are filtered during deleteRows', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        createAndRegisterColumnFilter(table, column0, ['1_0']);
        table.render();
        expect(table.filteredRows().length).toBe(1);

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        table.deleteRows([row1]);

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('gets fired if rows are filtered during deleteAllRows', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        createAndRegisterColumnFilter(table, column0, ['1_0']);
        table.render();
        expect(table.filteredRows().length).toBe(1);

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        table.deleteAllRows();

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onFilter).toHaveBeenCalled();
      });

      it('does not get fired if rows are updated but row filter state has not changed', () => {
        let model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        createAndRegisterColumnFilter(table, column0, ['1_0']);
        table.render();

        spyOn(listener, '_onFilter');
        table.on('filter', listener._onFilter);

        // update cell 1 of row -> row still accepted by filter
        let rows = helper.createModelRows(2, 1);
        rows[0].id = row1.id;
        rows[0].cells[0].text = row1.cells[0].text;
        rows[0].cells[1].text = 'updated1';
        table.updateRows(rows);

        expect(table.filteredRows().length).toBe(1);
        expect(listener._onFilter).not.toHaveBeenCalled();
      });

      it('gets sent to server containing rowIds when rows are filtered', () => {
        let model = helper.createModelFixture(2, 2),
          adapter = helper.createTableAdapter(model),
          table = adapter.createWidget(model, session.desktop) as SpecTable,
          column0 = table.columns[0];

        table.render();

        createAndRegisterColumnFilter(table, column0, ['1_0']);

        expect(table.rows[0].filterAccepted).toBe(false);
        expect(table.rows[1].filterAccepted).toBe(true);

        sendQueuedAjaxCalls(null, 400);

        expect(jasmine.Ajax.requests.count()).toBe(1);

        let event = new RemoteEvent(table.id, 'filter', {
          rowIds: [table.rows[1].id],
          showBusyIndicator: false
        });
        expect(mostRecentJsonRequest()).toContainEvents(event);
      });
    });
  });

  describe('user filter states', () => {

    it('uses column-specific mappers to load and store user filter states', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          id: 'c0',
          objectType: Column
        }, {
          id: 'c1',
          objectType: BooleanColumn
        }, {
          id: 'c2',
          objectType: DateColumn
        }, {
          id: 'c3',
          objectType: NumberColumn
        }, {
          id: 'c4',
          objectType: IconColumn
        }]
      });
      let customFilter = (row: TableRow): boolean => false;
      table.addFilter(customFilter); // should NOT be overwritten below
      table.addFilter(scout.create(TableTextUserFilter, { // should be overwritten below
        session: session,
        table: table,
        text: 'xyz'
      }));

      let userFilterStates = [
        scout.create(TableTextUserFilterStateDo, {
          text: 'abc'
        }),
        scout.create(TextColumnUserFilterStateDo, {
          columnId: 'c0',
          selectedValues: ['apple', 'banana'],
          textFilter: 'coconut'
        }),
        scout.create(BooleanColumnUserFilterStateDo, {
          columnId: 'c1',
          selectedValues: [true]
        }),
        scout.create(DateColumnUserFilterStateDo, {
          columnId: 'c2',
          selectedValues: [1001, 1002],
          dateFrom: dates.create('2010-01-11'),
          dateTo: dates.create('2020-02-22')
        }),
        scout.create(NumberColumnUserFilterStateDo, {
          columnId: 'c3',
          selectedValues: [123, 456],
          numberFrom: 77,
          numberTo: 88
        }),
        scout.create(ColumnUserFilterStateDo, {
          columnId: 'c4',
          selectedValues: ['foo', 'bar']
        })
      ];
      table.applyUserFilterStates(userFilterStates);

      expect(table.filters.length).toBe(7);
      expect(table.filters[0].createdByFunction).toBe(true);
      expect(table.filters[0].accept).toBe(customFilter);
      expect(table.filters[1]).toBeInstanceOf(TableTextUserFilter);
      expect(table.filters[2]).toBeInstanceOf(TextColumnUserFilter);
      expect(table.filters[3]).toBeInstanceOf(ColumnUserFilter);
      expect(table.filters[4]).toBeInstanceOf(DateColumnUserFilter);
      expect(table.filters[5]).toBeInstanceOf(NumberColumnUserFilter);
      expect(table.filters[6]).toBeInstanceOf(ColumnUserFilter);

      let filterStates2 = tableUiPreferences.createUserFilterStates(table);
      expect(filterStates2).toEqual(userFilterStates);

      (table.getFilter('c3') as NumberColumnUserFilter).numberTo = 99;
      let filterStates3 = tableUiPreferences.createUserFilterStates(table);
      expect(filterStates3).not.toEqual(userFilterStates);
      expect(filterStates3[0]).toEqual(userFilterStates[0]);
      expect(filterStates3[1]).toEqual(userFilterStates[1]);
      expect(filterStates3[2]).toEqual(userFilterStates[2]);
      expect(filterStates3[3]).toEqual(userFilterStates[3]);
      expect(filterStates3[4]).not.toEqual(userFilterStates[4]);
      expect(filterStates3[4]).toEqual(scout.create(NumberColumnUserFilterStateDo, {
        columnId: 'c3',
        selectedValues: [123, 456],
        numberFrom: 77,
        numberTo: 99
      }));
      expect(filterStates3[5]).toEqual(userFilterStates[5]);
    });
  });
});
