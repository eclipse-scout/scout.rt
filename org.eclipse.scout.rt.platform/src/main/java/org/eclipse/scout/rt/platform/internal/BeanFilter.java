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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * extract {@link Bean} annotated classes
 */
public class BeanFilter {
  private static final Logger LOG = LoggerFactory.getLogger(BeanFilter.class);

  private IClassInventory m_classInventory;
  private Set<Class> m_allBeans;

  /**
   * @return all {@link Bean} annotated classes
   * <p>
   * Includes all classes that implement an interface that has a {@link Bean} annotation
   */
  public Set<Class> collect(IClassInventory classInventory) {
    m_classInventory = classInventory;
    m_allBeans = new HashSet<>();

    // 1. collect all annotations annotated with @Bean and register all classes that are directly annotated with @Bean
    Set<IClassInfo> annotationsWithBeanAnnotation = new HashSet<>();
    Set<IClassInfo> candidates = new HashSet<>();
    for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(Bean.class)) {
      if (ci.isAnnotation()) {
        annotationsWithBeanAnnotation.add(ci);
      }
      else {
        candidates.add(ci);
      }
    }

    // 2. get all candidates that are somehow annotated with @Bean
    for (IClassInfo annotation : annotationsWithBeanAnnotation) {
      try {
        candidates.addAll(classInventory.getKnownAnnotatedTypes(annotation));
      }
      catch (Exception e) {
        LOG.warn("Could not resolve known annotated types for [{}]", annotation.name(), e);
      }
    }

    // find all classes that are somehow annotated with @Bean
    candidates
        .parallelStream()
        .forEach(this::collectWithSubClasses);

    return m_allBeans;
  }

  /**
   * @param ci
   */
  private void collectWithSubClasses(IClassInfo ci) {
    if (ci.isEnum() || ci.isAnnotation() || ci.isSynthetic()) {
      LOG.warn("Skipping bean candidate '{}' because it is no supported class type (enum, annotation, anonymous class).", ci.name());
      return;
    }

    collect(ci);

    if (!ci.isFinal()) {
      try {
        Set<IClassInfo> allKnownSubClasses = m_classInventory.getAllKnownSubClasses(ci);
        for (IClassInfo subClass : allKnownSubClasses) {
          collect(subClass);
        }
      }
      catch (Exception e) {
        LOG.warn("Could not resolve known sub classes of [{}]", ci.name(), e);
      }
    }
  }

  private void collect(IClassInfo ci) {
    if (ci.hasAnnotation(IgnoreBean.class)) {
      LOG.debug("Skipping bean candidate '{}' because it is annotated with '{}'.", ci.name(), IgnoreBean.class.getSimpleName());
      return;
    }
    if (!ci.isInstanciable()) {
      LOG.debug("Skipping bean candidate '{}' because it is not instanciable.", ci.name());
      return;
    }
    if (!(ci.hasNoArgsConstructor() || ci.hasInjectableConstructor())) {
      LOG.warn("Skipping bean candidate '{}' because it has no empty or injectable constructor().", ci.name());
      return;
    }
    addBean(ci);
  }

  private void addBean(IClassInfo ci) {
    try {
      Class<?> clazz = ci.resolveClass();
      synchronized (m_allBeans) {
        m_allBeans.add(clazz);
      }
    }
    catch (Exception ex) {
      LOG.warn("Could not resolve class [{}]", ci.name(), ex);
    }
  }
}
