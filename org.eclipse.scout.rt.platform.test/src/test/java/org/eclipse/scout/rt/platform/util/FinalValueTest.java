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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link FinalValue}
 */
public class FinalValueTest {
  private static final String TEST_VALUE = "test";

  @Test
  public void testUnset() {
    FinalValue<String> s = new FinalValue<>();
    assertFalse(s.isSet());
  }

  @Test
  public void testSetViaConstructor() {
    FinalValue<String> s = new FinalValue<>(TEST_VALUE);
    assertTestValue(s);
  }

  @Test
  public void testSuccessfulSet() {
    FinalValue<String> s = new FinalValue<>();
    s.set(TEST_VALUE);
    assertTestValue(s);
  }

  @Test(expected = AssertionException.class)
  public void testDupplicateSet() {
    FinalValue<String> s = new FinalValue<>();
    s.set(TEST_VALUE);
    s.set(TEST_VALUE);
  }

  @Test
  public void testLazySet() throws Exception {
    FinalValue<String> s = new FinalValue<>();
    String value = s.setIfAbsent(TEST_VALUE);
    assertTestValue(s);
    assertEquals(TEST_VALUE, value);
  }

  @Test(expected = RuntimeException.class)
  public void testLazySetWithException() throws Exception {
    FinalValue<String> s = new FinalValue<>();
    String value = s.setIfAbsent(new Callable<String>() {
      @Override
      public String call() throws Exception {
        throw new Exception();
      }
    });
    assertTestValue(s);
    assertEquals(TEST_VALUE, value);
  }

  @Test(expected = MyRuntimeException.class)
  public void testLazySetWithCustomException() {
    FinalValue<String> s = new FinalValue<>();
    String value = s.setIfAbsent(new Callable<String>() {
      @Override
      public String call() {
        throw new MyRuntimeException();
      }
    });
    assertTestValue(s);
    assertEquals(TEST_VALUE, value);
  }

  @Test
  public void testLazyDuplicateSet() throws Exception {
    FinalValue<String> s = new FinalValue<>();
    s.setIfAbsent(TEST_VALUE);
    String value2 = s.setIfAbsent("other");
    assertTestValue(s);
    assertEquals(TEST_VALUE, value2);
  }

  @Test
  public void testNoDoubleInitializationTry() throws Exception {
    FinalValue<String> s = new FinalValue<>();
    s.setIfAbsent((String) null);
    Assert.assertNull(s.setIfAbsent("should not matter"));
  }

  private void assertTestValue(FinalValue<String> s) {
    assertTrue(s.isSet());
    assertEquals(TEST_VALUE, s.get());
  }

  class MyRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }
}
