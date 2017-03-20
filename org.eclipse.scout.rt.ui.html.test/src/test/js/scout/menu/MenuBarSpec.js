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
describe("MenuBar", function() {
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.MenuSpecHelper(session);
  });

  function createMenuBar() {
    return scout.create('MenuBar', {
      parent: new scout.NullWidget(),
      session: session,
      menuOrder: new scout.MenuItemsOrder(session, 'Table')
    });
  }

  describe('setMenuItems', function() {

    it('prefers EmptySpace for the left position if menu has multiple menuTypes', function() {
      var menu1 = helper.createMenu(helper.createModel('multi')),
        menu2 = helper.createMenu(helper.createModel('selection')),
        menuBar = createMenuBar(),
        menus = [menu2, menu1];

      menu1.menuTypes = ['Table.EmptySpace', 'Table.SingleSelection'];
      menu2.menuTypes = ['Table.SingleSelection'];

      menuBar.render(session.$entryPoint);
      menuBar.setMenuItems(menus);

      expect(menuBar.menuItems.length).toBe(3); // 2 + separator
      expect(menuBar.menuItems[0]).toBe(menu1);
    });

    it('must add/destroy dynamically created separators', function() {
      var separator,
        menu1 = helper.createMenu(helper.createModel('empty')),
        menu2 = helper.createMenu(helper.createModel('selection-1')),
        menu3 = helper.createMenu(helper.createModel('selection-2')),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.menuTypes = ['Table.EmptySpace'];
      menu2.menuTypes = ['Table.SingleSelection'];

      menuBar.render(session.$entryPoint);
      menuBar.setMenuItems(menus);

      // a separator must be added between EmptySpace and Selection Menus
      expect(menuBar.menuItems.length).toBe(3);
      separator = menuBar.menuItems[1];
      expect(separator.separator).toBe(true);
      expect(separator.createdBy).toBe(menuBar.menuSorter);

      // when menu-bar is updated, the old separator must be destroyed
      // and a new separator with different ID should be created
      menus = [menu1, menu3];
      menuBar.setMenuItems(menus);
      expect(separator.destroyed).toBe(true);
      expect(separator.id).not.toBe(menuBar.menuItems[1].id);
    });

    it('renders menu bar invisible if no visible menu items are available', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.visible = false;
      menu2.visible = false;

      menuBar.render(session.$entryPoint);
      menuBar.setMenuItems(menus);

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);
      expect(menuBar.$container.isVisible()).toBe(false);
    });

    it('renders menu bar visible if at least one visible menu item is available', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.visible = false;
      menu2.visible = true;

      menuBar.render(session.$entryPoint);
      menuBar.setMenuItems(menus);

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);
      expect(menuBar.$container.isVisible()).toBe(true);
    });

  });

  describe('layout', function() {
    beforeEach(function() {
      $(document.head)
        .appendElement('<style type="text/css">', 'jasmine-test-support')
        .text('.menu-item { display: inline-block; }');
    });

    afterEach(function() {
      $('.jasmine-test-support').remove();
    });

    it('gets invalidated if a menu changes its visibility', function() {
      var menu1 = helper.createMenu(helper.createModel('foo')),
        menu2 = helper.createMenu(helper.createModel('bar')),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.visible = true;
      menu2.visible = true;
      menuBar.setMenuItems(menus);
      menuBar.render(session.$entryPoint);
      menuBar.htmlComp.setSize(new scout.Dimension(500, 50));

      expect(menu1.$container.isVisible()).toBe(true);
      expect(scout.HtmlComponent.get(menuBar.$container).valid).toBe(true);

      var event = createPropertyChangeEvent(menu1, {
        "visible": false
      });
      menu1.onModelPropertyChange(event);

      expect(menu1.$container.isVisible()).toBe(false);
      expect(scout.HtmlComponent.get(menuBar.$container).valid).toBe(false);
    });
  });

  describe('updateDefaultMenu', function() {
    it('marks first visible and enabled menu that reacts to ENTER keystroke as default menu', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menuBar.setMenuItems(menus);
      menuBar.render(session.$entryPoint);

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);

      expect(menu1.$container).not.toHaveClass('default-menu');
      expect(menu2.$container).toHaveClass('default-menu');
    });

    it('updates state if menu gets enabled or disabled', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      var ellipsisMenu = scout.menus.createEllipsisMenu({
        parent: new scout.NullWidget(),
        session: session
      });
      ellipsisMenu.render(session.$entryPoint);

      menuBar.setMenuItems(menus);
      menuBar.render(session.$entryPoint);
      expect(menu1.rendered).toBe(true);
      expect(menu1.$container).not.toHaveClass('default-menu');
      expect(menu2.rendered).toBe(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.$container).toHaveClass('default-menu');

      var event = createPropertyChangeEvent(menu2, {
        "enabled": false
      });
      menu2.onModelPropertyChange(event);
      expect(menuBar.defaultMenu).toBe(null);
      expect(menu2.$container).not.toHaveClass('default-menu');

      event = createPropertyChangeEvent(menu2, {
        "enabled": true
      });
      menu2.onModelPropertyChange(event);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.$container).toHaveClass('default-menu');
    });

    it('considers rendered state of default menu', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      var ellipsisMenu = scout.menus.createEllipsisMenu({
        parent: new scout.NullWidget(),
        session: session
      });
      ellipsisMenu.render(session.$entryPoint);

      menuBar.setMenuItems(menus);
      menuBar.render(session.$entryPoint);
      expect(menu1.rendered).toBe(true);
      expect(menu1.$container).not.toHaveClass('default-menu');
      expect(menu2.rendered).toBe(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.$container).toHaveClass('default-menu');

      // Move default menu into ellipsis and call updateDefaultMenu explicitly to recalculate state
      scout.menus.moveMenuIntoEllipsis(menu2, ellipsisMenu);
      menuBar.updateDefaultMenu();
      expect(menu1.rendered).toBe(true);
      expect(menu1.$container).not.toHaveClass('default-menu');
      expect(menu2.rendered).toBe(false);
      expect(menuBar.defaultMenu).toBe(menu2);

      var event = createPropertyChangeEvent(menu2, {
        "enabled": false
      });
      menu2.onModelPropertyChange(event);
      expect(menuBar.defaultMenu).toBe(null);
      expect(menu2.rendered).toBe(false);

      event = createPropertyChangeEvent(menu2, {
        "enabled": true
      });
      menu2.onModelPropertyChange(event);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.rendered).toBe(false);
    });
  });

});
