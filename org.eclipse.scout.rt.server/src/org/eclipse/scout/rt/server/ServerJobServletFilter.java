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
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Expected init-param example: session=com.bsiag.myapp.server.ServerSession
 */
public class ServerJobServletFilter implements Filter {
  private static final long serialVersionUID = 1L;
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
    HttpServletRequest req = (HttpServletRequest) sreq;
    HttpServletResponse res = (HttpServletResponse) sres;
    if ("/process".equals(req.getServletPath())) {
      chain.doFilter(sreq, sres);
      return;
    }
    // get cached session
    IServerSession serverSession = (IServerSession) req.getSession().getAttribute(IServerSession.class.getName());
    // create new session
    synchronized (req.getSession()) {
      serverSession = (IServerSession) req.getSession().getAttribute(IServerSession.class.getName());
      if (serverSession == null) {
        String qname = config.getInitParameter("session");
        Class<? extends IServerSession> serverSessionClass;
        if (qname == null) throw new ServletException("Expected init-param \"session\"");
        int i = qname.lastIndexOf('.');
        try {
          serverSessionClass = (Class<? extends IServerSession>) Platform.getBundle(qname.substring(0, i)).loadClass(qname);
        }
        catch (ClassNotFoundException e) {
          throw new ServletException("Loading class " + qname, e);
        }
        try {
          serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(serverSessionClass, null);
          // store new session
          req.getSession().setAttribute(IServerSession.class.getName(), serverSession);
        }
        catch (Throwable t) {
          LOG.error("create session " + serverSessionClass, t);
          res.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
      }
    }
    // process service request
    Map<Class, Object> backup = ThreadContext.backup();
    try {
      ThreadContext.put(req);
      ThreadContext.put(res);

      ServerJob job = createServiceTunnelServerJob(serverSession, chain, sreq, sres);
      IStatus status = job.runNow(new NullProgressMonitor());
      if (!status.isOK()) {
        try {
          ProcessingException p = new ProcessingException(status);
          p.addContextMessage("Client=" + req.getRemoteUser() + "@" + req.getRemoteAddr() + "/" + req.getRemoteHost());
          SERVICES.getService(IExceptionHandlerService.class).handleException(p);
        }
        catch (Throwable fatal) {
          // nop
        }
      }
    }
    finally {
      ThreadContext.restore(backup);
    }
  }

  protected ServerJob createServiceTunnelServerJob(IServerSession serverSession, FilterChain chain, ServletRequest request, ServletResponse response) {
    return new ServiceTunnelServiceJob(serverSession, chain, request, response);
  }

  protected class ServiceTunnelServiceJob extends ServerJob {

    protected FilterChain m_chain;
    protected ServletRequest m_request;
    protected ServletResponse m_response;

    public ServiceTunnelServiceJob(IServerSession serverSession, FilterChain chain, ServletRequest request, ServletResponse response) {
      super("ServiceTunnel", serverSession);
      m_chain = chain;
      m_request = request;
      m_response = response;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      // delegate to filter chain
      m_chain.doFilter(m_request, m_response);
      return Status.OK_STATUS;
    }
  }
}
