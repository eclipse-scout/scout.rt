/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 * Tests for {@link ScoutAssert}.
 *
 * @since 3.10.0-M3
 */
public class ScoutAssertTest {

  /**
   * Test for {@link ScoutAssert#assertComparableEquals(Comparable, Comparable)} and
   * {@link ScoutAssert#assertComparableEquals(String, Comparable, Comparable)}.
   *
   * @See Bug 420183
   */
  @Test
  public void testAssertComparableEquals() {
    BigDecimal b1 = BigDecimal.ONE;
    BigDecimal b2 = new BigDecimal("1.0000");
    BigDecimal b3 = BigDecimal.ONE;
    BigDecimal b4 = BigDecimal.ZERO;

    //Compare b1 and b2
    assertEquals(0, b1.compareTo(b2));
    assertNotEquals(b1, b2);
    ScoutAssert.assertComparableEquals(b1, b2);
    ScoutAssert.assertComparableEquals(b2, b1);
    ScoutAssert.assertComparableEquals("b1 <--ComparableEquals--> b2", b1, b2);
    ScoutAssert.assertComparableEquals("b2 <--ComparableEquals--> b1", b2, b1);

    //Compare same instance (b1 and b3):
    assertSame(b1, b3);
    ScoutAssert.assertComparableEquals(b1, b3);
    ScoutAssert.assertComparableEquals(b3, b1);
    ScoutAssert.assertComparableEquals("b1 <--ComparableEquals--> b3", b1, b3);
    ScoutAssert.assertComparableEquals("b3 <--ComparableEquals--> b1", b3, b1);

    //Compare null:
    ScoutAssert.assertComparableEquals(null, null);

    //Compare not_null and null => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals(b1, null);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().contains("expected:<1> but was:<null>"));
    }

    //Compare null and not_null => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals(null, b1);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().contains("expected:<null> but was:<1>"));
    }

    //Compare 0 and 1 => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals(b1, b4);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().startsWith("expected"));
    }

    //Compare 1 and 0 => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals("MY_TEST", b4, b1);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().startsWith("MY_TEST "));
      assertTrue(e.getMessage().contains("expected:<0> but was:<1>"));
    }
  }

  @Test
  public void testAssertThrows() {
    // runtime exception
    final VetoException runtimeException = new VetoException("msg");
    assertSame(runtimeException, ScoutAssert.assertThrows(VetoException.class, () -> raise(runtimeException)));
    assertSame(runtimeException, ScoutAssert.assertThrows(RuntimeException.class, () -> raise(runtimeException)));
    assertSame(runtimeException, ScoutAssert.assertThrows(Exception.class, () -> raise(runtimeException)));
    assertSame(runtimeException, ScoutAssert.assertThrows(Throwable.class, () -> raise(runtimeException)));
    try {
      ScoutAssert.assertThrows(Error.class, () -> raise(runtimeException));
      fail("expecting assertion to fail");
    }
    catch (AssertionError expected) {
    }

    // runtime exception
    final InterruptedException exception = new InterruptedException("msg");
    assertSame(exception, ScoutAssert.assertThrows(InterruptedException.class, () -> raise(exception)));
    assertSame(exception, ScoutAssert.assertThrows(Exception.class, () -> raise(exception)));
    assertSame(exception, ScoutAssert.assertThrows(Throwable.class, () -> raise(exception)));
    try {
      ScoutAssert.assertThrows(Error.class, () -> raise(exception));
      fail("expecting assertion to fail");
    }
    catch (AssertionError expected) {
    }

    // error
    final PlatformError error = new PlatformError("msg");
    assertSame(error, ScoutAssert.assertThrows(PlatformError.class, () -> raise(error)));
    assertSame(error, ScoutAssert.assertThrows(Error.class, () -> raise(error)));
    assertSame(error, ScoutAssert.assertThrows(Throwable.class, () -> raise(error)));
    try {
      ScoutAssert.assertThrows(Exception.class, () -> raise(error));
      fail("expecting assertion to fail");
    }
    catch (AssertionError expected) {
    }
  }

  private void raise(Throwable t) throws Throwable {
    throw t;
  }

  @Test
  public void testAssertEqualsWithComparisonFailureWithStrings() {
    try {
      ScoutAssert.assertEqualsWithComparisonFailure("foo", "bar");
      fail("expecting assertion to fail");
    }
    catch (ComparisonFailure e) {
      assertEquals("foo", e.getExpected());
      assertEquals("bar", e.getActual());
    }
  }

  @Test
  public void testAssertEqualsWithComparisonFailureMessageWithStrings() {
    try {
      ScoutAssert.assertEqualsWithComparisonFailure("message", "foo", "bar");
      fail("expecting assertion to fail");
    }
    catch (ComparisonFailure e) {
      assertEquals("foo", e.getExpected());
      assertEquals("bar", e.getActual());
      assertTrue(e.getMessage().startsWith("message"));
    }
  }

  @Test
  public void testAssertEqualsWithComparisonFailureWithObjects() {
    Object o1 = new Object() {
      @Override
      public String toString() {
        return "object1";
      }
    };
    Object o2 = new Object() {
      @Override
      public String toString() {
        return "object2";
      }
    };
    try {
      ScoutAssert.assertEqualsWithComparisonFailure(o1, o2);
      fail("expecting assertion to fail");
    }
    catch (ComparisonFailure e) {
      assertEquals("object1", e.getExpected());
      assertEquals("object2", e.getActual());
    }
  }
}
