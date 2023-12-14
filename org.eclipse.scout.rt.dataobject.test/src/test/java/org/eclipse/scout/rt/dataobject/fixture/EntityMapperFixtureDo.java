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
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
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

  public DoCollection<String> stringCollection() {
    return doCollection("stringCollection");
  }

  public DoCollection<OtherEntityMapperFixtureDo> entityCollection() {
    return doCollection("entityCollection");
  }

  public DoList<OtherEntityMapperFixtureDo> entityList() {
    return doList("entityList");
  }

  public DoSet<OtherEntityMapperFixtureDo> entitySet() {
    return doSet("entitySet");
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

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withStringCollection(Collection<? extends String> stringCollection) {
    stringCollection().updateAll(stringCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withStringCollection(String... stringCollection) {
    stringCollection().updateAll(stringCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<String> getStringCollection() {
    return stringCollection().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withEntityCollection(Collection<? extends OtherEntityMapperFixtureDo> entityCollection) {
    entityCollection().updateAll(entityCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withEntityCollection(OtherEntityMapperFixtureDo... entityCollection) {
    entityCollection().updateAll(entityCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<OtherEntityMapperFixtureDo> getEntityCollection() {
    return entityCollection().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withEntityList(Collection<? extends OtherEntityMapperFixtureDo> entityList) {
    entityList().updateAll(entityList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withEntityList(OtherEntityMapperFixtureDo... entityList) {
    entityList().updateAll(entityList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<OtherEntityMapperFixtureDo> getEntityList() {
    return entityList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withEntitySet(Collection<? extends OtherEntityMapperFixtureDo> entitySet) {
    entitySet().updateAll(entitySet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperFixtureDo withEntitySet(OtherEntityMapperFixtureDo... entitySet) {
    entitySet().updateAll(entitySet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<OtherEntityMapperFixtureDo> getEntitySet() {
    return entitySet().get();
  }
}
