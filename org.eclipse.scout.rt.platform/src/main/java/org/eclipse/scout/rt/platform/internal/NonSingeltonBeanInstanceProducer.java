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

import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.InjectBean;
import org.eclipse.scout.rt.platform.exception.BeanCreationException;

/**
 * This producers creates always a new instance for the given bean. It is used as default producer except for the ones
 * annotated with {@link ApplicationScoped}.
 * <p>
 * Instances returned are created and fully initialized with respect to:
 * <ul>
 * <li>Annotation {@link InjectBean} on constructor parameters
 * <li>Annotation {@link InjectBean} on class fields
 * <li>Annotation {@link InjectBean} on initializer methods parameters
 * <li>Annotation {@link PostConstruct} on initializer methods
 * </ul>
 * <p>
 * It ensures that circular dependencies are detected and reported by throwing a {@link BeanCreationException}.
 * <p>
 * This class is thread safe. Concurrent invocations of {@link #produce(IBean)} create multiple instances.
 * <p>
 * <b>Important:</b> Beans are discarded without any clean-up operations if the creation process throws any exception.
 * The implementer of a bean's constructor and its {@link PostConstruct}-annotated methods is responsible for proper
 * disposal of already bound resources.
 *
 * @see SingeltonBeanInstanceProducer
 */
public class NonSingeltonBeanInstanceProducer<T> implements IBeanInstanceProducer<T> {

  /** Lazy initialized; No thread safety required */
  private Supplier<? extends T> m_beanInstanceCreator;

  @Override
  public T produce(IBean<T> bean) {
    Class<? extends T> beanClazz = bean.getBeanClazz();
    if (m_beanInstanceCreator == null) {
      m_beanInstanceCreator = BeanInstanceUtil.beanInstanceCreator(beanClazz);
    }
    return BeanInstanceUtil.createAndAssertNoCircularDependency(m_beanInstanceCreator, beanClazz);
  }
}
