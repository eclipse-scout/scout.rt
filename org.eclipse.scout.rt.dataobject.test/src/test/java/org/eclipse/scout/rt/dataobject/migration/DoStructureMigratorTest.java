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

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;
import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PersonFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PersonFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PersonFixtureTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureCaseSensitiveNameMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PetFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PostalAddressFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PostalAddressFixtureUpdateVersionOnlyMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_4;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDoStructureMigrationHandler_5;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link DoStructureMigrator}.
 */
public class DoStructureMigratorTest {

  private static final List<IBean<?>> TEST_BEANS = new ArrayList<>();

  private static DoStructureMigrationContext s_migrationContext;
  private static DoStructureMigrator s_migrator;

  @BeforeClass
  public static void beforeClass() {
    DoStructureMigrationTestHelper testHelper = BEANS.get(DoStructureMigrationTestHelper.class);
    TestDoStructureMigrationInventory inventory = new TestDoStructureMigrationInventory(
        testHelper.getFixtureNamespaces(),
        testHelper.getFixtureTypeVersions(),
        testHelper.getFixtureContextDataClasses(),
        new PostalAddressFixtureUpdateVersionOnlyMigrationHandler_2(),
        new PetFixtureCaseSensitiveNameMigrationHandler_2(),
        new PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3(),
        new CustomerFixtureMigrationHandler_3(),
        new CharlieCustomerFixtureMigrationHandler_3(),
        new HouseFixtureDoStructureMigrationHandler_2(),
        new RoomFixtureDoStructureMigrationHandler_2(),
        new RoomFixtureDoStructureMigrationHandler_3(),
        new RoomFixtureDoStructureMigrationHandler_4(),
        new RoomFixtureDoStructureMigrationHandler_5(),
        new PersonFixtureDoStructureMigrationHandler_2());

    TEST_BEANS.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(TestDoStructureMigrationInventory.class, inventory).withReplace(true)));

    s_migrationContext = BEANS.get(DoStructureMigrationContext.class);
    s_migrator = BEANS.get(DoStructureMigrator.class);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(TEST_BEANS);
  }

  /**
   * Tests migration of a data object without prior type version with an implemented migration handler.
   * <p>
   * Uses data object {@link PetFixtureDo} and migration handlers {@link PetFixtureCaseSensitiveNameMigrationHandler_2}.
   */
  @Test
  public void testMigrationWithoutPreviousTypeVersion() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "bravoFixture.PetFixture")
        .put("name", "CHARLIE")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual, BravoFixture_2.VERSION)); // stop at bravoFixture-2, otherwise namespace will change too

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "bravoFixture.PetFixture")
        .put("_typeVersion", BravoFixture_2.VERSION.unwrap())
        .put("name", "Charlie")
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Tests migration of a data object without prior type version with an empty migration handler.
   * <p>
   * Uses data object {@link PostalAddressFixtureDo} and migration handlers
   * {@link PostalAddressFixtureUpdateVersionOnlyMigrationHandler_2}.
   */
  @Test
  public void testMigrationUpdateVersionOnly() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PostalAddressFixture")
        .put("street", "Main street 12")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PostalAddressFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())
        .put("street", "Main street 12")
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Tests migration of a data object with prior type version and with an empty migration handler.
   * <p>
   * Uses data object {@link PostalAddressFixtureDo} and migration handlers
   * {@link PostalAddressFixtureUpdateVersionOnlyMigrationHandler_2}.
   */
  @Test
  public void testUpdateVersionWithPreviousTypeVersion() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PostalAddressFixture")
        .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
        .put("street", "Main street 12")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PostalAddressFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())
        .put("street", "Main street 12")
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Tests namespace change.
   * <p>
   * Uses data object {@link PetFixtureDo} and migration handlers {@link PetFixtureCaseSensitiveNameMigrationHandler_2}
   * and {@link PetFixtureAlfaNamespaceFamilyFriendlyMigrationHandler_3}.
   */
  @Test
  public void testNamespaceChange() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "bravoFixture.PetFixture")
        .put("_typeVersion", BravoFixture_1.VERSION.unwrap())
        .put("name", "JOHN")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "alfaFixture.PetFixture")
        .put("_typeVersion", AlfaFixture_3.VERSION.unwrap())
        .put("name", "John")
        .put("familyFriendly", true)
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Uses data object {@link CustomerFixtureDo} and migration handlers {@link CustomerFixtureMigrationHandler_3}.
   */
  @Test
  public void testMigrationHandlerForOriginalDataObject() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "alfaFixture.CustomerFixture")
        .put("_typeVersion", AlfaFixture_1.VERSION.unwrap())
        .put("firstName", "John")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "alfaFixture.CustomerFixture")
        .put("_typeVersion", AlfaFixture_3.VERSION.unwrap())
        .put("firstName", "JOHN") // uppercase due to CustomerFixtureMigrationHandler_3
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Migration handler for original data objects are not executed anymore when a data object is replaced an and own type
   * version is used.
   * <p>
   * Uses data object {@link CharlieCustomerFixtureDo} and migration handlers
   * {@link CharlieCustomerFixtureMigrationHandler_3}.
   */
  @Test
  public void testMigrationHandlerForSubclassedDataObject() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "alfaFixture.CustomerFixture")
        .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
        .put("firstName", "John")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "alfaFixture.CustomerFixture")
        .put("_typeVersion", CharlieFixture_3.VERSION.unwrap())
        .put("firstName", "john") // lowercase due to CharlieCustomerFixtureMigrationHandler_3
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  @Test
  public void testHouseFixtureMigration_1_to_5() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.BuildingFixture")
        .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
        .put("name", "Family Doe")
        .putList("rooms", BEANS.get(DoEntityBuilder.class)
            .put("_type", "charlieFixture.RoomFixture")
            .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
            .put("roomName", "Kitchen")
            .build())
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.HouseFixture") // BuildingFixture -> HouseFixture
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap()) // updated version
        .put("name", "Family Doe")
        .putList("rooms", BEANS.get(DoEntityBuilder.class)
            .put("_type", "charlieFixture.RoomFixture")
            .put("_typeVersion", CharlieFixture_5.VERSION.unwrap()) // updated version
            .put("name", "Kitchen") // roomName -> name
            .put("displayText", "Family Doe: Kitchen") // display text was calculated based on name of house and room
            .build())
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  @Test
  public void testHouseFixtureMigration_3_to_5() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.HouseFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())
        .put("name", "Family Doe")
        .putList("rooms", BEANS.get(DoEntityBuilder.class)
            .put("_type", "charlieFixture.RoomFixture")
            .put("_typeVersion", CharlieFixture_3.VERSION.unwrap())
            .put("name", "Kitchen")
            .put("areaInSquareFoot", 10764)
            .build())
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.HouseFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())
        .put("name", "Family Doe")
        .putList("rooms", BEANS.get(DoEntityBuilder.class)
            .put("_type", "charlieFixture.RoomFixture")
            .put("_typeVersion", CharlieFixture_5.VERSION.unwrap()) // updated version
            .put("name", "Kitchen")
            .put("displayText", "Family Doe: Kitchen (1000m2)") // display text was calculated based on areaInSquareMeter and name of house and room
            .put("areaInSquareMeter", 1000) // areaInSquareFoot -> areaInSquareMeter
            .build())
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Uses data object {@link PersonFixtureDo} and migration handlers {@link PersonFixtureDoStructureMigrationHandler_2}
   * with {@link PersonFixtureTargetContextData}.
   * <p>
   * {@link PersonFixtureTargetContextData} contains additional assertions used to verify correct order of migration vs.
   * context data initialization.
   */
  @Test
  public void testStackedLocalContextData() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PersonFixture")
        .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
        .put("name", "John Doe")
        .putList("children", BEANS.get(DoEntityBuilder.class)
            .put("_type", "charlieFixture.PersonFixture")
            .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
            .put("name", "John Doe Junior")
            .putList("children", BEANS.get(DoEntityBuilder.class)
                .put("_type", "charlieFixture.PersonFixture")
                .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
                .put("name", "John Doe Junior 2. Gen")
                .build())
            .build())
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PersonFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap()) // updated version
        .put("name", "John Doe")
        .put("relation", "(none)") // add relation
        .putList("children", BEANS.get(DoEntityBuilder.class)
            .put("_type", "charlieFixture.PersonFixture")
            .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())// updated version
            .put("name", "John Doe Junior")
            .put("relation", "Child of John Doe") // added relation
            .putList("children", BEANS.get(DoEntityBuilder.class)
                .put("_type", "charlieFixture.PersonFixture")
                .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())// updated version
                .put("name", "John Doe Junior 2. Gen")
                .put("relation", "Child of John Doe Junior") // added relation
                .build())
            .build())
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Uses data object {@link PersonFixtureDo} and migration handlers {@link PersonFixtureDoStructureMigrationHandler_2}
   * with {@link PersonFixtureTargetContextData}.
   */
  @Test
  public void testListManipulationViaMigrationHandler() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PersonFixture")
        .put("_typeVersion", CharlieFixture_1.VERSION.unwrap())
        .put("name", "example")
        .build();

    assertTrue(s_migrator.migrateDataObject(s_migrationContext, actual));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("_type", "charlieFixture.PersonFixture")
        .put("_typeVersion", CharlieFixture_2.VERSION.unwrap()) // updated version
        .put("name", "example")
        .put("relation", "(none)") // added relation
        .putList("children", BEANS.get(DoEntityBuilder.class) // added child
            .put("_type", "charlieFixture.PersonFixture")
            .put("_typeVersion", CharlieFixture_2.VERSION.unwrap())
            .put("name", "Jane Doe")
            .put("relation", "(undefined)")
            .build())
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }
}
