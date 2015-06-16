/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.ICancellable;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;
import org.eclipse.scout.rt.shared.servicetunnel.HttpException;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

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
public class RemoteServiceInvocationCallable implements Callable<IServiceTunnelResponse>, ICancellable {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RemoteServiceInvocationCallable.class);

  private final AbstractHttpServiceTunnel m_tunnel;
  private final ServiceTunnelRequest m_serviceRequest;

  private final AtomicBoolean m_cancelled = new AtomicBoolean(false);

  public RemoteServiceInvocationCallable(final AbstractHttpServiceTunnel tunnel, final ServiceTunnelRequest serviceRequest) {
    m_tunnel = tunnel;
    m_serviceRequest = serviceRequest;
  }

  /**
   * Invokes the remote service operation.
   *
   * @return {@link IServiceTunnelResponse}; is never <code>null</code>.
   */
  @Override
  public IServiceTunnelResponse call() throws Exception {
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
      final URLConnection m_urlConnection = m_tunnel.createURLConnection(m_serviceRequest, requestData);

      // Receive the response.
      final int httpStatusCode = (m_urlConnection instanceof HttpURLConnection ? ((HttpURLConnection) m_urlConnection).getResponseCode() : 200);
      m_tunnel.preprocessHttpResponse(m_urlConnection, m_serviceRequest, httpStatusCode);
      if (httpStatusCode != 0 && (httpStatusCode < 200 || httpStatusCode > 299)) {
        return new ServiceTunnelResponse(null, null, new HttpException(httpStatusCode)); // request failed
      }

      try (InputStream in = m_urlConnection.getInputStream()) {
        return m_tunnel.getContentHandler().readResponse(in);
      }
    }
    finally {
      if (LOG.isDebugEnabled()) {
        final long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tStart);
        LOG.debug("TIME {}.{} {}ms {} bytes", new Object[]{m_serviceRequest.getServiceInterfaceClassName(), m_serviceRequest.getOperation(), elapsedMillis, nBytes});
      }
    }
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    // From UI Server, this method is invoked from 'JsonMessageRequestInterceptor.handleCancelRequest(IUiSession)'

    final long requestSequence = m_serviceRequest.getRequestSequence();

    if (!m_cancelled.compareAndSet(false, true)) {
      return false;
    }

    try {
      final ServiceTunnelRequest cancelRequest = m_tunnel.createServiceTunnelRequest(IRunMonitorCancelService.class, IRunMonitorCancelService.class.getMethod(IRunMonitorCancelService.CANCEL_METHOD, long.class), new Object[]{requestSequence});
      final RemoteServiceInvocationCallable remoteInvocationCallable = m_tunnel.createRemoteServiceInvocationCallable(cancelRequest);

      final RunMonitor runMonitor = BEANS.get(RunMonitor.class); // do not link the RunMonitor with the current RunMonitor to not cancel this request.
      final JobInput jobInput = Jobs.newInput(m_tunnel.createCurrentRunContext().runMonitor(runMonitor)).name("Cancellation request [%s]", requestSequence);
      final IServiceTunnelResponse cancelResponse = Jobs.schedule(remoteInvocationCallable, jobInput).awaitDoneAndGet(10, TimeUnit.SECONDS);

      if (cancelResponse == null) {
        return false;
      }
      if (cancelResponse.getException() != null) {
        LOG.warn("Failed to cancel server processing", cancelResponse.getException());
        return false;
      }

      return ((Boolean) cancelResponse.getData()).booleanValue();
    }
    catch (final Exception e) {
      LOG.warn(String.format("Failed to cancel server processing [requestSequence=%s]", requestSequence), e);
      return false;
    }
  }

  @Override
  public boolean isCancelled() {
    return m_cancelled.get();
  }
}
