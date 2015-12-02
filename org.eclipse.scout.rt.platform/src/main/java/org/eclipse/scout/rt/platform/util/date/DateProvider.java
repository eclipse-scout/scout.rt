/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.util.date;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Simple Date provider implementation.
 *
 * @since 5.2
 */
public class DateProvider implements IDateProvider {

  protected Date getDate() {
    return new Date();
  }

  @Override
  public Date currentMillis() {
    return getDate();
  }

  @Override
  public Date currentSeconds() {
    return DateUtility.truncDateToSecond(getDate());
  }

  @Override
  public Date currentDay() {
    return DateUtility.truncDate(getDate());
  }

  @Override
  public Date currentMonth() {
    return DateUtility.truncDateToMonth(getDate());
  }

  @Override
  public Date currentWeek() {
    return DateUtility.truncDateToWeek(getDate());
  }

  @Override
  public Date currentYear() {
    return DateUtility.truncDateToYear(getDate());
  }

  @Override
  public TimeZone currentTimeZone() {
    return TimeZone.getDefault();
  }

  @Override
  public Calendar currentCalendar() {
    return Calendar.getInstance();
  }

}
