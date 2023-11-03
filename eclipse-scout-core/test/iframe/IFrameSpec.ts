/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, IFrame, keys, scout, WidgetField} from '../../src/index';
import {JQueryTesting, KeyStrokeModifier} from '../../src/testing';

describe('IFrame', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('setLocation', () => {
    it('sets the location of the iframe', () => {
      let iframe = scout.create(IFrame, {
        parent: session.desktop
      });
      iframe.render();
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');

      iframe.setLocation('https://www.bing.com');
      expect(iframe.location).toBe('https://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('https://www.bing.com');
    });

    it('sets the location to about:blank if location is empty', () => {
      let iframe = scout.create(IFrame, {
        parent: session.desktop,
        location: 'https://www.bing.com'
      });
      iframe.render();
      expect(iframe.location).toBe('https://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('https://www.bing.com');

      iframe.setLocation(null);
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');
    });
  });

  describe('keystrokes', () => {
    let iframe: IFrame;
    let outerField: WidgetField;
    let pressed;

    beforeEach(() => {
      iframe = scout.create(IFrame, {
        parent: session.desktop,
        location: '_res/Inputs.html',
        sandboxEnabled: false
      });
      outerField = scout.create(WidgetField, {
        parent: session.desktop,
        fieldWidget: iframe,
        keyStrokes: [{
          id: 'KeyStroke',
          objectType: Action
        }]
      });
      outerField.render();
      outerField.widget('KeyStroke').on('action', () => {
        pressed = true;
      });
      pressed = false;
    });

    function focusElemAndTriggerKey(doc: Document, id: string, key: number, modifier?: KeyStrokeModifier) {
      let elem = doc.getElementById(id);
      elem.focus();
      JQueryTesting.triggerKeyInputCapture($(elem), key, modifier);
    }

    it('work even if focus is in iframe ', async () => {
      outerField.widget('KeyStroke', Action).setKeyStroke('ESC');
      let doc = await JQueryTesting.whenDocLoad(iframe.$iframe);
      focusElemAndTriggerKey(doc, 'no_input', keys.ESC);
      expect(pressed).toBe(true);
    });

    it('are partially disabled if focus is in an input of the iframe', async () => {
      outerField.widget('KeyStroke', Action).setKeyStroke('BACKSPACE');
      let doc = await JQueryTesting.whenDocLoad(iframe.$iframe);
      focusElemAndTriggerKey(doc, 'input', keys.BACKSPACE);
      expect(pressed).toBe(false);

      focusElemAndTriggerKey(doc, 'no_input', keys.BACKSPACE);
      expect(pressed).toBe(true);
    });

    it('are partially disabled if focus is on a button of the iframe', async () => {
      outerField.widget('KeyStroke', Action).setKeyStroke('SPACE');
      let doc = await JQueryTesting.whenDocLoad(iframe.$iframe);
      focusElemAndTriggerKey(doc, 'button', keys.SPACE);
      expect(pressed).toBe(false);

      focusElemAndTriggerKey(doc, 'input_button', keys.SPACE);
      expect(pressed).toBe(false);

      focusElemAndTriggerKey(doc, 'no_input', keys.SPACE);
      expect(pressed).toBe(true);
    });

    it('are partially disabled if focus is in an textarea of the iframe', async () => {
      outerField.widget('KeyStroke', Action).setKeyStroke('ctrl-shift-up');
      let doc = await JQueryTesting.whenDocLoad(iframe.$iframe);
      focusElemAndTriggerKey(doc, 'input', keys.UP, 'ctrl-shift');
      expect(pressed).toBe(true);

      // Textarea is a multiline input -> ctrl-shift-up must not be propagated
      pressed = false;
      focusElemAndTriggerKey(doc, 'textarea', keys.UP, 'ctrl-shift');
      expect(pressed).toBe(false);

      // Also don't propagate enter
      pressed = false;
      outerField.widget('KeyStroke', Action).setKeyStroke('enter');
      focusElemAndTriggerKey(doc, 'textarea', keys.ENTER);
      expect(pressed).toBe(false);
    });
  });
});
