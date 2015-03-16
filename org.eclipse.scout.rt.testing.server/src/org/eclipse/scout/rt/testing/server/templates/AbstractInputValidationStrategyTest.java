/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.templates;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.server.DefaultTransactionDelegate;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test to find all service methods, where an input validation strategy annotation is missing. <br/>
 * <br/>
 * A scout server checks the input of a service method call. The checks/validations are defined/collected in a strategy
 * ({@link IValidationStrategy}). There are two main strategies, {@link IValidationStrategy.QUERY} and
 * {@link IValidationStrategy.PROCESS}. Use the query strategy if the service method does only reads else use process.
 * The strategy is either defined by an annotation or by a policy according to the method name.
 */
@RunWith(PlatformTestRunner.class)
// TODO [dwi]: ask jgu whether to remove
public abstract class AbstractInputValidationStrategyTest {

  protected abstract String[] getConfiguredSharedBundles();

  protected abstract String[] getConfiguredServerBundles();

  @Test
  public void validateServices() throws Exception {
    Set<Method> collector = new HashSet<Method>();
    for (IService service : SERVICES.getServices(IService.class)) {
      if (service == null) {
        throw new Exception("Service is null. Test started too early or some services failed to initialize.");
      }
      Class<?> serviceClass = service.getClass();
      if (Proxy.isProxyClass(serviceClass)) {
        continue;
      }
      checkServiceClass(serviceClass, collector);
    }
    report(collector);
  }

  protected void checkServiceClass(Class<?> serviceClass, Set<Method> collector) throws Exception {
    Collection<Class<?>> interfacesHierarchy = new ArrayList<Class<?>>();
    for (Class<?> ci : ServiceUtility.getInterfacesHierarchy(serviceClass, Object.class)) {
      interfacesHierarchy.add(ci);
    }

    for (Method m : serviceClass.getDeclaredMethods()) {
      if (m.getParameterTypes().length == 0 || !Modifier.isPublic(m.getModifiers())) {
        continue;
      }
      else if (hasStrategyByPolicy(m)) {
        continue;
      }
      else {
        checkStrategyByAnnotation(serviceClass, interfacesHierarchy, m, collector);
      }
    }
  }

  protected boolean hasStrategyByPolicy(Method m) throws Exception {
    // check by policy
    String name = m.getName();
    return DefaultTransactionDelegate.DEFAULT_QUERY_NAMES_PATTERN.matcher(name).matches() ||
        DefaultTransactionDelegate.DEFAULT_PROCESS_NAMES_PATTERN.matcher(name).matches();
  }

  protected void checkStrategyByAnnotation(Class<?> serviceClass, Collection<Class<?>> interfacesHierarchy, Method base, Set<Method> collector) throws Exception {
    // logic from DefaultTransactionDelegate
    // -> either service implementing class or any super class contains an annotation
    Class<?> c = serviceClass;
    while (c != null) {
      if (c.getAnnotation(RemoteServiceAccessDenied.class) != null || c.getAnnotation(InputValidation.class) != null) {
        return;
      }
      try {
        Method m = c.getDeclaredMethod(base.getName(), base.getParameterTypes());
        if ((m.getAnnotation(InputValidation.class) != null || m.getAnnotation(RemoteServiceAccessDenied.class) != null)) {
          return;
        }
      }
      catch (NoSuchMethodException e) {
        //nop
      }
      c = c.getSuperclass();
    }
    // -> or each interface declaring the method has an annotation
    for (Class<?> ci : interfacesHierarchy) {
      if (ci.getAnnotation(RemoteServiceAccessDenied.class) == null && ci.getAnnotation(InputValidation.class) == null) {
        try {
          Method m = ci.getDeclaredMethod(base.getName(), base.getParameterTypes());
          if (m.getAnnotation(RemoteServiceAccessDenied.class) == null && m.getAnnotation(InputValidation.class) == null) {
            collector.add(m);
          }
        }
        catch (NoSuchMethodException e) {
          //nop
        }
      }
    }
  }

  protected void report(Set<Method> collector) throws Exception {
    if (!collector.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (Method m : collector) {
        builder.append("\n");
        builder.append(m.getDeclaringClass().getName());
        builder.append(".");
        builder.append(m.getName());
      }
      throw new Exception("Missing validation strategy on: " + builder.toString());
    }
  }
}
