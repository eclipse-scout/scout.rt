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
package org.eclipse.scout.rt.servicetunnel.http.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 * Abstract non-public implementation of a tunnel used to invoke a service through HTTP.
 *
 * @author awe (refactoring)
 */
public abstract class AbstractInternalHttpServiceTunnel<T extends ISession> extends AbstractServiceTunnel<T> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractInternalHttpServiceTunnel.class);

  private IServiceTunnelContentHandler m_contentHandler;

  public AbstractInternalHttpServiceTunnel(T session, URL url) {
    this(session, url, null);
  }

  /**
   * @param url
   * @param version
   *          the version that is sent down to the server with every request.
   *          This allows the server to check client request and refuse old
   *          clients. Check the servers HttpProxyHandlerServlet init-parameter
   *          (example: min-version="0.0.0") If the version parameter is null,
   *          the product bundle (for example com.myapp.ui.swing) version is
   *          used
   */
  public AbstractInternalHttpServiceTunnel(T session, URL url, String version) {
    super(session, version);
    setServerURL(url);
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
    if (getServerURL().getProtocol().startsWith("file")) {
      throw new IOException("File connection is not supporting HTTP: " + getServerURL());
    }
    URLConnection urlConn;
    // configure POST with text/xml
    urlConn = getServerURL().openConnection();
    String contentType = "text/xml";
    urlConn.setRequestProperty("Content-type", contentType);
    urlConn.setDoOutput(true);
    urlConn.setDoInput(true);
    urlConn.setDefaultUseCaches(false);
    urlConn.setUseCaches(false);
    addCustomHeaders(urlConn, "POST");
    OutputStream httpOut = urlConn.getOutputStream();
    httpOut.write(callData);
    httpOut.close();
    httpOut = null;
    return urlConn;
  }

  /**
   * @param method
   *          GET or POST override this method to add custom HTTP headers
   */
  protected void addCustomHeaders(URLConnection urlConn, String method) throws IOException {
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
  protected IServiceTunnelResponse tunnel(final IServiceTunnelRequest req) throws JobExecutionException {
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
   * Signals the server to cancel processing jobs for the current session.
   *
   * @return true if cancel was successful and transaction was in fact cancelled, false otherwise
   */
  protected boolean sendCancelRequest(long requestSequence) {
    try {
      IServiceTunnelRequest cancelCall = createServiceTunnelRequest(getVersion(), IServerProcessingCancelService.class, IServerProcessingCancelService.class.getMethod("cancel", long.class), new Object[]{requestSequence});
      IHttpBackgroundExecutable executor = createHttpBackgroundExecutor(cancelCall, new Object());
      IFuture<?> future = schedule(executor, cancelCall);
      try {
        future.get(10, TimeUnit.SECONDS);
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
      catch (ProcessingException ie) {
        return false;
      }
    }
    catch (Throwable e) {
      LOG.warn("failed to cancel server processing", e);
      return false;
    }
  }

  protected abstract IFuture<?> schedule(IRunnable runnable, IServiceTunnelRequest req) throws JobExecutionException;

  protected IHttpBackgroundExecutable createHttpBackgroundExecutor(IServiceTunnelRequest request, Object lock) {
    return new HttpBackgroundExecutable(request, lock, this);
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
