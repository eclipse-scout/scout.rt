/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, ComboMenu, EllipsisMenu, FormFieldMenu, Menu, MenuBar, scout, Session} from '../../src/index';

describe('ComboMenu', () => {

  let session: Session, $sandbox: JQuery;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
  });

  function createComboMenu(): ComboMenu {
    return scout.create(ComboMenu, {
      parent: session.desktop,
      childActions: [{
        id: 'ComboMenuChild1',
        objectType: Menu,
        text: 'Combo menu'
      }, {
        id: 'ComboMenuChild2',
        objectType: Menu,
        subMenuVisibility: Menu.SubMenuVisibility.ALWAYS,
        childActions: [{
          objectType: Menu,
          text: 'Child menu 1'
        }, {
          objectType: Menu,
          text: 'Child menu 2'
        }]
      }]
    });
  }

  describe('doAction', () => {
    it('does not toggle sub menu', () => {
      let comboMenu = createComboMenu();
      expect(comboMenu.isToggleAction()).toBe(false);

      let actionCalled = false;
      let selectedCalled = false;
      comboMenu.on('action', () => {
        actionCalled = true;
      });
      comboMenu.on('propertyChange:selected', () => {
        selectedCalled = false;
      });
      comboMenu.doAction();
      expect(actionCalled).toBe(true);
      expect(selectedCalled).toBe(false);
      expect(comboMenu.popup).toBe(null);
    });
  });

  describe('isTabTarget', () => {
    it('returns false for children if combo menu is invisible', () => {
      let comboMenu = createComboMenu();
      comboMenu.render();
      expect(comboMenu.childActions[0].isTabTarget()).toBeTrue();
      expect(comboMenu.childActions[1].isTabTarget()).toBeTrue();

      comboMenu.setVisible(false);
      expect(comboMenu.childActions[0].isTabTarget()).toBeFalse();
      expect(comboMenu.childActions[1].isTabTarget()).toBeFalse();

      comboMenu.setVisible(true);
      expect(comboMenu.childActions[0].isTabTarget()).toBeTrue();
      expect(comboMenu.childActions[1].isTabTarget()).toBeTrue();
    });

    it('returns false for children if combo menu is in ellipsis', () => {
      let ellipsis = scout.create(EllipsisMenu, {
        parent: session.desktop
      });
      ellipsis.render();
      let comboMenu = createComboMenu();
      comboMenu.render();
      expect(comboMenu.childActions[0].isTabTarget()).toBeTrue();
      expect(comboMenu.childActions[1].isTabTarget()).toBeTrue();

      comboMenu._setOverflown(true);
      ellipsis.setChildActions([comboMenu]);
      expect(comboMenu.childActions[0].isTabTarget()).toBeFalse();
      expect(comboMenu.childActions[1].isTabTarget()).toBeFalse();

      comboMenu._setOverflown(false);
      ellipsis.setChildActions([]);
      expect(comboMenu.childActions[0].isTabTarget()).toBeTrue();
      expect(comboMenu.childActions[1].isTabTarget()).toBeTrue();
    });
  });

  describe('tabbable', () => {
    it('is set on first child action to true if combo menu is the first menu in the menu bar', () => {
      let menuBar = scout.create(MenuBar, {
        parent: session.desktop
      });
      let menu1 = createComboMenu();
      let menu2 = scout.create(Menu, {parent: session.desktop, text: 'Menu2'});
      menuBar.setMenuItems([menu1, menu2]);
      menuBar.render();

      expect(menu1.tabbable).toBe(false);
      expect(menu1.$container.attr('tabindex')).toBe(undefined);
      expect(menu1.childActions[0].tabbable).toBe(true);
      expect(menu1.childActions[0].$container.attr('tabindex')).toBe('0');
      expect(menu2.tabbable).toBe(false);
      expect(menu2.$container.attr('tabindex')).toBe(undefined);
    });
  });

  describe('ContextMenu', () => {

    it('combo menu sub menu can be opened in context menu, even multiple times', () => {
      let ellipsis = scout.create(EllipsisMenu, {
        parent: session.desktop
      });
      ellipsis.render();
      let comboMenu = createComboMenu();
      expect(comboMenu.childActions[0].isTabTarget()).toBeTrue();
      expect(comboMenu.childActions[1].isTabTarget()).toBeTrue();

      comboMenu._setOverflown(true);
      ellipsis.setHidden(false);
      ellipsis.setChildActions([comboMenu]);
      ellipsis.setSelected(true);
      expect(ellipsis.popup).not.toBe(null);
      expect(session.desktop.getPopups().length).toBe(1);

      comboMenu.childActions[1].setSelected(true);
      expect(session.desktop.getPopups().length).toBe(2);
      session.desktop.getPopups().forEach(popup => {
        popup.animateRemoval = false;
      });

      comboMenu.childActions[1].setSelected(false);
      expect(session.desktop.getPopups().length).toBe(1);

      comboMenu.childActions[1].setSelected(true);
      expect(session.desktop.getPopups().length).toBe(2);
      session.desktop.getPopups().forEach(popup => {
        popup.animateRemoval = false;
      });

      ellipsis.setSelected(false);
      expect(ellipsis.popup).toBe(null);
      expect(session.desktop.getPopups().length).toBe(0);
    });
  });

  describe('preferred size', () => {

    it('considers pref sizes of child layouts', () => {
      let comboMenu = scout.create(ComboMenu, {
        parent: session.desktop,
        childActions: [
          {
            id: 'childMenu1',
            objectType: Menu,
            text: 'Foo'
          },
          {
            id: 'childMenu2',
            objectType: FormFieldMenu,
            field: {
              id: 'childMenu2Button',
              objectType: Button,
              label: 'Button Menu'
            }
          },
          {
            id: 'childMenu3',
            objectType: Menu,
            text: 'Bar',
            childActions: [{
              id: 'childMenu31',
              objectType: Menu,
              text: 'Bar Foo'
            }, {
              id: 'childMenu32',
              objectType: Menu,
              text: 'Bar Bar'
            }]
          }
        ]
      });
      expect(comboMenu.childActions.length).toBe(3);
      expect(comboMenu.childActions[0].childActions.length).toBe(0);
      expect(comboMenu.childActions[1].childActions.length).toBe(0);
      expect(comboMenu.childActions[1]).toBeInstanceOf(FormFieldMenu);
      expect((comboMenu.childActions[1] as FormFieldMenu).field).toBeInstanceOf(Button);
      expect(comboMenu.childActions[2].childActions.length).toBe(2);
      let childMenu1 = comboMenu.widget('childMenu1');
      let childMenu2 = comboMenu.widget('childMenu2');
      let childMenu2Button = comboMenu.widget('childMenu2Button');
      let childMenu3 = comboMenu.widget('childMenu3');
      let childMenu31 = comboMenu.widget('childMenu31');
      let childMenu32 = comboMenu.widget('childMenu32');

      // ----------------------------

      comboMenu.render($sandbox);
      expect(comboMenu.rendered).toBe(true);
      expect(comboMenu.htmlComp.valid).toBe(false);
      expect(childMenu1.rendered).toBe(true);
      expect(childMenu1.htmlComp.valid).toBe(false);
      expect(childMenu2.rendered).toBe(true);
      expect(childMenu2.htmlComp.valid).toBe(false);
      expect(childMenu2Button.rendered).toBe(true);
      expect(childMenu2Button.htmlComp.valid).toBe(false);
      expect(childMenu3.rendered).toBe(true);
      expect(childMenu3.htmlComp.valid).toBe(false);
      expect(childMenu31.rendered).toBe(false);
      expect(childMenu32.rendered).toBe(false);

      let comboMenuPrefSizeSpy = spyOn(comboMenu.htmlComp.layout, 'preferredLayoutSize').and.callThrough();
      let childMenu1PrefSizeSpy = spyOn(childMenu1.htmlComp.layout, 'preferredLayoutSize').and.callThrough();
      let childMenu2PrefSizeSpy = spyOn(childMenu2.htmlComp.layout, 'preferredLayoutSize').and.callThrough();
      let childMenu2ButtonPrefSizeSpy = spyOn(childMenu2Button.htmlComp.layout, 'preferredLayoutSize').and.callThrough();
      let childMenu3PrefSizeSpy = spyOn(childMenu3.htmlComp.layout, 'preferredLayoutSize').and.callThrough();

      comboMenu.htmlComp.prefSize();
      // Computing the prefSize does not validate the layout...
      expect(comboMenu.htmlComp.valid).toBe(false);
      expect(childMenu1.htmlComp.valid).toBe(false);
      expect(childMenu2.htmlComp.valid).toBe(false);
      expect(childMenu2Button.htmlComp.valid).toBe(false);
      expect(childMenu3.htmlComp.valid).toBe(false);
      // ...but each layout should have been asked for its pref size
      expect(comboMenuPrefSizeSpy).toHaveBeenCalled();
      expect(childMenu1PrefSizeSpy).toHaveBeenCalled();
      expect(childMenu2PrefSizeSpy).toHaveBeenCalled();
      expect(childMenu2ButtonPrefSizeSpy).toHaveBeenCalled();
      expect(childMenu3PrefSizeSpy).toHaveBeenCalled();
    });
  });
});
