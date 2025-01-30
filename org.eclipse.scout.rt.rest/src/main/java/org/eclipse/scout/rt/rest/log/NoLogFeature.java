/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.log;

import java.util.Collections;
import java.util.Set;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationClassesContributor;

/**
 * Installs the {@link NoLogFilter} for all REST methods that are annotated with &#64;{@link NoLog}.
 */
public class NoLogFeature implements DynamicFeature {

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    NoLog noLogAnnotationMethod = resourceInfo.getResourceMethod().getAnnotation(NoLog.class);
    NoLog noLogAnnotationClass = resourceInfo.getResourceClass().getAnnotation(NoLog.class);

    // Compute if logging is disabled
    boolean noLog = false;
    if (noLogAnnotationMethod != null) {
      noLog = noLogAnnotationMethod.value();
    }
    else if (noLogAnnotationClass != null) {
      noLog = noLogAnnotationClass.value();
    }

    // If not loggable, install a filter that sets the "NoLog" attribute
    if (noLog) {
      context.register(NoLogFilter.class);
    }
  }

  /**
   * Installs the {@link NoLogFilter} into the REST application.
   */
  public static class NoLogFilterFeatureContributor implements IRestApplicationClassesContributor {

    @Override
    public Set<Class<?>> contribute() {
      return Collections.singleton(NoLogFeature.class);
    }
  }
}
