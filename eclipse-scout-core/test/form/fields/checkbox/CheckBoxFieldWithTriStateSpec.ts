/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CheckBoxField, ValueField} from '../../../../src/index';

describe('CheckBoxFieldWithTriState', () => {

  describe('inheritance', () => {

    let session: SandboxSession;
    let checkBoxField: CheckBoxField;

    beforeEach(() => {
      setFixtures(sandbox());
      session = sandboxSession();
      let model = createSimpleModel(CheckBoxField, session);
      model.triStateEnabled = true;
      checkBoxField = new CheckBoxField();
      checkBoxField.init(model);
    });

    it('inherits from ValueField', () => {
      expect(ValueField.prototype.isPrototypeOf(checkBoxField)).toBe(true);
    });

    it('_renderValue sets checked and undefined classes', () => {
      let $div = $('<div>');
      checkBoxField.render($div);

      checkBoxField.setValue(false);
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(false);
      checkBoxField.toggleChecked();
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(true);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(false);
      checkBoxField.toggleChecked();
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(true);
      checkBoxField.toggleChecked();
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(false);
    });

    it('_renderValue sets disabled property', () => {
      let $div = $('<div>');
      checkBoxField.render($div);

      checkBoxField.setEnabled(false);
      expect(checkBoxField.$field.hasClass('disabled')).toBe(true);
      expect(checkBoxField.$checkBox.hasClass('disabled')).toBe(true);
      checkBoxField.setEnabled(true);
      expect(checkBoxField.$field.hasClass('disabled')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('disabled')).toBe(false);
    });

  });

});
