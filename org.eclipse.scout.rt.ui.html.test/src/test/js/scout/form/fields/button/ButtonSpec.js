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

  describe('setting legacy styles', function() {
    it('sets style attributes', function() {
      var button = scout.create('Button', {
        parent: session.desktop,
        label: 'label',
        foregroundColor: 'red',
        backgroundColor: 'yellow',
        font: '15-ITALIC-Times New Roman'
      });
      button.render();
      expect(button.$field[0].style.color).toBe('red');
      expect(button.$field[0].style.backgroundColor).toBe('yellow');
      expect(button.$field[0].style.fontFamily).toMatch(/['"]?Times New Roman['"]?/);
      expect(button.$field[0].style.fontSize).toBe('15pt');
      expect(button.$field[0].style.fontStyle).toBe('italic');
      expect(button.$field[0].style.fontWeight).toBe('');
      expect(button.$buttonLabel[0].style.color).toBe('red');
      expect(button.$buttonLabel[0].style.backgroundColor).toBe('');
      expect(button.$buttonLabel[0].style.fontFamily).toMatch(/['"]?Times New Roman['"]?/);
      expect(button.$buttonLabel[0].style.fontSize).toBe('15pt');
      expect(button.$buttonLabel[0].style.fontStyle).toBe('italic');
      expect(button.$buttonLabel[0].style.fontWeight).toBe('');

      button.setFont(null);
      expect(button.$field[0].style.color).toBe('red');
      expect(button.$field[0].style.backgroundColor).toBe('yellow');
      expect(button.$field[0].style.fontFamily).toBe('');
      expect(button.$field[0].style.fontSize).toBe('');
      expect(button.$field[0].style.fontStyle).toBe('');
      expect(button.$field[0].style.fontWeight).toBe('');
      expect(button.$buttonLabel[0].style.color).toBe('red');
      expect(button.$buttonLabel[0].style.backgroundColor).toBe('');
      expect(button.$buttonLabel[0].style.fontFamily).toBe('');
      expect(button.$buttonLabel[0].style.fontSize).toBe('');
      expect(button.$buttonLabel[0].style.fontStyle).toBe('');
      expect(button.$buttonLabel[0].style.fontWeight).toBe('');

      button.setForegroundColor('green');
      expect(button.$field[0].style.color).toBe('green');
      expect(button.$field[0].style.backgroundColor).toBe('yellow');
      expect(button.$field[0].style.fontFamily).toBe('');
      expect(button.$field[0].style.fontSize).toBe('');
      expect(button.$field[0].style.fontStyle).toBe('');
      expect(button.$field[0].style.fontWeight).toBe('');
      expect(button.$buttonLabel[0].style.color).toBe('green');
      expect(button.$buttonLabel[0].style.backgroundColor).toBe('');
      expect(button.$buttonLabel[0].style.fontFamily).toBe('');
      expect(button.$buttonLabel[0].style.fontSize).toBe('');
      expect(button.$buttonLabel[0].style.fontStyle).toBe('');
      expect(button.$buttonLabel[0].style.fontWeight).toBe('');

      button.setFont('bold');
      expect(button.$field[0].style.color).toBe('green');
      expect(button.$field[0].style.backgroundColor).toBe('yellow');
      expect(button.$field[0].style.fontFamily).toBe('');
      expect(button.$field[0].style.fontSize).toBe('');
      expect(button.$field[0].style.fontStyle).toBe('');
      expect(button.$field[0].style.fontWeight).toBe('bold');
      expect(button.$buttonLabel[0].style.color).toBe('green');
      expect(button.$buttonLabel[0].style.backgroundColor).toBe('');
      expect(button.$buttonLabel[0].style.fontFamily).toBe('');
      expect(button.$buttonLabel[0].style.fontSize).toBe('');
      expect(button.$buttonLabel[0].style.fontStyle).toBe('');
      expect(button.$buttonLabel[0].style.fontWeight).toBe('bold');
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

  describe('click event', function() {

    it('is triggered when doAction is called', function() {
      var button = scout.create('Button', {
        parent: session.desktop
      });
      var executed = 0;
      button.on('click', function(event) {
        executed++;
      });

      expect(executed).toBe(0);
      button.doAction();
      expect(executed).toBe(1);
    });

    it('is fired when doAction is called even if it is a toggle button', function() {
      var button = scout.create('Button', {
        parent: session.desktop,
        displayStyle: scout.Button.DisplayStyle.TOGGLE
      });
      var executed = 0;
      var selected = null;
      button.on('click', function(event) {
        // State is already changed so that listener can react on new state
        selected = button.selected;
        executed++;
      });
      expect(executed).toBe(0);
      expect(selected).toBe(null);

      button.doAction();
      expect(executed).toBe(1);
      expect(selected).toBe(true);

      button.doAction();
      expect(executed).toBe(2);
      expect(selected).toBe(false);
    });

  });

});
