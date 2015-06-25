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
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.internal.DefaultBeanInstanceProducer;

public class BeanMetaData implements IBeanMetaData {
  private final Class<?> m_beanClazz;
  private final Map<Class<? extends Annotation>, Annotation> m_beanAnnotations;
  private Object m_initialInstance;
  private IBeanInstanceProducer<?> m_producer;

  public BeanMetaData(Class<?> clazz) {
    this(clazz, null);
  }

  public BeanMetaData(Class<?> clazz, Object initialInstance) {
    m_beanClazz = Assertions.assertNotNull(clazz);
    m_beanAnnotations = new HashMap<>();
    readStaticAnnoations(clazz, false);
    m_initialInstance = initialInstance;
  }

  public BeanMetaData(IBean<?> template) {
    m_beanClazz = Assertions.assertNotNull(template.getBeanClazz());
    m_beanAnnotations = new HashMap<>(template.getBeanAnnotations());
    m_initialInstance = template.getInitialInstance();
    m_producer = template.getBeanInstanceProducer();
  }

  /**
   * @return
   */
  protected void readStaticAnnoations(Class<?> clazz, boolean inheritedOnly) {
    if (clazz == null || Object.class.equals(clazz)) {
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
    for (Class<?> ifc : clazz.getInterfaces()) {
      readStaticAnnoations(ifc, true);
    }
  }

  @Override
  public Class<?> getBeanClazz() {
    return m_beanClazz;
  }

  @Override
  public Object getInitialInstance() {
    return m_initialInstance;
  }

  /**
   * set the initial instance
   *
   * @return this supporting the fluent api
   */
  @Override
  public BeanMetaData initialInstance(Object initialInstance) {
    m_initialInstance = initialInstance;
    return this;
  }

  /**
   * set the instance producer if a special handling is needed. Default is {@link DefaultBeanInstanceProducer}
   *
   * @return this supporting the fluent api
   */
  @Override
  public BeanMetaData producer(IBeanInstanceProducer<?> producer) {
    m_producer = producer;
    return this;
  }

  @Override
  public IBeanInstanceProducer<?> getProducer() {
    return m_producer;
  }

  /**
   * convenience set or reset the {@link Replace} annotation
   *
   * @return this supporting the fluent api
   */
  @Override
  public BeanMetaData replace(boolean set) {
    if (set) {
      addAnnotation(AnnotationFactory.createReplace());
    }
    else {
      removeAnnotation(Replace.class);
    }
    return this;
  }

  /**
   * convenience set or reset the {@link Order} annotation
   *
   * @return this supporting the fluent api
   */
  @Override
  public BeanMetaData order(double order) {
    addAnnotation(AnnotationFactory.createOrder(order));
    return this;
  }

  /**
   * convenience set or remove reset the {@link ApplicationScoped} annotation
   *
   * @return this supporting the fluent api
   */
  @Override
  public BeanMetaData applicationScoped(boolean set) {
    if (set) {
      addAnnotation(AnnotationFactory.createApplicationScoped());
    }
    else {
      removeAnnotation(ApplicationScoped.class);
    }
    return this;
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
  public void setBeanAnnotations(Map<Class<? extends Annotation>, Annotation> annotations) {
    m_beanAnnotations.clear();
    m_beanAnnotations.putAll(annotations);
  }

  /**
   * add an annotation
   *
   * @return this supporting the fluent api
   */
  @Override
  public BeanMetaData addAnnotation(Annotation annotation) {
    m_beanAnnotations.put(annotation.annotationType(), annotation);
    return this;
  }

  /**
   * remove an annotation
   *
   * @return this supporting the fluent api
   */
  @Override
  public IBeanMetaData removeAnnotation(Class<? extends Annotation> annotationType) {
    m_beanAnnotations.remove(annotationType);
    return this;
  }

}
