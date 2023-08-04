/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CheckBoxField, keys, scout, ValueField} from '../../../../src/index';
import {JQueryTesting} from '../../../../src/testing';

describe('CheckBoxField', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('inheritance', () => {
    let checkBox: CheckBoxField;

    beforeEach(() => {
      let model = createSimpleModel('CheckBoxField', session);
      checkBox = new CheckBoxField();
      checkBox.init(model);
    });

    it('inherits from ValueField', () => {
      expect(ValueField.prototype.isPrototypeOf(checkBox)).toBe(true);
    });

    it('_renderValue sets checked property', () => {
      let $div = $('<div>');
      checkBox.render($div);

      checkBox.setValue(true);
      expect(checkBox.$checkBox.hasClass('checked')).toBe(true);
      checkBox.setValue(false);
      expect(checkBox.$checkBox.hasClass('checked')).toBe(false);
    });

    it('_renderValue sets disabled property', () => {
      let $div = $('<div>');
      checkBox.render($div);

      checkBox.setEnabled(false);
      expect(checkBox.$field.hasClass('disabled')).toBe(true);
      expect(checkBox.$checkBox.hasClass('disabled')).toBe(true);
      checkBox.setEnabled(true);
      expect(checkBox.$field.hasClass('disabled')).toBe(false);
      expect(checkBox.$checkBox.hasClass('disabled')).toBe(false);
    });
  });

  describe('keyStroke', () => {

    it('toggles the value', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.value).toBe(false);

      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.value).toBe(true);

      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.value).toBe(false);

      // Set another keystroke -> only the new one has to be active
      field.setKeyStroke('ctrl-g');
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.value).toBe(false);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.value).toBe(true);

      // Remove keystroke -> value should stay unchanged because keystroke must not be executed
      field.setKeyStroke(null);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.value).toBe(true);
    });

  });

  describe('value', () => {
    it('is always true or false', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop
      });
      expect(field.value).toBe(false);
      expect(field.initialValue).toBe(false);

      field.setValue(true);
      expect(field.value).toBe(true);

      field.setValue(false);
      expect(field.value).toBe(false);

      field.setValue(null);
      expect(field.value).toBe(false);
    });

    it('can be null if tristate is enabled', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop,
        triStateEnabled: true
      });
      expect(field.value).toBe(null);
      expect(field.initialValue).toBe(null);

      field.setValue(true);
      expect(field.value).toBe(true);

      field.setValue(false);
      expect(field.value).toBe(false);

      field.setValue(null);
      expect(field.value).toBe(null);

      field.setTriStateEnabled(false);
      expect(field.value).toBe(false);

      field.setValue(null);
      expect(field.value).toBe(false);
    });
  });

  describe('saveNeeded', () => {
    it('is false initially', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop
      });
      expect(field.saveNeeded).toBe(false);

      field = scout.create(CheckBoxField, {
        parent: session.desktop,
        value: true
      });
      expect(field.saveNeeded).toBe(false);
    });

    it('is updated when value changes', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop
      });
      expect(field.saveNeeded).toBe(false);

      field.setValue(true);
      expect(field.saveNeeded).toBe(true);

      field.setValue(false);
      expect(field.saveNeeded).toBe(false);
    });
  });

  describe('aria properties', () => {
    let checkBox: CheckBoxField;

    beforeEach(() => {
      let model = createSimpleModel('CheckBoxField', session);
      checkBox = new CheckBoxField();
      checkBox.init(model);
    });

    it('has aria role checkbox', () => {
      let $div = $('<div>');
      checkBox.render($div);
      expect(checkBox.$checkBox).toHaveAttr('role', 'checkbox');
    });

    it('has aria-labelledby set', () => {
      let $div = $('<div>');
      checkBox.render($div);
      expect(checkBox.$checkBox.attr('aria-labelledby')).toBeTruthy();
      expect(checkBox.$checkBox.attr('aria-labelledby')).toBe(checkBox.$checkBoxLabel.attr('id'));
      expect(checkBox.$checkBox.attr('aria-label')).toBeFalsy();
    });

    it('has aria-checked property set', () => {
      let $div = $('<div>');
      checkBox.render($div);

      expect(checkBox.$checkBox).toHaveAttr('aria-checked', 'false');
      checkBox.setValue(true);
      expect(checkBox.$checkBox).toHaveAttr('aria-checked', 'true');

      checkBox.setTriStateEnabled(true);
      checkBox.setValue(null);
      expect(checkBox.$checkBox).toHaveAttr('aria-checked', 'mixed');
    });
  });
});
