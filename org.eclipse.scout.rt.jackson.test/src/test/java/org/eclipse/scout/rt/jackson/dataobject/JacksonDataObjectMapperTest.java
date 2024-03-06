/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.testing.TestingDataObjectHelper;
import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper.StreamReadConstraintsConfigProperty;
import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper.StreamWriteConstraintsConfigProperty;
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
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadConstraints.Builder;
import com.fasterxml.jackson.core.StreamWriteConstraints;
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

  protected final String m_longStringValue = IntStream.range(1, 10_0000).mapToObj(Integer::toString).collect(Collectors.joining());

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamReadConstraints_maxStringLength() {
    // Jackson does not check string length for very short input streams
    assertEquals("12345", runTestStreamReadConstraints(b -> b.maxStringLength(3), "{\"attribute\" : \"12345\"}").get("attribute"));

    // Jackson checks string length correctly only for longer input streams
    assertThrows(PlatformException.class, () -> runTestStreamReadConstraints(b -> b.maxStringLength(3), "{\"attribute\" : \"" + m_longStringValue + "\"}"));
  }

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamReadConstraints_maxDocumentLength() {
    // Jackson does not check document length for very short input streams
    assertEquals("12345", runTestStreamReadConstraints(b -> b.maxDocumentLength(3), "{\"attribute\" : \"12345\"}").get("attribute"));

    // Jackson checks document length correctly only for longer input streams
    assertThrows(PlatformException.class, () -> runTestStreamReadConstraints(b -> b.maxDocumentLength(3), "{\"attribute\" : \"" + m_longStringValue + "\"}"));
  }

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamReadConstraints_maxNumberLength() {
    assertThrows(PlatformException.class, () -> runTestStreamReadConstraints(b -> b.maxNumberLength(3), "{\"attribute\" : 1234}"));
  }

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamReadConstraints_maxNameLength() {
    assertThrows(PlatformException.class, () -> runTestStreamReadConstraints(b -> b.maxNameLength(8), "{\"attribute\": 1234}"));
  }

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamReadConstraints_maxNestingDepth() {
    assertThrows(PlatformException.class, () -> runTestStreamReadConstraints(b -> b.maxNestingDepth(1), "{\"attribute\" : []}"));
  }

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamReadConstraints_maxBigIntScale() {
    // BigDecimal scale (100001) magnitude exceeds the maximum allowed (100000)
    BigDecimal value = new BigDecimal("1").setScale(100_001, RoundingMode.UNNECESSARY);

    // reading a big integer attribute which is given as very large decimal value in JSON
    // See com.fasterxml.jackson.core.StreamReadConstraints#validateBigIntegerScale for fixed scale limit of 100k
    String json = m_mapper.writeValue(BEANS.get(DoEntityBuilder.class).put("bigIntegerAttribute", value).build());
    assertThrows(PlatformException.class, () -> runTestStreamReadConstraints(b -> b.maxNumberLength(100_002), json, TestComplexEntityDo.class));
  }

  protected DoEntity runTestStreamReadConstraints(Consumer<Builder> builderConsumer, String json) {
    return runTestStreamReadConstraints(builderConsumer, json, DoEntity.class);
  }

  protected <T> T runTestStreamReadConstraints(Consumer<Builder> builderConsumer, String json, Class<T> expectedClass) {
    Builder builder = StreamReadConstraints.builder();
    builderConsumer.accept(builder);
    IBean bean = BeanTestingHelper.get().mockConfigProperty(StreamReadConstraintsConfigProperty.class, builder.build());
    try {
      JacksonDataObjectMapper mapper = new JacksonDataObjectMapper(); // force new instance to apply config change
      return mapper.readValue(json, expectedClass);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }

  @Test
  public void testParseStreamReadConstraintsProperty_defaultValues() {
    StreamReadConstraints constraints = BEANS.get(StreamReadConstraintsConfigProperty.class).parse(CollectionUtility.emptyHashMap());
    assertEquals(1000, constraints.getMaxNestingDepth());
    assertEquals(-1, constraints.getMaxDocumentLength());
    assertEquals(50_000, constraints.getMaxNameLength());
    assertEquals(1000, constraints.getMaxNumberLength());
    assertEquals(100_000_000, constraints.getMaxStringLength());
  }

  @Test
  public void testParseStreamReadConstraintsProperty_invalidKey() {
    assertThrows(PlatformException.class, () -> BEANS.get(StreamReadConstraintsConfigProperty.class).parse(Map.of("foo", "bar")));
  }

  @Test
  public void testParseStreamReadConstraintsProperty_values() {
    StreamReadConstraints constraints = BEANS.get(StreamReadConstraintsConfigProperty.class).parse(Map.of(
        StreamReadConstraintsConfigProperty.MAX_NESTING_DEPTH, "1",
        StreamReadConstraintsConfigProperty.MAX_DOCUMENT_LENGTH, "2",
        StreamReadConstraintsConfigProperty.MAX_NAME_LENGTH, "3",
        StreamReadConstraintsConfigProperty.MAX_NUMBER_LENGTH, "4",
        StreamReadConstraintsConfigProperty.MAX_STRING_LENGTH, "5"));

    assertEquals(1, constraints.getMaxNestingDepth());
    assertEquals(2, constraints.getMaxDocumentLength());
    assertEquals(3, constraints.getMaxNameLength());
    assertEquals(4, constraints.getMaxNumberLength());
    assertEquals(5, constraints.getMaxStringLength());
  }

  @Test
  public void testParseStreamReadConstraintsProperty_incompleteValues() {
    StreamReadConstraints constraints = BEANS.get(StreamReadConstraintsConfigProperty.class).parse(Map.of(
        StreamReadConstraintsConfigProperty.MAX_NESTING_DEPTH, "1",
        StreamReadConstraintsConfigProperty.MAX_DOCUMENT_LENGTH, "2",
        StreamReadConstraintsConfigProperty.MAX_NAME_LENGTH, "3"));

    assertEquals(1, constraints.getMaxNestingDepth());
    assertEquals(2, constraints.getMaxDocumentLength());
    assertEquals(3, constraints.getMaxNameLength());
    assertEquals(1000, constraints.getMaxNumberLength());
    assertEquals(100_000_000, constraints.getMaxStringLength());
  }

  /**
   * NOTE: This test case just covers the current behavior of Jackson. This behavior is not enforced or verified by the
   * Scout data object mapper and may change in a future release of Scout using a newer version of Jackson.
   */
  @Test
  public void testStreamWriteConstraints_maxNestingDepth() {
    IDoEntity entity = BEANS.get(DoEntityBuilder.class).put("attribute", "a").build();
    assertEquals("{\"attribute\":\"a\"}", runTestStreamWriteConstraints(b -> b.maxNestingDepth(1), entity));

    IDoEntity nestedEntity = BEANS.get(DoEntityBuilder.class).put("attribute", BEANS.get(DoEntityBuilder.class).put("attribute", "a").build()).build();
    assertThrows(PlatformException.class, () -> runTestStreamWriteConstraints(b -> b.maxNestingDepth(1), nestedEntity));

    IDoEntity listEntity = BEANS.get(DoEntityBuilder.class).putList("attribute", "a", "b").build();
    assertThrows(PlatformException.class, () -> runTestStreamWriteConstraints(b -> b.maxNestingDepth(1), listEntity));
  }

  protected String runTestStreamWriteConstraints(Consumer<StreamWriteConstraints.Builder> builderConsumer, IDoEntity entity) {
    StreamWriteConstraints.Builder builder = StreamWriteConstraints.builder();
    builderConsumer.accept(builder);
    IBean bean = BeanTestingHelper.get().mockConfigProperty(StreamWriteConstraintsConfigProperty.class, builder.build());
    try {
      JacksonDataObjectMapper mapper = new JacksonDataObjectMapper(); // force new instance to apply config change
      return mapper.writeValue(entity);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }

  @Test
  public void testParseStreamWriteConstraintsProperty_defaultValues() {
    StreamWriteConstraints constraints = BEANS.get(StreamWriteConstraintsConfigProperty.class).parse(CollectionUtility.emptyHashMap());
    assertEquals(1000, constraints.getMaxNestingDepth());
  }

  @Test
  public void testParseStreamWriteConstraintsProperty_invalidKey() {
    assertThrows(PlatformException.class, () -> BEANS.get(StreamWriteConstraintsConfigProperty.class).parse(Map.of("foo", "bar")));
  }

  @Test
  public void testParseStreamWriteConstraintsProperty_values() {
    StreamWriteConstraints constraints = BEANS.get(StreamWriteConstraintsConfigProperty.class).parse(Map.of(StreamReadConstraintsConfigProperty.MAX_NESTING_DEPTH, "1"));
    assertEquals(1, constraints.getMaxNestingDepth());
  }
}
