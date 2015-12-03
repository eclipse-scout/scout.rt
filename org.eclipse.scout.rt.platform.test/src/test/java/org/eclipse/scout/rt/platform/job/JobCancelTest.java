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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.runner.statement.ReplaceJobManagerStatement.JUnitJobManager;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobCancelTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    // Use dedicated job manager because job manager is shutdown in tests.
    m_jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JUnitJobManager() {
      // must be a subclass in order to replace JUnitJobManager
    });
  }

  @After
  public void after() {
    JobTestUtil.unregisterAndShutdownJobManager(m_jobManagerBean);
  }

  @Test
  public void testCancelSoft() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (RunMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-before");
        }

        try {
          setupLatch.countDownAndBlock(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          protocol.add("interrupted");
        }

        if (RunMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-after");
        }

        verifyLatch.countDown();
      }
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(false /* soft */));
    assertTrue(verifyLatch.await());

    // VERIFY
    assertEquals(Arrays.asList("cancelled-after"), protocol);
    assertTrue(future.isCancelled());

    assertTrue(future.awaitDone(1, TimeUnit.SECONDS));
    assertTrue(future.isCancelled());
  }

  @Test
  public void testCancelForce() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (RunMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-before");
        }

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted");
        }

        if (RunMonitor.CURRENT.get().isCancelled()) {
          protocol.add("cancelled-after");
        }

        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(true /* force */));
    assertTrue(verifyLatch.await());

    // VERIFY
    assertTrue(future.isCancelled());
    assertEquals(Arrays.asList("interrupted", "cancelled-after"), protocol);

    assertTrue(future.awaitDone(5, TimeUnit.SECONDS));
    assertTrue(future.isCancelled());
  }

  @Test
  public void testCancelBeforeRunning() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running");
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withSchedulingDelay(10, TimeUnit.SECONDS));

    // RUN THE TEST
    future.cancel(true);

    // VERIFY
    assertTrue(future.isCancelled());
    assertTrue(protocol.isEmpty());

    assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
    assertTrue(future.isCancelled());
  }

  @Test
  public void testCancelPeriodicAction() throws Exception {
    final AtomicInteger count = new AtomicInteger();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    final IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (count.incrementAndGet() == 3) {
          setupLatch.countDown();
          verifyLatch.await();
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withPeriodicExecutionAtFixedRate(10, TimeUnit.MILLISECONDS)
        .withExceptionHandling(null, false));

    assertTrue(setupLatch.await());

    // RUN THE TEST
    future.cancel(false);
    verifyLatch.countDown();

    // VERIFY
    assertTrue(future.isCancelled());
    assertEquals(3, count.get());

    assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
    assertTrue(future.isCancelled());
  }

  @Test
  public void testShutdownJobManagerAndSchedule() throws InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(2);

    Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future2 = Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    assertTrue(latch.await());

    // SHUTDOWN
    Jobs.getJobManager().shutdown();

    IFuture<Void> future3 = Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    // VERIFY
    assertEquals(CollectionUtility.hashSet("running-1", "running-2", "interrupted-1", "interrupted-2", "done-1", "done-2"), protocol);
    assertTrue(future3.isCancelled());

    assertTrue(future2.awaitDone(1, TimeUnit.SECONDS));
    assertTrue(future2.isCancelled());
  }

  /**
   * Cancel of a job that has a child job.
   */
  @Test
  public void testCancelChildJob() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);
    final BlockingCountDownLatch job2DoneLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(2);

    final AtomicReference<IFuture<?>> childFutureRef = new AtomicReference<>();

    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {

        //attach to child runmonitor -> nested cancel
        IFuture<?> childFuture = Jobs.getJobManager().schedule(new IRunnable() {

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
            if (RunMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-2-cancelled (monitor)");
            }
            job2DoneLatch.countDown();
            verifyLatch.countDown();
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent())
            .withName("job-2")
            .withExceptionHandling(null, false));
        childFutureRef.set(childFuture);

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-1-interrupted");
        }
        if (IFuture.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled (future)");
        }
        if (RunMonitor.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled (monitor)");
        }
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1"));

    assertTrue(setupLatch.await());
    childFutureRef.get().cancel(true);
    assertTrue(job2DoneLatch.await());
    setupLatch.unblock();
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet(
        "job-2-interrupted",
        "job-2-cancelled (future)",
        "job-2-cancelled (monitor)"), protocol);
  }

  /**
   * Cancel of a job that has a nested job.
   */
  @Test
  public void testCancelParentJob() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(3);
    final BlockingCountDownLatch job1DoneLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    IFuture<Void> future1 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        //re-use runmonitor -> nested cancel
        Jobs.getJobManager().schedule(new IRunnable() {

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
            if (RunMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-2-cancelled (monitor)");
            }
            verifyLatch.countDown();
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent())
            .withName("job-2")
            .withExceptionHandling(null, false));

        //does not re-use runmonitor -> no nested cancel
        Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("job-3-interrupted");
            }
            if (IFuture.CURRENT.get().isCancelled()) {
              protocol.add("job-3-cancelled (future)");
            }
            if (RunMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-3-cancelled (monitor)");
            }
            verifyLatch.countDown();
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent().withRunMonitor(new RunMonitor()))
            .withName("job-3")
            .withExceptionHandling(null, false));

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-1-interrupted");
        }
        if (IFuture.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled (future)");
        }
        if (RunMonitor.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled (monitor)");
        }
        job1DoneLatch.countDown();
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1"));

    assertTrue(setupLatch.await());
    future1.cancel(true);
    assertTrue(job1DoneLatch.await());
    setupLatch.unblock();
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet(
        "job-1-interrupted",
        "job-1-cancelled (future)",
        "job-1-cancelled (monitor)",
        "job-2-interrupted",
        "job-2-cancelled (future)",
        "job-2-cancelled (monitor)"), protocol);
  }

  /**
   * Cancel multiple jobs with the same job-id.
   */
  @Test
  @Times(20)
  public void testCancelMultipleJobsByName() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final String commonJobName = "777";

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(6);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(4);

    // Job-1 (common-id) => CANCEL
    Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName(commonJobName)
        .withMutex(null)
        .withExceptionHandling(null, false));

    // Job-2 (common-id) => CANCEL
    Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName(commonJobName)
        .withMutex(null)
        .withExceptionHandling(null, false));

    // Job-3 (common-id) => CANCEL
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Job-3a (other name, same re-used runMonitor => CANCEL AS WELL)
        Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("job-3a-interrupted");
            }
            verifyLatch.countDown();
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent())
            .withName("otherName")
            .withMutex(null)
            .withExceptionHandling(null, false));

        // Job-3b (other name, other runMonitor => NO CANCEL)
        Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("job-3b-interrupted");
            }
          }
        }, Jobs.newInput()
            .withRunContext(RunContexts.copyCurrent().withRunMonitor(new RunMonitor()))
            .withName("otherName")
            .withMutex(null)
            .withExceptionHandling(null, false));

        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-3-interrupted");
        }
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName(commonJobName)
        .withMutex(null));

    // Job-4 (common-id, but not-null mutex)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-4-interrupted");
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName(commonJobName)
        .withMutex(Jobs.newMutex())
        .withExceptionHandling(null, false));

    assertTrue(setupLatch.await());
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchName(commonJobName)
        .andMatchMutex(null)
        .toFilter(), true);
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("job-1-interrupted", "job-2-interrupted", "job-3-interrupted", "job-3a-interrupted"), protocol);
    setupLatch.unblock(); // release not cancelled jobs
  }

  /**
   * Tests that a job is not run if the RunMonitor is already cancelled.
   */
  @Test
  public void testCancelRunContextPriorSchedulingJob() {
    RunContext runContext = RunContexts.copyCurrent();

    // 1. Cancel the RunMonitor
    runContext.getRunMonitor().cancel(false);

    // 2. Schedule the job (should never)
    final AtomicBoolean executed = new AtomicBoolean(false);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, Jobs.newInput().withRunContext(runContext));

    future.awaitDone();
    assertFalse(executed.get());
    assertTrue(future.isCancelled());
  }
}
