scout.Texts = function(textMap) {
  this._textMap = textMap || {};
};

/**
 * @param textKey key to lookup the text
 * @param vararg texts to replace the placeholders specified by {0}, {1}, etc.
 */
scout.Texts.prototype.get = function(textKey) {
  if (!this.exists(textKey)) {
    return '[undefined text: ' + textKey + ']';
  }
  var i, placeholder,
    len = arguments.length,
    text = this._textMap[textKey];

  if (len === 1) {
    return text;
  }

  for (i = 1; i < len; i++) {
    placeholder = '{' + (i - 1) + '}';
    text = text.replace(placeholder, arguments[i]);
  }
  return text;
};

scout.Texts.prototype.optGet = function(textKey, defaultValue) {
  if (!this.exists(textKey)) {
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
  return this._textMap.hasOwnProperty(textKey);
};
