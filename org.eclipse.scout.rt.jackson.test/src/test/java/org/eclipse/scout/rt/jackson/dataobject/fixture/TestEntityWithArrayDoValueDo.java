/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestEntityWithArrayDoValue")
public class TestEntityWithArrayDoValueDo extends DoEntity {

  public DoValue<String[]> stringArrayAttribute() {
    return doValue("stringArrayAttribute");
  }

  public DoValue<TestItemDo[]> itemDoArrayAttribute() {
    return doValue("itemDoArrayAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithArrayDoValueDo withStringArrayAttribute(String[] stringArrayAttribute) {
    stringArrayAttribute().set(stringArrayAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String[] getStringArrayAttribute() {
    return stringArrayAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithArrayDoValueDo withItemDoArrayAttribute(TestItemDo[] itemDoArrayAttribute) {
    itemDoArrayAttribute().set(itemDoArrayAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo[] getItemDoArrayAttribute() {
    return itemDoArrayAttribute().get();
  }
}
