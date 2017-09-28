/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.IPermitAcquiredCallback;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.QueuePosition;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Before;
import org.junit.Test;

public class MutualExclusionTest {

  private ExecutionSemaphore m_mutex;

  private IFuture<?> m_task1;
  private IFuture<?> m_task2;
  private IFuture<?> m_task3;
  private IFuture<?> m_task4;

  @Before
  public void before() {
    m_mutex = (ExecutionSemaphore) Jobs.newExecutionSemaphore(1);

    m_task1 = mock(IFuture.class);
    when(m_task1.getJobInput()).thenReturn(Jobs.newInput()
        .withExecutionSemaphore(m_mutex)
        .withName("job-1"));
    when(m_task1.getExecutionSemaphore()).thenReturn(m_mutex);

    m_task2 = mock(IFuture.class);
    when(m_task2.getJobInput()).thenReturn(Jobs.newInput()
        .withExecutionSemaphore(m_mutex)
        .withName("job-2"));
    when(m_task2.getExecutionSemaphore()).thenReturn(m_mutex);

    m_task3 = mock(IFuture.class);
    when(m_task3.getJobInput()).thenReturn(Jobs.newInput()
        .withExecutionSemaphore(m_mutex)
        .withName("job-3"));
    when(m_task3.getExecutionSemaphore()).thenReturn(m_mutex);

    m_task4 = mock(IFuture.class);
    when(m_task4.getJobInput()).thenReturn(Jobs.newInput()
        .withExecutionSemaphore(null)
        .withName("job-2"));
    when(m_task4.getExecutionSemaphore()).thenReturn(null);
  }

  @Test(expected = AssertionException.class)
  public void testWrongMutexType() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withExecutionSemaphore(mock(IExecutionSemaphore.class)));
  }

  @Test(expected = AssertionException.class)
  public void testNoMutexTask1() {
    m_mutex.acquire(m_task4, QueuePosition.HEAD);
  }

  @Test(expected = AssertionException.class)
  public void testNoMutexTask2() {
    m_mutex.compete(m_task4, QueuePosition.HEAD, mock(IPermitAcquiredCallback.class));
  }

  /**
   * Tests non-blocking mutex acquisition.
   */
  @Test(timeout = 1000)
  public void testAcquisition1() {
    ExecutionSemaphore mutex = (ExecutionSemaphore) m_task1.getExecutionSemaphore();

    assertEquals(0, mutex.getCompetitorCount());

    // Task1 acquires the mutex.
    mutex.acquire(m_task1, QueuePosition.HEAD);
    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Wrong mutex release.
    try {
      mutex.release(m_task2);
      fail();
    }
    catch (AssertionException e) {
      assertTrue(mutex.isPermitOwner(m_task1));
      assertEquals(1, mutex.getCompetitorCount());
    }

    // Task1 releases the mutex.
    mutex.release(m_task1);
    assertFalse(mutex.isPermitOwner(m_task1));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Tests blocking mutex acquisition.
   */
  @Test(timeout = 1000)
  public void testAcquisition2() {
    ExecutionSemaphore mutex = (ExecutionSemaphore) m_task1.getExecutionSemaphore();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex.
    final AtomicReference<Thread> thread = new AtomicReference<>();
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, new IPermitAcquiredCallback() {

      @Override
      public void onPermitAcquired() {
        thread.set(Thread.currentThread());
      }
    }));
    assertSame(Thread.currentThread(), thread.get());

    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Wrong mutex release.
    try {
      mutex.release(m_task2);
      fail();
    }
    catch (AssertionException e) {
      assertTrue(mutex.isPermitOwner(m_task1));
      assertEquals(1, mutex.getCompetitorCount());
    }

    // Task1 releases the mutex.
    mutex.release(m_task1);
    assertFalse(mutex.isPermitOwner(m_task1));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Task1 acquires the mutex. Then, task2 tries to acquire the mutex (without blocking). Afterwards, task3 tries to
   * acquire the mutex (without blocking), and puts itself in front of the queue. Once task1 releases the mutex, it is
   * passed to the task3, and then to task2.
   */
  @Test(timeout = 1000)
  public void testAcquisition3() {
    ExecutionSemaphore mutex = (ExecutionSemaphore) m_task1.getExecutionSemaphore();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex
    IPermitAcquiredCallback callbackTask1 = mock(IPermitAcquiredCallback.class);
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, callbackTask1));
    verify(callbackTask1, times(1)).onPermitAcquired();

    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Task2 tries to acquire the mutex (without blocking)
    IPermitAcquiredCallback callbackTask2 = mock(IPermitAcquiredCallback.class);
    assertFalse(mutex.compete(m_task2, QueuePosition.TAIL, callbackTask2));
    verify(callbackTask2, never()).onPermitAcquired();
    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    // Task3 tries to acquire the mutex (without blocking)
    IPermitAcquiredCallback callbackTask3 = mock(IPermitAcquiredCallback.class);
    assertFalse(mutex.compete(m_task3, QueuePosition.HEAD, callbackTask3));
    verify(callbackTask3, never()).onPermitAcquired();
    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(3, mutex.getCompetitorCount());

    mutex.release(m_task1);
    verify(callbackTask3, times(1)).onPermitAcquired();
    assertTrue(mutex.isPermitOwner(m_task3));
    assertEquals(2, mutex.getCompetitorCount());

    mutex.release(m_task3);
    verify(callbackTask2, times(1)).onPermitAcquired();
    assertTrue(mutex.isPermitOwner(m_task2));
    assertEquals(1, mutex.getCompetitorCount());

    mutex.release(m_task2);
    assertFalse(mutex.isPermitOwner(m_task2));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Task1 acquires the mutex. Then, task2 tries to acquire the mutex and blocks until acquired. Once task1 releases the
   * mutex, task2 does become the mutex owner.
   */
  @Test(timeout = 5000)
  public void testAcquisition4() {
    final ExecutionSemaphore mutex = (ExecutionSemaphore) m_task1.getExecutionSemaphore();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex
    IPermitAcquiredCallback callbackTask1 = mock(IPermitAcquiredCallback.class);
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, callbackTask1));
    verify(callbackTask1, times(1)).onPermitAcquired();

    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Task2 tries to acquire the mutex, and blocks until acquired
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        mutex.acquire(m_task2, QueuePosition.TAIL);
      }
    }, Jobs.newInput());

    try {
      future.awaitDone(1, TimeUnit.SECONDS);
      fail("timeout expected");
    }
    catch (TimedOutError e) {
      // NOOP
    }
    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    mutex.release(m_task1);
    assertTrue(mutex.isPermitOwner(m_task2));
    assertEquals(1, mutex.getCompetitorCount());

    mutex.release(m_task2);
    assertFalse(mutex.isPermitOwner(m_task2));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Task1 acquires the mutex. Then, task2 tries to acquire the mutex and blocks until acquired. Because task2 cannot
   * acquire the mutex (hold by task1), it is cancelled hard (interrupted=true). That causes task2 no longer to wait for
   * the mutex, meaning that when task1 releases the mutex, task2 does not become the mutex owner.
   */
  @Test(timeout = 5_000)
  public void testAcquisition5() throws java.lang.InterruptedException {
    final ExecutionSemaphore mutex = (ExecutionSemaphore) m_task1.getExecutionSemaphore();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex
    IPermitAcquiredCallback callbackTask1 = mock(IPermitAcquiredCallback.class);
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, callbackTask1));
    verify(callbackTask1, times(1)).onPermitAcquired();

    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Task2 tries to acquire the mutex, and blocks until acquired
    final BlockingCountDownLatch interruptedLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          mutex.acquire(m_task2, QueuePosition.TAIL);
        }
        catch (ThreadInterruptedError e) {
          interruptedLatch.countDown();
        }
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    JobTestUtil.waitForPermitCompetitors(mutex, 2);
    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    future.cancel(true);

    assertTrue(interruptedLatch.await());
    assertTrue(mutex.isPermitOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    mutex.release(m_task1);
    assertFalse(mutex.isPermitOwner(m_task2));
    assertEquals(0, mutex.getCompetitorCount());
  }
}
