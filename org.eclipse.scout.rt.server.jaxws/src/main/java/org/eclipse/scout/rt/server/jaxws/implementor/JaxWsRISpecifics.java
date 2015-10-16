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
 * JAX-WS implementor specifics of 'JAX-WS Reference Implementation' as contained in JDK.
 *
 * @since 5.1
 */
public class JaxWsRISpecifics extends JaxWsImplementorSpecifics {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsRISpecifics.class);

  @Override
  public void setSocketConnectTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put("com.sun.xml.internal.ws.connect.timeout", timeoutMillis);
    }
    else {
      requestContext.remove("com.sun.xml.internal.ws.connect.timeout");
    }
  }

  @Override
  public void setSocketReadTimeout(final Map<String, Object> requestContext, final int timeoutMillis) {
    if (timeoutMillis > 0) {
      requestContext.put("com.sun.xml.internal.ws.request.timeout", timeoutMillis);
    }
    else {
      requestContext.remove("com.sun.xml.internal.ws.request.timeout");
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
}
