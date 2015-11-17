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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.internal.DefaultBeanInstanceProducer;

/**
 * Bean descriptor holding meta data for an {@link IBean} creation.<br>
 * Use with {@link IBeanManager#registerBean(BeanMetaData)} to register a new {@link IBean}.
 *
 * @since 5.1
 */
public class BeanMetaData {
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
    readStaticAnnotations(clazz, false);
    inheritOrderIfRequired(clazz);
    initProducerAnnotation();
    m_initialInstance = initialInstance;

  }

  private void initProducerAnnotation() {
    BeanProducer annotation = getBeanAnnotation(BeanProducer.class);
    if (annotation != null) {
      Class<? extends IBeanInstanceProducer> producerClass = annotation.value();
      IBeanInstanceProducer producer;
      try {
        producer = producerClass.newInstance();
        withProducer(producer);
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new AssertionException(String.format("Error creating producer instance for %s for bean %s: %s", producerClass, m_beanClazz, e.getMessage()));
      }
    }
  }

  public BeanMetaData(IBean<?> template) {
    m_beanClazz = Assertions.assertNotNull(template.getBeanClazz());
    m_beanAnnotations = new HashMap<>(template.getBeanAnnotations());
    m_initialInstance = template.getInitialInstance();
    m_producer = template.getBeanInstanceProducer();
  }

  protected void inheritOrderIfRequired(Class<?> startClass) {
    boolean isReplaceAnnotationPresent = getBeanAnnotation(Replace.class) != null;
    boolean isOrderAnnotationPresent = getBeanAnnotation(Order.class) != null;
    if (isOrderAnnotationPresent || !isReplaceAnnotationPresent) {
      // don't inherit order annotation because:
      // - we have a specific order defined
      // - or we are not replacing anything
      return;
    }

    // we have no order and are replacing: inherit order from replaced beans
    Class<?> superClass = startClass;
    Order orderAnnot = null;
    while (orderAnnot == null && superClass != null && !Object.class.equals(superClass) && superClass.isAnnotationPresent(Replace.class)) {
      // search for the first @Order up the hierarchy as long as beans are replaced
      orderAnnot = superClass.getAnnotation(Order.class);
      superClass = superClass.getSuperclass();
    }

    // if no order has been found so far, check if the last level (which no longer has a replace annotation) has one
    if (orderAnnot == null && superClass != null) {
      orderAnnot = superClass.getAnnotation(Order.class);
    }

    if (orderAnnot != null) {
      withAnnotation(orderAnnot);
    }
  }

  /**
   * Adds the annotations present on the given class to the annotation map.
   *
   * @param clazz
   * @param inheritedOnly
   */
  protected void readStaticAnnotations(Class<?> clazz, boolean inheritedOnly) {
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
    readStaticAnnotations(clazz.getSuperclass(), true);
    for (Class<?> ifc : clazz.getInterfaces()) {
      readStaticAnnotations(ifc, true);
    }
  }

  /**
   * @return the bean {@link Class}.
   */
  public Class<?> getBeanClazz() {
    return m_beanClazz;
  }

  /**
   * @return the initial instance of the bean.
   */
  public Object getInitialInstance() {
    return m_initialInstance;
  }

  /**
   * set the initial instance
   *
   * @return this supporting the fluent api
   * @throws IllegalArgumentException
   *           if argument initialInstance is not null and is not an instance of the bean class.
   */
  public BeanMetaData withInitialInstance(Object initialInstance) {
    if (initialInstance != null && !m_beanClazz.isInstance(initialInstance)) {
      throw new IllegalArgumentException(String.format("InitialInstance '%s' is not of type '%s'", initialInstance, m_beanClazz));
    }
    m_initialInstance = initialInstance;
    return this;
  }

  /**
   * set the instance producer if a special handling is needed. Default is {@link DefaultBeanInstanceProducer}
   *
   * @return this supporting the fluent api
   */
  public BeanMetaData withProducer(IBeanInstanceProducer<?> producer) {
    m_producer = producer;
    return this;
  }

  /**
   * @return The {@link IBeanInstanceProducer} responsible for instance creations in this bean.
   */
  public IBeanInstanceProducer<?> getProducer() {
    return m_producer;
  }

  /**
   * convenience set or reset the {@link Replace} annotation
   *
   * @return this supporting the fluent api
   */
  public BeanMetaData withReplace(boolean replace) {
    if (replace) {
      withAnnotation(AnnotationFactory.createReplace());
    }
    else {
      withoutAnnotation(Replace.class);
    }
    return this;
  }

  /**
   * convenience set or reset the {@link Order} annotation
   *
   * @return this supporting the fluent api
   */
  public BeanMetaData withOrder(double order) {
    withAnnotation(AnnotationFactory.createOrder(order));
    return this;
  }

  /**
   * convenience set or remove reset the {@link ApplicationScoped} annotation
   *
   * @return this supporting the fluent api
   */
  public BeanMetaData withApplicationScoped(boolean set) {
    if (set) {
      withAnnotation(AnnotationFactory.createApplicationScoped());
    }
    else {
      withoutAnnotation(ApplicationScoped.class);
    }
    return this;
  }

  /**
   * Gets the {@link Annotation} instance for the given {@link Annotation} {@link Class}.
   *
   * @param annotation
   *          The {@link Annotation} {@link Class} to search.
   * @return The {@link Annotation} instance if this annotation exists for this {@link BeanMetaData} or
   *         <code>null</code> otherwise.
   */
  @SuppressWarnings("unchecked")
  public <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotationClazz) {
    return (ANNOTATION) m_beanAnnotations.get(annotationClazz);
  }

  /**
   * Gets a {@link Map} holding all {@link Annotation}s of this {@link BeanMetaData}.
   *
   * @return A {@link Map} with the {@link Annotation} class as key and the {@link Annotation} instance as value.
   */
  public Map<Class<? extends Annotation>, Annotation> getBeanAnnotations() {
    return new HashMap<Class<? extends Annotation>, Annotation>(m_beanAnnotations);
  }

  /**
   * Replaces all annotations in this {@link BeanMetaData} with the ones provided.
   *
   * @param annotations
   *          The new annotations.
   */
  public void setBeanAnnotations(Map<Class<? extends Annotation>, Annotation> annotations) {
    m_beanAnnotations.clear();
    m_beanAnnotations.putAll(annotations);
  }

  /**
   * add an annotation
   *
   * @return this supporting the fluent api
   */
  public BeanMetaData withAnnotation(Annotation annotation) {
    m_beanAnnotations.put(annotation.annotationType(), annotation);
    return this;
  }

  /**
   * Adds all {@link Annotation}s in the given {@link Collection}.
   * 
   * @param annotations
   *          The {@link Annotation}s to add.
   * @return this
   */
  public BeanMetaData withAnnotations(Collection<Annotation> annotations) {
    if (CollectionUtility.hasElements(annotations)) {
      for (Annotation a : annotations) {
        withAnnotation(a);
      }
    }
    return this;
  }

  /**
   * remove an annotation
   *
   * @return this supporting the fluent api
   */
  public BeanMetaData withoutAnnotation(Class<? extends Annotation> annotationType) {
    m_beanAnnotations.remove(annotationType);
    return this;
  }
}
