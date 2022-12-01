/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import java.util.Set;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationHelper;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_3;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * For a type version and type name only one migration handler can exists, thus renaming namespace of type name and
 * adding new attribut in same migration handler.
 */
@IgnoreBean
public class PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
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
    return CollectionUtility.hashSet("bravoFixture.PetFixture"); // references the old type name (is renamed to alfaFixture.PetFixture in PetFixtureAlfaNamespaceMigrationHandler_3)
  }

  @Override
  protected boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    boolean changed = false;
    changed |= BEANS.get(DoStructureMigrationHelper.class).renameTypeName(doEntity, "alfaFixture.PetFixture");

    if (doEntity.has("familyFriendly")) {
      return changed; // already migrated
    }

    doEntity.put("familyFriendly", true);
    return true;
  }

  @Override
  protected boolean updateTypeVersion(IDoEntity doEntity) {
    return BEANS.get(DoStructureMigrationHelper.class).updateTypeVersion(doEntity, typeVersionToUpdate());
  }
}
