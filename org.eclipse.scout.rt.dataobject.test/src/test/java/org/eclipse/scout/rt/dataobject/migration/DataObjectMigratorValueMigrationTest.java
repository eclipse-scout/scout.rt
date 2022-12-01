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
package org.eclipse.scout.rt.dataobject.migration;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrator.DataObjectMigratorResult;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoStructureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoValueMigrationHandler_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypeFixtureStringId;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypesFixture;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureAlwaysAcceptDoValueMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureDoValueMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomSizeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypesFixture;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link DataObjectMigrator}, with focus on data object value migrations ({@link IDoValueMigrationHandler}).
 */
public class DataObjectMigratorValueMigrationTest {

  private static final List<IBean<?>> TEST_BEANS = new ArrayList<>();

  private static DataObjectMigrationContext s_migrationContext;
  private static DataObjectMigrator s_migrator;

  @BeforeClass
  public static void beforeClass() {
    DataObjectMigrationTestHelper testHelper = BEANS.get(DataObjectMigrationTestHelper.class);
    TestDataObjectMigrationInventory inventory = new TestDataObjectMigrationInventory(
        testHelper.getFixtureNamespaces(),
        testHelper.getFixtureTypeVersions(),
        Collections.emptyList(),
        Arrays.asList(new HouseFixtureDoStructureMigrationHandler_3()),
        Arrays.asList(
            new PetFixtureDoValueMigrationHandler_3(),
            new PetFixtureAlwaysAcceptDoValueMigrationHandler_3(),
            new RoomSizeFixtureDoValueMigrationHandler_2(),
            new RoomTypeFixtureDoValueMigrationHandler_2(),
            new HouseTypeFixtureDoValueMigrationHandler_2(),
            new HouseFixtureDoValueMigrationHandler_1()));

    TEST_BEANS.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(TestDataObjectMigrationInventory.class, inventory).withReplace(true)));

    s_migrationContext = BEANS.get(DataObjectMigrationContext.class)
        .putGlobal(BEANS.get(DoValueMigrationIdsContextData.class)
            // by default, all value migrations are executed, except RoomSizeFixtureDoValueMigrationHandler_2 (PetFixtureAlwaysAcceptDoValueMigrationHandler_3 always accepts)
            .withAppliedValueMigrationIds(CollectionUtility.hashSet(PetFixtureAlwaysAcceptDoValueMigrationHandler_3.ID, RoomSizeFixtureDoValueMigrationHandler_2.ID)));
    s_migrator = BEANS.get(DataObjectMigrator.class);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(TEST_BEANS);
  }

  /**
   * Tests applied value migrations provided in context.
   */
  @Test
  public void testAppliedValueMigrationHandlers() {
    RoomFixtureDo original = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withAreaInSquareMeter(10);

    // no value migrations will match the provided data object
    // RoomSizeFixtureDoValueMigrationHandler_2 is ignored by default in s_migrationContext
    DataObjectMigratorResult<RoomFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertFalse(result.isChanged()); // no changes (structure nor values changed)

    RoomFixtureDo expected = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withAreaInSquareMeter(10);

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests non-idempotent value migration with {@link RoomSizeFixtureDoValueMigrationHandler_2}.<br>
   * Migration handler matches and replaces whole data object (RoomFixtureDo).
   */
  @Test
  public void testNonIdempotentValueMigration() {
    RoomFixtureDo original = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withAreaInSquareMeter(10);

    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class)
        .putGlobal(BEANS.get(DoValueMigrationIdsContextData.class)
            .withAppliedValueMigrationIds(Collections.emptySet())); // run all value migrations

    // migration run 1
    DataObjectMigratorResult<RoomFixtureDo> result = s_migrator.applyValueMigration(ctx, original);

    assertTrue(result.isChanged());

    RoomFixtureDo expected = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withAreaInSquareMeter(20); // increased by 10 through RoomSizeFixtureDoValueMigrationHandler_2

    assertEqualsWithComparisonFailure(expected, result.getDataObject());

    // migration run 2: data object is changed again, due to non-idempotent value migration
    result = s_migrator.applyValueMigration(ctx, result.getDataObject());

    assertTrue(result.isChanged());

    expected = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withAreaInSquareMeter(30); // increased by 10 through RoomSizeFixtureDoValueMigrationHandler_2

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests StringId value rename with {@link HouseTypeFixtureDoValueMigrationHandler_2}.<br>
   * Migration handler matches specific ID type.
   */
  @Test
  public void testRenameStringIdValue() {
    HouseFixtureDo original = BEANS.get(HouseFixtureDo.class)
        .withName("example")
        .withHouseType(HouseTypeFixtureStringId.of("house")); // will be migrated by HouseTypeFixtureDoValueMigrationHandler_2

    // migration run 1
    DataObjectMigratorResult<HouseFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    HouseFixtureDo expected = BEANS.get(HouseFixtureDo.class)
        .withName("example")
        .withHouseType(HouseTypesFixture.DETACHED_HOUSE);

    assertEqualsWithComparisonFailure(expected, result.getDataObject());

    // migration run 2: re-apply the same (idempotent) value migration, no changes should occur
    result = s_migrator.applyValueMigration(s_migrationContext, result.getDataObject());

    assertFalse(result.isChanged());

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests recursive value migrations with {@link HouseFixtureDoValueMigrationHandler_1} and
   * {@link RoomTypeFixtureDoValueMigrationHandler_2}.<br>
   * Migration handler matches and replaces whole data object (RoomFixtureDo).
   */
  @Test
  public void testReplaceDataObject() {
    HouseFixtureDo original = BEANS.get(HouseFixtureDo.class)
        .withName("tiny house")
        .withRooms(BEANS.get(RoomFixtureDo.class)
            .withName("tiny room"));

    DataObjectMigratorResult<HouseFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    HouseFixtureDo expected = BEANS.get(HouseFixtureDo.class)
        .withName("tiny house")
        .withRooms(BEANS.get(RoomFixtureDo.class)
            .withName("migrated tiny room")
            .withRoomType(RoomTypesFixture.ROOM));

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Value migration handler for {@link PetFixtureDo} matches a generic DoValue<IDoEntity> node.
   */
  @Test
  public void testAssignableValueClass() {
    RoomFixtureDo original = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withCustomData(BEANS.get(PetFixtureDo.class)
            .withName("Name: Fluffy")); // Will be migrated by PetFixtureDoValueMigrationHandler_3

    DataObjectMigratorResult<RoomFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    RoomFixtureDo expected = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withCustomData(BEANS.get(PetFixtureDo.class)
            .withName("Fluffy")); // migrated from "Name: Fluffy" to "Fluffy" by PetFixtureDoValueMigrationHandler_3

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Value migration handler for {@link PetFixtureDo} that always accepts (thus ignoring that already applied).
   */
  @Test
  public void testAlwaysAcceptMigrationHandler() {
    RoomFixtureDo original = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withCustomData(BEANS.get(PetFixtureDo.class)
            .withName("Nickname: Fluffy")); // Will be migrated by PetFixtureAlwaysAcceptDoValueMigrationHandler_3

    // check that part of the set of applied value migration IDs
    assertTrue(s_migrationContext.getGlobal(DoValueMigrationIdsContextData.class).getAppliedValueMigrationIds().contains(PetFixtureAlwaysAcceptDoValueMigrationHandler_3.ID));
    DataObjectMigratorResult<RoomFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    // executed even if already applied
    assertTrue(result.isChanged());

    RoomFixtureDo expected = BEANS.get(RoomFixtureDo.class)
        .withName("example")
        .withCustomData(BEANS.get(PetFixtureDo.class)
            .withName("Fluffy")); // migrated from "Nickname: Fluffy" to "Fluffy" by PetFixtureAlwaysAcceptDoValueMigrationHandler_3

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests combined data object structure and value migration with {@link HouseFixtureDoStructureMigrationHandler_3} and
   * {@link RoomTypeFixtureDoValueMigrationHandler_2}.
   */
  @Test
  public void testStructureAndValueMigrations() {
    // raw DoEntity data object
    IDoEntity original = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.HouseFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap()) // will be updated by HouseFixtureDoStructureMigrationHandler_3
        .build();

    DataObjectMigratorResult<IDoEntity> result = s_migrator.migrateDataObject(s_migrationContext, original, IDoEntity.class);

    assertTrue(result.isChanged());

    RoomFixtureDo room1 = BEANS.get(RoomFixtureDo.class)
        .withName("example room 1")
        .withRoomType(RoomTypesFixture.ROOM);
    RoomFixtureDo room2 = BEANS.get(RoomFixtureDo.class)
        .withName("example room 2")
        .withRoomType(RoomTypesFixture.ROOM); // changed by RoomTypeFixtureDoValueMigrationHandler_2
    HouseFixtureDo expected = BEANS.get(HouseFixtureDo.class)
        .withRooms(room1, room2);

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }
}
