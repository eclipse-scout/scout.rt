/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import static org.eclipse.scout.rt.server.commons.opentelemetry.SpanNamePropagationFromDownstream.updateSpanName;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.opentelemetry.HttpServletRequestTextMapGetter;
import org.eclipse.scout.rt.server.commons.opentelemetry.OpenTelemetryFilterInstrumenterFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryFilter implements Filter {
  private Instrumenter<HttpServletRequest, HttpServletResponse> m_instrumenter;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_instrumenter = BEANS.get(OpenTelemetryFilterInstrumenterFactory.class).createInstrumenter();
  }

  @Override
  public void doFilter(ServletRequest request0, ServletResponse response0, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) request0;
    HttpServletResponse response = (HttpServletResponse) response0;

    Context parentContext = GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator()
        .extract(Context.current(), request, BEANS.get(HttpServletRequestTextMapGetter.class));

    if (!m_instrumenter.shouldStart(parentContext, request)) {
      chain.doFilter(request, response);
      return;
    }

    Context context = m_instrumenter.start(parentContext, request);

    try (Scope ignored = context.makeCurrent()) {
      chain.doFilter(request, response);
      updateSpanName(request.getMethod());
    }
    catch (Throwable t) {
      m_instrumenter.end(context, request, response, t);
      throw t;
    }
    m_instrumenter.end(context, request, response, null);
  }
}
