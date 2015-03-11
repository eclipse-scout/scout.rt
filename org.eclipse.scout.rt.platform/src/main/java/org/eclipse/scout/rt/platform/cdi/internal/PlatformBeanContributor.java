/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.cdi.internal;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;

/**
 * auto find all {@link Bean} annotated classes
 */
public class PlatformBeanContributor implements IBeanContributor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PlatformBeanContributor.class);

  @Override
  public void contributeBeans(IClassInventory classInventory, IBeanContext context) {
    Set<Class<?>> allBeans = new HashSet<>();

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

    // 2. register all classes that are indirectly annotated with @Bean
    for (IClassInfo annotation : beanAnnotations) {
      try {
        for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(annotation.resolveClass())) {
          collectWithSubClasses(classInventory, ci, allBeans);
        }
      }
      catch (ClassNotFoundException e) {
        LOG.warn("Error loading class '" + annotation.name() + "' with flags 0x" + Integer.toHexString(annotation.flags()), e);
      }
    }

    // 3. add all classes to the context
    for (Class<?> bean : allBeans) {
      context.registerClass(bean);
    }
  }

  /**
   * @param ci
   */
  private void collectWithSubClasses(IClassInventory classInventory, IClassInfo ci, Set<Class<?>> collector) {
    if (ci.isEnum() || ci.isAnnotation() || ci.isSynthetic() || !ci.isPublic()) {
      LOG.debug("Skipping bean candidate '{0}' because it is no supported class type (enum, annotation, anonymous class) or is not public.", ci.name());
      return;
    }

    collect(ci, collector);

    if (!ci.isFinal()) {
      try {
        Set<IClassInfo> allKnownSubClasses = classInventory.getAllKnownSubClasses(ci.resolveClass());
        for (IClassInfo subClass : allKnownSubClasses) {
          collect(subClass, collector);
        }
      }
      catch (ClassNotFoundException e) {
        LOG.warn("Error loading class '" + ci.name() + "' with flags 0x" + Integer.toHexString(ci.flags()), e);
      }
    }
  }

  private void collect(IClassInfo ci, Set<Class<?>> collector) {
    try {
      if (ci.isAbstract() || ci.isInterface()) {
        LOG.debug("Skipping bean candidate '{0}' because it is abstract or an interface.", ci.name());
        return;
      }

      if (!ci.hasNoArgsConstructor()) {
        LOG.debug("Skipping bean candidate '{0}' because no default constructor could be found.", ci.name());
        return;
      }

      Class<?> clazz = ci.resolveClass();
      // top level or static inner
      if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
        LOG.debug("Skipping bean candidate '{0}' because it is a non static inner class.", ci.name());
        return;
      }

      collector.add(clazz);
    }
    catch (ClassNotFoundException ex) {
      LOG.warn("Error loading class '" + ci.name() + "' with flags 0x" + Integer.toHexString(ci.flags()), ex);
    }
  }
}
