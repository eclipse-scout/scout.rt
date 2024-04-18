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

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDataObjectVisitorExtension;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ILenientDataObjectMapper;
import org.eclipse.scout.rt.dataobject.id.UnknownId;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrator.DataObjectMigratorResult;
import org.eclipse.scout.rt.dataobject.migration.fixture.EntityWithIdFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerGenderFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerGenderFixtureEnum;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoStructureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoValueMigrationHandler_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypeFixtureStringId;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypesFixture;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.OldHouseTypeFixtureStringIdTypeNameRenameMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureAlwaysAcceptDoValueMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureDoValueMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomSizeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeCompositeFixture;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeCompositeFixtureDataObjectVisitorExtension;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeCompositeFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeFixtureStringId;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypesCollectionFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypesFixture;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link DataObjectMigrator}, with focus on data object value migrations ({@link IDoValueMigrationHandler}).
 */
public class DataObjectMigratorValueMigrationTest {

  protected static final RoomTypeFixtureStringId ROOM_TYPE_STANDARD_ROOM = RoomTypeFixtureStringId.of("standard-room"); // 'standard-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2
  protected static final RoomTypeFixtureStringId ROOM_TYPE_SMALL_ROOM = RoomTypeFixtureStringId.of("small-room"); // 'small-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2

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
            new CustomerGenderFixtureDoValueMigrationHandler_2(),
            new HouseTypeFixtureDoValueMigrationHandler_2(),
            new HouseFixtureDoValueMigrationHandler_1(),
            new OldHouseTypeFixtureStringIdTypeNameRenameMigrationHandler()));

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
   * Tests the behavior of {@link DataObjectMigrator} regarding invalid structures before executing value migration
   * {@link CustomerGenderFixtureDoValueMigrationHandler_2}.
   */
  @Test
  public void testValueMigrationWithInvalidStructure() {
    // raw DoEntity data object
    IDoEntity original = BEANS.get(DoEntityBuilder.class)
        .put("_type", "alfaFixture.CustomerFixture")
        .put("_typeVersion", AlfaFixture_3.VERSION.unwrap())
        .put("firstName", "John")
        .put("lastName", "Doe")
        .put("gender", "m")
        .build();

    DataObjectMigrationContext localeMigrationContext = BEANS.get(DataObjectMigrationContext.class)
        .putGlobal(BEANS.get(DoValueMigrationIdsContextData.class)
            .withAppliedValueMigrationIds(CollectionUtility.hashSet(CustomerGenderFixtureDoValueMigrationHandler_2.ID)));

    // using a migration context where CustomerGenderFixtureDoValueMigrationHandler_2 was already executed, so this method call will fail because gender "m" cannot be read by regular data object mapper
    assertThrows(PlatformException.class, () -> s_migrator.migrateDataObject(localeMigrationContext, original, CustomerFixtureDo.class));

    // in default migration context CustomerGenderFixtureDoValueMigrationHandler_2 will be executed resulting in a valid final data object (gender "male")
    DataObjectMigratorResult<CustomerFixtureDo> result = s_migrator.migrateDataObject(s_migrationContext, original, CustomerFixtureDo.class);

    assertTrue(result.isChanged());

    CustomerFixtureDo expected = BEANS.get(CustomerFixtureDo.class)
        .withFirstName("John")
        .withLastName("Doe")
        .withGender(CustomerGenderFixtureEnum.MALE);

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

  /**
   * Similar as {@link #testStructureAndValueMigrations()} but including an intermediate migrations.
   */
  @Test
  public void testStructureAndValueMigrationsWithIntermediateMigrations() {
    // raw DoEntity data object
    IDoEntity original = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.HouseFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap()) // will be updated by HouseFixtureDoStructureMigrationHandler_3
        .build();

    DataObjectMigrationContext ctx = s_migrationContext.copy();

    // For all intermediate migrations, store typed data object for equality check afterwards and modify a copy

    DoEntityHolder<IDoEntity> intermediate1ResultHolder = new DoEntityHolder<>();
    ctx.getIntermediateMigrations().add((innerCtx, typedDataObject) -> {
      intermediate1ResultHolder.setValue((IDoEntity) typedDataObject);
      IDoEntity clonedDoEntity = BEANS.get(DataObjectHelper.class).clone((IDoEntity) typedDataObject);
      clonedDoEntity.put("intermediate", "ctx-1");
      return DataObjectMigratorResult.of(clonedDoEntity, true);
    });

    DoEntityHolder<IDoEntity> intermediate2ResultHolder = new DoEntityHolder<>();
    ctx.getIntermediateMigrations().add((innerCtx, typedDataObject) -> {
      intermediate2ResultHolder.setValue((IDoEntity) typedDataObject);
      IDoEntity clonedDoEntity = BEANS.get(DataObjectHelper.class).clone((IDoEntity) typedDataObject);
      clonedDoEntity.put("intermediate", "ctx-2");
      return DataObjectMigratorResult.of(clonedDoEntity, true);
    });

    DoEntityHolder<IDoEntity> localIntermediate1ResultHolder = new DoEntityHolder<>();
    IDataObjectIntermediateMigration<IDoEntity> localIntermediateMigration1 = (innerCtx, typedDataObject) -> {
      localIntermediate1ResultHolder.setValue(typedDataObject);
      IDoEntity clonedDoEntity = BEANS.get(DataObjectHelper.class).clone(typedDataObject);
      clonedDoEntity.put("intermediate", "local-1");
      return DataObjectMigratorResult.of(clonedDoEntity, true);
    };

    DoEntityHolder<IDoEntity> localIntermediate2ResultHolder = new DoEntityHolder<>();
    IDataObjectIntermediateMigration<IDoEntity> localIntermediateMigration2 = (innerCtx, typedDataObject) -> {
      localIntermediate2ResultHolder.setValue(typedDataObject);
      IDoEntity clonedDoEntity = BEANS.get(DataObjectHelper.class).clone(typedDataObject);
      clonedDoEntity.put("intermediate", "local-2");
      return DataObjectMigratorResult.of(clonedDoEntity, true);
    };

    DataObjectMigratorResult<IDoEntity> result = s_migrator.migrateDataObject(ctx, original, IDoEntity.class, Collections.emptyList(), CollectionUtility.arrayList(localIntermediateMigration1, localIntermediateMigration2));
    assertTrue(result.isChanged());

    // same for all checks
    RoomFixtureDo expectedRoom1 = BEANS.get(RoomFixtureDo.class)
        .withName("example room 1")
        .withRoomType(RoomTypesFixture.ROOM);

    // Verify results of intermediate migration
    RoomFixtureDo room2 = BEANS.get(RoomFixtureDo.class)
        .withName("example room 2")
        .withRoomType(RoomTypeFixtureStringId.of("standard-room")); // not changed yet by RoomTypeFixtureDoValueMigrationHandler_2 (only structure migration handlers were applied)
    HouseFixtureDo expected = BEANS.get(HouseFixtureDo.class)
        .withRooms(expectedRoom1, room2);

    assertEqualsWithComparisonFailure(expected, intermediate1ResultHolder.getValue());
    expected.put("intermediate", "ctx-1");
    assertEqualsWithComparisonFailure(expected, intermediate2ResultHolder.getValue());
    expected.put("intermediate", "ctx-2");
    assertEqualsWithComparisonFailure(expected, localIntermediate1ResultHolder.getValue());
    expected.put("intermediate", "local-1");
    assertEqualsWithComparisonFailure(expected, localIntermediate2ResultHolder.getValue());

    // Verify final result
    RoomFixtureDo expectedRoom2 = BEANS.get(RoomFixtureDo.class)
        .withName("example room 2")
        .withRoomType(RoomTypesFixture.ROOM); // changed by RoomTypeFixtureDoValueMigrationHandler_2
    expected = BEANS.get(HouseFixtureDo.class)
        .withRooms(expectedRoom1, expectedRoom2);

    expected.put("intermediate", "local-2");

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests the changed flag behavior of intermediate migrations.
   */
  @Test
  public void testIntermediateMigrationChangedFlag() {
    HouseFixtureDo original = BEANS.get(HouseFixtureDo.class);

    IDataObjectIntermediateMigration<IDoEntity> localIntermediateMigrationNoChange = (innerCtx, typedDataObject) -> {
      typedDataObject.put("normalized", true); // a non-relevant change
      return DataObjectMigratorResult.of(typedDataObject, false);
    };
    IDataObjectIntermediateMigration<IDoEntity> localIntermediateMigrationChange = (innerCtx, typedDataObject) -> {
      typedDataObject.put("intermediate", "changed");
      return DataObjectMigratorResult.of(typedDataObject, true);
    };

    // no change
    DataObjectMigratorResult<IDoEntity> result = s_migrator.migrateDataObject(s_migrationContext, original, IDoEntity.class, Collections.emptyList(), Collections.singletonList(localIntermediateMigrationNoChange));
    assertFalse(result.isChanged());
    HouseFixtureDo expected = BEANS.get(HouseFixtureDo.class);
    expected.put("normalized", true);
    assertEqualsWithComparisonFailure(expected, result.getDataObject());

    // change
    result = s_migrator.migrateDataObject(s_migrationContext, original, IDoEntity.class, Collections.emptyList(), Collections.singletonList(localIntermediateMigrationChange));
    assertTrue(result.isChanged());
    expected = BEANS.get(HouseFixtureDo.class);
    expected.put("intermediate", "changed");
    assertEqualsWithComparisonFailure(expected, result.getDataObject());

    // no change and change
    result = s_migrator.migrateDataObject(s_migrationContext, original, IDoEntity.class, Collections.emptyList(), CollectionUtility.arrayList(localIntermediateMigrationNoChange, localIntermediateMigrationChange));
    assertTrue(result.isChanged());
    expected = BEANS.get(HouseFixtureDo.class);
    expected.put("normalized", true);
    expected.put("intermediate", "changed");
    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests value migration for a DoList with duplicate values.
   */
  @Test
  public void testListValueMigration() {
    RoomTypesCollectionFixtureDo original = BEANS.get(RoomTypesCollectionFixtureDo.class)
        .withRoomTypesList(
            RoomTypesFixture.ROOM,
            RoomTypesFixture.LIVING_ROOM,
            ROOM_TYPE_STANDARD_ROOM, // 'standard-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2
            ROOM_TYPE_SMALL_ROOM); // 'small-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2

    DataObjectMigratorResult<RoomTypesCollectionFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    RoomTypesCollectionFixtureDo expected = BEANS.get(RoomTypesCollectionFixtureDo.class)
        .withRoomTypesList(
            RoomTypesFixture.ROOM,
            RoomTypesFixture.LIVING_ROOM,
            // Duplicate values in list are expected. Might be an inconsistent state depending on business logic, though.
            RoomTypesFixture.ROOM,
            RoomTypesFixture.ROOM);

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests value migration for a DoSet with duplicate values.
   */
  @Test
  public void testSetValueMigration() {
    RoomTypesCollectionFixtureDo original = BEANS.get(RoomTypesCollectionFixtureDo.class)
        .withRoomTypesSet(
            RoomTypesFixture.ROOM,
            RoomTypesFixture.LIVING_ROOM,
            ROOM_TYPE_STANDARD_ROOM, // 'standard-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2
            ROOM_TYPE_SMALL_ROOM); // 'small-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2

    DataObjectMigratorResult<RoomTypesCollectionFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    RoomTypesCollectionFixtureDo expected = BEANS.get(RoomTypesCollectionFixtureDo.class)
        .withRoomTypesSet(
            RoomTypesFixture.ROOM, // duplicate values in set have been removed
            RoomTypesFixture.LIVING_ROOM);

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Tests value migration for a map with duplicate keys.
   */
  @Test
  public void testMapValueMigration() {
    RoomFixtureDo room = BEANS.get(RoomFixtureDo.class)
        .withName("room")
        .withRoomType(RoomTypesFixture.ROOM);
    RoomFixtureDo livingRoom = BEANS.get(RoomFixtureDo.class)
        .withName("living room")
        .withRoomType(RoomTypesFixture.LIVING_ROOM);
    RoomFixtureDo standardRoom = BEANS.get(RoomFixtureDo.class)
        .withName("standard room")
        .withRoomType(ROOM_TYPE_STANDARD_ROOM);
    RoomFixtureDo smallRoom = BEANS.get(RoomFixtureDo.class)
        .withName("small room")
        .withRoomType(ROOM_TYPE_SMALL_ROOM);

    RoomTypesCollectionFixtureDo original = BEANS.get(RoomTypesCollectionFixtureDo.class)
        .withRoomTypesMap(CollectionUtility.hashMap(
            ImmutablePair.of(RoomTypesFixture.ROOM, room),
            ImmutablePair.of(RoomTypesFixture.LIVING_ROOM, livingRoom),
            ImmutablePair.of(ROOM_TYPE_STANDARD_ROOM, standardRoom), // 'standard-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2
            ImmutablePair.of(ROOM_TYPE_SMALL_ROOM, smallRoom))); // 'small-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2

    DataObjectMigratorResult<RoomTypesCollectionFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    Map<RoomTypeFixtureStringId, RoomFixtureDo> resultMap = result.getDataObject().getRoomTypesMap();
    assertEquals(2, resultMap.size());
    assertEquals("living room", resultMap.get(RoomTypesFixture.LIVING_ROOM).getName());

    // Merged keys result in a single, randomly selected value, depending on internal order of the original map and implementation details of AbstractReplacingDataObjectVisitor.
    String roomName = resultMap.get(RoomTypesFixture.ROOM).getName();
    assertTrue("room".equals(roomName) || "standard room".equals(roomName) || "small room".equals(roomName));
  }

  /**
   * Tests value migration for objects handled by an {@link IDataObjectVisitorExtension}.
   *
   * @see RoomTypeCompositeFixture
   * @see RoomTypeCompositeFixtureDataObjectVisitorExtension
   */
  @Test
  public void testValueMigrationWithDataObjectVisitorExtension() {
    RoomTypeCompositeFixtureDo original = BEANS.get(RoomTypeCompositeFixtureDo.class)
        .withRoomTypeComposite(new RoomTypeCompositeFixture(
            ROOM_TYPE_STANDARD_ROOM, // 'standard-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2
            ROOM_TYPE_SMALL_ROOM)); // 'small-room' will be migrated to 'room' by RoomTypeFixtureDoValueMigrationHandler_2

    DataObjectMigratorResult<RoomTypeCompositeFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, original);

    assertTrue(result.isChanged());

    RoomTypeCompositeFixtureDo expected = BEANS.get(RoomTypeCompositeFixtureDo.class)
        .withRoomTypeComposite(new RoomTypeCompositeFixture(
            RoomTypesFixture.ROOM,
            RoomTypesFixture.ROOM));

    assertEqualsWithComparisonFailure(expected, result.getDataObject());
  }

  /**
   * Testcase for IdTypeName renaming from 'charlieFixture.OldHouseTypeFixtureStringId' to
   * 'charlieFixture.HouseTypeFixtureStringId'
   * <p>
   * Setup EntityWithIdFixtureDo using raw data object mapper.
   */
  @Test
  public void testHouseTypeFixtureStringIdTypeNameRenameMigration_01() {
    IDoEntity entity = (IDoEntity) BEANS.get(IDataObjectMapper.class).readValueRaw("{\"_type\" : \"EntityWithIdFixture\", \"id\" : \"charlieFixture.OldHouseTypeFixtureStringId:foo\"}");
    DataObjectMigratorResult<EntityWithIdFixtureDo> result = s_migrator.migrateDataObject(s_migrationContext, entity, EntityWithIdFixtureDo.class);
    assertTrue(result.isChanged());
    assertTrue(result.getDataObject().getId() instanceof HouseTypeFixtureStringId);
    assertEquals("foo", result.getDataObject().getId().unwrap());
  }

  /**
   * Testcase for IdTypeName renaming from 'charlieFixture.OldHouseTypeFixtureStringId' to
   * 'charlieFixture.HouseTypeFixtureStringId'
   * <p>
   * Setup EntityWithIdFixtureDo using lenient data object mapper.
   */
  @Test
  public void testHouseTypeFixtureStringIdTypeNameRenameMigration_02() {
    EntityWithIdFixtureDo entity = BEANS.get(ILenientDataObjectMapper.class).readValue("{\"_type\" : \"EntityWithIdFixture\", \"id\" : \"charlieFixture.OldHouseTypeFixtureStringId:foo\"}", EntityWithIdFixtureDo.class);
    DataObjectMigratorResult<EntityWithIdFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, entity);
    assertTrue(result.isChanged());
    assertTrue(result.getDataObject().getId() instanceof HouseTypeFixtureStringId);
    assertEquals("foo", result.getDataObject().getId().unwrap());
  }

  /**
   * Testcase for IdTypeName renaming from 'charlieFixture.OldHouseTypeFixtureStringId' to
   * 'charlieFixture.HouseTypeFixtureStringId'
   * <p>
   * Setup EntityWithIdFixtureDo using programmatic instance of UnknownId.
   */
  @Test
  public void testHouseTypeFixtureStringIdTypeNameRenameMigration_03() {
    EntityWithIdFixtureDo entity = BEANS.get(EntityWithIdFixtureDo.class);
    //noinspection deprecation
    entity.put("id", UnknownId.of("charlieFixture.OldHouseTypeFixtureStringId", "foo"));
    DataObjectMigratorResult<EntityWithIdFixtureDo> result = s_migrator.applyValueMigration(s_migrationContext, entity);
    assertTrue(result.isChanged());
    assertTrue(result.getDataObject().getId() instanceof HouseTypeFixtureStringId);
    assertEquals("foo", result.getDataObject().getId().unwrap());
  }
}
