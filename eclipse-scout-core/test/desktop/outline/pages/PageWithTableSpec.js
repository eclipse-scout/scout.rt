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
// eslint-disable-next-line max-classes-per-file
import {arrays, Outline, PageWithTable, scout, StaticLookupCall} from '../../../../src/index';
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

  it('does not fail when cells with null values are inserted into a smart column ', function(done) {
    class DummyLookupCall extends StaticLookupCall {
      constructor() {
        super();
      }

      _data() {
        return [
          ['key0', 'Key 0'],
          ['key1', 'Key 1']
        ];
      }
    }

    class SamplePageWithTable extends PageWithTable {
      createChildPage(row) {
        return scout.create('Page', {
          parent: this.getOutline()
        });
      }

      _loadTableData(searchFilter) {
        var data = [{
          string: 'string 1',
          smartValue: null
        }, {
          string: 'string 2',
          smartValue: null
        }, {
          string: 'string 3',
          smartValue: 'key0'
        }, {
          string: 'string 4',
          smartValue: 'key1'
        }, {
          string: 'string 5',
          smartValue: 'key0'
        }];
        return $.resolvedPromise(data);
      }

      _transformTableDataToTableRows(tableData) {
        return tableData
          .map(function(row) {
            return {
              data: row,
              cells: [
                row.string,
                row.smartValue
              ]
            };
          });
      }
    }

    jasmine.clock().uninstall();
    var lookupCall = new DummyLookupCall();
    lookupCall.init({session: session});
    page = new SamplePageWithTable();
    page.init({
      parent: outline,
      detailTable: {
        objectType: 'Table',
        columns: [
          {
            id: 'StringColumn',
            objectType: 'Column',
            sortActive: true,
            sortIndex: 0
          },
          {
            id: 'SmartColumn',
            objectType: 'SmartColumn',
            lookupCall: lookupCall
          }
        ]
      }
    });
    outline.insertNode(page);
    outline.selectNode(page);
    page.detailTable.when('propertyChange:loading').then(function(event) {
      // Loading is set to true when update buffer finishes
      expect(page.detailTable.rows[0].cells[0].text).toEqual('string 1');
      expect(page.detailTable.rows[0].cells[1].text).toEqual('');
      expect(page.detailTable.rows[2].cells[0].text).toEqual('string 3');
      expect(page.detailTable.rows[2].cells[1].text).toEqual('Key 0');
      done();
    })
      .catch(fail);
  });

});
