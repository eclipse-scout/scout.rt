/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('TabItem', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TabBoxSpecHelper(session);
  });

  describe('_renderStatusVisible', function() {
    var tabBox;

    beforeEach(function() {
      var tabItem = helper.createTabItem({
        label: 'Foo'
      });
      tabBox = helper.createTabBoxWith([tabItem]);
    });

    it('invalidates tabarea if status visibility changes', function() {
      tabBox.render();
      tabBox.validateLayout();
      expect(scout.HtmlComponent.get(tabBox.header.tabArea.$container).valid).toBe(true);
      expect(tabBox.header.tabArea.tabs[0]._computeVisible()).toBe(false);

      // TabArea needs to be invalidated, it may necessary to show ellipsis now because status got visible
      tabBox.tabItems[0].setTooltipText('test');
      expect(tabBox.header.tabArea.tabs[0]._computeVisible()).toBe(true);
      expect(scout.HtmlComponent.get(tabBox.header.tabArea.$container).valid).toBe(false);
    });

  });

  describe('_renderCssClass', function() {
    var tabItem, tabBox, tab;

    beforeEach(function() {
      tabItem = helper.createTabItem({
        cssClass: 'foo1'
      });
      tabBox = helper.createTabBoxWith([tabItem]);
      tabBox.render();
      tab = tabBox.header.tabArea.tabs[0];
    });

    it('adds CSS class to both, TabItem and GroupBox', function() {
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
});
