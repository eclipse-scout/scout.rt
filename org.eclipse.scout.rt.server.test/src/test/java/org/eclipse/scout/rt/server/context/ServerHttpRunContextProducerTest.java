/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import static org.eclipse.scout.rt.testing.platform.BeanTestingHelper.TESTING_BEAN_ORDER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.shared.ui.UiEngineType;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for {@link ServerHttpRunContextProducer}
 */
@RunWith(PlatformTestRunner.class)
@RunWithSubject("test")
public class ServerHttpRunContextProducerTest {
  protected static final String TEST_SESSION_ID = "test-session-42";
  protected static final String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";
  protected static final String TEST_NODE_NAME = "testNode";
  protected static final NodeId TEST_NODE_ID = NodeId.of(TEST_NODE_NAME);

  protected ServerHttpRunContextProducer m_producer;

  @Before
  public void before() {
    m_producer = BEANS.get(ServerHttpRunContextProducer.class);
  }

  @Test
  public void testProduceServerRunContext() {
    HttpServletRequest req = createRequestMock(null, null, null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    RunContext context = m_producer.produce(req, resp);
    assertTrue("produce() must return a ServerRunContext", context instanceof ServerRunContext);
    assertEquals("test", context.getUser().getUserId());

    IBean<?> additionalRunContextFactoryBean = BeanTestingHelper.get().registerBean(new BeanMetaData(OtherRunContextFactory.class).withReplace(true).withOrder(TESTING_BEAN_ORDER)); // register an additional RunContextFactory with a lower order
    try {
      // check that testing bean is correctly registered
      assertTrue(BEANS.get(RunContextFactory.class) instanceof OtherRunContextFactory);
      assertFalse(RunContexts.empty() instanceof ServerRunContext); // will just create a RunContext

      assertTrue(m_producer.produce(req, resp) instanceof ServerRunContext); // should always create a ServerRunContext
    }
    finally {
      BeanTestingHelper.get().unregisterBean(additionalRunContextFactoryBean);
    }
  }

  @Test
  public void testProduceSessionId() {
    HttpServletRequest req = createRequestMock(TEST_SESSION_ID, null, null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    RunContext context = m_producer.produce(req, resp);
    assertEquals(TEST_SESSION_ID, context.getThreadLocal(SessionId.CURRENT));
  }

  @Test
  public void testProduceNoSessionId() {
    HttpServletRequest req = createRequestMock(null, null, null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    RunContext context = m_producer.produce(req, resp);
    assertNull(context.getThreadLocal(SessionId.CURRENT));
  }

  @Test
  public void testProduceUserAgent() {
    HttpServletRequest req = createRequestMock(null, CHROME_USER_AGENT, null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    RunContext context = m_producer.produce(req, resp);
    ServerRunContext serverRunContext = (ServerRunContext) context;

    assertNotNull(serverRunContext.getUserAgent());
    assertEquals(UiEngineType.CHROME, serverRunContext.getUserAgent().getUiEngineType());
  }

  @Test
  public void testProduceNoUserAgent() {
    HttpServletRequest req = createRequestMock(null, null, null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    RunContext context = m_producer.produce(req, resp);
    ServerRunContext serverRunContext = (ServerRunContext) context;

    assertNotNull(serverRunContext.getUserAgent());
    assertEquals(UiEngineType.UNKNOWN, serverRunContext.getUserAgent().getUiEngineType());
  }

  @Test
  public void testProduceClientNodeId() {
    HttpServletRequest req = createRequestMock(TEST_SESSION_ID, null, TEST_NODE_NAME);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    ServerRunContext context = (ServerRunContext) m_producer.produce(req, resp);
    assertEquals(TEST_NODE_ID, context.getClientNodeId());
  }

  @Test
  public void testProduceNoClientNodeId() {
    HttpServletRequest req = createRequestMock(null, null, null);
    HttpServletResponse resp = mock(HttpServletResponse.class);

    ServerRunContext context = (ServerRunContext) m_producer.produce(req, resp);
    assertNull(context.getClientNodeId());
  }

  protected HttpServletRequest createRequestMock(String sessionId, String userAgent, String clientNodeName) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(eq(SessionId.HTTP_HEADER_NAME))).thenReturn(sessionId);
    when(req.getHeader(eq(NodeId.HTTP_HEADER_NAME))).thenReturn(clientNodeName);
    when(req.getHeader(eq("User-Agent"))).thenReturn(userAgent);
    return req;
  }

  @IgnoreBean
  public static class OtherRunContextFactory extends RunContextFactory {
  }
}
