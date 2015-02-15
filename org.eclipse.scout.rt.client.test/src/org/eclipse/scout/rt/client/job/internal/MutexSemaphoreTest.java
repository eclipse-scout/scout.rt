/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.holders.BooleanHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MutexSemaphoreTest {

  @Mock
  private Object m_obj1;
  @Mock
  private Object m_obj2;
  @Mock
  private Object m_obj3;
  @Mock
  private Object m_obj4;
  @Mock
  private Object m_obj5;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAcquireAndRelease() throws InterruptedException {
    MutexSemaphore<Object> mutexSemaphore = new MutexSemaphore<Object>();

    // state: []
    assertFalse(mutexSemaphore.isModelThread());
    assertIdle(mutexSemaphore);
    assertEquals(0, mutexSemaphore.getPermitCount());

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_obj1));
    // state: [(obj1)]
    assertNotIdle(mutexSemaphore);
    assertFalse(mutexSemaphore.isModelThread());
    assertEquals(1, mutexSemaphore.getPermitCount());

    // Test model thread
    mutexSemaphore.registerAsModelThread();
    assertTrue(mutexSemaphore.isModelThread());

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj2));
    // state: [(obj1), obj2]
    assertNotIdle(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_obj3 /* Head */));
    // state: [(obj1), obj3, obj2]
    assertNotIdle(mutexSemaphore);
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj4));
    // state: [(obj1), obj3, obj2, obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(4, mutexSemaphore.getPermitCount());

    assertSame(m_obj3, mutexSemaphore.pollElseRelease());
    // state: [(obj3), obj2, obj4]
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.isModelThread());
    assertNotIdle(mutexSemaphore);

    assertSame(m_obj2, mutexSemaphore.pollElseRelease());
    // state: [(obj2), obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_obj5));
    // state: [(obj2), obj5, obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertSame(m_obj5, mutexSemaphore.pollElseRelease());
    // state: [(obj5), obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertSame(m_obj4, mutexSemaphore.pollElseRelease());
    // state: [(obj4)]
    assertNotIdle(mutexSemaphore);
    assertEquals(1, mutexSemaphore.getPermitCount());

    assertNull(mutexSemaphore.pollElseRelease());
    // state: []
    assertIdle(mutexSemaphore);
    assertNull(mutexSemaphore.pollElseRelease());
    assertEquals(0, mutexSemaphore.getPermitCount());

    // state: []
    assertIdle(mutexSemaphore);

    assertTrue(mutexSemaphore.tryAcquireElseOfferHead(m_obj1));
    // state: [(obj1)]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_obj2));
    // state: [(obj1), obj2]

    assertNotIdle(mutexSemaphore);
    assertSame(m_obj2, mutexSemaphore.pollElseRelease());
    // state: [(obj2)]

    assertNotIdle(mutexSemaphore);

    assertNull(mutexSemaphore.pollElseRelease());
    // state: []
    assertIdle(mutexSemaphore);
  }

  @Test
  public void testWaitForIdle() throws InterruptedException {
    final MutexSemaphore<Object> mutexSemaphore = new MutexSemaphore<Object>();

    // state: []
    assertIdle(mutexSemaphore);

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_obj1));
    // state: [(obj1)]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj2));
    // state: [(obj1), obj2]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj3));
    // state: [(obj1), obj2, obj3]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj4));
    // state: [(obj1), obj2, obj3, obj4]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_obj5));
    // state: [(obj1), obj5, obj2, obj3, obj4]
    assertNotIdle(mutexSemaphore);

    final AtomicInteger protocolCount = new AtomicInteger(5);
    final ExecutorService executor = Executors.newFixedThreadPool(1);

    final BooleanHolder waitForIdleResult = new BooleanHolder(false);
    executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          waitForIdleResult.setValue(mutexSemaphore.waitForIdle(10, TimeUnit.SECONDS));
        }
        catch (InterruptedException e) {
        }
        finally {
          executor.shutdown();
        }
      }
    });

    protocolCount.decrementAndGet();
    assertSame(m_obj5, mutexSemaphore.pollElseRelease());
    // state: [(obj5), obj2, obj3, obj4]
    Thread.sleep(100);

    protocolCount.decrementAndGet();
    assertSame(m_obj2, mutexSemaphore.pollElseRelease());
    // state: [(obj2), obj3, obj4]
    Thread.sleep(100);

    simulateWaitForIdleSpuriousWakeup(mutexSemaphore);

    protocolCount.decrementAndGet();
    assertSame(m_obj3, mutexSemaphore.pollElseRelease());
    // state: [(obj3), obj4]
    Thread.sleep(100);

    protocolCount.decrementAndGet();
    assertSame(m_obj4, mutexSemaphore.pollElseRelease());
    // state: [(obj4)]
    Thread.sleep(100);

    protocolCount.decrementAndGet();
    assertNull(mutexSemaphore.pollElseRelease());
    // state: []

    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    assertTrue(waitForIdleResult.getValue());
    assertTrue(mutexSemaphore.isIdle());
  }

  @Test
  public void testClear() {
    final MutexSemaphore<Object> mutexSemaphore = new MutexSemaphore<Object>();

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_obj1));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj2));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_obj3));

    assertSame(m_obj1, mutexSemaphore.getMutexOwner());

    assertEquals(3, mutexSemaphore.getPermitCount());
    assertFalse(mutexSemaphore.isIdle());

    mutexSemaphore.registerAsModelThread();
    assertTrue(mutexSemaphore.isModelThread());

    mutexSemaphore.clear();
    assertEquals(0, mutexSemaphore.getPermitCount());
    assertTrue(mutexSemaphore.isIdle());
    assertNull(mutexSemaphore.getMutexOwner());
    assertFalse(mutexSemaphore.isModelThread());
    assertFalse(mutexSemaphore.isModelThread());
  }

  private void assertNotIdle(MutexSemaphore<Object> mutexSemaphore) throws InterruptedException {
    assertFalse(mutexSemaphore.isIdle());
    assertFalse(mutexSemaphore.waitForIdle(100, TimeUnit.MILLISECONDS));
  }

  private void assertIdle(MutexSemaphore<Object> mutexSemaphore) throws InterruptedException {
    assertTrue(mutexSemaphore.isIdle());
    assertTrue(mutexSemaphore.waitForIdle(100, TimeUnit.MILLISECONDS));
  }

  private static void simulateWaitForIdleSpuriousWakeup(final MutexSemaphore<Object> mutexSemaphore) {
    mutexSemaphore.m_idleLock.lock();
    try {
      mutexSemaphore.m_idleCondition.signalAll();
    }
    finally {
      mutexSemaphore.m_idleLock.unlock();
    }
  }
}
