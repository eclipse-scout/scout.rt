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
/* global FormSpecHelper, scout.MenuSpecHelper */
describe("ValueField", function() {
  var session, helper, menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    menuHelper = new scout.MenuSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  describe("property status visible", function() {
    var formField, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      formField = new scout.ValueField();
      formField._render = function($parent) {
        this.addContainer($parent, 'form-field');
        this.addField($('<div>'));
        this.addStatus();
      };
      formField.init(model);
    });

    it("shows a status even though status visible is false but there are visible menus", function() {
      formField.statusVisible = false;
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      formField.menus = [menu0];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);
      expect(formField.$status.isVisible()).toBe(true);

      var event = createPropertyChangeEvent(formField, {
        menusVisible: false
      });
      formField.onModelPropertyChange(event);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe("menu visibility", function() {
    var formField, model;

    beforeEach(function() {
      jasmine.Ajax.install();
      jasmine.clock().install();
      model = helper.createFieldModel();
      formField = new scout.ValueField();
      formField._render = function($parent) {
        this.addContainer($parent, 'form-field');
        this.addField($('<input>'));
        this.addStatus();
      };
      formField.init(model);
    });

    afterEach(function() {
      // Close context menus
      removePopups(session);
    });

    it("context menu only shows visible menus", function() {
      var menuModel1 = menuHelper.createModel('menu'),
        menu1 = menuHelper.createMenu(menuModel1),
        menuModel2 = menuHelper.createModel('menu'),
        menu2 = menuHelper.createMenu(menuModel2);
      menu2.visible = false;
      formField.menus = [menu1, menu2];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);

      formField.$status.triggerContextMenu();
      sendQueuedAjaxCalls();

      var $menu = $('body').find('.popup-body');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });

    it("context menu only shows only menus of specific type", function() {
      var menuModel1 = menuHelper.createModel('menu'),
        menu1 = menuHelper.createMenu(menuModel1),
        menuModel2 = menuHelper.createModel('menu'),
        menu2 = menuHelper.createMenu(menuModel2);
      menu1.menuTypes = ['ValueField.Null', 'ValueField.NotNull'];
      menu2.menuTypes = ['ValueField.Null'];
      formField.menus = [menu1, menu2];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);

      formField.currentMenuTypes = ['Null'];
      formField.$status.triggerContextMenu();

      var $menu = $('body').find('.popup-body');
      expect($menu.find('.menu-item').length).toBe(2);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
      expect($menu.find('.menu-item').eq(1).isVisible()).toBe(true);

      // close menu
      formField.$status.triggerContextMenu();

      // open again and change current menu types
      formField.displayText = 'abc';
      formField.currentMenuTypes = ['NotNull'];
      formField.$status.triggerContextMenu();

      // trigger acceptInput request (menu won't be shown before request is not finished)
      jasmine.clock().tick(0);
      sendQueuedAjaxCalls();
      // Simulate request response was processed...
      session._requestsPendingCounter--;
      session._fireRequestFinished(mostRecentJsonRequest());

      $menu = $('body').find('.popup-body');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });

    it("context menu triggers a display text changed event", function() {
      var menuModel1 = menuHelper.createModel('menu'),
        menu1 = menuHelper.createMenu(menuModel1);
      menu1.menuTypes = ['ValueField.Null', 'ValueField.NotNull'];
      formField.menus = [menu1];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);

      formField.$field.focus();
      formField.$field.val('abc123contextmenu');
      formField.$status.triggerContextMenu();
      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);

      var event = new scout.Event(formField.id, 'displayTextChanged', {
        displayText: 'abc123contextmenu', whileTyping: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

});
