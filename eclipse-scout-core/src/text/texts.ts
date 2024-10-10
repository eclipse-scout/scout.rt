/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, Session, TextMap} from '../index';
import $ from 'jquery';

export type TextMapType = Record<string, TextMap>;

export const texts = {

  /**
   * This default language is used whenever a new text is registered.
   */
  defaultLanguage: 'en',

  TEXT_KEY_REGEX: /^\${textKey:([^}]+)}$/,

  textsByLocale: {} as TextMapType,

  bootstrap(url: string | string[]): JQuery.Promise<any> {
    if (!url) {
      return $.resolvedPromise({});
    }
    let promises = [];
    let urls = arrays.ensure(url);
    urls.forEach(url => promises.push(
      $.ajaxJson(url).then(texts._handleBootstrapResponse.bind(this, url)))
    );
    return $.promiseAll(promises);
  },

  /** @internal */
  _setTextsByLocale(val: TextMapType) {
    texts.textsByLocale = val;
  },

  /** @internal */
  _handleBootstrapResponse(url: string, data: any) {
    App.handleJsonError(url, data);
    texts.init(data);
  },

  init(model: Record<string, Record<string, string>>) {
    Object.keys(model).forEach(languageTag => {
      let textMap = model[languageTag];
      texts.get(languageTag).addAll(textMap);
    });
  },

  /**
   * Links the texts of the given languageTag to make parent lookup possible (e.g. look first in de-CH, then in de, then in default).
   */
  link(languageTag: string) {
    let tags = texts.createOrderedLanguageTags(languageTag);
    let child: TextMap;
    tags.forEach(tag => {
      let textMap: TextMap = texts._get(tag);
      if (!textMap) {
        // If there are no texts for the given tag, create an empty Texts object for linking purpose
        textMap = new TextMap();
        texts._put(tag, textMap);
      }
      if (child) {
        child.setParent(textMap);
      }
      child = textMap;
    });
  },

  /**
   * Creates an array containing all relevant tags.
   *
   * Examples:
   * - 'de-CH' generates the array: ['de-CH', 'de', 'default']
   * - 'de' generates the array: ['de', 'default']
   * - 'default' generates the array: ['default']
   */
  createOrderedLanguageTags(languageTag: string): string[] {
    let tags = [languageTag];

    let i = languageTag.lastIndexOf('-');
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
   * If no TextMap exists for the languageTag given, a new empty map is created.
   * @returns the TextMap for the given languageTag. Never returns null or undefined.
   */
  get(languageTag: string): TextMap {
    let textMap = texts._get(languageTag);
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
   * @internal
   */
  _put(languageTag: string, textMap: TextMap) {
    texts.textsByLocale[languageTag] = textMap;
  },

  /**
   * Extracts NLS texts from the DOM tree. Texts are expected in the following format:
   *
   *   `<scout-text data-key="..." data-value="..." />`
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
   * Returns the given text key in the form `'${textKey:AKey}'`.
   *
   * @param key the text key to convert (e.g. `'AKey'`)
   * @returns the given text key in the form `'${textKey:AKey}'`
   */
  buildKey(key: string): string {
    return '${textKey:' + key + '}';
  },

  /**
   * Returns the text key (e.g. `'AKey'`) if the given text has the form `'${textKey:AKey}'`. Otherwise,
   * the input is returned unchanged.
   *
   * @param value either an arbitrary text or a special string of the form `'${textKey:AKey}'`
   * @returns the resolved text key or the unchanged value if the text key could not be extracted.
   */
  resolveKey(value: string): string {
    let match = texts.TEXT_KEY_REGEX.exec(value);
    if (match) {
      return match[1];
    }
    return value;
  },

  /**
   * If the given text has the form `'${textKey:AKey}'`, the key is extracted and the text for this
   * key in the given languages is resolved and returned. Otherwise, the input is returned unchanged.
   *
   * @param value either an arbitrary text or a special string of the form `'${textKey:AKey}'`
   * @param languageTag the languageTag to use for the text lookup with the resolved key.
   * @returns the resolved text in the given language or the unchanged text if the text key could not be extracted.
   */
  resolveText(value: string, languageTag: string): string {
    let key = texts.resolveKey(value);
    if (key !== value) {
      return texts.get(languageTag).get(key);
    }
    return value;
  },

  /**
   * Converts the value of the specified property from the form `'${textKey:...}'` into a resolved text.
   * The value remains unchanged if it does not match the {@linkplain texts#resolveText supported format}.
   *
   * @param object non-null object having a text property which may contain a text key (must not be null)
   * @param textProperty name of the property on the given object which may contain a text key. By default, 'text' is used as property name.
   * @param session session defining the locale to be used when resolving a text. Can be undefined when given 'object' has a session property, otherwise mandatory.
   */
  resolveTextProperty(object: any, textProperty?: string, session?: Session) {
    textProperty = textProperty || 'text';
    session = object.session || session;
    let value = object[textProperty];
    let text = texts.resolveText(value, session.locale.languageTag);
    if (text !== value) {
      object[textProperty] = text;
    }
  },

  /**
   * Registers a texts map for a text key.
   * The texts for the default language specified by {@link texts.defaultLanguage} are registered as default texts.
   *
   * @param key the text key under which the given textsArg map will be registered.
   * @param textsArg an object with the languageTag as key and the translated text as value
   */
  registerTexts(key: string, textsArg: Record<string, string>) {
    // In case of changed defaultLanguage clear the 'default' entry
    texts.get('default').remove(key);

    for (let languageTag in textsArg) {
      let text = textsArg[languageTag];
      // Use defaultLanguage as default, if specified (maybe changed or set to null by the app).
      if (languageTag && languageTag === texts.defaultLanguage) {
        languageTag = 'default';
      }
      texts.get(languageTag).add(key, text);
    }
  }
};
