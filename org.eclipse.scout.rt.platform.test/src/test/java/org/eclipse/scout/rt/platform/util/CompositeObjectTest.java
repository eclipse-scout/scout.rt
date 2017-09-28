/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CompositeObject}
 *
 * @since 3.10.0-M2
 */
public class CompositeObjectTest {

  private static final CompositeObject sameLongValue = new CompositeObject(1234L);
  private static final CompositeObject otherLongValue = new CompositeObject(9876L);
  private static final CompositeObject sameDoubleValue = new CompositeObject(9876d);
  private static final CompositeObject otherDoubleValue = new CompositeObject(1234d);
  private static final CompositeObject sameLongArray = new CompositeObject(1234L, 5678L, 8901L);
  private static final CompositeObject otherLongArray = new CompositeObject(9876L, 5432L, 2109L);
  private static final CompositeObject other2LongArray = new CompositeObject(9876L, 5432L, 1L);
  private static final CompositeObject other3LongArray = new CompositeObject(9876L, 5432L, null);
  private static final CompositeObject other4LongArray = new CompositeObject(1234L, 5678L, null);
  private static final CompositeObject sameDoubleArray = new CompositeObject(9876d, 5432d, 2109d);
  private static final CompositeObject otherDoubleArray = new CompositeObject(1234d, 5678d, 8901d);
  private static final CompositeObject other2DoubleArray = new CompositeObject(1234d, 5678d, 1d);
  private static final CompositeObject other3DoubleArray = new CompositeObject(1234d, 5678d, null);
  private static final CompositeObject other4DoubleArray = new CompositeObject(9876d, 5432d, null);

  private CompositeObject m_compObjLongValue;
  private CompositeObject m_compObjDoubleValue;

  private CompositeObject m_compObjLongArray;
  private CompositeObject m_compObjDoubleArray;

  @Before
  public void setUp() {
    m_compObjLongValue = new CompositeObject(1234L);
    m_compObjDoubleValue = new CompositeObject(9876d);

    m_compObjLongArray = new CompositeObject(1234L, 5678L, 8901L);
    m_compObjDoubleArray = new CompositeObject(9876d, 5432d, 2109d);
  }

  @Test
  public void testEqualsNotTypeCompositeObject() {
    // not of type CompositeObject
    assertFalse(m_compObjLongValue.equals(1234L));
    assertFalse(m_compObjDoubleValue.equals(9876d));
    assertFalse(m_compObjLongArray.equals(1234L));
    assertFalse(m_compObjDoubleArray.equals(1234L));
  }

  @Test
  public void testEqualsDifferentLength() {
    // not same CompositeObject.m_value length
    assertFalse(m_compObjLongValue.equals(m_compObjLongArray));
    assertFalse(m_compObjLongArray.equals(m_compObjLongValue));
    assertFalse(m_compObjDoubleValue.equals(m_compObjDoubleArray));
    assertFalse(m_compObjDoubleArray.equals(m_compObjDoubleValue));
  }

  @Test
  public void testEqualsSameLengthTypeValue() {
    // same CompositeObject.m_value length, same type, same value
    assertTrue(m_compObjLongValue.equals(sameLongValue));
    assertTrue(sameLongValue.equals(m_compObjLongValue));
    assertTrue(m_compObjDoubleValue.equals(sameDoubleValue));
    assertTrue(sameDoubleValue.equals(m_compObjDoubleValue));
    assertTrue(m_compObjLongArray.equals(sameLongArray));
    assertTrue(sameLongArray.equals(m_compObjLongArray));
    assertTrue(m_compObjDoubleArray.equals(sameDoubleArray));
    assertTrue(sameDoubleArray.equals(m_compObjDoubleArray));
  }

  @Test
  public void testEqualsSameLengthTypeDifferentValue() {
    // same CompositeObject.m_value length, same type, not same value
    assertFalse(m_compObjLongValue.equals(otherLongValue));
    assertFalse(otherLongValue.equals(m_compObjLongValue));
    assertFalse(m_compObjDoubleValue.equals(otherDoubleValue));
    assertFalse(otherDoubleValue.equals(m_compObjDoubleValue));

    assertFalse(m_compObjLongArray.equals(otherLongArray));
    assertFalse(m_compObjLongArray.equals(other2LongArray));
    assertFalse(m_compObjLongArray.equals(other3LongArray));
    assertFalse(otherLongArray.equals(m_compObjLongArray));
    assertFalse(other2LongArray.equals(m_compObjLongArray));
    assertFalse(other3LongArray.equals(m_compObjLongArray));

    assertFalse(m_compObjDoubleArray.equals(otherDoubleArray));
    assertFalse(m_compObjDoubleArray.equals(other2DoubleArray));
    assertFalse(m_compObjDoubleArray.equals(other3DoubleArray));
    assertFalse(otherDoubleArray.equals(m_compObjDoubleArray));
    assertFalse(other2DoubleArray.equals(m_compObjDoubleArray));
    assertFalse(other3DoubleArray.equals(m_compObjDoubleArray));
  }

  @Test
  public void testEqualsDifferentType() {
    // same CompositeObject.m_value length, not same type, not same value
    assertFalse(m_compObjLongValue.equals(m_compObjDoubleValue));
    assertFalse(m_compObjDoubleValue.equals(m_compObjLongValue));
    assertFalse(m_compObjLongArray.equals(m_compObjDoubleArray));
    assertFalse(m_compObjDoubleArray.equals(m_compObjLongArray));

    // same CompositeObject.m_value length, not same type, same value
    assertFalse(m_compObjLongValue.equals(otherDoubleValue));
    assertFalse(otherDoubleValue.equals(m_compObjLongValue));
    assertFalse(m_compObjDoubleArray.equals(otherLongValue));
    assertFalse(otherLongValue.equals(m_compObjDoubleArray));
  }

  @Test
  public void testEqualsNotComparable() {
    // not of type comparable -> uses toString()
    CompositeObject a = new CompositeObject(new A(123456L));
    CompositeObject b = new CompositeObject(new B(123456L));
    CompositeObject c = new CompositeObject(new C(123456L));
    CompositeObject c2 = new CompositeObject(new C(123456L));

    assertTrue(a.equals(b));
    assertTrue(b.equals(a));

    assertTrue(a.equals(c));
    assertFalse(c.equals(a));

    assertTrue(b.equals(c));
    assertFalse(c.equals(b));

    assertFalse(c.equals(c2));
    assertFalse(c2.equals(c));
  }

  @Test
  public void testCompareToDifferentLength() {
    // not same CompositeObject.m_value length
    assertEquals(-1, m_compObjLongValue.compareTo(m_compObjLongArray));
    assertEquals(1, m_compObjLongArray.compareTo(m_compObjLongValue));
    assertEquals(-1, m_compObjDoubleValue.compareTo(m_compObjDoubleArray));
    assertEquals(1, m_compObjDoubleArray.compareTo(m_compObjDoubleValue));
  }

  @Test
  public void testCompareToSameLengthTypeValue() {
    //same CompositeObject.m_value length, same type, same value
    assertEquals(0, m_compObjLongValue.compareTo(sameLongValue));
    assertEquals(0, sameLongValue.compareTo(m_compObjLongValue));
    assertEquals(0, m_compObjDoubleValue.compareTo(sameDoubleValue));
    assertEquals(0, sameDoubleValue.compareTo(m_compObjDoubleValue));
    assertEquals(0, m_compObjLongArray.compareTo(sameLongArray));
    assertEquals(0, sameLongArray.compareTo(m_compObjLongArray));
    assertEquals(0, m_compObjDoubleArray.compareTo(sameDoubleArray));
    assertEquals(0, sameDoubleArray.compareTo(m_compObjDoubleArray));
  }

  @Test
  public void testCompareToSameLengthTypeDifferentValue() {
    // same CompositeObject.m_value length, same type, not same value
    assertEquals(-1, m_compObjLongValue.compareTo(otherLongValue));
    assertEquals(1, otherLongValue.compareTo(m_compObjLongValue));
    assertEquals(1, m_compObjDoubleValue.compareTo(otherDoubleValue));
    assertEquals(-1, otherDoubleValue.compareTo(m_compObjDoubleValue));

    assertEquals(-1, m_compObjLongArray.compareTo(otherLongArray));
    assertEquals(-1, m_compObjLongArray.compareTo(other2LongArray));
    assertEquals(-1, m_compObjLongArray.compareTo(other3LongArray));
    assertEquals(1, m_compObjLongArray.compareTo(other4LongArray));
    assertEquals(1, otherLongArray.compareTo(m_compObjLongArray));
    assertEquals(1, other2LongArray.compareTo(m_compObjLongArray));
    assertEquals(1, other3LongArray.compareTo(m_compObjLongArray));
    assertEquals(-1, other4LongArray.compareTo(m_compObjLongArray));

    assertEquals(1, m_compObjDoubleArray.compareTo(otherDoubleArray));
    assertEquals(1, m_compObjDoubleArray.compareTo(other2DoubleArray));
    assertEquals(1, m_compObjDoubleArray.compareTo(other3DoubleArray));
    assertEquals(1, m_compObjDoubleArray.compareTo(other4DoubleArray));
    assertEquals(-1, otherDoubleArray.compareTo(m_compObjDoubleArray));
    assertEquals(-1, other2DoubleArray.compareTo(m_compObjDoubleArray));
    assertEquals(-1, other3DoubleArray.compareTo(m_compObjDoubleArray));
    assertEquals(-1, other4DoubleArray.compareTo(m_compObjDoubleArray));
  }

  @Test
  public void testCompareToDifferentType() {
    // same CompositeObject.m_value length, not same number type, not same value
    testCompareToDifferentTypeWithClassCastException(m_compObjLongValue, m_compObjDoubleValue);
    testCompareToDifferentTypeWithClassCastException(m_compObjDoubleValue, m_compObjLongValue);
    testCompareToDifferentTypeWithClassCastException(m_compObjLongArray, m_compObjDoubleArray);
    testCompareToDifferentTypeWithClassCastException(m_compObjDoubleArray, m_compObjLongArray);

    testCompareToDifferentTypeWithClassCastException(m_compObjLongArray, other2DoubleArray);
    testCompareToDifferentTypeWithClassCastException(m_compObjLongArray, other3DoubleArray);
    testCompareToDifferentTypeWithClassCastException(m_compObjLongArray, other4DoubleArray);
    testCompareToDifferentTypeWithClassCastException(other2DoubleArray, m_compObjLongArray);
    testCompareToDifferentTypeWithClassCastException(other3DoubleArray, m_compObjLongArray);
    testCompareToDifferentTypeWithClassCastException(other4DoubleArray, m_compObjLongArray);

    testCompareToDifferentTypeWithClassCastException(m_compObjDoubleArray, other2LongArray);
    testCompareToDifferentTypeWithClassCastException(m_compObjDoubleArray, other3LongArray);
    testCompareToDifferentTypeWithClassCastException(m_compObjDoubleArray, other4LongArray);
    testCompareToDifferentTypeWithClassCastException(other2LongArray, m_compObjDoubleArray);
    testCompareToDifferentTypeWithClassCastException(other3LongArray, m_compObjDoubleArray);
    testCompareToDifferentTypeWithClassCastException(other4LongArray, m_compObjDoubleArray);

    // same CompositeObject.m_value length, not same type, same value
    testCompareToDifferentTypeWithClassCastException(m_compObjLongValue, otherDoubleValue);
    testCompareToDifferentTypeWithClassCastException(otherDoubleValue, m_compObjLongValue);
    testCompareToDifferentTypeWithClassCastException(m_compObjLongArray, otherDoubleArray);
    testCompareToDifferentTypeWithClassCastException(otherDoubleArray, m_compObjLongArray);
    testCompareToDifferentTypeWithClassCastException(m_compObjDoubleArray, otherLongArray);
    testCompareToDifferentTypeWithClassCastException(otherLongArray, m_compObjDoubleArray);
  }

  private void testCompareToDifferentTypeWithClassCastException(CompositeObject a, CompositeObject b) {
    try {
      a.compareTo(b);
      fail("Expects ClassCastException when comparing " + a.toString() + " with " + b.toString());
    }
    catch (ClassCastException e) {
      // ok
    }
  }

  @Test
  public void testCompareToNotComparable() {
    // not of type comparable -> uses toString()
    CompositeObject a = new CompositeObject(new A(123456L));
    CompositeObject b = new CompositeObject(new B(123456L));
    CompositeObject c = new CompositeObject(new C(123456L));
    CompositeObject c2 = new CompositeObject(new C(123456L));

    assertTrue(a.compareTo(b) == 0);
    assertTrue(b.compareTo(a) == 0);

    assertTrue(a.compareTo(c) < 0);
    assertTrue(c.compareTo(a) > 0);

    assertTrue(b.compareTo(c) < 0);
    assertTrue(c.compareTo(b) > 0);

    assertTrue(c.compareTo(c2) == 0);
    assertTrue(c2.compareTo(c) == 0);
  }

  static class A {
    long m_value;

    public A(long value) {
      m_value = value;
    }

    @Override
    public int hashCode() {
      return Long.valueOf(m_value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof A)) {
        return false;
      }
      A other = (A) obj;
      if (m_value != other.m_value) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return String.valueOf(m_value);
    }
  }

  static class B extends A {
    public B(long value) {
      super(value);
    }
  }

  static class C extends A {
    public C(long value) {
      super(value);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj;
    }

    @Override
    public String toString() {
      return "Class " + this.getClass().toString() + ": " + m_value;
    }
  }
}
