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
    // 1. collect all annotations annotated with @Bean and register all classes that are directly annotated with @Bean
    Set<IClassInfo> beanAnnotations = new HashSet<>();
    for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(Bean.class)) {
      if ((ci.flags() & IClassInfo.ACC_ANNOTATION) != 0) {
        beanAnnotations.add(ci);
      }
      else {
        register(context, ci);
      }
    }

    // 2. register all classes that are indirectly annotated with @Bean
    for (IClassInfo annotation : beanAnnotations) {
      try {
        for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(annotation.resolveClass())) {
          register(context, ci);
        }
      }
      catch (ClassNotFoundException e) {
        LOG.warn("Error loading class", e);
      }
    }
  }

  /**
   * @param ci
   */
  private void register(IBeanContext context, IClassInfo ci) {
    if (ci.isEnum() || ci.isAnnotation() || ci.isSynthetic() || !ci.isPublic()) {
      return;
    }
    try {
      Class<?> clazz = ci.resolveClass();
      // top level or static inner
      if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
        return;
      }
      context.registerClass(clazz);
    }
    catch (ClassNotFoundException ex) {
      LOG.warn("class " + ci.name() + " with flags 0x" + Integer.toHexString(ci.flags()), ex);
    }
  }
}
