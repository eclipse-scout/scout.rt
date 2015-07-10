/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

public interface IDateField extends IBasicField<Date> {

  String PROP_HAS_DATE = "hasDate";
  String PROP_HAS_TIME = "hasTime";
  String PROP_DATE_FORMAT_PATTERN = "dateFormatPattern";
  String PROP_TIME_FORMAT_PATTERN = "timeFormatPattern";

  @Override
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
   * if {@link IDateField#isHasTime()} == true the auto time millis is considered if only a date input occurs.<BR>
   * <b>NOTE:</b> in case of 0l the current time will be taken.
   *
   * @param l
   */
  void setAutoTimeMillis(long l);

  /**
   * if {@link IDateField#isHasTime()} == true the auto time millis is considered if only a date input occurs.<BR>
   * <b>NOTE:</b> in case of 0l the current time will be taken.
   *
   * @param l
   */
  void setAutoDate(Date d);

  /**
   * if {@link IDateField#isHasDate()} == true the auto date is considered if only a time input occurs.<br>
   * <b>NOTE:</b> in case of null the current date will be taken.
   *
   * @param l
   */
  void setAutoTimeMillis(int hour, int minute, int second);

  /**
   * if {@link IDateField#isHasTime()} == true the auto time millis is considered if only a date input occurs.<BR>
   * <b>NOTE:</b> in case of 0l the current time will be taken.
   *
   * @param l
   */
  long getAutoTimeMillis();

  /**
   * @return the time value as a double in the range from [0..1[ for 00:00 - 23:59:59
   */
  Double getTimeValue();

  /**
   * set the time value as a double in the range from [0..1[ for 00:00 - 23:59:59
   */
  void setTimeValue(Double d);

  void adjustDate(int days, int months, int years);

  void adjustTime(int days, int months, int years);
}
