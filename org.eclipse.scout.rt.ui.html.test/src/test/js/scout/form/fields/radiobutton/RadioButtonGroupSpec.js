/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("RadioButtonGroup", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  function expectEnabled(field, expectedEnabled, expectedEnabledComputed, hasClass) {
    expect(field.enabled).toBe(expectedEnabled);
    expect(field.enabledComputed).toBe(expectedEnabledComputed);
    if (hasClass) {
      expect(field.$field).toHaveClass(hasClass);
    }
  }

  describe('gridColumnCount', function() {
    it('calculates column count correctly', function() {
      var numButtons = 3;
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, numButtons);
      radioButtonGroup.render();

      expect(radioButtonGroup.gridColumnCount).toBe(numButtons);
      expect(radioButtonGroup._setGridColumnCount(numButtons)).toBe(false);

      expect(radioButtonGroup._setGridColumnCount(scout.RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT)).toBe(false);
      expect(radioButtonGroup.gridColumnCount).toBe(numButtons);

      radioButtonGroup.gridDataHints.h = 2;
      expect(radioButtonGroup._setGridColumnCount(0)).toBe(true);
      expect(radioButtonGroup.gridColumnCount).toBe(0);

      radioButtonGroup.gridDataHints.h = numButtons;
      expect(radioButtonGroup._setGridColumnCount(-2 /* also triggers 'set to default' */ )).toBe(true);
      expect(radioButtonGroup.gridColumnCount).toBe(1);

      expect(radioButtonGroup._setGridColumnCount(scout.RadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT)).toBe(false);
      expect(radioButtonGroup.gridColumnCount).toBe(1);

      expect(radioButtonGroup._setGridColumnCount(4)).toBe(true);
      expect(radioButtonGroup.gridColumnCount).toBe(4);

      expect(radioButtonGroup.logicalGrid.dirty).toBe(true);
      expect(radioButtonGroup.htmlBody.layout.valid).toBe(false);
    });
  });

  describe('enabled', function() {
    it('propagation', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
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

  describe('init', function() {
    it('sets the value if it is provided', function() {
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        value: 1,
        fields: [{
          objectType: 'RadioButton',
          radioValue: 0
        }, {
          objectType: 'RadioButton',
          radioValue: 1
        }]
      });
      expect(radioButtonGroup.value).toBe(1);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });

    it('selects the correct button if it is selected', function() {
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        fields: [{
          objectType: 'RadioButton'
        }, {
          objectType: 'RadioButton',
          selected: true
        }]
      });
      expect(radioButtonGroup.value).toBe(null);
      expect(radioButtonGroup.errorStatus).toBe(null);
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });
  });

  describe('lookupCall', function(){

    it('creates a radio button for each lookup row', function(){
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        lookupCall: "DummyLookupCall"
      });

      jasmine.clock().tick(300);
      expect(radioButtonGroup.radioButtons.length).toBe(3);
      expect(radioButtonGroup.lookupCall).not.toBe(null);
      expect(radioButtonGroup.errorStatus).toBe(null);
    });

    it('selects correct radio button', function(){
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        lookupCall: "DummyLookupCall",
        value: 1
      });

      jasmine.clock().tick(300);
      expect(radioButtonGroup.radioButtons.length).toBe(3);
      expect(radioButtonGroup.errorStatus).toBe(null);
      expect(radioButtonGroup.lookupCall).not.toBe(null);
      expect(radioButtonGroup.value).toBe(1);
      expect(radioButtonGroup.selectedButton.radioValue).toBe(1);
    });

    it('lookupRow lives on the radioButton', function(){
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        lookupCall: "DummyLookupCall"
      });

      jasmine.clock().tick(300);
      radioButtonGroup.setValue(2);
      expect(radioButtonGroup.value).toBe(2);
      expect(radioButtonGroup.errorStatus).toBe(null);
      expect(radioButtonGroup.selectedButton.radioValue).toBe(2);
      expect(radioButtonGroup.selectedButton.lookupRow.key).toBe(2);
    });

  });

  describe('setValue', function() {
    it('updates the currently selected button', function() {
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        fields: [{
          objectType: 'RadioButton',
          radioValue: 0
        }, {
          objectType: 'RadioButton',
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

    it('does nothing if radio buttons have no radioValue', function() {
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        fields: [{
          objectType: 'RadioButton'
        }, {
          objectType: 'RadioButton',
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

    it('unselects every button when setting it to null', function() {
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        fields: [{
          objectType: 'RadioButton',
          radioValue: 0
        }, {
          objectType: 'RadioButton',
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

  describe('label', function() {
    it('is linked with the buttons', function() {
      var group = helper.createRadioButtonGroup(session.desktop, 2);
      group.setLabel('label');
      group.radioButtons[0].setLabel('label 0');
      group.radioButtons[1].setLabel('label 1');
      group.render();
      expect(group.radioButtons[0].$field.attr('aria-labelledby')).toBeTruthy();
      expect(group.radioButtons[0].$field.attr('aria-labelledby')).toBe(group.$label.attr('id') + ' ' + group.radioButtons[0].$buttonLabel.attr('id'));
      expect(group.radioButtons[1].$field.attr('aria-labelledby')).toBeTruthy();
      expect(group.radioButtons[1].$field.attr('aria-labelledby')).toBe(group.$label.attr('id') + ' ' + group.radioButtons[1].$buttonLabel.attr('id'));
    });
  });

  describe('selectButton', function() {
    it('selects the new button and unselects the old one', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
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

    it('makes only the new button tabbable', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
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

    it('does not remove the tabindex if the button is deselected', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
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

    it('focuses the new button if the old button had the focus', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
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

    it('is called when setting the button directly', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
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

    it('triggers a property change event', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      var triggeredEvent = null;
      var eventCount = 0;
      radioButtonGroup.on('propertyChange', function(event) {
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

    it('makes sure only one button is selected even if multiple buttons are selected during init', function() {
      var radioButtonGroup = scout.create('RadioButtonGroup', {
        parent: session.desktop,
        fields: [{
          objectType: 'RadioButton',
          selected: true
        }, {
          objectType: 'RadioButton',
          selected: true
        }]
      });
      // Only the second button should be selected
      expect(radioButtonGroup.selectedButton).toBe(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[0].selected).toBe(false);
      expect(radioButtonGroup.radioButtons[1].selected).toBe(true);
    });
  });

  describe('insertButton', function() {
    it('inserts the button at the end', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      expect(radioButtonGroup.fields.length).toBe(2);
      expect(radioButtonGroup.radioButtons.length).toBe(2);

      var button = scout.create('RadioButton', {
        parent: radioButtonGroup,
        selected: true
      });
      radioButtonGroup.insertButton(button);
      expect(radioButtonGroup.fields.length).toBe(3);
      expect(radioButtonGroup.fields[2]).toBe(button);
      expect(radioButtonGroup.radioButtons.length).toBe(3);
      expect(radioButtonGroup.radioButtons[2]).toBe(button);
      expect(radioButtonGroup.selectedButton).toBe(button);

      var button2 = scout.create('RadioButton', {
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

  describe('focus', function() {
    it('focuses the selected button', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      radioButtonGroup.selectButton(radioButtonGroup.radioButtons[1]);
      expect(radioButtonGroup.radioButtons[1].$field).not.toBeFocused();

      radioButtonGroup.focus();
      expect(radioButtonGroup.radioButtons[1].$field).toBeFocused();
    });

    it('focuses the first button if no button is selected', function() {
      var radioButtonGroup = helper.createRadioButtonGroup(session.desktop, 2);
      radioButtonGroup.render();
      expect(radioButtonGroup.selectedButton).toBe(null);
      expect(radioButtonGroup.radioButtons[0].$field).not.toBeFocused();

      radioButtonGroup.focus();
      expect(radioButtonGroup.radioButtons[0].$field).toBeFocused();
    });
  });
});
