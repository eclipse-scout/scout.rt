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

import java.util.List;
import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureWrapperCompositeId;
import org.eclipse.scout.rt.dataobject.id.AbstractIdCodecTest.FixtureCompositeWithNullValuesId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Test;

/**
 * Test cases for {@link IdFactory}.
 */
public class IdFactoryTest {

  @Test
  public void testCreateStringId() {
    String str = "aaabbbccc";
    FixtureStringId id = BEANS.get(IdFactory.class).createInternal(FixtureStringId.class, str);
    assertEquals(str, id.unwrap());
  }

  @Test
  public void testCreateStringId_NullValue() {
    String str = null;
    FixtureStringId id = BEANS.get(IdFactory.class).createInternal(FixtureStringId.class, str);
    assertNull(id);
  }

  @Test(expected = PlatformException.class)
  public void testCreateStringId_WrongArgumentType() {
    BEANS.get(IdFactory.class).createInternal(FixtureStringId.class, UUID.fromString("144a0317-8cb5-40b2-981f-1fae0781715f"));
  }

  @Test
  public void testCreateFixtureStringId_CustomOf() {
    String str = "aaabbbccc";
    FixtureStringId_RawTypesOf id = BEANS.get(IdFactory.class).createInternal(FixtureStringId_RawTypesOf.class, str);
    assertEquals(str, id.unwrap());
  }

  @Test(expected = PlatformException.class)
  public void testCreateFixtureStringId_CustomRawTypesOf() {
    BEANS.get(IdFactory.class).createInternal(FixtureStringId_WrongRawTypesOfMethodName.class, "foo");
  }

  @Test(expected = PlatformException.class)
  public void testCreateFixtureStringId_WrongCardinality() {
    BEANS.get(IdFactory.class).createInternal(FixtureStringId.class, "arg1", "arg2");
  }

  @Test(expected = PlatformException.class)
  public void testCreateFixtureStringId_NonStaticOf() {
    BEANS.get(IdFactory.class).createInternal(FixtureStringId_NonStaticRawTypesOf.class, "arg1");
  }

  @Test
  public void testCreateFixtureUuId() {
    UUID uuid = UUID.fromString("144a0317-8cb5-40b2-981f-1fae0781715f");
    FixtureUuId id = BEANS.get(IdFactory.class).createInternal(FixtureUuId.class, uuid);
    assertEquals(uuid, id.unwrap());
  }

  @Test
  public void testCreateFixtureLongId() {
    Long value = Long.valueOf(42);
    FixtureLongId id = BEANS.get(IdFactory.class).createInternal(FixtureLongId.class, value);
    assertEquals(value, id.unwrap());
  }

  @Test(expected = PlatformException.class)
  public void testCreateInvalidType_MissingOf() {
    BEANS.get(IdFactory.class).createInternal(IllegalUuId1_MissingOf.class, UUID.fromString("144a0317-8cb5-40b2-981f-1fae0781715f"));
  }

  @Test(expected = PlatformException.class)
  public void testCreateInvalidType_WrongOf() {
    BEANS.get(IdFactory.class).createInternal(IllegalUuId2_WrongOf.class, UUID.fromString("144a0317-8cb5-40b2-981f-1fae0781715f"));
  }

  @Test
  public void testCompositeIdByComponents() {
    FixtureCompositeId id = BEANS.get(IdFactory.class).createInternal(FixtureCompositeId.class, "abc", UUID.fromString("f0583afe-01d2-45a8-9c4e-c8342d721d9e"));
    List<? extends IId> idComponents = id.unwrap();
    assertEquals(2, idComponents.size());
    assertEquals("abc", idComponents.get(0).unwrap());
    assertEquals(UUID.fromString("f0583afe-01d2-45a8-9c4e-c8342d721d9e"), idComponents.get(1).unwrap());
  }

  @Test
  public void testCompositeIdByComponent() {
    FixtureCompositeId_CardinalityOne id = BEANS.get(IdFactory.class).createInternal(FixtureCompositeId_CardinalityOne.class, "abc");
    List<? extends IId> idComponents = id.unwrap();
    assertEquals(1, idComponents.size());
    assertEquals("abc", idComponents.get(0).unwrap());
  }

  @Test
  public void testCompositeIdByComponents_NullValue() {
    // FixtureCompositeId does not support partial null values
    FixtureCompositeId id = BEANS.get(IdFactory.class).createInternal(FixtureCompositeId.class, "abc", null);
    assertNull(id);

    // FixtureCompositeWithNullValuesId supports partial null values
    FixtureCompositeWithNullValuesId id2 = BEANS.get(IdFactory.class).createInternal(FixtureCompositeWithNullValuesId.class, "abc", null);
    List<? extends IId> id2Components = id2.unwrap();
    assertEquals("abc", id2Components.get(0).unwrap());
    assertNull(id2Components.get(1));
  }

  @Test
  public void testCompositeIdByComponents_AllNullValues() {
    FixtureCompositeId id = BEANS.get(IdFactory.class).createInternal(FixtureCompositeId.class, null, null);
    assertNull(id);
    FixtureCompositeWithNullValuesId id2 = BEANS.get(IdFactory.class).createInternal(FixtureCompositeWithNullValuesId.class, null, null);
    assertNull(id2);
  }

  @Test(expected = PlatformException.class)
  public void testCompositeId_nonNativeTypes() {
    BEANS.get(IdFactory.class).createInternal(FixtureCompositeId.class, FixtureStringId.of("abc"), FixtureStringId.of("efg"));
  }

  @Test(expected = PlatformException.class)
  public void testCompositeId_toSmallCardinality() {
    BEANS.get(IdFactory.class).createInternal(FixtureCompositeId.class, "abc");
  }

  @Test(expected = PlatformException.class)
  public void testCompositeId_toLargeCardinality() {
    BEANS.get(IdFactory.class).createInternal(FixtureCompositeId.class, "abc", "def", "hij");
  }

  @Test(expected = PlatformException.class)
  public void testCompositeId_noRawTypesAnnotation() {
    BEANS.get(IdFactory.class).createInternal(FixtureCompositeId_NoRawTypes.class, "abc", "def");
  }

  @Test
  public void testCompositeIdWrappingComposite() {
    String c1 = "abc";
    UUID c2 = UUID.fromString("f0583afe-01d2-45a8-9c4e-c8342d721d9e");
    String c3 = "efg";
    FixtureCompositeId expectedPart1 = FixtureCompositeId.of(c1, c2);
    FixtureStringId expectedPart2 = FixtureStringId.of(c3);

    FixtureWrapperCompositeId id = BEANS.get(IdFactory.class).createInternal(FixtureWrapperCompositeId.class, c1, c2, c3);
    List<? extends IId> idComponents = id.unwrap();
    assertEquals(expectedPart1, idComponents.get(0));
    assertEquals(expectedPart2, idComponents.get(1));
  }

  /**
   * IRootId implementation with optional @RawTypes annotation.
   */
  @IgnoreBean
  public static final class FixtureStringId_RawTypesOf extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    private FixtureStringId_RawTypesOf(String id) {
      super(id);
    }

    @RawTypes
    public static FixtureStringId_RawTypesOf of(String id) {
      if (StringUtility.isNullOrEmpty(id)) {
        return null;
      }
      return new FixtureStringId_RawTypesOf(id);
    }
  }

  /**
   * Wrong method name for @RawType annotated method.
   */
  @IgnoreBean
  public static final class FixtureStringId_WrongRawTypesOfMethodName extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    private FixtureStringId_WrongRawTypesOfMethodName(String id) {
      super(id);
    }

    @RawTypes
    public static FixtureStringId_WrongRawTypesOfMethodName ofCustom(String id) {
      if (StringUtility.isNullOrEmpty(id)) {
        return null;
      }
      return new FixtureStringId_WrongRawTypesOfMethodName(id);
    }
  }

  /**
   * IId with non-static of method.
   */
  @IgnoreBean
  public static final class FixtureStringId_NonStaticRawTypesOf extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    private FixtureStringId_NonStaticRawTypesOf(String id) {
      super(id);
    }

    @RawTypes
    public FixtureStringId_NonStaticRawTypesOf ofCustom(String id) {
      if (StringUtility.isNullOrEmpty(id)) {
        return null;
      }
      return new FixtureStringId_NonStaticRawTypesOf(id);
    }
  }

  /**
   * IId with missing of().
   */
  @IgnoreBean
  public static final class IllegalUuId1_MissingOf extends AbstractUuId {
    private static final long serialVersionUID = 1L;

    private IllegalUuId1_MissingOf(UUID id) {
      super(id);
    }
  }

  /**
   * IId with wrong of() signature.
   */
  @IgnoreBean
  public static final class IllegalUuId2_WrongOf extends AbstractUuId {
    private static final long serialVersionUID = 1L;

    public static IllegalUuId2_WrongOf of(UUID id, String anotherParam) {
      return new IllegalUuId2_WrongOf(id);
    }

    private IllegalUuId2_WrongOf(UUID id) {
      super(id);
    }
  }

  /**
   * IId with missing RawTypes-annotated method.
   */
  @IgnoreBean
  public static final class FixtureCompositeId_NoRawTypes extends AbstractCompositeId {
    private static final long serialVersionUID = 1L;

    public static FixtureCompositeId_NoRawTypes of(FixtureStringId c1, FixtureUuId c2) {
      return new FixtureCompositeId_NoRawTypes(c1, c2);
    }

    public static FixtureCompositeId_NoRawTypes of(String c1, UUID c2) {
      return of(FixtureStringId.of(c1), FixtureUuId.of(c2));
    }

    private FixtureCompositeId_NoRawTypes(FixtureStringId c1, FixtureUuId c2) {
      super(c1, c2);
    }
  }

  /**
   * {@link ICompositeId} implementation using cardinality one.
   */
  @IgnoreBean
  public static final class FixtureCompositeId_CardinalityOne extends AbstractCompositeId {
    private static final long serialVersionUID = 1L;

    public static FixtureCompositeId_CardinalityOne of(FixtureStringId c1) {
      return new FixtureCompositeId_CardinalityOne(c1);
    }

    @RawTypes
    public static FixtureCompositeId_CardinalityOne of(String c1) {
      return of(FixtureStringId.of(c1));
    }

    private FixtureCompositeId_CardinalityOne(FixtureStringId c1) {
      super(c1);
    }
  }
}
