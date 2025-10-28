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

import javax.security.auth.Subject;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ForbiddenException;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.eclipse.scout.rt.rest.id.IdSignatureClientRequestFilter;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.context.HttpServerRunContextProducer;
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
        }, ServiceTunnelExceptionTranslator.class);
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
    ServiceTunnelResponse serviceResponse = invokeService(serverRunContext, serviceRequest);
    // include client notifications in response (piggyback)
    serviceResponse.setNotifications(serverRunContext.getClientNotificationCollector().consume());
    return serviceResponse;
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

  public static class ServiceTunnelExceptionTranslator implements IExceptionTranslator<RuntimeException> {

    private DefaultExceptionTranslator m_defaultExceptionTranslator = BEANS.get(DefaultExceptionTranslator.class);

    @Override
    public RuntimeException translate(Throwable throwable) {
      Exception translated = getDefaultExceptionTranslator().translate(throwable);
      if (translated instanceof RuntimeException) {
        return (RuntimeException) translated;
      }
      return new ProcessingException("Error while processing request", throwable);
    }

    protected DefaultExceptionTranslator getDefaultExceptionTranslator() {
      return m_defaultExceptionTranslator;
    }
  }
}
