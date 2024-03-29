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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IgnoreBean
public class CustomerFixtureMigrationHandler_3 extends AbstractDoStructureMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return AlfaFixture_3.class;
  }

  /**
   * References {@link CustomerFixtureDo}.
   */
  @Override
  public Set<String> getTypeNames() {
    return CollectionUtility.hashSet("alfaFixture.CustomerFixture");
  }

  @Override
  protected boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    if (!doEntity.has("firstName")) {
      return false; // nothing to migrate
    }

    String oldName = doEntity.getString("firstName");
    String newName = StringUtility.uppercase(oldName); // CharlieCustomerFixtureMigrationHandler_3 uses lowercase

    if (ObjectUtility.notEquals(oldName, newName)) {
      doEntity.put("firstName", newName);
      return true;
    }

    return false;
  }
}
