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
describe("Button", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('setLabel', function() {
    it('toggles the class with-label on the icon', function() {
      var button = scout.create('Button', {
        parent: session.desktop,
        label: 'label',
        iconId: scout.icons.ANGLE_DOWN
      });
      button.render();
      expect(button.$field.data('$icon')).toHaveClass('with-label');

      button.setLabel(null);
      expect(button.$field.data('$icon')).not.toHaveClass('with-label');

      button.setLabel('a new label');
      expect(button.$field.data('$icon')).toHaveClass('with-label');
    });
  });

  describe('setIconId', function() {
    it('toggles the class with-label on the icon', function() {
      var button = scout.create('Button', {
        parent: session.desktop,
        label: 'label',
        iconId: scout.icons.ANGLE_DOWN
      });
      button.render();
      expect(button.$field.data('$icon')).toHaveClass('with-label');

      button.setIconId(null);
      expect(button.$field.data('$icon')).toBeFalsy();

      button.setIconId(scout.icons.ANGLE_UP);
      expect(button.$field.data('$icon')).toHaveClass('with-label');
    });
  });

  describe('keyStrokeScope', function() {
    it('may be an id of a form field and will be resolved when initialized', function() {
      var form = scout.create('Form', {
        id: 'myForm',
        parent: session.desktop,
        rootGroupBox: {
          id: 'myMainBox',
          objectType: 'GroupBox',
          fields: [{
            id: 'myButton',
            objectType: 'Button',
            keyStroke: 'ctrl-1',
            keyStrokeScope: 'myMainBox'
          }]
        }
      });
      var button = form.widget('myButton');
      expect(button.keyStrokeScope).toBe(form.rootGroupBox);
    });

    it('may be an an outer form', function() {
      var form = scout.create('Form', {
        parent: session.desktop,
        id: 'outerForm',
        rootGroupBox: {
          objectType: 'GroupBox',
          fields: [{
            objectType: 'WrappedFormField',
            innerForm: {
              id: 'innerForm',
              objectType: 'Form',
              rootGroupBox: {
                objectType: 'GroupBox',
                fields: [{
                  id: 'myButton',
                  objectType: 'Button',
                  keyStroke: 'ctrl-1',
                  keyStrokeScope: 'outerForm'
                }]
              }
            }
          }]
        }
      });
      var button = form.widget('myButton');
      expect(button.keyStrokeScope).toBe(form);
    });
  });

});
