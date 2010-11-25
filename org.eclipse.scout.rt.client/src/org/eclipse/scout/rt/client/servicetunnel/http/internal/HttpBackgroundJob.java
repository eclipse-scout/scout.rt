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
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.Activator;
import org.eclipse.scout.rt.client.servicetunnel.http.HttpServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.HttpException;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

public class HttpBackgroundJob extends JobEx {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HttpBackgroundJob.class);

  private final Object m_callerLock;
  private final ServiceTunnelRequest m_req;
  private ServiceTunnelResponse m_res;
  private final InternalHttpServiceTunnel m_tunnel;
  private URLConnection m_urlConn;
  private boolean m_debug;

  /**
   * @param name
   */
  public HttpBackgroundJob(String name, ServiceTunnelRequest req, Object callerLock, InternalHttpServiceTunnel tunnel) {
    super(name);
    m_req = req;
    m_callerLock = callerLock;
    m_tunnel = tunnel;
    String text = null;
    if (Activator.getDefault() != null) {
      text = Activator.getDefault().getBundle().getBundleContext().getProperty(HttpServiceTunnel.HTTP_DEBUG_PARAM);
    }
    if (text != null && text.equalsIgnoreCase("true")) {
      m_debug = true;
    }
  }

  public ServiceTunnelResponse getResponse() {
    return m_res;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    InputStream httpin = null;
    try {
      delayForDebug(m_req, 0);
      long time1 = 0, time2 = 0;
      //
      if (m_debug) time1 = System.nanoTime();
      // build soap request
      ByteArrayOutputStream msgout = new ByteArrayOutputStream();
      m_tunnel.getContentHandler().writeRequest(msgout, m_req);
      msgout.close();
      byte[] callData = msgout.toByteArray();
      // send
      m_urlConn = m_tunnel.createURLConnection(m_req, callData);
      if (monitor.isCanceled()) throw new InterruptedException();
      // receive
      int code = (m_urlConn instanceof HttpURLConnection ? ((HttpURLConnection) m_urlConn).getResponseCode() : 200);
      m_tunnel.preprocessHttpRepsonse(m_urlConn, m_req, code);
      if (code == 0 || (code >= 200 && code <= 299)) {
        // ok
      }
      else {
        m_res = new ServiceTunnelResponse(code, null, null, new HttpException(code));
        return Status.CANCEL_STATUS;
      }
      httpin = m_urlConn.getInputStream();
      if (monitor.isCanceled()) throw new InterruptedException();
      m_res = m_tunnel.getContentHandler().readResponse(httpin);
      httpin.close();
      httpin = null;
      if (monitor.isCanceled()) throw new InterruptedException();
      if (m_debug) time2 = System.nanoTime();
      if (m_debug) LOG.debug("TIME " + m_req.getServiceInterfaceClassName() + "." + m_req.getOperation() + " " + (time2 - time1) / 1000000L + "ms " + callData.length + " bytes");
      return Status.OK_STATUS;
    }
    catch (Throwable e) {
      m_res = new ServiceTunnelResponse(null, null, e);
      return Status.CANCEL_STATUS;
    }
    finally {
      if (httpin != null) {
        try {
          httpin.close();
        }
        catch (Throwable fatal) {
        }
      }
      synchronized (m_callerLock) {
        m_callerLock.notifyAll();
      }
    }
  }

  @Override
  protected void canceling() {
    Thread t = getThread();
    if (t != null) {
      t.interrupt();
    }
    if (m_urlConn instanceof HttpURLConnection) {
      try {
        ((HttpURLConnection) m_urlConn).disconnect();
      }
      catch (Throwable x) {
        // nop
      }
    }
  }

  private void delayForDebug(ServiceTunnelRequest call, long millis) {
    if (millis <= 0) return;
    //
    System.out.println("#Delay " + millis + "ms for debugging " + call.getServiceInterfaceClassName() + "." + call.getOperation());
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
    }
  }

}
