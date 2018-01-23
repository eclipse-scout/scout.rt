/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

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
