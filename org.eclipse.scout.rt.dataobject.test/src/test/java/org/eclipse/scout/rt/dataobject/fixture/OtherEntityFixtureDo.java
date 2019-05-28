/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("OtherEntityFixture")
@TypeVersion("scout-8.0.0")
public class OtherEntityFixtureDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<Boolean> active() {
    return doValue("active");
  }

  public DoValue<OtherEntityFixtureDo> nestedOtherEntity() {
    return doValue("nestedOtherEntity");
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

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo withNestedOtherEntity(OtherEntityFixtureDo nestedOtherEntity) {
    nestedOtherEntity().set(nestedOtherEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo getNestedOtherEntity() {
    return nestedOtherEntity().get();
  }
}
