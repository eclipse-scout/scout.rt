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
/* global OutlineSpecHelper */
describe("DesktopBenchSpec", function() {
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    helper = new OutlineSpecHelper(sandboxSession({
      desktop: {
        benchVisible: true
      }
    }));
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("updateOutlineContent", function() {
    var outline, bench, model, node, desktop;

    beforeEach(function() {
      model = helper.createModelFixture(3, 2, true);
      outline = helper.createOutline(model);
      node = model.nodes[0];
      desktop = helper.session.desktop;
      bench = desktop.bench;
      desktop.setOutline(outline);
    });

    it("called when an outline page gets selected", function() {
      spyOn(bench, 'updateOutlineContent');
      outline.selectNodes(outline.nodes[1]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(1);
    });

    it("doesn't get called if page already is selected", function() {
      spyOn(bench, 'updateOutlineContent');
      outline.selectNodes(outline.nodes[1]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(1);

      outline.selectNodes(outline.nodes[1]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(1);

      outline.selectNodes([]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(2);

      outline.selectNodes([]);
      expect(bench.updateOutlineContent.calls.count()).toEqual(2);
    });
  });

});
