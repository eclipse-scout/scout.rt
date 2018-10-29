/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class HttpServerRunContextProducerTest {

  static final String TEST_USER_AGENT_STRING = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)";

  @Test
  public void testGetOrCreateScoutSessionWithCustomId() {
    HttpServerRunContextProducer producer = new HttpServerRunContextProducer();
    assertTrue(producer.hasSessionSupport());

    HttpServletRequest req = createRequestMock(null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    ServerRunContext serverRunContextForSessionStart = (ServerRunContext) producer.getInnerRunContextProducer().produce(req, resp);
    String sessionId = "testid";
    IServerSession session = producer.getOrCreateScoutSession(req, serverRunContextForSessionStart, sessionId);
    try {
      assertNotNull(session);
      assertEquals(sessionId, session.getId());
    }
    finally {
      session.stop();
    }
  }

  @Test
  public void testGetOrCreateScoutSessionWithRandomId() {
    HttpServerRunContextProducer producer = new HttpServerRunContextProducer();
    assertTrue(producer.hasSessionSupport());

    HttpServletRequest req = createRequestMock(null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    ServerRunContext context = producer.produce(req, resp);
    IServerSession session = context.getSession();
    try {
      assertNotNull(session);
      assertNotNull(session.getId());
    }
    finally {
      session.stop();
    }
  }

  @Test
  public void testGetOrCreateScoutSessionWithExistingSessionIdOnHttpSession() {
    HttpServerRunContextProducer producer = new HttpServerRunContextProducer();
    assertTrue(producer.hasSessionSupport());

    String existingScoutSessionId = "testId";
    HttpServletRequest req = createRequestMock(existingScoutSessionId);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    ServerRunContext context = producer.produce(req, resp);
    IServerSession session = context.getSession();
    try {
      assertNotNull(session);
      assertEquals(existingScoutSessionId, session.getId());
    }
    finally {
      session.stop();
    }
  }

  protected HttpServletRequest createRequestMock(String scoutSessionId) {
    HttpSession httpSession = mock(HttpSession.class);
    when(httpSession.getAttribute(eq(HttpServerRunContextProducer.SCOUT_SESSION_ID_KEY))).thenReturn(scoutSessionId);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(eq("User-Agent"))).thenReturn(TEST_USER_AGENT_STRING);
    when(req.getSession()).thenReturn(httpSession);
    when(req.getSession(anyBoolean())).thenReturn(httpSession);
    return req;
  }
}
