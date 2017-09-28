/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global removePopups */
describe("TableUpdateBuffer", function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });


  it("buffers updateRow calls and processes them when all promises resolve", function(done) {
    var table = helper.createTable(helper.createModelFixture(2, 2));
    table.render();

    var deferred = $.Deferred();
    var promise = deferred.promise();
    table.updateBuffer.pushPromise(promise);

    var row = {
      id: table.rows[0].id,
      cells: ['newCellText0', 'newCellText1']
    };
    table.updateRow(row);
    expect(table.updateBuffer.isBuffering()).toBe(true);
    expect(table.rows[0].cells[0].text).toBe('0_0');
    expect(table.loading).toBe(true);

    promise.done(function() {
      expect(table.updateBuffer.isBuffering()).toBe(false);
      expect(table.rows[0].cells[0].text).toBe('newCellText0');
      expect(table.loading).toBe(false);
      done();
    });
    deferred.resolve();
  });

  it("prevents rendering viewport while buffering", function(done) {
    var table = helper.createTable(helper.createModelFixture(2, 0));
    table.render();

    var deferred = $.Deferred();
    var promise = deferred.promise();
    table.updateBuffer.pushPromise(promise);

    var rows = [{
      cells: ['a', 'b']
    }];
    table.insertRows(rows);
    expect(table.$rows().length).toBe(0);

    var row = {
      id: table.rows[0].id,
      cells: ['newCellText0', 'newCellText1']
    };
    table.updateRow(row);
    expect(table.$rows().length).toBe(0);

    promise.done(function() {
      expect(table.$rows().length).toBe(1);
      var $cells0 = table.$cellsForRow(table.$rows().eq(0));
      expect($cells0.eq(0).text()).toBe('newCellText0');
      done();
    });
    deferred.resolve();
  });

  it("processes immediately when a resolved promise is added", function() {
    var table = helper.createTable(helper.createModelFixture(2, 2));
    table.render();

    var deferred = $.Deferred();
    var promise = deferred.promise();
    deferred.resolve();
    table.updateBuffer.pushPromise(promise);

    var row = {
      id: table.rows[0].id,
      cells: ['newCellText0', 'newCellText1']
    };
    table.updateRow(row);
    expect(table.updateBuffer.isBuffering()).toBe(false);
    expect(table.rows[0].cells[0].text).toBe('newCellText0');
    expect(table.loading).toBe(false);
    expect(table._renderViewportBlocked).toBe(false);
  });

});
