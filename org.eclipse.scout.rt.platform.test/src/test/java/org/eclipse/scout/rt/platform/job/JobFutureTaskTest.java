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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.testing.platform.testcategory.SlowTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SlowTest.class)
public class JobFutureTaskTest {
  private static final int ROUNDS = 100000;
  private static final String TEST_HINT = "JobFutureTaskTest_testRaceConditionOnCancelWithJobManager";

  /**
   * Test that cancelling a Job will not interrupt a wrong Thread. The test only runs for a short time, so the
   * reliability is low.
   * <p>
   * The parameter {@link #ROUNDS} is set to a reasonable low value in order to have a fast test run.
   * <p>
   * However, to have a more reliable test result, set {@link #ROUNDS} to 100000000.
   */
  @Test
  public void testRaceConditionOnCancel() throws InterruptedException {
    Semaphore scheduleLock = new Semaphore(100, true);
    AtomicInteger failureCount = new AtomicInteger();
    AtomicBoolean active = new AtomicBoolean(true);
    Random rnd = new Random(1234L);

    //progress
    AtomicInteger runCount = new AtomicInteger();
    Jobs.schedule(() -> {
      long t0 = System.currentTimeMillis();
      while (active.get()) {
        SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
        long c = runCount.get();
        long t1 = System.currentTimeMillis();
        System.out.println("Run: " + c + " [" + (1000L * c / (t1 - t0)) + " run/sec]");
      }
    }, Jobs.newInput().withExecutionHint(TEST_HINT));

    try {
      for (int i = 0; i < ROUNDS; i++) {
        scheduleLock.acquire(1);

        IFuture<String> f = Jobs.schedule(() -> {
          SleepUtil.sleepSafe(1, TimeUnit.MILLISECONDS);
          return "FOO";
        }, Jobs.newInput().withExecutionHint(TEST_HINT));

        Jobs.schedule(() -> {
          //noinspection ResultOfMethodCallIgnored
          Thread.interrupted();//clear state
          try {
            Thread.sleep(0L, rnd.nextInt(100000));
          }
          catch (InterruptedException e) {
            failureCount.incrementAndGet();
            System.out.println("FAILURE: 'f.cancel(true)' cancelled the wrong thread (sleep)");
          }
          try {
            f.cancel(true);
            String s = f.awaitDoneAndGet();
            if (s == null) {
              failureCount.incrementAndGet();
              System.out.println("FAILURE: value is null");
            }
          }
          catch (FutureCancelledError | ThreadInterruptedError e) {
            //nop
          }
          catch (Throwable t) {
            t.printStackTrace();
          }
          runCount.incrementAndGet();
          scheduleLock.release(1);
        }, Jobs.newInput().withExecutionHint(TEST_HINT));
      }
    }
    finally {
      active.set(false);
      Predicate<IFuture<?>> filter = Jobs.newFutureFilterBuilder().andMatchExecutionHint(TEST_HINT).toFilter();
      Jobs.getJobManager().awaitFinished(filter, 1, TimeUnit.MINUTES);
      Jobs.getJobManager().cancel(filter, true);
    }
    Assertions.assertEqual(0, failureCount.get());
  }

  @Test(expected = FutureCancelledError.class)
  public void testCancelOfRunMonitorCancelsFuture() {
    RunContext runContext = RunContexts.empty().withRunMonitor(new RunMonitor() {

      @Override
      protected List<ICancellable> getCancellables() {
        return Collections.emptyList(); // simulate there are NO cancellables to cancel, especially not the JobFutureTask that indirectly references this RunMonitor
      }
    });
    runContext.getRunMonitor().cancel(false);

    IFuture<String> f = Jobs.schedule(() -> {
      System.out.println("Run");
      return "done";
    }, Jobs.newInput().withRunContext(runContext));
    String result = f.awaitDoneAndGet();
    System.out.println("Result: " + result);
  }
}
