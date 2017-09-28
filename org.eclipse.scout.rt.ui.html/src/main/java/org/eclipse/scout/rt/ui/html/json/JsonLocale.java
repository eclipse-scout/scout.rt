/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLocale implements IJsonObject {
  private static final Logger LOG = LoggerFactory.getLogger(JsonLocale.class);

  private final Locale m_locale;

  public JsonLocale(Locale locale) {
    m_locale = locale;
  }

  public Locale getLocale() {
    return m_locale;
  }

  @Override
  public JSONObject toJson() {
    return localeToJson(m_locale);
  }

  protected JSONObject decimalFormatSymbolsToJson(DecimalFormatSymbols symbols) {
    JSONObject json = new JSONObject();
    json.put("decimalSeparator", String.valueOf(symbols.getDecimalSeparator()));
    json.put("groupingSeparator", String.valueOf(symbols.getGroupingSeparator()));
    json.put("minusSign", String.valueOf(symbols.getMinusSign()));
    return json;
  }

  protected JSONObject dateFormatSymbolsToJson(DateFormatSymbols symbols) {
    JSONObject json = new JSONObject();
    json.put("months", new JSONArray(Arrays.copyOf(symbols.getMonths(), 12)));
    json.put("monthsShort", new JSONArray(Arrays.copyOf(symbols.getShortMonths(), 12)));
    json.put("weekdays", new JSONArray(Arrays.copyOfRange(symbols.getWeekdays(), 1, 8)));
    json.put("weekdaysShort", new JSONArray(Arrays.copyOfRange(symbols.getShortWeekdays(), 1, 8)));
    json.put("am", symbols.getAmPmStrings()[Calendar.AM]);
    json.put("pm", symbols.getAmPmStrings()[Calendar.PM]);
    return json;
  }

  protected JSONObject localeToJson(Locale locale) {
    JSONObject json = new JSONObject();
    DecimalFormat defaultDecimalFormat = getDefaultDecimalFormat(locale);
    SimpleDateFormat defaultDateFormat = getDefaultSimpleDateFormat(locale);
    DateFormatProvider dateFormatProvider = BEANS.get(DateFormatProvider.class);
    String dateFormatPattern = dateFormatProvider.getDateFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_DATE, locale);
    String timeFormatPattern = dateFormatProvider.getDateFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME, locale);
    json.put("languageTag", locale.toLanguageTag());
    json.put("displayLanguage", locale.getDisplayLanguage(locale));
    json.put("displayCountry", locale.getDisplayCountry(locale));
    json.put("decimalFormatPatternDefault", defaultDecimalFormat.toPattern());
    json.put("dateFormatPatternDefault", dateFormatPattern);
    json.put("timeFormatPatternDefault", timeFormatPattern);
    json.put("decimalFormatSymbols", decimalFormatSymbolsToJson(defaultDecimalFormat.getDecimalFormatSymbols()));
    json.put("dateFormatSymbols", dateFormatSymbolsToJson(defaultDateFormat.getDateFormatSymbols()));
    return json;
  }

  protected static DecimalFormat getDefaultDecimalFormat(Locale locale) {
    return BEANS.get(NumberFormatProvider.class).getNumberInstance(locale);
  }

  protected static SimpleDateFormat getDefaultSimpleDateFormat(Locale locale) {
    DateFormat format = BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.DEFAULT, locale);
    if (format instanceof SimpleDateFormat) {
      return (SimpleDateFormat) format;
    }
    LOG.info("No locale specific date format available, using default locale");
    return new SimpleDateFormat();
  }

  public static JSONObject toJson(Locale locale) {
    return locale == null ? null : new JsonLocale(locale).toJson();
  }
}
