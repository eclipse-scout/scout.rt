/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableSpecHelper} from '../../src/testing/index';
import {App, ChildModelOf, ErrorHandler, scout, SmartColumn, StaticLookupCall} from '../../src/index';

describe('TableUpdateBuffer', () => {
  let session: SandboxSession, helper: TableSpecHelper, origErrorHandler: ErrorHandler, errHandlerSpy: jasmine.Spy;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.clock().install();

    origErrorHandler = App.get().errorHandler;
    const newErrorHandler = scout.create(ErrorHandler);
    errHandlerSpy = spyOn(newErrorHandler, 'handle').and.callThrough();
    App.get().errorHandler = newErrorHandler;
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    App.get().errorHandler = origErrorHandler;
  });

  it('buffers updateRow calls and processes them when all promises resolve', done => {
    let table = helper.createTable(helper.createModelFixture(2, 2));
    table.render();

    let deferred = $.Deferred();
    let promise = deferred.promise();
    table.updateBuffer.pushPromise(promise);

    let row = {
      id: table.rows[0].id,
      cells: ['newCellText0', 'newCellText1']
    };
    table.updateRow(row);
    expect(table.updateBuffer.isBuffering()).toBe(true);
    expect(table.rows[0].cells[0].text).toBe('0_0');
    expect(table.loading).toBe(true);

    promise.then(() => {
      expect(table.updateBuffer.isBuffering()).toBe(false);
      expect(table.rows[0].cells[0].text).toBe('newCellText0');
      expect(table.loading).toBe(false);
      done();
    });
    deferred.resolve();
    jasmine.clock().tick(500);
  });

  it('ignores removed rows', done => {
    class TableUpdateBufferSpecLookupCall extends StaticLookupCall<number> {
      protected override _data(): any[] {
        return [
          [1, 'Foo'],
          [2, 'Bar'],
          [3, 'Baz']
        ];
      }
    }

    let columnModel = helper.createModelColumn('smart', SmartColumn<number>) as ChildModelOf<SmartColumn<number>>;
    columnModel.lookupCall = TableUpdateBufferSpecLookupCall;
    let table = helper.createTable(helper.createModel([columnModel], helper.createModelRows([columnModel], 0)));
    table.on('propertyChange:loading', e => {
      if (e.oldValue && !e.newValue) { // change to loading=false after the last promise is resolved
        expect(errHandlerSpy.calls.count()).toEqual(0);
        done();
      }
    });

    table.insertRows([{cells: [1]}, {cells: [2]}, {cells: [3]}]);
    table.render();
    table.deleteRow(table.rows[0]);
    jasmine.clock().tick(500); // here the LookupCall is invoked and therefore TableUpdateBuffer#process
  });

  it('prevents rendering viewport while buffering', done => {
    let table = helper.createTable(helper.createModelFixture(2, 0));
    table.render();

    let deferred = $.Deferred();
    let promise = deferred.promise();
    table.updateBuffer.pushPromise(promise);

    let rows = [{
      cells: ['a', 'b']
    }];
    table.insertRows(rows);
    expect(table.$rows().length).toBe(0);

    let row = {
      id: table.rows[0].id,
      cells: ['newCellText0', 'newCellText1']
    };
    table.updateRow(row);
    expect(table.$rows().length).toBe(0);

    promise.then(() => {
      expect(table.$rows().length).toBe(1);
      let $cells0 = table.$cellsForRow(table.$rows().eq(0));
      expect($cells0.eq(0).text()).toBe('newCellText0');
      done();
    });
    deferred.resolve();
    jasmine.clock().tick(500);
  });
});
