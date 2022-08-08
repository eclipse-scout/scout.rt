/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, ParsingFailedStatus, scout, Status, StringField, ValueField} from '../../../src/index';
import {FormSpecHelper, MenuSpecHelper} from '../../../src/testing/index';

/* global removePopups */
describe('ValueField', () => {
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

  describe('property status visible', () => {
    let formField, model;

    beforeEach(() => {
      model = helper.createFieldModel();
      formField = new ValueField();
      formField._render = function() {
        this.addContainer(this.$parent, 'form-field');
        this.addField($('<div>'));
        this.addStatus();
      };
      formField.init(model);
    });

    it('shows a status even though status visible is false but there are visible menus', () => {
      formField.statusVisible = false;
      let menu0 = menuHelper.createMenu(menuHelper.createModel());
      formField.menus = [menu0];
      formField.menusVisible = true;
      formField.render();
      expect(formField.$status.isVisible()).toBe(true);

      formField.setProperty('menusVisible', false);
      expect(formField.$status.isVisible()).toBe(false);
    });

  });

  describe('init', () => {

    it('sets display text using formatValue if value is set initially', () => {
      let field = helper.createField('StringField', session.desktop, {
        value: 'Foo'
      });
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');
      expect(field.empty).toBe(false);
    });

    it('does not override display text using formatValue if display text is set initially', () => {
      // Don't parse the value, this is actually the same as one would call setDisplayText after the init
      let field = helper.createField('StringField', session.desktop, {
        displayText: 'Bar'
      });
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('Bar');
      expect(field.empty).toBe(true);
    });

    it('does not override display text using formatValue if display text is set initially even if value is set as well', () => {
      // Don't override display text, otherwise specifying the display text would not have any effect
      let field = helper.createField('StringField', session.desktop, {
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

    it('does not set value if value is invalid initially', () => {
      let field = new StringField();
      field.setValidator(value => {
        throw 'Validation failed';
      });
      field.init({
        parent: session.desktop,
        value: 'Foo'
      });
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.value).toBe(null);
      expect(field.empty).toBe(true);
    });

    it('does not override the errorStatus if an errorStatus is set initially', () => {
      // Mainly needed for page reload case with scout classic, but may be useful for scout JS too
      let field = helper.createField('StringField', session.desktop, {
        errorStatus: {
          message: 'initial error status'
        }
      });
      expect(field.errorStatus.message).toBe('initial error status');
      expect(field.empty).toBe(true);
    });

    it('does not override the errorStatus if an errorStatus set initially even if an invalid value is set', () => {
      // Don't override error status, otherwise specifying the error status would not have any effect
      let field = new StringField();
      field.setValidator(value => {
        throw 'Validation failed';
      });
      field.init({
        parent: session.desktop,
        value: 'Foo',
        errorStatus: {
          children: [{
            message: 'initial error status'
          }]
        }
      });
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.errorStatus.children.length).toEqual(2);
      expect(findInitialError(field).message).toEqual('initial error status');

      // Same thing should happen when setValue is called
      field.setValue('ABC');
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.errorStatus.children.length).toEqual(2);
      expect(findInitialError(field).message).toEqual('initial error status');

      // calling setErrorStatus error status may be set explicitly independent of the value
      field.setErrorStatus(Status.error({
        message: 'another error'
      }));
      expect(field.errorStatus.message).toBe('another error');

      function findInitialError(field) {
        return arrays.find(field.errorStatus.children, status => {
          return !(status instanceof ParsingFailedStatus);
        });
      }
    });

    it('calls validate and format when value is set initially', () => {
      let field = new StringField();
      field.setValidator(value => {
        return (value === 'gelb' ? 'rot' : value);
      });
      field.setFormatter(value => {
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

  describe('setValue', () => {

    it('sets the value, formats it and sets the display text', () => {
      let field = helper.createField('StringField');
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

    it('does not set the value but the error status and display text if the validation fails', () => {
      let field = helper.createField('StringField');
      field.setValidator(value => {
        throw new Error('Validation failed');
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);
      expect(field.displayText).toBe('Foo');
    });

    it('deletes the error status if value is valid', () => {
      let field = helper.createField('StringField');
      field.setValidator(value => {
        throw new Error('Validation failed');
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setValidator(value => {
        return value;
      });
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
    });

    it('does not fire a property change if the value has not changed', () => {
      let field = helper.createField('StringField');
      let count = 0;
      field.on('propertyChange', event => {
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

    it('executes every validator when validating the value', () => {
      let field = helper.createField('StringField');
      field.addValidator(value => {
        if (value === 'hi') {
          throw 'Hi is not allowed';
        }
        return value;
      }, false);
      field.addValidator(value => {
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

    it('converts undefined to null', () => {
      // Allowing undefined would break the equals checks in ValueField.js
      let field = helper.createField('StringField');
      field.setValue(undefined);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

  });

  describe('_validateValue', () => {

    it('may throw an error if value is invalid', () => {
      let field = helper.createField('StringField');
      field.setValidator(value => {
        throw new Error('an error');
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('[undefined text: InvalidValueMessageX]');
    });

    it('may throw a ParsingFailedStatus if value is invalid', () => {
      let field = helper.createField('StringField');
      field.setValidator(value => {
        throw ParsingFailedStatus.error({
          message: 'Custom message'
        });
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('Custom message');
    });

    it('may throw a message if value is invalid', () => {
      let field = helper.createField('StringField');
      field.setValidator(value => {
        throw 'Invalid value';
      });
      field.setValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus.message).toBe('Invalid value');
    });

  });

  describe('parseAndSetValue', () => {

    it('parses and sets the value', () => {
      let field = helper.createField('StringField');
      field.parseAndSetValue('Foo');
      expect(field.displayText).toBe('Foo');
      expect(field.value).toBe('Foo');
    });

    it('does not set the value but the error status if the parsing fails', () => {
      let field = helper.createField('StringField');
      field.setParser(text => {
        throw new Error('Parsing failed');
      });
      field.parseAndSetValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);
    });

    it('deletes the error status if parsing succeeds', () => {
      let field = helper.createField('StringField');
      field.setParser(value => {
        throw new Error('Validation failed');
      });
      field.parseAndSetValue('Foo');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setParser(value => {
        return value;
      });
      field.parseAndSetValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('acceptInput', () => {

    it('accepts the current display text by calling parse, validate and format', () => {
      let field = helper.createField('StringField');
      field.setParser(displayText => {
        return (displayText === 'blau' ? 'gelb' : displayText);
      });
      field.setValidator(value => {
        return (value === 'gelb' ? 'rot' : value);
      });
      field.setFormatter(value => {
        return (value === 'rot' ? 'lila' : value);
      });
      field._readDisplayText = () => 'blau';
      field.acceptInput();
      // 'blau' -> (parse) 'gelb' -> (validate) 'rot'
      expect(field.value).toBe('rot');
      // 'blau' -> (parse) 'gelb' -> (validate) 'rot' -> (format) 'lila'
      expect(field.displayText).toBe('lila');
    });

    it('is triggered when input is accepted', () => {
      let field = helper.createField('StringField');
      let displayText;
      field.render();
      field.on('acceptInput', event => {
        displayText = event.displayText;
      });
      field.$field.val('a value');
      field.acceptInput();
      expect(displayText).toBe('a value');
    });

    it('contains the actual displayText even if it was changed using format value', () => {
      let field = helper.createField('StringField');
      field.render();
      field.setFormatter(value => {
        return 'formatted value';
      });

      let displayText;
      field.on('acceptInput', event => {
        displayText = event.displayText;
      });
      field.$field.val('a value');
      field.acceptInput();
      expect(displayText).toBe('formatted value');
    });

    it('updates the display text even if it was changed using parse value', () => {
      let field = helper.createField('StringField');
      field.setParser(text => {
        if (text === 'Error') {
          throw new Error();
        } else if (text === 'Foo') {
          return 'Bar';
        }
        return 'Text';
      });
      field.render();
      field.$field.val('Foo');
      field.acceptInput();
      expect(field.displayText).toBe('Bar');
      expect(field.$field.val()).toBe('Bar');
      expect(field.value).toBe('Bar');

      // Value stays unchanged if input is invalid
      field.$field.val('Error');
      field.acceptInput();
      expect(field.displayText).toBe('Error');
      expect(field.$field.val()).toBe('Error');
      expect(field.value).toBe('Bar');
      expect(field.errorStatus instanceof Status).toBe(true);

      // Revert back to valid value -> display text has to be updated as well even though value was not changed
      field.$field.val('Foo');
      field.acceptInput();
      expect(field.displayText).toBe('Bar');
      expect(field.$field.val()).toBe('Bar');
      expect(field.value).toBe('Bar');
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('validator', () => {

    it('may be set initially', () => {
      let field = scout.create(StringField, {
        parent: session.desktop,
        value: 'hi',
        validator: (value, defaultValidator) => {
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

  describe('addValidator', () => {

    it('adds a validator and revalidates the value', () => {
      let field = helper.createField('StringField');
      field.setValue('hi');
      field.addValidator(value => {
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

  describe('removeValidator', () => {
    let validator = value => {
      if (value === 'hi') {
        throw 'Hi is not allowed';
      }
      return value;
    };

    it('removes a validator and revalidates the value', () => {
      let field = helper.createField('StringField');
      field.setValue('hi');
      field.addValidator(validator);
      expect(field.errorStatus.message).toBe('Hi is not allowed');
      field.removeValidator(validator);
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('setValidator', () => {

    it('removes every validator and sets the new one', () => {
      let field = helper.createField('StringField');
      expect(field.validators.length).toBe(1);
      field.setValidator((value, defaultValidator) => {
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

  describe('setValidators', () => {

    it('replaces the list of validators with the given ones', () => {
      let field = helper.createField('StringField');
      field.setValidators([value => {
        if (value === 'hi') {
          throw 'Hi is not allowed';
        }
        return value;
      }, value => {
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

  describe('parser', () => {
    it('may be set initially', () => {
      let field = scout.create(StringField, {
        parent: session.desktop,
        parser: (displayText, defaultParser) => {
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

  describe('setParser', () => {

    it('replaces the existing parser by a new one and parses the display text again', () => {
      let field = helper.createField('StringField');
      field.setValue('1234-1234-1234-1234');
      expect(field.displayText).toBe('1234-1234-1234-1234');
      expect(field.value).toBe('1234-1234-1234-1234');

      field.setParser((displayText, defaultParser) => {
        if (displayText) {
          return displayText.replace(/-/g, '');
        }
        return defaultParser(displayText);
      });
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234123412341234');
    });

  });

  describe('formatter', () => {
    it('may be set initially', () => {
      let field = scout.create(StringField, {
        parent: session.desktop,
        value: '1234123412341234',
        formatter: (value, defaultFormatter) => {
          let displayText = defaultFormatter(value);
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

  describe('setFormatter', () => {

    it('replaces the existing formatter by a new one and formats the value again', () => {
      let field = helper.createField('StringField');
      field.setValue('1234123412341234');
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234123412341234');

      field.setFormatter((value, defaultFormatter) => {
        let displayText = defaultFormatter(value);
        if (!displayText) {
          return displayText;
        }
        return displayText.match(/.{4}/g).join('-');
      });
      expect(field.value).toBe('1234123412341234');
      expect(field.displayText).toBe('1234-1234-1234-1234');
    });

  });

  describe('clear', () => {

    it('removes the text and accepts input', () => {
      // Not quite the same as ctrl-a del, but it is easier to handle.
      // E.g. DateField uses displayText to mark the day when the popup opens. If display text is not set a day might be selected even though input was cleared.
      let field = helper.createField('StringField');
      let inputAccepted = false;
      field.render();
      field.setValue('abc');
      field.on('acceptInput', () => {
        inputAccepted = true;
      });
      expect(field.$field.val()).toBe('abc');
      expect(field.value).toBe('abc');
      expect(field.displayText).toBe('abc');

      field.clear();
      expect(field.$field.val()).toBe('');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(inputAccepted).toBe(true);
    });

  });

  describe('validation: initialValue, touched, empty and mandatory', () => {

    let field;

    beforeEach(() => {
      field = helper.createField('StringField');
    });

    it('sets initialValue when markAsSaved is called', () => {
      field.setValue('Foo');
      expect(field.initialValue).toBeFalsy();
      field.markAsSaved();
      expect(field.initialValue).toBe('Foo');
      expect(field.touched).toBe(false);
    });

    it('sets touched to true when value is different from initial value', () => {
      field.setValue('Foo');
      field.markAsSaved();
      expect(field.touched).toBe(false);
      field.setValue('Bar');
      expect(field.touched).toBe(true);
      field.setValue('Foo');
      expect(field.touched).toBe(false);
    });

    it('sets empty to true when value is an empty string (for StringField)', () => {
      field.setValue(null);
      expect(field.empty).toBe(true);
      field.setValue('Foo');
      expect(field.empty).toBe(false);
      field.setValue(null);
      expect(field.empty).toBe(true);
    });

    it('validate returns valid when errorStatus is not set and field is not mandatory', () => {
      field.setValue(null);
      field.setErrorStatus(null);
      field.setMandatory(false);
      let result = field.getValidationResult();
      expect(result.valid).toBe(true);
    });

    it('validate returns not valid when errorStatus is set or field is mandatory and empty', () => {
      let errorStatus = new Status({
        severity: Status.Severity.ERROR
      });
      field.setErrorStatus(errorStatus);
      let result = field.getValidationResult();
      expect(result.valid).toBe(false);
      expect(result.validByErrorStatus).toBe(false);

      field.setErrorStatus(null);
      field.setMandatory(true);
      result = field.getValidationResult();
      expect(result.valid).toBe(false);
      expect(result.validByMandatory).toBe(false);
    });

  });

  describe('menu visibility', () => {
    let formField, model;

    beforeEach(() => {
      model = helper.createFieldModel();
      formField = new ValueField();
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

    afterEach(() => {
      // Close context menus
      removePopups(session);
    });

    it('context menu only shows visible menus', () => {
      let menuModel1 = menuHelper.createModel('menu'),
        menu1 = menuHelper.createMenu(menuModel1),
        menuModel2 = menuHelper.createModel('menu'),
        menu2 = menuHelper.createMenu(menuModel2);
      menu2.visible = false;
      formField.menus = [menu1, menu2];
      formField.render();

      formField.$status.triggerContextMenu();

      let $menu = $('body').find('.context-menu');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });

    it('context menu only shows menus of specific type', () => {
      let menuModel1 = menuHelper.createModel('menu'),
        menu1 = menuHelper.createMenu(menuModel1),
        menuModel2 = menuHelper.createModel('menu'),
        menu2 = menuHelper.createMenu(menuModel2);
      menu1.menuTypes = ['ValueField.Null', 'ValueField.NotNull'];
      menu2.menuTypes = ['ValueField.Null'];
      formField.setMenus([menu1, menu2]);
      formField.render();

      formField.setCurrentMenuTypes(['ValueField.Null']);
      formField.fieldStatus.showContextMenu();

      let $menu = $('body').find('.context-menu');
      expect($menu.find('.menu-item').length).toBe(2);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
      expect($menu.find('.menu-item').eq(1).isVisible()).toBe(true);

      formField.fieldStatus.hideContextMenu();

      // open again and change current menu types
      formField.setValue('abc');
      formField.setCurrentMenuTypes(['ValueField.NotNull']);
      formField.fieldStatus.showContextMenu();

      $menu = $('body').find('.context-menu');
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });
  });
});
