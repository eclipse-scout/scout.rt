/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.job.filter.future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.server.session.context.ServerSessionRunContexts;
import org.eclipse.scout.rt.shared.session.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerSessionRunContextFutureFilterTest {

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

    when(m_serverJobFuture.getJobInput()).thenReturn(Jobs.newInput().withRunContext(ServerSessionRunContexts.empty().withSession(m_serverSession1)));
    when(m_jobFuture.getJobInput()).thenReturn(Jobs.newInput().withRunContext(RunContexts.empty()));
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
}
