/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;
import org.eclipse.scout.rt.platform.util.Assertions;

import io.opentelemetry.api.trace.Tracer;

@ApplicationScoped
public class ServiceUtility {

  /**
   * @return the reflective service operation that can be called using {@link #invoke(Object, Method, Object[])}
   */
  public Method getServiceOperation(Class<?> serviceClass, String operation, Class<?>[] paramTypes) {
    Assertions.assertNotNull(serviceClass, "service class is null");
    try {
      return serviceClass.getMethod(operation, paramTypes);
    }
    catch (NoSuchMethodException | SecurityException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  /**
   * Invokes the given operation on the service.
   *
   * @return the result of the service invocation.
   * @throws RuntimeException
   *           if the service invocation failed. Hence, runtime exceptions are propagated, any other exception is
   *           translated into {@link PlatformException}.
   */
  @SuppressWarnings("squid:S1181") // Throwable and Error should not be caught
  public Object invoke(final Object service, final Method operation, final Object[] args) {
    Assertions.assertNotNull(service, "service is null");
    Assertions.assertNotNull(operation, "operation is null");

    Tracer tracer = BEANS.get(ITracingHelper.class).createTracer(ServiceUtility.class);
    return BEANS.get(ITracingHelper.class).wrapInSpan(tracer, service.getClass().getSimpleName(), span -> {
      span.setAttribute("scout.server.service.name", service.getClass().getName());
      span.setAttribute("scout.server.service.operation", operation.getName());
      try {
        return operation.invoke(service, args != null ? args : new Object[0]);
      }
      catch (final Throwable t) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(t);
      }
    });
  }
}
