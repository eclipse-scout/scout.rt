/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.fixture;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.http.HttpResponse;
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

  private final HashMap<Long, Thread> m_runningMap = new HashMap<>();

  public MockServiceTunnel() throws Exception {
    super(UriUtility.toUri("http://mock/process"));
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
  protected ServiceTunnelResponse mockServiceCall(ServiceTunnelRequest req) {
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
      Object result = serviceUtility.invoke(service, serviceOperation, req.getArgs());
      return new ServiceTunnelResponse(result, null);
    }
    catch (Throwable t) {
      return new ServiceTunnelResponse(null, t);
    }
  }

  @Override
  protected HttpResponse<InputStream> executeRequest(ServiceTunnelRequest call, byte[] callData) {
    return null;
  }
}
