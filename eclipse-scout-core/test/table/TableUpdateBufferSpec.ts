/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableSpecHelper} from '../../src/testing/index';

describe('TableUpdateBuffer', () => {
  let session: SandboxSession, helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
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
  });
});
