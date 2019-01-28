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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.eclipse.scout.rt.jackson.dataobject.fixture.TestVersionedDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoNode;
import org.eclipse.scout.rt.platform.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JsonDataObjectsRawSerializationTest {

  protected static IDataObjectMapper s_dataObjectMapper;
  protected static DataObjectSerializationTestHelper s_testHelper;

  /**
   * Visitor for {@link IDataObject} structures.
   */
  @Bean
  public static class DataObjectVisitor {

    /**
     * Visits given {@code dataObject} and all its nested attributes.The given {@code visitor} is called for each
     * attribute whose type is assignable from the given {@code type}.
     *
     * @param dataObject
     *          data object to visit
     * @param visitor
     *          consumer called for each element whose type is assignable from the given {@code type}
     * @param type
     *          type of elements to be visited, only attributes which are assignable from the given {@code type} are
     *          visited.
     */
    public <T> void visit(IDataObject dataObject, Consumer<T> visitor, Class<T> type) {
      visitRec(dataObject, visitor, type);
    }

    protected <T> void visitRec(Object obj, Consumer<T> visitor, Class<T> type) {
      if (obj == null) {
        return;
      }

      // visit current object if type matches
      if (type.isAssignableFrom(obj.getClass())) {
        visitor.accept(type.cast(obj));
      }

      // visit all nested children
      if (obj instanceof Collection) {
        visitCollectionImpl((Collection) obj, visitor, type);
      }
      else if (obj instanceof Map) {
        visitMapImpl((Map<?, ?>) obj, visitor, type);
      }
      else if (obj instanceof IDoEntity) {
        visitDoEntityImpl((IDoEntity) obj, visitor, type);
      }
      else if (obj instanceof DoList) {
        visitCollectionImpl(((DoList) obj).get(), visitor, type);
      }
    }

    protected <T> void visitCollectionImpl(Collection collection, Consumer<T> visitor, Class<T> type) {
      for (Object item : collection) {
        visitRec(item, visitor, type);
      }
    }

    protected <T> void visitMapImpl(Map<?, ?> map, Consumer<T> visitor, Class<T> type) {
      for (Entry<?, ?> entry : map.entrySet()) {
        visitRec(entry.getKey(), visitor, type);
        visitRec(entry.getValue(), visitor, type);
      }
    }

    protected <T> void visitDoEntityImpl(IDoEntity entity, Consumer<T> visitor, Class<T> type) {
      for (DoNode node : entity.allNodes().values()) {
        visitRec(node.get(), visitor, type);
      }
    }
  }

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
    BEANS.get(DataObjectVisitor.class).visit(dataObject, this::assertType, IDataObject.class);
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
