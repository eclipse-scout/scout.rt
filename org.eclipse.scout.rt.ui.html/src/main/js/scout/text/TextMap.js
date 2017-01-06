/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TextMap = function(textMap) {
  this.map = textMap || {};
};

scout.TextMap.TEXT_KEY_REGEX = /\$\{textKey\:([a-zA-Z0-9\.]*)\}/;

/**
 * Returns the text for the given key.
 * If the key does not exist in this text map, a lookup in the parent text map is done.
 *
 * @param textKey key to lookup the text
 * @param vararg texts to replace the placeholders specified by {0}, {1}, etc.
 */
scout.TextMap.prototype.get = function(textKey) {
  if (!this._exists(textKey)) {
    if (this.parent) {
      return scout.TextMap.prototype.get.apply(this.parent, arguments);
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
};

scout.TextMap.prototype.optGet = function(textKey, defaultValue) {
  if (!this._exists(textKey)) {
    if (this.parent) {
      return scout.TextMap.prototype.optGet.apply(this.parent, arguments);
    }
    return defaultValue;
  }
  if (arguments.length > 2) {
    // dynamically call text() without 'defaultValue' argument
    var args = Array.prototype.slice.call(arguments, 2);
    args.unshift(textKey); // add textKey as first argument
    return scout.TextMap.prototype.get.apply(this, args);
  }
  return this.get(textKey);
};

scout.TextMap.prototype.exists = function(textKey) {
  if (this._exists(textKey)) {
    return true;
  }
  if (this.parent) {
    return this.parent.exists(textKey);
  }
  return false;
};

scout.TextMap.prototype._exists = function(textKey) {
  return this.map.hasOwnProperty(textKey);
};

scout.TextMap.prototype.add = function(textKey, text) {
  this.map[textKey] = text;
};

/**
 * Adds all texts from the given textMap to this textMap
 * @param {Object|scout.TextMap} textMap either a plain object or a {@link scout.TextMap}
 */
scout.TextMap.prototype.addAll = function(textMap) {
  if (!textMap) {
    return;
  }
  if (textMap instanceof scout.TextMap) {
    textMap = textMap.map;
  }
  scout.objects.copyOwnProperties(textMap, this.map);
};

scout.TextMap.prototype.setParent = function(parent) {
  this.parent = parent;
};

scout.TextMap.prototype.remove = function(textKey) {
  delete this.map[textKey];
};
