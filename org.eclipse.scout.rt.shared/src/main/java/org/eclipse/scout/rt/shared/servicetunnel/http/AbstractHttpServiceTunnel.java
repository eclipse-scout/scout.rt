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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.UriUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.context.ICancellable;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 * Abstract tunnel used to invoke a service through HTTP.
 */
public abstract class AbstractHttpServiceTunnel<T extends ISession> extends AbstractServiceTunnel<T> {

  public static final String TOKEN_AUTH_HTTP_HEADER = "X-ScoutAccessToken";

  private IServiceTunnelContentHandler m_contentHandler;
  private final URL m_serverUrl;

  public AbstractHttpServiceTunnel(T session) {
    this(session, getConfiguredServerUrl());
  }

  public AbstractHttpServiceTunnel(T session, URL url) {
    super(session);
    m_serverUrl = url;
  }

  protected static URL getConfiguredServerUrl() {
    IConfigProperty<String> targetUrlProperty = CONFIG.getProperty(ServiceTunnelTargetUrlProperty.class);
    String url = targetUrlProperty.getValue();
    try {
      URL targetUrl = UriUtility.toUrl(url);
      if (targetUrl == null) {
        throw new IllegalArgumentException("No target url configured. Please specify a target URL in the config.properties using property '" + targetUrlProperty.getKey() + "'.");
      }
      return targetUrl;
    }
    catch (ProcessingException e) {
      throw new IllegalArgumentException("targetUrl: " + url, e);
    }
  }

  public URL getServerUrl() {
    return m_serverUrl;
  }

  /**
   * @param call
   *          the original call
   * @param callData
   *          the data created by the {@link IServiceTunnelContentHandler} used
   *          by this tunnel Create url connection and write post data (if
   *          required)
   * @throws IOException
   *           override this method to customize the creation of the {@link URLConnection} see
   *           {@link #addCustomHeaders(URLConnection, String)}
   */
  protected URLConnection createURLConnection(IServiceTunnelRequest call, byte[] callData) throws IOException {
    // fast check of dummy URL's
    if (getServerUrl().getProtocol().startsWith("file")) {
      throw new IOException("File connection is not supporting HTTP: " + getServerUrl());
    }

    // configure POST with text/xml
    URLConnection urlConn = getServerUrl().openConnection();
    String contentType = "text/xml";
    urlConn.setRequestProperty("Content-type", contentType);
    urlConn.setDoOutput(true);
    urlConn.setDoInput(true);
    urlConn.setDefaultUseCaches(false);
    urlConn.setUseCaches(false);
    addCustomHeaders(urlConn, "POST", callData);
    try (OutputStream httpOut = urlConn.getOutputStream()) {
      httpOut.write(callData);
    }
    return urlConn;
  }

  /**
   * @param method
   *          GET or POST override this method to add custom HTTP headers
   */
  protected void addCustomHeaders(URLConnection urlConn, String method, byte[] callData) throws IOException {
    addSignatureHeader(urlConn, method, callData);
  }

  protected void addSignatureHeader(URLConnection urlConn, String method, byte[] callData) throws IOException {
    try {
      String token = createAuthToken(urlConn, method, callData);
      if (StringUtility.hasText(token)) {
        urlConn.setRequestProperty(TOKEN_AUTH_HTTP_HEADER, token);
      }
    }
    catch (ProcessingException e) {
      throw new IOException(e);
    }
  }

  protected String createAuthToken(URLConnection urlConn, String method, byte[] callData) throws ProcessingException {
    if (!DefaultAuthToken.isActive()) {
      return null;
    }
    String userId = CollectionUtility.firstElement(getSession().getSubject().getPrincipals()).getName();
    DefaultAuthToken token = BEANS.get(DefaultAuthToken.class);
    token.init(userId);
    return token.toString();
  }

  /**
   * @return msgEncoder used to encode and decode a request / response to and
   *         from the binary stream. Default is the {@link DefaultServiceTunnelContentHandler} which handles soap style
   *         messages
   */
  public IServiceTunnelContentHandler getContentHandler() {
    return m_contentHandler;
  }

  /**
   * @param msgEncoder
   *          that can encode and decode a request / response to and from the
   *          binary stream. Default is the {@link DefaultServiceTunnelContentHandler} which handles soap
   *          style messages
   */
  public void setContentHandler(IServiceTunnelContentHandler e) {
    m_contentHandler = e;
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) throws ProcessingException {
    if (m_contentHandler == null) {
      m_contentHandler = new DefaultServiceTunnelContentHandler();
      m_contentHandler.initialize();
    }
    return super.invokeService(serviceInterfaceClass, operation, callerArgs);
  }

  @Override
  // Method overwritten to be accessible from within @{link RemoteServiceInvocationCallable}.
  protected IServiceTunnelRequest createServiceTunnelRequest(Class serviceInterfaceClass, Method operation, Object[] args) {
    return super.createServiceTunnelRequest(serviceInterfaceClass, operation, args);
  }

  /**
   * Creates the {@link Callable} to invoke the remote service operation described by 'serviceRequest'.
   * <p>
   * To enable cancellation, the callable returned must also implement {@link ICancellable}, so that the remote
   * operation can be cancelled once the current {@link RunMonitor} gets cancelled.
   */
  protected RemoteServiceInvocationCallable createRemoteServiceInvocationCallable(IServiceTunnelRequest serviceRequest) {
    return new RemoteServiceInvocationCallable(this, serviceRequest);
  }

  @Override
  protected IServiceTunnelResponse tunnel(final IServiceTunnelRequest serviceRequest) {
    final long requestSequence = serviceRequest.getRequestSequence();

    // Create the Callable to be given to the job manager for execution.
    final RemoteServiceInvocationCallable remoteInvocationCallable = createRemoteServiceInvocationCallable(serviceRequest);

    // Create a monitor and register it as child monitor of the current monitor so that the service request is cancelled once the current monitor gets cancelled.
    // Furthermore, this monitor is given to the job manager, so that the job is cancelled as well.
    final RunMonitor monitor = BEANS.get(RunMonitor.class);
    monitor.registerCancellable(remoteInvocationCallable);
    RunMonitor.CURRENT.get().registerCancellable(monitor);

    // Invoke the service operation asynchronously (to enable cancellation) and wait until completed or cancelled.
    final JobInput jobInput = Jobs.newInput(createCurrentRunContext().runMonitor(monitor)).name("Remote service request [%s]", requestSequence);

    final IServiceTunnelResponse serviceResponse;
    try {
      serviceResponse = Jobs.schedule(remoteInvocationCallable, jobInput).awaitDoneAndGet();
    }
    catch (final Throwable t) {
      return new ServiceTunnelResponse(null, null, t);
    }

    if (monitor.isCancelled()) {
      return new ServiceTunnelResponse(null, null, new InterruptedException(ScoutTexts.get("UserInterrupted")));
    }
    return serviceResponse;
  }

  /**
   * @return a copy of the current calling context to be used to invoke the remote service operation.
   */
  protected RunContext createCurrentRunContext() {
    return RunContexts.copyCurrent();
  }

  /**
   * This method is called just after the http response is received but before
   * the http response is processed by scout. This might be used to read and
   * interpret custom http headers.
   *
   * @since 06.07.2009
   */
  protected void preprocessHttpResponse(URLConnection urlConn, IServiceTunnelRequest call, int httpCode) {
  }
}
