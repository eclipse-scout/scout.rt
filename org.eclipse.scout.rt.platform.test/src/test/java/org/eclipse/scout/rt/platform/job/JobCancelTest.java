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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobCancelTest {

  private JobManager m_jobManager;
  private static ScheduledExecutorService s_executor;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newScheduledThreadPool(5);
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testCancelSoft() throws ProcessingException, InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-before");
        }

        try {
          setupLatch.countDownAndBlock(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          protocol.add("interrupted");
        }

        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-after");
        }

        verifyLatch.countDown();
      }
    }, JobInput.defaults());

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(false /* soft */));
    assertTrue(verifyLatch.await());

    // VERIFY
    assertEquals(Arrays.asList("cancelled-after"), protocol);
    assertTrue(future.isCancelled());

    try {
      assertNull(future.awaitDoneAndGet(1, TimeUnit.SECONDS));
      assertTrue(future.isCancelled());
    }
    catch (JobExecutionException e) {
      fail();
    }
  }

  @Test
  public void testCancelForce() throws ProcessingException, InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-before");
        }

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted");
        }

        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-after");
        }

        verifyLatch.countDown();
      }
    }, JobInput.defaults());

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(true /* force */));
    assertTrue(verifyLatch.await());

    // VERIFY
    assertTrue(future.isCancelled());
    assertEquals(Arrays.asList("interrupted", "cancelled-after"), protocol);

    try {
      assertNull(future.awaitDoneAndGet(5, TimeUnit.SECONDS));
      assertTrue(future.isCancelled());
    }
    catch (JobExecutionException e) {
      fail();
    }
  }

  @Test
  public void testCancelBeforeRunning() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running");
      }
    }, 500, TimeUnit.MILLISECONDS, JobInput.defaults());

    // RUN THE TEST
    future.cancel(true);
    Thread.sleep(TimeUnit.MILLISECONDS.toMillis(600)); // Wait some time so that the job could be scheduled (should not happen).

    // VERIFY
    assertTrue(future.isCancelled());
    assertTrue(protocol.isEmpty());

    try {
      assertNull(future.awaitDoneAndGet(10, TimeUnit.SECONDS));
      assertTrue(future.isCancelled());
    }
    catch (JobExecutionException e) {
      fail();
    }
  }

  @Test
  public void testCancelPeriodicAction() throws Exception {
    final AtomicInteger count = new AtomicInteger();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    final IFuture<Void> future = m_jobManager.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (count.incrementAndGet() == 3) {
          setupLatch.countDown();
          verifyLatch.await();
        }
      }
    }, 10L, 10L, TimeUnit.MILLISECONDS, JobInput.empty());

    assertTrue(setupLatch.await());

    // RUN THE TEST
    future.cancel(false);
    verifyLatch.countDown();

    // VERIFY
    Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // Wait some time so that the job could be rescheduled.  (should not happen).
    assertTrue(future.isCancelled());
    assertEquals(3, count.get());

    try {
      assertNull(future.awaitDoneAndGet(10, TimeUnit.SECONDS));
      assertTrue(future.isCancelled());
    }
    catch (JobExecutionException e) {
      fail();
    }
  }

  @Test
  public void testShutdownJobManagerAndSchedule() throws InterruptedException, ProcessingException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(2);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-1");
        try {
          latch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted-1");
        }
        finally {
          protocol.add("done-1");
        }
      }
    }, JobInput.defaults());

    IFuture<Void> future2 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-2");
        try {
          latch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted-2");
        }
        finally {
          protocol.add("done-2");
        }
      }
    }, JobInput.defaults());

    assertTrue(latch.await());

    // SHUTDOWN
    m_jobManager.shutdown();

    IFuture<Void> future3 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-3");
        try {
          latch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("running-3");
        }
        finally {
          protocol.add("done-3");
        }
      }
    }, JobInput.defaults());

    // VERIFY
    assertEquals(CollectionUtility.hashSet("running-1", "running-2", "interrupted-1", "interrupted-2", "done-1", "done-2"), protocol);
    assertTrue(future3.isCancelled());

    try {
      assertNull(future2.awaitDoneAndGet(1, TimeUnit.SECONDS));
      assertTrue(future2.isCancelled());
    }
    catch (JobExecutionException e) {
      fail();
    }
  }

  /**
   * Cancel cancellation of a job that has a nested job.
   */
  @Test
  public void testCancelNestedJobs() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);
    final BlockingCountDownLatch job1DoneLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("job-2-interrupted");
            }
            if (IFuture.CURRENT.get().isCancelled()) {
              protocol.add("job-2-cancelled (future)");
            }
            if (IProgressMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-2-cancelled (monitor)");
            }
            verifyLatch.countDown();
          }
        }, JobInput.defaults().name("job-2"));

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-1-interrupted");
        }
        if (IFuture.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled (future)");
        }
        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled (monitor)");
        }
        job1DoneLatch.countDown();
        verifyLatch.countDown();
      }
    }, JobInput.defaults().name("job-1"));

    assertTrue(setupLatch.await());
    future1.cancel(true);
    assertTrue(job1DoneLatch.await());
    setupLatch.unblock();
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("job-1-interrupted", "job-1-cancelled (future)", "job-1-cancelled (monitor)"), protocol);
  }

  /**
   * Cancel multiple jobs with the same job-id.
   */
  @Test
  public void testCancelMultipleJobsWithSameId() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final String commonJobId = "777";

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(5);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    // Job-1 (common-id)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-1-interrupted");
        }
        verifyLatch.countDown();
      }
    }, JobInput.defaults().id(commonJobId));

    // Job-2 (common-id)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-2-interrupted");
        }
        verifyLatch.countDown();
      }
    }, JobInput.defaults().id(commonJobId));

    // Job-3 (common-id)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Job-3a
        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("job-3b-interrupted");
            }
          }
        }, JobInput.defaults().id("123"));

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-3a-interrupted");
        }
        verifyLatch.countDown();
      }
    }, JobInput.defaults().id(commonJobId));

    // Job-4 (common-id, but not-null mutex)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-4-interrupted");
        }
      }
    }, JobInput.defaults().id(commonJobId).mutex(new Object()));

    assertTrue(setupLatch.await());
    m_jobManager.cancel(newJobIdAndMutexFilter(commonJobId, null), true);

    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("job-1-interrupted", "job-2-interrupted", "job-3a-interrupted"), protocol);
  }

  private static AndFilter<IFuture<?>> newJobIdAndMutexFilter(final String jobId, final Object mutex) {
    final IFilter<IFuture<?>> jobFilter = JobFutureFilters.allFilter().ids(jobId);
    final IFilter<IFuture<?>> mutexFilter = new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return future.getJobInput().getMutex() == mutex;
      }
    };

    return new AndFilter<>(jobFilter, mutexFilter);
  }
}
