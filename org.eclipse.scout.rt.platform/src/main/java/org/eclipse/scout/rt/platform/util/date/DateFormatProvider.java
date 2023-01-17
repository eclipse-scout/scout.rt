/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.date;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Provider for {@link DateFormat} instances and string-patterns for DateFormats. Scout consistently uses this provider
 * whenever a {@link DateFormat} is needed.
 * <p>
 * For {@link DateFormat} instances this default implementation delegates to {@link DateFormat}'s static getters.
 *
 * @since 5.1
 */
@ApplicationScoped
public class DateFormatProvider {

  /**
   * Constant for isolated date pattern.
   */
  public static final int PATTERN_STYLE_ISOLATED_DATE = 1;

  /**
   * Constant for isolated time pattern.
   */
  public static final int PATTERN_STYLE_ISOLATED_TIME = 2;

  /**
   * Constant for isolated time pattern with seconds.
   */
  public static final int PATTERN_STYLE_ISOLATED_TIME_WITH_SECONDS = 3;

  /**
   * Constant for isolated time pattern with seconds and milliseconds.
   */
  public static final int PATTERN_STYLE_ISOLATED_TIME_WITH_MILLISECONDS = 4;

  /**
   * delegates to {@link DateFormat#getAvailableLocales()}
   */
  public Locale[] getAvailableLocales() {
    return DateFormat.getAvailableLocales();
  }

  /**
   * delegates to {@link DateFormat#getTimeInstance(int, Locale)}
   */
  public DateFormat getTimeInstance(int style, Locale locale) {
    return DateFormat.getTimeInstance(style, locale);
  }

  /**
   * delegates to {@link DateFormat#getDateInstance(int, Locale)}
   */
  public DateFormat getDateInstance(int style, Locale locale) {
    return DateFormat.getDateInstance(style, locale);
  }

  /**
   * delegates to {@link DateFormat#getDateTimeInstance(int, int, Locale)}
   */
  public DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
    return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
  }

  public String getDateFormatPattern(int patternStyle, Locale locale) {
    switch (patternStyle) {
      case PATTERN_STYLE_ISOLATED_DATE:
        return getIsolatedDateFormatPattern(locale);
      case PATTERN_STYLE_ISOLATED_TIME:
      case PATTERN_STYLE_ISOLATED_TIME_WITH_SECONDS:
      case PATTERN_STYLE_ISOLATED_TIME_WITH_MILLISECONDS:
        return getIsolatedTimeFormatPattern(patternStyle, locale);
      default:
        throw new IllegalArgumentException("Illegal patternStyle " + patternStyle);
    }
  }

  /**
   * <pre>
   * en-gb    "dd/MM/yyyy"
   * nl-be    "dd/MM/yyyy"
   *
   * fr-ch    "dd.MM.yyyy"
   * it-ch    "dd.MM.yyyy"
   *
   * cs       "d.M.yyyy"
   * fi       "d.M.yyyy"
   *
   * el       "d/M/yyyy"
   *
   * fa       "yyyy/MM/dd"
   * ja       "yyyy/MM/dd"
   *
   * hu       "yyyy.MM.dd"
   *
   * zh       "yyyy-MM-dd"
   *
   * ca       "dd/MM/yyyy"
   * es       "dd/MM/yyyy"
   * fr       "dd/MM/yyyy"
   * gl       "dd/MM/yyyy"
   * it       "dd/MM/yyyy"
   * nl       "dd-MM-yyyy"
   * vi       "dd/MM/yyyy"
   *
   * bs       "dd.MM.yyyy"
   * de       "dd.MM.yyyy"
   * et       "dd.MM.yyyy"
   * me       "dd.MM.yyyy"
   * mk       "dd.MM.yyyy"
   * no       "dd.MM.yyyy"
   * pl       "dd.MM.yyyy"
   * rs       "dd.MM.yyyy"
   * ru       "dd.MM.yyyy"
   * sr       "dd.MM.yyyy"
   * tr       "dd.MM.yyyy"
   * uk       "dd.MM.yyyy"
   *
   * default  "MM/dd/yyyy"
   * </pre>
   */
  protected String getIsolatedDateFormatPattern(Locale locale) {
    if (locale == null) {
      locale = NlsLocale.get();
    }
    String localeName = StringUtility.emptyIfNull(locale.toLanguageTag()).toLowerCase();

    // Check longer locale names first
    if (localeName.startsWith("en-gb") || localeName.startsWith("nl-be")) {
      return "dd/MM/yyyy";
    }
    if (localeName.startsWith("fr-ch") || localeName.startsWith("it-ch")) {
      return "dd.MM.yyyy";
    }

    // Now check short names
    if (localeName.startsWith("cs") || localeName.startsWith("fi")) {
      return "d.M.yyyy";
    }
    if (localeName.startsWith("el")) {
      return "d/M/yyyy";
    }
    if (localeName.startsWith("fa") || localeName.startsWith("ja")) {
      return "yyyy/MM/dd";
    }
    if (localeName.startsWith("hu")) {
      return "yyyy.MM.dd";
    }
    if (localeName.startsWith("zh")) {
      return "yyyy-MM-dd";
    }
    if (localeName.startsWith("no")
        || localeName.startsWith("pl")
        || localeName.startsWith("rs")
        || localeName.startsWith("ru")
        || localeName.startsWith("sr")
        || localeName.startsWith("tr")
        || localeName.startsWith("uk")
        || localeName.startsWith("me")
        || localeName.startsWith("mk")
        || localeName.startsWith("et")
        || localeName.startsWith("de")
        || localeName.startsWith("bs")) {
      return "dd.MM.yyyy";
    }
    if (localeName.startsWith("es")
        || localeName.startsWith("ca")
        || localeName.startsWith("it")
        || localeName.startsWith("fr")
        || localeName.startsWith("gl")
        || localeName.startsWith("vi")) {
      return "dd/MM/yyyy";
    }
    if (localeName.startsWith("nl")) {
      return "dd-MM-yyyy";
    }

    // Default format
    return "MM/dd/yyyy";
  }

  /**
   * <pre>
   * en       "h:mm a"
   *
   * default  "HH:mm"
   * </pre>
   *
   * If pattern style requests seconds and/or milliseconds, :ss or :ss[decimal separator]SSS is added after :mm.
   */
  protected String getIsolatedTimeFormatPattern(int patternStyle, Locale locale) {
    if (locale == null) {
      locale = NlsLocale.get();
    }
    String localeName = StringUtility.emptyIfNull(locale.toLanguageTag()).toLowerCase();

    String secondsSuffix = "";
    if (patternStyle == PATTERN_STYLE_ISOLATED_TIME_WITH_SECONDS) {
      secondsSuffix = ":ss";
    }
    else if (patternStyle == PATTERN_STYLE_ISOLATED_TIME_WITH_MILLISECONDS) {
      DecimalFormat decimalFormat = BEANS.get(NumberFormatProvider.class).getNumberInstance(locale);
      char decimalSeparator = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
      secondsSuffix += ":ss" + decimalSeparator + "SSS";
    }

    if (localeName.startsWith("en")) {
      return "h:mm" + secondsSuffix + " a";
    }

    // Default format
    return "HH:mm" + secondsSuffix;
  }
}
