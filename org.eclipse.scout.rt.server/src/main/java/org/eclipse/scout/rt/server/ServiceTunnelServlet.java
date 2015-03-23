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
package org.eclipse.scout.rt.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.commons.cache.IClientIdentificationService;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.server.commons.servletfilter.helper.HttpAuthJaasFilter;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Version;

/**
 * Use this Servlet to dispatch scout UI service requests using {@link IServiceTunnelRequest},
 * {@link IServiceTunnelResponse} and any {@link IServiceTunnelContentHandler} implementation.
 * <p/>
 * By default there is a JAAS convenience filter {@link HttpAuthJaasFilter} on /process and a {@link SoapWsseJaasFilter}
 * on /ajax with priority 1000.
 */
public class ServiceTunnelServlet extends HttpServletEx {
  public static final String HTTP_DEBUG_PARAM = "org.eclipse.scout.rt.server.http.debug";

  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceTunnelServlet.class);

  private transient IServiceTunnelContentHandler m_contentHandler;
  private Version m_requestMinVersion;
  private final boolean m_debug;

  public ServiceTunnelServlet() {
    this(ConfigIniUtility.getPropertyBoolean(HTTP_DEBUG_PARAM, false));
  }

  public ServiceTunnelServlet(boolean debug) {
    m_debug = debug;
  }

  // === HTTP-GET ===

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(req, res);

    try {
      // Create the job-input on behalf of which the server-job is run.
      ServerJobInput input = ServerJobInput.fillEmpty();
      input.name("AdminServiceCall");
      input.subject(subject);
      input.servletRequest(req);
      input.servletResponse(res);
      input.locale(Locale.getDefault());
      input.userAgent(UserAgent.createDefault());
      input.session(lookupServerSessionOnHttpSession(input.copy()));

      input = interceptServerJobInput(input);

      invokeAdminServiceInServerJob(input);
    }
    catch (ProcessingException e) {
      throw new ServletException("Failed to invoke AdminServlet", e);
    }
  }

  // === HTTP-POST ===

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(req, res);

    try {
      IServiceTunnelRequest serviceRequest = deserializeServiceRequest(req.getInputStream());

      // Create the job-input on behalf of which the server-job is run.
      ServerJobInput input = ServerJobInput.fillEmpty();
      input.name("RemoteServiceCall");
      input.id(String.valueOf(serviceRequest.getRequestSequence())); // to cancel server jobs and associated transactions.
      input.subject(subject);
      input.servletRequest(req);
      input.servletResponse(res);
      input.locale(serviceRequest.getLocale());
      input.userAgent(UserAgent.createByIdentifier(serviceRequest.getUserAgent()));
      input.session(lookupServerSessionOnHttpSession(input.copy()));

      input = interceptServerJobInput(input);

      IServiceTunnelResponse serviceResponse = invokeServiceInServerJob(input, serviceRequest);

      serializeServiceResponse(res, serviceResponse);
    }
    catch (Exception e) {
      if (isConnectionError(e)) {
        // NOOP: Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
      }
      else {
        LOG.error(String.format("Client=%s@%s/%s", req.getRemoteUser(), req.getRemoteAddr(), req.getRemoteHost()), e);
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  // === SERVICE INVOCATION ===

  /**
   * Method invoked to delegate the HTTP request to the 'admin service' on behalf of a server job.
   *
   * @param input
   *          input to be used to run the server job with current context information set.
   */
  protected void invokeAdminServiceInServerJob(final ServerJobInput input) throws ProcessingException {
    final HttpServletRequest request = input.getServletRequest();
    final HttpServletResponse response = input.getServletResponse();

    ServerJobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        String key = AdminSession.class.getName();

        AdminSession adminSession = (AdminSession) SERVICES.getService(IHttpSessionCacheService.class).getAndTouch(key, request, response);
        if (adminSession == null) {
          adminSession = new AdminSession();
          SERVICES.getService(IHttpSessionCacheService.class).put(key, adminSession, request, response);
        }
        adminSession.serviceRequest(request, response);
      }
    }, input);
  }

  /**
   * Method invoked to delegate the HTTP request to the 'process service' on behalf of a server job.
   *
   * @param input
   *          input to be used to run the server job with current context information set.
   * @param serviceTunnelRequest
   *          describes the service to be invoked.
   * @return {@link IServiceTunnelResponse} response sent back to the client.
   */
  protected IServiceTunnelResponse invokeServiceInServerJob(final ServerJobInput input, final IServiceTunnelRequest serviceTunnelRequest) throws ProcessingException {
    return ServerJobs.runNow(new ICallable<IServiceTunnelResponse>() {

      @Override
      public IServiceTunnelResponse call() throws Exception {
        return new DefaultTransactionDelegate(getRequestMinVersion(), isDebug()).invoke(serviceTunnelRequest);
      }
    }, input);
  }

  // === MESSAGE UNMARSHALLING / MARSHALLING ===

  /**
   * Method invoked to deserialize a service request to be given to the service handler.
   */
  protected IServiceTunnelRequest deserializeServiceRequest(InputStream in) throws Exception {
    return m_contentHandler.readRequest(in);
  }

  /**
   * Method invoked to serialize a service response to be sent back to the client.
   */
  protected void serializeServiceResponse(HttpServletResponse httpResponse, IServiceTunnelResponse res) throws Exception {
    // security: do not send back error stack trace
    if (res.getException() != null) {
      res.getException().setStackTrace(new StackTraceElement[0]);
    }

    httpResponse.setDateHeader("Expires", -1);
    httpResponse.setHeader("Cache-Control", "no-cache");
    httpResponse.setHeader("pragma", "no-cache");
    httpResponse.setContentType("text/xml");
    m_contentHandler.writeResponse(httpResponse.getOutputStream(), res);
  }

  // === INITIALIZATION ===

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    m_requestMinVersion = initRequestMinVersion(config);
  }

  /**
   * Method invoked by 'HTTP-GET' and 'HTTP-POST' to identify the session-class and to initialize the content handler
   * for serialization/deserialization.
   */
  @Internal
  protected void lazyInit(HttpServletRequest req, HttpServletResponse res) throws ServletException {
    m_contentHandler = createContentHandler();
  }

  /**
   * Reads the minimum version a request must have.
   * <p/>
   * The version has to be defined as init parameter in the servlet configuration. <br/>
   * This can be done by adding a new init-param at the {@link DefaultHttpProxyHandlerServlet} on the extension point
   * org.eclipse.equinox.http.registry.servlets and setting its name to min-version and its value to the desired version
   * (like 1.2.3). <br/>
   * If there is no min-version defined it uses the Bundle-Version of the bundle which contains the running product.
   */
  @Internal
  protected Version initRequestMinVersion(ServletConfig config) {
    Version version = null;
    String v = config.getInitParameter("min-version");
    if (v != null) {
      Version tmp = Version.parseVersion(v);
      version = new Version(tmp.getMajor(), tmp.getMinor(), tmp.getMicro());
    }
    else if (Platform.getProduct() != null) {
      v = (String) Platform.getProduct().getDefiningBundle().getHeaders().get("Bundle-Version");
      Version tmp = Version.parseVersion(v);
      version = new Version(tmp.getMajor(), tmp.getMinor(), tmp.getMicro());
    }

    return version;
  }

  /**
   * Create the (reusable) content handler (soap, xml, binary) for marshalling scout/osgi remote service calls
   * <p>
   * This method is part of the protected api and can be overridden.
   */
  protected IServiceTunnelContentHandler createContentHandler() {
    DefaultServiceTunnelContentHandler e = new DefaultServiceTunnelContentHandler();
    e.initialize();
    return e;
  }

  /**
   * Override this method to intercept the {@link ServerJobInput} used to run server jobs. The default implementation
   * simply returns the given input.
   */
  protected ServerJobInput interceptServerJobInput(final ServerJobInput input) {
    return input;
  }

  // === SESSION LOOKUP ===

  @Internal
  protected IServerSession lookupServerSessionOnHttpSession(ServerJobInput jobInput) throws ProcessingException, ServletException {
    HttpServletRequest req = jobInput.getServletRequest();
    HttpServletResponse res = jobInput.getServletResponse();

    //external request: apply locking, this is the session initialization phase
    IHttpSessionCacheService cacheService = SERVICES.getService(IHttpSessionCacheService.class);
    IServerSession serverSession = (IServerSession) cacheService.getAndTouch(IServerSession.class.getName(), req, res);
    if (serverSession == null) {
      synchronized (req.getSession()) {
        serverSession = (IServerSession) cacheService.get(IServerSession.class.getName(), req, res); // double checking
        if (serverSession == null) {
          serverSession = provideServerSession(jobInput);
          cacheService.put(IServerSession.class.getName(), serverSession, req, res);
        }
      }
    }
    return serverSession;
  }

  /**
   * Method invoked to provide a new {@link IServerSession} for the current HTTP-request.
   *
   * @param input
   *          context information about the ongoing HTTP-request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerJobInput input) throws ProcessingException {
    final IServerSession serverSession = OBJ.get(ServerSessionProvider.class).provide(input);
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(input.getServletRequest(), input.getServletResponse()));
    return serverSession;
  }

  // === Helper methods ===

  /**
   * @return minimal version a service request must have.
   */
  protected Version getRequestMinVersion() {
    return m_requestMinVersion;
  }

  /**
   * @return <code>true</code> if the {@link ServiceTunnelServlet} runs in debug mode; see property
   *         {@link ServiceTunnelServlet#HTTP_DEBUG_PARAM}.
   */
  protected boolean isDebug() {
    return m_debug;
  }

  @Internal
  protected boolean isConnectionError(Exception e) {
    Throwable cause = e;
    while (cause != null) {
      if (cause instanceof SocketException) {
        return true;
      }
      else if (cause.getClass().getSimpleName().equalsIgnoreCase("EofException")) {
        return true;
      }
      else if (cause instanceof InterruptedIOException) {
        return true;
      }
      // next
      cause = cause.getCause();
    }
    return false;
  }
}
