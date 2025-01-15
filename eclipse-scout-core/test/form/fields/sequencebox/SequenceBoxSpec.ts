/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, CheckBoxField, DateField, dates, FormField, LabelField, Menu, ModelOf, scout, SequenceBox, SequenceBoxGridConfig, SequenceBoxModel, Status, StringField} from '../../../../src/index';
import {CloneSpecHelper, FormSpecHelper, JQueryTesting, MenuSpecHelper} from '../../../../src/testing/index';

describe('SequenceBox', () => {
  let session: SandboxSession, helper: FormSpecHelper, menuHelper: MenuSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    menuHelper = new MenuSpecHelper(session);
  });

  function createField(modelProperties?: SequenceBoxModel): SequenceBox {
    let seqBox = helper.createField(SequenceBox, session.desktop, modelProperties);
    let fields = [
      helper.createField(StringField, seqBox, {
        statusVisible: false
      }),
      helper.createField(DateField, seqBox, {
        statusVisible: false
      })
    ];
    seqBox.setProperty('fields', fields);
    return seqBox;
  }

  describe('mandatory indicator', () => {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it('does not exist', () => {
      let field = createField({
        mandatory: true
      });
      field.render();

      expect(field.$mandatory).toBeUndefined();
    });

  });

  describe('label width', () => {

    it('is 0 if it is empty', () => {
      let field = createField();
      field.render();
      // css is not applied, therefore we need to adjust display style here
      field.fields[0].$label.css('display', 'inline-block');
      field.validateLayout();

      expect(field.fields[0].$label.outerWidth(true)).toBe(0);
    });

  });

  describe('status handling', () => {

    it('moves the error status of the last field to the seq box', () => {
      let field = createField({
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

    it('suppressStatus of the last field does not change', () => {
      let field = createField({
        statusVisible: false
      });
      field.render();

      let innerField0 = field.fields[0],
        innerField1 = field.fields[1];

      expect(innerField0.suppressStatus).toBeNull();
      innerField0.setSuppressStatus(FormField.SuppressStatus.ALL);
      expect(innerField0.suppressStatus).toBe(FormField.SuppressStatus.ALL);
      innerField0.setSuppressStatus(null);
      expect(innerField0.suppressStatus).toBeNull();

      innerField1.setVisible(false);

      expect(innerField0.suppressStatus).toBe(FormField.SuppressStatus.ICON);
      innerField0.setSuppressStatus(FormField.SuppressStatus.ALL);
      expect(innerField0.suppressStatus).toBe(FormField.SuppressStatus.ICON);
      innerField0.setSuppressStatus(null);
      expect(innerField0.suppressStatus).toBe(FormField.SuppressStatus.ICON);

      innerField1.setVisible(true);

      expect(innerField0.suppressStatus).toBeNull();
      innerField0.setSuppressStatus(FormField.SuppressStatus.ALL);
      expect(innerField0.suppressStatus).toBe(FormField.SuppressStatus.ALL);
      innerField0.setSuppressStatus(null);
      expect(innerField0.suppressStatus).toBeNull();
    });

    it('moves the tooltip of the last field to the seq box', () => {
      let field = createField({
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

    it('moves the menus of the last field to the seq box', () => {
      let field = createField({
        statusVisible: false
      });
      let menu0 = menuHelper.createMenu(menuHelper.createModel());
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

    it('does not display the error message of the last field, only the one of the seq box', () => {
      let field = createField({
        statusVisible: false
      });
      field.render();

      expect(field.fields[1].tooltip()).toBeFalsy();
      expect(field.tooltip()).toBeFalsy();

      field.fields[1].setProperty('errorStatus', {
        message: 'foo'
      });

      expect(field.fields[1].tooltip()).toBeFalsy();
      expect(field.tooltip().rendered).toBe(true);
    });

    it('removes the tooltip from the seq box if last field gets invisible', () => {
      let field = createField({
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

    it('moves the tooltip from the first field to the seq box if it gets the last field after a visibility change', () => {
      let field = createField({
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

    it('moves the error from the first field to the seq box if it gets the last field after a visibility change', () => {
      let field = createField({
        statusVisible: false
      });
      field.fields[0].errorStatus = new Status({
        message: 'foo'
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.tooltip()).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltip().rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      field.fields[1].setProperty('visible', false);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].tooltip()).toBe(null);
      expect(field.fields[0].errorStatus.message).toBe('foo');
    });

    it('makes sure the status may be displayed on the field again if the field was the last visible field once', () => {
      let field = createField({
        statusVisible: false
      });
      field.fields[0].errorStatus = new Status({
        message: 'foo'
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.tooltip()).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltip().rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      field.fields[1].setProperty('visible', false);

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
      expect(field.fields[0].$status.isVisible()).toBe(false);
      expect(field.fields[0].tooltip()).toBe(null);
      expect(field.fields[0].errorStatus.message).toBe('foo');

      field.fields[1].setProperty('visible', true);

      expect(field.$status.isVisible()).toBe(false);
      expect(field.errorStatus).toBeFalsy();
      expect(field.tooltip()).toBeFalsy();
      expect(field.fields[0].$status.isVisible()).toBe(true);
      expect(field.fields[0].tooltip().rendered).toBe(true);
      expect(field.fields[0].errorStatus.message).toBe('foo');
    });

    it('shows the error status of the seq box', () => {
      let field = createField({
        errorStatus: {
          // @ts-expect-error
          statusVisible: false,
          message: 'foo'
        }
      });
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.errorStatus.message).toBe('foo');
    });

    it('shows the tooltip of the seq box', () => {
      let field = createField({
        statusVisible: false,
        tooltipText: 'foo'
      });
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.tooltipText).toBe('foo');
    });

    it('renders invisible menus of the seq box', () => {
      let menu0 = menuHelper.createMenu(menuHelper.createModel());
      let field = createField({
        statusVisible: false,
        menus: [menu0],
        menusVisible: false
      });
      field.render();

      expect(field.$status.isVisible()).toBe(false);
      expect(field.menus.length).toBe(1);
      expect(field.$container).not.toHaveClass('has-menus');
    });

    it('renders visible menus of the seq box', () => {
      let menu0 = menuHelper.createMenu(menuHelper.createModel());
      let field = createField({
        statusVisible: false,
        menus: [menu0],
        menusVisible: true
      });
      field.render();

      expect(field.$status.isVisible()).toBe(true);
      expect(field.menus.length).toBe(1);
      expect(field.$container).toHaveClass('has-menus');
    });

    it('prefers the error status of the last visible field', () => {
      let field = createField({
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

    it('also remembers the new error status of the seq box if the status is currently overwritten by the field', () => {
      let field = createField({
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

    it('also remembers the error status of the seq box if the status is overwritten by the field multiple times', () => {
      let field = createField({
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

    it('does not change the seq box status if the last visible field is not changed', () => {
      let field = createField({
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

    it('removes the error status from the sequenceBox after all fields are valid', () => {
      let field = createField({
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

  describe('label', () => {

    it('focuses the first visible field when clicked', () => {
      let field = createField();
      field.render();
      field.setLabel('box label');
      JQueryTesting.triggerClick(field.$label);
      expect(field.fields[0].$field).toBeFocused();

      field.fields[0].setVisible(false);
      JQueryTesting.triggerClick(field.$label);
      let dateField = field.fields[1] as DateField;
      expect(dateField.$dateField).toBeFocused();
    });

  });

  describe('clone', () => {
    it('considers the clone properties and deep clones fields', () => {
      let cloneHelper = new CloneSpecHelper();
      let seqBox = scout.create(SequenceBox, {
        parent: session.desktop,
        id: 'seq01',
        label: 'abc',
        fields: [{
          objectType: StringField,
          labelVisible: false
        }, {
          objectType: DateField,
          label: 'a date field'
        }],
        menus: [{
          objectType: Menu
        }]
      });
      let clone = seqBox.clone({
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
      expect(clone.logicalGrid.gridConfig instanceof SequenceBoxGridConfig).toBe(true);
    });
  });

  describe('focus', () => {
    it('focuses the first field', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: StringField
        }, {
          objectType: StringField
        }]
      });
      box.render();
      expect(box.fields[0].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[0].$field).toBeFocused();
    });

    it('focuses the second field if the first is disabled', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: StringField,
          enabled: false
        }, {
          objectType: StringField,
          enabled: true
        }]
      });
      box.render();
      expect(box.fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[1].$field).toBeFocused();
    });

    it('focuses the second field if the first is not focusable', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: LabelField
        }, {
          objectType: StringField
        }]
      });
      box.render();
      expect(box.fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[1].$field).toBeFocused();
    });
  });

  describe('autoDate on datefields', () => {
    it('is set on following date fields if the date changes in a date field', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: DateField
        }, {
          objectType: DateField
        }]
      });
      box.render();
      let firstField = box.fields[0] as DateField;
      expect(firstField.autoDate).toBe(null);
      let secondField = box.fields[1] as DateField;
      expect(secondField.autoDate).toBe(null);

      let date = dates.create('2017-05-23 12:30:00.000');

      firstField.setValue(date);

      expect(firstField.value.toISOString()).toBe(date.toISOString());
      expect(firstField.autoDate).toBe(null);
      expect(secondField.autoDate.toISOString()).toBe(dates.shift(date, 0, 0, 1).toISOString());
    });

    it('is set only on following fields in the sequence box', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: DateField
        }, {
          objectType: DateField
        }, {
          objectType: DateField
        }, {
          objectType: DateField
        }]
      });
      box.render();
      let firstField = box.fields[0] as DateField;
      let secondField = box.fields[1] as DateField;
      let thirdField = box.fields[2] as DateField;
      let fourthField = box.fields[3] as DateField;

      expect(firstField.autoDate).toBe(null);
      expect(secondField.autoDate).toBe(null);
      expect(thirdField.autoDate).toBe(null);
      expect(fourthField.autoDate).toBe(null);

      let date = dates.create('2017-05-23 12:30:00.000');

      secondField.setValue(date);

      expect(firstField.autoDate).toBe(null);
      expect(secondField.autoDate).toBe(null);
      expect(secondField.value.toISOString()).toBe(date.toISOString());
      expect(thirdField.autoDate.toISOString()).toBe(dates.shift(date, 0, 0, 1).toISOString());
      expect(fourthField.autoDate.toISOString()).toBe(dates.shift(date, 0, 0, 1).toISOString());

      let date2 = dates.create('2017-05-26 12:30:00.000');
      thirdField.setValue(date2);

      expect(thirdField.value.toISOString()).toBe(date2.toISOString());
      expect(fourthField.autoDate.toISOString()).toBe(dates.shift(date2, 0, 0, 1).toISOString());
    });

    it('is correctly removed again after a date field value is removed', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: DateField
        }, {
          objectType: DateField
        }, {
          objectType: DateField
        }, {
          objectType: DateField
        }]
      });
      box.render();
      let firstField = box.fields[0] as DateField;
      let secondField = box.fields[1] as DateField;
      let thirdField = box.fields[2] as DateField;
      let fourthField = box.fields[3] as DateField;
      expect(firstField.autoDate).toBe(null);
      expect(secondField.autoDate).toBe(null);
      expect(thirdField.autoDate).toBe(null);
      expect(fourthField.autoDate).toBe(null);

      let date = dates.create('2017-05-23 12:30:00.000');
      let date2 = dates.create('2017-05-26 12:30:00.000');

      firstField.setValue(date);
      thirdField.setValue(date2);

      expect(firstField.value.toISOString()).toBe(date.toISOString());
      expect(secondField.autoDate.toISOString()).toBe(dates.shift(date, 0, 0, 1).toISOString());
      expect(thirdField.value.toISOString()).toBe(date2.toISOString());
      expect(fourthField.autoDate.toISOString()).toBe(dates.shift(date2, 0, 0, 1).toISOString());

      firstField.setValue(null);
      expect(firstField.value).toBe(null);
      expect(secondField.autoDate).toBe(null);
      expect(thirdField.autoDate).toBe(null);
      // field3.autoDate shouldn't be touched by field0's value change
      expect(fourthField.autoDate.toISOString()).toBe(dates.shift(date2, 0, 0, 1).toISOString());
    });

    it('is correctly set within sequence boxes containing other fields as well', () => {
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: DateField
        }, {
          objectType: StringField
        }, {
          objectType: DateField
        }, {
          objectType: StringField
        }]
      });
      box.render();
      let firstField = box.fields[0] as DateField;
      let thirdField = box.fields[2] as DateField;
      expect(firstField.autoDate).toBe(null);
      expect(thirdField.autoDate).toBe(null);

      let date = dates.create('2017-05-23 12:30:00.000');

      firstField.setValue(date);

      expect(firstField.value.toISOString()).toBe(date.toISOString());
      expect(thirdField.autoDate.toISOString()).toBe(dates.shift(date, 0, 0, 1).toISOString());
    });

    it('works correctly with values already set on the datefield model', () => {
      let date = dates.create('2017-05-23 12:30:00.000');
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: DateField,
          value: date
        }, {
          objectType: DateField
        }]
      });
      box.render();
      let firstField = box.fields[0] as DateField;
      let secondField = box.fields[1] as DateField;
      expect(firstField.autoDate).toBe(null);
      expect(secondField.autoDate.toISOString()).toBe(dates.shift(date, 0, 0, 1).toISOString());
    });

    it('dont conflict with already set/programmed autoDates', () => {
      let date = dates.create('2017-05-23 12:30:00.000');
      let date2 = dates.create('2017-05-27 12:30:00.000');
      let box = scout.create(SequenceBox, {
        parent: session.desktop,
        fields: [{
          objectType: DateField,
          autoDate: date
        }, {
          objectType: DateField,
          autoDate: date
        }]
      });
      box.render();
      let firstField = box.fields[0] as DateField;
      let secondField = box.fields[1] as DateField;
      firstField.setValue(date2);
      expect(firstField.autoDate.toISOString()).toBe(date.toISOString());
      expect(secondField.autoDate.toISOString()).toBe(date.toISOString());
    });
  });

  describe('menus', () => {
    it('are replaced by the menus of the last field', () => {
      let field = createField({
        menus: [{
          objectType: Menu,
          text: 'seq menu'
        }]
      });
      field.render();
      expect(field.fieldStatus.menus).toEqual(field.menus);

      field.fields[1].insertMenu({objectType: Menu, text: 'field menu'});
      expect(field.fieldStatus.menus).toEqual(field.fields[1].menus);
      field.fieldStatus.showContextMenu();
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().eq(0).text()).toBe('field menu');
      field.fieldStatus.hideContextMenu();

      field.fields[1].deleteMenu(field.fields[1].menus[0]);
      expect(field.fieldStatus.menus).toEqual(field.menus);
      field.fieldStatus.showContextMenu();
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().eq(0).text()).toBe('seq menu');
      field.fieldStatus.hideContextMenu();
    });

    it('adds all menus to sequence-box even if they are invisible', () => {
      let field = createField({
        menus: [{
          objectType: Menu,
          text: 'seq menu'
        }]
      });
      field.fields[1].insertMenus([
        {objectType: Menu, text: 'visible menu'},
        {objectType: Menu, text: 'invisible menu', visible: false}]);
      field.render();

      // ensure both menus are added to field
      expect(field.menus).toEqual(field.fields[1].menus);

      // only visible menu on fieldStatus
      expect(field.fieldStatus.menus).toEqual([field.menus[0]]);
      field.fieldStatus.showContextMenu();
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().length).toBe(1);
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().eq(0).text()).toBe('visible menu');
      field.fieldStatus.hideContextMenu();

      field.menus[1].setVisible(true);
      // both menus on fieldStatus
      expect(field.fieldStatus.menus).toEqual(field.menus);
      field.fieldStatus.showContextMenu();
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().length).toBe(2);
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().eq(0).text()).toBe('visible menu');
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().eq(1).text()).toBe('invisible menu');
      field.fieldStatus.hideContextMenu();

      // cleanup
      field.fields[1].deleteMenu(field.fields[1].menus[1]);
      field.fields[1].deleteMenu(field.fields[1].menus[0]);
      expect(field.fieldStatus.menus).toEqual(field.menus);
      field.fieldStatus.showContextMenu();
      expect(field.fieldStatus.contextMenu.$visibleMenuItems().eq(0).text()).toBe('seq menu');
      field.fieldStatus.hideContextMenu();
    });
  });

  describe('aria properties', () => {

    it('has aria-labelledby set to sequence label + field label', () => {
      let field = createField();
      field.setLabel('box label');
      let stringField = field.fields[0] as StringField;
      stringField.setLabel('first label');
      let dateField = field.fields[1] as DateField;
      dateField.setLabel('second label');
      dateField.setHasTime(true);
      field.render();
      expect(stringField.$field.attr('aria-labelledby')).toBeTruthy();
      expect(stringField.$field.attr('aria-labelledby')).toBe(field.$label.attr('id') + ' ' + stringField.$label.attr('id'));
      expect(stringField.$field.attr('aria-label')).toBeFalsy();

      expect(dateField.$dateField.attr('aria-labelledby')).toBeTruthy();
      expect(dateField.$dateField.attr('aria-labelledby')).toBe(field.$label.attr('id') + ' ' + dateField.$label.attr('id'));
      expect(dateField.$dateField.attr('aria-label')).toBeFalsy();

      expect(dateField.$timeField.attr('aria-labelledby')).toBeTruthy();
      expect(dateField.$timeField.attr('aria-labelledby')).toBe(field.$label.attr('id') + ' ' + dateField.$label.attr('id'));
      expect(dateField.$timeField.attr('aria-label')).toBeFalsy();
    });
  });

  describe('focused', () => {

    function testFields(fields: ModelOf<FormField>[] | Partial<ModelOf<SequenceBox>>, expectSequenceBoxToBeFocused: boolean[]) {
      const sequenceBox = helper.createField(SequenceBox, session.desktop, Array.isArray(fields) ? {fields} : fields);
      sequenceBox.render(); // required, because field listener is attached in _render

      const focusField = (field: FormField) => {
        // Use setFocused() instead of focus(), so the test will also work if the browser window does not have the focus
        sequenceBox.fields.forEach(f => f.setFocused(false));
        field?.setFocused(true);
      };

      expect(sequenceBox.focused).toBe(false);
      sequenceBox.fields.forEach((field, index) => {
        focusField(field);
        expect(sequenceBox.focused).withContext(`field index ${index}`).toBe(expectSequenceBoxToBeFocused[index]);
      });
      focusField(null);
      expect(sequenceBox.focused).toBe(false);

      sequenceBox.destroy();
    }

    it('marks sequence box as focused when focused field does not have a label', () => {
      testFields([
        {objectType: StringField, label: 'From', labelVisible: false},
        {objectType: StringField, label: 'To', labelVisible: false}
      ], [true, true]);
      testFields([
        {objectType: StringField, label: 'From', labelVisible: false},
        {objectType: StringField, label: 'To', labelVisible: true}
      ], [true, false]);
      testFields([
        {objectType: StringField, label: 'From', labelVisible: true},
        {objectType: StringField, label: 'To', labelVisible: false}
      ], [false, false]);
      testFields([
        {objectType: StringField, label: 'From', labelPosition: FormField.LabelPosition.ON_FIELD},
        {objectType: StringField, label: 'To', labelPosition: FormField.LabelPosition.ON_FIELD}
      ], [true, true]);
    });

    it('does not mark sequence box as focused when focused field is a button or a check box', () => {
      testFields([
        {objectType: Button, label: 'A', labelVisible: false},
        {objectType: StringField, label: 'B', labelVisible: false}
      ], [false, false]);
      testFields([
        {objectType: StringField, label: 'A', labelVisible: false},
        {objectType: CheckBoxField, label: 'B', labelVisible: false},
        {objectType: StringField, label: 'C', labelVisible: false}
      ], [true, false, false]);
    });

    it('ignores invisible fields', () => {
      testFields([
        {objectType: StringField, label: 'A', labelVisible: false},
        {objectType: StringField, label: 'B', visible: false},
        {objectType: StringField, label: 'C', labelVisible: false}
      ], [true, false, true]);
    });

    it('only marks the sequence box up to the first field with a label', () => {
      testFields([
        {objectType: StringField, label: 'A', labelPosition: FormField.LabelPosition.ON_FIELD},
        {objectType: StringField, label: 'B', labelVisible: false},
        {objectType: StringField},
        {objectType: StringField, label: 'D'},
        {objectType: StringField, label: 'E', labelPosition: FormField.LabelPosition.ON_FIELD},
        {objectType: StringField, label: 'F', labelVisible: false}
      ], [true, true, true, false, false, false]);
    });

    it('ignores short labels that only contain punctuation', () => {
      testFields([
        {objectType: StringField, labelVisible: false},
        {objectType: StringField, label: ''}
      ], [true, true]);
      testFields([
        {objectType: StringField, labelVisible: false},
        {objectType: StringField, label: '  '}
      ], [true, true]);
      testFields([
        {objectType: StringField, labelVisible: false},
        {objectType: StringField, label: '-'}
      ], [true, true]);
      testFields([
        {objectType: StringField, labelVisible: false},
        {objectType: StringField, label: '->'}
      ], [true, false]);
    });

    it('allows overriding the default behavior with special marker classes', () => {
      testFields([
        {objectType: StringField, label: 'A', cssClass: 'consider-for-outer-focus'},
        {objectType: CheckBoxField, cssClass: 'consider-for-outer-focus'},
        {objectType: StringField, label: 'C', labelVisible: false},
        {objectType: StringField, labelVisible: false, cssClass: 'never-consider-for-outer-focus'},
        {objectType: StringField, label: 'E', labelVisible: false}
      ], [true, true, true, false, false]);
      testFields({
        cssClass: 'consider-inner-focus',
        fields: [
          {objectType: StringField, label: 'A', cssClass: 'never-consider-for-outer-focus'},
          {objectType: StringField, label: 'B'},
          {objectType: Button, label: 'C'},
          {objectType: CheckBoxField, label: 'D'},
          {objectType: StringField, label: 'E', labelPosition: FormField.LabelPosition.RIGHT}
        ]
      }, [true, true, true, true, true]);
      testFields({
        cssClass: 'never-consider-inner-focus',
        fields: [
          {objectType: StringField, label: 'A', cssClass: 'never-consider-for-outer-focus'},
          {objectType: StringField, label: 'B', labelVisible: false},
          {objectType: StringField, label: 'C', labelPosition: FormField.LabelPosition.ON_FIELD},
          {objectType: StringField, label: '-'},
          {objectType: StringField, cssClass: 'consider-for-outer-focus'}
        ]
      }, [false, false, false, false, false]);
    });
  });
});
