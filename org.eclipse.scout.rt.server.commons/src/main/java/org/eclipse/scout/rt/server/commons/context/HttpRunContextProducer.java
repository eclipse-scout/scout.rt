/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.context;

import java.security.AccessController;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.opentelemetry.HttpServletRequestTextMapGetter;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.logging.ServletDiagnosticsProviderFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;

/**
 * Creates a {@link RunContext} based on a {@link HttpServletRequest} and the current JAAS context.
 *
 * @since 9.0
 */
@ApplicationScoped
public class HttpRunContextProducer {

  private final ServletDiagnosticsProviderFactory m_servletDiagProviderFactory;
  private final CorrelationId m_correlationIdProvider;

  public HttpRunContextProducer() {
    m_servletDiagProviderFactory = createServletDiagnosticsProviderFactory();
    m_correlationIdProvider = createCorrelationId();
  }

  /**
   * Creates a new {@link RunContext} based on the {@link HttpServletRequest} specified.
   */
  public RunContext produce(HttpServletRequest req, HttpServletResponse resp) {
    return produce(req, resp, null);
  }

  /**
   * Fills the {@link RunContext} specified based on the values of the given {@link HttpServletRequest}.
   *
   * @param existing
   *          This is the context that should be extended with the attributes from the {@link HttpServletRequest}. May
   *          be {@code null}. In that case a new one is created.
   */
  public RunContext produce(HttpServletRequest req, HttpServletResponse resp, RunContext existing) {
    RunContext contextToFill = existing;
    if (contextToFill == null) {
      contextToFill = RunContexts.copyCurrent(true);
    }

    return contextToFill
        .withSubject(Subject.getSubject(AccessController.getContext()))
        .withCorrelationId(currentCorrelationId(req))
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, req)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, resp)
        .withDiagnostics(getServletDiagnosticsProviderFactory().getProviders(req, resp))
        .withLocale(req.getLocale())
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withOpenTelemetryContext(extractOpenTelemetryContext(req));
  }

  protected String currentCorrelationId(HttpServletRequest req) {
    String cid = req.getHeader(CorrelationId.HTTP_HEADER_NAME);
    if (StringUtility.hasText(cid)) {
      return cid;
    }
    return getCorrelationIdProvider().newCorrelationId();
  }

  /**
   * Extracts the OpenTelemetry {@link Context} out of the incoming request
   *
   * @param request
   *     incoming request
   * @return the extracted context
   */
  protected Context extractOpenTelemetryContext(HttpServletRequest request) {
    return GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator()
        .extract(Context.current(), request, BEANS.get(HttpServletRequestTextMapGetter.class));
  }

  protected ServletDiagnosticsProviderFactory createServletDiagnosticsProviderFactory() {
    return BEANS.get(ServletDiagnosticsProviderFactory.class);
  }

  protected CorrelationId createCorrelationId() {
    return BEANS.get(CorrelationId.class);
  }

  protected CorrelationId getCorrelationIdProvider() {
    return m_correlationIdProvider;
  }

  protected ServletDiagnosticsProviderFactory getServletDiagnosticsProviderFactory() {
    return m_servletDiagProviderFactory;
  }
}
