/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Code, CodeType, ObjectOrModel, objects, texts} from '../index';
import $ from 'jquery';

export const codes = {
  /**
   * This default language is used whenever a code registers its texts in scout.texts.
   */
  defaultLanguage: 'en',

  registry: {} as Record<string, CodeType<any>>,

  bootstrap(url: string): JQuery.Promise<any> {
    let promise: JQuery.Promise<any> = url ? $.ajaxJson(url) : $.resolvedPromise({});
    return promise.then(codes._preInit.bind(this, url));
  },

  /** @internal */
  _preInit(url: string, data: any) {
    if (data && data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    codes.init(data);
  },

  init(data?: any) {
    data = data || {};
    Object.keys(data).forEach(codeTypeId => codes.add(data[codeTypeId]));
  },

  add(codeTypes: ObjectOrModel<CodeType<any>> | ObjectOrModel<CodeType<any>>[]) {
    let types = arrays.ensure(codeTypes);
    types.forEach(codeTypeOrModel => {
      let codeType = CodeType.ensure(codeTypeOrModel);
      if (codeType) {
        codes.registry[codeType.id] = codeType;
      }
    });
  },

  /**
   * @param codeTypes code types or code type ids to remove
   */
  remove(codeTypes: string | CodeType<any> | (string | CodeType<any>)[]) {
    let types = arrays.ensure(codeTypes);
    types.forEach(codeType => {
      let id;
      if (typeof codeType === 'string') {
        id = codeType;
      } else {
        id = codeType.id;
      }
      delete codes.registry[id];
    });
  },

  /**
   * Returns a code for the given codeId in the CodeType with given codeTypeId.
   * @returns a code for the given codeTypeId and codeId.
   * @throw Error if CodeType or Code does not exist
   */
  get<T>(codeTypeId: string, codeId: T): Code<T> {
    return codes.codeType(codeTypeId).get(codeId);
  },

  /**
   * Same as {@link get}, but does not throw an error if the CodeType or Code does not exist.
   * @returns Code for the given codeTypeId and codeId or null/undefined.
   */
  optGet<T>(codeTypeId: string, codeId: T): Code<T> {
    let codeType = codes.codeType(codeTypeId, true);
    if (!codeType) {
      return null;
    }
    return codeType.optGet(codeId);
  },

  codeType(codeTypeId: string, optional?: boolean): CodeType<any> {
    let codeType = codes.registry[codeTypeId];
    if (!optional && !codeType) {
      throw new Error('No CodeType found for id=' + codeTypeId);
    }
    return codeType;
  },

  codeTypeByClass<T>(clazz: new() => T): T {
    let allCodeTypes = objects.values(codes.registry);
    for (let codeType of allCodeTypes) {
      if (codeType instanceof clazz) {
        return codeType;
      }
    }
    return null;
  },

  /**
   * Registers texts for a code. It uses the method generateTextKey to generate the text key.
   * The texts for the default locale specified by defaultLanguage are used as default texts.
   *
   * @param key the text key under which the given textsArg map will be registered.
   * @param textsArg an object with the languageTag as key and the translated text as value
   * @returns the generated text key for the given object.
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
