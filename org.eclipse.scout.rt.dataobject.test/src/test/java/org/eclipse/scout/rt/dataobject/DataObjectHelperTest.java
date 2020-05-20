/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.SimpleFixtureDo;
import org.eclipse.scout.rt.dataobject.testing.TestingDataObjectHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.After;
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
  public void testAssertValue() {
    SimpleFixtureDo testObj = BEANS.get(SimpleFixtureDo.class)
        .withId(TEST_UUID)
        .withName1("Hugo");

    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValue(null));
    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValue(testObj.createDate()));
    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValue(testObj.name2()));
    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValue((DoValue<?>) testObj.get("doesNotExist")));
    assertEquals(TEST_UUID, m_helper.assertValue(testObj.id()));
    assertEquals("Hugo", m_helper.assertValue(testObj.name1()));
  }

  @Test
  public void testAssertValueHasText() {
    SimpleFixtureDo testObj = BEANS.get(SimpleFixtureDo.class)
        .withId(TEST_UUID)
        .withName1("Hugo");

    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValueHasText(null));
    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValueHasText(testObj.name2()));
    @SuppressWarnings("unchecked")
    DoValue<String> dummyDoValue = (DoValue<String>) testObj.get("doesNotExist");
    ScoutAssert.assertThrows(AssertionException.class, () -> m_helper.assertValueHasText(dummyDoValue));
    assertEquals("Hugo", m_helper.assertValueHasText(testObj.name1()));
  }
}
