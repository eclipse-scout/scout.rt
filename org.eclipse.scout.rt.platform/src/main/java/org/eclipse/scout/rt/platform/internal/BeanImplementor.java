/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
  private final IBeanInstanceProducer<T> m_producer;
  private boolean m_instanceAvailable;

  /**
   * Creates a {@link BeanImplementor} with the default producer.
   */
  public BeanImplementor(BeanMetaData beanData) {
    this(beanData, getDefaultBeanInstanceProducer(beanData));
  }

  private static <T> IBeanInstanceProducer<T> getDefaultBeanInstanceProducer(BeanMetaData beanData) {
    if (beanData.getBeanAnnotation(ApplicationScoped.class) != null) {
      return new SingeltonBeanInstanceProducer<>();
    }
    return new NonSingeltonBeanInstanceProducer<>();
  }

  @SuppressWarnings("unchecked")
  public BeanImplementor(BeanMetaData beanData, IBeanInstanceProducer<T> beanInstanceProducer) {
    m_beanClazz = (Class<? extends T>) Assertions.assertNotNull(beanData.getBeanClazz());
    m_beanAnnotations = new HashMap<>(Assertions.assertNotNull(beanData.getBeanAnnotations()));
    m_initialInstance = (T) beanData.getInitialInstance();
    m_instanceAvailable = m_initialInstance != null;
    if (m_initialInstance != null && !hasAnnotation(ApplicationScoped.class)) {
      throw new IllegalArgumentException(String.format("Instance constructor only allows application scoped instances. Class '%s' does not have the '%s' annotation.", getBeanClazz().getName(), ApplicationScoped.class.getName()));
    }
    if (beanData.getProducer() != null) {
      m_producer = (IBeanInstanceProducer<T>) beanData.getProducer();
    }
    else if (!m_beanClazz.isInterface()) {
      m_producer = beanInstanceProducer;
    }
    else {
      m_producer = null;
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
  public <ANNOTATION extends Annotation> boolean hasAnnotation(Class<ANNOTATION> annotation) {
    return getBeanAnnotation(annotation) != null;
  }

  @Override
  public Map<Class<? extends Annotation>, Annotation> getBeanAnnotations() {
    return new HashMap<>(m_beanAnnotations);
  }

  @Override
  public T getInitialInstance() {
    return m_initialInstance;
  }

  @Override
  public boolean isInstanceAvailable() {
    return m_instanceAvailable;
  }

  @Override
  public T getInstance() {
    if (m_initialInstance == null) {
      if (m_producer == null) {
        return null;
      }
      T instance = m_producer.produce(this);
      m_instanceAvailable = instance != null;
      return instance;
    }
    return m_initialInstance;
  }

  @Override
  public IBeanInstanceProducer<T> getBeanInstanceProducer() {
    return m_producer;
  }

  @Override
  public int hashCode() {
    return m_beanClazz.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("IBean[");
    for (Annotation a : getBeanAnnotations().values()) {
      buf.append('@');
      buf.append(a.annotationType().getSimpleName());
      buf.append(' ');
    }
    buf.append(getBeanClazz().getName());
    T instance = getInitialInstance();
    if (instance != null) {
      buf.append(" with initial instance ").append(instance.toString());
    }
    buf.append(']');
    return buf.toString();
  }
}
