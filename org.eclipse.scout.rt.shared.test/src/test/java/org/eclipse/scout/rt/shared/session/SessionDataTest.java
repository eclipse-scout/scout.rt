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
package org.eclipse.scout.rt.shared.session;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(PlatformTestRunner.class)
public class SessionDataTest {

  private static final Object TEST_DATA = new Object();
  private static final String TEST_KEY = "testKey";

  private SessionData m_sessionData;

  @Before
  public void before() {
    m_sessionData = new SessionData();
  }

  @Test(expected = AssertionException.class)
  public void testSetDataNullKey() {
    m_sessionData.set(null, TEST_DATA);
  }

  @Test(expected = AssertionException.class)
  public void testGetDataNullKey() {
    m_sessionData.get(null);
  }

  @Test
  public void testSetData() {
    assertNull(m_sessionData.get(TEST_KEY));
    m_sessionData.set(TEST_KEY, TEST_DATA);
    assertSame(TEST_DATA, m_sessionData.get(TEST_KEY));
    m_sessionData.set(TEST_KEY, null);
    assertNull(m_sessionData.get(TEST_KEY));
  }

  @Test
  public void testComputeIfAbsent() {
    final BooleanHolder computedHolder = new BooleanHolder();
    Object value = m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        computedHolder.setValue(true);
        return TEST_DATA;
      }
    });
    assertSame(TEST_DATA, value);
    assertSame(TEST_DATA, m_sessionData.get(TEST_KEY));
    assertTrue(computedHolder.getValue());
  }

  @Test(expected = AssertionException.class)
  public void testComputeIfAbsentNullKey() {
    m_sessionData.computeIfAbsent(null, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        return null;
      }
    });
  }

  @Test(expected = AssertionException.class)
  public void testComputeIfAbsentNullProducer() {
    m_sessionData.computeIfAbsent(TEST_KEY, null);
  }

  @Test(expected = PlatformException.class)
  public void testComputeIfAbsentProducerThrowsException() {
    m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        throw new IOException();
      }
    });
  }

  @Test
  public void testComputeIfAbsentValueAlreadySet() {
    m_sessionData.set(TEST_KEY, TEST_DATA);
    Object value = m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        fail("Value is already set");
        return null;
      }
    });
    assertSame(TEST_DATA, value);
  }

  @Test
  public void testComputeIfAbsentConcurrentProducers() {
    final CountDownLatch phase1 = new CountDownLatch(1);
    final CountDownLatch phase2 = new CountDownLatch(1);
    IFuture<Object> firstProducerFuture = Jobs.schedule(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        return m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // release the second producer
            phase1.countDown();
            // wait until the second producer has returned its value
            phase2.await();
            // since the second producer has already provided its value, this return value is not used
            return new Object();
          }
        });
      }
    }, Jobs
        .newInput()
        .withRunContext(RunContexts.empty()));

    IFuture<Object> secondProducerFuture = Jobs.schedule(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        // wait until the first producer is creating a new value
        phase1.await();
        try {
          return m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
              return TEST_DATA;
            }
          });
        }
        finally {
          // release first producer
          phase2.countDown();
        }
      }
    }, Jobs
        .newInput()
        .withRunContext(RunContexts.empty()));

    assertSame(TEST_DATA, firstProducerFuture.awaitDoneAndGet());
    assertSame(TEST_DATA, secondProducerFuture.awaitDoneAndGet());
    assertSame(TEST_DATA, m_sessionData.get(TEST_KEY));
  }

  @Test
  public void testComputeIfAbsentReturningNull() {
    final BooleanHolder computedHolder = new BooleanHolder();
    // 1. compute null value
    Object value = m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        computedHolder.setValue(true);
        return null;
      }
    });
    assertNull(value);
    assertTrue(computedHolder.getValue());

    // 2. compute non-null value
    computedHolder.setValue(false);
    value = m_sessionData.computeIfAbsent(TEST_KEY, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        computedHolder.setValue(true);
        return TEST_DATA;
      }
    });
    assertSame(TEST_DATA, value);
    assertTrue(computedHolder.getValue());
  }

  @Test
  public void testComputeIfAbsentDataWithOtherTypeAlreadySet() {
    m_sessionData.set(TEST_KEY, TEST_DATA);
    Object value = m_sessionData.computeIfAbsent(TEST_KEY, new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "test";
      }
    });
    assertSame(TEST_DATA, value);
  }
}
