/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.util.Locale;

import org.eclipse.scout.rt.platform.util.StringUtility;

public final class LocaleUtility {

  private LocaleUtility() {
  }

  /**
   * <b>Note:</b> Since Java 1.7, it is recommended to use {@link Locale#toLanguageTag()} and
   * {@link Locale#forLanguageTag(String)} instead of this method as {@link Locale#toString()} should be used for
   * debugging only.
   *
   * @return the parsed locale such as created by {@link Locale#toString()}
   */
  public static Locale parse(String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    int a = s.indexOf('_');
    int b = (a >= 0 && a + 1 < s.length() ? s.indexOf('_', a + 1) : -1);
    if (a >= 0 && b >= 0) {
      return new Locale(s.substring(0, a), s.substring(a + 1, b), s.substring(b + 1));
    }
    if (a >= 0) {
      return new Locale(s.substring(0, a), s.substring(a + 1));
    }
    return new Locale(s);
  }

  /**
   * Returns the given locale as a string in the form <i>Language, Country</i>. The name of the language is returned in
   * the language of the {@linkplain NlsLocale#get() current locale}. The country is returned in the short form
   * (region/country code). It is omitted if the locale contains no country information.
   * <p>
   * Examples:
   * <ul>
   * <li>"English, US"
   * <li>"German, CH"
   * <li>"Deutsch, CH"
   * </ul>
   *
   * @return {@code null} if the given locale is {@code null}
   */
  public static String getLocaleText(Locale locale) {
    if (locale == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();

    // Language
    sb.append(getLanguageText(locale));

    // Country
    String countryShort = locale.getCountry();
    if (StringUtility.hasText(countryShort)) {
      sb.append(", ").append(countryShort.toUpperCase());
    }

    return sb.toString();
  }

  /**
   * Returns the name of the language represented by the given locale. The name is returned in the language of the
   * {@linkplain NlsLocale#get() current locale}.
   *
   * @return {@code null} if the given locale is {@code null}
   */
  public static String getLanguageText(Locale locale) {
    if (locale == null) {
      return null;
    }
    return locale.getDisplayLanguage(NlsLocale.get());
  }
}
