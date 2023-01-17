/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IDateField extends IValueField<Date> {

  String PROP_HAS_DATE = "hasDate";
  String PROP_HAS_TIME = "hasTime";
  String PROP_TIMEPICKER_RESOLUTION = "timePickerResolution";
  String PROP_DATE_FORMAT_PATTERN = "dateFormatPattern";
  String PROP_TIME_FORMAT_PATTERN = "timeFormatPattern";
  String PROP_AUTO_DATE = "autoDate";
  String PROP_ALLOWED_DATES = "allowedDates";

  IDateFieldUIFacade getUIFacade();

  void setFormat(String format);

  String getFormat();

  void setDateFormatPattern(String dateFormatPattern);

  String getDateFormatPattern();

  void setTimeFormatPattern(String timeFormatPattern);

  String getTimeFormatPattern();

  /**
   * @return the date-time format created using {@link #getFormat()} that contains the date and time part
   */
  DateFormat getDateFormat();

  /**
   * @return the date format created using {@link #getFormat()} that only contains the date part
   */
  DateFormat getIsolatedDateFormat();

  /**
   * @return the time format created using {@link #getFormat()} that only contains the time part
   */
  DateFormat getIsolatedTimeFormat();

  boolean isHasDate();

  void setHasDate(boolean b);

  boolean isHasTime();

  void setHasTime(boolean b);

  /**
   * @return the time picker resolution
   * @see IDateField#setTimePickerResolution(int)
   */
  int getTimePickerResolution();

  /**
   * To set the time picker resolution. If the resolution is < 1 the picker will not be displayed. Otherwise the picker
   * starts with every full hour and 00 minutes and increments the minutes with the resolution until the minutes are
   * less than 60.
   *
   * @param resolution
   */
  void setTimePickerResolution(int resolution);

  /**
   * @param autoDate
   *          The date to be used when setting a value "automatically", e.g. when the date picker is opened initially or
   *          when a date or time is entered and the other component has to be filled. <code>null</code> means "use
   *          current date and time".
   */
  void setAutoDate(Date autoDate);

  /**
   * @return the date to be used when setting a value "automatically", e.g. when the date picker is opened initially or
   *         when a date or time is entered and the other component has to be filled. If the return value is
   *         <code>null</code>, the current date and time should be used.
   */
  Date getAutoDate();

  /**
   * @return the time value as a double in the range from [0..1[ for 00:00 - 23:59:59.
   */
  Double getTimeValue();

  /**
   * Set the time value as a double in the range from [0..1[ for 00:00 - 23:59:59.
   */
  void setTimeValue(Double d);

  /**
   * Sets a list of allowed dates. When the given list is not empty or null only the dates contained in the list can be
   * chosen in the date-picker or entered manually in the date-field. All other dates are disabled. When the list is
   * empty or null all dates are available again.
   */
  void setAllowedDates(List<Date> allowedDates);

  List<Date> getAllowedDates();

}
