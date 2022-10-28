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
import {Menu, menus, scout} from '../../src/index';
import {MenuSpecHelper} from '../../src/testing/index';

describe('menus', () => {
  let helper: MenuSpecHelper, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new MenuSpecHelper(session);
  });

  describe('filter', () => {

    it('does nothing if no menus are given', () => {
      let items;
      items = menus.filter(items);
      expect(items).toBeUndefined();
    });

    it('returns no menus if no types are given', () => {
      let items, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel('1'));
      menu2 = helper.createMenu(helper.createModel('2'));
      menu3 = helper.createMenu(helper.createModel('3'));
      menu3.visible = false;

      items = menus.filter([menu1, menu2, menu3]);
      expect(items).toEqual([]);
    });

    it('only returns visible menus, if onlyVisible param is set to true', () => {
      let items, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel('1'));
      menu1.setMenuTypes(['SingleSelection']);
      menu2 = helper.createMenu(helper.createModel('2'));
      menu2.setMenuTypes(['SingleSelection']);
      menu3 = helper.createMenu(helper.createModel('3'));
      menu3.setMenuTypes(['SingleSelection']);
      menu3.visible = false;

      items = menus.filter([menu1, menu2, menu3], 'SingleSelection', {
        onlyVisible: true
      });

      expect(items).toEqual([menu1, menu2]);
    });

    it('only returns menus with given type (even when menu is not visible)', () => {
      let items, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel('1'));
      menu1.setMenuTypes(['MultiSelection', 'SingleSelection']);
      menu1.visible = false;
      menu2 = helper.createMenu(helper.createModel('2'));
      menu2.setMenuTypes(['MultiSelection']);
      menu3 = helper.createMenu(helper.createModel('3'));
      menu3.setMenuTypes(['SingleSelection']);

      items = menus.filter([menu1, menu2, menu3], 'SingleSelection');

      expect(items).toEqual([menu1, menu3]);
    });

    it('only returns parent menus if child menus should be displayed', () => {
      let items, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel('1'));
      menu1.setMenuTypes(['MultiSelection', 'SingleSelection']);
      menu2 = helper.createMenu(helper.createModel('2'));
      menu2.setMenuTypes(['SingleSelection']);
      menu3 = helper.createMenu(helper.createModel('3'));
      menu3.setMenuTypes(['MultiSelection']);
      menu2.childActions = [menu3];

      items = menus.filter([menu1, menu2], 'SingleSelection');
      expect(items).toEqual([menu1]);

      menu3.setMenuTypes(['SingleSelection']);
      items = menus.filter([menu1, menu2], 'SingleSelection');
      expect(items).toEqual([menu1, menu2]);
    });

    it('only returns parent menus if child menus have correct type', () => {
      let items, parentMenu, menu1, menu2;
      parentMenu = helper.createMenu(helper.createModel('1'));
      menu1 = helper.createMenu(helper.createModel('2'));
      menu1.setMenuTypes(['SingleSelection']);
      menu2 = helper.createMenu(helper.createModel('3'));
      menu2.setMenuTypes(['EmptySpace']);
      parentMenu.childActions = [menu1, menu2];

      items = menus.filter([parentMenu], 'EmptySpace');
      expect(items).toEqual([parentMenu]);

      menu2.setMenuTypes(['SingleSelection']);
      items = menus.filter([parentMenu], 'EmptySpace');
      expect(items).toEqual([]);
    });

    it('returns a menu with no menuTypes if the at least one defaultMenuType matches the given types', () => {
      let menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel('1'));
      menu1.setMenuTypes(['test.MenuType1', 'test.MenuType2']);
      menu1.visible = false;
      menu2 = helper.createMenu(helper.createModel('2'));
      menu2.setMenuTypes(['test.MenuType1']);
      menu3 = helper.createMenu(helper.createModel('3'));
      menu3.setMenuTypes(['test.MenuType2']);

      expect(menus.filter([menu1, menu2, menu3], 'test.MenuType1'))
        .toEqual([menu1, menu2]);
      expect(menus.filter([menu1, menu2, menu3], 'test.MenuType1', {defaultMenuTypes: ['test.MenuType1', 'test.MenuType3']}))
        .toEqual([menu1, menu2]);

      menu3.setMenuTypes([]);
      expect(menus.filter([menu1, menu2, menu3], 'test.MenuType1', {defaultMenuTypes: ['test.MenuType1', 'test.MenuType3']}))
        .toEqual([menu1, menu2, menu3]);
    });
  });

  describe('updateSeparatorVisibility', () => {
    it('makes leading separators invisible', () => {
      let menu1 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu2 = scout.create(Menu, {
        parent: session.desktop
      });
      let items = [menu1, menu2];
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(false);
      expect(items[1].visible).toBe(true);
    });

    it('makes trailing separators invisible', () => {
      let menu1 = scout.create(Menu, {
        parent: session.desktop
      });
      let menu2 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let items = [menu1, menu2];
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(true);
      expect(items[1].visible).toBe(false);
    });

    it('makes duplicate separators invisible', () => {
      let menu0 = scout.create(Menu, {
        parent: session.desktop
      });
      let menu1 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu2 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu3 = scout.create(Menu, {
        parent: session.desktop
      });
      let items = [menu0, menu1, menu2, menu3];
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(true);
      expect(items[1].visible).toBe(false);
      expect(items[2].visible).toBe(true);
      expect(items[3].visible).toBe(true);
    });

    it('makes all separators invisible if there are no other visible menus', () => {
      let menu0 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu1 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let items = [menu0, menu1];
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(false);
      expect(items[1].visible).toBe(false);
    });

    it('reverts to old state if sibling menus get visible', () => {
      let menu0 = scout.create(Menu, {
        parent: session.desktop,
        visible: false
      });
      let menu1 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu2 = scout.create(Menu, {
        parent: session.desktop
      });
      let items = [menu0, menu1, menu2];
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(false);
      expect(items[1].visible).toBe(false);
      expect(items[2].visible).toBe(true);

      menu0.setVisible(true);
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(true);
      expect(items[1].visible).toBe(true);
      expect(items[2].visible).toBe(true);
    });

    it('considers all rules', () => {
      let menu0 = scout.create(Menu, {
        parent: session.desktop,
        visible: false
      });
      let menu1 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu2 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu3 = scout.create(Menu, {
        parent: session.desktop
      });
      let menu4 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu5 = scout.create(Menu, {
        parent: session.desktop
      });
      let menu6 = scout.create(Menu, {
        parent: session.desktop,
        separator: true
      });
      let menu7 = scout.create(Menu, {
        parent: session.desktop,
        visible: false
      });
      let items = [menu0, menu1, menu2, menu3, menu4, menu5, menu6, menu7];
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(false);
      expect(items[1].visible).toBe(false);
      expect(items[2].visible).toBe(false);
      expect(items[3].visible).toBe(true);
      expect(items[4].visible).toBe(true);
      expect(items[5].visible).toBe(true);
      expect(items[6].visible).toBe(false);
      expect(items[7].visible).toBe(false);

      menu0.setVisible(true);
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(true);
      expect(items[1].visible).toBe(false);
      expect(items[2].visible).toBe(true);
      expect(items[3].visible).toBe(true);
      expect(items[4].visible).toBe(true);
      expect(items[5].visible).toBe(true);
      expect(items[6].visible).toBe(false);
      expect(items[7].visible).toBe(false);

      menu7.setVisible(true);
      menus.updateSeparatorVisibility(items);
      expect(items[0].visible).toBe(true);
      expect(items[1].visible).toBe(false);
      expect(items[2].visible).toBe(true);
      expect(items[3].visible).toBe(true);
      expect(items[4].visible).toBe(true);
      expect(items[5].visible).toBe(true);
      expect(items[6].visible).toBe(true);
      expect(items[7].visible).toBe(true);
    });
  });
});
