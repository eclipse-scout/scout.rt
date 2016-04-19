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
describe("MenuBar", function() {
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.MenuSpecHelper(session);
  });

  describe('setMenuItems', function() {

    it('prefers EmptySpace for the left position if menu has multiple menuTypes', function() {
      var menu1 = helper.createMenu(helper.createModel('multi')),
        menu2 = helper.createMenu(helper.createModel('selection')),
        menuBar = scout.create('MenuBar', {
          parent: new scout.NullWidget(),
          session: session,
          menuOrder: new scout.MenuItemsOrder(session, 'Table')
        }),
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
        menuBar = scout.create('MenuBar', {
          parent: new scout.NullWidget(),
          session: session,
          menuOrder: new scout.MenuItemsOrder(session, 'Table')
        }),
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

    it('renders defaultMenu', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = scout.create('MenuBar', {
          parent: new scout.NullWidget(),
          session: session,
          menuOrder: new scout.MenuItemsOrder(session, 'Table')
        }),
        menus = [menu1, menu2];

      menuBar.render(session.$entryPoint);
      menuBar.setMenuItems(menus);

      // <fix for layout issues>
      // Menu item DIVs are too wide, because in Jasmine tests there are no CSS rules. Therefore, all
      // items are overflown and removed by the menubar layout. To fix this for this test, we manually
      // override the layout and force the drawing of the menu items without looking at the width.
      menuBar.rebuildItemsInternal();
      // </fix>

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);

      expect(menu1.$container.hasClass('default-menu')).toBe(false);
      expect(menu2.$container.hasClass('default-menu')).toBe(true);
    });

    it('renders menu bar invisible if no visible menu items are available', function() {
      var modelMenu1 = helper.createModel('foo');
      var modelMenu2 = helper.createModel('bar');
      modelMenu2.keyStroke = 'enter';

      var menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = scout.create('MenuBar', {
          parent: new scout.NullWidget(),
          session: session,
          menuOrder: new scout.MenuItemsOrder(session, 'Table')
        }),
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
        menuBar = scout.create('MenuBar', {
          parent: new scout.NullWidget(),
          session: session,
          menuOrder: new scout.MenuItemsOrder(session, 'Table')
        }),
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
    it('gets invalidated if a menu changes its visibility', function() {
      var menu1 = helper.createMenu(helper.createModel('foo')),
        menu2 = helper.createMenu(helper.createModel('bar')),
        menuBar = scout.create('MenuBar', {
          parent: new scout.NullWidget(),
          session: session,
          menuOrder: new scout.MenuItemsOrder(session, 'Table')
        }),
        menus = [menu1, menu2];

      menu1.visible = true;
      menu2.visible = true;
      menuBar.setMenuItems(menus);
      menuBar.render(session.$entryPoint);
      menuBar.$right.setVisible(false); // Right box should not influence the layout (due to missing css)
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

});
