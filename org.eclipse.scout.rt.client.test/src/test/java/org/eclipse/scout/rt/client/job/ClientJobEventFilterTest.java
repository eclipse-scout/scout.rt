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
import org.eclipse.scout.commons.filter.OrFilter;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
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

    JobInput clientJobInput = ClientJobs.newInput(ClientRunContexts.empty().session(m_clientSession1));
    when(m_clientJobFuture.getJobInput()).thenReturn(clientJobInput);

    JobInput modelJobInput = ModelJobs.newInput(ClientRunContexts.empty().session(m_clientSession1));
    when(m_modelJobFuture.getJobInput()).thenReturn(modelJobInput);

    JobInput jobInput = Jobs.newInput(RunContexts.empty());
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(Jobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));
    assertFalse(ClientJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));
    assertFalse(ModelJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));
    assertFalse(new OrFilter<>(ClientJobs.newEventFilter(), ModelJobs.newEventFilter()).accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_jobFuture)));

    assertTrue(ClientJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture)));
    assertFalse(ModelJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture)));

    assertFalse(ClientJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture)));
    assertTrue(ModelJobs.newEventFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture)));
  }

  @Test
  public void testEventTypes() {
    assertFalse(ClientJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture)));
    assertTrue(ClientJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture)));
    assertTrue(ClientJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.DONE, m_clientJobFuture)));
    assertFalse(ClientJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN, JobEventType.DONE).accept(new JobEvent(m_jobManager, JobEventType.SHUTDOWN, m_clientJobFuture)));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.isPeriodic()).thenReturn(true);
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture);

    assertTrue(ClientJobs.newEventFilter().accept(clientEvent));
    assertTrue(ClientJobs.newEventFilter().periodic().accept(clientEvent));
    assertFalse(ClientJobs.newEventFilter().notPeriodic().accept(clientEvent));

    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture);
    when(m_modelJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ModelJobs.newEventFilter().accept(modelEvent));
    assertTrue(ModelJobs.newEventFilter().periodic().accept(modelEvent));
    assertFalse(ModelJobs.newEventFilter().notPeriodic().accept(modelEvent));
  }

  @Test
  public void testSession() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED, m_modelJobFuture);

    assertTrue(ClientJobs.newEventFilter().session(m_clientSession1).accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().session(m_clientSession1).accept(modelEvent));
    assertFalse(ClientJobs.newEventFilter().session(m_clientSession2).accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().session(m_clientSession2).accept(modelEvent));
  }

  @Test
  public void testShutdownEvent() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN, null);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN, null);

    assertTrue(ClientJobs.newEventFilter().session(m_clientSession1).accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().session(m_clientSession1).accept(modelEvent));
    assertTrue(ClientJobs.newEventFilter().session(m_clientSession2).accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().session(m_clientSession2).accept(modelEvent));

    assertFalse(ClientJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession1).accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession1).accept(modelEvent));
    assertFalse(ClientJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession2).accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().eventTypes(JobEventType.ABOUT_TO_RUN).session(m_clientSession2).accept(modelEvent));
  }

  @Test
  public void testCurrentSession() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    ISession.CURRENT.set(m_clientSession1);
    assertTrue(ClientJobs.newEventFilter().currentSession().accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().currentSession().accept(modelEvent));
    ISession.CURRENT.set(m_clientSession2);
    assertFalse(ClientJobs.newEventFilter().currentSession().accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().currentSession().accept(modelEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    ISession.CURRENT.set(m_clientSession1);
    assertFalse(ClientJobs.newEventFilter().notCurrentSession().accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().notCurrentSession().accept(modelEvent));
    ISession.CURRENT.set(m_clientSession2);
    assertTrue(ClientJobs.newEventFilter().notCurrentSession().accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().notCurrentSession().accept(modelEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    assertTrue(ClientJobs.newEventFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(modelEvent));
    assertFalse(ClientJobs.newEventFilter().futures(m_modelJobFuture).accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().futures(m_clientJobFuture).accept(modelEvent));
  }

  @Test
  public void testCurrentFuture() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);

    IFuture.CURRENT.remove();
    assertFalse(ClientJobs.newEventFilter().currentFuture().accept(clientEvent));

    IFuture.CURRENT.set(m_clientJobFuture);
    assertTrue(ClientJobs.newEventFilter().currentFuture().accept(clientEvent));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_modelJobFuture);

    IFuture.CURRENT.set(m_clientJobFuture);
    assertFalse(ClientJobs.newEventFilter().notCurrentFuture().accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().notCurrentFuture().accept(modelEvent));
    IFuture.CURRENT.remove();

    IFuture.CURRENT.set(m_modelJobFuture);
    assertTrue(ClientJobs.newEventFilter().notCurrentFuture().accept(clientEvent));
    assertFalse(ModelJobs.newEventFilter().notCurrentFuture().accept(modelEvent));
    IFuture.CURRENT.remove();

    assertTrue(ClientJobs.newEventFilter().notCurrentFuture().accept(clientEvent));
    assertTrue(ModelJobs.newEventFilter().notCurrentFuture().accept(modelEvent));
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    m_clientJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ClientJobs.newEventFilter().accept(clientEvent));
    assertFalse(ClientJobs.newEventFilter().mutex(null).accept(clientEvent));
    assertTrue(ClientJobs.newEventFilter().mutex(mutexObject1).accept(clientEvent));
    assertFalse(ClientJobs.newEventFilter().mutex(mutexObject2).accept(clientEvent));

    clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);
    m_clientJobFuture.getJobInput().mutex(null);
    assertTrue(ClientJobs.newEventFilter().accept(clientEvent));
    assertTrue(ClientJobs.newEventFilter().mutex(null).accept(clientEvent));
    assertFalse(ClientJobs.newEventFilter().mutex(mutexObject1).accept(clientEvent));
    assertFalse(ClientJobs.newEventFilter().mutex(mutexObject2).accept(clientEvent));
  }

  @Test
  public void testCustomFilter() {
    JobEvent clientEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, m_clientJobFuture);

    // False Filter
    assertFalse(ClientJobs.newEventFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).accept(clientEvent));

    // True Filter
    assertTrue(ClientJobs.newEventFilter().andFilter(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).accept(clientEvent));

    // True/False Filter
    assertFalse(ClientJobs.newEventFilter().andFilter(new IFilter<JobEvent>() {

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
