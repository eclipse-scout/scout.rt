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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Tests for {@link ClientSessionRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class ClientSessionRegistryTest {

  @BeanMock
  private IClientNotificationService m_mockService;

  @Mock
  private IClientSession m_clientSession;

  @Before
  public void before() {
    when(m_clientSession.getId()).thenReturn("testSessionId");
    when(m_clientSession.getUserId()).thenReturn("testUserId");
  }

  /**
   * Tests that the client session is registered on the Back-End, when the session is started.
   */
  @Test
  public void testRegisteredOnBackendWhenSessionStarted() {
    ClientSessionRegistry reg = new TestClientSessionRegistry();
    reg.sessionStarted(m_clientSession);
    verify(m_mockService).registerSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void testUnRegisteredWhenSessionStopped() {
    ClientSessionRegistry reg = new TestClientSessionRegistry();
    reg.sessionStopped(m_clientSession);
    verify(m_mockService).unregisterSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
  }

  class TestClientSessionRegistry extends ClientSessionRegistry {
    @Override
    protected void ensureUserIdAvailable(IClientSession session) {
      //nop
    }
  }

}
