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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureRawOnlyDoStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureTypedOnlyDoStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureFamilyFriendlyMigrationHandlerInvalidTypeVersionToUpdate_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_4;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_5;
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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link DoStructureMigrationInventory}.
 */
public class DoStructureMigrationInventoryTest {

  private static DoStructureMigrationInventory s_inventory;

  @BeforeClass
  public static void beforeClass() {
    s_inventory = new TestDoStructureMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(), // AlfaFixture_7 is explicitly not registered
            new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
            new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5()),
        Arrays.asList(
            HouseFixtureStructureMigrationTargetContextData.class,
            HouseFixtureRawOnlyDoStructureMigrationTargetContextData.class,
            HouseFixtureTypedOnlyDoStructureMigrationTargetContextData.class,
            CustomerFixtureTargetContextData.class,
            CharlieCustomerFixtureTargetContextData.class),
        new HouseFixtureDoStructureMigrationHandler_2(),
        new RoomFixtureDoStructureMigrationHandler_2(),
        new RoomFixtureDoStructureMigrationHandler_3(),
        new RoomFixtureDoStructureMigrationHandler_4(),
        new RoomFixtureDoStructureMigrationHandler_5(),
        new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3());
  }

  /**
   * Tests for {@link DoStructureMigrationInventory#validateMigrationHandlerUniqueness(Map)} ()}.
   */
  @Test
  public void testValidateMigrationHandlerUniqueness() {
    TestDoStructureMigrationInventory inventory = new TestDoStructureMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(),
            new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
            new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5()),
        Collections.emptyList(),
        new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3());

    assertNotNull(inventory); // no validation error on creation of inventory

    assertThrows(PlatformException.class, () -> new TestDoStructureMigrationInventory(
        Arrays.asList(new AlfaFixtureNamespace(), new BravoFixtureNamespace(), new CharlieFixtureNamespace()),
        Arrays.asList(
            new AlfaFixture_1(), new AlfaFixture_2(), new AlfaFixture_3(), new AlfaFixture_6(),
            new BravoFixture_1(), new BravoFixture_2(), new BravoFixture_3(),
            new CharlieFixture_1(), new CharlieFixture_2(), new CharlieFixture_3(), new CharlieFixture_4(), new CharlieFixture_5()),
        Collections.emptyList(),
        new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3(),
        new PetFixtureFamilyFriendlyMigrationHandlerInvalidTypeVersionToUpdate_3())); // added this migration handler here -> validation error because multiple migration handlers per type version/type name
  }

  /**
   * Validates internal structure of inventory ({@link DoStructureMigrationInventory#m_orderedVersions}.
   */
  @Test
  public void testOrdered() {
    assertEquals(
        Arrays.asList(
            AlfaFixture_1.VERSION, BravoFixture_1.VERSION, CharlieFixture_1.VERSION,
            AlfaFixture_2.VERSION, BravoFixture_2.VERSION, CharlieFixture_2.VERSION,
            AlfaFixture_3.VERSION, BravoFixture_3.VERSION, CharlieFixture_3.VERSION,
            CharlieFixture_4.VERSION,
            AlfaFixture_6.VERSION,
            CharlieFixture_5.VERSION // after charlieFixture-4, must not necessarily be after alfaFixture-6
        ),
        new ArrayList<>(s_inventory.m_orderedVersions));
  }

  /**
   * Validates internal structure of inventory ({@link DoStructureMigrationInventory#m_typeNameVersions}.
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
    assertEquals(CharlieFixture_2.VERSION, s_inventory.m_typeNameToCurrentTypeVersion.get("charlieFixture.HouseFixture"));
    assertEquals(CharlieFixture_5.VERSION, s_inventory.m_typeNameToCurrentTypeVersion.get("charlieFixture.RoomFixture"));
    assertEquals(CharlieFixture_2.VERSION, s_inventory.m_typeNameToCurrentTypeVersion.get("charlieFixture.PostalAddressFixture"));
  }

  @Test
  public void testGetDoMigrationContextValues() {
    Assert.assertThrows(AssertionException.class, () -> s_inventory.getDoMigrationContextValues(null));

    assertEquals(0, s_inventory.getDoMigrationContextValues(BEANS.get(DoEntityBuilder.class).put("_type", "unknown").build()).size());

    Set<Class<? extends IDoStructureMigrationTargetContextData>> contextDataSet;

    assertEquals(CollectionUtility.hashSet(HouseFixtureStructureMigrationTargetContextData.class, HouseFixtureRawOnlyDoStructureMigrationTargetContextData.class),
        s_inventory.getDoMigrationContextValues(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.HouseFixture").build()));

    assertEquals(CollectionUtility.hashSet(HouseFixtureStructureMigrationTargetContextData.class, HouseFixtureTypedOnlyDoStructureMigrationTargetContextData.class),
        s_inventory.getDoMigrationContextValues(BEANS.get(HouseFixtureDo.class)));

    // Subclasses data object, using origin instance (thus new instead of BEANS.get) [not a real case]
    assertEquals(CollectionUtility.hashSet(CustomerFixtureTargetContextData.class),
        s_inventory.getDoMigrationContextValues(new CustomerFixtureDo()));

    assertEquals(CollectionUtility.hashSet(CustomerFixtureTargetContextData.class, CharlieCustomerFixtureTargetContextData.class),
        s_inventory.getDoMigrationContextValues(BEANS.get(CharlieCustomerFixtureDo.class)));
  }

  @Test
  public void testFindNextMigrationHandlerVersion() {
    // Missing migration handler from charlieFixture-1 to -2 [lorem.Migrationless].
    assertNull(s_inventory.findNextMigrationHandlerVersion("charlieFixture.PostalAddressFixture", CharlieFixture_1.VERSION));

    // Regular case [lorem.Example]
    assertEquals(CharlieFixture_2.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", null));
    assertEquals(CharlieFixture_2.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_1.VERSION));
    assertEquals(CharlieFixture_3.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_2.VERSION));
    assertEquals(CharlieFixture_4.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_3.VERSION));
    assertEquals(CharlieFixture_5.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_4.VERSION));
    assertNull(s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", CharlieFixture_5.VERSION)); // current type version
    assertNull(s_inventory.findNextMigrationHandlerVersion("charlieFixture.RoomFixture", AlfaFixture_7.VERSION)); // invalid, unknown type version

    //  [lorem.One/lorem.Two]
    assertEquals(CharlieFixture_2.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.BuildingFixture", CharlieFixture_1.VERSION));
    // invalid, BuildingFixture not available for this type version (next version from full list is returned due to possible renamings)
    assertEquals(CharlieFixture_4.VERSION, s_inventory.findNextMigrationHandlerVersion("charlieFixture.BuildingFixture", CharlieFixture_3.VERSION));
    assertNull(s_inventory.findNextMigrationHandlerVersion("charlieFixture.HouseFixture", CharlieFixture_2.VERSION)); // current type version
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
  public void testGetMigrationHandlers() {
    assertThrows(AssertionException.class, () -> s_inventory.getMigrationHandlers(null));
    assertThrows(AssertionException.class, () -> s_inventory.getMigrationHandlers(AlfaFixture_7.VERSION)); // no registered

    // alfaFixture-1
    assertTrue(s_inventory.getMigrationHandlers(AlfaFixture_1.VERSION).isEmpty()); // no handlers

    Map<String, IDoStructureMigrationHandler> migrationHandlers;

    // charlieFixture-2
    migrationHandlers = s_inventory.getMigrationHandlers(CharlieFixture_2.VERSION);
    assertEquals(2, migrationHandlers.size());

    assertTrue(migrationHandlers.get("charlieFixture.BuildingFixture") instanceof HouseFixtureDoStructureMigrationHandler_2);
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_2);

    // bravoFixture-3
    migrationHandlers = s_inventory.getMigrationHandlers(BravoFixture_3.VERSION);
    assertEquals(1, migrationHandlers.size());

    assertTrue(migrationHandlers.get("bravoFixture.PetFixture") instanceof PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3);

    // charlieFixture-3
    migrationHandlers = s_inventory.getMigrationHandlers(CharlieFixture_3.VERSION);
    assertEquals(1, migrationHandlers.size());
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_3);

    // charlieFixture-4
    migrationHandlers = s_inventory.getMigrationHandlers(CharlieFixture_4.VERSION);
    assertEquals(1, migrationHandlers.size());
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_4);

    // charlieFixture-5
    migrationHandlers = s_inventory.getMigrationHandlers(CharlieFixture_5.VERSION);
    assertEquals(1, migrationHandlers.size());
    assertTrue(migrationHandlers.get("charlieFixture.RoomFixture") instanceof RoomFixtureDoStructureMigrationHandler_5);
  }
}
