/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormSpecHelper} from '@eclipse-scout/testing';
import {FormMenu, menus, scout} from '../../src/index';

/* global linkWidgetAndAdapter */
describe('FormMenu', () => {
  let session, desktop, helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    desktop = session.desktop;
  });

  function createMenu(modelProperties) {
    let menu = helper.createField('FormMenu', desktop, modelProperties);
    menu.form = helper.createFormWithOneField();
    menu.desktop = desktop;
    return menu;
  }

  function findPopup() {
    return $('.popup');
  }

  describe('setSelected', () => {

    it('opens and closes the form popup', () => {
      let menu = createMenu();
      menu.render();
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      menu.setSelected(false);
      expect(findPopup()).not.toExist();
    });

    it('opens the popup and the ellipsis if the menu is overflown', () => {
      let ellipsisMenu = menus.createEllipsisMenu({
        parent: session.desktop
      });
      ellipsisMenu.render();

      let menu = createMenu();
      menu.render();

      menus.moveMenuIntoEllipsis(menu, ellipsisMenu);
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

    it('opens the popup but not the ellipsis if the menu is overflown and mobile popup style is used', () => {
      let ellipsisMenu = menus.createEllipsisMenu({
        parent: session.desktop
      });
      ellipsisMenu.render();

      let menu = createMenu({popupStyle: FormMenu.PopupStyle.MOBILE});
      menu.render();

      menus.moveMenuIntoEllipsis(menu, ellipsisMenu);
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

  describe('detach', () => {
    it('does not fail if a parent is detached', () => {
      let menuBar = scout.create('MenuBar', {parent: session.desktop});
      let menu = createMenu();
      // Link menu with the menu bar
      menuBar.setMenuItems([menu]);
      menuBar.render();
      menu.setSelected(true);
      expect(menu.popup.rendered).toBe(true);

      // Popup#_renderOnDetach will remove the popup and eventually the form.
      // As soon as the form is removed, the FormMenu will unselected the menu which closes the popup (FormMenu#_onFormRemove).
      menuBar.detach();
      expect(menu.popup).toBe(null);
    });
  });

  describe('onModelPropertyChange', () => {

    describe('selected', () => {

      it('calls setSelected', () => {
        let menu = createMenu();
        linkWidgetAndAdapter(menu, 'MenuAdapter');
        menu.render();
        expect(findPopup()).not.toExist();

        spyOn(menu, 'setSelected');

        let event = createPropertyChangeEvent(menu, {
          'selected': true
        });
        menu.modelAdapter.onModelPropertyChange(event);
        expect(menu.setSelected).toHaveBeenCalled();
      });

    });

  });

});
