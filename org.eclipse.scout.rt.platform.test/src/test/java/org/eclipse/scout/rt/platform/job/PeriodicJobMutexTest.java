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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PeriodicJobMutexTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  @Test
  public void testAtFixedRate() {
    testInternal(Jobs.newInput()
        .withPeriodicExecutionAtFixedRate(100, TimeUnit.MILLISECONDS)
        .withSchedulingSemaphore(Jobs.newSchedulingSemaphore(1)));
  }

  @Test
  public void testWithFixedDelay() {
    testInternal(Jobs.newInput()
        .withPeriodicExecutionWithFixedDelay(100, TimeUnit.MILLISECONDS)
        .withSchedulingSemaphore(Jobs.newSchedulingSemaphore(1)));
  }

  /**
   * This test schedules a job according to the given input.
   * <p>
   * After 2 iterations, the periodic job is cancelled. While running, this job is checked to be the mutex owner. Then,
   * another job with the same mutex is scheduled and awaited for (timeout expected because mutual exclusion). And
   * finally, another job with the same mutex is scheduled, which is expected to be run after the timed out job. Both
   * that jobs are expected to be run right after one iterations completes.
   */
  private void testInternal(final JobInput periodicJobInput) {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final AtomicInteger rounds = new AtomicInteger(0);

    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // cancel after 2 iterations
        if (rounds.getAndIncrement() == 2) {
          IFuture.CURRENT.get().cancel(false);
          return;
        }

        protocol.add("begin");

        // This task should be mutex-owner
        IFuture<?> currentFuture = IFuture.CURRENT.get();
        ISchedulingSemaphore mutex = currentFuture.getSchedulingSemaphore();
        if (mutex != null && mutex.isPermitOwner(currentFuture)) {
          protocol.add("mutex-owner");
        }

        // Schedule other job with same mutex, and wait for it to complete.
        // expected: that job must not commence execution until the periodic job completes this iteration
        // However, to circumvent deadlock-detection, schedule this job within another job, that is not mutex owner
        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              Jobs.schedule(new IRunnable() {

                @Override
                public void run() throws Exception {
                  protocol.add("running-2");
                }
              }, Jobs.newInput()
                  .withSchedulingSemaphore(periodicJobInput.getSchedulingSemaphore())
                  .withExecutionHint(JOB_IDENTIFIER))
                  .awaitDone(200, TimeUnit.MILLISECONDS);
            }
            catch (TimeoutException e) {
              protocol.add("timeout-because-mutex-owner");
            }
          }
        }, Jobs.newInput()
            .withExecutionHint(JOB_IDENTIFIER))
            .awaitDone();

        // Schedule other job with same mutex
        // expected: must only commence execution once this iteration completes.
        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add("running-3");
          }
        }, Jobs.newInput()
            .withSchedulingSemaphore(periodicJobInput.getSchedulingSemaphore())
            .withExecutionHint(JOB_IDENTIFIER));

        protocol.add("end");
      }
    }, periodicJobInput.copy()
        .withSchedulingDelay(200, TimeUnit.MILLISECONDS) // schedule delayed
        .withExecutionHint(JOB_IDENTIFIER));

    // Schedule other job
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("other job");
      }
    }, Jobs.newInput()
        .withSchedulingSemaphore(periodicJobInput.getSchedulingSemaphore()));

    // Wait for the job to complete
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 10, TimeUnit.SECONDS);

    List<String> expected = new ArrayList<String>();
    expected.add("other job");

    expected.add("begin");
    expected.add("mutex-owner");
    expected.add("timeout-because-mutex-owner");
    expected.add("end");
    expected.add("running-2");
    expected.add("running-3");

    expected.add("begin");
    expected.add("mutex-owner");
    expected.add("timeout-because-mutex-owner");
    expected.add("end");
    expected.add("running-2");
    expected.add("running-3");

    assertEquals(expected, protocol);
  }
}
