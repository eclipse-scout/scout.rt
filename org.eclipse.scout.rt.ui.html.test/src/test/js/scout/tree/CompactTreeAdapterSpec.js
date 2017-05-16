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
describe("CompactTreeAdapter", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TreeSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  describe("selectNodes", function() {

    it("selects child node and notifies server if server selects the first title node", function() {
      var model = helper.createModelFixture(2, 1, true);
      var adapter = helper.createCompactTreeAdapter(model);
      var tree = adapter.createWidget(model, session.desktop);
      tree.render();

      var nodes = [tree.nodes[0]];
      adapter._onNodesSelected([nodes[0].id]);
      sendQueuedAjaxCalls();
      expect(tree.selectedNodes[0]).toBe(tree.nodes[0].childNodes[0]);
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var event = new scout.RemoteEvent(tree.id, 'nodesSelected', {
        nodeIds: [tree.nodes[0].childNodes[0].id]
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

});
