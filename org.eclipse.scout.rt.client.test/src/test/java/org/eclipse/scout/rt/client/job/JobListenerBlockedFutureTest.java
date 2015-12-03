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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
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
    P_JobChangeListener listener = new P_JobChangeListener();
    IJobListenerRegistration listenerRegistration = Jobs.getJobManager().addListener(listener);
    IClientSession clientSession = mock(IClientSession.class);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // NOOP
      }
    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(clientSession, true)));

    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 1, TimeUnit.MINUTES));
    Jobs.getJobManager().shutdown();
    listenerRegistration.dispose();

    // verify Event Types
    List<JobEventType> expectedStati = new ArrayList<>();
    expectedStati.add(JobEventType.SCHEDULED);
    expectedStati.add(JobEventType.ABOUT_TO_RUN);
    expectedStati.add(JobEventType.DONE);
    expectedStati.add(JobEventType.SHUTDOWN);
    assertEquals(expectedStati, listener.m_stati);

    // verify Futures
    List<IFuture<?>> expectedFutures = new ArrayList<>();
    expectedFutures.add(future);
    expectedFutures.add(future);
    expectedFutures.add(future);
    expectedFutures.add(null);
    assertEquals(expectedFutures, listener.m_futures);
  }

  @Test(timeout = 10000)
  public void testEventsForBlockingJob() throws Exception {
    final IBlockingCondition condition = Jobs.getJobManager().createBlockingCondition("test condition", true);

    IClientSession clientSession = mock(IClientSession.class);
    when(clientSession.getModelJobMutex()).thenReturn(Jobs.newMutex());

    P_JobChangeListener modelJobListener = new P_JobChangeListener();
    P_JobChangeListener jobListener = new P_JobChangeListener();
    IJobListenerRegistration modelJobListenerRegistration = Jobs.getJobManager().addListener(ModelJobs.newEventFilterBuilder().toFilter(), modelJobListener);
    IJobListenerRegistration jobListenerRegistration = Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder().toFilter(), jobListener);
    IFuture<Void> outerFuture = null;
    final IHolder<IFuture<?>> innerFuture = new Holder<IFuture<?>>();
    try {
      final JobInput input = ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession, true));

      // start recording of events
      outerFuture = Jobs.getJobManager().schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          innerFuture.setValue(Jobs.getJobManager().schedule(new IRunnable() {

            @Override
            public void run() throws Exception {
              condition.setBlocking(false);

              // Wait until the outer future is re-acquiring the mutex.
              JobTestUtil.waitForMutexCompetitors(input.getMutex(), 2); // 2=outer-job + inner-job
            }
          }, input.copy().withSchedulingDelay(200, TimeUnit.MILLISECONDS)));

          condition.waitFor();
        }
      }, input.copy());
      assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
          .andMatchFuture(outerFuture)
          .toFilter(), 1, TimeUnit.MINUTES));
      Jobs.getJobManager().shutdown();
    }
    finally {
      modelJobListenerRegistration.dispose();
      jobListenerRegistration.dispose();
    }

    // verify Event Types
    List<JobEventType> expectedStati = new ArrayList<>();
    expectedStati.add(JobEventType.SCHEDULED); // outer
    expectedStati.add(JobEventType.ABOUT_TO_RUN); // outer
    expectedStati.add(JobEventType.SCHEDULED); // inner
    expectedStati.add(JobEventType.BLOCKED); // outer
    expectedStati.add(JobEventType.ABOUT_TO_RUN); // inner
    expectedStati.add(JobEventType.UNBLOCKED); // outer
    expectedStati.add(JobEventType.DONE); // inner
    expectedStati.add(JobEventType.RESUMED); // outer
    expectedStati.add(JobEventType.DONE); // outer
    assertEquals(expectedStati, modelJobListener.m_stati);

    expectedStati = new ArrayList<>();
    expectedStati.add(JobEventType.SCHEDULED); // outer
    expectedStati.add(JobEventType.ABOUT_TO_RUN); // outer
    expectedStati.add(JobEventType.SCHEDULED); // inner
    expectedStati.add(JobEventType.BLOCKED); // outer
    expectedStati.add(JobEventType.ABOUT_TO_RUN); // inner
    expectedStati.add(JobEventType.UNBLOCKED); // outer
    expectedStati.add(JobEventType.DONE); // inner
    expectedStati.add(JobEventType.RESUMED); // outer
    expectedStati.add(JobEventType.DONE); // outer
    expectedStati.add(JobEventType.SHUTDOWN);
    assertEquals(expectedStati, jobListener.m_stati);

    // verify Futures
    List<IFuture<?>> expectedFutures = new ArrayList<>();
    expectedFutures.add(outerFuture); // scheduled
    expectedFutures.add(outerFuture); // about to run
    expectedFutures.add(innerFuture.getValue()); // scheduled
    expectedFutures.add(outerFuture); // blocked
    expectedFutures.add(innerFuture.getValue()); // about to run
    expectedFutures.add(outerFuture); // unblocked
    expectedFutures.add(innerFuture.getValue()); // done
    expectedFutures.add(outerFuture); // resumed
    expectedFutures.add(outerFuture); // done
    assertEquals(expectedFutures, modelJobListener.m_futures);

    expectedFutures = new ArrayList<>();
    expectedFutures.add(outerFuture); // scheduled
    expectedFutures.add(outerFuture); // about to run
    expectedFutures.add(innerFuture.getValue()); // scheduled
    expectedFutures.add(outerFuture); // blocked
    expectedFutures.add(innerFuture.getValue()); // about to run
    expectedFutures.add(outerFuture); // unblocked
    expectedFutures.add(innerFuture.getValue()); // done
    expectedFutures.add(outerFuture); // resumed
    expectedFutures.add(outerFuture); // done
    expectedFutures.add(null); // shutdown
    assertEquals(expectedFutures, jobListener.m_futures);

  }

  private static final class P_JobChangeListener implements IJobListener {

    private final List<JobEventType> m_stati = new ArrayList<>();
    private final List<IFuture<?>> m_futures = new ArrayList<>();

    @Override
    public void changed(JobEvent event) {
      m_stati.add(event.getType());
      m_futures.add(event.getFuture());
    }
  }
}
