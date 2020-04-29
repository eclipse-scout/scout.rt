/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects} from '../index';

export default class TextMap {

  constructor(textMap) {
    this.map = textMap || {};
  }

  static TEXT_KEY_REGEX = /\${textKey:([a-zA-Z0-9.]*)}/;

  /**
   * Returns the text for the given key.
   * If the key does not exist in this text map, a lookup in the parent text map is done.
   *
   * @param textKey key to lookup the text
   * @param vararg texts to replace the placeholders specified by {0}, {1}, etc.
   */
  get(textKey, ...vararg) {
    if (!this._exists(textKey)) {
      if (this.parent) {
        return this.parent.get(textKey, ...vararg);
      }
      return '[undefined text: ' + textKey + ']';
    }
    let len = vararg.length;
    let text = this.map[textKey];

    if (len === 0) {
      return text;
    }

    for (let i = 0; i < len; i++) {
      text = text.replace(new RegExp('\\{' + (i) + '\\}', 'g'), vararg[i]);
    }
    return text;
  }

  optGet(textKey, defaultValue, ...vararg) {
    if (!this._exists(textKey)) {
      if (this.parent) {
        return this.parent.optGet(textKey, defaultValue, ...vararg);
      }
      return defaultValue;
    }
    return this.get(textKey, ...vararg);
  }

  exists(textKey) {
    if (this._exists(textKey)) {
      return true;
    }
    if (this.parent) {
      return this.parent.exists(textKey);
    }
    return false;
  }

  _exists(textKey) {
    return this.map.hasOwnProperty(textKey);
  }

  add(textKey, text) {
    this.map[textKey] = text;
  }

  /**
   * Adds all texts from the given textMap to this textMap
   * @param {Object|TextMap} textMap either a plain object or a {@link TextMap}
   */
  addAll(textMap) {
    if (!textMap) {
      return;
    }
    if (textMap instanceof TextMap) {
      textMap = textMap.map;
    }
    objects.copyOwnProperties(textMap, this.map);
  }

  setParent(parent) {
    this.parent = parent;
  }

  remove(textKey) {
    delete this.map[textKey];
  }
}
