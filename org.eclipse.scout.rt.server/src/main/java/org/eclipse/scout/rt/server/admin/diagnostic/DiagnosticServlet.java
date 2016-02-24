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
package org.eclipse.scout.rt.server.admin.diagnostic;

import java.io.IOException;
import java.security.AccessController;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.ServletExceptionTranslator;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public class DiagnosticServlet extends ServiceTunnelServlet {
  private static final long serialVersionUID = 1L;
  private static final String DIAGNOSTIC_SESSION_KEY = DiagnosticSession.class.getName();

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

    createServletRunContext(servletRequest, servletResponse).run(new IRunnable() {
      @Override
      public void run() throws Exception {
        ServerRunContext serverRunContext = ServerRunContexts.copyCurrent();
        serverRunContext.withUserAgent(UserAgent.createDefault());
        serverRunContext.withSession(lookupServerSessionOnHttpSession(null, serverRunContext.copy()));

        invokeDiagnosticService(serverRunContext);
      }
    }, ServletExceptionTranslator.class);
  }

  /**
   * Method invoked to delegate the HTTP request to the 'diagnostic service'.
   */
  protected void invokeDiagnosticService(final ServerRunContext serverRunContext) throws Exception {
    serverRunContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
        final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

        DiagnosticSession diagnosticSession = (DiagnosticSession) BEANS.get(IHttpSessionCacheService.class).getAndTouch(DIAGNOSTIC_SESSION_KEY, servletRequest, servletResponse);
        if (diagnosticSession == null) {
          diagnosticSession = new DiagnosticSession();
          BEANS.get(IHttpSessionCacheService.class).put(DIAGNOSTIC_SESSION_KEY, diagnosticSession, servletRequest, servletResponse);
        }
        diagnosticSession.serviceRequest(servletRequest, servletResponse);
      }
    }, DefaultExceptionTranslator.class);
  }
}
