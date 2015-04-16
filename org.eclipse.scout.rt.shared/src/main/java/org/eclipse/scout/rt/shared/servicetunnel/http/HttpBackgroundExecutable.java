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
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.servicetunnel.HttpException;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;

/**
 * A runnable which is executed by a Job, the run() method performs an HTTP request and returns the response.
 */
public class HttpBackgroundExecutable implements IHttpBackgroundExecutable {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HttpBackgroundExecutable.class);
  public static final String HTTP_DEBUG_PARAM = "org.eclipse.scout.rt.client.http.debug";

  private final Object m_callerLock;
  private final IServiceTunnelRequest m_req;
  private final AbstractHttpServiceTunnel m_tunnel;
  private final boolean m_debug;

  private IServiceTunnelResponse m_res;
  private URLConnection m_urlConn;

  public HttpBackgroundExecutable(IServiceTunnelRequest req, Object callerLock, AbstractHttpServiceTunnel tunnel) {
    m_req = req;
    m_callerLock = callerLock;
    m_tunnel = tunnel;
    m_debug = isDebug();
  }

  private static boolean isDebug() {
    return ConfigIniUtility.getPropertyBoolean(HTTP_DEBUG_PARAM, false);
  }

  @Override
  public IServiceTunnelResponse getResponse() {
    return m_res;
  }

  @Override
  public void run() {
    try {
      delayForDebug(m_req, 0);
      long time1 = 0, time2 = 0;
      //
      if (m_debug) {
        time1 = System.nanoTime();
      }

      // build soap request
      ByteArrayOutputStream msgout = new ByteArrayOutputStream();
      m_tunnel.getContentHandler().writeRequest(msgout, m_req);
      msgout.close();
      byte[] callData = msgout.toByteArray();

      // send
      m_urlConn = m_tunnel.createURLConnection(m_req, callData);

      // receive
      int code = (m_urlConn instanceof HttpURLConnection ? ((HttpURLConnection) m_urlConn).getResponseCode() : 200);
      m_tunnel.preprocessHttpRepsonse(m_urlConn, m_req, code);
      if (code == 0 || (code >= 200 && code <= 299)) {
        // ok
      }
      else {
        m_res = new ServiceTunnelResponse(null, null, new HttpException(code));
        return;
      }

      try (InputStream httpin = m_urlConn.getInputStream()) {
        m_res = m_tunnel.getContentHandler().readResponse(httpin);
        httpin.close();
      }

      if (m_debug) {
        time2 = System.nanoTime();
        LOG.debug("TIME " + m_req.getServiceInterfaceClassName() + "." + m_req.getOperation() + " " + (time2 - time1) / 1000000L + "ms " + callData.length + " bytes");
      }
    }
    catch (Throwable e) {
      //cancel has precedence over failure
      if (m_res == null) {
        m_res = new ServiceTunnelResponse(null, null, e);
      }
    }
    finally {
      synchronized (m_callerLock) {
        m_callerLock.notifyAll();
      }
    }
  }

  private void delayForDebug(IServiceTunnelRequest call, long millis) throws InterruptedException {
    if (millis <= 0) {
      return;
    }
    //
    System.out.println("#Delay " + millis + "ms for debugging " + call.getServiceInterfaceClassName() + "." + call.getOperation());
    Thread.sleep(millis);
  }

}
