/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collections;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithUuIdDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithUuIdMapKeyDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Test;

public class UuIdSerializationTest {

  protected DataObjectSerializationTestHelper m_testHelper;
  protected IDataObjectMapper m_dataObjectMapper;

  protected static final UUID TEST_UUID = UUID.fromString("38f760f7-c5c1-488c-81b4-cbe90c08423d");

  @Before
  public void before() {
    m_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
    m_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
  }

  @Test
  public void testSerializeDeserialize_EntityWithId() throws Exception {
    TestEntityWithUuIdDo entity = new TestEntityWithUuIdDo();
    entity.withId(FixtureUuId.of(TEST_UUID));
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithUuIdDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithUuIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithUuIdDo.class);
    assertEquals(TEST_UUID, marshalled.getId().unwrap());
    assertEquals(FixtureUuId.class, marshalled.getId().getClass());
  }

  @Test
  public void testSerializeDeserialize_EntityWithNullId() throws Exception {
    TestEntityWithUuIdDo entity = new TestEntityWithUuIdDo();
    entity.withId(null);
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithUuIdNullDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithUuIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithUuIdDo.class);
    assertNull(marshalled.getId());
    assertTrue(marshalled.has("id"));
  }

  @Test
  public void testDeserialize_IdRaw() throws Exception {
    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithUuIdDoRaw.json"));
    DoEntity marshalled = m_dataObjectMapper.readValue(expectedJson, DoEntity.class);
    assertEquals(TEST_UUID.toString(), marshalled.getString("id"));
    assertEquals(TEST_UUID, BEANS.get(DataObjectHelper.class).getUuidAttribute(marshalled, "id"));
  }

  @Test
  public void testSerializeDeserialize_EntityWithIdMapKey() throws Exception {
    TestEntityWithUuIdMapKeyDo entity = new TestEntityWithUuIdMapKeyDo();
    entity.withMap(Collections.singletonMap(FixtureUuId.of(TEST_UUID), "test"));
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithUuIdMapKeyDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithUuIdMapKeyDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithUuIdMapKeyDo.class);
    assertEquals(Collections.singletonMap(FixtureUuId.of(TEST_UUID), "test"), marshalled.getMap());
  }

  @Test(expected = PlatformException.class)
  public void testSerializeDeserialize_EntityWithIdMapKeyNull() throws Exception {
    TestEntityWithUuIdMapKeyDo entity = new TestEntityWithUuIdMapKeyDo();
    entity.withMap(Collections.singletonMap(null, "test"));
    m_dataObjectMapper.writeValue(entity);
  }

  protected URL toURL(String resourceName) {
    return UuIdSerializationTest.class.getResource(resourceName);
  }
}
