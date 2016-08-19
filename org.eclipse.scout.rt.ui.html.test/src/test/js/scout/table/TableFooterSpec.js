/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TableFooterSpec", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("render", function() {

    it("attaches listener to the table but only once", function() {
      var model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);
      expect(table.footer).not.toBeUndefined();
      var listenerCount = table.events._eventListeners.length;

      table.setTableStatusVisible(false);
      table.setTableStatusVisible(true);

      // Still same amount of listeners expected after footer visibility changed
      expect(table.events._eventListeners.length).toBe(listenerCount);
    });

  });

  describe("controls", function() {

    function createTableControl() {
      var action = new scout.TableControl();
      action.init(createSimpleModel('TableControl', session));
      return action;
    }

    it("removes old and renders new controls on property change", function() {
      var model = helper.createModelFixture(2);
      model.tableStatusVisible = true;
      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var controls = [createTableControl()];
      table.setTableControls(controls);
      expect(controls[0].rendered).toBe(true);

      var newControls = [createTableControl(), createTableControl()];
      table.setTableControls(newControls);
      expect(controls[0].rendered).toBe(false);
      expect(newControls[0].rendered).toBe(true);
      expect(newControls[0].rendered).toBe(true);
    });
  });

});
