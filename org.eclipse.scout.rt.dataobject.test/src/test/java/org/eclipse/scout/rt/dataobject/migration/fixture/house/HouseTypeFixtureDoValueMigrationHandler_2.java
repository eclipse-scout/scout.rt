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
 * Rename house type ID value: house -> detached-house
 */
@IgnoreBean
public class HouseTypeFixtureDoValueMigrationHandler_2 extends AbstractDoValueMigrationHandler<HouseTypeFixtureStringId> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("e0d6af03-767a-4212-a4bc-3ff86fd5f3eb");

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return CharlieFixture_2.class;
  }

  @Override
  public HouseTypeFixtureStringId migrate(DoStructureMigrationContext ctx, HouseTypeFixtureStringId value) {
    return "house".equals(value.unwrap()) ? HouseTypesFixture.DETACHED_HOUSE : value;
  }
}
