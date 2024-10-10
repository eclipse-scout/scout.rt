/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * A provider of the current date and time.
 * <p>
 * Consistently using a date provider enables to test code which uses the current system time in its operations. Use
 * Scout's <i>TestingUtility</i> to mock this interface and provide a fixed date.
 * <p>
 * <b>Usage:</b>
 *
 * <pre>
 * Date now = BEANS.get(IDateProvider).currentMillis();
 * data.setTitle("New entry @ " + DateUtility.formatDateTime(now));
 * data.setCreateDate(now);
 * </pre>
 *
 * @since 5.2
 */
@ApplicationScoped
public interface IDateProvider {

  /**
   * Provides the current time (UTC timezone) in milliseconds.
   *
   * @since 9.0
   */
  long currentUTCMillis();

  /**
   * Provides the current date and time in the default locale, not truncated (as much precision as available).
   */
  Date currentMillis();

  /**
   * Provides the current date and time in the default locale, the time truncated to second precision.
   */
  Date currentSeconds();

  /**
   * Provides the current date and time in the default locale, the time truncated to midnight (00:00:00.000)
   */
  Date currentDay();

  /**
   * Provides the current date and time in the default locale, the time truncated to the current week
   */
  Date currentWeek();

  /**
   * Provides the current date and time in the default locale, the time truncated to the current month
   */
  Date currentMonth();

  /**
   * Provides the current date and time in the default locale, the time truncated to the current year
   */
  Date currentYear();

  /**
   * Provides the current time zone
   */
  TimeZone currentTimeZone();

  /**
   * Provides the current calendar
   */
  Calendar currentCalendar();

  /**
   * Provides the current date without timezone
   */
  LocalDate currentLocalDate();

  /**
   * Provides the current date and time without timezone
   */
  LocalDateTime currentLocalDateTime();
}
