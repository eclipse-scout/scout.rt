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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.ISchedulingSemaphore;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  @Test
  public void testThreadName() throws Exception {
    final AtomicReference<String> actualThreadName = new AtomicReference<>();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualThreadName.set(Thread.currentThread().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withThreadName("test-thread")
        .withName("job-1"));

    future.awaitDone();
    assertTrue("actual=" + actualThreadName.get(), actualThreadName.get().matches("test-thread-\\d+ 'job-1'"));
  }

  @Test
  @Times(100) // regression
  public void testThreadNameWithStateChange() throws Exception {
    final ISchedulingSemaphore mutex = Jobs.newSchedulingSemaphore(1);
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    final AtomicReference<Thread> future1WorkerThreadHolder = new AtomicReference<>();

    // Job-1 (same mutex as job-2)
    final IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        future1WorkerThreadHolder.set(Thread.currentThread());

        // verify job-status is 'RUNNING'.
        String currentThreadName = Thread.currentThread().getName();
        assertTrue("actual=" + currentThreadName, currentThreadName.matches("scout-thread-\\d+ 'job-1'"));

        // Start blocking
        condition.waitFor();

        // verify job-status is 'RUNNING'.
        currentThreadName = Thread.currentThread().getName();
        assertTrue("actual=" + currentThreadName, currentThreadName.matches("scout-thread-\\d+ 'job-1'"));
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1")
        .withSchedulingSemaphore(mutex));

    // Job-2 (same mutex as job-1)
    IFuture<Void> future2 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          // verify job1 to be in blocked state.
          String threadNameJob1 = future1WorkerThreadHolder.get().getName();
          assertTrue("actual=" + threadNameJob1, threadNameJob1.matches("scout-thread-\\d+ \\(WAITING_FOR_BLOCKING_CONDITION\\) 'job-1'"));
        }
        finally {
          // Release job-1
          condition.setBlocking(false);

          // Wait until job-1 is competing for the mutex anew.
          waitForUpdatedThreadName(future1, future1WorkerThreadHolder.get(), JobState.WAITING_FOR_PERMIT); // because job-1 asynchronously acquires the mutex

          // verify job1 to be in resume state.
          String threadNameJob1 = future1WorkerThreadHolder.get().getName();
          assertTrue("actual=" + threadNameJob1, threadNameJob1.matches("scout-thread-\\d+ \\(WAITING_FOR_PERMIT\\) 'job-1'"));
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-2")
        .withSchedulingSemaphore(mutex));

    // Wait until completed and propagate assertions
    future1.awaitDoneAndGet(10, TimeUnit.SECONDS);
    future2.awaitDoneAndGet(10, TimeUnit.SECONDS);
  }

  /**
   * Waits until the thread-name is updated with the given state.
   */
  private static void waitForUpdatedThreadName(IFuture<?> future, Thread thread, JobState state) {
    JobTestUtil.waitForState(future, state);

    // Wait for the thread-name to be changed. That is, because the state is set before the name is changed.
    final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
    while (!thread.getName().contains(state.name())) {
      if (System.currentTimeMillis() > deadline) {
        fail("Timeout elapsed while waiting for a job to change its name.");
      }
      Thread.yield();
    }
  }
}
