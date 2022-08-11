/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ImageField} from '../../../../src/index';
import {FormSpecHelper, MenuSpecHelper} from '../../../../src/testing/index';

/* global removePopups */
describe('ImageField', () => {
  let session, helper, menuHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    menuHelper = new MenuSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  describe('menu visibility', () => {
    let imageField;

    beforeEach(() => {
      imageField = helper.createField(ImageField);
    });

    afterEach(() => {
      // Close context menus
      removePopups(session);
    });

    it('context menu only shows menus of specific type', () => {
      let menu1 = menuHelper.createMenu(menuHelper.createModel('menu', null, [ImageField.MenuTypes.Null, ImageField.MenuTypes.ImageUrl])),
        menu2 = menuHelper.createMenu(menuHelper.createModel('menu', null, [ImageField.MenuTypes.Null]));
      imageField.setMenus([menu1, menu2]);
      imageField.render();

      imageField.fieldStatus.showContextMenu();

      let $menu = $('body').find('.context-menu');
      expect($menu.find('.menu-item').length).toBe(2);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
      expect($menu.find('.menu-item').eq(1).isVisible()).toBe(true);

      imageField.fieldStatus.hideContextMenu();

      // open again and change current menu types
      imageField.setImageUrl('abc');
      imageField.fieldStatus.showContextMenu();

      $menu = $('body').find('.context-menu');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });
  });
});
