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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.PlaceholderException;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.http.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.inspector.CallInspector;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.admin.inspector.SessionInspector;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.security.UpdateServiceConfigurationPermission;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.VersionMismatchException;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Use this servlet together with a {@link ServerJobServletFilter}
 */
public class ServiceTunnelServlet extends HttpServletEx {
  public static final String HTTP_DEBUG_PARAM = "org.eclipse.scout.rt.server.http.debug";
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceTunnelServlet.class);

  private transient IServiceTunnelContentHandler m_msgEncoder;
  private transient Bundle[] m_orderedBundleList;
  private Object m_msgEncoderLock = new Boolean(true);
  private Object m_orderedBundleListLock = new Boolean(true);
  private Class<? extends IServerSession> m_serverSessionClass;
  private Version m_requestMinVersion;
  private boolean m_debug;

  public ServiceTunnelServlet() {
    String text = Activator.getDefault().getBundle().getBundleContext().getProperty(HTTP_DEBUG_PARAM);
    if (text != null && text.equalsIgnoreCase("true")) {
      m_debug = true;
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    m_requestMinVersion = initRequestMinVersion(config);
  }

  @SuppressWarnings("unchecked")
  protected void lazyInit(HttpServletRequest req, HttpServletResponse res) throws ServletException {
    if (m_serverSessionClass == null) {
      String qname = getServletConfig().getInitParameter("session");
      if (qname != null) {
        int i = qname.lastIndexOf('.');
        try {
          m_serverSessionClass = (Class<? extends IServerSession>) Platform.getBundle(qname.substring(0, i)).loadClass(qname);
        }
        catch (ClassNotFoundException e) {
          throw new ServletException("Loading class " + qname, e);
        }
      }
    }
    if (m_serverSessionClass == null) {
      // find bundle that defines this servlet
      try {
        Bundle bundle = findServletContributor(req.getServletPath());
        if (bundle != null) {
          m_serverSessionClass = (Class<? extends IServerSession>) bundle.loadClass(bundle.getSymbolicName() + ".ServerSession");
        }
      }
      catch (Throwable t) {
        // nop
      }
    }
    if (m_serverSessionClass == null) {
      throw new ServletException("Expected init-param \"session\"");
    }
  }

  /**
   * <p>
   * Reads the minimum version a request must have.
   * </p>
   * <p>
   * The version has to be defined as init parameter in the servlet configuration. <br/>
   * This can be done by adding a new init-param at the {@link DefaultHttpProxyHandlerServlet} on the extension point
   * org.eclipse.equinox.http.registry.servlets and setting its name to min-version and its value to the desired version
   * (like 1.2.3).
   * </p>
   * <p>
   * If there is no min-version defined it uses the Bundle-Version of the bundle which contains the running product.
   * </p>
   * 
   * @param config
   * @return
   */
  protected Version initRequestMinVersion(ServletConfig config) {
    Version version = null;
    String v = config.getInitParameter("min-version");
    if (v != null) {
      Version tmp = Version.parseVersion(v);
      version = new Version(tmp.getMajor(), tmp.getMinor(), tmp.getMicro());
    }
    else if (Platform.getProduct() != null) {
      v = (String) Platform.getProduct().getDefiningBundle().getHeaders().get("Bundle-Version");
      Version tmp = Version.parseVersion(v);
      version = new Version(tmp.getMajor(), tmp.getMinor(), tmp.getMicro());
    }

    return version;
  }

  protected IServiceTunnelContentHandler createMessageEncoder(Class<? extends IServerSession> sessionClass) {
    DefaultServiceTunnelContentHandler e = new DefaultServiceTunnelContentHandler();
    e.initialize(getOrderedBundleList(), sessionClass.getClassLoader());
    return e;
  }

  private IServiceTunnelContentHandler getServiceTunnelContentHandler() {
    synchronized (m_msgEncoderLock) {
      if (m_msgEncoder == null) {
        m_msgEncoder = createMessageEncoder(m_serverSessionClass);
      }
    }
    return m_msgEncoder;
  }

  private Bundle[] getOrderedBundleList() {
    synchronized (m_orderedBundleListLock) {
      if (m_orderedBundleList == null) {
        String prefix = m_serverSessionClass.getPackage().getName().replaceAll("^(.*\\.)(client|shared|server)(\\.core)?.*$", "$1");
        m_orderedBundleList = BundleInspector.getOrderedBundleList(prefix, "org.eclipse.scout.");
      }
    }
    return m_orderedBundleList;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    lazyInit(req, res);
    Map<Class, Object> backup = ThreadContext.backup();
    try {
      ThreadContext.put(req);
      ThreadContext.put(res);

      try {
        ServiceTunnelRequest serviceTunnelReq = null;
        if ("POST".equalsIgnoreCase(req.getMethod())) {
          serviceTunnelReq = deserializeInput(req.getInputStream());
        }
        else {
          String q = req.getQueryString();
          if (q != null) {
            for (String s : q.split("[&]")) {
              if (s.startsWith("msg=")) {
                String msg = URLDecoder.decode(s.substring(4), "UTF-8");
                serviceTunnelReq = deserializeInput(new ByteArrayInputStream(msg.getBytes("UTF-8")));
              }
            }
          }

        }
        if (serviceTunnelReq != null) {
          req.setAttribute(ServiceTunnelRequest.class.getName(), serviceTunnelReq);
          LocaleThreadLocal.set(serviceTunnelReq.getLocale());
          NlsLocale.setThreadDefault(new NlsLocale(serviceTunnelReq.getNlsLocale()));
        }
        //
        try {
          IServerSession serverSession;
          // apply locking, this is the session initialization phase
          synchronized (req.getSession()) {
            serverSession = (IServerSession) req.getSession().getAttribute(IServerSession.class.getName());
            if (serverSession == null) {
              serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(m_serverSessionClass, null);
              req.getSession().setAttribute(IServerSession.class.getName(), serverSession);
            }
          }
          if (serverSession == null) {
            throw new ServletException("expected a IServerSession in the ThreadContext.");
          }
          ServerJob job = createServiceTunnelServerJob(serverSession, serviceTunnelReq, req, res);
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
        catch (ProcessingException pe) {
          if (pe.getCause() != null) throw pe.getCause();
          else throw pe;
        }
      }
      catch (IOException e) {
        throw e;
      }
      catch (ServletException e) {
        throw e;
      }
      catch (Throwable e) {
        throw new ServletException("Client=" + req.getRemoteUser() + "@" + req.getRemoteAddr() + "/" + req.getRemoteHost(), e);
      }
    }
    finally {
      ThreadContext.restore(backup);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    // get session
    if (!ACCESS.check(new UpdateServiceConfigurationPermission())) {
      throw new SecurityException("Access denied, no " + UpdateServiceConfigurationPermission.class.getName() + " found");
    }
    //
    HttpSession session = req.getSession();
    String key = AdminSession.class.getName();
    AdminSession as = (AdminSession) session.getAttribute(key);
    if (as == null) {
      as = new AdminSession();
      session.setAttribute(key, as);
    }
    as.serviceRequest(req, res);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    try {
      // store start time millis
      req.setAttribute(ServiceTunnelServlet.class.getName() + ".requestStart", System.nanoTime());
      // process service request
      long time1 = 0, time2 = 0;
      if (m_debug) time1 = System.nanoTime();
      //
      ServiceTunnelRequest serviceReq = (ServiceTunnelRequest) req.getAttribute(ServiceTunnelRequest.class.getName());
      handleSoapServiceCall(req, resp, serviceReq);
      if (m_debug) {
        time2 = System.nanoTime();
        LOG.debug("TIME " + serviceReq.getServiceInterfaceClassName() + "." + serviceReq.getOperation() + " " + (time2 - time1) / 1000000L + "ms");
      }
    }
    catch (Throwable t) {
      // cancel tx
      ITransaction transaction = ThreadContext.get(ITransaction.class);
      if (transaction != null) {
        transaction.addFailure(t);
      }
      // send error response
      try {
        // rewrite exception to avoid class not found exception in client, mostly due to unknown classes in the throwable's or statuse's getCause()
        Throwable saveEx = PlaceholderException.transformException(t);
        if (!(saveEx instanceof ProcessingException)) {
          saveEx = new ProcessingException(saveEx.getMessage(), saveEx.getCause());
        }
        ServiceTunnelResponse hres = new ServiceTunnelResponse(null, null, saveEx);
        Long t1 = (Long) req.getAttribute(ServiceTunnelServlet.class.getName() + ".requestStart");
        if (t1 != null) {
          hres.setProcessingDuration((System.nanoTime() - t1) / 1000000L);
        }
        serializeOutput(resp, hres);
      }
      catch (Throwable ex) {
        // nop
      }
      // handle error
      boolean needsLog = true;
      Throwable cause = t;
      while (cause != null && needsLog) {
        if (cause instanceof SocketException && cause.getMessage().equals("Connection reset by peer: socket write error")) {
          // we don't want to throw an exception, if the client closed the connection
          needsLog = false;
        }
        else if (cause instanceof InterruptedIOException) {
          needsLog = false;
        }
        // next
        cause = cause.getCause();
      }
      // on info log level show all exceptions in log file
      if (LOG.isInfoEnabled() || LOG.isDebugEnabled()) {
        needsLog = true;
      }
      if (needsLog) {
        try {
          ProcessingException pe = (t instanceof ProcessingException) ? (ProcessingException) t : new ProcessingException("servletHandler", t);
          pe.addContextMessage("Client=" + req.getRemoteUser() + "@" + req.getRemoteAddr() + "/" + req.getRemoteHost());
          SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
        }
        catch (Throwable fatal) {
          // nop
        }
      }
    }
  }

  /**
   * this method is executed within a IServerSession context that is provided by
   * either a filter or directly above
   */
  protected void handleSoapServiceCall(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServiceTunnelRequest serviceReq) throws Throwable {
    String soapOperation = ServiceTunnelRequest.toSoapOperation(serviceReq.getServiceInterfaceClassName(), serviceReq.getOperation());
    IServerSession serverSession = ThreadContext.get(IServerSession.class);
    String authenticatedUser = serverSession.getUserId();
    if (LOG.isDebugEnabled()) LOG.debug("request started " + httpRequest.getRemoteAddr() + "/" + authenticatedUser + " at " + new Date());
    // version check of request
    if (m_requestMinVersion != null) {
      String v = serviceReq.getVersion();
      if (v == null) {
        v = "0.0.0";
      }
      Version requestVersion = Version.parseVersion(v);
      if (requestVersion.compareTo(m_requestMinVersion) < 0) {
        ServiceTunnelResponse serviceRes = new ServiceTunnelResponse(null, null, new VersionMismatchException(requestVersion.toString(), m_requestMinVersion.toString()));
        serializeOutput(httpResponse, serviceRes);
        return;
      }
    }
    CallInspector callInspector = null;
    SessionInspector sessionInspector = ProcessInspector.getDefault().getSessionInspector(serverSession, true);
    if (sessionInspector != null) {
      callInspector = sessionInspector.requestCallInspector(serviceReq);
    }
    ServiceTunnelResponse serviceRes = null;
    try {
      // check if locales changed
      Locale userLocale = LocaleThreadLocal.get();
      NlsLocale userNlsLocale = NlsLocale.getDefault();
      if (CompareUtility.equals(userLocale, serverSession.getLocale()) && CompareUtility.equals(userNlsLocale, serverSession.getNlsLocale())) {
        // ok
      }
      else {
        serverSession.setLocale(userLocale);
        serverSession.setNlsLocale(userNlsLocale);
        if (serverSession instanceof AbstractServerSession) {
          ((AbstractServerSession) serverSession).execLocaleChanged();
        }
      }
      //
      Class<?> serviceInterfaceClass = null;
      for (Bundle b : getOrderedBundleList()) {
        try {
          serviceInterfaceClass = b.loadClass(serviceReq.getServiceInterfaceClassName());
          break;
        }
        catch (ClassNotFoundException e) {
          // nop
        }
      }
      if (serviceInterfaceClass == null) {
        throw new ClassNotFoundException(serviceReq.getServiceInterfaceClassName());
      }
      //check access level 1: service registration property
      Object service = SERVICES.getService(serviceInterfaceClass);
      if (service == null) {
        throw new ProcessingException("service registry does not contain a service of type " + serviceReq.getServiceInterfaceClassName());
      }
      Method serviceOp = ServiceUtility.getServiceOperation(serviceInterfaceClass, serviceReq.getOperation(), serviceReq.getParameterTypes());
      IAccessControlService acs = SERVICES.getService(IAccessControlService.class);
      if (acs != null && !acs.checkServiceTunnelAccess(serviceInterfaceClass, serviceOp, serviceReq.getArgs())) {
        throw new ProcessingException("access denied to " + serviceReq.getServiceInterfaceClassName() + "#" + serviceOp.getName(), new SecurityException("Access denied"));
      }
      Object data = ServiceUtility.invoke(serviceOp, service, serviceReq.getArgs());
      Object[] outParameters = ServiceUtility.extractHolderArguments(serviceReq.getArgs());
      serviceRes = new ServiceTunnelResponse(data, outParameters, null);
      serviceRes.setSoapOperation(soapOperation);
      // add performance data
      Long t1 = (Long) httpRequest.getAttribute(ServiceTunnelServlet.class.getName() + ".requestStart");
      if (t1 != null) {
        serviceRes.setProcessingDuration((System.nanoTime() - t1) / 1000000L);
      }
      // add accumulated client notifications as side-payload
      IClientNotification[] na = SERVICES.getService(IClientNotificationService.class).getNextNotifications(0);
      serviceRes.setClientNotifications(na);
      serializeOutput(httpResponse, serviceRes);
    }
    finally {
      if (callInspector != null) {
        try {
          callInspector.update();
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
        try {
          callInspector.close(serviceRes);
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
        try {
          callInspector.getSessionInspector().update();
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
      }
    }
  }

  protected ServiceTunnelRequest deserializeInput(InputStream in) throws Exception {
    ServiceTunnelRequest req = getServiceTunnelContentHandler().readRequest(in);
    return req;
  }

  protected void serializeOutput(HttpServletResponse httpResponse, ServiceTunnelResponse res) throws Exception {
    try {
      httpResponse.setDateHeader("Expires", -1);
      httpResponse.setHeader("Cache-Control", "no-cache");
      httpResponse.setHeader("pragma", "no-cache");
      httpResponse.setContentType("text/xml");
      getServiceTunnelContentHandler().writeResponse(httpResponse.getOutputStream(), res);
    }
    finally {
    }
  }

  private Bundle findServletContributor(String alias) throws CoreException {
    BundleContext context = Activator.getDefault().getBundle().getBundleContext();
    ServiceReference ref = context.getServiceReference(IExtensionRegistry.class.getName());
    Bundle bundle = null;
    if (ref != null) {
      IExtensionRegistry reg = (IExtensionRegistry) context.getService(ref);
      if (reg != null) {
        IExtensionPoint xpServlet = reg.getExtensionPoint("org.eclipse.equinox.http.registry.servlets");
        if (xpServlet != null) {
          for (IExtension xServlet : xpServlet.getExtensions()) {
            for (IConfigurationElement cServlet : xServlet.getConfigurationElements()) {
              if (cServlet.getName().equals("servlet")) {
                if (this.getClass().getName().equals(cServlet.getAttribute("class"))) {
                  // half match, go on looping
                  bundle = Platform.getBundle(xServlet.getContributor().getName());
                  if (alias.equals(cServlet.getAttribute("alias"))) {
                    // full match, return
                    return bundle;
                  }
                }
              }
            }
          }
        }
      }
    }
    return bundle;
  }

  protected ServerJob createServiceTunnelServerJob(IServerSession serverSession, ServiceTunnelRequest serviceTunnelRequest, HttpServletRequest request, HttpServletResponse response) {
    return new ServiceTunnelServiceJob(serverSession, serviceTunnelRequest, request, response);
  }

  protected class ServiceTunnelServiceJob extends ServerJob {

    protected ServiceTunnelRequest m_serviceTunnelRequest;
    protected HttpServletRequest m_request;
    protected HttpServletResponse m_response;

    public ServiceTunnelServiceJob(IServerSession serverSession, ServiceTunnelRequest serviceTunnelRequest, HttpServletRequest request, HttpServletResponse response) {
      super("ServiceTunnel", serverSession);
      m_serviceTunnelRequest = serviceTunnelRequest;
      m_request = request;
      m_response = response;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      if (m_serviceTunnelRequest != null) {
        ServiceTunnelServlet.this.doPost(m_request, m_response);
      }
      else {
        ServiceTunnelServlet.super.service(m_request, m_response);
      }
      return Status.OK_STATUS;
    }
  }
}
