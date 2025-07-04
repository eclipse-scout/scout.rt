/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, scout, Table, TableClientUiPreferencesDo, uiPreferences, UiPreferences, UiPreferencesDo, UiPreferencesUpdateDo} from '../../src/index';
import {SpecUiPreferencesStore} from '../../src/testing';

describe('UiPreferences', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('bootstrap and load', () => {

    it('loads and subscribes during bootstrap', async () => {
      let table1 = scout.create(Table, {
        parent: session.desktop,
        id: 't1'
      });
      let table2 = scout.create(Table, {
        parent: session.desktop,
        id: 't2'
      });
      expect(table1.tileMode).toBe(false);
      expect(table2.tileMode).toBe(false);

      let store = SpecUiPreferencesStore.get();
      expect(store.loadCount).toBe(0);
      expect(store.subscribers.length).toBe(0);
      expect(uiPreferences.getTablePreferences(table1)).toBe(undefined);
      expect(uiPreferences.getTablePreferences(table2)).toBe(undefined);
      store.preferences = scout.create(UiPreferencesDo, {
        tablePreferences: [
          scout.create(TableClientUiPreferencesDo, {
            tableId: 't1',
            tileMode: false
          }),
          scout.create(TableClientUiPreferencesDo, {
            tableId: 't2',
            tileMode: false
          })
        ]
      });

      await uiPreferences.bootstrap();

      expect(store.loadCount).toBe(1);
      expect(store.subscribers.length).toBe(1);
      expect(uiPreferences.getTablePreferences(table1)).toBeInstanceOf(TableClientUiPreferencesDo);
      expect(uiPreferences.getTablePreferences(table1).tileMode).toBe(false);
      expect(table1.tileMode).toBe(false);
      expect(uiPreferences.getTablePreferences(table2)).toBeInstanceOf(TableClientUiPreferencesDo);
      expect(uiPreferences.getTablePreferences(table2).tileMode).toBe(false);
      expect(table2.tileMode).toBe(false);

      store.subscribers[0](scout.create(UiPreferencesUpdateDo, {
        preferences: scout.create(UiPreferencesDo, {
          tablePreferences: [
            scout.create(TableClientUiPreferencesDo, {
              tableId: 't1',
              tileMode: true
            })
          ]
        })
      }));

      expect(store.loadCount).toBe(1); // still 1
      expect(uiPreferences.getTablePreferences(table1)).toBeInstanceOf(TableClientUiPreferencesDo);
      expect(uiPreferences.getTablePreferences(table1).tileMode).toBe(true);
      expect(table1.tileMode).toBe(false); // not updated automatically
      expect(uiPreferences.getTablePreferences(table2)).toBe(undefined);
      expect(table2.tileMode).toBe(false);
    });
  });

  describe('store', () => {

    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('coalesces multiple changes', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        id: 't1',
        columns: [
          {
            objectType: Column,
            id: 'c1'
          }
        ]
      });

      let store = SpecUiPreferencesStore.get();
      expect(store.storeCount).toBe(0);

      uiPreferences.installTableListener(table);
      table.setTileMode(true);
      table.columns[0].setWidth(123);

      jasmine.clock().tick(1000);
      expect(store.storeCount).toBe(1);
      jasmine.clock().tick(1000);
      expect(store.storeCount).toBe(1);

      table.columns[0].setWidth(456);
      table.columns[0].setWidth(789);
      expect(store.storeCount).toBe(1);
      jasmine.clock().tick(1000);
      expect(store.storeCount).toBe(2);
    });
  });
});
