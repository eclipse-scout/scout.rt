/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Code, CodeType, ObjectOrModel, objects, scout, texts} from '../index';
import $ from 'jquery';

export const codes = {
  /**
   * This default language is used whenever a code registers its texts in scout.texts.
   */
  defaultLanguage: 'en',

  registry: {} as Record<string, CodeType<any>>,

  bootstrap(url: string): JQuery.Promise<any> {
    let promise: JQuery.PromiseBase<any, any, any, any, any, any, any, any, any, any, any, any> = url ? $.ajaxJson(url) : $.resolvedPromise({});
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
      codes.registry[codeType.id] = codeType;
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
   * Returns a code for the given codeId. When you work with hard-coded codes
   * you should always use this function and not <code>optGet</code>.
   *
   * The codeId is a string in the following format:
   *
   * "[CodeType.id] [Code.id]"
   *
   * Examples:
   * "71074 104860"
   * "MessageChannel Phone"
   *
   * CodeType.id and {@link Code.id} are separated by a space.
   * The {@link Code.id} alone is not unique, that's why the {@link CodeType.id} must be always provided.
   *
   * You can also call this function with two arguments. In that case the first argument
   * is the codeTypeId and the second is the codeId.
   *
   * @param vararg either only "[CodeType.id]" or "[CodeType.id] [Code.id]"
   * @param codeId
   * @returns a code for the given codeId
   * @throw Error if code does not exist
   */
  get<T>(vararg: string, codeId?: T): Code<T> {
    // eslint-disable-next-line prefer-rest-params
    return codes._get('get', objects.argumentsToArray(arguments));
  },

  /**
   * Same as <code>get</code>, but does not throw an error if the code does not exist.
   * You should always use this function when you work with codes coming from a dynamic data source.
   *
   * @param vararg
   * @param codeId
   * @returns code for the given codeId or undefined if code does not exist
   */
  optGet<T>(vararg: string, codeId?: T): Code<T> {
    // eslint-disable-next-line prefer-rest-params
    return codes._get('optGet', objects.argumentsToArray(arguments));
  },

  /** @internal */
  _get(funcName: string, funcArgs: any[]): Code<any> {
    let codeTypeId, codeId;
    if (funcArgs.length === 2) {
      codeTypeId = funcArgs[0];
      codeId = funcArgs[1];
    } else {
      let tmp = funcArgs[0].split(' ');
      if (tmp.length !== 2) {
        throw new Error('Invalid string. Must have format "[CodeType.id] [Code.id]"');
      }
      codeTypeId = tmp[0];
      codeId = tmp[1];
    }
    scout.assertParameter('codeTypeId', codeTypeId);
    scout.assertParameter('codeId', codeId);
    return codes.codeType(codeTypeId)[funcName](codeId);
  },

  codeType(codeTypeId: string, optional?: boolean): CodeType<any> {
    let codeType = codes.registry[codeTypeId];
    if (!optional && !codeType) {
      throw new Error('No CodeType found for id=' + codeTypeId);
    }
    return codeType;
  },

  generateTextKey(code: Code<any>): string {
    // Use __ as prefix to reduce the possibility of overriding 'real' keys
    return '__code.' + code.id;
  },

  /**
   * Registers texts for a code. It uses the method generateTextKey to generate the text key.
   * The texts for the default locale specified by defaultLanguage are used as default texts.
   *
   * @param code the code to register the text for
   * @param textsArg an object with the languageTag as key and the translated text as value
   * @returns the generated text key
   */
  registerTexts(code: Code<any>, textsArg: Record<string, string>): string {
    let key = codes.generateTextKey(code);

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
    return key;
  }
};
