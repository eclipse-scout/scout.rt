/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job.filter.future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ModelJobFutureFilterTest {

  @Test
  public void test() {
    IClientSession session1 = mock(IClientSession.class);
    when(session1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    IClientSession session2 = mock(IClientSession.class);
    when(session2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    Predicate<IFuture<?>> filter = ModelJobFutureFilter.INSTANCE;

    // not a model job (no Future)
    assertFalse(filter.test(null));

    // not a model job (no ClientRunContext)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));

    // not a model job (no ClientRunContext)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(RunContexts.empty()))));

    // not a model job (no mutex and not session on ClientRunContext)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()))));

    // not a model job (no mutex)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false)))));

    // not a model job (wrong mutex type)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false))
        .withExecutionSemaphore(Jobs.newExecutionSemaphore(1)))));

    // not a model job (different session on ClientRunContext and mutex)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withExecutionSemaphore(session2.getModelJobSemaphore()))));

    // not a model job (no session on ClientRunContext)
    assertFalse(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(null, false))
        .withExecutionSemaphore(session1.getModelJobSemaphore()))));

    // this is a model job (same session on ClientRunContext and mutex)
    assertTrue(filter.test(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withExecutionSemaphore(session1.getModelJobSemaphore()))));
  }
}
