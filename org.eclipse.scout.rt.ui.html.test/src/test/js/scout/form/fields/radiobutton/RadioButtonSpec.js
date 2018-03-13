/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("RadioButton", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('keyStroke', function() {

    it('selects the button', function() {
      var field = scout.create('RadioButton', {
        parent: session.desktop,
        keyStroke: 'ctrl-b'
      });
      field.render();
      expect(field.selected).toBe(false);

      session.desktop.$container.triggerKeyInputCapture(scout.keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Another execution does not change the selection state
      session.desktop.$container.triggerKeyInputCapture(scout.keys.B, 'ctrl');
      expect(field.selected).toBe(true);

      // Set another key stroke -> only the new one has to be active
      field.setKeyStroke('ctrl-g');
      field.setSelected(false);
      session.desktop.$container.triggerKeyInputCapture(scout.keys.B, 'ctrl');
      expect(field.selected).toBe(false);
      session.desktop.$container.triggerKeyInputCapture(scout.keys.G, 'ctrl');
      expect(field.selected).toBe(true);

      // Remove key stroke -> selected property should stay unchanged because key stroke must not be executed
      field.setKeyStroke(null);
      field.setSelected(false);
      session.desktop.$container.triggerKeyInputCapture(scout.keys.G, 'ctrl');
      expect(field.selected).toBe(false);
    });

  });
});
