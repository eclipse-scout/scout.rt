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
import java.math.BigInteger;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestDuplicatedAttribute")
public class TestDuplicatedAttributeDo extends DoEntity {

  public DoValue<String> stringAttribute() {
    return doValue("stringAttribute");
  }

  public DoValue<BigDecimal> bigDecimalAttribute() {
    return doValue("bigDecimalAttribute");
  }

  public DoValue<BigInteger> bigIntegerAttribute() {
    return doValue("bigIntegerAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestDuplicatedAttributeDo withStringAttribute(String stringAttribute) {
    stringAttribute().set(stringAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStringAttribute() {
    return stringAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDuplicatedAttributeDo withBigDecimalAttribute(BigDecimal bigDecimalAttribute) {
    bigDecimalAttribute().set(bigDecimalAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigDecimal getBigDecimalAttribute() {
    return bigDecimalAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDuplicatedAttributeDo withBigIntegerAttribute(BigInteger bigIntegerAttribute) {
    bigIntegerAttribute().set(bigIntegerAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigInteger getBigIntegerAttribute() {
    return bigIntegerAttribute().get();
  }
}
