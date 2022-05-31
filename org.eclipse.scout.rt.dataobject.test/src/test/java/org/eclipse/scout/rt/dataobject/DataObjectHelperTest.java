/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.CollectionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.ListEntityContributionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.OtherEntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.SimpleFixtureDo;
import org.eclipse.scout.rt.dataobject.testing.TestingDataObjectHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class DataObjectHelperTest {

  protected static final String TEST_DATE_STRING = "2017-11-30 17:29:12.583";
  protected static final Date TEST_DATE = DateUtility.parse(TEST_DATE_STRING, IValueFormatConstants.DEFAULT_DATE_PATTERN);
  protected static final UUID TEST_UUID = UUID.randomUUID();

  protected static final String SERIALIZED_DO_ENTITY_VALUE = "Serialized_DoEntity";
  protected static final int SERIALIZED_DO_ENTITY_VALUE_BYTE = 42;
  protected static final IDoEntity DESERIALIZED_DO_ENTITY_VALUE = BEANS.get(DoEntity.class);
  protected static final IDoEntity DESERIALIZED_DO_ENTITY_VALUE_RAW = BEANS.get(DoEntity.class);

  protected IBean<TestingDataObjectHelper> m_testDataObjectHelperRegistrationBackup;
  protected IDoEntity m_entity;
  protected IDoEntity m_subEntity;
  protected DataObjectHelper m_helper;
  protected IDataObjectMapper m_dataObjectMapperMock;
  protected IBean<IDataObjectMapper> m_dataObjectMapperMockRegistration;

  @Before
  @SuppressWarnings("unchecked")
  public void before() {
    m_testDataObjectHelperRegistrationBackup = Platform.get().getBeanManager().getBean(TestingDataObjectHelper.class);
    Platform.get().getBeanManager().unregisterBean(m_testDataObjectHelperRegistrationBackup);
    // create a mock for IDataObjectMapper returning fixed values for serialized and deserialized objects
    m_dataObjectMapperMock = Mockito.mock(IDataObjectMapper.class);
    when(m_dataObjectMapperMock.readValue(Mockito.any(String.class), Mockito.any(Class.class))).thenReturn(DESERIALIZED_DO_ENTITY_VALUE);
    when(m_dataObjectMapperMock.readValueRaw(Mockito.any(String.class))).thenReturn(DESERIALIZED_DO_ENTITY_VALUE_RAW);
    when(m_dataObjectMapperMock.writeValue(Mockito.any(Object.class))).thenReturn(SERIALIZED_DO_ENTITY_VALUE);
    doAnswer(a -> {
      a.getArgument(0, OutputStream.class).write(SERIALIZED_DO_ENTITY_VALUE_BYTE);
      return null;
    }).when(m_dataObjectMapperMock).writeValue(Mockito.any(OutputStream.class), Mockito.any(Object.class));
    m_dataObjectMapperMockRegistration = Platform.get().getBeanManager().registerBean(new BeanMetaData(IDataObjectMapper.class, m_dataObjectMapperMock).withApplicationScoped(true));

    m_helper = BEANS.get(DataObjectHelper.class);
    m_entity = BEANS.get(DoEntity.class);
    m_subEntity = BEANS.get(DoEntity.class);
    m_subEntity.put("name", "subEntity");

    m_entity.put("integer", 42);
    m_entity.put("double", 12.34);
    m_entity.put("bigInteger", new BigInteger("420"));
    m_entity.put("date", TEST_DATE);
    m_entity.put("dateString", TEST_DATE_STRING);
    m_entity.put("dateInvalid", "abc");
    m_entity.put("uuid", TEST_UUID);
    m_entity.put("uuidString", TEST_UUID.toString());
    m_entity.put("uuidInvalid", BigInteger.TEN);
    m_entity.put("locale", Locale.GERMANY);
    m_entity.put("localeStringGER", "de-DE");
    m_entity.put("localeStringITA", Locale.ITALY.toLanguageTag());
    m_entity.put("localeInvalid", BigDecimal.ONE);
    m_entity.put("entity", m_subEntity);
  }

  @After
  public void after() {
    Platform.get().getBeanManager().registerBean(new BeanMetaData(m_testDataObjectHelperRegistrationBackup));
    Platform.get().getBeanManager().unregisterBean(m_dataObjectMapperMockRegistration);
  }

  @Test
  public void testGetIntegerAttribute() {
    assertEquals(Integer.valueOf(42), m_helper.getIntegerAttribute(m_entity, "integer"));
    assertNull(m_helper.getIntegerAttribute(m_entity, "foo"));
  }

  @Test
  public void testGetDoubleAttribute() {
    assertEquals(Double.valueOf(12.34), m_helper.getDoubleAttribute(m_entity, "double"));
    assertNull(m_helper.getDoubleAttribute(m_entity, "foo"));
  }

  @Test
  public void testGetBigIntegerAttribute() {
    assertEquals(new BigInteger("420"), m_helper.getBigIntegerAttribute(m_entity, "bigInteger"));
    assertNull(m_helper.getBigIntegerAttribute(m_entity, "foo"));
  }

  @Test
  public void testGetDateAttribute() {
    assertEquals(TEST_DATE, m_helper.getDateAttribute(m_entity, "date"));
    assertEquals(TEST_DATE, m_helper.getDateAttribute(m_entity, "dateString"));
    assertNull(m_helper.getDateAttribute(m_entity, "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDateAttributeIllegalFormat() {
    m_helper.getDateAttribute(m_entity, "dateInvalid");
  }

  @Test
  public void testGetUuidAttribute() {
    assertEquals(TEST_UUID, m_helper.getUuidAttribute(m_entity, "uuid"));
    assertEquals(TEST_UUID, m_helper.getUuidAttribute(m_entity, "uuidString"));
    assertNull(m_helper.getUuidAttribute(m_entity, "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUuidAttributeIllegalFormat() {
    m_helper.getUuidAttribute(m_entity, "uuidInvalid");
  }

  @Test
  public void testGetLocaleAttribute() {
    assertEquals(Locale.GERMANY, m_helper.getLocaleAttribute(m_entity, "locale"));
    assertEquals(Locale.GERMANY, m_helper.getLocaleAttribute(m_entity, "localeStringGER"));
    assertEquals(Locale.ITALY, m_helper.getLocaleAttribute(m_entity, "localeStringITA"));
    assertNull(m_helper.getLocaleAttribute(m_entity, "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetLocaleAttributeIllegalFormat() {
    m_helper.getLocaleAttribute(m_entity, "localeInvalid");
  }

  @Test
  public void testGetEntityAttribute() {
    assertEquals(m_subEntity, m_helper.getEntityAttribute(m_entity, "entity"));
    assertNull(m_helper.getEntityAttribute(m_entity, "foo"));
    assertEquals("subEntity", m_helper.getEntityAttribute(m_entity, "entity").getString("name"));
  }

  @Test
  public void testClone() {
    assertNull(m_helper.clone(null));
    DoEntity clone = m_helper.clone(BEANS.get(DoEntity.class));
    assertSame(DESERIALIZED_DO_ENTITY_VALUE, clone);
  }

  @Test
  public void testCloneRaw() {
    assertNull(m_helper.cloneRaw(null));
    IDoEntity clone = m_helper.cloneRaw(BEANS.get(DoEntity.class));
    assertSame(DESERIALIZED_DO_ENTITY_VALUE_RAW, clone);
  }

  @Test
  public void testToString() {
    assertEquals("null", m_helper.toString(null));
    assertEquals(SERIALIZED_DO_ENTITY_VALUE, m_helper.toString(BEANS.get(DoEntity.class)));
  }

  @Test
  public void testToBytes() {
    assertArrayEquals(new byte[]{}, m_helper.toBytes(null));
    assertEquals(SERIALIZED_DO_ENTITY_VALUE_BYTE, m_helper.toBytes(BEANS.get(DoEntity.class))[0]);
  }

  @Test
  public void testAssertValue() {
    SimpleFixtureDo testObj = BEANS.get(SimpleFixtureDo.class)
        .withId(TEST_UUID)
        .withName1("Hugo");

    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValue(null));
    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValue(testObj.createDate()));
    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValue(testObj.name2()));
    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValue((DoValue<?>) testObj.get("doesNotExist")));
    assertEquals(TEST_UUID, m_helper.assertValue(testObj.id()));
    assertEquals("Hugo", m_helper.assertValue(testObj.name1()));
  }

  @Test
  public void testAssertValueHasText() {
    SimpleFixtureDo testObj = BEANS.get(SimpleFixtureDo.class)
        .withId(TEST_UUID)
        .withName1("Hugo");

    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValueHasText(null));
    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValueHasText(testObj.name2()));
    @SuppressWarnings("unchecked")
    DoValue<String> dummyDoValue = (DoValue<String>) testObj.get("doesNotExist");
    Assert.assertThrows(AssertionException.class, () -> m_helper.assertValueHasText(dummyDoValue));
    assertEquals("Hugo", m_helper.assertValueHasText(testObj.name1()));
  }

  @Test
  public void testCleanValue() {
    SimpleFixtureDo testObj = BEANS.get(SimpleFixtureDo.class)
        .withId(null)
        .withCreateDate(null);

    assertTrue(testObj.id().exists());
    assertTrue(testObj.createDate().exists());
    m_helper.clean(testObj);
    assertFalse(testObj.id().exists());
    assertFalse(testObj.createDate().exists());
  }

  @Test
  public void testCleanList() {
    Map<String, OtherEntityFixtureDo> otherEntitiesMap = new HashMap<>();
    otherEntitiesMap.put("key", BEANS.get(OtherEntityFixtureDo.class)
        .withNestedOtherEntity(BEANS.get(OtherEntityFixtureDo.class))
        .withItems(Collections.emptyList()));

    EntityFixtureDo testObj = BEANS.get(EntityFixtureDo.class)
        .withOtherEntities(Collections.emptyList())
        .withOtherEntitiesMap(otherEntitiesMap)
        .withOtherEntitiesList(Collections.emptyList())
        .withOtherEntity(BEANS.get(OtherEntityFixtureDo.class).withItems(Collections.emptyList()));

    assertTrue(testObj.getOtherEntitiesMap().get("key").items().exists());
    assertTrue(testObj.otherEntities().exists());
    assertTrue(testObj.otherEntitiesList().exists());
    assertTrue(testObj.getOtherEntity().items().exists());
    m_helper.clean(testObj);
    assertFalse(testObj.getOtherEntitiesMap().get("key").items().exists());
    assertFalse(testObj.otherEntities().exists());
    assertFalse(testObj.getOtherEntity().items().exists());
    assertTrue(testObj.otherEntitiesList().exists()); // DoValue<Collection> is not cleaned
  }

  @Test
  public void testCleanCollection() {
    CollectionFixtureDo testObj = BEANS.get(CollectionFixtureDo.class)
        .withSimpleDoCollection(Collections.emptyList())
        .withSimpleDoSet(Collections.emptySet());

    assertTrue(testObj.simpleDoCollection().exists());
    assertTrue(testObj.simpleDoSet().exists());
    m_helper.clean(testObj);
    assertFalse(testObj.simpleDoCollection().exists());
    assertFalse(testObj.simpleDoSet().exists());
  }

  @Test
  public void testCleanDataObject() {
    IDataObject testObj = DoList.of(
        List.of(BEANS.get(SimpleFixtureDo.class)
            .withId(null)
            .withCreateDate(null)));

    assertTrue(((SimpleFixtureDo) ((DoList) testObj).get(0)).id().exists());
    assertTrue(((SimpleFixtureDo) ((DoList) testObj).get(0)).createDate().exists());
    m_helper.clean(testObj);
    assertFalse(((SimpleFixtureDo) ((DoList) testObj).get(0)).id().exists());
    assertFalse(((SimpleFixtureDo) ((DoList) testObj).get(0)).createDate().exists());
  }

  @Test
  public void testCleanContributions() {
    OtherEntityFixtureDo testObj = BEANS.get(OtherEntityFixtureDo.class);
    testObj.putContribution(BEANS.get(ListEntityContributionFixtureDo.class)
        .withId(null)
        .withEntities(Collections.emptyList()));

    assertTrue(testObj.getContribution(ListEntityContributionFixtureDo.class).id().exists());
    assertTrue(testObj.getContribution(ListEntityContributionFixtureDo.class).entities().exists());
    m_helper.clean(testObj);
    assertFalse(testObj.getContribution(ListEntityContributionFixtureDo.class).id().exists());
    assertFalse(testObj.getContribution(ListEntityContributionFixtureDo.class).entities().exists());
  }

  @Test
  public void testCleanContributionsNestedEntity() {
    EntityFixtureDo testObj = BEANS.get(EntityFixtureDo.class);
    testObj.putContribution(BEANS.get(ListEntityContributionFixtureDo.class)
        .withId(null)
        .withEntities(BEANS.get(EntityFixtureDo.class).withOtherEntities(Collections.emptyList())));

    assertTrue(testObj.getContribution(ListEntityContributionFixtureDo.class).id().exists());
    assertTrue(testObj.getContribution(ListEntityContributionFixtureDo.class).entities().exists());
    assertTrue(testObj.getContribution(ListEntityContributionFixtureDo.class).getEntities().get(0).otherEntities().exists());
    m_helper.clean(testObj);
    assertFalse(testObj.getContribution(ListEntityContributionFixtureDo.class).id().exists());
    assertTrue(testObj.getContribution(ListEntityContributionFixtureDo.class).entities().exists());
    assertFalse(testObj.getContribution(ListEntityContributionFixtureDo.class).getEntities().get(0).otherEntities().exists());
  }

  @Test
  public void testTruncateStringValue() {
    m_helper.truncateStringValue(null, 10);
    Assert.assertThrows(AssertionException.class, () -> m_helper.truncateStringValue(null, -1));
    Assert.assertThrows(AssertionException.class, () -> m_helper.truncateStringValue(null, 0));

    DoValue<String> nullValue = DoValue.of(null);
    m_helper.truncateStringValue(nullValue, 10);
    assertNull(nullValue.get());

    DoValue<String> value = DoValue.of("test");
    m_helper.truncateStringValue(value, 10);
    assertEquals("test", value.get());

    m_helper.truncateStringValue(value, 4);
    assertEquals("test", value.get());

    m_helper.truncateStringValue(value, 3);
    assertEquals("tes", value.get());

    // test with entity
    FixtureDoEntity entity = BEANS.get(FixtureDoEntity.class);
    assertFalse(entity.name().exists());

    m_helper.truncateStringValue(entity.name(), 10);
    assertFalse(entity.name().exists());
    assertNull(entity.getName());

    entity.withName("test");
    assertTrue(entity.name().exists());

    m_helper.truncateStringValue(entity.name(), 4);
    assertEquals("test", entity.getName());

    m_helper.truncateStringValue(entity.name(), 3);
    assertEquals("tes", entity.getName());
  }

  @Test
  public void testEnsureValue() {
    m_helper.ensureValue(null, null);

    DoValue<String> value = DoValue.of(null);
    m_helper.ensureValue(value, null);
    assertNull(value.get());

    m_helper.ensureValue(value, "test");
    assertEquals("test", value.get());

    m_helper.ensureValue(value, "other value");
    assertEquals("test", value.get());

    // test with entity
    FixtureDoEntity entity = BEANS.get(FixtureDoEntity.class);
    assertFalse(entity.name().exists());

    m_helper.ensureValue(entity.name(), null);
    assertTrue(entity.name().exists());
    assertNull(entity.getName());

    m_helper.ensureValue(entity.name(), "test");
    assertEquals("test", entity.getName());

    m_helper.ensureValue(entity.name(), "other value");
    assertEquals("test", entity.getName());
  }

  @Test
  public void testSupplyValue() {
    m_helper.supplyValue(null, null);

    DoValue<String> value = DoValue.of(null);
    m_helper.supplyValue(value, () -> null);
    assertNull(value.get());

    m_helper.supplyValue(value, () -> "test");
    assertEquals("test", value.get());

    m_helper.supplyValue(value, () -> "other value");
    assertEquals("test", value.get());

    // test with entity
    FixtureDoEntity entity = BEANS.get(FixtureDoEntity.class);
    assertFalse(entity.name().exists());

    m_helper.supplyValue(entity.name(), null);
    assertFalse(entity.name().exists());
    assertNull(entity.getName());

    m_helper.supplyValue(entity.name(), () -> null);
    assertTrue(entity.name().exists());
    assertNull(entity.getName());

    m_helper.supplyValue(entity.name(), () -> "test");
    assertEquals("test", entity.getName());

    m_helper.supplyValue(entity.name(), () -> "other value");
    assertEquals("test", entity.getName());
  }

  @Test
  public void testEnsureDeclaredNodes() {
    EntityFixtureDo testObj = BEANS.get(EntityFixtureDo.class);
    assertFalse(testObj.id().exists());
    assertFalse(testObj.otherEntitiesList().exists());
    assertFalse(testObj.otherEntities().exists());
    assertFalse(testObj.otherEntitiesMap().exists());

    EntityFixtureDo testObjEnsuredNodes = m_helper.ensureDeclaredNodes(testObj);
    assertEquals(testObj, testObjEnsuredNodes);
    assertTrue(testObjEnsuredNodes.id().exists());
    assertTrue(testObjEnsuredNodes.otherEntitiesList().exists());
    assertTrue(testObjEnsuredNodes.otherEntities().exists());
    assertTrue(testObjEnsuredNodes.otherEntitiesMap().exists());
  }

  @Test
  public void testEnsureDeclaredNodesDefaultValues() {
    CollectionFixtureDo testObj = BEANS.get(CollectionFixtureDo.class);
    CollectionFixtureDo testObjEnsuredNodes = m_helper.ensureDeclaredNodes(testObj);
    assertEquals(CollectionUtility.emptyHashSet(), testObjEnsuredNodes.getSimpleDoSet());
    assertEquals(CollectionUtility.emptyArrayList(), testObjEnsuredNodes.getSimpleDoCollection());

    EntityFixtureDo testObj2 = BEANS.get(EntityFixtureDo.class);
    EntityFixtureDo testObj2EnsuredNodes = m_helper.ensureDeclaredNodes(testObj2);
    assertNull(testObj2EnsuredNodes.getId());
    assertEquals(CollectionUtility.emptyArrayList(), testObj2EnsuredNodes.getOtherEntities());
    assertNull(testObj2EnsuredNodes.getOtherEntitiesList());
    assertNull(testObj2EnsuredNodes.getOtherEntitiesMap());
  }

  @Test
  public void testExtendInvalidInvocations() {
    Assert.assertThrows(AssertionException.class, () -> m_helper.extend(null, null));
    Assert.assertThrows(AssertionException.class, () -> m_helper.extend(null, new DoEntity()));
  }

  @Test
  public void testExtendNullOrEmptyTemplate() {
    // null and empty template
    DoEntity entity = new DoEntity();
    assertSame(entity, m_helper.extend(entity, null));
    assertEquals(0, entity.allNodes().size());

    assertSame(entity, m_helper.extend(entity, new DoEntity()));
    assertEquals(0, entity.allNodes().size());
  }

  @Test
  public void testExtendEitherAllAvailableOrAllMissing() {
    final IDoEntity target = BEANS.get(DoEntityBuilder.class)
        .put("nullAttribute", null)
        .put("stringAttribute", "s")
        .put("numberAttribute", 104L)
        .putList("nullList", (List<?>) null)
        .putList("stringList", Arrays.asList("a", "b", "c"))
        .build();

    final IDoEntity template = BEANS.get(DoEntityBuilder.class)
        .put("nullAttribute", "non-null")
        .put("stringAttribute", "t")
        .put("numberAttribute", 42L)
        .putList("nullList", Arrays.asList("a", "b", "c"))
        .putList("stringList", Arrays.asList("x", "y", "z"))
        .build();

    final IDoEntity targetExtended = m_helper.extend(target, template);
    assertSame(target, targetExtended);

    // check that JSON content did not change
    assertNull(targetExtended.get("nullAttribute"));
    assertEquals("s", targetExtended.get("stringAttribute"));
    assertEquals(104L, targetExtended.get("numberAttribute"));
    assertEquals(Arrays.asList(), targetExtended.getList("nullList"));
    assertEquals(Arrays.asList("a", "b", "c"), targetExtended.getList("stringList"));
    assertEquals(5, targetExtended.all().size());

    IDoEntity empty = new DoEntity();
    final IDoEntity extendedEmpty = m_helper.extend(empty, template);
    assertSame(empty, extendedEmpty);

    // check that empty JSON content is extended
    assertEquals(template, extendedEmpty);
  }

  @Test
  public void testExtend() {
    final IDoEntity target = BEANS.get(DoEntityBuilder.class)
        .put("stringAttribute", "s")
        .putList("stringList", Arrays.asList("a", "b", "c"))
        .build();

    final IDoEntity template = BEANS.get(DoEntityBuilder.class)
        .put("nullAttribute", null)
        .put("stringAttribute", "t")
        .put("otherAttribute", "t")
        .putList("nullList", (List<?>) null)
        .putList("stringList", Arrays.asList("x", "y", "z"))
        .putList("otherList", Arrays.asList("x", "y", "z"))
        .build();

    final IDoEntity targetExtended = m_helper.extend(target, template);
    assertSame(target, targetExtended);

    // check JSON content
    final IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("stringAttribute", "s")
        .putList("stringList", Arrays.asList("a", "b", "c"))
        .put("nullAttribute", null)
        .put("otherAttribute", "t")
        .putList("nullList", (List<?>) null)
        .putList("otherList", Arrays.asList("x", "y", "z"))
        .build();

    assertEquals(expected, target);
    assertEquals(expected, targetExtended);
  }

  @Test
  public void testExtendAllDoNodeTypesAllMissing() {
    final IDoEntity target = BEANS.get(DoEntityBuilder.class).build();

    final IDoEntity template = BEANS.get(DoEntityBuilder.class)
        .put("stringAttribute", "t")
        .putList("stringList", Arrays.asList("x", "y", "z")).build();
    template.putSet("stringSet", CollectionUtility.hashSet("a", "b", "c"));
    template.putCollection("stringCollection", Arrays.asList("u", "v", "w"));

    final IDoEntity targetExtended = m_helper.extend(target, template);
    assertSame(target, targetExtended);

    assertEquals(template, target);
    assertEquals(template, targetExtended);
  }

  @Test
  public void testExtendAllDoNodeTypesAllExisting() {
    final IDoEntity target = BEANS.get(DoEntityBuilder.class)
        .put("stringAttribute", "t")
        .putList("stringList", Arrays.asList("x", "y", "z")).build();
    target.putSet("stringSet", CollectionUtility.hashSet("a", "b", "c"));
    target.putCollection("stringCollection", Arrays.asList("u", "v", "w"));

    final IDoEntity template = BEANS.get(DoEntityBuilder.class)
        .put("stringAttribute", "t2")
        .putList("stringList", Arrays.asList("x2", "y2", "z2")).build();
    target.putSet("stringSet", CollectionUtility.hashSet("a2", "b2", "c2"));
    target.putCollection("stringCollection", Arrays.asList("u2", "v2", "w2"));

    final IDoEntity targetExtended = m_helper.extend(target, template);
    assertSame(target, targetExtended);
    assertEquals(target, targetExtended);
    assertNotEquals(template, targetExtended);
  }

  @Test(expected = AssertionException.class)
  public void testExtendUnexpectedNodeType() {
    final IDoEntity target = BEANS.get(DoEntityBuilder.class).build();

    final IDoEntity template = BEANS.get(DoEntity.class);
    template.putNode("foo", new DoNode<>(null, null, null));

    m_helper.extend(target, template);
  }

  public static class FixtureDoEntity extends DoEntity {

    public DoValue<String> name() {
      return doValue("name");
    }

    public FixtureDoEntity withName(String name) {
      name().set(name);
      return this;
    }

    public String getName() {
      return name().get();
    }
  }
}
