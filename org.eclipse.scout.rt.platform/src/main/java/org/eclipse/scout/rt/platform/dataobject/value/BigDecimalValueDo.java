/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject.value;

import java.math.BigDecimal;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("BigDecimalValue")
public class BigDecimalValueDo extends DoEntity implements IValueDo<BigDecimal> {

  public static BigDecimalValueDo of(BigDecimal value) {
    return BEANS.get(BigDecimalValueDo.class).withValue(value);
  }

  @Override
  public DoValue<BigDecimal> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimalValueDo withValue(BigDecimal value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimal getValue() {
    return value().get();
  }
}
