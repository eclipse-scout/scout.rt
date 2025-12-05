/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.api.data.ApiExposedHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.eclipse.scout.rt.rest.resource.AbstractApiExposedFeature.IApiExposedFeatureFilterContributor;
import org.eclipse.scout.rt.rest.resource.ApiExposedFilter;
import org.eclipse.scout.rt.rest.resource.ApiExposedFilter.RejectNonExposedRequestsWithNotFoundConfigProperty;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Priority(Priorities.AUTHORIZATION)
@Bean
public class OptionsMethodProcessorApiExposedFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(OptionsMethodProcessorApiExposedFilter.class);

  @Inject
  private Provider<ExtendedUriInfo> m_extendedUriInfo;

  private final ApiExposedHelper m_apiExposedHelper = BEANS.get(ApiExposedHelper.class);

  @SuppressWarnings("deprecation")
  private final boolean m_rejectRequests = CONFIG.getPropertyValue(RejectNonExposedRequestsWithNotFoundConfigProperty.class);

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    ExtendedUriInfo extendedUriInfo = m_extendedUriInfo.get();

    String proxiedRequestHeader = requestContext.getHeaderString(ApiExposedFilter.HTTP_HEADER_NAME);
    if (proxiedRequestHeader == null) {
      // request is not proxied (= no header set)
      LOG.debug("Access allowed, request is not proxied: {}", extendedUriInfo.getPath());
      return;
    }

    List<Method> actualMethods = extendedUriInfo
        .getMatchedRuntimeResources()
        .stream()
        .map(RuntimeResource::getResourceMethods)
        .flatMap(Collection::stream)
        .map(ResourceMethod::getInvocable)
        .map(Invocable::getHandlingMethod)
        .filter(m -> {
          Class<?> declaringClass = m.getDeclaringClass();
          if (declaringClass == null) {
            return false;
          }

          Class<?> enclosingClass = declaringClass.getEnclosingClass();
          if (enclosingClass == null) {
            return true;
          }

          return !OptionsMethodProcessor.class.isAssignableFrom(enclosingClass);
        })
        .toList();

    if (actualMethods.isEmpty()) {
      // continue, no match
      return;
    }

    if (actualMethods.stream().anyMatch(m_apiExposedHelper::isApiExposed)) {
      // continue, at least one api-exposed method (even though not all may be exposed, OPTIONS request may therefore return too many methods)
      return;
    }

    // not allowed
    boolean increaseLogLevel = !m_rejectRequests || Platform.get().inDevelopmentMode();
    LOG.atLevel(increaseLogLevel ? Level.WARN : Level.DEBUG).log("External access to {} not allowed, reason: classes/methods ({}) are not marked with {}", extendedUriInfo.getPath(), actualMethods, ApiExposed.class.getSimpleName());
    if (m_rejectRequests) {
      requestContext.abortWith(BEANS.get(ErrorResponseBuilder.class).withHttpStatus(Response.Status.NOT_FOUND).withMessage(BEANS.get(ApiExposedFilter.class).getNotFoundMessage(requestContext)).build());
    }
  }

  public static class OptionsMethodProcessorApiExposedFilterContributor implements IApiExposedFeatureFilterContributor {

    @Override
    public boolean configure(ResourceInfo resourceInfo, FeatureContext context) {
      Class<?> resourceClass = resourceInfo.getResourceClass();
      Class<?> enclosingClass = resourceClass.getEnclosingClass();
      if (enclosingClass != null && OptionsMethodProcessor.class.isAssignableFrom(enclosingClass)) {
        LOG.debug("Install {} for {}", OptionsMethodProcessorApiExposedFilter.class.getSimpleName(), resourceInfo);
        // do not use an application-scoped bean here otherwise jakarta.inject won't work
        context.register(BEANS.getBeanManager().getBean(OptionsMethodProcessorApiExposedFilter.class).getBeanClazz());
        return true;
      }

      return false;
    }
  }
}
