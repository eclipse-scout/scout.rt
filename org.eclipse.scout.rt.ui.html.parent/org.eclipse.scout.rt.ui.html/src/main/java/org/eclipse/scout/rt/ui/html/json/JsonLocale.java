/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONObject;

public class JsonLocale implements IJsonObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonLocale.class);

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
    JsonObjectUtility.putProperty(json, "decimalSeparator", String.valueOf(symbols.getDecimalSeparator()));
    JsonObjectUtility.putProperty(json, "groupingSeparator", String.valueOf(symbols.getGroupingSeparator()));
    JsonObjectUtility.putProperty(json, "minusSign", String.valueOf(symbols.getMinusSign()));
    return json;
  }

  protected JSONObject dateFormatSymbolsToJson(DateFormatSymbols symbols) {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "months", JsonObjectUtility.newJSONArray(symbols.getMonths()));
    JsonObjectUtility.putProperty(json, "monthsShort", JsonObjectUtility.newJSONArray(symbols.getShortMonths()));
    JsonObjectUtility.putProperty(json, "weekdays", JsonObjectUtility.newJSONArray(Arrays.copyOfRange(symbols.getWeekdays(), 1, 8)));
    JsonObjectUtility.putProperty(json, "weekdaysShort", JsonObjectUtility.newJSONArray(Arrays.copyOfRange(symbols.getShortWeekdays(), 1, 8)));
    JsonObjectUtility.putProperty(json, "am", symbols.getAmPmStrings()[Calendar.AM]);
    JsonObjectUtility.putProperty(json, "pm", symbols.getAmPmStrings()[Calendar.PM]);
    return json;
  }

  protected JSONObject localeToJson(Locale locale) {
    JSONObject json = new JSONObject();
    DecimalFormat defaultDecimalFormat = getDefaultDecimalFormat(locale);
    SimpleDateFormat defaultDateFormat = getDefaultSimpleDateFormat(locale);
    JsonObjectUtility.putProperty(json, "languageTag", locale.toLanguageTag());
    JsonObjectUtility.putProperty(json, "decimalFormatPatternDefault", defaultDecimalFormat.toPattern());
    JsonObjectUtility.putProperty(json, "dateFormatPatternDefault", defaultDateFormat.toPattern());
    JsonObjectUtility.putProperty(json, "decimalFormatSymbols", decimalFormatSymbolsToJson(defaultDecimalFormat.getDecimalFormatSymbols()));
    JsonObjectUtility.putProperty(json, "dateFormatSymbols", dateFormatSymbolsToJson(defaultDateFormat.getDateFormatSymbols()));
    return json;
  }

  protected static DecimalFormat getDefaultDecimalFormat(Locale locale) {
    NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
    if (numberFormat instanceof DecimalFormat) {
      return (DecimalFormat) numberFormat;
    }
    LOG.info("No locale specific decimal format available, using default locale");
    return new DecimalFormat();
  }

  protected static SimpleDateFormat getDefaultSimpleDateFormat(Locale locale) {
    DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
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
