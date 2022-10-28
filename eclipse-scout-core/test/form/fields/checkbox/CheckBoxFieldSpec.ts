/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CheckBoxField, CheckBoxFieldModel, keys, scout, ValueField} from '../../../../src/index';
import {triggerKeyInputCapture} from '../../../../src/testing/jquery-testing';

describe('CheckBoxField', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('inheritance', () => {
    let checkBox: CheckBoxField;
    let model: CheckBoxFieldModel;

    beforeEach(() => {
      model = createSimpleModel('CheckBoxField', session);
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

  describe('label', () => {

    it('is linked with the field', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      // Actually only $checkBoxLabel needs to be linked, but since addField does it automatically $label is linked as well.
      // It doesn't matter because it will always be empty anyway.
      expect(field.$field.attr('aria-labelledby')).toBe(field.$checkBoxLabel.attr('id') + ' ' + field.$label.attr('id'));
    });

  });

  describe('keyStroke', () => {

    it('toggles the value', () => {
      let field = scout.create(CheckBoxField, {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.value).toBe(null);

      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.value).toBe(true);

      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.value).toBe(false);

      // Set another keystroke -> only the new one has to be active
      field.setKeyStroke('ctrl-g');
      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.value).toBe(false);
      triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.value).toBe(true);

      // Remove key stroke -> value should stay unchanged because key stroke must not be executed
      field.setKeyStroke(null);
      triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.value).toBe(true);
    });

  });
});
