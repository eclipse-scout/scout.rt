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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.server.commons.cache.IClientIdentificationService;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.job.ServerJobs;
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
      ServerRunContext runContext = ServerRunContexts.empty();
      runContext.subject(Subject.getSubject(AccessController.getContext()));
      runContext.servletRequest(httpRequest);
      runContext.servletResponse(httpResponse);
      runContext.session(lookupServerSessionOnHttpSession(runContext.copy()));

      runContext = interceptRunContext(runContext);

      try {
        continueChain(chain, runContext);
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
   * Override this method to intercept the {@link ServerRunContext} used to process a request. The default
   * implementation simply returns the given <code>runContext</code>.
   */
  protected ServerRunContext interceptRunContext(final ServerRunContext runContext) {
    return runContext;
  }

  @Internal
  protected IServerSession lookupServerSessionOnHttpSession(final ServerRunContext runContext) throws ProcessingException, ServletException {
    HttpServletRequest req = runContext.servletRequest();
    HttpServletResponse res = runContext.servletResponse();

    //external request: apply locking, this is the session initialization phase
    IHttpSessionCacheService cacheService = SERVICES.getService(IHttpSessionCacheService.class);
    IServerSession serverSession = (IServerSession) cacheService.getAndTouch(IServerSession.class.getName(), req, res);
    if (serverSession == null) {
      synchronized (req.getSession()) {
        serverSession = (IServerSession) cacheService.get(IServerSession.class.getName(), req, res); // double checking
        if (serverSession == null) {
          serverSession = provideServerSession(runContext);
          cacheService.put(IServerSession.class.getName(), serverSession, req, res);
        }
      }
    }
    return serverSession;
  }

  /**
   * Method invoked to provide a new {@link IServerSession} for the current HTTP-request.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing HTTP-request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerRunContext runContext) throws ProcessingException {
    final IServerSession serverSession = OBJ.get(ServerSessionProvider.class).provide(runContext);
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(runContext.servletRequest(), runContext.servletResponse()));
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
   * Method invoked to continue the chain on behalf of the given <code>RunContext</code>.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing HTTP-request.
   */
  protected void continueChain(final FilterChain chain, final ServerRunContext runContext) throws ProcessingException {
    ServerJobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        chain.doFilter(runContext.servletRequest(), runContext.servletResponse());
      }
    }, ServerJobs.newInput(runContext));
  }
}
