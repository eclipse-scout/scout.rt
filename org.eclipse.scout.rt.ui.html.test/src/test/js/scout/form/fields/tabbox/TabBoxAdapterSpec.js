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
describe('TabBoxAdapter', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TabBoxSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('onModelPropertyChange', function() {

    describe('selectedTab', function() {
      it('selects the tab but does not send a selectTab event', function() {
        var tabBox = helper.createTabBoxWith2Tabs();
        linkWidgetAndAdapter(tabBox, 'TabBoxAdapter');
        linkWidgetAndAdapter(tabBox.tabItems[0], 'TabItemAdapter');
        linkWidgetAndAdapter(tabBox.tabItems[1], 'TabItemAdapter');
        tabBox.setSelectedTab(tabBox.tabItems[0]);
        expect(tabBox.selectedTab).toBe(tabBox.tabItems[0]);
        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(1);

        // clear requests
        jasmine.Ajax.uninstall();
        jasmine.Ajax.install();

        var event = createPropertyChangeEvent(tabBox, {
          selectedTab: tabBox.tabItems[1].id
        });
        tabBox.modelAdapter.onModelPropertyChange(event);
        expect(tabBox.selectedTab).toBe(tabBox.tabItems[1]);

        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(0);
      });
    });
  });

});
