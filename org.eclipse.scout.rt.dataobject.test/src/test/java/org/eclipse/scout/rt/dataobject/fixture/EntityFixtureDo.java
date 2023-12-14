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
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("EntityFixture")
public class EntityFixtureDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<OtherEntityFixtureDo> otherEntity() {
    return doValue("otherEntity");
  }

  public DoList<OtherEntityFixtureDo> otherEntities() {
    return doList("otherEntities");
  }

  public DoValue<List<OtherEntityFixtureDo>> otherEntitiesList() {
    return doValue("otherEntitiesList");
  }

  public DoValue<Map<String, OtherEntityFixtureDo>> otherEntitiesMap() {
    return doValue("otherEntitiesMap");
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
  public EntityFixtureDo withOtherEntity(OtherEntityFixtureDo otherEntity) {
    otherEntity().set(otherEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OtherEntityFixtureDo getOtherEntity() {
    return otherEntity().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntities(Collection<? extends OtherEntityFixtureDo> otherEntities) {
    otherEntities().updateAll(otherEntities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntities(OtherEntityFixtureDo... otherEntities) {
    otherEntities().updateAll(otherEntities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<OtherEntityFixtureDo> getOtherEntities() {
    return otherEntities().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntitiesList(List<OtherEntityFixtureDo> otherEntitiesList) {
    otherEntitiesList().set(otherEntitiesList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<OtherEntityFixtureDo> getOtherEntitiesList() {
    return otherEntitiesList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntitiesMap(Map<String, OtherEntityFixtureDo> otherEntitiesMap) {
    otherEntitiesMap().set(otherEntitiesMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, OtherEntityFixtureDo> getOtherEntitiesMap() {
    return otherEntitiesMap().get();
  }
}
