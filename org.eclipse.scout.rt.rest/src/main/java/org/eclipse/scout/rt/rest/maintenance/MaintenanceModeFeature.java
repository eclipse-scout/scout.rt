/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.maintenance;

import java.util.Collections;
import java.util.Set;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationClassesContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Installs the {@link MaintenanceModeFilter} for REST resource types and their methods which are annotated with {@link MaintenanceMode} to check for maintenance mode on access.</p>
 * <p>If the filter is installed for at least one context it is also ensured that an implementation of {@link IMaintenanceModeService} is provided.</p>
 */
public class MaintenanceModeFeature implements DynamicFeature {

  private static final Logger LOG = LoggerFactory.getLogger(MaintenanceModeFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    MaintenanceMode annotation = getMaintenanceModeAnnotation(resourceInfo, context);

    // compute if operation is disabled for maintenance mode, register filter if disabled for maintenance mode
    if (annotation != null && annotation.disabled()) {
      Class<? extends MaintenanceModeFilter> filterClass = getMaintenanceModeFilterClass();
      LOG.debug("Install {} for resource {}", filterClass.getSimpleName(), resourceInfo);
      context.register(filterClass);
      Assertions.assertNotNull(BEANS.opt(IMaintenanceModeService.class), "{} is installed for resource {} (by annotation); however no {} bean implementation is available", filterClass.getSimpleName(), resourceInfo, IMaintenanceModeService.class.getSimpleName());
    }
  }

  protected Class<? extends MaintenanceModeFilter> getMaintenanceModeFilterClass() {
    return MaintenanceModeFilter.class;
  }

  /**
   * Get {@link MaintenanceMode} annotation; if provided on type and method, method wins.
   */
  protected MaintenanceMode getMaintenanceModeAnnotation(ResourceInfo resourceInfo, FeatureContext context) {
    return ObjectUtility.nvlOpt(resourceInfo.getResourceMethod().getAnnotation(MaintenanceMode.class), () -> resourceInfo.getResourceClass().getAnnotation(MaintenanceMode.class));
  }

  /**
   * Installs the {@link MaintenanceModeFeature} into the REST application.
   */
  public static class MaintenanceModeFilterFeatureContributor implements IRestApplicationClassesContributor {

    @Override
    public Set<Class<?>> contribute() {
      return Collections.singleton(MaintenanceModeFeature.class);
    }
  }
}
