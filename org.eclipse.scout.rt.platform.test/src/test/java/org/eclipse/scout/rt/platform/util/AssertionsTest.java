/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AssertionsTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testInstance() {
    String s = "test";
    String res = Assertions.assertInstance(s, String.class);
    assertEquals(s, res);
  }

  @Test(expected = AssertionException.class)
  public void testInstance_AssertionError() {
    Assertions.assertInstance(new Object(), String.class);
  }

  @Test(expected = AssertionException.class)
  public void testInstanceNullValue() {
    Assertions.assertInstance(null, String.class);
  }

  @Test
  public void testInstanceCustomMessage() {
    expectedEx.expect(AssertionException.class);
    expectedEx.expectMessage("custom arg1");
    Assertions.assertInstance(new Object(), String.class, "custom {}", "arg1");
  }

  @Test
  public void testInstanceCustomMessageNullValue() {
    expectedEx.expect(AssertionException.class);
    expectedEx.expectMessage("custom arg1");
    Assertions.assertInstance(null, String.class, "custom {}", "arg1");
  }

  @Test
  public void testType() {
    String s = "test";
    String res = Assertions.assertType(s, String.class);
    assertEquals(s, res);
  }

  @Test
  public void testTypeNull() {
    String s = null;
    String res = Assertions.assertType(s, String.class);
    assertEquals(s, res);

    assertNull(Assertions.assertType(null, BigDecimal.class));
    assertNull(Assertions.assertType(null, Date.class));
  }

  @Test(expected = AssertionException.class)
  public void testType_AssertionError() {
    Assertions.assertType(new Object(), String.class);
  }

  @Test
  public void testTypeCustomMessage() {
    expectedEx.expect(AssertionException.class);
    expectedEx.expectMessage("custom arg1");
    Assertions.assertType(new Object(), String.class, "custom {}", "arg1");
  }

  @Test
  public void testTypeCustomMessageNull() {
    assertNull(Assertions.assertType(null, String.class, "custom {}", "arg1"));
  }

  @Test
  public void testNotNull_Positive() {
    Object object = new Object();
    assertSame(object, Assertions.assertNotNull(object));
  }

  @Test(expected = AssertionException.class)
  public void testNotNull_Negative() {
    Assertions.assertNotNull(null);
  }

  @Test
  public void testNotNullOrEmpty_Positive() {
    assertEquals("NOT-NULL", Assertions.assertNotNullOrEmpty("NOT-NULL"));
  }

  @Test
  public void testNotNullOrEmpty_Negative1() {
    // Verify 'NULL'
    try {
      Assertions.assertNotNullOrEmpty(null);
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }

    // Verify 'empty'
    try {
      Assertions.assertNotNullOrEmpty("");
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }
  }

  @Test
  public void testNotNullOrEmpty_Negative2() {
    // Verify 'NULL'
    try {
      Assertions.assertNotNullOrEmpty(null, "failure");
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }

    // Verify 'empty'
    try {
      Assertions.assertNotNullOrEmpty("", "failure");
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }
  }

  @Test
  public void testTrue_Positive() {
    assertTrue(Assertions.assertTrue(true));
  }

  @Test(expected = AssertionException.class)
  public void testTrue_Negative() {
    Assertions.assertTrue(false);
  }

  @Test
  public void testFalse_Positive() {
    assertFalse(Assertions.assertFalse(false));
  }

  @Test(expected = AssertionException.class)
  public void testFalse_Negative() {
    Assertions.assertFalse(true);
  }

  @Test
  public void testNull_positive() {
    assertNull(Assertions.assertNull(null));
  }

  @Test(expected = AssertionException.class)
  public void testNull_negative() {
    Assertions.assertNull(new Object());
  }

  @Test
  public void testLess1() {
    assertEquals(1, Assertions.assertLess(1, 2).intValue());
  }

  @Test(expected = AssertionException.class)
  public void testLess2() {
    Assertions.assertLess(1, 1);
  }

  @Test(expected = AssertionException.class)
  public void testLess3() {
    Assertions.assertLess(1, 0);
  }

  @Test
  public void testLessOrEqual1() {
    assertEquals(1, Assertions.assertLessOrEqual(1, 2).intValue());
  }

  @Test
  public void testLessOrEqual2() {
    assertEquals(1, Assertions.assertLessOrEqual(1, 1).intValue());
  }

  @Test(expected = AssertionException.class)
  public void testLessOrEqual3() {
    Assertions.assertLessOrEqual(1, 0);
  }

  @Test(expected = AssertionException.class)
  public void testGreater1() {
    Assertions.assertGreater(1, 2);
  }

  @Test(expected = AssertionException.class)
  public void testGreater2() {
    Assertions.assertGreater(1, 1);
  }

  @Test
  public void testGreater3() {
    assertEquals(1, Assertions.assertGreater(1, 0).intValue());
  }

  @Test(expected = AssertionException.class)
  public void testGreaterOrEqual1() {
    Assertions.assertGreaterOrEqual(1, 2);
  }

  @Test
  public void testGreaterOrEqual2() {
    assertEquals(1, Assertions.assertGreaterOrEqual(1, 1).intValue());
  }

  @Test
  public void testGreaterOrEqual3() {
    assertEquals(1, Assertions.assertGreaterOrEqual(1, 0).intValue());
  }

  @Test(expected = AssertionException.class)
  public void testEqual1() {
    Assertions.assertEqual(1, 2);
  }

  @Test
  public void testEqual2() {
    assertEquals(1, Assertions.assertEqual(1, 1).intValue());
  }

  @Test(expected = AssertionException.class)
  public void testEqual3() {
    Assertions.assertEqual(1, 0);
  }

  @Test
  public void testEquals1() {
    assertEquals("value", Assertions.assertEquals("value", "value"));
  }

  @Test(expected = AssertionException.class)
  public void testEquals2() {
    Assertions.assertEquals("value", "something other");
  }

  @Test(expected = AssertionException.class)
  public void testNotEquals1() {
    Assertions.assertNotEquals("value", "value");
  }

  @Test
  public void testNotEquals2() {
    assertEquals("value", Assertions.assertNotEquals("value", "something other"));
  }

  @Test
  public void testSame() {
    Object object = new Object();
    assertSame(object, Assertions.assertSame(object, object));
  }

  @Test(expected = AssertionException.class)
  public void testSame2() {
    Assertions.assertSame(new Object(), new Object());
  }

  @Test(expected = AssertionException.class)
  public void testNotSame1() {
    Object object = new Object();
    Assertions.assertNotSame(object, object);
  }

  @Test
  public void testNotSame2() {
    Object object = new Object();
    assertSame(object, Assertions.assertNotSame(object, new Object()));
  }

  @Test
  public void testFail() {
    // 1. Test with simple message
    try {
      Assertions.fail("failure");
      fail();
    }
    catch (AssertionException e) {
      assertEquals("Assertion error: failure", e.getMessage());
    }

    // 2. Test with message with message arguments
    try {
      Assertions.fail("failure [{}, {}]", "A", "B");
      fail();
    }
    catch (AssertionException e) {
      assertEquals("Assertion error: failure [A, B]", e.getMessage());
    }

    // 3. Test with null message
    try {
      Assertions.fail(null);
      fail();
    }
    catch (AssertionException e) {
      assertEquals("Assertion error: null", e.getMessage());
    }

    // 4. Test with message and null argument
    try {
      Assertions.fail("failure {}", (Object) null);
      fail();
    }
    catch (AssertionException e) {
      assertEquals("Assertion error: failure null", e.getMessage());
    }
  }
}
