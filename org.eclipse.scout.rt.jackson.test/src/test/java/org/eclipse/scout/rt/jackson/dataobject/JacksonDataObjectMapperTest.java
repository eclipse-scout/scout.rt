/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.testing.TestingDataObjectHelper;
import org.eclipse.scout.rt.jackson.dataobject.fixture.ITestBaseEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCustomImplementedEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithInterface1Do;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CloneUtility;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Various test cases with requires a real jackson serializer/deserializer for testing
 */
public class JacksonDataObjectMapperTest {

  protected IBean<TestingDataObjectHelper> m_testDataObjectHelperRegistrationBackup;
  protected DataObjectSerializationTestHelper m_testHelper;
  protected JacksonDataObjectMapper m_mapper;

  @Before
  public void before() {
    m_testDataObjectHelperRegistrationBackup = Platform.get().getBeanManager().getBean(TestingDataObjectHelper.class);
    Platform.get().getBeanManager().unregisterBean(m_testDataObjectHelperRegistrationBackup);
    m_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
    m_mapper = BEANS.get(JacksonDataObjectMapper.class);
  }

  @After
  public void after() {
    Platform.get().getBeanManager().registerBean(new BeanMetaData(m_testDataObjectHelperRegistrationBackup));
  }

  @Test
  public void testReadWriteValue() {
    assertNull(m_mapper.writeValue(null));
    assertNull(m_mapper.readValue((String) null, null));
    assertNull(m_mapper.readValue((String) null, Object.class));

    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("foo", "bar");
    entity.put("baz", 42);
    String json = m_mapper.writeValue(entity);
    DoEntity parsedEntity = m_mapper.readValue(json, DoEntity.class);
    String jsonParsedEntity = m_mapper.writeValue(parsedEntity);
    m_testHelper.assertJsonEquals(json, jsonParsedEntity);
  }

  @Test(expected = AssertionException.class)
  public void testReadValueWithNullInputStream() {
    m_mapper.readValue((InputStream) null, Object.class);
  }

  @Test(expected = IllegalArgumentException.class) // thrown by Jackson
  public void testReadValueWithNullValueType() {
    m_mapper.readValue(new ByteArrayInputStream(new byte[0]), null);
  }

  @Test(expected = AssertionException.class)
  public void testWriteValueWithNullOutputStream() {
    m_mapper.writeValue(null, null);
  }

  @Test
  public void testReadWriteValueWithStreams() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    m_mapper.writeValue(bos, null);
    assertEquals(0, bos.toByteArray().length);

    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("foo", "bar");
    entity.put("baz", 42);
    ByteArrayOutputStream expected = new ByteArrayOutputStream();
    m_mapper.writeValue(expected, entity);
    DoEntity parsedEntity = m_mapper.readValue(new ByteArrayInputStream(expected.toByteArray()), DoEntity.class);
    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    m_mapper.writeValue(actual, parsedEntity);
    assertArrayEquals(expected.toByteArray(), actual.toByteArray());
  }

  @Test(expected = PlatformException.class)
  public void testWriteValueException() {
    m_mapper.writeValue(new Object());
  }

  @Test(expected = PlatformException.class)
  public void testReadValueException() {
    m_mapper.readValue("{\"foo\" : 1}", BigDecimal.class);
  }

  @Test
  public void testToString() {
    // register DataObjectMapper using default (non-testing) JacksonDataObjectMapper implementation of IDataObjectMapper
    List<IBean<?>> registeredBeans = BEANS.get(BeanTestingHelper.class).registerBeans(
        new BeanMetaData(DataObjectHelper.class).withOrder(1).withApplicationScoped(true),
        new BeanMetaData(IDataObjectMapper.class).withOrder(1).withApplicationScoped(true).withInitialInstance(new JacksonDataObjectMapper()));
    try {
      DoEntity entity = BEANS.get(DoEntity.class);
      entity.put("stringAttribute", "foo");
      entity.put("intAttribute", 42);
      entity.putList("listAttribute", Arrays.asList(1, 2, 3));
      assertEquals("DoEntity {\"intAttribute\":42,\"listAttribute\":[1,2,3],\"stringAttribute\":\"foo\"}", entity.toString());
    }
    finally {
      BEANS.get(BeanTestingHelper.class).unregisterBeans(registeredBeans);
    }
  }

  @Test
  public void testCloneDoEntity() throws Exception {
    DoEntityHolder<DoEntity> holder = new DoEntityHolder<>();
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("foo", "bar");
    entity.put("42", new BigDecimal("1234.56"));
    holder.setValue(entity);

    DoEntityHolder<DoEntity> holderClone = CloneUtility.createDeepCopyBySerializing(holder);
    assertEqualsWithComparisonFailure(entity, holderClone.getValue());
  }

  @Test
  public void testCloneComplexDoEntity() throws Exception {
    TestComplexEntityDo testDo = BEANS.get(TestComplexEntityDo.class);
    testDo.id().set("4d2abc01-afc0-49f2-9eee-a99878d49728");
    testDo.stringAttribute().set("foo");
    testDo.integerAttribute().set(42);
    testDo.longAttribute().set(123L);
    testDo.floatAttribute().set(12.34f);
    testDo.doubleAttribute().set(56.78);
    testDo.bigDecimalAttribute().set(new BigDecimal("1.23456789"));
    testDo.bigIntegerAttribute().set(new BigInteger("123456789"));
    testDo.dateAttribute().set(new Date(123456789));
    testDo.objectAttribute().set("fooObject");
    testDo.withUuidAttribute(UUID.fromString("298d64f9-821d-49fe-91fb-6fb9860d4950"));
    testDo.withLocaleAttribute(Locale.forLanguageTag("de-CH"));

    DoEntityHolder<TestComplexEntityDo> holder = new DoEntityHolder<>();
    holder.setValue(testDo);

    DoEntityHolder<TestComplexEntityDo> holderClone = CloneUtility.createDeepCopyBySerializing(holder);
    assertEqualsWithComparisonFailure(testDo, holderClone.getValue());
  }

  @Test
  public void testCloneDoEntityWithInterface() throws Exception {
    DoEntityHolder<ITestBaseEntityDo> holder = new DoEntityHolder<>();
    holder.setValue(BEANS.get(TestEntityWithInterface1Do.class));
    holder.getValue().stringAttribute().set("foo");

    DoEntityHolder<ITestBaseEntityDo> holderClone = CloneUtility.createDeepCopyBySerializing(holder);
    assertEqualsWithComparisonFailure(holder.getValue(), holderClone.getValue());
  }

  @Test
  public void testCloneCustomImplementedEntityDo() throws Exception {
    DoEntityHolder<TestCustomImplementedEntityDo> holder = new DoEntityHolder<>();
    holder.setValue(BEANS.get(TestCustomImplementedEntityDo.class));
    holder.getValue().put("stringAttribute", "foo");

    DoEntityHolder<TestCustomImplementedEntityDo> holderClone = CloneUtility.createDeepCopyBySerializing(holder);
    assertEqualsWithComparisonFailure(holder.getValue(), holderClone.getValue());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDisabledDefaultTyping() {
    assertNull(m_mapper.getObjectMapper().getSerializationConfig().getDefaultTyper(null));
    assertNull(m_mapper.getObjectMapper().getSerializationConfig().getDefaultTyper(TypeFactory.defaultInstance().constructType(DoEntity.class)));
    assertNull(m_mapper.getObjectMapper().getSerializationConfig().getDefaultTyper(TypeFactory.defaultInstance().constructType(Object.class)));

    assertNull(m_mapper.getObjectMapper().getDeserializationConfig().getDefaultTyper(null));
    assertNull(m_mapper.getObjectMapper().getDeserializationConfig().getDefaultTyper(TypeFactory.defaultInstance().constructType(DoEntity.class)));
    assertNull(m_mapper.getObjectMapper().getDeserializationConfig().getDefaultTyper(TypeFactory.defaultInstance().constructType(Object.class)));
  }
}
