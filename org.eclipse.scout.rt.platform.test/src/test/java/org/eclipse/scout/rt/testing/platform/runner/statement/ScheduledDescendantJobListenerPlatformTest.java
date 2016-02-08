/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(PlatformTestRunner.class)
public class ScheduledDescendantJobListenerPlatformTest {

  private static final String NESTED_JOB1 = "nestedJob-1";
  private static final String NESTED_JOB2 = "nestedJob-2";
  private static final String NESTED_JOB3 = "nestedJob-3";

  private static final CyclicBarrier ALIEN_JOB_CREATOR_BARRIER = new CyclicBarrier(2);
  private static final AtomicBoolean ALIEN_JOB_CREATOR_RUNNING = new AtomicBoolean(true);
  private static final AtomicBoolean ALIEN_JOB_STARTED = new AtomicBoolean();

  private IRegistrationHandle m_listenerRegistration;
  private AssertNoRunningJobsStatement.ScheduledDescendantJobListener m_jobListener;
  private Map<String, NestedJob> m_nestedJobsByName;

  /**
   * Starts an alien job creator used to create new jobs that are not part of the jobs being created during test
   * execution.
   */
  @BeforeClass
  public static void beforeClass() {
    // This outer job helps creating new jobs.
    Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        while (true) {
          ALIEN_JOB_CREATOR_BARRIER.await();

          if (!ALIEN_JOB_CREATOR_RUNNING.get()) {
            break;
          }

          // create and schedule new alien job
          Jobs.schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              // just schedule an empty job without doing anything
            }
          }, Jobs.newInput());

          ALIEN_JOB_STARTED.set(true);

          ALIEN_JOB_CREATOR_BARRIER.reset();
        }
      }
    }, Jobs.newInput());
  }

  /**
   * Stops the alien job creator.
   */
  @AfterClass
  public static void afterClass() throws Exception {
    ALIEN_JOB_CREATOR_RUNNING.set(false);
    ALIEN_JOB_CREATOR_BARRIER.await(5, TimeUnit.SECONDS);
  }

  /**
   * Helps creating a new alien job within a test method.
   */
  private static void createAlientJob() throws Exception {
    ALIEN_JOB_STARTED.set(false);
    ALIEN_JOB_CREATOR_BARRIER.await();
    int i = 0;
    while (!ALIEN_JOB_STARTED.get()) {
      if (i < 10) {
        Thread.sleep(500);
        i++;
      }
      else {
        fail("alien job was not scheduled wihtin 5s");
      }
    }
  }

  @Before
  public void before() {
    installScheduledJobListener();
    m_nestedJobsByName = new ConcurrentHashMap<>();
  }

  @After
  public void after() {
    if (m_listenerRegistration != null) {
      m_listenerRegistration.dispose();
    }
    // release all nested job latches
    for (NestedJob nestedJob : m_nestedJobsByName.values()) {
      nestedJob.getLatch().countDown();
    }
  }

  /* *************************************************************************************
   * no nested jobs
   * *************************************************************************************/

  @Test
  public void testNoNestedJobs() {
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
  }

  /* *************************************************************************************
   * no nested but an alien job
   * *************************************************************************************/

  @Test
  public void testNoNestedButAlienJob() throws Exception {
    doTestNoNestedButAlienJob();
  }

  @Test
  public void testNoNestedButAlienJobStartedInNestedJob() {
    runInNestedJob(new IRunnable() {
      @Override
      public void run() throws Exception {
        doTestNoNestedButAlienJob();
      }
    });
  }

  private void doTestNoNestedButAlienJob() throws Exception {
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
    createAlientJob();
    // alien job must no be recorded by our job listener
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
  }

  /* *************************************************************************************
   * one nested job
   * *************************************************************************************/

  @Test
  public void testOneNestedJob() throws Exception {
    doTestOneNestedJob();
  }

  @Test
  public void testOneNestedJobInNestedJob() {
    runInNestedJob(new IRunnable() {
      @Override
      public void run() throws Exception {
        doTestOneNestedJob();
      }
    });
  }

  private void doTestOneNestedJob() throws InterruptedException {
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
    CountDownLatch creatingJobsLatch = new CountDownLatch(1);
    scheduleJobs(creatingJobsLatch, new JobSpec(NESTED_JOB1));
    creatingJobsLatch.await(5, TimeUnit.SECONDS);
    assertEquals(
        CollectionUtility.hashSet(
            m_nestedJobsByName.get(NESTED_JOB1).getFuture()),
        m_jobListener.getScheduledFutures());
  }

  /* *************************************************************************************
   * two nested jobs
   * *************************************************************************************/

  @Test
  public void testTwoNestedJobs() throws Exception {
    doTestTwoNestedJobs();
  }

  @Test
  public void testTwoNestedJobInNestedJobs() {
    runInNestedJob(new IRunnable() {
      @Override
      public void run() throws Exception {
        doTestTwoNestedJobs();
      }
    });
  }

  private void doTestTwoNestedJobs() throws InterruptedException {
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
    CountDownLatch creatingJobsLatch = new CountDownLatch(2);
    scheduleJobs(creatingJobsLatch, new JobSpec(NESTED_JOB1), new JobSpec(NESTED_JOB2));
    creatingJobsLatch.await(5, TimeUnit.SECONDS);
    assertEquals(
        CollectionUtility.hashSet(
            m_nestedJobsByName.get(NESTED_JOB1).getFuture(),
            m_nestedJobsByName.get(NESTED_JOB2).getFuture()),
        m_jobListener.getScheduledFutures());
  }

  /* *************************************************************************************
   * one nested job with another nested job
   * *************************************************************************************/

  @Test
  public void testOneNestedJobWithAnotherNestedJob() throws Exception {
    doTestOneNestedJobWithAnotherNestedJob();
  }

  @Test
  public void testOneNestedJobWithAnotherNestedJobInNestedJob() {
    runInNestedJob(new IRunnable() {
      @Override
      public void run() throws Exception {
        doTestOneNestedJobWithAnotherNestedJob();
      }
    });
  }

  private void doTestOneNestedJobWithAnotherNestedJob() throws InterruptedException {
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
    CountDownLatch creatingJobsLatch = new CountDownLatch(2);
    scheduleJobs(creatingJobsLatch, new JobSpec(NESTED_JOB1, new JobSpec(NESTED_JOB2)));
    creatingJobsLatch.await(5, TimeUnit.SECONDS);
    assertEquals(
        CollectionUtility.hashSet(
            m_nestedJobsByName.get(NESTED_JOB1).getFuture(),
            m_nestedJobsByName.get(NESTED_JOB2).getFuture()),
        m_jobListener.getScheduledFutures());
  }

  /* *************************************************************************************
   * three jobs nested in each other, i.e. j1(j2(j3))
   * *************************************************************************************/

  @Test
  public void testOneNestedJobWithThreeLevels() throws Exception {
    doTestOneNestedJobWithThreeLevels();
  }

  @Test
  public void testOneNestedJobWithThreeLevelsInNestedJob() {
    runInNestedJob(new IRunnable() {
      @Override
      public void run() throws Exception {
        doTestOneNestedJobWithThreeLevels();
      }
    });
  }

  private void doTestOneNestedJobWithThreeLevels() throws InterruptedException {
    assertEquals(Collections.<IFuture<?>> emptySet(), m_jobListener.getScheduledFutures());
    CountDownLatch creatingJobsLatch = new CountDownLatch(3);
    scheduleJobs(creatingJobsLatch, new JobSpec(NESTED_JOB1, new JobSpec(NESTED_JOB2, new JobSpec(NESTED_JOB3))));
    creatingJobsLatch.await(5, TimeUnit.SECONDS);
    assertEquals(
        CollectionUtility.hashSet(
            m_nestedJobsByName.get(NESTED_JOB1).getFuture(),
            m_nestedJobsByName.get(NESTED_JOB2).getFuture(),
            m_nestedJobsByName.get(NESTED_JOB3).getFuture()),
        m_jobListener.getScheduledFutures());
  }

  /* *************************************************************************************
   * utilities
   * *************************************************************************************/

  private void runInNestedJob(final IRunnable runnable) {
    IFuture<Void> future = Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        // install new scheduled job listener
        installScheduledJobListener();
        runnable.run();
      }
    }, Jobs.newInput());
    future.awaitDone(5, TimeUnit.SECONDS);
  }

  private void installScheduledJobListener() {
    if (m_listenerRegistration != null) {
      m_listenerRegistration.dispose();
    }
    m_jobListener = new AssertNoRunningJobsStatement.ScheduledDescendantJobListener();
    m_listenerRegistration = Jobs.getJobManager().addListener(m_jobListener);
  }

  /**
   * Creates jobs according to the given job specifications and schedules them. The given {@link CountDownLatch}
   * provided by the caller is used to wait until all nested jobs have been scheduled.
   */
  private void scheduleJobs(final CountDownLatch creatingJobsLatch, JobSpec... jobSpecs) {
    for (final JobSpec jobSpec : jobSpecs) {
      final CountDownLatch latch = new CountDownLatch(1);
      IFuture<Void> future = Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          JobSpec[] subJobs = jobSpec.getSubJobs();
          scheduleJobs(creatingJobsLatch, subJobs);
          latch.await();
        }
      }, Jobs.newInput().withName(jobSpec.getName()));
      m_nestedJobsByName.put(jobSpec.getName(), new NestedJob(future, latch));
      creatingJobsLatch.countDown();
    }
  }

  /**
   * Describes a job with its potential sub jobs.
   */
  private static class JobSpec {
    private final String m_name;
    private final JobSpec[] m_subJobs;

    public JobSpec(String name, JobSpec... subJobs) {
      m_name = name;
      m_subJobs = subJobs;
    }

    public String getName() {
      return m_name;
    }

    public JobSpec[] getSubJobs() {
      return m_subJobs;
    }
  }

  /**
   * References a started job, i.e. its {@link IFuture} and the {@link CountDownLatch} used to let it finish.
   */
  private static class NestedJob {
    private final IFuture<?> m_future;
    private final CountDownLatch m_latch;

    public NestedJob(IFuture<?> future, CountDownLatch latch) {
      m_future = future;
      m_latch = latch;
    }

    public IFuture<?> getFuture() {
      return m_future;
    }

    public CountDownLatch getLatch() {
      return m_latch;
    }
  }
}
