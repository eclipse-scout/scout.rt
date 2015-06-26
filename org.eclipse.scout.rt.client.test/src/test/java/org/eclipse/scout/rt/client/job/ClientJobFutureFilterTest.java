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
import org.eclipse.scout.rt.platform.job.JobFutureFilters;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ClientJobFutureFilterTest {

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
    assertTrue(Jobs.newFutureFilter().accept(m_jobFuture));
    assertFalse(ClientJobs.newFutureFilter().accept(m_jobFuture));
    assertFalse(ModelJobs.newFutureFilter().accept(m_jobFuture));
    assertFalse(new OrFilter<>(ClientJobs.newFutureFilter(), ModelJobs.newFutureFilter()).accept(m_jobFuture));

    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().accept(m_clientJobFuture));

    assertFalse(ClientJobs.newFutureFilter().accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().accept(m_modelJobFuture));
  }

  @Test
  public void testBlocked() {
    when(m_clientJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().andAreBlocked().accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andAreNotBlocked().accept(m_clientJobFuture));

    when(m_modelJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ModelJobs.newFutureFilter().accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andAreBlocked().accept(m_modelJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andAreNotBlocked().accept(m_modelJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().andArePeriodic().accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andAreNotPeriodic().accept(m_clientJobFuture));

    when(m_modelJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ModelJobs.newFutureFilter().accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andArePeriodic().accept(m_modelJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andAreNotPeriodic().accept(m_modelJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(ClientJobs.newFutureFilter().andMatchSession(m_clientSession1).accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andMatchSession(m_clientSession1).accept(m_modelJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchSession(m_clientSession2).accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchSession(m_clientSession2).accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertTrue(ClientJobs.newFutureFilter().andMatchCurrentSession().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andMatchCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertFalse(ClientJobs.newFutureFilter().andMatchCurrentSession().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertFalse(ClientJobs.newFutureFilter().andMatchNotCurrentSession().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchNotCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertTrue(ClientJobs.newFutureFilter().andMatchNotCurrentSession().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andMatchNotCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(ClientJobs.newFutureFilter().andMatchFutures(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchFutures(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));
    assertTrue(new JobFutureFilters.FutureFilter(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));

    assertFalse(ClientJobs.newFutureFilter().andMatchFutures(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andMatchFutures(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));
    assertTrue(new JobFutureFilters.FutureFilter(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilter().andMatchFutures(m_modelJobFuture).accept(m_modelJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchFutures(m_modelJobFuture).accept(m_modelJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchFutures(m_clientJobFuture).accept(m_modelJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchFutures(m_clientJobFuture).accept(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilter().andMatchFutures(m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchFutures(m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchFutures(m_clientJobFuture).accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().andMatchFutures(m_clientJobFuture).accept(m_clientJobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertTrue(ClientJobs.newFutureFilter().andMatchCurrentFuture().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertFalse(ClientJobs.newFutureFilter().andMatchCurrentFuture().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andMatchCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertFalse(ClientJobs.newFutureFilter().andMatchNotCurrentFuture().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().andMatchNotCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertTrue(ClientJobs.newFutureFilter().andMatchNotCurrentFuture().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().andMatchNotCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    m_clientJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchMutex(null).accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().andMatchMutex(mutexObject1).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchMutex(mutexObject2).accept(m_clientJobFuture));

    m_clientJobFuture.getJobInput().mutex(null);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().andMatchMutex(null).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchMutex(mutexObject1).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().andMatchMutex(mutexObject2).accept(m_clientJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(ClientJobs.newFutureFilter().andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_clientJobFuture));

    // True Filter
    assertTrue(ClientJobs.newFutureFilter().andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).accept(m_clientJobFuture));

    // True/False Filter
    assertFalse(ClientJobs.newFutureFilter().andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_clientJobFuture));
  }
}
