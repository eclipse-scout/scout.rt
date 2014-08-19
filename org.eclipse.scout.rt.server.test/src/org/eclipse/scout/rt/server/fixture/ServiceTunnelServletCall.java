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
package org.eclipse.scout.rt.server.fixture;

import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.shared.servicetunnel.DefaultServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.osgi.framework.Bundle;

/**
 * Calls the /process servlet. Requires the config.ini variable <code>server.url</code>
 */
public class ServiceTunnelServletCall extends Thread {
  private IServiceTunnelRequest m_req;
  private IServiceTunnelResponse m_res;
  private String m_serverUrl;

  public ServiceTunnelServletCall(ServiceTunnelRequest req, String serverUrl) {
    m_req = req;
    m_serverUrl = serverUrl;
  }

  public IServiceTunnelResponse getServiceTunnelResponse() {
    return m_res;
  }

  @Override
  public void run() {
    try {
      URL url = new URL(m_serverUrl + "/process");
      IServiceTunnelContentHandler contentHandler = new DefaultServiceTunnelContentHandler();
      Bundle bundle = Activator.getDefault().getBundle();
      contentHandler.initialize(new Bundle[]{bundle}, null);
      //
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Authorization", "Basic " + Base64Utility.encode("admin:manager".getBytes()));
      conn.setDoInput(true);
      conn.setDoOutput(true);
      contentHandler.writeRequest(conn.getOutputStream(), m_req);
      m_res = contentHandler.readResponse(conn.getInputStream());
    }
    catch (Throwable e) {
      m_res = new ServiceTunnelResponse(null, null, e);
    }
  }
}
