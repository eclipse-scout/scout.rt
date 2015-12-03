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

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.IMutex.QueuePosition;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Before;
import org.junit.Test;

public class MutexTest {

  private IMutex m_mutex;

  private IFuture<?> m_task1;
  private IFuture<?> m_task2;
  private IFuture<?> m_task3;
  private IFuture<?> m_task4;

  @Before
  public void before() {
    m_mutex = Jobs.newMutex();

    m_task1 = mock(IFuture.class);
    when(m_task1.getJobInput()).thenReturn(Jobs.newInput()
        .withMutex(m_mutex)
        .withName("job-1"));

    m_task2 = mock(IFuture.class);
    when(m_task2.getJobInput()).thenReturn(Jobs.newInput()
        .withMutex(m_mutex)
        .withName("job-2"));

    m_task3 = mock(IFuture.class);
    when(m_task3.getJobInput()).thenReturn(Jobs.newInput()
        .withMutex(m_mutex)
        .withName("job-3"));

    m_task4 = mock(IFuture.class);
    when(m_task4.getJobInput()).thenReturn(Jobs.newInput()
        .withMutex(null)
        .withName("job-2"));
  }

  @Test(expected = AssertionException.class)
  public void testNoMutexTask1() {
    m_mutex.acquire(m_task4, QueuePosition.HEAD);
  }

  @Test(expected = AssertionException.class)
  public void testNoMutexTask2() {
    m_mutex.compete(m_task4, QueuePosition.HEAD, mock(IMutexAcquiredCallback.class));
  }

  /**
   * Tests non-blocking mutex acquisition.
   */
  @Test(timeout = 1000)
  public void testAcquisition1() {
    IMutex mutex = m_task1.getJobInput().getMutex();

    assertEquals(0, mutex.getCompetitorCount());

    // Task1 acquires the mutex.
    mutex.acquire(m_task1, QueuePosition.HEAD);
    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Wrong mutex release.
    try {
      mutex.release(m_task2);
      fail();
    }
    catch (AssertionException e) {
      assertTrue(mutex.isMutexOwner(m_task1));
      assertEquals(1, mutex.getCompetitorCount());
    }

    // Task1 releases the mutex.
    mutex.release(m_task1);
    assertFalse(mutex.isMutexOwner(m_task1));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Tests blocking mutex acquisition.
   */
  @Test(timeout = 1000)
  public void testAcquisition2() {
    IMutex mutex = m_task1.getJobInput().getMutex();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex.
    final AtomicReference<Thread> thread = new AtomicReference<>();
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, new IMutexAcquiredCallback() {

      @Override
      public void onMutexAcquired() {
        thread.set(Thread.currentThread());
      }
    }));
    assertSame(Thread.currentThread(), thread.get());

    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Wrong mutex release.
    try {
      mutex.release(m_task2);
      fail();
    }
    catch (AssertionException e) {
      assertTrue(mutex.isMutexOwner(m_task1));
      assertEquals(1, mutex.getCompetitorCount());
    }

    // Task1 releases the mutex.
    mutex.release(m_task1);
    assertFalse(mutex.isMutexOwner(m_task1));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Task1 acquires the mutex. Then, task2 tries to acquire the mutex (without blocking). Afterwards, task3 tries to
   * acquire the mutex (without blocking), and puts itself in front of the queue. Once task1 releases the mutex, it is
   * passed to the task3, and then to task2.
   */
  @Test(timeout = 1000)
  public void testAcquisition3() {
    IMutex mutex = m_task1.getJobInput().getMutex();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex
    IMutexAcquiredCallback callbackTask1 = mock(IMutexAcquiredCallback.class);
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, callbackTask1));
    verify(callbackTask1, times(1)).onMutexAcquired();

    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Task2 tries to acquire the mutex (without blocking)
    IMutexAcquiredCallback callbackTask2 = mock(IMutexAcquiredCallback.class);
    assertFalse(mutex.compete(m_task2, QueuePosition.TAIL, callbackTask2));
    verify(callbackTask2, never()).onMutexAcquired();
    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    // Task3 tries to acquire the mutex (without blocking)
    IMutexAcquiredCallback callbackTask3 = mock(IMutexAcquiredCallback.class);
    assertFalse(mutex.compete(m_task3, QueuePosition.HEAD, callbackTask3));
    verify(callbackTask3, never()).onMutexAcquired();
    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(3, mutex.getCompetitorCount());

    mutex.release(m_task1);
    verify(callbackTask3, times(1)).onMutexAcquired();
    assertTrue(mutex.isMutexOwner(m_task3));
    assertEquals(2, mutex.getCompetitorCount());

    mutex.release(m_task3);
    verify(callbackTask2, times(1)).onMutexAcquired();
    assertTrue(mutex.isMutexOwner(m_task2));
    assertEquals(1, mutex.getCompetitorCount());

    mutex.release(m_task2);
    assertFalse(mutex.isMutexOwner(m_task2));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Task1 acquires the mutex. Then, task2 tries to acquire the mutex and blocks until acquired. Once task1 releases the
   * mutex, task2 does become the mutex owner.
   */
  @Test(timeout = 5000)
  public void testAcquisition4() {
    final IMutex mutex = m_task1.getJobInput().getMutex();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex
    IMutexAcquiredCallback callbackTask1 = mock(IMutexAcquiredCallback.class);
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, callbackTask1));
    verify(callbackTask1, times(1)).onMutexAcquired();

    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Task2 tries to acquire the mutex, and blocks until acquired
    boolean timeout = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        mutex.acquire(m_task2, QueuePosition.TAIL);
      }
    }, Jobs.newInput())
        .awaitDone(1, TimeUnit.SECONDS);
    assertFalse(timeout);
    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    mutex.release(m_task1);
    assertTrue(mutex.isMutexOwner(m_task2));
    assertEquals(1, mutex.getCompetitorCount());

    mutex.release(m_task2);
    assertFalse(mutex.isMutexOwner(m_task2));
    assertEquals(0, mutex.getCompetitorCount());
  }

  /**
   * Task1 acquires the mutex. Then, task2 tries to acquire the mutex and blocks until acquired. Because task2 cannot
   * acquire the mutex (hold by task1), it is cancelled hard (interrupted=true). That causes task2 no longer to wait for
   * the mutex, meaning that when task1 releases the mutex, task2 does not become the mutex owner.
   */
  @Test(timeout = 5_000)
  public void testAcquisition5() throws InterruptedException {
    final IMutex mutex = m_task1.getJobInput().getMutex();

    assertEquals(0, mutex.getCompetitorCount());

    // Make task1 to acquire the mutex
    IMutexAcquiredCallback callbackTask1 = mock(IMutexAcquiredCallback.class);
    assertTrue(mutex.compete(m_task1, QueuePosition.HEAD, callbackTask1));
    verify(callbackTask1, times(1)).onMutexAcquired();

    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(1, mutex.getCompetitorCount());

    // Task2 tries to acquire the mutex, and blocks until acquired
    final BlockingCountDownLatch interruptedLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          mutex.acquire(m_task2, QueuePosition.TAIL);
        }
        catch (ProcessingException e) {
          interruptedLatch.countDown();
        }
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    JobTestUtil.waitForMutexCompetitors(mutex, 2);
    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    future.cancel(true);

    assertTrue(interruptedLatch.await());
    assertTrue(mutex.isMutexOwner(m_task1));
    assertEquals(2, mutex.getCompetitorCount());

    mutex.release(m_task1);
    assertFalse(mutex.isMutexOwner(m_task2));
    assertEquals(0, mutex.getCompetitorCount());
  }
}
