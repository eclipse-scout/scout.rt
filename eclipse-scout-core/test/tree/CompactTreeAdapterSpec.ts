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
import {RemoteEvent, Tree} from '../../src/index';
import {TreeSpecHelper} from '../../src/testing/index';

describe('CompactTreeAdapter', () => {
  let session: SandboxSession;
  let helper: TreeSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TreeSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  describe('selectNodes', () => {

    it('selects child node and notifies server if server selects the first title node', () => {
      let model = helper.createModelFixture(2, 1, true);
      let adapter = helper.createCompactTreeAdapter(model);
      let tree = adapter.createWidget(model, session.desktop) as Tree;
      tree.render();

      let nodes = [tree.nodes[0]];
      // @ts-ignore
      adapter._onNodesSelected([nodes[0].id]);
      sendQueuedAjaxCalls();
      expect(tree.selectedNodes[0]).toBe(tree.nodes[0].childNodes[0]);
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let event = new RemoteEvent(tree.id, 'nodesSelected', {
        nodeIds: [tree.nodes[0].childNodes[0].id]
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });
  });
});
