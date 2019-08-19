/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ModelJobsTest {

  private IClientSession m_clientSession;

  @Before
  public void before() {
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));
  }

  @Test
  public void testIsModelJob() {
    IClientSession session1 = mock(IClientSession.class);
    when(session1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    IClientSession session2 = mock(IClientSession.class);
    when(session2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    // not a model job (no Future)
    assertFalse(ModelJobs.isModelJob(null));

    // not a model job (no ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));

    // not a model job (no ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(RunContexts.empty()))));

    // not a model job (no mutex and not session on ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()))));

    // not a model job (no mutex)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false)))));

    // not a model job (wrong mutex type)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false))
        .withExecutionSemaphore(Jobs.newExecutionSemaphore(1)))));

    // not a model job (different session on ClientRunContext and mutex)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withExecutionSemaphore(session2.getModelJobSemaphore()))));

    // not a model job (no session on ClientRunContext)
    assertFalse(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(null, false))
        .withExecutionSemaphore(session1.getModelJobSemaphore()))));

    // this is a model job (same session on ClientRunContext and mutex)
    assertTrue(ModelJobs.isModelJob(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withExecutionSemaphore(session1.getModelJobSemaphore()))));
  }

  @Test
  public void testIsModelThread() {
    final IClientSession clientSession1 = mock(IClientSession.class);
    when(clientSession1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    final IClientSession clientSession2 = mock(IClientSession.class);
    when(clientSession2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    ModelJobs.schedule(() -> {
      // Test model thread for same session (1)
      assertTrue(ModelJobs.isModelThread());

      // Test model thread for same session (2)
      ClientRunContexts.copyCurrent().run(() -> assertTrue(ModelJobs.isModelThread()));
      // Test model thread for other session
      ClientRunContexts.copyCurrent().withSession(clientSession2, true).run(() -> assertTrue(ModelJobs.isModelThread()));
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession1, true))).awaitDoneAndGet();
  }

  @Test
  public void testScheduleWithoutInput() {
    ClientRunContexts.copyCurrent().withSession(m_clientSession, false).run(() -> {
      // Test schedule
      IFuture<?> actualFuture = ModelJobs.schedule((Callable<IFuture<?>>) () -> IFuture.CURRENT.get(), ModelJobs.newInput(ClientRunContexts.copyCurrent()))
          .awaitDoneAndGet();

      assertTrue(ModelJobs.isModelJob(actualFuture));

      // Test schedule with delay
      actualFuture = ModelJobs.schedule((Callable<IFuture<?>>) () -> IFuture.CURRENT.get(), ModelJobs.newInput(ClientRunContexts.copyCurrent())).awaitDoneAndGet();

      assertTrue(ModelJobs.isModelJob(actualFuture));
      assertTrue(actualFuture.getJobInput().getRunContext() instanceof ClientRunContext);
    });
  }

  @Test(expected = AssertionException.class)
  public void testScheduleWithoutInputWithoutSession() {
    ClientRunContexts.empty().run(() -> ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.copyCurrent())));
  }

  @Test
  public void testNewInput() {
    ClientRunContext runContext = ClientRunContexts.empty().withSession(m_clientSession, true);

    assertSame(runContext, ModelJobs.newInput(runContext).getRunContext());
    assertEquals("scout-model-thread", ModelJobs.newInput(runContext).getThreadName());
    assertSame(m_clientSession.getModelJobSemaphore(), ModelJobs.newInput(runContext).getExecutionSemaphore());
  }

  @Test(expected = AssertionException.class)
  public void testNewInputNullInput() {
    ModelJobs.newInput(null);
  }

  @Test(expected = AssertionException.class)
  public void testNewInputNullSession() {
    ModelJobs.newInput(ClientRunContexts.empty().withSession(null, true));
  }
}
