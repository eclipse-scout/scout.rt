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
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class JobStateTest {

  private static IBean<IJobManager> s_jobManagerBean;

  @BeforeClass
  public static void beforeClass() {
    // Use dedicated job manager because job manager is shutdown in tests.
    s_jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JobManager() {
      // must be a subclass in order to replace JobManager
    });
  }

  @AfterClass
  public static void afterClass() {
    JobTestUtil.unregisterAndShutdownJobManager(s_jobManagerBean);
  }

  @Test
  public void testRunningAndDone() throws ThreadInterruptedException, java.lang.InterruptedException {
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
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  @Times(100) // regression
  public void testWaitingForMutexAndBlockingCondition() throws ThreadInterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final BlockingCountDownLatch job1RunningLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch job2UnblockedLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch job3RunningLatch = new BlockingCountDownLatch(1);
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    final IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    // Schedule job-1
    IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job1RunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(mutex));

    assertTrue(job1RunningLatch.await()); // wait until running (for idempotent event assertion)

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
        .withExecutionSemaphore(mutex));

    // Wait until competing for a permit.
    // That is for idempotent event assertion, because permit is acquired asynchronously in another thread.
    // However, permit acquisition is guaranteed to be in the 'as-scheduled' order. Nevertheless, the SCHEDULING and PENDING event of the next job would possibly interfere.
    JobTestUtil.waitForPermitCompetitors(mutex, 2);

    // Schedule job-3
    IFuture<Void> future3 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job3RunningLatch.countDownAndBlock();
        JobTestUtil.waitForPermitCompetitors(mutex, 2);
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withExecutionSemaphore(mutex));

    // Wait until competing for a permit.
    // That is for idempotent event assertion, because permit is acquired asynchronously in another thread.
    // However, permit acquisition is guaranteed to be in the 'as-scheduled' order. Nevertheless, the SCHEDULING and PENDING event of the next job would possibly interfere.
    JobTestUtil.waitForPermitCompetitors(mutex, 3);

    assertEquals(JobState.RUNNING, future1.getState());
    assertEquals(JobState.WAITING_FOR_PERMIT, future2.getState());
    assertEquals(JobState.WAITING_FOR_PERMIT, future3.getState());
    job1RunningLatch.unblock();

    job2RunningLatch.await();
    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.RUNNING, future2.getState());
    assertEquals(JobState.WAITING_FOR_PERMIT, future3.getState());
    job2RunningLatch.unblock();

    job3RunningLatch.await();
    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, future2.getState());
    assertEquals(JobState.RUNNING, future3.getState());

    condition.setBlocking(false);
    JobTestUtil.waitForPermitCompetitors(mutex, 2); // job-2 and job-3

    assertEquals(JobState.DONE, future1.getState());
    assertEquals(JobState.WAITING_FOR_PERMIT, future2.getState());
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
    assertStateChangedEvent(future1, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future2, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future2, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future3, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future3, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future2, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertBlockedStateEvent(future2, condition, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future3, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future2, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future3, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future2, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future2, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testPending() throws ThreadInterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final BlockingCountDownLatch runningLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        runningLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(2, TimeUnit.SECONDS)));

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
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  @Times(100) // regression
  public void testBlockedAndInterrupted() throws ThreadInterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);
    final AtomicReference<Thread> workerThread = new AtomicReference<>();

    final IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        workerThread.set(Thread.currentThread());
        assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());
        try {
          condition.waitFor("ABC");
          fail("interruption expected");
        }
        catch (ThreadInterruptedException e) {
          assertTrue(Thread.interrupted());
          Thread.currentThread().interrupt(); // Restore interrupted status
          assertTrue(mutex.isPermitOwner(IFuture.CURRENT.get()));
          assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());
        }
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(mutex));

    // Wait until job-1 is running
    JobTestUtil.waitForState(future1, JobState.WAITING_FOR_BLOCKING_CONDITION);

    // Interrupt worker thread
    workerThread.get().interrupt();
    future1.awaitDoneAndGet();
    assertTrue(future1.isDone());
    assertFalse(future1.isCancelled());

    JobTestUtil.waitForPermitCompetitors(mutex, 0); // wait because permit is released just after done (max 30s)

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future1, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertHintChangedEvent(JobEventType.JOB_EXECUTION_HINT_ADDED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertBlockedStateEvent(future1, condition, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertHintChangedEvent(JobEventType.JOB_EXECUTION_HINT_REMOVED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.RUNNING, capturedEvents.get(i)); // due to interruption
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  @Times(100) // regression
  public void testBlockedAndTimeout() throws ThreadInterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);
    final AtomicReference<Thread> workerThread = new AtomicReference<>();

    final IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        workerThread.set(Thread.currentThread());
        assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());

        try {
          condition.waitFor(1, TimeUnit.MILLISECONDS, "ABC");
          fail("timeout expected");
        }
        catch (TimedOutException e) {
          assertTrue(mutex.isPermitOwner(IFuture.CURRENT.get()));
          assertSame(JobState.RUNNING, IFuture.CURRENT.get().getState());
        }
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(mutex));

    // Wait until job-1 completed
    future1.awaitDoneAndGet(10, TimeUnit.SECONDS);
    assertTrue(future1.isDone());
    assertFalse(future1.isCancelled());

    JobTestUtil.waitForPermitCompetitors(mutex, 0); // wait because permit is released just after done (max 30s)

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future1, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertHintChangedEvent(JobEventType.JOB_EXECUTION_HINT_ADDED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertBlockedStateEvent(future1, condition, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertHintChangedEvent(JobEventType.JOB_EXECUTION_HINT_REMOVED, future1, "ABC", capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.RUNNING, capturedEvents.get(i)); // due to timeout
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future1, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleAtFixedRate() throws ThreadInterruptedException, java.lang.InterruptedException {
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
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // first round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // second round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // third round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleAtFixedRateAndMutex() throws ThreadInterruptedException, java.lang.InterruptedException {
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
        .withExecutionSemaphore(Jobs.newExecutionSemaphore(1))
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // first round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // second round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // third round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleWithFixedDelay() throws ThreadInterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(3, 1, TimeUnit.MILLISECONDS))));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // first round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // second round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // third round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testScheduleWithFixedDelayAndMutex() throws ThreadInterruptedException, java.lang.InterruptedException {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
    }, Jobs.newInput()
        .withExecutionSemaphore(Jobs.newExecutionSemaphore(1))
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(3, 1, TimeUnit.MILLISECONDS))));

    future.awaitDone(5, TimeUnit.SECONDS);

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // first round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // second round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // third round
    assertStateChangedEvent(future, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  private static void assertStateChangedEvent(IFuture<?> expectedFuture, JobState expectedState, JobEvent actualEvent) {
    assertSame(expectedFuture, actualEvent.getData().getFuture());
    assertSame(expectedState, actualEvent.getData().getState());
    assertSame(JobEventType.JOB_STATE_CHANGED, actualEvent.getType());
  }

  private static void assertBlockedStateEvent(IFuture<?> expectedFuture, IBlockingCondition expectedCondition, JobEvent actualEvent) {
    assertSame(expectedFuture, actualEvent.getData().getFuture());
    assertSame(JobState.WAITING_FOR_BLOCKING_CONDITION, actualEvent.getData().getState());
    assertSame(expectedCondition, actualEvent.getData().getBlockingCondition());
    assertSame(JobEventType.JOB_STATE_CHANGED, actualEvent.getType());
  }

  private static void assertHintChangedEvent(JobEventType expectedType, IFuture<?> expectedFuture, String expectedExecutionHint, JobEvent actualEvent) {
    assertSame(expectedFuture, actualEvent.getData().getFuture());
    assertSame(expectedExecutionHint, actualEvent.getData().getExecutionHint());
    assertSame(expectedType, actualEvent.getType());
  }

  private static class JobEventCaptureListener implements IJobListener {

    final List<JobEvent> events = new ArrayList<>();
    final List<JobState> futureStates = new ArrayList<>();

    @Override
    public void changed(JobEvent event) {
      events.add(event);
      futureStates.add(event.getData().getFuture() != null ? event.getData().getFuture().getState() : null);
    }

    public List<JobEvent> getCapturedEvents() {
      return events;
    }

    public List<JobState> getCapturedFutureStates() {
      return futureStates;
    }
  }
}
