/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Menu, scout} from '../../src/index';

describe('ComboMenu', () => {

  let session, $sandbox;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
  });

  function createComboMenu() {
    return scout.create('ComboMenu', {
      parent: session.desktop,
      childActions: [{
        id: 'ComboMenuChild1',
        objectType: 'Menu',
        text: 'Combo menu'
      }, {
        id: 'ComboMenuChild2',
        objectType: 'Menu',
        subMenuVisibility: Menu.SubMenuVisibility.ALWAYS,
        childActions: [{
          objectType: 'Menu',
          text: 'Child menu 1'
        }, {
          objectType: 'Menu',
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
      comboMenu.on('action', () => actionCalled = true);
      comboMenu.on('propertyChange:selected', () => selectedCalled = false);
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
      let ellipsis = scout.create('EllipsisMenu', {
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

  describe('ContextMenu', () => {

    it('combo menu sub menu can be opened in context menu, even multiple times', () => {
      let ellipsis = scout.create('EllipsisMenu', {
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
      session.desktop.getPopups().forEach(popup => popup.animateRemoval = false);

      comboMenu.childActions[1].setSelected(false);
      expect(session.desktop.getPopups().length).toBe(1);

      comboMenu.childActions[1].setSelected(true);
      expect(session.desktop.getPopups().length).toBe(2);
      session.desktop.getPopups().forEach(popup => popup.animateRemoval = false);

      ellipsis.setSelected(false);
      expect(ellipsis.popup).toBe(null);
      expect(session.desktop.getPopups().length).toBe(0);
    });
  });
});
