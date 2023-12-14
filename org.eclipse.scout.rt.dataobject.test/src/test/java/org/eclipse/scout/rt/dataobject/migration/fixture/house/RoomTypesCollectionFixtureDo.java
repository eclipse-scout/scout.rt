/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;

/**
 * Used for value migration tests for lists, sets and maps.
 */
@TypeName("charlieFixture.RoomTypesCollectionFixture")
@TypeVersion(CharlieFixture_1.class)
public class RoomTypesCollectionFixtureDo extends DoEntity {

  public DoList<RoomTypeFixtureStringId> roomTypesList() {
    return doList("roomTypesList");
  }

  public DoSet<RoomTypeFixtureStringId> roomTypesSet() {
    return doSet("roomTypesSet");
  }

  public DoValue<Map<RoomTypeFixtureStringId, RoomFixtureDo>> roomTypesMap() {
    return doValue("roomTypesMap");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypesCollectionFixtureDo withRoomTypesList(Collection<? extends RoomTypeFixtureStringId> roomTypesList) {
    roomTypesList().updateAll(roomTypesList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypesCollectionFixtureDo withRoomTypesList(RoomTypeFixtureStringId... roomTypesList) {
    roomTypesList().updateAll(roomTypesList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<RoomTypeFixtureStringId> getRoomTypesList() {
    return roomTypesList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypesCollectionFixtureDo withRoomTypesSet(Collection<? extends RoomTypeFixtureStringId> roomTypesSet) {
    roomTypesSet().updateAll(roomTypesSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypesCollectionFixtureDo withRoomTypesSet(RoomTypeFixtureStringId... roomTypesSet) {
    roomTypesSet().updateAll(roomTypesSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<RoomTypeFixtureStringId> getRoomTypesSet() {
    return roomTypesSet().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypesCollectionFixtureDo withRoomTypesMap(Map<RoomTypeFixtureStringId, RoomFixtureDo> roomTypesMap) {
    roomTypesMap().set(roomTypesMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<RoomTypeFixtureStringId, RoomFixtureDo> getRoomTypesMap() {
    return roomTypesMap().get();
  }
}
