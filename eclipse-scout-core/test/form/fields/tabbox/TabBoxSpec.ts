/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, scout, TabBox, TabItem} from '../../../../src/index';
import {JQueryTesting, TabBoxSpecHelper} from '../../../../src/testing/index';

describe('TabBox', () => {
  let session: SandboxSession;
  let helper: TabBoxSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TabBoxSpecHelper(session);
  });

  describe('render', () => {
    let tabBox: TabBox;

    beforeEach(() => {
      let tabItem = helper.createTabItem();
      tabBox = helper.createTabBoxWith([tabItem]);
    });

    it('does NOT call layout for the selected tab on initialization', () => {
      spyOn(session.layoutValidator, 'invalidateTree').and.callThrough();
      tabBox.render();
      expect(session.layoutValidator.invalidateTree).not.toHaveBeenCalled();
    });

    it('must not create LogicalGridData for tab items', () => {
      tabBox.render();
      expect(tabBox.tabItems[0].htmlComp.layoutData).toBe(null);
    });

  });

  describe('remove', () => {

    it('does not fail if there was no selected tab', () => {
      let tabBox = scout.create(TabBox, {parent: session.desktop});
      tabBox.render();
      tabBox.remove();
      expect().nothing();
    });

  });

  describe('selection', () => {

    it('should select tabs by ID', () => {
      let tabItemA = helper.createTabItem({
        id: 'Foo'
      });
      let tabItemB = helper.createTabItem({
        id: 'Bar'
      });
      let tabBox = helper.createTabBoxWith([tabItemA, tabItemB]);
      tabBox.setSelectedTab('Foo');
      expect(tabBox.selectedTab).toBe(tabItemA);
      tabBox.setSelectedTab('Bar');
      expect(tabBox.selectedTab).toBe(tabItemB);
    });

  });

  describe('key handling', () => {

    it('supports left/right keys to select a tab-item', () => {
      let tabItemA = helper.createTabItem({
        label: 'tab 01'
      });
      let tabItemB = helper.createTabItem({
        label: 'tab 02'
      });
      let tabBox = helper.createTabBoxWith([tabItemA, tabItemB]);
      tabBox.render();

      tabItemA.focus();
      // check right/left keys
      expect(tabBox.selectedTab).toBe(tabItemA);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.RIGHT);
      expect(tabBox.selectedTab).toBe(tabItemB);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.LEFT);
      expect(tabBox.selectedTab).toBe(tabItemA);

      // make sure that nothing happens when first or last tab is selected and left/right is pressed
      tabBox.setSelectedTab(tabItemA);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.LEFT);
      expect(tabBox.selectedTab).toBe(tabItemA); // still A

      tabBox.setSelectedTab(tabItemB);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.RIGHT);
      expect(tabBox.selectedTab).toBe(tabItemB); // still B
    });

  });

  describe('first class', () => {
    let tabBox;

    beforeEach(() => {
      tabBox = scout.create(TabBox, {
        parent: session.desktop,
        tabItems: [{
          objectType: TabItem,
          label: 'first'
        }, {
          objectType: TabItem,
          label: 'second'
        }]
      });
      // set the tab-item to inline-block to ensure correct width calculation (e.g. PhantomJS)
      $('<style>' +
        '.tab-item { display: inline-block;}' +
        '</style>').appendTo($('#sandbox'));
    });

    it('is added to the first tab item', () => {
      tabBox.render();
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container).toHaveClass('first');
      expect(tabBox.header.tabArea.tabs[1].$container).not.toHaveClass('first');
    });

    it('is added to the first visible tab item', () => {
      tabBox = scout.create(TabBox, {
        parent: session.desktop,
        tabItems: [{
          objectType: TabItem,
          label: 'first',
          visible: false
        }, {
          objectType: TabItem,
          label: 'second'
        }, {
          objectType: TabItem,
          label: 'third'
        }]
      });
      tabBox.render();
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container.isVisible()).toBe(false);
      expect(tabBox.header.tabArea.tabs[1].$container).toHaveClass('first');
      expect(tabBox.header.tabArea.tabs[2].$container).not.toHaveClass('first');
    });

    it('is correctly updated when visibility changes', () => {
      tabBox.render();
      tabBox.validateLayout();
      tabBox.tabItems[0].setVisible(false);
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container.isVisible()).toBe(false);
      expect(tabBox.header.tabArea.tabs[1].$container).toHaveClass('first');
    });

  });

  describe('aria properties', () => {
    let tabBox: TabBox, tabItem1: TabItem, tabItem2: TabItem;

    beforeEach(() => {
      tabItem1 = helper.createTabItem();
      tabItem2 = helper.createTabItem();
      tabBox = helper.createTabBoxWith([tabItem1, tabItem2]);
    });

    it('has aria role tablist', () => {
      tabBox.render();
      expect(tabBox.$container).toHaveAttr('role', 'tablist');
    });

    it('has a content area with aria role tabpanel', () => {
      tabBox.render();
      expect(tabBox._$tabContent).toHaveAttr('role', 'tabpanel');
    });

    it('has tabs with aria role tab', () => {
      tabBox.render();
      tabBox.header.tabArea.tabs.forEach(tab => {
        expect(tab.$container).toHaveAttr('role', 'tab');
      });
      expect(tabBox._$tabContent).toHaveAttr('role', 'tabpanel');
    });

    it('has selected tabs aria-selected property set to true', () => {
      tabBox.render();
      // per default first tab is selected
      expect(tabItem1.getTab().$container).toHaveAttr('aria-selected', 'true');
      expect(tabItem2.getTab().$container.attr('aria-selected')).toBeFalsy();
      // test switch back and forth
      tabBox.setSelectedTab(tabItem2);
      expect(tabItem1.getTab().$container.attr('aria-selected')).toBeFalsy();
      expect(tabItem2.getTab().$container).toHaveAttr('aria-selected', 'true');
      tabBox.setSelectedTab(tabItem1);
      expect(tabItem1.getTab().$container).toHaveAttr('aria-selected', 'true');
      expect(tabItem2.getTab().$container.attr('aria-selected')).toBeFalsy();
    });
  });
});
