/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {SpecTable, TableModelWithCells, TableSpecHelper} from '../../src/testing/index';
import {HtmlTile, KeyTableFilter, scout, TableTextUserFilter} from '../../src';

describe('TableTileModeSpec', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;
  let model: TableModelWithCells;
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

  afterEach(() => {
    session = null;
    helper = null;
    model = null;
    table = null;
  });

  describe('tile mode', () => {
    it('table', () => {
      expect(table.rendered).toBe(true);
    });

    it('switch', () => {
      table.setTileMode(true);
      expect(table.tileMode).toBe(true);
      expect(table.tableTileGridMediator.tiles.length).toBe(table.rows.length);
    });

    it('selection synchronizes', () => {
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
      table.columns[0].grouped = true;
      table.sort(table.columns[0], 'desc');
      table.setTileMode(true);
      expect(table.tableTileGridMediator.tileAccordion.groups.length).toBe(5);
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
  });
});
