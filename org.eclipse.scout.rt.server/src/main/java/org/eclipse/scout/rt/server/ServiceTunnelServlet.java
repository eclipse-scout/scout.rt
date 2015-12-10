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
import java.lang.reflect.Method;
import java.net.SocketException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.clientnotification.TransactionalClientNotificationCollector;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.context.ServletRunContexts;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceUtility;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this Servlet to dispatch scout UI service requests using {@link IServiceTunnelRequest},
 * {@link IServiceTunnelResponse} and any {@link IServiceTunnelContentHandler} implementation.
 */
public class ServiceTunnelServlet extends HttpServlet {

  public static final String SESSION_ID = "sessionId";

  private static final String ADMIN_SESSION_KEY = AdminSession.class.getName();

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelServlet.class);

  private transient IServiceTunnelContentHandler m_contentHandler;
  private boolean m_debug;

  // === HTTP-GET ===

  @Override
  protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
    if (Subject.getSubject(AccessController.getContext()) == null) {
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(servletRequest, servletResponse);

    try {
      ServletRunContexts.copyCurrent().withLocale(Locale.getDefault()).withServletRequest(servletRequest).withServletResponse(servletResponse).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          ServerRunContext serverRunContext = ServerRunContexts.copyCurrent()
              .withUserAgent(UserAgent.createDefault())
              .withProperty(SESSION_ID, UUID.randomUUID().toString());
          serverRunContext.withSession(lookupServerSessionOnHttpSession(serverRunContext.copy()));

          invokeAdminService(serverRunContext);
        }
      }, BEANS.get(ExceptionTranslator.class));
    }
    catch (Exception e) {
      throw new ServletException("Failed to invoke AdminServlet", e);
    }
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
      ServletRunContexts.copyCurrent().withServletRequest(servletRequest).withServletResponse(servletResponse).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          ServiceTunnelRequest serviceRequest = deserializeServiceRequest();
          if (isSessionLess(serviceRequest)) {
            // TODO [5.2] jgu: Use ServiceOperationInvoker; change ServiceOperationInvoker to support this requirement
            invokeServiceWithoutSession(serviceRequest);
          }
          else {
            // Collector to collect transactional client notifications issued during processing of the current request.
            TransactionalClientNotificationCollector transactionalClientNotificationCollector = new TransactionalClientNotificationCollector();
            // Enable global cancellation of the service request.
            RunMonitor runMonitor = BEANS.get(RunMonitor.class);
            ServerRunContext serverRunContext = ServerRunContexts.copyCurrent()
                .withLocale(serviceRequest.getLocale())
                .withUserAgent(UserAgent.createByIdentifier(serviceRequest.getUserAgent()))
                .withRunMonitor(runMonitor)
                .withTransactionalClientNotificationCollector(transactionalClientNotificationCollector)
                .withClientNodeId(serviceRequest.getClientNodeId())
                .withProperty(SESSION_ID, serviceRequest.getSessionId());
            serverRunContext.withSession(lookupServerSessionOnHttpSession(serverRunContext.copy()));

            IServerSession session = serverRunContext.getSession();
            long requestSequence = serviceRequest.getRequestSequence();

            BEANS.get(RunMonitorCancelRegistry.class).register(session, requestSequence, runMonitor); // enable global cancellation
            try {
              ServiceTunnelResponse serviceResponse = invokeService(serverRunContext, serviceRequest);
              // Include transactional client notification in response (piggyback).
              serviceResponse.setNotifications(transactionalClientNotificationCollector.consume());
              serializeServiceResponse(serviceResponse);
            }
            finally {
              BEANS.get(RunMonitorCancelRegistry.class).unregister(session, requestSequence);
            }
          }
        }

      }, BEANS.get(ExceptionTranslator.class));
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

  /**
   * Do not create a server session for IClientNotificationService
   */
  protected boolean isSessionLess(ServiceTunnelRequest serviceRequest) throws Exception {
    Class<?> serviceInterfaceClass = SerializationUtility.getClassLoader().loadClass(serviceRequest.getServiceInterfaceClassName());
    return IClientNotificationService.class.isAssignableFrom(serviceInterfaceClass);
  }

  // === SERVICE INVOCATION ===

  /**
   * Method invoked to delegate the HTTP request to the 'admin service'.
   */
  protected void invokeAdminService(final ServerRunContext serverRunContext) throws Exception {
    serverRunContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
        final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

        AdminSession adminSession = (AdminSession) BEANS.get(IHttpSessionCacheService.class).getAndTouch(ADMIN_SESSION_KEY, servletRequest, servletResponse);
        if (adminSession == null) {
          adminSession = new AdminSession();
          BEANS.get(IHttpSessionCacheService.class).put(ADMIN_SESSION_KEY, adminSession, servletRequest, servletResponse);
        }
        adminSession.serviceRequest(servletRequest, servletResponse);
      }
    }, BEANS.get(ExceptionTranslator.class));
  }

  /**
   * Method invoked to delegate the HTTP request to the 'process service'.
   */
  protected ServiceTunnelResponse invokeService(final ServerRunContext serverRunContext, final ServiceTunnelRequest serviceTunnelRequest) {
    return serverRunContext.call(new Callable<ServiceTunnelResponse>() {

      @Override
      public ServiceTunnelResponse call() throws Exception {
        return BEANS.get(ServiceOperationInvoker.class).invoke(serviceTunnelRequest);
      }
    });
  }

  /**
   * Process request without creating a server session
   */
  protected void invokeServiceWithoutSession(ServiceTunnelRequest serviceRequest) throws ClassNotFoundException, Exception {
    // TODO [5.2] jgu: Use ServiceOperationInvoker; change ServiceOperationInvoker to support this requirement
    ServiceUtility serviceUtility = BEANS.get(ServiceUtility.class);

    Class<?> serviceInterfaceClass = SerializationUtility.getClassLoader().loadClass(serviceRequest.getServiceInterfaceClassName());
    Method serviceOp = serviceUtility.getServiceOperation(serviceInterfaceClass, serviceRequest.getOperation(), serviceRequest.getParameterTypes());
    Object service = BEANS.get(serviceInterfaceClass);
    if (service == null) {
      throw new SecurityException("service registry does not contain a service of type " + serviceRequest.getServiceInterfaceClassName());
    }
    Object data = serviceUtility.invoke(service, serviceOp, serviceRequest.getArgs());
    Object[] outParameters = serviceUtility.extractHolderArguments(serviceRequest.getArgs());
    ServiceTunnelResponse serviceResponse = new ServiceTunnelResponse(data, outParameters);
    serviceResponse.setNotifications(new ArrayList<ClientNotificationMessage>());
    serializeServiceResponse(serviceResponse);
  }

  // === MESSAGE UNMARSHALLING / MARSHALLING ===

  /**
   * Method invoked to deserialize a service request to be given to the service handler.
   */
  protected ServiceTunnelRequest deserializeServiceRequest() throws Exception {
    return m_contentHandler.readRequest(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get().getInputStream());
  }

  /**
   * Method invoked to serialize a service response to be sent back to the client.
   */
  protected void serializeServiceResponse(ServiceTunnelResponse serviceResponse) throws Exception {
    // security: do not send back error stack trace
    if (serviceResponse.getException() != null) {
      serviceResponse.getException().setStackTrace(new StackTraceElement[0]);
    }

    HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();
    servletResponse.setDateHeader("Expires", -1);
    servletResponse.setHeader("Cache-Control", "no-cache");
    servletResponse.setHeader("pragma", "no-cache");
    servletResponse.setContentType(m_contentHandler.getContentType());
    m_contentHandler.writeResponse(servletResponse.getOutputStream(), serviceResponse);
  }

  // === INITIALIZATION ===

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    // read config
    m_debug = "true".equals(config.getInitParameter("debug"));
  }

  /**
   * Method invoked by 'HTTP-GET' and 'HTTP-POST' to identify the session-class and to initialize the content handler
   * for serialization/deserialization.
   */
  @Internal
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

  @Internal
  protected IServerSession lookupServerSessionOnHttpSession(final ServerRunContext serverRunContext) throws ServletException {
    final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

    //external request: apply locking, this is the session initialization phase
    IHttpSessionCacheService cacheService = BEANS.get(IHttpSessionCacheService.class);
    IServerSession serverSession = (IServerSession) cacheService.getAndTouch(IServerSession.class.getName(), servletRequest, servletResponse);
    if (serverSession == null) {
      synchronized (servletRequest.getSession()) {
        serverSession = (IServerSession) cacheService.get(IServerSession.class.getName(), servletRequest, servletResponse); // double checking
        if (serverSession == null) {
          final IServerSession newServerSession = provideServerSession(serverRunContext);

          servletRequest.getSession(true).setAttribute("scout.httpsession.binding.listener", new HttpSessionBindingListener() {
            @Override
            public void valueBound(HttpSessionBindingEvent event) {
              // NOOP
            }

            @Override
            public void valueUnbound(HttpSessionBindingEvent event) {
              newServerSession.stop();
            }
          });

          cacheService.put(IServerSession.class.getName(), newServerSession, servletRequest, servletResponse);
          return newServerSession;
        }
      }
    }
    return serverSession;
  }

  /**
   * Method invoked to provide a new {@link IServerSession} for the current HTTP-request.
   *
   * @param serverRunContext
   *          <code>ServerRunContext</code> with information about the ongoing service request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerRunContext serverRunContext) {
    String sessionId = serverRunContext.getProperty(SESSION_ID);
    return BEANS.get(ServerSessionProvider.class).provide(serverRunContext, sessionId);
  }

  // === Helper methods ===

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
