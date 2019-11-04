/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/**
 * Custom JavaScript Date Format
 *
 * Support for formatting and parsing dates based on a pattern string and some locale
 * information from the server model. A subset of the standard Java pattern strings
 * (see SimpleDateFormat) with the most commonly used patterns is supported.
 *
 * This object only operates on the local time zone.
 * <p>
 * locale.dateFormatSymbols contains:
 * <ul>
 * <li>weekdays start with Sunday (starts at 0 and not 1 as it does in java)</li>
 * <li>weekdaysShort start with Sunday (starts at 0 and not 1 as it does in java)</li>
 * <li>months start with January</li>
 * <li>monthsShort start with January<7li>
 * <li>am</li>
 * <li>pm</li>
 *</ul>
 *
 * @see http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
 */
const DateFormatPatternType = {
  YEAR: 'year',
  MONTH: 'month',
  WEEK_IN_YEAR: 'week_in_year',
  DAY_IN_MONTH: 'day_in_month',
  WEEKDAY: 'weekday',
  HOUR_24: 'hour_24',
  HOUR_12: 'hour_12',
  AM_PM: 'am_pm',
  MINUTE: 'minute',
  SECOND: 'second',
  MILLISECOND: 'millisecond',
  TIMEZONE: 'timezone'
};

export default DateFormatPatternType;
