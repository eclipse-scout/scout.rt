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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerJobFutureFilterTest {

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

    JobInput serverJobInput = ServerJobs.newInput(ServerRunContexts.empty().session(m_serverSession1));
    when(m_serverJobFuture.getJobInput()).thenReturn(serverJobInput);

    JobInput jobInput = Jobs.newInput(RunContexts.empty());
    when(m_jobFuture.getJobInput()).thenReturn(jobInput);
  }

  @Test
  public void testEmptyFilter() {
    assertTrue(Jobs.newFutureFilter().accept(m_jobFuture));
    assertFalse(ServerJobs.newFutureFilter().accept(m_jobFuture));
    assertTrue(ServerJobs.newFutureFilter().accept(m_serverJobFuture));
  }

  @Test
  public void testBlocked() {
    when(m_serverJobFuture.isBlocked()).thenReturn(true);
    assertTrue(ServerJobs.newFutureFilter().accept(m_serverJobFuture));
    assertTrue(ServerJobs.newFutureFilter().blocked().accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().notBlocked().accept(m_serverJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_serverJobFuture.isPeriodic()).thenReturn(true);
    assertTrue(ServerJobs.newFutureFilter().accept(m_serverJobFuture));
    assertTrue(ServerJobs.newFutureFilter().periodic().accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().notPeriodic().accept(m_serverJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(ServerJobs.newFutureFilter().session(m_serverSession1).accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().session(m_serverSession2).accept(m_serverJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_serverSession1);
    assertTrue(ServerJobs.newFutureFilter().currentSession().accept(m_serverJobFuture));
    ISession.CURRENT.set(m_serverSession2);
    assertFalse(ServerJobs.newFutureFilter().currentSession().accept(m_serverJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_serverSession1);
    assertFalse(ServerJobs.newFutureFilter().notCurrentSession().accept(m_serverJobFuture));
    ISession.CURRENT.set(m_serverSession2);
    assertTrue(ServerJobs.newFutureFilter().notCurrentSession().accept(m_serverJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(ServerJobs.newFutureFilter().futures(m_serverJobFuture).accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().futures(mock(IFuture.class)).accept(m_jobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_serverJobFuture);
    assertTrue(ServerJobs.newFutureFilter().currentFuture().accept(m_serverJobFuture));
    IFuture.CURRENT.set(m_jobFuture);
    assertFalse(ServerJobs.newFutureFilter().currentFuture().accept(m_serverJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_serverJobFuture);
    assertFalse(ServerJobs.newFutureFilter().notCurrentFuture().accept(m_serverJobFuture));
    IFuture.CURRENT.set(m_jobFuture);
    assertTrue(ServerJobs.newFutureFilter().notCurrentFuture().accept(m_serverJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutex() {
    Object mutexObject1 = new Object();
    Object mutexObject2 = new Object();

    m_serverJobFuture.getJobInput().mutex(mutexObject1);
    assertTrue(ServerJobs.newFutureFilter().accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().mutex(null).accept(m_serverJobFuture));
    assertTrue(ServerJobs.newFutureFilter().mutex(mutexObject1).accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().mutex(mutexObject2).accept(m_serverJobFuture));

    m_serverJobFuture.getJobInput().mutex(null);
    assertTrue(ServerJobs.newFutureFilter().accept(m_serverJobFuture));
    assertTrue(ServerJobs.newFutureFilter().mutex(null).accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().mutex(mutexObject1).accept(m_serverJobFuture));
    assertFalse(ServerJobs.newFutureFilter().mutex(mutexObject2).accept(m_serverJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(ServerJobs.newFutureFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_serverJobFuture));

    // True Filter
    assertTrue(ServerJobs.newFutureFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).accept(m_serverJobFuture));

    // True/False Filter
    assertFalse(ServerJobs.newFutureFilter().andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).andFilter(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).accept(m_serverJobFuture));
  }
}
