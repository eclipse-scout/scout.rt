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

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithTypedIdDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Before;
import org.junit.Test;

public class TypedIdSerializationTest {

  protected static final FixtureStringId STRING_ID = FixtureStringId.of("foo");
  protected static final FixtureUuId UU_ID = FixtureUuId.of("8fa211b0-fd83-42cb-96c9-1942e274ce79");

  protected DataObjectSerializationTestHelper m_testHelper;
  protected IDataObjectMapper m_dataObjectMapper;

  @Before
  public void before() {
    m_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
    m_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
  }

  @Test
  public void testSerializeDeserialize_EntityWithTypedId() throws Exception {
    TestEntityWithTypedIdDo entity = BEANS.get(TestEntityWithTypedIdDo.class);
    entity.withStringId(TypedId.of(STRING_ID));
    entity.withUuId(TypedId.of(UU_ID));
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithTypedIdDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithTypedIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithTypedIdDo.class);
    assertEquals(STRING_ID, marshalled.getStringId().getId());
    assertEquals(UU_ID, marshalled.getUuId().getId());
  }

  @Test
  public void testSerializeDeserialize_EntityWithTypedCompositeId() throws Exception {
    FixtureCompositeId c = FixtureCompositeId.of(STRING_ID, UU_ID);
    TestEntityWithTypedIdDo entity = BEANS.get(TestEntityWithTypedIdDo.class)
        .withIid(TypedId.of(c));
    String json = m_dataObjectMapper.writeValue(entity);

    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithTypedCompositeIdDo.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithTypedIdDo marshalled = m_dataObjectMapper.readValue(expectedJson, TestEntityWithTypedIdDo.class);
    assertEquals(c, marshalled.getIid().getId());
  }

  protected URL toURL(String resourceName) {
    return TypedIdSerializationTest.class.getResource(resourceName);
  }
}
