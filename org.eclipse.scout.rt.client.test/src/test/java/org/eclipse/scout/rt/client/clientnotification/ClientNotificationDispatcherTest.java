/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.clientnotification;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.session.Sessions;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWithSubject("default")
@RunWith(PlatformTestRunner.class)
public class ClientNotificationDispatcherTest {
  @Test
  public void testIsSessionValid() {
    ClientNotificationDispatcher dispatcher = new ClientNotificationDispatcher();
    String sid = Sessions.randomSessionId();

    assertFalse(dispatcher.isSessionValid(null, sid)); // session not found

    IClientSession session = new P_ClientNotificationDispatcherTestSession(dispatcher);
    assertFalse(dispatcher.isSessionValid(session, sid)); // session not valid to receive notification if not started/starting

    ModelJobs.schedule(() -> session.start(sid), ModelJobs
        .newInput(ClientRunContexts.copyCurrent().withSession(session, true))
        .withName("Starting ClientSession [sessionId={}]", sid)
        .withExceptionHandling(null, false)).awaitDoneAndGet();
    assertTrue(dispatcher.isSessionValid(session, sid)); // session valid to receive notification while started

    ModelJobs.schedule(() -> session.stop(), ModelJobs
        .newInput(ClientRunContexts.copyCurrent().withSession(session, true))
        .withName("Stopping ClientSession [sessionId={}]", sid)
        .withExceptionHandling(null, false)).awaitDoneAndGet();
    assertTrue(dispatcher.isSessionValid(session, sid)); // session valid to receive notification while stopped
  }

  @IgnoreBean
  public static class P_ClientNotificationDispatcherTestSession extends AbstractClientSession {

    private final ClientNotificationDispatcher m_dispatcher;

    public P_ClientNotificationDispatcherTestSession(ClientNotificationDispatcher dispatcher) {
      super(true);
      m_dispatcher = dispatcher;
    }

    @Override
    protected void execLoadSession() {
      assertTrue(m_dispatcher.isSessionValid(this, this.getId())); // session valid to receive notification while starting
    }
  }
}
