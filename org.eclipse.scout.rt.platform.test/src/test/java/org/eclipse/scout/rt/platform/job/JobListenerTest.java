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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.job.filter.FutureFilter;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobListenerTest {

  private IJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testEvents() throws Exception {
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(listener, new AlwaysFilter<JobEvent>());

    IFuture<Void> future = null;
    JobInput input = JobInput.empty();
    future = m_jobManager.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
      }
    }, input);
    m_jobManager.awaitDone(new FutureFilter(future), 1, TimeUnit.MINUTES);
    m_jobManager.shutdown();
    m_jobManager.removeListener(listener);

    List<JobEventType> expectedStati = new ArrayList<>();
    expectedStati.add(JobEventType.SCHEDULED);
    expectedStati.add(JobEventType.ABOUT_TO_RUN);
    expectedStati.add(JobEventType.DONE);
    expectedStati.add(JobEventType.SHUTDOWN);
    assertEquals(expectedStati, listener.m_eventTypes);

    List<IFuture<Void>> expectedFutures = new ArrayList<>();
    expectedFutures.add(future); // scheduled
    expectedFutures.add(future); // about to run
    expectedFutures.add(future); // done
    expectedFutures.add(null); // shutdown
    assertEquals(expectedFutures, listener.m_futures);
  }

  @Test
  public void testCancel() throws Exception {
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(listener, new AlwaysFilter<JobEvent>());
    final BooleanHolder hasStarted = new BooleanHolder(Boolean.FALSE);
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        hasStarted.setValue(Boolean.TRUE);
      }
    }, 200, TimeUnit.MILLISECONDS, JobInput.empty());
    future.cancel(true);
    m_jobManager.awaitDone(new FutureFilter(future), 1, TimeUnit.MINUTES);
    m_jobManager.removeListener(listener);
    m_jobManager.shutdown();

    Assert.assertFalse(hasStarted.getValue().booleanValue());
    assertEquals(Collections.singletonList(JobEventType.SCHEDULED), listener.m_eventTypes); // cancelled tasks are in state 'done' --> same behavior as Java Futures
    assertEquals(Collections.singletonList(future), listener.m_futures);
    assertTrue(future.isCancelled());
  }

  private static final class P_JobChangeListener implements IJobListener {

    private final List<JobEventType> m_eventTypes = new ArrayList<>();
    private final List<IFuture<?>> m_futures = new ArrayList<>();

    @Override
    public void changed(JobEvent event) {
      m_eventTypes.add(event.getType());
      m_futures.add(event.getFuture());
    }
  }
}
