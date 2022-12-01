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
 * Removes the prefix "Name: " from the name attribute (if the prefix is present).
 */
@IgnoreBean
public class PetFixtureDoValueMigrationHandler_3 extends AbstractDoValueMigrationHandler<PetFixtureDo> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("4650729f-1c79-4553-8eb0-715bd7dac9fd");

  protected static final String NAME_PREFIX = "Name: ";

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return AlfaFixture_3.class;
  }

  @Override
  public PetFixtureDo migrate(DataObjectMigrationContext ctx, PetFixtureDo value) {
    if (!value.getName().startsWith(NAME_PREFIX)) {
      return value; // already migrated or nothing to migrate
    }

    return BEANS.get(DataObjectHelper.class).clone(value) // clone provided value to allow change detection by caller
        .withName(StringUtility.removePrefixes(value.getName(), NAME_PREFIX));
  }
}
