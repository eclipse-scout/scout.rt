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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AwaitDoneTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  @Test
  public void testAwaitAllDone() {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("run-1");
      }
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    future.awaitDone(10, TimeUnit.SECONDS);
    assertEquals(CollectionUtility.hashSet("run-1"), protocol);
    assertTrue(future.isDone());
  }

  @Test
  public void testAwaitFutureDone1() {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final IFuture<Void> future1 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("run-1");
      }
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertEquals(CollectionUtility.hashSet("run-1"), protocol);
    assertTrue(Jobs.getJobManager().isDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1)
        .toFilter()));
    assertTrue(Jobs.getJobManager().isDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1)
        .toFilter()));
  }

  @Test
  public void testAwaitFutureDone2() {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latchJob2 = new BlockingCountDownLatch(1);

    final IFuture<Void> future1 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("run-1");
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionHint(JOB_IDENTIFIER));

    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latchJob2.await();
        protocol.add("run-2");
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertTrue(Jobs.getJobManager().isDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1)
        .toFilter()));
    assertFalse(Jobs.getJobManager().isDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter()));
    try {
      Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
          .andMatchExecutionHint(JOB_IDENTIFIER)
          .toFilter(), 500, TimeUnit.MILLISECONDS);
      fail("timeout expected");
    }
    catch (TimedOutError e) {
      // NOOP
    }
    assertEquals(CollectionUtility.hashSet("run-1"), protocol);

    latchJob2.countDown();
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertTrue(Jobs.getJobManager().isDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter()));
    assertEquals(CollectionUtility.hashSet("run-1", "run-2"), protocol);
  }

  @Test
  public void testAwaitDoneOrWaiting() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("before-1");
        condition.waitFor();
        protocol.add("after-2");
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    try {
      Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
          .andMatchFuture(future)
          .toFilter(), 100, TimeUnit.MILLISECONDS);
      fail("timeout expected");
    }
    catch (TimedOutError e) {
      // NOOP
    }

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .andMatchNotState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter(), 10, TimeUnit.SECONDS);

    assertEquals(Arrays.asList("before-1"), protocol);

    // Cleanup
    condition.setBlocking(false);
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);

  }

  /**
   * Tests that 'JobManager.awaitDone' returns once the Future is cancelled, even if that job is still runnning.
   */
  @Test
  public void testAwaitDoneWithCancelledJob() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch continueRunningLatch = new BlockingCountDownLatch(1);

    final IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          Thread.interrupted(); // ensure the thread's interrupted status to be cleared in order to continue the test.
          continueRunningLatch.countDownAndBlock(); // continue running
        }
      }
    }, Jobs.newInput());

    assertTrue(setupLatch.await());

    // cancel the Future in 1 second
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // run the test.
        future.cancel(true);
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)));

    // start waiting for the job to complete or until cancelled
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 5, TimeUnit.SECONDS);

    continueRunningLatch.release();
  }
}
