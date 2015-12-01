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
package org.eclipse.scout.rt.client.fixture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.client.servicetunnel.http.ClientHttpServiceTunnel;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.UriUtility;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceUtility;

@IgnoreBean
public class MockServiceTunnel extends ClientHttpServiceTunnel {

  private final HashMap<Long, Thread> m_runningMap = new HashMap<Long, Thread>();

  public MockServiceTunnel() throws Exception {
    super(UriUtility.toUrl("http://mock/process"));
    resetRequestSequenceGenerator();
  }

  public static void resetRequestSequenceGenerator() throws Exception {
    Field f = ServiceTunnelRequest.class.getDeclaredField("requestSequenceGenerator");
    f.setAccessible(true);
    AtomicLong gen = (AtomicLong) f.get(null);
    gen.set(0);
  }

  public Thread getThreadByRequestSequence(long requestSequence) {
    return m_runningMap.get(requestSequence);
  }

  /**
   * @return the service response You may call callTargetService() to simply call a service for test purpose (without a
   *         transaction!)
   */
  protected ServiceTunnelResponse mockServiceCall(ServiceTunnelRequest req) throws Exception {
    try {
      ServiceUtility serviceUtility = BEANS.get(ServiceUtility.class);

      Class<?> serviceInterface = Class.forName(req.getServiceInterfaceClassName());
      Method serviceOperation = serviceUtility.getServiceOperation(serviceInterface, req.getOperation(), req.getParameterTypes());
      Object service = null;
      for (Object t : BEANS.all(serviceInterface)) {
        if (Proxy.isProxyClass(t.getClass())) {
          continue;
        }
        service = t;
        break;
      }
      Object result = serviceUtility.invoke(serviceOperation, service, req.getArgs());
      return new ServiceTunnelResponse(result, null, null);
    }
    catch (Throwable t) {
      return new ServiceTunnelResponse(null, null, t);
    }
  }

  @Override
  protected URLConnection createURLConnection(final ServiceTunnelRequest call, byte[] callData) throws IOException {
    URLConnection urlConn = new MockHttpURLConnection(getServerUrl()) {
      @Override
      protected void mockHttpServlet(InputStream servletIn, OutputStream servletOut) throws Exception {
        ServiceTunnelRequest req = getContentHandler().readRequest(servletIn);
        try {
          m_runningMap.put(call.getRequestSequence(), Thread.currentThread());
          //
          ServiceTunnelResponse res = MockServiceTunnel.this.mockServiceCall(req);
          if (res.getException() != null) {
            throw new Exception(res.getException());
          }
          getContentHandler().writeResponse(servletOut, res);
        }
        finally {
          m_runningMap.remove(call.getRequestSequence());
        }
      }
    };
    //
    String contentType = "text/xml";
    urlConn.setRequestProperty("Content-type", contentType);
    urlConn.setDoOutput(true);
    urlConn.setDoInput(true);
    urlConn.setDefaultUseCaches(false);
    urlConn.setUseCaches(false);
    //
    OutputStream httpOut = urlConn.getOutputStream();
    httpOut.write(callData);
    httpOut.close();
    httpOut = null;
    return urlConn;
  }
}
