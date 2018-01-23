/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import java.util.Date;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.date.DateUtility;

/**
 * Constant values used for {@link ValueFormat} annotation
 */
public interface IValueFormatConstants {

  /**
   * Pattern for dates <b>without</b> time and <b>without</b> time zone.
   * <ul>
   * <li>Users in different time zones see the same formatted date, but not necessarily the same point in time.
   * <li>Serialization: The date object's time zone is ignored.
   * <li>Deserialization: The resulting date object will be in the machine's local time zone.
   * </ul>
   * <i>Useful for:</i> birth dates
   */
  String DATE_PATTERN = "yyyy-MM-dd";

  /**
   * Pattern for dates <b>with</b> time and <b>without</b> time zone.
   * <ul>
   * <li>Users in different time zones see the same formatted date, but not necessarily the same point in time.
   * <li>Serialization: The date object's time zone is ignored.
   * <li>Deserialization: The resulting date object will be in the machine's local time zone.
   * </ul>
   * <i>Useful for:</i> recurring reminders, dates that should always stay as they were entered
   */
  String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * Pattern for dates <b>with</b> time and <b>with</b> time zone.
   * <ul>
   * <li>Users in different time zones see the same point in time, but not necessarily the same formatted date.
   * <li>Serialization: The date object's time zone is included in the resulting string.
   * <li>Deserialization: The resulting date object will be in the time zone that was specified by the input string.
   * </ul>
   * <i>Useful for:</i> appointments, log time stamps
   */
  String TIMESTAMP_WITH_TIMEZONE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS Z";

  /**
   * Default date pattern used in serialization
   *
   * @see #TIMESTAMP_PATTERN
   */
  String DEFAULT_DATE_PATTERN = TIMESTAMP_PATTERN;

  /**
   * Parse function for String-formatted dates using {@link #DEFAULT_DATE_PATTERN}
   *
   * @see DoEntity#get(String, Function)
   */
  Function<Object, Date> parseDefaultDate = value -> DateUtility.parse(Assertions.assertType(value, String.class), DEFAULT_DATE_PATTERN);
}
