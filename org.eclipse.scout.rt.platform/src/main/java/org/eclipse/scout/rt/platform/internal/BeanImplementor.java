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
package org.eclipse.scout.rt.platform.internal;

import java.lang.annotation.Annotation;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BeanCreationException;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;
import org.eclipse.scout.rt.platform.interceptor.internal.BeanProxyImplementor;

public class BeanImplementor<T> implements IBean<T> {
  private static final ThreadLocal<Deque<String>> INSTANTIATION_STACK = new ThreadLocal<>();

  private final Class<? extends T> m_beanClazz;
  private final Map<Class<? extends Annotation>, Annotation> m_beanAnnotations;
  private final Semaphore m_instanceLock = new Semaphore(1, true);
  private final T m_initialInstance;
  private BeanManagerImplementor m_beanManager;
  private T m_instance;

  @SuppressWarnings("unchecked")
  public BeanImplementor(BeanMetaData beanData, BeanManagerImplementor beanManager) {
    m_beanClazz = (Class<? extends T>) Assertions.assertNotNull(beanData.getBeanClazz());
    m_beanAnnotations = new HashMap<Class<? extends Annotation>, Annotation>(Assertions.assertNotNull(beanData.getBeanAnnotations()));
    m_initialInstance = (T) beanData.getInitialInstance();
    if (m_initialInstance != null && getBeanAnnotation(ApplicationScoped.class) == null) {
      throw new IllegalArgumentException(String.format("Instance constructor only allows application scoped instances. Class '%s' does not have the '%s' annotation.", getBeanClazz().getName(), ApplicationScoped.class.getName()));
    }
    m_instance = m_initialInstance;
    m_beanManager = beanManager;
  }

  @Override
  public Class<? extends T> getBeanClazz() {
    return m_beanClazz;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotationClazz) {
    return (ANNOTATION) m_beanAnnotations.get(annotationClazz);
  }

  @Override
  public Map<Class<? extends Annotation>, Annotation> getBeanAnnotations() {
    return new HashMap<Class<? extends Annotation>, Annotation>(m_beanAnnotations);
  }

  @Override
  public T getInitialInstance() {
    return m_initialInstance;
  }

  @Override
  public T getInstance(Class<T> queryType) {
    T instance = getRawInstance();
    IBeanDecorationFactory deco = m_beanManager.getBeanDecorationFactory();
    if (deco != null && queryType.isInterface()) {
      IBeanInterceptor<T> interceptor = deco.decorate(this, queryType);
      if (interceptor != null) {
        instance = new BeanProxyImplementor<T>(this, interceptor, instance, queryType).getProxy();
      }
    }
    return instance;
  }

  protected T getRawInstance() {
    if (m_instance != null) {
      return m_instance;
    }
    if (getBeanClazz().isInterface()) {
      return null;
    }

    Deque<String> stack = INSTANTIATION_STACK.get();
    String beanName = getBeanClazz().getName();
    if (stack != null && stack.contains(beanName)) {
      String message = String.format("The requested bean is currently being created. Creation path: [%s]", CollectionUtility.format(stack, ", "));
      throw new BeanCreationException(beanName, message);
    }

    if (BeanManagerImplementor.isApplicationScoped(this)) {
      m_instanceLock.acquireUninterruptibly();
      try {
        if (m_instance == null) {
          m_instance = createNewInstance();
        }
        return m_instance;
      }
      finally {
        m_instanceLock.release();
      }
    }
    return createNewInstance();
  }

  /**
   * @returns Returns a new instance of the bean managed by this registration.
   */
  protected T createNewInstance() {
    Class<? extends T> beanClass = getBeanClazz();
    Deque<String> stack = INSTANTIATION_STACK.get();
    boolean removeStack = false;
    if (stack == null) {
      stack = new LinkedList<>();
      INSTANTIATION_STACK.set(stack);
      removeStack = true;
    }

    try {
      stack.addLast(beanClass.getName());
      return BeanInstanceUtil.create(beanClass);
    }
    finally {
      if (removeStack) {
        INSTANTIATION_STACK.remove();
      }
      else {
        stack.removeLast();
      }
    }
  }

  protected void dispose() {
    m_instance = null;
    m_beanManager = null;
  }

  @Override
  public int hashCode() {
    return m_beanClazz.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("IBean[");
    for (Annotation a : getBeanAnnotations().values()) {
      buf.append("@");
      buf.append(a.annotationType().getSimpleName());
      buf.append(" ");
    }
    buf.append(getBeanClazz().getName());
    buf.append("]");
    return buf.toString();
  }
}
