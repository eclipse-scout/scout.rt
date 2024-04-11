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
import java.util.Map;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.ILenientDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.eclipse.scout.rt.dataobject.id.UnknownId;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithIIdDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
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
  protected IDataObjectMapper m_lenientDataObjectMapper;

  @Before
  public void before() {
    m_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
    m_lenientDataObjectMapper = BEANS.get(ILenientDataObjectMapper.class);
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
  public void testDeserialize_EntityWithUnqualifiedIId() throws Exception {
    String json = m_testHelper.readResourceAsString(toURL("TestEntityWithIIdUnqualified2Do.json"));

    // IId serialized as unqualified iid is not deserializable (IId class type is not available)
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));
  }

  @Test
  public void testSerializeDeserialize_EntityWithQualifiedIId() throws Exception {
    TestEntityWithIIdDo entity = BEANS.get(TestEntityWithIIdDo.class).withIid(COMPOSITE_ID_1);
    String json = m_dataObjectMapper.writeValue(entity);
    String expectedJson = m_testHelper.readResourceAsString(toURL("TestEntityWithIIdQualified2Do.json"));
    m_testHelper.assertJsonEquals(expectedJson, json);

    TestEntityWithIIdDo actual = m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    assertEquals(actual.getIid(), COMPOSITE_ID_1);
  }

  /**
   * Using a valid {@link #UU_ID} ({@link FixtureUuId}) as {@link IUuId}.
   */
  @Test
  public void testSerializeDeserialize_EntityWithCorrectlyTypedQualifiedId() {
    String json = m_dataObjectMapper.writeValue(BEANS.get(DoEntityBuilder.class)
        .put("iUuId", "scout.FixtureUuId:8fa211b0-fd83-42cb-96c9-1942e274ce79")
        .build());

    TestEntityWithIIdDo entity = m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    assertEquals(UU_ID, entity.getIUuId());

    json = m_dataObjectMapper.writeValue(BEANS.get(DoEntityBuilder.class)
        .put("iUuIdMap", Collections.singletonMap("scout.FixtureUuId:8fa211b0-fd83-42cb-96c9-1942e274ce79", "test"))
        .build());

    entity = m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    assertEquals(1, entity.getIUuIdMap().size());
    assertEquals("test", entity.getIUuIdMap().get(UU_ID));
  }

  /**
   * Using an invalid {@link #STRING_1_ID} ({@link FixtureStringId}) as {@link IUuId}.
   */
  @Test
  public void testSerializeDeserialize_EntityWithWronglyTypedQualifiedId() {
    String json = m_dataObjectMapper.writeValue(BEANS.get(DoEntityBuilder.class)
        .put("iUuId", "scout.FixtureStringId:foo")
        .build());

    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    String mapJson = m_dataObjectMapper.writeValue(BEANS.get(DoEntityBuilder.class)
        .put("iUuIdMap", Collections.singletonMap("scout.FixtureStringId:foo", "test"))
        .build());

    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(mapJson, TestEntityWithIIdDo.class));
  }

  @Test
  public void testDeserializeInvalidUnqualifiedId() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"uuId\" : \"a;b;c\" }"; // composite-id format for non-composite id
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient.getUuId());
    assertEquals("a;b;c", marshalledLenient.getString("uuId"));
  }

  @Test
  public void testDeserializeInvalidUnqualifiedIdMapKey() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"map\" : { \"a;b;c\" : \"value\" } }"; // composite-id format for non-composite id
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    Map<FixtureStringId, String> map = marshalledLenient.getMap(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(1, map.size());
    //noinspection AssertBetweenInconvertibleTypes
    assertEquals("a;b;c", CollectionUtility.firstElement(map.keySet())); // string is returned as key because lenient data object mapper is used
    assertEquals("value", CollectionUtility.firstElement(map.values()));
  }

  @Test
  public void testDeserializeInvalidQualifiedId() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"iid\" : \"scout.unknown:unknown\" }";
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    //noinspection deprecation
    assertEquals(UnknownId.of("scout.unknown","unknown"), marshalledLenient.getIid());
  }

  @Test
  public void testDeserializeMissingQualifiedIdFormat() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"iid\" : \"unknown\" }";
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient.getIid());
    assertEquals("unknown", marshalledLenient.getString("iid"));
  }

  @Test
  public void testDeserializeInvalidQualifiedIdMapKey() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"iUuIdMap\" : { \"scout.unknown:unknown\" : \"value\" } }";
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    Map<IUuId, String> map = marshalledLenient.getIUuIdMap(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(1, map.size());
    //noinspection AssertBetweenInconvertibleTypes
    assertEquals("scout.unknown:unknown", CollectionUtility.firstElement(map.keySet())); // string is returned as key because lenient data object mapper is used
    assertEquals("value", CollectionUtility.firstElement(map.values()));
  }

  @Test
  public void testDeserializeInvalidQualifiedIdMapKeyWithValidDo() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"iUuIdDoMap\" : { \"scout.unknown:unknown\" : { \"_type\" : \"TestItem\", \"id\" : \"1\" } } }";
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    Map<IUuId, TestItemDo> map = marshalledLenient.getIUuIdDoMap(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(1, map.size());
    //noinspection AssertBetweenInconvertibleTypes
    assertEquals("scout.unknown:unknown", CollectionUtility.firstElement(map.keySet())); // string is returned as key because lenient data object mapper is used
    assertEquals(BEANS.get(TestItemDo.class).withId("1"), CollectionUtility.firstElement(map.values())); // typed
  }

  @Test
  public void testDeserializeValidQualifiedIdMapKeyWithInvalidDo() {
    String json = "{\"_type\" : \"scout.TestEntityWithIId\", \"iUuIdDoMap\" : { \"scout.FixtureUuId:e7a8792f-7ed1-48a6-9d14-5ebbfd1d00ff\" : { \"_type\" : \"unknown\", \"id\" : \"1\" } } }";
    assertThrows(PlatformException.class, () -> m_dataObjectMapper.readValue(json, TestEntityWithIIdDo.class));

    TestEntityWithIIdDo marshalledLenient = m_lenientDataObjectMapper.readValue(json, TestEntityWithIIdDo.class);
    Map<IUuId, TestItemDo> map = marshalledLenient.getIUuIdDoMap(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(1, map.size());
    assertEquals(FixtureUuId.of("e7a8792f-7ed1-48a6-9d14-5ebbfd1d00ff"), CollectionUtility.firstElement(map.keySet()));
    DoEntity untypedDoEntity = CollectionUtility.firstElement(map.values()); // unknown -> untyped
    assertEquals("unknown", untypedDoEntity.getString("_type"));
    assertEquals("1", untypedDoEntity.getString("id"));
  }

  protected URL toURL(String resourceName) {
    return IIdSerializationTest.class.getResource(resourceName);
  }
}
