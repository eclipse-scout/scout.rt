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
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobFutureVisitTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  private IExecutionSemaphore m_mutex1;
  private IExecutionSemaphore m_mutex2;
  private IExecutionSemaphore m_mutex3;

  private Set<String> protocol;

  private BlockingCountDownLatch m_latch;

  private IBlockingCondition bc1;
  private IBlockingCondition bc2;

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() throws InterruptedException {
    m_jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JobManager() {
    });

    m_mutex1 = Jobs.newExecutionSemaphore(1);
    m_mutex2 = Jobs.newExecutionSemaphore(1);
    m_mutex3 = Jobs.newExecutionSemaphore(1);

    // prepare the test-case
    protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    bc1 = Jobs.newBlockingCondition(true);
    bc2 = Jobs.newBlockingCondition(true);

    m_latch = new BlockingCountDownLatch(3);

    // SESSION 1 (JOB-1)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc1.waitFor();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex1_job1")
        .withExecutionSemaphore(m_mutex1)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 1 (JOB-2)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        m_latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex1_job2")
        .withExecutionSemaphore(m_mutex1)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 1 (JOB-3)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex1_job3")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(m_mutex1));

    // =========
    // SESSION 2 (JOB-1)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job1")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(m_mutex2));

    // SESSION 2 (JOB-2)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc2.waitFor();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job2")
        .withExecutionSemaphore(m_mutex2)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 2  (JOB-3)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc2.setBlocking(false);

        JobTestUtil.waitForPermitCompetitors(m_mutex2, 3); // Wait until job 'mutex2_job2' is re-acquiring the mutex. [3=job-2, job-3, job-4]

        m_latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job3")
        .withExecutionSemaphore(m_mutex2)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 2  (JOB-4)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job4")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(m_mutex2));

    // =========
    // SESSION 3 (JOB-1)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        m_latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex3_job1")
        .withExecutionSemaphore(m_mutex3)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    assertTrue(m_latch.await());
  }

  @After
  public void after() {
    m_latch.unblock();
    bc1.setBlocking(false);

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 10, TimeUnit.SECONDS);

    JobTestUtil.unregisterAndShutdownJobManager(m_jobManagerBean);
  }

  @Test
  public void testProtocol() {
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch

    assertEquals(expected, protocol);
  }

  @Test
  public void testVisitNullFilter() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(null);

    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  @Test
  public void testVisitFilter() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter());

    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  @Test
  public void testVisitBlockedFilter() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter());

    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
//    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
//    expected.add("mutex2_job2"); // // re-acquiring mutex
//    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  @Test
  public void testVisitNotBlockedFilter() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .andMatchNotState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter());

    Set<String> expected = new HashSet<>();
//    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  @Test
  public void testVisitSession1Filter() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchNameRegex(Pattern.compile("mutex1_.*"))
        .toFilter());

    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
//    expected.add("mutex2_job2"); // // re-acquiring mutex
//    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  @Test
  public void testVisitSession2Filter() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchNameRegex(Pattern.compile("mutex2_.*"))
        .toFilter());

    Set<String> expected = new HashSet<>();
//    expected.add("mutex1_job1"); // blocked
//    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  @Test
  public void testVisitSessionFilterAndBlocked() {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchNameRegex(Pattern.compile("mutex1_.*"))
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter());
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
//    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
//    expected.add("mutex2_job2"); // // re-acquiring mutex
//    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, extractJobNames(futures));
  }

  private static Set<String> extractJobNames(Set<IFuture<?>> futures) {
    Set<String> jobNames = new HashSet<>();
    for (IFuture<?> future : futures) {
      jobNames.add(future.getJobInput().getName());
    }
    return jobNames;
  }
}
