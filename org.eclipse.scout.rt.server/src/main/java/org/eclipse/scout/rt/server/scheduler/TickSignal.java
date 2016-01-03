/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.scheduler;

import org.eclipse.scout.rt.platform.job.IJobManager;

/**
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@Deprecated
public class TickSignal {
  private int m_second;
  private int m_minute;
  private int m_hour;
  private int m_dayOfWeek;
  private int m_day;
  private int m_dayOfMonthReverse;
  private int m_dayOfYear;
  private int m_week;
  private int m_month;
  private int m_year;
  private int m_secondOfDay;

  public TickSignal(int second, int minute, int hour, int dayOfWeek, int day, int dayOfMonthReverse, int dayOfYear, int week, int month, int year) {
    m_second = second;
    m_minute = minute;
    m_hour = hour;
    m_dayOfWeek = dayOfWeek;
    m_day = day;
    m_dayOfMonthReverse = dayOfMonthReverse;
    m_dayOfYear = dayOfYear;
    m_week = week;
    m_month = month;
    m_year = year;
    m_secondOfDay = m_hour * 3600 + m_minute * 60 + m_second;
  }

  public int getSecond() {
    return m_second;
  }

  public int getMinute() {
    return m_minute;
  }

  public int getHour() {
    return m_hour;
  }

  public int getDayOfWeek() {
    return m_dayOfWeek;
  }

  /**
   * day of month
   */
  public int getDay() {
    return m_day;
  }

  public int getDayOfMonthReverse() {
    return m_dayOfMonthReverse;
  }

  public int getDayOfYear() {
    return m_dayOfYear;
  }

  /**
   * week of year
   */
  public int getWeek() {
    return m_week;
  }

  /**
   * month of year
   */
  public int getMonth() {
    return m_month;
  }

  public int getYear() {
    return m_year;
  }

  public int getSecondOfDay() {
    return m_secondOfDay;
  }

  public String getTextShort() {
    return formatAsDoubleDigit(m_day) + "." + formatAsDoubleDigit(m_month) + "." + m_year + " " + formatAsDoubleDigit(m_hour) + ":" + formatAsDoubleDigit(m_minute) + ":" + formatAsDoubleDigit(m_second);
  }

  public String getTextLong() {
    return formatAsDoubleDigit(m_day) + "." + formatAsDoubleDigit(m_month) + "." + m_year + " " + formatAsDoubleDigit(m_hour) + ":" + formatAsDoubleDigit(m_minute) + ":" + formatAsDoubleDigit(m_second) + " dayOfWeek=" + m_dayOfWeek
        + " dayOfMonthReverse=" + m_dayOfMonthReverse + " week=" + m_week + " dayOfYear=" + m_dayOfYear;
  }

  @Override
  public String toString() {
    return "TickSignal[" + getTextLong() + "]";
  }

  private String formatAsDoubleDigit(int i) {
    String s = "" + (i + 100);
    return s.substring(s.length() - 2);
  }

}
