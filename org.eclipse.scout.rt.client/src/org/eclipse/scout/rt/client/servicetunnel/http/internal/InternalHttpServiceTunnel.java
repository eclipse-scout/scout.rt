/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 *
 */
public class InternalHttpServiceTunnel extends AbstractServiceTunnel {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InternalHttpServiceTunnel.class);

  private IServiceTunnelContentHandler m_contentHandler;
  private ClientNotificationPollingJob m_pollingJob;
  private final Object m_pollingJobLock = new Object();
  //
  private String m_ajaxSessionId;
  private String m_ajaxUserId;

  public InternalHttpServiceTunnel(IClientSession session, String url) throws ProcessingException {
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
  public InternalHttpServiceTunnel(IClientSession session, String url, String version) throws ProcessingException {
    super(session, version);
    try {
      if (url != null) {
        setServerURL(new URL(url));
      }
    }
    catch (MalformedURLException e) {
      throw new ProcessingException(url, e);
    }
    if (session.getWebSessionId() != null) {
      m_ajaxSessionId = session.getWebSessionId();
      String userId = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().getName();
      m_ajaxUserId = userId;
    }
  }

  @Override
  public void setAnalyzeNetworkLatency(boolean b) {
    super.setAnalyzeNetworkLatency(b);
    updatePollingJobInternal();
  }

  @Override
  public void setClientNotificationPollInterval(long intervallMillis) {
    super.setClientNotificationPollInterval(intervallMillis);
    updatePollingJobInternal();
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
  protected URLConnection createURLConnection(ServiceTunnelRequest call, byte[] callData) throws IOException {
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
    if (m_ajaxSessionId != null && m_ajaxUserId != null) {
      urlConn.setRequestProperty("Ajax-SessionId", m_ajaxSessionId);
      urlConn.setRequestProperty("Ajax-UserId", m_ajaxUserId);
    }
  }

  private void updatePollingJobInternal() {
    synchronized (m_pollingJobLock) {
      long p = getClientNotificationPollInterval();
      boolean b = isAnalyzeNetworkLatency();
      if (p > 0) {
        if (m_pollingJob == null) {
          m_pollingJob = new ClientNotificationPollingJob(getClientSession(), p, b);
          m_pollingJob.schedule();
        }
        else {
          m_pollingJob.updatePollingValues(p, b);
        }
      }
      else {
        if (m_pollingJob != null) {
          m_pollingJob.cancel();
          m_pollingJob = null;
        }
      }
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
      String prefix = getClientSession().getClass().getPackage().getName().replaceAll("^(.*\\.)(client|shared|server)(\\.core)?.*$", "$1");
      m_contentHandler.initialize(BundleInspector.getOrderedBundleList(prefix, "org.eclipse.scout."), getClientSession().getClass().getClassLoader());
    }
    return super.invokeService(serviceInterfaceClass, operation, callerArgs);
  }

  @Override
  protected ServiceTunnelResponse tunnelOnline(final ServiceTunnelRequest req) {
    if (ClientJob.isCurrentJobCanceled()) {
      return new ServiceTunnelResponse(null, null, new InterruptedException(ScoutTexts.get("UserInterrupted")));
    }
    final Object backgroundLock = new Object();
    HttpBackgroundJob backgroundJob = new HttpBackgroundJob(ScoutTexts.get("ServerCallProcessing"), req, backgroundLock, this);
    decorateBackgroundJob(req, backgroundJob);
    // wait until done
    ServiceTunnelResponse res = null;
    synchronized (backgroundLock) {
      backgroundJob.schedule();
      while (true) {
        res = backgroundJob.getResponse();
        if (res != null) {
          break;
        }
        if (JobEx.isCurrentJobCanceled()) {
          break;
        }
        IProgressMonitor mon = backgroundJob.getMonitor();
        if (mon != null && mon.isCanceled()) {
          break;
        }
        if (backgroundJob.getState() == JobEx.NONE) {
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

    IProgressMonitor mon = backgroundJob.getMonitor();
    if (JobEx.isCurrentJobCanceled() || (mon != null && mon.isCanceled())) {
      sendCancelRequest();
    }

    if (res == null) {
      backgroundJob.cancel();
      return new ServiceTunnelResponse(null, null, new InterruptedException(ScoutTexts.get("UserInterrupted")));
    }
    else {
      return res;
    }
  }

  /**
   * Signals the server to cancel processing jobs for the current session.
   */
  protected void sendCancelRequest() {
    try {
      ServiceTunnelRequest cancelHttpRequest = new ServiceTunnelRequest(getVersion(), IServerProcessingCancelService.class, IServerProcessingCancelService.class.getMethod("cancel"), new Object[0]);
      HttpBackgroundJob cancelHttpJob = new HttpBackgroundJob(ScoutTexts.get("ServerCallCancelProcessing"), cancelHttpRequest, new Object(), this);
      cancelHttpJob.schedule();
    }
    catch (Throwable e) {
      LOG.warn("failed to cancel server processing", e);
    }
  }

  /**
   * Override this method to decide when background jobs to the backend should be presented to the user or not (for
   * cancelling)
   * The default makes all jobs cancellable except IPingService (used for client notification polling)
   */
  protected void decorateBackgroundJob(ServiceTunnelRequest call, Job backgroundJob) {
    backgroundJob.setUser(false);
    if (call.getServiceInterfaceClassName().equals(IPingService.class.getName())) {
      backgroundJob.setSystem(true);
    }
    else {
      backgroundJob.setSystem(false);
    }
  }

  /**
   * This method is called just after the http response is received but before
   * the http response is processed by scout. This might be used to read and
   * interpret custom http headers.
   * 
   * @since 06.07.2009
   */
  protected void preprocessHttpRepsonse(URLConnection urlConn, ServiceTunnelRequest call, int httpCode) {
  }

}
