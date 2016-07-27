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
scout.Texts = function(textMap) {
  this._textMap = textMap || {};
};

scout.Texts.TEXT_KEY_REGEX = /\$\{textKey\:([a-zA-Z0-9\.]*)\}/;

/**
 * Returns the text for the given key.
 * If the key does not exist in this text map, a lookup in the parent text map is done.
 *
 * @param textKey key to lookup the text
 * @param vararg texts to replace the placeholders specified by {0}, {1}, etc.
 */
scout.Texts.prototype.get = function(textKey) {
  if (!this._exists(textKey)) {
    if (this.parent) {
      return scout.Texts.prototype.get.apply(this.parent, arguments);
    }
    return '[undefined text: ' + textKey + ']';
  }
  var len = arguments.length,
    text = this._textMap[textKey];

  if (len === 1) {
    return text;
  }

  for (var i = 1; i < len; i++) {
    text = text.replace(new RegExp('\\{' + (i - 1) + '\\}', 'g'), arguments[i]);
  }
  return text;
};

scout.Texts.prototype.optGet = function(textKey, defaultValue) {
  if (!this._exists(textKey)) {
    if (this.parent) {
      return scout.Texts.prototype.optGet.apply(this.parent, arguments);
    }
    return defaultValue;
  }
  if (arguments.length > 2) {
    // dynamically call text() without 'defaultValue' argument
    var args = Array.prototype.slice.call(arguments, 2);
    args.unshift(textKey); // add textKey as first argument
    return scout.Texts.prototype.get.apply(this, args);
  }
  return this.get(textKey);
};

scout.Texts.prototype.exists = function(textKey) {
  if (this._exists(textKey)) {
    return true;
  }
  if (this.parent) {
    return this.parent.exists(textKey);
  }
  return false;
};

scout.Texts.prototype._exists = function(textKey) {
  return this._textMap.hasOwnProperty(textKey);
};

scout.Texts.prototype.setParent = function(parent) {
  this.parent = parent;
};

// ----- static methods -----

/**
 * Extracts NLS texts from the DOM tree. Texts are expected in the following format:
 *
 *   <scout-text data-key="..." data-value="..." />
 *
 * This method returns a map with all found texts. It must be called before scout.prepareDOM()
 * is called, as that method removes all <scout-text> tags.
 */
scout.Texts.readFromDOM = function() {
  var textMap = {};
  $('scout-text').each(function() {
    // No need to unescape strings (the browser did this already)
    var key = $(this).data('key');
    var value = $(this).data('value');
    textMap[key] = value;
  });
  return textMap;
};

scout.Texts.resolveKey = function(value) {
  var result = this.TEXT_KEY_REGEX.exec(value);
  if (result && result.length === 2) {
    return result[1];
  }
};
