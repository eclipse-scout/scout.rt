/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.job.filter.future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
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
  public void before() throws Exception {
    MockitoAnnotations.openMocks(this).close();

    when(m_serverJobFuture.getJobInput()).thenReturn(Jobs.newInput().withRunContext(ServerRunContexts.empty().withSession(m_serverSession1)));
    when(m_jobFuture.getJobInput()).thenReturn(Jobs.newInput().withRunContext(RunContexts.empty()));
  }

  @Test
  public void testRepetitive() {
    when(m_serverJobFuture.isSingleExecution()).thenReturn(false);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .test(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andAreNotSingleExecuting()
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andAreSingleExecuting()
        .toFilter()
        .test(m_serverJobFuture));
  }

  @Test
  public void testSession() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(m_serverSession1))
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(m_serverSession2))
        .toFilter()
        .test(m_serverJobFuture));
  }

  @Test
  public void testCurrentSession() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(m_serverSession1))
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new SessionFutureFilter(m_serverSession2))
        .toFilter()
        .test(m_serverJobFuture));
  }

  @Test
  public void testNotCurrentSession() {
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new SessionFutureFilter(m_serverSession1))
        .toFilter()
        .test(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new SessionFutureFilter(m_serverSession2))
        .toFilter()
        .test(m_serverJobFuture));
  }

  @Test
  public void testFuture() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(m_serverJobFuture)
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchFuture(mock(IFuture.class))
        .toFilter()
        .test(m_jobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_serverJobFuture);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .test(m_serverJobFuture));
    IFuture.CURRENT.set(m_jobFuture);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatch(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .test(m_serverJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_serverJobFuture);
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .test(m_serverJobFuture));

    IFuture.CURRENT.set(m_jobFuture);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchNot(new FutureFilter(IFuture.CURRENT.get()))
        .toFilter()
        .test(m_serverJobFuture));
    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutualExclusion() {
    IExecutionSemaphore mutex1 = Jobs.newExecutionSemaphore(1);
    IExecutionSemaphore mutex2 = Jobs.newExecutionSemaphore(1);

    m_serverJobFuture.getJobInput().withExecutionSemaphore(mutex1);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchExecutionSemaphore(null)
        .toFilter()
        .test(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchExecutionSemaphore(mutex1)
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchExecutionSemaphore(mutex2)
        .toFilter()
        .test(m_serverJobFuture));

    m_serverJobFuture.getJobInput().withExecutionSemaphore(null);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .toFilter()
        .test(m_serverJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchExecutionSemaphore(null)
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchExecutionSemaphore(mutex1)
        .toFilter()
        .test(m_serverJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ServerRunContext.class)
        .andMatchExecutionSemaphore(mutex2)
        .toFilter()
        .test(m_serverJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ServerRunContext.class).andMatch(future -> false).toFilter().test(m_serverJobFuture));

    // True Filter
    assertTrue(Jobs.newFutureFilterBuilder().andMatchRunContext(ServerRunContext.class).andMatch(future -> true).toFilter().test(m_serverJobFuture));

    // True/False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ServerRunContext.class).andMatch(future -> true).andMatch(future -> false).toFilter().test(m_serverJobFuture));
  }
}
