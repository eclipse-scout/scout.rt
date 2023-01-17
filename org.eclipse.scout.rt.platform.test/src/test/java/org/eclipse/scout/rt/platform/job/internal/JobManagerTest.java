/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class JobManagerTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    // Use dedicated job manager because job manager is shutdown in tests.
    m_jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JobManager() {
      // must be a subclass in order to replace JobManager
    });
  }

  @After
  public void after() {
    JobTestUtil.unregisterAndShutdownJobManager(m_jobManagerBean);
  }

  @Test
  public void testUnregisterWhenFinished() {
    IFuture<Void> future = Jobs.schedule(() -> {
      // NOOP
    }, Jobs.newInput());
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test
  public void testUnregisterWhenCancelledDuringExecution() throws InterruptedException {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.schedule(() -> {
      latch.countDownAndBlock();
    }, Jobs.newInput());

    latch.await();
    future.cancel(false);
    latch.unblock();
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test
  public void testUnregisterWhenCancelledBeforeExecution() {
    IFuture<Void> future = Jobs.schedule(() -> {
      // NOOP
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.HOURS)));
    future.cancel(false);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test
  public void testRepetitiveJobsFinishNormally1() {
    IFuture<Void> future = Jobs.schedule(() -> {
      // NOOP
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(100, 1, TimeUnit.MILLISECONDS))));
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test

  public void testRepetitiveJobsFinishNormally2() {
    IFuture<Void> future = Jobs.schedule(() -> {
      // NOOP
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .withRepeatCount(100))));
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test
  public void testRepetitiveJobsCancelled1() {
    IFuture<Void> future = Jobs.schedule(() -> {
      IFuture.CURRENT.get().cancel(true);
      // NOOP
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(100, 1, TimeUnit.MILLISECONDS))));
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test

  public void testRepetitiveJobsCancelled2() {
    IFuture<Void> future = Jobs.schedule(() -> {
      IFuture.CURRENT.get().cancel(true);
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .withRepeatCount(100))));
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }
}
