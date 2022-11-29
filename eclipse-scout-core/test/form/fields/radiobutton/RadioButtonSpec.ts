/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, RadioButton, scout} from '../../../../src/index';
import {triggerKeyInputCapture} from '../../../../src/testing/jquery-testing';

describe('RadioButton', () => {
  let session;

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

      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Another execution does not change the selection state
      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Set another key stroke -> only the new one has to be active
      field.setKeyStroke('ctrl-g');
      field.setSelected(false);
      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(false);
      triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.selected).toBe(true);

      // Remove key stroke -> selected property should stay unchanged because key stroke must not be executed
      field.setKeyStroke(null);
      field.setSelected(false);
      triggerKeyInputCapture(session.desktop.$container, keys.G, 'ctrl');
      expect(field.selected).toBe(false);
    });

    it('does not focus the button', () => {
      let field = scout.create(RadioButton, {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.selected).toBe(false);

      triggerKeyInputCapture(session.desktop.$container, keys.B, 'ctrl');
      expect(field.selected).toBe(true);
      expect(field.isFocused()).toBe(false);
    });

  });
});
