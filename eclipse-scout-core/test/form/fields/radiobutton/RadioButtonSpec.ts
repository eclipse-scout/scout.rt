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
import {keys, RadioButton, scout} from '../../../../src/index';
import {JQueryTesting} from '../../../../src/testing';

describe('RadioButton', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('label', () => {

    it('is linked with the field', () => {
      let field = scout.create(RadioButton, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.$field.attr('aria-labelledby')).toBe(field.$buttonLabel.attr('id'));
    });

  });

  describe('keyStroke', () => {

    it('selects the button', () => {
      let field = scout.create(RadioButton, {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.selected).toBe(false);

      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Another execution does not change the selection state
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Set another keystroke -> only the new one has to be active
      field.setKeyStroke('ctrl-g');
      field.setSelected(false);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(false);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.selected).toBe(true);

      // Remove keystroke -> selected property should stay unchanged because keystroke must not be executed
      field.setKeyStroke(null);
      field.setSelected(false);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.selected).toBe(false);
    });

    it('does not focus the button', () => {
      let field = scout.create(RadioButton, {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.selected).toBe(false);

      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(true);
      expect(field.isFocused()).toBe(false);
    });

  });
});
