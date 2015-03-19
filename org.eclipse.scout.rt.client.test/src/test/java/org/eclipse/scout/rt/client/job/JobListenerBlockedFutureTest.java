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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.internal.MutexSemaphores;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobListenerBlockedFutureTest {

  private P_JobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new P_JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testEvents() throws Exception {
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(listener, new AlwaysFilter<JobEvent>());
    IClientSession clientSession = mock(IClientSession.class);

    ClientJobInput input = ClientJobInput.empty().session(clientSession);
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
      }
    }, input);
    assertTrue(m_jobManager.awaitDone(ClientJobFutureFilters.newFilter().futures(future), 1, TimeUnit.MINUTES));
    m_jobManager.shutdown();
    m_jobManager.removeListener(listener);

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
    final IBlockingCondition condition = m_jobManager.createBlockingCondition("test condition", true);
    P_JobChangeListener modelJobListener = new P_JobChangeListener();
    P_JobChangeListener clientJobListener = new P_JobChangeListener();

    m_jobManager.addListener(modelJobListener, ModelJobEventFilter.INSTANCE);
    m_jobManager.addListener(clientJobListener, ClientJobEventFilter.INSTANCE);
    IFuture<Void> outerFuture = null;
    final IHolder<IFuture<?>> innerFuture = new Holder<IFuture<?>>();
    try {
      IClientSession clientSession = mock(IClientSession.class);
      final ModelJobInput input = ModelJobInput.empty().session(clientSession);

      // start recording of events
      outerFuture = m_jobManager.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          innerFuture.setValue(m_jobManager.schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              condition.setBlocking(false);

              // Wait until the outer future is re-acquiring the mutex.
              m_jobManager.waitForPermitsAcquired(input.getMutex(), 2); // 2=outer-job + inner-job
            }
          }, 200, TimeUnit.MILLISECONDS, input));

          condition.waitFor();
        }
      }, input);
      assertTrue(m_jobManager.awaitDone(ClientJobFutureFilters.newFilter().futures(outerFuture), 1, TimeUnit.MINUTES));
      m_jobManager.shutdown();
    }
    finally {
      m_jobManager.removeListener(modelJobListener);
      m_jobManager.removeListener(clientJobListener);
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
    expectedStati.add(JobEventType.DONE); // inner
//    expectedStati.add(JobChangeEventType.SHUTDOWN); // is not associated with a model-job event
    assertEquals(expectedStati, modelJobListener.m_stati);
    assertTrue(clientJobListener.m_stati.isEmpty());

    // verify Futures
    List<IFuture<?>> expectedFutures = new ArrayList<>();
    expectedFutures.add(outerFuture);
    expectedFutures.add(outerFuture);
    expectedFutures.add(innerFuture.getValue());
    expectedFutures.add(outerFuture);
    expectedFutures.add(innerFuture.getValue());
    expectedFutures.add(outerFuture);
    expectedFutures.add(innerFuture.getValue());
    expectedFutures.add(outerFuture);
    assertEquals(expectedFutures, modelJobListener.m_futures);
    assertTrue(clientJobListener.m_futures.isEmpty());

//    Assert.assertEquals(expectedStati.size(), listener.m_events.size());
//    for (JobChangeEvent evt : listener.m_events) {
//      Assert.assertEquals(expectedStati.remove(0), evt.getType());
//      Assert.assertSame(expectedFutures.remove(0), evt.getFuture());
//    }
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

  private class P_JobManager extends JobManager {

    private MutexSemaphores m_mutexSemaphores;

    /**
     * Blocks the current thread until the expected number of mutex-permits is acquired; Waits for maximal 30s.
     */
    public void waitForPermitsAcquired(Object mutexObject, int expectedPermitCount) throws InterruptedException {
      long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

      // Wait until the other jobs tried to re-acquire the mutex.
      while (m_mutexSemaphores.getPermitCount(mutexObject) != expectedPermitCount) {
        if (System.currentTimeMillis() > deadline) {
          fail(String.format("Timeout elapsed while waiting for a mutex-permit count. [expectedPermitCount=%s, actualPermitCount=%s]", expectedPermitCount, m_mutexSemaphores.getPermitCount(mutexObject)));
        }
        Thread.sleep(10);
      }
    }
  }
}
