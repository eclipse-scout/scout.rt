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
  });

  function expectEnabled(field, expectedEnabled, expectedEnabledComputed, hasClass) {
    expect(field.enabled).toBe(expectedEnabled);
    expect(field.enabledComputed).toBe(expectedEnabledComputed);
    if (hasClass) {
      expect(field.$field).toHaveClass(hasClass);
    }
  }

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
  });  

});
