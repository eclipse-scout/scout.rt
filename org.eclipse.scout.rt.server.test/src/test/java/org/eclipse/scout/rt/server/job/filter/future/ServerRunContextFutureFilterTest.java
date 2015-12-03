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
package org.eclipse.scout.rt.server.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextFutureFilterTest {

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

    when(m_serverJobFuture.getJobInput()).thenReturn(Jobs.newInput().withRunContext(ServerRunContexts.empty().withSession(m_serverSession1)));
    when(m_jobFuture.getJobInput()).thenReturn(Jobs.newInput().withRunContext(RunContexts.empty()));
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testBlocked() {
    when(m_serverJobFuture.isBlocked()).thenReturn(true);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andAreBlocked()
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andAreNotBlocked()
        .toFilter()
        .accept(m_serverJobFuture));
  }

  @Test
  public void testPeriodic() {
    when(m_serverJobFuture.getSchedulingRule()).thenReturn(JobInput.SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andArePeriodicExecuting()
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andAreSingleExecuting()
        .toFilter()
        .accept(m_serverJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(m_serverSession1))
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(m_serverSession2))
        .toFilter()
        .accept(m_serverJobFuture));
  }

  @Test
  public void testCurrentSession() {
    ISession.CURRENT.set(m_serverSession1);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));

    ISession.CURRENT.set(m_serverSession2);
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testNotCurrentSession() {
    ISession.CURRENT.set(m_serverSession1);
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));

    ISession.CURRENT.set(m_serverSession2);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new SessionFutureFilter(ISession.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));
    ISession.CURRENT.remove();
  }

  @Test
  public void testFuture() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(m_serverJobFuture)
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(mock(IFuture.class))
        .toFilter()
        .accept(m_jobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_serverJobFuture);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));
    IFuture.CURRENT.set(m_jobFuture);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_serverJobFuture);
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));

    IFuture.CURRENT.set(m_jobFuture);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .accept(m_serverJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutex() {
    IMutex mutex1 = Jobs.newMutex();
    IMutex mutex2 = Jobs.newMutex();

    m_serverJobFuture.getJobInput().withMutex(mutex1);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex1)
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex2)
        .toFilter()
        .accept(m_serverJobFuture));

    m_serverJobFuture.getJobInput().withMutex(null);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .accept(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(null)
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex1)
        .toFilter()
        .accept(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchMutex(mutex2)
        .toFilter()
        .accept(m_serverJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ServerRunContext.class).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).toFilter().accept(m_serverJobFuture));

    // True Filter
    assertTrue(Jobs.newFutureFilterBuilder().andMatchRunContext(ServerRunContext.class).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).toFilter().accept(m_serverJobFuture));

    // True/False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ServerRunContext.class).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return true;
      }
    }).andMatch(new IFilter<IFuture<?>>() {

      @Override
      public boolean accept(IFuture<?> future) {
        return false;
      }
    }).toFilter().accept(m_serverJobFuture));
  }
}
