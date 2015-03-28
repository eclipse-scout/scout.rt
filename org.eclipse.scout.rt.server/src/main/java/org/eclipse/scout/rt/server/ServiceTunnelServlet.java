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
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
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
      ServerRunContext runContext = ServerRunContexts.empty();
      runContext.subject(subject);
      runContext.servletRequest(req);
      runContext.servletResponse(res);
      runContext.locale(Locale.getDefault());
      runContext.userAgent(UserAgent.createDefault());
      runContext.session(lookupServerSessionOnHttpSession(runContext.copy()));

      runContext = interceptRunContext(runContext);

      invokeAdminService(runContext);
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

      ServerRunContext runContext = ServerRunContexts.empty();
      runContext.subject(subject);
      runContext.servletRequest(req);
      runContext.servletResponse(res);
      runContext.locale(serviceRequest.getLocale());
      runContext.userAgent(UserAgent.createByIdentifier(serviceRequest.getUserAgent()));
      runContext.session(lookupServerSessionOnHttpSession(runContext.copy()));

      runContext = interceptRunContext(runContext);

      IServiceTunnelResponse serviceResponse = invokeService(runContext, serviceRequest);

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
   * Method invoked to delegate the HTTP request to the 'admin service'.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing HTTP-request to be used to invoke the admin
   *          service.
   */
  protected void invokeAdminService(final ServerRunContext runContext) throws ProcessingException {
    final HttpServletRequest request = runContext.servletRequest();
    final HttpServletResponse response = runContext.servletResponse();

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
    }, ServerJobs.newInput(runContext).name("AdminServiceCall"));
  }

  /**
   * Method invoked to delegate the HTTP request to the 'process service'.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing HTTP-request to be used to invoke the service.
   * @param serviceTunnelRequest
   *          describes the service to be invoked.
   * @return {@link IServiceTunnelResponse} response sent back to the client.
   */
  protected IServiceTunnelResponse invokeService(final ServerRunContext runContext, final IServiceTunnelRequest serviceTunnelRequest) throws ProcessingException {
    return ServerJobs.runNow(new ICallable<IServiceTunnelResponse>() {

      @Override
      public IServiceTunnelResponse call() throws Exception {
        return new DefaultTransactionDelegate(getRequestMinVersion(), isDebug()).invoke(serviceTunnelRequest);
      }
    }, ServerJobs.newInput(runContext).name("RemoteServiceCall").id(String.valueOf(serviceTunnelRequest.getRequestSequence()))); // the job's id is set so that the client can cancel ongoing service calls.
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
   * Override this method to intercept the {@link ServerRunContext} used to process a request. The default
   * implementation simply returns the given <code>runContext</code>.
   */
  protected ServerRunContext interceptRunContext(final ServerRunContext runContext) {
    return runContext;
  }

  // === SESSION LOOKUP ===

  @Internal
  protected IServerSession lookupServerSessionOnHttpSession(final ServerRunContext runContext) throws ProcessingException, ServletException {
    HttpServletRequest req = runContext.servletRequest();
    HttpServletResponse res = runContext.servletResponse();

    //external request: apply locking, this is the session initialization phase
    IHttpSessionCacheService cacheService = SERVICES.getService(IHttpSessionCacheService.class);
    IServerSession serverSession = (IServerSession) cacheService.getAndTouch(IServerSession.class.getName(), req, res);
    if (serverSession == null) {
      synchronized (req.getSession()) {
        serverSession = (IServerSession) cacheService.get(IServerSession.class.getName(), req, res); // double checking
        if (serverSession == null) {
          serverSession = provideServerSession(runContext);
          cacheService.put(IServerSession.class.getName(), serverSession, req, res);
        }
      }
    }
    return serverSession;
  }

  /**
   * Method invoked to provide a new {@link IServerSession} for the current HTTP-request.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing HTTP-request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerRunContext runContext) throws ProcessingException {
    final IServerSession serverSession = OBJ.get(ServerSessionProvider.class).provide(runContext);
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(runContext.servletRequest(), runContext.servletResponse()));
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
