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
describe("DesktopBench", function() {
  var helper, session, desktop, formHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        benchVisible: true
      }
    });
    desktop = session.desktop;
    helper = new scout.OutlineSpecHelper(session);
    formHelper = new scout.FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("updateOutlineContent", function() {
    var outline, bench, model, node;

    beforeEach(function() {
      model = helper.createModelFixture(3, 2, true);
      outline = helper.createOutline(model);
      node = model.nodes[0];
      node.detailForm = formHelper.createFormWithOneField();
      node.detailFormVisible = true;
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

    it("sets detailForm as outlineContent if node gets selected", function() {
      // node 0 has a detail form
      outline.selectNodes(outline.nodes[1]);
      expect(outline.selectedNodes[0].detailForm).toBeFalsy();
      expect(bench.outlineContent).toBeFalsy();

      outline.selectNodes(outline.nodes[0]);
      expect(outline.selectedNodes[0].detailForm).toBeTruthy();
      expect(bench.outlineContent).toBe(outline.selectedNodes[0].detailForm);

      outline.selectNodes(outline.nodes[1]);
      expect(outline.selectedNodes[0].detailForm).toBeFalsy();
      expect(bench.outlineContent).toBeFalsy();
    });
  });

});
