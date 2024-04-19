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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationInventory.FindNextMigrationHandlerVersionStatus;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.DuplicateIdFixtureDoValueMigrationHandler_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureRawOnlyStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureTypedOnlyStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseTypeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.OldHouseTypeFixtureStringIdTypeNameRenameMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureFamilyFriendlyMigrationHandlerInvalidTypeVersionToUpdate_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_4;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_5;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomSizeFixtureDoValueMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomTypeFixtureDoValueMigrationHandler_2;
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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link DataObjectMigrationInventory}.
 */
public class DataObjectMigrationInventoryTest {

  private static DataObjectMigrationInventory s_inventory;

  @BeforeClass
  public static void beforeClass() {
    s_inventory = new TestDataObjectMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace(), new DeltaFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(), // AlfaFixture_7 is explicitly not registered
            new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
            new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5(),
            new DeltaFixture_1(), new DeltaFixture_2()),
        Arrays.asList(
            HouseFixtureStructureMigrationTargetContextData.class,
            HouseFixtureRawOnlyStructureMigrationTargetContextData.class,
            HouseFixtureTypedOnlyStructureMigrationTargetContextData.class,
            CustomerFixtureTargetContextData.class,
            CharlieCustomerFixtureTargetContextData.class),
        Arrays.asList(
            new HouseFixtureDoStructureMigrationHandler_2(),
            new RoomFixtureDoStructureMigrationHandler_2(),
            new RoomFixtureDoStructureMigrationHandler_3(),
            new RoomFixtureDoStructureMigrationHandler_4(),
            new RoomFixtureDoStructureMigrationHandler_5(),
            new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3()),
        Arrays.asList(
            // registration order here is relevant for test method testValueMigrationHandlersOrdered
            new RoomSizeFixtureDoValueMigrationHandler_2(),
            new RoomTypeFixtureDoValueMigrationHandler_2(),
            new HouseTypeFixtureDoValueMigrationHandler_2(),
            new OldHouseTypeFixtureStringIdTypeNameRenameMigrationHandler_2()));
  }

  /**
   * Tests for {@link DataObjectMigrationInventory#validateStructureMigrationHandlerUniqueness(Map)} ()}.
   */
  @Test
  public void testValidateMigrationHandlerUniqueness() {
    TestDataObjectMigrationInventory inventory = new TestDataObjectMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(),
            new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
            new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5()),
        Collections.emptyList(),
        Arrays.asList(new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3()),
        Collections.emptyList());

    assertNotNull(inventory); // no validation error on creation of inventory

    assertThrows(PlatformException.class, () -> new TestDataObjectMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(),
            new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
            new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5()),
        Collections.emptyList(),
        Arrays.asList(
            new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3(),
            new PetFixtureFamilyFriendlyMigrationHandlerInvalidTypeVersionToUpdate_3()), // added this migration handler here -> validation error because multiple migration handlers per type version/type name
        Collections.emptyList()));
  }

  /**
   * Validates internal structure of inventory ({@link DataObjectMigrationInventory#m_orderedVersions}.
   */
  @Test
  public void testOrderedVersions() {
    assertEquals(
        Arrays.asList(
            AlfaFixture_1.VERSION, BravoFixture_1.VERSION, CharlieFixture_1.VERSION, DeltaFixture_1.VERSION,
            AlfaFixture_2.VERSION, BravoFixture_2.VERSION, CharlieFixture_2.VERSION, DeltaFixture_2.VERSION,
            AlfaFixture_3.VERSION, BravoFixture_3.VERSION, CharlieFixture_3.VERSION,
            CharlieFixture_4.VERSION,
            AlfaFixture_6.VERSION,
            CharlieFixture_5.VERSION // after charlieFixture-4, must not necessarily be after alfaFixture-6
        ),
        new ArrayList<>(s_inventory.m_orderedVersions));
  }

  /**
   * Validates internal structure of inventory ({@link DataObjectMigrationInventory#m_typeNameVersions}.
   */
  @Test
  public void testTypeNameVersions() {
    assertEquals(
        CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.BuildingFixture", Arrays.asList(CharlieFixture_2.VERSION)),
            new ImmutablePair<>("charlieFixture.RoomFixture", Arrays.asList(CharlieFixture_2.VERSION, CharlieFixture_3.VERSION, CharlieFixture_4.VERSION, CharlieFixture_5.VERSION)),
            new ImmutablePair<>("bravoFixture.PetFixture", Arrays.asList(BravoFixture_3.VERSION))),
        s_inventory.m_typeNameVersions);
  }

  @Test
  public void testTypeNameToCurrentTypeVersion() {
    assertEquals(CharlieFixture_3.VERSION, s_inventory.m_typeNameToCurrentTypeVersion.get("charlieFixture.HouseFixture"));
    assertEquals(CharlieFixture_5.VERSION, s_inventory.m_typeNameToCurrentTypeVersion.get("charlieFixture.RoomFixture"));
    assertEquals(CharlieFixture_2.VERSION, s_inventory.m_typeNameToCurrentTypeVersion.get("charlieFixture.PostalAddressFixture"));
  }

  @Test
  public void testGetDoMigrationContextValues() {
    Assert.assertThrows(AssertionException.class, () -> s_inventory.getStructureMigrationTargetContextDataClasses(null));

    assertEquals(0, s_inventory.getStructureMigrationTargetContextDataClasses(BEANS.get(DoEntityBuilder.class).put("_type", "unknown").build()).size());

    assertEquals(CollectionUtility.hashSet(HouseFixtureStructureMigrationTargetContextData.class, HouseFixtureRawOnlyStructureMigrationTargetContextData.class),
        s_inventory.getStructureMigrationTargetContextDataClasses(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.HouseFixture").build()));

    assertEquals(CollectionUtility.hashSet(HouseFixtureStructureMigrationTargetContextData.class, HouseFixtureTypedOnlyStructureMigrationTargetContextData.class),
        s_inventory.getStructureMigrationTargetContextDataClasses(BEANS.get(HouseFixtureDo.class)));

    // Subclasses data object, using origin instance (thus new instead of BEANS.get) [not a real case]
    assertEquals(CollectionUtility.hashSet(CustomerFixtureTargetContextData.class),
        s_inventory.getStructureMigrationTargetContextDataClasses(new CustomerFixtureDo()));

    assertEquals(CollectionUtility.hashSet(CustomerFixtureTargetContextData.class, CharlieCustomerFixtureTargetContextData.class),
        s_inventory.getStructureMigrationTargetContextDataClasses(BEANS.get(CharlieCustomerFixtureDo.class)));
  }

  @Test
  public void testIsUpToDateOrMigrationAvailable() {
    // Non-existing type names
    assertFalse(s_inventory.isUpToDateOrMigrationAvailable("lorem.Ipsum", null));
    assertFalse(s_inventory.isUpToDateOrMigrationAvailable("lorem.Dolor", CharlieFixture_1.VERSION));

    // Missing migration handler from charlieFixture-1 to -2 [lorem.Migrationless].
    assertFalse(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.PostalAddressFixture", CharlieFixture_1.VERSION));

    // Regular case [lorem.Example]
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", null));
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", CharlieFixture_1.VERSION));
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", CharlieFixture_2.VERSION));
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", CharlieFixture_3.VERSION));
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", CharlieFixture_4.VERSION));
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", CharlieFixture_5.VERSION)); // current type version

    // invalid, unknown type version
    assertFalse(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.RoomFixture", AlfaFixture_7.VERSION));

    //  [lorem.One/lorem.Two]
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.BuildingFixture", CharlieFixture_1.VERSION));

    // invalid, BuildingFixture not available for this type version (next version from full list is returned due to possible renamings)
    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.BuildingFixture", CharlieFixture_3.VERSION));

    assertTrue(s_inventory.isUpToDateOrMigrationAvailable("charlieFixture.HouseFixture", CharlieFixture_3.VERSION)); // current type version
  }

  @Test
  public void testFindNextMigrationHandlerVersion() {
    // Missing migration handler from charlieFixture-1 to -2 [lorem.Migrationless].
    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.NO_MIGRATION_HANDLERS, null),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.PostalAddressFixture", CharlieFixture_1.VERSION));

    // Regular case [lorem.Example]
    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.NO_TYPE_VERSION_YET, CharlieFixture_2.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", null));

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, CharlieFixture_2.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_1.VERSION));

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, CharlieFixture_3.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_2.VERSION));

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, CharlieFixture_4.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_3.VERSION));

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, CharlieFixture_5.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_4.VERSION));

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.UP_TO_DATE, null),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_5.VERSION)); // current type version

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.UNKNOWN_TYPE_VERSION, null),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", AlfaFixture_7.VERSION)); // invalid, unknown type version

    //  [lorem.One/lorem.Two]
    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, CharlieFixture_2.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.BuildingFixture", CharlieFixture_1.VERSION));

    // invalid, BuildingFixture not available for this type version (next version from full list is returned due to possible renamings)
    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, CharlieFixture_4.VERSION),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.BuildingFixture", CharlieFixture_3.VERSION));

    assertEquals(ImmutablePair.of(FindNextMigrationHandlerVersionStatus.UP_TO_DATE, null),
        s_inventory.findNextMigrationHandlerVersion("charlieFixture.HouseFixture", CharlieFixture_3.VERSION)); // current type version
  }

  @Test
  public void testGetVersions() {
    assertThrows(AssertionException.class, () -> s_inventory.getVersions(CollectionUtility.emptyHashMap(), AlfaFixture_7.VERSION)); // alfaFixture-7 is unknown

    assertEquals(CollectionUtility.emptyArrayList(), s_inventory.getVersions(CollectionUtility.emptyHashMap(), null));
    assertEquals(CollectionUtility.emptyArrayList(), s_inventory.getVersions(CollectionUtility.emptyHashMap(), CharlieFixture_2.VERSION));

    // Only versions with handlers are returned

    assertEquals(Arrays.asList(CharlieFixture_2.VERSION, BravoFixture_3.VERSION, CharlieFixture_3.VERSION, CharlieFixture_4.VERSION, CharlieFixture_5.VERSION),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.BuildingFixture", CharlieFixture_1.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_1.VERSION)),
            null));

    assertEquals(Arrays.asList(CharlieFixture_3.VERSION, CharlieFixture_4.VERSION, CharlieFixture_5.VERSION),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_2.VERSION)),
            null));

    assertEquals(Arrays.asList(CharlieFixture_4.VERSION, CharlieFixture_5.VERSION),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_3.VERSION)),
            null));

    assertEquals(Arrays.asList(CharlieFixture_5.VERSION),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_4.VERSION)),
            null));

    assertEquals(CollectionUtility.emptyArrayList(),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_5.VERSION)),
            null));

    // With limit of toVersion
    assertEquals(Arrays.asList(CharlieFixture_3.VERSION),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_2.VERSION)),
            CharlieFixture_3.VERSION));

    // With limit of toVersion (lower than first returned version CharlieFixture_3)
    assertEquals(Collections.emptyList(),
        s_inventory.getVersions(CollectionUtility.hashMap(
            new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
            new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_2.VERSION)),
            CharlieFixture_2.VERSION));
  }

  @Test
  public void testGetStructureMigrationHandlers() {
    assertThrows(AssertionException.class, () -> s_inventory.getStructureMigrationHandlers(null));
    assertThrows(AssertionException.class, () -> s_inventory.getStructureMigrationHandlers(AlfaFixture_7.VERSION)); // no registered

    // alfaFixture-1
    assertTrue(s_inventory.getStructureMigrationHandlers(AlfaFixture_1.VERSION).isEmpty()); // no handlers

    Map<String, IDoStructureMigrationHandler> migrationHandlers;

    // charlieFixture-2
    migrationHandlers = s_inventory.getStructureMigrationHandlers(CharlieFixture_2.VERSION);
    assertEquals(2, migrationHandlers.size());

    assertTrue(migrationHandlers.get("charlieFixture.BuildingFixture") instanceof HouseFixtureDoStructureMigrationHandler_2);
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_2);

    // bravoFixture-3
    migrationHandlers = s_inventory.getStructureMigrationHandlers(BravoFixture_3.VERSION);
    assertEquals(1, migrationHandlers.size());

    assertTrue(migrationHandlers.get("bravoFixture.PetFixture") instanceof PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3);

    // charlieFixture-3
    migrationHandlers = s_inventory.getStructureMigrationHandlers(CharlieFixture_3.VERSION);
    assertEquals(1, migrationHandlers.size());
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_3);

    // charlieFixture-4
    migrationHandlers = s_inventory.getStructureMigrationHandlers(CharlieFixture_4.VERSION);
    assertEquals(1, migrationHandlers.size());
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_4);

    // charlieFixture-5
    migrationHandlers = s_inventory.getStructureMigrationHandlers(CharlieFixture_5.VERSION);
    assertEquals(1, migrationHandlers.size());
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_5);
  }

  /**
   * Tests sort order of value migration handlers.
   */
  @Test
  public void testValueMigrationHandlersOrdered() {
    // all value migration handlers, ordered (see initialization of s_inventory)
    List<IDoValueMigrationHandler<?>> valueMigrationHandlers = s_inventory.getValueMigrationHandlers();
    assertEquals(4, valueMigrationHandlers.size());

    // untyped value migration handler before regular migrations (because of primary sort order)
    assertEquals(OldHouseTypeFixtureStringIdTypeNameRenameMigrationHandler_2.ID, valueMigrationHandlers.get(0).id());

    // regular migration handlers
    assertEquals(RoomTypeFixtureDoValueMigrationHandler_2.ID, valueMigrationHandlers.get(1).id());
    // HouseTypeFixtureDoValueMigrationHandler_2 after RoomTypeFixtureDoValueMigrationHandler_2 because of registration order (same type version)
    assertEquals(HouseTypeFixtureDoValueMigrationHandler_2.ID, valueMigrationHandlers.get(2).id());
    // RoomSizeFixtureDoValueMigrationHandler_2 after HouseTypeFixtureDoValueMigrationHandler_2 because of type version (registration order is overridden)
    assertEquals(RoomSizeFixtureDoValueMigrationHandler_2.ID, valueMigrationHandlers.get(3).id());
  }

  /**
   * Register two value migration handlers with identical ID -> exception expected
   */
  @Test
  public void testDuplicateValueMigrationIds() {
    PlatformException exception = assertThrows(PlatformException.class, () -> new TestDataObjectMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace(), new DeltaFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(),
            new BravoFixture_1(), new BravoFixture_2(),
            new CharlieFixture_1(), new CharlieFixture_2(),
            new DeltaFixture_1(), new DeltaFixture_2()),
        Collections.emptyList(),
        Collections.emptyList(),
        Arrays.asList(
            new RoomSizeFixtureDoValueMigrationHandler_2(),
            new DuplicateIdFixtureDoValueMigrationHandler_1())));

    assertTrue(exception.getMessage().contains(DuplicateIdFixtureDoValueMigrationHandler_1.ID.unwrapAsString()));
  }
}
