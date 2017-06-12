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

  /**
   * @return all {@link Bean} annotated classes
   *         <p>
   *         Includes all classes that implement an interface that has a {@link Bean} annotation
   */
  public Set<Class> collect(IClassInventory classInventory) {
    Set<Class> allBeans = new HashSet<>();

    // 1. collect all annotations annotated with @Bean and register all classes that are directly annotated with @Bean
    Set<IClassInfo> beanAnnotations = new HashSet<>();
    for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(Bean.class)) {
      if (ci.isAnnotation()) {
        beanAnnotations.add(ci);
      }
      else {
        collectWithSubClasses(classInventory, ci, allBeans);
      }
    }

    // 2. register all classes that are somehow annotated with @Bean
    for (IClassInfo annotation : beanAnnotations) {
      try {
        for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(annotation)) {
          collectWithSubClasses(classInventory, ci, allBeans);
        }
      }
      catch (Exception e) {
        LOG.warn("Could not resolve known annotated types for [{}]", annotation.name(), e);
      }
    }

    return allBeans;
  }

  /**
   * @param ci
   */
  private void collectWithSubClasses(IClassInventory classInventory, IClassInfo ci, Set<Class> collector) {
    if (ci.isEnum() || ci.isAnnotation() || ci.isSynthetic() || !ci.isPublic()) {
      LOG.debug("Skipping bean candidate '{}' because it is no supported class type (enum, annotation, anonymous class) or is not public.", ci.name());
      return;
    }

    collect(ci, collector);

    if (!ci.isFinal()) {
      try {
        Set<IClassInfo> allKnownSubClasses = classInventory.getAllKnownSubClasses(ci);
        for (IClassInfo subClass : allKnownSubClasses) {
          collect(subClass, collector);
        }
      }
      catch (Exception e) {
        LOG.warn("Could not resolve known sub classes of [{}]", ci.name(), e);
      }
    }
  }

  private void collect(IClassInfo ci, Set<Class> collector) {
    if (ci.hasAnnotation(IgnoreBean.class)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Skipping bean candidate '{}' because it is annotated with '{}'.", ci.name(), IgnoreBean.class.getSimpleName());
      }
      return;
    }
    if (!ci.isInstanciable()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Skipping bean candidate '{}' because it is not instanciable.", ci.name());
      }
      return;
    }
    if (!ci.hasNoArgsConstructor()) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Skipping bean candidate '{}' because it has no empty constructor().", ci.name());
      }
      return;
    }
    try {
      collector.add(ci.resolveClass());
    }
    catch (Exception ex) {
      LOG.warn("Could not resolve class [{}]", ci.name(), ex);
    }
  }
}
