/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Locale, LocaleModel, objects, scout, texts} from '../index';
import $ from 'jquery';

export const locales = {
  localesMap: {},

  bootstrap(url: string): JQuery.Promise<void> {
    let promise: JQuery.Promise<any> = url ? $.ajaxJson(url) : $.resolvedPromise([]);
    return promise.then(locales._handleBootstrapResponse.bind(this, url));
  },

  /** @internal */
  _handleBootstrapResponse(url: string, data: any) {
    if (data && data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    locales.init(data);
  },

  init(data: LocaleModel[]) {
    data.forEach(locale => {
      locales.localesMap[locale.languageTag] = new Locale(locale);
    });
  },

  /** @internal */
  _get(languageTag: string): Locale {
    return locales.localesMap[languageTag];
  },

  /**
   * Checks whether there is a locale definition for the given language tag.
   * @param explicit if true, the country code is considered, meaning if languageTag is 'de-CH'
   *   and there is a locale for 'de' but not for 'de-CH', true will be returned nonetheless. Default false (consistent to #get).
   */
  has(languageTag: string, explicit?: boolean): boolean {
    explicit = scout.nvl(explicit, false);
    if (explicit) {
      return !!locales._get(languageTag);
    }
    return !!locales.get(languageTag);
  },

  /**
   * @returns the locale for the given languageTag.
   * If there is no locale found for the given tag, it tries to load the locale without the country code.
   * If there is still no locale found, null is returned.
   */
  get(languageTag: string): Locale {
    let locale,
      tags = texts.createOrderedLanguageTags(languageTag);

    tags.some(tag => {
      locale = locales._get(tag);
      return !!locale;
    }, this);

    if (!locale) {
      return null;
    }

    return locale;
  },

  getNavigatorLanguage(): string {
    return navigator.language || navigator['userLanguage'];
  },

  /**
   * @returns for the language returned by the navigator.
   * If no locale is found, the first locale with the language of the navigator is returned.
   * (e.g. if browser returns 'de' and there is no locale for 'de', check if there is one for 'de-CH', 'de-DE' etc. and take the first.)
   * If still no locale is found, the default locale {@link Locale.DEFAULT} is returned.
   */
  getNavigatorLocale(): Locale {
    let languageTag = locales.getNavigatorLanguage();
    if (!languageTag) {
      //  No language returned by the browser, using default locale (should not happen with modern browsers, but we never know...)
      $.log.warn('Browser returned no language. Using default locale.');
      return new Locale();
    }

    let locale = locales.get(languageTag);
    if (locale) {
      // If a locale was found for the language returned by the navigator, use that one
      return locale;
    }

    // Otherwise search a locale with the same language
    $.log.isInfoEnabled() && $.log.info('Locale for languageTag ' + languageTag + ' not found. Trying to load best match.');
    let language = locales.splitLanguageTag(languageTag)[0];
    locale = locales.findFirstForLanguage(language);
    if (locale) {
      return locale;
    }

    // If still not found, use the default locale
    $.log.isInfoEnabled() && $.log.info('Still no matching locale for languageTag ' + languageTag + ' found. Using default locale.');
    return new Locale();
  },

  getAll(): Locale[] {
    return objects.values(locales.localesMap);
  },

  getAllLanguageTags(): string[] {
    return Object.keys(locales.localesMap);
  },

  /**
   * Returns the first locale for the given language.
   * @param language a language without country code (e.g. en or de)
   */
  findFirstForLanguage(language: string): Locale {
    scout.assertParameter('language', language);
    return arrays.find(locales.getAll(), locale => locale.language === language, this);
  },

  /**
   * Splits the language tag and returns an array containing the language and the country.
   */
  splitLanguageTag(languageTag: string): string[] {
    if (!languageTag) {
      return [];
    }
    return languageTag.split('-');
  }
};
