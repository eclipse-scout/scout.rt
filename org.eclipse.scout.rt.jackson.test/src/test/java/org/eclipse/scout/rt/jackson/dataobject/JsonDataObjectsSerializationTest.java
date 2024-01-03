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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoCollection;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.fixture.FixtureHierarchicalLookupRowDo;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.lookup.LookupResponse;
import org.eclipse.scout.rt.dataobject.value.DateValueDo;
import org.eclipse.scout.rt.dataobject.value.IntegerValueDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.AbstractTestAddressDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.DoubleContributionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.ITestBaseEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestBigIntegerDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestBinaryDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestBinaryResourceDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCollectionsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCollectionsIDoEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample1Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample1DoContributionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample2Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample3Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCurrencyDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCustomImplementedEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDateDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoMapDoMapEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoMapDoMapStringDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoMapEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoMapListEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoMapObjectDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoMapStringDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoValuePojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDuplicatedAttributeDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestElectronicAddressDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEmptyObject;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithArrayDoValueDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithDoValueOfObjectDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithGenericValuesDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithIIdDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithInterface1Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithInterface2Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithListsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithNestedEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithVariousIdsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestGenericDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestGenericDoEntityMapDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItem3Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemContributionOneDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemContributionTwoDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemExDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemPojo2;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestMapDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestMixedRawBigIntegerDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestNestedRawDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestOptionalDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPersonDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPhysicalAddressDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPhysicalAddressExDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPojoWithJacksonAnnotations;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPojoWithLocaleProperties;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample1ContributionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample1Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample2Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample3Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestRenamedAttributeDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestSetDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestStringHolder;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestStringHolderPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestStringPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestSubPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestThrowableDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestTypedUntypedInnerDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestTypedUntypedOuterDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestVersionedDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestWithEmptyTypeNameDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestWithoutTypeNameDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestWithoutTypeNameSubclassDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestWithoutTypeNameSubclassWithTypeNameDo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.StrictSimpleDateFormat;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Various test cases serializing and deserializing Scout data objects from/to JSON
 */
public class JsonDataObjectsSerializationTest {

  protected static final Date DATE_TRUNCATED = DateUtility.parse("1990-10-20 00:00:00.000", IValueFormatConstants.DEFAULT_DATE_PATTERN);
  protected static final Date DATE = DateUtility.parse("2017-11-30 17:29:12.583", IValueFormatConstants.DEFAULT_DATE_PATTERN);
  protected static final Date DATE_2 = DateUtility.parse("2017-12-30 16:13:44.879", IValueFormatConstants.DEFAULT_DATE_PATTERN);
  protected static final Date DATE_TIMEZONE = DateUtility.parse("2022-07-11T08:49:42.654Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  protected static final UUID UUID_1 = UUID.fromString("ab8b13a4-b2a0-47a0-9d79-80039417b843");
  protected static final UUID UUID_2 = UUID.fromString("87069a20-6fc5-4b6a-9bc2-2e6cb75d7571");

  protected static final BinaryResource BINARY_RESOURCE = BinaryResources.create()
      .withContent("123".getBytes())
      .withContentType("image/jpeg")
      .withFilename("unicorn.jpg")
      .build();

  protected static final BinaryResource BINARY_RESOURCE_NULL_CONTENT = BinaryResources.create()
      .withContentType("image/jpeg")
      .withFilename("unicorn.jpg")
      .build();

  protected static DataObjectSerializationTestHelper s_testHelper;
  protected static DataObjectHelper s_dataObjectHelper;

  protected static ObjectMapper s_dataObjectMapper;
  protected static ObjectMapper s_lenientDataObjectMapper;
  protected static ObjectMapper s_defaultJacksonObjectMapper;

  @SuppressWarnings("deprecation")
  @BeforeClass
  public static void beforeClass() {
    s_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
    s_dataObjectHelper = BEANS.get(DataObjectHelper.class);
    s_dataObjectMapper = BEANS.get(JacksonPrettyPrintDataObjectMapper.class).getObjectMapper();
    s_lenientDataObjectMapper = BEANS.get(JacksonLenientDataObjectMapper.class).getObjectMapper();

    s_defaultJacksonObjectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_DEFAULT)
        .setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN))
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }

  // ------------------------------------ DoValue test cases ------------------------------------

  /**
   * POJO root class which contains a {@code DoValue<String>} element
   */
  @Test
  public void testSerialize_DoValuePojo() throws Exception {
    TestDoValuePojo pojo = new TestDoValuePojo();
    pojo.setStringValue(DoValue.of("foo"));
    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonEquals("TestDoValuePojo.json", json);

    TestDoValuePojo pojoMarshalled = s_dataObjectMapper.readValue(json, TestDoValuePojo.class);
    assertEquals(pojo.getStringValue().get(), pojoMarshalled.getStringValue().get());
  }

  @Test
  public void testDeserialize_DoValuePojo() throws Exception {
    String inputJson = readResourceAsString("TestDoValuePojo.json");
    TestDoValuePojo pojo = s_dataObjectMapper.readValue(inputJson, TestDoValuePojo.class);
    assertEquals("foo", pojo.getStringValue().get());

    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonEquals("TestDoValuePojo.json", json);
  }

  /**
   * TestBigIntegerDo as root object (DoEntity), containing a {@code DoValue<BigInteger>} element
   */
  @Test
  public void testSerialize_TestBigIntegerDo() throws Exception {
    TestBigIntegerDo testDo = BEANS.get(TestBigIntegerDo.class);
    testDo.bigIntegerAttribute().set(new BigInteger("123456"));
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBigIntegerDo.json", json);

    TestBigIntegerDo pojoMarshalled = s_dataObjectMapper.readValue(json, TestBigIntegerDo.class);
    assertEquals(testDo.getBigIntegerAttribute(), pojoMarshalled.getBigIntegerAttribute());
  }

  @Test
  public void testDeserialize_TestBigIntegerDo() throws Exception {
    String inputJson = readResourceAsString("TestBigIntegerDo.json");
    TestBigIntegerDo testDo = s_dataObjectMapper.readValue(inputJson, TestBigIntegerDo.class);
    assertEquals("123456", testDo.getBigIntegerAttribute().toString());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBigIntegerDo.json", json);
  }

  @Test
  public void testDeserialize_TestEntityWithVariousIds() throws Exception {
    String inputJson = readResourceAsString("TestEntityWithVariousIdsDo.json");
    TestEntityWithVariousIdsDo testDo = s_dataObjectMapper.readValue(inputJson, TestEntityWithVariousIdsDo.class);

    TestEntityWithVariousIdsDo expected = BEANS.get(TestEntityWithVariousIdsDo.class)
        .withStringId(FixtureStringId.of("string-id"))
        .withIId(FixtureStringId.of("i-id"))
        .withStringIds(FixtureStringId.of("string-id-1"), FixtureStringId.of("string-id-2"))
        .withIIds(FixtureStringId.of("i-id-1"), FixtureStringId.of("i-id-2"))
        .withManualStringIds(Arrays.asList(FixtureStringId.of("manual-string-id-1"), FixtureStringId.of("manual-string-id-2")))
        .withManualIIds(Arrays.asList(FixtureStringId.of("manual-i-id-1"), FixtureStringId.of("manual-i-id-2")))
        .withStringIdKeyMap(CollectionUtility.hashMap(
            ImmutablePair.of(FixtureStringId.of("string-id-map-key-1"), "plain-string-id-map-value-1"),
            ImmutablePair.of(FixtureStringId.of("string-id-map-key-2"), "plain-string-id-map-value-2")))
        .withIIdKeyMap(CollectionUtility.hashMap(
            ImmutablePair.of(FixtureStringId.of("i-id-map-key-1"), "plain-i-id-map-value-1"),
            ImmutablePair.of(FixtureStringId.of("i-id-map-key-2"), "plain-i-id-map-value-2")))
        .withStringIdValueMap(CollectionUtility.hashMap(
            ImmutablePair.of("plain-string-id-map-key-1", FixtureStringId.of("string-id-map-value-1")),
            ImmutablePair.of("plain-string-id-map-key-2", FixtureStringId.of("string-id-map-value-2"))))
        .withIIdValueMap(CollectionUtility.hashMap(
            ImmutablePair.of("plain-i-id-map-key-1", FixtureStringId.of("i-id-map-value-1")),
            ImmutablePair.of("plain-i-id-map-key-2", FixtureStringId.of("i-id-map-value-2"))));

    assertEquals(expected, testDo);
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithVariousIdsDo.json", json);
  }

  @Test
  public void testDeserialize_TestEntityWithNestedEntityDo() throws Exception {
    String inputJson = readResourceAsString("TestEntityWithNestedEntityDo.json");
    TestEntityWithNestedEntityDo testDo = s_dataObjectMapper.readValue(inputJson, TestEntityWithNestedEntityDo.class);
    assertEquals("123456", testDo.getBigIntegerAttribute().toString());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithNestedEntityDo.json", json);
  }

  @Test
  public void testSerialize_EntityDoWithArrayDoValue() throws Exception {
    TestEntityWithArrayDoValueDo testDo = BEANS.get(TestEntityWithArrayDoValueDo.class);
    testDo.stringArrayAttribute().set(new String[]{"one", "two", "three"});
    testDo.itemDoArrayAttribute().set(new TestItemDo[]{createTestItemDo("1", "foo"), createTestItemDo("2", "bar")});
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithArrayDoValueDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithArrayDoValueDo() throws Exception {
    String jsonInput = readResourceAsString("TestEntityWithArrayDoValueDo.json");
    TestEntityWithArrayDoValueDo entity = s_dataObjectMapper.readValue(jsonInput, TestEntityWithArrayDoValueDo.class);

    String[] valuesWithExplicitType = entity.get("stringArrayAttribute", String[].class);
    assertArrayEquals(new String[]{"one", "two", "three"}, valuesWithExplicitType);

    String[] valuesWithInferedType = entity.get("stringArrayAttribute", String[].class);
    assertArrayEquals(new String[]{"one", "two", "three"}, valuesWithInferedType);

    TestItemDo[] itemDo = entity.get("itemDoArrayAttribute", TestItemDo[].class);
    assertEquals("1", itemDo[0].getId());
    assertEquals("foo", itemDo[0].getStringAttribute());

    assertEquals("2", entity.itemDoArrayAttribute().get()[1].getId());
    assertEquals("bar", entity.itemDoArrayAttribute().get()[1].getStringAttribute());

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testSerialize_DateDo() throws Exception {
    final String dateWithTimeZoneString = "2017-11-30 17:29:12.583 +0100";
    final Date dateWithTimezone = DateUtility.parse(dateWithTimeZoneString, IValueFormatConstants.TIMESTAMP_WITH_TIMEZONE_PATTERN);
    final String dateWithTimeZoneFormattedLocal = DateUtility.format(dateWithTimezone, IValueFormatConstants.TIMESTAMP_WITH_TIMEZONE_PATTERN);
    final String dateWithTimeZoneZuluString = "2017-11-30T17:29:12.583Z";
    final Date dateWithTimezoneZulu = DateUtility.parse(dateWithTimeZoneZuluString, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    TestDateDo testDo = BEANS.get(TestDateDo.class);
    testDo.withDateDefault(DATE)
        .withDateOnly(DATE_TRUNCATED)
        .withDateOnlyDoList(DATE_TRUNCATED)
        .withDateOnlyList(Arrays.asList(DATE_TRUNCATED))
        .withDateWithTimestamp(DATE)
        .withDateWithTimestampWithTimezone(dateWithTimezone)
        .withDateWithTimestampZulu(dateWithTimezoneZulu)
        .withDateYearMonth(DATE_TRUNCATED)
        .withCustomDateFormat(DATE)
        .withCustomDateDoList(DATE, DATE_2);

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    String expectedJson = readResourceAsString("TestDateDo.json");
    // replace expected date to expect formatted date in local timezone representation
    expectedJson = expectedJson.replace(dateWithTimeZoneString, dateWithTimeZoneFormattedLocal);
    s_testHelper.assertJsonEquals(expectedJson, json);

    // deserialize and check
    TestDateDo testDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
    assertEquals(DATE, testDoMarshalled.dateDefault().get());
    assertEquals(DATE_TRUNCATED, testDoMarshalled.dateOnly().get());
    assertEquals(DATE_TRUNCATED, testDoMarshalled.dateOnlyDoList().get().get(0));
    assertEquals(DATE_TRUNCATED, testDoMarshalled.dateOnlyList().get().get(0));
    assertEquals(DATE, testDoMarshalled.dateWithTimestamp().get());
    assertEquals(dateWithTimezone, testDoMarshalled.dateWithTimestampWithTimezone().get());
    assertEquals(dateWithTimezoneZulu, testDoMarshalled.dateWithTimestampZulu().get());
    assertEquals(DateUtility.truncDateToMonth(DATE_TRUNCATED), testDoMarshalled.getDateYearMonth());
  }

  @Test(expected = JsonMappingException.class)
  public void testSerialize_InvalidDateDo() throws Exception {
    TestDateDo testDo = BEANS.get(TestDateDo.class).withInvalidDateFormat(DATE);
    s_dataObjectMapper.writeValueAsString(testDo);
  }

  /**
   * JSON file with a valid date but format pattern on TestDateDo attribute "invalidDateFormat" is invalid
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDeserialize_InvalidDateDo() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDateDo.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  /**
   * JSON file with an invalid date for TestDateDo attribute "dateDefault"
   */
  @Test(expected = InvalidFormatException.class)
  public void testDeserialize_InvalidDate2Do() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDate2Do.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  /**
   * JSON file with a valid date for TestDateDo attribute "dateOnly", but pattern does not match exactly.
   * <p>
   * Requires {@link StrictSimpleDateFormat}
   */
  @Test(expected = InvalidFormatException.class)
  public void testDeserialize_InvalidDate3Do() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDate3Do.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  /**
   * JSON file with an incomplete date for TestDateDo attribute "dateDefault"
   */
  @Test(expected = InvalidFormatException.class)
  public void testDeserialize_InvalidDate4Do() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDate4Do.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  /**
   * JSON file with a valid date for TestDateDo attribute "dateDefault", but pattern does not match exactly.
   * <p>
   * Requires {@link StrictSimpleDateFormat}
   */
  @Test(expected = InvalidFormatException.class)
  public void testDeserialize_InvalidDate5Do() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDate5Do.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  @Test
  public void testSerializeDeserialize_NullDateDo() throws Exception {
    TestDateDo date = BEANS.get(TestDateDo.class).withDateDefault(null);
    String json = s_dataObjectMapper.writeValueAsString(date);
    assertJsonEquals("TestNullDateDo.json", json);
  }

  @Test
  public void testSerializeDeserialize_NullStringDateDo() throws Exception {
    String expectedJson = readResourceAsString("TestNullStringDateDo.json");
    TestDateDo date = s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
    assertNull(date.getDateDefault());
  }

  @Test
  public void testDeserialize_EmptyDateDo() throws Exception {
    String expectedJson = readResourceAsString("TestEmptyDateDo.json");
    TestDateDo date = s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
    assertNull(date.getDateDefault());
  }

  @Test
  public void testSerialize_BinaryResource() throws Exception {
    TestBinaryResourceDo testDo = BEANS.get(TestBinaryResourceDo.class).withBrDefault(BINARY_RESOURCE);
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBinaryResourceDo.json", json);

    TestBinaryResourceDo pojoMarshalled = s_dataObjectMapper.readValue(json, TestBinaryResourceDo.class);
    assertEquals(testDo.getBrDefault(), pojoMarshalled.getBrDefault());
  }

  @Test
  public void testSerialize_BinaryResource_NullContent() throws Exception {
    TestBinaryResourceDo testDo = BEANS.get(TestBinaryResourceDo.class).withBrDefault(BINARY_RESOURCE_NULL_CONTENT);
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBinaryResourceDoNullContent.json", json);

    TestBinaryResourceDo pojoMarshalled = s_dataObjectMapper.readValue(json, TestBinaryResourceDo.class);
    assertEquals(testDo.getBrDefault(), pojoMarshalled.getBrDefault());
  }

  @Test
  public void testDeserialize_BinaryResource() throws Exception {
    String inputJson = readResourceAsString("TestBinaryResourceDo.json");
    TestBinaryResourceDo testDo = s_dataObjectMapper.readValue(inputJson, TestBinaryResourceDo.class);
    assertEquals(BINARY_RESOURCE, testDo.getBrDefault());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBinaryResourceDo.json", json);
  }

  @Test
  public void testDeserialize_BinaryResource_NullContent() throws Exception {
    String inputJson = readResourceAsString("TestBinaryResourceDoNullContent.json");
    TestBinaryResourceDo testDo = s_dataObjectMapper.readValue(inputJson, TestBinaryResourceDo.class);
    assertEquals(BINARY_RESOURCE_NULL_CONTENT, testDo.getBrDefault());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBinaryResourceDoNullContent.json", json);
  }

  @Test
  public void testDeserialize_BinaryResource_WithoutCharset() throws Exception {
    String inputJson = readResourceAsString("TestBinaryResourceDoWithoutCharset.json");
    TestBinaryResourceDo testDo = s_dataObjectMapper.readValue(inputJson, TestBinaryResourceDo.class);
    assertEquals(BINARY_RESOURCE, testDo.getBrDefault());
  }

  @Test
  public void testDeserialize_BinaryResource_NullValues() throws Exception {
    String inputJson = readResourceAsString("TestBinaryResourceDoNullValues.json");
    TestBinaryResourceDo testDo = s_dataObjectMapper.readValue(inputJson, TestBinaryResourceDo.class);
    assertEquals(BinaryResources.create()
        .withContentType("image/jpeg")
        .withFilename("unicorn.jpg")
        .build(), testDo.getBrDefault());
  }

  /**
   * Testcase ensures that correct entity object is set as "current value" on json parser to deserialize fields with
   * special formatting annotation located after _type attribute.
   */
  @Test
  public void testDeserializeTestDateDo_UnorderedSpecialAttributesAfterType() throws Exception {
    runTestDeserializeTestDateDo("TestDateDoUnorderedAttributes.json");
    runTestDeserializeTestDateDo("TestDateDoUnorderedAttributes2.json");
    runTestDeserializeTestDateDo("TestDateDoUnorderedAttributes3.json");
  }

  /**
   * Testcase ensures that correct entity object is set as "current value" on json parser to deserialize fields with
   * special formatting annotation located before _type attribute.
   */
  @Test
  public void testDeserializeTestDateDo_UnorderedSpecialAttributesBeforeType() throws Exception {
    runTestDeserializeTestDateDo("TestDateDoUnorderedAttributes4.json");
    runTestDeserializeTestDateDo("TestDateDoUnorderedAttributes4.json");
  }

  @Test
  public void testDeserializeTestDateDo_OrderedAttributes() throws Exception {
    runTestDeserializeTestDateDo("TestDateDoOrderedAttributes.json");
  }

  @Test
  public void testDeserializeTestDateDo_WithoutTypeAttribute() throws Exception {
    runTestDeserializeTestDateDo("TestDateDoWithoutTypeAttribute.json");
  }

  protected void runTestDeserializeTestDateDo(String resource) throws Exception {
    String expectedJson = readResourceAsString(resource);
    TestDateDo marshalled = s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
    assertEquals(DATE, marshalled.dateDefault().get());
    assertEquals(DATE_TRUNCATED, marshalled.dateOnly().get());
    assertEquals(DateUtility.truncDateToMonth(DATE_TRUNCATED), marshalled.getDateYearMonth());
    assertEquals(DATE_TIMEZONE, marshalled.aaaDate().get());
  }

  @Test
  public void testSerializeDeserialize_RenamedAttributeDo() throws Exception {
    TestRenamedAttributeDo testDo = BEANS.get(TestRenamedAttributeDo.class)
        .withAllAttribute(new BigDecimal("42"))
        .withDateAttribute(DATE)
        .withGet("get-value")
        .withHas("has-value")
        .withPut("put-value")
        .withHashCodeAttribute(42)
        .withWaitAttribute(123)
        .withCloneAttribute(BigDecimal.ZERO, BigDecimal.ONE)
        .withFinalizeAttribute(Arrays.asList(BigInteger.ONE, BigInteger.TEN, BigInteger.ZERO));

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    TestRenamedAttributeDo testDoMarshalled = s_dataObjectMapper.readValue(json, TestRenamedAttributeDo.class);
    assertEqualsWithComparisonFailure(testDo, testDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_TestItemExDo() throws Exception {
    TestItemExDo testDo = BEANS.get(TestItemExDo.class).withId("foo");

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    TestItemExDo testDoMarshalled = s_dataObjectMapper.readValue(json, TestItemExDo.class);
    assertEqualsWithComparisonFailure(testDo, testDoMarshalled);
  }

  @Test
  public void testDeserializeDuplicatedAttribute() throws Exception {
    String json = readResourceAsString("TestDuplicatedAttributeDo.json");
    TestDuplicatedAttributeDo entity = s_dataObjectMapper.readValue(json, TestDuplicatedAttributeDo.class);
    assertEquals("secondValue", entity.getStringAttribute());
    assertEquals(new BigDecimal("2.0"), entity.getBigDecimalAttribute());
    assertEquals(new BigInteger("1"), entity.getBigIntegerAttribute());
  }

  @Test
  public void testDeserializeDuplicatedAttributeRaw() throws Exception {
    String json = readResourceAsString("TestDuplicatedAttributeDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals("secondValue", entity.getString("stringAttribute"));
    assertEquals(new BigDecimal("2.0"), entity.getDecimal("bigDecimalAttribute"));
    assertEquals(new BigDecimal("1"), entity.getDecimal("bigIntegerAttribute"));
  }

  @Test
  public void testSerializeDeserialize_BinaryDo() throws Exception {
    TestBinaryDo binary = BEANS.get(TestBinaryDo.class);
    byte[] content = IOUtility.readFromUrl(getResource("TestBinaryContent.jpg"));
    binary.withContent(content);
    String json = s_dataObjectMapper.writeValueAsString(binary);
    assertJsonEquals("TestBinaryDo.json", json);

    TestBinaryDo marshalled = s_dataObjectMapper.readValue(json, TestBinaryDo.class);
    assertArrayEquals(content, marshalled.getContent());

    // read object as raw Map<String, String> object
    Map<String, String> rawObject = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    String base64encoded = Base64Utility.encode(content);
    assertEquals(base64encoded, rawObject.get("content"));
    assertEqualsWithComparisonFailure(binary, marshalled);
  }

  @Test
  public void testSerializeDeserialize_Locale() throws Exception {
    String jsonPlainLocale = s_dataObjectMapper.writeValueAsString(Locale.GERMANY);
    // All locales (whether wrapped within DoEntity structure or not) are serialized by custom Scout behavior, as they will when Jackson is upgraded to 3.0.
    // Issue 1600 (https://github.com/FasterXML/jackson-databind/issues/1600)
    assertEquals("\"de-DE\"", jsonPlainLocale);

    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("locale", Locale.GERMANY);
    String jsonEntity = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestLocale.json", jsonEntity);
  }

  @Test
  public void testSerializeDeserialize_ROOT_Locale() throws Exception {
    try {
      HashMap<Locale, String> localeMap = CollectionUtility.hashMap(
          new ImmutablePair<>(Locale.ROOT, "Root"),
          new ImmutablePair<>(Locale.forLanguageTag("de-CH"), "German, Switzerland"));
      TestPojoWithLocaleProperties pojo = new TestPojoWithLocaleProperties();
      pojo.setLocale1(Locale.ROOT);
      pojo.setLocale2(Locale.forLanguageTag("de-CH"));
      pojo.setLocaleStringMap(localeMap);

      // disable ordering map entries by keys for the default jackson serializer as it expects
      // the key object to implement java.lang.Comparable (which java.util.Locale does not).
      s_defaultJacksonObjectMapper.disable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

      String serializedDefaultJackson = s_defaultJacksonObjectMapper.writeValueAsString(pojo);
      TestPojoWithLocaleProperties deserializedDefaultJackson = s_defaultJacksonObjectMapper.readValue(serializedDefaultJackson, TestPojoWithLocaleProperties.class);
      assertJsonEquals("TestSerializeDeserialize_ROOT_Locale_defaultJackson.json", serializedDefaultJackson);
      assertThat(deserializedDefaultJackson.getLocale1(), is(Locale.ROOT));
      assertThat(deserializedDefaultJackson.getLocale2(), is(Locale.forLanguageTag("de-CH")));
      assertThat(deserializedDefaultJackson.getLocaleStringMap(), is(localeMap));

      String serializedScout = s_dataObjectMapper.writeValueAsString(pojo);
      TestPojoWithLocaleProperties deserializedScout = s_dataObjectMapper.readValue(serializedScout, TestPojoWithLocaleProperties.class);
      assertJsonEquals("TestSerializeDeserialize_ROOT_Locale_scout.json", serializedScout);
      assertThat(deserializedScout.getLocale1(), is(Locale.ROOT));
      assertThat(deserializedScout.getLocale2(), is(Locale.forLanguageTag("de-CH")));
      assertThat(deserializedScout.getLocaleStringMap(), is(localeMap));
    }
    finally {
      s_defaultJacksonObjectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }
  }

  @Test
  public void testSerializeDeserialize_Currency() throws Exception {
    String jsonPlainCurrency = s_dataObjectMapper.writeValueAsString(Currency.getInstance("CHF"));
    assertEquals("\"CHF\"", jsonPlainCurrency);

    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("currency", Currency.getInstance("USD"));
    String jsonEntity = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestCurrency.json", jsonEntity);

    assertEquals(Currency.getInstance("EUR"), s_dataObjectMapper.readValue("\"EUR\"", Currency.class));
    assertEquals(Currency.getInstance("EUR"), s_dataObjectMapper.readValue("\"eur\"", Currency.class));
    assertNull(s_dataObjectMapper.readValue("null", Currency.class));

    assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue("\"nop\"", Currency.class));
  }

  @Test
  public void testSerializeDeserialize_CurrencyDo() throws Exception {
    Map<Currency, String> currencyMap = new LinkedHashMap<>();
    currencyMap.put(Currency.getInstance("CHF"), "Switzerland");
    currencyMap.put(Currency.getInstance("EUR"), "Europe");
    TestCurrencyDo currencyDo = BEANS.get(TestCurrencyDo.class)
        .withCurrency(Currency.getInstance("CHF"))
        .withCurrencies(Currency.getInstance("USD"), Currency.getInstance("EUR"))
        .withCurrencyMap(currencyMap);

    String json = s_dataObjectMapper.writeValueAsString(currencyDo);
    assertJsonEquals("TestCurrencyDo.json", json);
  }

  @Test
  public void testSerializeDeserialize_CurrencyDoLowercase() throws Exception {
    Map<Currency, String> currencyMap = new LinkedHashMap<>();
    currencyMap.put(Currency.getInstance("CHF"), "Switzerland");
    currencyMap.put(Currency.getInstance("EUR"), "Europe");
    TestCurrencyDo currencyDo = BEANS.get(TestCurrencyDo.class)
        .withCurrency(Currency.getInstance("CHF"))
        .withCurrencies(Currency.getInstance("USD"), Currency.getInstance("EUR"))
        .withCurrencyMap(currencyMap);

    String json = readResourceAsString("TestCurrencyDoLowercase.json");
    TestCurrencyDo read = s_dataObjectMapper.readValue(json, TestCurrencyDo.class);
    assertEquals(currencyDo, read);
  }

  @Test
  public void testSerializeDeserialize_CurrencyDoNullValues() throws Exception {
    String json = readResourceAsString("TestCurrencyDoNullValues.json");
    TestCurrencyDo read = s_dataObjectMapper.readValue(json, TestCurrencyDo.class);
    assertNull(read.getCurrency());
    assertNull(read.getCurrencyMap());
    List<Currency> nullValues = new ArrayList<>();
    nullValues.add(null);
    nullValues.add(null);
    assertEquals(nullValues, read.getCurrencies());
  }

  @Test
  public void testSerializeDeserialize_URI() throws Exception {
    URI uri = new URI("https://www.example.org/path/page?q=query");
    String jsonPlainUri = s_dataObjectMapper.writeValueAsString(uri);
    assertEquals("\"https://www.example.org/path/page?q=query\"", jsonPlainUri);

    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("uri", uri);
    String jsonEntity = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestURI.json", jsonEntity);
  }

  @Test
  public void testSerializeDeserialize_DoValueOfObject() throws Exception {
    // String
    TestEntityWithDoValueOfObjectDo stringObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject("test-string");
    String json = s_dataObjectMapper.writeValueAsString(stringObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_String.json", json);

    TestEntityWithDoValueOfObjectDo marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals("test-string", marshalled.getObject());
    assertEquals(DoValue.class, marshalled.object().getClass());

    // Long
    TestEntityWithDoValueOfObjectDo LongObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject(Long.valueOf(42));
    json = s_dataObjectMapper.writeValueAsString(LongObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_Long.json", json);

    marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals(Integer.valueOf(42), marshalled.getObject()); // Integer because Jackson uses the smallest possible number type
    assertEquals(DoValue.class, marshalled.object().getClass());

    // Entity
    TestEntityWithDoValueOfObjectDo entityObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject(createTestItemDo("test-id", "test_value"));
    json = s_dataObjectMapper.writeValueAsString(entityObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_Entity.json", json);

    marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals(createTestItemDo("test-id", "test_value"), marshalled.getObject());
    assertEquals(DoValue.class, marshalled.object().getClass());

    // ad-hoc Entity
    TestEntityWithDoValueOfObjectDo adHocEntityObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject(
        BEANS.get(DoEntityBuilder.class)
            .put("longValue", Long.valueOf(42))
            .putList("listValue", List.of("a", "b", "c"))
            .build());
    json = s_dataObjectMapper.writeValueAsString(adHocEntityObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_AdHocEntity.json", json);

    marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals(DoEntity.class, marshalled.getObject().getClass());
    DoEntity entity = (DoEntity) marshalled.getObject();
    assertEquals(Integer.valueOf(42), entity.get("longValue"));
    assertEquals(List.of("a", "b", "c"), entity.get("listValue"));
    assertEquals(DoValue.class, marshalled.object().getClass());
    assertEquals(DoList.class, entity.getNode("listValue").getClass());

    // List
    TestEntityWithDoValueOfObjectDo listObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject(List.of("a", "b", "c"));
    json = s_dataObjectMapper.writeValueAsString(listObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_List.json", json);

    marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals(ArrayList.class, marshalled.getObject().getClass());
    assertEquals(List.of("a", "b", "c"), marshalled.getObject());
    assertEquals(DoValue.class, marshalled.object().getClass());

    // List of Entities
    TestEntityWithDoValueOfObjectDo listOfEntitiesObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject(
        List.of(
            createTestItemDo("test-id-a", "value-a"),
            createTestItemDo("test-id-b", "value-b")));
    json = s_dataObjectMapper.writeValueAsString(listOfEntitiesObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_EntityList.json", json);

    marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals(ArrayList.class, marshalled.getObject().getClass());
    assertEquals(
        List.of(
            createTestItemDo("test-id-a", "value-a"),
            createTestItemDo("test-id-b", "value-b")),
        marshalled.getObject());
    assertEquals(DoValue.class, marshalled.object().getClass());

    // List of ad-hoc Entities
    TestEntityWithDoValueOfObjectDo listOfAdHocEntitiesObj = BEANS.get(TestEntityWithDoValueOfObjectDo.class).withObject(
        List.of(
            BEANS.get(DoEntityBuilder.class).put("longValue", Long.valueOf(42)).putList("listValue", List.of("a", "b")).build(),
            BEANS.get(DoEntityBuilder.class).put("longValue", Long.valueOf(43)).putList("listValue", List.of("c", "d")).build()));
    json = s_dataObjectMapper.writeValueAsString(listOfAdHocEntitiesObj);
    assertJsonEquals("TestEntityWithDoValueOfObjectDo_AdHocEntityList.json", json);

    marshalled = s_dataObjectMapper.readValue(json, TestEntityWithDoValueOfObjectDo.class);
    assertEquals(ArrayList.class, marshalled.getObject().getClass());
    assertEquals(
        List.of(
            BEANS.get(DoEntityBuilder.class).put("longValue", Integer.valueOf(42)).putList("listValue", List.of("a", "b")).build(),
            BEANS.get(DoEntityBuilder.class).put("longValue", Integer.valueOf(43)).putList("listValue", List.of("c", "d")).build()),
        marshalled.getObject());
    assertEquals(DoValue.class, marshalled.object().getClass());
  }

  /**
   * Tests mixed cases with a typed DO entity containing an untyped DO entity where no type information is available
   * (i.e. the attribute for the inner DO entity is not defined in the outer DO entity class).
   */
  @Test
  public void testSerializeDeserialize_TypedUntypedWithoutTypeInformation() throws Exception {
    IDoEntity untyped = BEANS.get(DoEntityBuilder.class)
        .put("stringId", "string-id")
        .putList("stringIdList", "string-id-1", "string-id-2")
        .build();

    TestEntityWithVariousIdsDo typed = BEANS.get(TestEntityWithVariousIdsDo.class)
        .withStringId(FixtureStringId.of("unqualified-string-id"))
        .withIId(FixtureStringId.of("qualified-string-id"));

    typed.put("untyped", untyped);

    String json = s_dataObjectMapper.writeValueAsString(typed);
    assertJsonEquals("TestTypedUntypedWithoutTypeInformation.json", json);

    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEqualsWithComparisonFailure(typed, marshalled);
  }

  /**
   * Tests mixed cases with an untyped DO entity containing a typed DO entity where no type information is available
   * (i.e. the attribute for the inner DO entity is not defined in the outer DO entity class).
   */
  @Test
  public void testSerializeDeserialize_UntypedTypedWithoutTypeInformation() throws Exception {
    TestEntityWithVariousIdsDo typed = BEANS.get(TestEntityWithVariousIdsDo.class)
        .withStringId(FixtureStringId.of("unqualified-string-id"))
        .withIId(FixtureStringId.of("qualified-string-id"));

    IDoEntity untyped = BEANS.get(DoEntityBuilder.class)
        .put("stringId", "string-id")
        .putList("stringIdList", "string-id-1", "string-id-2")
        .build();

    untyped.put("typed", typed);

    String json = s_dataObjectMapper.writeValueAsString(untyped);
    assertJsonEquals("TestUntypedTypedWithoutTypeInformation.json", json);

    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEqualsWithComparisonFailure(untyped, marshalled);
  }

  /**
   * Tests mixed cases with a typed DO entity containing an untyped DO entity where type information is available (i.e.
   * the attribute for the inner DO entity is defined in the outer DO entity class).
   */
  @Test
  public void testSerializeDeserialize_TypedUntypedWithTypeInformation() throws Exception {
    TestTypedUntypedOuterDo outer = BEANS.get(TestTypedUntypedOuterDo.class)
        .withStringId(FixtureStringId.of("unqualified-string-id"))
        .withIId(FixtureStringId.of("qualifiedid"));

    TestTypedUntypedInnerDo inner = BEANS.get(TestTypedUntypedInnerDo.class)
        .withStringId(FixtureStringId.of("unqualified-string-id"))
        .withIId(FixtureStringId.of("qualifiedid"));

    IDoEntity innerUntyped = BEANS.get(DataObjectHelper.class).cloneRaw(inner);
    outer.put("inner", innerUntyped);

    String json = s_dataObjectMapper.writeValueAsString(outer);
    assertJsonEquals("TestTypedUntypedWithTypeInformation.json", json);

    // create expected do entity
    outer.withInner(inner);

    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEqualsWithComparisonFailure(outer, marshalled);
  }

  /**
   * Tests mixed cases with an untyped DO entity containing a typed DO entity where type information is available (i.e.
   * the attribute for the inner DO entity is defined in the outer DO entity class).
   */
  @Test
  public void testSerializeDeserialize_UntypedTypedWithTypeInformation() throws Exception {
    TestTypedUntypedOuterDo outer = BEANS.get(TestTypedUntypedOuterDo.class)
        .withStringId(FixtureStringId.of("unqualified-string-id"))
        .withIId(FixtureStringId.of("qualifiedid"));

    TestTypedUntypedInnerDo inner = BEANS.get(TestTypedUntypedInnerDo.class)
        .withStringId(FixtureStringId.of("unqualified-string-id"))
        .withIId(FixtureStringId.of("qualifiedid"));

    IDoEntity outerUntyped = BEANS.get(DataObjectHelper.class).cloneRaw(outer);
    outerUntyped.put("inner", inner);

    String json = s_dataObjectMapper.writeValueAsString(outerUntyped);
    assertJsonEquals("TestUntypedTypedWithTypeInformation.json", json);

    // create expected do entity
    outer.withInner(inner);

    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEqualsWithComparisonFailure(outer, marshalled);
  }

  // ------------------------------------ plain POJO test cases ------------------------------------

  /**
   * POJO object with two strings, using one regular setter and one with() setter method
   */
  @Test
  public void testSerializeDeserialize_TestStringPojo() throws Exception {
    TestStringPojo pojo = new TestStringPojo();
    pojo.withString("foo");
    pojo.setString2("bar");

    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonEquals("TestStringPojo.json", json);

    TestStringPojo testPojo = s_dataObjectMapper.readValue(json, TestStringPojo.class);
    assertEquals("foo", testPojo.getString());
    assertEquals("bar", testPojo.getString2());
  }

  @Test
  public void testSerializeDeserialize_StringHolder() throws Exception {
    TestStringHolderPojo pojo = new TestStringHolderPojo();
    pojo.setStringHolder(new TestStringHolder());
    pojo.getStringHolder().setString("foo");

    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonEquals("TestStringHolderPojo.json", json);

    TestStringHolderPojo testMarshalled = s_dataObjectMapper.readValue(json, TestStringHolderPojo.class);
    assertEquals("foo", testMarshalled.getStringHolder().getString());
  }

  @Test
  public void testSerializeDeserialize_PojoWithJacksonAnnotations() throws Exception {
    // custom DoObjectMapper configured like default object mapper
    @SuppressWarnings("deprecation")
    final ObjectMapper customDoObjectMapper = BEANS.get(JacksonPrettyPrintDataObjectMapper.class).createObjectMapperInstance(false)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN));

    TestPojoWithJacksonAnnotations pojo = new TestPojoWithJacksonAnnotations();
    pojo.setDefaultDate(DATE);
    pojo.setFormattedDate(DATE);
    pojo.setId("object-id-1");
    pojo.setIgnoredAttribute(123);
    pojo.setRenamedAttribute("renamed-attribute-value");

    String jsonDefaultMapper = s_defaultJacksonObjectMapper.writeValueAsString(pojo);
    String jsonDoMapper = customDoObjectMapper.writeValueAsString(pojo);
    assertEquals(jsonDefaultMapper, jsonDoMapper);

    TestPojoWithJacksonAnnotations pojoMarshalledDefaultMapper = s_defaultJacksonObjectMapper.readValue(jsonDefaultMapper, TestPojoWithJacksonAnnotations.class);
    TestPojoWithJacksonAnnotations pojoMarshalledDoMapper = customDoObjectMapper.readValue(jsonDefaultMapper, TestPojoWithJacksonAnnotations.class);
    assertEquals(pojoMarshalledDefaultMapper, pojoMarshalledDoMapper);
  }

  // ------------------------------------ Raw data object test cases ------------------------------------

  @Test
  public void testSerialize_SimpleDoRaw() throws Exception {
    DoEntity testDo = BEANS.get(DoEntity.class);
    testDo.put("bigIntegerAttribute", DoValue.of(new BigInteger("123456")));
    testDo.put("bigDecimalAttribute", new BigDecimal("789.0"));
    testDo.put("dateAttribute", DoValue.of(DateUtility.parse("2017-09-22 14:23:12.123", IValueFormatConstants.DEFAULT_DATE_PATTERN)));

    DoEntity testDo2 = BEANS.get(DoEntity.class);
    testDo2.put("bigIntegerAttribute2", DoValue.of(new BigInteger("789")));

    testDo.put("itemAttributeNode", testDo2);
    testDo.put("itemAttributeRef", testDo2);
    testDo.put("itemsAttributeList", Arrays.asList(testDo2, testDo2));
    testDo.put("attributeWithNullValue", null);
    testDo.putList("listAttributeWithNullValue", null);

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestSimpleDoRaw.json", json);
  }

  @Test
  public void testSerialize_SimpleDoWithPojoRaw() throws Exception {
    DoEntity testDo = BEANS.get(DoEntity.class);
    testDo.put("bigIntegerAttribute", DoValue.of(new BigInteger("123456")));
    testDo.put("bigDecimalAttribute", new BigDecimal("789.0"));
    testDo.put("dateAttribute", DoValue.of(DateUtility.parse("2017-09-22 14:23:12.123", IValueFormatConstants.DEFAULT_DATE_PATTERN)));

    TestSubPojo sub = new TestSubPojo();
    sub.setBar("bar");
    testDo.put("sub", sub);

    DoEntity testDo2 = BEANS.get(DoEntity.class);
    testDo2.put("bigIntegerAttribute2", DoValue.of(new BigInteger("789")));
    testDo.put("itemAttributeNode", testDo2);
    testDo.put("itemAttributeRef", testDo2);
    testDo.put("itemsAttributeList", Arrays.asList(testDo2, testDo2));

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestSimpleDoWithPojoRaw.json", json);
  }

  @Test
  public void testDeserialize_SimpleDoRaw() throws Exception {
    String jsonInput = readResourceAsString("TestSimpleDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    // raw properties got default JSON->Java conversion types
    assertEquals(Integer.valueOf(123456), entity.get("bigIntegerAttribute"));
    assertEquals(new BigDecimal("789.0"), entity.get("bigDecimalAttribute"));
    assertEquals("2017-09-22 14:23:12.123", entity.get("dateAttribute"));

    // assert "null" values raw
    assertNull(entity.get("attributeWithNullValue"));
    assertTrue(entity.getList("listAttributeWithNullValue").isEmpty());

    // assert "null" values when read as string
    assertNull(entity.getString("attributeWithNullValue"));
    assertTrue(entity.getStringList("listAttributeWithNullValue").isEmpty());

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testSerialize_EmptyRawDo() throws Exception {
    DoEntity testDo = BEANS.get(DoEntity.class);
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEmptyDoEntity.json", json);
  }

  @Test
  public void testSerialize_EmptyAttributeNameDo() throws Exception {
    DoEntity testDo = BEANS.get(DoEntity.class);
    testDo.put("", "");
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEmptyAttributeNameDo.json", json);
  }

  @Test
  public void testSerialize_EntityWithEmptyObjectDo() throws Exception {
    DoEntity testDo = BEANS.get(DoEntity.class);
    testDo.put("emptyObject", new TestEmptyObject());
    testDo.put("emptyList", Arrays.asList());
    testDo.put("emptyEntity", BEANS.get(DoEntity.class));
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithEmptyObjectDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithEmptyObjectDo() throws Exception {
    String input = readResourceAsString("TestEntityWithEmptyObjectDo.json");
    DoEntity entity = s_dataObjectMapper.readValue(input, DoEntity.class);
    assertTrue(entity.getList("emptyList", Object.class).isEmpty());
    DoEntity emptyObject = entity.get("emptyObject", DoEntity.class);
    assertTrue(emptyObject.allNodes().isEmpty());
    DoEntity emptyEntity = entity.get("emptyEntity", DoEntity.class);
    assertTrue(emptyEntity.allNodes().isEmpty());
  }

  @Test
  public void testSerialize_MixedRawDo() throws Exception {
    TestMixedRawBigIntegerDo dataObject = new TestMixedRawBigIntegerDo();
    dataObject.withBigIntegerAttribute(new BigInteger("123456"));
    dataObject.put("bigDecimalAttribute", new BigDecimal("789.0"));
    dataObject.put("dateAttribute", DateUtility.parse("2017-09-22 14:23:12.123", IValueFormatConstants.DEFAULT_DATE_PATTERN));
    dataObject.withNumberAttribute(42);

    String json = s_dataObjectMapper.writeValueAsString(dataObject);
    assertJsonEquals("TestMixedRawBigIntegerDo.json", json);
  }

  @Test
  public void testDeserialize_MixedRawDo() throws Exception {
    String jsonInput = readResourceAsString("TestMixedRawBigIntegerDo.json");
    TestMixedRawBigIntegerDo mixedEntityDo = s_dataObjectMapper.readValue(jsonInput, TestMixedRawBigIntegerDo.class);

    // known properties have types according to declaration in TestMixedRawBigIntegerDo
    assertEquals(new BigInteger("123456"), mixedEntityDo.getBigIntegerAttribute());
    assertEquals(Integer.valueOf(42), mixedEntityDo.getNumberAttribute());

    // raw properties got default JSON->Java conversion types
    assertEquals(new BigDecimal("789.0"), mixedEntityDo.get("bigDecimalAttribute"));
    assertEquals("2017-09-22 14:23:12.123", mixedEntityDo.get("dateAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(mixedEntityDo);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialize_MixedRawDoAsPlainRaw() throws Exception {
    String jsonInput = readResourceAsString("TestMixedRawBigIntegerDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    // raw properties got default JSON->Java conversion types
    assertEquals(Integer.valueOf(123456), entity.getNode("bigIntegerAttribute").get());
    assertEquals(Integer.valueOf(42), entity.getNode("numberAttribute").get());
    assertEquals(new BigDecimal("789.0"), entity.getNode("bigDecimalAttribute").get());
    assertEquals("2017-09-22 14:23:12.123", entity.getNode("dateAttribute").get());

    // use accessor methods to convert value to specific type
    assertEquals(new BigInteger("123456"), s_dataObjectHelper.getBigIntegerAttribute(entity, "bigIntegerAttribute"));
    assertEquals(Integer.valueOf(42), s_dataObjectHelper.getIntegerAttribute(entity, "numberAttribute"));
    assertEquals(Double.valueOf(789.0), s_dataObjectHelper.getDoubleAttribute(entity, "bigDecimalAttribute"));
    assertEquals(new BigDecimal("789.0"), entity.getDecimal("bigDecimalAttribute"));
    assertEquals("2017-09-22 14:23:12.123", entity.getString("dateAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialize_ComplexEntityDoRaw() throws Exception {
    String jsonInput = readResourceAsString("TestComplexEntityDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    TestComplexEntityDo expected = createTestDo();
    assertEquals(expected.getStringAttribute(), entity.getString("stringAttribute"));
    assertEquals(expected.getIntegerAttribute(), s_dataObjectHelper.getIntegerAttribute(entity, "integerAttribute"));
    assertEquals(expected.getDoubleAttribute(), s_dataObjectHelper.getDoubleAttribute(entity, "doubleAttribute"));
    assertEquals(expected.getStringListAttribute(), entity.getList("stringListAttribute", String.class));

    // floating point values are converted to Double
    assertEquals(expected.getFloatAttribute().floatValue(), entity.get("floatAttribute", BigDecimal.class).floatValue(), 0);
    assertEquals(expected.getBigDecimalAttribute(), entity.get("bigDecimalAttribute", BigDecimal.class));
    assertEquals(expected.getBigDecimalAttribute(), entity.getDecimal("bigDecimalAttribute"));
    // short integer/long values are converted to Integer
    assertEquals(expected.getBigIntegerAttribute(), NumberUtility.toBigInteger(entity.get("bigIntegerAttribute", Integer.class).longValue()));
    assertEquals(expected.getLongAttribute().longValue(), entity.get("longAttribute", Integer.class).longValue());
    // date value is converted to String
    assertEquals(expected.getDateAttribute(), DateUtility.parse(entity.get("dateAttribute", String.class), IValueFormatConstants.TIMESTAMP_PATTERN));
    assertEquals(expected.getDateAttribute(), s_dataObjectHelper.getDateAttribute(entity, "dateAttribute"));

    // UUID value is converted to String
    assertEquals(expected.getUuidAttribute(), s_dataObjectHelper.getUuidAttribute(entity, "uuidAttribute"));

    // Locale value is converted to String
    assertEquals(expected.getLocaleAttribute(), s_dataObjectHelper.getLocaleAttribute(entity, "localeAttribute"));

    // check nested DoEntity
    IDoEntity itemAttribute = s_dataObjectHelper.getEntityAttribute(entity, "itemAttribute");
    assertEquals(expected.getItemAttribute().getId(), itemAttribute.getString("id"));
    assertEquals(expected.getItemAttribute().getStringAttribute(), itemAttribute.getString("stringAttribute"));

    // nested List<DoEntity>
    List<DoEntity> itemsAttribute = entity.getList("itemsAttribute", DoEntity.class);
    assertEquals(expected.getItemsAttribute().get(0).getId(), itemsAttribute.get(0).get("id", String.class));
    assertEquals(expected.getItemsAttribute().get(1).getId(), itemsAttribute.get(1).get("id", String.class));

    // check roundtrip back to JSON
    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialze_EntityWithNestedDoNodeRaw() throws Exception {
    String jsonInput = readResourceAsString("TestEntityWithNestedDoNodeRaw.json");
    TestComplexEntityDo testDo = BEANS.get(TestComplexEntityDo.class);
    testDo.itemAttribute().set(BEANS.get(TestItemDo.class).withId("1234-3").withStringAttribute("bar"));
    String json = s_defaultJacksonObjectMapper.writeValueAsString(testDo); // write using default jackson (does not write _type attribute)
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  /**
   * {@code DoValue<String[]>} read as raw JSON is converted to {@code List<String>}
   */
  @Test
  public void testDeserialize_EntityWithArrayDoValueDoRaw() throws Exception {
    String jsonInput = readResourceAsString("TestEntityWithArrayDoValueDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    final List<String> expected = Arrays.asList("one", "two", "three");

    List<String> valuesWithExplicitType = entity.getList("stringArrayAttribute", String.class);
    assertEquals(expected, valuesWithExplicitType);

    List<String> valuesWithInferedType = entity.getList("stringArrayAttribute", String.class);
    assertEquals(expected, valuesWithInferedType);

    List<DoEntity> itemDoArray = entity.getList("itemDoArrayAttribute", DoEntity.class);
    assertEquals("1", itemDoArray.get(0).get("id"));
    assertEquals("foo", itemDoArray.get(0).get("stringAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialize_EntityWithoutTypeRaw() throws Exception {
    String inputJson = readResourceAsString("TestBigIntegerDoWithoutType.json");
    DoEntity testDo = s_dataObjectMapper.readValue(inputJson, DoEntity.class);

    // BigInteger is converted to integer when read as raw value
    assertEquals(Integer.valueOf(123456), testDo.get("bigIntegerAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBigIntegerDoRaw.json", json); // is written without type information
  }

  @Test
  public void testDeserialize_RawBinaryResource() throws Exception {
    String inputJson = readResourceAsString("TestBinaryResourceDoWithoutType.json");
    DoEntity testDo = s_dataObjectMapper.readValue(inputJson, DoEntity.class);

    assertEquals("MTIz", testDo.get("content"));
    assertEquals(3, testDo.get("contentLength"));
    assertEquals(-1, testDo.get("lastModified")); // becomes an Integer (not long)
    assertEquals("image/jpeg", testDo.get("contentType"));
    assertEquals("unicorn.jpg", testDo.get("filename"));
    assertNull(testDo.get("charset"));
    assertEquals(19726487, testDo.get("fingerprint")); // becomes an Integer (not long)
    assertEquals(false, testDo.get("cachingAllowed"));
    assertEquals(0, testDo.get("cacheMaxAge"));
  }

  // ------------------------------------ Raw data object test cases with type name ------------------------

  @Test
  public void testSerialize_TypedEntity() throws Exception {
    DoEntity typedEntity = BEANS.get(DoEntity.class);
    typedEntity.put(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME, "TestMyCustomType");
    typedEntity.put("date", DATE);
    typedEntity.put("string", "foo");
    typedEntity.put("integer", 42);
    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonEquals("TestMyCustomTypeDo.json", json); // is written with type information
  }

  @Test
  public void testSerialize_EmptyTypedEntity() throws Exception {
    DoEntity typedEntity = BEANS.get(DoEntity.class);
    typedEntity.put(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME, "TestMyCustomTypeEmpty");
    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonEquals("TestMyCustomTypeEmptyDo.json", json); // is written with type information
  }

  @Test
  public void testDeserialize_TypedEntity() throws Exception {
    String inputJson = readResourceAsString("TestMyCustomTypeDo.json");
    DoEntity typedEntity = s_dataObjectMapper.readValue(inputJson, DoEntity.class);
    assertTrue(typedEntity instanceof DoEntity);

    assertEquals("TestMyCustomType", typedEntity.get(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));
    assertEquals("TestMyCustomType", typedEntity.get(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));

    assertEquals(DATE, s_dataObjectHelper.getDateAttribute(typedEntity, "date"));
    assertEquals("foo", typedEntity.get("string"));
    assertEquals(new BigDecimal("42"), typedEntity.getDecimal("integer"));

    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonEquals("TestMyCustomTypeDo.json", json); // is written with type information
  }

  @Test
  public void testDeserialize_EmptyTypedEntity() throws Exception {
    String inputJson = readResourceAsString("TestMyCustomTypeEmptyDo.json");
    DoEntity typedEntity = s_dataObjectMapper.readValue(inputJson, DoEntity.class);
    assertTrue(typedEntity instanceof DoEntity);

    assertEquals("TestMyCustomTypeEmpty", typedEntity.get(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));
    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonEquals("TestMyCustomTypeEmptyDo.json", json); // is written with type information
  }

  // ------------------------------------ DoEntity with list test cases ------------------------------------

  @Test
  public void testEntityWithLists() {
    TestEntityWithListsDo testDo = BEANS.get(TestEntityWithListsDo.class);
    TestItemDo item1 = createTestItemDo("id-1", "foo");
    TestItemDo item2 = createTestItemDo("id-2", "bar");

    // test withItems(...) method
    testDo.withItemsDoListAttribute(item1, item2);
    assertEquals(2, testDo.itemsDoListAttribute().size());
    testDo.getItemsDoListAttribute().remove(1); // check that remove is supported
    assertEquals(1, testDo.itemsDoListAttribute().size());

    // test withItems(List) method
    testDo.withItemsDoListAttribute(Arrays.asList(item1, item2));
    assertEquals(2, testDo.itemsDoListAttribute().size());

    testDo.getItemsDoListAttribute().remove(1); // check that remove is supported
    assertEquals(1, testDo.itemsDoListAttribute().size());

    // test items.set() method
    List<TestItemDo> list = new ArrayList<>();
    list.add(item1);
    list.add(item2);
    testDo.itemsDoListAttribute().set(list);
    assertEquals(2, testDo.itemsDoListAttribute().size());
    testDo.getItemsDoListAttribute().remove(1); // check that remove is supported
    assertEquals(1, testDo.itemsDoListAttribute().size());
  }

  @Test
  public void testSerialize_EntityWithLists() throws Exception {
    TestEntityWithListsDo testDo = new TestEntityWithListsDo();

    List<TestItemDo> list = new ArrayList<>();
    list.add(createTestItemDo("foo-ID-1", "bar-string-attribute-1"));
    list.add(BEANS.get(TestItemDo.class).withId("foo-ID-2").withStringAttribute("bar-string-attribute-2"));
    testDo.withItemsListAttribute(list);

    testDo.withItemsDoListAttribute(
        createTestItemDo("foo-ID-3", "bar-string-attribute-3"),
        createTestItemDo("foo-ID-4", "bar-string-attribute-4"));

    testDo.withStringListAttribute(Arrays.asList("stringA", "stringB"));
    testDo.withStringDoListAttribute("stringC", "stringD");

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithListsDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithLists() throws Exception {
    String json = readResourceAsString("TestEntityWithListsDo.json");
    TestEntityWithListsDo testDo = s_dataObjectMapper.readValue(json, TestEntityWithListsDo.class);
    assertEquals("foo-ID-1", testDo.getItemsListAttribute().get(0).getId());
    assertEquals("foo-ID-2", testDo.getItemsListAttribute().get(1).getId());
    assertEquals("foo-ID-3", testDo.getItemsDoListAttribute().get(0).getId());
    assertEquals("foo-ID-4", testDo.getItemsDoListAttribute().get(1).getId());
    assertEquals("stringA", testDo.getStringListAttribute().get(0));
    assertEquals("stringB", testDo.getStringListAttribute().get(1));
    assertEquals("stringC", testDo.getStringDoListAttribute().get(0));
    assertEquals("stringD", testDo.getStringDoListAttribute().get(1));
  }

  @Test
  public void testSerialize_EntityWithEmptyLists() throws Exception {
    TestEntityWithListsDo testDo = new TestEntityWithListsDo();
    List<TestItemDo> list = new ArrayList<>();
    testDo.withItemsListAttribute(list);
    testDo.withItemsDoListAttribute(list);
    testDo.stringListAttribute().create();
    testDo.stringDoListAttribute().create();
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithEmptyListsDo.json", json);
  }

  @Test
  public void testSerialize_EntityWithOneEmptyList() throws Exception {
    TestEntityWithListsDo testDo = new TestEntityWithListsDo();
    List<TestItemDo> list = new ArrayList<>();
    testDo.withItemsListAttribute(list);
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestEntityWithOneEmptyListDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithEmptyLists() throws Exception {
    String json = readResourceAsString("TestEntityWithEmptyListsDo.json");
    TestEntityWithListsDo testDo = s_dataObjectMapper.readValue(json, TestEntityWithListsDo.class);
    assertTrue(testDo.getItemsListAttribute().isEmpty());
    assertTrue(testDo.getItemsDoListAttribute().isEmpty());
    assertTrue(testDo.stringListAttribute().exists());
    assertNull(testDo.stringListAttribute().get());
    assertTrue(testDo.getStringDoListAttribute().isEmpty());
  }

  @Test
  public void testSerialize_EmptyDoList() throws Exception {
    testSerialize_EmptyDoCollection(new DoList<>());
  }

  @Test
  public void testSerialize_EmptyDoSet() throws Exception {
    testSerialize_EmptyDoCollection(new DoSet<>());
  }

  @Test
  public void testSerialize_EmptyDoCollection() throws Exception {
    testSerialize_EmptyDoCollection(new DoCollection<>());
  }

  protected void testSerialize_EmptyDoCollection(IDoCollection<String, ?> collection) throws JsonProcessingException {
    String json = s_dataObjectMapper.writeValueAsString(collection);
    assertJsonEquals("TestEmptyDoCollection.json", json);
  }

  @Test
  public void testDeserialize_EmptyDoList() throws Exception {
    testDeserialize_EmptyIDoCollection(DoList.class);
  }

  @Test
  public void testDeserialize_EmptyDoSet() throws Exception {
    testDeserialize_EmptyIDoCollection(DoSet.class);
  }

  @Test
  public void testDeserialize_EmptyDoCollection() throws Exception {
    testDeserialize_EmptyIDoCollection(DoCollection.class);
  }

  protected <DO_COLLECTION extends IDoCollection> void testDeserialize_EmptyIDoCollection(Class<DO_COLLECTION> doCollectionClass) throws IOException {
    String json = readResourceAsString("TestEmptyDoCollection.json");
    DO_COLLECTION testDo = s_dataObjectMapper.readValue(json, doCollectionClass);
    assertTrue(testDo.isEmpty());
  }

  @Test
  public void testSerialize_StringDoList() throws Exception {
    testSerialize_StringIDoCollection(new DoList<>());
  }

  @Test
  public void testSerialize_StringDoSet() throws Exception {
    testSerialize_StringIDoCollection(new DoSet<>());
  }

  @Test
  public void testSerialize_StringDoCollection() throws Exception {
    testSerialize_StringIDoCollection(new DoCollection<>());
  }

  protected void testSerialize_StringIDoCollection(IDoCollection<String, ?> collection) throws JsonProcessingException {
    collection.add("foo");
    String json = s_dataObjectMapper.writeValueAsString(collection);
    assertJsonEquals("TestStringDoCollection.json", json);
  }

  @Test
  public void testDeserialize_StringDoList() throws Exception {
    testDeserialize_StringIDoCollection(DoList.class);
  }

  @Test
  public void testDeserialize_StringDoSet() throws Exception {
    testDeserialize_StringIDoCollection(DoSet.class);
  }

  @Test
  public void testDeserialize_StringDoCollection() throws Exception {
    testDeserialize_StringIDoCollection(DoCollection.class);
  }

  protected <DO_COLLECTION extends IDoCollection> void testDeserialize_StringIDoCollection(Class<DO_COLLECTION> doCollectionClass) throws IOException {
    String json = readResourceAsString("TestStringDoCollection.json");
    DO_COLLECTION testDo = s_dataObjectMapper.readValue(json, doCollectionClass);
    assertEquals(1, testDo.size());
    assertEquals("foo", testDo.iterator().next());
  }

  @Test
  public void testSerialize_TestItemDoList() throws Exception {
    testSerialize_TestItemIDoCollection(new DoList<>());
  }

  @Test
  public void testSerialize_TestItemDoSet() throws Exception {
    testSerialize_TestItemIDoCollection(new DoSet<>());
  }

  @Test
  public void testSerialize_TestItemDoCollection() throws Exception {
    testSerialize_TestItemIDoCollection(new DoCollection<>());
  }

  protected void testSerialize_TestItemIDoCollection(IDoCollection<TestItemDo, ?> collection) throws JsonProcessingException {
    collection.add(createTestItemDo("foo", "bar"));
    String json = s_dataObjectMapper.writeValueAsString(collection);
    assertJsonEquals("TestItemDoCollection.json", json);
  }

  @Test
  public void testDeserialize_TestItemDoList() throws Exception {
    //noinspection unchecked
    testDeserialize_TestItemIDoCollection(DoList.class);
  }

  @Test
  public void testDeserialize_TestItemDoSet() throws Exception {
    //noinspection unchecked
    testDeserialize_TestItemIDoCollection(DoSet.class);
  }

  @Test
  public void testDeserialize_TestItemDoCollection() throws Exception {
    //noinspection unchecked
    testDeserialize_TestItemIDoCollection(DoCollection.class);
  }

  protected <DO_COLLECTION extends IDoCollection<TestItemDo, ?>> void testDeserialize_TestItemIDoCollection(Class<DO_COLLECTION> doCollectionClass) throws IOException {
    String json = readResourceAsString("TestItemDoCollection.json");
    @SuppressWarnings("unchecked")
    DO_COLLECTION testDo = s_dataObjectMapper.readValue(json, doCollectionClass);
    assertEquals(1, testDo.size());
    TestItemDo item = testDo.iterator().next();
    assertEquals("foo", item.getId());
    assertEquals("bar", item.getStringAttribute());
  }

  @Test
  public void testDeserialize_TestItemDoListAsObjectList() throws Exception {
    String json = readResourceAsString("TestItemDoCollection.json");
    // read value as raw DoList without concrete bind type information
    DoList<TestItemDo> testDo = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    assertEquals("foo", testDo.get(0).getId());
    assertEquals("bar", testDo.get(0).getStringAttribute());
  }

  // ------------------------------------ Complex DoEntity test cases ------------------------------------

  @Test
  public void testSerialize_ComplexDoEntity() throws Exception {
    TestComplexEntityDo testDo = createTestDo();
    String doJson = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestComplexEntityDo.json", doJson);

    // comparison with plain jackson object mapper and POJO object -> must result in same JSON
    TestComplexEntityPojo testPoJo = createTestPoJo();
    String pojoJson = s_defaultJacksonObjectMapper.writeValueAsString(testPoJo);
    assertJsonEquals("TestComplexEntityDo.json", doJson);
    assertEquals(doJson, pojoJson);
  }

  @Test
  public void testDeserialize_ComplexEntityDo() throws Exception {
    String jsonInput = readResourceAsString("TestComplexEntityDo.json");
    TestComplexEntityDo testDo = s_dataObjectMapper.readValue(jsonInput, TestComplexEntityDo.class);
    TestComplexEntityDo testDoExpected = createTestDo();
    assertEqualsWithComparisonFailure(testDoExpected, testDo);
  }

  @Test
  public void testDeserialize_EntityWithoutType() throws Exception {
    String inputJson = readResourceAsString("TestBigIntegerDoWithoutType.json");
    TestBigIntegerDo testDo = s_dataObjectMapper.readValue(inputJson, TestBigIntegerDo.class);
    assertEquals(new BigInteger("123456"), testDo.getBigIntegerAttribute());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestBigIntegerDo.json", json);
  }

  @Test
  public void testSerializeDeserialize_EntityWithoutTypeNameAnnotation() throws Exception {
    TestWithoutTypeNameDo entity = BEANS.get(TestWithoutTypeNameDo.class)
        .withId("foo")
        .withValue("bar");
    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestWithoutTypeName.json", json);

    TestWithoutTypeNameDo marshalled = s_dataObjectMapper.readValue(json, TestWithoutTypeNameDo.class);
    assertEqualsWithComparisonFailure(entity, marshalled);

    // read without type information results in raw DoEntity
    IDoEntity rawEntity = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(DoEntity.class, rawEntity.getClass());
    String jsonRaw = s_dataObjectMapper.writeValueAsString(rawEntity);
    assertEquals(json, jsonRaw);
  }

  @Test
  public void testSerializeDeserialize_EntityWithoutTypeNameAnnotationSubclass() throws Exception {
    TestWithoutTypeNameDo entity = BEANS.get(TestWithoutTypeNameSubclassDo.class)
        .withId("foo")
        .withValue("bar")
        .withIdSub("sub");
    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestWithoutTypeNameSubclass.json", json);

    TestWithoutTypeNameSubclassDo marshalled = s_dataObjectMapper.readValue(json, TestWithoutTypeNameSubclassDo.class);
    assertEqualsWithComparisonFailure(entity, marshalled);

    TestWithoutTypeNameDo marshalledSuperClass = s_dataObjectMapper.readValue(json, TestWithoutTypeNameDo.class);
    assertEquals(TestWithoutTypeNameDo.class, marshalledSuperClass.getClass());
    String jsonRaw = s_dataObjectMapper.writeValueAsString(marshalledSuperClass);
    assertEquals(json, jsonRaw);

    IDoEntity marshalledSuperInterface = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(DoEntity.class, marshalledSuperInterface.getClass());
    jsonRaw = s_dataObjectMapper.writeValueAsString(marshalledSuperInterface);
    assertEquals(json, jsonRaw);
  }

  @Test
  public void testSerializeDeserialize_EntityWithoutTypeNameAnnotationSubclassWithTypeName() throws Exception {
    TestWithoutTypeNameDo entity = BEANS.get(TestWithoutTypeNameSubclassWithTypeNameDo.class)
        .withId("foo")
        .withValue("bar")
        .withIdSub("sub");
    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestWithoutTypeNameSubclassWithTN.json", json);

    TestWithoutTypeNameDo marshalled = s_dataObjectMapper.readValue(json, TestWithoutTypeNameDo.class);
    assertEqualsWithComparisonFailure(entity, marshalled);
  }

  @Test
  public void testSerializeDeserialize_EntityWithEmptyTypeName() throws Exception {
    TestWithEmptyTypeNameDo entity = BEANS.get(TestWithEmptyTypeNameDo.class)
        .withId("foo2")
        .withValue("bar2");
    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestWithEmptyTypeName.json", json);

    TestWithEmptyTypeNameDo marshalled = s_dataObjectMapper.readValue(json, TestWithEmptyTypeNameDo.class);
    assertEqualsWithComparisonFailure(entity, marshalled);

    IDoEntity marshalledSuperInterface = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(DoEntity.class, marshalledSuperInterface.getClass());
    String jsonRaw = s_dataObjectMapper.writeValueAsString(marshalledSuperInterface);
    assertEquals(json, jsonRaw);
  }

  @Test
  public void testSerializeDeserialize_CollectionsIDoEntityDo() throws Exception {
    Collection<IDoEntity> coll = new ArrayList<>();
    coll.add(createTestItemDo("collection", "1"));
    coll.add(createTestItemDo("collection", "2"));
    TestCollectionsIDoEntityDo entity = BEANS.get(TestCollectionsIDoEntityDo.class)
        .withDoEntityAttribute(createTestItemDo("bar", "1"))
        .withDoEntityCollectionAttribute(coll)
        .withDoEntityDoListAttribute(createTestItemDo("dolist", "1"), createTestItemDo("dolist", "2"))
        .withDoEntityListAttribute(Arrays.asList(createTestItemDo("list", "1"), createTestItemDo("list", "2")));
    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestCollectionsIDoEntity.json", json);
  }

  // ------------------------------------ DoEntity with collections test cases ------------------------------------

  @Test
  public void testSerialize_TestCollectionsDo() throws Exception {
    TestCollectionsDo testDo = createTestCollectionsDo();
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestCollectionsDo.json", json);
  }

  @Test
  public void testSerialize_TestCollectionsDoNullValues() throws Exception {
    TestCollectionsDo testDo = BEANS.get(TestCollectionsDo.class)
        .withItemDoAttribute(null)
        .withItemCollectionAttribute(null)
        .withItemListAttribute(null)
        .withItemPojoAttribute(null)
        .withItemPojoCollectionAttribute(null)
        .withItemPojoListAttribute(null);
    testDo.itemDoListAttribute().set(null);
    testDo.itemPojoDoListAttribute().set(null);
    testDo.itemPojo2DoListAttribute().set(null);
    testDo.itemDoSetAttribute().set(null);
    testDo.itemPojoDoSetAttribute().set(null);
    testDo.itemPojo2DoSetAttribute().set(null);
    testDo.itemDoCollectionAttribute().set(null);
    testDo.itemPojoDoCollectionAttribute().set(null);
    testDo.itemPojo2DoCollectionAttribute().set(null);

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonEquals("TestCollectionsDoNullValuesEmptyDoList.json", json);
  }

  @Test
  public void testDeserialize_TestCollectionsDo() throws Exception {
    String json = readResourceAsString("TestCollectionsDo.json");
    TestCollectionsDo doMarhalled = s_dataObjectMapper.readValue(json, TestCollectionsDo.class);
    TestCollectionsDo expectedDo = createTestCollectionsDo();
    assertEqualsWithComparisonFailure(expectedDo, doMarhalled);
  }

  @Test
  public void testDeserialize_TestCollectionsDoNullValues() throws Exception {
    String json = readResourceAsString("TestCollectionsDoNullValues.json");
    TestCollectionsDo doMarhalled = s_dataObjectMapper.readValue(json, TestCollectionsDo.class);

    assertNull(doMarhalled.getItemDoAttribute());
    assertNull(doMarhalled.getItemCollectionAttribute());
    assertNull(doMarhalled.getItemListAttribute());
    assertNull(doMarhalled.getItemPojoAttribute());
    assertNull(doMarhalled.getItemPojoCollectionAttribute());
    assertNull(doMarhalled.getItemPojoListAttribute());
    assertTrue(doMarhalled.getItemDoListAttribute().isEmpty());
    assertTrue(doMarhalled.getItemPojoDoListAttribute().isEmpty());
    assertTrue(doMarhalled.getItemPojo2DoListAttribute().isEmpty());
    assertTrue(doMarhalled.getItemDoSetAttribute().isEmpty());
    assertTrue(doMarhalled.getItemPojoDoSetAttribute().isEmpty());
    assertTrue(doMarhalled.getItemPojo2DoSetAttribute().isEmpty());
    assertTrue(doMarhalled.getItemDoCollectionAttribute().isEmpty());
    assertTrue(doMarhalled.getItemPojoDoCollectionAttribute().isEmpty());
    assertTrue(doMarhalled.getItemPojo2DoCollectionAttribute().isEmpty());

    json = s_dataObjectMapper.writeValueAsString(doMarhalled);
    assertJsonEquals("TestCollectionsDoNullValuesEmptyDoList.json", json);
  }

  @Test
  public void testSerializeDeserialize_TestCollectionsDoRaw() throws Exception {
    String json = readResourceAsString("TestCollectionsDoRaw.json");
    DoEntity doMarhalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    TestCollectionsDo expectedDo = createTestCollectionsDo();
    assertEquals(expectedDo.getItemDoAttribute().getId(), doMarhalled.get("itemDoAttribute", DoEntity.class).get("id"));

    List<DoEntity> list = doMarhalled.getList("itemDoListAttribute", DoEntity.class);
    assertEquals(expectedDo.getItemDoListAttribute().get(0).getId(), list.get(0).get("id"));

    String serialized = s_dataObjectMapper.writeValueAsString(doMarhalled);
    s_testHelper.assertJsonEquals(json, serialized);
  }

  protected TestCollectionsDo createTestCollectionsDo() {
    TestCollectionsDo testDo = BEANS.get(TestCollectionsDo.class);

    // setup TestItemDo attributes
    testDo.withItemDoAttribute(createTestItemDo("d1", "itemDo-as-attribute"));
    testDo.withItemCollectionAttribute(Arrays.asList(
        createTestItemDo("d2", "itemDo-as-collection-item-1"),
        createTestItemDo("d3", "itemDo-as-collection-item-2"),
        null)); // test with null value in collection
    testDo.withItemListAttribute(Arrays.asList(createTestItemDo("d4", "itemDo-as-list-item-1"), createTestItemDo("d5", "itemDo-as-list-item-2")));
    testDo.withItemDoListAttribute(createTestItemDo("d8", "itemDo-as-DoList-item-1"), createTestItemDo("d9", "itemDo-as-DoList-item-2"));
    testDo.withItemDoSetAttribute(createTestItemDo("d10", "itemDo-as-DoSet-item-1"), createTestItemDo("d11", "itemDo-as-DoSet-item-2"));
    testDo.withItemDoCollectionAttribute(createTestItemDo("d12", "itemDo-as-DoCollection-item-1"), createTestItemDo("d13", "itemDo-as-DoCollection-item-2"));

    // setup TestItemPojo attributes
    testDo.withItemPojoAttribute(createTestItemPojo("p1", "itemPojo-as-attribute"));
    testDo.withItemPojoCollectionAttribute(Arrays.asList(createTestItemPojo("p2", "itemPojo-as-collection-item-1"), createTestItemPojo("p3", "itemPojo-as-collection-item-2")));
    testDo.withItemPojoListAttribute(Arrays.asList(createTestItemPojo("p4", "itemPojo-as-list-item-1"), createTestItemPojo("p5", "itemPojo-as-list-item-2")));
    testDo.withItemPojoDoListAttribute(createTestItemPojo("p8", "itemPojo-as-DoList-item-1"), createTestItemPojo("p9", "itemPojo-as-DoList-item-2"));
    testDo.withItemPojo2DoListAttribute(createTestItemPojo2("p10", "itemPojo2-as-DoList-item-1"), createTestItemPojo2("p11", "itemPojo2-as-DoList-item-2"));
    testDo.withItemPojoDoSetAttribute(createTestItemPojo("p12", "itemPojo-as-DoSet-item-1"), createTestItemPojo("p13", "itemPojo-as-DoSet-item-2"));
    testDo.withItemPojo2DoSetAttribute(createTestItemPojo2("p14", "itemPojo2-as-DoSet-item-1"), createTestItemPojo2("p15", "itemPojo2-as-DoSet-item-2"));
    testDo.withItemPojoDoCollectionAttribute(createTestItemPojo("p16", "itemPojo-as-DoCollection-item-1"), createTestItemPojo("p17", "itemPojo-as-DoCollection-item-2"));
    testDo.withItemPojo2DoCollectionAttribute(createTestItemPojo2("p18", "itemPojo2-as-DoCollection-item-1"), createTestItemPojo2("p19", "itemPojo2-as-DoCollection-item-2"));

    // setup abstract/interface attributes
    testDo.withItemDoListAbstractAttribute(Arrays.asList(
        BEANS.get(TestElectronicAddressDo.class).withId("elecAddress").withEmail("foo@bar.de"), BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress").withCity("Example")));
    testDo.withItemDoListInterfaceAttribute(Arrays.asList(
        BEANS.get(TestElectronicAddressDo.class).withId("elecAddress").withEmail("foo@bar.de"), BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress").withCity("Example")));
    testDo.withItemListAbstractAttribute(Arrays.asList(
        BEANS.get(TestElectronicAddressDo.class).withId("elecAddress").withEmail("foo@bar.de"), BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress").withCity("Example")));
    testDo.withItemListInterfaceAttribute(Arrays.asList(
        BEANS.get(TestElectronicAddressDo.class).withId("elecAddress").withEmail("foo@bar.de"), BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress").withCity("Example")));
    return testDo;
  }

  @Test
  public void testSerializeDeserialize_EntityWithCollectionRaw() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("attribute1", Arrays.asList("list-item-1", "list-item-2"));
    entity.put("attribute2", Arrays.asList(123, 45.69));
    entity.put("attribute3", Arrays.asList(UUID_1, UUID_2));
    entity.put("attribute4", Arrays.asList(DATE, DATE_TRUNCATED));
    entity.put("attribute5", Arrays.asList(createTestItemDo("item-do-key-1", "item-do-value-1"), createTestItemDo("item-do-key-2", "item-do-value-2")));
    entity.put("attribute6", Arrays.asList(createTestItemDo("item-do-key-3", "item-do-value-3"), "bar"));
    entity.put("attribute7", Arrays.asList(createTestItemPojo2("item-pojo-key-1", "item-pojo-value-1"), createTestItemPojo2("item-pojo-key-2", "item-pojo-value-2")));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestEntityWithCollectionRaw.json", json);

    DoEntity doMarshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals("list-item-1", doMarshalled.get("attribute1", List.class).get(0));
    assertEquals("list-item-2", doMarshalled.get("attribute1", List.class).get(1));

    assertEquals(123, doMarshalled.get("attribute2", List.class).get(0));
    assertEquals(new BigDecimal("45.69"), doMarshalled.get("attribute2", List.class).get(1));

    List<UUID> attribute3 = doMarshalled.getList("attribute3", item -> UUID.fromString((String) item));
    assertEquals(UUID_1, attribute3.get(0));
    assertEquals(UUID_2, attribute3.get(1));

    List<Date> attribute4 = doMarshalled.getList("attribute4", IValueFormatConstants.parseDefaultDate);
    assertEquals(DATE, attribute4.get(0));
    assertEquals(DATE_TRUNCATED, attribute4.get(1));

    List<TestItemDo> attribute5 = doMarshalled.getList("attribute5", TestItemDo.class);
    assertEquals("item-do-key-1", attribute5.get(0).getId());
    assertEquals("item-do-key-2", attribute5.get(1).getId());

    List<Object> attribute6 = doMarshalled.getList("attribute6");
    assertEquals("item-do-key-3", ((TestItemDo) attribute6.get(0)).getId());
    assertEquals("bar", attribute6.get(1));

    // TestItemPojo2 is deserialized as DoTypedEntity with type 'TestItem2'
    List<DoEntity> attribute7 = doMarshalled.getList("attribute7", DoEntity.class);
    assertEquals("item-pojo-key-1", attribute7.get(0).get("id"));
    assertEquals("TestItem2", attribute7.get(0).get(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));
    assertEquals("item-pojo-key-2", attribute7.get(1).get("id"));
    assertEquals("TestItem2", attribute7.get(1).get(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME));
  }

  @Test
  public void testSerializeDeserialize_TestMapDo() throws Exception {
    TestMapDo mapDo = new TestMapDo();
    Map<String, String> stringStringMap = new HashMap<>();
    stringStringMap.put("foo1", "bar");
    stringStringMap.put("foo2", "baz");
    stringStringMap.put("foo3", null); // test for a null value as string
    mapDo.withStringStringMapAttribute(stringStringMap);

    Map<Integer, Integer> integerIntegerMap = new HashMap<>();
    integerIntegerMap.put(1, 42);
    integerIntegerMap.put(2, 21);
    mapDo.withIntegerIntegerMapAttribute(integerIntegerMap);

    Map<String, TestItemPojo> stringPojoMap = new HashMap<>();
    stringPojoMap.put("pojoKey1", createTestItemPojo("item-key1", "value1"));
    stringPojoMap.put("pojoKey2", createTestItemPojo("item-key2", "value2"));
    mapDo.withStringTestItemPojoMapAttribute(stringPojoMap);

    Map<String, TestItemDo> stringDoMap = new HashMap<>();
    stringDoMap.put("doKey1", createTestItemDo("item-key3", "value3"));
    stringDoMap.put("doKey2", createTestItemDo("item-key4", "value4"));
    stringDoMap.put("doKey3", null); // test for a null value as data object
    mapDo.withStringDoTestItemMapAttribute(stringDoMap);

    Map<String, AbstractTestAddressDo> stringAbstractDoMap = new HashMap<>();
    stringAbstractDoMap.put("doAbstractKey1", BEANS.get(TestElectronicAddressDo.class).withId("elecAddress").withEmail("foo@bar.de"));
    stringAbstractDoMap.put("doAbstractKey2", BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress").withCity("Example"));
    mapDo.withStringDoAbstractAddressMapAttribute(stringAbstractDoMap);

    Map<Double, TestItemDo> doubleDoMap = new HashMap<>();
    doubleDoMap.put(1.11, createTestItemDo("item-key5", "value5"));
    doubleDoMap.put(2.22, createTestItemDo("item-key6", "value6"));
    mapDo.withDoubleTestItemDoMapAttribute(doubleDoMap);

    Map<Date, UUID> dateUUIDMap = new LinkedHashMap<>();
    dateUUIDMap.put(DATE, UUID_1);
    dateUUIDMap.put(DATE_TRUNCATED, UUID_2);
    mapDo.withDateUUIDMapAttribute(dateUUIDMap);

    Locale deCh = new Locale("de", "CH");
    Locale enUs = new Locale("en", "US");
    Locale frCh = new Locale("fr", "CH");
    Locale en = new Locale("en");
    Map<Locale, Locale> localeLocaleMap = new LinkedHashMap<>();
    localeLocaleMap.put(deCh, frCh);
    localeLocaleMap.put(enUs, en);
    mapDo.withLocaleLocaleMapAttribute(localeLocaleMap);

    Map<String, Map<String, List<TestItemDo>>> complexMap = new HashMap<>();
    complexMap.put("complex1", CollectionUtility.hashMap(
        ImmutablePair.of("complex1-inner-1", List.of(
            createTestItemDo("complex1-inner-1-list-1", "complex1-inner-1-list-1-attribute"),
            createTestItemDo("complex1-inner-1-list-2", "complex1-inner-1-list-2-attribute"))),
        ImmutablePair.of("complex1-inner-2", List.of(
            createTestItemDo("complex1-inner-2-list-1", "complex1-inner-2-list-1-attribute"),
            createTestItemDo("complex1-inner-2-list-2", "complex1-inner-2-list-2-attribute")))));
    complexMap.put("complex2", CollectionUtility.hashMap(
        ImmutablePair.of("complex2-inner-1", List.of(
            createTestItemDo("complex2-inner-1-list-1", "complex2-inner-1-list-1-attribute"),
            createTestItemDo("complex2-inner-1-list-2", "complex2-inner-1-list-2-attribute"))),
        ImmutablePair.of("complex2-inner-2", List.of(
            createTestItemDo("complex2-inner-2-list-1", "complex2-inner-2-list-1-attribute"),
            createTestItemDo("complex2-inner-2-list-2", "complex2-inner-2-list-2-attribute")))));
    mapDo.withStringMapStringMapStringListTestItemDoMapAttribute(complexMap);

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonEquals("TestMapDo.json", json);

    TestMapDo marshalled = s_dataObjectMapper.readValue(json, TestMapDo.class);
    assertEqualsWithComparisonFailure(mapDo, marshalled);
  }

  /**
   * Illegal case: using a DO entity as a key in a map.
   */
  @Test
  public void testSerialize_illegalKeyTypeMap() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    Map<TestItemDo, String> illegalKeyTypeMap = new HashMap<>();
    illegalKeyTypeMap.put(createTestItemDo("key", "value"), "foo");
    entity.put("mapAttribute", illegalKeyTypeMap);
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    DoEntity marshalledMapAttribute = marshalled.get("mapAttribute", DoEntity.class);
    assertEquals(illegalKeyTypeMap.values().iterator().next(), marshalledMapAttribute.allNodes().values().iterator().next().get());
    assertNotEquals(illegalKeyTypeMap.keySet().iterator().next(), marshalledMapAttribute.allNodes().keySet().iterator().next()); // TestItemDo cannot be used as key, is serialized using toString() default serializer
  }

  /**
   * Illegal case: using a pojo as a key in a map.
   */
  @Test
  public void testSerialize_illegalKeyTypeMap2() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    Map<TestStringPojo, String> illegalKeyTypeMap = new HashMap<>();
    illegalKeyTypeMap.put(new TestStringPojo().withString("id"), "foo");
    entity.put("mapAttribute", illegalKeyTypeMap);
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    DoEntity marshalledMapAttribute = marshalled.get("mapAttribute", DoEntity.class);
    assertEquals(illegalKeyTypeMap.values().iterator().next(), marshalledMapAttribute.allNodes().values().iterator().next().get());
    assertNotEquals(illegalKeyTypeMap.keySet().iterator().next(), marshalledMapAttribute.allNodes().keySet().iterator().next()); // TestItemDo cannot be used as key, is serialized using toString() default serializer
  }

  @Test(expected = JsonMappingException.class)
  public void testSerialize_nullKeyMap() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("mapAttribute", Collections.singletonMap(null, "foo"));
    s_dataObjectMapper.writeValueAsString(entity);
  }

  @Test
  public void testSerializeDeserialize_EntityWithMapRaw() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("mapAttribute1", Collections.singletonMap("key", "value"));
    entity.put("mapAttribute2", Collections.singletonMap(123, 45.69));
    entity.put("mapAttribute3", Collections.singletonMap(UUID_1, DATE));
    entity.put("mapAttribute4", Collections.singletonMap("foo", createTestItemDo("key", "value")));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestEntityWithMapRaw.json", json);

    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    // raw Map attributes are deserialized as DoEntity, since no concrete type information about correct Map type is available
    DoEntity attribute1 = marshalled.get("mapAttribute1", DoEntity.class);
    assertEquals("value", attribute1.get("key"));
    DoEntity attribute2 = marshalled.get("mapAttribute2", DoEntity.class);
    assertEquals(new BigDecimal("45.69"), attribute2.get("123"));
    DoEntity attribute3 = marshalled.get("mapAttribute3", DoEntity.class);
    assertEquals(DATE, IValueFormatConstants.parseDefaultDate.apply(attribute3.get(UUID_1.toString())));
    DoEntity attribute4 = marshalled.get("mapAttribute4", DoEntity.class);
    assertEquals("key", attribute4.get("foo", TestItemDo.class).getId());
    assertEquals("value", attribute4.get("foo", TestItemDo.class).getStringAttribute());
  }

  @Test
  public void testSerializeDeserialize_TestSetDo() throws Exception {
    TestSetDo setDo = createTestSetDo();
    String json = s_dataObjectMapper.writeValueAsString(setDo);
    assertJsonEquals("TestSetDo.json", json);

    TestSetDo marshalled = s_dataObjectMapper.readValue(json, TestSetDo.class);

    // set with primitive type must be unordered equals
    assertTrue(CollectionUtility.equalsCollection(setDo.getDateSetAttribute(), marshalled.getDateSetAttribute(), false));
    assertTrue(CollectionUtility.equalsCollection(setDo.getIntegerSetAttribute(), marshalled.getIntegerSetAttribute(), false));
    assertTrue(CollectionUtility.equalsCollection(setDo.getStringSetAttribute(), marshalled.getStringSetAttribute(), false));

    // set of TestItemDo must be unordered equals
    List<TestItemDo> expected = new ArrayList<>(setDo.getItemDoSetAttribute());
    expected.sort(Comparator.comparing(TestItemDo::getId));
    List<TestItemDo> actual = new ArrayList<>(marshalled.getItemDoSetAttribute());
    actual.sort(Comparator.comparing(TestItemDo::getId));
    for (int i = 0; i < expected.size(); i++) {
      assertEqualsWithComparisonFailure(expected.get(i), actual.get(i));
    }

    // set of TestItemPojo must be unordered equals
    List<TestItemPojo> expectedPojo = new ArrayList<>(setDo.getItemPojoSetAttribute());
    expectedPojo.sort(Comparator.comparing(TestItemPojo::getId));
    List<TestItemPojo> actualPojo = new ArrayList<>(marshalled.getItemPojoSetAttribute());
    actualPojo.sort(Comparator.comparing(TestItemPojo::getId));
    for (int i = 0; i < expectedPojo.size(); i++) {
      assertEquals(expectedPojo.get(i).getId(), actualPojo.get(i).getId());
      assertEquals(expectedPojo.get(i).getStringAttribute(), actualPojo.get(i).getStringAttribute());
    }
  }

  @Test
  public void testSerializeDeserialize_TestSetDoRaw() throws Exception {
    String json = readResourceAsString("TestSetDoRaw.json");
    DoEntity doMarhalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    TestSetDo expectedDo = createTestSetDo();
    assertTrue(CollectionUtility.equalsCollection(expectedDo.getStringSetAttribute(), doMarhalled.getList("stringSetAttribute"), false));
    assertTrue(CollectionUtility.equalsCollection(expectedDo.getIntegerSetAttribute(), doMarhalled.getList("integerSetAttribute"), false));

    String serialized = s_dataObjectMapper.writeValueAsString(doMarhalled);
    s_testHelper.assertJsonEquals(json, serialized);
  }

  protected TestSetDo createTestSetDo() {
    TestSetDo setDo = new TestSetDo();
    Set<String> stringSet = new LinkedHashSet<>();
    stringSet.add("foo");
    stringSet.add("bar");
    setDo.withStringSetAttribute(stringSet);

    Set<Integer> integerSet = new LinkedHashSet<>();
    integerSet.add(21);
    integerSet.add(42);
    setDo.withIntegerSetAttribute(integerSet);

    Set<TestItemPojo> pojoSet = new LinkedHashSet<>();
    pojoSet.add(createTestItemPojo("item-key1", "value1"));
    pojoSet.add(createTestItemPojo("item-key2", "value2"));
    setDo.withItemPojoSetAttribute(pojoSet);

    Set<TestItemDo> doSet = new LinkedHashSet<>();
    doSet.add(createTestItemDo("item-key3", "value3"));
    doSet.add(createTestItemDo("item-key4", "value4"));
    setDo.withItemDoSetAttribute(doSet);

    Set<Date> dateSet = new LinkedHashSet<>();
    dateSet.add(DATE);
    dateSet.add(DATE_TRUNCATED);
    setDo.withDateSetAttribute(dateSet);
    return setDo;
  }

  @Test
  public void testSerializeDeserialize_EntityWithSetRaw() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    Set<String> stringSet = new LinkedHashSet<>();
    stringSet.add("foo");
    stringSet.add("bar");
    entity.put("setAttribute", stringSet);
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    // Set is deserialized as list if no type information is available
    List<String> setAttribute = marshalled.getStringList("setAttribute");
    assertTrue(CollectionUtility.equalsCollection(stringSet, setAttribute, false));
  }

  @Test
  public void testSerializeDeserialize_TestSetDoWithDuplicateValues() throws Exception {
    // simulate JSON for TestSetDo which contains duplicated values for stringSetAttribute
    DoEntity setDo = BEANS.get(DoEntity.class);
    setDo.put(ScoutDataObjectModule.DEFAULT_TYPE_ATTRIBUTE_NAME, "TestSet");
    List<String> stringList = new ArrayList<>();
    stringList.add("foo");
    stringList.add("bar");
    stringList.add("foo");
    setDo.put("stringSetAttribute", stringList);

    String json = s_dataObjectMapper.writeValueAsString(setDo);
    TestSetDo marshalled = s_dataObjectMapper.readValue(json, TestSetDo.class);

    // duplicated values are eliminated when deserializing JSON to TestSetDo since stringSetAttribute has Set as java type
    assertEquals(2, marshalled.getStringSetAttribute().size());
    assertTrue(marshalled.getStringSetAttribute().contains("foo"));
    assertTrue(marshalled.getStringSetAttribute().contains("bar"));
  }

  // ------------------------------------ DoMapEntity test cases ------------------------------------

  @Test
  public void testSerializeDeserialize_DoMapString() throws Exception {
    TestDoMapStringDo mapDo = BEANS.get(TestDoMapStringDo.class);
    mapDo.put("mapAttribute1", "foo");
    mapDo.put("mapAttribute2", "bar");
    mapDo.withCount(42);

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonEquals("TestDoMapStringDo.json", json);

    TestDoMapStringDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapStringDo.class);
    assertEqualsWithComparisonFailure(mapDo, marshalled);

    assertEquals("foo", marshalled.get("mapAttribute1"));
    assertEquals("bar", marshalled.get("mapAttribute2"));
    assertEquals(Integer.valueOf(42), marshalled.getCount());
  }

  @Test
  public void testSerializeDeserialize_DoMapEntity() throws Exception {
    TestDoMapEntityDo mapDo = BEANS.get(TestDoMapEntityDo.class);
    mapDo.put("mapAttribute1", createTestItemDo("id-1", "value-1"));
    mapDo.put("mapAttribute2", createTestItemDo("id-2", "value-2"));
    mapDo.withCount(42);
    mapDo.withNamedItem(createTestItemDo("namedItem", "value-named"));
    mapDo.withNamedItem3(createTestItem3Do("namedItem3", new BigDecimal("42")));

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonEquals("TestDoMapEntityDo.json", json);

    TestDoMapEntityDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapEntityDo.class);
    assertEqualsWithComparisonFailure(mapDo, marshalled);

    assertEquals("value-named", marshalled.getNamedItem().getStringAttribute());
    assertEquals("value-named", marshalled.get("namedItem").getStringAttribute());
    assertEquals(new BigDecimal("42"), marshalled.getNamedItem3().getBigDecimalAttribute());
  }

  @Test
  public void testSerializeDeserialize_DoMapObject_DoEntity() throws Exception {
    TestDoMapObjectDo mapDo = BEANS.get(TestDoMapObjectDo.class);
    mapDo.put("mapAttribute1", createTestItemDo("id-1", "value-1"));
    mapDo.put("mapAttribute2", createTestItemDo("id-2", "value-2"));
    mapDo.withCount(42);
    mapDo.withNamedItem(createTestItemDo("namedItem", "value-named"));
    mapDo.withNamedItem3(createTestItem3Do("namedItem3", new BigDecimal("42")));

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonEquals("TestDoMapObject_DoEntityDo.json", json);

    TestDoMapObjectDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapObjectDo.class);
    assertEqualsWithComparisonFailure(mapDo, marshalled);

    assertEquals("value-named", marshalled.getNamedItem().getStringAttribute());
    assertEquals(new BigDecimal("42"), marshalled.getNamedItem3().getBigDecimalAttribute());
    assertEquals(Integer.valueOf(42), marshalled.getCount());

    assertEquals("value-1", marshalled.get("mapAttribute1", TestItemDo.class).getStringAttribute());
    assertEquals("id-2", marshalled.get("mapAttribute2", TestItemDo.class).getId());
  }

  @Test
  public void testSerializeDeserialize_DoMapObject_ListEntity() throws Exception {
    TestDoMapObjectDo mapDo = BEANS.get(TestDoMapObjectDo.class);
    mapDo.putList("mapAttribute1", List.of(createTestItemDo("id-1", "value-1"), createTestItemDo("id-2", "value-2")));
    mapDo.withCount(42);
    mapDo.withNamedItem(createTestItemDo("namedItem", "value-named"));
    mapDo.withNamedItem3(createTestItem3Do("namedItem3", new BigDecimal("42")));

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonEquals("TestDoMapObject_ListEntityDo.json", json);

    TestDoMapObjectDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapObjectDo.class);
    assertEqualsWithComparisonFailure(mapDo, marshalled);

    assertEquals("value-named", marshalled.getNamedItem().getStringAttribute());
    assertEquals(new BigDecimal("42"), marshalled.getNamedItem3().getBigDecimalAttribute());
    assertEquals(Integer.valueOf(42), marshalled.getCount());

    @SuppressWarnings("unchecked")
    List<TestItemDo> attribute1 = (List<TestItemDo>) marshalled.get("mapAttribute1", List.class);
    assertEquals("value-1", attribute1.get(0).getStringAttribute());
    assertEquals("id-2", attribute1.get(1).getId());
  }

  @Test
  public void testSerializeDeserialize_DoMapEntityRaw() throws Exception {
    DoEntity mapDo = BEANS.get(DoEntity.class);
    mapDo.put("mapAttribute1", createTestItemDo("id-1", "value-1"));
    mapDo.put("mapAttribute2", createTestItemDo("id-2", "value-2"));
    String json = s_dataObjectMapper.writeValueAsString(mapDo); // write TestDoMapEntityDo as DoEntity to exclude writing the _type property

    // read object as raw Map<String, String> object
    TypeReference<Map<String, TestItemDo>> typeRef = new TypeReference<>() {
    };
    Map<String, TestItemDo> marshalled = s_dataObjectMapper.readValue(json, typeRef);
    assertEqualsWithComparisonFailure(mapDo.get("mapAttribute1", IDoEntity.class), marshalled.get("mapAttribute1"));
    assertEqualsWithComparisonFailure(mapDo.get("mapAttribute2", IDoEntity.class), marshalled.get("mapAttribute2"));
  }

  @Test
  public void testSerializeDeserialize_DoMapEntityRaw2() throws Exception {
    String json = readResourceAsString("TestDoMapEntityDoRaw.json");
    TestDoMapEntityDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapEntityDo.class);

    assertEquals("value-1", marshalled.get("mapAttribute1").getStringAttribute());
    assertEquals("value-2", marshalled.get("mapAttribute2").getStringAttribute());
    assertEquals("value-named", marshalled.getNamedItem().getStringAttribute());
    assertEquals(new BigDecimal("42"), marshalled.getNamedItem3().getBigDecimalAttribute());
    assertEquals(Integer.valueOf(42), marshalled.getCount());
  }

  @Test(expected = ClassCastException.class)
  public void testSerializeDeserialize_DoMapEntity_illegalAccess() {
    TestDoMapEntityDo mapDo = BEANS.get(TestDoMapEntityDo.class);
    mapDo.put("namedItem3", createTestItemDo("id-1", "value-1"));
    mapDo.getNamedItem3(); // access item with type TestItem3 which was filled with a wrong TestItemDo value
  }

  @Test
  public void testSerializeDeserialize_DoMapListEntity() throws Exception {
    TestDoMapListEntityDo mapDo = BEANS.get(TestDoMapListEntityDo.class);
    mapDo.put("mapAttribute1", CollectionUtility.arrayList(createTestItemDo("id-1a", "value-1a"), createTestItemDo("id-1b", "value-1b")));
    mapDo.put("mapAttribute2", CollectionUtility.arrayList(createTestItemDo("id-2a", "value-2a"), createTestItemDo("id-2b", "value-2b")));
    mapDo.withCount(42);

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonEquals("TestDoMapListEntityDo.json", json);

    TestDoMapListEntityDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapListEntityDo.class);
    assertEqualsWithComparisonFailure(mapDo, marshalled);

    assertEquals("value-1a", marshalled.get("mapAttribute1").get(0).getStringAttribute());
    assertEquals("value-2b", marshalled.get("mapAttribute2").get(1).getStringAttribute());
    assertEquals(Integer.valueOf(42), marshalled.getCount());
  }

  @Test
  public void testSerializeDeserialize_DoMapListEntityRaw() throws Exception {
    DoEntity mapDo = BEANS.get(DoEntity.class);
    mapDo.put("mapAttribute1", CollectionUtility.arrayList(createTestItemDo("id-1a", "value-1a"), createTestItemDo("id-1b", "value-1b")));
    mapDo.put("mapAttribute2", CollectionUtility.arrayList(createTestItemDo("id-2a", "value-2a"), createTestItemDo("id-2b", "value-2b")));
    String json = s_dataObjectMapper.writeValueAsString(mapDo); // write TestDoMapEntityDo as DoEntity to exclude writing the _type property

    // read object as raw Map<String, String> object
    TypeReference<Map<String, List<TestItemDo>>> typeRef = new TypeReference<>() {
    };
    Map<String, List<TestItemDo>> marshalled = s_dataObjectMapper.readValue(json, typeRef);
    assertEquals("value-1a", marshalled.get("mapAttribute1").get(0).getStringAttribute());
    assertEquals("value-2b", marshalled.get("mapAttribute2").get(1).getStringAttribute());

    assertEqualsWithComparisonFailure(mapDo.get("mapAttribute1", List.class).get(0), marshalled.get("mapAttribute1").get(0));
    assertEqualsWithComparisonFailure(mapDo.get("mapAttribute2", List.class).get(1), marshalled.get("mapAttribute2").get(1));
  }

  @Test
  public void testSerializeDeserialize_DoMapListEntityRaw2() throws Exception {
    String json = readResourceAsString("TestDoMapListEntityDoRaw.json");
    TestDoMapListEntityDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapListEntityDo.class);

    assertEquals("value-1a", marshalled.get("mapAttribute1").get(0).getStringAttribute());
    assertEquals("value-2b", marshalled.get("mapAttribute2").get(1).getStringAttribute());
    assertEquals(Integer.valueOf(42), marshalled.getCount());
  }

  @Test
  public void testSerializeDeserialize_DoMapDoMapString() throws Exception {
    TestDoMapStringDo mapDo = BEANS.get(TestDoMapStringDo.class);
    mapDo.put("mapAttribute1", "foo");
    mapDo.put("mapAttribute2", "bar");
    mapDo.withCount(42);

    TestDoMapStringDo map2Do = BEANS.get(TestDoMapStringDo.class);
    map2Do.put("mapAttribute1", "baz");
    map2Do.put("mapAttribute2", "buz");
    map2Do.withCount(13);

    TestDoMapDoMapStringDo mapMapDo = BEANS.get(TestDoMapDoMapStringDo.class);
    mapMapDo.put("mapMap1", mapDo);
    mapMapDo.put("mapMap2", map2Do);
    mapMapDo.withCount(142);
    mapMapDo.withNamedItem(createTestItemDo("namedItemInMap", "value-named-in-map"));
    mapMapDo.withNamedItem3(createTestItem3Do("namedItem3", new BigDecimal("420")));

    String json = s_dataObjectMapper.writeValueAsString(mapMapDo);
    assertJsonEquals("TestDoMapDoMapStringDo.json", json);

    TestDoMapDoMapStringDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapDoMapStringDo.class);
    assertEqualsWithComparisonFailure(mapMapDo, marshalled);

    assertEquals("value-named-in-map", marshalled.getNamedItem().getStringAttribute());
    assertEquals(mapDo, marshalled.get("mapMap1"));
    assertEquals(map2Do, marshalled.get("mapMap2"));
    assertEquals("foo", marshalled.get("mapMap1").get("mapAttribute1"));
    assertEquals("buz", marshalled.get("mapMap2").get("mapAttribute2"));
    assertEquals(new BigDecimal("420"), marshalled.getNamedItem3().getBigDecimalAttribute());
  }

  @Test
  public void testSerializeDeserialize_DoMapDoMapEntity() throws Exception {
    TestDoMapEntityDo mapDo = BEANS.get(TestDoMapEntityDo.class);
    mapDo.put("mapAttribute1", createTestItemDo("id-1", "value-1"));
    mapDo.put("mapAttribute2", createTestItemDo("id-2", "value-2"));
    mapDo.withCount(42);
    mapDo.withNamedItem(createTestItemDo("namedItem", "value-named"));
    mapDo.withNamedItem3(createTestItem3Do("namedItem3", new BigDecimal("42")));

    TestDoMapDoMapEntityDo mapMapDo = BEANS.get(TestDoMapDoMapEntityDo.class);
    mapMapDo.put("mapMap1", mapDo);
    mapMapDo.put("mapMap2", mapDo);
    mapMapDo.withCount(142);
    mapMapDo.withNamedItem(createTestItemDo("namedItemInMap", "value-named-in-map"));
    mapMapDo.withNamedItem3(createTestItem3Do("namedItem3", new BigDecimal("420")));

    String json = s_dataObjectMapper.writeValueAsString(mapMapDo);
    assertJsonEquals("TestDoMapDoMapEntityDo.json", json);

    TestDoMapDoMapEntityDo marshalled = s_dataObjectMapper.readValue(json, TestDoMapDoMapEntityDo.class);
    assertEqualsWithComparisonFailure(mapMapDo, marshalled);

    assertEquals("value-named-in-map", marshalled.getNamedItem().getStringAttribute());
    assertEquals(mapDo, marshalled.get("mapMap1"));
    assertEquals(mapDo, marshalled.get("mapMap2"));
    assertEquals("value-named", marshalled.get("mapMap1").get("namedItem").getStringAttribute());
    assertEquals(new BigDecimal("420"), marshalled.getNamedItem3().getBigDecimalAttribute());
  }

  // ------------------------------------ polymorphic test cases ------------------------------------

  /**
   * <pre>
                +--------------------+
                | AbstractAddressDo  |
                +---------^----------+
                          |
              +-----------+--------------+
              |                          |
     +--------+-----------+   +----------+---------+
     |ElectronicAddressDo |   | PhysicalAddressDo  |
     +--------------------+   +--------------------+
   * </pre>
   */
  @Test
  public void testSerialize_TestPersonDo() throws Exception {
    TestPersonDo personDo = createTestPersonDo();
    String json = s_dataObjectMapper.writeValueAsString(personDo);
    assertJsonEquals("TestPersonDo.json", json);
  }

  @Test
  public void testDeserialize_TestPersonDo() throws Exception {
    String input = readResourceAsString("TestPersonDo.json");
    TestPersonDo personDo = s_dataObjectMapper.readValue(input, TestPersonDo.class);
    assertTestPersonDo(personDo);
  }

  protected TestPersonDo createTestPersonDo() {
    TestPersonDo personDo = BEANS.get(TestPersonDo.class);
    personDo.withBirthday(DATE_TRUNCATED);
    TestElectronicAddressDo electronicAddress = BEANS.get(TestElectronicAddressDo.class).withId("elecAddress");
    electronicAddress.email().set("foo@bar.de");
    TestPhysicalAddressDo physicalAddress = BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress");
    physicalAddress.city().set("Example");
    TestPhysicalAddressExDo physicalAddressEx = BEANS.get(TestPhysicalAddressExDo.class).withId("physicAddressEx");
    physicalAddressEx.poBox().set("1234");
    personDo.withDefaultAddress(electronicAddress);
    personDo.withAddresses(Arrays.asList(electronicAddress, physicalAddress, physicalAddressEx));
    return personDo;
  }

  protected void assertTestPersonDo(TestPersonDo actual) {
    TestPersonDo expected = createTestPersonDo();
    assertEqualsWithComparisonFailure(expected, actual);

    assertEquals(expected.birthday().get(), actual.getBirthday());
    assertEquals(expected.birthday().get().getTime(), actual.getBirthday().getTime());

    assertTrue(actual.getDefaultAddress() instanceof TestElectronicAddressDo);
    assertEquals(expected.getDefaultAddress().getId(), actual.getDefaultAddress().getId());
    assertEquals(expected.getDefaultAddress().get("email"), ((TestElectronicAddressDo) actual.getDefaultAddress()).email().get());

    assertTrue(actual.getAddresses().get(0) instanceof TestElectronicAddressDo);
    assertEquals(expected.getAddresses().get(0).getId(), actual.getAddresses().get(0).getId());
    assertEquals(expected.getAddresses().get(0).get("email"), ((TestElectronicAddressDo) actual.getAddresses().get(0)).email().get());

    assertTrue(actual.getAddresses().get(1) instanceof TestPhysicalAddressDo);
    assertEquals(expected.getAddresses().get(1).getId(), actual.getAddresses().get(1).getId());
    assertEquals(expected.getAddresses().get(1).get("city"), ((TestPhysicalAddressDo) actual.getAddresses().get(1)).city().get());

    assertTrue(actual.getAddresses().get(2) instanceof TestPhysicalAddressExDo);
    assertEquals(expected.getAddresses().get(2).getId(), actual.getAddresses().get(2).getId());
    assertEquals(expected.getAddresses().get(2).get("poBox"), ((TestPhysicalAddressExDo) actual.getAddresses().get(2)).poBox().get());
  }

  @Test
  public void testSerializeDeserialize_DoEntityRawWithNestedPersons() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    TestPersonDo person = createTestPersonDo();
    entity.put("person", person);
    entity.putList("persons", Arrays.asList(person, person));
    String json = s_dataObjectMapper.writeValueAsString(entity);

    DoEntity entityMarshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals(person.birthday().get(), entityMarshalled.get("person", TestPersonDo.class).getBirthday());
    assertTestPersonDo(entityMarshalled.get("person", TestPersonDo.class));
    assertTestPersonDo(entityMarshalled.getList("persons", TestPersonDo.class).get(0));
    assertTestPersonDo(entityMarshalled.getList("persons", TestPersonDo.class).get(1));
  }

  // ------------------------------------ replacement / extensibility test cases ------------------------------------

  /**
   * <pre>
                +--------------------+
                | AbstractAddressDo  |
                +---------^----------+
                          |
              +-----------+--------------+
              |                          |
     +--------+-----------+   +----------+---------+
     |ElectronicAddressDo |   | PhysicalAddressDo  |
     +--------------------+   +----------+---------+
                                         |
                              +----------+---------+
                              |PhysicalAddressExDo |  (@Replace)
                              +--------------------+
   * </pre>
   */
  @Test
  public void testDeserialize_TestPersonDoWithReplacedPhysicalAddress() throws Exception {
    IBean<?> registeredBean = BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(TestPhysicalAddressExDo.class).withReplace(true));
    try {
      String input = readResourceAsString("TestPersonDo.json");
      TestPersonDo personDo = s_dataObjectMapper.readValue(input, TestPersonDo.class);
      assertEquals(personDo.getAddresses().get(0).getClass(), TestElectronicAddressDo.class);
      assertEquals(personDo.getAddresses().get(1).getClass(), TestPhysicalAddressExDo.class);
      assertEquals(personDo.getAddresses().get(2).getClass(), TestPhysicalAddressExDo.class);
    }
    finally {
      BEANS.get(BeanTestingHelper.class).unregisterBean(registeredBean);
    }
  }

  /**
   * Test case 1: Core DO replaced with Project DO, keeping the assigned core TypeName
   *
   * <pre>
    +--------------------+
    |  TestCoreExampleDo |     (@TypeName("TestCoreExample"))
    +---------+----------+
              |
    +--------------------+
    |TestProjectExampleDo|    (@Replace)
    +--------------------+
   * </pre>
   */
  @Test
  public void testSerializeDeserialize_ReplacedCoreDo() throws Exception {
    TestCoreExample1Do coreDo = BEANS.get(TestCoreExample1Do.class);
    coreDo.withName("core-name1");
    String json = s_dataObjectMapper.writeValueAsString(coreDo);
    String expectedJson = readResourceAsString("TestCoreExample1Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestCoreExample1Do coreDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestCoreExample1Do.class);
    assertEqualsWithComparisonFailure(coreDo, coreDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_ProjectDo() throws Exception {
    TestProjectExample1Do projectDo = BEANS.get(TestProjectExample1Do.class);
    projectDo.withName("core-name1");
    projectDo.withNameEx("project-name1");
    String json = s_dataObjectMapper.writeValueAsString(projectDo);
    String expectedJson = readResourceAsString("TestProjectExample1Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestProjectExample1Do coreDoMarshalled = s_dataObjectMapper.readValue(json, TestProjectExample1Do.class);
    assertEqualsWithComparisonFailure(projectDo, coreDoMarshalled);
  }

  /**
   * Test case 2: Core DO replaced with Project DO, changing the assigned core TypeName
   *
   * <pre>
    +---------------------+
    |  TestCoreExample2Do |     (@TypeName("TestCoreExample2"))
    +---------+-----------+
              |
    +---------------------+
    |TestProjectExample2Do|    (@Replace), (@TypeName("TestProjectExample2"))
    +---------------------+
   * </pre>
   */
  @Test
  public void testSerializeDeserialize_ReplacedCore2Do() throws Exception {
    TestCoreExample2Do coreDo = BEANS.get(TestCoreExample2Do.class);
    coreDo.withName("core-name2");
    String json = s_dataObjectMapper.writeValueAsString(coreDo);
    String expectedJson = readResourceAsString("TestCoreExample2Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestCoreExample2Do coreDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestCoreExample2Do.class);
    assertEqualsWithComparisonFailure(coreDo, coreDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_Project2Do() throws Exception {
    TestProjectExample2Do projectDo = BEANS.get(TestProjectExample2Do.class);
    projectDo.withName("core-name2");
    projectDo.withNameEx("project-name2");
    String json = s_dataObjectMapper.writeValueAsString(projectDo);
    String expectedJson = readResourceAsString("TestProjectExample2Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestProjectExample2Do coreDoMarshalled = s_dataObjectMapper.readValue(json, TestProjectExample2Do.class);
    assertEqualsWithComparisonFailure(projectDo, coreDoMarshalled);
  }

  /**
   * Test case 3: Core DO extended in Project DO, changing the assigned core TypeName
   *
   * <pre>
    +---------------------+
    |  TestCoreExample3Do |     (@TypeName("TestCoreExample3"))
    +---------+-----------+
              |
    +---------------------+
    |TestProjectExample3Do|    (@TypeName("TestProjectExample3"))
    +---------------------+
   * </pre>
   */
  @Test
  public void testSerializeDeserialize_ExtendedCore3Do() throws Exception {
    TestCoreExample3Do coreDo = BEANS.get(TestCoreExample3Do.class);
    coreDo.withName("core-name3");
    String json = s_dataObjectMapper.writeValueAsString(coreDo);
    String expectedJson = readResourceAsString("TestCoreExample3Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestCoreExample3Do coreDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestCoreExample3Do.class);
    assertEqualsWithComparisonFailure(coreDo, coreDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_Project3Do() throws Exception {
    TestProjectExample3Do projectDo = BEANS.get(TestProjectExample3Do.class);
    projectDo.withName("core-name3");
    projectDo.withNameEx("project-name3");
    String json = s_dataObjectMapper.writeValueAsString(projectDo);
    String expectedJson = readResourceAsString("TestProjectExample3Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestProjectExample3Do coreDoMarshalled = s_dataObjectMapper.readValue(json, TestProjectExample3Do.class);
    assertEqualsWithComparisonFailure(projectDo, coreDoMarshalled);
  }

  // ------------------------------------ exception handling tests ------------------------------------

  @Test(expected = JsonMappingException.class)
  public void testMappingException() throws Exception {
    s_dataObjectMapper.readValue("[]", DoEntity.class);
  }

  @Test(expected = JsonParseException.class)
  public void testParseException() throws Exception {
    s_dataObjectMapper.readValue("[", DoEntity.class);
  }

  @Test
  public void testInvalidAttributeJsonMappingException() {
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue("{\"value\": \"abc\"}", IntegerValueDo.class));
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue("{\"value\": 42}", DateValueDo.class));
  }

  @Test
  public void testInvalidAttributeJsonMappingExceptionMessage() {
    JsonMappingException exception = assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue("{\"value\": \"abc\"}", IntegerValueDo.class));
    assertTrue(exception.getMessage().startsWith("Failed to deserialize attribute 'value' of entity org.eclipse.scout.rt.dataobject.value.IntegerValueDo, value was abc"));
  }

  // ------------------------------------ performance tests ------------------------------------

  @Test
  public void testGeneratedLargeJsonObject() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    // generate some complex, random JSON structure
    for (int i = 0; i < 1000; i++) {
      switch (i % 5) {
        case 0:
          entity.put("attribute" + i, createTestDo());
          break;
        case 1:
          entity.put("attribute" + i, createTestCollectionsDo());
          break;
        case 2:
          entity.put("attribute" + i, createTestPersonDo());
          break;
        case 3:
          entity.put("attribute" + i, createTestItemDo("id" + i, "value" + i));
          break;
        case 4:
          entity.putNode("attribute" + i, DoValue.of("simple-value" + i));
          break;
      }
    }
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEqualsWithComparisonFailure(entity, marshalled);
  }

  // ------------------------------------ generic attribute definition tests -----------------------------

  @Test
  public void testSerializeDeserialize_EntityWithGenericSimpleValues() throws Exception {
    TestEntityWithGenericValuesDo genericDo = BEANS.get(TestEntityWithGenericValuesDo.class);
    TestGenericDo<String> stringValueDo = new TestGenericDo<>();
    stringValueDo.withGenericAttribute("foo");
    TestGenericDo<BigDecimal> bdValueDo = new TestGenericDo<>();
    bdValueDo.withGenericAttribute(new BigDecimal("1234567890.1234567890"));
    genericDo.withGenericListAttribute(stringValueDo, bdValueDo);

    TestGenericDo<String> stringAttribute = new TestGenericDo<>();
    stringAttribute.withGenericAttribute("bar");
    genericDo.withGenericStringAttribute(stringAttribute);

    String json = s_dataObjectMapper.writeValueAsString(genericDo);
    assertJsonEquals("TestEntityWithGenericSimpleValuesDo.json", json);

    TestEntityWithGenericValuesDo marshalled = s_dataObjectMapper.readValue(json, TestEntityWithGenericValuesDo.class);
    assertEquals("foo", marshalled.getGenericListAttribute().get(0).genericAttribute().get());
    assertEquals(new BigDecimal("1234567890.1234567890"), marshalled.getGenericListAttribute().get(1).genericAttribute().get());
    assertEquals("bar", marshalled.getGenericStringAttribute().genericAttribute().get());
    assertEqualsWithComparisonFailure(genericDo, marshalled);
  }

  @Test
  public void testSerializeDeserialize_GenericDoComplexValues() throws Exception {
    TestGenericDo<TestItemDo> itemValueDo = new TestGenericDo<>();
    itemValueDo.withGenericAttribute(createTestItemDo("foo-id", "foo-attribute"));

    String json = s_dataObjectMapper.writeValueAsString(itemValueDo);
    assertJsonEquals("TestGenericWithComplexValueDo.json", json);

    // read value with complete generic type definition
    TestGenericDo<TestItemDo> marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    assertEqualsWithComparisonFailure(itemValueDo, marshalled);
    assertEquals("foo-id", marshalled.getGenericAttribute().getId());

    // read value with incomplete generic type definition
    @SuppressWarnings("unchecked")
    TestGenericDo<TestItemDo> marshalled2 = s_dataObjectMapper.readValue(json, new TypeReference<TestGenericDo>() {
    });
    assertEqualsWithComparisonFailure(itemValueDo, marshalled2);
    assertEquals("foo-id", marshalled2.getGenericAttribute().getId());

    // read value with no generic type definition
    @SuppressWarnings("unchecked")
    TestGenericDo<TestItemDo> marshalled3 = (TestGenericDo<TestItemDo>) s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEqualsWithComparisonFailure(itemValueDo, marshalled3);
    assertEquals("foo-id", marshalled3.getGenericAttribute().getId());
  }

  @Test
  public void testSerializeDeserialize_EntityWithGenericDoComplexValues() throws Exception {
    TestEntityWithGenericValuesDo genericDo = BEANS.get(TestEntityWithGenericValuesDo.class);
    TestGenericDo<TestItemDo> itemValueDo = new TestGenericDo<>();
    itemValueDo.withGenericAttribute(createTestItemDo("foo-id", "foo-attribute"));
    genericDo.withGenericAttribute(itemValueDo);

    Collection<TestGenericDo<?>> itemsList = new ArrayList<>();
    itemsList.add(new TestGenericDo<>().withGenericAttribute(createTestItemDo("foo-id-2", "foo-attribute-2")));
    genericDo.withGenericListAttribute(itemsList);

    String json = s_dataObjectMapper.writeValueAsString(genericDo);
    assertJsonEquals("TestEntityWithGenericComplexValuesDo.json", json);

    TestEntityWithGenericValuesDo marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    assertEqualsWithComparisonFailure(genericDo, marshalled);
    assertEquals("foo-id", marshalled.getGenericAttribute().get("genericAttribute", TestItemDo.class).getId());
    assertEquals("foo-id-2", marshalled.getGenericListAttribute().get(0).get("genericAttribute", TestItemDo.class).getId());
  }

  @Test
  public void testSerializeDeserialize_GenericDoListValues() throws Exception {
    TestGenericDo<List<TestItemDo>> itemsValueDo = new TestGenericDo<>();
    List<TestItemDo> items = new ArrayList<>();
    items.add(createTestItemDo("foo-id-1", "foo-attribute-1"));
    items.add(createTestItemDo("foo-id-2", "foo-attribute-2"));
    itemsValueDo.withGenericAttribute(items);

    String json = s_dataObjectMapper.writeValueAsString(itemsValueDo);
    assertJsonEquals("TestGenericWithListValueDo.json", json);

    TestGenericDo<List<TestItemDo>> marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    // Marshaled class is not equals to serialized class, since List<TestItemDo> is deserialized to DoList<TestItemDo>. Compare only the List<TestItemDo> content.
    assertEqualsWithComparisonFailure(items, marshalled.get("genericAttribute"));

    DoEntity entity = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEqualsWithComparisonFailure(items, entity.get("genericAttribute"));
  }

  @Test
  public void testSerializeDeserialize_GenericDoListAttribute() throws Exception {
    TestGenericDo<TestItemDo> itemsDo = new TestGenericDo<>();
    List<TestItemDo> list = new ArrayList<>();
    list.add(createTestItemDo("foo-id-1", "foo-attribute-1"));
    list.add(createTestItemDo("foo-id-2", "foo-attribute-2"));
    itemsDo.withGenericListAttribute(list);

    String json = s_dataObjectMapper.writeValueAsString(itemsDo);
    assertJsonEquals("TestGenericWithListAttributeDo.json", json);

    // read value with complete generic type definition
    TestGenericDo<TestItemDo> marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    assertEqualsWithComparisonFailure(itemsDo, marshalled);
    assertEquals("foo-id-1", marshalled.getGenericListAttribute().get(0).getId());

    // read value with incomplete generic type definition
    @SuppressWarnings("unchecked")
    TestGenericDo<TestItemDo> marshalled2 = s_dataObjectMapper.readValue(json, new TypeReference<TestGenericDo>() {
    });
    assertEqualsWithComparisonFailure(itemsDo, marshalled2);
    assertEquals("foo-id-1", marshalled2.getGenericListAttribute().get(0).getId());

    // read value with no generic type definition
    @SuppressWarnings("unchecked")
    TestGenericDo<TestItemDo> marshalled3 = (TestGenericDo<TestItemDo>) s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEqualsWithComparisonFailure(itemsDo, marshalled3);
    assertEquals("foo-id-1", marshalled3.getGenericListAttribute().get(0).getId());
  }

  @Test
  public void testSerializeDeserialize_GenericDoMapAttribute() throws Exception {
    TestGenericDo<TestItemDo> itemsDo = new TestGenericDo<>();
    Map<String, TestItemDo> map = new LinkedHashMap<>();
    map.put("key1", createTestItemDo("foo-id-1", "foo-attribute-1"));
    map.put("key2", createTestItemDo("foo-id-2", "foo-attribute-2"));
    itemsDo.withGenericMapAttribute(map);

    // tests that DoEntitySerializer#serializeMap checks for Object.class of value type
    String json = s_dataObjectMapper.writeValueAsString(itemsDo);
    assertJsonEquals("TestGenericWithMapAttributeDo.json", json);

    // read value with complete generic type definition
    // read value with incomplete generic type definition
    // read value with no generic type definition
    // -> would all fail in comparison because TestItemDo cannot be deserialized anymore, resulting in a linked hash map instead of a concret DO
  }

  @Test
  public void testSerializeDeserialize_GenericMap() throws Exception {
    TestGenericDo<Map<String, TestItemDo>> itemsMapDo = new TestGenericDo<>();

    Map<String, TestItemDo> map = new LinkedHashMap<>();
    map.put("foo-1", createTestItemDo("foo-id-1", "foo-attribute-1"));
    map.put("foo-2", createTestItemDo("foo-id-2", "foo-attribute-2"));
    itemsMapDo.withGenericAttribute(map);

    String json = s_dataObjectMapper.writeValueAsString(itemsMapDo);
    assertJsonEquals("TestGenericWithMapValueDo.json", json);

    TestGenericDo<Map<String, TestItemDo>> marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    // Marshaled class is not equals to serialized class, since Map<String, TestItemDo> is deserialized to a DoEntity. Compare the expected map with all attribute values.
    assertEqualsWithComparisonFailure(map, marshalled.get("genericAttribute", DoEntity.class).all());
  }

  @Test
  public void testSerializeDeserialize_GenericDoEntityMap() throws Exception {
    TestGenericDoEntityMapDo<Map<String, TestItemDo>> itemsMapDo = new TestGenericDoEntityMapDo<>();

    Map<String, TestItemDo> map = new LinkedHashMap<>();
    map.put("foo-1", createTestItemDo("foo-id-1", "foo-attribute-1"));
    map.put("foo-2", createTestItemDo("foo-id-2", "foo-attribute-2"));
    itemsMapDo.withGenericMapAttribute(map);

    String json = s_dataObjectMapper.writeValueAsString(itemsMapDo);
    assertJsonEquals("TestGenericDoEntityMapDo.json", json);

    TestGenericDoEntityMapDo<Map<String, TestItemDo>> marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    assertEqualsWithComparisonFailure(itemsMapDo, marshalled);
    assertEquals("foo-id-1", marshalled.genericMapAttribute().get().get("foo-1").getId());
  }

  @Test
  public void testSerializeDeserialize_DoEntityWithContributions() throws Exception {
    TestItemDo doEntity = BEANS.get(TestItemDo.class).withId("123456789");
    TestItemContributionOneDo contributionOne = doEntity.contribution(TestItemContributionOneDo.class).withName("one");
    TestItemContributionTwoDo contributionTwo = doEntity.contribution(TestItemContributionTwoDo.class).withName("two");

    String json = s_dataObjectMapper.writeValueAsString(doEntity);
    assertJsonEquals("TestDoEntityWithContributions.json", json);

    TestItemDo marshalledDoEntity = s_dataObjectMapper.readValue(json, TestItemDo.class);
    assertEquals("123456789", marshalledDoEntity.getId());

    // no node for contributions in typed mode
    assertFalse(marshalledDoEntity.has(ScoutDataObjectModule.DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME));
    assertEquals(2, marshalledDoEntity.getContributions().size());
    assertEquals(contributionOne, marshalledDoEntity.getContribution(TestItemContributionOneDo.class));
    assertEquals(contributionTwo, marshalledDoEntity.getContribution(TestItemContributionTwoDo.class));
    assertEqualsWithComparisonFailure(doEntity, marshalledDoEntity);
  }

  @Test
  public void testDeserialize_DoEntityWithUnknownContribution() throws Exception {
    String json = readResourceAsString("TestDoEntityWithUnknownContribution.json");
    TestItemDo marshalledDoEntity = s_dataObjectMapper.readValue(json, TestItemDo.class);
    assertEquals("123456789", marshalledDoEntity.getId());

    // no node for contributions in typed mode
    assertFalse(marshalledDoEntity.has(ScoutDataObjectModule.DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME));
    // doesn't fail even if internal instance is of type IDoEntity instead of IDoEntityContribution due to unknown type name
    assertEquals(1, marshalledDoEntity.getContributions().size());
    // throws due to tried casting
    assertThrows(ClassCastException.class, () -> CollectionUtility.firstElement(marshalledDoEntity.getContributions()).getString("name"));
  }

  @Test
  public void testSerializationValidation_DoEntityWithContribution() throws JsonProcessingException {
    serializeContribution(TestItemDo.class, TestItemContributionOneDo.class);
    serializeContribution(TestItemDo.class, TestItemContributionTwoDo.class);

    assertThrows(JsonMappingException.class, () -> serializeContribution(TestItemDo.class, TestCoreExample1DoContributionFixtureDo.class));
    assertThrows(JsonMappingException.class, () -> serializeContribution(TestItemDo.class, TestCoreExample1DoContributionFixtureDo.class));
    assertThrows(JsonMappingException.class, () -> serializeContribution(TestItemDo.class, TestProjectExample1ContributionFixtureDo.class));

    // not using BEANS.get because bean is replaced by ProjectFixtureDo (only for validation, not a real case this way)
    assertThrows(JsonMappingException.class, () -> serializeContribution(new TestCoreExample1Do(), TestItemContributionOneDo.class));
    assertThrows(JsonMappingException.class, () -> serializeContribution(new TestCoreExample1Do(), TestItemContributionTwoDo.class));
    assertThrows(JsonMappingException.class, () -> serializeContribution(new TestCoreExample1Do(), TestProjectExample1ContributionFixtureDo.class));
    serializeContribution(new TestCoreExample1Do(), TestCoreExample1DoContributionFixtureDo.class);

    // using subclasses data object
    assertThrows(JsonMappingException.class, () -> serializeContribution(TestProjectExample1Do.class, TestItemContributionOneDo.class));
    assertThrows(JsonMappingException.class, () -> serializeContribution(TestProjectExample1Do.class, TestItemContributionTwoDo.class));
    serializeContribution(TestProjectExample1Do.class, TestCoreExample1DoContributionFixtureDo.class);
    serializeContribution(TestProjectExample1Do.class, TestProjectExample1ContributionFixtureDo.class);

    // verify contribution DO with two containers
    serializeContribution(TestItemDo.class, DoubleContributionFixtureDo.class);
    serializeContribution(TestDateDo.class, DoubleContributionFixtureDo.class);
  }

  // ------------------------------------ entity with IDoEntity interface definition tests -----------------------------

  @Test
  public void testSerializeDeserialize_DoEntityWithInterface() throws Exception {
    ITestBaseEntityDo baseEntity = BEANS.get(TestEntityWithInterface1Do.class)
        .withDoubleAttribute(42.0)
        .withStringAttribute("foo")
        .withItemDoAttribute(createTestItemDo("id-1", "value-1"))
        .withDoubleListAttribute(Arrays.asList(42.0, 43.0))
        .withStringListAttribute("foo", "bar", "baz")
        .withItemDoListAttribute(createTestItemDo("id-2", "value-2"), createTestItemDo("id-3", "value-3"));

    String json = s_dataObjectMapper.writeValueAsString(baseEntity);
    assertJsonEquals("TestEntityWithInterface1Do.json", json);

    ITestBaseEntityDo marshalled = s_dataObjectMapper.readValue(json, ITestBaseEntityDo.class);
    assertEqualsWithComparisonFailure(baseEntity, marshalled);
  }

  @Test
  public void testSerializeDeserialize_DoEntityWithInterfaceEx() throws Exception {
    ITestBaseEntityDo baseEntity = BEANS.get(TestEntityWithInterface2Do.class)
        .withDoubleAttribute(42.0)
        .withStringAttribute("foo")
        .withItemDoAttribute(createTestItemDo("id-1", "value-1"))
        .withDoubleListAttribute(Arrays.asList(42.0, 43.0))
        .withStringListAttribute("foo", "bar", "baz")
        .withItemDoListAttribute(createTestItemDo("id-2", "value-2"), createTestItemDo("id-3", "value-3"))
        .withStringAttributeEx("fooEx");

    String json = s_dataObjectMapper.writeValueAsString(baseEntity);
    assertJsonEquals("TestEntityWithInterface2Do.json", json);

    ITestBaseEntityDo marshalled = s_dataObjectMapper.readValue(json, ITestBaseEntityDo.class);
    assertEqualsWithComparisonFailure(baseEntity, marshalled);

    IDoEntity marshalledDoEntity = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEqualsWithComparisonFailure(baseEntity, marshalledDoEntity);
  }

  @Test
  public void testSerializeDeserialize_CustomImplementedEntity() throws Exception {
    TestCustomImplementedEntityDo entity = BEANS.get(TestCustomImplementedEntityDo.class);
    entity.put("stringAttribute", "foo");
    entity.put("doubleAttribute", new BigDecimal("1234567.89"));
    entity.put("itemAttribute", createTestItemDo("id-1", "foo-item-1"));
    entity.dateAttribute().set(DATE_TRUNCATED);

    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonEquals("TestCustomImplementedEntityDo.json", json);

    IDoEntity marshalledDoEntity = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEqualsWithComparisonFailure(entity, marshalledDoEntity);
  }

  @Test
  public void testSerializeDeserialize_IDoEntity() throws Exception {
    IDoEntity entity = BEANS.get(DoEntity.class);
    entity.put("attribute", "value");
    String json = s_dataObjectMapper.writeValueAsString(entity);

    IDoEntity marshalledDoEntity = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(DoEntity.class, marshalledDoEntity.getClass());
    assertEqualsWithComparisonFailure(entity, marshalledDoEntity);
  }

  // ------------------------------------ IDataObject interface tests -----------------------------------

  @Test
  public void testSerializeDeserialize_EmptyIDataObject() throws Exception {
    IDataObject marshalledEntity = s_dataObjectMapper.readValue("{}", IDataObject.class);
    assertEquals(DoEntity.class, marshalledEntity.getClass());
    assertTrue(((DoEntity) marshalledEntity).allNodes().isEmpty());

    IDataObject marshalledList = s_dataObjectMapper.readValue("[]", IDataObject.class);
    assertEquals(DoList.class, marshalledList.getClass());
    assertTrue(((DoList) marshalledList).isEmpty());
  }

  @Test
  public void testSerializeDeserialize_EntityIDataObject() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("stringAttribute", "value-string");
    entity.put("doubleAttribute", new BigDecimal("1234567.89"));
    entity.put("itemDoAttribute", createTestItemDo("id", "value"));

    IDataObject dataObject = entity;
    String json = s_dataObjectMapper.writeValueAsString(dataObject);
    assertJsonEquals("TestEntityIDataObject.json", json);

    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEqualsWithComparisonFailure(entity, marshalled);
  }

  @Test
  public void testSerializeDeserialize_StringListIDataObject() throws Exception {
    DoList<String> list = new DoList<>();
    list.add("foo");
    list.add("bar");

    IDataObject dataObject = list;
    String json = s_dataObjectMapper.writeValueAsString(dataObject);
    assertJsonEquals("TestStringListIDataObject.json", json);

    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEqualsWithComparisonFailure(list, marshalled);
  }

  @Test
  public void testSerializeDeserialize_ItemDoListIDataObject() throws Exception {
    DoList<TestItemDo> list = new DoList<>();
    list.add(createTestItemDo("id-1", "string-1"));
    list.add(createTestItemDo("id-2", "string-2"));

    IDataObject dataObject = list;
    String json = s_dataObjectMapper.writeValueAsString(dataObject);
    assertJsonEquals("TestItemDoListIDataObject.json", json);

    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEqualsWithComparisonFailure(list, marshalled);
  }

  @Test
  public void testSerializeDeserialize_ObjectListIDataObject() throws Exception {
    DoList<Object> list = new DoList<>();
    list.add("foo");
    TestItemDo item1 = createTestItemDo("id-1", "string-1");
    list.add(item1);
    list.add("bar");
    TestItemDo item2 = createTestItemDo("id-2", "string-2");
    list.add(item2);

    IDataObject dataObject = list;
    String json = s_dataObjectMapper.writeValueAsString(dataObject);
    assertJsonEquals("TestObjectListIDataObject.json", json);

    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEqualsWithComparisonFailure(list, marshalled);
    DoList marshalledList = (DoList) marshalled;
    assertEquals("foo", marshalledList.get(0));
    assertEqualsWithComparisonFailure(item1, marshalledList.get(1));
    assertEquals("bar", marshalledList.get(2));
    assertEqualsWithComparisonFailure(item2, marshalledList.get(3));
  }

  // ------------------------------------ tests with custom JSON type property name -----------------------------------

  @Test
  public void testSerializeDeserialize_CustomTypePropertyName() throws Exception {
    ObjectMapper mapper = createCustomScoutDoObjectMapper(c -> c.withTypeAttributeName("_customType"));
    mapper.disable(SerializationFeature.INDENT_OUTPUT);

    TestComplexEntityDo entityDo = BEANS.get(TestComplexEntityDo.class);
    entityDo.withId("foo");
    String json = mapper.writeValueAsString(entityDo);
    assertEquals("{\"_customType\":\"TestComplexEntity\",\"id\":\"foo\"}", json);

    DoEntity marshalled = mapper.readValue(json, DoEntity.class);
    assertEquals(TestComplexEntityDo.class, marshalled.getClass());
    assertEquals("foo", ((TestComplexEntityDo) marshalled).getId());
  }

  @Test
  public void testSerializeDeserializeComplexEntity_SuppressType() throws Exception {
    ObjectMapper mapper = createCustomScoutDoObjectMapper(c -> c.withSuppressTypeAttribute(true));
    TestComplexEntityDo entityDo = createTestDo();
    String json = mapper.writeValueAsString(entityDo);
    assertJsonEquals("TestComplexEntityDoRaw.json", json);
  }

  @Test
  public void testSerializeDeserializeCollection_SuppressType() throws Exception {
    ObjectMapper mapper = createCustomScoutDoObjectMapper(c -> c.withSuppressTypeAttribute(true));
    // disable Jackson typing for pojo classes (suppress flag is only for data objects)
    mapper.addMixIn(TestItemPojo.class, NoTypes.class);
    mapper.addMixIn(TestItemPojo2.class, NoTypes.class);
    TestCollectionsDo testDo = createTestCollectionsDo();
    String json = mapper.writeValueAsString(testDo);
    assertJsonEquals("TestCollectionsDoRaw.json", json);
  }

  @Test
  public void testSerializeDeserializeSet_SuppressType() throws Exception {
    ObjectMapper mapper = createCustomScoutDoObjectMapper(c -> c.withSuppressTypeAttribute(true));
    // disable Jackson typing for pojo classes (suppress flag is only for data objects)
    mapper.addMixIn(TestItemPojo.class, NoTypes.class);
    TestSetDo setDo = createTestSetDo();
    String json = mapper.writeValueAsString(setDo);
    assertJsonEquals("TestSetDoRaw.json", json);
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  static class NoTypes {
  }

  // ------------------------------------ tests with type version ------------------------------------

  @Test
  public void testSerializeDeserialize_VersionedDo() throws Exception {
    TestVersionedDo versioned = BEANS.get(TestVersionedDo.class).withName("lorem");
    String json = s_dataObjectMapper.writeValueAsString(versioned);
    assertJsonEquals("TestVersionedDo.json", json);

    // deserialized version-annotated data object contains no version attribute node
    TestVersionedDo entity = s_dataObjectMapper.readValue(json, TestVersionedDo.class);
    assertNull(entity.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));
  }

  @Test
  public void testSerializeDeserialize_VersionedDoWithoutVersion() throws Exception {
    runTestVersionedDo("TestVersionedDoNoVersion.json");
  }

  @Test
  public void testSerializeDeserialize_VersionedDoInvalidVersion() throws Exception {
    runTestVersionedDo("TestVersionedDoInvalidVersion.json");
  }

  @Test
  public void testSerializeDeserialize_VersionedDoEmptyVersion() throws Exception {
    runTestVersionedDo("TestVersionedDoEmptyVersion.json");
  }

  @Test
  public void testSerializeDeserialize_VersionedDoNullVersion() throws Exception {
    runTestVersionedDo("TestVersionedDoNullVersion.json");
  }

  @Test
  public void testSerializeDeserialize_VersionedDoHigherVersion() throws Exception {
    TestVersionedDo doMarshalled = runTestVersionedDo("TestVersionedDoHigherVersion.json");

    // serializing data object to JSON causes version to be set to class-file annotated version (override value contained in type version attribute)
    String serialized = s_dataObjectMapper.writeValueAsString(doMarshalled);
    JsonNode rawTree = s_defaultJacksonObjectMapper.readTree(serialized);
    assertEquals("jacksonFixture-1.0.0", rawTree.get(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME).asText());
  }

  protected TestVersionedDo runTestVersionedDo(String resourceName) throws Exception {
    String json = readResourceAsString(resourceName);
    TestVersionedDo doMarhalled = s_dataObjectMapper.readValue(json, TestVersionedDo.class);

    // deserialized version-annotated data object contains no version attribute node (even when created by a JSON document containing any version data)
    assertNull(doMarhalled.getString(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));

    String serialized = s_dataObjectMapper.writeValueAsString(doMarhalled);
    assertJsonEquals("TestVersionedDo.json", serialized);
    return doMarhalled;
  }

  @Test
  public void testSerializeDeserialize_CustomTypeVersionPropertyName() throws Exception {
    ObjectMapper mapper = createCustomScoutDoObjectMapper(c -> c.withTypeVersionAttributeName("_customTypeVersion"));
    mapper.disable(SerializationFeature.INDENT_OUTPUT);
    TestVersionedDo entityDo = BEANS.get(TestVersionedDo.class);
    entityDo.withName("foo");
    String json = mapper.writeValueAsString(entityDo);
    assertEquals("{\"_type\":\"jacksonFixture.TestVersioned\",\"_customTypeVersion\":\"jacksonFixture-1.0.0\",\"name\":\"foo\"}", json);

    DoEntity marshalled = mapper.readValue(json, DoEntity.class);
    assertEquals(TestVersionedDo.class, marshalled.getClass());
    assertEquals("foo", ((TestVersionedDo) marshalled).getName());
  }

  @Test
  public void testSerializeDeserialize_VersionedDoRaw() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME, "123");
    String json = s_dataObjectMapper.writeValueAsString(entity);

    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals("123", marshalled.get(ScoutDataObjectModule.DEFAULT_TYPE_VERSION_ATTRIBUTE_NAME));
  }

  /**
   * Various tests for floating number deserialization
   * <p>
   * Note: Jackson allows to deserialize numbers which are specified as JSON numbers or JSON strings within the JSON
   * string to deserialize.
   */
  @Test
  public void testDeserialize_Numbers() throws Exception {
    TestComplexEntityDo entity = s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "123.456"), TestComplexEntityDo.class);
    assertEquals(Float.valueOf(123.456f), entity.getFloatAttribute());

    entity = s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "\"123.456\""), TestComplexEntityDo.class);
    assertEquals(Float.valueOf(123.456f), entity.getFloatAttribute());

    entity = s_dataObjectMapper.readValue(createTestComplexEntityJson("doubleAttribute", "123.456"), TestComplexEntityDo.class);
    assertEquals(Double.valueOf(123.456), entity.getDoubleAttribute());

    entity = s_dataObjectMapper.readValue(createTestComplexEntityJson("doubleAttribute", "\"123.456\""), TestComplexEntityDo.class);
    assertEquals(Double.valueOf(123.456), entity.getDoubleAttribute());

    entity = s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "123.456"), TestComplexEntityDo.class);
    assertEquals(new BigDecimal("123.456"), entity.getBigDecimalAttribute());

    entity = s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "\"123.456\""), TestComplexEntityDo.class);
    assertEquals(new BigDecimal("123.456"), entity.getBigDecimalAttribute());

    Assert.assertThrows(JsonParseException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "123-456"), TestComplexEntityDo.class));
    Assert.assertThrows(JsonParseException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "123-456-100"), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "\"123-456\""), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "\"10-03-2019\""), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("floatAttribute", "\"123,456\""), TestComplexEntityDo.class));

    Assert.assertThrows(JsonParseException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("doublelAttribute", "123-456"), TestComplexEntityDo.class));
    Assert.assertThrows(JsonParseException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("doubleAttribute", "123-456-100"), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("doubleAttribute", "\"123-456\""), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("doubleAttribute", "\"10-03-2019\""), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("doubleAttribute", "\"123,456\""), TestComplexEntityDo.class));

    Assert.assertThrows(JsonParseException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "123-456"), TestComplexEntityDo.class));
    Assert.assertThrows(JsonParseException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "123-456-100"), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "\"123-456\""), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "\"10-03-2019\""), TestComplexEntityDo.class));
    Assert.assertThrows(InvalidFormatException.class, () -> s_dataObjectMapper.readValue(createTestComplexEntityJson("bigDecimalAttribute", "\"123,456\""), TestComplexEntityDo.class));
  }

  protected String createTestComplexEntityJson(String attributeName, String value) {
    return "{\"_type\" : \"TestComplexEntity\", \"" + attributeName + "\" : " + value + "}";
  }

  @Test
  public void testSerializeDeserialize_collectionFormat01() throws Exception {
    TestDateDo testDo = BEANS.get(TestDateDo.class);
    testDo.withADummySet(CollectionUtility.hashSet("1", "2"))
        .withDateWithTimestamp(DATE)
        .withDateOnly(DATE_TRUNCATED);
    testDo.withZDummySet(CollectionUtility.hashSet("3", "4"));
    String json = s_dataObjectMapper.writeValueAsString(testDo);

    // deserialize and check
    TestDateDo testDoMarshalled = s_dataObjectMapper.readValue(json, TestDateDo.class);
    assertEquals(DATE, testDoMarshalled.getDateWithTimestamp());
    assertEquals(DATE_TRUNCATED, testDoMarshalled.getDateOnly());
    assertEquals(Set.of("1", "2"), testDoMarshalled.getADummySet());
    assertEquals(Set.of("3", "4"), testDoMarshalled.getZDummySet());
  }

  @Test
  public void testSerializeDeserialize_collectionFormat02() throws Exception {
    TestDateDo testDo = BEANS.get(TestDateDo.class);
    testDo.withZDummySet(CollectionUtility.hashSet("1", "2"))
        .withDateWithTimestamp(DATE)
        .withDateOnly(DATE_TRUNCATED);

    String json = s_dataObjectMapper.writeValueAsString(testDo);

    // deserialize and check
    TestDateDo testDoMarshalled = s_dataObjectMapper.readValue(json, TestDateDo.class);
    assertEquals(DATE, testDoMarshalled.getDateWithTimestamp());
    assertEquals(DATE_TRUNCATED, testDoMarshalled.getDateOnly());
  }

  @Test
  public void testSerializeLiteral() throws Exception {
    assertEquals("42", s_dataObjectMapper.writeValueAsString(42));
    assertEquals("42.12345", s_dataObjectMapper.writeValueAsString(42.12345));
    assertEquals("true", s_dataObjectMapper.writeValueAsString(true));
    assertEquals("false", s_dataObjectMapper.writeValueAsString(false));
    assertEquals("\"foo\"", s_dataObjectMapper.writeValueAsString("foo"));
    assertEquals("\"foo\\\\n\\\\rbar\"", s_dataObjectMapper.writeValueAsString("foo\\n\\rbar"));
  }

  /**
   * <b>NOTE</b> Serializing java exception is discouraged, since most concrete exception classes are not
   * JSON-serializable.
   * <p>
   * The Jackson library out of the box supports to serialize and deserialize an {@link Exception} or {@link Throwable}
   * using its message, cause and stacktrace elements as fields. This test ensures this basic functionality when
   * serializing {@link Exception} or {@link Throwable} within data objects.
   */
  @Test
  public void testSerializeDeserializeThrowable() throws Exception {
    Throwable throwable = new Throwable("throwable-message");
    throwable.setStackTrace(new StackTraceElement[]{new StackTraceElement("mocked-throwable-class", "mocked-method-01", "mocked-file-01", 42)});
    Exception exception = new Exception("exception-message");
    exception.setStackTrace(new StackTraceElement[]{new StackTraceElement("mocked-exception-class", "mocked-method-02", "mocked-file-02", 43)});
    TestThrowableDo entity = BEANS.get(TestThrowableDo.class)
        .withThrowable(throwable)
        .withException(exception);

    // Note: no plain JSON comparison, since JSON-serialized Exception and Throwable depends on used JRE

    String json = s_dataObjectMapper.writeValueAsString(entity);
    TestThrowableDo marshalled = s_dataObjectMapper.readValue(json, TestThrowableDo.class);
    assertEquals(throwable.getMessage(), marshalled.getThrowable().getMessage());
    assertEquals(exception.getMessage(), marshalled.getException().getMessage());
    assertArrayEquals(throwable.getStackTrace(), marshalled.getThrowable().getStackTrace());
    assertArrayEquals(exception.getStackTrace(), marshalled.getException().getStackTrace());
  }

  @Test
  public void testSerializeDeserializeOptionalDo() throws Exception {
    @SuppressWarnings("unchecked")
    TestOptionalDo optional = BEANS.get(TestOptionalDo.class)
        .withOptString(Optional.empty())
        .withOptStringList(Optional.empty(), Optional.of("foo"));
    String json = s_dataObjectMapper.writeValueAsString(optional);

    // currently serializable using Scout Jackson implementation, but without values, e.g. useless!
    assertJsonEquals("TestOptionalDo.json", json);

    // currently not deserializable using Scout Jackson implementation
    JsonMappingException exception = assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestOptionalDo.class));

    // TODO [23.1] pbz remove when JDK 11 is no longer supported
    if ("11".equals(System.getProperty("java.specification.version"))) {
      assertTrue("expected cause UnrecognizedPropertyException, got " + exception.getCause(), exception.getCause() instanceof UnrecognizedPropertyException);
    }
    else {
      assertTrue("expected cause InvalidDefinitionException, got " + exception.getCause(), exception.getCause() instanceof InvalidDefinitionException);
    }
  }

  @Test
  public void testSerializeDeserializeHierarchicalLookupCall() throws Exception {
    FixtureHierarchicalLookupRowDo parent = createRow(FixtureUuId.create());
    FixtureHierarchicalLookupRowDo rowA = createRow(FixtureUuId.create());
    FixtureHierarchicalLookupRowDo rowB = createRow(FixtureUuId.create()).withParentId(parent.getId());
    LookupResponse<FixtureHierarchicalLookupRowDo> response = LookupResponse.create(Arrays.asList(parent, rowA, rowB));
    String json = s_dataObjectMapper.writeValueAsString(response);

    LookupResponse<FixtureHierarchicalLookupRowDo> marshalled = s_dataObjectMapper.readValue(json, new TypeReference<>() {
    });
    assertEquals(parent, marshalled.getRows().get(0));
    assertEquals(rowA, marshalled.getRows().get(1));
    assertEquals(rowB, marshalled.getRows().get(2));
    assertEquals(parent.getId(), marshalled.getRows().get(2).getParentId());
  }

  protected FixtureHierarchicalLookupRowDo createRow(FixtureUuId id) {
    return BEANS.get(FixtureHierarchicalLookupRowDo.class)
        .withId(id)
        .withText("Mock");
  }

  /**
   * Serialize & deserialize entity with nested map containing unknown DO class referenced by _type.
   */
  @Test
  public void testSerializeWithUnknownNestedEntity() throws Exception {
    IDoEntity raw = BEANS.get(DoEntityBuilder.class)
        .put("_type", "UnknownType")
        .put("nr", 12345)
        .build();
    TestMapDo obj = BEANS.get(TestMapDo.class);
    // add 'wrong' item into map using a non-existent _type
    obj.put(obj.stringDoTestItemMapAttribute().getAttributeName(), CollectionUtility.hashMap(ImmutablePair.of("one", raw)));
    // typed access to attribute is not possible
    assertThrows(ClassCastException.class, () -> obj.getStringDoTestItemMapAttribute().get("one").getId());

    String serialized = s_dataObjectMapper.writeValueAsString(obj);
    assertJsonEquals("TestEntityWithUnknownNestedEntity.json", serialized);

    // NOK - read unknown entity into a concrete DO class declaring nested attribute as concrete class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(serialized, TestMapDo.class));

    TestMapDo marshalledLenient = s_lenientDataObjectMapper.readValue(serialized, TestMapDo.class);
    IDoEntity doEntity = marshalledLenient.getStringDoTestItemMapAttribute().get("one");
    assertEquals(DoEntity.class, doEntity.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownType", doEntity.getString("_type"));
    assertEquals(12345, doEntity.getDecimal("nr").intValue());
  }

  /**
   * Serialize & deserialize entity with nested raw IDoValue containing unknown DO class referenced by _type.
   */
  @Test
  public void testSerializeWithUnknownNestedEntity2() throws Exception {
    IDoEntity raw = BEANS.get(DoEntityBuilder.class)
        .put("_type", "UnknownType")
        .put("nr", 12345)
        .build();

    TestMapDo obj = BEANS.get(TestMapDo.class);
    // add 'wrong' item into map using a non-existent _type
    obj.put(obj.iDoEntityAttribute().getAttributeName(), raw);
    assertEquals(Integer.valueOf(12345), obj.getIDoEntityAttribute().get("nr", Integer.class));

    String serialized = s_dataObjectMapper.writeValueAsString(obj);
    assertJsonEquals("TestEntityWithUnknownNestedEntity2.json", serialized);

    // OK - read unknown entity into a concrete DO class declaring nested attribute as raw IDoEntity
    TestMapDo entityMarshalled = s_dataObjectMapper.readValue(serialized, TestMapDo.class);
    assertEquals(Integer.valueOf(12345), entityMarshalled.getIDoEntityAttribute().get("nr", Integer.class));
  }

  /**
   * Serialize & deserialize entity with nested TestItemDo entity containing unknown DO class referenced by _type.
   */
  @Test
  public void testSerializeWithUnknownNestedEntity3() throws Exception {
    IDoEntity raw = BEANS.get(DoEntityBuilder.class)
        .put("_type", "UnknownType")
        .put("nr", 12345)
        .build();

    TestItemEntityDo itemEntity = BEANS.get(TestItemEntityDo.class);
    // add 'wrong' item into object using a non-existent _type
    itemEntity.put(itemEntity.item().getAttributeName(), raw);
    // typed access to attribute is not possible
    assertThrows(ClassCastException.class, () -> itemEntity.getItem().getId());

    String serialized = s_dataObjectMapper.writeValueAsString(itemEntity);
    assertJsonEquals("TestEntityWithUnknownNestedEntity3.json", serialized);

    // NOK - read unknown entity into a concrete DO class declaring nested attribute as concrete class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(serialized, TestItemEntityDo.class));

    TestItemEntityDo marshalledLenient = s_lenientDataObjectMapper.readValue(serialized, TestItemEntityDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient.getItem());
    IDoEntity doEntity = marshalledLenient.item().get();
    assertEquals(DoEntity.class, doEntity.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownType", doEntity.getString("_type"));
    assertEquals(12345, doEntity.getDecimal("nr").intValue());
  }

  /**
   * Serialize & deserialize entity with nested TestItemDo entity containing raw DO class without _type reference.
   */
  @Test
  public void testSerializeWithUnknownNestedEntity4() throws Exception {
    IDoEntity raw = BEANS.get(DoEntityBuilder.class)
        .put("nr", 12345)
        .put("id", "myId")
        .build();

    TestItemEntityDo itemEntity = BEANS.get(TestItemEntityDo.class);
    // add 'wrong' item into object using a non-existent _type
    itemEntity.put(itemEntity.item().getAttributeName(), raw);
    assertThrows(ClassCastException.class, () -> itemEntity.getItem().getId());

    String serialized = s_dataObjectMapper.writeValueAsString(itemEntity);
    assertJsonEquals("TestEntityWithUnknownNestedEntity4.json", serialized);

    // OK - read unknown entity into a concrete DO class declaring nested attribute as correct TestItemDo -> unknown entity is parsed correctly into TestItemDo
    TestItemEntityDo entityMarshalled = s_dataObjectMapper.readValue(serialized, TestItemEntityDo.class);
    assertEquals(Integer.valueOf(12345), entityMarshalled.get("item", IDoEntity.class).get("nr", Integer.class));
    assertEquals("myId", entityMarshalled.getItem().getId());
  }

  /**
   * Serialize & deserialize entity with nested ITestBaseEntityDo interface entity containing unknown DO class
   * referenced by _type.
   */
  @Test
  public void testSerializeWithUnknownNestedEntity5() throws Exception {
    IDoEntity raw = BEANS.get(DoEntityBuilder.class)
        .put("_type", "UnknownType")
        .put("nr", 12345)
        .build();

    TestItemEntityDo itemEntity = BEANS.get(TestItemEntityDo.class);
    // add 'wrong' item into object using a non-existent _type
    itemEntity.put(itemEntity.itemIfc().getAttributeName(), raw);
    assertThrows(ClassCastException.class, () -> itemEntity.getItemIfc().stringAttribute());

    String serialized = s_dataObjectMapper.writeValueAsString(itemEntity);
    assertJsonEquals("TestEntityWithUnknownNestedEntity5.json", serialized);

    // NOK - read unknown entity into a concrete DO class declaring nested attribute as concrete DO interface
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(serialized, TestItemEntityDo.class));

    TestItemEntityDo marshalledLenient = s_lenientDataObjectMapper.readValue(serialized, TestItemEntityDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient.getItemIfc());
    IDoEntity doEntity = marshalledLenient.itemIfc().get(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(DoEntity.class, doEntity.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownType", doEntity.getString("_type"));
    assertEquals(12345, doEntity.getDecimal("nr").intValue());
  }

  /**
   * Serialize & deserialize entity with nested ITestBaseEntityDo interface entity containing raw DO class referenced by
   * correct _type.
   */
  @Test
  public void testSerializeWithUnknownNestedEntity6() throws Exception {
    IDoEntity raw = BEANS.get(DoEntityBuilder.class)
        .put("_type", "TestEntityWithInterface1")
        .put("stringAttribute", "foo")
        .build();

    TestItemEntityDo itemEntity = BEANS.get(TestItemEntityDo.class);
    // add 'wrong' item into object using a non-existent _type
    itemEntity.put(itemEntity.itemIfc().getAttributeName(), raw);
    assertThrows(ClassCastException.class, () -> itemEntity.getItemIfc().stringAttribute());

    String serialized = s_dataObjectMapper.writeValueAsString(itemEntity);
    assertJsonEquals("TestEntityWithUnknownNestedEntity6.json", serialized);

    // OK - read raw entity into a concrete DO class declaring nested attribute as correct ITestBaseEntityDo -> raw entity is parsed correctly into TestEntityWithInterface1 instance
    TestItemEntityDo entityMarshalled = s_dataObjectMapper.readValue(serialized, TestItemEntityDo.class);
    assertEquals("foo", entityMarshalled.getItemIfc().stringAttribute().get());
  }

  /**
   * Tests that {@link DoEntitySerializer#serializeAttribute(String, Object, JsonGenerator, SerializerProvider)} takes
   * into account {@link ScoutDataObjectModuleContext#isLenientMode()}.
   */
  @Test
  public void testSerializeWithInvalidAttributeType() throws Exception {
    TestEntityWithIIdDo doEntity = BEANS.get(TestEntityWithIIdDo.class);
    doEntity.put(doEntity.iUuId().getAttributeName(), "unknown"); // invalid format of an IUuId
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.writeValueAsString(doEntity));

    String serialized = s_lenientDataObjectMapper.writeValueAsString(doEntity);
    assertEquals("{\"_type\":\"scout.TestEntityWithIId\",\"iUuId\":\"unknown\"}", serialized);
  }

  /**
   * Tests that {@link DoEntitySerializer#serializeMap(String, Map, JsonGenerator, SerializerProvider)} takes into
   * account {@link ScoutDataObjectModuleContext#isLenientMode()}.
   */
  @Test
  public void testSerializeWithInvalidMapType() throws Exception {
    TestEntityWithIIdDo doEntity = BEANS.get(TestEntityWithIIdDo.class);
    Map<Object, Object> map = new HashMap<>();
    map.put(FixtureUuId.of("1317fd5c-82f7-451e-b1a3-9cb837081e40"), 123); // valid key, invalid value
    map.put("unknown", "a string"); // invalid key, valid value
    doEntity.put(doEntity.iUuIdMap().getAttributeName(), map);
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.writeValueAsString(doEntity));

    String serialized = s_lenientDataObjectMapper.writeValueAsString(doEntity);
    assertEquals("{\"_type\":\"scout.TestEntityWithIId\",\"iUuIdMap\":{\"scout.FixtureUuId:1317fd5c-82f7-451e-b1a3-9cb837081e40\":123,\"unknown\":\"a string\"}}", serialized);
  }

  /**
   * Tests that {@link DoCollectionSerializer#serializeList(Iterable, JsonGenerator, SerializerProvider)} takes into
   * account {@link ScoutDataObjectModuleContext#isLenientMode()}.
   */
  @Test
  public void testSerializeWithInvalidCollectionType() throws Exception {
    TestEntityWithIIdDo doEntity = BEANS.get(TestEntityWithIIdDo.class);
    doEntity.putList(doEntity.stringIdsAsDoList().getAttributeName(), List.of("unknown", FixtureStringId.of("known"))); // invalid format of an IUuId
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.writeValueAsString(doEntity));

    String serialized = s_lenientDataObjectMapper.writeValueAsString(doEntity);
    assertEquals("{\"_type\":\"scout.TestEntityWithIId\",\"stringIdsAsDoList\":[\"unknown\",\"known\"]}", serialized);
  }

  /**
   * JSON without type information, expect concrete class.
   */
  @Test
  public void testDeserializeType_ConcreteClass1() throws Exception {
    String json = "{\"foo\" : \"bar\"}";
    TestItemEntityDo marshalled = s_dataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertEquals(TestItemEntityDo.class, marshalled.getClass());
  }

  /**
   * JSON with type information (class available), expect concrete class.
   */
  @Test
  public void testDeserializeType_ConcreteClass2() throws Exception {
    String json = "{\"_type\" : \"TestItemEntity\"}";
    // OK - read into correct class
    TestItemEntityDo marshalled = s_dataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertEquals(TestItemEntityDo.class, marshalled.getClass());
    // NOK - read into wrong class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestItemDo.class));

    s_lenientDataObjectMapper.readValue(json, TestItemDo.class);

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json, TestItemDo.class);
    assertEquals(TestItemEntityDo.class, marshalledLenient.getClass()); // different type than requested
  }

  /**
   * JSON with type information (class not known), expect concrete class.
   */
  @Test
  public void testDeserializeType_ConcreteClass3() throws JsonProcessingException {
    String json = "{\"_type\" : \"UnknownEntity\"}";
    // NOK - read into wrong class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestItemEntityDo.class));

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertEquals(DoEntity.class, marshalledLenient.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownEntity", marshalledLenient.getString("_type"));
  }

  /**
   * JSON with type information (class available) and nested type information (class available), expect concrete class.
   */
  @Test
  public void testDeserializeType_ConcreteClass4() throws Exception {
    String json = "{\"_type\" : \"TestItemEntity\", \"item\" : {\"_type\" : \"TestItem\"}}";
    // OK - read into correct class
    TestItemEntityDo marshalled = s_dataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertEquals(TestItemEntityDo.class, marshalled.getClass());
    assertEquals(TestItemDo.class, marshalled.getItem().getClass());
    // NOK - read into wrong class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestItemDo.class));

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json, TestItemDo.class);
    assertEquals(TestItemEntityDo.class, marshalledLenient.getClass()); // different type than requested

    // NOK - read into wrong class
    String json2 = "{\"_type\" : \"TestItemEntity\", \"item\" : {\"_type\" : \"TestItem2\"}}";
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json2, TestItemEntityDo.class));

    TestItemEntityDo marshalledLenient2 = s_lenientDataObjectMapper.readValue(json2, TestItemEntityDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient2.getItem());
    IDoEntity doEntity = marshalledLenient2.item().get(); // accessing this way works due to missing checks for generics at runtime
    // TestItemPojo2 is deserialized as DoTypedEntity with type 'TestItem2'
    assertEquals(DoEntity.class, doEntity.getClass()); // raw DO entity due to type mismatch
  }

  /**
   * JSON with type information (class available) and nested type information (class not known), expect concrete class.
   */
  @Test
  public void testDeserializeType_ConcreteClass5() throws JsonProcessingException {
    String json = "{\"_type\" : \"TestItemEntity\", \"item\" : {\"_type\" : \"UnknownEntity\"}}";
    // NOK - read into wrong class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestItemEntityDo.class));

    TestItemEntityDo marshalledLenient = s_lenientDataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient.getItem());

    IDoEntity doEntity = marshalledLenient.item().get(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(DoEntity.class, doEntity.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownEntity", doEntity.getString("_type"));
  }

  /**
   * JSON without type information, expect concrete interface.
   */
  @Test
  public void testDeserializeType_ConcreteIfc1() throws JsonProcessingException {
    String json = "{\"foo\" : \"bar\"}";
    // Assertion error: multiple instances found for query: interface org.eclipse.scout.rt.jackson.dataobject.fixture.ITestBaseEntityDo
    assertThrows(AssertionException.class, () -> s_dataObjectMapper.readValue(json, ITestBaseEntityDo.class));

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json, ITestBaseEntityDo.class);
    assertEquals(DoEntity.class, marshalledLenient.getClass()); // raw DO entity due to type mismatch
    assertEquals("bar", marshalledLenient.getString("foo"));
  }

  /**
   * JSON with type information (class available), expect concrete interface.
   */
  @Test
  public void testDeserializeType_ConcreteIfc2() throws Exception {
    String json = "{\"_type\" : \"TestEntityWithInterface1\"}";
    // OK - read into correct interface
    ITestBaseEntityDo marshalled = s_dataObjectMapper.readValue(json, ITestBaseEntityDo.class);
    assertTrue(marshalled instanceof ITestBaseEntityDo);
    // NOK - read into wrong interface
    String json2 = "{\"_type\" : \"TestItem\"}";
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json2, ITestBaseEntityDo.class));
    s_lenientDataObjectMapper.readValue(json, ITestBaseEntityDo.class);

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json2, ITestBaseEntityDo.class);
    assertEquals(TestItemDo.class, marshalledLenient.getClass()); // different type than requested
  }

  /**
   * JSON with type information (class not known), expect concrete interface.
   */
  @Test
  public void testDeserializeType_ConcreteIfc3() throws JsonProcessingException {
    String json = "{\"_type\" : \"UnknownEntity\"}";
    // NOK - read into wrong interface
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, ITestBaseEntityDo.class));

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json, ITestBaseEntityDo.class);
    assertEquals(DoEntity.class, marshalledLenient.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownEntity", marshalledLenient.getString("_type"));
  }

  /**
   * JSON with type information (class available) and nested type information (class available), expect concrete
   * interface.
   */
  @Test
  public void testDeserializeType_ConcreteIfc4() throws Exception {
    String json = "{\"_type\" : \"TestItemEntity\", \"itemIfc\" : {\"_type\" : \"TestEntityWithInterface1\"}}";
    // OK - read into correct class
    TestItemEntityDo marshalled = s_dataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertEquals(TestItemEntityDo.class, marshalled.getClass());
    assertEquals(TestEntityWithInterface1Do.class, marshalled.getItemIfc().getClass());
    // NOK - read into wrong class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestItemDo.class));

    IDoEntity marshalledLenient = s_lenientDataObjectMapper.readValue(json, TestItemDo.class);
    assertEquals(TestItemEntityDo.class, marshalledLenient.getClass()); // different type than requested

    // NOK - read into wrong class
    String json2 = "{\"_type\" : \"TestItemEntity\", \"itemIfc\" : {\"_type\" : \"TestItem\"}}";
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json2, TestItemEntityDo.class));

    TestItemEntityDo marshalledLenient2 = s_lenientDataObjectMapper.readValue(json2, TestItemEntityDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient2.getItemIfc());
    IDoEntity doEntity = marshalledLenient2.itemIfc().get(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(TestItemDo.class, doEntity.getClass()); // different type than requested
  }

  /**
   * JSON with type information (class available) and nested type information (class not known), expect concrete
   * interface.
   */
  @Test
  public void testDeserializeType_ConcreteIfc5() throws JsonProcessingException {
    String json = "{\"_type\" : \"TestItemEntity\", \"itemIfc\" : {\"_type\" : \"UnknownEntity\"}}";
    // NOK - read into wrong class
    assertThrows(JsonMappingException.class, () -> s_dataObjectMapper.readValue(json, TestItemEntityDo.class));

    TestItemEntityDo marshalledLenient = s_lenientDataObjectMapper.readValue(json, TestItemEntityDo.class);
    assertThrows(ClassCastException.class, () -> marshalledLenient.getItemIfc());
    IDoEntity doEntity = marshalledLenient.itemIfc().get(); // accessing this way works due to missing checks for generics at runtime
    assertEquals(DoEntity.class, doEntity.getClass()); // raw DO entity due to type mismatch
    assertEquals("UnknownEntity", doEntity.getString("_type"));
  }

  /**
   * JSON without type information, expect generic DoEntity.
   */
  @Test
  public void testDeserializeType_DoEntity1() throws Exception {
    String json = "{\"foo\" : \"bar\"}";
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals(DoEntity.class, marshalled.getClass());
    assertFalse(marshalled.has("_type"));
  }

  /**
   * JSON with type information (class available), expect generic DoEntity.
   */
  @Test
  public void testDeserializeType_DoEntity2() throws Exception {
    String json = "{\"_type\" : \"TestItem\"}";
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals(TestItemDo.class, marshalled.getClass());
    assertFalse(marshalled.has("_type"));
  }

  /**
   * JSON with type information (class not known), expect generic DoEntity.
   */
  @Test
  public void testDeserializeType_DoEntity3() throws Exception {
    String json = "{\"_type\" : \"UnknownEntity\"}";
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals(DoEntity.class, marshalled.getClass());
    assertEquals("UnknownEntity", marshalled.get("_type"));
  }

  /**
   * JSON with type information (class available), expect nested generic DoEntity.
   */
  @Test
  public void testDeserializeType_DoEntity4() throws Exception {
    String json = "{\"_type\" : \"TestNestedRaw\", \"doEntity\" : {\"_type\" : \"TestItem\"}}";
    TestNestedRawDo marshalled = s_dataObjectMapper.readValue(json, TestNestedRawDo.class);
    assertEquals(TestItemDo.class, marshalled.getDoEntity().getClass());
    assertFalse(marshalled.getDoEntity().has("_type"));
  }

  /**
   * JSON with type information (class not known), expect nested generic DoEntity.
   */
  @Test
  public void testDeserializeType_DoEntity5() throws Exception {
    String json = "{\"_type\" : \"TestNestedRaw\", \"doEntity\" : {\"_type\" : \"UnknownEntity\"}}";
    TestNestedRawDo marshalled = s_dataObjectMapper.readValue(json, TestNestedRawDo.class);
    assertEquals(DoEntity.class, marshalled.getDoEntity().getClass());
    assertEquals("UnknownEntity", marshalled.getDoEntity().get("_type"));
  }

  /**
   * JSON without type information, expect generic IDoEntity.
   */
  @Test
  public void testDeserializeType_IDoEntity1() throws Exception {
    String json = "{\"foo\" : \"bar\"}";
    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(DoEntity.class, marshalled.getClass());
    assertFalse(marshalled.has("_type"));
  }

  /**
   * JSON with type information (class available), expect generic IDoEntity.
   */
  @Test
  public void testDeserializeType_IDoEntity2() throws Exception {
    String json = "{\"_type\" : \"TestItem\"}";
    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(TestItemDo.class, marshalled.getClass());
    assertFalse(marshalled.has("_type"));
  }

  /**
   * JSON with type information (class not known), expect generic IDoEntity.
   */
  @Test
  public void testDeserializeType_IDoEntity3() throws Exception {
    String json = "{\"_type\" : \"UnknownEntity\"}";
    IDoEntity marshalled = s_dataObjectMapper.readValue(json, IDoEntity.class);
    assertEquals(DoEntity.class, marshalled.getClass());
    assertEquals("UnknownEntity", marshalled.get("_type"));
  }

  /**
   * JSON with type information (class available), expect nested generic IDoEntity.
   */
  @Test
  public void testDeserializeType_IDoEntity4() throws Exception {
    String json = "{\"_type\" : \"TestNestedRaw\", \"iDoEntity\" : {\"_type\" : \"TestItem\"}}";
    TestNestedRawDo marshalled = s_dataObjectMapper.readValue(json, TestNestedRawDo.class);
    assertEquals(TestItemDo.class, marshalled.getIDoEntity().getClass());
    assertFalse(marshalled.getIDoEntity().has("_type"));
  }

  /**
   * JSON with type information (class not known), expect nested generic IDoEntity.
   */
  @Test
  public void testDeserializeType_IDoEntity5() throws Exception {
    String json = "{\"_type\" : \"TestNestedRaw\", \"iDoEntity\" : {\"_type\" : \"UnknownEntity\"}}";
    TestNestedRawDo marshalled = s_dataObjectMapper.readValue(json, TestNestedRawDo.class);
    assertEquals(DoEntity.class, marshalled.getIDoEntity().getClass());
    assertEquals("UnknownEntity", marshalled.getIDoEntity().get("_type"));
  }

  /**
   * JSON without type information, expect generic IDataObject.
   */
  @Test
  public void testDeserializeType_IDataObject1() throws Exception {
    String json = "{\"foo\" : \"bar\"}";
    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEquals(DoEntity.class, marshalled.getClass());
    assertFalse(((IDoEntity) marshalled).has("_type"));
  }

  /**
   * JSON with type information (class available), expect generic IDataObject.
   */
  @Test
  public void testDeserializeType_IDataObject2() throws Exception {
    String json = "{\"_type\" : \"TestItem\"}";
    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEquals(TestItemDo.class, marshalled.getClass());
    assertFalse(((IDoEntity) marshalled).has("_type"));
  }

  /**
   * JSON with type information (class not known), expect generic IDataObject.
   */
  @Test
  public void testDeserializeType_IDataObject3() throws Exception {
    String json = "{\"_type\" : \"UnknownEntity\"}";
    IDataObject marshalled = s_dataObjectMapper.readValue(json, IDataObject.class);
    assertEquals(DoEntity.class, marshalled.getClass());
    assertEquals("UnknownEntity", ((IDoEntity) marshalled).get("_type"));
  }

  /**
   * JSON with type information (class available), expect nested generic IDataObject.
   */
  @Test
  public void testDeserializeType_IDataObject4() throws Exception {
    String json = "{\"_type\" : \"TestNestedRaw\", \"iDataObject\" : {\"_type\" : \"TestItem\"}}";
    TestNestedRawDo marshalled = s_dataObjectMapper.readValue(json, TestNestedRawDo.class);
    assertEquals(TestItemDo.class, marshalled.getIDataObject().getClass());
    assertFalse(((IDoEntity) marshalled.getIDataObject()).has("_type"));
  }

  /**
   * JSON with type information (class not known), expect nested generic IDataObject.
   */
  @Test
  public void testDeserializeType_IDataObject5() throws Exception {
    String json = "{\"_type\" : \"TestNestedRaw\", \"iDataObject\" : {\"_type\" : \"UnknownEntity\"}}";
    TestNestedRawDo marshalled = s_dataObjectMapper.readValue(json, TestNestedRawDo.class);
    assertEquals(DoEntity.class, marshalled.getIDataObject().getClass());
    assertEquals("UnknownEntity", ((IDoEntity) marshalled.getIDataObject()).get("_type"));
  }

  @Test
  public void testDeserializeTypedPojo() throws Exception {
    TestItemPojo pojo = new TestItemPojo();
    pojo.setId("foo");
    String json = s_defaultJacksonObjectMapper.writeValueAsString(pojo);
    // NOK - read into wrong pojo class
    assertThrows(InvalidTypeIdException.class, () -> s_defaultJacksonObjectMapper.readValue(json, TestItemPojo2.class));
    // NOK - read unknown type into pojo class
    assertThrows(InvalidTypeIdException.class, () -> s_defaultJacksonObjectMapper.readValue("{\"_type\":\"Unknown\"}", TestItemPojo.class));

    // no DO entity, even lenient data object mapper cannot deal with this
    assertThrows(InvalidTypeIdException.class, () -> s_lenientDataObjectMapper.readValue(json, TestItemPojo2.class));
    assertThrows(InvalidTypeIdException.class, () -> s_lenientDataObjectMapper.readValue("{\"_type\":\"Unknown\"}", TestItemPojo.class));
  }

  // ------------------------------------ common test helper methods ------------------------------------

  protected ObjectMapper createCustomScoutDoObjectMapper(Consumer<ScoutDataObjectModuleContext> contextConsumer) {
    //noinspection deprecation
    return new JacksonPrettyPrintDataObjectMapper() {
      @Override
      protected void prepareScoutDataModuleContext(ScoutDataObjectModuleContext moduleContext) {
        super.prepareScoutDataModuleContext(moduleContext);
        contextConsumer.accept(moduleContext);
      }
    }.getObjectMapper();
  }

  protected TestComplexEntityDo createTestDo() {
    TestComplexEntityDo testDo = BEANS.get(TestComplexEntityDo.class);
    testDo.id().set("4d2abc01-afc0-49f2-9eee-a99878d49728");
    testDo.stringAttribute().set("foo");
    testDo.integerAttribute().set(42);
    testDo.longAttribute().set(123L);
    testDo.floatAttribute().set(12.34f);
    testDo.doubleAttribute().set(56.78);
    testDo.bigDecimalAttribute().set(new BigDecimal("1.23456789"));
    testDo.bigIntegerAttribute().set(new BigInteger("123456789"));
    testDo.dateAttribute().set(DATE);
    testDo.objectAttribute().set("fooObject");
    testDo.withUuidAttribute(UUID.fromString("298d64f9-821d-49fe-91fb-6fb9860d4950"));
    testDo.withLocaleAttribute(Locale.forLanguageTag("de-CH"));

    List<TestItemDo> list = new ArrayList<>();
    list.add(BEANS.get(TestItemDo.class).withId("1234-1"));
    list.add(BEANS.get(TestItemDo.class).withId("1234-2"));
    testDo.itemsAttribute().set(list);

    testDo.itemAttribute().set(BEANS.get(TestItemDo.class).withId("1234-3").withStringAttribute("bar"));
    testDo.stringListAttribute().set(Arrays.asList("foo", "bar"));
    return testDo;
  }

  protected TestComplexEntityPojo createTestPoJo() {
    TestComplexEntityPojo testPoJo = new TestComplexEntityPojo();
    testPoJo.setId("4d2abc01-afc0-49f2-9eee-a99878d49728");
    testPoJo.setStringAttribute("foo");
    testPoJo.setIntegerAttribute(42);
    testPoJo.setLongAttribute(123L);
    testPoJo.setFloatAttribute(12.34f);
    testPoJo.setDoubleAttribute(56.78);
    testPoJo.setBigDecimalAttribute(new BigDecimal("1.23456789"));
    testPoJo.setBigIntegerAttribute(new BigInteger("123456789"));
    testPoJo.setDateAttribute(DATE);
    testPoJo.setObjectAttribute("fooObject");
    testPoJo.setUuidAttribute(UUID.fromString("298d64f9-821d-49fe-91fb-6fb9860d4950"));
    testPoJo.setLocaleAttribute(Locale.forLanguageTag("de-CH"));

    testPoJo.setItemsAttribute(new ArrayList<>());
    TestItemPojo testItemPoJo1 = new TestItemPojo();
    testItemPoJo1.setId("1234-1");
    testPoJo.getItemsAttribute().add(testItemPoJo1);

    TestItemPojo testItemPoJo2 = new TestItemPojo();
    testItemPoJo2.setId("1234-2");
    testPoJo.getItemsAttribute().add(testItemPoJo2);

    TestItemPojo testItemPoJo3 = new TestItemPojo();
    testItemPoJo3.setId("1234-3");
    testItemPoJo3.setStringAttribute("bar");
    testPoJo.setItemAttribute(testItemPoJo3);

    List<String> stringList = Arrays.asList("foo", "bar");
    testPoJo.setStringListAttribute(stringList);

    return testPoJo;
  }

  protected TestItemDo createTestItemDo(String id, String attribute) {
    return BEANS.get(TestItemDo.class).withId(id).withStringAttribute(attribute);
  }

  protected TestItem3Do createTestItem3Do(String id, BigDecimal attribute) {
    return BEANS.get(TestItem3Do.class).withId(id).withBigDecimalAttribute(attribute);
  }

  protected TestItemPojo createTestItemPojo(String id, String attribute) {
    TestItemPojo testPojo = new TestItemPojo();
    testPojo.setId(id);
    testPojo.setStringAttribute(attribute);
    return testPojo;
  }

  protected TestItemPojo2 createTestItemPojo2(String id, String attribute) {
    TestItemPojo2 testPojo = new TestItemPojo2();
    testPojo.setId(id);
    testPojo.setStringAttribute(attribute);
    return testPojo;
  }

  protected void assertJsonEquals(String expectedResourceName, String actual) {
    s_testHelper.assertJsonEquals(getResource(expectedResourceName), actual);
  }

  protected String readResourceAsString(String resourceName) throws IOException {
    return s_testHelper.readResourceAsString(getResource(resourceName));
  }

  protected URL getResource(String expectedResourceName) {
    return JsonDataObjectsSerializationTest.class.getResource(expectedResourceName);
  }

  protected void serializeContribution(Class<? extends IDoEntity> doEntityClass, Class<? extends IDoEntityContribution> contributionClass) throws JsonProcessingException {
    serializeContribution(BEANS.get(doEntityClass), contributionClass);
  }

  protected void serializeContribution(IDoEntity doEntity, Class<? extends IDoEntityContribution> contributionClass) throws JsonProcessingException {
    doEntity.contribution(contributionClass);
    s_dataObjectMapper.writeValueAsString(doEntity);
  }
}
