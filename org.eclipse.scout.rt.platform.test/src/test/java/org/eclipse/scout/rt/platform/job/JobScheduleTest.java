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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobScheduleTest {

  @Test
  public void testWithCallable() {
    IFuture<String> future = Jobs.getJobManager().schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "running";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // VERIFY
    assertEquals("running", future.awaitDoneAndGet());
    assertTrue(future.isDone());
  }

  @Test
  public void testWithRunnable() {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running");
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // VERIFY
    assertNull(future.awaitDoneAndGet());
    assertEquals(CollectionUtility.hashSet("running"), protocol);
    assertTrue(future.isDone());
  }

  @Test
  public void testProcessingExceptionWithRunnable() {
    final ProcessingException exception = new ProcessingException();

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

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
  public void testProcessingExceptionWithCallable() {
    final ProcessingException exception = new ProcessingException();

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

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
  public void testRuntimeExceptionWithRunnable() {
    final RuntimeException exception = new RuntimeException();

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

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
  public void testRuntimeExceptionWithCallable() {
    final RuntimeException exception = new RuntimeException();

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

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
  public void testExceptionExceptionWithRunnable() {
    final Exception exception = new Exception();

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

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
  public void testExceptionExceptionWithCallable() {
    final Exception exception = new Exception();

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

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
  public void testWorkerThread() {
    final Set<Thread> protocol = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(Thread.currentThread());

        Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add(Thread.currentThread());
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent()))
            .awaitDoneAndGet();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()))
        .awaitDoneAndGet();

    assertEquals(2, protocol.size());
    assertFalse(protocol.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());

        Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent())
            .withName("XYZ"))
            .awaitDoneAndGet();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("ABC"))
        .awaitDoneAndGet();
    assertTrue(actualThreadName1.getValue(), actualThreadName1.getValue().matches("scout-thread-(\\d)+ \\(Running\\) \"ABC\""));
    assertTrue(actualThreadName2.getValue(), actualThreadName2.getValue().matches("scout-thread-(\\d)+ \\(Running\\) \"XYZ\""));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentFuture() {
    final Holder<IFuture<?>> expectedFuture1 = new Holder<>();
    final Holder<IFuture<?>> expectedFuture2 = new Holder<>();

    final Holder<IFuture<?>> actualFuture1 = new Holder<>();
    final Holder<IFuture<?>> actualFuture2 = new Holder<>();

    IFuture.CURRENT.remove();

    expectedFuture1.setValue(Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFuture1.setValue(IFuture.CURRENT.get());

        expectedFuture2.setValue(Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualFuture2.setValue(IFuture.CURRENT.get());
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent())));

        expectedFuture2.getValue().awaitDoneAndGet(); // wait for the job to complete
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())));

    expectedFuture1.getValue().awaitDoneAndGet(); // wait for the job to complete

    assertNotNull(expectedFuture1.getValue());
    assertNotNull(expectedFuture2.getValue());

    assertSame(expectedFuture1.getValue(), actualFuture1.getValue());
    assertSame(expectedFuture2.getValue(), actualFuture2.getValue());

    assertNull(IFuture.CURRENT.get());
  }

  @Test
  public void testScheduleAndGet() {
    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(1);
      }
    }, Jobs.newInput()).awaitDoneAndGet();
    protocol.add(2);

    assertEquals(Arrays.asList(1, 2), protocol);
  }

  @Test
  public void testParallelExecution() throws Exception {
    final BlockingCountDownLatch barrier = new BlockingCountDownLatch(3);

    IFuture<Void> future1 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future2 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future3 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    assertTrue(barrier.await());
    barrier.unblock();

    // wait for all jobs to complete
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1, future2, future3)
        .toFilter(), 10, TimeUnit.SECONDS));
  }

  @Test
  public void testExpired() {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, Jobs.newInput()
        .withSchedulingDelay(1, TimeUnit.SECONDS)
        .withExpirationTime(500, TimeUnit.MILLISECONDS)
        .withRunContext(RunContexts.empty()));

    future.awaitDone();
    assertTrue(future.isCancelled());
    assertFalse(executed.get());
  }

  @Test
  public void testExpireNever() {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, Jobs.newInput()
        .withSchedulingDelay(1, TimeUnit.SECONDS)
        .withRunContext(RunContexts.empty())
        .withExpirationTime(JobInput.INFINITE_EXPIRATION, TimeUnit.MILLISECONDS));

    future.awaitDoneAndGet();
    assertTrue(executed.get());
  }

  @Test
  public void testScheduleDelayed() {
    final AtomicLong tRunning = new AtomicLong();

    long tScheduled = System.nanoTime();
    String result = Jobs.getJobManager().schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        tRunning.set(System.nanoTime());
        return "executed";
      }
    }, Jobs.newInput()
        .withSchedulingDelay(2, TimeUnit.SECONDS)
        .withRunContext(RunContexts.empty()))
        .awaitDoneAndGet(5, TimeUnit.SECONDS);

    assertEquals("executed", result);
    long delta = tRunning.get() - tScheduled;
    assertTrue(delta >= TimeUnit.SECONDS.toNanos(2));
  }

  @Test
  public void testScheduleDelayedWithMutex() {
    final IMutex mutex = Jobs.newMutex();

    IFuture<String> future1 = Jobs.getJobManager().schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "job-1";
      }
    }, Jobs.newInput().withMutex(mutex));

    IFuture<String> future2 = Jobs.getJobManager().schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "job-2";
      }
    }, Jobs.newInput()
        .withSchedulingDelay(500, TimeUnit.MILLISECONDS)
        .withMutex(mutex));

    assertEquals("job-2", future2.awaitDoneAndGet(10, TimeUnit.SECONDS));
    assertEquals("job-1", future1.awaitDoneAndGet());
  }

  @Test
  public void testScheduleWithTimeoutWithMutex() {
    final IMutex mutex = Jobs.newMutex();

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    IFuture<String> future1 = Jobs.getJobManager().schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        latch.countDownAndBlock();
        return "job-1";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withMutex(mutex)
        .withExceptionHandling(null, false));

    IFuture<String> future2 = Jobs.getJobManager().schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "job-2";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withMutex(mutex));

    try {
      assertEquals("job-2", future2.awaitDoneAndGet(2, TimeUnit.SECONDS));
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isTimeout());
    }

    latch.unblock();

    // wait for all jobs to complete
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1, future2)
        .toFilter(), 10, TimeUnit.SECONDS));
  }

  @Test
  public void testMissingJobInput() {
    final AtomicReference<Boolean> running = new AtomicReference<Boolean>(false);

    IFuture<Void> future = null;
    try {
      future = Jobs.getJobManager().schedule(new IRunnable() {

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
