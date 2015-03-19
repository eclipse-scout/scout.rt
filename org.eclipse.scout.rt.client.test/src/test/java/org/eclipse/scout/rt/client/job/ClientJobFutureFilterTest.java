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

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.job.IFuture;
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

    ClientJobInput clientJobInput = ClientJobInput.empty().setSession(m_clientSession1);
    when(m_clientJobFuture.getJobInput()).thenReturn(clientJobInput);

    ModelJobInput modelJobInput = ModelJobInput.empty().setSession(m_clientSession1);
    when(m_modelJobFuture.getJobInput()).thenReturn(modelJobInput);

    JobInput jobInput = JobInput.empty();
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testJobType() {
    assertTrue(ClientJobFutureFilters.newFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().clientJobsOnly().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().modelJobsOnly().accept(m_clientJobFuture));

    assertTrue(ClientJobFutureFilters.newFilter().accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().clientJobsOnly().accept(m_modelJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().modelJobsOnly().accept(m_modelJobFuture));

    assertFalse(ClientJobFutureFilters.newFilter().accept(m_jobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().clientJobsOnly().accept(m_jobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().modelJobsOnly().accept(m_jobFuture));
  }

  @Test
  public void testBlocked() {
    when(m_clientJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.newFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().blocked().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().notBlocked().accept(m_clientJobFuture));

    when(m_modelJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.newFilter().accept(m_modelJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().blocked().accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().notBlocked().accept(m_modelJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_clientJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.newFilter().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().periodic().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().notPeriodic().accept(m_clientJobFuture));

    when(m_modelJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ClientJobFutureFilters.newFilter().accept(m_modelJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().periodic().accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().notPeriodic().accept(m_modelJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(ClientJobFutureFilters.newFilter().session(m_clientSession1).accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().session(m_clientSession1).accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().session(m_clientSession2).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().session(m_clientSession2).accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertTrue(ClientJobFutureFilters.newFilter().currentSession().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().currentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertFalse(ClientJobFutureFilters.newFilter().currentSession().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().currentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_clientSession1);
    assertFalse(ClientJobFutureFilters.newFilter().notCurrentSession().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().notCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.set(m_clientSession2);
    assertTrue(ClientJobFutureFilters.newFilter().notCurrentSession().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().notCurrentSession().accept(m_modelJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(ClientJobFutureFilters.newFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().futures(m_clientJobFuture, m_modelJobFuture).accept(m_modelJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().futures(m_modelJobFuture).accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().futures(m_clientJobFuture).accept(m_modelJobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertTrue(ClientJobFutureFilters.newFilter().currentFuture().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().currentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertFalse(ClientJobFutureFilters.newFilter().currentFuture().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().currentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);
    assertFalse(ClientJobFutureFilters.newFilter().notCurrentFuture().accept(m_clientJobFuture));
    assertTrue(ClientJobFutureFilters.newFilter().notCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.set(m_modelJobFuture);
    assertTrue(ClientJobFutureFilters.newFilter().notCurrentFuture().accept(m_clientJobFuture));
    assertFalse(ClientJobFutureFilters.newFilter().notCurrentFuture().accept(m_modelJobFuture));
    IFuture.CURRENT.remove();
  }
}
