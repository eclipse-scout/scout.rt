/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureIntegerId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureWrapperCompositeId;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract test cases for {@link IdCodec} implementation.
 */
@SuppressWarnings("squid:S00100")
public abstract class AbstractIdCodecTest {

  protected static final UUID TEST_UUID = UUID.fromString("5833aae1-c813-4d7c-a342-56a53772a3ea");
  protected static final String TEST_STRING = "foobar";
  protected static final String TEST_STRING_2 = "bazäöl";
  protected static final Date TEST_DATE = new Date(123456789);

  protected IBean<?> m_codecBean;

  @Before
  public void before() {
    m_codecBean = BeanTestingHelper.get().registerBean(new BeanMetaData(getCodecReplacementType()).withReplace(true));
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBean(m_codecBean);
  }

  protected Class<? extends IdCodec> getCodecReplacementType() {
    return P_IdCodec.class;
  }

  /**
   * @return IdCodec instance used for tests
   */
  protected abstract IdCodec getCodec();

  @Test
  public void testToQualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    String ext1 = getCodec().toQualified(id1);
    assertEquals("scout.FixtureUuId:" + TEST_UUID, ext1);
  }

  @Test
  public void testToQualifiedStringId() {
    String s = "scout.FixtureDateId:my-contain_special$characters{like:or}";
    FixtureStringId id1 = FixtureStringId.of(s);
    String ext1 = getCodec().toQualified(id1);
    assertEquals("scout.FixtureStringId:scout.FixtureDateId:my-contain_special$characters{like:or}", ext1);
  }

  @Test
  public void testToQualifiedDateId() {
    Date date = new Date(123456789);
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    String ext1 = getCodec().toQualified(id1);
    assertEquals("scout.FixtureDateId:123456789", ext1);
  }

  @Test
  public void testToQualifiedLocaleId() {
    Locale locale = Locale.ITALY;
    FixtureLocaleId id = FixtureLocaleId.of(locale);
    String ext = getCodec().toQualified(id);
    assertEquals("scout.FixtureLocaleId:it-IT", ext);
  }

  @Test
  public void testToQualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> getCodec().toQualified(id));

    try {
      getCodec().registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
      String ext = getCodec().toQualified(id);
      assertEquals("scout.FixtureCustomComparableRawDataId:100", ext);
    }
    finally {
      getCodec().unregisterRawTypeMapper(CustomComparableRawDataType.class);
    }
  }

  @Test
  public void testToQualifiedCompositeId() {
    FixtureCompositeId id = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    String ext = getCodec().toQualified(id);
    assertEquals("scout.FixtureCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea", ext);
  }

  @Test
  public void testToQualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    String ext = getCodec().toQualified(id);
    assertEquals("scout.FixtureWrapperCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl", ext);
  }

  @Test
  public void testToQualifiedCompositeIdPartialNullValues() {
    FixtureCompositeWithNullValuesId id1 = FixtureCompositeWithNullValuesId.of(null, UUID.fromString("711dc5d6-0a42-4f54-b79c-50110b9e742a"));
    assertEquals("scout.FixtureCompositeWithNullValuesId:;711dc5d6-0a42-4f54-b79c-50110b9e742a", getCodec().toQualified(id1));
  }

  @Test
  public void testToQualifiedCompositeIdPartialNullStringValuesA() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("foo", "");
    assertEquals("scout.FixtureCompositeWithNullStringValuesId:foo;", getCodec().toQualified(id1));
  }

  @Test
  public void testToQualifiedCompositeIdPartialNullStringValuesB() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("", "bar");
    assertEquals("scout.FixtureCompositeWithNullStringValuesId:;bar", getCodec().toQualified(id1));
  }

  @Test
  public void testToQualifiedCompositeIdPartialNullAllTypes() {
    assertEquals("scout.FixtureCompositeWithAllTypesId:foo;;;;;", getCodec().toQualified(FixtureCompositeWithAllTypesId.of("foo", null, null, null, null, null)));
    assertEquals("scout.FixtureCompositeWithAllTypesId:;5833aae1-c813-4d7c-a342-56a53772a3ea;;;;", getCodec().toQualified(FixtureCompositeWithAllTypesId.of(null, TEST_UUID, null, null, null, null)));
    assertEquals("scout.FixtureCompositeWithAllTypesId:;;42;;;", getCodec().toQualified(FixtureCompositeWithAllTypesId.of(null, null, 42L, null, null, null)));
    assertEquals("scout.FixtureCompositeWithAllTypesId:;;;43;;", getCodec().toQualified(FixtureCompositeWithAllTypesId.of(null, null, null, 43, null, null)));
    assertEquals("scout.FixtureCompositeWithAllTypesId:;;;;123456789;", getCodec().toQualified(FixtureCompositeWithAllTypesId.of(null, null, null, null, TEST_DATE, null)));
    assertEquals("scout.FixtureCompositeWithAllTypesId:;;;;;de-DE", getCodec().toQualified(FixtureCompositeWithAllTypesId.of(null, null, null, null, null, Locale.GERMANY)));
  }

  @Test
  public void testToQualifiedIdNullValue() {
    assertNull(getCodec().toQualified(null));
  }

  @Test
  public void testToQualifiedIdUnsupportedType() {
    // no IdTypeName annotation
    assertThrows(PlatformException.class, () -> getCodec().toQualified((IId) () -> null));
  }

  @Test
  public void testToQualifiedUnknownId() {
    //noinspection deprecation
    UnknownId id = UnknownId.of("unknown", "foo");
    String qualifiedId = getCodec().toQualified(id);
    assertEquals("unknown:foo", qualifiedId);
  }

  @Test
  public void testToQualifiedUnknownId_idTypeNameNull() {
    //noinspection deprecation
    UnknownId id = UnknownId.of(null, "foo");
    String qualifiedId = getCodec().toQualified(id);
    assertEquals("foo", qualifiedId);
  }

  @Test
  public void testToQualifiedUnknownId_Composite() {
    //noinspection deprecation
    UnknownId id = UnknownId.of("unknown", "foo;bar;baz");
    String qualifiedId = getCodec().toQualified(id);
    assertEquals("unknown:foo;bar;baz", qualifiedId);
  }

  @Test
  public void testToQualifiedUnknownId_CompositeIdTypeNameNull() {
    //noinspection deprecation
    UnknownId id = UnknownId.of(null, "foo;bar;baz");
    String qualifiedId = getCodec().toQualified(id);
    assertEquals("foo;bar;baz", qualifiedId);
  }

  @Test
  public void testToUnqualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    String ext1 = getCodec().toUnqualified(id1);
    assertEquals(TEST_UUID.toString(), ext1);
  }

  @Test
  public void testToUnqualifiedDateId() {
    Date date = BEANS.get(IDateProvider.class).currentMillis();
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    String ext1 = getCodec().toUnqualified(id1);
    assertEquals("" + date.getTime(), ext1);
  }

  @Test
  public void testToUnqualifiedLocaleId() {
    Locale locale = Locale.US;
    FixtureLocaleId id = FixtureLocaleId.of(locale);
    String ext = getCodec().toUnqualified(id);
    assertEquals("en-US", ext);
  }

  @Test
  public void testToUnqualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> getCodec().toUnqualified(id));

    try {
      getCodec().registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
      String ext = getCodec().toUnqualified(id);
      assertEquals("100", ext);
    }
    finally {
      getCodec().unregisterRawTypeMapper(CustomComparableRawDataType.class);
    }
  }

  @Test
  public void testToUnqualifiedCompositeId() {
    FixtureCompositeId id = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    String ext = getCodec().toUnqualified(id);
    assertEquals("foobar;5833aae1-c813-4d7c-a342-56a53772a3ea", ext);
  }

  @Test
  public void testToUnqualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    String ext = getCodec().toUnqualified(id);
    assertEquals("foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl", ext);
  }

  @Test
  public void testToUnqualifiedCompositeIdPartialNullValues() {
    FixtureCompositeWithNullValuesId id1 = FixtureCompositeWithNullValuesId.of(null, UUID.fromString("711dc5d6-0a42-4f54-b79c-50110b9e742a"));
    assertEquals(";711dc5d6-0a42-4f54-b79c-50110b9e742a", getCodec().toUnqualified(id1));
  }

  @Test
  public void testToUnqualifiedCompositeIdPartialNullStringValuesA() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("foo", "");
    assertEquals("foo;", getCodec().toUnqualified(id1));
  }

  @Test
  public void testToUnqualifiedCompositeIdPartialNullStringValuesB() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("", "bar");
    assertEquals(";bar", getCodec().toUnqualified(id1));
  }

  @Test
  public void testToUnqualifiedCompositeIdPartialNullAllTypes() {
    assertEquals("foo;;;;;", getCodec().toUnqualified(FixtureCompositeWithAllTypesId.of("foo", null, null, null, null, null)));
    assertEquals(";5833aae1-c813-4d7c-a342-56a53772a3ea;;;;", getCodec().toUnqualified(FixtureCompositeWithAllTypesId.of(null, TEST_UUID, null, null, null, null)));
    assertEquals(";;42;;;", getCodec().toUnqualified(FixtureCompositeWithAllTypesId.of(null, null, 42L, null, null, null)));
    assertEquals(";;;43;;", getCodec().toUnqualified(FixtureCompositeWithAllTypesId.of(null, null, null, 43, null, null)));
    assertEquals(";;;;123456789;", getCodec().toUnqualified(FixtureCompositeWithAllTypesId.of(null, null, null, null, TEST_DATE, null)));
    assertEquals(";;;;;de-DE", getCodec().toUnqualified(FixtureCompositeWithAllTypesId.of(null, null, null, null, null, Locale.GERMANY)));
  }

  @Test
  public void testCompositeIdAllNullAllTypes() {
    assertNull(FixtureCompositeWithAllTypesId.of((String) null, null, null, null, null, null));
    assertNull(FixtureCompositeWithAllTypesId.of((FixtureStringId) null, null, null, null, null, null));
  }

  @Test
  public void testToUnqualifiedIdNullValue() {
    assertNull(getCodec().toUnqualified(null));
  }

  @Test
  public void testToUnqualifiedIdUnsupportedType() {
    // unsupported type to unwrap
    assertThrows(PlatformException.class, () -> getCodec().toUnqualified((IId) () -> null));
  }

  @Test
  public void testToUnqualifiedUnknownId() {
    //noinspection deprecation
    UnknownId id = UnknownId.of("unknown", "foo");
    String unqualifiedId = getCodec().toUnqualified(id);
    assertEquals("foo", unqualifiedId);
  }

  @Test
  public void testToUnqualifiedUnknownId_Composite() {
    //noinspection deprecation
    UnknownId id = UnknownId.of("unknown", "foo;bar;baz");
    String unqualifiedId = getCodec().toUnqualified(id);
    assertEquals("foo;bar;baz", unqualifiedId);
  }

  @Test
  public void testFromQualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    IId id2 = getCodec().fromQualified("scout.FixtureUuId:" + TEST_UUID);
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedStringId() {
    String s = "scout.FixtureDateId:my-contain_special$characters{like:or}";
    FixtureStringId id1 = FixtureStringId.of(s);
    IId id2 = getCodec().fromQualified("scout.FixtureStringId:" + s);
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedDateId() {
    Date date = new Date(123456789);
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    IId id2 = getCodec().fromQualified("scout.FixtureDateId:123456789");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedLocaleId() {
    Locale locale = Locale.GERMANY;
    FixtureLocaleId id1 = FixtureLocaleId.of(locale);
    IId id2 = getCodec().fromQualified("scout.FixtureLocaleId:de-DE");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id1 = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> getCodec().fromQualified("scout.FixtureCustomComparableRawDataId:100"));

    try {
      getCodec().registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
      IId id2 = getCodec().fromQualified("scout.FixtureCustomComparableRawDataId:100");
      assertEquals(id1, id2);
    }
    finally {
      getCodec().unregisterRawTypeMapper(CustomComparableRawDataType.class);
    }
  }

  @Test
  public void testFromQualifiedCompositeId() {
    FixtureCompositeId id1 = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    IId id2 = getCodec().fromQualified("scout.FixtureCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id1 = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    IId id2 = getCodec().fromQualified("scout.FixtureWrapperCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedNullValue() {
    assertNull(getCodec().fromQualified(null));
  }

  @Test
  public void testFromQualifiedRootIdEmptyValue() {
    FixtureStringId id1 = FixtureStringId.of("");
    IId id2 = getCodec().fromQualified("scout.FixtureStringId:");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeIdPartlyEmptyValue() {
    FixtureCompositeId id1 = FixtureCompositeId.of("", TEST_UUID);
    IId id2 = getCodec().fromQualified("scout.FixtureCompositeId:;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeIdAllEmptyValue() {
    assertNull(getCodec().fromQualified("scout.FixtureCompositeWithNullStringValuesId:;"));
    assertNull(getCodec().fromQualified("scout.FixtureCompositeWithNullStringValuesId:"));
    assertNull(getCodec().fromQualified("scout.FixtureCompositeId:"));
    assertNull(getCodec().fromQualified("scout.FixtureCompositeId:;"));
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullValues() {
    FixtureCompositeWithNullValuesId id1 = FixtureCompositeWithNullValuesId.of(null, UUID.fromString("711dc5d6-0a42-4f54-b79c-50110b9e742a"));
    IId id2 = getCodec().fromQualified("scout.FixtureCompositeWithNullValuesId:;711dc5d6-0a42-4f54-b79c-50110b9e742a");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullStringValuesA() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("foo", "");
    FixtureCompositeWithNullStringValuesId id2 = FixtureCompositeWithNullStringValuesId.of("foo", null);
    assertEquals(id1, id2);
    IId id3 = getCodec().fromQualified("scout.FixtureCompositeWithNullStringValuesId:foo;");
    assertEquals(id1, id3);
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullStringValuesB() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("", "bar");
    FixtureCompositeWithNullStringValuesId id2 = FixtureCompositeWithNullStringValuesId.of(null, "bar");
    assertEquals(id1, id2);
    IId id3 = getCodec().fromQualified("scout.FixtureCompositeWithNullStringValuesId:;bar");
    assertEquals(id1, id3);
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullAllTypes() {
    assertEquals(FixtureCompositeWithAllTypesId.of("foo", null, null, null, null, null), getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:foo;;;;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, TEST_UUID, null, null, null, null), getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;5833aae1-c813-4d7c-a342-56a53772a3ea;;;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, 42L, null, null, null), getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;;42;;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, null, 43, null, null), getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;;;43;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, null, null, TEST_DATE, null), getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;;;;123456789;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, null, null, null, Locale.GERMANY), getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;;;;;de-DE"));
  }

  @Test
  public void testFromQualifiedCompositeIdWrongNumberOfComponents() {
    assertThrows(PlatformException.class, () -> getCodec().fromQualified("scout.FixtureCompositeId:foo"));
    assertThrows(PlatformException.class, () -> getCodec().fromQualified("scout.FixtureCompositeId:foo;" + TEST_UUID + ";foo"));
    assertThrows(PlatformException.class, () -> getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;"));
    assertThrows(PlatformException.class, () -> getCodec().fromQualified("scout.FixtureCompositeWithAllTypesId:;;;"));
  }

  @Test
  public void testFromUnqualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    FixtureUuId id2 = getCodec().fromUnqualified(FixtureUuId.class, TEST_UUID.toString());
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedDateId() {
    Date date = new Date(123456789);
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    IId id2 = getCodec().fromUnqualified(FixtureDateId.class, "123456789");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedLocaleId() {
    Locale locale = Locale.GERMANY;
    FixtureLocaleId id1 = FixtureLocaleId.of(locale);
    IId id2 = getCodec().fromUnqualified(FixtureLocaleId.class, "de-DE");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id1 = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(FixtureCustomComparableRawDataId.class, "100"));

    try {
      getCodec().registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
      IId id2 = getCodec().fromUnqualified(FixtureCustomComparableRawDataId.class, "100");
      assertEquals(id1, id2);
    }
    finally {
      getCodec().unregisterRawTypeMapper(CustomComparableRawDataType.class);
    }
  }

  @Test
  public void testFromUnqualifiedCompositeId() {
    FixtureCompositeId id1 = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    FixtureCompositeId id2 = getCodec().fromUnqualified(FixtureCompositeId.class, "foobar;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id1 = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    IId id2 = getCodec().fromUnqualified(FixtureWrapperCompositeId.class, "foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedNullIdClass() {
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(null, null));
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(null, "foo"));
  }

  @Test
  public void testFromUnqualifiedNullValue() {
    assertNull(getCodec().fromUnqualified(FixtureStringId.class, null));
  }

  @Test
  public void testFromUnqualifiedStringIdEmptyValue() {
    FixtureStringId id1 = FixtureStringId.of("");
    assertNull(id1);
    IId id2 = getCodec().fromUnqualified(FixtureStringId.class, "");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedUuIdEmptyValue() {
    FixtureUuId id1 = FixtureUuId.of("");
    assertNull(id1);
    IId id2 = getCodec().fromUnqualified(FixtureUuId.class, "");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedCompositeIdPartialEmptyValue() {
    FixtureCompositeId id1 = FixtureCompositeId.of("", TEST_UUID);
    IId id2 = getCodec().fromUnqualified(FixtureCompositeId.class, ";" + TEST_UUID);
    assertEquals(id1, id2);

    FixtureCompositeId id3 = FixtureCompositeId.of("abc", null);
    assertNull(id3);
    IId id4 = getCodec().fromUnqualified(FixtureCompositeId.class, "foo;");
    assertNull(id4);
  }

  @Test
  public void testFromUnqualifiedCompositeIdAllEmptyValue() {
    assertNull(FixtureCompositeId.of("", null));
    assertNull(getCodec().fromUnqualified(FixtureCompositeId.class, ""));
    assertNull(getCodec().fromUnqualified(FixtureCompositeId.class, ";"));
  }

  @Test
  public void testFromUnqualifiedCompositeIdPartialNullAllTypes() {
    assertEquals(FixtureCompositeWithAllTypesId.of("foo", null, null, null, null, null), getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, "foo;;;;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, TEST_UUID, null, null, null, null), getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";5833aae1-c813-4d7c-a342-56a53772a3ea;;;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, 42L, null, null, null), getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";;42;;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, null, 43, null, null), getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";;;43;;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, null, null, TEST_DATE, null), getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";;;;123456789;"));
    assertEquals(FixtureCompositeWithAllTypesId.of(null, null, null, null, null, Locale.GERMANY), getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";;;;;de-DE"));
  }

  @Test
  public void testFromUnqualifiedCompositeIdWrongNumberOfComponents() {
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(FixtureCompositeId.class, "foo"));
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(FixtureCompositeId.class, "foo;" + TEST_UUID + ";foo"));
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";"));
    assertThrows(PlatformException.class, () -> getCodec().fromUnqualified(FixtureCompositeWithAllTypesId.class, ";;;"));
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_InvalidType() {
    getCodec().fromQualified("scout.FixtureUuId:Other:" + TEST_UUID);
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_UnknownType() {
    getCodec().fromQualified("DoesNotExist:" + TEST_UUID);
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_InvalidFormat_noColon() {
    getCodec().fromQualified("DoesNotExist");
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_InvalidFormat_duplicateColon() {
    getCodec().fromQualified("DoesNotExist:d:d");
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_UnsupportedWrappedType() {
    getCodec().fromQualified("DoesNotExist:" + TEST_UUID);
  }

  @Test
  public void testFromQualifiedLenient_Default() {
    FixtureUuId id = FixtureUuId.of(TEST_UUID);
    IId id2 = getCodec().fromQualified("scout.FixtureUuId:" + TEST_UUID, IdCodecFlag.LENIENT);
    assertEquals(id, id2);
  }

  @Test
  public void testFromQualifiedLenient_UnknownType() {
    IId id = getCodec().fromQualified("DoesNotExist:" + TEST_UUID, IdCodecFlag.LENIENT);
    assertTrue(id instanceof UnknownId);
    UnknownId uid = (UnknownId) id;
    assertEquals("DoesNotExist", uid.getIdTypeName());
    assertEquals(TEST_UUID.toString(), uid.getId());
    assertEquals(TEST_UUID.toString(), uid.unwrap());
    //noinspection deprecation
    assertEquals(UnknownId.of("DoesNotExist", TEST_UUID.toString()), id);
  }

  @Test
  public void testFromQualifiedLenient_CompositeUnknownType() {
    IId id = getCodec().fromQualified("DoesNotExist:abc;123", IdCodecFlag.LENIENT);
    assertTrue(id instanceof UnknownId);
    UnknownId uid = (UnknownId) id;
    assertEquals("DoesNotExist", uid.getIdTypeName());
    assertEquals("abc;123", uid.getId());
    assertEquals("abc;123", uid.unwrap());
    //noinspection deprecation
    assertEquals(UnknownId.of("DoesNotExist", "abc;123"), id);
  }

  @Test
  public void testFromQualifiedLenient_WrongFormat() {
    IId id = getCodec().fromQualified("Does:Not:Exist:" + TEST_UUID, IdCodecFlag.LENIENT);
    assertTrue(id instanceof UnknownId);
    UnknownId uid = (UnknownId) id;
    assertEquals("Does", uid.getIdTypeName());
    assertEquals("Not:Exist:" + TEST_UUID, uid.getId());
    assertEquals("Not:Exist:" + TEST_UUID, uid.unwrap());
    //noinspection deprecation
    assertEquals(UnknownId.of("Does", "Not:Exist:" + TEST_UUID), id);
  }

  @Test
  public void testFromQualifiedLenient_NoIdTypeName() {
    IId id = getCodec().fromQualified("Foo" + TEST_UUID, IdCodecFlag.LENIENT);
    assertTrue(id instanceof UnknownId);
    UnknownId uid = (UnknownId) id;
    assertNull(uid.getIdTypeName());
    assertEquals("Foo" + TEST_UUID, uid.getId());
    assertEquals("Foo" + TEST_UUID, uid.unwrap());
    //noinspection deprecation
    assertEquals(UnknownId.of(null, "Foo" + TEST_UUID), id);
  }

  @Test
  public void testFromQualifiedLenient_CompositeWrongCardinalityToSmall() {
    IId id = getCodec().fromQualified("scout.FixtureCompositeId:" + TEST_UUID, IdCodecFlag.LENIENT);
    assertTrue(id instanceof UnknownId);
    UnknownId uid = (UnknownId) id;
    assertEquals("scout.FixtureCompositeId", uid.getIdTypeName());
    assertEquals(TEST_UUID.toString(), uid.getId());
    assertEquals(TEST_UUID.toString(), uid.unwrap());
    //noinspection deprecation
    assertEquals(UnknownId.of("scout.FixtureCompositeId", TEST_UUID.toString()), id);
  }

  @Test
  public void testFromQualifiedLenient_CompositeWrongCardinalityToHigh() {
    IId id = getCodec().fromQualified("scout.FixtureCompositeId:" + "a;b;c", IdCodecFlag.LENIENT);
    assertTrue(id instanceof UnknownId);
    UnknownId uid = (UnknownId) id;
    assertEquals("scout.FixtureCompositeId", uid.getIdTypeName());
    assertEquals("a;b;c", uid.getId());
    assertEquals("a;b;c", uid.unwrap());
    //noinspection deprecation
    assertEquals(UnknownId.of("scout.FixtureCompositeId", "a;b;c"), id);
  }

  @Test
  public void testRegisterTypeMapper() {
    assertThrows(AssertionException.class, () -> getCodec().registerRawTypeMapper(null, x -> x, x -> "x"));
    assertThrows(AssertionException.class, () -> getCodec().registerRawTypeMapper(String.class, null, x -> "x"));
    assertThrows(AssertionException.class, () -> getCodec().registerRawTypeMapper(String.class, x -> x, null));
  }

  @Test
  public void testQualifiedSignature() {
    var ids = new HashSet<IId>();
    collectSignatureIds(ids);
    ids.forEach(id -> {
      String serialized = getCodec().toQualified(id, IdCodecFlag.SIGNATURE);
      IId deserialized = getCodec().fromQualified(serialized, IdCodecFlag.SIGNATURE);
      assertQualifiedSignature(id, serialized, deserialized);

      serialized = getCodec().toQualified(id, Set.of(IdCodecFlag.SIGNATURE));
      deserialized = getCodec().fromQualified(serialized, Set.of(IdCodecFlag.SIGNATURE));
      assertQualifiedSignature(id, serialized, deserialized);
    });
  }

  @Test
  public void testUnqualifiedSignature() {
    var ids = new HashSet<IId>();
    collectSignatureIds(ids);
    ids.forEach(id -> {
      String serialized = getCodec().toUnqualified(id, IdCodecFlag.SIGNATURE);
      IId deserialized = getCodec().fromUnqualified(id.getClass(), serialized, IdCodecFlag.SIGNATURE);
      assertUnqualifiedSignature(id, serialized, deserialized);

      serialized = getCodec().toUnqualified(id, Set.of(IdCodecFlag.SIGNATURE));
      deserialized = getCodec().fromUnqualified(id.getClass(), serialized, Set.of(IdCodecFlag.SIGNATURE));
      assertUnqualifiedSignature(id, serialized, deserialized);
    });
  }

  protected void assertQualifiedSignature(IId id, String serialized, IId deserialized) {
    assertSignature(id, serialized, deserialized);
  }

  protected void assertUnqualifiedSignature(IId id, String serialized, IId deserialized) {
    assertSignature(id, serialized, deserialized);
  }

  protected void assertSignature(IId id, String serialized, IId deserialized) {
    assertEquals(id, deserialized);
  }

  protected void collectSignatureIds(Set<IId> ids) {
    ids.add(FixtureUuId.of(TEST_UUID));
    ids.add(FixtureStringId.of(TEST_STRING));
    ids.add(FixtureDateId.of(TEST_DATE));
    ids.add(FixtureLocaleId.of(Locale.ITALY));
    ids.add(FixtureCompositeId.of(TEST_STRING, TEST_UUID));
    ids.add(FixtureWrapperCompositeId.of(TEST_STRING, TEST_UUID, TEST_STRING_2));
    ids.add(FixtureCompositeWithNullValuesId.of(null, UUID.fromString("711dc5d6-0a42-4f54-b79c-50110b9e742a")));
    ids.add(FixtureCompositeWithNullStringValuesId.of("foo", ""));
    ids.add(FixtureCompositeWithNullStringValuesId.of("", "bar"));
    ids.add(FixtureCompositeWithAllTypesId.of("foo", null, null, null, null, null));
    ids.add(FixtureCompositeWithAllTypesId.of(null, TEST_UUID, null, null, null, null));
    ids.add(FixtureCompositeWithAllTypesId.of(null, null, 42L, null, null, null));
    ids.add(FixtureCompositeWithAllTypesId.of(null, null, null, 43, null, null));
    ids.add(FixtureCompositeWithAllTypesId.of(null, null, null, null, TEST_DATE, null));
    ids.add(FixtureCompositeWithAllTypesId.of(null, null, null, null, null, Locale.GERMANY));
  }

  @Test
  public void testEmptyIdWithSignature() {
    assertThrows(AssertionException.class, () -> getCodec().fromQualified("scout.FixtureIntegerId:###CNCgkNhEN4PEpNkWvRPY/jEIwn49f1xGLgmXyi6SdlI=", IdCodecFlag.SIGNATURE));
    assertThrows(AssertionException.class, () -> getCodec().fromUnqualified(FixtureIntegerId.class, "###CNCgkNhEN4PEpNkWvRPY/jEIwn49f1xGLgmXyi6SdlI=", IdCodecFlag.SIGNATURE));
  }

  @Test
  public void testSignatureIdWithoutPassword() {
    BeanTestingHelper.get().unregisterBean(m_codecBean);

    var stringId = FixtureStringId.of("some");
    assertEquals("scout.FixtureStringId:some", getCodec().toQualified(stringId, IdCodecFlag.SIGNATURE));
    assertEquals("some", getCodec().toUnqualified(stringId, IdCodecFlag.SIGNATURE));
    assertEquals(stringId, getCodec().fromQualified("scout.FixtureStringId:some", IdCodecFlag.SIGNATURE));
    assertEquals(stringId, getCodec().fromUnqualified(FixtureStringId.class, "some", IdCodecFlag.SIGNATURE));

    var integerId = FixtureIntegerId.of(42);
    assertThrows(AssertionException.class, () -> getCodec().toQualified(integerId, IdCodecFlag.SIGNATURE));
    assertThrows(AssertionException.class, () -> getCodec().toUnqualified(integerId, IdCodecFlag.SIGNATURE));
    assertThrows(AssertionException.class, () -> getCodec().fromQualified("scout.FixtureIntegerId:42###CNCgkNhEN4PEpNkWvRPY/jEIwn49f1xGLgmXyi6SdlI=", IdCodecFlag.SIGNATURE));
    assertThrows(AssertionException.class, () -> getCodec().fromUnqualified(FixtureIntegerId.class, "42###CNCgkNhEN4PEpNkWvRPY/jEIwn49f1xGLgmXyi6SdlI=", IdCodecFlag.SIGNATURE));
  }

  @IdTypeName("scout.FixtureDateId")
  protected static final class FixtureDateId extends AbstractRootId<Date> {
    private static final long serialVersionUID = 1L;

    private FixtureDateId(Date id) {
      super(id);
    }

    public static FixtureDateId of(Date date) {
      if (date == null) {
        return null;
      }
      return new FixtureDateId(date);
    }
  }

  @IdTypeName("scout.FixtureCompositeWithNullStringValuesId")
  protected static final class FixtureCompositeWithNullStringValuesId extends AbstractCompositeId {
    private static final long serialVersionUID = 1L;

    private FixtureCompositeWithNullStringValuesId(FixtureStringId c1, FixtureStringId c2) {
      super(c1, c2);
    }

    @RawTypes
    public static FixtureCompositeWithNullStringValuesId of(String c1, String c2) {
      if (StringUtility.isNullOrEmpty(c1) && StringUtility.isNullOrEmpty(c2)) {
        return null;
      }
      return new FixtureCompositeWithNullStringValuesId(FixtureStringId.of(c1), FixtureStringId.of(c2));
    }

    public static FixtureCompositeWithNullStringValuesId of(FixtureStringId c1, FixtureStringId c2) {
      if (c1 == null && c2 == null) {
        return null;
      }
      return new FixtureCompositeWithNullStringValuesId(c1, c2);
    }
  }

  @IdTypeName("scout.FixtureCompositeWithNullValuesId")
  protected static final class FixtureCompositeWithNullValuesId extends AbstractCompositeId {
    private static final long serialVersionUID = 1L;

    private FixtureCompositeWithNullValuesId(FixtureStringId c1, FixtureUuId c2) {
      super(c1, c2);
    }

    @RawTypes
    public static FixtureCompositeWithNullValuesId of(String c1, UUID c2) {
      if (StringUtility.isNullOrEmpty(c1) && c2 == null) {
        return null;
      }
      return new FixtureCompositeWithNullValuesId(FixtureStringId.of(c1), FixtureUuId.of(c2));
    }

    public static FixtureCompositeWithNullValuesId of(FixtureStringId c1, FixtureUuId c2) {
      if (c1 == null && c2 == null) {
        return null;
      }
      return new FixtureCompositeWithNullValuesId(c1, c2);
    }
  }

  @IdTypeName("scout.FixtureCompositeWithAllTypesId")
  protected static final class FixtureCompositeWithAllTypesId extends AbstractCompositeId {
    private static final long serialVersionUID = 1L;

    private FixtureCompositeWithAllTypesId(FixtureStringId c1, FixtureUuId c2, FixtureLongId c3, FixtureIntegerId c4, FixtureDateId c5, FixtureLocaleId c6) {
      super(c1, c2, c3, c4, c5, c6);
    }

    @RawTypes
    public static FixtureCompositeWithAllTypesId of(String c1, UUID c2, Long c3, Integer c4, Date c5, Locale c6) {
      if (StringUtility.isNullOrEmpty(c1) && c2 == null && c3 == null && c4 == null && c5 == null && c6 == null) {
        return null;
      }
      return new FixtureCompositeWithAllTypesId(FixtureStringId.of(c1), FixtureUuId.of(c2), FixtureLongId.of(c3), FixtureIntegerId.of(c4), FixtureDateId.of(c5), FixtureLocaleId.of(c6));
    }

    public static FixtureCompositeWithAllTypesId of(FixtureStringId c1, FixtureUuId c2, FixtureLongId c3, FixtureIntegerId c4, FixtureDateId c5, FixtureLocaleId c6) {
      if (c1 == null && c2 == null && c3 == null && c4 == null && c5 == null && c6 == null) {
        return null;
      }
      return new FixtureCompositeWithAllTypesId(c1, c2, c3, c4, c5, c6);
    }
  }

  @IdTypeName("scout.FixtureLocaleId")
  protected static final class FixtureLocaleId extends AbstractRootId<Locale> {
    private static final long serialVersionUID = 1L;

    private FixtureLocaleId(Locale id) {
      super(id);
    }

    public static FixtureLocaleId of(Locale locale) {
      if (locale == null) {
        return null;
      }
      return new FixtureLocaleId(locale);
    }
  }

  protected static final class CustomComparableRawDataType implements Comparable<CustomComparableRawDataType> {
    private final int m_value;

    private CustomComparableRawDataType(int value) {
      m_value = value;
    }

    static CustomComparableRawDataType of(String value) {
      return new CustomComparableRawDataType(Integer.parseInt(value));
    }

    @Override
    public String toString() {
      return "" + m_value;
    }

    @Override
    public int compareTo(CustomComparableRawDataType o) {
      return m_value - o.m_value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CustomComparableRawDataType that = (CustomComparableRawDataType) o;
      return m_value == that.m_value;
    }

    @Override
    public int hashCode() {
      return Objects.hash(m_value);
    }
  }

  @IdTypeName("scout.FixtureCustomComparableRawDataId")
  protected static final class FixtureCustomComparableRawDataId extends AbstractRootId<CustomComparableRawDataType> {
    private static final long serialVersionUID = 1L;

    private FixtureCustomComparableRawDataId(CustomComparableRawDataType id) {
      super(id);
    }

    public static FixtureCustomComparableRawDataId of(CustomComparableRawDataType date) {
      if (date == null) {
        return null;
      }
      return new FixtureCustomComparableRawDataId(date);
    }
  }

  protected static class P_IdCodec extends IdCodec {

    @Override
    protected byte[] getIdSignaturePassword() {
      return "42".getBytes(StandardCharsets.UTF_8);
    }
  }
}
