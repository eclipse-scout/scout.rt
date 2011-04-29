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
package org.eclipse.scout.rt.client.servicetunnel.http;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.servicetunnel.http.internal.InternalHttpServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

public class HttpServiceTunnel extends InternalHttpServiceTunnel {
  public static final String HTTP_DEBUG_PARAM = "org.eclipse.scout.rt.client.http.debug";

  private String m_ajaxSessionId;
  private String m_ajaxUserId;

  public HttpServiceTunnel(IClientSession session, String url) throws ProcessingException {
    super(session, url);
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
  public HttpServiceTunnel(IClientSession session, String url, String version) throws ProcessingException {
    super(session, url, version);
  }

  /**
   * true: The ajaxSessionId is sent with every request as http header "Ajax-SessionId" to the /process servlet.
   * <p>
   * This is necessary since the web-gui-servlet is itself a single servlet that is calling the server-servlet /process
   * with a single cookie and therefore a single http session. When "Ajax-SessionId" is set, the ServiceTunnelServlet
   * /process recognizes this and is not associating the scout server session with the HttpSession but with a custom
   * cache associated with this ajax (remote) session id.
   * <p>
   * In addition, a "Ajax-UserId" header is added to impersonate the originally calling user (retrieved from the current
   * {@link Subject}s first principal).
   */
  public void setAjaxSessionId(String ajaxSessionId) {
    m_ajaxSessionId = ajaxSessionId;
    String userId = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().getName();
    m_ajaxUserId = userId;
  }

  /**
   * see {@link #setAjaxSessionId(String)}
   */
  public String getAjaxSessionId() {
    return m_ajaxSessionId;
  }

  @Override
  public void setAnalyzeNetworkLatency(boolean b) {
    super.setAnalyzeNetworkLatency(b);
  }

  @Override
  public void setClientNotificationPollInterval(long intervallMillis) {
    super.setClientNotificationPollInterval(intervallMillis);
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
  @Override
  protected URLConnection createURLConnection(ServiceTunnelRequest call, byte[] callData) throws IOException {
    return super.createURLConnection(call, callData);
  }

  /**
   * @param method
   *          GET or POST override this method to add custom HTTP headers
   */
  @Override
  protected void addCustomHeaders(URLConnection urlConn, String method) throws IOException {
    super.addCustomHeaders(urlConn, method);
    if (m_ajaxSessionId != null) {
      urlConn.setRequestProperty("Ajax-SessionId", m_ajaxSessionId);
      urlConn.setRequestProperty("Ajax-UserId", m_ajaxUserId);
    }
  }

  /**
   * @return msgEncoder used to encode and decode a request / response to and
   *         from the binary stream. Default is the {@link DefaultServiceTunnelContentHandler} which handles soap style
   *         messages
   */
  @Override
  public IServiceTunnelContentHandler getContentHandler() {
    return super.getContentHandler();
  }

  /**
   * @param msgEncoder
   *          that can encode and decode a request / response to and from the
   *          binary stream. Default is the {@link DefaultServiceTunnelContentHandler} which handles soap
   *          style messages
   */
  @Override
  public void setContentHandler(IServiceTunnelContentHandler e) {
    super.setContentHandler(e);
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) throws ProcessingException {
    return super.invokeService(serviceInterfaceClass, operation, callerArgs);
  }

  @Override
  protected ServiceTunnelResponse tunnelOnline(final ServiceTunnelRequest call) {
    try {
      //if (!call.getOperation().equals("ping")) TuningUtility.startTimer();
      return super.tunnelOnline(call);
    }
    finally {
      //if (!call.getOperation().equals("ping")) TuningUtility.stopTimer("tunnelOnline " + call);
    }
  }

  /**
   * Override this method to decide when background jobs to the backend should be presented to the user or not (for
   * cancelling)
   * The default makes all jobs cancellable except IPingService (used for client notification polling)
   */
  @Override
  protected void decorateBackgroundJob(ServiceTunnelRequest call, Job backgroundJob) {
    super.decorateBackgroundJob(call, backgroundJob);
  }

  /**
   * This method is called just after the http response is received but before
   * the http response is processed by scout. This might be used to read and
   * interpret custom http headers.
   * 
   * @since 06.07.2009
   */
  @Override
  protected void preprocessHttpRepsonse(URLConnection urlConn, ServiceTunnelRequest call, int httpCode) {
    super.preprocessHttpRepsonse(urlConn, call, httpCode);
  }

}
