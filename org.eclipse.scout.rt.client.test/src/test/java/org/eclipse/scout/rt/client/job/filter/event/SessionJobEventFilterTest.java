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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.event.SessionJobEventFilter;
import org.junit.Test;

public class SessionJobEventFilterTest {

  @Test
  public void test() {
    IClientSession session1 = mock(IClientSession.class);
    IClientSession session2 = mock(IClientSession.class);

    SessionJobEventFilter filter = new SessionJobEventFilter(session1);

    // Tests JobEvent of an event without a job associated
    JobEvent event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(null));
    assertFalse(filter.test(event));

    // Tests JobEvent with job without RunContext
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput())));
    assertFalse(filter.test(event));

    // Tests JobEvent with job with RunContext
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(RunContexts.empty()))));
    assertFalse(filter.test(event));

    // Tests JobEvent with job with ClientRunContext without session
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty()))));
    assertFalse(filter.test(event));

    // Tests JobEvent with job with ClientRunContext with correct session
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(session1, false)))));
    assertTrue(filter.test(event));

    // Tests JobEvent with job with ClientRunContext with wrong session
    event = new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED,
        new JobEventData().withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(session2, false)))));
    assertFalse(filter.test(event));

    // Tests adaptable to the session
    assertSame(session1, filter.getAdapter(ISession.class));
  }
}
