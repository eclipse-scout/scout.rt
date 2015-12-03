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
package org.eclipse.scout.rt.server.job.filter.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.event.SessionJobEventFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextJobEventFilterTest {

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

    JobInput serverJobInput = Jobs.newInput()
        .withRunContext(ServerRunContexts.empty().withSession(m_serverSession1));
    when(m_serverJobFuture.getJobInput()).thenReturn(serverJobInput);

    JobInput jobInput = Jobs.newInput().withRunContext(RunContexts.empty());
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(Jobs.newEventFilterBuilder().toFilter().accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(m_serverJobFuture)));
  }

  @Test
  public void testEventTypes() {
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(m_serverJobFuture)));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture)));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(m_jobManager, JobEventType.DONE).withFuture(m_serverJobFuture)));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(m_jobManager, JobEventType.SHUTDOWN).withFuture(m_serverJobFuture)));
  }

  @Test
  public void testJobType() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(m_serverJobFuture);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(serverEvent));

    JobEvent jobEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(m_jobFuture);
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(jobEvent));
  }

  @Test
  public void testPeriodic() {
    when(m_serverJobFuture.getSchedulingRule()).thenReturn(JobInput.SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE);
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(m_serverJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(serverEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andArePeriodicExecuting()
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andAreSingleExecuting()
        .toFilter()
        .accept(serverEvent));
  }

  @Test
  public void testSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(m_serverJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionJobEventFilter(m_serverSession1))
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionJobEventFilter(m_serverSession2))
        .toFilter()
        .accept(serverEvent));
  }

  @Test
  public void testShutdownEvent() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.SHUTDOWN);

    assertTrue(Jobs.newEventFilterBuilder()
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(serverEvent));
  }

  @Test
  public void testCurrentSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);

    ISession.CURRENT.set(m_serverSession1);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(serverEvent));

    ISession.CURRENT.set(m_serverSession2);
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(serverEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);

    ISession.CURRENT.set(m_serverSession1);
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(serverEvent));
    ISession.CURRENT.set(m_serverSession2);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(serverEvent));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(m_serverJobFuture, m_jobFuture)
        .toFilter()
        .accept(serverEvent));
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(m_jobFuture)
        .toFilter()
        .accept(serverEvent));
  }

  @Test
  public void testCurrentFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);

    IFuture.CURRENT.remove();
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(serverEvent));

    IFuture.CURRENT.set(m_serverJobFuture);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(serverEvent));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);
    JobEvent jobEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_jobFuture);

    IFuture.CURRENT.set(m_serverJobFuture);
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(serverEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(jobEvent));
    IFuture.CURRENT.remove();

    IFuture.CURRENT.set(m_jobFuture);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(jobEvent));
    IFuture.CURRENT.remove();

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(serverEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(jobEvent));
  }

  @Test
  public void testMutex() {
    IMutex mutex1 = Jobs.newMutex();
    IMutex mutex2 = Jobs.newMutex();

    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);
    m_serverJobFuture.getJobInput().withMutex(mutex1);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(serverEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex1)
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex2)
        .toFilter()
        .accept(serverEvent));

    serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);
    m_serverJobFuture.getJobInput().withMutex(null);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(serverEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex1)
        .toFilter()
        .accept(serverEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex2)
        .toFilter()
        .accept(serverEvent));
  }

  @Test
  public void testCustomFilter() {
    JobEvent serverEvent = new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN).withFuture(m_serverJobFuture);

    // False Filter
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class).andMatch(new IFilter<JobEvent>() {

          @Override
          public boolean accept(JobEvent event) {
            return false;
          }
        })
        .toFilter()
        .accept(serverEvent));

    // True Filter
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class).andMatch(new IFilter<JobEvent>() {

          @Override
          public boolean accept(JobEvent event) {
            return true;
          }
        })
        .toFilter()
        .accept(serverEvent));

    // True/False Filter
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ServerRunContext.class).andMatch(new IFilter<JobEvent>() {

          @Override
          public boolean accept(JobEvent event) {
            return true;
          }
        }).andMatch(new IFilter<JobEvent>() {

          @Override
          public boolean accept(JobEvent event) {
            return false;
          }
        })
        .toFilter()
        .accept(serverEvent));
  }
}
