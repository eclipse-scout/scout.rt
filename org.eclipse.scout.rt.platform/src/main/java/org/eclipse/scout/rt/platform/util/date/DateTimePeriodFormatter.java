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
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
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
   * Formats the given duration to a human-readable text.<br>
   * The format in general is: {@code d [day(s)] h [hour] 'h' m [minute] 'm' s [second] (. zzz [millisecond]) 's'}<br>
   * <ul>
   * <li>This method ensures that digits of the same time-unit always appear in the same position when right-aligned (except for days)</li>
   * <li>Zero-valued units are left out if there is no higher unit with non-zero value.</li>
   * </ul>
   * Examples:
   * <ul>
   *   <li>{@code "3 days 15h 00m 04s"}</li>
   *   <li>{@code "1h 02m 03s"}</li>
   *   <li>{@code "0m 13s"}</li>
   *   <li>{@code "5s"}</li>
   *   <li>{@code "2m 05.123s"}</li>
   *   <li>{@code 0s} (if {@code includeMilliseconds=false})</li>
   *   <li>{@code 0.000s} (if {@code includeMilliseconds=true})</li>
   * </ul>
   *
   * @param duration
   *     The time duration to format.<br>
   *     If this argument is negative, the result is the same as if zero was passed.<br>
   *     If this argument is null, the result is also null.
   * @param includeMilliseconds
   *     If {@code true}, the milliseconds part of the duration is included in the output.
   * @return The period formatted in a human-readable text
   */
  public String formatDuration(Duration duration, boolean includeMilliseconds) {
    // KEEP IN SYNC WITH eclipse-scout-core/src/util/dates.ts#formatDuration(number, boolean, locale)
    if (duration == null) {
      return null;
    }
    if (duration.isNegative()) {
      duration = Duration.ZERO;
    }
    long days = duration.toDaysPart();
    long hours = duration.toHoursPart();
    long minutes = duration.toMinutesPart();
    long seconds = duration.toSecondsPart();

    // the highest non-zero unit can be displayed without padding-zeros
    // all lower units are displayed with padding zeroes, even if they are zero
    // this is done so that two different periods formatted by this algorithm can be easily compared when stacked on top of one another
    // because the digits of the same time-unit always appear in the same position of the text
    boolean showDays = days > 0;
    boolean showHours = showDays || hours > 0;
    boolean showMinutes = showHours || minutes > 0;
    StringBuilder sb = new StringBuilder();
    if (showDays) {
      sb.append(days).append(" ").append(days > 1 ? TEXTS.get("Days") : TEXTS.get("Day")).append(" ");
    }
    if (showHours) {
      // because the days-part has different lengths ('2 days' vs '1 day'), there is no need to pad the hours-part with zeros
      // as the digits of the day cannot possibly align anyway. Make the text more readable by leaving out this leading zero.
      sb.append(hours).append("h ");
    }
    if (showMinutes) {
      sb.append(showHours ? StringUtility.lpad("" + minutes, "0", 2) : minutes).append("m ");
    }
    // seconds are always shown
    sb.append(showMinutes ? StringUtility.lpad("" + seconds, "0", 2) : seconds);
    // milliseconds are always shown if the corresponding flag is set
    if (includeMilliseconds) {
      char decimalSeparator = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get()).getDecimalFormatSymbols().getDecimalSeparator();
      sb.append(decimalSeparator).append(StringUtility.lpad("" + duration.toMillisPart(), "0", 3));
    }
    sb.append("s");
    return sb.toString();
  }

  /**
   * Formats the given duration to a human-readable text with seconds as the highest precision.
   * See {@link #formatDuration(Duration, boolean)}.
   *
   * @param duration
   *     The duration to format
   * @return The formatted text
   */
  public String formatDuration(Duration duration) {
    return formatDuration(duration, false);
  }

  /**
   * Formats the given time period to a human-readable text. Does not include milliseconds.<br>
   * See {@link #formatDuration(Duration, boolean)}.
   *
   * @param periodInDays
   *     The period given in days, i.e. {@code 1 day = 1.0}, {@code 12 hours = 0.5}<br>
   *     The fraction of seconds is rounded with {@link RoundingMode#HALF_UP}.
   * @return The formatted text
   */
  public String formatTimePeriod(BigDecimal periodInDays) {
    if (periodInDays == null) {
      return null;
    }
    int seconds = periodInDays.multiply(BigDecimal.valueOf(SECONDS_PER_DAY)).setScale(0, RoundingMode.HALF_UP).intValue();
    return formatDuration(Duration.ofSeconds(seconds));
  }

  /**
   * Formats the given time period to a human-readable text. Includes milliseconds.<br>
   * See {@link #formatDuration(Duration, boolean)}.
   *
   * @param milliSecs
   *     The period given in milliseconds
   * @return The formatted text
   */
  public String formatTimePeriodOfMs(Long milliSecs) {
    if (milliSecs == null) {
      return null;
    }
    Duration duration = Duration.ofMillis(milliSecs);
    return formatDuration(duration, true);
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
