/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;

/**
 * Used for value migration tests for composite objects handled by
 * {@link RoomTypeCompositeFixtureDataObjectVisitorExtension}.
 */
@TypeName("charlieFixture.RoomTypeCompositeFixture")
@TypeVersion(CharlieFixture_1.class)
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
