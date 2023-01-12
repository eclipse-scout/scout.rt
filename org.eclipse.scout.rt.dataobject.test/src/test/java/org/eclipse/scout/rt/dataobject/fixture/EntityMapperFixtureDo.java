/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("EntityMapperFixture")
public class EntityMapperFixtureDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<OtherEntityMapperFixtureDo> otherEntity() {
    return doValue("otherEntity");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withOtherEntity(OtherEntityMapperFixtureDo otherEntity) {
    otherEntity().set(otherEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityMapperFixtureDo getOtherEntity() {
    return otherEntity().get();
  }
}
