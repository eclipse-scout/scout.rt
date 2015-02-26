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
package org.eclipse.scout.rt.platform.cdi.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.interceptor.AroundInvoke;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.IInterceptedBean;
import org.eclipse.scout.rt.platform.cdi.interceptor.InvocationContext;
import org.eclipse.scout.rt.platform.cdi.interceptor.internal.SimpleInvocationContext;

/**
 *
 */
public class InterceptedBean<T> extends Bean<T> implements IInterceptedBean<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InterceptedBean.class);
  private IBean<T> m_interceptedBean;
  private Object m_interceptor;

  public InterceptedBean(IBean<T> bean, Object interceptor) {
    super(bean.getBeanClazz());
    setBeanAnnotations(bean.getBeanAnnotations());
    m_interceptedBean = bean;
    m_interceptor = interceptor;

  }

  @Override
  protected T createNewInstance() {
    T instance = getInteceptedBean().get();
    List<Method> interceptorMethods = new ArrayList<Method>();
    // read around invoke methods
    for (Method m : getInterceptor().getClass().getDeclaredMethods()) {
      if (m.getAnnotation(AroundInvoke.class) != null) {
        interceptorMethods.add(m);
      }
    }
    if (interceptorMethods.size() == 1) {
      instance = (T) Proxy.newProxyInstance(getBeanClazz().getClassLoader(),
          BeanUtility.getInterfacesHierarchy(getBeanClazz(), Object.class),
          new P_ProxyInvocationHandler(instance, getInterceptor(), interceptorMethods.get(0)));
    }
    else if (interceptorMethods.size() > 1) {
      throw new IllegalArgumentException(String.format("Interceptors are allowed to provide exactly one method annotated with AroundInvoke. '%s' has more than 1!", getInterceptor().getClass()));
    }

    return instance;
  }

  @Override
  public final boolean isIntercepted() {
    return true;
  }

  public Object getInterceptor() {
    return m_interceptor;
  }

  @Override
  public IBean<T> getInteceptedBean() {
    return m_interceptedBean;
  }

  private class P_ProxyInvocationHandler implements InvocationHandler {

    private final Object m_target;
    private final Object m_interceptor;
    private Method m_interceptorMethod;

    private P_ProxyInvocationHandler(Object target, Object interceptor, Method interceptorMethod) {
      m_target = target;
      m_interceptor = interceptor;
      m_interceptorMethod = interceptorMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Class<?>[] parameterTypes = m_interceptorMethod.getParameterTypes();
      if (parameterTypes.length == 0) {
        return m_interceptorMethod.invoke(m_interceptor);
      }
      else if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(InvocationContext.class)) {
        SimpleInvocationContext context = new SimpleInvocationContext(m_target, method, method, args);
        Map<String, Object> contextData = new HashMap<>();
        contextData.put(IBean.class.getName(), InterceptedBean.this);
        context.setContextData(contextData);
        try {
          return m_interceptorMethod.invoke(m_interceptor, context);
        }
        catch (Exception e) {
          Throwable toLog = e;
          if (e.getCause() != null) {
            toLog = e.getCause();
          }
          LOG.error(String.format("Error during interceptor '%s' for method '%s' on '%s'", getInterceptor().getClass().getName(), method.getName(), m_target.getClass().getName()), toLog);
          return null;
        }
      }
      else {
        throw new IllegalArgumentException(String.format("Interceptor method can have only zero or one argument which is assignable from %s. Method '%s' of '%s' has wrong arguments!", InvocationContext.class.getName(), m_interceptorMethod, m_interceptor.getClass().getName()));
      }

    }
  }
}
