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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobScheduleTest {

  private IJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testWithCallable() throws ProcessingException {
    IFuture<String> future = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        return "running";
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    // VERIFY
    assertEquals("running", future.awaitDoneAndGet());
    assertTrue(future.isDone());
  }

  @Test
  public void testWithRunnable() throws ProcessingException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running");
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    // VERIFY
    assertNull(future.awaitDoneAndGet());
    assertEquals(CollectionUtility.hashSet("running"), protocol);
    assertTrue(future.isDone());
  }

  @Test
  public void testProcessingExceptionWithRunnable() throws ProcessingException {
    final ProcessingException exception = new ProcessingException();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(exception, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testProcessingExceptionWithCallable() throws ProcessingException {
    final ProcessingException exception = new ProcessingException();

    IFuture<Void> future = m_jobManager.schedule(new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        throw exception;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(exception, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testRuntimeExceptionWithRunnable() throws ProcessingException {
    final RuntimeException exception = new RuntimeException();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testRuntimeExceptionWithCallable() throws ProcessingException {
    final RuntimeException exception = new RuntimeException();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testExceptionExceptionWithRunnable() throws ProcessingException {
    final Exception exception = new Exception();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testExceptionExceptionWithCallable() throws ProcessingException {
    final Exception exception = new Exception();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testWorkerThread() throws ProcessingException {
    final Set<Thread> protocol = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(Thread.currentThread());

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add(Thread.currentThread());
          }
        }, Jobs.newInput(RunContexts.copyCurrent())).awaitDoneAndGet();
      }
    }, Jobs.newInput(RunContexts.copyCurrent())).awaitDoneAndGet();

    assertEquals(2, protocol.size());
    assertFalse(protocol.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
          }
        }, Jobs.newInput(RunContexts.copyCurrent()).name("XYZ")).awaitDoneAndGet();
      }
    }, Jobs.newInput(RunContexts.copyCurrent()).name("ABC")).awaitDoneAndGet();
    assertTrue(actualThreadName1.getValue(), actualThreadName1.getValue().matches("scout-thread-(\\d)+ \\[Running\\] ABC"));
    assertTrue(actualThreadName2.getValue(), actualThreadName2.getValue().matches("scout-thread-(\\d)+ \\[Running\\] XYZ"));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentFuture() throws ProcessingException {
    final Holder<IFuture<?>> expectedFuture1 = new Holder<>();
    final Holder<IFuture<?>> expectedFuture2 = new Holder<>();

    final Holder<IFuture<?>> actualFuture1 = new Holder<>();
    final Holder<IFuture<?>> actualFuture2 = new Holder<>();

    IFuture.CURRENT.remove();

    expectedFuture1.setValue(m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFuture1.setValue(IFuture.CURRENT.get());

        expectedFuture2.setValue(m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualFuture2.setValue(IFuture.CURRENT.get());
          }
        }, Jobs.newInput(RunContexts.copyCurrent())));

        expectedFuture2.getValue().awaitDoneAndGet(); // wait for the job to complete
      }
    }, Jobs.newInput(RunContexts.copyCurrent())));

    expectedFuture1.getValue().awaitDoneAndGet(); // wait for the job to complete

    assertNotNull(expectedFuture1.getValue());
    assertNotNull(expectedFuture2.getValue());

    assertSame(expectedFuture1.getValue(), actualFuture1.getValue());
    assertSame(expectedFuture2.getValue(), actualFuture2.getValue());

    assertNull(IFuture.CURRENT.get());
  }

  @Test
  public void testScheduleAndGet() throws ProcessingException {
    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.sleep(500); // Wait some time to test that 'Future.get' blocks.
        protocol.add(1);
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));
    future.awaitDoneAndGet();
    protocol.add(2);

    assertEquals(Arrays.asList(1, 2), protocol);
  }

  @Test
  public void testParallelExecution() throws Exception {
    final BlockingCountDownLatch barrier = new BlockingCountDownLatch(3);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    assertTrue(barrier.await());
    barrier.unblock();
  }

  @Test
  public void testExpired() throws ProcessingException {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, 1, TimeUnit.SECONDS, Jobs.newInput(RunContexts.empty()).expirationTime(500, TimeUnit.MILLISECONDS));

    try {
      assertNull(future.awaitDoneAndGet());
      assertTrue(future.isCancelled());
      assertFalse(executed.get());
    }
    catch (JobExecutionException e) {
      fail();
    }
  }

  @Test
  public void testExpireNever() throws ProcessingException {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, 1, TimeUnit.SECONDS, Jobs.newInput(RunContexts.empty()).expirationTime(JobInput.INFINITE_EXPIRATION, TimeUnit.MILLISECONDS));

    future.awaitDoneAndGet();
    assertTrue(executed.get());
  }

  @Test
  public void testScheduleDelayed() throws ProcessingException {
    final AtomicLong tRunning = new AtomicLong();

    long tScheduled = System.nanoTime();
    String result = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        tRunning.set(System.nanoTime());
        return "executed";
      }
    }, 2, TimeUnit.SECONDS, Jobs.newInput(RunContexts.empty())).awaitDoneAndGet(5, TimeUnit.SECONDS);

    assertEquals("executed", result);
    long delta = tRunning.get() - tScheduled;
    assertTrue(delta >= TimeUnit.SECONDS.toNanos(2));
  }

  @Test
  public void testScheduleDelayedWithMutex() throws ProcessingException {
    final Object mutex = new Object();

    IFuture<String> future1 = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        Thread.sleep(2000);
        return "job-1";
      }
    }, Jobs.newInput(RunContexts.empty()).mutex(mutex));

    IFuture<String> future2 = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        return "job-2";
      }
    }, 500, TimeUnit.MILLISECONDS, Jobs.newInput(RunContexts.empty()).mutex(mutex));

    assertEquals("job-2", future2.awaitDoneAndGet(10, TimeUnit.SECONDS));
    assertEquals("job-1", future1.awaitDoneAndGet());
  }

  @Test
  public void testScheduleWithTimeoutWithMutex() throws ProcessingException {
    final Object mutex = new Object();

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        latch.countDownAndBlock();
        return "job-1";
      }
    }, Jobs.newInput(RunContexts.empty()).mutex(mutex));

    IFuture<String> future2 = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        return "job-2";
      }
    }, Jobs.newInput(RunContexts.empty()).mutex(mutex));

    try {
      assertEquals("job-2", future2.awaitDoneAndGet(2, TimeUnit.SECONDS));
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isTimeout());
    }

    latch.unblock();
  }

  @Test
  public void testMissingJobInput() throws JobExecutionException {
    final AtomicReference<Boolean> running = new AtomicReference<Boolean>(false);

    IFuture<Void> future = null;
    try {
      future = m_jobManager.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          running.set(true);
        }
      }, null);
      fail();
    }
    catch (AssertionException e) {
      assertFalse(running.get());
      assertNull(future);
    }
  }

}
