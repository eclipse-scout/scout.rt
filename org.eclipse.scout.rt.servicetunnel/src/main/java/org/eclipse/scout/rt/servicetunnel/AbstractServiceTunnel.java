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
package org.eclipse.scout.rt.servicetunnel;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.IApplication;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.JobExecutionException;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.VersionMismatchException;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.ServiceUtility;

/**
 * Service tunnel is Thread-Safe.
 *
 * @author awe (refactoring)
 */
public abstract class AbstractServiceTunnel<T extends ISession> implements IServiceTunnel {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractServiceTunnel.class);

  private String m_version;
  private URL m_serverURL;
  private String m_sharedSecret;
  private final T m_session;

  /**
   * If the version parameter is null, the product bundle (e.g. com.bsiag.crm.ui.swing) version is used.
   */
  //TODO [nosgi] imo no version param, use config.ini servicetunnel.version
  public AbstractServiceTunnel(T session, String version) {
    m_session = session;
    m_version = getVersion(version);
    m_sharedSecret = ConfigIniUtility.getProperty(PROP_SHARED_SECRET, ConfigIniUtility.getProperty("scout.ajax.token.key"));
    String url = ConfigIniUtility.getProperty(PROP_TARGET_URL, ConfigIniUtility.getProperty("server.url"));
    if (!StringUtility.isNullOrEmpty(url)) {
      try {
        m_serverURL = new URL(url);
      }
      catch (MalformedURLException e) {
        throw new IllegalArgumentException("targetUrl: " + url, e);
      }
    }
  }

  private static String getVersion(String providedVersion) {
    if (providedVersion == null) {
      IApplication app = BEANS.opt(IApplication.class);
      if (app != null) {
        String version = app.getVersion();
        if (version != null) {
          return version;
        }
      }
    }
    return providedVersion;
  }

  public String getVersion() {
    return m_version;
  }

  @Override
  public URL getServerURL() {
    return m_serverURL;
  }

  @Override
  public void setServerURL(URL url) {
    m_serverURL = url;
  }

  public String getSharedSecret() {
    return m_sharedSecret;
  }

  protected T getSession() {
    return m_session;
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) throws ProcessingException {
    if (getServerURL() == null) {
      throw new ProcessingException("serverURL is null. Check proxyHandler extension. Example value is: http://localhost:8080/myapp/process");
    }
    long t0 = System.nanoTime();
    try {
      if (callerArgs == null) {
        callerArgs = new Object[0];
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("" + serviceInterfaceClass + "." + operation + "(" + Arrays.asList(callerArgs) + ")");
      }
      Object[] serializableArgs = ServiceUtility.filterHolderArguments(callerArgs);
      IServiceTunnelRequest call = createServiceTunnelRequest(getVersion(), serviceInterfaceClass, operation, serializableArgs);
      decorateServiceRequest(call);
      //
      IServiceTunnelResponse response = tunnel(call);
      // check if response is interrupted (incomplete /null=interrupted)
      if (response == null) {
        response = new ServiceTunnelResponse(null, null, new InterruptedException());
      }
      onInvokeService(t0, response);
      // error handler
      Throwable t = response.getException();
      if (t != null) {
        String msg = "Calling " + serviceInterfaceClass.getSimpleName() + "." + operation.getName() + "()";
        ProcessingException pe;
        if (t instanceof VersionMismatchException) {
          VersionMismatchException ve = (VersionMismatchException) t;
          pe = ve;
        }
        else if (t instanceof ProcessingException) {
          ((ProcessingException) t).addContextMessage(msg);
          pe = (ProcessingException) t;
        }
        else {
          pe = new ProcessingException(msg, t);
        }
        // combine local and remote stacktraces
        StackTraceElement[] trace1 = pe.getStackTrace();
        StackTraceElement[] trace2 = new Exception().getStackTrace();
        StackTraceElement[] both = new StackTraceElement[trace1.length + trace2.length];
        System.arraycopy(trace1, 0, both, 0, trace1.length);
        System.arraycopy(trace2, 0, both, trace1.length, trace2.length);
        pe.setStackTrace(both);
        throw pe;
      }
      ServiceUtility.updateHolderArguments(callerArgs, response.getOutVars(), false);
      return response.getData();
    }
    catch (Throwable t) {
      if (t instanceof ProcessingException) {
        throw (ProcessingException) t;
      }
      else {
        throw new ProcessingException(serviceInterfaceClass.getSimpleName() + "." + operation.getName() + "(" + VerboseUtility.dumpObjects(callerArgs) + ")", t);
      }
    }
  }

  protected IServiceTunnelRequest createServiceTunnelRequest(String version, Class serviceInterfaceClass, Method operation, Object[] args) {
    UserAgent userAgent = UserAgent.CURRENT.get();
    if (userAgent == null) {
      LOG.warn("No UserAgent set on calling context; include default in service-request");
      userAgent = UserAgent.createDefault();
    }

    // default implementation
    ServiceTunnelRequest call = new ServiceTunnelRequest(version, serviceInterfaceClass.getName(), operation.getName(), operation.getParameterTypes(), args);
    call.setClientSubject(getSession().getSubject());
    call.setUserAgent(userAgent.createIdentifier());

    return call;
  }

  /**
   * Override this method to do additional things before the service is called
   */
  protected void decorateServiceRequest(IServiceTunnelRequest call) {
  }

  /**
   * Override this method to do additional things after the the response has been received.
   *
   * @param t0
   *          System time before the request has been started (may be used for performance analyzing).
   * @param response
   */
  protected void onInvokeService(long t0, IServiceTunnelResponse response) {
  }

  protected abstract IServiceTunnelResponse tunnel(final IServiceTunnelRequest call) throws JobExecutionException;
}
