/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.math.BigInteger;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestMixedRawBigInteger")
public class TestMixedRawBigIntegerDo extends DoEntity {

  public DoValue<BigInteger> bigIntegerAttribute() {
    return doValue("bigIntegerAttribute");
  }

  public DoValue<Number> numberAttribute() {
    return doValue("numberAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestMixedRawBigIntegerDo withBigIntegerAttribute(BigInteger bigIntegerAttribute) {
    bigIntegerAttribute().set(bigIntegerAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigInteger getBigIntegerAttribute() {
    return bigIntegerAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestMixedRawBigIntegerDo withNumberAttribute(Number numberAttribute) {
    numberAttribute().set(numberAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Number getNumberAttribute() {
    return numberAttribute().get();
  }
}
