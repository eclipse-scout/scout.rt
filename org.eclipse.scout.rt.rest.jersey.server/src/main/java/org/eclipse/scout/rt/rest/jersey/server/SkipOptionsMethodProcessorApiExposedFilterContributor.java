/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.resource.AbstractApiExposedFeature.ApiApiExposedFeature;
import org.eclipse.scout.rt.rest.resource.AbstractApiExposedFeature.IApiExposedFeatureFilterContributor;
import org.eclipse.scout.rt.rest.resource.ApiExposedFilter;
import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OptionsMethodProcessor} registers two additional artificial REST operations for each actual REST operations.
 * These artificial REST operations are never annotated with {@link ApiExposed} (all <i>OPTIONS</i> requests would be blocked),
 * therefore ignore these artificial REST operations for the {@link ApiApiExposedFeature} and allow invocation independent of {@link ApiExposed}
 * (that is invocations for these artificial operations are always allowed as also the actual operations may be called possibly resulting
 * in an HTTP forbidden error code).
 */
@Bean
public class SkipOptionsMethodProcessorApiExposedFilterContributor implements IApiExposedFeatureFilterContributor {

  private static final Logger LOG = LoggerFactory.getLogger(SkipOptionsMethodProcessorApiExposedFilterContributor.class);

  @Override
  public boolean configure(ResourceInfo resourceInfo, FeatureContext context) {
    Class<?> resourceClass = resourceInfo.getResourceClass();
    Class<?> enclosingClass = resourceClass.getEnclosingClass();
    if (enclosingClass != null && OptionsMethodProcessor.class.isAssignableFrom(enclosingClass)) {
      LOG.debug("Skip {} check and installation for {}", ApiExposedFilter.class.getSimpleName(), resourceInfo);
      return true;
    }

    return false;
  }
}
