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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobListenerBlockedFutureTest {

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
  public void testEvents() throws Exception {
    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(captureListener);

    IClientSession clientSession = mock(IClientSession.class);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(clientSession, true)));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 1, TimeUnit.MINUTES);
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

  @Test(timeout = 10000)
  public void testEventsForBlockingJob() {
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    IClientSession clientSession = mock(IClientSession.class);
    when(clientSession.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    JobEventCaptureListener captureListener = new JobEventCaptureListener();
    Jobs.getJobManager().addListener(ModelJobs.newEventFilterBuilder().toFilter(), captureListener);

    IFuture<Void> outerFuture = null;
    final AtomicReference<IFuture<?>> innerFuture = new AtomicReference<>();
    final JobInput modelJobInput = ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession, true));

    // start recording of events
    outerFuture = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        innerFuture.set(Jobs.getJobManager().schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            condition.setBlocking(false);

            // Wait until the outer future is re-acquiring the mutex.
            JobTestUtil.waitForPermitCompetitors(modelJobInput.getExecutionSemaphore(), 2); // 2=outer-job + inner-job
          }
        }, modelJobInput.copy()
            .withName("inner")
            .withExecutionTrigger(Jobs.newExecutionTrigger()
                .withStartIn(2, TimeUnit.SECONDS))));

        condition.waitFor();
      }
    }, modelJobInput.copy()
        .withName("outer"));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(outerFuture)
        .toFilter(), 1, TimeUnit.MINUTES);

    Jobs.getJobManager().shutdown();

    // verify events
    int i = -1;
    List<JobEvent> capturedEvents = captureListener.getCapturedEvents();
    List<JobState> capturedFutureStates = captureListener.getCapturedFutureStates();

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++; // inner
    assertStateChangedEvent(innerFuture.get(), JobState.SCHEDULED, capturedEvents.get(i));
    assertEquals(JobState.SCHEDULED, capturedFutureStates.get(i));

    i++; // inner
    assertStateChangedEvent(innerFuture.get(), JobState.PENDING, capturedEvents.get(i));
    assertEquals(JobState.PENDING, capturedFutureStates.get(i));

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.WAITING_FOR_BLOCKING_CONDITION, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_BLOCKING_CONDITION, capturedFutureStates.get(i));

    i++; // inner
    assertStateChangedEvent(innerFuture.get(), JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // inner
    assertStateChangedEvent(innerFuture.get(), JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.WAITING_FOR_PERMIT, capturedEvents.get(i));
    assertEquals(JobState.WAITING_FOR_PERMIT, capturedFutureStates.get(i));

    i++; // inner
    assertStateChangedEvent(innerFuture.get(), JobState.DONE, capturedEvents.get(i));
    assertEquals(JobState.DONE, capturedFutureStates.get(i));

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.RUNNING, capturedEvents.get(i));
    assertEquals(JobState.RUNNING, capturedFutureStates.get(i));

    i++; // outer
    assertStateChangedEvent(outerFuture, JobState.DONE, capturedEvents.get(i));
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
