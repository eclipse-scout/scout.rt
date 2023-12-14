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

import java.math.BigDecimal;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;

@TypeName("scout.BigDecimalValue")
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
