/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.id.UnknownId;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoValueUntypedMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoValueMigrationId;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;

/**
 * Migration handler migrating former OldHouseTypeFixtureStringId instances to {@link HouseTypeFixtureStringId}.
 */
public class OldHouseTypeFixtureStringIdTypeNameRenameMigrationHandler extends AbstractDoValueUntypedMigrationHandler<UnknownId> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("543df71a-e095-499c-a684-2d4e5604a391");

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return CharlieFixture_1.class;
  }

  @Override
  public Object migrate(DataObjectMigrationContext ctx, UnknownId value) {
    if ("charlieFixture.OldHouseTypeFixtureStringId".equals(value.getIdTypeName())) {
      return HouseTypeFixtureStringId.of(value.getId());
    }
    return value;
  }
}
