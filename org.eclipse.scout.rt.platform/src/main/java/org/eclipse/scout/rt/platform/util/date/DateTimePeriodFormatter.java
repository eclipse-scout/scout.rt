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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Helper to format various types of date/time and periods.
 */
@ApplicationScoped
public class DateTimePeriodFormatter {

  public static final int SECONDS_PER_DAY = 86400; // 60 * 60 * 24

  /**
   * Format: "d hh:mm:ss" = d [day(s)] hh [hour] : mm [minute] : ss [second] <br>
   * <ul>
   * <li>1 day = 1.0d</li>
   * <li>If the argument d is negative, the result is '00:00:00'</li>
   * <li>If the argument d is null, the result is null</li>
   * <li>Fraction of seconds are rounded with {@link RoundingMode#HALF_UP}</li>
   * </ul>
   * Example: {@code "3 days 15:42:54"}
   */
  public String formatTimePeriod(BigDecimal d) {
    if (d == null) {
      return null;
    }
    else if (d.compareTo(BigDecimal.ZERO) <= 0) {
      return "00:00:00";
    }
    long days = d.longValue();
    int sec = d.subtract(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(SECONDS_PER_DAY)).setScale(0, RoundingMode.HALF_UP).intValue();
    int s = sec % 60;
    sec = sec / 60;
    int m = sec % 60;
    sec = sec / 60;
    int h = sec % 24;
    String t = "";
    if (days > 0) {
      if (days > 1) {
        t = days + " " + TEXTS.get("Days") + " ";
      }
      else {
        t = days + " " + TEXTS.get("Day") + " ";
      }
    }
    t = t + StringUtility.lpad("" + h, "0", 2) + ":" + StringUtility.lpad("" + m, "0", 2) + ":" + StringUtility.lpad("" + s, "0", 2);
    return t;
  }

  /**
   * Format: "d hh:mm:ss.zzz" = d [day(s)] hh [hour] : mm [minute] : ss [second] . zzz [millisecond]
   * <ul>
   * <li>1 day = 86400000L (1000 * 60 * 60 * 24)</li>
   * <li>If the argument milliSecs is negative, the result is '00:00:00.000'</li>
   * <li>If the argument milliSecs is null, the result is null</li>
   * </ul>
   * Example: {@code "1 day 15:42:54.002"}
   */
  public String formatTimePeriodOfMs(Long milliSecs) {
    char decimalSeparator = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get()).getDecimalFormatSymbols().getDecimalSeparator();
    if (milliSecs == null) {
      return null;
    }
    else if (milliSecs.longValue() <= 0) {
      return "00:00:00" + decimalSeparator + "000";
    }

    BigDecimal msConvertedToDays = new BigDecimal(milliSecs - (milliSecs % 1000)).divide(new BigDecimal(1000 * SECONDS_PER_DAY), MathContext.DECIMAL128);
    return formatTimePeriod(msConvertedToDays) + decimalSeparator + StringUtility.lpad("" + milliSecs % 1000, "0", 3);
  }

  /**
   * Returns a localized string with the date part in format {@link DateFormat#SHORT} and the time part in
   * {@link DateFormat#MEDIUM}. <br>
   * This method can be used when a date-time should be displayed with seconds (since Scout uses
   * {@link DateFormat#SHORT} for both parts by default).
   */
  public String formatDateTimeWithSeconds(Date date) {
    if (date == null) {
      return null;
    }
    DateFormat dateFormatWithSeconds = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, NlsLocale.get());
    return dateFormatWithSeconds.format(date);
  }

  /**
   * Returns a localized string with the date part in format {@link DateFormat#SHORT}, the time part in
   * {@link DateFormat#MEDIUM} and additionally the milliseconds.<br>
   * This method can be used when a date-time should be displayed with milliseconds (since Scout uses
   * {@link DateFormat#SHORT} for both parts by default and Java doesn't provide a localized solution for this). This
   * method fails for locales where numbers are formatted right to left in formatting pattern.
   */
  // The generated pattern could be cached per Locale. Preliminary tests didn't show performance problems.
  public String formatDateTimeWithMilliSeconds(Date date) {
    if (date == null) {
      return null;
    }
    // get seconds and milliseconds patterns. In most locales ss and SSS.
    String localPatternChars = DateFormatSymbols.getInstance(NlsLocale.get()).getLocalPatternChars();
    String secondSymbol = Character.toString(localPatternChars.charAt(DateFormat.SECOND_FIELD));
    String secondsPattern = secondSymbol + secondSymbol;

    String millisecondSymbol = Character.toString(localPatternChars.charAt(DateFormat.MILLISECOND_FIELD));
    String millisecondsPattern = millisecondSymbol + millisecondSymbol + millisecondSymbol;

    // Add milliseconds with decimalSeparator after seconds.
    DecimalFormat decimalFormat = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get());
    char decimalSeparator = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
    DateFormat dateFormatWithSeconds = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, NlsLocale.get());
    if (dateFormatWithSeconds instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) dateFormatWithSeconds).toPattern();
      int index = pattern.indexOf(secondsPattern);
      if (index >= 0) {
        pattern = pattern.substring(0, index + secondsPattern.length()) + decimalSeparator + millisecondsPattern + pattern.substring(index + secondsPattern.length());
      }
      // format value
      SimpleDateFormat milliFormatter = new SimpleDateFormat(pattern, NlsLocale.get());
      return milliFormatter.format(date);
    }
    else {
      return dateFormatWithSeconds.format(date);
    }
  }
}
