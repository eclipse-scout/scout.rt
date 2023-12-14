/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.diagnostic;

import java.io.IOException;
import java.security.AccessController;

import javax.security.auth.Subject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.eclipse.scout.rt.server.context.HttpServerRunContextProducer;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;

public class DiagnosticServlet extends ServiceTunnelServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }

  @Override
  protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
    if (Subject.getSubject(AccessController.getContext()) == null) {
      servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(servletRequest, servletResponse);

    BEANS.get(HttpServerRunContextProducer.class)
        .withSessionSupport(false)
        .produce(servletRequest, servletResponse)
        .run(() -> invokeDiagnosticService(ServerRunContexts.copyCurrent()), ServletExceptionTranslator.class);
  }

  /**
   * Method invoked to delegate the HTTP request to the 'diagnostic service'.
   */
  @SuppressWarnings("squid:S00112")
  protected void invokeDiagnosticService(final ServerRunContext serverRunContext) throws Exception {
    serverRunContext.run(() -> {
      final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
      final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

      new DiagnosticSession().serviceRequest(servletRequest, servletResponse);
    }, DefaultExceptionTranslator.class);
  }
}
