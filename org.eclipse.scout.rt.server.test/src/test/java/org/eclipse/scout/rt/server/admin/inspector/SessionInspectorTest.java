/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.admin.inspector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.eclipse.scout.rt.testing.server.junit.rule.ServerJobRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ScoutPlatformTestRunner.class)
public class SessionInspectorTest {

  private static final Long CREATION_TIME = 23456789L;
  private static final Long LAST_ACCESS_TIME = 23459999L;

  private HttpServletRequest servletRequestBackup;

  @Rule
  public TestRule serverJobRule = new ServerJobRule();

  @Before
  public void setUp() throws Exception {
    servletRequestBackup = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
  }

  @After
  public void tearDown() throws Exception {
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(servletRequestBackup);
  }

  @Test
  public void testSessionInspectorWithoutServletRequest() {
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(null);
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), (IServerSession) ISession.CURRENT.get());
    assertNull(inspector.getInfo().getCreationTime());
    assertNull(inspector.getInfo().getLastAccessedTime());
  }

  @Test
  public void testSessionInspectorWithoutHttpSession() {
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(servletRequest);
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), (IServerSession) ISession.CURRENT.get());
    assertNull(inspector.getInfo().getCreationTime());
    assertNull(inspector.getInfo().getLastAccessedTime());
  }

  @Test
  public void testSessionInspectorWithServletRequest() {
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    HttpSession httpSession = Mockito.mock(HttpSession.class);
    Mockito.when(servletRequest.getSession()).thenReturn(httpSession);
    Mockito.when(httpSession.getCreationTime()).thenReturn(CREATION_TIME);
    Mockito.when(httpSession.getLastAccessedTime()).thenReturn(LAST_ACCESS_TIME);
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(servletRequest);
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), (IServerSession) ISession.CURRENT.get());
    assertEquals(CREATION_TIME, inspector.getInfo().getCreationTime());
    assertEquals(LAST_ACCESS_TIME, inspector.getInfo().getLastAccessedTime());
  }

  @Test
  public void testSessionInspectorWithServerSession() {
    IServerSession serverSession = (IServerSession) ISession.CURRENT.get();
    SessionInspector inspector = new SessionInspector(ProcessInspector.instance(), serverSession);
    assertEquals(ProcessInspector.instance(), inspector.getProcessInspector());
    assertEquals(serverSession, inspector.getServerSession());
    assertEquals(serverSession.getId(), inspector.getInfo().getSessionId());
    assertEquals(serverSession.getUserId(), inspector.getInfo().getUserId());
  }
}
