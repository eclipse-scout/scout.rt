/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BooleanColumn, Column, DateColumn, NumberColumn, ObjectIdProvider, scout, Table, TableClientUiPreferenceProfileDo, TableClientUiPreferencesDo, TableColumnClientUiPreferenceDo, TableRow, TableTextUserFilter, tableUiPreferences,
  TableUiPreferences, TextColumnUserFilter, Tile, uiPreferences, UiPreferencesDo, WidgetModel
} from '../../src/index';
import {SpecUiPreferencesStore} from '../../src/testing';

describe('TableUiPreferences', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  describe('global settings', () => {

    it('stores changes and can restore them', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1',
        uiPreferencesEnabled: true
      });

      let c2 = table.columnById('c2');
      c2.setWidth(202);

      jasmine.clock().tick(1000);
      let prefStore = SpecUiPreferencesStore.get();
      expect(prefStore.preferences).toBeInstanceOf(UiPreferencesDo);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(false);
      expect(prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.size).toBe(1);
      let profile = prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.get(TableUiPreferences.PROFILE_ID_GLOBAL);
      expect(profile).toBeTruthy();
      expect(profile.columns.length).toBe(5);
      expect(profile.columns[0].columnId).toBe('c2');
      expect(profile.columns[0].width).toBe(202); // <--
      expect(profile.columns[1].columnId).toBe('c3');
      expect(profile.columns[1].width).toBe(103);
      expect(profile.columns.map(c => c.columnId)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);

      let c6 = table.columnById('c6');
      table.moveColumn(c6, 1);

      jasmine.clock().tick(1000);
      expect(prefStore.preferences).toBeInstanceOf(UiPreferencesDo);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(false);
      expect(prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.size).toBe(1);
      profile = prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.get(TableUiPreferences.PROFILE_ID_GLOBAL);
      expect(profile).toBeTruthy();
      expect(profile.columns.length).toBe(5);
      expect(profile.columns.map(c => c.columnId)).toEqual(['c2', 'c6', 'c3', 'c4', 'c5']);

      table.setTileMode(true);

      jasmine.clock().tick(1000);
      expect(prefStore.preferences).toBeInstanceOf(UiPreferencesDo);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(true); // <--
      expect(prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.size).toBe(1);

      // -----

      // Create a new table instance with the same id but uiPreferencesEnabled=false
      table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1'
      });
      expect(table.tileMode).toBe(false);
      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.columns.map(c => c.width)).toEqual([60, 102, 103, 104, 105, 106]);

      // Create a new table instance with the same id and uiPreferencesEnabled=true
      table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1',
        uiPreferencesEnabled: true
      });
      expect(table.tileMode).toBe(true);
      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c6', 'c3', 'c4', 'c5']);
      expect(table.columns.map(c => c.width)).toEqual([60, 202, 106, 103, 104, 105]);
    });

    it('restore to factory setting resets table to initial settings', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1',
        uiPreferencesEnabled: true
      });
      let prefStore = SpecUiPreferencesStore.get();
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(0);
      expect(prefStore.preferences).toBe(null);

      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, false, true, false]);

      let c2 = table.columnById('c2');
      let c3 = table.columnById('c3');
      let c4 = table.columnById('c4');
      let c5 = table.columnById('c5');
      let c6 = table.columnById('c6');

      c2.setWidth(202);
      table.group(c3);
      table.addSortColumn(c4, 'desc');
      c5.setVisible(false);
      table.removeSortColumn(c5);
      table.moveColumn(c6, 1);

      expect(c2.width).toBe(202);
      expect(c3.grouped).toBe(true);
      expect(c3.sortIndex).toBe(0);
      expect(c3.sortAscending).toBe(true);
      expect(c4.sortIndex).toBe(1);
      expect(c4.sortAscending).toBe(false);
      expect(c5.visible).toBe(false);
      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c6', 'c3', 'c4', 'c5']);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, true, true, false]);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c2', 'c6', 'c3', 'c4']);

      // Wait for it to have been stored
      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(1);
      expect(prefStore.preferences).toBeInstanceOf(UiPreferencesDo);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(false);
      expect(prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.size).toBe(1);

      // -----

      table.resetToInitialUiPreferences();
      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(2);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(false);
      expect(prefStore.preferences.tablePreferences[0].tablePreferenceProfiles.size).toBe(0); // <--

      expect(c2.width).toBe(102);
      expect(c3.grouped).toBe(false);
      expect(c3.sortIndex).toBe(-1);
      expect(c3.sortAscending).toBe(true);
      expect(c4.sortIndex).toBe(-1);
      expect(c4.sortAscending).toBe(true);
      expect(c5.visible).toBe(true);
      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, false, true, false]);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);

      // -----

      // Reset a second time (should not have an effect)
      table.resetToInitialUiPreferences();
      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(2); // <-- unchanged

      expect(c2.width).toBe(102);
      expect(c3.grouped).toBe(false);
      expect(c3.sortIndex).toBe(-1);
      expect(c3.sortAscending).toBe(true);
      expect(c4.sortIndex).toBe(-1);
      expect(c4.sortAscending).toBe(true);
      expect(c5.visible).toBe(true);
      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, false, true, false]);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);
    });

    it('restore to factory setting does not reset the tile mode', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1',
        uiPreferencesEnabled: true
      });
      let prefStore = SpecUiPreferencesStore.get();
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(0);
      expect(prefStore.preferences).toBe(null);

      table.setTileMode(true);

      // Wait for it to have been stored
      jasmine.clock().tick(1000);
      expect(prefStore.preferences).toBeInstanceOf(UiPreferencesDo);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(true);

      // -----

      table.resetToInitialUiPreferences();
      jasmine.clock().tick(1000);
      expect(prefStore.preferences.tablePreferences.length).toBe(1);
      expect(prefStore.preferences.tablePreferences[0].tableId).toBe('t1');
      expect(prefStore.preferences.tablePreferences[0].tileMode).toBe(true);

      expect(table.tileMode).toBe(true);
    });

    it('can toggle uiPreferencesEnabled', async () => {
      let prefStore = SpecUiPreferencesStore.get();
      prefStore.preferences = scout.create(UiPreferencesDo, {
        tablePreferences: [
          scout.create(TableClientUiPreferencesDo, {
            tableId: 't1',
            tileMode: true,
            tablePreferenceProfiles: new Map([
              [TableUiPreferences.PROFILE_ID_GLOBAL, scout.create(TableClientUiPreferenceProfileDo, {
                columns: [
                  scout.create(TableColumnClientUiPreferenceDo, {
                    columnId: 'c2',
                    width: 302
                  }),
                  scout.create(TableColumnClientUiPreferenceDo, {
                    columnId: 'c3',
                    width: 303
                  })
                ]
              })]
            ])
          })
        ]
      });
      jasmine.clock().uninstall();
      await uiPreferences.load();
      jasmine.clock().install();

      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1'
      });
      let c2 = table.columnById('c2');
      let c3 = table.columnById('c3');
      let c4 = table.columnById('c4');
      let listenerCount1 = table.events.count();

      expect(c2.width).toBe(102);
      expect(c3.width).toBe(103);
      expect(c4.width).toBe(104);

      c2.setWidth(202);
      jasmine.clock().tick(1000);
      expect(c2.width).toBe(202);
      expect(prefStore.loadCount).toBe(1);
      expect(prefStore.storeCount).toBe(0); // <--

      // -----

      table.setUiPreferencesEnabled(true);
      let listenerCount2 = table.events.count();

      expect(c2.width).toBe(302);
      expect(c3.width).toBe(303);
      expect(c4.width).toBe(104);
      expect(listenerCount2).toBeGreaterThan(listenerCount1);

      c3.setWidth(403);
      c4.setWidth(404);
      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(1);
      expect(prefStore.storeCount).toBe(1); // <--

      // -----

      table.setUiPreferencesEnabled(false);
      let listenerCount3 = table.events.count();

      // Uninstalling does not reset the state
      expect(c2.width).toBe(302);
      expect(c3.width).toBe(403);
      expect(c4.width).toBe(404);
      expect(listenerCount3).toBeLessThan(listenerCount2); // some listeners were also installed by the tile grid mediator

      table.resetToInitialUiPreferences();
      jasmine.clock().tick(1000);
      // setUiPreferencesEnabled(true) has stored the previous state as the new initial state
      expect(c2.width).toBe(202);
      expect(c3.width).toBe(103);
      expect(c4.width).toBe(104);
      expect(prefStore.loadCount).toBe(1);
      expect(prefStore.storeCount).toBe(1);
    });
  });

  describe('table profiles', () => {

    it('changes table to profile without resetting filters', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1'
      });
      table.saveInitialUiPreferences();

      let c2 = table.columnById('c2');
      let c3 = table.columnById('c3');
      let c4 = table.columnById('c4');
      let c5 = table.columnById('c5');
      let c6 = table.columnById('c6');

      // -----

      c2.setWidth(202);
      table.group(c3);
      table.addSortColumn(c4, 'desc');
      c5.setVisible(false);
      table.removeSortColumn(c5);
      table.moveColumn(c6, 1);
      table.addFilter(scout.create(TextColumnUserFilter, {
        session: session,
        table: table,
        column: c2,
        freeText: 'foo'
      }));

      let profile1 = tableUiPreferences.createProfile(table);

      // -----

      c3.setWidth(203);
      table.removeGroupColumn(c3);
      table.addSortColumn(c4, 'asc');
      table.moveColumn(c6, 2);
      table.addFilter(scout.create(TableTextUserFilter, {
        session: session,
        table: table,
        text: 'bar'
      }));

      let profile2 = tableUiPreferences.createProfile(table);

      // -----

      tableUiPreferences.applyProfile(table, profile1);

      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c6', 'c3', 'c4', 'c5']);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c2', 'c6', 'c3', 'c4']);
      expect(table.columns.map(c => c.width)).toEqual([60, 202, 106, 103, 104, 105]);
      expect(table.columns.map(c => c.grouped)).toEqual([false, false, false, true, false, false]);
      expect(table.columns.map(c => c.sortIndex)).toEqual([-1, -1, -1, 0, 1, -1]);
      expect(table.columns.map(c => c.sortAscending)).toEqual([true, true, true, true, false, false]);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, true, true, false]);
      expect(table.filterCount()).toBe(2);

      tableUiPreferences.applyProfile(table, profile2);

      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c6', 'c4', 'c5']);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c2', 'c3', 'c6', 'c4']);
      expect(table.columns.map(c => c.width)).toEqual([60, 202, 203, 106, 104, 105]);
      expect(table.columns.map(c => c.grouped)).toEqual([false, false, false, false, false, false]);
      expect(table.columns.map(c => c.sortIndex)).toEqual([-1, -1, -1, -1, 0, -1]);
      expect(table.columns.map(c => c.sortAscending)).toEqual([true, true, true, true, true, false]);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, false, true, false]);
      expect(table.filterCount()).toBe(2);

      table.resetToInitialUiPreferences();

      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.columns.map(c => c.width)).toEqual([60, 102, 103, 104, 105, 106]);
      expect(table.columns.map(c => c.grouped)).toEqual([false, false, false, false, false, false]);
      expect(table.columns.map(c => c.sortIndex)).toEqual([-1, -1, -1, -1, 0, -1]);
      expect(table.columns.map(c => c.sortAscending)).toEqual([true, true, true, true, false, true]);
      expect(table.columns.map(c => c.sortActive)).toEqual([false, false, false, false, true, false]);
      expect(table.filterCount()).toBe(0);

      tableUiPreferences.applyProfile(table, profile1);
      expect(table.filterCount()).toBe(0);
    });

    it('ignores guiOnly and non-displayable columns', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1',
        checkable: true,
        rowIconVisible: true,
        rowIconColumnWidth: 96
      });
      table.saveInitialUiPreferences();
      expect(table.columns.length).toBe(6 + 2);
      expect(table.columns.map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.visibleColumns().map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c2', 'c3', 'c4', 'c5', 'c6']);

      let c2 = table.columnById('c2');
      c2.setWidth(202);

      let profile = tableUiPreferences.createProfile(table);
      expect(profile.columns.length).toBe(5);
      expect(profile.columns.map(c => c.columnId)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);

      table.resetToInitialUiPreferences();
      expect(table.columns.length).toBe(6 + 2);
      expect(table.columns.map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(c2.width).toBe(102);
      tableUiPreferences.applyProfile(table, profile);
      expect(table.columns.length).toBe(6 + 2);
      expect(table.columns.map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(c2.width).toBe(202);

      let c4 = table.columnById('c4');
      c4.setDisplayable(false);
      expect(table.visibleColumns().map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c2', 'c3', 'c5', 'c6']);
      table.moveColumn(c2, 5);
      expect(table.visibleColumns().map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c3', 'c5', 'c6', 'c2']);
      let profile2 = tableUiPreferences.createProfile(table);
      expect(profile2.columns.length).toBe(4);
      expect(profile2.columns.map(c => c.columnId)).toEqual(['c3', 'c5', 'c6', 'c2']);

      table.resetToInitialUiPreferences();
      expect(table.columns.length).toBe(6 + 2);
      expect(table.columns.map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.visibleColumns().map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c2', 'c3', 'c5', 'c6']);
      tableUiPreferences.applyProfile(table, profile2);
      expect(table.columns.length).toBe(6 + 2);
      expect(table.columns.map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c1', 'c4', 'c3', 'c5', 'c6', 'c2']);
      expect(table.visibleColumns().map(c => c.guiOnly ? null : c.id)).toEqual([null, null, 'c3', 'c5', 'c6', 'c2']);
    });

    it('places new columns at the end', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        id: 't1',
        columns: [{
          id: 'c1',
          objectType: Column,
          width: 101
        }, {
          id: 'c2',
          objectType: Column,
          displayable: false,
          width: 102
        }, {
          id: 'c3',
          objectType: Column,
          width: 103
        }, {
          id: 'c4',
          objectType: Column,
          displayable: false,
          primaryKey: true,
          width: 104
        }, {
          id: 'c5',
          objectType: Column,
          width: 105,
          visible: false
        }]
      });
      table.saveInitialUiPreferences();

      // Apply a profile without information about some columns
      tableUiPreferences.applyProfile(table, scout.create(TableClientUiPreferenceProfileDo, {
        columns: [
          scout.create(TableColumnClientUiPreferenceDo, {
            columnId: 'c3',
            viewIndex: 333,
            visible: true,
            width: 200
          }),
          scout.create(TableColumnClientUiPreferenceDo, {
            columnId: 'c4',
            viewIndex: 444,
            visible: true,
            width: 400
          }),
          scout.create(TableColumnClientUiPreferenceDo, {
            columnId: 'c5',
            viewIndex: 5,
            visible: true,
            width: 500
          })
        ]
      }));

      // expect:
      // - non-displayable columns at front, pk column first
      // - preferences ignored for non-displayable columns
      expect(table.columns.map(c => c.id)).toEqual(['c4', 'c2', 'c5', 'c3', 'c1']);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c5', 'c3', 'c1']);
      expect(table.columns.map(c => c.width)).toEqual([104, 102, 500, 200, 101]);

      table.resetToInitialUiPreferences();
      expect(table.columns.map(c => c.id)).toEqual(['c1', 'c2', 'c3', 'c4', 'c5']);
      expect(table.visibleColumns().map(c => c.id)).toEqual(['c1', 'c3']);
      expect(table.columns.map(c => c.width)).toEqual([101, 102, 103, 104, 105]);
    });

    it('ignores compact mode', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1'
      });
      table.columnById('c3').setVisible(false);

      const getColumnId = (column: Column) => {
        if (ObjectIdProvider.get().isUiSeqId(column.id)) {
          return null;
        }
        return column.id;
      };

      expect(table.columns.length).toBe(6);
      expect(table.columns.map(c => getColumnId(c))).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6']);
      expect(table.columns.map(c => c.guiOnly)).toEqual([undefined, undefined, undefined, undefined, undefined, undefined]);
      expect(table.columns.map(c => c.visible)).toEqual([false, true, false, true, true, true]);
      expect(table.columns.map(c => c.visibleIgnoreCompacted)).toEqual([false, true, false, true, true, true]);
      expect(table.columns.map(c => c.compacted)).toEqual([false, false, false, false, false, false]);
      let profile1 = tableUiPreferences.createProfile(table);
      expect(profile1.columns.length).toBe(5);
      expect(profile1.columns.map(c => c.columnId)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);
      expect(profile1.columns.map(c => c.visible)).toEqual([true, false, true, true, true]);
      expect(profile1.columns.map(c => c.width)).toEqual([102, 103, 104, 105, 106]);

      table.setCompact(true);
      expect(table.columns.length).toBe(7);
      expect(table.columns.map(c => getColumnId(c))).toEqual(['c1', 'c2', 'c3', 'c4', 'c5', 'c6', null]);
      expect(table.columns.map(c => c.guiOnly)).toEqual([undefined, undefined, undefined, undefined, undefined, undefined, true]);
      expect(table.columns.map(c => c.visible)).toEqual([false, false, false, false, false, false, true]);
      expect(table.columns.map(c => c.visibleIgnoreCompacted)).toEqual([false, true, false, true, true, true, true]);
      expect(table.columns.map(c => c.compacted)).toEqual([false, true, true, true, true, true, false]);
      let profile2 = tableUiPreferences.createProfile(table);
      expect(profile2.columns.length).toBe(5); // <--
      expect(profile2.columns.map(c => c.columnId)).toEqual(['c2', 'c3', 'c4', 'c5', 'c6']);
      expect(profile2.columns.map(c => c.visible)).toEqual([true, false, true, true, true]);
      expect(profile2.columns.map(c => c.width)).toEqual([102, 103, 104, 105, 106]);
    });

    it('can ignore table events', () => {
      let table = scout.create(SpecTable, {
        parent: session.desktop,
        id: 't1',
        uiPreferencesEnabled: true
      });
      let prefStore = SpecUiPreferencesStore.get();
      let c2 = table.columnById('c2');

      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(0);

      c2.setWidth(202);

      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(1);
      expect(prefStore.preferences?.tablePreferences[0]?.tablePreferenceProfiles?.get(TableUiPreferences.PROFILE_ID_GLOBAL)?.columns[0]?.width).toBe(202);

      tableUiPreferences.withIgnoreTableEvents(() => {
        c2.setWidth(303);
      });

      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(1);
      expect(prefStore.preferences?.tablePreferences[0]?.tablePreferenceProfiles?.get(TableUiPreferences.PROFILE_ID_GLOBAL)?.columns[0]?.width).toBe(202);

      c2.setWidth(404);

      jasmine.clock().tick(1000);
      expect(prefStore.loadCount).toBe(0);
      expect(prefStore.storeCount).toBe(2);
      expect(prefStore.preferences?.tablePreferences[0]?.tablePreferenceProfiles?.get(TableUiPreferences.PROFILE_ID_GLOBAL)?.columns[0]?.width).toBe(404);
    });
  });

  // -----------------------------------------------------------------------------------------

  class SpecTable extends Table {

    protected override _jsonModel(): WidgetModel {
      return {
        objectType: Table,
        columns: [{
          id: 'c1',
          objectType: Column,
          primaryKey: true,
          displayable: false
        }, {
          id: 'c2',
          objectType: Column,
          width: 102
        }, {
          id: 'c3',
          objectType: Column,
          width: 103
        }, {
          id: 'c4',
          objectType: NumberColumn,
          width: 104
        }, {
          id: 'c5',
          objectType: DateColumn,
          width: 105,
          sortIndex: 0,
          sortAscending: false
        }, {
          id: 'c6',
          objectType: BooleanColumn,
          width: 106
        }],
        tileProducer: (row: Table) => scout.create(SpecTile, {parent: this, row})
      };
    }
  }

  class SpecTile extends Tile {
    row: TableRow;
  }
});
