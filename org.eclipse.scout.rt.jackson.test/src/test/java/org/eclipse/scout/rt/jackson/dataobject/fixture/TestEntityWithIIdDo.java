/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("scout.TestEntityWithIId")
public class TestEntityWithIIdDo extends DoEntity {

  public DoValue<FixtureUuId> uuId() {
    return doValue("uuId");
  }

  public DoValue<FixtureStringId> stringId() {
    return doValue("stringId");
  }

  public DoValue<FixtureLongId> longId() {
    return doValue("longId");
  }

  public DoValue<Map<FixtureStringId, String>> map() {
    return doValue("map");
  }

  public DoValue<List<FixtureLongId>> longIds() {
    return doValue("longIds");
  }

  public DoList<FixtureLongId> longIdsAsDoList() {
    return doList("longIdsAsDoList");
  }

  public DoValue<List<FixtureStringId>> stringIds() {
    return doValue("stringIds");
  }

  public DoList<FixtureStringId> stringIdsAsDoList() {
    return doList("stringIdsAsDoList");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withUuId(FixtureUuId uuId) {
    uuId().set(uuId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuId getUuId() {
    return uuId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withStringId(FixtureStringId stringId) {
    stringId().set(stringId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureStringId getStringId() {
    return stringId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withLongId(FixtureLongId longId) {
    longId().set(longId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureLongId getLongId() {
    return longId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withMap(Map<FixtureStringId, String> map) {
    map().set(map);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<FixtureStringId, String> getMap() {
    return map().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withLongIds(List<FixtureLongId> longIds) {
    longIds().set(longIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureLongId> getLongIds() {
    return longIds().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withLongIdsAsDoList(Collection<? extends FixtureLongId> longIdsAsDoList) {
    longIdsAsDoList().updateAll(longIdsAsDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withLongIdsAsDoList(FixtureLongId... longIdsAsDoList) {
    longIdsAsDoList().updateAll(longIdsAsDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureLongId> getLongIdsAsDoList() {
    return longIdsAsDoList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withStringIds(List<FixtureStringId> stringIds) {
    stringIds().set(stringIds);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureStringId> getStringIds() {
    return stringIds().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withStringIdsAsDoList(Collection<? extends FixtureStringId> stringIdsAsDoList) {
    stringIdsAsDoList().updateAll(stringIdsAsDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withStringIdsAsDoList(FixtureStringId... stringIdsAsDoList) {
    stringIdsAsDoList().updateAll(stringIdsAsDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureStringId> getStringIdsAsDoList() {
    return stringIdsAsDoList().get();
  }
}
