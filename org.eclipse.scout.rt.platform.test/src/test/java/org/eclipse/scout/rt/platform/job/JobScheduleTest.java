/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.*;

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

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobScheduleTest {

  @Test
  public void testWithCallable() {
    IFuture<String> future = Jobs.getJobManager().schedule(() -> "running", Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // VERIFY
    assertEquals("running", future.awaitDoneAndGet());
    assertTrue(future.isDone());
  }

  @Test
  public void testWithRunnable() {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.
    IFuture<Void> future = Jobs.getJobManager().schedule(() -> {
      protocol.add("running");
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // VERIFY
    assertNull(future.awaitDoneAndGet());
    assertEquals(CollectionUtility.hashSet("running"), protocol);
    assertTrue(future.isDone());
  }

  @Test
  public void testProcessingExceptionWithRunnable() {
    final ProcessingException exception = new ProcessingException("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw exception;
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
    final ProcessingException exception = new ProcessingException("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw exception;
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
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw exception;
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testRuntimeExceptionWithCallable() {
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw exception;
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    try {
      future.awaitDoneAndGet();
      fail("Exception expected");
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testExceptionExceptionWithRunnable() {
    final Exception exception = new Exception("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw exception;
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    try {
      future.awaitDoneAndGet();
      fail("PlatformException expected");
    }
    catch (PlatformException e) {
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testExceptionExceptionWithCallable() {
    final Exception exception = new Exception("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw exception;
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    try {
      future.awaitDoneAndGet();
      fail("PlatformException expected");
    }
    catch (PlatformException e) {
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test()
  public void testErrorWithRunnable() {
    final Error error = new Error("expected JUnit test exception");

    IFuture<Void> future = Jobs.getJobManager().schedule((IRunnable) () -> {
      throw error;
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    try {
      future.awaitDoneAndGet(5, TimeUnit.SECONDS); // test with timeout in case the error is not propagated
      fail("Exception expected");
    }
    catch (Error e) {
      assertSame(e, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testWorkerThread() {
    final Set<Thread> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.

    Jobs.getJobManager().schedule(() -> {
          protocol.add(Thread.currentThread());

          Jobs.getJobManager().schedule(() -> {
                protocol.add(Thread.currentThread());
              }, Jobs.newInput()
                  .withRunContext(RunContexts.copyCurrent()))
              .awaitDoneAndGet();
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent()))
        .awaitDoneAndGet();

    assertEquals(2, protocol.size());
    assertFalse(protocol.contains(Thread.currentThread()));
  }

  @Test
  public void testCurrentFuture() {
    final Holder<IFuture<?>> expectedFuture1 = new Holder<>();
    final Holder<IFuture<?>> expectedFuture2 = new Holder<>();

    final Holder<IFuture<?>> actualFuture1 = new Holder<>();
    final Holder<IFuture<?>> actualFuture2 = new Holder<>();

    IFuture.CURRENT.remove();

    expectedFuture1.setValue(Jobs.getJobManager().schedule(() -> {
      actualFuture1.setValue(IFuture.CURRENT.get());

      expectedFuture2.setValue(Jobs.getJobManager().schedule(() -> actualFuture2.setValue(IFuture.CURRENT.get()), Jobs.newInput()
          .withRunContext(RunContexts.copyCurrent())));

      expectedFuture2.getValue().awaitDoneAndGet(); // wait for the job to complete
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
    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<>()); // synchronized because modified/read by different threads.

    Jobs.getJobManager().schedule(() -> {
      protocol.add(1);
    }, Jobs.newInput()).awaitDoneAndGet();
    protocol.add(2);

    assertEquals(Arrays.asList(1, 2), protocol);
  }

  @Test
  public void testParallelExecution() throws Exception {
    final BlockingCountDownLatch barrier = new BlockingCountDownLatch(3);

    IFuture<Void> future1 = Jobs.getJobManager().schedule(() -> {
      barrier.countDownAndBlock();
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future2 = Jobs.getJobManager().schedule(() -> {
      barrier.countDownAndBlock();
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future3 = Jobs.getJobManager().schedule(() -> {
      barrier.countDownAndBlock();
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    assertTrue(barrier.await());
    barrier.unblock();

    // wait for all jobs to complete
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1, future2, future3)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test
  public void testExpired() {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = Jobs.getJobManager().schedule(() -> executed.set(true), Jobs.newInput()
        .withExpirationTime(500, TimeUnit.MILLISECONDS)
        .withRunContext(RunContexts.empty())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)));

    future.awaitDone();
    assertTrue(future.isCancelled());
    assertFalse(executed.get());
  }

  @Test
  public void testExpireNever() {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = Jobs.getJobManager().schedule(() -> executed.set(true), Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExpirationTime(JobInput.EXPIRE_NEVER, TimeUnit.MILLISECONDS)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)));

    future.awaitDoneAndGet();
    assertTrue(executed.get());
  }

  @Test
  public void testScheduleDelayed() {
    long tolerance = 50;
    final AtomicLong tRunning = new AtomicLong();

    long tScheduled = System.currentTimeMillis();
    String result = Jobs.getJobManager().schedule(() -> {
          tRunning.set(System.currentTimeMillis());
          return "executed";
        }, Jobs.newInput()
            .withExecutionTrigger(Jobs.newExecutionTrigger()
                .withStartIn(2, TimeUnit.SECONDS)))
        .awaitDoneAndGet(5, TimeUnit.SECONDS);

    assertEquals("executed", result);
    long delta = tRunning.get() - tScheduled;
    assertTrue(delta >= TimeUnit.SECONDS.toMillis(2) - tolerance);
  }

  @Test
  public void testScheduleDelayedWithMutex() {
    final IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    IFuture<String> future1 = Jobs.getJobManager().schedule(() -> "job-1", Jobs.newInput().withExecutionSemaphore(mutex));

    IFuture<String> future2 = Jobs.getJobManager().schedule(() -> "job-2", Jobs.newInput()
        .withExecutionSemaphore(mutex)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(500, TimeUnit.MILLISECONDS)));

    assertEquals("job-2", future2.awaitDoneAndGet(10, TimeUnit.SECONDS));
    assertEquals("job-1", future1.awaitDoneAndGet());
  }

  @Test
  public void testScheduleWithTimeoutWithMutex() {
    final IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    IFuture<String> future1 = Jobs.getJobManager().schedule(() -> {
      latch.countDownAndBlock();
      return "job-1";
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExecutionSemaphore(mutex)
        .withExceptionHandling(null, false));

    IFuture<String> future2 = Jobs.getJobManager().schedule(() -> "job-2", Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExecutionSemaphore(mutex));

    try {
      assertEquals("job-2", future2.awaitDoneAndGet(2, TimeUnit.SECONDS));
      fail("TimeoutException expected");
    }
    catch (TimedOutError e) {
      // NOOP
    }

    latch.unblock();

    // wait for all jobs to complete
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1, future2)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test
  public void testMissingJobInput() {
    final AtomicReference<Boolean> running = new AtomicReference<>(false);

    IFuture<Void> future = null;
    try {
      future = Jobs.getJobManager().schedule(() -> running.set(true), null);
      fail();
    }
    catch (AssertionException e) {
      assertFalse(running.get());
      assertNull(future);
    }
  }

  @Test
  public void testScheduleJobWithCancelledRunMonitor() {
    // setup run context with already cancelled run monitor
    RunContext runContext = RunContexts.empty();
    runContext.getRunMonitor().cancel(true);

    // schedule job
    IFuture<Void> future = Jobs.schedule(() -> fail("job must not be executed"), Jobs.newInput().withRunContext(runContext));
    future.awaitDone();

    assertTrue(future.isCancelled());

    // future must not be referenced by job manager
    assertEquals(0, Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder().andMatchFuture(future).toFilter()).size());
  }
}
