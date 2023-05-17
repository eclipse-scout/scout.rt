/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {FormSpecHelper, JQueryTesting} from '../../src/testing';
import {Form, InputFieldKeyStrokeContext, keys, KeyStroke, keyStrokeModifier, scout, ScoutKeyboardEvent, StringField, VirtualKeyStrokeEvent, Widget} from '../../src';

describe('InputFieldKeyStrokeContext', () => {
  let session: SandboxSession,
    helper: FormSpecHelper,
    form: Form,
    stringField: SpecStringField,
    $input: JQuery,
    keyStrokeContext: SpecInputFieldKeyStrokeContext;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    form = helper.createFormWithFields(session.desktop, true);
    form.render();
    stringField = scout.create(SpecStringField, {
      parent: form.rootGroupBox
    });
    form.rootGroupBox.insertField(stringField);
    $input = stringField.$field;
    keyStrokeContext = stringField.keyStrokeContext;
  });

  function createKeyStrokeEvent(which: number, options?: { ctrl?: boolean; alt?: boolean; shift?: boolean }): VirtualKeyStrokeEvent {
    const ctrl = scout.nvl(options && options.ctrl, false),
      alt = scout.nvl(options && options.alt, false),
      shift = scout.nvl(options && options.shift, false);

    return new VirtualKeyStrokeEvent(which, ctrl, alt, shift, KeyStroke.Mode.DOWN, $input.get(0));
  }

  function triggerInput(which: number) {
    JQueryTesting.triggerKeyDown($input, which);
  }

  function createKeyStroke(which: number, field: Widget, handle: (event: JQuery.KeyboardEventBase) => void, options?: { ctrl?: boolean; alt?: boolean; shift?: boolean }): KeyStroke {
    const keyStroke = new KeyStroke();
    keyStroke.field = field;
    keyStroke.which = [which];
    keyStroke.ctrl = scout.nvl(options && options.ctrl, false);
    keyStroke.alt = scout.nvl(options && options.alt, false);
    keyStroke.shift = scout.nvl(options && options.shift, false);
    keyStroke.handle = handle;
    return keyStroke;
  }

  class SpecStringField extends StringField {
    declare keyStrokeContext: SpecInputFieldKeyStrokeContext;

    protected override _createKeyStrokeContext(): SpecInputFieldKeyStrokeContext {
      return new SpecInputFieldKeyStrokeContext();
    }
  }

  class SpecInputFieldKeyStrokeContext extends InputFieldKeyStrokeContext {
    declare _stopPropagationKeys: Record<number, number[]>;

    constructor(multiline?: boolean) {
      super(multiline);
    }

    override _applyPropagationFlags(event: ScoutKeyboardEvent) {
      super._applyPropagationFlags(event);
    }

    override _isInputEvent(event: ScoutKeyboardEvent): boolean {
      return super._isInputEvent(event);
    }

    override _isInputField(element: HTMLElement): boolean {
      return super._isInputField(element);
    }

    override _isLetterKeyStroke(event: ScoutKeyboardEvent): boolean {
      return super._isLetterKeyStroke(event);
    }

    override _isNumberKeyStroke(event: ScoutKeyboardEvent): boolean {
      return super._isNumberKeyStroke(event);
    }
  }

  describe('accept', () => {

    it('prevents triggering of key strokes in other contexts listening on input events', () => {
      let formEnterTriggered = false,
        formATriggered = false,
        form7Triggered = false,
        formDeleteTriggered = false;
      form.registerKeyStrokes([
        createKeyStroke(keys.ENTER, form, e => {
          formEnterTriggered = true;
        }),
        createKeyStroke(keys.A, form, e => {
          formATriggered = true;
        }),
        createKeyStroke(keys['7'], form, e => {
          form7Triggered = true;
        }),
        createKeyStroke(keys.DELETE, form, e => {
          formDeleteTriggered = true;
        })
      ]);

      let fieldEnterTriggered = false,
        fieldATriggered = false,
        field7Triggered = false,
        fieldDeleteTriggered = false;
      stringField.registerKeyStrokes([
        createKeyStroke(keys.ENTER, stringField, e => {
          fieldEnterTriggered = true;
        }),
        createKeyStroke(keys.A, stringField, e => {
          fieldATriggered = true;
        }),
        createKeyStroke(keys['7'], stringField, e => {
          field7Triggered = true;
        }),
        createKeyStroke(keys.DELETE, stringField, e => {
          fieldDeleteTriggered = true;
        })
      ]);

      expect(formEnterTriggered).toBeFalse();
      expect(formATriggered).toBeFalse();
      expect(form7Triggered).toBeFalse();
      expect(formDeleteTriggered).toBeFalse();

      expect(fieldEnterTriggered).toBeFalse();
      expect(fieldATriggered).toBeFalse();
      expect(field7Triggered).toBeFalse();
      expect(fieldDeleteTriggered).toBeFalse();

      triggerInput(keys.ENTER);
      expect(formEnterTriggered).toBeTrue();
      expect(fieldEnterTriggered).toBeTrue();

      triggerInput(keys.A);
      expect(formATriggered).toBeFalse();
      expect(fieldATriggered).toBeTrue();

      triggerInput(keys['7']);
      expect(form7Triggered).toBeFalse();
      expect(field7Triggered).toBeTrue();

      triggerInput(keys.DELETE);
      expect(formDeleteTriggered).toBeFalse();
      expect(fieldDeleteTriggered).toBeTrue();
    });
  });

  describe('_applyPropagationFlags', () => {

    it('stops propagation of input events', () => {
      // not a stop propagation key
      let event = createKeyStrokeEvent(keys.ENTER);
      keyStrokeContext._applyPropagationFlags(event);
      expect(event.isPropagationStopped()).toBeFalse();

      // not a stop propagation key, but input event (target + letter)
      event = createKeyStrokeEvent(keys.A);
      keyStrokeContext._applyPropagationFlags(event);
      expect(event.isPropagationStopped()).toBeTrue();

      // not a stop propagation key, but input event (target + number)
      event = createKeyStrokeEvent(keys['7']);
      keyStrokeContext._applyPropagationFlags(event);
      expect(event.isPropagationStopped()).toBeTrue();

      // stop propagation key
      event = createKeyStrokeEvent(keys.DELETE);
      keyStrokeContext._applyPropagationFlags(event);
      expect(event.isPropagationStopped()).toBeTrue();
    });
  });

  describe('_isInputEvent', () => {

    it('is true if is letter or number and target is input field', () => {
      // input field
      let event = createKeyStrokeEvent(keys.ENTER);
      expect(keyStrokeContext._isInputEvent(event)).toBeFalse();

      event = createKeyStrokeEvent(keys.A);
      expect(keyStrokeContext._isInputEvent(event)).toBeTrue();

      event = createKeyStrokeEvent(keys['7']);
      expect(keyStrokeContext._isInputEvent(event)).toBeTrue();

      event = createKeyStrokeEvent(keys.DELETE);
      expect(keyStrokeContext._isInputEvent(event)).toBeFalse();

      // no input field
      event = createKeyStrokeEvent(keys.ENTER);
      event.target = stringField.$container.get(0);
      expect(keyStrokeContext._isInputEvent(event)).toBeFalse();

      event = createKeyStrokeEvent(keys.A);
      event.target = stringField.$container.get(0);
      expect(keyStrokeContext._isInputEvent(event)).toBeFalse();

      event = createKeyStrokeEvent(keys['7']);
      event.target = stringField.$container.get(0);
      expect(keyStrokeContext._isInputEvent(event)).toBeFalse();

      event = createKeyStrokeEvent(keys.DELETE);
      event.target = stringField.$container.get(0);
      expect(keyStrokeContext._isInputEvent(event)).toBeFalse();
    });

    it('_isInputField is true if target is an input field', () => {
      const event = createKeyStrokeEvent(keys.ENTER);
      expect(keyStrokeContext._isInputField(event.target)).toBeTrue();

      event.target = stringField.$container.get(0);
      expect(keyStrokeContext._isInputField(event.target)).toBeFalse();
    });

    it('_isLetterKeyStroke is true if is letter', () => {
      // none
      let event = createKeyStrokeEvent(keys.A);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys.B);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys.C);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys['3']);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['7']);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.ENTER);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.RIGHT);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.HOME);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.BACKSPACE);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SEMICOLON);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SPACE);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.DELETE);
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();

      // shift
      event = createKeyStrokeEvent(keys.A, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys.B, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys.C, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys['3'], {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['7'], {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.ENTER, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.RIGHT, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.HOME, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.BACKSPACE, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SEMICOLON, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SPACE, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.DELETE, {shift: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();

      // ctrl
      event = createKeyStrokeEvent(keys.A, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.B, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.C, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['3'], {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['7'], {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.ENTER, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.RIGHT, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.HOME, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.BACKSPACE, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SEMICOLON, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SPACE, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.DELETE, {ctrl: true});
      expect(keyStrokeContext._isLetterKeyStroke(event)).toBeFalse();
    });

    it('_isNumberKeyStroke is true if is number', () => {
      // none
      let event = createKeyStrokeEvent(keys.A);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.B);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.C);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['3']);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys['7']);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys.ENTER);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.RIGHT);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.HOME);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.BACKSPACE);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SEMICOLON);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SPACE);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.DELETE);
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();

      // shift
      event = createKeyStrokeEvent(keys.A, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.B, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.C, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['3'], {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys['7'], {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeTrue();
      event = createKeyStrokeEvent(keys.ENTER, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.RIGHT, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.HOME, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.BACKSPACE, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SEMICOLON, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SPACE, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.DELETE, {shift: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();

      // ctrl
      event = createKeyStrokeEvent(keys.A, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.B, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.C, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['3'], {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys['7'], {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.ENTER, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.RIGHT, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.HOME, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.BACKSPACE, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SEMICOLON, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.SPACE, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
      event = createKeyStrokeEvent(keys.DELETE, {ctrl: true});
      expect(keyStrokeContext._isNumberKeyStroke(event)).toBeFalse();
    });
  });

  describe('stopPropagationKeys', () => {

    it('are initialized correctly', () => {
      expect(Object.keys(keyStrokeContext._stopPropagationKeys).length).toBe(4);
      expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL]).toEqual([
        keys.A,
        keys.C,
        keys.Y,
        keys.V,
        keys.Z,
        keys.BACKSPACE,
        keys.RIGHT,
        keys.LEFT,
        keys.HOME,
        keys.END
      ]);
      expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT]).toEqual([
        keys.RIGHT,
        keys.LEFT,
        keys.HOME,
        keys.END
      ]);
      expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.SHIFT]).toEqual([
        keys.RIGHT,
        keys.LEFT,
        keys.HOME,
        keys.END
      ]);
      expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.NONE]).toEqual([
        keys.SEMICOLON,
        keys.DASH,
        keys.COMMA,
        keys.POINT,
        keys.FORWARD_SLASH,
        keys.OPEN_BRACKET,
        keys.BACK_SLASH,
        keys.CLOSE_BRACKET,
        keys.SINGLE_QUOTE,
        keys.MULTIPLY,
        keys.ADD,
        keys.SUBTRACT,
        keys.DECIMAL_POINT,
        keys.DIVIDE,
        keys.NUMPAD_0,
        keys.NUMPAD_1,
        keys.NUMPAD_2,
        keys.NUMPAD_3,
        keys.NUMPAD_4,
        keys.NUMPAD_5,
        keys.NUMPAD_6,
        keys.NUMPAD_7,
        keys.NUMPAD_8,
        keys.NUMPAD_9,
        keys.MULTIPLY,
        keys.BACKSPACE,
        keys.DELETE,
        keys.SPACE,
        keys.RIGHT,
        keys.LEFT,
        keys.HOME,
        keys.END
      ]);
    });

    it('are updated correctly when multiline property changes', () => {
      const multilineNavigationKeys = [
        keys.UP,
        keys.DOWN
      ];

      multilineNavigationKeys.forEach(key => {
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL]).not.toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT]).not.toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.SHIFT]).not.toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.NONE]).not.toContain(key);
      });

      keyStrokeContext.setMultiline(true);
      multilineNavigationKeys.forEach(key => {
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL]).toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT]).toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.SHIFT]).toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.NONE]).toContain(key);
      });

      keyStrokeContext.setMultiline(false);
      multilineNavigationKeys.forEach(key => {
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL]).not.toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.CTRL | keyStrokeModifier.SHIFT]).not.toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.SHIFT]).not.toContain(key);
        expect(keyStrokeContext._stopPropagationKeys[keyStrokeModifier.NONE]).not.toContain(key);
      });
    });
  });
});
