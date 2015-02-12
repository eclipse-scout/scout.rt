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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.FutureTaskEx;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MutualExclusionTest {

  private static ExecutorService s_executor;

  private IClientSession m_clientSession;
  private ModelJobManager m_jobManager;

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
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession.getTexts()).thenReturn(new ScoutTexts());
    when(m_clientSession.getModelJobManager()).thenReturn(new ModelJobManager());

    m_jobManager = m_clientSession.getModelJobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test(expected = JobExecutionException.class)
  public void testModelThread() throws ProcessingException {
    m_jobManager.createBlockingCondition("bc").releaseMutexAndAwait();
  }

  /**
   * Tests serial execution of model jobs.
   */
  @Test
  public void testModelJobs() throws ProcessingException, InterruptedException {
    final Set<Integer> protocol = new HashSet<>();

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add(1);
      }
    }.schedule();
    new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add(2);
      }
    }.schedule();
    new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add(3);
      }
    }.schedule();

    m_jobManager.waitForIdle(10, TimeUnit.SECONDS);

    assertEquals(CollectionUtility.hashSet(1, 2, 3), protocol);
  }

  /**
   * Tests serial execution of nested model jobs.
   */
  @Test
  public void testNestedModelJobs() throws ProcessingException, InterruptedException {
    final List<Integer> protocol = new ArrayList<>();

    final CountDownLatch latch = new CountDownLatch(1);

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        protocol.add(1);

        // RUN-NOW
        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            protocol.add(2);
          }
        }.runNow();

        protocol.add(3);

        // SCHEDULE
        new ModelJob<Void>("job-3", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor3) throws ProcessingException {
            protocol.add(7);

            // SCHEDULE
            ModelJob<Void> job4 = new ModelJob<Void>("job-4", m_clientSession) {

              @Override
              protected void onRunVoid(IProgressMonitor monitor4) throws ProcessingException {
                protocol.add(14);
              }
            };
            IFuture<Void> future = job4.schedule();
            try {
              future.get(2, TimeUnit.SECONDS);
            }
            catch (JobExecutionException e) {
              if (e.isTimeout()) {
                protocol.add(8);
              }
            }

            protocol.add(9);

            // RUN-NOW
            new ModelJob<Void>("job-2", m_clientSession) {

              @Override
              protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
                protocol.add(10);
              }
            }.runNow();

            protocol.add(11);
          }
        }.schedule();

        protocol.add(4);

        // SCHEDULE
        new ModelJob<Void>("job-5", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor5) throws ProcessingException {
            protocol.add(12);

            new ModelJob<Void>("job-6", m_clientSession) {

              @Override
              protected void onRunVoid(IProgressMonitor monitor6) throws ProcessingException {
                protocol.add(15);
                latch.countDown();
              }
            }.schedule();

            protocol.add(13);
          }
        }.schedule();

        protocol.add(5);

        _sleep(500);

        protocol.add(6);
      }
    }.schedule().get();

    latch.await(30, TimeUnit.SECONDS);

    assertEquals(CollectionUtility.arrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), protocol);
  }

  /**
   * Tests that a model-job cannot wait for a scheduled job.
   */
  @Test
  public void testMutexDeadlock() throws ProcessingException, InterruptedException {
    final Holder<JobExecutionException> jeeHolder = new Holder<>();
    final BooleanHolder onRunVoidJob2 = new BooleanHolder(false);

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            onRunVoidJob2.setValue(true);
          }
        };

        IFuture<Void> future = job2.schedule();
        try {
          future.get(3, TimeUnit.SECONDS);
        }
        catch (JobExecutionException e) {
          jeeHolder.setValue(e);
        }
      }

    }.schedule().get();

    assertFalse(onRunVoidJob2.getValue());
    JobExecutionException e = jeeHolder.getValue();
    assertNotNull(e);
    assertTrue(e.isTimeout());
  }

  /**
   * Tests a BlockingCondition that blocks a single model-thread.
   */
  @Test
  public void testBlockingConditionSingle() throws ProcessingException, InterruptedException {
    final List<String> protocol = new ArrayList<>();

    final IBlockingCondition BC = m_clientSession.getModelJobManager().createBlockingCondition("bc");

    ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running");

        if (m_jobManager.isIdle()) {
          protocol.add("idle1");
        }
        if (m_jobManager.isBlocked(this)) {
          protocol.add("blocked1");
        }
        if (isBlocked()) {
          protocol.add("isBlocked1");
        }
        if (m_jobManager.isModelThread()) {
          protocol.add("modelThread1");
        }

        protocol.add("beforeAwait");
        BC.releaseMutexAndAwait();
        protocol.add("afterAwait");

        if (m_jobManager.isIdle()) {
          protocol.add("idle2");
        }
        if (m_jobManager.isBlocked(this)) {
          protocol.add("blocked2");
        }
        if (isBlocked()) {
          protocol.add("isBlocked2");
        }
        if (m_jobManager.isModelThread()) {
          protocol.add("modelThread2");
        }
      }
    };
    job1.schedule();
    _sleep(500); // sleep until job1 enters the blocking condition.
    protocol.add("beforeSignaling");

    assertFalse(m_jobManager.isModelThread());
    assertTrue(m_jobManager.isIdle());
    assertTrue(job1.isBlocked());
    assertTrue(m_jobManager.isBlocked(job1));

    BC.signalAll();
    _sleep(500);
    assertTrue(m_jobManager.waitForIdle(5, TimeUnit.SECONDS));
    assertFalse(m_jobManager.isModelThread());

    assertEquals(Arrays.asList("running", "modelThread1", "beforeAwait", "beforeSignaling", "afterAwait", "modelThread2"), protocol);
  }

  /**
   * Tests a BlockingCondition that blocks multiple model-threads.
   */
  @Test
  public void testBlockingConditionMultipleFlat() throws ProcessingException, InterruptedException {
    final IBlockingCondition BC = m_clientSession.getModelJobManager().createBlockingCondition("bc");

    // run the test 2 times to also test reusability of a blocking condition.
    runTestBlockingConditionMultipleFlat(BC);
    runTestBlockingConditionMultipleFlat(BC);
  }

  /**
   * Tests a BlockingCondition that blocks multiple model-threads that were scheduled as nested jobs.
   */
  @Test
  public void testBlockingConditionMultipleNested() throws ProcessingException, InterruptedException {
    final IBlockingCondition BC = m_clientSession.getModelJobManager().createBlockingCondition("bc");

    // run the test 2 times to also test reusability of a blocking condition.
    runTestBlockingCondition2(BC);
    runTestBlockingCondition2(BC);
  }

  /**
   * We have 1 job entering a blocking condition and gets canceled. This test verifies that the job is removed from the
   * job-map and is not marked as blocked anymore.
   */
  @Test
  public void testBlockingCondition_InterruptedWhileBeingBlocked1() throws JobExecutionException {
    final List<String> protocol = new ArrayList<>();
    final IBlockingCondition BC = m_jobManager.createBlockingCondition("bc");

    final ModelJob<Void> job = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running");

        try {
          protocol.add("before-await");
          BC.releaseMutexAndAwait();
        }
        catch (ProcessingException e) {
          if (e.isInterruption()) {
            protocol.add("interrupted");
          }
        }
      }
    };
    IFuture<Void> future = job.schedule();

    _sleep(500);
    assertTrue(m_jobManager.isIdle());
    assertTrue(m_jobManager.isBlocked(job));
    assertEquals(future, m_jobManager.getFuture(job));

    job.cancel(true); // force interruption
    _sleep(500);

    assertTrue(m_jobManager.isIdle());
    assertFalse(m_jobManager.isBlocked(job));
    assertNull(m_jobManager.getFuture(job));
    assertTrue(future.isCancelled());
    assertEquals(Arrays.asList("running", "before-await", "interrupted"), protocol);
  }

  /**
   * We have 3 jobs that are scheduled simultaneously. Thereby, job1 enters a blocking condition which in turn lets job2
   * running. Job2 goes to sleep forever, meaning that job3 will never start running. The test verifies, that when job1
   * is interrupted, job3 must not be scheduled because the mutex-owner is still job2.
   */
  @Test
  public void testBlockingCondition_InterruptedWhileBeingBlocked2() throws JobExecutionException {
    final List<String> protocol = new ArrayList<>();
    final IBlockingCondition BC = m_jobManager.createBlockingCondition("bc");

    final ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-1");

        try {
          BC.releaseMutexAndAwait();
        }
        catch (ProcessingException e) {
          if (e.isInterruption()) {
            protocol.add("interrupted-1");
          }

          if (!m_jobManager.isModelThread()) {
            protocol.add("non-model-thread-1");
          }
        }
      }
    };

    final ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-2");
        _sleep(Long.MAX_VALUE);
      }
    };
    final ModelJob<Void> job3 = new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-3");
        _sleep(Long.MAX_VALUE);
      }
    };

    IFuture<Void> future1 = job1.schedule();
    IFuture<Void> future2 = job2.schedule();
    IFuture<Void> future3 = job3.schedule();

    assertEquals(future1, m_jobManager.getFuture(job1));
    assertEquals(future2, m_jobManager.getFuture(job2));
    assertEquals(future3, m_jobManager.getFuture(job3));

    _sleep(500);

    assertTrue(m_jobManager.isBlocked(job1));

    job1.cancel(true); // force interruption

    _sleep(500);

    assertFalse(m_jobManager.isIdle());
    assertFalse(m_jobManager.isBlocked(job1));

    assertNull(m_jobManager.getFuture(job1));
    assertEquals(future2, m_jobManager.getFuture(job2));
    assertEquals(future3, m_jobManager.getFuture(job3));

    assertEquals(Arrays.asList("running-1", "running-2", "interrupted-1", "non-model-thread-1"), protocol);
  }

  /**
   * We have 3 jobs that are scheduled simultaneously. Thereby, job1 enters a blocking condition which in turn lets job2
   * running. Job2 unblocks job1 so that job1 is trying to re-acquire the mutex. While waiting for the mutex to be
   * available, job1 is interrupted due to a cancel-request of job2.<br/>
   * This test verifies, that even if being interrupted while re-acquiring the mutex, job1 still waits for the mutex to
   * become available and becomes the mutex owner as soon as job2 completes. Also job3 does not start running while job1
   * is interrupted.
   */
  @Test
  public void testBlockingCondition_InterruptedWhileReAcquiringTheMutex() throws ProcessingException {
    final List<String> protocol = new ArrayList<>();
    final IBlockingCondition BC = m_jobManager.createBlockingCondition("bc");

    final CountDownLatch latchJob2 = new CountDownLatch(1);

    final ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-1 (a)");

        try {
          BC.releaseMutexAndAwait();
          protocol.add("not-interrupted-1");
        }
        catch (JobExecutionException e) {
          protocol.add("interrupted-1");
        }

        protocol.add("running-1 (b)");
      }
    };

    final ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-2");
        _sleep(1000);
        BC.signalAll();
        _sleep(1000);
        job1.cancel(true); // interrupt job1 while acquiring the mutex

        try {
          latchJob2.await();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    };
    final ModelJob<Void> job3 = new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-3");
        _sleep(Long.MAX_VALUE);
      }
    };

    IFuture<Void> future1 = job1.schedule();
    IFuture<Void> future2 = job2.schedule();
    IFuture<Void> future3 = job3.schedule();

    _sleep(4000);

    assertEquals(Arrays.asList("running-1 (a)", "running-2"), protocol);
    assertEquals(3, m_jobManager.m_mutexSemaphore.getPermitCount()); // job1 (trying to re-acquire the mutex, job2 (currently the mutex-owner), job3 (trying to acquire the mutex)
    assertFalse(m_jobManager.isBlocked(job1)); // the blocking condition is reversed but still job1 is  not running yet because waiting for the mutex.
    assertFalse(m_jobManager.isBlocked(job2));
    assertFalse(m_jobManager.isBlocked(job3));

    assertEquals(future1, m_jobManager.getFuture(job1));
    assertTrue(future1.isCancelled());
    try {
      future1.get(0, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
    }

    assertEquals(future2, m_jobManager.getFuture(job2));
    assertFalse(future2.isCancelled());
    try {
      future2.get(0, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isTimeout());
    }

    assertEquals(future3, m_jobManager.getFuture(job3));
    assertFalse(future3.isCancelled());
    try {
      future3.get(0, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isTimeout());
    }

    // let job2 finish its work
    latchJob2.countDown();
    _sleep(1000);

    assertEquals(Arrays.asList("running-1 (a)", "running-2", "not-interrupted-1", "running-1 (b)", "running-3"), protocol);
    assertEquals(1, m_jobManager.m_mutexSemaphore.getPermitCount()); // job3
    assertFalse(m_jobManager.isBlocked(job1));
    assertFalse(m_jobManager.isBlocked(job3));

    assertNull(m_jobManager.getFuture(job1));
    assertTrue(future1.isCancelled());
    assertTrue(future1.isDone());
    try {
      future1.get(0, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
    }

    assertNull(m_jobManager.getFuture(job2));
    assertTrue(future2.isDone());
    assertFalse(future2.isCancelled());
    try {
      assertNull(future2.get(0, TimeUnit.MILLISECONDS));
    }
    catch (JobExecutionException e) {
      fail();
    }

    assertEquals(future3, m_jobManager.getFuture(job3));
    assertFalse(future3.isCancelled());
    try {
      future3.get(0, TimeUnit.MILLISECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isTimeout());
    }
  }

  /**
   * We have 3 jobs that get scheduled simultaneously. The first waits some time so that job2 and job3 get queued. If
   * job2 gets scheduled, it is rejected by the executor. The test verifies, that job3 still gets scheduled.
   */
  @Test
  public void testRejection() throws InterruptedException, JobExecutionException {
    final List<String> protocol = new ArrayList<>();

    final ThreadPoolExecutor executorMock = mock(ThreadPoolExecutor.class);
    final ModelJobManager jobManager = new ModelJobManager() {
      @Override
      protected ExecutorService createExecutor() {
        return executorMock;
      }
    };

    final AtomicInteger count = new AtomicInteger();
    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        final FutureTaskEx<?> futureTask = (FutureTaskEx) invocation.getArguments()[0];

        switch (count.incrementAndGet()) {
          case 1: // job1
            s_executor.execute(new Runnable() {

              @Override
              public void run() {
                _sleep(2000); // wait for the other jobs to be queued
                futureTask.run();
              }
            });
            break;
          case 2: // job2 gets rejected
            jobManager.handleJobRejected(futureTask);
            break;
          case 3: // job3
            s_executor.execute(new Runnable() {

              @Override
              public void run() {
                futureTask.run();
              }
            });
            break;
        }
        return null;
      }
    }).when(executorMock).execute(any(Runnable.class));
    when(m_clientSession.getModelJobManager()).thenReturn(jobManager);

    ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-job-1");
      }
    };
    ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-job-2");
      }
    };
    ModelJob<Void> job3 = new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-job-3");
      }
    };

    IFuture<Void> future1 = job1.schedule();
    IFuture<Void> future2 = job2.schedule();
    IFuture<Void> future3 = job3.schedule();

    assertTrue(jobManager.waitForIdle(10, TimeUnit.SECONDS));
    assertEquals(Arrays.asList("running-job-1", "running-job-3"), protocol);
    assertTrue(jobManager.m_jobMap.isEmpty());
    assertFalse(future1.isCancelled());
    assertTrue(future2.isCancelled());
    assertFalse(future3.isCancelled());
  }

  /**
   * We have 4 jobs that get scheduled simultaneously. The first waits some time so that job2, job3 and job4 get queued.
   * Job1 then enters a blocking condition, which allows job2 to run. While job2 is running, it unblocks job1 so that
   * job1 competes for the mutex anew. However, job1 is rejected by the executor. This test verifies, that the execution
   * of job1 is cancelled and that job3 is not scheduled prior job2 finished. When job2 is finished, job3 starts
   * running and waits until being cancelled. That allows job4 to run.
   */
  @Test
  public void testBlockedJobRejection1() throws InterruptedException, ProcessingException {
    final List<String> protocol = new ArrayList<>();

    final ThreadPoolExecutor executorMock = mock(ThreadPoolExecutor.class);
    final ModelJobManager jobManager = new ModelJobManager() {
      @Override
      protected ExecutorService createExecutor() {
        return executorMock;
      }
    };

    final AtomicInteger count = new AtomicInteger();
    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        final FutureTaskEx<?> futureTask = (FutureTaskEx) invocation.getArguments()[0];

        switch (count.incrementAndGet()) {
          case 1: // job1 gets scheduled
            s_executor.execute(new Runnable() {

              @Override
              public void run() {
                _sleep(2000); // wait for the other jobs to be queued
                futureTask.run();
              }
            });
            break;
          case 2: // job2 gets scheduled after job1 enters blocking condition
            s_executor.execute(new Runnable() {

              @Override
              public void run() {
                futureTask.run();
              }
            });
            break;
          case 3: // job1 re-acquires the mutex after being released from the blocking condition
            jobManager.handleJobRejected(futureTask);
            break;
          case 4: // job 3 gets scheduled
            s_executor.execute(new Runnable() {

              @Override
              public void run() {
                futureTask.run();
              }
            });
            break;
          case 5: // job 4 gets scheduled
            s_executor.execute(new Runnable() {

              @Override
              public void run() {
                futureTask.run();
              }
            });
            break;
        }
        return null;
      }
    }).when(executorMock).execute(any(Runnable.class));
    when(m_clientSession.getModelJobManager()).thenReturn(jobManager);

    final IBlockingCondition BC = jobManager.createBlockingCondition("bc");

    ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        try {
          protocol.add("running-job-1 (a)");
          BC.releaseMutexAndAwait();
          protocol.add("running-job-1 (b)");
        }
        catch (JobExecutionException e) {
          if (e.isInterruption()) {
            protocol.add("running-job-1 (c) [interrupted]");
          }
          if (e.isRejection()) {
            protocol.add("running-job-1 (d) [rejected]");
          }
        }

        if (m_jobManager.isModelThread()) {
          protocol.add("running-job-1 (e) [model-thread]");
        }
      }
    };
    ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-job-2 (a)");
        BC.signalAll();
        _sleep(1000); // wait that jbo1 can acquire mutex after being unblocked.
        protocol.add("running-job-2 (b)");
      }
    };

    ModelJob<Void> job3 = new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-job-3");
        _sleep(Long.MAX_VALUE);
      }
    };

    ModelJob<Void> job4 = new ModelJob<Void>("job-4", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-job-4");
      }
    };

    IFuture<Void> future1 = job1.schedule();
    IFuture<Void> future2 = job2.schedule();
    IFuture<Void> future3 = job3.schedule();
    IFuture<Void> future4 = job4.schedule();

    _sleep(500);
    assertFalse(jobManager.waitForIdle(5, TimeUnit.SECONDS)); // job4 is pending
    assertEquals(Arrays.asList("running-job-1 (a)", "running-job-2 (a)", "running-job-2 (b)", "running-job-1 (d) [rejected]", "running-job-3"), protocol);

    assertEquals(CollectionUtility.hashSet(jobManager.getFuture(job3), jobManager.getFuture(job4)), new HashSet<>(jobManager.m_jobMap.copyJobMap().values()));

    try {
      future1.get();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
    }
    assertTrue(future1.isCancelled());
    assertTrue(future1.isDone());

    assertFalse(future2.isCancelled());
    assertTrue(future2.isDone());

    assertFalse(future3.isCancelled());
    assertFalse(future3.isDone());

    assertFalse(future4.isCancelled());
    assertFalse(future4.isDone());

    // cancel job3
    future3.cancel(true);

    assertTrue(jobManager.waitForIdle(10, TimeUnit.SECONDS));
    assertEquals(Arrays.asList("running-job-1 (a)", "running-job-2 (a)", "running-job-2 (b)", "running-job-1 (d) [rejected]", "running-job-3", "running-job-4"), protocol);
    assertTrue(jobManager.m_jobMap.isEmpty());

    assertTrue(future3.isCancelled());
    assertTrue(future3.isDone());

    assertFalse(future4.isCancelled());
    assertTrue(future4.isDone());

  }

  private void runTestBlockingConditionMultipleFlat(final IBlockingCondition BC) throws ProcessingException, InterruptedException {
    final List<String> protocol = new ArrayList<>();

    final ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("job-1-beforeAwait");
        BC.releaseMutexAndAwait();
        protocol.add("job-X-afterAwait");
        _sleep(500);
      }
    };
    job1.schedule();
    final ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("job-2-beforeAwait");
        BC.releaseMutexAndAwait();
        protocol.add("job-X-afterAwait");
      }
    };
    job2.schedule();
    final ModelJob<Void> job3 = new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("job-3-beforeAwait");
        BC.releaseMutexAndAwait();
        protocol.add("job-X-afterAwait");
      }
    };
    job3.schedule();
    new ModelJob<Void>("job-4", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("job-4-running");
      }
    }.schedule();
    new ModelJob<Void>("job-5", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("job-5-running");
        if (job1.isBlocked()) {
          protocol.add("job-1-blocked");
        }
        if (job2.isBlocked()) {
          protocol.add("job-2-blocked");
        }
        if (job3.isBlocked()) {
          protocol.add("job-3-blocked");
        }

        protocol.add("job-5-signaling");
        BC.signalAll();
        _sleep(500);

        if (!job1.isBlocked()) {
          protocol.add("job-1-unblocked");
        }
        if (!job2.isBlocked()) {
          protocol.add("job-2-unblocked");
        }
        if (!job3.isBlocked()) {
          protocol.add("job-3-unblocked");
        }

        if (!m_jobManager.getFuture(job1).isDone()) {
          protocol.add("job-1-stillRunning");
        }
        if (!m_jobManager.getFuture(job2).isDone()) {
          protocol.add("job-2-stillRunning");
        }
        if (!m_jobManager.getFuture(job2).isDone()) {
          protocol.add("job-3-stillRunning");
        }

        protocol.add("job-5-ending");
      }
    }.schedule();

    assertTrue(m_jobManager.waitForIdle(10, TimeUnit.SECONDS));

    // Assertions must be done against 2 results because there is no guarantee about the sequence in which waiting threads are released when the blocking condition falls.
    List<String> expected1 = new ArrayList<>();
    expected1.add("job-1-beforeAwait");
    expected1.add("job-2-beforeAwait");
    expected1.add("job-3-beforeAwait");
    expected1.add("job-5-running");
    expected1.add("job-1-blocked");
    expected1.add("job-2-blocked");
    expected1.add("job-3-blocked");
    expected1.add("job-5-signaling");
    expected1.add("job-1-unblocked");
    expected1.add("job-2-unblocked");
    expected1.add("job-3-unblocked");
    expected1.add("job-1-stillRunning");
    expected1.add("job-2-stillRunning");
    expected1.add("job-3-stillRunning");
    expected1.add("job-5-ending");
    expected1.add("job-X-afterAwait"); // no guarantee about the sequence in which waiting threads are released when the blocking condition falls.
    expected1.add("job-X-afterAwait"); // no guarantee about the sequence in which waiting threads are released when the blocking condition falls.
    expected1.add("job-X-afterAwait"); // no guarantee about the sequence in which waiting threads are released when the blocking condition falls.
    expected1.add("job-4-running");
  }

  private void runTestBlockingCondition2(final IBlockingCondition BC) throws InterruptedException, JobExecutionException {
    final List<String> protocol = new ArrayList<>();

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        final ModelJob<Void> job1 = this;

        protocol.add("job-1-running");

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            final ModelJob<Void> job2 = this;

            protocol.add("job-2-running");

            if (m_jobManager.isBlocked(job1)) {
              protocol.add("job-1-blocked");
            }

            new ModelJob<Void>("job-3", m_clientSession) {

              @Override
              protected void onRunVoid(IProgressMonitor monitor3) throws ProcessingException {
                protocol.add("job-3-running");

                if (m_jobManager.isBlocked(job1)) {
                  protocol.add("job-1-blocked");
                }
                if (m_jobManager.isBlocked(job2)) {
                  protocol.add("job-2-blocked");
                }

                new ModelJob<Void>("job-4", m_clientSession) {

                  @Override
                  protected void onRunVoid(IProgressMonitor monitor4) throws ProcessingException {
                    protocol.add("job-4-running");
                  }
                }.schedule();
                _sleep(500); // just that job4 would have enough time to be scheduled (should not be happen)

                protocol.add("job-3-before-signaling");
                if (BC.hasWaitingThreads()) {
                  protocol.add("job-3-hasWaitingThreads");
                }
                BC.signalAll();
                _sleep(500); // just that the signaled jobs could run (should not be happen)
                protocol.add("job-3-after-signaling");

                new ModelJob<Void>("job-5", m_clientSession) {

                  @Override
                  protected void onRunVoid(IProgressMonitor monitor4) throws ProcessingException {
                    protocol.add("job-5-running");
                  }
                }.schedule();
              }
            }.schedule();

            _sleep(500); // just that job3 would have enough time to be scheduled (should not be happen)
            protocol.add("job-2-beforeAwait");
            BC.releaseMutexAndAwait();
            protocol.add("JOB-X-AFTERAWAIT");
          }
        }.schedule();
        _sleep(500); // just that job2 would have enough time to be scheduled (should not be happen)

        protocol.add("job-1-beforeAwait");
        BC.releaseMutexAndAwait();
        protocol.add("JOB-X-AFTERAWAIT");
      }
    }.schedule();
    assertTrue(m_jobManager.waitForIdle(10, TimeUnit.SECONDS));

    List<String> expected = new ArrayList<>();
    expected.add("job-1-running");
    expected.add("job-1-beforeAwait");
    expected.add("job-2-running");
    expected.add("job-1-blocked");
    expected.add("job-2-beforeAwait");
    expected.add("job-3-running");
    expected.add("job-1-blocked");
    expected.add("job-2-blocked");
    expected.add("job-3-before-signaling");
    expected.add("job-3-hasWaitingThreads");
    expected.add("job-3-after-signaling");
    expected.add("JOB-X-AFTERAWAIT"); // no guarantee about the sequence in which waiting threads are released when the blocking condition falls.
    expected.add("JOB-X-AFTERAWAIT"); // no guarantee about the sequence in which waiting threads are released when the blocking condition falls.
    expected.add("job-4-running");
    expected.add("job-5-running");

    assertEquals(expected, protocol); // for logging
  }

  private static void _sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      // NOOP
    }
  }
}
