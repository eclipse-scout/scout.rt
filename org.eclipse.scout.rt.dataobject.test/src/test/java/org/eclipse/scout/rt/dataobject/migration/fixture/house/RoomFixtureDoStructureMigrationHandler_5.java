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

import java.util.Set;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IgnoreBean
public class RoomFixtureDoStructureMigrationHandler_5 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return CharlieFixture_5.class;
  }

  /**
   * References {@link RoomFixtureDo}.
   */
  @Override
  public Set<String> getTypeNames() {
    return CollectionUtility.hashSet("charlieFixture.RoomFixture");
  }

  @Override
  protected boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    if (doEntity.has("displayText")) {
      // already migrated
      return false;
    }

    // Default display text format: [house name]: [room name] ([area in square meter]m2)
    String roomName = doEntity.getString("name");
    Integer areaInSquareMeter = doEntity.get("areaInSquareMeter", Integer.class);
    if (areaInSquareMeter != null) {
      roomName += " (" + areaInSquareMeter + "m2)";
    }
    String houseName = ctx.get(HouseFixtureStructureMigrationTargetContextData.class).getName();
    String displayText = StringUtility.join(": ", houseName, roomName);

    doEntity.put("displayText", displayText);

    return true;
  }
}
