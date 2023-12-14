/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.value;

import java.util.Date;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Serialized <b>with date, time and time zone</b>.
 * <ul>
 * <li>Use {@link DateValueDo} if time is irrelevant.
 * <li>Use {@link DateTimeValueDo} if time zone is irrelevant.
 * </ul>
 */
@TypeName("scout.DateTimeWithTimeZoneValue")
public class DateTimeWithTimeZoneValueDo extends DoEntity implements IValueDo<Date> {

  public static DateTimeWithTimeZoneValueDo of(Date value) {
    return BEANS.get(DateTimeWithTimeZoneValueDo.class).withValue(value);
  }

  @Override
  @ValueFormat(pattern = IValueFormatConstants.TIMESTAMP_WITH_TIMEZONE_PATTERN)
  public DoValue<Date> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DateTimeWithTimeZoneValueDo withValue(Date value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getValue() {
    return value().get();
  }
}
