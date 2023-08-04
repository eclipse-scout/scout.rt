/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, Form, GroupBox, icons, Menu, scout, WrappedFormField} from '../../../../src/index';

describe('Button', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('setLabel', () => {
    it('toggles the class with-label on the icon', () => {
      let button = scout.create(Button, {
        parent: session.desktop,
        label: 'label',
        iconId: icons.ANGLE_DOWN
      });
      button.render();
      expect(button.$field.data('$icon')).toHaveClass('with-label');

      button.setLabel(null);
      expect(button.$field.data('$icon')).not.toHaveClass('with-label');

      button.setLabel('a new label');
      expect(button.$field.data('$icon')).toHaveClass('with-label');
    });
  });

  describe('setIconId', () => {
    it('toggles the class with-label on the icon', () => {
      let button = scout.create(Button, {
        parent: session.desktop,
        label: 'label',
        iconId: icons.ANGLE_DOWN
      });
      button.render();
      expect(button.$field.data('$icon')).toHaveClass('with-label');

      button.setIconId(null);
      expect(button.$field.data('$icon')).toBeFalsy();

      button.setIconId(icons.ANGLE_UP);
      expect(button.$field.data('$icon')).toHaveClass('with-label');
    });
  });

  describe('setting legacy styles', () => {
    it('sets style attributes', () => {
      let button = scout.create(Button, {
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

  describe('keyStrokeScope', () => {
    it('may be an id of a form field and will be resolved when initialized', () => {
      let form = scout.create(Form, {
        id: 'myForm',
        parent: session.desktop,
        rootGroupBox: {
          id: 'myMainBox',
          objectType: GroupBox,
          fields: [{
            id: 'myButton',
            objectType: Button,
            keyStroke: 'ctrl-1',
            keyStrokeScope: 'myMainBox'
          }]
        }
      });
      let button = form.widget('myButton', Button);
      expect(button.keyStrokeScope).toBe(form.rootGroupBox);
    });

    it('may be an an outer form', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        id: 'outerForm',
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: WrappedFormField,
            innerForm: {
              id: 'innerForm',
              objectType: Form,
              rootGroupBox: {
                objectType: GroupBox,
                fields: [{
                  id: 'myButton',
                  objectType: Button,
                  keyStroke: 'ctrl-1',
                  keyStrokeScope: 'outerForm'
                }]
              }
            }
          }]
        }
      });
      let button = form.widget('myButton', Button);
      expect(button.keyStrokeScope).toBe(form);
    });
  });

  describe('click event', () => {

    it('is triggered when doAction is called', () => {
      let button = scout.create(Button, {
        parent: session.desktop
      });
      let executed = 0;
      button.on('click', event => {
        executed++;
      });

      expect(executed).toBe(0);
      button.doAction();
      expect(executed).toBe(1);
    });

    it('is fired when doAction is called even if it is a toggle button', () => {
      let button = scout.create(Button, {
        parent: session.desktop,
        displayStyle: Button.DisplayStyle.TOGGLE
      });
      let executed = 0;
      let selected = null;
      button.on('click', event => {
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

  describe('aria properties', () => {

    it('has aria role link if it is Button.DisplayStyle.LINK', () => {
      // in case of default button it is already a button html element, so no aria needed
      let defaultButton = scout.create(Button, {
        parent: session.desktop,
        label: 'label',
        displayStyle: Button.DisplayStyle.DEFAULT
      });
      defaultButton.render();
      expect(defaultButton.$field).not.toHaveAttr('role');

      let linkButton = scout.create(Button, {
        parent: session.desktop,
        label: 'label',
        displayStyle: Button.DisplayStyle.LINK
      });
      linkButton.render();
      expect(linkButton.$field).toHaveAttr('role', 'link');
    });

    it('has attribute aria-haspopup set to menu if it has sub menus', () => {
      let button = scout.create(Button, {
        parent: session.desktop,
        label: 'label'
      });
      button.render();
      expect(button.$field).not.toHaveAttr('aria-haspopup');
      expect(button.$field).not.toHaveAttr('aria-expanded');
      button.insertMenu(scout.create(Menu, {
        parent: button
      }));
      expect(button.$field).toHaveAttr('aria-haspopup', 'menu');
      expect(button.$field).toHaveAttr('aria-expanded', 'false');
    });

    it('has aria pressed set correctly if toggle action', () => {
      let button = scout.create(Button, {
        parent: session.desktop,
        label: 'label',
        displayStyle: Button.DisplayStyle.TOGGLE
      });
      button.render();
      expect(button.$field).toHaveAttr('aria-pressed', 'false');
      button.setSelected(true);
      expect(button.$field).toHaveAttr('aria-pressed', 'true');
    });
  });
});
