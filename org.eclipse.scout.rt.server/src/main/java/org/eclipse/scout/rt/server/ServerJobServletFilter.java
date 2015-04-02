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
import org.eclipse.scout.rt.server.commons.context.ServletRunContexts;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Filter to run the ongoing HTTP-request in a server job. Requests targeted to '/process' are not run in a server job
 * because done in {@link ServiceTunnelServlet}.
 */
//TODO dwi remove?
public class ServerJobServletFilter implements Filter {

  // TODO [dwi][nosgi]: rename this class

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
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(request);
    if (!config.isActive()) {
      chain.doFilter(request, response);
      return;
    }

    try {
      ServletRunContexts.copyCurrent().servletRequest((HttpServletRequest) request).servletResponse((HttpServletResponse) response).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          if ("/process".equals(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get().getServletPath())) {
            chain.doFilter(request, response);
          }
          else {
            continueChain(chain, lookupServerSessionOnHttpSession());
          }
        }
      });
    }
    catch (ProcessingException e) {
      handleException(e);
    }
    catch (RuntimeException e) {
      handleException(new ProcessingException("Unexpected error while processing HTTP request", e));
    }
  }

  // === SESSION LOOKUP ===

  @Internal
  protected IServerSession lookupServerSessionOnHttpSession() throws ProcessingException, ServletException {
    final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

    //external request: apply locking, this is the session initialization phase
    IHttpSessionCacheService cacheService = SERVICES.getService(IHttpSessionCacheService.class);
    IServerSession serverSession = (IServerSession) cacheService.getAndTouch(IServerSession.class.getName(), servletRequest, servletResponse);
    if (serverSession == null) {
      synchronized (servletRequest.getSession()) {
        serverSession = (IServerSession) cacheService.get(IServerSession.class.getName(), servletRequest, servletResponse); // double checking
        if (serverSession == null) {
          serverSession = provideServerSession();
          cacheService.put(IServerSession.class.getName(), serverSession, servletRequest, servletResponse);
        }
      }
    }
    return serverSession;
  }

  /**
   * Method invoked to provide a new {@link IServerSession} for the current HTTP-request.
   */
  protected IServerSession provideServerSession() throws ProcessingException {
    final HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    final HttpServletResponse servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

    final IServerSession serverSession = OBJ.get(ServerSessionProvider.class).provide(ServerRunContexts.copyCurrent());
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(servletRequest, servletResponse));
    return serverSession;
  }

  /**
   * Method invoked to handle exceptions which are thrown while continuing chain.
   */
  protected void handleException(ProcessingException pe) {
    HttpServletRequest servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();

    pe.addContextMessage("Client=%s@%s/%s", servletRequest.getRemoteUser(), servletRequest.getRemoteAddr(), servletRequest.getRemoteHost());
    try {
      SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
    }
    catch (RuntimeException e) {
      LOG.warn("Failed to handle request exception", e);
    }
  }

  /**
   * Method invoked to continue the chain on behalf of a <code>ServerRunContext</code>.
   *
   * @param serverSession
   *          the server session which belongs to the ongoing HTTP request.
   */
  protected void continueChain(final FilterChain chain, final IServerSession serverSession) throws ProcessingException {
    ServerRunContexts.copyCurrent().session(serverSession).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        chain.doFilter(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get(), IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
      }
    });
  }
}
