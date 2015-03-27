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
package org.eclipse.scout.rt.platform.interceptor.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;

/**
 * default implementation used to wrap around an interface using a java proxy {@link InvocationHandler}
 */
public class BeanProxyImplementor<T> implements InvocationHandler {
  private final IBean<T> m_bean;
  private final IBeanInterceptor<T> m_interceptor;
  private final T m_impl;
  private final Class[] m_types;
  private final T m_proxy;

  @SuppressWarnings("unchecked")
  public BeanProxyImplementor(IBean<T> bean, IBeanInterceptor<T> interceptor, T impl, Class... types) {
    m_bean = bean;
    m_interceptor = interceptor;
    m_impl = impl;
    m_types = types;
    m_proxy = (T) Proxy.newProxyInstance(impl.getClass().getClassLoader(), m_types, this);
  }

  public T getImpl() {
    return m_impl;
  }

  public T getProxy() {
    return m_proxy;
  }

  @Override
  public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
    switch (method.getName()) {
      case "hashCode": {
        return (m_impl != null) ? m_impl.hashCode() : m_proxy.hashCode();
      }
      case "equals": {
        return m_proxy == args[0];
      }
      case "toString": {
        return "{proxy}" + ((m_impl != null) ? m_impl.toString() : Arrays.toString(m_types));
      }
    }

    IBeanInvocationContext<T> ic = new IBeanInvocationContext<T>() {
      @Override
      public IBean<T> getTargetBean() {
        return m_bean;
      }

      @Override
      public T getTargetObject() {
        return m_impl;
      }

      @Override
      public Method getTargetMethod() {
        return method;
      }

      @Override
      public Object[] getTargetArgs() {
        return args;
      }

      @Override
      public Object proceed() throws ProcessingException {
        try {
          return method.invoke(m_impl, args);
        }
        catch (IllegalAccessException e) {
          throw new ProcessingException("access denied", e);
        }
        catch (IllegalArgumentException e) {
          throw new ProcessingException("argument mismatch", e);
        }
        catch (InvocationTargetException e) {
          Throwable t = e.getTargetException();
          if (t instanceof ProcessingException) {
            throw (ProcessingException) t;
          }
          if (t instanceof PlatformException) {
            throw (PlatformException) t;
          }
          throw new ProcessingException("unexpected", t);
        }
      }
    };
    return m_interceptor.invoke(ic);
  }

}
