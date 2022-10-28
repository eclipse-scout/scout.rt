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
import {HtmlComponent, TabBox} from '../../../../src/index';
import {TabBoxSpecHelper} from '../../../../src/testing/index';

describe('TabItem', () => {
  let session: SandboxSession;
  let helper: TabBoxSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TabBoxSpecHelper(session);
  });

  describe('_renderStatusVisible', () => {
    let tabBox: TabBox;

    beforeEach(() => {
      let tabItem = helper.createTabItem({
        label: 'Foo'
      });
      tabBox = helper.createTabBoxWith([tabItem]);
    });

    it('invalidates tabarea if status visibility changes', () => {
      tabBox.render();
      tabBox.validateLayout();
      expect(HtmlComponent.get(tabBox.header.tabArea.$container).valid).toBe(true);
      let firstTab = tabBox.header.tabArea.tabs[0];
      expect(firstTab['_computeVisible']()).toBe(false);

      // TabArea needs to be invalidated, it may necessary to show ellipsis now because status got visible
      tabBox.tabItems[0].setTooltipText('test');
      expect(firstTab['_computeVisible']()).toBe(true);
      expect(HtmlComponent.get(tabBox.header.tabArea.$container).valid).toBe(false);
    });

  });

  describe('_renderCssClass', () => {
    let tabItem, tabBox, tab;

    beforeEach(() => {
      tabItem = helper.createTabItem({
        cssClass: 'foo1'
      });
      tabBox = helper.createTabBoxWith([tabItem]);
      tabBox.render();
      tab = tabBox.header.tabArea.tabs[0];
    });

    it('adds CSS class to both, TabItem and GroupBox', () => {
      // Test initial CSS class
      expect(tab.$container.hasClass('foo1')).toBe(true);
      expect(tabItem.$container.hasClass('foo1')).toBe(true);

      // Test adding a CSS class
      tabItem.setProperty('cssClass', 'foo2');
      expect(tab.$container.hasClass('foo1')).toBe(false);
      expect(tabItem.$container.hasClass('foo1')).toBe(false);
      expect(tab.$container.hasClass('foo2')).toBe(true);
      expect(tabItem.$container.hasClass('foo2')).toBe(true);

      // Test adding another CSS class
      tabItem.setProperty('cssClass', 'foo3');
      expect(tab.$container.hasClass('foo2')).toBe(false);
      expect(tabItem.$container.hasClass('foo2')).toBe(false);
      expect(tab.$container.hasClass('foo3')).toBe(true);
      expect(tabItem.$container.hasClass('foo3')).toBe(true);
    });
  });

  describe('tooltip text', () => {
    let tabItem, tabBox, tab;

    beforeEach(() => {
      tabItem = helper.createTabItem({
        tooltipText: 'foo1'
      });
      tabBox = helper.createTabBoxWith([tabItem]);
      tabBox.render();
      tab = tabBox.header.tabArea.tabs[0];
    });

    it('is shown initially', () => {
      expect(tab.$container.hasClass('has-tooltip')).toBe(true);
      expect(tab.tooltipText).toBe('foo1');
    });

    it('is updated', () => {
      tabItem.setProperty('tooltipText', 'foo2');
      expect(tab.$container.hasClass('has-tooltip')).toBe(true);
      expect(tab.tooltipText).toBe('foo2');
    });

    it('is removed', () => {
      tabItem.setProperty('tooltipText', null);
      expect(tab.$container.hasClass('has-tooltip')).toBe(false);
      expect(tab.tooltipText).toBe(null);
    });
  });

  describe('select', () => {
    it('TabItem.select sets selectedTab on parent TabBox', () => {
      let tabItem1 = helper.createTabItem({
          label: 'Foo 1'
        }),
        tabItem2 = helper.createTabItem({
          label: 'Foo 2'
        }),
        tabItem3 = helper.createTabItem({
          label: 'Foo 3'
        }),
        tabBox = helper.createTabBoxWith([tabItem1, tabItem2, tabItem3]);
      tabBox.render();
      expect(tabBox.selectedTab).toBe(tabItem1);
      tabItem1.select();
      expect(tabBox.selectedTab).toBe(tabItem1);
      tabItem3.select();
      expect(tabBox.selectedTab).toBe(tabItem3);
      tabItem2.select();
      expect(tabBox.selectedTab).toBe(tabItem2);
      tabItem1.select();
      expect(tabBox.selectedTab).toBe(tabItem1);
    });
  });
});
