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
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public class DiagnosticServlet extends ServiceTunnelServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    lazyInit(req, res);

    try {
      ServerRunContext runContext = ServerRunContexts.empty();
      runContext.subject(subject);
      runContext.servletRequest(req);
      runContext.servletResponse(res);
      runContext.locale(Locale.getDefault());
      runContext.userAgent(UserAgent.createDefault());
      runContext.session(lookupServerSessionOnHttpSession(runContext.copy()));

      runContext = interceptRunContext(runContext);

      invokeDiagnosticService(runContext);

    }
    catch (ProcessingException e) {
      throw new ServletException("Failed to invoke DiagnosticServlet", e);
    }
  }

  /**
   * Method invoked to delegate the HTTP request to the 'diagnostic service'.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing HTTP-request to be used to invoke the
   *          diagnostic service.
   */
  protected void invokeDiagnosticService(final ServerRunContext runContext) throws ProcessingException {
    ServerJobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        final HttpServletRequest request = runContext.servletRequest();
        final HttpServletResponse response = runContext.servletResponse();
        final HttpSession httpSession = request.getSession();
        final String key = DiagnosticSession.class.getName();

        DiagnosticSession diagnosticSession = (DiagnosticSession) httpSession.getAttribute(key);
        if (diagnosticSession == null) {
          diagnosticSession = new DiagnosticSession();
          httpSession.setAttribute(key, diagnosticSession);
        }
        diagnosticSession.serviceRequest(request, response);
      }
    }, ServerJobs.newInput(runContext).name("DiagnosticServiceCall"));
  }
}
