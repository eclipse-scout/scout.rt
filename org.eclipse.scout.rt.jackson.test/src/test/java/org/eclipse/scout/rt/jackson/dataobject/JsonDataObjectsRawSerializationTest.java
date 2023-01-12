/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DataObjectVisitors;
import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestVersionedDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JsonDataObjectsRawSerializationTest {

  protected static IDataObjectMapper s_dataObjectMapper;
  protected static DataObjectSerializationTestHelper s_testHelper;

  @BeforeClass
  public static void beforeClass() {
    s_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
    s_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
  }

  @Test
  public void testComplexEntityDo() {
    testRawDataObjectMapper("TestComplexEntityDo.json");
  }

  @Test
  public void testItemDoListDataObject() {
    testRawDataObjectMapper("TestItemDoListIDataObject.json");
  }

  @Test
  public void testDoMapEntityDo() {
    testRawDataObjectMapper("TestDoMapEntityDo.json");
  }

  @Test
  public void testComplexEntityDoRaw() {
    testRawDataObjectMapper("TestComplexEntityDoRaw.json");
  }

  @Test
  public void testMyCustomTypeDo() {
    testRawDataObjectMapper("TestMyCustomTypeDo.json");
  }

  @Test
  public void testDoEntityWithContributions() {
    DoEntity doEntity = (DoEntity) testRawDataObjectMapper("TestDoEntityWithContributions.json");
    assertTrue(doEntity.getContributions().isEmpty()); // in raw mode, contributions are just a regular node
    List<IDoEntity> contributions = doEntity.getList(ScoutDataObjectModule.DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME, IDoEntity.class);
    assertNotNull(contributions);
    assertEquals(2, contributions.size());
    assertEquals("scout.TestItemContributionOne", contributions.get(0).getString(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));
    assertEquals("scout.TestItemContributionTwo", contributions.get(1).getString(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));
  }

  /**
   * @see DoEntitySerializerAttributeNameComparator
   */
  @Test
  public void testAttributeSerializationOrder() {
    // Manual creation of raw entity with random order of attributes
    IDoEntity entity = BEANS.get(DoEntityBuilder.class)
        .put("bravo", "bravo-value")
        .putList(ScoutDataObjectModule.DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME,
            BEANS.get(DoEntityBuilder.class)
                .put(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME, "TestItemContributionBravo")
                .put("name", "bravo")
                .build(),
            BEANS.get(DoEntityBuilder.class)
                .put(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME, "TestItemContributionAlfa")
                .put("name", "alfa")
                .build())
        .put(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME, "TestItem")
        .put(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME, "scout-11.0.0")
        .put("alfa", "alfa-value")
        .build();

    // Contributions are serialized in insertion order
    String json = s_dataObjectMapper.writeValue(entity);
    assertJsonEquals("TestAttributeSerializationOrder.json", json);
  }

  @Test
  public void testVersionedDo() {
    TestVersionedDo versioned = BEANS.get(TestVersionedDo.class).withName("lorem");
    String json = s_dataObjectMapper.writeValue(versioned);
    DoEntity rawEntity_8_0_0 = (DoEntity) s_dataObjectMapper.readValueRaw(json);
    assertEquals("jacksonFixture-1.0.0", rawEntity_8_0_0.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_a_b_c = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoInvalidVersion.json"));
    assertEquals("scout-a.b.c", rawEntity_a_b_c.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_emptyVersion = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoEmptyVersion.json"));
    assertEquals("", rawEntity_emptyVersion.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_nullVersion = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoNullVersion.json"));
    assertNull(rawEntity_nullVersion.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_noVersion = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoNoVersion.json"));
    assertNull(rawEntity_noVersion.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));
  }

  @Test
  public void testCloneRaw() {
    TestComplexEntityDo entity = BEANS.get(TestComplexEntityDo.class)
        .withStringAttribute("str1")
        .withItemAttribute(BEANS.get(TestItemDo.class).withStringAttribute("str2"));
    IDoEntity clone = BEANS.get(DataObjectHelper.class).cloneRaw(entity);
    assertNoTypes(clone);
    assertEquals("str1", clone.get("stringAttribute"));
    Object item = clone.get("itemAttribute");
    assertNotNull(item);
    assertEquals(DoEntity.class, item.getClass());
    assertEquals("str2", ((DoEntity) item).get("stringAttribute"));
  }

  protected IDataObject testRawDataObjectMapper(String jsonFileName) {
    String json = readResourceAsString(jsonFileName);
    IDataObject object = s_dataObjectMapper.readValueRaw(json);
    assertNoTypes(object);
    return object;
  }

  /**
   * Asserts given {@code dataObject} contains no concrete data object instances except {@link DoEntity} and
   * {@link DoList}. Raw data objects will never contain {@link DoSet} or {@link DoCollection}.
   */
  protected void assertNoTypes(IDataObject dataObject) {
    DataObjectVisitors.forEach(dataObject, IDataObject.class, this::assertType);
  }

  protected void assertType(IDataObject entity) {
    assertTrue("Expected type DoEntity or DoList, was " + entity.getClass(), entity.getClass().equals(DoEntity.class) || entity.getClass().equals(DoList.class));
  }

  protected void assertJsonEquals(String expectedResourceName, String actual) {
    s_testHelper.assertJsonEquals(getResource(expectedResourceName), actual);
  }

  protected String readResourceAsString(String resourceName) {
    try {
      return s_testHelper.readResourceAsString(getResource(resourceName));
    }
    catch (IOException e) {
      throw new AssertionError("Failed to read resource " + resourceName + ", error=" + e.getMessage(), e);
    }
  }

  protected URL getResource(String expectedResourceName) {
    return JsonDataObjectsRawSerializationTest.class.getResource(expectedResourceName);
  }
}
