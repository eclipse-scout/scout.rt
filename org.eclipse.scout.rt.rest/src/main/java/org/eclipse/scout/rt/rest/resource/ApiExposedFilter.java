/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import java.io.IOException;
import java.net.URI;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Dynamically installed request filter that rejects proxied requests to methods which are not marked as @{@link ApiExposed}.
 *
 * @see AbstractApiExposedFeature
 */
@Priority(Priorities.AUTHORIZATION)
@ApplicationScoped
public class ApiExposedFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(ApiExposedFilter.class);

  public static final String HTTP_HEADER_NAME = "X-Scout-Proxied-Request";

  protected static final String DEFAULT_FORBIDDEN_MESSAGE = "Forbidden";

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String proxiedRequestHeader = requestContext.getHeaderString(HTTP_HEADER_NAME);
    if (proxiedRequestHeader == null) {
      // request is not proxied (= no header set)
      LOG.debug("Access allowed, request is not proxied: {}", getRequestPath(requestContext));
      return;
    }

    // not allowed
    LOG.atLevel(Platform.get().inDevelopmentMode() ? Level.WARN : Level.DEBUG).log("External access to {} not allowed, reason: class/method is not marked with {}", getRequestPath(requestContext), ApiExposed.class.getSimpleName());
    requestContext.abortWith(BEANS.get(ErrorResponseBuilder.class).withHttpStatus(Response.Status.FORBIDDEN).withMessage(getForbiddenMessage(requestContext)).build());
  }

  protected URI getRequestPath(ContainerRequestContext requestContext) {
    UriInfo uriInfo = requestContext.getUriInfo();
    return uriInfo == null ? null : uriInfo.getRequestUri();
  }

  /**
   * Message for declined requests.
   */
  public String getForbiddenMessage(ContainerRequestContext requestContext) {
    return DEFAULT_FORBIDDEN_MESSAGE;
  }
}
