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
describe("Menu", function() {

  var helper, session, $sandbox, modelMenu1, modelMenu2, menu1, menu2;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new scout.MenuSpecHelper(session);
    modelMenu1 = helper.createModel('foo');
    menu1 = helper.createMenu(modelMenu1);
    modelMenu2 = helper.createModel('bar');
    modelMenu2.keyStroke = 'enter';
    menu2 = helper.createMenu(modelMenu2);
  });

  describe('defaults', function() {

    it('should have expected defaults', function() {
      expect(menu1.overflow).toBe(false);
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
      menu1.enabled = true;
      menu1.visible = true;
      menu1.actionStyle = scout.Action.ActionStyle.BUTTON;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.actionStyle = scout.Action.ActionStyle.DEFAULT;
      expect(menu1.isTabTarget()).toBe(true);

      menu1.separator = true;
      expect(menu1.isTabTarget()).toBe(false);

      menu1.separator = false;
      menu1.enabled = false;
      expect(menu1.isTabTarget()).toBe(false);
    });

  });

});
