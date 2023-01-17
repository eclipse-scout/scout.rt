/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Session, TextMap} from '../index';
import $ from 'jquery';

export type TextMapType = Record<string, TextMap>;

export const texts = {

  TEXT_KEY_REGEX: /\${textKey:([^}]*)}/,

  textsByLocale: {} as TextMapType,

  bootstrap(url: string | string[]): JQuery.Promise<any> {
    if (!url) {
      return $.resolvedPromise({});
    }
    let promises = [];
    let urls = arrays.ensure(url);
    urls.forEach(url => promises.push(
      $.ajaxJson(url).then(texts._preInit.bind(this, url)))
    );
    return $.promiseAll(promises);
  },

  /** @internal */
  _setTextsByLocale(val: TextMapType) {
    texts.textsByLocale = val;
  },

  /** @internal */
  _preInit(url: string | string[], data: any) {
    if (data && data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    texts.init(data);
  },

  init(model: Record<string, Record<string, string>>) {
    Object.keys(model)
      .forEach(languageTag => texts.get(languageTag).addAll(model[languageTag]), this);
  },

  /**
   * Links the texts of the given languageTag to make parent lookup possible (e.g. look first in de-CH, then in de, then in default)
   */
  link(languageTag: string) {
    let tags = texts.createOrderedLanguageTags(languageTag);
    let child;
    tags.forEach(tag => {
      let textMap: TextMap = texts._get(tag);
      if (!textMap) {
        // If there are no texts for the given tag, create an empty Texts object for linking purpose
        textMap = new TextMap();
        texts.put(tag, textMap);
      }
      if (child) {
        child.setParent(textMap);
      }
      child = textMap;
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
  createOrderedLanguageTags(languageTag: string): string[] {
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
  },

  /**
   * Returns the (modifiable) TextMap for the given language tag.
   */
  get(languageTag: string): TextMap {
    let textMap: TextMap = texts._get(languageTag);
    if (textMap) {
      return textMap;
    }

    texts.link(languageTag);
    textMap = texts._get(languageTag);
    if (!textMap) {
      throw new Error('Texts missing for the language tag ' + languageTag);
    }
    return textMap;
  },

  /** @internal */
  _get(languageTag: string): TextMap {
    return texts.textsByLocale[languageTag];
  },

  /**
   * Registers the text map for the given locale.
   * If there already is a text map registered for that locale, it will be replaced, meaning existing texts for that locale are deleted.
   */
  put(languageTag: string, textMap: TextMap) {
    texts.textsByLocale[languageTag] = textMap;
  },

  /**
   * Extracts NLS texts from the DOM tree. Texts are expected in the following format:
   *
   *   <scout-text data-key="..." data-value="..." />
   *
   * This method returns a map with all found texts. It must be called before scout.prepareDOM()
   * is called, as that method removes all <scout-text> tags.
   */
  readFromDOM(): Record<string, string> {
    let textMap = {};
    $('scout-text').each(function() {
      // No need to unescape strings (the browser did this already)
      let key = $(this).data('key');
      // noinspection UnnecessaryLocalVariableJS
      let value = $(this).data('value');
      textMap[key] = value;
    });
    return textMap;
  },

  /**
   * @param key to convert into a string with the form '${textKey:AKey}'.
   * @returns text containing the text key like like '${textKey:AKey}'.
   */
  buildKey(key: string): string {
    return '${textKey:' + key + '}';
  },

  /**
   * @param value text which contains a text key like '${textKey:AKey}'.
   * @returns the resolved key or the unchanged value if the text key could not be extracted.
   */
  resolveKey(value: string): string {
    let result = texts.TEXT_KEY_REGEX.exec(value);
    if (result && result.length === 2) {
      return result[1];
    }
    return value;
  },

  /**
   * @param value text which contains a text key like '${textKey:AKey}'.
   * @param languageTag the languageTag to use for the text lookup with the resolved key.
   * @returns the resolved text in the language of the given session or the unchanged text if the text key could not be extracted.
   */
  resolveText(value: string, languageTag: string): string {
    let key = texts.resolveKey(value);
    if (key !== value) {
      return texts.get(languageTag).get(key);
    }
    return value;
  },

  /**
   * Utility function to easily replace an object property which contains a text key like '${textKey:AKey}'.
   *
   * @param object object having a text property which contains a text-key
   * @param [textProperty] name of the property where a text-key should be replaced by a text. By default, 'text' is used as property name.
   * @param [session] can be undefined when given 'object' has a session property, otherwise mandatory
   */
  resolveTextProperty(object: any, textProperty?: string, session?: Session) {
    textProperty = textProperty || 'text';
    session = object.session || session;
    let value = object[textProperty];
    let text = texts.resolveText(value, session.locale.languageTag);
    if (text !== value) {
      object[textProperty] = text;
    }
  }
};
