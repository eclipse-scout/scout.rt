/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.BackendUrlProperty;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.rest.jersey.TestJerseyMockConnector;
import org.eclipse.scout.rt.rest.jersey.TestingRestClientConfigFactory;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.eclipse.scout.rt.testing.platform.mock.RegisterBeanTestRule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.testing.http.MockLowLevelHttpResponse;

/**
 * Tests for {@link HttpServiceTunnel}
 */
@RunWith(PlatformTestRunner.class)
public class HttpServiceTunnelTest {

  @Rule
  public MockConfigPropertyRule<String> m_mockBackendUrlPropertyRule = new MockConfigPropertyRule<>(BackendUrlProperty.class, "");

  @Rule
  public final RegisterBeanTestRule<BinaryServiceTunnelContentHandler> m_testContentHandlerBeanRule = new RegisterBeanTestRule<BinaryServiceTunnelContentHandler>(BinaryServiceTunnelContentHandler.class, () -> getTestContentHandler());

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(TestingRestClientConfigFactory.class).setupMockConnectorProvider();
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(TestingRestClientConfigFactory.class).resetConnectorProvider();
  }

  @Test
  public void testTunnel() throws IOException {
    MockLowLevelHttpResponse expectedResponse = new MockLowLevelHttpResponse().setContent(serialize(new ServiceTunnelResponse("testData")));
    HttpServiceTunnel tunnel = createHttpServiceTunnel(expectedResponse);

    ServiceTunnelRequest request = new ServiceTunnelRequest("IPingService", "ping", null, null);
    ServiceTunnelResponse response = tunnel.tunnel(request);
    assertNotNull(response);
    assertEquals("testData", response.getData());
  }

  @Test
  public void testTunnelException() throws IOException {
    MockLowLevelHttpResponse expectedResponse = new MockLowLevelHttpResponse().setStatusCode(401).setContent(InputStream.nullInputStream());
    HttpServiceTunnel tunnel = createHttpServiceTunnel(expectedResponse);

    ServiceTunnelRequest request = new ServiceTunnelRequest("IPingService", "ping", null, null);
    VetoException vetoException = assertThrows(VetoException.class, () -> tunnel.tunnel(request));
    assertTrue(vetoException.getCause() instanceof NotAuthorizedException);
  }

  protected HttpServiceTunnel createHttpServiceTunnel(final MockLowLevelHttpResponse expectedResponse) throws IOException {
    TestJerseyMockConnector testJerseyMockConnector = BEANS.get(TestJerseyMockConnector.class);
    testJerseyMockConnector.setNextResponseStatus(Status.fromStatusCode(expectedResponse.getStatusCode()));
    testJerseyMockConnector.setNextResponseEntityStream(expectedResponse.getContent());
    return new HttpServiceTunnel();
  }

  /**
   * {@link HttpServiceTunnel} should be inactive, if no url is defined.
   */
  @Test
  public void testInactive() {
    m_mockBackendUrlPropertyRule.setValue(null);
    HttpServiceTunnel tunnel = new HttpServiceTunnel();
    assertFalse(tunnel.isActive());

    m_mockBackendUrlPropertyRule.setValue("");
    tunnel = new HttpServiceTunnel();
    assertFalse(tunnel.isActive());
  }

  @Test
  public void testActive() {
    m_mockBackendUrlPropertyRule.setValue("http://example.com");
    HttpServiceTunnel tunnel = new HttpServiceTunnel();
    assertTrue(tunnel.isActive());
  }

  protected byte[] serialize(ServiceTunnelResponse response) throws IOException {
    return SerializationUtility.createObjectSerializer().serialize(response);
  }

  private BinaryServiceTunnelContentHandler getTestContentHandler() {
    return new BinaryServiceTunnelContentHandler() {

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
      protected void initialize() {
      }
    };
  }
}
