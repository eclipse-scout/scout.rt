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

import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CharlieCustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.CustomerFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.PostalAddressFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.RoomFixtureDo;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_3;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_5;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DoStructureMigrationHelperTest {

  private static DoStructureMigrationHelper s_helper;

  @BeforeClass
  public static void beforeClass() {
    s_helper = BEANS.get(DoStructureMigrationHelper.class);
  }

  /**
   * These constants must never change.
   */
  @Test
  public void testConstants() {
    assertEquals("_type", DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME);
    assertEquals("_typeVersion", DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME);
  }

  @Test
  public void testGetType() {
    assertNull(s_helper.getType(BEANS.get(DoEntityBuilder.class).build()));
    assertEquals("alfaFixture.Example", s_helper.getType(BEANS.get(DoEntityBuilder.class).put("_type", "alfaFixture.Example").build()));
  }

  @Test
  public void testSetType() {
    IDoEntity doEntity = BEANS.get(DoEntityBuilder.class).build();
    s_helper.setType(doEntity, "alfaFixture.Example");
    assertEquals("alfaFixture.Example", s_helper.getType(doEntity));
    assertEquals("alfaFixture.Example", doEntity.getString("_type"));
  }

  @Test
  public void testGetTypeVersion() {
    assertNull(s_helper.getTypeVersion(BEANS.get(DoEntityBuilder.class).build()));
    assertEquals(AlfaFixture_1.VERSION, s_helper.getTypeVersion(BEANS.get(DoEntityBuilder.class).put("_typeVersion", "alfaFixture-1").build()));
  }

  @Test
  public void testSetTypeVersion() {
    IDoEntity doEntity = BEANS.get(DoEntityBuilder.class).build();
    s_helper.setTypeVersion(doEntity, AlfaFixture_1.VERSION);
    assertEquals(AlfaFixture_1.VERSION, s_helper.getTypeVersion(doEntity));
    assertEquals(AlfaFixture_1.VERSION.unwrap(), doEntity.getString("_typeVersion"));
  }

  @Test
  public void testUpdateTypeVersion() {
    IDoEntity doEntity = BEANS.get(DoEntityBuilder.class).build();
    s_helper.setTypeVersion(doEntity, AlfaFixture_1.VERSION);
    assertFalse(s_helper.updateTypeVersion(doEntity, AlfaFixture_1.VERSION)); // already up-to-date
    assertTrue(s_helper.updateTypeVersion(doEntity, AlfaFixture_2.VERSION)); // changed
    assertEquals(AlfaFixture_2.VERSION.unwrap(), doEntity.getString("_typeVersion")); // verify
    assertFalse(s_helper.updateTypeVersion(doEntity, AlfaFixture_2.VERSION)); // already up-to-date
  }

  @Test
  public void testCollectRawDataObjectTypeVersions() {
    DoStructureMigrationHelper helper = BEANS.get(DoStructureMigrationHelper.class);

    // Single data object
    Assert.assertEquals(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION)),
        helper.collectRawDataObjectTypeVersions(rawDataObject(BEANS.get(HouseFixtureDo.class))));

    // Data object containing another data objects (house owner attribute not set here)
    Assert.assertEquals(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
        new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_5.VERSION),
        new ImmutablePair<>("charlieFixture.PostalAddressFixture", CharlieFixture_2.VERSION)),
        helper.collectRawDataObjectTypeVersions(rawDataObject(
            BEANS.get(HouseFixtureDo.class)
                .withRooms(BEANS.get(RoomFixtureDo.class))
                .withPostalAddress(BEANS.get(PostalAddressFixtureDo.class)))));

    // Data object with a subclass but using origin class (thus no BEANS.get) [not a real case]
    Assert.assertEquals(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
        new ImmutablePair<>("alfaFixture.CustomerFixture", AlfaFixture_3.VERSION)),
        helper.collectRawDataObjectTypeVersions(rawDataObject(
            BEANS.get(HouseFixtureDo.class)
                .withOwner(new CustomerFixtureDo()))));

    // Data object with a subclass using replaced class
    Assert.assertEquals(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION),
        new ImmutablePair<>("alfaFixture.CustomerFixture", CharlieFixture_3.VERSION)),
        helper.collectRawDataObjectTypeVersions(rawDataObject(
            BEANS.get(HouseFixtureDo.class)
                .withOwner(BEANS.get(CharlieCustomerFixtureDo.class)))));

    // Must not find any type versions because a typed data object is used. When requesting type versions of raw data object,
    // typed data object must not be returned (only raw objects are relevant for migration).
    assertTrue(helper.collectRawDataObjectTypeVersions(BEANS.get(HouseFixtureDo.class)).isEmpty());
  }

  @Test
  public void testIsMigrationApplicable() {
    assertFalse(s_helper.isMigrationApplicable(BEANS.get(RoomFixtureDo.class), CharlieFixture_2.VERSION)); // typed data object (not raw)
    assertTrue(s_helper.isMigrationApplicable(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.RoomFixture").build(), CharlieFixture_2.VERSION)); // no type version (yet)
    assertFalse(s_helper.isMigrationApplicable(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.RoomFixture").put("_typeVersion", AlfaFixture_1.VERSION.unwrap()).build(), CharlieFixture_2.VERSION)); // different namespace
    assertFalse(s_helper.isMigrationApplicable(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.RoomFixture").put("_typeVersion", CharlieFixture_2.VERSION.unwrap()).build(), CharlieFixture_2.VERSION)); // same version
    assertTrue(s_helper.isMigrationApplicable(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.RoomFixture").put("_typeVersion", CharlieFixture_1.VERSION.unwrap()).build(), CharlieFixture_2.VERSION)); // lower version
    assertFalse(s_helper.isMigrationApplicable(BEANS.get(DoEntityBuilder.class).put("_type", "charlieFixture.RoomFixture").put("_typeVersion", CharlieFixture_3.VERSION.unwrap()).build(), CharlieFixture_2.VERSION)); // higher version (invalid)
  }

  /**
   * References {@link HouseFixtureDo} with type version {@link CharlieFixture_2} and {@link RoomFixtureDo} with type
   * version {@link CharlieFixture_5}.
   */
  @Test
  public void testIsAnyMigrationRequired() {
    assertFalse(s_helper.isAnyMigrationRequired(CollectionUtility.emptyHashMap())); // no versions at all -> not outdated

    assertTrue(s_helper.isAnyMigrationRequired(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_1.VERSION.unwrap()),
        new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_1.VERSION.unwrap()))));

    // invalid constellation, all data objects within a persisted data object have a consistent type version
    assertTrue(s_helper.isAnyMigrationRequired(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION.unwrap()),
        new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_1.VERSION.unwrap()))));

    // invalid constellation, all data objects within a persisted data object have a consistent type version
    assertTrue(s_helper.isAnyMigrationRequired(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_1.VERSION.unwrap()),
        new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_5.VERSION.unwrap()))));

    assertFalse(s_helper.isAnyMigrationRequired(CollectionUtility.hashMap(
        new ImmutablePair<>("charlieFixture.HouseFixture", CharlieFixture_2.VERSION.unwrap()),
        new ImmutablePair<>("charlieFixture.RoomFixture", CharlieFixture_5.VERSION.unwrap()))));
  }

  /**
   * References {@link HouseFixtureDo} that has type version {@link CharlieFixture_2}.
   */
  @Test
  public void testIsMigrationRequired() {
    assertTrue(s_helper.isMigrationRequired("charlieFixture.HouseFixture", null)); // no type version
    assertFalse(s_helper.isMigrationRequired("charlieFixture.HouseFixture", NamespaceVersion.of("unknown", "1.0.0"))); // unknown type version
    assertTrue(s_helper.isMigrationRequired("charlieFixture.BuildingFixture", CharlieFixture_1.VERSION)); // unknown type name (e.g. renamed)
    assertTrue(s_helper.isMigrationRequired("charlieFixture.HouseFixture", CharlieFixture_1.VERSION)); // lower type version
    assertFalse(s_helper.isMigrationRequired("charlieFixture.HouseFixture", CharlieFixture_2.VERSION)); // same type version
    assertFalse(s_helper.isMigrationRequired("charlieFixture.HouseFixture", CharlieFixture_3.VERSION)); // newer type version (invalid)
  }

  @Test
  public void testRenameTypeName() {
    // Regular rename
    IDoEntity actual = BEANS.get(DoEntityBuilder.class).put("_type", "lorem").build();
    assertTrue(s_helper.renameTypeName(actual, "ipsum"));
    IDoEntity expected = BEANS.get(DoEntityBuilder.class).put("_type", "ipsum").build();
    assertEqualsWithComparisonFailure(expected, actual);

    // Already renamed
    actual = BEANS.get(DoEntityBuilder.class).put("_type", "ipsum").build();
    assertFalse(s_helper.renameTypeName(actual, "ipsum"));
    expected = BEANS.get(DoEntityBuilder.class).put("_type", "ipsum").build();
    assertEqualsWithComparisonFailure(expected, actual);
  }

  @Test
  public void testRenameAttribute() {
    // non-existing attribute
    IDoEntity actual = BEANS.get(DoEntityBuilder.class).put("lorem", "ipsum").build();
    assertFalse(s_helper.renameAttribute(actual, "dolor", "sid"));
    IDoEntity expected = BEANS.get(DoEntityBuilder.class).put("lorem", "ipsum").build();
    assertEqualsWithComparisonFailure(expected, actual);

    // existing attribute
    actual = BEANS.get(DoEntityBuilder.class).put("lorem", "ipsum").build();
    assertTrue(s_helper.renameAttribute(actual, "lorem", "sid"));
    expected = BEANS.get(DoEntityBuilder.class).put("sid", "ipsum").build();
    assertEqualsWithComparisonFailure(expected, actual);

    // existing attribute (node only, no value)
    actual = BEANS.get(DoEntityBuilder.class).put("lorem", null).build();
    assertTrue(s_helper.renameAttribute(actual, "lorem", "sid"));
    expected = BEANS.get(DoEntityBuilder.class).put("sid", null).build();
    assertEqualsWithComparisonFailure(expected, actual);
  }

  protected IDataObject rawDataObject(IDataObject dataObject) {
    IDataObjectMapper mapper = BEANS.get(IDataObjectMapper.class);
    return mapper.readValueRaw(mapper.writeValue(dataObject));
  }
}
