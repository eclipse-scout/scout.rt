/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TabBoxSpecHelper} from '../../../../src/testing/index';

describe('TabBoxAdapter', () => {
  let session: SandboxSession;
  let helper: TabBoxSpecHelper;

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

      it('is correctly set even if tabItems property is sent as well', () => {
        let tabBox = helper.createTabBox();
        linkWidgetAndAdapter(tabBox, 'TabBoxAdapter');

        let message = {
          adapterData: mapAdapterData([{objectType: 'TabItem', id: 'ti1'}, {objectType: 'TabItem', id: 'ti2'}]),
          events: [createPropertyChangeEvent(tabBox.modelAdapter, {
            selectedTab: 'ti2',
            tabItems: ['ti1', 'ti2'] // server may send it after selectedTab which is actually the wrong order
          })]
        };
        session._processSuccessResponse(message);
        expect(tabBox.tabItems.length).toBe(2);
        expect(tabBox.selectedTab).toBe(tabBox.tabItems[1]);
        expect(tabBox.header.tabArea.selectedTab).toBe(tabBox.header.tabArea.getTabForItem(tabBox.selectedTab));
      });
    });
  });

});
