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
 * Serialized <b>with date</b> but <b>without time</b>.
 * <p>
 * Use {@link DateTimeValueDo} if time is relevant.
 */
@TypeName("scout.DateValue")
public class DateValueDo extends DoEntity implements IValueDo<Date> {

  public static DateValueDo of(Date value) {
    return BEANS.get(DateValueDo.class).withValue(value);
  }

  @Override
  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<Date> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DateValueDo withValue(Date value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getValue() {
    return value().get();
  }
}
