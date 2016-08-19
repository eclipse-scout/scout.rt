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
describe('TabBox', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TabBoxSpecHelper(session);
  });

  describe('render', function() {
    var tabBox;

    beforeEach(function() {
      var tabItem = helper.createTabItem();
      tabBox = helper.createTabBox([tabItem]);
    });

    it('does NOT call layout for the selected tab on initialization', function() {
      spyOn(session.layoutValidator, 'invalidateTree').and.callThrough();
      tabBox.render(session.$entryPoint);
      expect(session.layoutValidator.invalidateTree).not.toHaveBeenCalled();
    });

    it('must not create LogicalGridData for tab items', function() {
      tabBox.render(session.$entryPoint);
      // See TabItem.js for the reason for this spec
      expect(tabBox.tabItems[0].htmlComp.layoutData).toBe(null);
    });

  });

});
