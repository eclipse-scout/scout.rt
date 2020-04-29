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
import {arrays, Locale, objects, scout, texts} from '../index';
import $ from 'jquery';

let localesMap = {};

export function bootstrap(url) {
  let promise = url ? $.ajaxJson(url) : $.resolvedPromise([]);
  return promise.then(_preInit.bind(this, url));
}

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
  data.forEach(locale => {
    localesMap[locale.languageTag] = new Locale(locale);
  }, this);
}

export function _get(languageTag) {
  return localesMap[languageTag];
}

/**
 * Checks whether there is a locale definition for the given language tag.
 * @param explicit if true, the country code is considered, meaning if languageTag is 'de-CH'
 *   and there is a locale for 'de' but not for 'de-CH', true will be returned nonetheless. Default false (consistent to #get).
 */
export function has(languageTag, explicit) {
  explicit = scout.nvl(explicit, false);
  if (explicit) {
    return !!_get(languageTag);
  }
  return !!get(languageTag);
}

/**
 * @returns {Locale} the locale for the given languageTag.
 * If there is no locale found for the given tag, it tries to load the locale without the country code.
 * If there is still no locale found, null is returned.
 */
export function get(languageTag) {
  let locale,
    tags = texts.createOrderedLanguageTags(languageTag);

  tags.some(tag => {
    locale = _get(tag);
    return !!locale;
  }, this);

  if (!locale) {
    return null;
  }

  return locale;
}

export function getNavigatorLanguage() {
  return navigator.language || navigator.userLanguage;
}

/**
 * @returns {Locale} for the language returned by the navigator.
 * If no locale is found, the first locale with the language of the navigator is returned.
 * (e.g. if browser returns 'de' and there is no locale for 'de', check if there is one for 'de-CH', 'de-DE' etc. and take the first.)
 * If still no locale is found, the default locale {@link Locale.DEFAULT} is returned.
 */
export function getNavigatorLocale() {
  let languageTag = getNavigatorLanguage();
  if (!languageTag) {
    //  No language returned by the browser, using default locale (should not happen with modern browsers, but we never know...)
    $.log.warn('Browser returned no language. Using default locale.');
    return new Locale();
  }

  let locale = get(languageTag);
  if (locale) {
    // If a locale was found for the language returned by the navigator, use that one
    return locale;
  }

  // Otherwise search a locale with the same language
  $.log.isInfoEnabled() && $.log.info('Locale for languageTag ' + languageTag + ' not found. Trying to load best match.');
  let language = splitLanguageTag(languageTag)[0];
  locale = findFirstForLanguage(language);
  if (locale) {
    return locale;
  }

  // If still not found, use the default locale
  $.log.isInfoEnabled() && $.log.info('Still no matching locale for languageTag ' + languageTag + ' found. Using default locale.');
  return new Locale();
}

export function getAll() {
  return objects.values(localesMap);
}

export function getAllLanguageTags() {
  return Object.keys(localesMap);
}

/**
 * Returns the first locale for the given language.
 * @param language a language without country code (e.g. en or de)
 */
export function findFirstForLanguage(language) {
  scout.assertParameter('language', language);
  return arrays.find(getAll(), locale => {
    if (locale.language === language) {
      return locale;
    }
  }, this);
}

/**
 * Splits the language tag and returns an array containing the language and the country.
 */
export function splitLanguageTag(languageTag) {
  if (!languageTag) {
    return [];
  }
  return languageTag.split('-');
}

export default {
  bootstrap,
  findFirstForLanguage,
  get,
  getAll,
  getAllLanguageTags,
  getNavigatorLanguage,
  getNavigatorLocale,
  has,
  init,
  localesMap,
  splitLanguageTag
};
