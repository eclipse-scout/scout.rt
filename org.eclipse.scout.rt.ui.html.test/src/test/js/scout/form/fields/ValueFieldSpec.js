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
/* global removePopups */
describe('ValueField', function() {
  var session, helper, menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    menuHelper = new scout.MenuSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  describe('property status visible', function() {
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

    it('shows a status even though status visible is false but there are visible menus', function() {
      formField.statusVisible = false;
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      formField.menus = [menu0];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);
      expect(formField.$status.isVisible()).toBe(true);

      formField.setProperty('menusVisible', false);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe('value and display-text', function() {

    var field;

    beforeEach(function() {
      field = helper.createField('StringField', session.desktop);
    });

    it('sets display-text when value is set', function() {
      field.setValue('Foo');
      expect(field.displayText).toBe('Foo');
      field.setValue(null);
      expect(field.displayText).toBe('');
    });

    it('sets value when _parseAndSetValue is called', function() {
      field._parseAndSetValue('Foo');
      expect(field.displayText).toBe('Foo');
      expect(field.value).toBe('Foo');
    });

    it('sets value and display text when accept input is called', function() {
      field._parseValue = function(displayText) {
        return (displayText === 'blau' ? 'gelb' : displayText);
      };
      field._validateValue = function(value) {
        return (value === 'gelb' ? 'rot' : value);
      };
      field._formatValue = function(value) {
        return (value === 'rot' ? 'lila' : value);
      };
      field._readDisplayText = function() {
        return 'blau';
      };
      field.acceptInput();
      // 'blau' -> (parse) 'gelb' -> (validate) 'rot'
      expect(field.value).toBe('rot');
      // 'blau' -> (parse) 'gelb' -> (validate) 'rot' -> (format) 'lila'
      expect(field.displayText).toBe('lila');
    });

  });

  describe('validation: initialValue, touched, empty and mandatory', function() {

    var field;

    beforeEach(function() {
      field = helper.createField('StringField', session.desktop);
    });

    it('sets _initialValue when markAsSaved is called', function() {
      field.setValue('Foo');
      expect(field._initialValue).toBeFalsy();
      field.markAsSaved();
      expect(field._initialValue).toBe('Foo');
      expect(field.touched).toBe(false);
    });

    it('sets touched to true when value is different from initial value', function() {
      field.setValue('Foo');
      field.markAsSaved();
      expect(field.touched).toBe(false);
      field.setValue('Bar');
      expect(field.touched).toBe(true);
      field.setValue('Foo');
      expect(field.touched).toBe(false);
    });

    it('sets empty to true when value is an empty string (for StringField)', function() {
      field.setValue(null);
      expect(field.empty).toBe(true);
      field.setValue('Foo');
      expect(field.empty).toBe(false);
      field.setValue(null);
      expect(field.empty).toBe(true);
    });

    it('validate returns valid when errorStatus is not set and field is not mandatory', function() {
      field.setValue(null);
      field.setErrorStatus(null);
      field.setMandatory(false);
      var status = field.validate();
      expect(status.valid).toBe(true);
    });

    it('validate returns not valid when errorStatus is set or field is mandatory and empty', function() {
      var errorStatus = new scout.Status({severity: scout.Status.Severity.ERROR});
      field.setErrorStatus(errorStatus);
      var status = field.validate();
      expect(status.valid).toBe(false);
      expect(status.validByErrorStatus).toBe(false);

      field.setErrorStatus(null);
      field.setMandatory(true);
      status = field.validate();
      expect(status.valid).toBe(false);
      expect(status.validByMandatory).toBe(false);
    });

  });

  describe('menu visibility', function() {
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

    it('context menu only shows visible menus', function() {
      var menuModel1 = menuHelper.createModel('menu'),
        menu1 = menuHelper.createMenu(menuModel1),
        menuModel2 = menuHelper.createModel('menu'),
        menu2 = menuHelper.createMenu(menuModel2);
      menu2.visible = false;
      formField.menus = [menu1, menu2];
      formField.menusVisible = true;
      formField.render(session.$entryPoint);

      formField.$status.triggerContextMenu();

      var $menu = $('body').find('.popup-body');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });

    it('context menu only shows only menus of specific type', function() {
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

      $menu = $('body').find('.popup-body');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });

    it('context menu triggers a display text changed event', function() {
      linkWidgetAndAdapter(formField, 'ValueFieldAdapter');

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
      event.showBusyIndicator = true;
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

});
