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
import {Dimension, Popup, scout} from '../../src/index';

describe('PopupWithHead', () => {
  let helper, session, $desktop;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true
      }
    });
    $desktop = session.desktop.$container;
    $('<style>' +
      '.desktop {position: absolute; left: 0; top: 0; width: 220px; height: 220px; background-color: blue;}' +
      '.popup {position: absolute;}' +
      '.popup-head {position: absolute;}' +
      '.popup-body {position: relative;}' +
      '.anchor {position: absolute; left: 70px; top: 70px; width: 80px; height: 80px; background-color: red;}' +
      '.popup-body > .menu-item {display: block; min-height: 30px; min-width: 140px; background-color: grey}' +
      '</style>').appendTo($('#sandbox'));
  });

  let entryPointSizeFunc = () => new Dimension($desktop.width(), $desktop.height());

  afterEach(() => {
    removePopups(session);
  });

  function createMenu(numActions) {
    numActions = scout.nvl(numActions, 2);
    let actions = [];
    for (let i = 0; i < numActions; i++) {
      actions.push({
        objectType: 'Menu',
        label: 'Menu' + i
      });
    }
    return scout.create('Menu', {
      parent: session.desktop,
      cssClass: 'anchor',
      childActions: actions
    });
  }

  describe('verticalAlignment', () => {
    describe('BOTTOM', () => {
      it('opens on the bottom of the anchor', () => {
        let menu = createMenu();
        menu.render();
        let popup = scout.create('MenuBarPopup', {
          parent: session.desktop,
          menu: menu,
          verticalAlignment: Popup.Alignment.BOTTOM,
          windowPaddingY: 10
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70);
        expect(popup.$container.cssHeight()).toBe(60);
      });
    });
  });

  describe('horizontalAlignment', () => {
    describe('LEFTEDGE', () => {
      it('opens on the left edge of the anchor', () => {
        let menu = createMenu();
        menu.render();
        let popup = scout.create('MenuBarPopup', {
          parent: session.desktop,
          menu: menu,
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          windowPaddingX: 10
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70);
        expect(popup.$container.cssWidth()).toBe(140);
      });
    });

    describe('RIGHTEDGE', () => {
      it('opens on the right edge of the anchor', () => {
        let menu = createMenu();
        menu.render();
        let popup = scout.create('MenuBarPopup', {
          parent: session.desktop,
          menu: menu,
          horizontalAlignment: Popup.Alignment.RIGHTEDGE,
          windowPaddingX: 10
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(10);
        expect(popup.$container.cssWidth()).toBe(140);
      });
    });
  });

  describe('verticalSwitch', () => {
    describe('with verticalAlign = bottom', () => {
      it('switches to top when overlapping bottom window border', () => {
        let menu = createMenu();
        menu.render();
        let popup = scout.create('MenuBarPopup', {
          parent: session.desktop,
          menu: menu,
          verticalAlignment: Popup.Alignment.BOTTOM,
          windowPaddingY: 10
        });
        $desktop.cssHeight(220 - 1);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 60);
        expect(popup.$container.cssHeight()).toBe(60);
      });

      it('does not switch but trim if top side is smaller', () => {
        let menu = createMenu();
        menu.render();
        let popup = scout.create('MenuBarPopup', {
          parent: session.desktop,
          menu: menu,
          verticalAlignment: Popup.Alignment.BOTTOM,
          windowPaddingY: 10
        });
        $desktop.cssHeight(220 - 20);
        menu.$container.cssTop(70 - 10);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 10);
        expect(popup.$container.cssHeight()).toBe(50);
      });
    });
  });

  describe('horizontalSwitch', () => {
    describe('with horizontalAlign = leftedge', () => {
      it('switches to rightedge when overlapping left window border', () => {
        let menu = createMenu();
        menu.render();
        let popup = scout.create('MenuBarPopup', {
          parent: session.desktop,
          menu: menu,
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          windowPaddingX: 10
        });
        $desktop.cssWidth(220 - 1);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(10);
        expect(popup.$container.cssWidth()).toBe(140);
      });
    });

    it('does not switch but trim if left side is smaller', () => {
      let menu = createMenu();
      menu.render();
      let popup = scout.create('MenuBarPopup', {
        parent: session.desktop,
        menu: menu,
        horizontalAlignment: Popup.Alignment.LEFTEDGE,
        windowPaddingX: 10
      });
      $desktop.cssWidth(220 - 20);
      menu.$container.cssLeft(70 - 10);
      popup.getWindowSize = entryPointSizeFunc;
      popup.open();
      expect(popup.$container.cssLeft()).toBe(70 - 10);
      expect(popup.$container.cssWidth()).toBe(130);
    });
  });
});
