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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
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
      // Create the job-input on behalf of which the server-job is run.
      ServerJobInput input = ServerJobInput.empty();
      input.name("DiagnosticServiceCall");
      input.subject(subject);
      input.servletRequest(req);
      input.servletResponse(res);
      input.locale(Locale.getDefault());
      input.userAgent(UserAgent.createDefault());
      input.session(lookupScoutServerSessionOnHttpSession(input.copy()));

      invokeDiagnosticServiceInServerJob(input);

    }
    catch (ProcessingException e) {
      throw new ServletException("Failed to invoke DiagnosticServlet", e);
    }
  }

  /**
   * Method invoked to delegate the HTTP request to the 'diagnostic service' on behalf of a server job.
   *
   * @param input
   *          input to be used to run the server job with current context information set.
   */
  protected void invokeDiagnosticServiceInServerJob(final ServerJobInput input) throws ProcessingException {
    OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        final HttpServletRequest request = input.getServletRequest();
        final HttpServletResponse response = input.getServletResponse();
        final HttpSession httpSession = request.getSession();
        final String key = DiagnosticSession.class.getName();

        DiagnosticSession diagnosticSession = (DiagnosticSession) httpSession.getAttribute(key);
        if (diagnosticSession == null) {
          diagnosticSession = new DiagnosticSession();
          httpSession.setAttribute(key, diagnosticSession);
        }
        diagnosticSession.serviceRequest(request, response);
      }
    }, input);
  }
}
