/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
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
    final LazyValue<Object> lazy = new LazyValue<>(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        producerCounter.incrementAndGet();
        return testObject;
      }
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

    final LazyValue<String> lazy = new LazyValue<>(new Callable<String>() {
      @Override
      public String call() throws Exception {
        if (flag.get()) {
          return "Hello";
        }
        throw new P_MyRuntimException();
      }
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
}
