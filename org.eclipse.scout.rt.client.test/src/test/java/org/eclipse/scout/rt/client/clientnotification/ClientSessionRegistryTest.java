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
package org.eclipse.scout.rt.client.clientnotification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 * Tests for {@link ClientSessionRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class ClientSessionRegistryTest {

  @Mock
  private IClientSession m_clientSession;

  @Before
  public void before() {
    when(m_clientSession.getId()).thenReturn("testSessionId");
    when(m_clientSession.getUserId()).thenReturn("testUserId");
  }

  /**
   * Tests that the client session is registered on the client, after it is started.
   */
  @Test
  public void testRegisteredWhenSessionStarted() {
    ClientSessionRegistry reg = new TestClientSessionRegistry();
    startSession(reg);

    final List<IClientSession> userSessions = reg.getClientSessionsForUser(m_clientSession.getUserId());
    assertEquals(m_clientSession, reg.getClientSession(m_clientSession.getId()));
    assertEquals(1, userSessions.size());
    assertEquals(m_clientSession, userSessions.get(0));
  }

  @Test
  public void testUnRegisteredWhenSessionStopped() {
    ClientSessionRegistry reg = new TestClientSessionRegistry();
    startSession(reg);
    reg.sessionStopped(m_clientSession);
    final List<IClientSession> userSessions = reg.getClientSessionsForUser(m_clientSession.getUserId());
    assertNull(reg.getClientSession(m_clientSession.getId()));
    assertTrue(userSessions.isEmpty());
  }

  private void startSession(ClientSessionRegistry reg) {
    reg.register(m_clientSession, m_clientSession.getId());
    reg.sessionStarted(m_clientSession);
  }

  class TestClientSessionRegistry extends ClientSessionRegistry {
    @Override
    protected void ensureUserIdAvailable(IClientSession session) {
      //nop
    }
  }

}
