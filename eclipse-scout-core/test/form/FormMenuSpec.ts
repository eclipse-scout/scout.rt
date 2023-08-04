/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper} from '../../src/testing/index';
import {ContextMenuPopup, Desktop, FormMenu, FormMenuModel, Menu, MenuBar, menus, scout} from '../../src/index';

describe('FormMenu', () => {
  let session: SandboxSession, desktop: Desktop, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    desktop = session.desktop;

    $(`<style>
      @keyframes nop { 0% { opacity: 1; } 100% { opacity: 1; } }
      .popup.animate-remove { animation: nop; animation-duration: 1s;}
      </style>`).appendTo($('#sandbox'));
  });

  function createMenu(model?: FormMenuModel): FormMenu {
    return scout.create(FormMenu, $.extend({
      parent: desktop,
      text: 'Menu',
      form: helper.createFormWithOneField()
    }, model));
  }

  function findPopup(): JQuery {
    return $('.popup');
  }

  describe('setSelected', () => {

    it('opens and closes the form popup', () => {
      let menu = createMenu();
      menu.render();
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      menu.popup.animateRemoval = false;
      menu.setSelected(false);
      expect(findPopup()).not.toExist();
    });

    it('unselects the menu and closes the popup if the form closes', () => {
      let menu = createMenu();
      menu.render();
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      menu.popup.animateRemoval = false;
      menu.form.close();
      expect(menu.selected).toBe(false);
      expect(menu.form).toBe(null);
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

    it('closes immediately when selected again during remove animation', () => {
      let menu = createMenu();
      menu.render();
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      menu.setSelected(false);
      // Still visible, remove animation started
      expect(findPopup()).toBeVisible();
      expect(menu.popup.isRemovalPending()).toBe(true);

      let $popup = menu.popup.$container;
      menu.setSelected(true);
      // Old popup is removed immediately, new one is rendered
      expect($popup.isAttached()).toBe(false);
      expect(findPopup()).toBeVisible();
      expect(menu.popup.rendered).toBe(true);
      expect(menu.popup.isRemovalPending()).toBe(false);
    });

    it('closes immediately when another form is opened in a context menu during remove animation', () => {
      let menu = createMenu();
      menu.render();
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      menu.setSelected(false);
      // Still visible, remove animation started
      let $popup = menu.popup.$container;
      expect($popup).toBeVisible();
      expect(menu.popup.isRemovalPending()).toBe(true);

      let contextMenu = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        menuItems: [menu] // will be cloned, but form won't
      });
      contextMenu.open();
      let menuInContextMenu = contextMenu.$menuItems().data('widget');
      menuInContextMenu.setSelected(true);
      // Old popup is removed immediately, context menu is rendered
      expect($popup.isAttached()).toBe(false);
      expect(menuInContextMenu.popup.$container).toBeVisible();
      expect(contextMenu.rendered).toBe(true);
    });

    it('closes immediately when another form is opened in a context menu during remove animation after closing form', () => {
      let menu = createMenu();
      menu.on('propertyChange:selected', event => {
        // When menu is unselected, form is set to null.
        // Probably the form menu should create a new form automatically, once the menu is selected again. But that doesn't happen (yet) so we do it manually
        if (event.newValue && !menu.form) {
          menu.setForm(helper.createFormWithOneField());
        }
      });
      menu.render();
      expect(findPopup()).not.toExist();

      menu.setSelected(true);
      expect(findPopup()).toBeVisible();

      // This is the difference to the above test case: form is closed instead of menu unselected
      menu.form.close();

      // Still visible, remove animation started
      let $popup = menu.popup.$container;
      expect($popup).toBeVisible();
      expect(menu.popup.isRemovalPending()).toBe(true);

      let contextMenu = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        menuItems: [menu] // will be cloned, but form won't
      });
      contextMenu.open();
      let menuInContextMenu = contextMenu.$menuItems().data('widget');
      menuInContextMenu.setSelected(true);
      // Old popup is removed immediately, context menu is rendered
      expect($popup.isAttached()).toBe(false);
      expect(menuInContextMenu.popup.$container).toBeVisible();
      expect(contextMenu.rendered).toBe(true);
    });

    it('closes immediately when opened in a context menu and another menu is opened during remove animation', () => {
      let menu = createMenu();
      let contextMenu = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        menuItems: [menu]
      });
      contextMenu.open();
      let menuInContextMenu = contextMenu.$menuItems().data('widget');
      menuInContextMenu.setSelected(true);
      expect(menuInContextMenu.popup.$container).toBeVisible();
      expect(contextMenu.rendered).toBe(true);

      // Menu (e.g. in menubar) is selected
      menu.render();
      menu.setSelected(true);
      expect(findPopup()).toBeVisible();
      // Context menu and popup are removed immediately, popup of menu is opened
      expect(contextMenu.rendered).toBe(false);
      expect(menuInContextMenu.popup).toBe(null);
      expect(menu.popup.$container).toBeVisible();
    });

    it('closes immediately when opened in a context menu and another form is opened in another context menu during remove animation', () => {
      let menu = createMenu();
      let contextMenu = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        menuItems: [menu]
      });
      contextMenu.open();
      let menuInContextMenu = contextMenu.$menuItems().data('widget');
      menuInContextMenu.setSelected(true);
      expect(menuInContextMenu.popup.$container).toBeVisible();
      expect(contextMenu.rendered).toBe(true);

      let contextMenu2 = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        menuItems: [menu]
      });
      contextMenu2.open();
      let menuInContextMenu2 = contextMenu2.$menuItems().data('widget');
      menuInContextMenu2.setSelected(true);
      // Old context menu and popup are removed immediately, new context menu is rendered
      expect(contextMenu.rendered).toBe(false);
      expect(menuInContextMenu.popup).toBe(null);
      expect(menuInContextMenu2.popup.$container).toBeVisible();
      expect(contextMenu2.rendered).toBe(true);
    });

    it('does not throw an exception if popup in ellipsis is closed', () => {
      let ellipsisMenu = menus.createEllipsisMenu({
        parent: session.desktop
      });
      ellipsisMenu.render();

      let menu = createMenu();
      // With scout classic, the owner is the null widget.
      // The parent should be the menu, but may be reset to the owner when the popup is destroyed.
      // This would lead to an exception when the form should be rendered because the null widget does not have a $container. (306348)
      menu.form.setOwner(session.root);
      menu.render();
      menu.setSelected(true);
      expect(menu.popup).toBeDefined();

      menu.popup.animateRemoval = false;
      menus.moveMenuIntoEllipsis(menu, ellipsisMenu);
      ellipsisMenu.setSelected(true);
      ellipsisMenu.destroy();
      menus.removeMenuFromEllipsis(menu);
      session.layoutValidator.validate();
    });

    describe('with mobile popup style', () => {

      it('opens and closes the form popup even if menu is not rendered', () => {
        let menu = createMenu({popupStyle: FormMenu.PopupStyle.MOBILE});
        expect(findPopup()).not.toExist();

        menu.setSelected(true);
        expect(findPopup()).toBeVisible();

        menu.popup.animateRemoval = false;
        menu.setSelected(false);
        expect(findPopup()).not.toExist();
      });

      it('opens the popup but not the ellipsis if the menu is overflown', () => {
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
  });

  describe('detach', () => {
    it('does not fail if a parent is detached', () => {
      let menuBar = scout.create(MenuBar, {parent: session.desktop});
      let menu = createMenu();
      // Link menu with the menu bar
      menuBar.setMenuItems([menu]);
      menuBar.render();
      menu.setSelected(true);
      expect(menu.popup.rendered).toBe(true);

      // Popup#_renderOnDetach will remove the popup and eventually the form.
      menuBar.detach();
      expect(menu.popup.rendered).toBe(false);
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

  describe('aria properties', () => {

    it('has aria role menuitem', () => {
      let testMenu = createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo'
      });
      testMenu.render();
      expect(testMenu.$container).toHaveAttr('role', 'menuitem');
    });

    it('has aria-haspopup set to dialog', () => {
      let testMenu = createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo'
      });
      testMenu.render();
      expect(testMenu.$container).toHaveAttr('aria-haspopup', 'dialog');
    });

    it('has aria-expanded set to true if it is selected', () => {
      let testMenu = createMenu({
        text: 'My Test Menu',
        tooltipText: 'moo'
      });
      testMenu.render();
      expect(testMenu.$container).toHaveAttr('aria-expanded', 'false');
      // also check that aria pressed is not set (not supported for menu items role)
      expect(testMenu.$container.attr('aria-pressed')).toBeFalsy();
      testMenu.setSelected(true);
      expect(testMenu.$container).toHaveAttr('aria-expanded', 'true');
      expect(testMenu.$container.attr('aria-pressed')).toBeFalsy();
    });
  });
});
