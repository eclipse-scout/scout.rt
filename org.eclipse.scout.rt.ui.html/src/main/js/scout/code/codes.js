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
scout.codes = {

  /**
   * This default language is used whenever a code registers its texts in scout.texts.
   */
  defaultLanguage: 'en',

  registry: {},

  bootstrap: function(url) {
    var promise = url ? $.ajaxJson(url) : $.resolvedPromise({});
    return promise.done(this.init.bind(this));
  },

  init: function(data) {
    data = data || {};
    Object.keys(data).forEach(function(codeTypeId) {
      this.add(data[codeTypeId]);
    }, this);
  },

  /**
   * @param codes one or more codeTypes, maybe an object or an array
   */
  add: function(codeTypes) {
    codeTypes = scout.arrays.ensure(codeTypes);
    codeTypes.forEach(function(codeType) {
      codeType = scout.CodeType.ensure(codeType);
      this.registry[codeType.id] = codeType;
    }, this);
  },

  remove: function(codeTypeId) {
    delete this.registry[codeTypeId];
    // FIXME [awe] 6.1 - also clean up texts?
  },

  /**
   * Returns a code for the given codeId. The codeId is a string in the following format:
   *
   * "[CodeType.id] [Code.id]"
   *
   * Examples:
   * "71074 104860"
   * "MessageChannel Phone"
   *
   * CodeType.id and Code.id are separated by a space.
   * The Code.id alone is not unique, that's why the CodeType.id must be always provided.
   *
   * You can also call this function with two arguments. In that case the first argument
   * is the codeTypeId and the second is the codeId.
   */
  get: function(vararg, codeId) {
    var codeTypeId;
    if (arguments.length === 2) {
      codeTypeId = vararg;
    } else {
      var tmp = vararg.split(' ');
      if (tmp.length !== 2) {
        throw new Error('Invalid string. Must have format "[CodeType.id] [Code.id]"');
      }
      codeTypeId = tmp[0];
      codeId = tmp[1];
    }
    scout.assertParameter('codeTypeId', codeTypeId);
    scout.assertParameter('codeId', codeId);
    return this.codeType(codeTypeId).get(codeId);
  },

  codeType: function(codeTypeId, optional) {
    var codeType = this.registry[codeTypeId];
    if (!optional && !codeType) {
      throw new Error('No CodeType found for id=' + codeTypeId);
    }
    return codeType;
  },

  generateTextKey: function(code) {
    // Use __ as prefix to reduce the possibility of overriding 'real' keys
    return '__code.' + code.id;
  },

  /**
   * Registers texts for a code. It uses the method generateTextKey to generate the text key.
   * The texts for the default locale specified by scout.codes.defaultLanguage are used as default texts.
   *
   * @param code the code to register the text for
   * @param texts an object with the languageTag as key and the translated text as value
   * @return the generated text key
   */
  registerTexts: function(code, texts) {
    var key = scout.codes.generateTextKey(code);

    // In case of changed defaultLanguage clear the 'default' entry
    scout.texts.get('default').remove(key);

    for (var languageTag in texts) { // NOSONAR
      var text = texts[languageTag];
      // Use defaultLanguage as default, if specified (may be changed or set to null by the app).
      if (languageTag && languageTag === this.defaultLanguage) {
        languageTag = 'default';
      }
      scout.texts.get(languageTag).add(key, text);
    }
    return key;
  }

};
