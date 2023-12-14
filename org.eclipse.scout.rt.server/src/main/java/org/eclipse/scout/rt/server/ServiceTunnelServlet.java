/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import java.io.IOException;
import java.security.AccessController;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

import javax.security.auth.Subject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.transaction.TransactionCancelledError;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.commons.idempotent.DuplicateRequestException;
import org.eclipse.scout.rt.server.commons.idempotent.SequenceNumberDuplicateDetector;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.context.HttpServerRunContextProducer;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry.IRegistrationHandle;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this Servlet to dispatch scout UI service requests using {@link ServiceTunnelRequest},
 * {@link ServiceTunnelResponse} and any {@link IServiceTunnelContentHandler} implementation.
 */
public class ServiceTunnelServlet extends AbstractHttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelServlet.class);

  protected static final String ADMIN_SESSION_KEY = "AdminSessionKey";
  protected static final String DUPLICATE_REQUEST_DETECTOR_SESSION_KEY = "DuplicateRequestDetector";

  protected transient IServiceTunnelContentHandler m_contentHandler;
  protected transient LazyValue<HttpServerRunContextProducer> m_serverRunContextProducer = new LazyValue<>(HttpServerRunContextProducer.class);
  protected transient LazyValue<HttpServletControl> m_httpServletControl = new LazyValue<>(HttpServletControl.class);
  protected transient LazyValue<HttpCacheControl> m_httpCacheControl = new LazyValue<>(HttpCacheControl.class);
  protected transient LazyValue<ServiceOperationInvoker> m_svcInvoker = new LazyValue<>(ServiceOperationInvoker.class);
  protected transient LazyValue<RunMonitorCancelRegistry> m_runMonCancelRegistry = new LazyValue<>(RunMonitorCancelRegistry.class);

  // === HTTP-GET ===

  @Override
  protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
    if (Subject.getSubject(AccessController.getContext()) == null) {
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(servletRequest, servletResponse);

    BEANS.get(HttpServerRunContextProducer.class)
        .withSessionSupport(false)
        .produce(servletRequest, servletResponse)
        .run(() -> invokeAdminService(ServerRunContexts.copyCurrent()), ServletExceptionTranslator.class);
  }

  /**
   * Method invoked to delegate the HTTP request to the 'admin service'.
   */
  @SuppressWarnings("squid:S00112")
  protected void invokeAdminService(final ServerRunContext serverRunContext) throws Exception {
    serverRunContext.run(() -> {
      final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
      final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

      m_httpServletControl.get().doDefaults(ServiceTunnelServlet.this, servletRequest, servletResponse);

      getAdminSession(servletRequest).serviceRequest(servletRequest, servletResponse);
    }, DefaultExceptionTranslator.class);
  }

  protected AdminSession getAdminSession(HttpServletRequest servletRequest) {
    HttpSession httpSession = servletRequest.getSession();
    AdminSession adminSession = (AdminSession) httpSession.getAttribute(ADMIN_SESSION_KEY);
    if (adminSession == null) {
      adminSession = new AdminSession();
      httpSession.setAttribute(ADMIN_SESSION_KEY, adminSession);
    }
    return adminSession;
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
      m_serverRunContextProducer.get()
          .getInnerRunContextProducer()
          .produce(servletRequest, servletResponse)
          .run(() -> {
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
          }, DefaultExceptionTranslator.class);
    }
    catch (DuplicateRequestException e) {
      final boolean interrupted = Thread.interrupted();
      if (interrupted) {
        LOG.debug("Duplicate Request{}", interruptInfo(interrupted), e);
      }
      else {
        LOG.warn("Duplicate Request{}", interruptInfo(interrupted), e);
      }
      servletResponse.sendError(HttpServletResponse.SC_CONFLICT, "Request is a duplicate");
    }
    catch (Throwable e) {//NOSONAR
      final boolean interrupted = Thread.interrupted();
      if (isConnectionError(e)) {
        // Ignore disconnect errors: do not throw an exception, if the client closed the connection.
        LOG.debug("Connection Error{}", interruptInfo(interrupted), e);
        // do not call sendError, as the connection is invalid anyway. May throw IllegalStateException otherwise hiding the original exception.
      }
      else if (isInterruption(e)) {
        if (isCancellation(e)) {
          // cancelled by client
          LOG.debug("Cancelled by client{}", interruptInfo(interrupted), e);
          servletResponse.sendError(HttpServletResponse.SC_ACCEPTED, "Request processing was cancelled");
        }
        else {
          // other interruption
          LOG.info("Interruption{}", interruptInfo(interrupted), e);
          servletResponse.sendError(HttpServletResponse.SC_ACCEPTED, "Request processing was interrupted");
        }
      }
      else {
        LOG.error("Client={}@{}/{}", servletRequest.getRemoteUser(), servletRequest.getRemoteAddr(), servletRequest.getRemoteHost(), e);
        servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  protected ServiceTunnelResponse doPost(ServiceTunnelRequest serviceRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("requestSequence {} {}.{}", serviceRequest.getRequestSequence(), serviceRequest.getServiceInterfaceClassName(), serviceRequest.getOperation());
    }

    final ServerRunContext serverRunContext = createServiceTunnelRunContext(serviceRequest);
    final IRegistrationHandle registrationHandle = registerForCancellation(serverRunContext, serviceRequest);
    try {
      ServiceTunnelResponse serviceResponse = invokeService(serverRunContext, serviceRequest);
      // include client notifications in response (piggyback)
      serviceResponse.setNotifications(serverRunContext.getClientNotificationCollector().consume());
      return serviceResponse;
    }
    finally {
      registrationHandle.unregister();
    }
  }

  protected String interruptInfo(boolean interrupted) {
    return interrupted ? ", thread was interrupted" : ", thread was not interrupted";
  }

  protected ServerRunContext createServiceTunnelRunContext(ServiceTunnelRequest serviceRequest) {
    // overwrite default settings from HTTP request with values from ServiceTunnelRequest
    final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent()
        .withLocale(serviceRequest.getLocale())
        .withUserAgent(UserAgents.createByIdentifier(serviceRequest.getUserAgent()))
        .withClientNodeId(serviceRequest.getClientNodeId());

    if (serviceRequest.getSessionId() != null) {
      final HttpServletRequest req = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
      final IServerSession session = m_serverRunContextProducer.get().getOrCreateScoutSession(req, serverRunContext, serviceRequest.getSessionId());
      serverRunContext.withSession(session);

      // duplicate detection
      LongPredicate duplicateRequestDetector = (LongPredicate) session
          .computeDataIfAbsent(DUPLICATE_REQUEST_DETECTOR_SESSION_KEY, this::createRequestSequenceValidator);
      if (!duplicateRequestDetector.test(serviceRequest.getRequestSequence())) {
        String msg = "clientNodeId: " + serviceRequest.getClientNodeId() + ", "
            + "sessionId: " + serviceRequest.getSessionId() + ", "
            + "operation: " + serviceRequest.getServiceInterfaceClassName() + "." + serviceRequest.getOperation();
        throw DuplicateRequestException.create(msg, serviceRequest.getRequestSequence());
      }
    }
    return serverRunContext;
  }

  /**
   * @return a function that returns true for valid request numbers and false for duplicate request number
   */
  protected LongPredicate createRequestSequenceValidator() {
    return new SequenceNumberDuplicateDetector(100, 1, TimeUnit.MINUTES, true);
  }

  protected IRegistrationHandle registerForCancellation(ServerRunContext runContext, ServiceTunnelRequest req) {
    String sessionId = runContext.getSession() != null ? runContext.getSession().getId() : null;
    return m_runMonCancelRegistry.get().register(runContext.getRunMonitor(), sessionId, req.getRequestSequence());
  }

  // === SERVICE INVOCATION ===

  /**
   * Method invoked to delegate the HTTP request to the 'process service'.
   */
  protected ServiceTunnelResponse invokeService(final ServerRunContext serverRunContext, final ServiceTunnelRequest serviceTunnelRequest) {
    return m_svcInvoker.get().invoke(serverRunContext, serviceTunnelRequest);
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

    m_httpServletControl.get().doDefaults(this, req, resp);

    m_httpCacheControl.get().checkAndSetCacheHeaders(req, resp, null);
    resp.setContentType(m_contentHandler.getContentType());
    m_contentHandler.writeResponse(resp.getOutputStream(), serviceResponse);
  }

  // === INITIALIZATION ===

  /**
   * Method invoked by 'HTTP-GET' and 'HTTP-POST' to identify the session-class and to initialize the content handler
   * for serialization/deserialization.
   */
  protected void lazyInit(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    if (m_contentHandler != null) {
      return;
    }
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

  protected boolean isConnectionError(Throwable e) {
    return BEANS.get(ConnectionErrorDetector.class).isConnectionError(e);
  }

  protected boolean isInterruption(Throwable e) {
    return BEANS.get(DefaultExceptionTranslator.class).throwableCausesAccept(e, t -> t instanceof AbstractInterruptionError);
  }

  /**
   * Special case of {@link AbstractInterruptionError}.
   */
  protected boolean isCancellation(Throwable e) {
    return BEANS.get(DefaultExceptionTranslator.class).throwableCausesAccept(e,
        t -> t instanceof FutureCancelledError
            || t instanceof TransactionCancelledError
            || t instanceof ThreadInterruptedError);
  }
}
