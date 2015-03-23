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
import org.eclipse.scout.rt.platform.BeanData;
import org.eclipse.scout.rt.platform.IBean;

public class BeanImplementor<T> implements IBean<T> {
  private static final ThreadLocal<Deque<String>> INSTANTIATION_STACK = new ThreadLocal<>();
  private final Class<? extends T> m_beanClazz;
  private final Map<Class<? extends Annotation>, Annotation> m_beanAnnotations;
  private final Semaphore m_instanceLock = new Semaphore(1, true);
  private final T m_initialInstance;
  private T m_instance;

  @SuppressWarnings("unchecked")
  public BeanImplementor(BeanData beanData) {
    m_beanClazz = Assertions.assertNotNull(beanData.getBeanClazz());
    m_beanAnnotations = new HashMap<Class<? extends Annotation>, Annotation>(Assertions.assertNotNull(beanData.getBeanAnnotations()));
    m_initialInstance = (T) beanData.getInitialInstance();
    if (m_initialInstance != null && getBeanAnnotation(ApplicationScoped.class) == null) {
      throw new IllegalArgumentException(String.format("Instance constructor only allows application scoped instances. Class '%s' does not have the '%s' annotation.", getBeanClazz().getName(), ApplicationScoped.class.getName()));
    }
    m_instance = m_initialInstance;
  }

  @Override
  public Class<? extends T> getBeanClazz() {
    return m_beanClazz;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotationClazz) {
    synchronized (m_beanAnnotations) {
      return (ANNOTATION) m_beanAnnotations.get(annotationClazz);
    }
  }

  @Override
  public Map<Class<? extends Annotation>, Annotation> getBeanAnnotations() {
    synchronized (m_beanAnnotations) {
      return new HashMap<Class<? extends Annotation>, Annotation>(m_beanAnnotations);
    }
  }

  @Override
  public T getInitialInstance() {
    return m_initialInstance;
  }

  @Override
  public T createInstance() {
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

    if (BeanContextImplementor.isApplicationScoped(this)) {
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + CollectionUtility.hashCode(m_beanAnnotations.values());
    result = prime * result + m_beanClazz.hashCode();
    if (m_initialInstance != null) {
      result = prime * result + (m_initialInstance != null ? 1231 : 1237);
      result = prime * result + m_initialInstance.hashCode();
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BeanImplementor other = (BeanImplementor) obj;
    if (!CollectionUtility.equalsCollection(m_beanAnnotations.values(), other.m_beanAnnotations.values())) {
      return false;
    }
    if (!m_beanClazz.equals(other.m_beanClazz)) {
      return false;
    }
    if ((m_initialInstance != null) != (other.m_initialInstance != null)) {
      return false;
    }
    if (m_initialInstance != null) {
      if (!m_initialInstance.equals(other.m_initialInstance)) {
        return false;
      }
    }
    return true;
  }
}
