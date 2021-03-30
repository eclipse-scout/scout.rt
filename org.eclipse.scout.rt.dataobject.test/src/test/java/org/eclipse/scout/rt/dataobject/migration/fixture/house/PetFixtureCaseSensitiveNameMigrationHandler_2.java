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
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_2;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IgnoreBean
public class PetFixtureCaseSensitiveNameMigrationHandler_2 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return BravoFixture_2.class;
  }

  /**
   * References {@link PetFixtureDo}.
   */
  @Override
  public Set<String> getTypeNames() {
    return CollectionUtility.hashSet("bravoFixture.PetFixture");
  }

  @Override
  protected boolean migrate(DoStructureMigrationContext ctx, IDoEntity doEntity) {
    if (!doEntity.has("name")) {
      return false; // nothing to migrate
    }

    String oldName = doEntity.getString("name");
    String newName = StringUtility.uppercaseFirst(StringUtility.lowercase(oldName));

    if (ObjectUtility.notEquals(oldName, newName)) {
      doEntity.put("name", newName);
      return true;
    }

    return false;
  }
}
