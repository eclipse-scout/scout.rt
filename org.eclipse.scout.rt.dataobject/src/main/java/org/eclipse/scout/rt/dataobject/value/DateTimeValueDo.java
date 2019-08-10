/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.value;

import java.util.Date;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Serialized <b>with date and time</b> but <b>without time zone</b> (local date).
 * <ul>
 * <li>Use {@link DateValueDo} if time is irrelevant.
 * <li>Use {@link DateTimeWithTimeZoneValueDo} if time and time zone is relevant.
 * </ul>
 */
@TypeName("scout.DateTimeValue")
public class DateTimeValueDo extends DoEntity implements IValueDo<Date> {

  public static DateTimeValueDo of(Date value) {
    return BEANS.get(DateTimeValueDo.class).withValue(value);
  }

  @Override
  @ValueFormat(pattern = IValueFormatConstants.TIMESTAMP_PATTERN)
  public DoValue<Date> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DateTimeValueDo withValue(Date value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getValue() {
    return value().get();
  }
}
