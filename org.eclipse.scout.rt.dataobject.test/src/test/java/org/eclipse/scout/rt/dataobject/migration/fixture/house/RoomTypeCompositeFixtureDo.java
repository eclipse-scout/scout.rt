/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.Charliefixture_1;

/**
 * Used for value migration tests for composite objects handled by
 * {@link RoomTypeCompositeFixtureDataObjectVisitorExtension}.
 */
@TypeName("charlieFixture.RoomTypeCompositeFixture")
@TypeVersion(Charliefixture_1.class)
public class RoomTypeCompositeFixtureDo extends DoEntity {

  public DoValue<RoomTypeCompositeFixture> roomTypeComposite() {
    return doValue("roomTypeComposite");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypeCompositeFixtureDo withRoomTypeComposite(RoomTypeCompositeFixture roomTypeComposite) {
    roomTypeComposite().set(roomTypeComposite);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public RoomTypeCompositeFixture getRoomTypeComposite() {
    return roomTypeComposite().get();
  }
}
