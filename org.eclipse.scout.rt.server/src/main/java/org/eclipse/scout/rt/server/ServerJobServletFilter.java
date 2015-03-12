/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

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

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.commons.cache.IClientIdentificationService;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Filter to run the ongoing HTTP-request in a server job. Requests targeted to '/process' are not run in a server job
 * because done in {@link ServiceTunnelServlet}.
 */
public class ServerJobServletFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerJobServletFilter.class);

  private FilterConfigInjection m_injection;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    m_injection = new FilterConfigInjection(config0, getClass());
  }

  @Override
  public void destroy() {
    m_injection = null;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(request);
    if (!config.isActive()) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if ("/process".equals(httpRequest.getServletPath())) {
      chain.doFilter(request, response);
      return;
    }

    try {
      // Create the job input on behalf of which the server job is run.
      ServerJobInput input = ServerJobInput.empty();
      input.name(ServerJobServletFilter.class.getSimpleName());
      input.subject(Subject.getSubject(AccessController.getContext()));
      input.servletRequest(httpRequest);
      input.servletResponse(httpResponse);
      input.session(lookupServerSessionOnHttpSession(input.copy()));

      input = interceptServerJobInput(input);

      try {
        continueChainInServerJob(chain, input);
      }
      catch (ProcessingException e) {
        handleException(e, httpRequest);
      }
      catch (RuntimeException e) {
        handleException(new ProcessingException("Unexpected error while processing HTTP request", e), httpRequest);
      }
    }
    catch (ProcessingException e) {
      throw new ServletException("Failed to process request", e);
    }
  }

  // === SESSION LOOKUP ===

  /**
   * Override this method to intercept the {@link ServerJobInput} used to run server jobs. The default implementation
   * simply returns the given input.
   */
  protected ServerJobInput interceptServerJobInput(final ServerJobInput input) {
    return input;
  }

  @Internal
  protected IServerSession lookupServerSessionOnHttpSession(ServerJobInput jobInput) throws ProcessingException, ServletException {
    HttpServletRequest req = jobInput.getServletRequest();
    HttpServletResponse res = jobInput.getServletResponse();

    //external request: apply locking, this is the session initialization phase
    IHttpSessionCacheService cacheService = SERVICES.getService(IHttpSessionCacheService.class);
    IServerSession serverSession = (IServerSession) cacheService.getAndTouch(IServerSession.class.getName(), req, res);
    if (serverSession == null) {
      synchronized (req.getSession()) {
        serverSession = (IServerSession) cacheService.get(IServerSession.class.getName(), req, res); // double checking
        if (serverSession == null) {
          serverSession = provideServerSession(jobInput);
          cacheService.put(IServerSession.class.getName(), serverSession, req, res);
        }
      }
    }
    return serverSession;
  }

  /**
   * Method invoked to provide a new {@link IServerSession} for the current HTTP-request.
   *
   * @param input
   *          context information about the ongoing HTTP-request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerJobInput input) throws ProcessingException {
    final IServerSession serverSession = OBJ.one(ServerSessionProvider.class).provide(input);
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(input.getServletRequest(), input.getServletResponse()));
    return serverSession;
  }

  /**
   * Method invoked to handle exceptions which are thrown while continuing chain.
   */
  protected void handleException(ProcessingException pe, HttpServletRequest request) {
    pe.addContextMessage("Client=%s@%s/%s", request.getRemoteUser(), request.getRemoteAddr(), request.getRemoteHost());
    try {
      SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
    }
    catch (RuntimeException e) {
      LOG.warn("Failed to handle request exception", e);
    }
  }

  /**
   * Method invoked to continue chain on behalf of a server job.
   *
   * @param input
   *          input to be used to run the server job with current context information set.
   */
  protected void continueChainInServerJob(final FilterChain chain, final ServerJobInput input) throws ProcessingException {
    OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        chain.doFilter(input.getServletRequest(), input.getServletResponse());
      }
    }, input);
  }
}
