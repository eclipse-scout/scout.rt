/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ContextMenuPopup, EllipsisMenu, Menu, scout, tooltips} from '../../src/index';
import {JQueryTesting, MenuSpecHelper} from '../../src/testing/index';

describe('Menu', () => {
  let session: SandboxSession, $sandbox: JQuery, menu1: Menu, menu2: Menu;
  let helper: MenuSpecHelper;

  beforeEach(() => {
    jasmine.clock().install();
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new MenuSpecHelper(session);
    menu1 = helper.createMenu({
      text: 'foo'
    });
    menu2 = helper.createMenu({
      text: 'bar',
      keyStroke: 'enter'
    });
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  describe('defaults', () => {

    it('should have expected defaults', () => {
      expect(menu1.overflown).toBe(false);
    });

  });

  describe('render', () => {

    it('render depending on the actionStyle', () => {
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      menu1.remove();

      menu1.actionStyle = Action.ActionStyle.BUTTON;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-button')).toBe(true);
      menu1.remove();

      // when button is used in overflow-menu, style should be back to menu-item
      menu1._setOverflown(true);
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
      menu1.remove();

      menu1._setOverflown(false);
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-item')).toBe(true);
    });

    it('render as separator', () => {
      menu1.separator = true;
      menu1.render($sandbox);
      expect(menu1.$container.hasClass('menu-separator')).toBe(true);
    });

    it('must not render childActions when popup is not open', () => {
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
      let $subMenu = $sandbox.find('.menu-item > span:contains(\'bar\')');
      expect($subMenu.length).toBe(1);

      // now the actual test case testing the bug from the ticket:
      // a property-change on 'childActions' occurs for menu1, when popup is not opened it must not render its submenus
      let modelMenu3 = helper.createModel('baz');
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

  describe('isTabTarget', () => {

    it('should return true when menu can be a target of TAB action', () => {
      menu1.setEnabled(true);
      menu1.setVisible(true);
      menu1.actionStyle = Action.ActionStyle.BUTTON;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.actionStyle = Action.ActionStyle.DEFAULT;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.separator = true;
      expect(menu1.isTabTarget()).toBe(false);

      menu1.separator = false;
      menu1.setEnabled(false);
      expect(menu1.isTabTarget()).toBe(false);
    });

  });

  describe('setTooltipText', () => {

    function find$Tooltips() {
      return $('body').find('.tooltip');
    }

    it('can update the tooltip text', () => {
      let testMenu = helper.createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo'
      });
      testMenu.render();

      let $tooltip = find$Tooltips();
      expect($tooltip.length).toBe(0);

      JQueryTesting.triggerMouseEnter(testMenu.$container);
      jasmine.clock().tick(1000);

      $tooltip = find$Tooltips();
      expect($tooltip.length).toBe(1);
      expect($tooltip.text()).toBe('moo');

      testMenu.setTooltipText('quack');

      $tooltip = find$Tooltips();
      expect($tooltip.length).toBe(1);
      expect($tooltip.text()).toBe('quack');

      JQueryTesting.triggerMouseLeave(testMenu.$container);
      JQueryTesting.triggerMouseEnter(testMenu.$container);
      jasmine.clock().tick(1000);

      $tooltip = find$Tooltips();
      expect($tooltip.length).toBe(1);
      expect($tooltip.text()).toBe('quack');

      // Close
      testMenu.setTooltipText('meeeep');
      tooltips.close(testMenu.$container);

      $tooltip = find$Tooltips();
      expect($tooltip.length).toBe(0);
    });

  });

  describe('status', () => {
    it('is enabled if inheritAccessibility=true and parent is enabled', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true
      });
      let parent = menu.parent;
      parent.setEnabled(true);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);

      expect(parent.enabled).toBe(true);
      expect(parent.enabledComputed).toBe(true);
    });

    it('is enabled if a child menu with inheritAccessibility=false is programmatically added', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true
      });
      let parent = menu.parent;

      let childMenu = scout.create(Menu, {
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
      menu.insertChildActions(childMenu);
      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);
      expect(childMenu.inheritAccessibility).toBe(false);
      expect(childMenu.enabled).toBe(true);
      expect(childMenu.enabledComputed).toBe(true);
      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is enabled if inheritAccessibility=false and parent is disabled', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: false,
        enabled: true
      });
      let parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(false);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is disabled if inheritAccessibility=true and parent is disabled', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true
      });
      let parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(false);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);
    });

    it('is enabled if inheritAccessibility=true and parent is disabled but has child actions with inheritAccessibility=false', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true,
        childActions: [{
          objectType: Menu,
          text: 'child0',
          inheritAccessibility: true,
          enabled: true
        }, {
          objectType: Menu,
          text: 'child1',
          inheritAccessibility: true,
          enabled: true,
          childActions: [{
            objectType: Menu,
            text: 'child1_0',
            inheritAccessibility: false,
            enabled: true
          }, {
            objectType: Menu,
            text: 'child1_1',
            inheritAccessibility: true,
            enabled: true
          }, {
            objectType: Menu,
            text: 'child1_2',
            inheritAccessibility: true,
            enabled: false
          }]
        }]
      });
      let parent = menu.parent;
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

    it('is updated if a child menus inherit-status changes', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true,
        childActions: [{
          objectType: Menu,
          text: 'child0',
          inheritAccessibility: true,
          enabled: true
        }, {
          objectType: Menu,
          text: 'child1',
          inheritAccessibility: true,
          enabled: true,
          childActions: [{
            objectType: Menu,
            text: 'child1_0',
            inheritAccessibility: false,
            enabled: true
          }]
        }]
      });

      let parent = menu.parent;
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

      // toggle inherit state of a child menu
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

    it('depends on next parent with inheritAccessibility=false if this.inheritAccessibility=true', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true,
        childActions: [{
          objectType: Menu,
          text: 'child1',
          inheritAccessibility: false,
          enabled: true,
          childActions: [{
            objectType: Menu,
            text: 'child1_0',
            inheritAccessibility: true,
            enabled: true
          }]
        }]
      });

      let parent = menu.parent;
      parent.setEnabled(false);

      expect(menu.inheritAccessibility).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.childActions.length).toBe(1);
      expect(menu.enabledComputed).toBe(true);

      expect(menu.childActions[0].childActions.length).toBe(1);
      expect(menu.childActions[0].inheritAccessibility).toBe(false);
      expect(menu.childActions[0].enabledComputed).toBe(true);

      expect(menu.childActions[0].childActions[0].inheritAccessibility).toBe(true);
      expect(menu.childActions[0].childActions[0].enabled).toBe(true);
      expect(menu.childActions[0].childActions[0].enabledComputed).toBe(true);

      expect(parent.enabled).toBe(false);
      expect(parent.enabledComputed).toBe(false);

      // explicitly call recompute on leaf without passing a parent-state
      menu.childActions[0].childActions[0].recomputeEnabled();

      expect(menu.childActions[0].childActions[0].inheritAccessibility).toBe(true);
      expect(menu.childActions[0].childActions[0].enabled).toBe(true);
      expect(menu.childActions[0].childActions[0].enabledComputed).toBe(true); // must stay true!
    });

    it('is updated if a child menus enabled or visible status changes', () => {
      let menu = helper.createMenu({
        text: 'menu',
        inheritAccessibility: true,
        enabled: true,
        childActions: [{
          objectType: Menu,
          text: 'child0',
          inheritAccessibility: true,
          enabled: true
        }, {
          objectType: Menu,
          text: 'child1',
          inheritAccessibility: true,
          enabled: true,
          childActions: [{
            objectType: Menu,
            text: 'child1_0',
            inheritAccessibility: false,
            enabled: true
          }]
        }]
      });

      let parent = menu.parent;
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

      // toggle the enabled and visible state of the child menu
      menu.childActions[1].childActions[0].setEnabled(false);
      expect(menu.enabledComputed).toBe(false);
      menu.childActions[1].childActions[0].setEnabled(true);
      expect(menu.enabledComputed).toBe(true);
      menu.childActions[1].childActions[0].setVisible(false);
      expect(menu.enabledComputed).toBe(false);
      menu.childActions[1].childActions[0].setVisible(true);
      expect(menu.enabledComputed).toBe(true);
    });

    function testEnabledInEllipsis(inheritAccessibility) {
      let ellipsis = scout.create(EllipsisMenu, {
        parent: session.desktop
      });
      ellipsis.render();
      let menu = helper.createMenu();
      menu.setInheritAccessibility(inheritAccessibility);
      menu.render();
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);

      menu._setOverflown(true);
      ellipsis.setChildActions([menu]);
      expect(ellipsis.enabledComputed).toBe(true);
      expect(menu.enabled).toBe(true);
      expect(menu.enabledComputed).toBe(true);
      expect(menu.$container).not.toHaveClass('disabled');
      ellipsis.setSelected(true);
      expect((ellipsis.popup as ContextMenuPopup).$menuItems().eq(0)).not.toHaveClass('disabled'); // $container of the cloned menu item

      ellipsis.setSelected(false);
      menu.setEnabled(false);
      expect(ellipsis.enabledComputed).toBe(true);
      expect(menu.enabled).toBe(false);
      expect(menu.enabledComputed).toBe(false);
      expect(menu.$container).toHaveClass('disabled');
      ellipsis.setSelected(true);
      expect((ellipsis.popup as ContextMenuPopup).$menuItems().eq(0)).toHaveClass('disabled');

      ellipsis.setSelected(false);
      menu._setOverflown(false);
      ellipsis.setChildActions([]);
      expect(menu.enabled).toBe(false);
      expect(menu.enabledComputed).toBe(false);
      expect(menu.$container).toHaveClass('disabled');
    }

    it('is updated even if menu is in ellipsis and has inheritAccessibility=false', () => {
      testEnabledInEllipsis(false);
    });

    it('is updated even if menu is in ellipsis and has inheritAccessibility=true', () => {
      testEnabledInEllipsis(true);
    });
  });

  describe('clone', () => {

    it('and change child actions', () => {
      let clone,
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

  describe('submenuIcon', () => {
    it('is moved into text if text position is bottom', () => {
      let menu = scout.create(Menu, {
        parent: session.desktop,
        text: 'text',
        iconId: 'iconId',
        childActions: [{objectType: Menu}]
      });
      menu.render();
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$container[0]);
      menu.setTextPosition(Menu.TextPosition.BOTTOM);
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$text[0]);
      menu.setTextPosition(Menu.TextPosition.DEFAULT);
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$container[0]);
    });

    it('is moved into text if text position is bottom and text set later', () => {
      let menu = scout.create(Menu, {
        parent: session.desktop,
        iconId: 'iconId',
        childActions: [{objectType: Menu}]
      });
      menu.render();
      menu.setTextPosition(Menu.TextPosition.BOTTOM);
      expect(menu.$submenuIcon).toBe(null);
      menu.setText('text');
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$text[0]);
    });

    it('is moved into text if text position is bottom and icon set later', () => {
      let menu = scout.create(Menu, {
        parent: session.desktop,
        text: 'text',
        childActions: [{objectType: Menu}]
      });
      menu.render();
      menu.setTextPosition(Menu.TextPosition.BOTTOM);
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$container[0]);
      menu.setIconId('icon');
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$text[0]);
    });

    it('is moved into text if text position is bottom and subMenuIVisibility set later', () => {
      let menu = scout.create(Menu, {
        parent: session.desktop,
        iconId: 'iconId',
        text: 'asdf',
        subMenuVisibility: Menu.SubMenuVisibility.NEVER,
        childActions: [{objectType: Menu}]
      });
      menu.render();
      menu.setTextPosition(Menu.TextPosition.BOTTOM);
      expect(menu.$submenuIcon).toBe(null);
      menu.setSubMenuVisibility(Menu.SubMenuVisibility.ALWAYS);
      expect(menu.$submenuIcon.parent()[0]).toBe(menu.$text[0]);
    });
  });

  describe('aria properties', () => {

    it('has aria role menuitem', () => {
      let testMenu = helper.createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo'
      });
      testMenu.render();
      expect(testMenu.$container).toHaveAttr('role', 'menuitem');
    });

    it('has aria role separator if it is a separator', () => {
      let testSeparator = helper.createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo',
        separator: true
      });
      testSeparator.render();
      expect(testSeparator.$container).toHaveAttr('role', 'separator');
    });

    it('has aria role menuitemcheckbox if it is a toggle action', () => {
      let testMenu = helper.createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo',
        toggleAction: true
      });
      testMenu.render();
      expect(testMenu.$container).toHaveAttr('role', 'menuitemcheckbox');
    });

    it('has aria checked set correctly if it is a menuitemcheckbox', () => {
      let testMenu = helper.createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo',
        toggleAction: true
      });
      testMenu.render();
      expect(testMenu.$container).toHaveAttr('role', 'menuitemcheckbox');
      expect(testMenu.$container).toHaveAttr('aria-checked', 'false');
      // also check that aria pressed is not set (not supported for menu items role)
      expect(testMenu.$container.attr('aria-pressed')).toBeFalsy();
      testMenu.setSelected(true);
      expect(testMenu.$container).toHaveAttr('aria-checked', 'true');
      expect(testMenu.$container.attr('aria-pressed')).toBeFalsy();
    });

    it('has aria-haspopup set to menu if it has child actions', () => {
      let menuWithChildActions = scout.create(Menu, {
        parent: session.desktop,
        text: 'text',
        childActions: [{objectType: Menu}]
      });
      menuWithChildActions.render();
      expect(menuWithChildActions.$container).toHaveAttr('aria-haspopup', 'menu');
    });

    it('has aria-expanded set to true if it has child actions and is selected', () => {
      let menuWithChildActions = scout.create(Menu, {
        parent: session.desktop,
        text: 'text',
        childActions: [{objectType: Menu}]
      });
      menuWithChildActions.render();
      expect(menuWithChildActions.$container).toHaveAttr('aria-expanded', 'false');
      // also check that aria pressed is not set (not supported for menu items role)
      expect(menuWithChildActions.$container.attr('aria-pressed')).toBeFalsy();
      menuWithChildActions.setSelected(true);
      expect(menuWithChildActions.$container).toHaveAttr('aria-expanded', 'true');
      expect(menuWithChildActions.$container.attr('aria-pressed')).toBeFalsy();
    });

    it('behaves like a menu with child actions if it is an ellipsis menu', () => {
      let ellipsis = scout.create(EllipsisMenu, {
        parent: session.desktop
      });
      ellipsis.render();
      expect(ellipsis.$container).toHaveAttr('role', 'menuitem');
      expect(ellipsis.$container).toHaveAttr('aria-haspopup', 'menu');
      expect(ellipsis.$container).toHaveAttr('aria-expanded', 'false');
      ellipsis.setSelected(true);
      expect(ellipsis.$container).toHaveAttr('aria-expanded', 'true');
    });
  });
});
