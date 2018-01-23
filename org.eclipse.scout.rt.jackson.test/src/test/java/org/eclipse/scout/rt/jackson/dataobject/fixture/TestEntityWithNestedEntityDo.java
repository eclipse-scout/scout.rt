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

@TypeName("TestEntityWithNestedEntity")
public class TestEntityWithNestedEntityDo extends DoEntity {

  public DoValue<BigInteger> bigIntegerAttribute() {
    return doValue("bigIntegerAttribute");
  }

  public DoValue<TestItemDo> itemAttribute() {
    return doValue("itemAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithNestedEntityDo withBigIntegerAttribute(BigInteger bigIntegerAttribute) {
    bigIntegerAttribute().set(bigIntegerAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BigInteger getBigIntegerAttribute() {
    return bigIntegerAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithNestedEntityDo withItemAttribute(TestItemDo itemAttribute) {
    itemAttribute().set(itemAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo getItemAttribute() {
    return itemAttribute().get();
  }
}
