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
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.internal.IProgressMonitorProvider;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.commons.UncaughtExceptionRunnable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MutexSemaphoreTest {

  private static ExecutorService s_executor;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  private ModelFutureTask<Object> m_task1;
  private ModelFutureTask<Object> m_task2;
  private ModelFutureTask<Object> m_task3;
  private ModelFutureTask<Object> m_task4;
  private ModelFutureTask<Object> m_task5;

  @Before
  @SuppressWarnings("unchecked")
  public void before() {
    m_task1 = new ModelFutureTask<>(mock(Callable.class), ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task2 = new ModelFutureTask<>(mock(Callable.class), ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task3 = new ModelFutureTask<>(mock(Callable.class), ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task4 = new ModelFutureTask<>(mock(Callable.class), ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task5 = new ModelFutureTask<>(mock(Callable.class), ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
  }

  @Test
  public void testAcquireAndRelease() throws InterruptedException {
    MutexSemaphore mutexSemaphore = new MutexSemaphore();

    // state: []
    assertEmpty(mutexSemaphore);
    assertEquals(0, mutexSemaphore.getPermitCount());

    assertTrue(mutexSemaphore.tryAcquireElseOffer(m_task1, true));
    // state: [(obj1)]
    assertNotEmpty(mutexSemaphore);
    assertEquals(1, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task2, true));
    // state: [(obj1), obj2]
    assertNotEmpty(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task3, false /* head */));
    // state: [(obj1), obj3, obj2]
    assertNotEmpty(mutexSemaphore);
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task4, true));
    // state: [(obj1), obj3, obj2, obj4]
    assertNotEmpty(mutexSemaphore);
    assertEquals(4, mutexSemaphore.getPermitCount());

    assertSame(m_task3, mutexSemaphore.releaseAndPoll());
    // state: [(obj3), obj2, obj4]
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertNotEmpty(mutexSemaphore);

    assertSame(m_task2, mutexSemaphore.releaseAndPoll());
    // state: [(obj2), obj4]
    assertNotEmpty(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task5, false /* head */));
    // state: [(obj2), obj5, obj4]
    assertNotEmpty(mutexSemaphore);
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertSame(m_task5, mutexSemaphore.releaseAndPoll());
    // state: [(obj5), obj4]
    assertNotEmpty(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertSame(m_task4, mutexSemaphore.releaseAndPoll());
    // state: [(obj4)]
    assertNotEmpty(mutexSemaphore);
    assertEquals(1, mutexSemaphore.getPermitCount());

    assertNull(mutexSemaphore.releaseAndPoll());
    // state: []
    assertEmpty(mutexSemaphore);
    assertNull(mutexSemaphore.releaseAndPoll());
    assertEquals(0, mutexSemaphore.getPermitCount());

    // state: []
    assertEmpty(mutexSemaphore);

    assertTrue(mutexSemaphore.tryAcquireElseOffer(m_task1, false /* head */));
    // state: [(obj1)]
    assertNotEmpty(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task2, false /* head */));
    // state: [(obj1), obj2]

    assertNotEmpty(mutexSemaphore);
    assertSame(m_task2, mutexSemaphore.releaseAndPoll());
    // state: [(obj2)]

    assertNotEmpty(mutexSemaphore);

    assertNull(mutexSemaphore.releaseAndPoll());
    // state: []
    assertEmpty(mutexSemaphore);
  }

  @Test
  public void waitUntilEmpty() throws Throwable {

    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    // state: []
    assertEmpty(mutexSemaphore);

    assertTrue(mutexSemaphore.tryAcquireElseOffer(m_task1, true));
    // state: [(obj1)]
    assertNotEmpty(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task2, true));
    // state: [(obj1), obj2]
    assertNotEmpty(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task3, true));
    // state: [(obj1), obj2, obj3]
    assertNotEmpty(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task4, true));
    // state: [(obj1), obj2, obj3, obj4]
    assertNotEmpty(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task5, false /* head */));
    // state: [(obj1), obj5, obj2, obj3, obj4]
    assertNotEmpty(mutexSemaphore);

    final AtomicInteger protocolCount = new AtomicInteger(5);

    final BooleanHolder waitUntilEmptyResult = new BooleanHolder(false);

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    UncaughtExceptionRunnable runnable = new UncaughtExceptionRunnable() {

      @Override
      protected void runSafe() throws Exception {
        setupLatch.countDown();
        try {
          waitUntilEmptyResult.setValue(mutexSemaphore.waitUntilEmpty(new AlwaysFilter<IFuture<?>>(), newDeadline(TimeUnit.SECONDS.toMillis(10))));
        }
        finally {
          verifyLatch.countDown();
        }
      }

      @Override
      protected void onUncaughtException(Throwable t) {
        setupLatch.release();
        verifyLatch.release();
      }
    };
    s_executor.execute(runnable);

    setupLatch.await();
    runnable.throwOnError();
    assertTrue(setupLatch.await());

    Thread.sleep(500); // Wait for the other thread to invoke 'waitUntilEmpty'.

    protocolCount.decrementAndGet();
    assertSame(m_task5, mutexSemaphore.releaseAndPoll());
    // state: [(obj5), obj2, obj3, obj4]

    protocolCount.decrementAndGet();
    assertSame(m_task2, mutexSemaphore.releaseAndPoll());
    // state: [(obj2), obj3, obj4]

    simulateWaitUntilEmptySpuriousWakeup(mutexSemaphore);

    protocolCount.decrementAndGet();
    assertSame(m_task3, mutexSemaphore.releaseAndPoll());
    // state: [(obj3), obj4]

    protocolCount.decrementAndGet();
    assertSame(m_task4, mutexSemaphore.releaseAndPoll());
    // state: [(obj4)]

    protocolCount.decrementAndGet();
    assertNull(mutexSemaphore.releaseAndPoll());
    // state: []

    verifyLatch.await();
    runnable.throwOnError();
    assertTrue(verifyLatch.await());

    assertTrue(waitUntilEmptyResult.getValue());
    assertTrue(mutexSemaphore.isEmpty(new AlwaysFilter<IFuture<?>>()));
    assertTrue(mutexSemaphore.waitUntilEmpty(new AlwaysFilter<IFuture<?>>(), newDeadline(TimeUnit.SECONDS.toMillis(10))));
  }

  @Test
  public void testClearAndCancel() {
    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    assertTrue(mutexSemaphore.tryAcquireElseOffer(m_task1, true));
    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task2, true));
    assertFalse(mutexSemaphore.tryAcquireElseOffer(m_task3, true));

    assertSame(m_task1, mutexSemaphore.getMutexOwner());

    assertEquals(3, mutexSemaphore.getPermitCount());
    assertFalse(mutexSemaphore.isEmpty(new AlwaysFilter<IFuture<?>>()));

    mutexSemaphore.reset();

    assertEquals(0, mutexSemaphore.getPermitCount());
    assertTrue(mutexSemaphore.isEmpty(new AlwaysFilter<IFuture<?>>())); // mutex-owner
    assertNull(mutexSemaphore.getMutexOwner());

    // Release the model mutex
    assertNull(mutexSemaphore.releaseAndPoll());
    assertEquals(0, mutexSemaphore.getPermitCount());
    assertTrue(mutexSemaphore.isEmpty(new AlwaysFilter<IFuture<?>>()));
    assertNull(mutexSemaphore.getMutexOwner());
  }

  private void assertNotEmpty(MutexSemaphore mutexSemaphore) throws InterruptedException {
    assertFalse(mutexSemaphore.isEmpty(new AlwaysFilter<IFuture<?>>()));
    assertFalse(mutexSemaphore.waitUntilEmpty(new AlwaysFilter<IFuture<?>>(), newDeadline(10)));
  }

  private void assertEmpty(MutexSemaphore mutexSemaphore) throws InterruptedException {
    assertTrue(mutexSemaphore.isEmpty(new AlwaysFilter<IFuture<?>>()));
    assertTrue(mutexSemaphore.waitUntilEmpty(new AlwaysFilter<IFuture<?>>(), newDeadline(10)));
  }

  private static void simulateWaitUntilEmptySpuriousWakeup(final MutexSemaphore mutexSemaphore) {
    mutexSemaphore.m_changedLock.lock();
    try {
      mutexSemaphore.m_changedCondition.signalAll();
    }
    finally {
      mutexSemaphore.m_changedLock.unlock();
    }
  }

  private static Date newDeadline(long millis) {
    return new Date(System.currentTimeMillis() + millis);
  }
}
