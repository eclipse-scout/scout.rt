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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_4;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@IgnoreBean
public class RoomFixtureDoStructureMigrationHandler_4 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return CharlieFixture_4.class;
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
    if (!doEntity.has("areaInSquareFoot")) {
      // already migrated or nothing to migrate
      return false;
    }

    Integer areaInSquareFoot = doEntity.get("areaInSquareFoot", Integer.class);
    if (areaInSquareFoot != null) {
      int areaInSquareMeter = Math.round(areaInSquareFoot.intValue() / 10.764f);
      doEntity.put("areaInSquareMeter", areaInSquareMeter);
    }
    doEntity.remove("areaInSquareFoot");

    return true;
  }
}
