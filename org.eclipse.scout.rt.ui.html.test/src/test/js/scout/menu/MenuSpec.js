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
/* global scout.MenuSpecHelper */
describe("Menu", function() {

  var helper, session, $sandbox, modelMenu1, modelMenu2, menu1, menu2;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new scout.MenuSpecHelper(session);
    modelMenu1 = helper.createModel('foo');
    menu1 = helper.createMenu(modelMenu1);
    modelMenu2 = helper.createModel('foo');
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
