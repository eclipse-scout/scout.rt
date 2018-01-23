package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestItem")
public class TestItemDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public TestItemDo setId(String id) {
    id().set(id);
    return this;
  }

  public DoValue<String> stringAttribute() {
    return doValue("stringAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo withStringAttribute(String stringAttribute) {
    stringAttribute().set(stringAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStringAttribute() {
    return stringAttribute().get();
  }
}
