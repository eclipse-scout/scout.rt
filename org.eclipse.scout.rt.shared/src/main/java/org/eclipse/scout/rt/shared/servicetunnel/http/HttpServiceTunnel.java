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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.UriUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledException;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 * Abstract tunnel used to invoke a service through HTTP.
 */
public class HttpServiceTunnel extends AbstractServiceTunnel {

  public static final String TOKEN_AUTH_HTTP_HEADER = "X-ScoutAccessToken";

  private IServiceTunnelContentHandler m_contentHandler;
  private final URL m_serverUrl;
  private final boolean m_active;

  public HttpServiceTunnel() {
    this(getConfiguredServerUrl());
  }

  public HttpServiceTunnel(URL url) {
    m_serverUrl = url;
    m_active = url != null;
  }

  protected static URL getConfiguredServerUrl() {
    String url = BEANS.get(ServiceTunnelTargetUrlProperty.class).getValue();
    try {
      return UriUtility.toUrl(url);
    }
    catch (RuntimeException e) {
      throw new IllegalArgumentException("targetUrl: " + url, e);
    }
  }

  @Override
  public boolean isActive() {
    return m_active;
  }

  public URL getServerUrl() {
    return m_serverUrl;
  }

  /**
   * @param call
   *          the original call
   * @param callData
   *          the data created by the {@link IServiceTunnelContentHandler} used by this tunnel Create url connection and
   *          write post data (if required)
   * @throws IOException
   *           override this method to customize the creation of the {@link URLConnection} see
   *           {@link #addCustomHeaders(URLConnection, String)}
   */
  protected URLConnection createURLConnection(ServiceTunnelRequest call, byte[] callData) throws IOException {
    // fast check of wrong URL's for this tunnel
    if (!"http".equalsIgnoreCase(getServerUrl().getProtocol()) && !"https".equalsIgnoreCase(getServerUrl().getProtocol())) {
      throw new IOException("URL '" + getServerUrl().toString() + "' is not supported by this tunnel ('" + getClass().getName() + "').");
    }
    if (!isActive()) {
      String key = BEANS.get(ServiceTunnelTargetUrlProperty.class).getKey();
      throw new IllegalArgumentException("No target URL configured. Please specify a target URL in the config.properties using property '" + key + "'.");
    }

    // configure POST with text/xml
    URLConnection urlConn = getServerUrl().openConnection();
    String contentType = "text/xml";
    urlConn.setRequestProperty("Content-type", contentType);
    urlConn.setDoOutput(true);
    urlConn.setDoInput(true);
    urlConn.setDefaultUseCaches(false);
    urlConn.setUseCaches(false);
    addCustomHeaders(urlConn, "POST", call, callData);
    try (OutputStream httpOut = urlConn.getOutputStream()) {
      httpOut.write(callData);
    }
    return urlConn;
  }

  /**
   * @param urlConn
   *          connection object
   * @param method
   *          GET or POST override this method to add custom HTTP headers
   * @param call
   *          request information
   * @param callData
   *          data as byte array
   * @throws IOException
   * @since 6.0
   */
  protected void addCustomHeaders(URLConnection urlConn, String method, ServiceTunnelRequest call, byte[] callData) throws IOException {
    addSignatureHeader(urlConn, method, callData);
    addCorrelationId(urlConn);
  }

  protected void addSignatureHeader(URLConnection urlConn, String method, byte[] callData) throws IOException {
    try {
      String token = createAuthToken(urlConn, method, callData);
      if (StringUtility.hasText(token)) {
        urlConn.setRequestProperty(TOKEN_AUTH_HTTP_HEADER, token);
      }
    }
    catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  /**
   * Method invoked to add the <em>correlation ID</em> as HTTP header to the request.
   */
  protected void addCorrelationId(final URLConnection urlConn) throws IOException {
    final String cid = CorrelationId.CURRENT.get();
    if (cid != null) {
      urlConn.setRequestProperty(CorrelationId.HTTP_HEADER_NAME, cid);
    }
  }

  protected String createAuthToken(URLConnection urlConn, String method, byte[] callData) {
    DefaultAuthToken token = DefaultAuthToken.create();
    if (token == null) {
      return null;
    }
    return token.toString();
  }

  /**
   * @return msgEncoder used to encode and decode a request / response to and from the binary stream. Default is the
   *         {@link BinaryServiceTunnelContentHandler} which handles binary messages
   */
  public IServiceTunnelContentHandler getContentHandler() {
    return m_contentHandler;
  }

  /**
   * @param msgEncoder
   *          that can encode and decode a request / response to and from the binary stream. Default is the
   *          {@link BinaryServiceTunnelContentHandler} which handles binary messages
   */
  public void setContentHandler(IServiceTunnelContentHandler e) {
    m_contentHandler = e;
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) {
    if (m_contentHandler == null) {
      m_contentHandler = BEANS.get(IServiceTunnelContentHandler.class);
      m_contentHandler.initialize();
    }
    return super.invokeService(serviceInterfaceClass, operation, callerArgs);
  }

  /**
   * Creates the {@link Callable} to invoke the remote service operation described by 'serviceRequest'.
   * <p>
   * To enable cancellation, the callable returned must also implement {@link ICancellable}, so that the remote
   * operation can be cancelled once the current {@link RunMonitor} gets cancelled.
   */
  protected RemoteServiceInvocationCallable createRemoteServiceInvocationCallable(ServiceTunnelRequest serviceRequest) {
    return new RemoteServiceInvocationCallable(this, serviceRequest);
  }

  @Override
  protected ServiceTunnelResponse tunnel(final ServiceTunnelRequest serviceRequest) {
    final long requestSequence = serviceRequest.getRequestSequence();

    // Create the Callable to be given to the job manager for execution.
    final RemoteServiceInvocationCallable remoteInvocationCallable = createRemoteServiceInvocationCallable(serviceRequest);

    // Register the execution monitor as child monitor of the current monitor so that the service request is cancelled once the current monitor gets cancelled.
    // Invoke the service operation asynchronously (to enable cancellation) and wait until completed or cancelled.
    final IFuture<ServiceTunnelResponse> future = Jobs
        .schedule(remoteInvocationCallable,
            Jobs.newInput().withRunContext(RunContext.CURRENT.get().copy())
                .withName(createServiceRequestName(requestSequence))
                .withExceptionHandling(null, false)) // do not handle uncaught exceptions because typically invoked from within a model job (might cause a deadlock, because ClientExceptionHandler schedules and waits for a model job to visualize the exception).
        .whenDone(new IDoneHandler<ServiceTunnelResponse>() {

          @Override
          public void onDone(DoneEvent<ServiceTunnelResponse> event) {
            if (event.isCancelled()) {
              remoteInvocationCallable.cancel();
            }
          }
        }, RunContext.CURRENT.get().copy()
            .withRunMonitor(BEANS.get(RunMonitor.class))); // separate monitor to not cancel this cancellation action.

    try {
      return future.awaitDoneAndGet();
    }
    catch (ThreadInterruptedException e) {
      future.cancel(true); // Ensure the monitor to be cancelled once this thread is interrupted to cancel the remote call.
      return new ServiceTunnelResponse(new ThreadInterruptedException(ScoutTexts.get("UserInterrupted"))); // Interruption has precedence over computation result or computation error.
    }
    catch (FutureCancelledException e) {
      return new ServiceTunnelResponse(new ThreadInterruptedException(ScoutTexts.get("UserInterrupted"))); // Cancellation has precedence over computation result or computation error.
    }
  }

  /**
   * This method is called just after the HTTP response is received, but before being processed, and might be used to
   * read and interpret custom HTTP headers.
   */
  protected void interceptHttpResponse(URLConnection urlConn, ServiceTunnelRequest call, int httpCode) {
    // subclasses may intercept HTTP response
  }

  /**
   * Returns the name to decorate the thread's name while executing the service request.
   */
  protected String createServiceRequestName(final long requestSequence) {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    final String submitter = (currentFuture != null ? currentFuture.getJobInput().getName() : Thread.currentThread().getName());
    return String.format("Tunneling service request [seq=%s, submitter=%s]", requestSequence, submitter);
  }
}
