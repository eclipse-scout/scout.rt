/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job.filter.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ModelJobEventFilterTest {

  @Test
  public void test() {
    IClientSession session1 = mock(IClientSession.class);
    when(session1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    IClientSession session2 = mock(IClientSession.class);
    when(session2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    Predicate<JobEvent> filter = ModelJobEventFilter.INSTANCE;

    // not a model job (no future)
    JobEvent event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED, new JobEventData().withFuture(null));
    assertFalse(filter.test(event));

    // not a model job (no ClientRunContext)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));
    assertFalse(filter.test(event));

    // not a model job (no ClientRunContext)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(RunContexts.empty()))));
    assertFalse(filter.test(event));

    // not a model job (no mutex and not session on ClientRunContext)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty()))));
    assertFalse(filter.test(event));

    // not a model job (no mutex)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(session1, false)))));
    assertFalse(filter.test(event));

    // not a model job (wrong mutex type)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(session1, false)).withExecutionSemaphore(Jobs.newExecutionSemaphore(1)))));
    assertFalse(filter.test(event));

    // not a model job (different session on ClientRunContext and mutex)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(session1, false)).withExecutionSemaphore(session2.getModelJobSemaphore()))));
    assertFalse(filter.test(event));

    // not a model job (no session on ClientRunContext)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(null, false)).withExecutionSemaphore(session1.getModelJobSemaphore()))));
    assertFalse(filter.test(event));

    // this is a model job (same session on ClientRunContext and mutex)
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(session1, false)).withExecutionSemaphore(session1.getModelJobSemaphore()))));
    assertTrue(filter.test(event));
  }
}
