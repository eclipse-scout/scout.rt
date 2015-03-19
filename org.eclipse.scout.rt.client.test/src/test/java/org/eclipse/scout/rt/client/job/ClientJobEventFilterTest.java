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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobEventFilters;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ClientJobEventFilterTest {

  @Mock
  private IJobManager m_jobManager;
  @Mock
  private IFuture<?> m_clientJobFuture;
  @Mock
  private IFuture<?> m_modelJobFuture;
  @Mock
  private IFuture<?> m_jobFuture;
  @Mock
  private IClientSession m_clientSession1;
  @Mock
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    ClientJobInput clientJobInput = ClientJobInput.empty().session(m_clientSession1);
    when(m_clientJobFuture.getJobInput()).thenReturn(clientJobInput);

    ModelJobInput modelJobInput = ModelJobInput.empty().session(m_clientSession1);
    when(m_modelJobFuture.getJobInput()).thenReturn(modelJobInput);

    JobInput jobInput = JobInput.empty();
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(JobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture)));
    assertFalse(ClientJobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));
    assertTrue(ClientJobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture)));
    assertTrue(ClientJobEventFilters.allFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture)));
  }

  @Test
  public void testEventTypes() {
    assertFalse(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture)));
    assertTrue(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture)));
    assertTrue(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.DONE, m_clientJobFuture)));
    assertFalse(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SHUTDOWN, m_clientJobFuture)));
  }

  @Test
  public void testJobType() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture);
    assertTrue(ClientJobEventFilters.allFilter().accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().clientJobsOnly().accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().modelJobsOnly().accept(clientEvent));

    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture);
    assertTrue(ClientJobEventFilters.allFilter().accept(modelEvent));
    assertFalse(ClientJobEventFilters.allFilter().clientJobsOnly().accept(modelEvent));
    assertTrue(ClientJobEventFilters.allFilter().modelJobsOnly().accept(modelEvent));

    JobEvent jobEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture);
    assertFalse(ClientJobEventFilters.allFilter().accept(jobEvent));
    assertFalse(ClientJobEventFilters.allFilter().clientJobsOnly().accept(jobEvent));
    assertFalse(ClientJobEventFilters.allFilter().modelJobsOnly().accept(jobEvent));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.isPeriodic()).thenReturn(true);
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture);

    assertTrue(ClientJobEventFilters.allFilter().accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().periodic().accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().notPeriodic().accept(clientEvent));

    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture);
    when(m_modelJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobEventFilters.allFilter().accept(modelEvent));
    assertTrue(ClientJobEventFilters.allFilter().periodic().accept(modelEvent));
    assertFalse(ClientJobEventFilters.allFilter().notPeriodic().accept(modelEvent));
  }

  @Test
  public void testSession() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture);

    assertTrue(ClientJobEventFilters.allFilter().session(m_clientSession1).accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().session(m_clientSession1).accept(modelEvent));
    assertFalse(ClientJobEventFilters.allFilter().session(m_clientSession2).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().session(m_clientSession2).accept(modelEvent));
  }

  @Test
  public void testShutdownEvent() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN, null);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN, null);

    assertTrue(ClientJobEventFilters.allFilter().session(m_clientSession1).accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().session(m_clientSession1).accept(modelEvent));
    assertTrue(ClientJobEventFilters.allFilter().session(m_clientSession2).accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().session(m_clientSession2).accept(modelEvent));

    assertFalse(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession1).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession1).accept(modelEvent));
    assertFalse(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession2).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession2).accept(modelEvent));
  }

  @Test
  public void testCurrentSession() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    ISession.CURRENT.set(m_clientSession1);
    assertTrue(ClientJobEventFilters.allFilter().currentSession().accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().currentSession().accept(modelEvent));
    ISession.CURRENT.set(m_clientSession2);
    assertFalse(ClientJobEventFilters.allFilter().currentSession().accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().currentSession().accept(modelEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    ISession.CURRENT.set(m_clientSession1);
    assertFalse(ClientJobEventFilters.allFilter().notCurrentSession().accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().notCurrentSession().accept(modelEvent));
    ISession.CURRENT.set(m_clientSession2);
    assertTrue(ClientJobEventFilters.allFilter().notCurrentSession().accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().notCurrentSession().accept(modelEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    assertTrue(ClientJobEventFilters.allFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(modelEvent));
    assertFalse(ClientJobEventFilters.allFilter().futures(m_modelJobFuture).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().futures(m_clientJobFuture).accept(modelEvent));
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    m_clientJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ClientJobEventFilters.allFilter().accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().mutex(null).accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().mutex(mutexObject1).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().mutex(mutexObject2).accept(clientEvent));

    clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    m_clientJobFuture.getJobInput().mutex(null);
    assertTrue(ClientJobEventFilters.allFilter().accept(clientEvent));
    assertTrue(ClientJobEventFilters.allFilter().mutex(null).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().mutex(mutexObject1).accept(clientEvent));
    assertFalse(ClientJobEventFilters.allFilter().mutex(mutexObject2).accept(clientEvent));
  }

  @Test
  public void testCustomFilter() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);

    // False Filter
    assertFalse(ClientJobEventFilters.allFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).accept(clientEvent));

    // True Filter
    assertTrue(ClientJobEventFilters.allFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).accept(clientEvent));

    // True/False Filter
    assertFalse(ClientJobEventFilters.allFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).accept(clientEvent));
  }
}
