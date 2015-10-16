/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * JAX-WS implementor specifics of 'JAX-WS Metro implementation'.
 *
 * @since 5.1
 */
public class JaxWsMetroSpecifics extends JaxWsImplementorSpecifics {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsMetroSpecifics.class);

  @Override
  public void setSocketConnectTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put("com.sun.xml.ws.connect.timeout", timeoutMillis);
    }
    else {
      requestContext.remove("com.sun.xml.ws.connect.timeout");
    }

  }

  @Override
  public void setSocketReadTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put("com.sun.xml.ws.request.timeout", timeoutMillis);
    }
    else {
      requestContext.remove("com.sun.xml.ws.request.timeout");
    }
  }

  @Override
  public void closeSocket(final Object port, final String operation) {
    try {
      ((Closeable) Proxy.getInvocationHandler(port)).close();
    }
    catch (final Throwable e) {
      LOG.error(String.format("Failed to close Socket for: %s", operation), e);
    }
  }

  @Override
  public String getVersionInfo() {
    try {
      final Package pck = Class.forName("com.sun.xml.ws.util.RuntimeVersion").getPackage();
      return String.format("JAX-WS Metro %s (http://jax-ws.java.net, %s, not bundled with JRE)", pck.getImplementationVersion(), pck.getImplementationVendor());
    }
    catch (final ClassNotFoundException e) {
      throw new PlatformException("Application configured to run with JAX-WS Metro (not bundled with JRE), but implementor could not be found on classpath.");
    }
  }
}
