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

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContextDataTarget;
import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

@DoStructureMigrationContextDataTarget(typeNames = {"charlieFixture.PersonFixture"})
public class PersonFixtureTargetContextData implements IDoStructureMigrationTargetContextData {

  private String m_name;

  public String getName() {
    return m_name;
  }

  @Override
  public boolean initialize(DoStructureMigrationContext ctx, IDoEntity doEntity) {
    // There are no migrations for charlieFixture-1, thus this context may only be initialized for >= charlieFixture-2.
    // A context data must only be initialized with an already migrated data object for a certain version.
    // The data object itself must be migrated as well as the correct type version must be set (in case type version switches are used within context)
    assertNotNull(doEntity.getString("relation")); // created by PersonFixtureDoStructureMigrationHandler_2
    assertEquals(CharlieFixture_2.VERSION, NamespaceVersion.of(doEntity.getString("_typeVersion"))); // version must be updated before context data is initialized

    m_name = doEntity.getString("name");
    return true;
  }
}
