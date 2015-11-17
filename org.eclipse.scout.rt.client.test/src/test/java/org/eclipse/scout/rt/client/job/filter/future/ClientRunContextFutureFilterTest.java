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
package org.eclipse.scout.rt.client.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextFutureFilterTest {

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

    JobInput clientJobInput = Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(m_clientSession1, true));
    when(m_clientJobFuture.getJobInput()).thenReturn(clientJobInput);

    JobInput modelJobInput = ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true));
    when(m_modelJobFuture.getJobInput()).thenReturn(modelJobInput);

    JobInput jobInput = Jobs.newInput().withRunContext(RunContexts.empty());
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testBlocked() {
    when(m_clientJobFuture.isBlocked()).thenReturn(true);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andAreBlocked()
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andAreNotBlocked()
        .toFilter()
        .accept(m_clientJobFuture));

    when(m_modelJobFuture.isBlocked()).thenReturn(true);

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .toFilter()
        .accept(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andAreBlocked()
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andAreNotBlocked()
        .toFilter()
        .accept(m_modelJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.getSchedulingRule()).thenReturn(JobInput.SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andArePeriodicExecuting()
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andAreSingleExecuting()
        .toFilter()
        .accept(m_clientJobFuture));

    when(m_modelJobFuture.getSchedulingRule()).thenReturn(JobInput.SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE);

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .toFilter()
        .accept(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andArePeriodicExecuting()
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andAreSingleExecuting()
        .toFilter()
        .accept(m_modelJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class).andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder().andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_modelJobFuture));

    ISession.CURRENT.set(m_clientSession2);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class).andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchNot(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_modelJobFuture));

    ISession.CURRENT.set(m_clientSession2);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchNot(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(new FutureFilter(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(ModelJobFutureFilter.INSTANCE)
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .accept(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .accept(m_modelJobFuture));

    assertTrue(new FutureFilter(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(ModelJobFutureFilter.INSTANCE)
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .accept(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .accept(m_clientJobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_modelJobFuture));

    IFuture.CURRENT.set(m_modelJobFuture);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_modelJobFuture));

    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_modelJobFuture));

    IFuture.CURRENT.set(m_modelJobFuture);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .accept(m_modelJobFuture));

    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    m_clientJobFuture.getJobInput().withMutex(mutexObject1);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutexObject1)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutexObject2)
        .toFilter()
        .accept(m_clientJobFuture));

    m_clientJobFuture.getJobInput().withMutex(null);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .accept(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutexObject1)
        .toFilter()
        .accept(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchMutex(mutexObject2)
        .toFilter()
        .accept(m_clientJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).toFilter().accept(m_clientJobFuture));

    // True Filter
    assertTrue(Jobs.newFutureFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).toFilter().accept(m_clientJobFuture));

    // True/False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).toFilter().accept(m_clientJobFuture));
  }
}
