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
scout.textProperties = {

  TEXT_KEY_REGEX: /\$\{textKey\:([a-zA-Z0-9\.]*)\}/,

  _textMap: {},

  bootstrap: function() {
    var that = this;

    // FIXME [awe] 6.1: load texts with different locales
    // - define which locales are available (or simply try to load a property file, and use default if it does not exist?)
    // - define which local is currently used, because this info is required to load the proper file
    return $.ajax({
      url: 'res/texts.properties',
      dataType: 'text',
      contentType: 'text/plain; charset=UTF-8'
    }).done(that._onTextsDone.bind(that))
      .fail(that._onTextsFail.bind(that));
  },

  _onTextsDone: function(data) {
    var keyValue, key, value,
      lines = data.split('\n'),
      textMap = {};
    lines.forEach(function(line) {
      keyValue = line.split('=');
      if (keyValue.length === 2) {
        key = keyValue[0].trim();
        value = keyValue[1].trim();
        textMap[key] = value;
      }
    });
    this._textMap = textMap;
  },

  _onTextsFail: function(jqXHR, textStatus, errorThrown) {
    throw new Error('Error while loading texts: ' + errorThrown);
  },

  getTextMap: function() {
    return this._textMap;
  },

  resolveTextKeys: function(value) {
    var textKey,
      result = this.TEXT_KEY_REGEX.exec(value);
    if (result && result.length === 2) {
      textKey = result[1];
      value = this._textMap[textKey];
    }
    return value;
  }

};
