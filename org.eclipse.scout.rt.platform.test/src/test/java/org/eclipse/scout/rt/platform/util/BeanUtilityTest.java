/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

/**
 * JUnit tests for {@link BeanUtility}
 *
 * @since 3.8.1
 */
public class BeanUtilityTest {

  @Rule
  public ErrorCollector collector = new ErrorCollector();

  private final boolean m_isJava21OrNewer = Runtime.version().feature() >= 21;

  @Test
  public void testGetConstructorNullAndDefault() throws Exception {
    assertNull(BeanUtility.findConstructor(null));
    //
    assertEquals(1, OnlyDefaultConstructor.class.getConstructors().length);
    @SuppressWarnings("JavaReflectionMemberAccess")
    Constructor<OnlyDefaultConstructor> expected = OnlyDefaultConstructor.class.getConstructor((Class<?>[]) null);
    assertEquals(expected, BeanUtility.findConstructor(OnlyDefaultConstructor.class));
    assertEquals(expected, BeanUtility.findConstructor(OnlyDefaultConstructor.class, (Class<?>[]) null));
    //
    assertNull(BeanUtility.findConstructor(OnlyPrivateDefaultConstructor.class));
  }

  @Test
  public void testGetConstructorInvisibleClass() {
    assertNull(BeanUtility.findConstructor(InvisibleClass.class));
  }

  @Test
  @SuppressWarnings("JavaReflectionMemberAccess")
  public void testGetConstructorNonStaticClass() throws Exception {
    assertNull(BeanUtility.findConstructor(NonStaticInnerClass.class));
    assertEquals(NonStaticInnerClass.class.getConstructor(BeanUtilityTest.class), BeanUtility.findConstructor(NonStaticInnerClass.class, BeanUtilityTest.class));
  }

  @Test
  public void testGetConstructorPolymorphism() throws Exception {
    assertNull(BeanUtility.findConstructor(ParamConstructor.class));
    assertNull(BeanUtility.findConstructor(ParamConstructor.class, String.class));
    //
    Constructor<ParamConstructor> expected = ParamConstructor.class.getConstructor(A.class);
    assertEquals(expected, BeanUtility.findConstructor(ParamConstructor.class, A.class));
    assertEquals(expected, BeanUtility.findConstructor(ParamConstructor.class, AExt.class));
  }

  @Test
  public void testGetConstructorPolymorphismOverloadedConstructors() throws Exception {
    assertEquals(MultiParamConstructor.class.getConstructor(), BeanUtility.findConstructor(MultiParamConstructor.class));
    assertEquals(MultiParamConstructor.class.getConstructor(A.class), BeanUtility.findConstructor(MultiParamConstructor.class, A.class));
    assertEquals(MultiParamConstructor.class.getConstructor(AExt.class), BeanUtility.findConstructor(MultiParamConstructor.class, AExt.class));
    assertEquals(MultiParamConstructor.class.getConstructor(A.class), BeanUtility.findConstructor(MultiParamConstructor.class, AExt2.class));
    assertEquals(MultiParamConstructor.class.getConstructor(AExt.class), BeanUtility.findConstructor(MultiParamConstructor.class, AExtExt.class));
  }

  @Test
  public void testGetConstructorAmbiguousSignatures() throws Exception {
    assertEquals(AmbiguousSignaturesConstructor.class.getConstructor(A.class, B.class), BeanUtility.findConstructor(AmbiguousSignaturesConstructor.class, A.class, B.class));
    assertEquals(AmbiguousSignaturesConstructor.class.getConstructor(AExt.class, B.class), BeanUtility.findConstructor(AmbiguousSignaturesConstructor.class, AExt.class, B.class));
    assertEquals(AmbiguousSignaturesConstructor.class.getConstructor(A.class, BExt.class), BeanUtility.findConstructor(AmbiguousSignaturesConstructor.class, A.class, BExt.class));
    try {
      Constructor<AmbiguousSignaturesConstructor> ctor = BeanUtility.findConstructor(AmbiguousSignaturesConstructor.class, AExt.class, BExt.class);
      fail("Expected ambiguous constructor but got '" + ctor.toString() + "'");
    }
    catch (RuntimeException e) {
      // ok
    }
  }

  @Test
  public void testGetConstructorPrimitiveAndComplexType() throws Exception {
    assertEquals(PrimitiveAndComplexTypeConstructor.class.getConstructor(long.class), BeanUtility.findConstructor(PrimitiveAndComplexTypeConstructor.class, long.class));
    assertEquals(PrimitiveAndComplexTypeConstructor.class.getConstructor(Long.class), BeanUtility.findConstructor(PrimitiveAndComplexTypeConstructor.class, Long.class));
    assertEquals(PrimitiveAndComplexTypeConstructor.class.getConstructor(Long.class), BeanUtility.findConstructor(PrimitiveAndComplexTypeConstructor.class, new Class<?>[]{null}));
  }

  @Test
  public void testGetConstructorComplexType() throws Exception {
    assertEquals(ComplexTypeConstructor.class.getConstructor(Long.class), BeanUtility.findConstructor(ComplexTypeConstructor.class, Long.class));
    // auto-boxing
    assertEquals(ComplexTypeConstructor.class.getConstructor(Long.class), BeanUtility.findConstructor(ComplexTypeConstructor.class, long.class));
    // incompatible types
    assertNull(BeanUtility.findConstructor(ComplexTypeConstructor.class, String.class));
  }

  @Test
  public void testGetConstructorPrimitiveType() throws Exception {
    assertEquals(PrimitiveTypeConstructor.class.getConstructor(long.class), BeanUtility.findConstructor(PrimitiveTypeConstructor.class, long.class));
    // auto-unboxing
    assertEquals(PrimitiveTypeConstructor.class.getConstructor(long.class), BeanUtility.findConstructor(PrimitiveTypeConstructor.class, Long.class));
  }

  @Test
  public void testGetConstructorArray() throws Exception {
    assertEquals(ArrayConstructor.class.getConstructor(String[].class), BeanUtility.findConstructor(ArrayConstructor.class, String[].class));
  }

  @Test
  public void testCreateInstanceNullAndEmpty() {
    assertNull(BeanUtility.createInstance(null));
    assertNull(BeanUtility.createInstance(null, null, null));
    //
    assertTrue(BeanUtility.createInstance(OnlyDefaultConstructor.class) instanceof OnlyDefaultConstructor);
    assertTrue(BeanUtility.createInstance(OnlyDefaultConstructor.class, (Object[]) null) instanceof OnlyDefaultConstructor);
    assertTrue(BeanUtility.createInstance(OnlyDefaultConstructor.class, null, null) instanceof OnlyDefaultConstructor);
  }

  @Test
  public void testCreateInstanceInvisibleClass() {
    assertNull(BeanUtility.createInstance(InvisibleClass.class));
  }

  @Test
  public void testCreateInstanceNonStaticClass() {
    assertNull(BeanUtility.createInstance(NonStaticInnerClass.class));
  }

  @Test
  public void testCreateInstanceNonStaticClassWithThisArgument() {
    assertNotNull(BeanUtility.createInstance(NonStaticInnerClass.class, this));
  }

  static final long TEST_LONG_42 = 42L;
  static final long TEST_LONG_NEG_42 = -42L;

  static final String EXPECTED_CONSTR_PRIM_LONG = "Expected constructor with primitive type long";
  static final String EXPECTED_CONSTR_LONG = "Expected constructor with complex type java.lang.Long";

  @Test
  public void testCreateInstancePrimitiveType() {
    {
      PrimitiveTypeConstructor obj = BeanUtility.createInstance(PrimitiveTypeConstructor.class, new Class<?>[]{long.class}, new Object[]{TEST_LONG_42});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_PRIM_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      PrimitiveTypeConstructor obj = BeanUtility.createInstance(PrimitiveTypeConstructor.class, new Class<?>[]{Long.class}, new Object[]{TEST_LONG_42});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_PRIM_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      PrimitiveTypeConstructor obj = BeanUtility.createInstance(PrimitiveTypeConstructor.class, TEST_LONG_42);
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      PrimitiveTypeConstructor obj = BeanUtility.createInstance(PrimitiveTypeConstructor.class, new Object[]{null});
      assertNull(obj);
    }
  }

  @Test
  public void testCreateInstanceComplexType() {
    {
      ComplexTypeConstructor obj = BeanUtility.createInstance(ComplexTypeConstructor.class, new Class<?>[]{Long.class}, new Object[]{TEST_LONG_42});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_PRIM_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      ComplexTypeConstructor obj = BeanUtility.createInstance(ComplexTypeConstructor.class, new Class<?>[]{long.class}, new Object[]{TEST_LONG_42});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_PRIM_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      ComplexTypeConstructor obj = BeanUtility.createInstance(ComplexTypeConstructor.class, TEST_LONG_42);
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      ComplexTypeConstructor obj = BeanUtility.createInstance(ComplexTypeConstructor.class, new Object[]{null});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_LONG, Long.MAX_VALUE, obj.getLong());
    }
  }

  @Test
  public void testCreateInstancePrimitiveAndComplexType() {
    {
      PrimitiveAndComplexTypeConstructor obj = BeanUtility.createInstance(PrimitiveAndComplexTypeConstructor.class, new Class<?>[]{long.class}, new Object[]{TEST_LONG_42});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_PRIM_LONG, TEST_LONG_42, obj.getLong());
    }
    {
      PrimitiveAndComplexTypeConstructor obj = BeanUtility.createInstance(PrimitiveAndComplexTypeConstructor.class, new Class<?>[]{Long.class}, new Object[]{TEST_LONG_42});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_PRIM_LONG, TEST_LONG_NEG_42, obj.getLong());
    }
    {
      PrimitiveAndComplexTypeConstructor obj = BeanUtility.createInstance(PrimitiveAndComplexTypeConstructor.class, TEST_LONG_42);
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_LONG, TEST_LONG_NEG_42, obj.getLong());
    }
    {
      PrimitiveAndComplexTypeConstructor obj = BeanUtility.createInstance(PrimitiveAndComplexTypeConstructor.class, new Object[]{null});
      assertNotNull(obj);
      assertEquals(EXPECTED_CONSTR_LONG, Long.MAX_VALUE, obj.getLong());
    }
  }

  static final String HELLO = "hello";
  static final String WORLD = "world";

  @Test
  public void testCreateInstanceArray() {
    {
      ArrayConstructor obj = BeanUtility.createInstance(ArrayConstructor.class, new Class<?>[]{String[].class}, new Object[]{new String[]{HELLO, WORLD}});
      assertNotNull(obj);
      assertArrayEquals(new String[]{HELLO, WORLD}, obj.getStringArray());
    }
    {
      ArrayConstructor obj = BeanUtility.createInstance(ArrayConstructor.class, (Object) new String[]{HELLO, WORLD});
      assertNotNull(obj);
      assertArrayEquals(new String[]{HELLO, WORLD}, obj.getStringArray());
    }
  }

  @Test
  public void testCreateInstanceVararg() {
    {
      VarargConstructor obj = BeanUtility.createInstance(VarargConstructor.class, new Class<?>[]{String[].class}, new Object[]{new String[]{HELLO, WORLD}});
      assertNotNull(obj);
      assertArrayEquals(new String[]{HELLO, WORLD}, obj.getStringArray());
    }
    {
      VarargConstructor obj = BeanUtility.createInstance(VarargConstructor.class, (Object) new String[]{HELLO, WORLD});
      assertNotNull(obj);
      assertArrayEquals(new String[]{HELLO, WORLD}, obj.getStringArray());
    }
  }

  @Test
  public void testGetInterfacesHierarchyWithoutFilterClass() {
    List<Class<?>> hierarchy = BeanUtility.getInterfacesHierarchy(InterfaceWithHierarchy.class, null);
    Iterator<Class<?>> it = hierarchy.iterator();
    assertEquals(Iterable.class, it.next());
    assertEquals(Collection.class, it.next());

    if (m_isJava21OrNewer) {
      assertEquals("java.util.SequencedCollection", it.next().getName());
    }
    assertEquals(Set.class, it.next());
    if (m_isJava21OrNewer) {
      assertEquals("java.util.SequencedSet", it.next().getName());
    }
    assertEquals(SortedSet.class, it.next());
    assertEquals(InterfaceWithHierarchy.class, it.next());
  }

  @Test
  public void testGetInterfacesHierarchyWithFilterClass() {
    List<Class<? extends Set>> hierarchy = BeanUtility.getInterfacesHierarchy(InterfaceWithHierarchy.class, Set.class);
    Iterator<Class<? extends Set>> it = hierarchy.iterator();
    assertEquals(Set.class, it.next());
    if (m_isJava21OrNewer) {
      assertEquals("java.util.SequencedSet", it.next().getName());
    }
    assertEquals(SortedSet.class, it.next());
    assertEquals(InterfaceWithHierarchy.class, it.next());
  }

  @Test
  public void testTypeDistancePrimitiveTypes() {
    List<ImmutablePair<Class<?>, Class<?>>> primitiveToObjectTypes = List.of(
        ImmutablePair.of(boolean.class, Boolean.class),
        ImmutablePair.of(byte.class, Byte.class),
        ImmutablePair.of(char.class, Character.class),
        ImmutablePair.of(short.class, Short.class),
        ImmutablePair.of(int.class, Integer.class),
        ImmutablePair.of(long.class, Long.class),
        ImmutablePair.of(float.class, Float.class),
        ImmutablePair.of(double.class, Double.class));

    // declared and actual are primitive types
    primitiveToObjectTypes.stream()
        .map(Pair::getLeft)
        .forEach(t -> collector.checkThat(
            String.format("distance to itself [primitive]: %s", t.getSimpleName()),
            BeanUtility.computeTypeDistance(t, t), CoreMatchers.is(0)));

    // declared and actual are object types
    primitiveToObjectTypes.stream()
        .map(Pair::getRight)
        .forEach(t -> collector.checkThat(
            String.format("distance to itself [object type]: %s", t.getSimpleName()),
            BeanUtility.computeTypeDistance(t, t), CoreMatchers.is(0)));

    // declared is primitive, actual an object type -> un-boxing
    primitiveToObjectTypes
        .forEach(p -> collector.checkThat(
            String.format("distance between declared=%s and actual=%s", p.getLeft().getSimpleName(), p.getRight().getSimpleName()), BeanUtility.computeTypeDistance(p.getLeft(), p.getRight()), CoreMatchers.is(1)));// declared is primitive, actual an object type -> un-boxing

    // declared is primitive, actual null
    primitiveToObjectTypes.stream()
        .map(Pair::getLeft)
        .forEach(t -> collector.checkThat(
            String.format("distance between declared=%s and actual=<null>", t.getSimpleName()), BeanUtility.computeTypeDistance(t, null), CoreMatchers.is(-1)));

    // declared is object type, actual a primitive -> boxing
    primitiveToObjectTypes
        .forEach(p -> collector.checkThat(
            String.format("distance between declared=%s and actual=%s", p.getRight().getSimpleName(), p.getLeft().getName()), BeanUtility.computeTypeDistance(p.getRight(), p.getLeft()), CoreMatchers.is(1)));

    // declared is object type, actual null
    primitiveToObjectTypes.stream()
        .map(Pair::getRight)
        .forEach(t -> collector.checkThat(
            String.format("distance between declared=%s and actual=<null>", t.getSimpleName()), BeanUtility.computeTypeDistance(t, null), CoreMatchers.is(0)));
  }

  public static class OnlyDefaultConstructor {
  }

  public static final class OnlyPrivateDefaultConstructor {
    private OnlyPrivateDefaultConstructor() {
    }
  }

  public static class ParamConstructor {
    public ParamConstructor(A paramA) {
    }
  }

  public static class MultiParamConstructor {
    public MultiParamConstructor() {
    }

    public MultiParamConstructor(A paramA) {
    }

    public MultiParamConstructor(AExt paramAExt) {
    }
  }

  public static class AmbiguousSignaturesConstructor {
    public AmbiguousSignaturesConstructor(A a, B b) {
    }

    public AmbiguousSignaturesConstructor(AExt aExt, B b) {
    }

    public AmbiguousSignaturesConstructor(A a, BExt bExt) {
    }
  }

  public static class PrimitiveTypeConstructor {
    private final long m_long;

    public PrimitiveTypeConstructor(long l) {
      m_long = l;
    }

    public long getLong() {
      return m_long;
    }
  }

  public static class ComplexTypeConstructor {
    private final long m_long;

    public ComplexTypeConstructor(Long l) {
      m_long = (l == null ? Long.MAX_VALUE : l.longValue());
    }

    public long getLong() {
      return m_long;
    }
  }

  public static class PrimitiveAndComplexTypeConstructor {
    private final long m_long;

    public PrimitiveAndComplexTypeConstructor(long l) {
      m_long = l;
    }

    public PrimitiveAndComplexTypeConstructor(Long l) {
      m_long = (l == null ? Long.MAX_VALUE : -l.longValue());
    }

    public long getLong() {
      return m_long;
    }
  }

  public static class ArrayConstructor {
    private final String[] m_stringArray;

    public ArrayConstructor(String[] stringArray) {
      m_stringArray = stringArray;
    }

    public String[] getStringArray() {
      return m_stringArray;
    }
  }

  public static class VarargConstructor {
    private final String[] m_stringArray;

    public VarargConstructor(String... stringArray) {
      m_stringArray = stringArray;
    }

    public String[] getStringArray() {
      return m_stringArray;
    }
  }

  public static class A {
  }

  public static class AExt extends A {
  }

  public static class AExt2 extends A {
  }

  public static class AExtExt extends AExt {
  }

  public static class B {
  }

  public static class BExt extends B {
  }

  private static class InvisibleClass {
  }

  public class NonStaticInnerClass {
  }

  public interface InterfaceWithHierarchy extends SortedSet<Object> {
  }
}
