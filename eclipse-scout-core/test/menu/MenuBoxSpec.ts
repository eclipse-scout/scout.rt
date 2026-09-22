/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {MenuBox, scout} from '../../src/index';
import {MenuSpecHelper} from '../../src/testing/index';

describe('MenuBox', () => {
  let helper: MenuSpecHelper, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new MenuSpecHelper(session);
  });

  function createMenuBox(): MenuBox {
    return scout.create(MenuBox, {
      parent: session.desktop
    });
  }

  describe('tabbable', () => {
    it('is set to the first menu that is a tab target', () => {
      let menuBox = createMenuBox();
      let menu1 = helper.createMenu({text: 'Menu 1'});
      let menu2 = helper.createMenu({text: 'Menu 2'});
      menuBox.setMenus([menu1, menu2]);
      menuBox.render();

      expect(menu1.$container.attr('tabindex')).toBe('0');
      expect(menu2.$container.attr('tabindex')).toBe(undefined);
    });

    it('is set to the default menu that is a tab target', () => {
      let menuBox = createMenuBox();
      let menu1 = helper.createMenu({text: 'Menu 1'});
      let menu2 = helper.createMenu({text: 'Menu 2', defaultMenu: true});
      menuBox.setMenus([menu1, menu2]);
      menuBox.render();

      expect(menu1.$container.attr('tabindex')).toBe(undefined);
      expect(menu2.$container.attr('tabindex')).toBe('0');
    });

    it('is not set to the default menu that is not a tab target', () => {
      let menuBox = createMenuBox();
      let menu1 = helper.createMenu({text: 'Menu 1', defaultMenu: true, visible: false});
      let menu2 = helper.createMenu({text: 'Menu 2'});
      menuBox.setMenus([menu1, menu2]);
      menuBox.render();

      expect(menu1.$container.attr('tabindex')).toBe(undefined);
      expect(menu2.$container.attr('tabindex')).toBe('0');
    });

    it('is set to the first visible default menu that is a tab target', () => {
      let menuBox = createMenuBox();
      let menu1 = helper.createMenu({text: 'Menu 1', defaultMenu: true, visible: false});
      let menu2 = helper.createMenu({text: 'Menu 2'});
      let menu3 = helper.createMenu({text: 'Menu 3', defaultMenu: true});
      menuBox.setMenus([menu1, menu2, menu3]);
      menuBox.render();

      expect(menu1.$container.attr('tabindex')).toBe(undefined);
      expect(menu2.$container.attr('tabindex')).toBe(undefined);
      expect(menu3.$container.attr('tabindex')).toBe('0');
    });
  });
});
