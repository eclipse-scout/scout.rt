/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, keys, Menu, Popup, scout, Widget} from '../../src/index';
import {JQueryTesting, MenuSpecHelper} from '../../src/testing/index';

describe('ContextMenuPopup', () => {
  let helper: MenuSpecHelper, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new MenuSpecHelper(session);
  });

  /**
   * Returns a clone for the given originalMenu. In the model we don't know the clone instance,
   * so we have to find the clones by traversing the DOM of the popup, since each DOM node
   * has a data() referencing the widget instance.
   */
  function findClone(popup: Popup, originalMenu: Menu): Menu {
    let clone: Menu = null;
    popup.$container.find('.menu-item').each(function() {
      let $menuItem = $(this);
      let widget = $menuItem.data('widget') as Widget;
      if (widget.cloneOf === originalMenu) {
        clone = widget as Menu;
      }
    });
    return clone;
  }

  describe('options.cloneMenuItems', () => {
    let popup: ContextMenuPopup, menu: Menu, childMenu: Menu;

    beforeEach(() => {
      menu = helper.createMenu(helper.createModel());
      childMenu = helper.createMenu(helper.createModel());
      menu.childActions = [childMenu];
    });

    describe('true', () => {

      it('clones the given menus and renders the clones', () => {
        let menuItems = [menu];
        menu.render();
        popup = scout.create(ContextMenuPopup, {
          parent: session.desktop,
          session: session,
          menuItems: menuItems,
          cloneMenuItems: true
        });
        popup.render();

        let menuClone = findClone(popup, menu);
        // clone is rendered in the popup
        expect(menuClone.rendered).toBe(true);
        expect(menuClone.$container.parent()[0]).toBe(popup.$body[0]);
        expect(menuClone.parent).toBe(popup);
        // original menu is still rendered in the desktop
        expect(menu.rendered).toBe(true);
        expect(menu.$container.parent()[0]).toBe(session.$entryPoint[0]);
        expect(menu.parent).toBe(session.desktop);
        popup.destroy();
      });

      it('only destroys and removes the clones on popup close', () => {
        let menuItems = [menu];
        menu.render();

        popup = scout.create(ContextMenuPopup, {
          parent: session.desktop,
          session: session,
          menuItems: menuItems,
          cloneMenuItems: true
        });
        popup.render();

        let menuClone = findClone(popup, menu);
        let childMenuClone = menuClone.childActions[0];
        popup.animateRemoval = false;
        popup.destroy();

        expect(menuClone.destroyed).toBe(true);
        expect(childMenuClone.destroyed).toBe(true);
        // original menu is still rendered and not destroyed
        expect(menu.destroyed).toBe(false);
        expect(menu.rendered).toBe(true);
        expect(childMenu.destroyed).toBe(false);
      });

    });

    describe('false', () => {

      it('renders the original menus', () => {
        let menuItems = [menu];
        popup = scout.create(ContextMenuPopup, {
          parent: session.desktop,
          session: session,
          menuItems: menuItems,
          cloneMenuItems: false
        });
        popup.render();

        let menuClone = findClone(popup, menu);
        expect(menuClone).toBe(null);
        // original menu is rendered in the popup
        expect(menu.rendered).toBe(true);
        expect(menu.$container.parent()[0]).toBe(popup.$body[0]);
        expect(menu.parent).toBe(popup);
        popup.destroy();
      });

      it('removes but does not destroy the menus on popup close', () => {
        let menuItems = [menu];
        popup = scout.create(ContextMenuPopup, {
          parent: session.desktop,
          session: session,
          menuItems: menuItems,
          cloneMenuItems: false,
          animateRemoval: false
        });
        popup.render();

        let menuClone = findClone(popup, menu);
        expect(menuClone).toBe(null);
        // original menu is rendered in the popup
        expect(menu.rendered).toBe(true);
        expect(menu.$container.parent()[0]).toBe(popup.$body[0]);
        expect(menu.parent).toBe(popup);

        popup.destroy();
        expect(menu.rendered).toBe(false);
        expect(menu.destroyed).toBe(false);
        expect(childMenu.destroyed).toBe(false);

        // render again -> must not fail
        popup = scout.create(ContextMenuPopup, {
          parent: session.desktop,
          session: session,
          menuItems: menuItems,
          cloneMenuItems: false
        });
        popup.render();
        popup.destroy();
      });
    });
  });

  describe('renderMenuItems', () => {
    let popup, menu0, menu1, menu2;

    beforeEach(() => {
      menu0 = helper.createMenu(helper.createModel());
      menu1 = helper.createMenu(helper.createModel());
      menu2 = helper.createMenu(helper.createModel());
    });

    it('renders invisible menus', () => {
      let menuItems = [menu0, menu1, menu2];
      menu2.setVisible(false);
      popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      let menu0Clone = findClone(popup, menu0);
      let menu1Clone = findClone(popup, menu1);
      let menu2Clone = findClone(popup, menu2);
      expect(menu0Clone.$container.isVisible()).toBe(true);
      expect(menu1Clone.$container.isVisible()).toBe(true);
      expect(menu2Clone.$container.isVisible()).toBe(false);
      popup.remove();
    });

    it('adds last and first classes', () => {
      let menuItems = [menu0, menu1, menu2];
      popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      let menu0Clone = findClone(popup, menu0);
      let menu2Clone = findClone(popup, menu2);
      expect(menu0Clone.$container).toHaveClass('first');
      expect(menu2Clone.$container).toHaveClass('last');
      popup.remove();
    });

    it('considers visibility when adding last and first classes', () => {
      let menuItems = [menu0, menu1, menu2];
      menu2.setVisible(false);
      popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      let menu0Clone = findClone(popup, menu0);
      let menu1Clone = findClone(popup, menu1);
      let menu2Clone = findClone(popup, menu2);
      expect(menu0Clone.$container).toHaveClass('first');
      expect(menu1Clone.$container).toHaveClass('last');
      expect(menu2Clone.$container).not.toHaveClass('last');
      popup.remove();
    });

    it('makes cloned menus non-tabbable', () => {
      menu0.tabbable = true; // <-- !
      let menuItems = [menu0, menu1, menu2];
      popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      let menu0Clone = findClone(popup, menu0);
      let menu1Clone = findClone(popup, menu1);
      let menu2Clone = findClone(popup, menu2);
      expect(menu0Clone.tabbable).toBe(false); // <-- !
      expect(menu1Clone.tabbable).toBe(false);
      expect(menu2Clone.tabbable).toBe(false);
      expect(menu0Clone.$container.attr('tabindex')).toBeUndefined(); // <-- !
      expect(menu1Clone.$container.attr('tabindex')).toBeUndefined();
      expect(menu2Clone.$container.attr('tabindex')).toBeUndefined();
      popup.remove();
    });

  });

  describe('preventDefault', () => {

    it('closes the popup when default is not prevented', () => {
      let menu1 = helper.createMenu(helper.createModel());
      let menu2 = helper.createMenu(helper.createModel());

      let menu1Clicked = false;
      menu1.on('action', event => {
        menu1Clicked = true;
      });

      let popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: [menu1, menu2],
        animateOpening: false,
        animateRemoval: false
      });

      popup.open();
      findClone(popup, menu1).doAction();
      expect(menu1Clicked).toBe(true);
      expect(popup.rendered).toBe(false);
    });

    it('does not close the popup when default is prevented', () => {
      let menu1 = helper.createMenu(helper.createModel());
      let menu2 = helper.createMenu(helper.createModel());

      let menu1Clicked = false;
      menu1.on('action', event => {
        event.preventDefault(); // <--
        menu1Clicked = true;
      });

      let popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: [menu1, menu2],
        animateOpening: false,
        animateRemoval: false
      });

      popup.open();
      findClone(popup, menu1).doAction();
      expect(menu1Clicked).toBe(true);
      expect(popup.rendered).toBe(true); // <--

      popup.close();
    });
  });

  describe('aria properties', () => {
    let popup: ContextMenuPopup, menu: Menu, childMenu: Menu;

    beforeEach(() => {
      menu = helper.createMenu(helper.createModel());
      childMenu = helper.createMenu(helper.createModel());
      menu.childActions = [childMenu];
    });

    it('has role menu', () => {
      let menuItems = [menu];
      menu.render();
      popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();
      expect(popup.$container).toHaveAttr('role', 'menu');
    });

    it('has aria-activedescendant set when navigating', () => {
      let menuItems = [menu];
      menu.render();
      popup = scout.create(ContextMenuPopup, {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();
      JQueryTesting.triggerKeyDown(popup.$body, keys.DOWN);
      expect(popup.$container.attr('aria-activedescendant')).toBeTruthy();
    });
  });
});
