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

@TypeName("scout.IntegerValue")
public class IntegerValueDo extends DoEntity implements IValueDo<Integer> {

  public static IntegerValueDo of(Integer value) {
    return BEANS.get(IntegerValueDo.class).withValue(value);
  }

  @Override
  public DoValue<Integer> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public IntegerValueDo withValue(Integer value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getValue() {
    return value().get();
  }
}
