/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeType, CodeTypeCache, ObjectOrModel, scout, systems, texts} from '../index';

let codeTypeCache: CodeTypeCache = null;

export const codes = {

  /**
   * This default language is used whenever a code registers its texts in {@link texts}.
   */
  defaultLanguage: 'en',

  /**
   * load codes from the main system
   */
  bootstrapSystem(): JQuery.Promise<void> {
    const url = systems.getOrCreate().getEndpointUrl('codes', 'codes');
    return codes.bootstrap(url);
  },

  /**
   * Initialize the code type map with the result of the given REST url.
   */
  bootstrap(url: string): JQuery.Promise<any> {
    if (!url) {
      // no need to create the codetype cache
      return $.resolvedPromise();
    }
    return codes.getCodeTypeCache().bootstrap(url);
  },

  /**
   * Adds the given CodeType models to the registry. Existing entries with the same ids are overwritten.
   * @returns The registered CodeType instances.
   */
  add(codeTypes: ObjectOrModel<CodeType<any, any, any>> | ObjectOrModel<CodeType<any, any, any>>[]): CodeType<any, any, any>[] {
    return codes.getCodeTypeCache().add(codeTypes);
  },

  /**
   * Removes the given CodeTypes from the registry.
   *
   * @param codeTypes code types or code type ids to remove.
   */
  remove(codeTypes: string | CodeType<any, any, any> | (string | CodeType<any, any, any>)[]) {
    codes.getCodeTypeCache().remove(codeTypes);
  },

  /**
   * Gets the CodeType with given id or Class.
   * @param codeTypeIdOrClassRef The CodeType id or Class
   * @returns The CodeType instance or undefined if not found.
   */
  get<T extends CodeType<any>>(codeTypeIdOrClassRef: string | (new() => T)): T {
    return codes.getCodeTypeCache().get(codeTypeIdOrClassRef);
  },

  /**
   * @returns the global {@link CodeTypeCache} instance. If required, a new one is created (on first use).
   */
  getCodeTypeCache(): CodeTypeCache {
    if (!codeTypeCache) {
      codeTypeCache = scout.create(CodeTypeCache);
    }
    return codeTypeCache;
  },

  /**
   * Registers texts for a code.
   * The texts for the default locale specified by defaultLanguage are used as default texts.
   *
   * @param key the text key under which the given textsArg map will be registered.
   * @param textsArg an object with the languageTag as key and the translated text as value
   * @internal
   */
  _registerTexts(key: string, textsArg: Record<string, string>) {
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
