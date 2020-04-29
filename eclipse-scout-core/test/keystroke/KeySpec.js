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
import {Key, KeyStroke} from '../../src/index';

describe('Key', () => {

  describe('toKeyStrokeString', () => {

    it('creates a string representing that key', () => {
      let key = new Key(new KeyStroke());
      key.which = 13;
      expect(key.toKeyStrokeString()).toBe('Enter');
    });

    it('considers modifiers', () => {
      let key = new Key(new KeyStroke());
      key.ctrl = true;
      key.which = 13;
      expect(key.toKeyStrokeString()).toBe('Ctrl-Enter');

      key = new Key(new KeyStroke());
      key.ctrl = true;
      key.shift = true;
      key.which = 13;
      expect(key.toKeyStrokeString()).toBe('Ctrl-Shift-Enter');

      key = new Key(new KeyStroke());
      key.ctrl = true;
      key.shift = true;
      key.alt = true;
      key.which = 13;
      expect(key.toKeyStrokeString()).toBe('Ctrl-Alt-Shift-Enter');

      key = new Key(new KeyStroke());
      key.ctrl = false;
      key.shift = false;
      key.alt = true;
      key.which = 13;
      expect(key.toKeyStrokeString()).toBe('Alt-Enter');
    });

  });
});
