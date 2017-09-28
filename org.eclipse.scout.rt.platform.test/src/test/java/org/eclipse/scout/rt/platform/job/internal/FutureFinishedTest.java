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
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FutureFinishedTest {

  @Test
  public void testCancelRunningJob() throws InterruptedException {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latch.countDownAndBlock();
      }
    }, Jobs.newInput());

    // Job still running
    assertTrue(latch.await());
    assertDone(future, false);
    assertFinished(future, false);

    // Cancel the job
    future.cancel(false);

    // Job cancelled, but not finished yet
    assertDone(future, true);
    assertFinished(future, false);

    // Let the job finish
    latch.unblock();
    future.awaitFinished(10, TimeUnit.SECONDS);
    assertDone(future, true);
    assertFinished(future, true);
  }

  @Test
  public void testNormalCompletion() throws InterruptedException {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latch.countDownAndBlock();
      }
    }, Jobs.newInput());

    // Job still running
    assertTrue(latch.await());
    assertDone(future, false);
    assertFinished(future, false);

    // Let the job finish
    latch.unblock();

    // Job cancelled, but not finished yet
    future.awaitFinished(10, TimeUnit.SECONDS);
    assertDone(future, true);
    assertFinished(future, true);
  }

  @Test
  public void testCancelledBeforeRunning() throws InterruptedException {
    final AtomicBoolean run = new AtomicBoolean(false);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        run.set(true);
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(2, TimeUnit.SECONDS)));
    future.cancel(false);

    // Job expected to be done and finished immediately
    assertDone(future, true);
    assertFinished(future, true);
  }

  @Test
  public void testNotifyWaitingThreads() throws Throwable {
    final String jobIdentifier = UUID.randomUUID().toString();
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDown();

        try {
          Jobs.getJobManager().awaitFinished(Jobs.newFutureFilterBuilder()
              .andMatchExecutionHint(jobIdentifier)
              .toFilter(), 10, TimeUnit.SECONDS);
        }
        catch (TimedOutError e) {
          fail("no timeout expected");
        }
      }
    }, Jobs.newInput());

    assertTrue(setupLatch.await());
    // Wait some time, so that the listener in controller is installed
    SleepUtil.sleepSafe(2, TimeUnit.SECONDS);

    // Run the test
    IFuture<Void> future = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());

    // verify
    future.awaitFinished(10, TimeUnit.SECONDS); // no exception expected
    controller.awaitDoneAndGet(10, TimeUnit.SECONDS); // no exception expected
  }

  private static void assertDone(IFuture<?> future, boolean expectedDone) {
    if (expectedDone) {
      assertTrue(future.isDone());
      future.awaitDone(1, TimeUnit.NANOSECONDS);
    }
    else {
      assertFalse(future.isDone());
      try {
        future.awaitDone(1, TimeUnit.NANOSECONDS);
        fail("timeout expected");
      }
      catch (TimedOutError e) {
        // NOOP
      }
    }
  }

  private static void assertFinished(IFuture<?> future, boolean expectedFinished) {
    if (expectedFinished) {
      assertTrue(future.isFinished());
      future.awaitFinished(1, TimeUnit.NANOSECONDS);
      Jobs.getJobManager().awaitFinished(Jobs.newFutureFilterBuilder()
          .andMatchFuture(future)
          .toFilter(), 1, TimeUnit.NANOSECONDS);

    }
    else {
      assertFalse(future.isFinished());
      try {
        future.awaitFinished(1, TimeUnit.NANOSECONDS);
        fail("timeout expected");
      }
      catch (TimedOutError e) {
        // NOOP
      }
      try {
        Jobs.getJobManager().awaitFinished(Jobs.newFutureFilterBuilder()
            .andMatchFuture(future)
            .toFilter(), 1, TimeUnit.NANOSECONDS);
        fail("timeout expected");
      }
      catch (TimedOutError e) {
        // NOOP
      }
    }
  }
}
