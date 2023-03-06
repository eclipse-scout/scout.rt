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
import org.eclipse.scout.rt.dataobject.migration.AbstractDoDeletionMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Migration empty on purpose to test {@link AbstractDoDeletionMigrationHandler}.
 */
@IgnoreBean
public class FloorFixtureDoStructureMigrationHandler_5 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return CharlieFixture_5.class;
  }

  @Override
  public Set<String> getTypeNames() {
    return CollectionUtility.hashSet("charlieFixture.FloorFixture");
  }

  @Override
  protected boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    return true;
  }
}
