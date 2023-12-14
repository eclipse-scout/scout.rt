/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ILenientDataObjectMapper;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.QualifiedIIdMapKeyDeserializer;

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

  public DoValue<FixtureCompositeId> compositeId() {
    return doValue("compositeId");
  }

  public DoValue<IId> iid() {
    return doValue("iid");
  }

  /**
   * Used for testing {@link QualifiedIIdDeserializer} regarding type safety.
   */
  public DoValue<IUuId> iUuId() {
    return doValue("iUuId");
  }

  public DoValue<Map<FixtureStringId, String>> map() {
    return doValue("map");
  }

  public DoValue<Map<FixtureCompositeId, String>> compositeMap() {
    return doValue("compositeMap");
  }

  /**
   * Used for testing {@link QualifiedIIdMapKeyDeserializer} regarding type safety.
   */
  public DoValue<Map<IUuId, String>> iUuIdMap() {
    return doValue("iUuIdMap");
  }

  /**
   * Used for testing {@link ILenientDataObjectMapper}.
   */
  public DoValue<Map<IUuId, TestItemDo>> iUuIdDoMap() {
    return doValue("iUuIdDoMap");
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
  public TestEntityWithIIdDo withCompositeId(FixtureCompositeId compositeId) {
    compositeId().set(compositeId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureCompositeId getCompositeId() {
    return compositeId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withIid(IId iid) {
    iid().set(iid);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IId getIid() {
    return iid().get();
  }

  /**
   * See {@link #iUuId()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withIUuId(IUuId iUuId) {
    iUuId().set(iUuId);
    return this;
  }

  /**
   * See {@link #iUuId()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public IUuId getIUuId() {
    return iUuId().get();
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
  public TestEntityWithIIdDo withCompositeMap(Map<FixtureCompositeId, String> compositeMap) {
    compositeMap().set(compositeMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<FixtureCompositeId, String> getCompositeMap() {
    return compositeMap().get();
  }

  /**
   * See {@link #iUuIdMap()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withIUuIdMap(Map<IUuId, String> iUuIdMap) {
    iUuIdMap().set(iUuIdMap);
    return this;
  }

  /**
   * See {@link #iUuIdMap()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Map<IUuId, String> getIUuIdMap() {
    return iUuIdMap().get();
  }

  /**
   * See {@link #iUuIdDoMap()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithIIdDo withIUuIdDoMap(Map<IUuId, TestItemDo> iUuIdDoMap) {
    iUuIdDoMap().set(iUuIdDoMap);
    return this;
  }

  /**
   * See {@link #iUuIdDoMap()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Map<IUuId, TestItemDo> getIUuIdDoMap() {
    return iUuIdDoMap().get();
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
