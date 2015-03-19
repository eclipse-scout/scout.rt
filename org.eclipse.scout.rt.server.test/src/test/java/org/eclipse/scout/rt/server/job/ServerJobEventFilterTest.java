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
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobEventFilters;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.server.IServerSession;
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

    ServerJobInput serverJobInput = ServerJobInput.empty().session(m_serverSession1);
    when(m_serverJobFuture.getJobInput()).thenReturn(serverJobInput);

    JobInput jobInput = JobInput.empty();
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(JobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture)));
    assertFalse(ServerJobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));
    assertTrue(ServerJobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture)));
  }

  @Test
  public void testEventTypes() {
    assertFalse(ServerJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture)));
    assertTrue(ServerJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture)));
    assertTrue(ServerJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.DONE, m_serverJobFuture)));
    assertFalse(ServerJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SHUTDOWN, m_serverJobFuture)));
  }

  @Test
  public void testJobType() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture);
    assertTrue(ServerJobEventFilters.allFilter().accept(serverEvent));

    JobEvent jobEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture);
    assertFalse(ServerJobEventFilters.allFilter().accept(jobEvent));
  }

  @Test
  public void testPeriodic() {
    when(m_serverJobFuture.isPeriodic()).thenReturn(true);
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture);

    assertTrue(ServerJobEventFilters.allFilter().accept(serverEvent));
    assertTrue(ServerJobEventFilters.allFilter().periodic().accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().notPeriodic().accept(serverEvent));
  }

  @Test
  public void testSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_serverJobFuture);

    assertTrue(ServerJobEventFilters.allFilter().session(m_serverSession1).accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().session(m_serverSession2).accept(serverEvent));
  }

  @Test
  public void testShutdownEvent() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN, null);

    assertTrue(ServerJobEventFilters.allFilter().session(m_serverSession1).accept(serverEvent));
    assertTrue(ServerJobEventFilters.allFilter().session(m_serverSession2).accept(serverEvent));

    assertFalse(ServerJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_serverSession1).accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_serverSession2).accept(serverEvent));
  }

  @Test
  public void testCurrentSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    ISession.CURRENT.set(m_serverSession1);
    assertTrue(ServerJobEventFilters.allFilter().currentSession().accept(serverEvent));
    ISession.CURRENT.set(m_serverSession2);
    assertFalse(ServerJobEventFilters.allFilter().currentSession().accept(serverEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    ISession.CURRENT.set(m_serverSession1);
    assertFalse(ServerJobEventFilters.allFilter().notCurrentSession().accept(serverEvent));
    ISession.CURRENT.set(m_serverSession2);
    assertTrue(ServerJobEventFilters.allFilter().notCurrentSession().accept(serverEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    assertTrue(ServerJobEventFilters.allFilter().futures(m_serverJobFuture, m_jobFuture).accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().futures(m_jobFuture).accept(serverEvent));
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);
    m_serverJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ServerJobEventFilters.allFilter().accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().mutex(null).accept(serverEvent));
    assertTrue(ServerJobEventFilters.allFilter().mutex(mutexObject1).accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().mutex(mutexObject2).accept(serverEvent));

    serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);
    m_serverJobFuture.getJobInput().mutex(null);
    assertTrue(ServerJobEventFilters.allFilter().accept(serverEvent));
    assertTrue(ServerJobEventFilters.allFilter().mutex(null).accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().mutex(mutexObject1).accept(serverEvent));
    assertFalse(ServerJobEventFilters.allFilter().mutex(mutexObject2).accept(serverEvent));
  }

  @Test
  public void testCustomFilter() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_serverJobFuture);

    // False Filter
    assertFalse(ServerJobEventFilters.allFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).accept(serverEvent));

    // True Filter
    assertTrue(ServerJobEventFilters.allFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).accept(serverEvent));

    // True/False Filter
    assertFalse(ServerJobEventFilters.allFilter().andFilter(new IFilter<JobEvent>() {

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
