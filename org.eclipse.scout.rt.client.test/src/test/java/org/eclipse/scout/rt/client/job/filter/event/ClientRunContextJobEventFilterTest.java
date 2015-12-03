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
package org.eclipse.scout.rt.client.job.filter.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.event.SessionJobEventFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextJobEventFilterTest {

  private IFuture<?> m_clientJobFuture;

  private IFuture<?> m_modelJobFuture;

  private IClientSession m_clientSession1;

  private IClientSession m_clientSession2;

  @Before
  public void before() {
    m_clientSession1 = mock(IClientSession.class);
    when(m_clientSession1.getModelJobMutex()).thenReturn(Jobs.newMutex());

    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getModelJobMutex()).thenReturn(Jobs.newMutex());

    m_clientJobFuture = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(m_clientSession1, true)));

    m_modelJobFuture = ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true)));
  }

  @Test
  public void testEventTypes() {
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(Jobs.getJobManager(), JobEventType.SCHEDULED).withFuture(m_clientJobFuture)));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture)));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.DONE)
        .toFilter()
        .accept(new JobEvent(Jobs.getJobManager(), JobEventType.DONE).withFuture(m_clientJobFuture)));

    assertFalse(
        Jobs.newEventFilterBuilder()
            .andMatchRunContext(ClientRunContext.class)
            .andMatchEventType(
                JobEventType.ABOUT_TO_RUN,
                JobEventType.DONE)
            .toFilter()
            .accept(new JobEvent(Jobs.getJobManager(), JobEventType.SHUTDOWN).withFuture(m_clientJobFuture)));
  }

  @Test
  public void testPeriodic() {
    IFuture<Void> clientJobFuture = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withPeriodicExecutionAtFixedRate(1, TimeUnit.SECONDS));

    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.SCHEDULED).withFuture(clientJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(clientEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andArePeriodicExecuting()
        .toFilter()
        .accept(clientEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andAreSingleExecuting()
        .toFilter()
        .accept(clientEvent));

    IFuture<Void> modelJobFuture = ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withPeriodicExecutionAtFixedRate(1, TimeUnit.SECONDS));
    JobEvent modelEvent = new JobEvent(Jobs.getJobManager(), JobEventType.SCHEDULED).withFuture(modelJobFuture);

    assertTrue(ModelJobs.newEventFilterBuilder()
        .toFilter()
        .accept(modelEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andArePeriodicExecuting()
        .toFilter()
        .accept(modelEvent));

    assertFalse(ModelJobs.newEventFilterBuilder()
        .andAreSingleExecuting()
        .toFilter()
        .accept(modelEvent));

    // cleanup
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchFuture(clientJobFuture, modelJobFuture)
        .toFilter(), true);

  }

  @Test
  public void testSession() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.SCHEDULED).withFuture(m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(Jobs.getJobManager(), JobEventType.SCHEDULED).withFuture(m_modelJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionJobEventFilter(m_clientSession1))
        .toFilter()
        .accept(clientEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andMatch(new SessionJobEventFilter(m_clientSession1))
        .toFilter()
        .accept(modelEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionJobEventFilter(m_clientSession2))
        .toFilter()
        .accept(clientEvent));

    assertFalse(ModelJobs.newEventFilterBuilder()
        .andMatch(new SessionJobEventFilter(m_clientSession2))
        .toFilter()
        .accept(modelEvent));
  }

  @Test
  public void testShutdownEvent() {
    JobEvent shutdownEvent = new JobEvent(Jobs.getJobManager(), JobEventType.SHUTDOWN);

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionJobEventFilter(m_clientSession1))
        .toFilter()
        .accept(shutdownEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchEventType(JobEventType.ABOUT_TO_RUN)
        .andMatch(new SessionJobEventFilter(m_clientSession1))
        .toFilter()
        .accept(shutdownEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.SHUTDOWN)
        .toFilter()
        .accept(shutdownEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchEventType(
            JobEventType.ABOUT_TO_RUN,
            JobEventType.SHUTDOWN)
        .toFilter()
        .accept(shutdownEvent));
  }

  @Test
  public void testCurrentSession() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_modelJobFuture);

    ISession.CURRENT.set(m_clientSession1);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(clientEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andMatch(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(modelEvent));

    ISession.CURRENT.set(m_clientSession2);

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(clientEvent));

    assertFalse(ModelJobs.newEventFilterBuilder()
        .andMatch(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(modelEvent));

    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_modelJobFuture);

    ISession.CURRENT.set(m_clientSession1);
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(clientEvent));

    assertFalse(ModelJobs.newEventFilterBuilder()
        .andMatchNot(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(modelEvent));

    ISession.CURRENT.set(m_clientSession2);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(clientEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andMatchNot(new SessionJobEventFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(modelEvent));

    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_modelJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .accept(clientEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .accept(modelEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .accept(clientEvent));

    assertFalse(ModelJobs.newEventFilterBuilder()
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .accept(modelEvent));
  }

  @Test
  public void testCurrentFuture() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);

    IFuture.CURRENT.remove();
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(clientEvent));

    IFuture.CURRENT.set(m_clientJobFuture);
    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(clientEvent));

    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);
    JobEvent modelEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_modelJobFuture);

    IFuture.CURRENT.set(m_clientJobFuture);
    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(clientEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(modelEvent));

    IFuture.CURRENT.remove();
    IFuture.CURRENT.set(m_modelJobFuture);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(clientEvent));

    assertFalse(ModelJobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(modelEvent));

    IFuture.CURRENT.remove();

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(clientEvent));

    assertTrue(ModelJobs.newEventFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(modelEvent));
  }

  @Test
  public void testMutex() {
    IMutex mutex1 = Jobs.newMutex();
    IMutex mutex2 = Jobs.newMutex();

    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);
    m_clientJobFuture.getJobInput().withMutex(mutex1);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(clientEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(clientEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutex1)
        .toFilter()
        .accept(clientEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutex2)
        .toFilter()
        .accept(clientEvent));

    clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);
    m_clientJobFuture.getJobInput().withMutex(null);

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(clientEvent));

    assertTrue(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(clientEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutex1)
        .toFilter()
        .accept(clientEvent));

    assertFalse(Jobs.newEventFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutex2)
        .toFilter()
        .accept(clientEvent));
  }

  @Test
  public void testCustomFilter() {
    JobEvent clientEvent = new JobEvent(Jobs.getJobManager(), JobEventType.ABOUT_TO_RUN).withFuture(m_clientJobFuture);

    // False Filter
    assertFalse(Jobs.newEventFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).toFilter().accept(clientEvent));

    // True Filter
    assertTrue(Jobs.newEventFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).toFilter().accept(clientEvent));

    // True/False Filter
    assertFalse(Jobs.newEventFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return true;
      }
    }).andMatch(new IFilter<JobEvent>() {

      @Override
      public boolean accept(JobEvent event) {
        return false;
      }
    }).toFilter().accept(clientEvent));
  }
}
