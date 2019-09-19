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
import {arrays, Outline, PageWithTable, scout} from '../../../../src/index';
import {OutlineSpecHelper} from '@eclipse-scout/testing';


describe('PageWithTable', function() {

  var session, helper;

  /** @type {Outline} */
  var outline;
  /** @type {PageWithTable} */
  var page;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    outline = helper.createOutline();

    page = scout.create('PageWithTable', {
      parent: outline,
      detailTable: {
        objectType: 'Table'
      }
    });
    outline.insertNodes([page], null);
    outline.render();
    page.detailTable.render();

    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  it('updates the page on table reload', function() {
    var counter = 0;
    page._loadTableData = function(searchFilter) {
      counter++;
      return $.resolvedDeferred();
    };
    page.detailTable.reload(); // this should trigger the _loadTableData of the page

    expect(page.detailTable.hasReloadHandler).toBe(true);
    expect(counter).toBe(1);
  });

  it('should handle errors in _onLoadTableDataDone', function() {
    page._loadTableData = function(searchFilter) {
      return $.resolvedDeferred([{
        rowId: 1,
        parentRow: 666, // does not exist -> causes an error in Table.js#insertRows
        cells: []
      }]);
    };
    expect(page.detailTable.tableStatus).toBe(undefined);
    page.detailTable.reload();
    jasmine.clock().tick(3);

    // expect error to be set as tableStatus
    var keys = Object.keys(page.detailTable.tableStatus);
    expect(arrays.containsAll(keys, ['message', 'code', 'severity'])).toBe(true);
  });

});
