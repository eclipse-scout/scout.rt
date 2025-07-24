/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {SpecTable, TableSpecHelper} from '../../src/testing/index';
import {LookupCall, scout, Tile} from '../../src';

describe('TileTableHeaderBox', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;
  let table: SpecTable;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);

    const model = helper.createModelFixture(3, 5);
    table = helper.createTable(model);

    table.setTileProducer(row => scout.create(Tile, {parent: table}));
    table.setTileMode(true);

    table.render();
  });

  afterEach(() => {
    session = null;
  });

  describe('groupBy and sortBy fields', () => {

    it('are updated when column structure changes', async () => {
      const [column0, column1, column2] = table.columns;
      const {groupByField, sortByField} = table.tileTableHeader;

      const getLookupKeys = async <T>(lookupCall: LookupCall<T>) => {
        const lookupResult = await lookupCall.cloneForAll().execute();
        return lookupResult.lookupRows.map(row => row.key);
      };

      expect(await getLookupKeys(groupByField.lookupCall)).toEqual([null, column0, column1, column2]);
      expect(await getLookupKeys(sortByField.lookupCall)).toEqual([
        {column: column0, asc: true},
        {column: column0, asc: false},
        {column: column1, asc: true},
        {column: column1, asc: false},
        {column: column2, asc: true},
        {column: column2, asc: false}
      ]);

      table.insertColumn(helper.createModelColumn('foo'));
      expect(table.columns.length).toBe(4);
      const fooColumn = table.columns[3];

      expect(await getLookupKeys(groupByField.lookupCall)).toEqual([null, column0, column1, column2, fooColumn]);
      expect(await getLookupKeys(sortByField.lookupCall)).toEqual([
        {column: column0, asc: true},
        {column: column0, asc: false},
        {column: column1, asc: true},
        {column: column1, asc: false},
        {column: column2, asc: true},
        {column: column2, asc: false},
        {column: fooColumn, asc: true},
        {column: fooColumn, asc: false}
      ]);

      groupByField.setValue(fooColumn);
      sortByField.setValue({column: fooColumn, asc: true});
      table.deleteColumn(column2);

      expect(await getLookupKeys(groupByField.lookupCall)).toEqual([null, column0, column1, fooColumn]);
      expect(await getLookupKeys(sortByField.lookupCall)).toEqual([
        {column: column0, asc: true},
        {column: column0, asc: false},
        {column: column1, asc: true},
        {column: column1, asc: false},
        {column: fooColumn, asc: true},
        {column: fooColumn, asc: false}
      ]);
      expect(groupByField.value).toBe(fooColumn);
      expect(sortByField.value).toEqual({column: fooColumn, asc: true});

      table.deleteColumn(fooColumn);

      expect(await getLookupKeys(groupByField.lookupCall)).toEqual([null, column0, column1]);
      expect(await getLookupKeys(sortByField.lookupCall)).toEqual([
        {column: column0, asc: true},
        {column: column0, asc: false},
        {column: column1, asc: true},
        {column: column1, asc: false}
      ]);
      expect(groupByField.value).toBeNull();
      expect(sortByField.value).toBeNull();
    });
  });
});
