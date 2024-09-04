/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.http.AbstractHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

/**
 * Tests for {@link HttpServiceTunnel}
 */
@RunWith(PlatformTestRunner.class)
public class HttpServiceTunnelTest {

  @BeanMock
  private ServiceTunnelTargetUrlProperty mockUrl;

  @BeanMock
  private IServiceTunnelContentHandler m_contentHandler;

  @Test
  public void testTunnel() throws IOException {
    when(mockUrl.getValue()).thenReturn("http://localhost");
    MockLowLevelHttpResponse expectedResponse = new MockLowLevelHttpResponse().setContent(serialize(new ServiceTunnelResponse("testData")));
    HttpServiceTunnel tunnel = createHttpServiceTunnel(expectedResponse);
    tunnel.setContentHandler(getTestContentHandler());

    ServiceTunnelRequest request = new ServiceTunnelRequest("IPingService", "ping", null, null);
    ServiceTunnelResponse response = tunnel.tunnel(request);
    assertNotNull(response);
    assertEquals("testData", response.getData());
  }

  @Test
  public void testTunnelException() {
    when(mockUrl.getValue()).thenReturn("http://localhost");
    MockLowLevelHttpResponse expectedResponse = new MockLowLevelHttpResponse().setStatusCode(401);
    HttpServiceTunnel tunnel = createHttpServiceTunnel(expectedResponse);
    tunnel.setContentHandler(getTestContentHandler());

    ServiceTunnelRequest request = new ServiceTunnelRequest("IPingService", "ping", null, null);
    ServiceTunnelResponse response = tunnel.tunnel(request);
    assertNotNull(response);
    assertTrue(response.getException() instanceof HttpServiceTunnelException);
    assertEquals(401, ((HttpServiceTunnelException) response.getException()).getHttpStatus());
  }

  protected HttpServiceTunnel createHttpServiceTunnel(final MockLowLevelHttpResponse expectedResponse) {
    HttpServiceTunnel tunnel = new HttpServiceTunnel() {

      @Override
      protected IHttpTransportManager getHttpTransportManager() {

        return new AbstractHttpTransportManager() {

          private MockHttpTransport m_transport = new MockHttpTransport.Builder()
              .setLowLevelHttpResponse(expectedResponse)
              .build();

          @Override
          public String getName() {
            return "scout.transport.test-service-tunnel";
          }

          @Override
          public HttpTransport getHttpTransport() {
            return m_transport;
          }

          @Override
          public HttpRequestFactory getHttpRequestFactory() {
            return m_transport.createRequestFactory(createHttpRequestInitializer());
          }
        };
      }
    };
    return tunnel;
  }

  /**
   * {@link HttpServiceTunnel} should be inactive, if no url is defined.
   */
  @Test
  public void testNullUrlConfig() {
    HttpServiceTunnel tunnel = new HttpServiceTunnel();
    assertNull(tunnel.getServerUrl());
    assertFalse(tunnel.isActive());
  }

  /**
   * {@link HttpServiceTunnel} should be inactive, if no url is defined.
   */
  @Test
  public void testEmptyUrlConfig() {
    when(mockUrl.getValue()).thenReturn(" ");
    HttpServiceTunnel tunnel = new HttpServiceTunnel();
    assertNull(tunnel.getServerUrl());
    assertFalse(tunnel.isActive());
  }

  /**
   * {@link HttpServiceTunnel} should be inactive, if no url is defined.
   */
  @Test(expected = RuntimeException.class)
  public void testInvalidUrlConfig() {
    when(mockUrl.getValue()).thenReturn("asdf");
    new HttpServiceTunnel();
  }

  @Test
  public void testValidUrlConfig() {
    when(mockUrl.getValue()).thenReturn("http://localhost");
    HttpServiceTunnel tunnel = new HttpServiceTunnel();
    assertNotNull(tunnel.getServerUrl());
    assertTrue(tunnel.isActive());
  }

  protected byte[] serialize(ServiceTunnelResponse response) throws IOException {
    return SerializationUtility.createObjectSerializer().serialize(response);
  }

  private IServiceTunnelContentHandler getTestContentHandler() {
    return new IServiceTunnelContentHandler() {

      @Override
      public void writeResponse(OutputStream out, ServiceTunnelResponse msg) {
      }

      @Override
      public void writeRequest(OutputStream out, ServiceTunnelRequest msg) {
      }

      @Override
      public ServiceTunnelResponse readResponse(InputStream in) throws IOException, ClassNotFoundException {
        return SerializationUtility.createObjectSerializer().deserialize(in, ServiceTunnelResponse.class);
      }

      @Override
      public ServiceTunnelRequest readRequest(InputStream in) {
        return null;
      }

      @Override
      public void initialize() {
      }

      @Override
      public String getContentType() {
        return null;
      }
    };
  }

}
