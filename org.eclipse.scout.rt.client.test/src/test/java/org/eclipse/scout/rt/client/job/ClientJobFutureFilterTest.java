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
import org.eclipse.scout.rt.platform.job.JobFutureFilters;
import org.eclipse.scout.rt.platform.job.JobInput;
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

    ClientJobInput clientJobInput = ClientJobInput.fillEmpty().session(m_clientSession1);
    when(m_clientJobFuture.getJobInput()).thenReturn(clientJobInput);

    ModelJobInput modelJobInput = ModelJobInput.fillEmpty().session(m_clientSession1);
    when(m_modelJobFuture.getJobInput()).thenReturn(modelJobInput);

    JobInput jobInput = JobInput.fillEmpty();
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(JobFutureFilters.allFilter().accept(m_jobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().accept(m_jobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
  }

  @Test
  public void testJobType() {
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().clientJobsOnly().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().modelJobsOnly().accept(m_clientJobFuture));

    assertTrue(ClientJobFutureFilters.allFilter().accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().clientJobsOnly().accept(m_modelJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().modelJobsOnly().accept(m_modelJobFuture));

    assertFalse(ClientJobFutureFilters.allFilter().accept(m_jobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().clientJobsOnly().accept(m_jobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().modelJobsOnly().accept(m_jobFuture));
  }

  @Test
  public void testBlocked() {
    when(m_clientJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().blocked().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().notBlocked().accept(m_clientJobFuture));

    when(m_modelJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_modelJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().blocked().accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().notBlocked().accept(m_modelJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().periodic().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().notPeriodic().accept(m_clientJobFuture));

    when(m_modelJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_modelJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().periodic().accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().notPeriodic().accept(m_modelJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(ClientJobFutureFilters.allFilter().session(m_clientSession1).accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().session(m_clientSession1).accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().session(m_clientSession2).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().session(m_clientSession2).accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertTrue(ClientJobFutureFilters.allFilter().currentSession().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().currentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertFalse(ClientJobFutureFilters.allFilter().currentSession().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().currentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertFalse(ClientJobFutureFilters.allFilter().notCurrentSession().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().notCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertTrue(ClientJobFutureFilters.allFilter().notCurrentSession().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().notCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(ClientJobFutureFilters.allFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().futures(m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().futures(m_clientJobFuture).accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertTrue(ClientJobFutureFilters.allFilter().currentFuture().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().currentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertFalse(ClientJobFutureFilters.allFilter().currentFuture().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().currentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertFalse(ClientJobFutureFilters.allFilter().notCurrentFuture().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().notCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertTrue(ClientJobFutureFilters.allFilter().notCurrentFuture().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().notCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    m_clientJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().mutex(null).accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().mutex(mutexObject1).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().mutex(mutexObject2).accept(m_clientJobFuture));

    m_clientJobFuture.getJobInput().mutex(null);
    assertTrue(ClientJobFutureFilters.allFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.allFilter().mutex(null).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().mutex(mutexObject1).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.allFilter().mutex(mutexObject2).accept(m_clientJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(ClientJobFutureFilters.allFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_clientJobFuture));

    // True Filter
    assertTrue(ClientJobFutureFilters.allFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).accept(m_clientJobFuture));

    // True/False Filter
    assertFalse(ClientJobFutureFilters.allFilter().andFilter(new IFilter<IFuture<?>>() {

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
