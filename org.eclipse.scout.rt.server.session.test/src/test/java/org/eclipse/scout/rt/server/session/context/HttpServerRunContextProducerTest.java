/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.context;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.runner.RunWith;

// FIXME PBZ SESSION split into two tests
@RunWith(PlatformTestRunner.class)
public class HttpServerRunContextProducerTest {

  //  static final String TEST_USER_AGENT_STRING = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)";
  //
  //  @Test
  //  public void testGetOrCreateScoutSessionWithCustomId() {
  //    HttpServerSessionRunContextProducer producer = new HttpServerSessionRunContextProducer();
  //    assertTrue(producer.hasSessionSupport());
  //
  //    HttpServletRequest req = createRequestMock(null, null);
  //    HttpServletResponse resp = mock(HttpServletResponse.class);
  //
  //    ServerRunContext serverRunContextForSessionStart = (ServerRunContext) producer.getInnerRunContextProducer().produce(req, resp);
  //    String sessionId = "testid";
  //    IServerSession session = producer.getOrCreateScoutSession(req, serverRunContextForSessionStart, sessionId);
  //    try {
  //      assertNotNull(session);
  //      assertEquals(sessionId, session.getId());
  //    }
  //    finally {
  //      session.stop();
  //    }
  //  }
  //
  //  @Test
  //  public void testGetOrCreateScoutSessionWithRandomId() {
  //    HttpServerSessionRunContextProducer producer = new HttpServerSessionRunContextProducer();
  //    assertTrue(producer.hasSessionSupport());
  //
  //    String clientSessionId = "testClientSessionId";
  //    HttpServletRequest req = createRequestMock(null, clientSessionId);
  //    HttpServletResponse resp = mock(HttpServletResponse.class);
  //
  //    ServerSessionRunContext context = producer.produce(req, resp);
  //    IServerSession session = context.getSession();
  //    try {
  //      assertNotNull(session);
  //      assertNotNull(session.getId());
  //      assertEquals(clientSessionId, context.call(SessionId.CURRENT::get));
  //    }
  //    finally {
  //      session.stop();
  //    }
  //  }
  //
  //  @Test
  //  public void testGetOrCreateScoutSessionWithExistingSessionIdOnHttpSession() {
  //    HttpServerSessionRunContextProducer producer = new HttpServerSessionRunContextProducer();
  //    assertTrue(producer.hasSessionSupport());
  //
  //    String existingServerSessionId = "testServerSessionId";
  //    String clientSessionId = "testClientSessionId";
  //    HttpServletRequest req = createRequestMock(existingServerSessionId, clientSessionId);
  //    HttpServletResponse resp = mock(HttpServletResponse.class);
  //
  //    ServerSessionRunContext context = producer.produce(req, resp);
  //    IServerSession session = context.getSession();
  //    try {
  //      assertNotNull(session);
  //      assertEquals(existingServerSessionId, session.getId());
  //      assertEquals(clientSessionId, context.call(SessionId.CURRENT::get));
  //    }
  //    finally {
  //      session.stop();
  //    }
  //  }
  //
  //  protected HttpServletRequest createRequestMock(String serverSessionId, String clientSessionId) {
  //    HttpSession httpSession = mock(HttpSession.class);
  //    when(httpSession.getAttribute(eq(HttpServerRunContextProducer.SCOUT_SESSION_ID_KEY))).thenReturn(serverSessionId);
  //
  //    HttpServletRequest req = mock(HttpServletRequest.class);
  //    when(req.getHeader(eq("User-Agent"))).thenReturn(TEST_USER_AGENT_STRING);
  //    when(req.getSession()).thenReturn(httpSession);
  //    when(req.getSession(anyBoolean())).thenReturn(httpSession);
  //    when(req.getHeader(SessionId.HTTP_HEADER_NAME)).thenReturn(clientSessionId);
  //    return req;
  //  }
}
