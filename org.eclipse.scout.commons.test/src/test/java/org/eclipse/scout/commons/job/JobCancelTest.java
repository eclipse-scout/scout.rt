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
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobCancelTest {

  private IJobManager<IJobInput> m_jobManager;
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
    m_jobManager = new JobManager<IJobInput>("scout-thread");
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
    });

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(false /* soft */));
    assertTrue(verifyLatch.await());

    // VERIFY
    assertEquals(Arrays.asList("cancelled-after"), protocol);
    assertTrue(future.isCancelled());

    try {
      future.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertFalse(e.isTimeout());
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
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
    });

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(true /* force */));
    assertTrue(verifyLatch.await());

    // VERIFY
    assertTrue(future.isCancelled());
    assertEquals(Arrays.asList("interrupted", "cancelled-after"), protocol);

    try {
      future.get(5, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertFalse(e.isTimeout());
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
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
    }, 500, TimeUnit.MILLISECONDS);

    // RUN THE TEST
    future.cancel(true);
    Thread.sleep(TimeUnit.MILLISECONDS.toMillis(600)); // Wait some time so that the job could be scheduled (should not happen).

    // VERIFY
    assertTrue(future.isCancelled());
    assertTrue(protocol.isEmpty());

    try {
      future.get(10, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertFalse(e.isTimeout());
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
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
      future.get(10, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertFalse(e.isTimeout());
      assertTrue(e.isCancellation());
    }
  }

  @Test
  public void testShutdownJobManagerAndSchedule() throws InterruptedException, ProcessingException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(2);

    IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

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
    });

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
    });

    assertTrue(latch.await());

    // SHUTDOWN
    m_jobManager.shutdown();

    JobExecutionException e3 = null;
    try {
      m_jobManager.schedule(new IRunnable() {

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
      });
      fail();
    }
    catch (JobExecutionException e) {
      e3 = e;
    }

    // VERIFY
    assertEquals(CollectionUtility.hashSet("running-1", "running-2", "interrupted-1", "interrupted-2", "done-1", "done-2"), protocol);

    assertNotNull(e3);
    assertTrue(e3.isRejection());
    assertFalse(e3.isTimeout());
    assertFalse(e3.isInterruption());
    assertFalse(e3.isCancellation());

    try {
      future1.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
      assertFalse(e.isRejection());
    }

    try {
      future2.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
      assertFalse(e.isRejection());
    }
  }

  @Test
  public void testShutdownJobManagerAndRunNow() throws ProcessingException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    m_jobManager.shutdown();
    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-1");
      }
    });

    assertEquals(CollectionUtility.arrayList("running-1"), protocol);
  }

  @Test
  public void testRunNowAndShutdownJobManager() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    // This job will cancel the job manager.
    s_executor.submit(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        setupLatch.await();
        m_jobManager.shutdown();
        return null;
      }
    });

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("1: running");
        try {
          if (IProgressMonitor.CURRENT.get().isCancelled()) {
            protocol.add("2: cancelled");
          }
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("3: interrupted");
          if (IProgressMonitor.CURRENT.get().isCancelled()) {
            protocol.add("4: cancelled");
          }
        }
        finally {
          verifyLatch.countDown();
        }
      }
    });

    // VERIFY
    assertTrue(verifyLatch.await());
    assertEquals(CollectionUtility.arrayList("1: running", "3: interrupted", "4: cancelled"), protocol);
  }
}
