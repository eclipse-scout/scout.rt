/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public interface JsonConstants {

  /**
   * Pattern for dates <b>without</b> time and <b>without</b> time zone.
   * <ul>
   * <li>Users in different time zones see the same formatted date, but not necessarily the same point in time.
   * <li>Serialization: The date object's time zone is ignored.
   * <li>Deserialization: The resulting date object will be in the machine's local time zone.
   * </ul>
   * <i>Useful for:</i> birth dates
   */
  String JSON_DATE_PATTERN = "yyyy-MM-dd";

  /**
   * Pattern for dates <b>with</b> time and <b>without</b> time zone.
   * <ul>
   * <li>Users in different time zones see the same formatted date, but not necessarily the same point in time.
   * <li>Serialization: The date object's time zone is ignored.
   * <li>Deserialization: The resulting date object will be in the machine's local time zone.
   * </ul>
   * <i>Useful for:</i> recurring reminders, dates that should always stay as they were entered
   */
  String JSON_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * Pattern for dates <b>with</b> time and <b>with</b> time zone.
   * <ul>
   * <li>Users in different time zones see the same point in time, but not necessarily the same formatted date.
   * <li>Serialization: The date object's time zone is included in the resulting string.
   * <li>Deserialization: The resulting date object will be in the time zone that was specified by the input string.
   * </ul>
   * <i>Useful for:</i> appointments, log time stamps
   */
  String JSON_TIMESTAMP_WITH_TIMEZONE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS Z";

  /**
   * Used as constant value for {@link JsonTypeInfo#property()} annotation to specify logical class name in JSON object.
   */
  String JSON_TYPE_PROPERTY = "objectType";
}
