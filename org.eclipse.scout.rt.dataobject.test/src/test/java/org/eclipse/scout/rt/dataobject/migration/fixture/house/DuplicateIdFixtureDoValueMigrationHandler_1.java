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

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoValueMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoValueMigrationId;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureTypeVersions.DeltaFixture_1;
import org.eclipse.scout.rt.platform.IgnoreBean;

@IgnoreBean
public class DuplicateIdFixtureDoValueMigrationHandler_1 extends AbstractDoValueMigrationHandler<IDoEntity> {

  // Same ID as RoomSizeFixtureDoValueMigrationHandler
  public static final DoValueMigrationId ID = DoValueMigrationId.of("ce318ac4-8b6e-4022-8de0-a0becb7358e5");

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return DeltaFixture_1.class;
  }

  @Override
  public IDoEntity migrate(DataObjectMigrationContext ctx, IDoEntity value) {
    return value; // no migration, used for checking for duplicate migration IDs
  }
}
