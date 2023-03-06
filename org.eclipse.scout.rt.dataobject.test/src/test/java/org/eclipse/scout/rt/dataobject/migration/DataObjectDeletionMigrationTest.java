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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.FloorFixtureDoStructureMigrationHandler_5;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.ScratchFixtureDeletionMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_4;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjectDeletionMigrationTest {

  private static final List<IBean<?>> TEST_BEANS = new ArrayList<>();

  private static DataObjectMigrationContext s_migrationContext;
  private static DataObjectMigrator s_migrator;

  @BeforeClass
  public static void beforeClass() {
    DataObjectMigrationTestHelper testHelper = BEANS.get(DataObjectMigrationTestHelper.class);
    TestDataObjectMigrationInventory inventory = new TestDataObjectMigrationInventory(
        testHelper.getFixtureNamespaces(),
        testHelper.getFixtureTypeVersions(),
        testHelper.getFixtureContextDataClasses(),
        Arrays.asList(new FloorFixtureDoStructureMigrationHandler_5(),
            new ScratchFixtureDeletionMigrationHandler()),
        Collections.emptyList());

    TEST_BEANS.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(TestDataObjectMigrationInventory.class, inventory).withReplace(true)));
    s_migrationContext = BEANS.get(DataObjectMigrationContext.class);
    s_migrator = BEANS.get(DataObjectMigrator.class);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(TEST_BEANS);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDeletionMigrationHandler() {
    IDoEntity doWithDeletedFloorFixture = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.FloorFixture")
        .put("_typeVersion", CharlieFixture_4.VERSION.unwrap())
        .put("scratch", BEANS.get(DoEntityBuilder.class)
            .put("_type", "bravoFixture.ScratchFixture")
            .put("_typeVersion", BravoFixture_1.VERSION.unwrap())
            .build())
        .build();

    s_migrator.applyStructureMigration(s_migrationContext, doWithDeletedFloorFixture, CharlieFixture_5.VERSION);
  }
}
