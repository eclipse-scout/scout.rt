/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('SequenceBox', function() {
  var session, helper, menuHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    menuHelper = new scout.MenuSpecHelper(session);
  });

  function createField(modelProperties) {
    var seqBox = helper.createField('SequenceBox', session.desktop, modelProperties);
    var fields = [
      helper.createField('StringField', seqBox, {
        statusVisible: false
      }),
      helper.createField('DateField', seqBox, {
        statusVisible: false
      })
    ];
    seqBox.setProperty('fields', fields);
    return seqBox;
  }

  describe('mandatory indicator', function() {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it('does not exist', function() {
      var field = createField({
        mandatory: true
      });
      field.render();

      expect(field.$mandatory).toBeUndefined();
    });

  });

  describe('label width', function() {

    it('is 0 if it is empty', function() {
      var field = createField();
      field.render();
      // css is not applied, therefore we need to adjust display style here
      field.fields[0].$label.css('display', 'inline-block');
      field.validateLayout();

      expect(field.fields[0].$label.outerWidth(true)).toBe(0);
    });

  });

  describe('status handling', function() {

    it('moves the error status of the last field to the seq box', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();

      field.fields[1].setErrorStatus({
        message: 'foo'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('foo');
    });

    it('moves the tooltip of the last field to the seq box', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.tooltipText).toBeFalsy();
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBeFalsy();

      field.fields[1].setProperty('tooltipText', 'foo');

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBe('foo');
    });

    it('moves the menus of the last field to the seq box', function() {
      var field = createField({
        statusVisible: false
      });
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      field.fields[1].menus = [menu0];
      field.fields[1].menusVisible = false;
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.menus.length).toBe(1);
      expect(field.$container).not.toHaveClass('has-menus');
      expect(field.fields[1].$status.isVisible()).toBe(false);

      field.fields[1].setProperty('menusVisible', true);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.menus.length).toBe(1);
      expect(field.$container).toHaveClass('has-menus');
      expect(field.fields[1].$status.isVisible()).toBe(false);
    });

    it('does not display the error message of the last field, only the one of the seq box', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      expect(field.fields[1]._tooltip()).toBeFalsy();
      expect(field._tooltip()).toBeFalsy();

      field.fields[1].setProperty('errorStatus', {
        message: 'foo'
      });

      expect(field.fields[1]._tooltip()).toBeFalsy();
      expect(field._tooltip().rendered).toBe(true);
    });

    it('removes the tooltip from the seq box if last field gets invisible', function() {
      var field = createField({
        statusVisible: false
      });
      field.fields[1].tooltipText = 'foo';
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBe('foo');

      field.fields[1].setProperty('visible', false);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.tooltipText).toBeFalsy();
      expect(field.fields[1].$container.isVisible()).toBe(false);
      expect(field.fields[1].tooltipText).toBe('foo');
    });

    it('moves the tooltip from the first field to the seq box if it gets the last field after a visibility change', function() {
      var field = createField({
        statusVisible: false
      });
      field.fields[0].tooltipText = 'foo';
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.tooltipText).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltipText).toBe('foo');

      field.fields[1].setProperty('visible', false);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].tooltipText).toBe('foo');
    });

    it('moves the error from the first field to the seq box if it gets the last field after a visibility change', function() {
      var field = createField({
        statusVisible: false
      });
      field.fields[0].errorStatus = new scout.Status({
        message: 'foo'
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field._tooltip()).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0]._tooltip().rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      field.fields[1].setProperty('visible', false);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0]._tooltip()).toBe(null);
      expect(field.fields[0].errorStatus.message).toBe('foo');
    });

    it('makes sure the status may be displayed on the field again if the field was the last visible field once', function() {
      var field = createField({
        statusVisible: false
      });
      field.fields[0].errorStatus = new scout.Status({
        message: 'foo'
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field._tooltip()).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0]._tooltip().rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      field.fields[1].setProperty('visible', false);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0]._tooltip()).toBe(null);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      field.fields[1].setProperty('visible', true);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field._tooltip()).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0]._tooltip().rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');
    });

    it('shows the error status of the seq box', function() {
      var field = createField({
        errorStatus: {
          statusVisible: false,
          message: 'foo'
        }
      });
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
    });

    it('shows the tooltip of the seq box', function() {
      var field = createField({
        statusVisible: false,
        tooltipText: 'foo'
      });
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
    });

    it('renders invisible menus of the seq box', function() {
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      var field = createField({
        statusVisible: false,
        menus: [menu0],
        menusVisible: false
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.menus.length).toBe(1);
      expect(field.$container).not.toHaveClass('has-menus');
    });

    it('renders visible menus of the seq box', function() {
      var menu0 = menuHelper.createMenu(menuHelper.createModel());
      var field = createField({
        statusVisible: false,
        menus: [menu0],
        menusVisible: true
      });
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.menus.length).toBe(1);
      expect(field.$container).toHaveClass('has-menus');
    });

    it('prefers the error status of the last visible field', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      field.setErrorStatus({
        message: 'box-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();

      field.fields[1].setErrorStatus({
        message: 'field-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error');

      field.fields[1].clearErrorStatus();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();
    });

    it('also remembers the new error status of the seq box if the status is currently overwritten by the field', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      field.setErrorStatus({
        message: 'box-error-1'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error-1');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();

      field.fields[1].setErrorStatus({
        message: 'field-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error');

      field.setErrorStatus({
        message: 'box-error-2'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error');

      field.fields[1].clearErrorStatus();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error-2');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();
    });

    it('also remembers the error status of the seq box if the status is overwritten by the field multiple times', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      field.setErrorStatus({
        message: 'box-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();

      field.fields[1].setErrorStatus({
        message: 'field-error-1'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error-1');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error-1');

      field.fields[1].setErrorStatus({
        message: 'field-error-2'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error-2');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error-2');

      field.fields[1].clearErrorStatus();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus).toBeFalsy();
    });

    it('does not change the seq box status if the last visible field is not changed', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      field.setErrorStatus({
        message: 'box-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].errorStatus).toBeFalsy();

      field.fields[0].setErrorStatus({
        message: 'field-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('field-error');

      field.fields[0].clearErrorStatus();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('box-error');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].errorStatus).toBeFalsy();
    });

    it('removes the error status from the sequenceBox after all fields are valid', function() {
      var field = createField({
        statusVisible: false
      });
      field.render();

      field.fields[1].setErrorStatus({
        message: 'field-error-2'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error-2');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error-2');

      field.fields[0].setErrorStatus({
        message: 'field-error'
      });

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('field-error-2');
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('field-error');
      expect(field.fields[1].$status.isVisible()).toBe(false);
      expect(field.fields[1].errorStatus.message).toBe('field-error-2');

      field.fields[1].clearErrorStatus();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('field-error');

      field.fields[0].clearErrorStatus();

      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].errorStatus).toBeFalsy();
      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
    });
  });

  describe('label', function() {

    it('is linked with the fields (also considers fields own label)', function() {
      var field = createField();
      field.setLabel('box label');
      field.fields[0].setLabel('first label');
      field.fields[1].setLabel('second label');
      field.fields[1].setHasTime(true);
      field.render();
      expect(field.fields[0].$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.fields[0].$field.attr('aria-labelledby')).toBe(field.$label.attr('id') + ' ' + field.fields[0].$label.attr('id'));
      expect(field.fields[1].$dateField.attr('aria-labelledby')).toBeTruthy();
      expect(field.fields[1].$dateField.attr('aria-labelledby')).toBe(field.$label.attr('id') + ' ' + field.fields[1].$label.attr('id'));
      expect(field.fields[1].$timeField.attr('aria-labelledby')).toBeTruthy();
      expect(field.fields[1].$timeField.attr('aria-labelledby')).toBe(field.$label.attr('id') + ' ' + field.fields[1].$label.attr('id'));
    });

    it('focuses the first visible field when clicked', function() {
      var field = createField();
      field.render();
      field.setLabel('box label');
      field.$label.triggerClick();
      expect(field.fields[0].$field).toBeFocused();

      field.fields[0].setVisible(false);
      field.$label.triggerClick();
      expect(field.fields[1].$dateField).toBeFocused();
    });

  });

  describe("clone", function() {
    it("considers the clone properties and deep clones fields", function() {
      var cloneHelper = new scout.CloneSpecHelper();
      var seqBox = scout.create('SequenceBox', {
        parent: session.desktop,
        id: 'seq01',
        label: 'abc',
        fields: [{
          objectType: 'StringField',
          labelVisible: false
        }, {
          objectType: 'DateField',
          label: 'a date field'
        }],
        menus: [{
          objectType: 'Menu'
        }]
      });
      var clone = seqBox.clone({
        parent: seqBox.parent
      });

      cloneHelper.validateClone(seqBox, clone);
      expect(clone.fields.length).toBe(2);
      expect(clone.cloneOf).toBe(seqBox);
      expect(clone.label).toBe('abc');
      expect(clone.fields[0].cloneOf).toBe(seqBox.fields[0]);
      expect(clone.fields[0].labelVisible).toBe(false);
      expect(clone.fields[1].cloneOf).toBe(seqBox.fields[1]);
      expect(clone.fields[1].label).toBe('a date field');

      // Assert that logical grid is a new instance
      expect(clone.logicalGrid).not.toBe(seqBox.logicalGrid);
      expect(clone.logicalGrid.gridConfig instanceof scout.SequenceBoxGridConfig).toBe(true);
    });
  });

  describe('focus', function() {
    it('focuses the first field', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'StringField'
        }, {
          objectType: 'StringField'
        }]
      });
      box.render();
      expect(box.fields[0].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[0].$field).toBeFocused();
    });

    it('focuses the second field if the first is disabled', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'StringField',
          enabled: false
        }, {
          objectType: 'StringField',
          enabled: true
        }]
      });
      box.render();
      expect(box.fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[1].$field).toBeFocused();
    });

    it('focuses the second field if the first is not focusable', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'LabelField'
        }, {
          objectType: 'StringField'
        }]
      });
      box.render();
      expect(box.fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[1].$field).toBeFocused();
    });
  });

  describe('autoDate on datefields', function() {
    it('is set on following date fields if the date changes in a date field', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }]
      });
      box.render();
      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[1].autoDate).toBe(null);

      var date = scout.dates.create('2017-05-23 12:30:00.000');

      box.fields[0].setValue(date);

      expect(box.fields[0].value.toISOString()).toBe(date.toISOString());
      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[1].autoDate.toISOString()).toBe(scout.dates.shift(date, 0, 0, 1).toISOString());
    });

    it('is set only on following fields in the sequence box', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }]
      });
      box.render();
      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[1].autoDate).toBe(null);
      expect(box.fields[2].autoDate).toBe(null);
      expect(box.fields[3].autoDate).toBe(null);

      var date = scout.dates.create('2017-05-23 12:30:00.000');

      box.fields[1].setValue(date);

      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[1].autoDate).toBe(null);
      expect(box.fields[1].value.toISOString()).toBe(date.toISOString());
      expect(box.fields[2].autoDate.toISOString()).toBe(scout.dates.shift(date, 0, 0, 1).toISOString());
      expect(box.fields[3].autoDate.toISOString()).toBe(scout.dates.shift(date, 0, 0, 1).toISOString());

      var date2 = scout.dates.create('2017-05-26 12:30:00.000');
      box.fields[2].setValue(date2);

      expect(box.fields[2].value.toISOString()).toBe(date2.toISOString());
      expect(box.fields[3].autoDate.toISOString()).toBe(scout.dates.shift(date2, 0, 0, 1).toISOString());
    });

    it('is correctly removed again after a date field value is removed', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }, {
          objectType: 'DateField'
        }]
      });
      box.render();
      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[1].autoDate).toBe(null);
      expect(box.fields[2].autoDate).toBe(null);
      expect(box.fields[3].autoDate).toBe(null);

      var date = scout.dates.create('2017-05-23 12:30:00.000');
      var date2 = scout.dates.create('2017-05-26 12:30:00.000');

      box.fields[0].setValue(date);
      box.fields[2].setValue(date2);

      expect(box.fields[0].value.toISOString()).toBe(date.toISOString());
      expect(box.fields[1].autoDate.toISOString()).toBe(scout.dates.shift(date, 0, 0, 1).toISOString());
      expect(box.fields[2].value.toISOString()).toBe(date2.toISOString());
      expect(box.fields[3].autoDate.toISOString()).toBe(scout.dates.shift(date2, 0, 0, 1).toISOString());

      box.fields[0].setValue(null);
      expect(box.fields[0].value).toBe(null);
      expect(box.fields[1].autoDate).toBe(null);
      expect(box.fields[2].autoDate).toBe(null);
      // field3.autoDate shouldn't be touched by field0's value change
      expect(box.fields[3].autoDate.toISOString()).toBe(scout.dates.shift(date2, 0, 0, 1).toISOString());
    });

    it('is correctly set within sequence boxes containing other fields as well', function() {
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'DateField'
        }, {
          objectType: 'StringField'
        }, {
          objectType: 'DateField'
        }, {
          objectType: 'StringField'
        }]
      });
      box.render();
      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[2].autoDate).toBe(null);

      var date = scout.dates.create('2017-05-23 12:30:00.000');

      box.fields[0].setValue(date);

      expect(box.fields[0].value.toISOString()).toBe(date.toISOString());
      expect(box.fields[2].autoDate.toISOString()).toBe(scout.dates.shift(date, 0, 0, 1).toISOString());
    });

    it('works correctly with values already set on the datefield model', function() {
      var date = scout.dates.create('2017-05-23 12:30:00.000');
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'DateField',
          value: date
        }, {
          objectType: 'DateField'
        }]
      });
      box.render();
      expect(box.fields[0].autoDate).toBe(null);
      expect(box.fields[1].autoDate.toISOString()).toBe(scout.dates.shift(date, 0, 0, 1).toISOString());
    });

    it('dont conflict with already set/programmed autoDates', function() {
      var date = scout.dates.create('2017-05-23 12:30:00.000');
      var date2 = scout.dates.create('2017-05-27 12:30:00.000');
      var box = scout.create('SequenceBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'DateField',
          autoDate: date
        }, {
          objectType: 'DateField',
          autoDate: date
        }]
      });
      box.render();
      box.fields[0].setValue(date2);
      expect(box.fields[0].autoDate.toISOString()).toBe(date.toISOString());
      expect(box.fields[1].autoDate.toISOString()).toBe(date.toISOString());
    });
  });
});
