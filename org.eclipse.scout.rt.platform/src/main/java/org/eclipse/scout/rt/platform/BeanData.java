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
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;

public class BeanData<T> {
  private final Class<? extends T> m_beanClazz;
  private final Object m_initialInstance;
  private final Map<Class<? extends Annotation>, Annotation> m_beanAnnotations;

  public BeanData(Class<? extends T> clazz) {
    this(clazz, null);
  }

  public BeanData(Class<? extends T> clazz, Object initialInstance) {
    m_beanClazz = Assertions.assertNotNull(clazz);
    m_beanAnnotations = new HashMap<>();
    readStaticAnnoations(clazz, false);
    m_initialInstance = initialInstance;
  }

  public BeanData(IBean<T> template) {
    m_beanClazz = Assertions.assertNotNull(template.getBeanClazz());
    m_beanAnnotations = new HashMap<>(template.getBeanAnnotations());
    m_initialInstance = template.getInitialInstance();
  }

  /**
   * @return
   */
  protected void readStaticAnnoations(Class<?> clazz, boolean inheritedOnly) {
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

  public Class<? extends T> getBeanClazz() {
    return m_beanClazz;
  }

  public Object getInitialInstance() {
    return m_initialInstance;
  }

  @SuppressWarnings("unchecked")
  public <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotationClazz) {
    synchronized (m_beanAnnotations) {
      return (ANNOTATION) m_beanAnnotations.get(annotationClazz);
    }
  }

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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + CollectionUtility.hashCode(m_beanAnnotations.values());
    result = prime * result + m_beanClazz.hashCode();
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
    BeanData other = (BeanData) obj;
    if (!CollectionUtility.equalsCollection(m_beanAnnotations.values(), other.m_beanAnnotations.values())) {
      return false;
    }
    if (!m_beanClazz.equals(other.m_beanClazz)) {
      return false;
    }
    return true;
  }
}
