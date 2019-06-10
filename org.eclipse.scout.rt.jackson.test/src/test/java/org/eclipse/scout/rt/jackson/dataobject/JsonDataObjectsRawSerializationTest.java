/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.eclipse.scout.rt.dataobject.DataObjectVisitors;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
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
  public void testVersionedDo() {
    TestVersionedDo versioned = BEANS.get(TestVersionedDo.class).withName("lorem");
    String json = s_dataObjectMapper.writeValue(versioned);
    DoEntity rawEntity_8_0_0 = (DoEntity) s_dataObjectMapper.readValueRaw(json);
    assertEquals("scout-8.0.0", rawEntity_8_0_0.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_a_b_c = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoInvalidVersion.json"));
    assertEquals("scout-a.b.c", rawEntity_a_b_c.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_emptyVersion = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoEmptyVersion.json"));
    assertEquals("", rawEntity_emptyVersion.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_nullVersion = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoNullVersion.json"));
    assertNull(rawEntity_nullVersion.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    DoEntity rawEntity_noVersion = (DoEntity) s_dataObjectMapper.readValueRaw(readResourceAsString("TestVersionedDoNoVersion.json"));
    assertNull(rawEntity_noVersion.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));
  }

  protected void testRawDataObjectMapper(String jsonFileName) {
    String json = readResourceAsString(jsonFileName);
    IDataObject object = s_dataObjectMapper.readValueRaw(json);
    assertNoTypes(object);
  }

  /**
   * Asserts given {@code dataObject} contains no concrete data object instances except {@link DoEntity} and
   * {@link DoList}.
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
