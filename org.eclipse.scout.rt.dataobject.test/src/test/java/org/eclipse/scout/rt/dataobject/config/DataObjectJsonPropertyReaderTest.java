/*******************************************************************************
 * Copyright (c) 2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.config;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link DataObjectJsonPropertyReader}.
 */
@RunWith(PlatformTestRunner.class)
public class DataObjectJsonPropertyReaderTest {

  private static final List<IBean<?>> s_beans = new ArrayList<>();

  private static final Map<String, Supplier<? extends IDoEntity>> m_propertyValueToDoEntityMap = new HashMap<>();

  @BeforeClass
  public static void beforeClass() throws Exception {
    s_beans.addAll(BEANS.get(BeanTestingHelper.class).registerBeans(new BeanMetaData(TestingDataObjectMapper.class)));
  }

  @Before
  public void before() {
    m_propertyValueToDoEntityMap.clear();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(s_beans);
  }

  @Test
  public void testNullArgument() throws Exception {
    Map<String, String> actualResult = new DataObjectJsonPropertyReader().readJsonPropertyValue(null);
    assertThat(actualResult, is(nullValue()));
  }

  @Test
  public void testEmptyStringArgument() throws Exception {
    testReadJsonPropertyValue(
        "",
        () -> {
          throw new RuntimeException("Data object mapper called with an empty string argument. Handle empty string arguments separately, don't pass to the data object mapper.");
        },
        Collections.emptyMap());
  }

  @Test
  public void testReadEmptyJsonObjectString() throws Exception {
    testReadJsonPropertyValue(
        "{}",
        () -> BEANS.get(DoEntity.class),
        Collections.emptyMap());
  }

  @Test
  public void testReadJsonObjectWithSingleEmptyStringProperty() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey\": \"\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey", "")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey", "")));
  }

  @Test
  public void testReadJsonObjectWithSingleStringProperty() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey\": \"testValue\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey", "testValue")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey", "testValue")));
  }

  @Test
  public void testReadJsonObjectWithMultipleStringProperties() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey1\": \"testValue1\", \"testKey2\": \"testValue2\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey1", "testValue1")
            .put("testKey2", "testValue2")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey1", "testValue1"),
            new ImmutablePair<>("testKey2", "testValue2")));
  }

  @Test
  public void testReadJsonObjectWithPropertyWithNullValue() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey1\": null, \"testKey2\": \"testValue2\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey1", null)
            .put("testKey2", "testValue2")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey1", null),
            new ImmutablePair<>("testKey2", "testValue2")));
  }

  @Test
  public void testReadJsonObjectWithIntegerProperty() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey1\": 1, \"testKey2\": \"testValue2\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey1", 1)
            .put("testKey2", "testValue2")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey1", "1"),
            new ImmutablePair<>("testKey2", "testValue2")));
  }

  @Test
  public void testReadJsonObjectWithBooleanProperty() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey1\": true, \"testKey2\": \"testValue2\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey1", true)
            .put("testKey2", "testValue2")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey1", "true"),
            new ImmutablePair<>("testKey2", "testValue2")));
  }

  @Test
  public void testReadJsonObjectWithDoubleProperty() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey1\": 1.2, \"testKey2\": \"testValue2\"}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey1", 1.2)
            .put("testKey2", "testValue2")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey1", "1.2"),
            new ImmutablePair<>("testKey2", "testValue2")));
  }

  @Test
  public void testReadJsonObjectWithJsonStringProperty() throws Exception {
    testReadJsonPropertyValue(
        "{\"testKey1\": {\"subKey1\": \"subValue1\", \"subKey2\": \"subValue2\"}, \"testKey2\": [\"testValue2Sub1\", \"testValue2Sub2\", \"testValue2Sub3\"]}",
        () -> BEANS.get(DoEntityBuilder.class)
            .put("testKey1", "{\"subKey1\": \"subValue1\", \"subKey2\": \"subValue2\"}")
            .put("testKey2", "[\"testValue2Sub1\", \"testValue2Sub2\", \"testValue2Sub3\"]")
            .build(),
        CollectionUtility.hashMap(
            new ImmutablePair<>("testKey1", "{\"subKey1\": \"subValue1\", \"subKey2\": \"subValue2\"}"),
            new ImmutablePair<>("testKey2", "[\"testValue2Sub1\", \"testValue2Sub2\", \"testValue2Sub3\"]")));
  }

  protected void testReadJsonPropertyValue(String json, Supplier<? extends IDoEntity> dataObjectMapperResultSupplier, Map<String, String> expectedValue) {
    m_propertyValueToDoEntityMap.put(json, dataObjectMapperResultSupplier);
    Map<String, String> actualValue = new DataObjectJsonPropertyReader().readJsonPropertyValue(json);
    assertThat(actualValue, is(expectedValue));
  }

  @IgnoreBean
  public static class TestingDataObjectMapper implements IDataObjectMapper {

    @Override
    public <T> T readValue(InputStream inputStream, Class<T> valueType) {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readValue(String value, Class<T> valueType) {
      Assertions.assertTrue(IDoEntity.class.equals(valueType), "Only valueType of {} is supported.", DoEntity.class);
      Assertions.assertTrue(m_propertyValueToDoEntityMap.containsKey(value), "Prepare mapping for value {} before using this {}.", value, this.getClass());
      return (T) m_propertyValueToDoEntityMap.get(value).get();
    }

    @Override
    public IDataObject readValueRaw(InputStream inputStream) {
      throw new UnsupportedOperationException();
    }

    @Override
    public IDataObject readValueRaw(String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void writeValue(OutputStream outputStream, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String writeValue(Object value) {
      throw new UnsupportedOperationException();
    }
  }
}
