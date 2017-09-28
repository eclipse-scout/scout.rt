/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.locales = {

  localesMap: {},

  bootstrap: function(url) {
    var promise = url ? $.ajaxJson(url) : $.resolvedPromise([]);
    return promise.done(this.init.bind(this));
  },

  init: function(data) {
    data.forEach(function(locale) {
      this.localesMap[locale.languageTag] = new scout.Locale(locale);
    }, this);
  },

  _get: function(languageTag) {
    return this.localesMap[languageTag];
  },

  /**
   * Checks whether there is a locale definition for the given language tag.
   * @param explicit if true, the country code is considered, meaning if languageTag is 'de-CH'
   *   and there is a locale for 'de' but not for 'de-CH', true will be returned nonetheless. Default false (consistent to #get).
   */
  has: function(languageTag, explicit) {
    explicit = scout.nvl(explicit, false);
    if (explicit) {
      return !!this._get(languageTag);
    }
    return !!this.get(languageTag);
  },

  /**
   * @returns the {@link scout.Locale} for the given languageTag.
   * If there is no locale found for the given tag, it tries to load the locale without the country code.
   * If there is still no locale found, null is returned.
   */
  get: function(languageTag) {
    var locale,
      tags = scout.texts.createOrderedLanguageTags(languageTag);

    tags.some(function(tag) {
      locale = this._get(tag);
      if (locale) {
        return true;
      }
    }, this);

    if (!locale) {
      return null;
    }

    return locale;
  },

  getNavigatorLanguage: function() {
    return navigator.language || navigator.userLanguage;
  },

  /**
   * @returns the {@link scout.Locale} for the language returned by the navigator.
   * If no locale is found, the first locale with the language of the navigator is returned.
   * (e.g. if browser returns 'de' and there is no locale for 'de', check if there is one for 'de-CH', 'de-DE' etc. and take the first.)
   * If still no locale is found, the default locale {@link scout.Locale.DEFAULT} is returned.
   */
  getNavigatorLocale: function() {
    var languageTag = scout.locales.getNavigatorLanguage();
    if (!languageTag) {
      //  No language returned by the browser, using default locale (should not happen with modern browsers, but we never know...)
      $.log.warn('Browser returned no language. Using default locale.');
      return new scout.Locale();
    }

    var locale = scout.locales.get(languageTag);
    if (locale) {
      // If a locale was found for the language returned by the navigator, use that one
      return locale;
    }

    // Otherwise search a locale with the same language
    $.log.info('Locale for languageTag ' + languageTag + ' not found. Trying to load best match.');
    var language = this.splitLanguageTag(languageTag)[0];
    locale = scout.locales.findFirstForLanguage(language);
    if (locale) {
      return locale;
    }

    // If still not found, use the default locale
    $.log.info('Still no matching locale for languageTag ' + languageTag + ' found. Using default locale.');
    return new scout.Locale();
  },

  getAll: function() {
    return scout.objects.values(this.localesMap);
  },

  getAllLanguageTags: function() {
    return Object.keys(this.localesMap);
  },

  /**
   * Returns the first locale for the given language.
   * @param language a language without country code (e.g. en or de)
   */
  findFirstForLanguage: function(language) {
    scout.assertParameter('language', language);
    return scout.arrays.find(this.getAll(), function(locale) {
      if (locale.language === language) {
        return locale;
      }
    }, this);
  },

  /**
   * Splits the language tag and returns an array containing the language and the country.
   */
  splitLanguageTag: function(languageTag) {
    if (!languageTag) {
      return [];
    }
    return languageTag.split('-');
  }

};
