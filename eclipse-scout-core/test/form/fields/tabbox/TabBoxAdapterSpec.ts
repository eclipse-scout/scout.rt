/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TabBoxSpecHelper} from '../../../../src/testing/index';

describe('TabBoxAdapter', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TabBoxSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('onModelPropertyChange', () => {

    describe('selectedTab', () => {
      it('selects the tab but does not send a selectTab event', () => {
        let tabBox = helper.createTabBoxWith2Tabs();
        linkWidgetAndAdapter(tabBox, 'TabBoxAdapter');
        linkWidgetAndAdapter(tabBox.tabItems[0], 'TabItemAdapter');
        linkWidgetAndAdapter(tabBox.tabItems[1], 'TabItemAdapter');
        tabBox.setSelectedTab(tabBox.tabItems[1]);
        expect(tabBox.selectedTab).toBe(tabBox.tabItems[1]);
        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(1);

        // clear requests
        jasmine.Ajax.uninstall();
        jasmine.Ajax.install();

        let event = createPropertyChangeEvent(tabBox, {
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
