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
describe("FormMenu", function() {
  var session, desktop, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    desktop = {
      $parent: session.$entryPoint,
      $toolContainer: session.$entryPoint.appendDiv('desktop-tool-box').hide()
    };
  });

  function createMenu(model) {
    model.form = helper.createFormWithOneField();
    model.desktop = desktop;

    var menu = new scout.FormMenu();
    menu.init(model);
    menu.position = function() {};
    return menu;
  }

  function findPopup() {
    return $('.popup');
  }

  describe("setSelected", function() {

    it("opens and closes the form popup", function() {
      var menu = createMenu(createSimpleModel('FormMenu', session));
      menu.render(session.$entryPoint);
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      menu.setSelected(false);
      expect(findPopup()).not.toExist();
    });

    it("opens the popup and the ellipsis if the menu is overflown", function() {
      var ellipsisMenu = scout.menus.createEllipsisMenu({
        parent: new scout.NullWidget(),
        session: session
      });
      ellipsisMenu.render(session.$entryPoint);

      var menu = createMenu(createSimpleModel('FormMenu', session));
      menu.render(session.$entryPoint);

      scout.menus.moveMenuIntoEllipsis(menu, ellipsisMenu);
      expect(menu.rendered).toBe(false);
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(ellipsisMenu.selected).toBe(true);
      expect(menu.selected).toBe(true);
      expect(findPopup()).toBeVisible();

      // cleanup
      menu.setSelected(false);
      ellipsisMenu.setSelected(false);
    });

    it("opens the popup but not the ellipsis if the menu is overflown and mobile popup style is used", function() {
      var ellipsisMenu = scout.menus.createEllipsisMenu({
        parent: new scout.NullWidget(),
        session: session
      });
      ellipsisMenu.render(session.$entryPoint);

      var model = createSimpleModel('FormMenu', session);
      model.popupStyle = scout.FormMenu.PopupStyle.MOBILE;
      var menu = createMenu(model);
      menu.render(session.$entryPoint);

      scout.menus.moveMenuIntoEllipsis(menu, ellipsisMenu);
      expect(menu.rendered).toBe(false);
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(ellipsisMenu.selected).toBe(false);
      expect(menu.selected).toBe(true);
      expect(findPopup()).toBeVisible();

      // cleanup
      menu.setSelected(false);
    });

  });

  describe("onModelPropertyChange", function() {

    describe("selected", function() {

      it("calls setSelected", function() {
        var menu = createMenu(createSimpleModel('FormMenu', session));
        menu.render(session.$entryPoint);
        expect(findPopup()).not.toExist();

        spyOn(menu, 'setSelected');

        var event = createPropertyChangeEvent(menu, {
          "selected": true
        });
        menu.onModelPropertyChange(event);
        expect(menu.setSelected).toHaveBeenCalled();
      });

    });

  });

});
