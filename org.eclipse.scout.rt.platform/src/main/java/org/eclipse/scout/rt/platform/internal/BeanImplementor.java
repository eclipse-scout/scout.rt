/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.util.Assertions;

public class BeanImplementor<T> implements IBean<T> {
  private final Class<? extends T> m_beanClazz;
  private final Map<Class<? extends Annotation>, Annotation> m_beanAnnotations;
  private final T m_initialInstance;
  private IBeanInstanceProducer<T> m_producer;

  @SuppressWarnings("unchecked")
  public BeanImplementor(BeanMetaData beanData) {
    m_beanClazz = (Class<? extends T>) Assertions.assertNotNull(beanData.getBeanClazz());
    m_beanAnnotations = new HashMap<Class<? extends Annotation>, Annotation>(Assertions.assertNotNull(beanData.getBeanAnnotations()));
    m_initialInstance = (T) beanData.getInitialInstance();
    if (m_initialInstance != null && getBeanAnnotation(ApplicationScoped.class) == null) {
      throw new IllegalArgumentException(String.format("Instance constructor only allows application scoped instances. Class '%s' does not have the '%s' annotation.", getBeanClazz().getName(), ApplicationScoped.class.getName()));
    }
    if (beanData.getProducer() != null) {
      m_producer = (IBeanInstanceProducer<T>) beanData.getProducer();
    }
    else if (!m_beanClazz.isInterface()) {
      m_producer = new DefaultBeanInstanceProducer<T>();
    }
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
  public T getInstance() {
    if (m_initialInstance == null) {
      if (m_producer == null) {
        return null;
      }
      return m_producer.produce(this);
    }
    return m_initialInstance;
  }

  @Override
  public IBeanInstanceProducer<T> getBeanInstanceProducer() {
    return m_producer;
  }

  protected void dispose() {
    m_producer = null;
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
