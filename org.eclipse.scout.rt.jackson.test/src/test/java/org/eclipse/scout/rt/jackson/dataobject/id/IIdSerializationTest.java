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

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithIIdDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Test;

public class IIdSerializationTest {

  protected static final FixtureLongId LONG_1_ID = FixtureLongId.of(37L);
  protected static final FixtureLongId LONG_2_ID = FixtureLongId.of(73L);
  protected static final FixtureLongId LONG_3_ID = FixtureLongId.of(23L);
  protected static final FixtureStringId STRING_1_ID = FixtureStringId.of("foo");
  protected static final FixtureStringId STRING_2_ID = FixtureStringId.of("foo2");
  protected static final FixtureUuId UU_ID = FixtureUuId.of("8fa211b0-fd83-42cb-96c9-1942e274ce79");
  protected static final FixtureStringId STRING_MARCO_ID = FixtureStringId.of("marco");
  protected static final FixtureCompositeId COMPOSITE_ID_1 = FixtureCompositeId.of(STRING_1_ID, UU_ID);
  protected static final FixtureCompositeId COMPOSITE_ID_2 = FixtureCompositeId.of(STRING_2_ID, UU_ID);

  protected DataObjectSerializationTestHelper m_testHelper;
  protected IDataObjectMapper m_dataObjectMapper;

  @Before
  public void before() {
    m_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
    m_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
  }

  @Test
  public void testSerializeDeserialize_EntityWithIId() throws Exception {
    TestEntityWithIIdDo entity = BEANS.get(TestEntityWithIIdDo.class)
        .withLongId(LONG_1_ID)
        .withStringId(STRING_1_ID)
        .withUuId(UU_ID)
        .withCompositeId(COMPOSITE_ID_1)
        .withMap(Collections.singletonMap(STRING_MARCO_ID, "polo"))
        .withCompositeMap(Collections.singletonMap(COMPOSITE_ID_1, "polo"))
        .withLongIds(Arrays.asList(LONG_1_ID, LONG_2_ID))
        .withLongIdsAsDoList(LONG_2_ID, LONG_3_ID)
        .withStringIds(Arrays.asList(STRING_1_ID, STRING_2_ID))
        .withStringIdsAsDoList(STRING_2_ID, STRING_2_ID);
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithIIdUnqualifiedDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithIIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithIIdDo.class);
    assertEquals(LONG_1_ID, marshalled.getLongId());
    assertEquals(STRING_1_ID, marshalled.getStringId());
    assertEquals(UU_ID, marshalled.getUuId());
    assertEquals(COMPOSITE_ID_1, marshalled.getCompositeId());
    assertEquals(Arrays.asList(LONG_1_ID, LONG_2_ID), marshalled.getLongIds());
    assertEquals(Arrays.asList(LONG_2_ID, LONG_3_ID), marshalled.getLongIdsAsDoList());
    assertEquals(Arrays.asList(STRING_1_ID, STRING_2_ID), marshalled.getStringIds());
    assertEquals(Arrays.asList(STRING_2_ID, STRING_2_ID), marshalled.getStringIdsAsDoList());
  }

  @Test
  public void testSerializeDeserialize_EntityWithUnqualifiedIId() throws Exception {
    TestEntityWithIIdDo entity = BEANS.get(TestEntityWithIIdDo.class).withIid(COMPOSITE_ID_1);
    String json = m_dataObjectMapper.writeValue(entity);
    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithIIdUnqualified2Do.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    // IId serialized as unqualified iid is not deserializable (IId class type is not available)
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));
  }

  protected URL toURL(String resourceName) {
    return IIdSerializationTest.class.getResource(resourceName);
  }
}
