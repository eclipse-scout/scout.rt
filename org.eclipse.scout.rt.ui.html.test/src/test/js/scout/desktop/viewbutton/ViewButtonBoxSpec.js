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
describe('ViewButtonBox', function() {
  var session, desktop, outlineHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('viewMenuTab', function() {
    var viewButtonBox;

    beforeEach(function() {
      viewButtonBox = scout.create('ViewButtonBox', {
        parent: new scout.NullWidget(),
        session: session,
        viewButtons: [scout.create('ViewButton', {
          parent: new scout.NullWidget(),
          session: session,
          displayStyle: 'MENU',
          visible: true
        })]
      });
    });

    it('is only visible if there are visible view buttons with displayStyle == "MENU"', function() {
      viewButtonBox.render(session.$entryPoint);
      expect(viewButtonBox.viewMenuTab.visible).toBe(true);
    });

    it('is not visible if there are no visible view buttons ith displayStyle == "MENU"', function() {
      viewButtonBox.viewButtons[0].visible = false;
      viewButtonBox.render(session.$entryPoint);
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

    it('is not visible if there are visible view buttons with displayStyle == "TAB"', function() {
      viewButtonBox.viewButtons[0].displayStyle = 'TAB';
      viewButtonBox.render(session.$entryPoint);
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

    it('is not visible if there are no view buttons at all', function() {
      viewButtonBox.viewButtons = [];
      viewButtonBox.render(session.$entryPoint);
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

  });

});
