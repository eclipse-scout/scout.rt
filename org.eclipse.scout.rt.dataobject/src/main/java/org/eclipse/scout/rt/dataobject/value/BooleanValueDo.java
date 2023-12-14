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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;

@TypeName("scout.BooleanValue")
public class BooleanValueDo extends DoEntity implements IValueDo<Boolean> {

  public static BooleanValueDo of(Boolean value) {
    return BEANS.get(BooleanValueDo.class).withValue(value);
  }

  @Override
  public DoValue<Boolean> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BooleanValueDo withValue(Boolean value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getValue() {
    return value().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isValue() {
    return nvl(getValue());
  }
}
