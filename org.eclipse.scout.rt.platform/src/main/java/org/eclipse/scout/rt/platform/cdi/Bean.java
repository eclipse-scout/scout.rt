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
package org.eclipse.scout.rt.platform.cdi;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;
import org.eclipse.scout.rt.platform.cdi.internal.BeanInstanceCreator;

/**
 *
 */
public class Bean<T> implements IBean<T> {

  private final Class<? extends T> m_beanClazz;
  private final Map<Class<? extends Annotation>, Annotation> m_beanAnnotations;

  private final boolean m_isInstanceBasedBean;
  private T m_instance;

  public Bean(Class<? extends T> clazz) {
    this(clazz, null, CollectionUtility.<Annotation> emptyArrayList());
  }

  public Bean(T instance) {
    this(instance, CollectionUtility.<Annotation> emptyArrayList());
  }

  @SuppressWarnings("unchecked")
  public Bean(T instance, List<Annotation> beanAnnotations) {
    this((Class<? extends T>) instance.getClass(), instance, beanAnnotations);
  }

  private Bean(Class<? extends T> clazz, T instance, List<Annotation> beanAnnotations) {
    m_beanClazz = Assertions.assertNotNull(clazz);
    m_instance = instance;
    m_isInstanceBasedBean = instance != null;
    m_beanAnnotations = new HashMap<>();
    readStaticAnnoations(clazz, false);
    if (beanAnnotations != null) {
      for (Annotation annotation : beanAnnotations) {
        addAnnotation(annotation);
      }
    }
    if (m_isInstanceBasedBean && getBeanAnnotation(ApplicationScoped.class) == null) {
      throw new IllegalArgumentException(String.format("Instance constructor only allows application scoped instances. Class '%s' does not have the '%s' annotation.", m_beanClazz.getName(), ApplicationScoped.class.getName()));
    }
  }

  /**
   * @return
   */
  private void readStaticAnnoations(Class<?> clazz, boolean inheritedOnly) {
    if (clazz == null || Object.class.getName().equals(clazz.getName())) {
      return;
    }
    for (Annotation a : clazz.getAnnotations()) {
      if (inheritedOnly) {
        if (a.annotationType().getAnnotation(Inherited.class) != null) {
          m_beanAnnotations.put(a.annotationType(), a);
        }
      }
      else {
        m_beanAnnotations.put(a.annotationType(), a);
      }
    }
    readStaticAnnoations(clazz.getSuperclass(), true);
  }

  @Override
  public T get() {
    if (BeanContext.isApplicationScoped(this)) {
      if (getInstance() == null) {
        setInstance(createNewInstance());
      }
      return getInstance();
    }
    else {
      return createNewInstance();
    }
  }

  protected T createNewInstance() {
    return BeanInstanceCreator.create(getBeanClazz());
  }

  public T getInstance() {
    return m_instance;
  }

  void setInstance(T instance) {
    m_instance = instance;
  }

  @Override
  public Class<? extends T> getBeanClazz() {
    return m_beanClazz;
  }

  @SuppressWarnings("unchecked")
  @Override
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

  public void setBeanAnnotations(Map<Class<? extends Annotation>, Annotation> annotations) {
    synchronized (m_beanAnnotations) {
      m_beanAnnotations.clear();
      m_beanAnnotations.putAll(annotations);
    }
  }

  public void addAnnotation(Annotation annotation) {
    synchronized (m_beanAnnotations) {
      m_beanAnnotations.put(annotation.annotationType(), annotation);
    }
  }

  public void removeAnnotation(Annotation annotation) {
    synchronized (m_beanAnnotations) {
      m_beanAnnotations.remove(annotation.annotationType());
    }
  }

  @Override
  public boolean isIntercepted() {
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_beanAnnotations == null) ? 0 : CollectionUtility.hashCode(m_beanAnnotations.values()));
    result = prime * result + ((m_beanClazz == null) ? 0 : m_beanClazz.hashCode());
    result = prime * result + (m_isInstanceBasedBean ? 1231 : 1237);
    if (m_isInstanceBasedBean) {
      result = prime * result + ((m_instance == null) ? 0 : m_instance.hashCode());
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
    Bean other = (Bean) obj;
    if (m_beanAnnotations == null) {
      if (other.m_beanAnnotations != null) {
        return false;
      }
    }
    else if (!CollectionUtility.equalsCollection(m_beanAnnotations.values(), other.m_beanAnnotations.values())) {
      return false;
    }
    if (m_beanClazz == null) {
      if (other.m_beanClazz != null) {
        return false;
      }
    }
    else if (!m_beanClazz.equals(other.m_beanClazz)) {
      return false;
    }
    if (m_isInstanceBasedBean != other.m_isInstanceBasedBean) {
      return false;
    }
    if (m_isInstanceBasedBean) {
      if (m_instance == null) {
        if (other.m_instance != null) {
          return false;
        }
      }
      else if (!m_instance.equals(other.m_instance)) {
        return false;
      }
    }
    return true;
  }
}
