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
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public class DiagnosticServlet extends ServiceTunnelServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    //invoke
    Map<Class, Object> backup = ThreadContext.backup();
    try {
      lazyInit(req, res);
      //
      //legacy, deprecated, do not use servlet request/response in scout code
      ThreadContext.putHttpServletRequest(req);
      ThreadContext.putHttpServletResponse(res);
      //
      UserAgent userAgent = UserAgent.createDefault();
      IServerSession serverSession = lookupScoutServerSessionOnHttpSession(req, res, subject, userAgent);
      //
      ServerJob job = new DiagnosticServiceJob(serverSession, subject, req, res);
      job.runNow(new NullProgressMonitor());
      job.throwOnError();
    }
    catch (ProcessingException e) {
      throw new ServletException(e);
    }
    finally {
      ThreadContext.restore(backup);
    }
  }

  private class DiagnosticServiceJob extends ServerJob {

    protected HttpServletRequest m_request;
    protected HttpServletResponse m_response;

    public DiagnosticServiceJob(IServerSession serverSession, Subject subject, HttpServletRequest request, HttpServletResponse response) {
      super("DiagnosticServiceCall", serverSession, subject);
      m_request = request;
      m_response = response;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      // get session
      HttpSession session = m_request.getSession();
      String key = DiagnosticSession.class.getName();
      DiagnosticSession ds = (DiagnosticSession) session.getAttribute(key);
      if (ds == null) {
        ds = new DiagnosticSession();
        session.setAttribute(key, ds);
      }
      ds.serviceRequest(m_request, m_response);
      return Status.OK_STATUS;
    }
  }
}
