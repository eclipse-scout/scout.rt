/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject.fixture;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("EntityFixture")
public class EntityFixtureDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoList<OtherEntityFixtureDo> otherEntities() {
    return doList("otherEntities");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntities(Collection<? extends OtherEntityFixtureDo> otherEntities) {
    otherEntities().clear();
    otherEntities().get().addAll(otherEntities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntities(OtherEntityFixtureDo... otherEntities) {
    return withOtherEntities(Arrays.asList(otherEntities));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<OtherEntityFixtureDo> getOtherEntities() {
    return otherEntities().get();
  }
}
