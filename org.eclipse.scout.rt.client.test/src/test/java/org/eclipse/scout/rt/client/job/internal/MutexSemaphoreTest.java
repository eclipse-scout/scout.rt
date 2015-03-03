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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IFutureVisitor;
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

  private IFuture<Object> m_iFuture1;
  private IFuture<Object> m_iFuture2;
  private IFuture<Object> m_iFuture3;

  @Before
  public void before() {
    m_task1 = new ModelFutureTask<>(ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task2 = new ModelFutureTask<>(ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task3 = new ModelFutureTask<>(ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task4 = new ModelFutureTask<>(ClientJobInput.empty(), mock(IProgressMonitorProvider.class));
    m_task5 = new ModelFutureTask<>(ClientJobInput.empty(), mock(IProgressMonitorProvider.class));

    m_iFuture1 = m_task1.getFuture();
    m_iFuture2 = m_task2.getFuture();
    m_iFuture3 = m_task3.getFuture();
  }

  @Test
  public void testAcquireAndRelease() throws InterruptedException {
    MutexSemaphore mutexSemaphore = new MutexSemaphore();

    // state: []
    assertIdle(mutexSemaphore);
    assertEquals(0, mutexSemaphore.getPermitCount());

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_task1));
    // state: [(obj1)]
    assertNotIdle(mutexSemaphore);
    assertEquals(1, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task2));
    // state: [(obj1), obj2]
    assertNotIdle(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_task3 /* Head */));
    // state: [(obj1), obj3, obj2]
    assertNotIdle(mutexSemaphore);
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task4));
    // state: [(obj1), obj3, obj2, obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(4, mutexSemaphore.getPermitCount());

    assertSame(m_task3, mutexSemaphore.releaseAndPoll());
    // state: [(obj3), obj2, obj4]
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertNotIdle(mutexSemaphore);

    assertSame(m_task2, mutexSemaphore.releaseAndPoll());
    // state: [(obj2), obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_task5));
    // state: [(obj2), obj5, obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(3, mutexSemaphore.getPermitCount());

    assertSame(m_task5, mutexSemaphore.releaseAndPoll());
    // state: [(obj5), obj4]
    assertNotIdle(mutexSemaphore);
    assertEquals(2, mutexSemaphore.getPermitCount());

    assertSame(m_task4, mutexSemaphore.releaseAndPoll());
    // state: [(obj4)]
    assertNotIdle(mutexSemaphore);
    assertEquals(1, mutexSemaphore.getPermitCount());

    assertNull(mutexSemaphore.releaseAndPoll());
    // state: []
    assertIdle(mutexSemaphore);
    assertNull(mutexSemaphore.releaseAndPoll());
    assertEquals(0, mutexSemaphore.getPermitCount());

    // state: []
    assertIdle(mutexSemaphore);

    assertTrue(mutexSemaphore.tryAcquireElseOfferHead(m_task1));
    // state: [(obj1)]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_task2));
    // state: [(obj1), obj2]

    assertNotIdle(mutexSemaphore);
    assertSame(m_task2, mutexSemaphore.releaseAndPoll());
    // state: [(obj2)]

    assertNotIdle(mutexSemaphore);

    assertNull(mutexSemaphore.releaseAndPoll());
    // state: []
    assertIdle(mutexSemaphore);
  }

  @Test
  public void testWaitForIdle() throws Throwable {
    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    // state: []
    assertIdle(mutexSemaphore);

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_task1));
    // state: [(obj1)]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task2));
    // state: [(obj1), obj2]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task3));
    // state: [(obj1), obj2, obj3]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task4));
    // state: [(obj1), obj2, obj3, obj4]
    assertNotIdle(mutexSemaphore);

    assertFalse(mutexSemaphore.tryAcquireElseOfferHead(m_task5));
    // state: [(obj1), obj5, obj2, obj3, obj4]
    assertNotIdle(mutexSemaphore);

    final AtomicInteger protocolCount = new AtomicInteger(5);

    final BooleanHolder waitForIdleResult = new BooleanHolder(false);

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    UncaughtExceptionRunnable runnable = new UncaughtExceptionRunnable() {

      @Override
      protected void runSafe() throws Exception {
        setupLatch.countDown();
        try {
          waitForIdleResult.setValue(mutexSemaphore.waitForIdle(10, TimeUnit.SECONDS));
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

    Thread.sleep(500); // Wait for the other thread to invoke 'waitForIdle'.

    protocolCount.decrementAndGet();
    assertSame(m_task5, mutexSemaphore.releaseAndPoll());
    // state: [(obj5), obj2, obj3, obj4]

    protocolCount.decrementAndGet();
    assertSame(m_task2, mutexSemaphore.releaseAndPoll());
    // state: [(obj2), obj3, obj4]

    simulateWaitForIdleSpuriousWakeup(mutexSemaphore);

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

    assertTrue(waitForIdleResult.getValue());
    assertTrue(mutexSemaphore.isIdle());
    assertTrue(mutexSemaphore.waitForIdle(0, TimeUnit.SECONDS));
  }

  @Test
  public void testClearAndCancel() {
    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_task1));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task2));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task3));

    assertSame(m_task1, mutexSemaphore.getMutexOwner());

    assertEquals(3, mutexSemaphore.getPermitCount());
    assertFalse(mutexSemaphore.isIdle());

    mutexSemaphore.clearAndCancel();

    assertEquals(1, mutexSemaphore.getPermitCount()); // mutex-owner
    assertFalse(mutexSemaphore.isIdle()); // mutex-owner
    assertSame(m_task1, mutexSemaphore.getMutexOwner());

    // Release the model mutex
    assertNull(mutexSemaphore.releaseAndPoll());
    assertEquals(0, mutexSemaphore.getPermitCount());
    assertTrue(mutexSemaphore.isIdle());
    assertNull(mutexSemaphore.getMutexOwner());
  }

  @Test
  public void testVisit() {
    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_task1));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task2));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task3));

    final List<IFuture<?>> visitedFutures = new ArrayList<>();
    mutexSemaphore.visit(new IFutureVisitor() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future);
        return true;
      }
    });

    assertEquals(CollectionUtility.arrayList(m_iFuture1, m_iFuture2, m_iFuture3), visitedFutures);
  }

  @Test
  public void testVisitEmpty() {
    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    final List<IFuture<?>> visitedFutures = new ArrayList<>();
    mutexSemaphore.visit(new IFutureVisitor() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future);
        return true;
      }
    });

    assertEquals(Collections.emptyList(), visitedFutures);
  }

  @Test
  public void testVisitAbort() {
    final MutexSemaphore mutexSemaphore = new MutexSemaphore();

    assertTrue(mutexSemaphore.tryAcquireElseOfferTail(m_task1));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task2));
    assertFalse(mutexSemaphore.tryAcquireElseOfferTail(m_task3));

    final List<IFuture<?>> visitedFutures = new ArrayList<>();
    mutexSemaphore.visit(new IFutureVisitor() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future);
        return false;
      }
    });

    assertEquals(CollectionUtility.arrayList(m_iFuture1), visitedFutures);
  }

  private void assertNotIdle(MutexSemaphore mutexSemaphore) throws InterruptedException {
    assertFalse(mutexSemaphore.isIdle());
    assertFalse(mutexSemaphore.waitForIdle(100, TimeUnit.MILLISECONDS));
  }

  private void assertIdle(MutexSemaphore mutexSemaphore) throws InterruptedException {
    assertTrue(mutexSemaphore.isIdle());
    assertTrue(mutexSemaphore.waitForIdle(100, TimeUnit.MILLISECONDS));
  }

  private static void simulateWaitForIdleSpuriousWakeup(final MutexSemaphore mutexSemaphore) {
    mutexSemaphore.m_idleLock.lock();
    try {
      mutexSemaphore.m_idleCondition.signalAll();
    }
    finally {
      mutexSemaphore.m_idleLock.unlock();
    }
  }
}
