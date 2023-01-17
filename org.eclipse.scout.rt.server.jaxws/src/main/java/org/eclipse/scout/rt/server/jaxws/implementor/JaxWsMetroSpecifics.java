/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.implementor;

import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.WebServiceRequestRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-WS implementor specifics of 'JAX-WS Metro implementation'.
 *
 * @since 5.1
 */
public class JaxWsMetroSpecifics extends JaxWsImplementorSpecifics {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsMetroSpecifics.class);
  private JaxWsClientPoolingHelper m_poolingHelper;

  @Override
  @PostConstruct
  protected void initConfig() {
    super.initConfig();
    m_implementorContextProperties.put(PROP_SOCKET_CONNECT_TIMEOUT, "com.sun.xml.ws.connect.timeout"); // com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT
    m_implementorContextProperties.put(PROP_SOCKET_READ_TIMEOUT, "com.sun.xml.ws.request.timeout"); // com.sun.xml.ws.developer.JAXWSProperties.REQUEST_TIMEOUT
    m_poolingHelper = new JaxWsClientPoolingHelper("com.sun.xml.ws.client.sei.SEIStub");
  }

  @Override
  public String getVersionInfo() {
    try {
      final Package pck = Class.forName("com.sun.xml.ws.util.RuntimeVersion").getPackage();
      return String.format("JAX-WS Metro %s (http://jax-ws.java.net, %s, not bundled with JRE)", pck.getImplementationVersion(), pck.getImplementationVendor());
    }
    catch (final ClassNotFoundException e) { // NOSONAR
      throw new PlatformException("Application configured to run with JAX-WS Metro (not bundled with JRE), but implementor could not be found on classpath.");
    }
  }

  @Override
  public void clearHttpResponseCode(Map<String, Object> ctx) {
    // The response context used by Metro is a read-only view on the received data. Hence clearing any value is not required at all.
  }

  @Override
  public void closeSocket(final Object port, final String operation) {
    try {
      ((Closeable) Proxy.getInvocationHandler(port)).close();
    }
    catch (final Exception e) {
      LOG.error("Failed to close Socket for: {}", operation, e);
    }
  }

  /**
   * Uses the <code>resetRequestContext</code> method provided by <code>com.sun.xml.ws.client.sei.SEIStub</code>.
   */
  @Override
  public void resetRequestContext(Object port) {
    if (!m_poolingHelper.resetRequestContext(port)) {
      super.resetRequestContext(port);
    }
  }

  @Override
  public boolean isValid(Object port) {
    return m_poolingHelper.isValid(port);
  }

  @Override
  public boolean isPoolingSupported() {
    return m_poolingHelper.isPoolingSupported();
  }

  @Override
  public void interceptWebServiceRequestRejected(final MessageContext messageContext, final int httpStatusCode) throws WebServiceRequestRejectedException {
    // SECURITY: Exit with an exception because JAX-WS METRO (v2.2.10) does not exit call chain upon returning 'false' for one-way communication requests.
    throw new WebServiceRequestRejectedException(httpStatusCode);
  }
}
