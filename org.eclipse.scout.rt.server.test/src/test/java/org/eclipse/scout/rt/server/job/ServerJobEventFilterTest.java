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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerJobEventFilterTest {

  @Mock
  private IJobManager m_jobManager;
  @Mock
  private IFuture<?> m_serverJobFuture;
  @Mock
  private IFuture<?> m_jobFuture;
  @Mock
  private IServerSession m_serverSession1;
  @Mock
  private IServerSession m_serverSession2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    JobInput serverJobInput = ServerJobs.newInput(ServerRunContexts.empty().session(m_serverSession1));
    when(m_serverJobFuture.getJobInput()).thenReturn(serverJobInput);

    JobInput jobInput = Jobs.newInput(RunContexts.empty());
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(Jobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture)));
    assertFalse(ServerJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));
    assertTrue(ServerJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture)));
  }

  @Test
  public void testEventTypes() {
    assertFalse(ServerJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture)));
    assertTrue(ServerJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture)));
    assertTrue(ServerJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.DONE, m_serverJobFuture)));
    assertFalse(ServerJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SHUTDOWN, m_serverJobFuture)));
  }

  @Test
  public void testJobType() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture);
    assertTrue(ServerJobs.newEventFilter().accept(serverEvent));

    JobEvent jobEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture);
    assertFalse(ServerJobs.newEventFilter().accept(jobEvent));
  }

  @Test
  public void testPeriodic() {
    when(m_serverJobFuture.isPeriodic()).thenReturn(true);
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture);

    assertTrue(ServerJobs.newEventFilter().accept(serverEvent));
    assertTrue(ServerJobs.newEventFilter().periodic().accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().notPeriodic().accept(serverEvent));
  }

  @Test
  public void testSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture);

    assertTrue(ServerJobs.newEventFilter().session(m_serverSession1).accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().session(m_serverSession2).accept(serverEvent));
  }

  @Test
  public void testShutdownEvent() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN, null);

    assertTrue(ServerJobs.newEventFilter().session(m_serverSession1).accept(serverEvent));
    assertTrue(ServerJobs.newEventFilter().session(m_serverSession2).accept(serverEvent));

    assertFalse(ServerJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_serverSession1).accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_serverSession2).accept(serverEvent));
  }

  @Test
  public void testCurrentSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    ISession.CURRENT.set(m_serverSession1);
    assertTrue(ServerJobs.newEventFilter().currentSession().accept(serverEvent));
    ISession.CURRENT.set(m_serverSession2);
    assertFalse(ServerJobs.newEventFilter().currentSession().accept(serverEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    ISession.CURRENT.set(m_serverSession1);
    assertFalse(ServerJobs.newEventFilter().notCurrentSession().accept(serverEvent));
    ISession.CURRENT.set(m_serverSession2);
    assertTrue(ServerJobs.newEventFilter().notCurrentSession().accept(serverEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    assertTrue(ServerJobs.newEventFilter().futures(m_serverJobFuture, m_jobFuture).accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().futures(m_jobFuture).accept(serverEvent));
  }

  @Test
  public void testCurrentFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    IFuture.CURRENT.remove();
    assertFalse(ServerJobs.newEventFilter().currentFuture().accept(serverEvent));

    IFuture.CURRENT.set(m_serverJobFuture);
    assertTrue(ServerJobs.newEventFilter().currentFuture().accept(serverEvent));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);
    JobEvent jobEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_jobFuture);

    IFuture.CURRENT.set(m_serverJobFuture);
    assertFalse(Jobs.newEventFilter().notCurrentFuture().accept(serverEvent));
    assertTrue(Jobs.newEventFilter().notCurrentFuture().accept(jobEvent));
    IFuture.CURRENT.remove();

    IFuture.CURRENT.set(m_jobFuture);
    assertTrue(Jobs.newEventFilter().notCurrentFuture().accept(serverEvent));
    assertFalse(Jobs.newEventFilter().notCurrentFuture().accept(jobEvent));
    IFuture.CURRENT.remove();

    assertTrue(Jobs.newEventFilter().notCurrentFuture().accept(serverEvent));
    assertTrue(Jobs.newEventFilter().notCurrentFuture().accept(jobEvent));
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);
    m_serverJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ServerJobs.newEventFilter().accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().mutex(null).accept(serverEvent));
    assertTrue(ServerJobs.newEventFilter().mutex(mutexObject1).accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().mutex(mutexObject2).accept(serverEvent));

    serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);
    m_serverJobFuture.getJobInput().mutex(null);
    assertTrue(ServerJobs.newEventFilter().accept(serverEvent));
    assertTrue(ServerJobs.newEventFilter().mutex(null).accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().mutex(mutexObject1).accept(serverEvent));
    assertFalse(ServerJobs.newEventFilter().mutex(mutexObject2).accept(serverEvent));
  }

  @Test
  public void testCustomFilter() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    // False Filter
    assertFalse(ServerJobs.newEventFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).accept(serverEvent));

    // True Filter
    assertTrue(ServerJobs.newEventFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).accept(serverEvent));

    // True/False Filter
    assertFalse(ServerJobs.newEventFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).accept(serverEvent));
  }
}
