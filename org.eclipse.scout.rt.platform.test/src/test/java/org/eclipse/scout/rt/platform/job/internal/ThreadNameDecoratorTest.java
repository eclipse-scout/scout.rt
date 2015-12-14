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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
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
    assertTrue("actual=" + actualThreadName.get(), actualThreadName.get().matches("test-thread-\\d+ \\(RUNNING\\) 'job-1'"));
  }

  @Test
  @Times(50)
  // This test is executed 50 times (regression)
  public void testThreadNameWithStateChange() throws Exception {
    final IMutex mutex = Jobs.newMutex();
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    final Holder<Thread> workerThreadJob1Holder = new Holder<>();
    final Holder<ThreadInfo> threadInfoJob1Holder = new Holder<>();

    // Job-1 (same mutex as job-2)
    IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        workerThreadJob1Holder.setValue(Thread.currentThread());
        threadInfoJob1Holder.setValue(ThreadInfo.CURRENT.get());

        // verify job-status is 'RUNNING'.
        String currentThreadName = Thread.currentThread().getName();
        assertTrue("actual=" + currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\(RUNNING\\) 'job-1'"));

        // Start blocking
        condition.waitFor();

        // verify job-status is 'RUNNING'.
        currentThreadName = Thread.currentThread().getName();
        assertTrue("actual=" + currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\(RUNNING\\) 'job-1'"));
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1")
        .withMutex(mutex));

    // Job-2 (same mutex as job-1)
    IFuture<Void> future2 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // verify job1 to be in blocked state.
        String threadNameJob1 = workerThreadJob1Holder.getValue().getName();
        assertTrue("actual=" + threadNameJob1, threadNameJob1.matches("scout-thread-\\d+ \\(WAITING_FOR_BLOCKING_CONDITION\\) 'job-1'"));

        // Release job-1
        condition.setBlocking(false);

        // Wait until job-1 is competing for the mutex anew.
        JobTestUtil.waitForMutexCompetitors(mutex, 2);

        // verify job1 to be in resume state.
        threadNameJob1 = workerThreadJob1Holder.getValue().getName();
        assertTrue("actual=" + threadNameJob1, threadNameJob1.matches("scout-thread-\\d+ \\(WAITING_FOR_MUTEX\\) 'job-1'"));
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1")
        .withMutex(mutex));

    future2.awaitDoneAndGet(10, TimeUnit.SECONDS);
    future1.awaitDoneAndGet(10, TimeUnit.SECONDS);
  }
}
