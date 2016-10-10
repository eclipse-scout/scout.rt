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
scout.texts = {

  TEXT_KEY_REGEX: /\$\{textKey\:([^\}]*)\}/,

  textsByLocale: {},

  bootstrap: function() {
    return $.ajaxJson('res/texts.json')
      .done(this.init.bind(this));
  },

  init: function(model) {
    var languageTags = Object.keys(model);
    languageTags.forEach(function(languageTag) {
      this.put(languageTag, new scout.TextMap(model[languageTag]));
    }, this);
    languageTags.forEach(function(languageTag) {
      this.link(languageTag);
    }, this);
  },

  /**
   * Links the texts of the given languageTag to make parent lookup possible (e.g. look first in de-CH, then in de, then in default)
   */
  link: function(languageTag) {
    var tags = this.splitLanguageTag(languageTag);
    var child;
    tags.forEach(function(tag) {
      var texts = this._get(tag);
      if (!texts) {
        // If there are no texts for the given tag, create an empty Texts object for linking purpose
        texts = new scout.TextMap();
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
   * Returns the (modifiable) TextMap for the given language tag.
   */
  get: function(languageTag) {
    var texts = this._get(languageTag);
    if (texts) {
      return texts;
    }

    this.link(languageTag);
    texts = this._get(languageTag);
    if (!texts) {
      throw new Error('Texts missing for the language tag ' + languageTag);
    }
    return texts;
  },

  _get: function(languageTag) {
    return this.textsByLocale[languageTag];
  },

  put: function(languageTag, texts) {
    this.textsByLocale[languageTag] = texts;
  },

  /**
   * Extracts NLS texts from the DOM tree. Texts are expected in the following format:
   *
   *   <scout-text data-key="..." data-value="..." />
   *
   * This method returns a map with all found texts. It must be called before scout.prepareDOM()
   * is called, as that method removes all <scout-text> tags.
   */
  readFromDOM: function() {
    var textMap = {};
    $('scout-text').each(function() {
      // No need to unescape strings (the browser did this already)
      var key = $(this).data('key');
      var value = $(this).data('value');
      textMap[key] = value;
    });
    return textMap;
  },

  /**
   * @param key to convert into a string with the form '${textKey:AKey}'.
   * @return text containing the text key like like '${textKey:AKey}'.
   */
  buildKey: function(key) {
    return '${textKey:' + key + '}';
  },

  /**
   * @param value text which contains a text key like '${textKey:AKey}'.
   * @return the resolved key or the unchanged value if the text key could not be extracted.
   */
  resolveKey: function(value) {
    var result = this.TEXT_KEY_REGEX.exec(value);
    if (result && result.length === 2) {
      return result[1];
    }
    return value;
  },

  /**
   * @param value text which contains a text key like '${textKey:AKey}'.
   * @param languageTag the languageTag to use for the text lookup with the resolved key.
   * @return the resolved text in the language of the given session or the unchanged text if the text key could not be extracted.
   */
  resolveText: function(value, languageTag) {
    var key = scout.texts.resolveKey(value);
    if (key !== value) {
      return scout.texts.get(languageTag).get(key);
    }
    return value;
  },

  /**
   * Utility function to easily replace an object property which contains a text key like '${textKey:AKey}'.
   *
   * @param object object having a text property which contains a text-key
   * @param textProperty (optional) name of the property where a text-key should be replaced by a text. By default 'text' is used as property name.
   * @param session (optional) can be undefined when given 'object' has a session property, otherwise mandatory
   */
  resolveTextProperty: function(object, textProperty, session) {
    textProperty = textProperty || 'text';
    session = object.session || session;
    var value = object[textProperty];
    var text = this.resolveText(value, session.locale.languageTag);
    if (text !== value) {
      object[textProperty] = text;
    }
  }
};
