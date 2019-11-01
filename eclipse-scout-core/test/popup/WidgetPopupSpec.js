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
describe("WidgetPopup", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createPopupWithFormAnd2Fields(initialFocus) {
    return scout.create('WidgetPopup', {
      parent: session.desktop,
      widget: {
        objectType: "Form",
        displayHint: scout.Form.DisplayHint.VIEW,
        modal: false,
        initialFocus: initialFocus,
        rootGroupBox: {
          objectType: "GroupBox",
          fields: [{
            id: 'First Field',
            objectType: 'StringField'
          }, {
            id: 'Second Field',
            objectType: 'StringField'
          }]
        }
      }
    });
  }

  describe('withFocusContext', function() {
    it('focuses the first focusable element when opened', function() {
      var popup = createPopupWithFormAnd2Fields();
      popup.open();
      expect(popup.widget.widget('First Field').isFocused()).toBe(true);
    });

    it('focuses the element specified by an inner element', function() {
      var popup = createPopupWithFormAnd2Fields('Second Field');
      popup.open();
      expect(popup.widget.widget('Second Field').isFocused()).toBe(true);
    });

    it('reverts focus correctly when popup is closed', function() {
      var field1 = scout.create('StringField', {
        parent: session.desktop
      });
      field1.render();
      var field2 = scout.create('StringField', {
        parent: session.desktop
      });
      field2.render();
      field2.focus();

      var popup = createPopupWithFormAnd2Fields('Second Field');
      popup.open();
      expect(popup.widget.widget('Second Field').isFocused()).toBe(true);

      popup.close();
      expect(field2.isFocused()).toBe(true);
    });
  });
});
