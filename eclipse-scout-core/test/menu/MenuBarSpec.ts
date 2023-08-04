/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, Button, ButtonAdapterMenu, Dimension, EllipsisMenu, GroupBoxMenuItemsOrder, HtmlComponent, Menu, MenuBar, MenuItemsOrder, MenuModel, scout, Table} from '../../src/index';
import {MenuSpecHelper} from '../../src/testing/index';
import {MenuOrder} from '../../src/menu/MenuItemsOrder';

describe('MenuBar', () => {
  let helper: MenuSpecHelper, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new MenuSpecHelper(session);
    $('<style>' +
      '.menubar { overflow: hidden; background-color: red; padding: 5px; }' +
      '.menubar > .menubox { display: inline-block; height: 100% }' +
      '.menubar > .menubox.right { float:right }' +
      '.menu-item { min-width: 110px; max-width: 110px; padding: 5px; display: inline-block; background-color: orange;}' +
      '.ellipsis { min-width: 40px; max-width: 40px; }' +
      '</style>').appendTo($('#sandbox'));
  });

  function createModel(text?: string, iconId?: string, menuTypes?: string[]): MenuModel {
    text = scout.nvl(text, 'Foo');
    return helper.createModel(text, iconId, menuTypes);
  }

  function createMenuBar(menuOrder?: MenuOrder): SpecMenuBar {
    return scout.create(SpecMenuBar, {
      parent: session.desktop,
      menuOrder: scout.nvl(menuOrder, new MenuItemsOrder(session, 'Table', [Table.MenuType.EmptySpace]))
    });
  }

  class SpecMenuBar extends MenuBar {
    declare _ellipsis: EllipsisMenu;
  }

  describe('setMenuItems', () => {

    it('prefers EmptySpace for the left position if menu has multiple menuTypes', () => {
      let menu1 = helper.createMenu(helper.createModel('multi')),
        menu2 = helper.createMenu(helper.createModel('selection')),
        menuBar = createMenuBar(),
        menus = [menu2, menu1];

      menu1.setMenuTypes([Table.MenuType.EmptySpace, Table.MenuType.SingleSelection]);
      menu2.setMenuTypes([Table.MenuType.SingleSelection]);

      menuBar.render();
      menuBar.setMenuItems(menus);

      expect(menuBar.orderedMenuItems.all.length).toBe(4); // 2 + separator + ellipsis
      expect(menuBar.orderedMenuItems.all[0]).toBe(menu1);
      expect(menuBar.orderedMenuItems.all.map(mi => {
        return mi.separator;
      })).toEqual([false, true, false, false]);
      expect(menuBar.orderedMenuItems.all.map(mi => {
        return mi.ellipsis;
      })).toEqual([undefined, undefined, undefined, true]);
    });

    it('must add/destroy dynamically created separators', () => {
      let separator,
        menu1 = helper.createMenu(createModel('empty')),
        menu2 = helper.createMenu(createModel('selection-1', null, [Table.MenuType.SingleSelection])),
        menu3 = helper.createMenu(createModel('selection-2', null, [Table.MenuType.SingleSelection])),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menuBar.render();
      menuBar.setMenuItems(menus);

      // a separator must be added between EmptySpace and Selection Menus and ellipsis at the end
      expect(menuBar.orderedMenuItems.all.length).toBe(4);
      separator = menuBar.orderedMenuItems.all[1];
      expect(separator.separator).toBe(true);
      expect(separator.createdBy).toBe(menuBar.menuSorter);

      // when menu-bar is updated, the old separator must be destroyed
      // and a new separator with different ID should be created
      menus = [menu1, menu3];
      menuBar.setMenuItems(menus);
      expect(separator.rendered).toBe(false);
      expect(separator.id).not.toBe(menuBar.orderedMenuItems.all[1].id);
    });

    it('renders menu bar invisible if no visible menu items are available', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      modelMenu2.keyStroke = 'enter';

      let menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.setVisible(false);
      menu2.setVisible(false);

      menuBar.render();
      menuBar.setMenuItems(menus);

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);
      expect(menuBar.$container.isVisible()).toBe(false);
    });

    it('renders menu bar visible if at least one visible menu item is available', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      modelMenu2.keyStroke = 'enter';

      let menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.setVisible(false);
      menu2.setVisible(true);

      menuBar.render();
      menuBar.setMenuItems(menus);

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);
      expect(menuBar.$container.isVisible()).toBe(true);
    });

    it('hides unnecessary explicit separator menus', () => {
      let sep1a = helper.createMenu({id: 'test.sep1a', separator: true});
      let sep1b = helper.createMenu({id: 'test.sep1b', separator: true});
      let menu1 = helper.createMenu({id: 'test.menu1', text: 'Menu 1 (L)'});
      let sep12a = helper.createMenu({id: 'test.sep12a', separator: true});
      let sep12b = helper.createMenu({id: 'test.sep12b', separator: true, menuTypes: [Table.MenuType.SingleSelection]}); // <-- will generate an additional artificial separator menu
      let menu2 = helper.createMenu({id: 'test.menu2', text: 'Menu 2 (L)', menuTypes: [Table.MenuType.SingleSelection]});
      let sep2a = helper.createMenu({id: 'test.sep2a', separator: true, menuTypes: [Table.MenuType.SingleSelection]});
      let sep2b = helper.createMenu({id: 'test.sep2b', separator: true, menuTypes: [Table.MenuType.SingleSelection]});

      let sep3a = helper.createMenu({id: 'test.sep3a', horizontalAlignment: 1, separator: true});
      let sep3b = helper.createMenu({id: 'test.sep3b', horizontalAlignment: 1, separator: true});
      let menu3 = helper.createMenu({id: 'test.menu3', horizontalAlignment: 1, text: 'Menu 3 (R)'});
      let sep34a = helper.createMenu({id: 'test.sep34a', horizontalAlignment: 1, separator: true});
      let sep34b = helper.createMenu({id: 'test.sep34b', horizontalAlignment: 1, separator: true});
      let menu4 = helper.createMenu({id: 'test.menu4', horizontalAlignment: 1, text: 'Menu 4 (R)'});
      let sep4a = helper.createMenu({id: 'test.sep4a', horizontalAlignment: 1, separator: true});
      let sep4b = helper.createMenu({id: 'test.sep4b', horizontalAlignment: 1, separator: true});

      let menuBar = createMenuBar();
      menuBar.render();
      menuBar.setMenuItems([
        sep1a, sep1b, menu1, sep12a, sep12b, menu2, sep2a, sep2b,
        sep3a, sep3b, menu3, sep34a, sep34b, menu4, sep4a, sep4b
      ]);

      expect(menuBar.orderedMenuItems.all.length).toBe(18); // all menus + one artificial separator menu + one artificial ellipsis menu
      expect(menuBar.orderedMenuItems.left.length).toBe(9);
      expect(menuBar.orderedMenuItems.right.length).toBe(9);

      function listVisibleMenuIds(menus) {
        return menus
          .filter(menu => {
            return menu.visible && !menu.ellipsis;
          })
          .map(menu => {
            return menu.id;
          })
          .join(', ');
      }

      expect(listVisibleMenuIds(menuBar.orderedMenuItems.all)).toBe('test.menu1, test.sep12b, test.menu2, test.menu3, test.sep34b, test.menu4');
      expect(listVisibleMenuIds(menuBar.orderedMenuItems.left)).toBe('test.menu1, test.sep12b, test.menu2');
      expect(listVisibleMenuIds(menuBar.orderedMenuItems.right)).toBe('test.menu3, test.sep34b, test.menu4');
    });

  });

  describe('tabbable', () => {
    it('is set to the first menu that is a tab target', () => {
      let menuBar = createMenuBar();
      let menu1 = helper.createMenu({text: 'Menu 1'});
      let menu2 = helper.createMenu({text: 'Menu 2'});
      menuBar.setMenuItems([menu1, menu2]);
      menuBar.render();

      expect(menu1.$container.attr('tabindex')).toBe('0');
      expect(menu2.$container.attr('tabindex')).toBe(undefined);
    });

    it('is updated when a menu item is focused', () => {
      // otherwise the menu item can not have the focus, because the DOM element is not focusable without a tabindex.
      let menu1 = helper.createMenu({text: 'Menu 1'});
      let menu2 = helper.createMenu({text: 'Menu 2'});

      let menuBar = createMenuBar();
      menuBar.render();
      menuBar.setMenuItems([menu1, menu2]);

      menu1.focus();
      expect(menu1.$container.attr('tabindex')).toBe('0');
      expect(menu2.$container.attr('tabindex')).toBe(undefined);

      menu2.focus();
      expect(menu1.$container.attr('tabindex')).toBe(undefined);
      expect(menu2.$container.attr('tabindex')).toBe('0');
    });
  });

  describe('propertyChange', () => {
    let menu, menuBar;

    beforeEach(() => {
      menu = helper.createMenu(createModel('foo'));
      menuBar = createMenuBar();
    });

    it('must listen on property changes of its menu items (even when menu bar is not rendered)', () => {
      menu.setVisible(false);
      menuBar.setMenuItems([menu]);
      expect(menuBar.visible).toBe(false);

      menu.setVisible(true);
      expect(menuBar.visible).toBe(true);

      menu.setVisible(false);
      expect(menuBar.visible).toBe(false);
    });

    // Note: the menu alone has already an event listener
    it('must remove property-change and focus listeners on destroy', () => {
      expect(menu.events.count()).toBe(1);

      menuBar.setMenuItems([menu]);
      expect(menu.events.count()).toBe(3);

      menuBar.destroy();
      expect(menu.events.count()).toBe(1);
    });

    it('automatically hides unused separators', () => {
      let menu1 = helper.createMenu(createModel('Menu 1'));
      let menu2 = helper.createMenu(createModel('Menu 2'));
      let menu3 = helper.createMenu(createModel('Menu 3'));
      let separator = helper.createMenu({separator: true});

      let menuBar = createMenuBar();
      menuBar.render();
      menuBar.setMenuItems([menu1, separator, menu2, menu3]);

      expect(menuBar.orderedMenuItems.all.length).toBeGreaterThanOrEqual(4); // because the elements have no real size, an artificial EllipsisMenu might be added
      expect(menuBar.orderedMenuItems.all[0]).toBe(menu1);
      expect(menuBar.orderedMenuItems.all[1]).toBe(separator);
      expect(menuBar.orderedMenuItems.all[2]).toBe(menu2);
      expect(menuBar.orderedMenuItems.all[3]).toBe(menu3);

      menu2.setVisible(false);
      expect(menu1.visible).toBe(true);
      expect(separator.visible).toBe(true);
      expect(menu2.visible).toBe(false); // <--
      expect(menu3.visible).toBe(true);

      menu3.setVisible(false);
      expect(menu1.visible).toBe(true);
      expect(separator.visible).toBe(false); // <--
      expect(menu2.visible).toBe(false);
      expect(menu3.visible).toBe(false); // <--

      menuBar.remove();
      menu2.setVisible(true);
      expect(menu1.visible).toBe(true);
      expect(separator.visible).toBe(true); // <--
      expect(menu2.visible).toBe(true); // <--
      expect(menu3.visible).toBe(false);

      menuBar.render();
      expect(menu1.$container.isVisible()).toBe(true);
      expect(separator.$container.isVisible()).toBe(true);
      expect(menu2.$container.isVisible()).toBe(true);
      expect(menu3.$container.isVisible()).toBe(false);
    });
  });

  describe('setVisible', () => {
    it('does not throw an error if called on menus moved to the ellipsis menu', () => {
      let menu1 = helper.createMenu(createModel('foo')),
        menu2 = helper.createMenu(createModel('bar')),
        menuBar = createMenuBar();

      let menu3model = createModel('boo');
      menu3model.visible = false;
      let menu3 = helper.createMenu(menu3model);

      let menu4model = createModel('far');
      menu4model.horizontalAlignment = 1;
      let menu4 = helper.createMenu(menu4model);

      let menus = [menu1, menu2, menu3, menu4];

      menuBar.setMenuItems(menus);
      menuBar.render();
      menuBar.htmlComp.setSize(new Dimension(100, 50));

      menu3.setVisible(true);
      menuBar.validateLayout();

      // no error is thrown.
      expect(true).toBe(true);
    });
  });

  describe('enabled', () => {
    it('updates enabledComputed properly if disabled menu is moved out of ellipsis', () => {
      let menu1 = helper.createMenu(createModel('move it'));
      let menu2 = helper.createMenu(createModel('my menu'));
      menu2.setEnabled(false);
      let menuBar = createMenuBar();
      menuBar.setMenuItems([menu1, menu2]);
      menuBar.render();
      // Menu is 110px, make menu bar larger so that first is displayed and second in ellipsis
      menuBar.htmlComp.setSize(new Dimension(160, 50));
      expect(menu1.$container.isVisible()).toBe(true);
      expect(menu2.enabledComputed).toBe(false);
      expect(menu2.$container).toHaveClass('overflown');

      menu1.setVisible(false);
      menu2.setEnabled(true);
      menuBar.validateLayout();

      expect(menu1.$container.isVisible()).toBe(false);
      expect(menu2.enabledComputed).toBe(true);
      expect(menu2.$container).not.toHaveClass('overflown');
    });

    it('updates enabledComputed properly if disabled menu is moved into ellipsis', () => {
      let menu1 = helper.createMenu(createModel('move it'));
      menu1.setVisible(false);
      let menu2 = helper.createMenu(createModel('my menu'));
      menu2.setEnabled(false);
      let menuBar = createMenuBar();
      menuBar.setMenuItems([menu1, menu2]);
      menuBar.render();
      // Menu is 110px, make menu bar larger so that first is displayed and second in ellipsis
      menuBar.htmlComp.setSize(new Dimension(160, 50));
      expect(menu1.$container.isVisible()).toBe(false);
      expect(menu2.enabledComputed).toBe(false);
      expect(menu2.$container).not.toHaveClass('overflown');

      menu1.setVisible(true);
      menu2.setEnabled(true);
      menuBar.validateLayout();

      expect(menu1.$container.isVisible()).toBe(true);
      expect(menu2.enabledComputed).toBe(true);
      expect(menu2.$container).toHaveClass('overflown');
    });
  });

  describe('layout', () => {
    it('gets invalidated if a menu changes its visibility', () => {
      let menu1 = helper.createMenu(createModel('foo')),
        menu2 = helper.createMenu(createModel('bar')),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menu1.setVisible(true);
      menu2.setVisible(true);
      menuBar.setMenuItems(menus);
      menuBar.render();
      menuBar.htmlComp.setSize(new Dimension(500, 50));

      expect(menu1.$container.isVisible()).toBe(true);
      expect(HtmlComponent.get(menuBar.$container).valid).toBe(true);

      menu1.setProperty('visible', false);

      expect(menu1.$container.isVisible()).toBe(false);
      expect(HtmlComponent.get(menuBar.$container).valid).toBe(false);
    });
  });

  describe('updateDefaultMenu', () => {
    it('marks first visible and enabled menu that reacts to ENTER keystroke as default menu', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      modelMenu2.keyStroke = 'enter';

      let menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menus = [menu1, menu2];

      menuBar.setMenuItems(menus);
      menuBar.render();

      expect(menuBar.menuItems.length).toBe(2);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);

      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.$container).toHaveClass('default');
    });

    it('marks ButtonAdapterMenu that reacts to ENTER keystroke as default menu', () => {
      let button = new Button();
      button.init({
        id: '123',
        parent: session.desktop
      });
      let adapterMenu = new ButtonAdapterMenu();
      adapterMenu.init({
        id: '234',
        button: button,
        parent: session.desktop
      });

      button.setProperty('defaultButton', false);
      button.setProperty('keyStroke', 'enter');

      let menuBar = createMenuBar(new GroupBoxMenuItemsOrder()),
        menus = [adapterMenu];

      menuBar.setMenuItems(menus);
      menuBar.render();

      expect(menuBar.menuItems.length).toBe(1);
      expect(menuBar.menuItems[0]).toBe(adapterMenu);

      expect(adapterMenu.$container).toHaveClass('default');
    });

    it('marks first visible and enabled menu that has the "defaultMenu" flag set as default menu', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      let modelMenu3 = createModel('bla');
      let modelMenu4 = createModel('xyz');
      let modelMenu5 = createModel('qux');
      let modelMenu6 = createModel('fum');
      // menu2 should not have the default menu class since the default menu is set to false
      modelMenu2.keyStroke = 'enter';
      modelMenu2.defaultMenu = false;
      // menu3 should have  the default menu class but is not the default menu of the menu bar since it is disabled
      modelMenu3.defaultMenu = true;
      modelMenu3.enabled = false;
      // menu4 has the default menu class and is the default menu of the menu bar.
      modelMenu4.defaultMenu = true;
      // menu5 should have the default menu class but is not the default menu in the menu bar.
      modelMenu5.defaultMenu = true;
      // menu6 should have the default menu class but is not the default menu in the menu bar.
      modelMenu6.keyStroke = 'enter';

      let menuBar = createMenuBar(),
        menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menu3 = helper.createMenu(modelMenu3),
        menu4 = helper.createMenu(modelMenu4),
        menu5 = helper.createMenu(modelMenu5),
        menu6 = helper.createMenu(modelMenu6),
        menus = [menu1, menu2, menu3, menu4, menu5, menu6];

      menuBar.setMenuItems(menus);
      menuBar.render();

      expect(menuBar.menuItems.length).toBe(6);
      expect(menuBar.menuItems[0]).toBe(menu1);
      expect(menuBar.menuItems[1]).toBe(menu2);
      expect(menuBar.menuItems[2]).toBe(menu3);
      expect(menuBar.menuItems[3]).toBe(menu4);
      expect(menuBar.menuItems[4]).toBe(menu5);
      expect(menuBar.menuItems[5]).toBe(menu6);

      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.$container).not.toHaveClass('default');
      expect(menu3.$container).not.toHaveClass('default');
      expect(menu4.$container).toHaveClass('default');
      expect(menu5.$container).not.toHaveClass('default');
      expect(menu6.$container).not.toHaveClass('default');

      expect(menuBar.defaultMenu).toBe(menu4);
      expect(menuBar.tabbableMenu).toBe(menu4);
    });

    it('updates state if menu gets enabled or disabled', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      modelMenu2.keyStroke = 'enter';

      let menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menusItems = [menu1, menu2];

      menuBar.setMenuItems(menusItems);
      menuBar.render();
      expect(menu1.rendered).toBe(true);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.rendered).toBe(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.$container).toHaveClass('default');
      expect(menuBar.tabbableMenu).toBe(menu2);

      menu2.setEnabled(false);
      expect(menuBar.defaultMenu).toBe(null);
      expect(menu2.$container).not.toHaveClass('default');
      expect(menuBar.tabbableMenu).toBe(menu1);

      menu2.setEnabled(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.$container).toHaveClass('default');
      expect(menuBar.tabbableMenu).toBe(menu2);
    });

    it('updates state if keyStroke or defaultMenu property of menu changes', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      modelMenu2.keyStroke = 'enter';

      let menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menuItems = [menu1, menu2];

      menuBar.setMenuItems(menuItems);
      menuBar.render();
      expect(menu1.rendered).toBe(true);
      expect(menu2.rendered).toBe(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.$container).toHaveClass('default');

      menu2.setProperty('keyStroke', null);
      expect(menuBar.defaultMenu).toBe(null);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.$container).not.toHaveClass('default');

      menu1.setProperty('keyStroke', 'enter');
      expect(menuBar.defaultMenu).toBe(menu1);
      expect(menu1.$container).toHaveClass('default');
      expect(menu2.$container).not.toHaveClass('default');

      menu2.setProperty('defaultMenu', true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.$container).toHaveClass('default');

      menu1.setProperty('defaultMenu', false);
      menu2.setProperty('defaultMenu', false);
      expect(menuBar.defaultMenu).toBe(null);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.$container).not.toHaveClass('default');

      menu1.setProperty('defaultMenu', undefined);
      menu2.setProperty('defaultMenu', undefined);
      expect(menuBar.defaultMenu).toBe(menu1);
      expect(menu1.$container).toHaveClass('default');
      expect(menu2.$container).not.toHaveClass('default');
    });

    it('is called even if menu is in ellipsis', () => {
      let modelMenu1 = createModel('foo');
      let modelMenu2 = createModel('bar');
      modelMenu2.keyStroke = 'enter';

      let menu1 = helper.createMenu(modelMenu1),
        menu2 = helper.createMenu(modelMenu2),
        menuBar = createMenuBar(),
        menuItems = [menu1, menu2];

      menuBar.setMenuItems(menuItems);
      menuBar.render();
      expect(menu1.rendered).toBe(true);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.rendered).toBe(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.$container).toHaveClass('default');

      // Move default menu into ellipsis and call updateDefaultMenu explicitly to recalculate state
      menu2._setOverflown(true);
      menuBar._ellipsis.setChildActions([menu2]);
      menuBar._ellipsis.setHidden(false);
      menuBar.updateDefaultMenu();
      expect(menu1.overflown).toBe(false);
      expect(menu1.$container).not.toHaveClass('default');
      expect(menu2.overflown).toBe(true);
      expect(menuBar.defaultMenu).toBe(menu2);

      menu2.setEnabled(false);
      expect(menuBar.defaultMenu).toBe(null);
      expect(menu2.overflown).toBe(true);

      menu2.setEnabled(true);
      expect(menuBar.defaultMenu).toBe(menu2);
      expect(menu2.overflown).toBe(true);
    });
  });

  describe('ellipsis position', () => {
    it('is calculated correctly for ellipsisPosition RIGHT', () => {
      let rightMenu1 = helper.createMenu(createModel('foo')),
        rightMenu2 = helper.createMenu(createModel('bar')),
        leftMenu1 = helper.createMenu(createModel('foo')),
        leftMenu2 = helper.createMenu(createModel('bar')),
        menuBar = createMenuBar(),
        menus = [rightMenu1, rightMenu2, leftMenu1, leftMenu2];

      rightMenu1.setVisible(true);
      rightMenu1.setHorizontalAlignment(1);
      rightMenu2.setVisible(false);
      rightMenu2.setHorizontalAlignment(1);
      leftMenu1.setVisible(true);
      leftMenu1.setHorizontalAlignment(-1);
      leftMenu2.setVisible(true);
      leftMenu2.setHorizontalAlignment(-1);
      menuBar.setEllipsisPosition(MenuBar.EllipsisPosition.RIGHT);
      menuBar.setMenuItems(menus);
      menuBar.render();

      expect(menuBar._ellipsis.rightAligned).toBe(true);
      expect(menuBar.orderedMenuItems.right[0]).toBe(rightMenu1);
      expect(menuBar.orderedMenuItems.right[1]).toBe(menuBar._ellipsis);
      expect(menuBar.orderedMenuItems.right[2]).toBe(rightMenu2);

      rightMenu2.setProperty('visible', true);

      expect(menuBar._ellipsis.rightAligned).toBe(true);
      expect(menuBar.orderedMenuItems.right[0]).toBe(rightMenu1);
      expect(menuBar.orderedMenuItems.right[1]).toBe(rightMenu2);
      expect(menuBar.orderedMenuItems.right[2]).toBe(menuBar._ellipsis);

      rightMenu1.setProperty('visible', false);
      rightMenu2.setProperty('visible', false);

      expect(menuBar._ellipsis.rightAligned).toBe(false);
      expect(menuBar.orderedMenuItems.left[0]).toBe(leftMenu1);
      expect(menuBar.orderedMenuItems.left[1]).toBe(leftMenu2);
      expect(menuBar.orderedMenuItems.left[2]).toBe(menuBar._ellipsis);
    });

    it('is calculated correctly for ellipsisPosition LEFT', () => {
      let rightMenu1 = helper.createMenu(createModel('foo')),
        rightMenu2 = helper.createMenu(createModel('bar')),
        leftMenu1 = helper.createMenu(createModel('foo')),
        leftMenu2 = helper.createMenu(createModel('bar')),
        menuBar = createMenuBar(),
        menus = [rightMenu1, rightMenu2, leftMenu1, leftMenu2];

      rightMenu1.setVisible(true);
      rightMenu1.setHorizontalAlignment(1);
      rightMenu2.setVisible(true);
      rightMenu2.setHorizontalAlignment(1);
      leftMenu1.setVisible(false);
      leftMenu1.setHorizontalAlignment(-1);
      leftMenu2.setVisible(true);
      leftMenu2.setHorizontalAlignment(-1);
      menuBar.setEllipsisPosition(MenuBar.EllipsisPosition.LEFT);
      menuBar.setMenuItems(menus);
      menuBar.render();

      expect(menuBar._ellipsis.rightAligned).toBe(false);
      expect(menuBar.orderedMenuItems.left[0]).toBe(leftMenu1);
      expect(menuBar.orderedMenuItems.left[1]).toBe(menuBar._ellipsis);
      expect(menuBar.orderedMenuItems.left[2]).toBe(leftMenu2);

      leftMenu1.setProperty('visible', true);

      expect(menuBar._ellipsis.rightAligned).toBe(false);
      expect(menuBar.orderedMenuItems.left[0]).toBe(menuBar._ellipsis);
      expect(menuBar.orderedMenuItems.left[1]).toBe(leftMenu1);
      expect(menuBar.orderedMenuItems.left[2]).toBe(leftMenu2);

      leftMenu1.setProperty('visible', false);
      leftMenu2.setProperty('visible', false);

      expect(menuBar._ellipsis.rightAligned).toBe(true);
      expect(menuBar.orderedMenuItems.right[0]).toBe(menuBar._ellipsis);
      expect(menuBar.orderedMenuItems.right[1]).toBe(rightMenu1);
      expect(menuBar.orderedMenuItems.right[2]).toBe(rightMenu2);
    });
  });

  describe('reorderMenus', () => {
    it('updates left-of-button correctly', () => {
      let button1 = scout.create(Menu, {
          parent: session.desktop,
          actionStyle: Action.ActionStyle.BUTTON
        }),
        button2 = scout.create(Menu, {
          parent: session.desktop,
          actionStyle: Action.ActionStyle.BUTTON
        }),
        menuBar = createMenuBar(),
        menus = [button1, button2];

      menuBar.setMenuItems(menus);
      menuBar.render();
      expect(button1.$container).toHaveClass('left-of-button');
      expect(button2.$container).not.toHaveClass('left-of-button');

      menuBar.reorderMenus();
      expect(button1.$container).toHaveClass('left-of-button');
      expect(button2.$container).not.toHaveClass('left-of-button');
    });

    it('updates last correctly', () => {
      let button1 = scout.create(Menu, {
          parent: session.desktop,
          actionStyle: Action.ActionStyle.BUTTON
        }),
        button2 = scout.create(Menu, {
          parent: session.desktop,
          actionStyle: Action.ActionStyle.BUTTON
        }),
        menuBar = createMenuBar(),
        menus = [button1, button2];

      menuBar.setMenuItems(menus);
      menuBar.render();
      menuBar.validateLayout();
      expect(button1.$container).not.toHaveClass('last');
      expect(button2.$container).toHaveClass('last');

      menuBar.reorderMenus();
      menuBar.validateLayout();
      expect(button1.$container).not.toHaveClass('last');
      expect(button2.$container).toHaveClass('last');
    });
  });

  describe('aria properties', () => {

    it('has aria role menubar', () => {
      let menubar = createMenuBar();
      menubar.render();
      expect(menubar.$container).toHaveAttr('role', 'menubar');
    });
  });
});
