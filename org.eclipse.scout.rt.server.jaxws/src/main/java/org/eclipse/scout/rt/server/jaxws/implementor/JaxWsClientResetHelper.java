/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.implementor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for resetting JAX-WS RI and Metro client objects so that they can be reused.
 *
 * @since 6.0.300
 */
public class JaxWsClientResetHelper {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsClientResetHelper.class);
  private final Method m_method;

  public JaxWsClientResetHelper(String className, String methodName) {
    Method method = lookupMethod(className, methodName);
    m_method = method;
  }

  /**
   * @return Returns <code>true</code> if the given port has been reset. Otherwise <code>false</code>.
   */
  public boolean resetRequestContext(Object port) {
    Assertions.assertNotNull(port);
    if (m_method == null) {
      return false;
    }
    if (!Proxy.isProxyClass(port.getClass())) {
      return false;
    }
    Object seiStub = Proxy.getInvocationHandler(port);
    try {
      m_method.invoke(seiStub);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
    return true;
  }

  /**
   * @return Returns the given no-arg method or null, if it does not exist.
   */
  protected Method lookupMethod(String className, String methodName) {
    try {
      Class<?> clazz = Class.forName(className);
      if (clazz == null) {
        LOG.warn("Could not find class [{}]", className);
        return null;
      }
      return clazz.getMethod(methodName);
    }
    catch (ReflectiveOperationException | SecurityException e) {
      LOG.info("Could not find reset method. Falling back to manual request context cleansing. [{}.{}]", className, methodName, e);
    }
    return null;
  }
}
