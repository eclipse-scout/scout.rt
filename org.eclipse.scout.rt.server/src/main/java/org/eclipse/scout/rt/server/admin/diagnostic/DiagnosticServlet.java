/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.service.SERVICES;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.context.ServletRunContexts;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public class DiagnosticServlet extends ServiceTunnelServlet {
  private static final long serialVersionUID = 1L;
  private static final String DIAGNOSTIC_SESSION_KEY = AdminSession.class.getName();

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

    try {
      ServletRunContexts.copyCurrent().locale(Locale.getDefault()).servletRequest(servletRequest).servletResponse(servletResponse).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          ServerRunContext serverRunContext = ServerRunContexts.copyCurrent();
          serverRunContext.userAgent(UserAgent.createDefault());
          serverRunContext.session(lookupServerSessionOnHttpSession(serverRunContext.copy()));

          invokeDiagnosticService(serverRunContext);
        }
      });
    }
    catch (ProcessingException e) {
      throw new ServletException("Failed to invoke DiagnosticServlet", e);
    }
  }

  /**
   * Method invoked to delegate the HTTP request to the 'diagnostic service'.
   */
  protected void invokeDiagnosticService(final ServerRunContext serverRunContext) throws ProcessingException {
    serverRunContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
        final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

        DiagnosticSession diagnosticSession = (DiagnosticSession) SERVICES.getService(IHttpSessionCacheService.class).getAndTouch(DIAGNOSTIC_SESSION_KEY, servletRequest, servletResponse);
        if (diagnosticSession == null) {
          diagnosticSession = new DiagnosticSession();
          SERVICES.getService(IHttpSessionCacheService.class).put(DIAGNOSTIC_SESSION_KEY, diagnosticSession, servletRequest, servletResponse);
        }
        diagnosticSession.serviceRequest(servletRequest, servletResponse);
      }
    });
  }
}
