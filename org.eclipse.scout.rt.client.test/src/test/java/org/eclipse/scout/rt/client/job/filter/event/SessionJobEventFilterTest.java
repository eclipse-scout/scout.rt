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
package org.eclipse.scout.rt.client.job.filter.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
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
    JobEvent event = new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)
        .withFuture(null);
    assertFalse(filter.accept(event));

    // Tests JobEvent with job without RunContext
    event = new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)
        .withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()));
    assertFalse(filter.accept(event));

    // Tests JobEvent with job with RunContext
    event = new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)
        .withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
            .withRunContext(RunContexts.empty())));
    assertFalse(filter.accept(event));

    // Tests JobEvent with job with ClientRunContext without session
    event = new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)
        .withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
            .withRunContext(ClientRunContexts.empty())));
    assertFalse(filter.accept(event));

    // Tests JobEvent with job with ClientRunContext with correct session
    event = new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)
        .withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
            .withRunContext(ClientRunContexts.empty().withSession(session1, false))));
    assertTrue(filter.accept(event));

    // Tests JobEvent with job with ClientRunContext with wrong session
    event = new JobEvent(mock(IJobManager.class), JobEventType.ABOUT_TO_RUN)
        .withFuture(Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
            .withRunContext(ClientRunContexts.empty().withSession(session2, false))));
    assertFalse(filter.accept(event));

    // Tests adaptable to the session
    assertSame(session1, filter.getAdapter(ISession.class));
  }
}
