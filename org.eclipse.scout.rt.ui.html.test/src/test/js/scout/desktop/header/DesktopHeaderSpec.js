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
describe("DesktopHeader", function() {
  var helper, session, desktop, formHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        benchVisible: true,
        headerVisible: true
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

  describe("onBenchOutlineContentChange", function() {
    var outline, bench, model, node0, node1;

    beforeEach(function() {
      model = helper.createModelFixture(3, 2, true);
      outline = helper.createOutline(model);
      node0 = outline.nodes[0];
      node0.detailForm = formHelper.createFormWithOneField();
      node0.detailFormVisible = true;
      node0.detailForm.rootGroupBox.menuBar.setMenuItems(scout.create('Menu', {
        parent: node0.detailForm
      }));
      node1 = outline.nodes[1];
      node1.detailForm = formHelper.createFormWithOneField();
      node1.detailFormVisible = true;
      node1.detailForm.rootGroupBox.menuBar.setMenuItems(scout.create('Menu', {
        parent: node1.detailForm
      }));
      bench = desktop.bench;
      desktop.setOutline(outline);
    });

    it("attaches listener to new outline content", function() {
      var detailForm0MenuBar = node0.detailForm.rootGroupBox.menuBar;
      var detailForm1MenuBar = node1.detailForm.rootGroupBox.menuBar;
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(0);
      expect(detailForm1MenuBar.events._eventListeners.length).toBe(0);
      outline.selectNodes(node0);
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(1);
      expect(detailForm1MenuBar.events._eventListeners.length).toBe(0);
    });

    it("removes listener from old outline content", function() {
      var detailForm0MenuBar = node0.detailForm.rootGroupBox.menuBar;
      var detailForm1MenuBar = node1.detailForm.rootGroupBox.menuBar;
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(0);
      expect(detailForm1MenuBar.events._eventListeners.length).toBe(0);
      outline.selectNodes(node0);
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(1);
      expect(detailForm1MenuBar.events._eventListeners.length).toBe(0);

      outline.selectNodes(node1);
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(0);
      expect(detailForm1MenuBar.events._eventListeners.length).toBe(1);

      outline.selectNodes(node0);
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(1);
      expect(detailForm1MenuBar.events._eventListeners.length).toBe(0);
    });

    it("removes listener when getting removed", function() {
      var detailForm0MenuBar = node0.detailForm.rootGroupBox.menuBar;
      outline.selectNodes(node0);
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(1);

      desktop.setHeaderVisible(false);
      expect(detailForm0MenuBar.events._eventListeners.length).toBe(0);
    });

  });

});
