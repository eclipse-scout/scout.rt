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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class TimeScaleBuilder {
  private IActivityMap m_map;

  public TimeScaleBuilder(IActivityMap map) {
    m_map = map;
  }

  public TimeScale build() {
    Date[] days = m_map.getDays();
    TimeScale set = null;
    switch (m_map.getPlanningMode()) {
      case IActivityMap.PLANNING_MODE_INTRADAY: {
        set = new IntradayTimeScale();
        buildIntradayScale(days, set);
        break;
      }
      case IActivityMap.PLANNING_MODE_DAY: {
        set = new TimeScale();
        buildDayScale(days, set);
        break;
      }
      case IActivityMap.PLANNING_MODE_WEEK: {
        set = new TimeScale();
        buildWeekScale(days, set);
        break;
      }
    }
    return set;
  }

  protected void buildIntradayScale(Date[] days, TimeScale scale) {
    scale.setDateFormat(createIntradayFormatWithWeekDay(DateFormat.LONG, DateFormat.SHORT));
    Calendar timeCal = Calendar.getInstance();
    for (Date d : days) {
      MajorTimeColumn curDayColumn = createMajorDayColumn(scale, d);
      timeCal.setTime(d);
      timeCal.set(Calendar.HOUR_OF_DAY, m_map.getFirstHourOfDay());
      int intervalMinutes = (int) Math.max(1, m_map.getIntradayInterval() / 1000L / 60L);
      int n = (m_map.getLastHourOfDay() - m_map.getFirstHourOfDay() + 1) * 60 / intervalMinutes;
      for (int i = 0; i < n; i++) {
        createMinorIntradayColumn(curDayColumn, timeCal.getTime(), intervalMinutes);
        timeCal.add(Calendar.MINUTE, intervalMinutes);
      }
    }
  }

  protected void buildDayScale(Date[] days, TimeScale scale) {
    scale.setDateFormat(createDayFormatWithWeekDay(DateFormat.LONG));
    Calendar dayCal = Calendar.getInstance();
    MajorTimeColumn curMonthColumn = null;
    Calendar curMonthCal = Calendar.getInstance();
    for (Date d : days) {
      dayCal.setTime(d);
      if (curMonthColumn == null || dayCal.get(Calendar.MONTH) != curMonthCal.get(Calendar.MONTH)) {
        curMonthColumn = createMajorMonthColumn(scale, d);
        curMonthCal.setTime(dayCal.getTime());
        DateUtility.truncCalendarToMonth(curMonthCal);
      }
      createMinorDayColumn(curMonthColumn, dayCal.getTime());
    }
  }

  protected void buildWeekScale(Date[] days, TimeScale scale) {
    scale.setDateFormat(new SimpleDateFormat("'" + ScoutTexts.get("Week") + "' w, yyyy"));
    Calendar weekCal = Calendar.getInstance();
    MajorTimeColumn curMonthColumn = null;
    Calendar curMonthCal = Calendar.getInstance();
    for (Date d : days) {
      weekCal.setTime(d);
      if (curMonthColumn == null || weekCal.get(Calendar.MONTH) != curMonthCal.get(Calendar.MONTH)) {
        curMonthColumn = createMajorMonthColumn(scale, d);
        curMonthCal.setTime(weekCal.getTime());
        DateUtility.truncCalendarToMonth(curMonthCal);
      }
      createMinorWeekColumn(curMonthColumn, weekCal.getTime());
    }
  }

  protected MajorTimeColumn createMajorMonthColumn(TimeScale scale, Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    DateUtility.truncCalendarToMonth(cal);
    Date a = cal.getTime();
    // cal.add(Calendar.MONTH,1);
    // Date b=cal.getTime();
    MajorTimeColumn col = new MajorTimeColumn(scale);
    col.setLargeText(new SimpleDateFormat("MMMMM, yyyy").format(a));
    col.setMediumText(new SimpleDateFormat("MMM yy").format(a));
    col.setSmallText(new SimpleDateFormat("MMM").format(a));
    return col;
  }

  protected MinorTimeColumn createMinorWeekColumn(MajorTimeColumn parent, Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    Date a = cal.getTime();
    if (m_map.isWorkDaysOnly()) {
      cal.add(Calendar.DATE, m_map.getWorkDayCount());
    }
    else {
      cal.add(Calendar.WEEK_OF_YEAR, 1);
    }
    cal.setTimeInMillis(cal.getTimeInMillis() - 1);
    Date justBeforeEnd = cal.getTime();
    MinorTimeColumn col = new MinorTimeColumn(parent, a, justBeforeEnd);
    col.setLargeText(new SimpleDateFormat("'" + ScoutTexts.get("Week") + "' w").format(a));
    col.setMediumText(new SimpleDateFormat("w").format(a));
    col.setSmallText(new SimpleDateFormat("w").format(a));
    StringBuilder ttBuf = new StringBuilder();
    ttBuf.append(new SimpleDateFormat("EEEEE").format(a));
    ttBuf.append(", ");
    ttBuf.append(DateFormat.getDateInstance(DateFormat.LONG, LocaleThreadLocal.get()).format(a));
    ttBuf.append(" - ");
    ttBuf.append(new SimpleDateFormat("EEEEE").format(justBeforeEnd));
    ttBuf.append(", ");
    ttBuf.append(DateFormat.getDateInstance(DateFormat.LONG, LocaleThreadLocal.get()).format(justBeforeEnd));
    col.setTooltipText(ttBuf.toString());
    return col;
  }

  protected MajorTimeColumn createMajorDayColumn(TimeScale scale, Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.set(Calendar.HOUR_OF_DAY, m_map.getFirstHourOfDay());
    Date a = cal.getTime();
    // cal.set(Calendar.HOUR_OF_DAY,m_map.getLastHourOfDay()+1);
    // Date b=cal.getTime();
    MajorTimeColumn col = new MajorTimeColumn(scale);
    col.setLargeText(createDayFormatWithWeekDay(DateFormat.MEDIUM).format(a));
    col.setMediumText(DateFormat.getDateInstance(DateFormat.SHORT, LocaleThreadLocal.get()).format(a));
    col.setSmallText(new SimpleDateFormat("dd.MM.").format(a));
    return col;
  }

  protected MinorTimeColumn createMinorDayColumn(MajorTimeColumn parent, Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    Date a = cal.getTime();
    cal.add(Calendar.DATE, 1);
    cal.setTimeInMillis(cal.getTimeInMillis() - 1);
    Date justBeforeEnd = cal.getTime();
    MinorTimeColumn col = new MinorTimeColumn(parent, a, justBeforeEnd);
    col.setLargeText(new SimpleDateFormat("dd").format(a));
    col.setMediumText(new SimpleDateFormat("dd").format(a));
    col.setSmallText(new SimpleDateFormat("dd").format(a));
    StringBuilder ttBuf = new StringBuilder();
    ttBuf.append(new SimpleDateFormat("EEEEE").format(a));
    ttBuf.append(", ");
    ttBuf.append(DateFormat.getDateInstance(DateFormat.LONG, LocaleThreadLocal.get()).format(a));
    col.setTooltipText(ttBuf.toString());
    return col;
  }

  protected MinorTimeColumn createMinorIntradayColumn(MajorTimeColumn parent, Date d, int durationInMinutes) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    int hour = -1;
    if (cal.get(Calendar.MINUTE) == 0) {
      hour = cal.get(Calendar.HOUR_OF_DAY);
    }
    Date a = cal.getTime();
    cal.add(Calendar.MINUTE, durationInMinutes);
    Date b = cal.getTime();
    Date justBeforeEnd = b;
    MinorTimeColumn col = new MinorTimeColumn(parent, a, b);
    if (hour >= 0) {
      col.setLargeText(new SimpleDateFormat("HH:mm").format(a));
      if (hour == m_map.getFirstHourOfDay() || hour == 12) {
        col.setMediumText(new SimpleDateFormat("HH:mm").format(a));
      }
      col.setSmallText("");
    }
    StringBuilder ttBuf = new StringBuilder();
    ttBuf.append(new SimpleDateFormat("EEEEE").format(a));
    ttBuf.append(", ");
    ttBuf.append(DateFormat.getDateInstance(DateFormat.LONG, LocaleThreadLocal.get()).format(a));
    ttBuf.append(", ");
    ttBuf.append(new SimpleDateFormat("HH:mm").format(a));
    ttBuf.append(" - ");
    ttBuf.append(new SimpleDateFormat("HH:mm").format(justBeforeEnd));
    col.setTooltipText(ttBuf.toString());
    return col;
  }

  protected SimpleDateFormat createDayFormatWithWeekDay(int dateStyle) {
    String pat;
    DateFormat df = DateFormat.getDateInstance(dateStyle, LocaleThreadLocal.get());
    if (df instanceof SimpleDateFormat) {
      pat = ((SimpleDateFormat) df).toPattern();
    }
    else {
      pat = "dd.MM.yyyy";
    }
    pat = "EE " + pat;
    //
    return new SimpleDateFormat(pat);
  }

  protected SimpleDateFormat createIntradayFormatWithWeekDay(int dateStyle, int timeStyle) {
    String pat;
    DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, LocaleThreadLocal.get());
    if (df instanceof SimpleDateFormat) {
      pat = ((SimpleDateFormat) df).toPattern();
    }
    else {
      pat = "dd.MM.yyyy HH:mm";
    }
    pat = "EE " + pat;
    return new SimpleDateFormat(pat);
  }

}
