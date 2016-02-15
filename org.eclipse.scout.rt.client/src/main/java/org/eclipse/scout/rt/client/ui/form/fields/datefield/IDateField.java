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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IDateField extends IValueField<Date> {

  String PROP_HAS_DATE = "hasDate";
  String PROP_HAS_TIME = "hasTime";
  String PROP_DATE_FORMAT_PATTERN = "dateFormatPattern";
  String PROP_TIME_FORMAT_PATTERN = "timeFormatPattern";
  String PROP_AUTO_DATE = "autoDate";

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
   * @param autoDate
   *          The date to be used when setting a value "automatically", e.g. when the date picker is opened initially or
   *          when a date or time is entered and the other component has to be filled. <code>null</code> means
   *          "use current date and time".
   */
  void setAutoDate(Date autoDate);

  /**
   * @return the date to be used when setting a value "automatically", e.g. when the date picker is opened initially or
   *         when a date or time is entered and the other component has to be filled. If the return value is
   *         <code>null</code>, the current date and time should be used.
   */
  Date getAutoDate();

  /**
   * @return the time value as a double in the range from [0..1[ for 00:00 - 23:59:59
   */
  Double getTimeValue();

  /**
   * set the time value as a double in the range from [0..1[ for 00:00 - 23:59:59
   */
  void setTimeValue(Double d);
}
