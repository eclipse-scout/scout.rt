/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, LabelField, LogicalGridLayout, RadioButton, RadioButtonGroup, scout} from '../../../../src/index';
import {DummyLookupCall, FormSpecHelper} from '../../../../src/testing/index';

describe('RadioButtonGroup', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function expectEnabled(field: FormField, expectedEnabled: boolean, expectedEnabledComputed: boolean, hasClass?: string) {
    expect(field.enabled).toBe(expectedEnabled);
    expect(field.enabledComputed).toBe(expectedEnabledComputed);
    if (hasClass) {
      expect(field.$field).toHaveClass(hasClass);
    }
  }

  describe('gridColumnCount', () => {
    it('calculates column count correctly', () => {
      let numButtons = 3;
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, numButtons);
      radioButtonGroup.render();

      expect(radioButtonGroup.gridColumnCount).toBe(numButtons);
      expect(radioButtonGroup._setGridColumnCount(numButtons)).toBe(false);

      expect(radioButtonGroup._setGridColumnCount(RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT)).toBe(false);
      expect(radioButtonGroup.gridColumnCount).toBe(numButtons);

      radioButtonGroup.gridDataHints.h = 2;
      expect(radioButtonGroup._setGridColumnCount(0)).toBe(true);
      expect(radioButtonGroup.gridColumnCount).toBe(0);

      radioButtonGroup.gridDataHints.h = numButtons;
      expect(radioButtonGroup._setGridColumnCount(-2 /* also triggers 'set to default' */)).toBe(true);
      expect(radioButtonGroup.gridColumnCount).toBe(1);

      expect(radioButtonGroup._setGridColumnCount(RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT)).toBe(false);
      expect(radioButtonGroup.gridColumnCount).toBe(1);

      expect(radioButtonGroup._setGridColumnCount(4)).toBe(true);
      expect(radioButtonGroup.gridColumnCount).toBe(4);

      expect(radioButtonGroup.logicalGrid.dirty).toBe(true);
      let layout = radioButtonGroup.htmlBody.layout as LogicalGridLayout;
      expect(layout.valid).toBe(false);
    });
  });

  describe('enabled', () => {
    it('propagation', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();

      expectEnabled(radioButtonGroup, true, true);
      expectEnabled(radioButtonGroup.getFields()[0], true, true);
      expectEnabled(radioButtonGroup.getFields()[1], true, true);

      radioButtonGroup.setEnabled(false);
      expectEnabled(radioButtonGroup, false, false, 'disabled');
      expectEnabled(radioButtonGroup.getFields()[0], true, false, 'disabled');
      expectEnabled(radioButtonGroup.getFields()[1], true, false, 'disabled');
    });
  });

  describe('init', () => {
    it('sets the value if it is provided', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        value: 1,
        fields: [{
          objectType: RadioButton,
          radioValue: 0
        }, {
          objectType: RadioButton,
          radioValue: 1
        }]
      });
      expect(radioButtonGroup.value).toBe(1);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });

    it('selects the correct button if it is selected', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        fields: [{
          objectType: RadioButton
        }, {
          objectType: RadioButton,
          selected: true
        }]
      });
      expect(radioButtonGroup.value).toBe(null);
      expect(radioButtonGroup.errorStatus).toBe(null);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });

    it('syncs this.radioButtons with this.fields', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        fields: [{
          objectType: RadioButton
        }, {
          objectType: RadioButton,
          selected: true
        }, {
          objectType: LabelField,
          label: 'XX'
        }]
      });
      expect(radioButtonGroup.fields.length).toBe(3);
      expect(radioButtonGroup.radioButtons.length).toBe(2);
    });
  });

  describe('lookupCall', () => {

    it('can be prepared with initial value', done => {
      let group = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall',
        value: 2
      });

      let lookupPrepared = group.when('prepareLookupCall');
      let lookupDone = group.when('lookupCallDone');
      group.render(); // triggers the execution of the lookup call
      jasmine.clock().tick(500);

      $.promiseAll([lookupPrepared, lookupDone]).then(event => {
        expect(event.lookupCall instanceof DummyLookupCall).toBe(true);
        expect(group.radioButtons.length).toBe(3);
      })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(500);
    });

    it('can be prepared with explicit value', done => {
      let group = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall'
      });

      let lookupPrepared = group.when('prepareLookupCall');
      let lookupDone = group.when('lookupCallDone');
      group.setValue(2);
      jasmine.clock().tick(500);

      $.promiseAll([lookupPrepared, lookupDone]).then(event => {
        expect(event.lookupCall instanceof DummyLookupCall).toBe(true);
        expect(group.radioButtons.length).toBe(3);
      })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(500);
    });

    it('creates a radio button for each lookup row', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall'
      });
      radioButtonGroup.render();
      jasmine.clock().tick(300);
      expect(radioButtonGroup.radioButtons.length).toBe(3);
      expect(radioButtonGroup.lookupCall).not.toBe(null);
      expect(radioButtonGroup.errorStatus).toBe(null);
    });

    it('selects correct radio button', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall',
        value: 1
      });
      radioButtonGroup.render(); // triggers the execution of the lookup call
      jasmine.clock().tick(300);
      expect(radioButtonGroup.radioButtons.length).toBe(3);
      expect(radioButtonGroup.errorStatus).toBe(null);
      expect(radioButtonGroup.lookupCall).not.toBe(null);
      expect(radioButtonGroup.value).toBe(1);
      expect(radioButtonGroup.selectedButton.radioValue).toBe(1);

      // select last
      radioButtonGroup.selectLastButton();
      expect(radioButtonGroup.value).toBe(3);

      // select first
      radioButtonGroup.selectFirstButton();
      expect(radioButtonGroup.value).toBe(1);

      // select by value
      radioButtonGroup.setValue(2);
      expect(radioButtonGroup.value).toBe(2);
    });

    it('lookupRow lives on the radioButton', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        lookupCall: 'DummyLookupCall'
      });
      radioButtonGroup.render(); // triggers the execution of the lookup call

      jasmine.clock().tick(300);
      radioButtonGroup.setValue(2);
      expect(radioButtonGroup.value).toBe(2);
      expect(radioButtonGroup.errorStatus).toBe(null);
      expect(radioButtonGroup.selectedButton.radioValue).toBe(2);
      expect(radioButtonGroup.selectedButton.lookupRow.key).toBe(2);
    });

  });

  describe('setValue', () => {
    it('updates the currently selected button', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        fields: [{
          objectType: RadioButton,
          radioValue: 0
        }, {
          objectType: RadioButton,
          radioValue: 1
        }]
      });
      radioButtonGroup.render();

      radioButtonGroup.setValue(1);
      expect(radioButtonGroup.value).toBe(1);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);

      radioButtonGroup.setValue(0);
      expect(radioButtonGroup.value).toBe(0);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(true);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);

      // check that value does not change when an illegal value is set
      radioButtonGroup.setValue(2);
      expect(radioButtonGroup.value).toBe(0);
    });

    it('does nothing if radio buttons have no radioValue', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        fields: [{
          objectType: RadioButton
        }, {
          objectType: RadioButton,
          selected: true
        }]
      });
      radioButtonGroup.render();

      radioButtonGroup.setValue(1);
      expect(radioButtonGroup.value).toBe(null);
      expect(radioButtonGroup.errorStatus).not.toBe(null);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);

      radioButtonGroup.setValue(0);
      expect(radioButtonGroup.value).toBe(null);
      expect(radioButtonGroup.errorStatus).not.toBe(null);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
    });

    it('unselects every button when setting it to null', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        fields: [{
          objectType: RadioButton,
          radioValue: 0
        }, {
          objectType: RadioButton,
          radioValue: 1
        }]
      });
      radioButtonGroup.render();

      radioButtonGroup.setValue(1);
      expect(radioButtonGroup.value).toBe(1);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);

      radioButtonGroup.setValue(null);
      expect(radioButtonGroup.value).toBe(null);
      expect(radioButtonGroup.selectedButton).toBe(null);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);
    });
  });

  describe('selectButton', () => {
    it('selects the new button and unselects the old one', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();

      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(true);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);

      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });

    it('makes only the new button tabbable', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();

      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].isTabbable()).toBe(true);
      expect(radioButtonGroup.radioButtons[1].isTabbable()).toBe(false);

      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].isTabbable()).toBe(false);
      expect(radioButtonGroup.radioButtons[1].isTabbable()).toBe(true);
    });

    it('does not remove the tabindex if the button is deselected', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();

      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(true);
      expect(radioButtonGroup.radioButtons[0].isTabbable()).toBe(true);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].isTabbable()).toBe(false);

      radioButtonGroup.selectButton(null);
      expect(radioButtonGroup.selectedButton).toBe(null);
      expect(radioButtonGroup.radioButtons[0].isTabbable()).toBe(true);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].isTabbable()).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);
    });

    it('focuses the new button if the old button had the focus', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[0]);

      // Previously selected button was not focused -> do not automatically focus the new button
      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].isFocused()).toBe(false);

      // Previously selected button was focused -> focus the new button
      radioButtonGroup.radioButtons[1].focus();
      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].isFocused()).toBe(true);
    });

    it('is called when setting the button directly', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();

      radioButtonGroup.radioButtons[0].setSelected(true);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(true);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);

      radioButtonGroup.radioButtons[1].setSelected(true);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });

    it('triggers a property change event', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      let triggeredEvent = null;
      let eventCount = 0;
      radioButtonGroup.on('propertyChange', event => {
        if (event.propertyName === 'selectedButton') {
          triggeredEvent = event;
          eventCount++;
        }
      });
      radioButtonGroup.radioButtons[0].setSelected(true);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[0]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(true);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(false);
      expect(triggeredEvent.newValue).toBe(radioButtonGroup.radioButtons[0]);
      expect(eventCount).toBe(1);

      radioButtonGroup.radioButtons[1].setSelected(true);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
      expect(triggeredEvent.newValue).toBe(radioButtonGroup.radioButtons[1]);
      expect(eventCount).toBe(2);
    });

    it('makes sure only one button is selected even if multiple buttons are selected during init', () => {
      let radioButtonGroup = scout.create(RadioButtonGroup, {
        parent: session.desktop,
        fields: [{
          objectType: RadioButton,
          selected: true
        }, {
          objectType: RadioButton,
          selected: true
        }]
      });
      // Only the second button should be selected
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });
  });

  describe('insertButton', () => {
    it('inserts the button at the end', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      expect(radioButtonGroup.fields.length).toBe(2);
      expect(radioButtonGroup.radioButtons.length).toBe(2);

      let button = scout.create(RadioButton, {
        parent: radioButtonGroup,
        selected: true
      });
      radioButtonGroup.insertButton(button);
      expect(radioButtonGroup.fields.length).toBe(3);
      expect(radioButtonGroup.fields[2]).toBe(button);
      expect(radioButtonGroup.radioButtons.length).toBe(3);
      expect(radioButtonGroup.radioButtons[2]).toBe(button);
      expect(radioButtonGroup.selectedButton).toBe(button);

      let button2 = scout.create(RadioButton, {
        parent: radioButtonGroup,
        selected: true
      });
      radioButtonGroup.insertButton(button2);
      expect(radioButtonGroup.fields.length).toBe(4);
      expect(radioButtonGroup.fields[3]).toBe(button2);
      expect(radioButtonGroup.radioButtons.length).toBe(4);
      expect(radioButtonGroup.radioButtons[3]).toBe(button2);
      expect(radioButtonGroup.selectedButton).toBe(button2);
    });
  });

  describe('focus', () => {
    it('focuses the selected button', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[1].$field).not.toBeFocused();

      radioButtonGroup.focus();
      expect(radioButtonGroup.radioButtons[1].$field).toBeFocused();
    });

    it('focuses the first button if no button is selected', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      expect(radioButtonGroup.selectedButton).toBe(null);
      expect(radioButtonGroup.radioButtons[0].$field).not.toBeFocused();

      radioButtonGroup.focus();
      expect(radioButtonGroup.radioButtons[0].$field).toBeFocused();
    });
  });

  describe('aria properties', () => {

    it('has aria-labelledby set on the buttons', () => {
      let group = helper.createRadioButtonGroup(session.desktop, 2);
      group.setLabel('label');
      group.radioButtons[0].setLabel('label 0');
      group.radioButtons[1].setLabel('label 1');
      group.render();

      expect(group.radioButtons[0].$field.attr('aria-labelledby')).toBeTruthy();
      expect(group.radioButtons[0].$field.attr('aria-labelledby')).toBe(group.$label.attr('id') + ' ' + group.radioButtons[0].$buttonLabel.attr('id'));
      expect(group.radioButtons[0].$field.attr('aria-label')).toBeFalsy();

      expect(group.radioButtons[1].$field.attr('aria-labelledby')).toBeTruthy();
      expect(group.radioButtons[1].$field.attr('aria-labelledby')).toBe(group.$label.attr('id') + ' ' + group.radioButtons[1].$buttonLabel.attr('id'));
      expect(group.radioButtons[1].$field.attr('aria-label')).toBeFalsy();
    });

    it('has aria role radiogroup', () => {
      let radioButtonGroup = helper.createRadioButtonGroup(session.desktop);
      radioButtonGroup.render();
      expect(radioButtonGroup.$body).toHaveAttr('role', 'radiogroup');
    });
  });
});
