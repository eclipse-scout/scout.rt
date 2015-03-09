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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.commons.UncaughtExceptionRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ModelJobCancelTest {

  private static ExecutorService s_executor;

  private IModelJobManager m_jobManager;

  @Mock
  private IClientSession m_clientSession;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_jobManager = new ModelJobManager();

    // initialize ClientSession
    when(m_clientSession.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession.getTexts()).thenReturn(new ScoutTexts());

    ISession.CURRENT.set(m_clientSession);
  }

  @After
  public void after() {
    m_jobManager.shutdown();

    ISession.CURRENT.remove();
  }

  @Test
  public void testCancelSoft() throws ProcessingException, InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

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
      }
    });

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(false /* soft */));

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS));

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
      }
    });

    assertTrue(setupLatch.await());

    // RUN THE TEST
    assertTrue(future.cancel(true /* force */));

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS));

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
  public void testCancelBeforeRunning() throws ProcessingException, InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch job1RunningLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-1");
        job1RunningLatch.countDownAndBlock();
      }
    });

    IFuture<Void> future2 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-2");
      }
    });

    // RUN THE TEST
    job1RunningLatch.await();
    future2.cancel(true);
    job1RunningLatch.unblock();

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS));

    // VERIFY
    assertEquals(CollectionUtility.arrayList("running-1"), protocol);

    future1.get();
    assertFalse(future1.isCancelled());
    assertTrue(future1.isDone());

    try {
      assertTrue(future2.isCancelled());
      assertTrue(future2.isDone());
      future2.get();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isRejection());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }
  }

  @Test
  public void testShutdownJobManagerAndSchedule() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

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
      }
    });

    assertTrue(latch.await());

    // SHUTDOWN
    m_jobManager.shutdown();

    IFuture<Void> future3 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-3");
      }
    });

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS)); // Wait until job-1 finished.

    // VERIFY
    assertEquals(CollectionUtility.arrayList("running-1", "interrupted-1", "done-1"), protocol);

    try {
      future1.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isRejection());
      assertFalse(e.isTimeout());
    }

    try {
      future2.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isRejection());
      assertFalse(e.isTimeout());
    }

    try {
      future3.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isRejection());
      assertFalse(e.isTimeout());
    }
  }

  @Test
  public void testShutdownJobManagerAndRunNow() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch shutdownLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch runLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        m_jobManager.shutdown();
        shutdownLatch.countDown();

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add("running");
          }
        });
        runLatch.countDown();
      }
    });

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS));
    assertTrue(shutdownLatch.await());
    assertTrue(future.isCancelled());
    assertTrue(runLatch.await());
    assertEquals(CollectionUtility.arrayList("running"), protocol);
  }

  @Test
  public void testRunNowAndShutdownJobManager() throws Throwable {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch finishLatch = new BlockingCountDownLatch(1);

    // This job will cancel the job manager.
    final UncaughtExceptionRunnable runnable = new UncaughtExceptionRunnable() {

      @Override
      protected void runSafe() throws Exception {
        setupLatch.await();
        m_jobManager.shutdown();
      }

      @Override
      protected void onUncaughtException(Throwable e) {
        setupLatch.unblock();
      }
    };
    s_executor.execute(runnable);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
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
              finishLatch.countDown();
            }
          }
        });
      }
    });

    // VERIFY
    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 30, TimeUnit.SECONDS));
    runnable.throwOnError();
    assertTrue(m_jobManager.isDone(new AlwaysFilter<IFuture<?>>()));
    finishLatch.await();
    assertEquals(CollectionUtility.arrayList("1: running", "3: interrupted", "4: cancelled"), protocol);
  }
}
