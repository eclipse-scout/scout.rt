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
/* global TableSpecHelper */
describe("MobileTable", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("render", function() {

    //FIXME cgu: temporarily disabled until mobile works again
//    it("does not create an addional div for scrolling", function() {
//      var model = helper.createModelFixture(2);
//      var table = helper.createMobileTable(model);
//      table.render(session.$entryPoint);
//      expect(table.$data).toBe(table.$data);
//    });

    it("does not display context menus", function() {
      var model = helper.createModelFixture(2,2);
      var table = helper.createMobileTable(model);
      table.render(session.$entryPoint);

      model.menus = [helper.createMenuModel('1','menu')];
      var $row0 = table.$data.children().eq(0);
      $row0.triggerContextMenu();

      var $menu = helper.getDisplayingContextMenu(table);
      expect($menu.length).toBeFalsy();
    });
  });


});
