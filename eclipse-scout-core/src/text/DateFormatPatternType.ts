/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EnumObject} from '../index';

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
export const DateFormatPatternType = {
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
} as const;

export type DateFormatPatternTypes = EnumObject<typeof DateFormatPatternType>;
