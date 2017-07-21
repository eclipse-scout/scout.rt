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
      field.setValidator(function(value) {
        throw "Validation failed";
      });
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
      field.setValidator(function(value) {
        throw "Validation failed";
      });
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
      field.setValidator(function(value) {
        return (value === 'gelb' ? 'rot' : value);
      });
      field.setFormatter(function(value) {
        return (value === 'rot' ? 'lila' : value);
      });
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
      field.setValidator(function(value) {
        throw new Error('Validation failed');
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);
      expect(field.displayText).toBe("Foo");
    });

    it('deletes the error status if value is valid', function() {
      var field = helper.createField('StringField');
      field.setValidator(function(value) {
        throw new Error('Validation failed');
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);

      field.setValidator(function(value) {
        return value;
      });
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
    });

    it('does not fire a property change if the value has not changed', function() {
      var field = helper.createField('StringField');
      var count = 0;
      field.on('propertyChange', function(event) {
        if (event.propertyName === 'value') {
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

    it('executes every validator when validating the value', function() {
      var field = helper.createField('StringField');
      field.addValidator(function(value) {
        if (value === 'hi') {
          throw 'Hi is not allowed';
        }
        return value;
      }, false);
      field.addValidator(function(value) {
        if (value === 'hello') {
          throw 'Hello is not allowed';
        }
        return value;
      }, false);
      expect(field.errorStatus).toBe(null);
      field.setValue('hi');
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      field.setValue('hello');
      expect(field.errorStatus.message).toBe('Hello is not allowed');
      field.setValue('Good evening');
      expect(field.errorStatus).toBe(null);
    });

    it('converts undefined to null', function() {
      // Allowing undefined would break the equals checks in ValueField.js
      var field = helper.createField('StringField');
      field.setValue(undefined);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

  });

  describe('_validateValue', function() {

    it('may throw an error if value is invalid', function() {
      var field = helper.createField('StringField');
      field.setValidator(function(value) {
        throw new Error('an error');
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('[undefined text: InvalidValueMessageX]');
    });

    it('may throw a scout.Status if value is invalid', function() {
      var field = helper.createField('StringField');
      field.setValidator(function(value) {
        throw scout.Status.error({
          message: 'Custom message'
        });
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('Custom message');
    });

    it('may throw a message if value is invalid', function() {
      var field = helper.createField('StringField');
      field.setValidator(function(value) {
        throw "Invalid value";
      });
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
      field.setParser(function(text) {
        throw new Error('Parsing failed');
      });
      field.parseAndSetValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);
    });

    it('deletes the error status if parsing succeeds', function() {
      var field = helper.createField('StringField');
      field.setParser(function(value) {
        throw new Error('Validation failed');
      });
      field.parseAndSetValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);

      field.setParser(function(value) {
        return value;
      });
      field.parseAndSetValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('acceptInput', function() {

    it('accepts the current display text by calling parse, validate and format', function() {
      var field = helper.createField('StringField');
      field.setParser(function(displayText) {
        return (displayText === 'blau' ? 'gelb' : displayText);
      });
      field.setValidator(function(value) {
        return (value === 'gelb' ? 'rot' : value);
      });
      field.setFormatter(function(value) {
        return (value === 'rot' ? 'lila' : value);
      });
      field._readDisplayText = function() {
        return 'blau';
      };
      field.acceptInput();
      // 'blau' -> (parse) 'gelb' -> (validate) 'rot'
      expect(field.value).toBe('rot');
      // 'blau' -> (parse) 'gelb' -> (validate) 'rot' -> (format) 'lila'
      expect(field.displayText).toBe('lila');
    });

    it('is triggered when input is accepted', function() {
      var field = helper.createField('StringField');
      var displayText;
      field.render();
      field.on('acceptInput', function(event) {
        displayText = event.displayText;
      });
      field.$field.val('a value');
      field.acceptInput();
      expect(displayText).toBe('a value');
    });

    it('contains the actual displayText even if it was changed using format value', function() {
      var field = helper.createField('StringField');
      field.render();
      field.setFormatter(function(value) {
        return 'formatted value';
      });

      var displayText;
      field.on('acceptInput', function(event) {
        displayText = event.displayText;
      });
      field.$field.val('a value');
      field.acceptInput();
      expect(displayText).toBe('formatted value');
    });

  });

  describe('validator', function() {

    it('may be set initially', function() {
      var field = scout.create('StringField', {
        parent: session.desktop,
        value: 'hi',
        validator: function(value, defaultValidator) {
          value = defaultValidator(value);
          if (value === 'hi') {
            throw 'Hi is not allowed';
          }
          return value;
        }
      });
      expect(field.validators.length).toBe(1);
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      expect(field.value).toBe(null);
    });

  });

  describe('addValidator', function() {

    it('adds a validator and revalidates the value', function() {
      var field = helper.createField('StringField');
      field.setValue('hi');
      field.addValidator(function(value) {
        if (value === 'hi') {
          throw 'Hi is not allowed';
        }
        return value;
      });
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      field.setValue('hello');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('removeValidator', function() {
    var validator = function(value) {
      if (value === 'hi') {
        throw 'Hi is not allowed';
      }
      return value;
    };

    it('removes a validator and revalidates the value', function() {
      var field = helper.createField('StringField');
      field.setValue('hi');
      field.addValidator(validator);
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      field.removeValidator(validator);
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('setValidator', function() {

    it('removes every validator and sets the new one', function() {
      var field = helper.createField('StringField');
      expect(field.validators.length).toBe(1);
      field.setValidator(function(value, defaultValidator) {
        value = defaultValidator(value);
        if (value === 'hi') {
          throw 'Hi is not allowed';
        }
        return value;
      });
      expect(field.validators.length).toBe(1);
      expect(field.errorStatus).toBe(null);
      field.setValue('hi');
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      field.setValue('Good evening');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('setValidators', function() {

    it('replaces the list of validators with the given ones', function() {
      var field = helper.createField('StringField');
      field.setValidators([function(value) {
        if (value === 'hi') {
          throw 'Hi is not allowed';
        }
        return value;
      }, function(value) {
        if (value === 'hello') {
          throw 'Hello is not allowed';
        }
        return value;
      }]);
      expect(field.validators.length).toBe(2);
      expect(field.errorStatus).toBe(null);
      field.setValue('hi');
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      field.setValue('hello');
      expect(field.errorStatus.message).toBe('Hello is not allowed');
      field.setValue('Good evening');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('parser', function() {
    it('may be set initially', function() {
      var field = scout.create('StringField', {
        parent: session.desktop,
        parser: function(displayText, defaultParser) {
          if (displayText) {
            return displayText.replace(/-/g, '');
          }
          return defaultParser(displayText);
        }
      });
      field.parseAndSetValue('1234-1234-1234-1234');
      expect(field.value).toBe('1234123412341234');
    });
  });

  describe('setParser', function() {

    it('replaces the existing parser by a new one and parses the display text again', function() {
      var field = helper.createField('StringField');
      field.setValue('1234-1234-1234-1234');
      expect(field.displayText).toBe('1234-1234-1234-1234');
      expect(field.value).toBe('1234-1234-1234-1234');

      field.setParser(function(displayText, defaultParser) {
        if (displayText) {
          return displayText.replace(/-/g, '');
        }
        return defaultParser(displayText);
      });
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234123412341234');
    });

  });

  describe('formatter', function() {
    it('may be set initially', function() {
      var field = scout.create('StringField', {
        parent: session.desktop,
        value: '1234123412341234',
        formatter: function(value, defaultFormatter) {
          var displayText = defaultFormatter(value);
          if (!displayText) {
            return displayText;
          }
          return displayText.match(/.{4}/g).join('-');
        }
      });
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234-1234-1234-1234');
    });
  });

  describe('setFormatter', function() {

    it('replaces the existing formatter by a new one and formats the value again', function() {
      var field = helper.createField('StringField');
      field.setValue('1234123412341234');
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234123412341234');

      field.setFormatter(function(value, defaultFormatter) {
        var displayText = defaultFormatter(value);
        if (!displayText) {
          return displayText;
        }
        return displayText.match(/.{4}/g).join('-');
      });
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234-1234-1234-1234');
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
      var result = field.getValidationResult();
      expect(result.valid).toBe(true);
    });

    it('validate returns not valid when errorStatus is set or field is mandatory and empty', function() {
      var errorStatus = new scout.Status({
        severity: scout.Status.Severity.ERROR
      });
      field.setErrorStatus(errorStatus);
      var result = field.getValidationResult();
      expect(result.valid).toBe(false);
      expect(result.validByErrorStatus).toBe(false);

      field.setErrorStatus(null);
      field.setMandatory(true);
      result = field.getValidationResult();
      expect(result.valid).toBe(false);
      expect(result.validByMandatory).toBe(false);
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

      var event = new scout.RemoteEvent(formField.id, 'acceptInput', {
        displayText: 'abc123contextmenu',
        whileTyping: false
      });
      event.showBusyIndicator = true;
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

  });

});
