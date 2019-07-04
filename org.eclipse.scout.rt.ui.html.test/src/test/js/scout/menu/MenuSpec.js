/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("Menu", function() {

  var helper, session, $sandbox, menu1, menu2;

  beforeEach(function() {
    jasmine.clock().install();
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new scout.MenuSpecHelper(session);
    menu1 = helper.createMenu({
      text: 'foo'
    });
    menu2 = helper.createMenu({
      text: 'bar',
      keyStroke: 'enter'
    });
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  describe('defaults', function() {

    it('should have expected defaults', function() {
      expect(menu1.overflown).toBe(false);
    });

  });

  describe('render', function() {

    it('render depending on the actionStyle', function() {
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      menu1.remove();

      menu1.actionStyle = scout.Action.ActionStyle.BUTTON;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-button')).toBe(true);
      menu1.remove();

      // when button is used in overflow-menu, style should be back to menu-item
      menu1.overflow = true;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      menu1.remove();

      menu1.overflow = false;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
    });

    it('render as separator', function() {
      menu1.separator = true;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-separator')).toBe(true);
    });

    it('must not render childActions when popup is not open', function() {
      // See ticket #173734
      // render menu1 (sub-menu not opened)
      menu1.childActions = [menu2];
      menu1.render($sandbox);
      expect(menu1.$container).toBeTruthy();
      expect(menu2.$container).not.toBeTruthy();

      // render menu2 (as clone) when sub-menu is opened
      // cannot access menu2 because context menu works with a clone, that's why we query the DOM here
      menu1.remove();
      menu1.setSelected(true);
      menu1.render($sandbox);
      expect(menu1.$container).toBeTruthy();
      var $subMenu = $sandbox.find('.menu-item > span:contains(\'bar\')');
      expect($subMenu.length).toBe(1);

      // now the actual test case testing the bug from the ticket:
      // a property-change on 'childActions' occurs for menu1, when popup is not opened it must not render its submenus
      var modelMenu3 = helper.createModel('baz');
      modelMenu3.id = '123';
      menu1.remove();
      menu1.setSelected(false);
      menu1.render($sandbox);
      menu1.setProperty('childActions', [modelMenu3]);
      expect(menu1.$container).toBeTruthy();
      $subMenu = $sandbox.find('.menu-item > span:contains(\'baz\')');
      expect($subMenu.length).toBe(0);
    });

  });

  describe('isTabTarget', function() {

    it('should return true when menu can be a target of TAB action', function() {
      menu1.setEnabled(true);
      menu1.visible = true;
      menu1.actionStyle = scout.Action.ActionStyle.BUTTON;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.actionStyle = scout.Action.ActionStyle.DEFAULT;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.separator = true;
      expect(menu1.isTabTarget()).toBe(false);

      menu1.separator = false;
      menu1.setEnabled(false);
      expect(menu1.isTabTarget()).toBe(false);
    });

  });

  describe('setTooltipText', function() {

    it('can update the tooltip text', function() {
      var testMenu = helper.createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo'
      });
      testMenu.render();

      var tooltip = $('body').find('.tooltip');
      expect(tooltip.length).toBe(0);

      testMenu.$container.triggerMouseEnter();
      jasmine.clock().tick(1000);

      tooltip = $('body').find('.tooltip');
      expect(tooltip.length).toBe(1);
      expect(tooltip.text()).toBe('moo');

      testMenu.setTooltipText('quack');

      tooltip = $('body').find('.tooltip');
      expect(tooltip.length).toBe(1);
      expect(tooltip.text()).toBe('quack');

      testMenu.$container.triggerMouseLeave();
      testMenu.$container.triggerMouseEnter();
      jasmine.clock().tick(1000);

      tooltip = $('body').find('.tooltip');
      expect(tooltip.length).toBe(1);
      expect(tooltip.text()).toBe('quack');

      // Close
      testMenu.setTooltipText('meeeep');
      scout.tooltips.close(testMenu.$container);

      tooltip = $('body').find('.tooltip');
      expect(tooltip.length).toBe(0);
    });

  });

  describe('status', function() {
    it('is enabled if inheritAccessibility=true and parent is enabled', function() {
      var menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true
      });
      var parent = menu.parent;
      parent.setEnabled(true);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);

      expect(parent.enabled).toBe(true);
      expect(parent.enabledComputed).toBe(true);
    });

    it('is enabled if a child menu with inheritAccessibility=false is programmatically added', function() {
      var menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true
      });
      var parent = menu.parent;

      var childMenu = scout.create('Menu', {
        text: 'child',
        inheritAccessibility: false,
        session: parent.session,
        parent: menu,
        enabled: true
      });

      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(false);
      expect(childMenu.inheritAccessibility).toBe(false);
      expect(childMenu.enabled).toBe(true);
      expect(childMenu.enabledComputed).toBe(true);
      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);

      // add the child menu
      menu.addChildActions(childMenu);
      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);
      expect(childMenu.inheritAccessibility).toBe(false);
      expect(childMenu.enabled).toBe(true);
      expect(childMenu.enabledComputed).toBe(true);
      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is enabled if inheritAccessibility=false and parent is disabled', function() {
      var menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: false,
        enabled: true
      });
      var parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(false);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is disabled if inheritAccessibility=true and parent is disabled', function() {
      var menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true
      });
      var parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(false);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is enabled if inheritAccessibility=true and parent is disabled but has child actions with inheritAccessibility=false', function() {
      var menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true,
        childActions: [{
          objectType: 'Menu',
          text: 'child0',
          inheritAccessibility: true,
          enabled: true
        }, {
          objectType: 'Menu',
          text: 'child1',
          inheritAccessibility: true,
          enabled: true,
          childActions: [{
            objectType: 'Menu',
            text: 'child1_0',
            inheritAccessibility: false,
            enabled: true
          }, {
            objectType: 'Menu',
            text: 'child1_1',
            inheritAccessibility: true,
            enabled: true
          }, {
            objectType: 'Menu',
            text: 'child1_2',
            inheritAccessibility: true,
            enabled: false
          }]
        }]
      });
      var parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.childActions.length).toBe(2);
      expect(menu.enabledComputed).toBe(true);

      expect(menu.childActions[0].childActions.length).toBe(0);
      expect(menu.childActions[0].inheritAccessibility).toBe(true);
      expect(menu.childActions[0].enabledComputed).toBe(false);

      expect(menu.childActions[1].childActions.length).toBe(3);
      expect(menu.childActions[1].inheritAccessibility).toBe(true);
      expect(menu.childActions[1].enabledComputed).toBe(true);

      expect(menu.childActions[1].childActions[0].inheritAccessibility).toBe(false);
      expect(menu.childActions[1].childActions[0].enabled).toBe(true);
      expect(menu.childActions[1].childActions[0].enabledComputed).toBe(true);

      expect(menu.childActions[1].childActions[1].inheritAccessibility).toBe(true);
      expect(menu.childActions[1].childActions[1].enabled).toBe(true);
      expect(menu.childActions[1].childActions[1].enabledComputed).toBe(false);

      expect(menu.childActions[1].childActions[2].inheritAccessibility).toBe(true);
      expect(menu.childActions[1].childActions[2].enabled).toBe(false);
      expect(menu.childActions[1].childActions[2].enabledComputed).toBe(false);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is updated if a child menus inherit-status changes', function() {
      var menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true,
        childActions: [{
          objectType: 'Menu',
          text: 'child0',
          inheritAccessibility: true,
          enabled: true
        }, {
          objectType: 'Menu',
          text: 'child1',
          inheritAccessibility: true,
          enabled: true,
          childActions: [{
            objectType: 'Menu',
            text: 'child1_0',
            inheritAccessibility: false,
            enabled: true
          }]
        }]
      });

      var parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.childActions.length).toBe(2);
      expect(menu.enabledComputed).toBe(true);

      expect(menu.childActions[0].childActions.length).toBe(0);
      expect(menu.childActions[0].inheritAccessibility).toBe(true);
      expect(menu.childActions[0].enabledComputed).toBe(false);

      expect(menu.childActions[1].childActions.length).toBe(1);
      expect(menu.childActions[1].inheritAccessibility).toBe(true);
      expect(menu.childActions[1].enabledComputed).toBe(true);

      expect(menu.childActions[1].childActions[0].inheritAccessibility).toBe(false);
      expect(menu.childActions[1].childActions[0].enabled).toBe(true);
      expect(menu.childActions[1].childActions[0].enabledComputed).toBe(true);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);

      // toggle the inherit state of a child menu
      menu.childActions[1].childActions[0].setInheritAccessibility(true);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(false);

      expect(menu.childActions[0].inheritAccessibility).toBe(true);
      expect(menu.childActions[0].enabledComputed).toBe(false);

      expect(menu.childActions[1].childActions.length).toBe(1);
      expect(menu.childActions[1].inheritAccessibility).toBe(true);
      expect(menu.childActions[1].enabledComputed).toBe(false);

      expect(menu.childActions[1].childActions[0].inheritAccessibility).toBe(true);
      expect(menu.childActions[1].childActions[0].enabled).toBe(true);
      expect(menu.childActions[1].childActions[0].enabledComputed).toBe(false);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });
  });

  describe('clone', function() {

    it('and change child actions', function() {
      var clone,
        menu = helper.createMenu({
          text: 'menu',
          keyStroke: 'F2'
        });
      menu.childActions = [menu1];
      menu.render($sandbox);

      clone = menu.clone({
        parent: menu.parent
      }, {
        delegateEventsToOriginal: [],
        delegateAllPropertiesToClone: true,
        delegateAllPropertiesToOriginal: true,
        excludePropertiesToOriginal: []
      });

      menu.setChildActions([menu1, menu2]);
      expect(clone.childActions.length).toBe(2);

      clone.setChildActions([menu2]);
      expect(menu.childActions.length).toBe(1);
      expect(menu.childActions[0]).toBe(menu2);
    });

  });

});
