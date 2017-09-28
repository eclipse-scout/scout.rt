/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.IFilter;
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

    IFilter<IFuture<?>> filter = ModelJobFutureFilter.INSTANCE;

    // not a model job (no Future)
    assertFalse(filter.accept(null));

    // not a model job (no ClientRunContext)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));

    // not a model job (no ClientRunContext)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(RunContexts.empty()))));

    // not a model job (no mutex and not session on ClientRunContext)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()))));

    // not a model job (no mutex)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false)))));

    // not a model job (wrong mutex type)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(session1, false))
        .withExecutionSemaphore(Jobs.newExecutionSemaphore(1)))));

    // not a model job (different session on ClientRunContext and mutex)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withExecutionSemaphore(session2.getModelJobSemaphore()))));

    // not a model job (no session on ClientRunContext)
    assertFalse(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(null, false))
        .withExecutionSemaphore(session1.getModelJobSemaphore()))));

    // this is a model job (same session on ClientRunContext and mutex)
    assertTrue(filter.accept(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty()
            .withSession(session1, false))
        .withExecutionSemaphore(session1.getModelJobSemaphore()))));
  }
}
