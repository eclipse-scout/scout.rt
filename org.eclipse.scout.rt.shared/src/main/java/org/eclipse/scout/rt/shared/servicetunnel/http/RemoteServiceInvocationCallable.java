/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;
import org.eclipse.scout.rt.shared.servicetunnel.HttpException;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a {@link Callable} to invoke the service operation as described by {@link IServiceTunnelRequest}
 * remotely on backend server. The HTTP connection is established based on the {@link AbstractHttpServiceTunnel}.
 * Additionally, this class implements {@link ICancellable} to cancel the ongoing request.
 * <p>
 * This class is intended to be given to the job manager for execution and to be registered within the
 * {@link RunMonitor} for cancellation support.
 *
 * @see AbstractHttpServiceTunnel
 */
public class RemoteServiceInvocationCallable implements Callable<ServiceTunnelResponse> {

  private static final Logger LOG = LoggerFactory.getLogger(RemoteServiceInvocationCallable.class);

  private final HttpServiceTunnel m_tunnel;
  private final ServiceTunnelRequest m_serviceRequest;

  public RemoteServiceInvocationCallable(final HttpServiceTunnel tunnel, final ServiceTunnelRequest serviceRequest) {
    m_tunnel = tunnel;
    m_serviceRequest = serviceRequest;
  }

  /**
   * Invokes the remote service operation.
   *
   * @return {@link IServiceTunnelResponse}; is never <code>null</code>.
   */
  @Override
  public ServiceTunnelResponse call() throws Exception {
    long nBytes = 0;

    final long tStart = LOG.isDebugEnabled() ? System.nanoTime() : 0L;
    try {
      // Create the request.
      final ByteArrayOutputStream requestMessage = new ByteArrayOutputStream();
      m_tunnel.getContentHandler().writeRequest(requestMessage, m_serviceRequest);
      requestMessage.close();
      final byte[] requestData = requestMessage.toByteArray();
      nBytes = requestData.length;

      // Send the request to the server.
      final URLConnection urlConnection = m_tunnel.createURLConnection(m_serviceRequest, requestData);

      // Receive the response.
      final int httpStatusCode = (urlConnection instanceof HttpURLConnection ? ((HttpURLConnection) urlConnection).getResponseCode() : 200);
      m_tunnel.interceptHttpResponse(urlConnection, m_serviceRequest, httpStatusCode);
      if (httpStatusCode != 0 && (httpStatusCode < 200 || httpStatusCode > 299)) {
        return new ServiceTunnelResponse(new HttpException(httpStatusCode)); // request failed
      }

      try (InputStream in = urlConnection.getInputStream()) {
        return m_tunnel.getContentHandler().readResponse(in);
      }
    }
    finally {
      if (LOG.isDebugEnabled()) {
        final long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tStart);
        LOG.debug("TIME {}.{} {}ms {} bytes", m_serviceRequest.getServiceInterfaceClassName(), m_serviceRequest.getOperation(), elapsedMillis, nBytes);
      }
    }
  }

  /**
   * Cancels the remote service operation on server side.
   */
  public void cancel() {
    try {
      final String sessionId = m_serviceRequest.getSessionId();
      if (sessionId == null) {
        return; // cannot cancel an event without session. The IRunMonitorCancelService requires a session.
      }

      final Method serviceMethod = IRunMonitorCancelService.class.getMethod(IRunMonitorCancelService.CANCEL_METHOD, long.class);
      final Object[] serviceArgs = new Object[]{m_serviceRequest.getRequestSequence()};
      ServiceTunnelRequest request = m_tunnel.createRequest(IRunMonitorCancelService.class, serviceMethod, serviceArgs);
      request.setClientNodeId(m_serviceRequest.getClientNodeId());
      request.setSessionId(sessionId);
      request.setUserAgent(m_serviceRequest.getUserAgent());
      m_tunnel.invokeService(request);
    }
    catch (final FutureCancelledError | ThreadInterruptedError e) { // NOSONAR
      // NOOP: Do not cancel 'cancel-request' to prevent loop.
    }
    catch (RuntimeException | NoSuchMethodException e) {
      LOG.warn("Failed to cancel server processing [requestSequence={}]", m_serviceRequest.getRequestSequence(), e);
    }
  }
}
