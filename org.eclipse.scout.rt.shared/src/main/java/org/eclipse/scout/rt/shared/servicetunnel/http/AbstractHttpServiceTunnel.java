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
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.SecurityUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.UriUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobException;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
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

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractHttpServiceTunnel.class);

  public static final String TOKEN_AUTH_HTTP_HEADER = "X-ScoutAccessToken";

  public static final String PROP_PRIVATE_KEY = "org.eclipse.scout.rt.servicetunnel.signature.privatekey";
  public static final String PROP_TARGET_URL = "org.eclipse.scout.rt.servicetunnel.targetUrl";

  private static final byte[] PRIVATE_KEY;
  static {
    String privateKeyForSigning = ConfigIniUtility.getProperty(PROP_PRIVATE_KEY);
    if (StringUtility.hasText(privateKeyForSigning)) {
      PRIVATE_KEY = Base64Utility.decode(privateKeyForSigning);
      if (PRIVATE_KEY == null || PRIVATE_KEY.length < 1) {
        throw new IllegalArgumentException("Invalid digital signature private key configured.");
      }
    }
    else {
      PRIVATE_KEY = null;
    }
  }

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
    String url = ConfigIniUtility.getProperty(PROP_TARGET_URL, ConfigIniUtility.getProperty("server.url") /* legacy */);
    try {
      URL targetUrl = UriUtility.toUrl(url);
      if (targetUrl == null) {
        throw new IllegalArgumentException("No target url configured. Please specify a target URL in the config.ini using property '" + PROP_TARGET_URL + "'.");
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
   * Signals the server to cancel processing jobs for the current session.
   *
   * @return true if cancel was successful and transaction was in fact cancelled, false otherwise
   */
  protected boolean sendCancelRequest(long requestSequence) {
    try {
      IServiceTunnelRequest cancelCall = createServiceTunnelRequest(IServerProcessingCancelService.class, IServerProcessingCancelService.class.getMethod("cancel", long.class), new Object[]{requestSequence});
      IHttpBackgroundExecutable executor = createHttpBackgroundExecutor(cancelCall, new Object());
      IFuture<?> future = schedule(executor, cancelCall);
      try {
        future.awaitDoneAndGet(10, TimeUnit.SECONDS);
        IServiceTunnelResponse cancelResult = executor.getResponse();
        if (cancelResult == null) {
          return false;
        }
        if (cancelResult.getException() != null) {
          LOG.warn("cancel failed", cancelResult.getException());
          return false;
        }
        Boolean result = (Boolean) cancelResult.getData();
        return result != null && result.booleanValue();
      }
      catch (ProcessingException | JobException ie) {
        return false;
      }
    }
    catch (Throwable e) {
      LOG.warn("failed to cancel server processing", e);
      return false;
    }
  }

  protected abstract IFuture<?> schedule(IRunnable runnable, IServiceTunnelRequest req);

  /**
   * @param method
   *          GET or POST override this method to add custom HTTP headers
   */
  protected void addCustomHeaders(URLConnection urlConn, String method, byte[] callData) throws IOException {
    addSignatureHeader(urlConn, method, callData);
  }

  protected void addSignatureHeader(URLConnection urlConn, String method, byte[] callData) throws IOException {
    if (PRIVATE_KEY == null) {
      // no private -> no signature
      return;
    }

    try {
      byte[] signature = SecurityUtility.createSignature(PRIVATE_KEY, callData);
      urlConn.setRequestProperty(TOKEN_AUTH_HTTP_HEADER, Base64Utility.encode(signature));
    }
    catch (ProcessingException e) {
      throw new IOException(e);
    }
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

  protected IHttpBackgroundExecutable createHttpBackgroundExecutor(IServiceTunnelRequest request, Object lock) {
    return new HttpBackgroundExecutable(request, lock, this);
  }

  @Override
  protected IServiceTunnelResponse tunnel(final IServiceTunnelRequest req) {
    final Object backgroundLock = new Object();
    IHttpBackgroundExecutable executor = createHttpBackgroundExecutor(req, backgroundLock);

    // wait until done
    IServiceTunnelResponse res = null;
    boolean cancelled = false;
    boolean sentCancelRequest = false;
    synchronized (backgroundLock) {
      IFuture<?> future = schedule(executor, req);
      while (true) {
        res = executor.getResponse();
        if (res != null) {
          break;
        }
        if ((!sentCancelRequest) && future.isCancelled()) {
          sentCancelRequest = true;
          boolean success = sendCancelRequest(req.getRequestSequence());
          if (success) {
            // in fact cancelled the job
            cancelled = true;
            break;
          }
          else {
            // cancel was not possible, continue
          }
        }
        if (future.isDone()) {
          break;
        }
        try {
          backgroundLock.wait(500);
        }
        catch (InterruptedException ie) {
          break;
        }
      }
    }
    if (res == null || cancelled) {
      return new ServiceTunnelResponse(null, null, new InterruptedException(ScoutTexts.get("UserInterrupted")));
    }
    return res;
  }

  /**
   * This method is called just after the http response is received but before
   * the http response is processed by scout. This might be used to read and
   * interpret custom http headers.
   *
   * @since 06.07.2009
   */
  protected void preprocessHttpRepsonse(URLConnection urlConn, IServiceTunnelRequest call, int httpCode) {
  }
}
