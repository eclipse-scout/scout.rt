/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {SpecTable, SpecTableModel, TableSpecHelper} from '../../src/testing/index';
import {Column, HtmlTile, KeyTableFilter, scout, TableRow, TableTextUserFilter, Tile, TileAccordion} from '../../src';

describe('TableTileMode', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;
  let model: SpecTableModel;
  let table: SpecTable;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);

    // regular table
    model = helper.createModelFixture(2, 5);
    table = helper.createTable(model);
    table.createTileForRow = row => {
      let model = {
        parent: table,
        content: '<p>' + row.id + '</p>'
      };
      return scout.create(HtmlTile, model);
    };
    table.render();
  });

  it('shows tiles instead of rows', () => {
    table.setTileMode(true);
    expect(table.tileMode).toBe(true);
    expect(table.tableTileGridMediator.tiles.length).toBe(table.rows.length);
  });

  it('synchronizes selection', () => {
    table.setTileMode(true);
    table.selectRows([table.rows[1], table.rows[2], table.rows[3]]);
    expect(table.selectedRows.length).toBe(3);
    expect(table.tableTileGridMediator.tileAccordion.getSelectedTileCount()).toBe(3);
    table.tableTileGridMediator.tileAccordion.selectTile(table.tableTileGridMediator.tiles[1]);
    expect(table.tableTileGridMediator.tileAccordion.getSelectedTileCount()).toBe(1);
    expect(table.selectedRows.length).toBe(1);
    expect(table.tableTileGridMediator.tiles[1].rowId).toBe(table.selectedRows[0].id);
  });

  it('groups data', () => {
    table.setTileMode(true);
    expect(table.tableTileGridMediator.tileAccordion.groups.map(g => g.id)).toEqual(['default']);

    table.group(table.columns[0], 'desc');
    expect(table.tableTileGridMediator.tileAccordion.groups.map(g => g.id)).toEqual(['4_0', '3_0', '2_0', '1_0', '0_0']);

    table.sort(table.columns[0], 'asc');
    expect(table.tableTileGridMediator.tileAccordion.groups.map(g => g.id)).toEqual(['0_0', '1_0', '2_0', '3_0', '4_0']);
  });

  it('reorders groups when tiles are reordered', () => {
    let table = helper.createTable({
      parent: session.desktop,
      columns: [
        {
          id: 'NameColumn',
          objectType: Column,
          summary: true
        },
        {
          id: 'TypeColumn',
          objectType: Column,
          grouped: true,
          sortIndex: 0,
          sortAscending: false
        }
      ],
      rows: [
        {id: 'row_a1', cells: ['A1', 'A']},
        {id: 'row_a2', cells: ['A2', 'A']},
        {id: 'row_b1', cells: ['B1', 'B']},
        {id: 'row_b2', cells: ['B2', 'B']},
        {id: 'row_c1', cells: ['C1', 'C']},
        {id: 'row_c2', cells: ['C2', 'C']}
      ]
    });

    class SpecTile extends Tile {
      row: TableRow;
    }

    table.createTileForRow = row => scout.create(SpecTile, {
      parent: table, row
    });
    table.setTileMode(true);
    table.render();
    let tileAccordion = table.tableTileGridMediator.tileAccordion as TileAccordion<SpecTile>;

    let tiles = tileAccordion.getTiles();
    expect(table.rows.map(row => row.id)).toEqual(['row_c1', 'row_c2', 'row_b1', 'row_b2', 'row_a1', 'row_a2']);
    expect(tiles.map(tile => tile.row.id)).toEqual(['row_c1', 'row_c2', 'row_b1', 'row_b2', 'row_a1', 'row_a2']);
    expect(tileAccordion.groups.map(group => group.title)).toEqual(['C', 'B', 'A']);

    table.sort(table.columnById('TypeColumn'), 'asc');

    tiles = tileAccordion.getTiles();
    expect(table.rows.map(row => row.id)).toEqual(['row_a1', 'row_a2', 'row_b1', 'row_b2', 'row_c1', 'row_c2']);
    expect(tiles.map(tile => tile.row.id)).toEqual(['row_a1', 'row_a2', 'row_b1', 'row_b2', 'row_c1', 'row_c2']);
    expect(tileAccordion.groups.map(group => group.title)).toEqual(['A', 'B', 'C']);
  });

  it('filters data (key filter)', () => {
    table.setCellValue(table.columns[0], table.rows[0], 'u001');
    table.setCellValue(table.columns[0], table.rows[1], 'u002');
    table.setCellValue(table.columns[0], table.rows[2], 'u003');
    table.setCellValue(table.columns[0], table.rows[3], 'u004');
    table.setCellValue(table.columns[0], table.rows[4], 'u005');
    table.setCellText(table.columns[0], table.rows[0], 'Alice Smith');
    table.setCellText(table.columns[0], table.rows[1], 'Bob Jones');
    table.setCellText(table.columns[0], table.rows[2], 'Charlie Black');
    table.setCellText(table.columns[0], table.rows[3], 'Doris Smith');
    table.setCellText(table.columns[0], table.rows[4], 'Elias Crawford');

    table.setTileMode(true);
    expect(table.filteredRows().length).toBe(5);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(5);

    let filter1 = new KeyTableFilter(row => row.cells[0].value);
    filter1.setAcceptedKeys('u001', 'u002', 'u003');
    table.addFilter(filter1);
    expect(table.filteredRows().length).toBe(3);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(3);

    table.addFilter(filter1);
    expect(table.filteredRows().length).toBe(3);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(3);

    table.removeFilter(filter1);
    expect(table.filteredRows().length).toBe(5);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(5);

    let filter2 = new KeyTableFilter(row => row.cells[0].value);
    filter2.setAcceptedKeys('u001', 'u003', 'u004', 'u005');
    table.addFilter(filter2);
    expect(table.filteredRows().length).toBe(4);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(4);

    let filter3 = scout.create(TableTextUserFilter, {
      session: session,
      table: table,
      text: 'jones'
    });
    table.addFilter(filter3);
    expect(table.filteredRows().length).toBe(0);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(0);

    let filter4 = scout.create(TableTextUserFilter, {
      session: session,
      table: table,
      text: 'black'
    });
    table.addFilter(filter4);
    expect(table.filteredRows().length).toBe(1);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(1);

    let filter5 = scout.create(TableTextUserFilter, {
      session: session,
      table: table,
      text: 'smith'
    });
    table.addFilter(filter5);
    expect(table.filteredRows().length).toBe(2);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(2);

    table.removeFilter(filter5);
    expect(table.filteredRows().length).toBe(4);
    expect(table.tableTileGridMediator.tileAccordion.getFilteredTileCount()).toBe(4);
  });

  it('updates enabled state correctly', () => {
    table.setEnabled(false);
    expect(table.$data.isEnabled()).toBe(false);

    table.setTileMode(true);
    table.setTileMode(false);
    expect(table.$data.isEnabled()).toBe(false);

    table.setEnabled(true);
    expect(table.$data.isEnabled()).toBe(true);

    table.setTileMode(true);
    table.setEnabled(false);
    table.setTileMode(false);
    expect(table.$data.isEnabled()).toBe(false);
  });

  it('restores focus', () => {
    table.focus();
    expect(table.$data).toHaveAttr('tabindex', '0');
    expect(table.$container).not.toHaveAttr('tabindex');
    expect(table.$data).toBeFocused();

    table.setTileMode(true);
    expect(table.$data).not.toHaveAttr('tabindex');
    expect(table.$container).toHaveAttr('tabindex', '0');
    expect(table.$container).toBeFocused();

    table.setTileMode(false);
    expect(table.$data).toHaveAttr('tabindex', '0');
    expect(table.$container).not.toHaveAttr('tabindex');
    expect(table.$data).toBeFocused();

    let $input = session.$entryPoint.appendElement('<input>');
    $input[0].focus();
    expect($input).toBeFocused();

    table.setTileMode(true);
    expect($input).toBeFocused();

    table.setTileMode(false);
    expect($input).toBeFocused();
  });
});
