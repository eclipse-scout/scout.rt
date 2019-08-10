/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collections;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithStringIdDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithStringIdMapKeyDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Test;

public class StringIdSerializationTest {

  protected DataObjectSerializationTestHelper m_testHelper;
  protected IDataObjectMapper m_dataObjectMapper;

  protected static final String TEST_ID = "aaabbbccc";

  @Before
  public void before() {
    m_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
    m_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
  }

  @Test
  public void testSerializeDeserialize_EntityWithId() throws Exception {
    TestEntityWithStringIdDo entity = new TestEntityWithStringIdDo();
    entity.withId(FixtureStringId.of(TEST_ID));
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithStringIdDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithStringIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithStringIdDo.class);
    assertEquals(TEST_ID, marshalled.getId().unwrap());
    assertEquals(FixtureStringId.class, marshalled.getId().getClass());
  }

  @Test
  public void testSerializeDeserialize_EntityWithNullId() throws Exception {
    TestEntityWithStringIdDo entity = new TestEntityWithStringIdDo();
    entity.withId(null);
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithStringIdNullDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithStringIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithStringIdDo.class);
    assertNull(marshalled.getId());
    assertTrue(marshalled.has("id"));
  }

  @Test
  public void testDeserialize_IdRaw() throws Exception {
    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithStringIdDoRaw.json"));
    DoEntity marshalled = m_dataObjectMapper.readValue(expectedJson, DoEntity.class);
    assertEquals(TEST_ID, marshalled.getString("id"));
  }

  @Test
  public void testSerializeDeserialize_EntityWithIdMapKey() throws Exception {
    TestEntityWithStringIdMapKeyDo entity = new TestEntityWithStringIdMapKeyDo();
    entity.withMap(Collections.singletonMap(FixtureStringId.of(TEST_ID), "test"));
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithStringIdMapKeyDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithStringIdMapKeyDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithStringIdMapKeyDo.class);
    assertEquals(Collections.singletonMap(FixtureStringId.of(TEST_ID), "test"), marshalled.getMap());
  }

  @Test(expected = PlatformException.class)
  public void testSerializeDeserialize_EntityWithIdMapKeyNull() throws Exception {
    TestEntityWithStringIdMapKeyDo entity = new TestEntityWithStringIdMapKeyDo();
    entity.withMap(Collections.singletonMap(null, "test"));
    m_dataObjectMapper.writeValue(entity);
  }

  protected URL toURL(String resourceName) {
    return StringIdSerializationTest.class.getResource(resourceName);
  }
}
