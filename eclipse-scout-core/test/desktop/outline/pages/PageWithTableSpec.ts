/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
// eslint-disable-next-line max-classes-per-file
import {arrays, Page, PageWithTable, scout, StaticLookupCall} from '../../../../src/index';
import {OutlineSpecHelper} from '../../../../src/testing/index';

describe('PageWithTable', () => {

  let session, helper;

  /** @type {Outline} */
  let outline;
  /** @type {PageWithTable} */
  let page;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    outline = helper.createOutline();

    page = scout.create(PageWithTable, {
      parent: outline,
      detailTable: {
        objectType: 'Table'
      }
    });
    outline.insertNodes([page], null);
    outline.render();
    outline.selectNode(page);
    page.detailTable.render();

    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('updates the page on table reload', () => {
    let counter = 0;
    page._loadTableData = searchFilter => {
      counter++;
      return $.resolvedDeferred();
    };
    page.detailTable.reload(); // this should trigger the _loadTableData of the page

    expect(page.detailTable.hasReloadHandler).toBe(true);
    expect(counter).toBe(1);
  });

  it('should handle errors in _onLoadTableDataDone', () => {
    page._loadTableData = searchFilter => $.resolvedDeferred([{
      rowId: 1,
      parentRow: 666, // does not exist -> causes an error in Table.js#insertRows
      cells: []
    }]);
    expect(page.detailTable.tableStatus).toBe(undefined);
    page.detailTable.reload();
    jasmine.clock().tick(3);

    // expect error to be set as tableStatus
    let keys = Object.keys(page.detailTable.tableStatus);
    expect(arrays.containsAll(keys, ['message', 'code', 'severity'])).toBe(true);
  });

  it('does not fail when cells with null values are inserted into a smart column ', done => {
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
        return scout.create(Page, {
          parent: this.getOutline()
        });
      }

      _loadTableData(searchFilter) {
        let data = [{
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
          .map(row => {
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
    let lookupCall = new DummyLookupCall();
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
    page.detailTable.when('propertyChange:loading').then(event => {
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
