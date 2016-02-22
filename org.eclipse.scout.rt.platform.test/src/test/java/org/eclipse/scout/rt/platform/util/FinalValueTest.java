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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
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
  public void testLazySet() throws Exception {
    FinalValue<String> s = new FinalValue<>();
    String value = s.setIfAbsent(TEST_VALUE);
    assertTestValue(s);
    assertEquals(TEST_VALUE, value);
  }

  @Test
  public void testLazySetWithException() throws Exception {
    FinalValue<String> s = new FinalValue<>();
    try {
      s.setIfAbsent(new Callable<String>() {
        @Override
        public String call() throws Exception {
          throw new Exception("expected JUnit test exception");
        }
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
      s.setIfAbsent(new Callable<String>() {
        @Override
        public String call() {
          throw new MyRuntimeException();
        }
      });
      fail("expecting MyRuntimeException");
    }
    catch (MyRuntimeException expected) {
    }
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

  @Test(timeout = 2000)
  public void testBlockingCalls() {
    final FinalValue<String> s = new FinalValue<>();

    final CountDownLatch setup = new CountDownLatch(1);
    final CountDownLatch latch = new CountDownLatch(1);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          // wait until test thread is invoking FinalValue producer's call method
          setup.await(5, TimeUnit.SECONDS);
          s.setIfAbsent(new Callable<String>() {
            @Override
            public String call() throws Exception {
              // release test thread
              latch.countDown();
              return "scheduled thread";
            }
          });
        }
        catch (InterruptedException e) {
          // nop
        }
      }
    });

    String value = s.setIfAbsent(new Callable<String>() {
      @Override
      public String call() throws Exception {
        setup.countDown();
        latch.await(5, TimeUnit.SECONDS);
        return "test thread";
      }
    });

    assertTrue(s.isSet());
    // exact assertion about value is not possible because execution order is not deterministic
    assertTrue(CompareUtility.isOneOf(value, "scheduled thread", "test thread"));
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
