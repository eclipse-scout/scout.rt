/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.platform.dataobject.TestingDataObjectHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
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
  protected static final DoEntity DESERIALIZED_DO_ENTITY_VALUE = BEANS.get(DoEntity.class);

  protected IBean<TestingDataObjectHelper> m_testDataObjectHelperRegistrationBackup;
  protected DoEntity m_entity;
  protected DoEntity m_subEntity;
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
    when(m_dataObjectMapperMock.writeValue(Mockito.any(Object.class))).thenReturn(SERIALIZED_DO_ENTITY_VALUE);
    m_dataObjectMapperMockRegistration = Platform.get().getBeanManager().registerBean(new BeanMetaData(IDataObjectMapper.class, m_dataObjectMapperMock).withApplicationScoped(true));

    m_helper = BEANS.get(DataObjectHelper.class);
    m_entity = new DoEntity();
    m_subEntity = new DoEntity();
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
    assertEquals(DESERIALIZED_DO_ENTITY_VALUE, clone);
  }

  @Test
  public void testToString() {
    assertEquals("null", m_helper.toString(null));
    assertEquals(SERIALIZED_DO_ENTITY_VALUE, m_helper.toString(BEANS.get(DoEntity.class)));
  }
}
