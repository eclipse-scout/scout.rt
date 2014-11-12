/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public final class TestingUtility {

  private TestingUtility() {
  }

  /**
   * Wait until the condition returns a non-null result or timeout is reached.
   * <p>
   * When timeout is reached an exception is thrown.
   */
  public static <T> T waitUntil(long timeout, WaitCondition<T> w) throws Throwable {
    long ts = System.currentTimeMillis() + timeout;
    T t = w.run();
    while ((t == null) && System.currentTimeMillis() < ts) {
      Thread.sleep(40);
      t = w.run();
    }
    if (t != null) {
      return t;
    }
    else {
      throw new InterruptedException("timeout reached");
    }
  }

  /**
   * Registers a service on behalf of the given bundle context and returns a map with the created service registration
   * objects.
   * 
   * @param bundleContext
   * @param ranking
   * @param services
   * @return
   */
  public static List<ServiceRegistration> registerServices(Bundle bundle, int ranking, Object... services) {
    ArrayList<ServiceRegistration> result = new ArrayList<ServiceRegistration>();
    Hashtable<String, Object> initParams = new Hashtable<String, Object>();
    initParams.put(Constants.SERVICE_RANKING, ranking);
    for (Object service : services) {
      ServiceRegistration reg = bundle.getBundleContext().registerService(computeServiceNames(service), service, initParams);
      result.add(reg);
      if (Proxy.isProxyClass(service.getClass())) {
        //nop
      }
      else if (service instanceof IService) {
        ((IService) service).initializeService(reg);
      }
    }
    SERVICES.clearCache();
    return result;
  }

  private static String[] computeServiceNames(Object service) {
    ArrayList<String> serviceNames = new ArrayList<String>();
    Class<?> implClass = service.getClass();
    while (implClass != null && implClass != Object.class) {
      serviceNames.add(implClass.getName());
      implClass = implClass.getSuperclass();
    }
    for (Class<?> c : ServiceUtility.getInterfacesHierarchy(service.getClass(), Object.class)) {
      serviceNames.add(c.getName());
    }
    return serviceNames.toArray(new String[serviceNames.size()]);
  }

  /**
   * Unregisters the given services.
   * 
   * @param dynamicServices
   */
  public static void unregisterServices(List<ServiceRegistration> registrationList) {
    if (registrationList == null) {
      return;
    }
    for (ServiceRegistration reg : registrationList) {
      reg.unregister();
    }
    SERVICES.clearCache();
  }

  /**
   * Clears Java's HTTP authentication cache.
   * 
   * @return Returns <code>true</code> if the operation was successful, otherwise <code>false</code>.
   */
  public static boolean clearHttpAuthenticationCache() {
    boolean successful = true;
    try {
      Class<?> c = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
      Field cacheField = c.getDeclaredField("cache");
      cacheField.setAccessible(true);
      Object cache = cacheField.get(null);
      Field hashtableField = cache.getClass().getDeclaredField("hashtable");
      hashtableField.setAccessible(true);
      Map<?, ?> map = (Map<?, ?>) hashtableField.get(cache);
      map.clear();
    }
    catch (Throwable t) {
      successful = false;
    }
    return successful;
  }

}
