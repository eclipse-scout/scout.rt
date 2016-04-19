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
/* global MenuSpecHelper */
describe("ContextMenuSpec", function() {
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.MenuSpecHelper(session);
  });

  describe('renderMenuItems', function() {
    var popup, menu0, menu1, menu2;

    beforeEach(function() {
      menu0 = helper.createMenu(helper.createModel()),
      menu1 = helper.createMenu(helper.createModel()),
      menu2 = helper.createMenu(helper.createModel());
    });

    it('renders invisible menus', function() {
      var menuItems = [menu0, menu1, menu2];
      menu2.visible = false;
      popup = scout.create('ContextMenuPopup', {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      var menu0Clone = session.getAdapterClones(menu0)[0];
      var menu1Clone = session.getAdapterClones(menu1)[0];
      var menu2Clone = session.getAdapterClones(menu2)[0];
      expect(menu0Clone.$container.isVisible()).toBe(true);
      expect(menu1Clone.$container.isVisible()).toBe(true);
      expect(menu2Clone.$container.isVisible()).toBe(false);
      popup.remove();
    });

    it('adds last and first classes', function() {
      var menuItems = [menu0, menu1, menu2];
      popup = scout.create('ContextMenuPopup', {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      var menu0Clone = session.getAdapterClones(menu0)[0];
      var menu1Clone = session.getAdapterClones(menu1)[0];
      var menu2Clone = session.getAdapterClones(menu2)[0];
      expect(menu0Clone.$container).toHaveClass('context-menu-item-first');
      expect(menu2Clone.$container).toHaveClass('context-menu-item-last');
      popup.remove();
    });


    it('considers visibility when adding last and first classes', function() {
      var menuItems = [menu0, menu1, menu2];
      menu2.visible = false;
      popup = scout.create('ContextMenuPopup', {
        parent: session.desktop,
        session: session,
        menuItems: menuItems
      });
      popup.render();

      var menu0Clone = session.getAdapterClones(menu0)[0];
      var menu1Clone = session.getAdapterClones(menu1)[0];
      var menu2Clone = session.getAdapterClones(menu2)[0];
      expect(menu0Clone.$container).toHaveClass('context-menu-item-first');
      expect(menu1Clone.$container).toHaveClass('context-menu-item-last');
      expect(menu2Clone.$container).not.toHaveClass('context-menu-item-last');
      popup.remove();
    });

  });

});
