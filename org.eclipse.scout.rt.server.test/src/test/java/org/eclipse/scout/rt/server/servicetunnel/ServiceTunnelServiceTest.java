/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.servicetunnel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerHttpRunContextProducer;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ServiceTunnelService}
 */
@RunWith(ServerTestRunner.class)
@RunWithSubject("default")
public class ServiceTunnelServiceTest {

  private HttpServletRequest m_requestMock;
  private HttpServletResponse m_responseMock;

  @Before
  public void before() {
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    when(m_requestMock.getHeader(SessionId.HTTP_HEADER_NAME)).thenReturn("testSessionId");
    when(m_requestMock.getHeader(NodeId.HTTP_HEADER_NAME)).thenReturn("testNodeId");
  }

  @Test
  public void testPostSuccessful() {
    verifyTestResponse(createServletRunContext(m_requestMock, m_responseMock).call(() -> new ServiceTunnelService().evaluate(prepareTestRequest())));
  }

  @Test
  public void testIncomingRequest() throws Exception {
    ServiceTunnelService s = BEANS.get(ServiceTunnelService.class);
    IObjectSerializer serializer = SerializationUtility.createObjectSerializer();
    byte[] serializedReq = serializer.serialize(prepareTestRequest());
    try (InputStream in = new ByteArrayInputStream(serializedReq); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(mock(HttpServletRequest.class));
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(mock(HttpServletResponse.class));
      Response r = s.incomingRequest(in);
      ((StreamingOutput) r.getEntity()).write(out);
      byte[] byteArray = out.toByteArray();
      verifyTestResponse(serializer.deserialize(byteArray, ServiceTunnelResponse.class));
    }
    finally {
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.remove();
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.remove();
    }
  }

  @Test
  public void testCreateServiceTunnelContext() {
    createServletRunContext(m_requestMock, m_responseMock).run(() -> {
      ServiceTunnelService s = BEANS.get(ServiceTunnelService.class);
      ServiceTunnelRequest serviceRequest = prepareTestRequest();
      ServerRunContext context = s.createServiceTunnelRunContext(serviceRequest);
      assertEquals("testSessionId", context.call(SessionId.CURRENT::get));
      assertEquals(NodeId.of("testNodeId"), context.getClientNodeId());
    });
  }

  private ServiceTunnelRequest prepareTestRequest() {
    Class[] parameterTypes = new Class[]{String.class};
    Object[] args = new Object[]{"test"};
    ServiceTunnelRequest req = new ServiceTunnelRequest(IPingService.class.getName(), "ping", parameterTypes, args);
    req.setUserAgent(UserAgents.createDefault().createIdentifier());
    return req;
  }

  private void verifyTestResponse(ServiceTunnelResponse res) {
    assertEquals("test", res.getData());
    assertNull(res.getException());
    assertEquals(0, res.getNotifications().size());
  }

  private static RunContext createServletRunContext(final HttpServletRequest req, final HttpServletResponse resp) {
    return BEANS.get(ServerHttpRunContextProducer.class).produce(req, resp);
  }
}
