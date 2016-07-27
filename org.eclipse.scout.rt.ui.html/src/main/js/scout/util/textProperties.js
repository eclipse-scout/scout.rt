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

  textsByLocale: {},

  bootstrap: function() {
    var that = this;

    return $.ajax({
      url: 'res/texts.json',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8'
    }).done(that._onLoadDone.bind(that))
    .fail(that._onLoadFail.bind(that));
  },

  _onLoadDone: function(data) {
    this.init(data);
  },

  _onLoadFail: function(jqXHR, textStatus, errorThrown) {
    throw new Error('Error while loading texts: ' + errorThrown);
  },

  init: function(model) {
    var textMap, languageTag;
    for (languageTag in model) {
      textMap = model[languageTag];
      this.put(languageTag, new scout.Texts(textMap));
    }
    for (languageTag in model) {
      this.link(languageTag);
    }
  },

  /**¨
   * Links the texts of the given languageTag to make parent lookup possible (e.g. look first in de-CH, then in de, then in default)
   */
  link: function(languageTag) {
    var tags = this.splitLanguageTag(languageTag);
    var child;
    tags.forEach(function(tag) {
      var texts = this._get(tag);
      if (!texts) {
        // If there are no texts for the given tag, create an empty Texts object for linking purpose
        texts = new scout.Texts();
        this.put(tag, texts);
      }
      if (child) {
        child.setParent(texts);
      }
      child = texts;
    }, this);
  },

  /**
   * Creates an array containing all relevant tags.
   * <p>
   * Examples:<br>
   * - 'de-CH' generates the array: ['de-CH', 'de', 'default']
   * - 'de' generates the array: ['de', 'default']
   * - 'default' generates the array: ['default']
   */
  splitLanguageTag: function(languageTag) {
    var tags = [],
      i = languageTag.lastIndexOf('-');

    tags.push(languageTag);

    while (i >= 0) {
      languageTag = languageTag.substring(0, i);
      tags.push(languageTag);
      i = languageTag.lastIndexOf('-');
    }

    if (languageTag !== 'default') {
      tags.push('default');
    }
    return tags;
  },

  /**
   * Returns the Texts object for the given language tag.
   */
  get: function(languageTag) {
    var texts = this._get(languageTag);
    if (!texts) {
      this.link(languageTag);
    }
    texts = this._get(languageTag);
    if (!texts) {
      throw new Error('texts still missing.');
    }
    return texts;
  },

  _get: function(languageTag) {
    return this.textsByLocale[languageTag];
  },

  put: function(languageTag, texts) {
    this.textsByLocale[languageTag] = texts;
  }

};
