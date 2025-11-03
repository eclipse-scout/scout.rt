/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.Alfafixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.Alfafixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.Alfafixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.Alfafixture_6;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.Alfafixture_7;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.Bravofixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.Bravofixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.Bravofixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.Charliefixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.Charliefixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.Charliefixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.Charliefixture_4;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.Charliefixture_5;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureTypeVersions.Deltafixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.DeltaFixtureTypeVersions.Deltafixture_2;
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
        new Alfafixture_1(), new Alfafixture_2(), new Alfafixture_3(), new Alfafixture_6(), new Alfafixture_7(),
        new Bravofixture_1(), new Bravofixture_2(), new Bravofixture_3(),
        new Charliefixture_1(), new Charliefixture_2(), new Charliefixture_3(), new Charliefixture_4(), new Charliefixture_5(),
        new Deltafixture_1(), new Deltafixture_2());
  }

  public Collection<Class<? extends IDoStructureMigrationTargetContextData>> getFixtureContextDataClasses() {
    return Arrays.asList(
        HouseFixtureStructureMigrationTargetContextData.class,
        HouseFixtureRawOnlyStructureMigrationTargetContextData.class,
        HouseFixtureTypedOnlyStructureMigrationTargetContextData.class,
        PersonFixtureTargetContextData.class);
  }
}
