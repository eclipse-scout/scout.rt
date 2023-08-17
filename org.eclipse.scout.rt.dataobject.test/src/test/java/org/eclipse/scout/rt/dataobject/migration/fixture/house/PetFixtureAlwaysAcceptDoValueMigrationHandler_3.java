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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Same as {@link PetFixtureDoValueMigrationHandler_3} but always accepting (thus ignoring that already applied).
 */
@IgnoreBean
public class PetFixtureAlwaysAcceptDoValueMigrationHandler_3 extends AbstractDoValueMigrationHandler<PetFixtureDo> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("5ef530a7-7383-41f5-92ba-6991abd3d5ec");

  protected static final String NAME_PREFIX = "Nickname: ";

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return AlfaFixture_3.class;
  }

  @Override
  public boolean accept(DataObjectMigrationContext ctx) {
    return true;
  }

  @Override
  public PetFixtureDo migrate(DataObjectMigrationContext ctx, PetFixtureDo value) {
    if (!value.getName().startsWith(NAME_PREFIX)) {
      return value; // already migrated or nothing to migrate
    }

    return BEANS.get(DataObjectHelper.class).cloneLenient(value) // clone provided value to allow change detection by caller
        .withName(StringUtility.removePrefixes(value.getName(), NAME_PREFIX));
  }
}
