/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.jackson.dataobject.IDataObjectSerializerProvider;
import org.eclipse.scout.rt.jackson.dataobject.JacksonPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithIIdDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;

public class QualifiedIIdSerializationTest {

  protected static final FixtureLongId LONG_1_ID = FixtureLongId.of(37L);
  protected static final FixtureLongId LONG_2_ID = FixtureLongId.of(73L);
  protected static final FixtureLongId LONG_3_ID = FixtureLongId.of(23L);
  protected static final FixtureStringId STRING_1_ID = FixtureStringId.of("foo");
  protected static final FixtureStringId STRING_2_ID = FixtureStringId.of("foo2");
  protected static final FixtureUuId UU_ID = FixtureUuId.of("8fa211b0-fd83-42cb-96c9-1942e274ce79");
  protected static final FixtureStringId STRING_MARCO_ID = FixtureStringId.of("marco");
  protected static final FixtureCompositeId COMPOSITE_ID_1 = FixtureCompositeId.of(STRING_1_ID, UU_ID);
  protected static final FixtureCompositeId COMPOSITE_ID_2 = FixtureCompositeId.of(STRING_2_ID, UU_ID);

  protected final List<IBean<?>> m_beans = new ArrayList<>();

  protected DataObjectSerializationTestHelper m_testHelper;
  protected IDataObjectMapper m_dataObjectMapper;

  @Before
  public void before() {
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(QualifiedIIdSerializationTest_DataObjectSerializerProvider.class)));

    m_dataObjectMapper = new JacksonPrettyPrintDataObjectMapper(); // create new instance not using BEANS#get because of serializer cache
    m_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    m_beans.clear();
  }

  @Test
  public void testSerializeDeserialize_EntityQualifiedIId() throws Exception {
    TestEntityWithIIdDo entity = BEANS.get(TestEntityWithIIdDo.class)
        .withLongId(LONG_1_ID)
        .withStringId(STRING_1_ID)
        .withUuId(UU_ID)
        .withCompositeId(COMPOSITE_ID_1)
        .withIid(COMPOSITE_ID_2)
        .withMap(Collections.singletonMap(STRING_MARCO_ID, "polo"))
        .withCompositeMap(Collections.singletonMap(COMPOSITE_ID_1, "polo"))
        .withLongIds(Arrays.asList(LONG_1_ID, LONG_2_ID))
        .withLongIdsAsDoList(LONG_2_ID, LONG_3_ID)
        .withStringIds(Arrays.asList(STRING_1_ID, STRING_2_ID))
        .withStringIdsAsDoList(STRING_2_ID, STRING_2_ID);
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithIIdDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithIIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithIIdDo.class);
    assertEquals(LONG_1_ID, marshalled.getLongId());
    assertEquals(STRING_1_ID, marshalled.getStringId());
    assertEquals(UU_ID, marshalled.getUuId());
    assertEquals(COMPOSITE_ID_1, marshalled.getCompositeId());
    assertEquals(COMPOSITE_ID_2, marshalled.getIid());
    assertEquals(Arrays.asList(LONG_1_ID, LONG_2_ID), marshalled.getLongIds());
    assertEquals(Arrays.asList(LONG_2_ID, LONG_3_ID), marshalled.getLongIdsAsDoList());
    assertEquals(Arrays.asList(STRING_1_ID, STRING_2_ID), marshalled.getStringIds());
    assertEquals(Arrays.asList(STRING_2_ID, STRING_2_ID), marshalled.getStringIdsAsDoList());
  }

  protected URL toURL(String resourceName) {
    return QualifiedIIdSerializationTest.class.getResource(resourceName);
  }

  @IgnoreBean
  protected static class QualifiedIIdSerializationTest_DataObjectSerializerProvider implements IDataObjectSerializerProvider {

    @Override
    public JsonSerializer<?> findSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription beanDesc) {
      if (type.hasRawClass(FixtureStringId.class) || type.hasRawClass(FixtureUuId.class) || type.hasRawClass(FixtureCompositeId.class)) {
        return new QualifiedIIdSerializer();
      }

      return null;
    }

    @Override
    public JsonDeserializer<?> findDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
      if (type.hasRawClass(FixtureStringId.class) || type.hasRawClass(FixtureUuId.class) || type.hasRawClass(FixtureCompositeId.class) || type.hasRawClass(IId.class)) {
        return new QualifiedIIdDeserializer(moduleContext, type.getRawClass().asSubclass(IId.class));
      }
      return null;
    }

    @Override
    public JsonSerializer<?> findKeySerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription beanDesc) {
      if (type.hasRawClass(FixtureStringId.class) || type.hasRawClass(FixtureUuId.class) || type.hasRawClass(FixtureCompositeId.class)) {
        return new QualifiedIIdMapKeySerializer();
      }
      return null;
    }

    @Override
    public KeyDeserializer findKeyDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
      if (type.hasRawClass(FixtureStringId.class) || type.hasRawClass(FixtureUuId.class) || type.hasRawClass(FixtureCompositeId.class) || type.hasRawClass(IId.class)) {
        return new QualifiedIIdMapKeyDeserializer(moduleContext, type.getRawClass().asSubclass(IId.class));
      }
      return null;
    }
  }
}
