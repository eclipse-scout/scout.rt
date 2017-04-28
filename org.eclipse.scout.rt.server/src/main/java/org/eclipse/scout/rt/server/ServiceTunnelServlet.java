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
package org.eclipse.scout.rt.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationCollector;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.logging.ServletDiagnosticsProviderFactory;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry.IRegistrationHandle;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.IServerSessionLifecycleHandler;
import org.eclipse.scout.rt.server.session.ServerSessionCache;
import org.eclipse.scout.rt.server.session.ServerSessionLifecycleHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.session.Sessions;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this Servlet to dispatch scout UI service requests using {@link IServiceTunnelRequest},
 * {@link IServiceTunnelResponse} and any {@link IServiceTunnelContentHandler} implementation.
 */
public class ServiceTunnelServlet extends AbstractHttpServlet {

  private static final String ADMIN_SESSION_KEY = AdminSession.class.getName();

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelServlet.class);

  private transient IServiceTunnelContentHandler m_contentHandler;

  protected RunContext createServletRunContext(final HttpServletRequest req, final HttpServletResponse resp) {
    final String cid = req.getHeader(CorrelationId.HTTP_HEADER_NAME);

    return RunContexts.copyCurrent(true)
        .withSubject(Subject.getSubject(AccessController.getContext()))
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, req)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, resp)
        .withDiagnostics(BEANS.get(ServletDiagnosticsProviderFactory.class).getProviders(req, resp))
//        .withDiagnostics(BEANS.all(IServletRunContextDiagnostics.class))
        .withLocale(Locale.getDefault())
        .withCorrelationId(cid != null ? cid : BEANS.get(CorrelationId.class).newCorrelationId());
  }

  // === HTTP-GET ===

  @Override
  protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
    if (Subject.getSubject(AccessController.getContext()) == null) {
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(servletRequest, servletResponse);

    createServletRunContext(servletRequest, servletResponse).run(new IRunnable() {
      @Override
      public void run() throws Exception {
        ServerRunContext serverRunContext = ServerRunContexts.copyCurrent();
        serverRunContext.withUserAgent(UserAgents.createDefault());
        serverRunContext.withSession(lookupServerSessionOnHttpSession(Sessions.randomSessionId(), serverRunContext));
        invokeAdminService(serverRunContext);
      }
    }, ServletExceptionTranslator.class);
  }

  // === HTTP-POST ===

  @Override
  protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
    if (Subject.getSubject(AccessController.getContext()) == null) {
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(servletRequest, servletResponse);

    try {
      createServletRunContext(servletRequest, servletResponse).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          ServiceTunnelRequest serviceRequest = deserializeServiceRequest();
          ServiceTunnelResponse serviceResponse = doPost(serviceRequest);

          // Clear the current thread's interruption status before writing the response to the output stream.
          // Otherwise, the stream gets silently corrupted, which triggers  a repetition of the current request by Java connection mechanism.
          IRestorer interruption = ThreadInterruption.clear();
          try {
            serializeServiceResponse(serviceResponse);
          }
          finally {
            interruption.restore();
          }
        }
      }, DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      if (isConnectionError(e)) {
        // Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
        LOG.debug("Connection Error: ", e);
      }
      else {
        LOG.error("Client={}@{}/{}", servletRequest.getRemoteUser(), servletRequest.getRemoteAddr(), servletRequest.getRemoteHost(), e);
        servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  protected ServiceTunnelResponse doPost(ServiceTunnelRequest serviceRequest) throws ServletException {
    ClientNotificationCollector collector = new ClientNotificationCollector();
    ServerRunContext serverRunContext = ServerRunContexts.copyCurrent()
        .withLocale(serviceRequest.getLocale())
        .withUserAgent(UserAgents.createByIdentifier(serviceRequest.getUserAgent()))
        .withClientNotificationCollector(collector)
        .withClientNodeId(serviceRequest.getClientNodeId());

    if (serviceRequest.getSessionId() != null) {
      serverRunContext.withSession(lookupServerSessionOnHttpSession(serviceRequest.getSessionId(), serverRunContext));
    }

    final IRegistrationHandle registrationHandle = registerForCancellation(serverRunContext, serviceRequest);
    try {
      ServiceTunnelResponse serviceResponse = invokeService(serverRunContext, serviceRequest);
      // include client notifications in response (piggyback)
      serviceResponse.setNotifications(collector.consume());
      return serviceResponse;
    }
    finally {
      registrationHandle.unregister();
    }
  }

  protected IRegistrationHandle registerForCancellation(ServerRunContext runContext, ServiceTunnelRequest req) {
    String sessionId = runContext.getSession() != null ? runContext.getSession().getId() : null;
    return BEANS.get(RunMonitorCancelRegistry.class).register(runContext.getRunMonitor(), sessionId, req.getRequestSequence());
  }

  // === SERVICE INVOCATION ===

  /**
   * Method invoked to delegate the HTTP request to the 'admin service'.
   */
  @SuppressWarnings("squid:S00112")
  protected void invokeAdminService(final ServerRunContext serverRunContext) throws Exception {
    serverRunContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
        final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

        BEANS.get(HttpServletControl.class).doDefaults(ServiceTunnelServlet.this, servletRequest, servletResponse);

        AdminSession adminSession = (AdminSession) BEANS.get(IHttpSessionCacheService.class).getAndTouch(ADMIN_SESSION_KEY, servletRequest, servletResponse);
        if (adminSession == null) {
          adminSession = new AdminSession();
          BEANS.get(IHttpSessionCacheService.class).put(ADMIN_SESSION_KEY, adminSession, servletRequest, servletResponse);
        }
        adminSession.serviceRequest(servletRequest, servletResponse);
      }
    }, DefaultExceptionTranslator.class);
  }

  /**
   * Method invoked to delegate the HTTP request to the 'process service'.
   */
  protected ServiceTunnelResponse invokeService(final ServerRunContext serverRunContext, final ServiceTunnelRequest serviceTunnelRequest) {
    return BEANS.get(ServiceOperationInvoker.class).invoke(serverRunContext, serviceTunnelRequest);
  }

  // === MESSAGE UNMARSHALLING / MARSHALLING ===

  /**
   * Method invoked to deserialize a service request to be given to the service handler.
   */
  protected ServiceTunnelRequest deserializeServiceRequest() throws IOException, ClassNotFoundException {
    return m_contentHandler.readRequest(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get().getInputStream());
  }

  /**
   * Method invoked to serialize a service response to be sent back to the client.
   */
  protected void serializeServiceResponse(ServiceTunnelResponse serviceResponse) throws IOException {
    HttpServletRequest req = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    HttpServletResponse resp = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

    BEANS.get(HttpServletControl.class).doDefaults(this, req, resp);

    BEANS.get(HttpCacheControl.class).checkAndSetCacheHeaders(req, resp, null, null);
    resp.setContentType(m_contentHandler.getContentType());
    m_contentHandler.writeResponse(resp.getOutputStream(), serviceResponse);
  }

  // === INITIALIZATION ===

  /**
   * Method invoked by 'HTTP-GET' and 'HTTP-POST' to identify the session-class and to initialize the content handler
   * for serialization/deserialization.
   */
  protected void lazyInit(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException {
    m_contentHandler = createContentHandler();
  }

  /**
   * Create the (reusable) content handler (soap, xml, binary) for marshalling scout remote service calls
   * <p>
   * This method is part of the protected api and can be overridden.
   */
  protected IServiceTunnelContentHandler createContentHandler() {
    IServiceTunnelContentHandler e = BEANS.get(IServiceTunnelContentHandler.class);
    e.initialize();
    return e;
  }

  // === SESSION LOOKUP ===

  protected IServerSession lookupServerSessionOnHttpSession(final String sessionId, final ServerRunContext serverRunContext) throws ServletException {
    //create, only, if no serverSession available for sessionId
    Assertions.assertNotNull(sessionId, "sessionId must not be null");
    Assertions.assertNotNull(serverRunContext, "serverRunContext must not be null");
    final HttpServletRequest req = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    final String clientNodeId = serverRunContext.getClientNodeId();

    //create and register new session
    IServerSessionLifecycleHandler lifecycleHandler = new ServerSessionLifecycleHandler(sessionId, clientNodeId, serverRunContext);

    return BEANS.get(ServerSessionCache.class).getOrCreate(lifecycleHandler, req.getSession());
  }

  // === Helper methods ===

  protected boolean isConnectionError(Exception e) {
    Throwable cause = e;
    while (cause != null) {
      if (cause instanceof SocketException) {
        return true;
      }
      else if ("EofException".equalsIgnoreCase(cause.getClass().getSimpleName())) {
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
