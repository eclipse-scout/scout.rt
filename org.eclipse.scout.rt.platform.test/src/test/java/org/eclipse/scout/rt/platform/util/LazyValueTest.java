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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.junit.Test;

/**
 * Tests for {@link FinalValue}
 */
public class LazyValueTest {

  @ApplicationScoped
  public static class TestBean {
  }

  @Test
  public void testGetWithProducer() {
    final AtomicInteger producerCounter = new AtomicInteger();
    final Object testObject = new Object();
    final LazyValue<Object> lazy = new LazyValue<>(() -> {
      producerCounter.incrementAndGet();
      return testObject;
    });

    assertFalse(lazy.isSet());
    assertEquals(0, producerCounter.get());

    assertSame(testObject, lazy.get());
    assertTrue(lazy.isSet());
    assertEquals(1, producerCounter.get());

    // Get again -> counter must not change
    assertSame(testObject, lazy.get());
    assertTrue(lazy.isSet());
    assertEquals(1, producerCounter.get());
  }

  @Test
  public void testGetWithBeanClass() {
    final LazyValue<TestBean> lazy = new LazyValue<>(TestBean.class);

    assertFalse(lazy.isSet());

    TestBean bean = lazy.get();
    assertNotNull(bean);
    assertTrue(lazy.isSet());
    assertSame(BEANS.get(TestBean.class), bean);

    assertSame(bean, lazy.get());
    assertTrue(lazy.isSet());
  }

  @Test
  public void testGetWithException() {
    final AtomicBoolean flag = new AtomicBoolean();

    final LazyValue<String> lazy = new LazyValue<>(() -> {
      if (flag.get()) {
        return "Hello";
      }
      throw new P_MyRuntimException();
    });

    assertFalse(lazy.isSet());
    try {
      lazy.get();
      fail("get() did not throw exception on first try");
    }
    catch (P_MyRuntimException e) {
      // ok, this is expected
    }

    try {
      lazy.get();
      fail("get() did not throw exception on second try");
    }
    catch (P_MyRuntimException e) {
      // ok, this is expected
    }

    flag.set(true);
    // now it should work
    assertEquals("Hello", lazy.get());

    flag.set(false);
    // should still work
    assertEquals("Hello", lazy.get());
  }

  private class P_MyRuntimException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  @Test
  public void testIfSet() {
    BooleanHolder hasBeenCalled = new BooleanHolder(false);
    LazyValue<String> s = new LazyValue<>(() -> "42");

    assertFalse(hasBeenCalled.getValue());
    s.ifSet(x -> hasBeenCalled.setValue(true));
    assertFalse(hasBeenCalled.getValue());

    assertNotNull(s.get());
    s.ifSet(x -> hasBeenCalled.setValue(true));
    assertTrue(hasBeenCalled.getValue());
  }
}
