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
/* global TreeSpecHelper */
describe("Compacttree", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TreeSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
    $.fx.off = true;
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  describe("creation", function() {

    it("adds no empty section node", function() {
      //top-level node (section) is only rendered, if there are child nodes
      var model = helper.createModelFixture(1);
      var tree = helper.createCompactTree(model);
      var spy = spyOn(tree, '_calculateCurrentViewRange').and.returnValue(new scout.Range(0, 1));
      tree.render(session.$entryPoint);
      expect(tree.$nodes().length).toBe(0);
    });

  });

});
