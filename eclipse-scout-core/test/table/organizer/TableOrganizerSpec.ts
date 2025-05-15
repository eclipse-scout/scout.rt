/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableSpecHelper} from '../../../src/testing/index';
import {arrays, Column, scout, TableOrganizer} from '../../../src';

describe('TableOrganizer', () => {

  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
  });

  afterEach(() => {
    session = null;
  });

  describe('install/uninstall', () => {

    it('does nothing without a table', () => {
      let organizer = scout.create(TableOrganizer);
      expect(organizer.table).toBeFalsy();
      organizer.uninstall(); // does not throw
      expect(organizer.getInvisibleColumns()).toEqual([]);
      expect(organizer.isColumnAddable()).toBe(false);
      let table = helper.createTable(helper.createModelFixture(1));
      expect(organizer.isColumnRemovable(table.columns[0])).toBe(false);
      expect(organizer.isColumnModifiable(table.columns[0])).toBe(false);
    });

    it('automatically creates a table organizer', () => {
      let tableModel = helper.createModelFixture(1);
      let table = helper.createTable(tableModel);

      expect(table.organizer).toBeInstanceOf(TableOrganizer);
    });

    it('does not automatically create a table organizer when explicitly set a custom instance', () => {
      let organizer = scout.create(TableOrganizer);

      let tableModel = helper.createModelFixture(1);
      tableModel.organizer = organizer;
      let table = helper.createTable(tableModel);

      expect(table.organizer).toBe(organizer);
    });

    it('does not automatically create a table organizer when explicitly set to null', () => {
      let tableModel = helper.createModelFixture(1);
      tableModel.organizer = null;
      let table = helper.createTable(tableModel);

      expect(table.organizer).toBe(null);
    });
  });

  describe('isColumnAddable', () => {

    it('is addable if there are addable columns', () => {
      let tableModel = helper.createModelFixture(1);
      let table = helper.createTable(tableModel);
      let organizer = table.organizer;

      organizer.getInvisibleColumns = () => [];
      expect(organizer.isColumnAddable()).toBe(false);
      organizer.getInvisibleColumns = () => [scout.create(Column, {parent: table})];
      expect(organizer.isColumnAddable()).toBe(true);
    });

    it('is addable if table is customizable', () => {
      let tableModel = helper.createModelFixture(1);
      let table = helper.createTable(tableModel);
      let organizer = table.organizer;

      organizer.getInvisibleColumns = () => [];
      expect(organizer.isColumnAddable()).toBe(false);
      table.isCustomizable = () => true;
      expect(organizer.isColumnAddable()).toBe(true);
      organizer.getInvisibleColumns = () => [scout.create(Column, {parent: table})];
      expect(organizer.isColumnAddable()).toBe(true);
    });

    it('is not addable if explicitly disabled', () => {
      let tableModel = helper.createModelFixture(1);
      tableModel.columnAddable = false;
      let table = helper.createTable(tableModel);
      let organizer = table.organizer;

      organizer.getInvisibleColumns = () => [];
      expect(organizer.isColumnAddable()).toBe(false);
      table.isCustomizable = () => true;
      expect(organizer.isColumnAddable()).toBe(false);
      organizer.getInvisibleColumns = () => [scout.create(Column, {parent: table})];
      expect(organizer.isColumnAddable()).toBe(false);

      table.columnAddable = true;
      expect(organizer.isColumnAddable()).toBe(true);
    });

    it('is not addable without table customizer', () => {
      let tableModel = helper.createModelFixture(1);
      tableModel.customizer = null;
      let table = helper.createTable(tableModel);

      expect(table.isColumnAddable()).toBe(false);
      table.columnAddable = true;
      expect(table.isColumnAddable()).toBe(false); // still false
    });
  });

  describe('isColumnRemovable', () => {

    it('is not removable if column position is fixed', () => {
      let tableModel = helper.createModelFixture(3);
      tableModel.columns[1].fixedPosition = true;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2];
      let organizer = table.organizer;

      expect(organizer.isColumnRemovable(column0)).toBe(true);
      expect(organizer.isColumnRemovable(column1)).toBe(false);
      expect(organizer.isColumnRemovable(column2)).toBe(true);
    });

    it('is not removable if column is the last visible column', () => {
      let tableModel = helper.createModelFixture(2);
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let organizer = table.organizer;

      expect(organizer.isColumnRemovable(column0)).toBe(true);
      expect(organizer.isColumnRemovable(column1)).toBe(true);
      arrays.remove(table.columns, column0);
      expect(organizer.isColumnRemovable(column0)).toBe(false); // already removed
      expect(organizer.isColumnRemovable(column1)).toBe(false); // last visible column
    });

    it('is removable if table is customizable', () => {
      let tableModel = helper.createModelFixture(1);
      let table = helper.createTable(tableModel);
      let column = table.columns[0];
      let organizer = table.organizer;

      expect(organizer.isColumnRemovable(column)).toBe(false);
      table.isCustomizable = () => true;
      expect(organizer.isColumnRemovable(column)).toBe(true);
    });

    it('is not removable if explicitly disabled', () => {
      let tableModel = helper.createModelFixture(2);
      tableModel.columns[0].removable = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let organizer = table.organizer;

      expect(organizer.isColumnRemovable(column0)).toBe(false);
      expect(organizer.isColumnRemovable(column1)).toBe(true);
    });

    it('is not removable without table customizer', () => {
      let tableModel = helper.createModelFixture(2);
      tableModel.organizer = null;
      tableModel.columns[0].removable = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];

      expect(table.isColumnRemovable(column0)).toBe(false);
      expect(table.isColumnRemovable(column1)).toBe(false);
    });
  });

  describe('isColumnModifiable', () => {

    it('is modifiable if table is customizable', () => {
      let tableModel = helper.createModelFixture(1);
      let table = helper.createTable(tableModel);
      let column = table.columns[0];
      let organizer = table.organizer;

      expect(organizer.isColumnModifiable(column)).toBe(false);
      table.isCustomizable = () => true;
      expect(organizer.isColumnModifiable(column)).toBe(true);
    });

    it('is not modifiable if explicitly disabled', () => {
      let tableModel = helper.createModelFixture(2);
      tableModel.columns[0].modifiable = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let organizer = table.organizer;

      expect(organizer.isColumnModifiable(column0)).toBe(false);
      expect(organizer.isColumnModifiable(column1)).toBe(false);

      table.isCustomizable = () => true;
      expect(organizer.isColumnModifiable(column0)).toBe(false);
      expect(organizer.isColumnModifiable(column1)).toBe(true);
    });

    it('is not modifiable without table customizer', () => {
      let tableModel = helper.createModelFixture(2);
      tableModel.organizer = null;
      tableModel.columns[0].modifiable = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];

      expect(table.isColumnModifiable(column0)).toBe(false);
      expect(table.isColumnModifiable(column1)).toBe(false);
    });
  });

  describe('getInvisibleColumns', () => {

    it('returns all invisible columns if no insertAfterColumn is provided', () => {
      let tableModel = helper.createModelFixture(5);
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2];
      let column3 = table.columns[3];
      let column4 = table.columns[4];
      let organizer = table.organizer;
      column2.setDisplayable(false);

      expect(organizer.getInvisibleColumns()).toEqual([]);
      column1.setVisible(false);
      expect(organizer.getInvisibleColumns()).toEqual([column1]);
      column0.setVisible(false);
      expect(organizer.getInvisibleColumns()).toEqual([column0, column1]);
      column4.setVisible(false);
      expect(organizer.getInvisibleColumns()).toEqual([column0, column1, column4]);
      column2.setVisible(false);
      column3.setVisible(false);
      expect(organizer.getInvisibleColumns()).toEqual([column0, column1, column3, column4]);
      column2.setVisible(true);
      expect(organizer.getInvisibleColumns()).toEqual([column0, column1, column3, column4]);
      column3.setVisible(true);
      expect(organizer.getInvisibleColumns()).toEqual([column0, column1, column4]);

      expect(organizer.getInvisibleColumns(column3)).toEqual([column0, column1, column4]);
    });

    it('returns only invisible columns that are between fixed columns around the insertAfterColumn', () => {
      let tableModel = helper.createModelFixture(8);
      tableModel.columns[1].visible = false;
      tableModel.columns[2].fixedPosition = true;
      tableModel.columns[3].visible = false;
      tableModel.columns[4].visible = false;
      tableModel.columns[5].fixedPosition = true;
      tableModel.columns[6].visible = false;
      tableModel.columns[7].fixedPosition = true;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2]; // fixed
      let column3 = table.columns[3];
      let column4 = table.columns[4];
      let column5 = table.columns[5]; // fixed
      let column6 = table.columns[6];
      let column7 = table.columns[7]; // fixed
      let organizer = table.organizer;

      expect(organizer.getInvisibleColumns()).toEqual([column1, column3, column4, column6]);
      expect(organizer.getInvisibleColumns(column0)).toEqual([column1]);
      expect(organizer.getInvisibleColumns(column1)).toEqual([column1, column3, column4, column6]);
      expect(organizer.getInvisibleColumns(column2)).toEqual([column3, column4]);
      expect(organizer.getInvisibleColumns(column3)).toEqual([column1, column3, column4, column6]);
      expect(organizer.getInvisibleColumns(column4)).toEqual([column1, column3, column4, column6]);
      expect(organizer.getInvisibleColumns(column5)).toEqual([column6]);
      expect(organizer.getInvisibleColumns(column6)).toEqual([column1, column3, column4, column6]);
      expect(organizer.getInvisibleColumns(column7)).toEqual([]);

      column5.setVisible(false);
      expect(organizer.getInvisibleColumns()).toEqual([column1, column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column0)).toEqual([column1]);
      expect(organizer.getInvisibleColumns(column1)).toEqual([column1, column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column2)).toEqual([column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column3)).toEqual([column1, column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column4)).toEqual([column1, column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column5)).toEqual([column1, column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column6)).toEqual([column1, column3, column4, column5, column6]);
      expect(organizer.getInvisibleColumns(column7)).toEqual([]);
    });

    it('ignores insertAfterColumn when it is invisible or belongs to a different table', () => {
      let tableModel = helper.createModelFixture(5);
      tableModel.columns[1].visible = false;
      tableModel.columns[3].fixedPosition = true;
      tableModel.columns[4].visible = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2];
      let column3 = table.columns[3];
      let column4 = table.columns[4];
      let organizer = table.organizer;

      expect(organizer.getInvisibleColumns()).toEqual([column1, column4]);
      expect(organizer.getInvisibleColumns(column0)).toEqual([column1]);
      expect(organizer.getInvisibleColumns(column1)).toEqual([column1, column4]); // invisible
      expect(organizer.getInvisibleColumns(column2)).toEqual([column1]);
      expect(organizer.getInvisibleColumns(column3)).toEqual([column4]);
      expect(organizer.getInvisibleColumns(column4)).toEqual([column1, column4]); // invisible

      let tableModel2 = helper.createModelFixture(1);
      tableModel2.columns[0].fixedPosition = true;
      let table2 = helper.createTable(tableModel2);
      let column0_2 = table2.columns[0];

      expect(organizer.getInvisibleColumns(column0_2)).toEqual([column1, column4]); // wrong table
    });
  });

  describe('showColumns', () => {

    it('makes selected columns visible in-place', () => {
      let tableModel = helper.createModelFixture(6);
      tableModel.columns[1].visible = false;
      tableModel.columns[3].visible = false;
      tableModel.columns[5].visible = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2];
      let column3 = table.columns[3];
      let column4 = table.columns[4];
      let column5 = table.columns[5];
      let organizer = table.organizer;
      spyOn(table, 'onColumnVisibilityChanged');

      expect(table.visibleColumns()).toEqual([column0, column2, column4]);
      expect(table.displayableColumns()).toEqual([column0, column1, column2, column3, column4, column5]);

      organizer.showColumns([column1, column3]);

      expect(table.visibleColumns()).toEqual([column0, column1, column2, column3, column4]);
      expect(table.displayableColumns()).toEqual([column0, column1, column2, column3, column4, column5]);
      expect(table.onColumnVisibilityChanged).toHaveBeenCalledTimes(1);
    });

    it('moves the selected columns after insertAfterColumn', () => {
      let tableModel = helper.createModelFixture(6);
      tableModel.columns[1].visible = false;
      tableModel.columns[3].visible = false;
      tableModel.columns[5].visible = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2];
      let column3 = table.columns[3];
      let column4 = table.columns[4];
      let column5 = table.columns[5];
      let organizer = table.organizer;
      spyOn(table, 'onColumnVisibilityChanged');

      expect(table.visibleColumns()).toEqual([column0, column2, column4]);
      expect(table.displayableColumns()).toEqual([column0, column1, column2, column3, column4, column5]);

      organizer.showColumns([column1, column3], column4);

      expect(table.visibleColumns()).toEqual([column0, column2, column4, column1, column3]);
      expect(table.displayableColumns()).toEqual([column0, column2, column4, column1, column3, column5]);
      expect(table.onColumnVisibilityChanged).toHaveBeenCalledTimes(1);
    });

    it('ignores insertAfterColumn when it is invisible or belongs to a different table', () => {
      let tableModel = helper.createModelFixture(6);
      tableModel.columns[1].visible = false;
      tableModel.columns[3].visible = false;
      tableModel.columns[5].visible = false;
      let table = helper.createTable(tableModel);
      let column0 = table.columns[0];
      let column1 = table.columns[1];
      let column2 = table.columns[2];
      let column3 = table.columns[3];
      let column4 = table.columns[4];
      let column5 = table.columns[5];
      let organizer = table.organizer;
      let spy = spyOn(table, 'onColumnVisibilityChanged');

      expect(table.visibleColumns()).toEqual([column0, column2, column4]);
      expect(table.displayableColumns()).toEqual([column0, column1, column2, column3, column4, column5]);

      organizer.showColumns([column1, column3], column5); // invisible

      expect(table.visibleColumns()).toEqual([column0, column1, column2, column3, column4]);
      expect(table.displayableColumns()).toEqual([column0, column1, column2, column3, column4, column5]);
      expect(table.onColumnVisibilityChanged).toHaveBeenCalledTimes(1);

      column1.setVisible(false);
      column3.setVisible(false);
      spy.calls.reset();

      let tableModel2 = helper.createModelFixture(1);
      tableModel2.columns[0].fixedPosition = true;
      let table2 = helper.createTable(tableModel2);
      let column0_2 = table2.columns[0];
      organizer.showColumns([column1, column3], column0_2); // wrong table

      expect(table.visibleColumns()).toEqual([column0, column1, column2, column3, column4]);
      expect(table.displayableColumns()).toEqual([column0, column1, column2, column3, column4, column5]);
      expect(table.onColumnVisibilityChanged).toHaveBeenCalledTimes(1);
    });
  });

  describe('moveColumns', () => {
    it('moves the columns to their new position', () => {
      let tableModel = helper.createModelFixture(4);
      let table = helper.createTable(tableModel);
      table.columns[1].setVisible(false);
      let columns = table.columns.slice();
      expect(table.visibleColumns()).toEqual([columns[0], columns[2], columns[3]]);

      table.organizer.moveColumns([columns[2], columns[3], columns[0]]);
      expect(table.visibleColumns()).toEqual([columns[2], columns[3], columns[0]]);
    });

    it('can move only some of the columns', () => {
      let tableModel = helper.createModelFixture(3);
      let table = helper.createTable(tableModel);
      let columns = table.columns.slice();
      expect(table.visibleColumns()).toEqual(columns);

      table.organizer.moveColumns([columns[2], columns[1]]);
      expect(table.visibleColumns()).toEqual([columns[2], columns[1], columns[0]]);
    });

    it('enhances the given array with guiOnly columns if necessary', () => {
      let tableModel = helper.createModelFixture(2);
      let table = helper.createTable(tableModel);
      table.setCheckable(true);
      table.setRowIconVisible(true);
      let columns = table.columns.slice();
      expect(table.visibleColumns()).toEqual([columns[0], columns[1], columns[2], columns[3]]);

      table.organizer.moveColumns([columns[3], columns[2]]);
      expect(table.visibleColumns()).toEqual([columns[0], columns[1], columns[3], columns[2]]);

      table.organizer.moveColumns([columns[0], columns[1], columns[3], columns[2]]);
      expect(table.visibleColumns()).toEqual([columns[0], columns[1], columns[3], columns[2]]);
    });
  });
});
