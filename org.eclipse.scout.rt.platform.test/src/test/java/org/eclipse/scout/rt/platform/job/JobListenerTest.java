/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobListenerTest {

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
  public void testEvents() {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    IFuture<Void> future = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(RunContexts.empty()));
    future.awaitDone(10, TimeUnit.SECONDS);
    Jobs.getJobManager().shutdown();

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

    i++;
    assertJobManagerShutdownEvent(capturedEvents.get(i));
    assertNull(capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testCancel() {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    final BooleanHolder hasStarted = new BooleanHolder(Boolean.FALSE);
    IFuture<Void> future = Jobs.getJobManager().schedule(() -> hasStarted.setValue(Boolean.TRUE), Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.HOURS)));

    future.cancel(true);
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);

    Jobs.getJobManager().shutdown();

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
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    i++;
    assertJobManagerShutdownEvent(capturedEvents.get(i));
    assertNull(capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testGlobalListener1() {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    IFuture<Void> future = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future.awaitDone();

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
  public void testGlobalListener2() {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(
            JobState.SCHEDULED,
            JobState.DONE)
        .toFilter(), captureListener);

    IFuture<Void> future = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future.awaitDone();

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testLocalListener1() throws InterruptedException {
    // schedule job, and install listener once started running
    final BlockingCountDownLatch jobRunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(() -> {
      jobRunningLatch.countDownAndBlock();
    }, Jobs.newInput());
    assertTrue(jobRunningLatch.await());

    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.DONE)
        .andMatchFuture(future)
        .toFilter(), captureListener);
    jobRunningLatch.unblock();
    future.awaitDone();

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testLocalListener2a() throws InterruptedException {
    // Schedule job-1
    IFuture<Void> future1 = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future1.awaitDone();

    // schedule job-2, and install listener once started running
    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future2 = Jobs.getJobManager().schedule(() -> {
      job2RunningLatch.countDownAndBlock();
    }, Jobs.newInput());
    assertTrue(job2RunningLatch.await());

    // install listener
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchFuture(future1, future2)
        .toFilter(), captureListener);

    job2RunningLatch.unblock();
    future2.awaitDone();

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future2, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testLocalListener2b() throws InterruptedException {
    IFuture<Void> future1 = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future1.awaitDone();

    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future2 = Jobs.getJobManager().schedule(() -> {
      job2RunningLatch.countDownAndBlock();
    }, Jobs.newInput());
    assertTrue(job2RunningLatch.await());

    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchFuture(future2)
        .andMatchFuture(future1)
        .toFilter(), captureListener);

    job2RunningLatch.unblock();
    future2.awaitDone();

    // verify events
    assertTrue(captureListener.getCapturedEvents().isEmpty());
    assertTrue(captureListener.getCapturedFutureStates().isEmpty());
  }

  @Test
  public void testLocalListener3() throws InterruptedException {
    IFuture<Void> future1 = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future1.awaitDone();

    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future2 = Jobs.getJobManager().schedule(() -> {
      job2RunningLatch.countDownAndBlock();
    }, Jobs.newInput());
    assertTrue(job2RunningLatch.await());

    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(future2)
        .toFilter(), captureListener);

    job2RunningLatch.unblock();
    future2.awaitDone();

    // verify events
    assertTrue(captureListener.getCapturedEvents().isEmpty());
    assertTrue(captureListener.getCapturedFutureStates().isEmpty());
  }

  @Test
  public void testLocalListener4() throws InterruptedException {
    final BlockingCountDownLatch jobRunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(() -> {
      jobRunningLatch.countDownAndBlock();
    }, Jobs.newInput());
    assertTrue(jobRunningLatch.await());

    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    future.addListener(captureListener);

    jobRunningLatch.unblock();
    future.awaitDone();

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++;
    assertStateChangedEvent(future, JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    assertEquals(i + 1, capturedEvents.size());
  }

  @Test
  public void testLocalListener5() throws InterruptedException {
    final BlockingCountDownLatch jobRunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(() -> {
      jobRunningLatch.countDownAndBlock();
    }, Jobs.newInput());
    assertTrue(jobRunningLatch.await());

    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    future.addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.DONE)
        .toFilter(), captureListener);

    jobRunningLatch.unblock();
    future.awaitDone();

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

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

  private static void assertJobManagerShutdownEvent(JobEvent actualEvent) {
    assertNull(actualEvent.getData().getFuture());
    assertNull(actualEvent.getData().getState());
    assertSame(JobEventType.JOB_MANAGER_SHUTDOWN, actualEvent.getType());
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
