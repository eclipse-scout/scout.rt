/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import java.util.Set;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.api.data.ApiExposedHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.rest.RestApplicationScope;
import org.eclipse.scout.rt.rest.RestApplicationScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of dynamic feature registering {@link ApiExposedFilter} for annotated methods/classes
 */
public abstract class AbstractApiExposedFeature implements DynamicFeature {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractApiExposedFeature.class);

  private final ApiExposedHelper m_apiExposedHelper = BEANS.get(ApiExposedHelper.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    // contributors may provide their own filters instead of this one or even skip filter registration
    for (IApiExposedFeatureFilterContributor f : BEANS.all(IApiExposedFeatureFilterContributor.class)) {
      if (f.configure(resourceInfo, context)) {
        LOG.debug("Do not install {} for {} (handled by {})", ApiExposedFilter.class.getSimpleName(), resourceInfo, f.getClass().getSimpleName());
        return;
      }
    }

    ApiExposed annotation = getApiExposedAnnotation(resourceInfo, context);

    // compute if operation is *not* api-exposed, if *not* exposed then install filter to restrict access
    if (annotation == null || !annotation.value()) {
      ApiExposedFilter filter = BEANS.get(ApiExposedFilter.class);
      LOG.debug("Install {} for {}", ApiExposedFilter.class.getSimpleName(), resourceInfo + (annotation == null ? " (no annotation present)" : ""));
      context.register(filter);
    }
    else {
      LOG.debug("Do not install {} for {}", ApiExposedFilter.class.getSimpleName(), resourceInfo);
    }
  }

  /**
   * Get {@link ApiExposed} annotation; if provided on type and method, method wins.
   */
  protected ApiExposed getApiExposedAnnotation(ResourceInfo resourceInfo, FeatureContext context) {
    return m_apiExposedHelper.getApiExposedAnnotation(resourceInfo.getResourceMethod());
  }

  public static class ApiExposedFeatureContributor implements RestApplication.IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return Set.of(ApiApiExposedFeature.class);
    }
  }

  @Bean
  public interface IApiExposedFeatureFilterContributor {
    boolean configure(ResourceInfo resourceInfo, FeatureContext context);
  }

  @RestApplicationScope(RestApplicationScopes.API)
  public static class ApiApiExposedFeature extends AbstractApiExposedFeature {
  }
}
