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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobStateTest {

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
  public void testRunningAndDone() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final BlockingCountDownLatch runningLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        runningLatch.countDownAndBlock();

      }
    }, Jobs.newInput());

    assertTrue(runningLatch.await());
    assertEquals(JobState.RUNNING, future.getState());
    runningLatch.unblock();
    future.awaitDone(5, TimeUnit.SECONDS);
    assertEquals(JobState.DONE, future.getState());

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  @Times(500) // regression
  @Ignore
  public void testWaitingForMutexAndBlockingCondition() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final BlockingCountDownLatch job1RunningLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch job2UnblockedLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch job3RunningLatch = new BlockingCountDownLatch(1);
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    final IMutex mutex = Jobs.newMutex();

    // Schedule job-1
    IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job1RunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withMutex(mutex));

    // Wait until running
    assertTrue(job1RunningLatch.await());

    // Schedule job-2
    IFuture<Void> future2 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job2RunningLatch.countDownAndBlock();
        condition.waitFor();
        job2UnblockedLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withMutex(mutex));

    // Schedule job-3
    IFuture<Void> future3 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job3RunningLatch.countDownAndBlock();
        JobTestUtil.waitForMutexCompetitors(mutex, 2);
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withMutex(mutex));

    assertTrue(job1RunningLatch.await());
    assertEquals(JobState.RUNNING, future1.getState());
    assertEquals(JobState.WAITING_FOR_MUTEX, future2.getState());
    assertEquals(JobState.WAITING_FOR_MUTEX, future3.getState());
    job1RunningLatch.unblock();

    job2RunningLatch.await();
    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.RUNNING, future2.getState());
    assertEquals(JobState.WAITING_FOR_MUTEX, future3.getState());
    job2RunningLatch.unblock();

    job3RunningLatch.await();
    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, future2.getState());
    assertEquals(JobState.RUNNING, future3.getState());

    condition.setBlocking(false);
    JobTestUtil.waitForMutexCompetitors(mutex, 2); // job-2 and job-3

    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.WAITING_FOR_MUTEX, future2.getState());
    assertEquals(JobState.RUNNING, future3.getState());

    job3RunningLatch.unblock();
    job2UnblockedLatch.await();

    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.RUNNING, future2.getState());
    assertEquals(JobState.DONE, future3.getState());
    job2UnblockedLatch.unblock();

    future2.awaitDoneAndGet(5, TimeUnit.SECONDS);
    future3.awaitDoneAndGet(5, TimeUnit.SECONDS);
    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.DONE, future2.getState());
    assertEquals(JobState.DONE, future3.getState());

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future3, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future3, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.WAITING_FOR_BLOCKING_CONDITION, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future3, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future3, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future2, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testPending() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final BlockingCountDownLatch runningLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        runningLatch.countDownAndBlock();
      }
    }, Jobs.newInput().withSchedulingDelay(2, TimeUnit.SECONDS));

    JobTestUtil.waitForState(future, JobState.PENDING);
    assertEquals(JobState.PENDING, future.getState());
    assertTrue(runningLatch.await());
    assertEquals(JobState.RUNNING, future.getState());
    runningLatch.unblock();
    future.awaitDone(5, TimeUnit.SECONDS);
    assertEquals(JobState.DONE, future.getState());

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testRejected() throws InterruptedException {
    Jobs.getJobManager().shutdown();

    IFuture<Void> future = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
    future.awaitDone(5, TimeUnit.SECONDS);
    assertEquals(JobState.REJECTED, future.getState());
  }

  @Test
  @Times(500) // regression
  public void testBlockedAndInterrupted() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final IMutex mutex = Jobs.newMutex();
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);
    final AtomicReference<Thread> workerThread = new AtomicReference<>();

    final IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        workerThread.set(Thread.currentThread());
        assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());
        try {
          condition.waitFor("ABC");
          fail("timeout expected");
        }
        catch (InterruptedException e) {
          assertFalse(mutex.isMutexOwner(IFuture.CURRENT.get()));
          assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());
        }
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withMutex(mutex));

    // Wait until job-1 is running
    JobTestUtil.waitForState(future1, JobState.WAITING_FOR_BLOCKING_CONDITION);

    // Interrupt worker thread
    workerThread.get().interrupt();
    future1.awaitDoneAndGet();
    assertTrue(future1.isDone());
    assertFalse(future1.isCancelled());
    assertEquals(0, mutex.getCompetitorCount());

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_EXECUTION_HINT_ADDED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.WAITING_FOR_BLOCKING_CONDITION, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_EXECUTION_HINT_REMOVED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.RUNNING, capturedEvents.get(i)); // due to interruption
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  @Times(100) // regression
  @Ignore
  public void testBlockedAndTimeout() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final IMutex mutex = Jobs.newMutex();
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);
    final BlockingCountDownLatch job1RunningLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Thread> workerThread = new AtomicReference<>();

    final IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        workerThread.set(Thread.currentThread());
        assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());

        job1RunningLatch.countDownAndBlock();
        try {
          condition.waitFor(1, TimeUnit.MILLISECONDS, "ABC");
          fail("timeout expected");
        }
        catch (TimeoutException e) {
          assertFalse(mutex.isMutexOwner(IFuture.CURRENT.get()));
          assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());
        }
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withMutex(mutex));

    // Wait until job-1 is running
    job1RunningLatch.await();
    job1RunningLatch.unblock();
    JobTestUtil.waitForState(future1, JobState.WAITING_FOR_BLOCKING_CONDITION);

    // Interrupt worker thread
    future1.awaitDoneAndGet();

    assertTrue(future1.isDone());
    assertFalse(future1.isCancelled());
    assertEquals(0, mutex.getCompetitorCount());

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_EXECUTION_HINT_ADDED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.WAITING_FOR_BLOCKING_CONDITION, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_EXECUTION_HINT_REMOVED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.RUNNING, capturedEvents.get(i)); // due to timeout
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future1, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleAtFixedRate() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final AtomicInteger rounds = new AtomicInteger(0);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (rounds.incrementAndGet() == 3) {
          IFuture.CURRENT.get().cancel(false);
        }
      }
    }, Jobs.newInput()
        .withSchedulingDelay(1, TimeUnit.MILLISECONDS)
        .withPeriodicExecutionAtFixedRate(1, TimeUnit.MILLISECONDS));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // first round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // second round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // third round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleAtFixedRateAndMutex() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final AtomicInteger rounds = new AtomicInteger(0);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (rounds.incrementAndGet() == 3) {
          IFuture.CURRENT.get().cancel(false);
        }
      }
    }, Jobs.newInput()
        .withMutex(Jobs.newMutex())
        .withSchedulingDelay(1, TimeUnit.MILLISECONDS)
        .withPeriodicExecutionAtFixedRate(1, TimeUnit.MILLISECONDS));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++; // first round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++; // second round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++; // third round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleWithFixedDelay() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final AtomicInteger rounds = new AtomicInteger(0);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (rounds.incrementAndGet() == 3) {
          IFuture.CURRENT.get().cancel(false);
        }
      }
    }, Jobs.newInput()
        .withSchedulingDelay(1, TimeUnit.MILLISECONDS)
        .withPeriodicExecutionWithFixedDelay(1, TimeUnit.MILLISECONDS));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // first round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // second round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // third round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleWithFixedDelayAndMutex() throws InterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final AtomicInteger rounds = new AtomicInteger(0);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (rounds.incrementAndGet() == 3) {
          IFuture.CURRENT.get().cancel(false);
        }
      }
    }, Jobs.newInput()
        .withMutex(Jobs.newMutex())
        .withSchedulingDelay(1, TimeUnit.MILLISECONDS)
        .withPeriodicExecutionWithFixedDelay(1, TimeUnit.MILLISECONDS));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++; // first round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++; // second round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.WAITING_FOR_MUTEX, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_MUTEX, capturedFutureStates.get(i));

    i++; // third round
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertEvent(JobEventType.JOB_STATE_CHANGED, future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  private static void assertEvent(JobEventType expectedType, IFuture<?> expectedFuture, Object expectedData, JobEvent actualEvent) {
    assertSame(expectedFuture, actualEvent.getFuture());
    assertSame(expectedData, actualEvent.getData());
    assertSame(expectedType, actualEvent.getType());
  }

  private static class JobEventCaptureListener implements IJobListener {

    final List<JobEvent> events = new ArrayList<>();
    final List<JobState> futureStates = new ArrayList<>();

    @Override
    public void changed(JobEvent event) {
      events.add(event);
      futureStates.add(event.getFuture() != null ? event.getFuture().getState() : null);
    }

    public List<JobEvent> getCapturedEvents() {
      return events;
    }

    public List<JobState> getCapturedFutureStates() {
      return futureStates;
    }
  }
}
