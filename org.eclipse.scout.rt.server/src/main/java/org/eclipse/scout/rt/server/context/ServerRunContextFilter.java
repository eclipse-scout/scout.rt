/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.context;

import java.io.IOException;
import java.security.AccessController;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.logging.ServletDiagnosticsProviderFactory;

/**
 * Filter which creates a {@link ServerRunContext} using the current {@link Subject} and calls the next filter inside
 * it. This ensures a proper {@link RunContext} for the subsequent filters and servlet.
 *
 * @since 6.1
 */
public class ServerRunContextFilter implements Filter {
  private RunContextProducer m_runContextProducer;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_runContextProducer = getRunContextProducer();
  }

  protected RunContextProducer getRunContextProducer() {
    return BEANS.get(ServerRunContextProducer.class);
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;
    lookupRunContext(req, resp).run(new IRunnable() {
      @Override
      public void run() throws IOException, ServletException {
        chain.doFilter(request, response);
      }
    });
  }

  @Override
  public void destroy() {
    // NOP
  }

  public RunContext lookupRunContext(HttpServletRequest req, HttpServletResponse resp) {
    final String cid = req.getHeader(CorrelationId.HTTP_HEADER_NAME);

    return m_runContextProducer
        .produce(Subject.getSubject(AccessController.getContext()))
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, req)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, resp)
//        .withDiagnostics(BEANS.all(IServletRunContextDiagnostics.class))
        .withDiagnostics(BEANS.get(ServletDiagnosticsProviderFactory.class).getProviders(req, resp))
        .withLocale(req.getLocale())
        .withCorrelationId(cid != null ? cid : BEANS.get(CorrelationId.class).newCorrelationId());
  }

}
