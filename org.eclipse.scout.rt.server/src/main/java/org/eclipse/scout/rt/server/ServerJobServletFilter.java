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

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Expected init-param example: session=com.bsiag.myapp.server.ServerSession
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
  @SuppressWarnings("unchecked")
  public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(sreq);
    if (!config.isActive()) {
      chain.doFilter(sreq, sres);
      return;
    }
    //
    HttpServletRequest httpRequest = (HttpServletRequest) sreq;
    HttpServletResponse httpResponse = (HttpServletResponse) sres;
    if ("/process".equals(httpRequest.getServletPath())) {
      chain.doFilter(sreq, sres);
      return;
    }
    IServerSession serverSession;
    // create new session
    synchronized (httpRequest.getSession()) {
      serverSession = (IServerSession) SERVICES.getService(IHttpSessionCacheService.class).getAndTouch(IServerSession.class.getName(), httpRequest, httpResponse);
      if (serverSession == null) {
        String qname = config.getInitParameter("session");
        Class<? extends IServerSession> serverSessionClass;
        if (qname == null) {
          throw new ServletException("Expected init-param \"session\"");
        }
        int i = qname.lastIndexOf('.');
        try {
          serverSessionClass = (Class<? extends IServerSession>) Platform.getBundle(qname.substring(0, i)).loadClass(qname);
        }
        catch (ClassNotFoundException e) {
          throw new ServletException("Loading class " + qname, e);
        }
        try {
          serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(serverSessionClass, (Subject) null);
          // store new session
          SERVICES.getService(IHttpSessionCacheService.class).put(IServerSession.class.getName(), serverSession, httpRequest, httpResponse);
        }
        catch (Throwable t) {
          LOG.error(String.format("Failed to create and start session '%s'", serverSessionClass), t);
          httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
      }
    }

    // Create the job-input on behalf of which the server-job is run.
    ServerJobInput input = ServerJobInput.empty();
    input.name(ServerJobServletFilter.class.getSimpleName());
    input.servletRequest(httpRequest);
    input.servletResponse(httpResponse);
    input.session(serverSession);
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
