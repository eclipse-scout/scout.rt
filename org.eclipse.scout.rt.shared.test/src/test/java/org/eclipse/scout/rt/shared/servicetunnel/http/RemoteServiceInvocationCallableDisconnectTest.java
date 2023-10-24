/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * It is essential to call {@link InputStream#close()} even in case of an error, otherwise connections are not
 * returned to the connection pool (at least for some, if not all, HTTP clients) leading to a connection leak.
 */
public class RemoteServiceInvocationCallableDisconnectTest {

  private InputStream m_mockInputStream;
  private HttpResponse<InputStream> m_mockResponse;
  private RemoteServiceInvocationCallable m_callable;

  @Before
  public void prepareRemoteServiceInvocationCallable() throws IOException, InterruptedException, ClassNotFoundException {

    ServiceTunnelRequest mockRequest = mock(ServiceTunnelRequest.class);
    //noinspection unchecked
    m_mockResponse = (HttpResponse<InputStream>) mock(HttpResponse.class);
    m_mockInputStream = mock(InputStream.class);
    when(m_mockResponse.body()).thenReturn(m_mockInputStream);

    HttpServiceTunnel mockTunnel = mock(HttpServiceTunnel.class);
    IServiceTunnelContentHandler contentHandler = mock(IServiceTunnelContentHandler.class);
    when(contentHandler.readResponse(any())).then(invocation -> {
      //noinspection ResultOfMethodCallIgnored
      m_mockInputStream.read();
      return null;
    });
    when(mockTunnel.getContentHandler()).thenReturn(contentHandler);
    when(mockTunnel.executeRequest(Mockito.eq(mockRequest), Mockito.any(byte[].class))).thenReturn(m_mockResponse);

    m_callable = new RemoteServiceInvocationCallable(mockTunnel, mockRequest);
  }

  @Test
  public void testDisconnectForErrorException() throws Exception {
    when(m_mockInputStream.read()).thenThrow(NullPointerException.class);

    // disconnect has not been called yet
    verify(m_mockInputStream, times(0)).close();
    verify(m_mockInputStream, times(0)).read();

    try {
      m_callable.call();
    }
    catch (NullPointerException e) {
      // disconnect has been called also for exceptions
      verify(m_mockInputStream, times(1)).close();
      verify(m_mockInputStream, times(1)).read();
      return;
    }
    fail(); // expected an exception?
  }

  @Test
  public void testDisconnectForErrorHttpStatusCode() throws Exception {
    when(m_mockResponse.statusCode()).thenReturn(500);

    // disconnect has not been called yet
    verify(m_mockInputStream, times(0)).close();

    ServiceTunnelResponse response = m_callable.call();
    Throwable exception = response.getException();

    assertNotNull(exception);
    assertTrue(exception instanceof RemoteSystemUnavailableException);

    // disconnect has been called also for status codes <> 200
    verify(m_mockInputStream, times(1)).close();
  }
}
