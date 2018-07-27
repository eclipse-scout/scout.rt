package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.io.IOException;

import org.eclipse.scout.rt.shared.servicetunnel.HttpException;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.api.client.http.HttpResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    m_mockResponse = mock(HttpResponse.class);

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
    assertTrue(exception instanceof HttpException);

    // disconnect has been called also for status codes <> 200
    verify(m_mockResponse, times(1)).disconnect();
  }

}
