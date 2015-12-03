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
