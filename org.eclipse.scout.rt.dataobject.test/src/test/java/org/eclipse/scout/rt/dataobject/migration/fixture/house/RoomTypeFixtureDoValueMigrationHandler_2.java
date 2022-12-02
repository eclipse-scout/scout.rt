/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoValueMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoValueMigrationId;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.IgnoreBean;

/**
 * Rename room type values: standard-room -> room, small-room -> room
 */
@IgnoreBean
public class RoomTypeFixtureDoValueMigrationHandler_2 extends AbstractDoValueMigrationHandler<RoomTypeFixtureStringId> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("89b9576d-4374-48f8-96ad-5395f635eb36");

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return CharlieFixture_2.class;
  }

  @Override
  public RoomTypeFixtureStringId migrate(DoStructureMigrationContext ctx, RoomTypeFixtureStringId value) {
    return "standard-room".equals(value.unwrap()) || "small-room".equals(value.unwrap()) ? RoomTypesFixture.ROOM : value;
  }
}
