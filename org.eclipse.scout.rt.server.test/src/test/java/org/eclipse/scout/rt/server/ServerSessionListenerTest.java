/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

/**
 * Tests for listeners in {@link AbstractServerSession}
 */
public class ServerSessionListenerTest {

  private IServerSession m_testSession;
  private ISessionListener m_listenerMock;

  @Before
  public void setup() {
    m_testSession = new TestServerSession();
    m_listenerMock = mock(ISessionListener.class);
    m_testSession.addListener(m_listenerMock);
  }

  @After
  public void cleanup() {
    m_testSession.removeListener(m_listenerMock);
  }

  @Test
  public void testListenerStartSession() {
    m_testSession.start("");
    verify(m_listenerMock, times(1)).sessionChanged(argThat(hasSessionType(SessionEvent.TYPE_STARTED)));
    verifyNoMoreInteractions(m_listenerMock);
  }

  @Test
  public void testListenerStopSession() {
    m_testSession.start("");
    m_testSession.stop();
    verify(m_listenerMock, times(1)).sessionChanged(argThat(hasSessionType(SessionEvent.TYPE_STARTED)));
    verify(m_listenerMock, times(1)).sessionChanged(argThat(hasSessionType(SessionEvent.TYPE_STOPPING)));
    verify(m_listenerMock, times(1)).sessionChanged(argThat(hasSessionType(SessionEvent.TYPE_STOPPED)));

    verifyNoMoreInteractions(m_listenerMock);
  }

  private ArgumentMatcher<SessionEvent> hasSessionType(final int sessionType) {
    return argument -> argument.getType() == sessionType;
  }
}
