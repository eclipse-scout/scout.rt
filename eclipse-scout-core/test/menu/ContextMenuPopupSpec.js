/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ContextMenuPopup, scout} from '../../src/index';
import {MenuSpecHelper} from '../../src/testing/index';

describe('ContextMenuPopup', () => {
  let helper, session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new MenuSpecHelper(session);
  });

  /**
   * Returns a clone for the given originalMenu. In the model we don't know the clone instance
   * so we have to find the clones by traversing the DOM of the popup, since each DOM node
   * has a data() referencing the widget instance.
   */
  function findClone(popup, originalMenu) {
    let clone = null;
    popup.$container.find('.menu-item').each(function() {
      let $menuItem = $(this);
      let widget = $menuItem.data('widget');
      if (widget.cloneOf === originalMenu) {
        clone = widget;
      }
    });
    return clone;
  }

  describe('options.cloneMenuItems', () => {
    let popup, menu, childMenu;

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
      menu2.visible = false;
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
      menu2.visible = false;
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

});
