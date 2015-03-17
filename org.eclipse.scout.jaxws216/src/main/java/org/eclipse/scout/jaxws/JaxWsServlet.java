/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws;

import java.io.IOException;
import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Binding;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.internal.servlet.EndpointServlet;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;

/**
 * Runs the webservice request in a server-job to propagate the current request-context and to run on behalf of a
 * Subject. SOAP handlers may add Principals later.
 *
 * @see {@link IAuthenticationHandler}.
 */
public class JaxWsServlet extends EndpointServlet {

  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsServlet.class);

  @Override
  protected void handleRequest(final HttpServletRequest request, final HttpServletResponse response, final Class<? extends Binding> bindingTypeFilter) throws ServletException, IOException {
    try {
      // Create the job-input on behalf of which the server-job is run.
      final ServerJobInput input = ServerJobInput.empty();
      input.name("JAX-WS Request");
      input.subject(getOrCreateSubject());
      input.servletRequest(request);
      input.servletResponse(response);
      input.locale(Locale.getDefault());
      input.sessionRequired(false);
      input.transactional(false);

      OBJ.get(IServerJobManager.class).runNow(new IRunnable() {

        @Override
        public void run() throws Exception {
          JaxWsServlet.super.handleRequest(request, response, bindingTypeFilter);
        }
      }, input);
    }
    catch (final ProcessingException | RuntimeException e) {
      LOG.error(String.format("Webservice request failed: [requestor=%s@%s/%s]", request.getRemoteUser(), request.getRemoteAddr(), request.getRemoteHost()), e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Method invoked to get the Subject from the current calling context or if not set, a new one is created.
   */
  protected Subject getOrCreateSubject() {
    // The request can be authenticated in two ways:
    //  1. By the application server itself or a previous filter.
    //     In such situation, we are already running in a respective doAs call which implies that the Subject obtained is not null and contains one principal at minimum.
    //  2. By a subsequent {@link IAuthenticationHandler} handler.
    //     This is if the Subject is null or there is no principal associated yet. If the current Subject is null or readonly, a new one is created.

    final Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      return new Subject(); // Scout JAX-WS precondition that Subject is set.
    }
    if (subject.getPrincipals().isEmpty() && subject.isReadOnly()) {
      return new Subject(); // Scout JAX-WS precondition that unauthenticated Subject is not sealed yet.
    }
    return subject; // request is already authenticated.
  }
}
