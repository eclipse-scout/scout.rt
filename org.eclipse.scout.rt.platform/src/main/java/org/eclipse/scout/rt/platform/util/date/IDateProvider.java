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

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * A provider of the current date and time.
 * <p>
 * Consistently using a date provider enables to test code which uses the current system time in its operations. Use
 * Scout TestingUtility to mock this interface and provide a fixed date.
 * </p>
 *
 * @since 5.2
 */
@ApplicationScoped
public interface IDateProvider {

  /**
   * Provides the current date and time in the default locale, not truncated (as much precision as available).
   *
   * @return current date and time
   */
  Date currentMillis();

  /**
   * Provides the current date and time in the default locale, the time truncated to second precision.
   *
   * @return current date and time
   */
  Date currentSeconds();

  /**
   * Provides the current date and time in the default locale, the time truncated to midnight (00:00:00.000)
   *
   * @return current date
   */
  Date currentDay();

  /**
   * Provides the current date and time in the default locale, the time truncated to the current week
   *
   * @return current date
   */
  Date currentWeek();

  /**
   * Provides the current date and time in the default locale, the time truncated to the current month
   *
   * @return current date
   */
  Date currentMonth();

  /**
   * Provides the current date and time in the default locale, the time truncated to the current year
   *
   * @return current date
   */
  Date currentYear();

  /**
   * Provides the current time zone
   *
   * @return Current time zone
   */
  TimeZone currentTimeZone();

  /**
   * Provides the current calendar
   *
   * @return Current calendar
   */
  Calendar currentCalendar();

}
