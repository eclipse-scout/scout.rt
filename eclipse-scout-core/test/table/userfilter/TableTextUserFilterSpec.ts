/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BeanColumn, NullWidget, ObjectFactory, scout, Table, TableTextUserFilter} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';

describe('TableTextUserFilter', () => {
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

  function createFilter(table: Table): TableTextUserFilter {
    return scout.create(TableTextUserFilter, {
      session: session,
      table: table
    });
  }

  class ASpecBeanColumn extends BeanColumn<{ a: string }> {
    override _renderValue($cell: JQuery, value: { a: string }) {
      $cell.appendDiv().text(value.a);
    }
  }

  describe('filter', () => {

    beforeEach(() => {
      ObjectFactory.get().register('ASpecBeanColumn', () => {
        return new ASpecBeanColumn();
      });
    });

    afterEach(() => {
      ObjectFactory.get().unregister('ASpecBeanColumn');
    });

    it('filters rows based on cell text', () => {
      let model = helper.createModelFixture(2, 0),
        table = helper.createTable(model),
        filter = createFilter(table);

      let rows = [{
        cells: ['cell00', 'cell01']
      }, {
        cells: ['cell10', 'cell11']
      }];
      table.insertRows(rows);

      expect(table.filteredRows().length).toBe(2);

      filter.text = 'cell11';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      // Capitalization is not relevant
      filter.text = 'CeLL';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(2);
      expect(table.filteredRows()[0]).toBe(table.rows[0]);
      expect(table.filteredRows()[1]).toBe(table.rows[1]);

      filter.text = 'asdf';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(0);

      table.removeFilter(filter);

      expect(table.filteredRows().length).toBe(2);
    });

    it('separates cell values with whitepace', () => {
      let model = helper.createModelFixture(2, 0),
        table = helper.createTable(model),
        filter = createFilter(table);

      let rows = [{
        cells: ['cell00', 'cell01']
      }, {
        cells: ['cell10', 'cell11']
      }];
      table.insertRows(rows);

      expect(table.filteredRows().length).toBe(2);

      filter.text = 'cell10cell';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(0);

      filter.text = 'cell10 cell';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);
    });

    it('works with bean columns', () => {
      let table = scout.create(Table, {
        parent: new NullWidget(),
        session: session,
        columns: [
          scout.create(ASpecBeanColumn, {
            session: session,
            width: 100
          })
        ]
      });
      let filter = createFilter(table);
      let bean0 = {
        a: 'bean0 text'
      };
      let bean1 = {
        a: 'bean1 text'
      };
      let rows = [{
        cells: [bean0]
      }, {
        cells: [bean1]
      }];
      table.insertRows(rows);

      expect(table.filteredRows().length).toBe(2);

      filter.text = 'bean1';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      filter.text = 'asdf';
      table.addFilter(filter);

      expect(table.filteredRows().length).toBe(0);

      table.removeFilter(filter);

      expect(table.filteredRows().length).toBe(2);
    });

  });
});
