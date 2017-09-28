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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
    }, Jobs.newInput());
    future.awaitFinished(10, TimeUnit.SECONDS);

    // Assert no futures left
    JobManager jobManager = (JobManager) Jobs.getJobManager();
    assertEquals(0, jobManager.getFutures(null).size());
  }

  @Test
  public void testUnregisterWhenCancelledDuringExecution() throws InterruptedException {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latch.countDownAndBlock();
      }
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        IFuture.CURRENT.get().cancel(true);
        // NOOP
      }
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        IFuture.CURRENT.get().cancel(true);
      }
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
