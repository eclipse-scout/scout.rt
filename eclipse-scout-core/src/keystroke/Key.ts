/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys} from '../index';

export default class Key {

  constructor(keyStroke, which) {
    this.keyStroke = keyStroke;
    this.which = which;

    this.ctrl = keyStroke.ctrl;
    this.alt = keyStroke.alt;
    this.shift = keyStroke.shift;

    this.keyStrokeMode = keyStroke.keyStrokeMode;
  }

  render($drawingArea, event) {
    this.$drawingArea = this.keyStroke.renderKeyBox($drawingArea, event);
    return !!this.$drawingArea;
  }

  remove() {
    this.keyStroke.removeKeyBox(this.$drawingArea);
    this.$drawingArea = null;
  }

  toKeyStrokeString() {
    let keyStroke = '';
    if (this.ctrl) {
      keyStroke += 'Ctrl-';
    }
    if (this.alt) {
      keyStroke += 'Alt-';
    }
    if (this.shift) {
      keyStroke += 'Shift-';
    }
    let key = keys.codesToKeys[this.which];
    if (key === undefined) {
      key = this.which;
    }
    keyStroke += key;
    return keyStroke;
  }
}
