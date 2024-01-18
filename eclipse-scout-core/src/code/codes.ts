/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, CodeType, ModelOf, ObjectOrModel, texts} from '../index';
import $ from 'jquery';

export const codes = {
  /**
   * This default language is used whenever a code registers its texts in scout.texts.
   */
  defaultLanguage: 'en',

  /**
   * Map of Code id to Code instance. Do not access directly. Instead, use {@link codes.get}.
   */
  registry: new Map<string /* CodeType.id */, CodeType<any>>,

  /**
   * Initialize the code type map with the result of the given REST url.
   */
  bootstrap(url: string): JQuery.Promise<any> {
    let promise: JQuery.Promise<any> = url ? $.ajaxJson(url) : $.resolvedPromise({});
    return promise.then(codes._preInit.bind(this, url));
  },

  /** @internal */
  _preInit(url: string, data: any) {
    if (!data) {
      return;
    }
    if (data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    codes.init(data);
  },

  /**
   * Adds all given CodeType models to the registry. The registry is not cleaned but existing entries with the same ids are overwritten.
   */
  init(data?: ModelOf<CodeType<any>>[]) {
    codes.add(data);
  },

  /**
   * Adds the given CodeType models to the registry. Existing entries with the same ids are overwritten.
   * @return The registered CodeType instances.
   */
  add(codeTypes: ObjectOrModel<CodeType<any>> | ObjectOrModel<CodeType<any>>[]): CodeType<any>[] {
    let registeredCodeTypes = [];
    arrays.ensure(codeTypes).forEach(codeTypeOrModel => {
      let codeType = CodeType.ensure(codeTypeOrModel);
      if (codeType && codeType.id) {
        codes.registry.set(codeType.id, codeType);
        registeredCodeTypes.push(codeType);
      }
    });
    return registeredCodeTypes;
  },

  /**
   * Removes the given CodeTypes from the registry.
   *
   * @param codeTypes code types or code type ids to remove.
   */
  remove(codeTypes: string | CodeType<any> | (string | CodeType<any>)[]) {
    arrays.ensure(codeTypes)
      .map(codeTypeOrId => typeof codeTypeOrId === 'string' ? codeTypeOrId : codeTypeOrId.id)
      .forEach(id => codes.registry.delete(id));
  },

  /**
   * Gets the CodeType with given id or Class.
   * @param codeTypeIdOrClassRef The CodeType id or Class
   * @return The CodeType instance or undefined if not found.
   */
  get<T extends CodeType<any>>(codeTypeIdOrClassRef: string | (new() => T)): T {
    if (typeof codeTypeIdOrClassRef === 'string') {
      return codes.registry.get(codeTypeIdOrClassRef) as T;
    }

    for (let codeType of codes.registry.values()) {
      if (codeType instanceof codeTypeIdOrClassRef) {
        return codeType as T;
      }
    }
    return undefined; // class not found
  },

  /**
   * Registers texts for a code.
   * The texts for the default locale specified by defaultLanguage are used as default texts.
   *
   * @param key the text key under which the given textsArg map will be registered.
   * @param textsArg an object with the languageTag as key and the translated text as value
   */
  registerTexts(key: string, textsArg: Record<string, string>) {
    // In case of changed defaultLanguage clear the 'default' entry
    texts.get('default').remove(key);

    for (let languageTag in textsArg) { // NOSONAR
      let text = textsArg[languageTag];
      // Use defaultLanguage as default, if specified (maybe changed or set to null by the app).
      if (languageTag && languageTag === codes.defaultLanguage) {
        languageTag = 'default';
      }
      texts.get(languageTag).add(key, text);
    }
  }
};
