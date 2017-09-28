/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.http.IHttpTransportBuilder;
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

    final MockLowLevelHttpResponse expectedResponse = new MockLowLevelHttpResponse().setContent(getInputStream(new ServiceTunnelResponse("testData", new Object[]{})));

    HttpServiceTunnel tunnel = new HttpServiceTunnel() {

      @Override
      protected IHttpTransportManager getHttpTransportManager() {
        return new IHttpTransportManager() {

          private MockHttpTransport m_transport = new MockHttpTransport.Builder()
              .setLowLevelHttpResponse(expectedResponse)
              .build();

          @Override
          public HttpTransport getHttpTransport() {
            return m_transport;
          }

          @Override
          public HttpRequestFactory getHttpRequestFactory() {
            return m_transport.createRequestFactory();
          }

          @Override
          public void interceptNewHttpTransport(IHttpTransportBuilder builder) {
            // nop
          }
        };
      }
    };
    tunnel.setContentHandler(getTestContentHandler());
    ServiceTunnelRequest request = new ServiceTunnelRequest("IPingService", "ping", null, null);
    ServiceTunnelResponse response = tunnel.tunnel(request);
    assertNotNull(response);
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

  private ByteArrayInputStream getInputStream(ServiceTunnelResponse response) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(response);
      oos.flush();
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private IServiceTunnelContentHandler getTestContentHandler() {
    return new IServiceTunnelContentHandler() {

      @Override
      public void writeResponse(OutputStream out, ServiceTunnelResponse msg) throws IOException {
      }

      @Override
      public void writeRequest(OutputStream out, ServiceTunnelRequest msg) throws IOException {
      }

      @Override
      public ServiceTunnelResponse readResponse(InputStream in) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bi = (ByteArrayInputStream) in;
        ObjectInputStream in2 = new ObjectInputStream(bi);
        Object o = in2.readObject();
        return (ServiceTunnelResponse) o;
      }

      @Override
      public ServiceTunnelRequest readRequest(InputStream in) throws IOException, ClassNotFoundException {
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
