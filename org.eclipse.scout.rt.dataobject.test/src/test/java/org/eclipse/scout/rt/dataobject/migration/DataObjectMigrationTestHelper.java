/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureRawOnlyStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureTypedOnlyStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PersonFixtureTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_6;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_7;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_4;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureTypeVersions.DeltaFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureTypeVersions.DeltaFixture_2;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.namespace.INamespace;

/**
 * Helper methods used within tests to access common fixtures (namespaces, type versions, context data classes).
 */
@ApplicationScoped
public class DataObjectMigrationTestHelper {

  public List<INamespace> getFixtureNamespaces() {
    return Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace(), new DeltaFixtureNamespace());
  }

  public Collection<ITypeVersion> getFixtureTypeVersions() {
    return Arrays.asList(
        new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(), new AlfaFixture_7(),
        new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
        new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5(),
        new DeltaFixture_1(), new DeltaFixture_2());
  }

  public Collection<Class<? extends IDoStructureMigrationTargetContextData>> getFixtureContextDataClasses() {
    return Arrays.asList(
        HouseFixtureStructureMigrationTargetContextData.class,
        HouseFixtureRawOnlyStructureMigrationTargetContextData.class,
        HouseFixtureTypedOnlyStructureMigrationTargetContextData.class,
        PersonFixtureTargetContextData.class);
  }
}
