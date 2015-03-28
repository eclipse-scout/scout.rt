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
    assertTrue(ClientJobs.newFutureFilter().blocked().accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().notBlocked().accept(m_clientJobFuture));

    when(m_modelJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ModelJobs.newFutureFilter().accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().blocked().accept(m_modelJobFuture));
    assertFalse(ModelJobs.newFutureFilter().notBlocked().accept(m_modelJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().periodic().accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().notPeriodic().accept(m_clientJobFuture));

    when(m_modelJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ModelJobs.newFutureFilter().accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().periodic().accept(m_modelJobFuture));
    assertFalse(ModelJobs.newFutureFilter().notPeriodic().accept(m_modelJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(ClientJobs.newFutureFilter().session(m_clientSession1).accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().session(m_clientSession1).accept(m_modelJobFuture));
    assertFalse(ClientJobs.newFutureFilter().session(m_clientSession2).accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().session(m_clientSession2).accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertTrue(ClientJobs.newFutureFilter().currentSession().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().currentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertFalse(ClientJobs.newFutureFilter().currentSession().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().currentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertFalse(ClientJobs.newFutureFilter().notCurrentSession().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().notCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertTrue(ClientJobs.newFutureFilter().notCurrentSession().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().notCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(ClientJobs.newFutureFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));
    assertTrue(new JobFutureFilters.FutureFilter(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));

    assertFalse(ClientJobs.newFutureFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));
    assertTrue(ModelJobs.newFutureFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));
    assertTrue(new JobFutureFilters.FutureFilter(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilter().futures(m_modelJobFuture).accept(m_modelJobFuture));
    assertFalse(ClientJobs.newFutureFilter().futures(m_modelJobFuture).accept(m_modelJobFuture));
    assertFalse(ModelJobs.newFutureFilter().futures(m_clientJobFuture).accept(m_modelJobFuture));
    assertFalse(ClientJobs.newFutureFilter().futures(m_clientJobFuture).accept(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilter().futures(m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().futures(m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().futures(m_clientJobFuture).accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().futures(m_clientJobFuture).accept(m_clientJobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertTrue(ClientJobs.newFutureFilter().currentFuture().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().currentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertFalse(ClientJobs.newFutureFilter().currentFuture().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().currentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertFalse(ClientJobs.newFutureFilter().notCurrentFuture().accept(m_clientJobFuture));
    assertTrue(ModelJobs.newFutureFilter().notCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertTrue(ClientJobs.newFutureFilter().notCurrentFuture().accept(m_clientJobFuture));
    assertFalse(ModelJobs.newFutureFilter().notCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    m_clientJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().mutex(null).accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().mutex(mutexObject1).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().mutex(mutexObject2).accept(m_clientJobFuture));

    m_clientJobFuture.getJobInput().mutex(null);
    assertTrue(ClientJobs.newFutureFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobs.newFutureFilter().mutex(null).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().mutex(mutexObject1).accept(m_clientJobFuture));
    assertFalse(ClientJobs.newFutureFilter().mutex(mutexObject2).accept(m_clientJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(ClientJobs.newFutureFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_clientJobFuture));

    // True Filter
    assertTrue(ClientJobs.newFutureFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).accept(m_clientJobFuture));

    // True/False Filter
    assertFalse(ClientJobs.newFutureFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_clientJobFuture));
  }
}
