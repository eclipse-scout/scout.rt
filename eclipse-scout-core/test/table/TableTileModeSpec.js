/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableSpecHelper} from '@eclipse-scout/testing';
import {scout} from '../../src';

describe('TableTileModeSpec', function() {
  var session;
  var helper;
  var model;
  var table;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);

    // regular table
    model = helper.createModelFixture(2, 5);
    table = helper.createTable(model);
    table.createTileForRow = function(row) {
      var model = {
        parent: table,
        content: '<p>' + row.id + '</p>'
      };
      return scout.create('HtmlTile', model);
    };
    table.render();
  });

  afterEach(function() {
    session = null;
    helper = null;
    model = null;
    table = null;
  });

  describe('tile mode', function() {
    it('table', function() {
      expect(table.rendered).toBe(true);
    });

    it('switch', function() {
      table.setTileMode(true);
      expect(table.tileMode).toBe(true);
      expect(table.tableTileGridMediator.tiles.length).toBe(table.rows.length);
    });

    it('selection synchronizes', function() {
      table.setTileMode(true);
      table.selectRows([table.rows[1], table.rows[2], table.rows[3]]);
      expect(table.selectedRows.length).toBe(3);
      expect(table.tableTileGridMediator.tileAccordion.getSelectedTileCount()).toBe(3);
      table.tableTileGridMediator.tileAccordion.selectTile(table.tableTileGridMediator.tiles[1]);
      expect(table.tableTileGridMediator.tileAccordion.getSelectedTileCount()).toBe(1);
      expect(table.selectedRows.length).toBe(1);
      expect(table.tableTileGridMediator.tiles[1].rowId).toBe(table.selectedRows[0].id);
    });

    it('groups data', function() {
      table.columns[0].grouped = true;
      table.sort(table.columns[0], 'desc');
      table.setTileMode(true);
      expect(table.tableTileGridMediator.tileAccordion.groups.length).toBe(5);
    });
  });
});
