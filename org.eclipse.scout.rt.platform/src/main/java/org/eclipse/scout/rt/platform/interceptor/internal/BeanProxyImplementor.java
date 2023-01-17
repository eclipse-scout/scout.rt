/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.interceptor.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.interceptor.DecoratingProxy;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;
import org.eclipse.scout.rt.platform.interceptor.IInstanceInvocationHandler;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * default implementation used to wrap around an interface using a java proxy {@link InvocationHandler}
 */
public class BeanProxyImplementor<T> implements IInstanceInvocationHandler<T> {
  private final IBean<T> m_bean;
  private final IBeanDecorator<T> m_interceptor;
  private final DecoratingProxy<T> m_decoratingProxy;

  public BeanProxyImplementor(IBean<T> bean, IBeanDecorator<T> interceptor, Class... types) {
    m_bean = Assertions.assertNotNull(bean);
    m_interceptor = Assertions.assertNotNull(interceptor);
    m_decoratingProxy = DecoratingProxy.newInstance(this, m_bean::getInstance, types);
  }

  public T getProxy() {
    return m_decoratingProxy.getProxy();
  }

  @Override
  public Object invoke(final T instance, final Method method, final Object[] args) throws Throwable {
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
          // Do not use DefaultRuntimeExceptionTranslator here because it would wrap checked exceptions into a PlatformException.
          // But this method must return an exception of a type that DefaultExceptionTranslator can unwrap again (see DefaultExceptionTranslator#isWrapperException()).
          Throwable originalThrowable = BEANS.get(DefaultExceptionTranslator.class).unwrap(e);
          if (originalThrowable instanceof Error) {
            throw (Error) originalThrowable;
          }
          if (originalThrowable instanceof RuntimeException) {
            throw ((RuntimeException) originalThrowable);
          }
          throw new UndeclaredThrowableException(originalThrowable);
        }
      }
    };
    return m_interceptor.invoke(ic);
  }

}
