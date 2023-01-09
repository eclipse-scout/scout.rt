/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockMakers;
import org.mockito.Mockito;

import com.google.api.client.http.HttpResponse;

/**
 * It is essential to call {@link HttpResponse#disconnect()} even in case of an error, otherwise connections are not
 * returned to the connection pool (at least for some, if not all, HTTP clients) leading to a connection leak.
 */
public class RemoteServiceInvocationCallableDisconnectTest {

  private HttpResponse m_mockResponse;
  private RemoteServiceInvocationCallable m_callable;

  @Before
  public void prepareRemoteServiceInvocationCallable() throws IOException {
    ServiceTunnelRequest mockRequest = mock(ServiceTunnelRequest.class);
    m_mockResponse = mock(HttpResponse.class, Mockito.withSettings().mockMaker(MockMakers.INLINE));

    HttpServiceTunnel mockTunnel = mock(HttpServiceTunnel.class);
    when(mockTunnel.getContentHandler()).thenReturn(mock(IServiceTunnelContentHandler.class));
    when(mockTunnel.executeRequest(Mockito.eq(mockRequest), Mockito.any(byte[].class))).thenReturn(m_mockResponse);

    m_callable = new RemoteServiceInvocationCallable(mockTunnel, mockRequest);
  }

  @Test
  public void testDisconnectForErrorException() throws Exception {
    when(m_mockResponse.getContent()).thenThrow(NullPointerException.class);

    // disconnect has not been called yet
    verify(m_mockResponse, times(0)).disconnect();
    verify(m_mockResponse, times(0)).getContent();

    try {
      m_callable.call();
    }
    catch (NullPointerException e) {
      // disconnect has been called also for exceptions
      verify(m_mockResponse, times(1)).disconnect();
      verify(m_mockResponse, times(1)).getContent();
      return;
    }
    fail(); // expected an exception?
  }

  @Test
  public void testDisconnectForErrorHttpStatusCode() throws Exception {
    when(m_mockResponse.getStatusCode()).thenReturn(500);

    // disconnect has not been called yet
    verify(m_mockResponse, times(0)).disconnect();

    ServiceTunnelResponse response = m_callable.call();
    Throwable exception = response.getException();

    assertNotNull(exception);
    assertTrue(exception instanceof RemoteSystemUnavailableException);

    // disconnect has been called also for status codes <> 200
    verify(m_mockResponse, times(1)).disconnect();
  }
}
