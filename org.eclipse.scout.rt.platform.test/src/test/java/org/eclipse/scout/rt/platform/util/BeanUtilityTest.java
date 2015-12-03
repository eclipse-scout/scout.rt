/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;

import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link BeanUtility}
 *
 * @since 3.8.1
 */
public class BeanUtilityTest {

  @Test
  public void testGetConstructorNullAndDefault() throws Exception {
    assertNull(BeanUtility.findConstructor(null));
    //
    assertEquals(1, OnlyDefalutConstructor.class.getConstructors().length);
    Constructor<OnlyDefalutConstructor> expected = OnlyDefalutConstructor.class.getConstructor((Class<?>[]) null);
    assertEquals(expected, BeanUtility.findConstructor(OnlyDefalutConstructor.class));
    assertEquals(expected, BeanUtility.findConstructor(OnlyDefalutConstructor.class, (Class<?>[]) null));
    //
    assertNull(BeanUtility.findConstructor(OnlyPrivateDefalutConstructor.class));
  }

  @Test
  public void testGetConstructorInvisibleClass() throws Exception {
    assertNull(BeanUtility.findConstructor(InvisibleClass.class));
  }

  @Test
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
    assertEquals(MultyParamConstructor.class.getConstructor(), BeanUtility.findConstructor(MultyParamConstructor.class));
    assertEquals(MultyParamConstructor.class.getConstructor(A.class), BeanUtility.findConstructor(MultyParamConstructor.class, A.class));
    assertEquals(MultyParamConstructor.class.getConstructor(AExt.class), BeanUtility.findConstructor(MultyParamConstructor.class, AExt.class));
    assertEquals(MultyParamConstructor.class.getConstructor(A.class), BeanUtility.findConstructor(MultyParamConstructor.class, AExt2.class));
    assertEquals(MultyParamConstructor.class.getConstructor(AExt.class), BeanUtility.findConstructor(MultyParamConstructor.class, AExtExt.class));
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
  public void testCreateInstanceNullAndEmpty() throws Exception {
    assertNull(BeanUtility.createInstance(null));
    assertNull(BeanUtility.createInstance(null, null, null));
    //
    assertTrue(BeanUtility.createInstance(OnlyDefalutConstructor.class) instanceof OnlyDefalutConstructor);
    assertTrue(BeanUtility.createInstance(OnlyDefalutConstructor.class, (Object[]) null) instanceof OnlyDefalutConstructor);
    assertTrue(BeanUtility.createInstance(OnlyDefalutConstructor.class, null, null) instanceof OnlyDefalutConstructor);
  }

  @Test
  public void testCreateInstanceInvisibleClass() throws Exception {
    assertNull(BeanUtility.createInstance(InvisibleClass.class));
  }

  @Test
  public void testCreateInstanceNonStaticClass() throws Exception {
    assertNull(BeanUtility.createInstance(NonStaticInnerClass.class));
  }

  @Test
  public void testCreateInstanceNonStaticClassWithThisArgument() throws Exception {
    assertNotNull(BeanUtility.createInstance(NonStaticInnerClass.class, this));
  }

  static final long TEST_LONG_42 = 42L;
  static final long TEST_LONG_NEG_42 = -42L;

  static final String EXPECTED_CONSTR_PRIM_LONG = "Expected constructor with primitive type long";
  static final String EXPECTED_CONSTR_LONG = "Expected constructor with complex type java.lang.Long";

  @Test
  public void testCreateInstancePrimitiveType() throws Exception {
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
  public void testCreateInstanceComplexType() throws Exception {
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
  public void testCreateInstancePrimitiveAndComplexType() throws Exception {
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
  public void testCreateInstanceArray() throws Exception {
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
  public void testCreateInstanceVararg() throws Exception {
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

  public static class OnlyDefalutConstructor {
  }

  public static class OnlyPrivateDefalutConstructor {
    private OnlyPrivateDefalutConstructor() {
    }
  }

  public static class ParamConstructor {
    public ParamConstructor(A paramA) {
    }
  }

  public static class MultyParamConstructor {
    public MultyParamConstructor() {
    }

    public MultyParamConstructor(A paramA) {
    }

    public MultyParamConstructor(AExt paramAExt) {
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
}
