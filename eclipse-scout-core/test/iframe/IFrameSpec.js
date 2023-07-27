// noinspection HttpUrlsUsage

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
import {keys, scout} from '../../src/index';

describe('IFrame', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('setLocation', () => {
    it('sets the location of the iframe', () => {
      let iframe = scout.create('IFrame', {
        parent: session.desktop
      });
      iframe.render();
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');

      iframe.setLocation('http://www.bing.com');
      expect(iframe.location).toBe('http://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('http://www.bing.com');
    });

    it('sets the location to about:blank if location is empty', () => {
      let iframe = scout.create('IFrame', {
        parent: session.desktop,
        location: 'http://www.bing.com'
      });
      iframe.render();
      expect(iframe.location).toBe('http://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('http://www.bing.com');

      iframe.setLocation(null);
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');
    });
  });

  describe('keystrokes', () => {
    let iframe;
    let outerField;
    let pressed;

    beforeEach(() => {
      iframe = scout.create('IFrame', {
        parent: session.desktop,
        location: '_res/Inputs.html',
        sandboxEnabled: false
      });
      outerField = scout.create('WidgetField', {
        parent: session.desktop,
        fieldWidget: iframe,
        keyStrokes: [{
          id: 'KeyStroke',
          objectType: 'Action'
        }]
      });
      outerField.render();
      outerField.widget('KeyStroke').on('action', () => {
        pressed = true;
      });
      pressed = false;
    });

    function whenDocLoad() {
      let def = $.Deferred();
      iframe.$iframe.on('load', () => {
        def.resolve(iframe.$iframe[0].contentDocument);
      });
      return def.promise();
    }

    function focusElemAndTriggerKey(doc, id, key, modifier) {
      let elem = doc.getElementById(id);
      elem.focus();
      $(elem).triggerKeyInputCapture(key, modifier);
    }

    it('work even if focus is in iframe ', async () => {
      outerField.widget('KeyStroke').setKeyStroke('ESC');
      let doc = await whenDocLoad();
      focusElemAndTriggerKey(doc, 'no_input', keys.ESC);
      expect(pressed).toBe(true);
    });

    it('are partially disabled if focus is in an input of the iframe', async () => {
      outerField.widget('KeyStroke').setKeyStroke('BACKSPACE');
      let doc = await whenDocLoad();
      focusElemAndTriggerKey(doc, 'input', keys.BACKSPACE);
      expect(pressed).toBe(false);

      focusElemAndTriggerKey(doc, 'no_input', keys.BACKSPACE);
      expect(pressed).toBe(true);
    });

    it('are partially disabled if focus is on a button of the iframe', async () => {
      outerField.widget('KeyStroke').setKeyStroke('SPACE');
      let doc = await whenDocLoad();
      focusElemAndTriggerKey(doc, 'button', keys.SPACE);
      expect(pressed).toBe(false);

      focusElemAndTriggerKey(doc, 'input_button', keys.SPACE);
      expect(pressed).toBe(false);

      focusElemAndTriggerKey(doc, 'no_input', keys.SPACE);
      expect(pressed).toBe(true);
    });

    it('are partially disabled if focus is in an textarea of the iframe', async () => {
      outerField.widget('KeyStroke').setKeyStroke('ctrl-shift-up');
      let doc = await whenDocLoad();
      focusElemAndTriggerKey(doc, 'input', keys.UP, 'ctrl-shift');
      expect(pressed).toBe(true);

      // Textarea is a multiline input -> ctrl-shift-up must not be propagated
      pressed = false;
      focusElemAndTriggerKey(doc, 'textarea', keys.UP, 'ctrl-shift');
      expect(pressed).toBe(false);

      // Also don't propagate enter
      pressed = false;
      outerField.widget('KeyStroke').setKeyStroke('enter');
      focusElemAndTriggerKey(doc, 'textarea', keys.ENTER);
      expect(pressed).toBe(false);
    });
  });
});
