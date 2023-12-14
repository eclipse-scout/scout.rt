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

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0;

@TypeName("OtherEntityFixture")
@TypeVersion(DataObjectFixture_1_0_0.class)
public class OtherEntityFixtureDo extends DoEntity implements IInterfaceFixtureDo {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<Boolean> active() {
    return doValue("active");
  }

  public DoList<String> items() {
    return doList("items");
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
  public Boolean getActive() {
    return active().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isActive() {
    return nvl(getActive());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo withItems(Collection<? extends String> items) {
    items().updateAll(items);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo withItems(String... items) {
    items().updateAll(items);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getItems() {
    return items().get();
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
