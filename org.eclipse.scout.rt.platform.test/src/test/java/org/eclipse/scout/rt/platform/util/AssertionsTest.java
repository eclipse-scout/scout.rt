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
import org.junit.Test;

public class AssertionsTest {

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
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertInstance(new Object(), String.class, "custom"));
    assertEquals("Assertion error: custom", e.getMessage());
  }

  @Test
  public void testInstanceCustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertInstance(new Object(), String.class, "custom {}", "arg1"));
    assertEquals("Assertion error: custom arg1", e.getMessage());
  }

  @Test
  public void testInstanceCustomMessageNullValue() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertInstance(null, String.class, "custom {}", "arg1"));
    assertEquals("Assertion error: custom arg1", e.getMessage());
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
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertType(new Object(), String.class, "custom"));
    assertEquals("Assertion error: custom", e.getMessage());
  }

  @Test
  public void testTypeCustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertType(new Object(), String.class, "custom {}", "arg1"));
    assertEquals("Assertion error: custom arg1", e.getMessage());
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
  public void testNotNullCustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotNull(null, "custom"));
    assertEquals("Assertion error: custom", e.getMessage());
  }

  @Test
  public void testNotNullCustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotNull(null, "custom {}", "arg1"));
    assertEquals("Assertion error: custom arg1", e.getMessage());
  }

  @Test
  public void testNotNullOrEmpty_Positive() {
    assertEquals("NOT-NULL", Assertions.assertNotNullOrEmpty("NOT-NULL"));
  }

  @Test(expected = AssertionException.class)
  public void testNotNullOrEmpty_Negative1() {
    Assertions.assertNotNullOrEmpty(null);
  }

  @Test(expected = AssertionException.class)
  public void testNotNullOrEmpty_Negative2() {
    Assertions.assertNotNullOrEmpty("");
  }

  @Test
  public void testNotNullOrEmpty_Negative1_CustomMessage() {
    AssertionException e1 = assertThrows(AssertionException.class, () -> Assertions.assertNotNullOrEmpty(null, "failure"));
    assertEquals("Assertion error: failure", e1.getMessage());
  }

  @Test
  public void testNotNullOrEmpty_Negative2_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotNullOrEmpty("", "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testNotNullOrEmpty_Negative1_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotNullOrEmpty(null, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
  }

  @Test
  public void testNotNullOrEmpty_Negative2_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotNullOrEmpty("", "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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
  public void testTrue_Negative_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertTrue(false, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testTrue_Negative_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertTrue(false, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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
  public void testFalse_Negative_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertFalse(true, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testFalse_Negative_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertFalse(true, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
  }

  @Test
  public void testNull_Positive() {
    assertNull(Assertions.assertNull(null));
  }

  @Test(expected = AssertionException.class)
  public void testNull_Negative() {
    Assertions.assertNull(new Object());
  }

  @Test
  public void testNull_Negative_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNull(new Object(), "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testNull_Negative_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNull(new Object(), "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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
  public void testLess_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertLess(1, 0, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testLess_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertLess(1, 0, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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

  @Test
  public void testLessOrEqual_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertLessOrEqual(1, 0, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testLessOrEqual_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertLessOrEqual(1, 0, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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

  @Test
  public void testGreater_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertGreater(1, 2, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testGreater_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertGreater(1, 2, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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

  @Test
  public void testGreaterOrEqual_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertGreaterOrEqual(1, 2, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testGreaterOrEqual_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertGreaterOrEqual(1, 2, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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
  public void testEqual_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertEqual(1, 0, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testEqual_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertEqual(1, 0, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
  }

  @Test
  public void testEquals1() {
    assertEquals("value", Assertions.assertEquals("value", "value"));
  }

  @Test(expected = AssertionException.class)
  public void testEquals2() {
    Assertions.assertEquals("value", "something other");
  }

  @Test
  public void testEquals_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertEquals("value", "something other", "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testEquals_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertEquals("value", "something other", "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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
  public void testNotEquals_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotEquals("value", "value", "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testNotEquals_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotEquals("value", "value", "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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

  @Test
  public void testSame_CustomMessage() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertSame(new Object(), new Object(), "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testSame_CustomMessageWithArgs() {
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertSame(new Object(), new Object(), "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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
  public void testNotSame_CustomMessage() {
    Object object = new Object();
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotSame(object, object, "failure"));
    assertEquals("Assertion error: failure", e.getMessage());
  }

  @Test
  public void testNotSame_CustomMessageWithArgs() {
    Object object = new Object();
    AssertionException e = assertThrows(AssertionException.class, () -> Assertions.assertNotSame(object, object, "failure {}", "arg1"));
    assertEquals("Assertion error: failure arg1", e.getMessage());
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

    // 2. Test with message and message arguments
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
