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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.UriUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.http.HttpClientManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;
import org.eclipse.scout.rt.shared.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract tunnel used to invoke a service through HTTP.
 */
public class HttpServiceTunnel extends AbstractServiceTunnel {
  private static final Logger LOG = LoggerFactory.getLogger(HttpServiceTunnel.class);

  public static final String TOKEN_AUTH_HTTP_HEADER = "X-ScoutAccessToken";

  private IServiceTunnelContentHandler m_contentHandler;
  private final URI m_serverUri;
  private final boolean m_active;

  public HttpServiceTunnel() {
    this(getConfiguredServerUri());
  }

  public HttpServiceTunnel(URI uri) {
    m_serverUri = uri;
    URL url = UriUtility.uriToUrl(uri);
    m_active = uri != null
        && ("http".equalsIgnoreCase(url.getProtocol()) || !"https".equalsIgnoreCase(url.getProtocol())); // fast check of wrong URL's for this tunnel
  }

  protected static URI getConfiguredServerUri() {
    String uri = BEANS.get(ServiceTunnelTargetUrlProperty.class).getValue();
    try {
      return UriUtility.toUri(uri);
    }
    catch (RuntimeException e) {
      throw new IllegalArgumentException("targetUrl: " + uri, e);
    }
  }

  @Override
  public boolean isActive() {
    return m_active;
  }

  public URI getServerUri() {
    return m_serverUri;
  }

  /**
   * Execute a {@link ServiceTunnelRequest}, returns the plain {@link HttpResponse} - (executed and) ready to be
   * processed to create a {@link ServiceTunnelResponse}.
   *
   * @param call
   *          the original call
   * @param callData
   *          the data created by the {@link IServiceTunnelContentHandler} used by this tunnel Create url connection and
   *          write post data (if required)
   * @throws IOException
   *           override this method to customize the creation of the {@link HttpResponse} see
   *           {@link #addCustomHeaders(HttpRequest.Builder, ServiceTunnelRequest, byte[])}
   */
  protected HttpResponse<InputStream> executeRequest(ServiceTunnelRequest call, byte[] callData) throws IOException, InterruptedException {
    if (!isActive()) {
      String key = BEANS.get(ServiceTunnelTargetUrlProperty.class).getKey();
      throw new IllegalArgumentException("Unsupported or no target URL configured. Please specify a target URL in the config.properties using property '" + key + "'.");
    }

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(getServerUri())
        .POST(BodyPublishers.ofByteArray(callData))
        .header("Content-Type", getContentHandler().getContentType())
        .header("Cache-Control", "no-cache")
        .header("Pragma", "no-cache");
    addCustomHeaders(requestBuilder, call, callData);

    return BEANS.get(HttpClientManager.class).getHttpClient().send(
        requestBuilder.build(),
        BodyHandlers.ofInputStream());
  }

  /**
   * @return the {@link IHttpTransportManager}
   */
  protected IHttpTransportManager getHttpTransportManager() {
    return BEANS.get(HttpServiceTunnelTransportManager.class);
  }

  /**
   * @param httpRequestBuilder
   *          request builder
   * @param call
   *          request information
   * @param callData
   *          data as byte array
   * @since 6.0
   */
  protected void addCustomHeaders(HttpRequest.Builder httpRequestBuilder, ServiceTunnelRequest call, byte[] callData) throws IOException {
    addSignatureHeader(httpRequestBuilder, callData);
    addCorrelationId(httpRequestBuilder);
  }

  protected void addSignatureHeader(HttpRequest.Builder httpRequestBuilder, byte[] callData) throws IOException {
    try {
      DefaultAuthToken token = BEANS.get(DefaultAuthTokenSigner.class).createDefaultSignedToken(DefaultAuthToken.class);
      if (token != null) {
        httpRequestBuilder.header(TOKEN_AUTH_HTTP_HEADER, token.toString());
      }
    }
    catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  /**
   * Method invoked to add the <em>correlation ID</em> as HTTP header to the request.
   */
  protected void addCorrelationId(final HttpRequest.Builder httpRequestBuilder) {
    final String cid = CorrelationId.CURRENT.get();
    if (cid != null) {
      httpRequestBuilder.header(CorrelationId.HTTP_HEADER_NAME, cid);
    }
  }

  /**
   * @return msgEncoder used to encode and decode a request / response to and from the binary stream. Default is the
   *         {@link BinaryServiceTunnelContentHandler} which handles binary messages
   */
  public IServiceTunnelContentHandler getContentHandler() {
    return m_contentHandler;
  }

  /**
   * @param e
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
    if (LOG.isDebugEnabled()) {
      LOG.debug("requestSequence {} {}.{}", serviceRequest.getRequestSequence(), serviceRequest.getServiceInterfaceClassName(), serviceRequest.getOperation());
    }
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
        .whenDone(event -> {
          if (event.isCancelled()) {
            remoteInvocationCallable.cancel();
          }
        }, RunContext.CURRENT.get().copy()
            .withRunMonitor(BEANS.get(RunMonitor.class))); // separate monitor to not cancel this cancellation action.

    try {
      return future.awaitDoneAndGet();
    }
    catch (ThreadInterruptedError e) { // NOSONAR
      future.cancel(true); // Ensure the monitor to be cancelled once this thread is interrupted to cancel the remote call.
      return new ServiceTunnelResponse(new ThreadInterruptedError("UserInterrupted")); // Interruption has precedence over computation result or computation error.
    }
    catch (FutureCancelledError e) { // NOSONAR
      return new ServiceTunnelResponse(new FutureCancelledError("UserInterrupted")); // Cancellation has precedence over computation result or computation error.
    }
  }

  /**
   * This method is called just after the HTTP response is received, but before being processed, and might be used to
   * read and interpret custom HTTP headers.
   */
  protected void interceptHttpResponse(HttpResponse httpResponse, ServiceTunnelRequest call) {
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
