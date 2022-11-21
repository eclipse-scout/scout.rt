/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureWrapperCompositeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link IdCodec}.
 */
public class IdCodecTest {

  protected static final UUID TEST_UUID = UUID.fromString("5833aae1-c813-4d7c-a342-56a53772a3ea");
  protected static final String TEST_STRING = "foobar";
  protected static final String TEST_STRING_2 = "bazäöl";

  protected IdCodec m_codec;

  @Before
  public void before() {
    m_codec = new IdCodec(); // create new instance for each test since some tests register custom type mappers
    m_codec.initialize(); // setup default type mapper
  }

  @Test
  public void testToQualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    String ext1 = m_codec.toQualified(id1);
    assertEquals("scout.FixtureUuId:" + TEST_UUID, ext1);
  }

  @Test
  public void testToQualifiedDateId() {
    Date date = new Date(123456789);
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    String ext1 = m_codec.toQualified(id1);
    assertEquals("scout.FixtureDateId:123456789", ext1);
  }

  @Test
  public void testToQualifiedLocaleId() {
    Locale locale = Locale.ITALY;
    FixtureLocaleId id = FixtureLocaleId.of(locale);
    String ext = m_codec.toQualified(id);
    assertEquals("scout.FixtureLocaleId:it-IT", ext);
  }

  @Test
  public void testToQualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> m_codec.toQualified(id));

    m_codec.registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
    String ext = m_codec.toQualified(id);
    assertEquals("scout.FixtureCustomComparableRawDataId:100", ext);
  }

  @Test
  public void testToQualifiedCompositeId() {
    FixtureCompositeId id = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    String ext = m_codec.toQualified(id);
    assertEquals("scout.FixtureCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea", ext);
  }

  @Test
  public void testToQualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    String ext = m_codec.toQualified(id);
    assertEquals("scout.FixtureWrapperCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl", ext);
  }

  @Test
  public void testToQualifiedIdNullValue() {
    assertNull(m_codec.toQualified(null));
  }

  @Test
  public void testToQualifiedIdUnsupportedType() {
    // no IdTypeName annotation
    assertThrows(PlatformException.class, () -> m_codec.toQualified((IId) () -> null));
  }

  @Test
  public void testToUnqualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    String ext1 = m_codec.toUnqualified(id1);
    assertEquals(TEST_UUID.toString(), ext1);
  }

  @Test
  public void testToUnqualifiedDateId() {
    Date date = BEANS.get(IDateProvider.class).currentMillis();
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    String ext1 = m_codec.toUnqualified(id1);
    assertEquals("" + date.getTime(), ext1);
  }

  @Test
  public void testToUnqualifiedLocaleId() {
    Locale locale = Locale.US;
    FixtureLocaleId id = FixtureLocaleId.of(locale);
    String ext = m_codec.toUnqualified(id);
    assertEquals("en-US", ext);
  }

  @Test
  public void testToUnqualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> m_codec.toUnqualified(id));

    m_codec.registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
    String ext = m_codec.toUnqualified(id);
    assertEquals("100", ext);
  }

  @Test
  public void testToUnqualifiedCompositeId() {
    FixtureCompositeId id = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    String ext = m_codec.toUnqualified(id);
    assertEquals("foobar;5833aae1-c813-4d7c-a342-56a53772a3ea", ext);
  }

  @Test
  public void testToUnqualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    String ext = m_codec.toUnqualified(id);
    assertEquals("foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl", ext);
  }

  @Test
  public void testToUnqualifiedIdNullValue() {
    assertNull(m_codec.toUnqualified(null));
  }

  @Test
  public void testToUnqualifiedIdUnsupportedType() {
    // unsupported type to unwrap
    assertThrows(PlatformException.class, () -> m_codec.toUnqualified((IId) () -> null));
  }

  @Test
  public void testFromQualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    IId id2 = m_codec.fromQualified("scout.FixtureUuId:" + TEST_UUID);
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedDateId() {
    Date date = new Date(123456789);
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    IId id2 = m_codec.fromQualified("scout.FixtureDateId:123456789");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedLocaleId() {
    Locale locale = Locale.GERMANY;
    FixtureLocaleId id1 = FixtureLocaleId.of(locale);
    IId id2 = m_codec.fromQualified("scout.FixtureLocaleId:de-DE");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id1 = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> m_codec.fromQualified("scout.FixtureCustomComparableRawDataId:100"));

    m_codec.registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
    IId id2 = m_codec.fromQualified("scout.FixtureCustomComparableRawDataId:100");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeId() {
    FixtureCompositeId id1 = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    IId id2 = m_codec.fromQualified("scout.FixtureCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id1 = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    IId id2 = m_codec.fromQualified("scout.FixtureWrapperCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedNullValue() {
    assertNull(m_codec.fromQualified(null));
  }

  @Test
  public void testFromQualifiedRootIdEmptyValue() {
    FixtureStringId id1 = FixtureStringId.of("");
    IId id2 = m_codec.fromQualified("scout.FixtureStringId:");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeIdPartlyEmptyValue() {
    FixtureCompositeId id1 = FixtureCompositeId.of("", TEST_UUID);
    IId id2 = m_codec.fromQualified("scout.FixtureCompositeId:;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeIdAllEmptyValue() {
    assertNull(m_codec.fromQualified("scout.FixtureCompositeWithNullStringValuesId:;"));
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullValues() {
    FixtureCompositeWithNullValuesId id1 = FixtureCompositeWithNullValuesId.of(null, UUID.fromString("711dc5d6-0a42-4f54-b79c-50110b9e742a"));
    IId id2 = m_codec.fromQualified("scout.FixtureCompositeWithNullValuesId:;711dc5d6-0a42-4f54-b79c-50110b9e742a");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullStringValuesA() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("foo", "");
    FixtureCompositeWithNullStringValuesId id2 = FixtureCompositeWithNullStringValuesId.of("foo", null);
    assertEquals(id1, id2);
    IId id3 = m_codec.fromQualified("scout.FixtureCompositeWithNullStringValuesId:foo;");
    assertEquals(id1, id3);
  }

  @Test
  public void testFromQualifiedCompositeIdPartialNullStringValuesB() {
    FixtureCompositeWithNullStringValuesId id1 = FixtureCompositeWithNullStringValuesId.of("", "bar");
    FixtureCompositeWithNullStringValuesId id2 = FixtureCompositeWithNullStringValuesId.of(null, "bar");
    assertEquals(id1, id2);
    IId id3 = m_codec.fromQualified("scout.FixtureCompositeWithNullStringValuesId:;bar");
    assertEquals(id1, id3);
  }

  @Test
  public void testFromQualifiedCompositeIdWrongNumberOfComponents() {
    assertThrows(PlatformException.class, () -> m_codec.fromQualified("scout.FixtureCompositeId:foo"));
    assertThrows(PlatformException.class, () -> m_codec.fromQualified("scout.FixtureCompositeId:foo;" + TEST_UUID + ";foo"));
    assertThrows(PlatformException.class, () -> m_codec.fromQualified("scout.FixtureCompositeId:"));
  }

  @Test
  public void testFromUnqualifiedRootId() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    FixtureUuId id2 = m_codec.fromUnqualified(FixtureUuId.class, TEST_UUID.toString());
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedDateId() {
    Date date = new Date(123456789);
    FixtureDateId id1 = IIds.create(FixtureDateId.class, date);
    IId id2 = m_codec.fromUnqualified(FixtureDateId.class, "123456789");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedLocaleId() {
    Locale locale = Locale.GERMANY;
    FixtureLocaleId id1 = FixtureLocaleId.of(locale);
    IId id2 = m_codec.fromUnqualified(FixtureLocaleId.class, "de-DE");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedFixtureCustomComparableRawDataId() {
    FixtureCustomComparableRawDataId id1 = IIds.create(FixtureCustomComparableRawDataId.class, new CustomComparableRawDataType(100));

    // no type mapper registered
    assertThrows(PlatformException.class, () -> m_codec.fromUnqualified(FixtureCustomComparableRawDataId.class, "100"));

    m_codec.registerRawTypeMapper(CustomComparableRawDataType.class, CustomComparableRawDataType::of, CustomComparableRawDataType::toString);
    IId id2 = m_codec.fromUnqualified(FixtureCustomComparableRawDataId.class, "100");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedCompositeId() {
    FixtureCompositeId id1 = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    FixtureCompositeId id2 = m_codec.fromUnqualified(FixtureCompositeId.class, "foobar;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedWrapperCompositeId() {
    FixtureWrapperCompositeId id1 = IIds.create(FixtureWrapperCompositeId.class, TEST_STRING, TEST_UUID, TEST_STRING_2);
    IId id2 = m_codec.fromUnqualified(FixtureWrapperCompositeId.class, "foobar;5833aae1-c813-4d7c-a342-56a53772a3ea;bazäöl");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedNullIdClass() {
    assertThrows(PlatformException.class, () -> m_codec.fromUnqualified(null, null));
    assertThrows(PlatformException.class, () -> m_codec.fromUnqualified(null, "foo"));
  }

  @Test
  public void testFromUnqualifiedNullValue() {
    assertNull(m_codec.fromUnqualified(FixtureStringId.class, null));
  }

  @Test
  public void testFromUnqualifiedRootIdEmptyValue() {
    FixtureStringId id1 = FixtureStringId.of("");
    assertNull(id1);
    IId id2 = m_codec.fromUnqualified(FixtureStringId.class, "");
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedCompositeIdEmptyValue() {
    FixtureCompositeId id1 = FixtureCompositeId.of("", TEST_UUID);
    IId id2 = m_codec.fromUnqualified(FixtureCompositeId.class, ";" + TEST_UUID);
    assertEquals(id1, id2);
  }

  @Test
  public void testFromUnqualifiedCompositeIdWrongNumberOfComponents() {
    assertThrows(PlatformException.class, () -> m_codec.fromUnqualified(FixtureCompositeId.class, "foo"));
    assertThrows(PlatformException.class, () -> m_codec.fromUnqualified(FixtureCompositeId.class, "foo;" + TEST_UUID + ";foo"));
    assertThrows(PlatformException.class, () -> m_codec.fromUnqualified(FixtureCompositeId.class, ""));
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_InvalidType() {
    m_codec.fromQualified("scout.FixtureUuId:Other:" + TEST_UUID);
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_UnknownType() {
    m_codec.fromQualified("DoesNotExist:" + TEST_UUID);
  }

  @Test(expected = PlatformException.class)
  public void testFromQualified_UnsupportedWrappedType() {
    m_codec.fromQualified("DoesNotExist:" + TEST_UUID);
  }

  @Test
  public void testFromQualifiedLenient() {
    m_codec.fromQualified("scout.FixtureUuId:" + TEST_UUID);
  }

  @Test
  public void testFromQualifiedLenient_UnknownType() {
    IId id = m_codec.fromQualifiedLenient("DoesNotExist:" + TEST_UUID);
    assertNull(id);
  }

  @Test
  public void testFromQualifiedLenient_WrongFormat() {
    IId id = m_codec.fromQualifiedLenient("Does:Not:Exist:" + TEST_UUID);
    assertNull(id);
  }

  @Test
  public void testRegisterTypeMapper() {
    assertThrows(AssertionException.class, () -> m_codec.registerRawTypeMapper(null, x -> x, x -> "x"));
    assertThrows(AssertionException.class, () -> m_codec.registerRawTypeMapper(String.class, null, x -> "x"));
    assertThrows(AssertionException.class, () -> m_codec.registerRawTypeMapper(String.class, x -> x, null));
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
}
