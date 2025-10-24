/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, ColumnUserFilter, scout, Table, TableRow, TableRowModel, TreeGridAriaRules} from '../../src/index';
import {JQueryTesting, SpecTable, TableSpecHelper} from '../../src/testing/index';
import $ from 'jquery';

describe('HierarchicalTableSpec', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createAndRegisterColumnFilter(table: Table, column: Column<any>, selectedValues: (string | number)[]): ColumnUserFilter {
    return helper.createAndRegisterColumnFilter({
      table: table,
      session: session,
      column: column,
      selectedValues: selectedValues
    });
  }

  function expectRowIds(rows: TableRow[], ids: (number | string)[]) {
    expect(rows.map(row => row.id)).toEqual(ids.map(id => id + ''));
  }

  function getTreeRows(table: Table): TableRow[] {
    let flatTreeRows = [];
    table.visitRows(row => {
      flatTreeRows.push(row);
    });
    return flatTreeRows;
  }

  function getUiRows(table: Table): TableRow[] {
    return $.makeArray(table.$rows()).map(row => {
      return $(row).data('row');
    });
  }

  function getExpandedRows(table: Table): TableRow[] {
    let expandedRows = [];
    table.visitRows(row => {
      if (row.expanded && row.childRows.length > 0) {
        expandedRows.push(row);
      }
    });
    return expandedRows;
  }

  function finishRowAnimation(table: Table) {
    // Stop the current animation and jump to the end state
    // $.fx.off = true does not seem to work consistently in this specific spec
    table.$rows().stop(false, true);
  }

  describe('add', () => {
    let table: SpecTable, rowIds: string[], rows: TableRowModel[];

    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     * 2
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      rows[1].parentRow = rows[0].id;
      table.updateRows(rows);
      table.render();
    });

    it('a root row', () => {
      table.insertRow(helper.createModelRow('33', ['newRow']));
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(3);
      expect(table.rootRows[2].id).toBe('33');

      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(3, 0, '33');
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('a child row to a row which is already a parent row (by id)', () => {
      let newRow = helper.createModelRow('33', ['newRow']),
        parentRow = table.rows[0];
      newRow.parentRow = parentRow.id;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(parentRow);

      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(2, 0, '33');
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('a child row to a row which is already a parent row (by pseudo row)', () => {
      let newRow = helper.createModelRow('33', ['newRow']),
        pseudoParentRow: any = {
          id: '0'
        };
      newRow.parentRow = pseudoParentRow;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[pseudoParentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(table.rows[0]);

      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(2, 0, '33');
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('a child row to a row which is already a parent row (by TableRow)', () => {
      let newRow = helper.createModelRow('33', ['newRow']),
        parentRow = table.rows[0];

      newRow.parentRow = parentRow;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(table.rows[0]);

      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(2, 0, '33');
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('a child row to a model row that was inserted before', () => {
      let parentRow: TableRowModel = {cells: ['newRow'], expanded: true};
      let childRow: TableRowModel = {cells: ['childRow'], parentRow: parentRow};

      table.deleteAllRows();
      table.insertRows([parentRow, childRow]);
      expect(table.rows.length).toBe(2);
      expect(table._filteredRows.length).toBe(2);
      expect(table.visibleRows.length).toBe(2);
      expect(table.rootRows.length).toBe(1);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(1);
      expect(table.rowsMap[parentRow.id].childRows[0]).toBe(table.rows[1]);
      expect(table.rowsMap[childRow.id].parentRow).toBe(table.rows[0]);
    });

    it('a child row to a row which is leaf', () => {
      let newRow = helper.createModelRow('33', ['newRow']),
        parentRow = table.rows[2];
      newRow.parentRow = parentRow.id;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(1);
      expect(table.rowsMap[33].parentRow).toBe(parentRow);

      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(3, 0, '33');
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('a child row to a collapsed row', () => {
      let newRow = helper.createModelRow('33', ['newRow']),
        parentRow = table.rows[0];
      newRow.parentRow = parentRow.id;
      table.collapseRow(parentRow);
      finishRowAnimation(table);
      table.insertRow(newRow);

      expect(table.rowsMap[parentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap['33'].parentRow).toBe(parentRow);

      expectRowIds(table.rows, [0, 1, 33, 2]);
      expectRowIds(getTreeRows(table), [0, 1, 33, 2]);
      expectRowIds(table._filteredRows, [0, 1, 33, 2]);
      expectRowIds(table.visibleRows, [0, 2]);
      expectRowIds(getUiRows(table), [0, 2]);
    });
  });

  describe('delete', () => {
    let table, rows, rowIds;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     * |-- 4
     * |-- 5
     * 6
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3', '4', '5', '6'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      rows[4].parentRow = rows[3].id;
      rows[5].parentRow = rows[3].id;
      table.updateRows(rows);
      table.render();
    });

    it('leaf row and expect the row structure to be valid', () => {
      table.deleteRow(rows[5]);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
      expect(rows[3].childRows.length).toBe(1);

      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(5, 1);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('leaf row with collapsed parent and expect the structure to be valid', () => {
      table.collapseRow(rows[3]);
      finishRowAnimation(table);
      expect(table.visibleRows.length).toBe(5);
      table.deleteRow(rows[5]);
      expect(rows[3].expanded).toBeFalsy();
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
      expect(rows[3].childRows.length).toBe(1);

      // structure
      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(5, 1);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectedRowIds.splice(4, 1);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('a parent row and expect all children are deleted cascading.', () => {
      table.deleteRow(rows[3]);
      expect(table.visibleRows.indexOf(rows[3])).toBe(-1);
      expect(table.visibleRows.indexOf(rows[4])).toBe(-1);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);

      // structure
      let expectedRowIds = rowIds.slice();
      expectedRowIds.splice(3, 3);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });
  });

  describe('structure', () => {
    let table, rows, rowIds;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it('is updated when insert a new child row', () => {
      let row = helper.createModelRow('33', ['newRow']);
      spyOn(table, '_renderViewport').and.callThrough();

      row.parentRow = rows[0].id;
      table.insertRow(row);
      row = table.rowById('33');
      expect(table._renderViewport.calls.count()).toBe(2);

      expectRowIds(table.visibleRows, [0, 1, 2, 33, 3]);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expect(rows[0].childRows.length).toBe(2);
      expect(rows[0].childRows[1]).toBe(row);
      expect(row.parentRow).toBe(rows[0]);
    });

    it('is updated when deleting a child row', () => {
      let expectedRowIds = [0, 1, 3];
      table.deleteRow(rows[2]);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it('is updated when deleting a row and its children', () => {
      // cascade deleted row
      let expectedRowIds = [0, 3];
      table.deleteRow(rows[1]);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

  });

  describe('expanded rows', () => {
    let table, rows, rowIds;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it('are valid after expand parent and its child row and expand parent again.', () => {
      let expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);

      table.collapseRow(rows[1]);
      finishRowAnimation(table);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 1, 3]);
      expectRowIds(getExpandedRows(table), [0]);
      expectRowIds(getUiRows(table), [0, 1, 3]);

      table.collapseRow(rows[0]);
      finishRowAnimation(table);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 3]);
      expectRowIds(getExpandedRows(table), []);
      expectRowIds(getUiRows(table), [0, 3]);

      table.expandRow(rows[0]);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 1, 3]);
      expectRowIds(getExpandedRows(table), [0]);
      expectRowIds(getUiRows(table), [0, 1, 3]);

    });

    it('are valid after expand all and collapse all', () => {
      let expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);

      table.collapseAll();
      finishRowAnimation(table);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 3]);
      expectRowIds(getExpandedRows(table), []);
      expectRowIds(getUiRows(table), [0, 3]);

      table.expandAll();
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);
    });
  });

  describe('selection', () => {
    let table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     * |-- 4
     * |-- 5
     * 6
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3', '4', '5', '6'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      rows[4].parentRow = rows[3].id;
      rows[5].parentRow = rows[3].id;
      table.updateRows(rows);
      table.render();
    });

    it('of all rows is valid if parent rows do not match a filter condition', () => {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row2']);
      let expectedRowIds = [0, 1, 2];
      expectRowIds(table.visibleRows, expectedRowIds);
      expect(arrays.equalsIgnoreOrder(expectedRowIds, Object.keys(table.visibleRowsMap).map(n => {
        return Number(n);
      }))).toBe(true);
      table.selectAll();
      expectRowIds(table.selectedRows, expectedRowIds);
    });

    it('a single row matching the filter', () => {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row4']);
      let expectedRowIds = [3, 4];
      expectRowIds(table.visibleRows, expectedRowIds);
      table.selectRow(table.rows[4]);
      expectRowIds(table.selectedRows, [4]);
    });

    it('a single row which is a parent row of a row matching the filter', () => {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row4']);
      let expectedRowIds = [3, 4];
      expectRowIds(table.visibleRows, expectedRowIds);
      table.selectRow(table.rows[3]);
      expectRowIds(table.selectedRows, [3]);
    });

    it('of a not visible row due to a filter', () => {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row4']);
      let expectedRowIds = [3, 4];
      expectRowIds(table.visibleRows, expectedRowIds);
      table.selectRow(table.rows[2]);
      expectRowIds(table.selectedRows, []);
    });

    it('changes when selected rows gets invisible due to collapse of a parent row.', () => {
      let initialSelection = [rows[2], rows[5]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);

      table.collapseRow(rows[3]);
      expect(table.selectedRows).toEqual([rows[2]]);

      table.collapseRow(rows[0]);
      expect(table.selectedRows).toEqual([]);

      table.expandRow(rows[0]);
      table.collapseRow(rows[3]);
      expect(table.selectedRows).toEqual([]);
    });

    it('of a row is still the same if the row gets collapsed. ', () => {
      let initialSelection = [rows[1], rows[2]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);

      table.collapseRow(rows[1]);
      expect(table.selectedRows).toEqual([rows[1]]);

      table.expandRow(rows[1]);
      expect(table.selectedRows).toEqual([rows[1]]);
    });

    it('is still the same after inserting rows', () => {
      let initialSelection = [rows[1], rows[2]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);

      let newRow01 = helper.createModelRow(undefined, ['newRow01']);
      newRow01.parentRow = rows[1].id;
      table.insertRow(newRow01);

      expect(table.selectedRows).toEqual(initialSelection);
    });

    it('is still the same after deleting an not selected row', () => {
      let initialSelection = [rows[1], rows[2]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(7);

      // delete a leaf
      table.deleteRow(rows[4]);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(6);

      // delete a parent
      table.deleteRow(rows[3]);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(4);
    });

    it('gets adjusted when deleting a selected row', () => {
      let initialSelection = [rows[2], rows[4]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(7);

      // delete a leaf
      table.deleteRow(rows[2]);
      expect(table.selectedRows).toEqual([rows[4]]);
      expect(table.visibleRows.length).toBe(6);

      // delete a parent
      table.deleteRow(rows[3]);
      expect(table.selectedRows).toEqual([]);
      expect(table.visibleRows.length).toBe(3);
    });

  });

  describe('update row', () => {
    let table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      // create hierarchy
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it('by changing the parent key', () => {
      table.rows[1].parentRow = table.rows[3];
      table.updateRow(table.rows[1]);

      // expectations
      let expectedRowIds = [0, 3, 1, 2];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [3, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);
      expectRowIds(table.rootRows, [0, 3]);
    });

    it('by removing the parent key', () => {
      table.rows[1].parentRow = null;
      table.updateRow(table.rows[1]);

      // expectations
      let expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [1]);
      expectRowIds(getUiRows(table), expectedRowIds);
      expectRowIds(table.rootRows, [0, 1, 3]);
    });

    it('by adding the parent key', () => {
      table.rows[3].parentRow = table.rows[2];
      table.updateRow(table.rows[1]);

      // expectations
      let expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1, 2]);
      expectRowIds(getUiRows(table), expectedRowIds);
      expectRowIds(table.rootRows, [0]);
    });

    it('applies expanded change correctly', () => {
      let rowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, rowIds);
      expectRowIds(table.visibleRows, rowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), [0, 1, 2, 3]);

      let row0 = {
        id: table.rows[0].id,
        expanded: false,
        cells: ['newRow0Cell0']
      };
      table.updateRow(row0);
      finishRowAnimation(table);

      expectRowIds(table.rows, rowIds);
      expectRowIds(table.visibleRows, [0, 3]);
      expectRowIds(getExpandedRows(table), [1]);
      expectRowIds(getUiRows(table), [0, 3]);
    });

  });

  describe('filtered visible', () => {
    let table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      // create hierarchy
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it('rows are correct after when a child row matches a filter in of a collapsed parent row.', () => {
      let column0 = table.columns[0];

      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, column0, ['row2']);
      expect(table.visibleRows.length).toBe(3);

      table.collapseRow(rows[1]);

      // expect the collapsed row is visible even when it does not match to the filter
      expect(table.visibleRows.length).toBe(2);
      expect(table.visibleRows[0]).toBe(rows[0]);
      expect(table.visibleRows[1]).toBe(rows[1]);

      table.expandRow(rows[1]);
      table.collapseRow(rows[0]);
      expect(table.visibleRows.length).toBe(1);
      expect(table.visibleRows[0]).toBe(rows[0]);
    });

  });

  describe('move', () => {
    let table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * 1
     * |-- 2
     * |-- 3
     * 4
     * 5
     **/
    beforeEach(() => {
      rowIds = ['0', '1', '2', '3', '4', '5'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      // create hierarchy
      rows[2].parentRow = rows[1].id;
      rows[3].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it('row down and expect to be moved after the next sibling on the same level.', () => {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowDown(rows[0]);
      expectRowIds(table.rows, [1, 2, 3, 0, 4, 5]);
    });

    it('row up and expect to be moved before the next sibling on the same level.', () => {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowUp(rows[4]);
      expectRowIds(table.rows, [0, 4, 1, 2, 3, 5]);
    });

    it('child row down and expect it will not be moved away of its siblings.', () => {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowDown(rows[2]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
      // expect to not move away of its parent
      table.moveRowDown(rows[2]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
    });

    it('child row up and expect it will not be moved away of its siblings.', () => {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowUp(rows[3]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
      // expect to not move away of its parent
      table.moveRowUp(rows[3]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
    });

  });

  describe('move visible row', () => {
    let table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0 (b)
     * 1 (a)
     * 2 (b)
     * |-- 3 (a)
     * |-- 4 (b)
     * 5 (b)
     * 6 (a)
     * 7 (b)
     **/
    beforeEach(function() {
      rowIds = ['0', '1', '2', '3', '4', '5', '6', '7'];
      let rowTexts = ['b', 'a', 'b', 'a', 'b', 'b', 'a', 'b'];
      rows = rowIds.map(id => {
        let rowData = helper.createModelRow(id, [rowTexts[id]]);
        rowData.expanded = true;
        return rowData;
      }, this);
      let model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model);
      // create hierarchy
      rows[3].parentRow = rows[2].id;
      rows[4].parentRow = rows[2].id;
      table.updateRows(rows);
      table.render();
    });

    it('up - expect the row gets moved above the previous visible row', () => {
      createAndRegisterColumnFilter(table, table.columns[0], ['a']);
      expectRowIds(table.visibleRows, [1, 2, 3, 6]);
      let moved = table.moveVisibleRowUp(rows[6]);
      expect(moved).toBe(true);
      expectRowIds(table.rows, [0, 1, 6, 2, 3, 4, 5, 7]);
      expectRowIds(table.visibleRows, [1, 6, 2, 3]);

      // once again expect to be moved before 1
      moved = table.moveVisibleRowUp(rows[6]);
      expect(moved).toBe(true);
      expectRowIds(table.rows, [0, 6, 1, 2, 3, 4, 5, 7]);
      expectRowIds(table.visibleRows, [6, 1, 2, 3]);

      // once again expect to move nothing since node 0 is invisible
      moved = table.moveVisibleRowUp(rows[6]);
      expect(moved).toBe(false);
      expectRowIds(table.rows, [0, 6, 1, 2, 3, 4, 5, 7]);
      expectRowIds(table.visibleRows, [6, 1, 2, 3]);

    });

    it('down - expect the row gets moved below the next visible row', () => {
      createAndRegisterColumnFilter(table, table.columns[0], ['a']);
      expectRowIds(table.visibleRows, [1, 2, 3, 6]);

      let moved = table.moveVisibleRowDown(rows[1]);
      expect(moved).toBe(true);
      expectRowIds(table.rows, [0, 2, 3, 4, 1, 5, 6, 7]);
      expectRowIds(table.visibleRows, [2, 3, 1, 6]);

      // once again expect to be moved after 6
      moved = table.moveVisibleRowDown(rows[1]);
      expect(moved).toBe(true);
      expectRowIds(table.rows, [0, 2, 3, 4, 5, 6, 1, 7]);
      expectRowIds(table.visibleRows, [2, 3, 6, 1]);

      // once again expect to move nothing since node 7 is invisible
      moved = table.moveVisibleRowDown(rows[1]);
      expect(moved).toBe(false);
      expectRowIds(table.rows, [0, 2, 3, 4, 5, 6, 1, 7]);
      expectRowIds(table.visibleRows, [2, 3, 6, 1]);
    });
  });

  describe('expandRows', () => {
    it('does not render duplicate rows when called while row is still collapsing', () => {
      let model = helper.createModelFixture(1, 2);
      let table = helper.createTable(model);
      let rows = [
        {cells: ['child0_row0'], parentRow: table.rows[0]},
        {cells: ['child1_row0'], parentRow: table.rows[1]}
      ];
      table.insertRows(rows);
      table.expandRows(table.rows);
      table.render();
      expect(table.$rows().length).toBe(4);

      table.collapseRow(table.rootRows[1]);
      table.expandRow(table.rootRows[1]);
      table.deleteRow(table.rootRows[1]);
      expect(table.$rows().length).toBe(2);

      table.collapseRow(table.rootRows[0]);
      table.rows[1].$row.stop(false, true); // Complete animation
      expect(table.$rows().length).toBe(1);
    });

    it('can expand rows during mouse down event', () => {
      let model = helper.createModelFixture(1, 2);
      let table = helper.createTable(model);
      let rows = [
        {cells: ['child0_row0'], parentRow: table.rows[0]},
        {cells: ['child1_row0'], parentRow: table.rows[1]}
      ];
      table.insertRows(rows);
      table.render();
      table.on('rowsSelected', event => table.expandAll());
      JQueryTesting.triggerMouseDown(table.rows[0].$row);

      for (let i = 0; i < 4; ++i) {
        expect(table.rows[i].expanded).toBe(true);
      }
    });
  });

  describe('aria properties', () => {

    it('has aria role treegrid', () => {
      let model = helper.createModelFixture(1, 2);
      let table = helper.createTable(model);
      let rows = [
        {cells: ['child0_row0'], parentRow: table.rows[0]},
        {cells: ['child1_row0'], parentRow: table.rows[1]}
      ];
      table.insertRows(rows);
      table.render();
      expect(table.$data).toHaveAttr('role', 'treegrid');

      // Delete children -> changes to regular table
      table.deleteRows([table.rootRows[0].childRows[0], table.rootRows[1].childRows[0]]);
      expect(table.$data).toHaveAttr('role', 'grid');

      // Insert children again -> changes to hierarchical table
      table.insertRows(rows);
      expect(table.$data).toHaveAttr('role', 'treegrid');
    });

    it('has treegrid aria rules set', () => {
      let model = helper.createModelFixture(1, 2);
      let table = helper.createTable(model);
      let rows = [
        {cells: ['child0_row0'], parentRow: table.rows[0]},
        {cells: ['child1_row0'], parentRow: table.rows[1]}
      ];
      table.insertRows(rows);
      table.render();
      expect(table.ariaRules instanceof TreeGridAriaRules).toBe(true);
    });

    it('has rows with aria-expanded set to true if rows are expanded', () => {
      let model = helper.createModelFixture(1, 2);
      let table = helper.createTable(model);
      let rows = [
        {cells: ['child0_row0'], parentRow: table.rows[0]},
        {cells: ['child1_row0'], parentRow: table.rows[1]}
      ];
      table.insertRows(rows);
      table.expandRow(table.rootRows[0]);
      table.render();
      expect(table.rootRows[0].$row).toHaveAttr('aria-expanded', 'true');
      expect(table.rootRows[1].$row).toHaveAttr('aria-expanded', 'false');
    });

    it('has posinset and setsize set', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.setViewRangeSize(5);
      let rows = [
        ...createModelRows(10, table.rootRows[0]),
        ...createModelRows(20, table.rootRows[1])
      ];
      table.insertRows(rows);
      table.render();
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertNotAriaRowIndexAndCount(table);

      table.expandRow(table.rootRows[0]);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 10);

      table.collapseRow(table.rootRows[0]);
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);

      table.expandRow(table.rootRows[0]);
      table.scrollTo(arrays.last(table.rootRows));
      helper.assertAriaPosInSetAndSize(table.rootRows, 1, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 6, 10);

      table.scrollTo(table.rootRows[0]);
      table.expandRow(table.rootRows[1]);
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 10);

      const filter = row => scout.isOneOf(row, table.rootRows[0].childRows[3], table.rootRows[1].childRows[2]);
      table.addFilter(filter);
      table.$rows().stop(false, true); // Finish filter animation
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 1);
      helper.assertAriaPosInSetAndSize(table.rootRows[1].childRows, 0, 1);

      table.removeFilter(filter);
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 10);

      table.deleteRows(table.rootRows[0].childRows.slice(0, 2));
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 8);

      table.deleteRows(table.rootRows[0].childRows.slice());
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[1].childRows, 0, 20);

      table.insertRows(createModelRows(2, table.rootRows[0]));
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 2);

      table.updateRows([{id: table.rootRows[1].id, cells: ['new1', 'new2']}]);
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
    });

    it('has posinset and setsize set, even when all rows are in viewport', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.setViewRangeSize(100);
      let rows = [
        ...createModelRows(10, table.rootRows[0]),
        ...createModelRows(20, table.rootRows[1])
      ];
      table.insertRows(rows);
      table.render();
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);

      table.expandRow(table.rootRows[0]);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 10);

      table.collapseRow(table.rootRows[0]);
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);

      table.expandAll();
      const filter = row => scout.isOneOf(row, table.rootRows[0].childRows[3], table.rootRows[1].childRows[2]);
      table.addFilter(filter);
      table.$rows().stop(false, true); // Finish filter animation
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 1);
      helper.assertAriaPosInSetAndSize(table.rootRows[1].childRows, 0, 1);

      table.removeFilter(filter);
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 10);

      table.deleteRows(table.rootRows[0].childRows.slice(0, 2));
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 8);

      table.deleteRows(table.rootRows[0].childRows.slice());
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[1].childRows, 0, 20);

      table.insertRows(createModelRows(2, table.rootRows[0]));
      helper.assertAriaPosInSetAndSize(table.rootRows, 0, 2);
      helper.assertAriaPosInSetAndSize(table.rootRows[0].childRows, 0, 2);
    });

    function createModelRows(size: number, parentRow: TableRow) {
      let rows = [];
      for (let i = 0; i < size; i++) {
        rows.push({cells: [''], parentRow});
      }
      return rows;
    }
  });

  describe('restoreSelection', () => {

    it('expands all parent rows while restoring selection', () => {
      let model = helper.createModelFixture(1, 0);
      model.columns[0].primaryKey = true;
      let table = helper.createTable({
        ...model,
        hierarchical: true
      });
      table.insertRows([
        helper.createModelRow('A', ['A']),
        helper.createModelRow('A1', ['A1'], 'A'),
        helper.createModelRow('A11', ['A11'], 'A1'),
        helper.createModelRow('A12', ['A12'], 'A1'),
        helper.createModelRow('A2', ['A2'], 'A'),
        helper.createModelRow('A21', ['A21'], 'A2'),
        helper.createModelRow('A22', ['A22'], 'A2'),
        helper.createModelRow('A3', ['A3'], 'A'),
        helper.createModelRow('B', ['B']),
        helper.createModelRow('B1', ['B1'], 'B'),
        helper.createModelRow('B11', ['B11'], 'B1'),
        helper.createModelRow('B2', ['B2'], 'B'),
        helper.createModelRow('B21', ['B21'], 'B2'),
        helper.createModelRow('C', ['C'])
      ]);

      expect(table.visibleRows.map(row => row.id)).toEqual(['A', 'B', 'C']);
      expect(table.getRowByKey(['A']).expanded).toBe(false);
      expect(table.getRowByKey(['B']).expanded).toBe(false);
      expect(table.getRowByKey(['C']).expanded).toBe(false);
      expect(table.selectedRows.map(row => row.id)).toEqual([]);

      table.restoreSelection([['A11'], ['B1'], ['C']]);
      expect(table.visibleRows.map(row => row.id)).toEqual(['A', 'A1', 'A11', 'A12', 'A2', 'A3', 'B', 'B1', 'B2', 'C']);
      expect(table.getRowByKey(['A']).expanded).toBe(true);
      expect(table.getRowByKey(['A1']).expanded).toBe(true);
      expect(table.getRowByKey(['A2']).expanded).toBe(false);
      expect(table.getRowByKey(['B']).expanded).toBe(true);
      expect(table.getRowByKey(['B1']).expanded).toBe(false);
      expect(table.getRowByKey(['B2']).expanded).toBe(false);
      expect(table.getRowByKey(['C']).expanded).toBe(false);
      expect(table.selectedRows.map(row => row.id)).toEqual(['A11', 'B1', 'C']);

      table.collapseAll();
      expect(table.getRowByKey(['A']).expanded).toBe(false);
      expect(table.getRowByKey(['B']).expanded).toBe(false);
      expect(table.getRowByKey(['C']).expanded).toBe(false);
      expect(table.selectedRows.map(row => row.id)).toEqual(['C']);

      // ----------

      table.expandParentRows(null);
      expect(table.getRowByKey(['A']).expanded).toBe(false);
      expect(table.getRowByKey(['B']).expanded).toBe(false);
      expect(table.getRowByKey(['C']).expanded).toBe(false);
      expect(table.selectedRows.map(row => row.id)).toEqual(['C']);
      table.expandParentRows([]);
      expect(table.getRowByKey(['A']).expanded).toBe(false);
      expect(table.getRowByKey(['B']).expanded).toBe(false);
      expect(table.getRowByKey(['C']).expanded).toBe(false);
      expect(table.selectedRows.map(row => row.id)).toEqual(['C']);
      table.expandParentRows([table.getRowByKey(['B11']), table.getRowByKey(['B21'])]);
      expect(table.getRowByKey(['A']).expanded).toBe(false);
      expect(table.getRowByKey(['B']).expanded).toBe(true);
      expect(table.getRowByKey(['B1']).expanded).toBe(true);
      expect(table.getRowByKey(['B2']).expanded).toBe(true);
      expect(table.getRowByKey(['C']).expanded).toBe(false);
      expect(table.selectedRows.map(row => row.id)).toEqual(['C']);
    });
  });
});
