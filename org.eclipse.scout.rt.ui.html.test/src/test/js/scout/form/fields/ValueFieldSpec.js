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
      formField._render = function() {
        this.addContainer(this.$parent, 'form-field');
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
      formField.render();
      expect(formField.$status.isVisible()).toBe(true);

      formField.setProperty('menusVisible', false);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe('init', function() {

    it('sets display text using formatValue if value is set initially', function() {
      var field = helper.createField('StringField', session.desktop, {
        value: 'Foo'
      });
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');
      expect(field.empty).toBe(false);
    });

    it('does not override display text using formatValue if display text is set initially', function() {
      // Don't parse the value, this is actually the same as one would call setDisplayText after the init
      var field = helper.createField('StringField', session.desktop, {
        displayText: 'Bar'
      });
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('Bar');
      expect(field.empty).toBe(true);
    });

    it('does not override display text using formatValue if display text is set initially even if value is set as well', function() {
      // Don't override display text, otherwise specifying the display text would not have any effect
      var field = helper.createField('StringField', session.desktop, {
        value: 'Foo',
        displayText: 'ABC'
      });
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('ABC');
      expect(field.empty).toBe(false);

      // The same could be achieved using setValue and setDisplayText
      field = helper.createField('StringField');
      field.setValue('Foo');
      field.setDisplayText('ABC');
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('ABC');
    });

    it('does not set value if value is invalid initially', function() {
      var field = new scout.StringField();
      field._validateValue = function(value) {
        throw "Validation failed";
      };
      field.init({
        parent: session.desktop,
        value: 'Foo'
      });
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.value).toBe(null);
      expect(field.empty).toBe(true);
    });

    it('does not override the errorStatus if an errorStatus is set initially', function() {
      // Mainly needed for page reload case with scout classic, but may be useful for scout JS too
      var field = helper.createField('StringField', session.desktop, {
        errorStatus: {
          message: 'initial error status'
        }
      });
      expect(field.errorStatus.message).toBe('initial error status');
      expect(field.empty).toBe(true);
    });

    it('does not override the errorStatus if an errorStatus set initially even if an invalid value is set', function() {
      // Don't override error status, otherwise specifying the error status would not have any effect
      var field = new scout.StringField();
      field._validateValue = function(value) {
        throw "Validation failed";
      };
      field.init({
        parent: session.desktop,
        value: 'Foo',
        errorStatus: {
          message: 'initial error status'
        }
      });
      expect(field.errorStatus.message).toBe('initial error status');

      // If setValue is called after initialization, error status will be replaced
      field.setValue('ABC');
      expect(field.errorStatus.message).toBe('Validation failed');

      // If calling setErrorStatus error status may be set explicitly independent of the value
      field.setErrorStatus(scout.Status.error({
        message: 'another error'
      }));
      expect(field.errorStatus.message).toBe('another error');
    });

    it('calls validate and format when value is set initially', function() {
      var field = new scout.StringField();
      field._validateValue = function(value) {
        return (value === 'gelb' ? 'rot' : value);
      };
      field._formatValue = function(value) {
        return (value === 'rot' ? 'lila' : value);
      };
      field.init({
        parent: session.desktop,
        value: 'gelb'
      });
      // 'gelb' -> (validate) 'rot'
      expect(field.value).toBe('rot');
      // 'gelb' -> (validate) 'rot' -> (format) 'lila'
      expect(field.displayText).toBe('lila');
    });
  });

  describe('setValue', function() {

    it('sets the value, formats it and sets the display text', function() {
      var field = helper.createField('StringField');
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

    it('does not set the value but the error status and display text if the validation fails', function() {
      var field = helper.createField('StringField');
      field._validateValue = function(value) {
        throw new Error('Validation failed');
      };
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);
      expect(field.displayText).toBe("Foo");
    });

    it('deletes the error status if value is valid', function() {
      var field = helper.createField('StringField');
      field._validateValue = function(value) {
        throw new Error('Validation failed');
      };
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);

      field._validateValue = function(value) {
        return value;
      };
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
    });

    it('does not fire a property change if the value has not changed', function() {
      var field = helper.createField('StringField');
      var count = 0;
      field.on('propertyChange', function(event) {
        if (event.name === 'value') {
          count++;
        }
      });
      field.setValue(' Foo   '); // trim is true
      expect(field.value).toBe('Foo');
      expect(count).toBe(1);

      field.setValue('Foo ');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
      // still one, even if setValue was called with a different value, after validation the value is the same
      expect(count).toBe(1);
    });

  });

  describe('_validateValue', function() {

    it('may throw an error if value is invalid', function() {
      var field = helper.createField('StringField');
      field._validateValue = function(value) {
        throw new Error('an error');
      };
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('[undefined text: InvalidValueMessageX]');
    });

    it('may throw a scout.Status if value is invalid', function() {
      var field = helper.createField('StringField');
      field._validateValue = function(value) {
        throw scout.Status.error({
          message: 'Custom message'
        });
      };
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('Custom message');
    });

    it('may throw a message if value is invalid', function() {
      var field = helper.createField('StringField');
      field._validateValue = function(value) {
        throw "Invalid value";
      };
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('Invalid value');
    });

  });

  describe('parseAndSetValue', function() {

    it('parses and sets the value', function() {
      var field = helper.createField('StringField');
      field.parseAndSetValue('Foo');
      expect(field.displayText).toBe('Foo');
      expect(field.value).toBe('Foo');
    });

    it('does not set the value but the error status if the parsing fails', function() {
      var field = helper.createField('StringField');
      field._parseValue = function(text) {
        throw new Error('Parsing failed');
      };
      field.parseAndSetValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);
    });

    it('deletes the error status if parsing succeeds', function() {
      var field = helper.createField('StringField');
      field._parseValue = function(value) {
        throw new Error('Validation failed');
      };
      field.parseAndSetValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);

      field._parseValue = function(value) {
        return value;
      };
      field.parseAndSetValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('acceptInput', function() {

    it('accepts the current display text by calling parse, validate and format', function() {
      var field = helper.createField('StringField');
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

  describe('displayTextChanged', function() {

    it('is triggered when input is accepted', function() {
      var field = helper.createField('StringField');
      var displayText;
      field.render();
      field.on('displayTextChanged', function(event) {
        displayText = event.displayText;
      });
      field.$field.val('a value');
      field.acceptInput();
      expect(displayText).toBe('a value');
    });

    it('contains the actual displayText even if it was changed using format value', function() {
      var field = helper.createField('StringField');
      field.render();
      field._formatValue = function(value) {
        return 'formatted value';
      };

      var displayText;
      field.on('displayTextChanged', function(event) {
        displayText = event.displayText;
      });
      field.$field.val('a value');
      field.acceptInput();
      expect(displayText).toBe('formatted value');
    });

  });

  describe('validation: initialValue, touched, empty and mandatory', function() {

    var field;

    beforeEach(function() {
      field = helper.createField('StringField');
    });

    it('sets initialValue when markAsSaved is called', function() {
      field.setValue('Foo');
      expect(field.initialValue).toBeFalsy();
      field.markAsSaved();
      expect(field.initialValue).toBe('Foo');
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
      var errorStatus = new scout.Status({
        severity: scout.Status.Severity.ERROR
      });
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
      model = helper.createFieldModel();
      formField = new scout.ValueField();
      formField._render = function() {
        this.addContainer(this.$parent, 'form-field');
        this.addField($('<input>'));
        this.addStatus();
      };
      formField._readDisplayText = function() {
        return this.$field.val();
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
      formField.render();

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
      formField.render();

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
      formField.render();

      formField.$field.focus();
      formField.$field.val('abc123contextmenu');
      formField.$status.triggerContextMenu();
      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);

      var event = new scout.RemoteEvent(formField.id, 'displayTextChanged', {
        displayText: 'abc123contextmenu',
        whileTyping: false
      });
      event.showBusyIndicator = true;
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

});
