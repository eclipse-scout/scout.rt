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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;

/**
 * Change history:
 * <ul>
 * <li>charlieFixture-2: roomName -> name</li>
 * <li>charlieFixture-3: added areaInSquareFoot</li> (would not need an update of type version if no real migration is
 * required)
 * <li>charlieFixture-4: changed areaInSquareFoot to areaInSquareMeter</li>
 * <li>charlieFixture-5: added displayText</li>
 * </ul>
 *
 * @since charlieFixture-1
 */
@TypeName("charlieFixture.RoomFixture")
@TypeVersion(CharlieFixture_5.class)
public class RoomFixtureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<String> displayText() {
    return doValue("displayText");
  }

  public DoValue<Integer> areaInSquareMeter() {
    return doValue("areaInSquareMeter");
  }

  public DoValue<RoomTypeFixtureStringId> roomType() {
    return doValue("roomType");
  }

  public DoValue<IDoEntity> customData() {
    return doValue("customData");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withDisplayText(String displayText) {
    displayText().set(displayText);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getDisplayText() {
    return displayText().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withAreaInSquareMeter(Integer areaInSquareMeter) {
    areaInSquareMeter().set(areaInSquareMeter);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getAreaInSquareMeter() {
    return areaInSquareMeter().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withRoomType(RoomTypeFixtureStringId roomType) {
    roomType().set(roomType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypeFixtureStringId getRoomType() {
    return roomType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomFixtureDo withCustomData(IDoEntity customData) {
    customData().set(customData);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getCustomData() {
    return customData().get();
  }
}
