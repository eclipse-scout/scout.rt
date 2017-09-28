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
describe("menus", function() {
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.MenuSpecHelper(session);
  });

  describe("filter", function() {

    it("does nothing if no menus are given", function() {
      var menus;
      menus = scout.menus.filter(menus);
      expect(menus).toBeUndefined();
    });

    it("returns no menus if no types are given", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel(1));
      menu2 = helper.createMenu(helper.createModel(2));
      menu3 = helper.createMenu(helper.createModel(3));
      menu3.visible = false;

      menus = scout.menus.filter([menu1, menu2, menu3]);
      expect(menus).toEqual([]);
    });

    it("only returns visible menus, if onlyVisible param is set to true", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel(1));
      menu1.menuTypes = ['SingleSelection'];
      menu2 = helper.createMenu(helper.createModel(2));
      menu2.menuTypes = ['SingleSelection'];
      menu3 = helper.createMenu(helper.createModel(3));
      menu3.menuTypes = ['SingleSelection'];
      menu3.visible = false;

      menus = scout.menus.filter([menu1, menu2, menu3], 'SingleSelection', true);

      expect(menus).toEqual([menu1, menu2]);
    });

    it("only returns menus with given type (even when menu is not visible)", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel(1));
      menu1.menuTypes = ['MultiSelection', 'SingleSelection'];
      menu1.visible = false;
      menu2 = helper.createMenu(helper.createModel(2));
      menu2.menuTypes = ['MultiSelection'];
      menu3 = helper.createMenu(helper.createModel(3));
      menu3.menuTypes = ['SingleSelection'];

      menus = scout.menus.filter([menu1, menu2, menu3], 'SingleSelection');

      expect(menus).toEqual([menu1, menu3]);
    });

    it("only returns parent menus if child menus should be displayed", function() {
      var menus, menu1, menu2, menu3;
      menu1 = helper.createMenu(helper.createModel(1));
      menu1.menuTypes = ['MultiSelection', 'SingleSelection'];
      menu2 = helper.createMenu(helper.createModel(2));
      menu2.menuTypes = ['SingleSelection'];
      menu3 = helper.createMenu(helper.createModel(3));
      menu3.menuTypes = ['MultiSelection'];
      menu2.childActions = [menu3];

      menus = scout.menus.filter([menu1, menu2], 'SingleSelection');
      expect(menus).toEqual([menu1]);

      menu3.menuTypes = ['SingleSelection'];
      menus = scout.menus.filter([menu1, menu2], 'SingleSelection');
      expect(menus).toEqual([menu1, menu2]);
    });

    it("only returns parent menus if child menus have correct type", function() {
      var menus, parentMenu, menu1, menu2;
      parentMenu = helper.createMenu(helper.createModel(1));
      parentMenu.menuTypes = [];
      menu1 = helper.createMenu(helper.createModel(2));
      menu1.menuTypes = ['SingleSelection'];
      menu2 = helper.createMenu(helper.createModel(3));
      menu2.menuTypes = ['EmptySpace'];
      parentMenu.childActions = [menu1, menu2];

      menus = scout.menus.filter([parentMenu], 'EmptySpace');
      expect(menus).toEqual([parentMenu]);

      menu2.menuTypes = ['SingleSelection'];
      menus = scout.menus.filter([parentMenu], 'EmptySpace');
      expect(menus).toEqual([]);
    });

  });

  describe("updateSeparatorVisibility", function() {
    it("makes leading separators invisible", function() {
      var menu1 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu2 = scout.create('Menu', {
        parent: session.desktop
      });
      var menus = [menu1, menu2];
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(false);
      expect(menus[1].visible).toBe(true);
    });

    it("makes trailing separators invisible", function() {
      var menu1 = scout.create('Menu', {
        parent: session.desktop
      });
      var menu2 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menus = [menu1, menu2];
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(true);
      expect(menus[1].visible).toBe(false);
    });

    it("makes duplicate separators invisible", function() {
      var menu0 = scout.create('Menu', {
        parent: session.desktop
      });
      var menu1 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu2 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu3 = scout.create('Menu', {
        parent: session.desktop
      });
      var menus = [menu0, menu1, menu2, menu3];
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(true);
      expect(menus[1].visible).toBe(false);
      expect(menus[2].visible).toBe(true);
      expect(menus[3].visible).toBe(true);
    });

    it("makes all separators invisible if there are no other visible menus", function() {
      var menu0 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu1 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menus = [menu0, menu1];
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(false);
      expect(menus[1].visible).toBe(false);
    });

    it("reverts to old state if sibling menus get visible", function() {
      var menu0 = scout.create('Menu', {
        parent: session.desktop,
        visible: false
      });
      var menu1 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu2 = scout.create('Menu', {
        parent: session.desktop
      });
      var menus = [menu0, menu1, menu2];
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(false);
      expect(menus[1].visible).toBe(false);
      expect(menus[2].visible).toBe(true);

      menu0.setVisible(true);
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(true);
      expect(menus[1].visible).toBe(true);
      expect(menus[2].visible).toBe(true);
    });

    it("considers all rules", function() {
      var menu0 = scout.create('Menu', {
        parent: session.desktop,
        visible: false
      });
      var menu1 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu2 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu3 = scout.create('Menu', {
        parent: session.desktop
      });
      var menu4 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu5 = scout.create('Menu', {
        parent: session.desktop
      });
      var menu6 = scout.create('Menu', {
        parent: session.desktop,
        separator: true
      });
      var menu7 = scout.create('Menu', {
        parent: session.desktop,
        visible: false
      });
      var menus = [menu0, menu1, menu2, menu3, menu4, menu5, menu6, menu7];
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(false);
      expect(menus[1].visible).toBe(false);
      expect(menus[2].visible).toBe(false);
      expect(menus[3].visible).toBe(true);
      expect(menus[4].visible).toBe(true);
      expect(menus[5].visible).toBe(true);
      expect(menus[6].visible).toBe(false);
      expect(menus[7].visible).toBe(false);

      menu0.setVisible(true);
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(true);
      expect(menus[1].visible).toBe(false);
      expect(menus[2].visible).toBe(true);
      expect(menus[3].visible).toBe(true);
      expect(menus[4].visible).toBe(true);
      expect(menus[5].visible).toBe(true);
      expect(menus[6].visible).toBe(false);
      expect(menus[7].visible).toBe(false);

      menu7.setVisible(true);
      scout.menus.updateSeparatorVisibility(menus);
      expect(menus[0].visible).toBe(true);
      expect(menus[1].visible).toBe(false);
      expect(menus[2].visible).toBe(true);
      expect(menus[3].visible).toBe(true);
      expect(menus[4].visible).toBe(true);
      expect(menus[5].visible).toBe(true);
      expect(menus[6].visible).toBe(true);
      expect(menus[7].visible).toBe(true);
    });
  });

});
