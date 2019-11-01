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
import {texts} from '../index';
import {objects} from '../index';
import {CodeType} from '../index';
import * as $ from 'jquery';
import {arrays} from '../index';
import {scout} from '../index';


/**
 * This default language is used whenever a code registers its texts in scout.texts.
 */
let defaultLanguage = 'en';

let registry = {};

export function bootstrap(url) {
  var promise = url ? $.ajaxJson(url) : $.resolvedPromise({});
  return promise.then(_preInit.bind(this, url));
}

//private
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

export function init(data) {
  data = data || {};
  Object.keys(data).forEach(function(codeTypeId) {
    add(data[codeTypeId]);
  }, this);
}

/**
 * @param codes one or more codeTypes, maybe an object or an array
 */
export function add(codeTypes) {
  codeTypes = arrays.ensure(codeTypes);
  codeTypes.forEach(function(codeType) {
    codeType = CodeType.ensure(codeType);
    registry[codeType.id] = codeType;
  }, this);
}

/**
 * @param codeTypes code types or code type ids to remove
 */
export function remove(codeTypes) {
  codeTypes = arrays.ensure(codeTypes);
  codeTypes.forEach(function(codeType) {
    var id;
    if (typeof codeType === 'string') {
      id = codeType;
    } else {
      id = codeType.id;
    }
    delete registry[id];
  }, this);
}

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
 * CodeType.id and Code.id are separated by a space.
 * The Code.id alone is not unique, that's why the CodeType.id must be always provided.
 *
 * You can also call this function with two arguments. In that case the first argument
 * is the codeTypeId and the second is the codeId.
 *
 * @param {string} vararg either only "[CodeType.id]" or "[CodeType.id] [Code.id]"
 * @param {string} [codeId]
 * @returns {Code} a code for the given codeId
 * @throw {Error} if code does not exist
 */
export function get(vararg, codeId) {
  return _get('get', objects.argumentsToArray(arguments));
}

/**
 * Same as <code>get</code>, but does not throw an error if the code does not exist.
 * You should always use this function when you work with codes coming from a dynamic data source.
 *
 * @param vararg
 * @param codeId
 * @returns {Code} code for the given codeId or undefined if code does not exist
 */
export function optGet(vararg, codeId) {
  return _get('optGet', objects.argumentsToArray(arguments));
}

//private
export function _get(funcName, funcArgs) {
  var codeTypeId, codeId;
  if (funcArgs.length === 2) {
    codeTypeId = funcArgs[0];
    codeId = funcArgs[1];
  } else {
    var tmp = funcArgs[0].split(' ');
    if (tmp.length !== 2) {
      throw new Error('Invalid string. Must have format "[CodeType.id] [Code.id]"');
    }
    codeTypeId = tmp[0];
    codeId = tmp[1];
  }
  scout.assertParameter('codeTypeId', codeTypeId);
  scout.assertParameter('codeId', codeId);
  return codeType(codeTypeId)[funcName](codeId);
}

export function codeType(codeTypeId, optional) {
  var codeType = registry[codeTypeId];
  if (!optional && !codeType) {
    throw new Error('No CodeType found for id=' + codeTypeId);
  }
  return codeType;
}

export function generateTextKey(code) {
  // Use __ as prefix to reduce the possibility of overriding 'real' keys
  return '__code.' + code.id;
}

/**
 * Registers texts for a code. It uses the method generateTextKey to generate the text key.
 * The texts for the default locale specified by defaultLanguage are used as default texts.
 *
 * @param code the code to register the text for
 * @param textsArg an object with the languageTag as key and the translated text as value
 * @return the generated text key
 */
export function registerTexts(code, textsArg) {
  var key = generateTextKey(code);

  // In case of changed defaultLanguage clear the 'default' entry
  texts.get('default').remove(key);

  for (var languageTag in textsArg) { // NOSONAR
    var text = textsArg[languageTag];
    // Use defaultLanguage as default, if specified (may be changed or set to null by the app).
    if (languageTag && languageTag === defaultLanguage) {
      languageTag = 'default';
    }
    texts.get(languageTag).add(key, text);
  }
  return key;
}


export default {
  add,
  bootstrap,
  codeType,
  defaultLanguage,
  generateTextKey,
  get,
  init,
  optGet,
  registerTexts,
  registry,
  remove
};
