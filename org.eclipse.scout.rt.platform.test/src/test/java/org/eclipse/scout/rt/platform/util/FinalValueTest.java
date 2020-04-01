/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.exception.PlatformException;
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
    assertNull(s.get());
  }

  @Test
  public void testSuccessfulSet() {
    FinalValue<String> s = new FinalValue<>();
    s.set(TEST_VALUE);
    assertTestValue(s);
  }

  @Test
  public void testDupplicateSet() {
    FinalValue<String> s = new FinalValue<>();
    s.set(TEST_VALUE);
    try {
      s.set(TEST_VALUE);
      fail("expecting AssertionException");
    }
    catch (AssertionException expected) {
    }
  }

  @Test
  public void testLazySet() {
    FinalValue<String> s = new FinalValue<>();
    String value = s.setIfAbsentAndGet(TEST_VALUE);
    assertTestValue(s);
    assertEquals(TEST_VALUE, value);
  }

  @Test
  public void testLazySetWithException() {
    FinalValue<String> s = new FinalValue<>();
    try {
      s.setIfAbsent(() -> {
        throw new Exception("expected JUnit test exception");
      });
      fail("expecting PlatformException");
    }
    catch (PlatformException expected) {
    }
  }

  @Test
  public void testLazySetWithCustomException() {
    FinalValue<String> s = new FinalValue<>();
    try {
      s.setIfAbsent(() -> {
        throw new MyRuntimeException();
      });
      fail("expecting MyRuntimeException");
    }
    catch (MyRuntimeException expected) {
    }
  }

  @Test
  public void testLazyDuplicateSet() {
    FinalValue<String> s = new FinalValue<>();
    s.setIfAbsentAndGet(TEST_VALUE);
    String value2 = s.setIfAbsentAndGet("other");
    assertTestValue(s);
    assertEquals(TEST_VALUE, value2);
  }

  @Test
  public void testNoDoubleInitializationTry() {
    FinalValue<String> s = new FinalValue<>();
    s.setIfAbsentAndGet((String) null);
    Assert.assertNull(s.setIfAbsentAndGet("should not matter"));
  }

  @Test(timeout = 2000)
  public void testBlockingCalls() {
    final FinalValue<String> s = new FinalValue<>();

    final CountDownLatch setup = new CountDownLatch(1);
    final CountDownLatch latch = new CountDownLatch(1);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(() -> {
      try {
        // wait until test thread is invoking FinalValue producer's call method
        setup.await(5, TimeUnit.SECONDS);
        s.setIfAbsent(() -> {
          // release test thread
          latch.countDown();
          return "scheduled thread";
        });
      }
      catch (InterruptedException e) {
        // nop
      }
    });

    String value = s.setIfAbsentAndGet(() -> {
      setup.countDown();
      latch.await(5, TimeUnit.SECONDS);
      return "test thread";
    });

    assertTrue(s.isSet());
    // exact assertion about value is not possible because execution order is not deterministic
    assertTrue(ObjectUtility.isOneOf(value, "scheduled thread", "test thread"));
  }

  private void assertTestValue(FinalValue<String> s) {
    assertTrue(s.isSet());
    assertEquals(TEST_VALUE, s.get());
  }

  class MyRuntimeException extends RuntimeException {

    MyRuntimeException() {
      super("expected JUnit test exception");
    }

    private static final long serialVersionUID = 1L;
  }
}
