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
 * JAX-WS implementor specifics of 'JAX-WS Reference Implementation' as contained in JDK.
 *
 * @since 5.1
 */
public class JaxWsRISpecifics extends JaxWsImplementorSpecifics {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsRISpecifics.class);
  private JaxWsClientPoolingHelper m_poolingHelper;

  @Override
  @PostConstruct
  protected void initConfig() {
    super.initConfig();
    m_implementorContextProperties.put(PROP_SOCKET_CONNECT_TIMEOUT, "com.sun.xml.internal.ws.connect.timeout"); // com.sun.xml.internal.ws.developer.JAXWSProperties.CONNECT_TIMEOUT
    m_implementorContextProperties.put(PROP_SOCKET_READ_TIMEOUT, "com.sun.xml.internal.ws.request.timeout"); // com.sun.xml.internal.ws.developer.JAXWSProperties.REQUEST_TIMEOUT
    m_poolingHelper = new JaxWsClientPoolingHelper("com.sun.xml.internal.ws.client.sei.SEIStub");
  }

  @Override
  public String getVersionInfo() {
    try {
      final Class<?> versionClass = Class.forName("com.sun.xml.internal.ws.util.RuntimeVersion");
      final String version = versionClass.getDeclaredField("VERSION").get(null).toString();

      return String.format("%s (http://jax-ws.java.net, %s, bundled with JRE)", version, versionClass.getPackage().getImplementationVendor());
    }
    catch (final ClassNotFoundException e) {
      throw new PlatformException("Application configured to run with JAX-WS RI (bundled with JRE), but implementor could not be found on classpath.");
    }
    catch (final ReflectiveOperationException e) {
      LOG.warn("Failed to read version information of JAX-WS implementor", e);
      return "JAX-WS RI bundled with JRE";
    }
  }

  @Override
  public void clearHttpResponseCode(Map<String, Object> ctx) {
    // The response context used by the JAX-WS RI is a read-only view on the received data. Hence clearing any value is not required at all.
  }

  @Override
  public void closeSocket(final Object port, final String operation) {
    try {
      ((Closeable) Proxy.getInvocationHandler(port)).close();
    }
    catch (final Throwable e) {
      LOG.error("Failed to close Socket for: {}", operation, e);
    }
  }

  /**
   * Uses the <code>resetRequestContext</code> method provided by
   * <code>com.sun.xml.internal.ws.client.sei.SEIStub</code>. The implementation falls back to the generic reset
   * strategy provided by the super class (e.g. if JAX-WS RI does not provide the resetRequestContext method because it
   * is too old).
   */
  @Override
  public void resetRequestContext(final Object port) {
    if (!m_poolingHelper.resetRequestContext(port)) {
      super.resetRequestContext(port);
    }
  }

  @Override
  public boolean isValid(final Object port) {
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
