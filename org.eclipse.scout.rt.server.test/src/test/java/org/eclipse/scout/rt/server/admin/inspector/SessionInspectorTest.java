/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.inspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class SessionInspectorTest {

  private static final Long CREATION_TIME = 23456789L;
  private static final Long LAST_ACCESS_TIME = 23459999L;

  private HttpServletRequest m_servletRequestBackup;

  @Before
  public void setUp() {
    m_servletRequestBackup = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
  }

  @After
  public void tearDown() {
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(m_servletRequestBackup);
  }

  @Test
  public void testSessionInspectorWithoutServletRequest() {
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(null);
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), ServerSessionProvider.currentSession());
    assertNull(inspector.getInfo().getCreationTime());
    assertNull(inspector.getInfo().getLastAccessedTime());
  }

  @Test
  public void testSessionInspectorWithoutHttpSession() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(servletRequest);
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), ServerSessionProvider.currentSession());
    assertNull(inspector.getInfo().getCreationTime());
    assertNull(inspector.getInfo().getLastAccessedTime());
  }

  @Test
  public void testSessionInspectorWithServletRequest() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpSession httpSession = mock(HttpSession.class);
    when(servletRequest.getSession()).thenReturn(httpSession);
    when(servletRequest.getSession(anyBoolean())).thenReturn(httpSession);
    when(httpSession.getCreationTime()).thenReturn(CREATION_TIME);
    when(httpSession.getLastAccessedTime()).thenReturn(LAST_ACCESS_TIME);

    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(servletRequest);
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), ServerSessionProvider.currentSession());
    assertEquals(CREATION_TIME, inspector.getInfo().getCreationTime());
    assertEquals(LAST_ACCESS_TIME, inspector.getInfo().getLastAccessedTime());
  }

  @Test
  public void testSessionInspectorWithServerSession() {
    IServerSession serverSession = ServerSessionProvider.currentSession();
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), serverSession);
    assertEquals(ProcessInspector.instance(), inspector.getProcessInspector());
    assertEquals(serverSession, inspector.getServerSession());
    assertEquals(serverSession.getId(), inspector.getInfo().getSessionId());
    assertEquals(serverSession.getUserId(), inspector.getInfo().getUserId());
  }
}
