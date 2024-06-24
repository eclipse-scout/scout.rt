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
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationInventoryTest;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationHelper;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_3;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * A second migration handler for type version {@link BravoFixture_3} for the type name 'bravoFixture.PetFixture', only
 * used within {@link DataObjectMigrationInventoryTest#testValidateMigrationHandlerUniqueness()}.
 */
@IgnoreBean
public class PetFixtureFamilyFriendlyMigrationHandlerInvalidTypeVersionToUpdate_3 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    // The target type version must be in the same namespace to trigger the migration handler correctly.
    // The namespace is then migrated as part of the handler execution.
    return BravoFixture_3.class;
  }

  protected NamespaceVersion typeVersionToUpdate() {
    return AlfaFixture_3.VERSION;
  }

  /**
   * References {@link PetFixtureDo}.
   */
  @Override
  public Set<String> getTypeNames() {
    return CollectionUtility.hashSet("bravoFixture.PetFixture");
  }

  @Override
  protected boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    if (doEntity.has("lorem")) {
      return false; // already migrated
    }

    doEntity.put("lorem", "ipsum");
    return true;
  }

  @Override
  protected boolean updateTypeVersion(IDoEntity doEntity) {
    return BEANS.get(DoStructureMigrationHelper.class).updateTypeVersion(doEntity, typeVersionToUpdate());
  }
}
