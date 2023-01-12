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

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoValueMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoValueMigrationId;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Replaces existing Room object, which contains values to be migrated.
 */
@IgnoreBean
public class HouseFixtureDoValueMigrationHandler_1 extends AbstractDoValueMigrationHandler<HouseFixtureDo> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("be93eba7-7691-4c16-bf79-16b5c1553a67");

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return CharlieFixture_1.class;
  }

  @Override
  public HouseFixtureDo migrate(DataObjectMigrationContext ctx, HouseFixtureDo value) {
    if (!value.rooms().exists()) {
      return value; // nothing to migrate
    }
    RoomFixtureDo room = CollectionUtility.firstElement(value.getRooms());
    if (room == null || !"tiny room".equals(room.getName())) {
      return value; // already migrated or nothing to migrate
    }

    // completely replace room object
    RoomFixtureDo newRoom = BEANS.get(RoomFixtureDo.class)
        .withName("migrated tiny room")
        // Should normally reference a constant such as RoomTypesFixture.ROOM to make sure it is a valid ID.
        // We intentionally insert a value which needs to be migrated by RoomTypeFixtureDoValueMigrationHandler_2.
        .withRoomType(RoomTypeFixtureStringId.of("standard-room"));

    return BEANS.get(DataObjectHelper.class).clone(value) // clone provided value to allow change detection by caller
        .withRooms(newRoom);
  }
}
