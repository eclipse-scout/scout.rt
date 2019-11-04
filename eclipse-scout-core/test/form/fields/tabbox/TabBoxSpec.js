/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, scout} from '../../../../src/index';
import {TabBoxSpecHelper} from '@eclipse-scout/testing';

describe('TabBox', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TabBoxSpecHelper(session);
  });

  describe('render', function() {
    var tabBox;

    beforeEach(function() {
      var tabItem = helper.createTabItem();
      tabBox = helper.createTabBoxWith([tabItem]);
    });

    it('does NOT call layout for the selected tab on initialization', function() {
      spyOn(session.layoutValidator, 'invalidateTree').and.callThrough();
      tabBox.render();
      expect(session.layoutValidator.invalidateTree).not.toHaveBeenCalled();
    });

    it('must not create LogicalGridData for tab items', function() {
      tabBox.render();
      expect(tabBox.tabItems[0].htmlComp.layoutData).toBe(null);
    });

  });

  describe('remove', function() {

    it('does not fail if there was no selected tab', function() {
      var tabBox = scout.create('TabBox', {parent: session.desktop});
      tabBox.render();
      tabBox.remove();
      expect().nothing();
    });

  });

  describe('selection', function() {

    it('should select tabs by ID', function() {
      var tabItemA = helper.createTabItem({
        id: 'Foo'
      });
      var tabItemB = helper.createTabItem({
        id: 'Bar'
      });
      var tabBox = helper.createTabBoxWith([tabItemA, tabItemB]);
      tabBox.selectTabById('Foo');
      expect(tabBox.selectedTab).toBe(tabItemA);
      tabBox.selectTabById('Bar');
      expect(tabBox.selectedTab).toBe(tabItemB);
    });

  });

  describe('key handling', function() {

    it('supports left/right keys to select a tab-item', function() {
      var tabItemA = helper.createTabItem({
        label: 'tab 01'
      });
      var tabItemB = helper.createTabItem({
        label: 'tab 02'
      });
      var tabBox = helper.createTabBoxWith([tabItemA, tabItemB]);
      tabBox.render();

      tabItemA.focus();
      // check right/left keys
      expect(tabBox.selectedTab).toBe(tabItemA);
      tabBox.header.tabArea.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tabBox.selectedTab).toBe(tabItemB);
      tabBox.header.tabArea.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tabBox.selectedTab).toBe(tabItemA);

      // make sure that nothing happens when first or last tab is selected and left/right is pressed
      tabBox.setSelectedTab(tabItemA);
      tabBox.header.tabArea.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tabBox.selectedTab).toBe(tabItemA); // still A

      tabBox.setSelectedTab(tabItemB);
      tabBox.header.tabArea.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tabBox.selectedTab).toBe(tabItemB); // still B
    });

  });

  describe('first class', function() {
    var tabBox;

    beforeEach(function() {
      tabBox = scout.create('TabBox', {
        parent: session.desktop,
        tabItems: [{
          objectType: 'TabItem',
          label: 'first'
        }, {
          objectType: 'TabItem',
          label: 'second'
        }]
      });
      // set the tab-item to inline-block to ensure correct width calculation (e.g. PhantomJS)
      $('<style>' +
        '.tab-item { display: inline-block;}' +
        '</style>').appendTo($('#sandbox'));
    });

    it('is added to the first tab item', function() {
      tabBox.render();
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container).toHaveClass('first');
      expect(tabBox.header.tabArea.tabs[1].$container).not.toHaveClass('first');
    });

    it('is added to the first visible tab item', function() {
      tabBox = scout.create('TabBox', {
        parent: session.desktop,
        tabItems: [{
          objectType: 'TabItem',
          label: 'first',
          visible: false
        }, {
          objectType: 'TabItem',
          label: 'second'
        }, {
          objectType: 'TabItem',
          label: 'third'
        }]
      });
      tabBox.render();
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container.isVisible()).toBe(false);
      expect(tabBox.header.tabArea.tabs[1].$container).toHaveClass('first');
      expect(tabBox.header.tabArea.tabs[2].$container).not.toHaveClass('first');
    });

    it('is correctly updated when visibility changes', function() {
      tabBox.render();
      tabBox.validateLayout();
      tabBox.tabItems[0].setVisible(false);
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container.isVisible()).toBe(false);
      expect(tabBox.header.tabArea.tabs[1].$container).toHaveClass('first');
    });

  });

});
