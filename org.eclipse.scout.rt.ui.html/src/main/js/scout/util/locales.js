/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
    url = scout.nvl(url, 'res/locales.json');
    return $.ajaxJson(url)
      .done(this.init.bind(this));
  },

  init: function(data) {
    data.forEach(function(locale) {
      this.localesMap[locale.languageTag] = locale;
    }, this);
  },

  _get: function(languageTag) {
    return this.localesMap[languageTag];
  },

  /**
   * @returns the {@link scout.Locale} for the given languageTag.
   * If there is no locale found for the given tag, it tries to load the locale without the country code.
   * If there is still no locale found, null is returned.
   */
  get: function(languageTag) {
    var locale,
      tags = scout.texts.splitLanguageTag(languageTag);

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
   * If no locale is found, the default locale {@link scout.Locale.DEFAULT} is returned.
   */
  getNavigatorLocale: function() {
    var languageTag = this.getNavigatorLanguage(),
      locale = this.get(languageTag);

    if (locale) {
      return locale;
    }

    // Use the default locale
    $.log.info('Locale for languageTag ' + languageTag + ' not found. Using default locale.');
    return new scout.Locale();
  }

};
