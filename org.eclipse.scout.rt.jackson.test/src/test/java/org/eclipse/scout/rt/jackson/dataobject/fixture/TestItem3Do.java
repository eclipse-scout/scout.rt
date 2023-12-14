/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.math.BigDecimal;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestItem3")
public class TestItem3Do extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<BigDecimal> bigDecimalAttribute() {
    return doValue("bigDecimalAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestItem3Do withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItem3Do withBigDecimalAttribute(BigDecimal bigDecimalAttribute) {
    bigDecimalAttribute().set(bigDecimalAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimal getBigDecimalAttribute() {
    return bigDecimalAttribute().get();
  }
}
