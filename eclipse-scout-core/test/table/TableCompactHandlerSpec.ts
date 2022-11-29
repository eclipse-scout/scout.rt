/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {CompactColumn} from '../../src';
import {TableSpecHelper} from '../../src/testing/index';

describe('TableCompactHandler', () => {
  let session, helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
  });

  afterEach(() => {
    session = null;
  });

  describe('handle', () => {
    describe('compact is true', () => {
      it('adds compact column and makes others invisible', () => {
        let table = helper.createTable(helper.createModelFixture(2, 1));
        expect(table.columns[0].isVisible()).toBe(true);
        expect(table.columns[1].isVisible()).toBe(true);
        expect(table.compactColumn).toBe(null);

        table.setCompact(true);
        expect(table.columns[0].isVisible()).toBe(false);
        expect(table.columns[1].isVisible()).toBe(false);
        expect(table.compactColumn instanceof CompactColumn).toBe(true);
      });

      it('ignores checkable and rowIcon columns', () => {
        let table = helper.createTable(helper.createModelFixture(2, 1));
        table.setCheckable(true);
        table.setRowIconVisible(true);
        expect(table.checkableColumn.isVisible()).toBe(true);
        expect(table.rowIconColumn.isVisible()).toBe(true);
        expect(table.columns[2].isVisible()).toBe(true);
        expect(table.columns[3].isVisible()).toBe(true);
        expect(table.compactColumn).toBe(null);

        table.setCompact(true);
        expect(table.checkableColumn.isVisible()).toBe(true);
        expect(table.rowIconColumn.isVisible()).toBe(true);
        expect(table.columns[2].isVisible()).toBe(false);
        expect(table.columns[3].isVisible()).toBe(false);
        expect(table.compactColumn instanceof CompactColumn).toBe(true);
      });
    });

    describe('compact is false', () => {
      it('removes compact column and makes others visible', () => {
        let table = helper.createTable(helper.createModelFixture(2, 1));
        table.setCompact(true);
        expect(table.columns[0].isVisible()).toBe(false);
        expect(table.columns[1].isVisible()).toBe(false);
        expect(table.compactColumn instanceof CompactColumn).toBe(true);

        table.setCompact(false);
        expect(table.columns[0].isVisible()).toBe(true);
        expect(table.columns[1].isVisible()).toBe(true);
        expect(table.compactColumn).toBe(null);
      });
    });
  });

  describe('updateValue', () => {
    it('computes compactValue for each row', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      let row0 = table.rows[0];
      let row1 = table.rows[1];
      expect(row0.compactValue).toBe(null);
      expect(row1.compactValue).toBe(null);

      table.setCompact(true);
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[0].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[1].text) >= 0).toBe(true);
    });

    it('renders compactValue', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      table.render();
      let row0 = table.rows[0];
      let row1 = table.rows[1];
      expect(row0.compactValue).toBe(null);
      expect(row1.compactValue).toBe(null);

      table.setCompact(true);
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[0].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[1].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row1.$row)[0].innerHTML.indexOf(row1.cells[0].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row1.$row)[0].innerHTML.indexOf(row1.cells[1].text) >= 0).toBe(true);
    });

    it('updates compactValue if a row is updated', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      table.render();
      table.setCompact(true);
      let row0 = table.rows[0];
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);

      let row = {
        id: row0.id,
        cells: ['newCellText0', 'newCellText1']
      };
      table.updateRows([row]);
      row0 = table.rows[0];
      expect(row0.compactValue.indexOf('newCellText0') >= 0).toBe(true);
      expect(row0.compactValue.indexOf('newCellText1') >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf('newCellText1') >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf('newCellText1') >= 0).toBe(true);
    });

    it('computes compactValue if a row is inserted', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      table.render();
      table.setCompact(true);

      table.insertRow({cells: ['newCellText0', 'newCellText1']});
      expect(table.rows[2].compactValue.indexOf('newCellText0') >= 0).toBe(true);
      expect(table.rows[2].compactValue.indexOf('newCellText1') >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, table.rows[2].$row)[0].innerHTML.indexOf('newCellText1') >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, table.rows[2].$row)[0].innerHTML.indexOf('newCellText1') >= 0).toBe(true);
    });

    it('updates compactValue if a column toggles its visibility', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      table.render();
      table.setCompact(true);
      let row0 = table.rows[0];
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);

      table.columns[0].setVisible(false);
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(false);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf(row0.cells[0].text) >= 0).toBe(false);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf(row0.cells[1].text) >= 0).toBe(true);

      table.columns[0].setVisible(true);
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(table.$cell(table.compactColumn, row0.$row)[0].innerHTML.indexOf(row0.cells[1].text) >= 0).toBe(true);
    });
  });

  describe('useOnlyVisibleColumns', () => {
    it('if true invisible columns are ignored', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      table.columns[0].setVisible(false);
      let row0 = table.rows[0];
      let row1 = table.rows[1];
      expect(row0.compactValue).toBe(null);
      expect(row1.compactValue).toBe(null);

      table.setCompact(true);
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(false);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[0].text) >= 0).toBe(false);
      expect(row1.compactValue.indexOf(row1.cells[1].text) >= 0).toBe(true);
    });

    it('if false invisible columns are not ignored', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      table.columns[0].setVisible(false);
      table.compactHandler.setUseOnlyVisibleColumns(false);
      let row0 = table.rows[0];
      let row1 = table.rows[1];
      expect(row0.compactValue).toBe(null);
      expect(row1.compactValue).toBe(null);

      table.setCompact(true);
      expect(row0.compactValue.indexOf(row0.cells[0].text) >= 0).toBe(true);
      expect(row0.compactValue.indexOf(row0.cells[1].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[0].text) >= 0).toBe(true);
      expect(row1.compactValue.indexOf(row1.cells[1].text) >= 0).toBe(true);
    });
  });
});
