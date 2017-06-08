/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.fixture;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnelTransportManager;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;

/**
 * Calls the /process servlet. Requires the config.properties variable <code>server.url</code>
 */
public class ServiceTunnelServletCall extends Thread {
  private ServiceTunnelRequest m_req;
  private ServiceTunnelResponse m_res;
  private String m_serverUrl;

  public ServiceTunnelServletCall(ServiceTunnelRequest req, String serverUrl) {
    m_req = req;
    m_serverUrl = serverUrl;
  }

  public ServiceTunnelResponse getServiceTunnelResponse() {
    return m_res;
  }

  @Override
  public void run() {
    try {
      URL url = new URL(m_serverUrl + "/process");
      final IServiceTunnelContentHandler contentHandler = BEANS.get(IServiceTunnelContentHandler.class);
      contentHandler.initialize();
      //
      HttpRequestFactory requestFactory = BEANS.get(HttpServiceTunnelTransportManager.class).getHttpRequestFactory();
      HttpRequest req = requestFactory.buildPostRequest(new GenericUrl(url), new HttpContent() {

        @Override
        public void writeTo(OutputStream out) throws IOException {
          contentHandler.writeRequest(out, m_req);
        }

        @Override
        public boolean retrySupported() {
          return false;
        }

        @Override
        public String getType() {
          return null;
        }

        @Override
        public long getLength() throws IOException {
          return 0;
        }
      });
      req.getHeaders().setAuthorization("Basic " + Base64Utility.encode("admin:manager".getBytes()));
      HttpResponse resp = req.execute();
      m_res = contentHandler.readResponse(resp.getContent());
    }
    catch (Throwable e) {
      m_res = new ServiceTunnelResponse(e);
    }
  }
}
