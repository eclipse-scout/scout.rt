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

static TEXT_KEY_REGEX = /\$\{textKey\:([a-zA-Z0-9\.]*)\}/;

/**
 * Returns the text for the given key.
 * If the key does not exist in this text map, a lookup in the parent text map is done.
 *
 * @param textKey key to lookup the text
 * @param vararg texts to replace the placeholders specified by {0}, {1}, etc.
 */
get(textKey) {
  if (!this._exists(textKey)) {
    if (this.parent) {
      return this.parent.get(...arguments);
    }
    return '[undefined text: ' + textKey + ']';
  }
  var len = arguments.length,
    text = this.map[textKey];

  if (len === 1) {
    return text;
  }

  for (var i = 1; i < len; i++) {
    text = text.replace(new RegExp('\\{' + (i - 1) + '\\}', 'g'), arguments[i]);
  }
  return text;
}

optGet(textKey, defaultValue) {
  if (!this._exists(textKey)) {
    if (this.parent) {
      return this.parent.optGet(...arguments);
    }
    return defaultValue;
  }
  if (arguments.length > 2) {
    // dynamically call text() without 'defaultValue' argument
    var args = [...arguments].slice(2);
    args.unshift(textKey); // add textKey as first argument
    return this.get(...args);
  }
  return this.get(textKey);
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
