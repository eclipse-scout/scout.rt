/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.servicetunnel;

import static org.eclipse.scout.rt.server.commons.opentelemetry.SpanNamePropagationFromDownstream.addNameToContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

import javax.security.auth.Subject;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.ApplicationScoped;
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
import org.eclipse.scout.rt.rest.id.IdSignatureClientRequestFilter;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.context.HttpServerRunContextProducer;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry.IRegistrationHandle;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this service to dispatch Scout UI requests using {@link ServiceTunnelRequest}, {@link ServiceTunnelResponse} and the {@link BinaryServiceTunnelContentHandler} implementation.
 */
@ApplicationScoped
public class ServiceTunnelService {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelService.class);

  protected static final String DUPLICATE_REQUEST_DETECTOR_SESSION_KEY = "DuplicateRequestDetector";

  protected transient BinaryServiceTunnelContentHandler m_contentHandler;
  protected transient LazyValue<HttpServerRunContextProducer> m_serverRunContextProducer = new LazyValue<>(HttpServerRunContextProducer.class);
  protected transient LazyValue<HttpCacheControl> m_httpCacheControl = new LazyValue<>(HttpCacheControl.class);
  protected transient LazyValue<ServiceOperationInvoker> m_svcInvoker = new LazyValue<>(ServiceOperationInvoker.class);
  protected transient LazyValue<RunMonitorCancelRegistry> m_runMonCancelRegistry = new LazyValue<>(RunMonitorCancelRegistry.class);

  @PostConstruct
  public void init() {
    m_contentHandler = createContentHandler();
  }

  /**
   * Check the {@link HttpServletRequest} if signature creation needs to be enabled. Default implementation checks
   * the {@link IdSignatureClientRequestFilter#ID_SIGNATURE_HTTP_HEADER}.
   */
  protected boolean enableSignature(HttpServletRequest servletRequest) {
    return Boolean.TRUE.toString().equalsIgnoreCase(servletRequest.getHeader(IdSignatureClientRequestFilter.ID_SIGNATURE_HTTP_HEADER));
  }

  // incoming request
  public void incomingRequest(InputStream in, OutputStream out) {
    if (Subject.current() == null) {
      throw new ForbiddenException();
    }

    try {
      m_serverRunContextProducer.get()
          .getInnerRunContextProducer()
          .produce(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get(), IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get())
          .withProperties(enableSignature(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get()) ? Map.of(ServiceTunnelOptions.ID_SIGNATURE_PROP, true) : Map.of())
          .run(() -> {
            ServiceTunnelRequest serviceRequest = deserializeServiceRequest(in);
            ServiceTunnelResponse serviceResponse = evaluate(serviceRequest);

            // Clear the current thread's interruption status before writing the response to the output stream.
            // Otherwise, the stream gets silently corrupted, which triggers  a repetition of the current request by Java connection mechanism.
            IRestorer interruption = ThreadInterruption.clear();
            try {
              serializeServiceResponse(out, serviceResponse);
            }
            finally {
              interruption.restore();
            }
          }, DefaultExceptionTranslator.class);
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
          throw new WebApplicationException("Request processing was cancelled", Response.Status.ACCEPTED);
        }
        else {
          // other interruption
          LOG.info("Interruption{}", interruptInfo(interrupted), e);
          throw new WebApplicationException("Request processing was interrupted", Response.Status.ACCEPTED);
        }
      }
      else {
        Optional<HttpServletRequest> optRequest = Optional.ofNullable(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
        LOG.error("Client={}@{}/{}", optRequest.map(HttpServletRequest::getRemoteUser), optRequest.map(HttpServletRequest::getRemoteAddr), optRequest.map(HttpServletRequest::getRemoteHost), e);
        throw new WebApplicationException("Error while processing request", Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
  }

  protected ServiceTunnelResponse evaluate(ServiceTunnelRequest serviceRequest) {
    addNameToContext(() -> buildSpanName(serviceRequest));
    return evaluateInternal(serviceRequest);
  }

  protected String buildSpanName(ServiceTunnelRequest serviceRequest) {
    String fullName = serviceRequest.getServiceInterfaceClassName();
    String serviceName = fullName.substring(fullName.lastIndexOf('.') + 1);
    return serviceName + "." + serviceRequest.getOperation();
  }

  protected ServiceTunnelResponse evaluateInternal(ServiceTunnelRequest serviceRequest) {
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
    }
    return serverRunContext;
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
  protected ServiceTunnelRequest deserializeServiceRequest(InputStream in) throws IOException, ClassNotFoundException {
    return m_contentHandler.readRequest(in);
  }

  /**
   * Method invoked to serialize a service response to be sent back to the client.
   */
  protected void serializeServiceResponse(OutputStream out, ServiceTunnelResponse serviceResponse) throws IOException {
    HttpServletRequest req = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    HttpServletResponse resp = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

    m_httpCacheControl.get().checkAndSetCacheHeaders(req, resp, null);

    m_contentHandler.writeResponse(out, serviceResponse);
  }

  // === INITIALIZATION ===

  /**
   * Create the (reusable) {@link BinaryServiceTunnelContentHandler} for marshalling scout remote service calls
   * <p>
   * This method is part of the protected api and can be overridden.
   */
  protected BinaryServiceTunnelContentHandler createContentHandler() {
    return BEANS.get(BinaryServiceTunnelContentHandler.class);
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
