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
import {arrays, TextMap} from '../index';
import $ from 'jquery';

const TEXT_KEY_REGEX = /\${textKey:([^}]*)}/;

let textsByLocale = {};

export function bootstrap(url) {
  if (!url) {
    return $.resolvedPromise({});
  }
  let promises = [];
  let urls = arrays.ensure(url);
  urls.forEach(url => promises.push(
    $.ajaxJson(url).then(_preInit.bind(this, url)))
  );
  return $.promiseAll(promises);
}

// private
export function _setTextsByLocale(val) {
  textsByLocale = val;
}

// private
export function _preInit(url, data) {
  if (data && data.error) {
    // The result may contain a json error (e.g. session timeout) -> abort processing
    throw {
      error: data.error,
      url: url
    };
  }
  init(data);
}

export function init(model) {
  let languageTags = Object.keys(model);
  languageTags.forEach(languageTag => {
    get(languageTag).addAll(model[languageTag]);
  }, this);
}

/**
 * Links the texts of the given languageTag to make parent lookup possible (e.g. look first in de-CH, then in de, then in default)
 */
export function link(languageTag) {
  let tags = createOrderedLanguageTags(languageTag);
  let child;
  tags.forEach(tag => {
    let texts = _get(tag);
    if (!texts) {
      // If there are no texts for the given tag, create an empty Texts object for linking purpose
      texts = new TextMap();
      put(tag, texts);
    }
    if (child) {
      child.setParent(texts);
    }
    child = texts;
  }, this);
}

/**
 * Creates an array containing all relevant tags.
 * <p>
 * Examples:<br>
 * - 'de-CH' generates the array: ['de-CH', 'de', 'default']
 * - 'de' generates the array: ['de', 'default']
 * - 'default' generates the array: ['default']
 */
export function createOrderedLanguageTags(languageTag) {
  let tags = [],
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
}

/**
 * Returns the (modifiable) TextMap for the given language tag.
 */
export function get(languageTag) {
  let texts = _get(languageTag);
  if (texts) {
    return texts;
  }

  link(languageTag);
  texts = _get(languageTag);
  if (!texts) {
    throw new Error('Texts missing for the language tag ' + languageTag);
  }
  return texts;
}

// private
export function _get(languageTag) {
  return textsByLocale[languageTag];
}

/**
 * Registers the text map for the given locale.
 * If there already is a text map registered for that locale, it will be replaced, meaning existing texts for that locale are deleted.
 *
 * @param {TextMap} textMap
 */
export function put(languageTag, textMap) {
  textsByLocale[languageTag] = textMap;
}

/**
 * Extracts NLS texts from the DOM tree. Texts are expected in the following format:
 *
 *   <scout-text data-key="..." data-value="..." />
 *
 * This method returns a map with all found texts. It must be called before scout.prepareDOM()
 * is called, as that method removes all <scout-text> tags.
 */
export function readFromDOM() {
  let textMap = {};
  $('scout-text').each(function() {
    // No need to unescape strings (the browser did this already)
    let key = $(this).data('key');
    // noinspection UnnecessaryLocalVariableJS
    let value = $(this).data('value');
    textMap[key] = value;
  });
  return textMap;
}

/**
 * @param {string} key to convert into a string with the form '${textKey:AKey}'.
 * @return {string} text containing the text key like like '${textKey:AKey}'.
 */
export function buildKey(key) {
  return '${textKey:' + key + '}';
}

/**
 * @param {string} value text which contains a text key like '${textKey:AKey}'.
 * @return {string} the resolved key or the unchanged value if the text key could not be extracted.
 */
export function resolveKey(value) {
  let result = TEXT_KEY_REGEX.exec(value);
  if (result && result.length === 2) {
    return result[1];
  }
  return value;
}

/**
 * @param {string} value text which contains a text key like '${textKey:AKey}'.
 * @param {string} languageTag the languageTag to use for the text lookup with the resolved key.
 * @return {string} the resolved text in the language of the given session or the unchanged text if the text key could not be extracted.
 */
export function resolveText(value, languageTag) {
  let key = resolveKey(value);
  if (key !== value) {
    return get(languageTag).get(key);
  }
  return value;
}

/**
 * Utility function to easily replace an object property which contains a text key like '${textKey:AKey}'.
 *
 * @param {object} object object having a text property which contains a text-key
 * @param {string} [textProperty] name of the property where a text-key should be replaced by a text. By default 'text' is used as property name.
 * @param {Session} [session] can be undefined when given 'object' has a session property, otherwise mandatory
 */
export function resolveTextProperty(object, textProperty, session) {
  textProperty = textProperty || 'text';
  session = object.session || session;
  let value = object[textProperty];
  let text = resolveText(value, session.locale.languageTag);
  if (text !== value) {
    object[textProperty] = text;
  }
}

export default {
  TEXT_KEY_REGEX,
  bootstrap,
  buildKey,
  createOrderedLanguageTags,
  get,
  init,
  link,
  put,
  readFromDOM,
  resolveKey,
  resolveText,
  resolveTextProperty,
  textsByLocale
};
