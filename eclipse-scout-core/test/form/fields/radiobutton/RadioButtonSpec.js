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
import {keys, scout} from '../../../../src/index';

describe('RadioButton', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('label', function() {

    it('is linked with the field', function() {
      var field = scout.create('RadioButton', {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.$field.attr('aria-labelledby')).toBe(field.$buttonLabel.attr('id'));
    });

  });

  describe('keyStroke', function() {

    it('selects the button', function() {
      var field = scout.create('RadioButton', {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.selected).toBe(false);

      session.desktop.$container.triggerKeyInputCapture(keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Another execution does not change the selection state
      session.desktop.$container.triggerKeyInputCapture(keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Set another key stroke -> only the new one has to be active
      field.setKeyStroke('ctrl-g');
      field.setSelected(false);
      session.desktop.$container.triggerKeyInputCapture(keys.B, 'ctrl');
      expect(field.selected).toBe(false);
      session.desktop.$container.triggerKeyInputCapture(keys.G, 'ctrl');
      expect(field.selected).toBe(true);

      // Remove key stroke -> selected property should stay unchanged because key stroke must not be executed
      field.setKeyStroke(null);
      field.setSelected(false);
      session.desktop.$container.triggerKeyInputCapture(keys.G, 'ctrl');
      expect(field.selected).toBe(false);
    });

    it('does not focus the button', function() {
      var field = scout.create('RadioButton', {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.selected).toBe(false);

      session.desktop.$container.triggerKeyInputCapture(keys.B, 'ctrl');
      expect(field.selected).toBe(true);
      expect(field.isFocused()).toBe(false);
    });

  });
});
