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
package org.eclipse.scout.rt.ui.html.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.servicetunnel.StaticDate;

/**
 * Helper to transform {@link Date} to a JSON string. Because JSON has no built-in date type, we use a string in a
 * canonical format. There are three valid formats:
 * <ul>
 * <li>"Full": {@value #JSON_PATTERN_FULL}
 * <li>"Date only": {@value #JSON_PATTERN_DATE_ONLY} (time part is implicitly 00:00:00.000)
 * <li>"Time only": {@value #JSON_PATTERN_TIME_ONLY} (date part is implicitly 01.01.1970)
 * </ul>
 * Additionally, each pattern can end with a <code>'Z'</code> character (see note about time zone below).
 * <p>
 * <b>Note about time zones:</b>
 * <p>
 * By default, the conversions always use <i>local time</i>. This means, only the "digits" of the given date are used,
 * the time zone is taken from the environment (i.e. server time). In Scout terminology, this is known as "Static Date".
 * <p>
 * However, there are cases, where an exact point in time has to be transfered, regardless of the local time zone. In
 * this case, the date has to be normalized to UTC. In the JSON representation, such dates are marked with a trailing
 * <code>'Z'</code> character.
 *
 * @see dates.js
 * @see DateFormat.js
 * @see StaticDate.java
 */
public class JsonDate implements IJsonObject {

  public static final String JSON_PATTERN_FULL = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String JSON_PATTERN_DATE_ONLY = "yyyy-MM-dd";
  public static final String JSON_PATTERN_TIME_ONLY = "HH:mm:ss.SSS";
  public static final String UTC_MARKER = "Z";

  private Date m_javaDate = null;
  private String m_jsonString = null;

  public JsonDate(Date javaDate) {
    m_javaDate = javaDate;
  }

  public JsonDate(String jsonString) {
    m_jsonString = jsonString;
  }

  public final Date getJavaDate() {
    return m_javaDate;
  }

  public final String getJsonString() {
    return m_jsonString;
  }

  @Override
  public Object toJson() {
    return asJsonString();
  }

  /**
   * Returns the value as a JSON string in one of the canonical formats. Both date and time parts are returned. The
   * local time zone is used (non-UTC-mode).
   */
  public String asJsonString() {
    return asJsonString(false);
  }

  /**
   * Returns the value as a JSON string in one of the canonical formats. Both date and time parts are returned.
   *
   * @param utc
   *          If <code>true</code>, the date is normalized to the UTC time zone. The return value will then end with a
   *          <code>'Z'</code> character. Otherwise, the local time zone is used (no trailing <code>'Z'</code>).
   */
  public String asJsonString(boolean utc) {
    return asJsonString(utc, true, true);
  }

  /**
   * Returns the value as a JSON string in one of the canonical formats.
   *
   * @param utc
   *          If <code>true</code>, the date is normalized to the UTC time zone. The return value will then end with a
   *          <code>'Z'</code> character. Otherwise, the local time zone is used (no trailing <code>'Z'</code>).
   * @param date
   *          If <code>true</code>, the "date part" (year/month/date) is included in the return value. If the date is
   *          not of concern, you can omit it in the output (and save some space) by setting this parameter to
   *          <code>false</code>.
   * @param time
   *          If <code>true</code>, the "time part" (hours/minutes/seconds/milliseconds) is included in the return
   *          value. If the time is not of concern, you can omit it in the output (and save some space) by setting this
   *          parameter to <code>false</code>.
   */
  public String asJsonString(boolean utc, boolean date, boolean time) {
    if (!date && !time) {
      return null;
    }
    if (m_javaDate != null) {
      String pattern = JSON_PATTERN_FULL;
      if (!date) {
        pattern = JSON_PATTERN_TIME_ONLY;
      }
      else if (!time) {
        pattern = JSON_PATTERN_DATE_ONLY;
      }
      m_jsonString = format(m_javaDate, pattern, utc);
    }
    return m_jsonString;
  }

  /**
   * Returns the value as a Java {@link Date}.
   */
  public Date asJavaDate() {
    if (m_jsonString != null) {
      String pattern = JSON_PATTERN_FULL;
      if (m_jsonString.matches("[\\d-]+Z?")) {
        pattern = JSON_PATTERN_DATE_ONLY;
      }
      else if (m_jsonString.matches("[\\d:.]+Z?")) {
        pattern = JSON_PATTERN_TIME_ONLY;
      }
      m_javaDate = parse(m_jsonString, pattern);
    }
    return m_javaDate;
  }

  public static String format(Date date, String pattern, boolean utc) {
    if (date == null || pattern == null) {
      return null;
    }
    Locale loc = NlsLocale.get();
    SimpleDateFormat sdf = new SimpleDateFormat(pattern, loc);
    if (utc) {
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    return sdf.format(date) + (utc ? UTC_MARKER : "");
  }

  public static Date parse(String input, String pattern) {
    if (StringUtility.isNullOrEmpty(input) || pattern == null) {
      return null;
    }
    try {
      Locale loc = NlsLocale.get();
      SimpleDateFormat sdf = new SimpleDateFormat(pattern, loc);
      if (input.endsWith(UTC_MARKER)) {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        input = input.substring(0, input.length() - 1);
      }
      return sdf.parse(input);
    }
    catch (ParseException e) {
      throw new IllegalArgumentException("parse(\"" + input + "\", \"" + pattern + "\") failed", e);
    }
  }
}
