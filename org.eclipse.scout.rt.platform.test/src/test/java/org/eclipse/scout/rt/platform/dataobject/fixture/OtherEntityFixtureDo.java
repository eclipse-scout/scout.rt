/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("OtherEntityFixture")
public class OtherEntityFixtureDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<Boolean> active() {
    return doValue("active");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean isActive() {
    return active().get();
  }
}
