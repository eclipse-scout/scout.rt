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
import java.util.concurrent.Callable;

import org.eclipse.scout.commons.FinalValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;

/**
 * default implementation used to wrap around an interface using a java proxy {@link InvocationHandler}
 */
public class BeanProxyImplementor<T> implements InvocationHandler {
  private final IBean<T> m_bean;
  private final IBeanDecorator<T> m_interceptor;
  private final Class[] m_types;
  private final T m_proxy;
  private final FinalValue<T> m_beanInstance;

  @SuppressWarnings("unchecked")
  public BeanProxyImplementor(IBean<T> bean, IBeanDecorator<T> interceptor, Class... types) {
    m_bean = bean;
    m_interceptor = interceptor;
    m_types = types;
    m_beanInstance = new FinalValue<>();
    m_proxy = (T) Proxy.newProxyInstance(BeanProxyImplementor.class.getClassLoader(), m_types, this);
  }

  public T getProxy() {
    return m_proxy;
  }

  @Override
  public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
    final T instance = m_beanInstance.setIfAbsent(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return m_bean.getInstance();
      }
    });

    if ("hashCode".equals(method.getName()) && (args == null || args.length == 0) && method.getParameterTypes().length == 0) {
      if (instance == null) {
        return hashCode();
      }
      return instance.hashCode();
    }
    else if ("equals".equals(method.getName()) && args != null && args.length == 1 && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Object.class)) {
      return m_proxy == args[0];
    }
    else if ("toString".equals(method.getName()) && (args == null || args.length == 0) && method.getParameterTypes().length == 0) {
      StringBuilder b = new StringBuilder();
      b.append("{proxy} ");
      if (instance != null) {
        b.append(instance.toString());
      }
      else {
        b.append(Arrays.toString(m_types));
      }
      return b.toString();
    }

    IBeanInvocationContext<T> ic = new IBeanInvocationContext<T>() {
      @Override
      public IBean<T> getTargetBean() {
        return m_bean;
      }

      @Override
      public T getTargetObject() {
        return instance;
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
      public Object proceed() {
        try {
          return method.invoke(instance, args);
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
