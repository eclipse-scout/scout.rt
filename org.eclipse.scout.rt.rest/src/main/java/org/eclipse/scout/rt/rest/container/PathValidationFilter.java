/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.container;

import static jakarta.ws.rs.core.Response.status;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.scout.rt.platform.util.PathValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container request filter that validates the path of the request using the {@link PathValidator}. The filter is
 * applied globally, but only if the incoming request has been matched to a particular resource by JAX-RS runtime.
 */
@Priority(Priorities.AUTHENTICATION) // should be one of the first post-matching filters to get executed
public class PathValidationFilter implements IRestContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(PathValidationFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    UriInfo uriInfo = requestContext.getUriInfo();
    if (uriInfo == null) {
      return;
    }

    String path = uriInfo.getPath();
    if (!PathValidator.isValid(path)) {
      LOG.info("Request with invalid path detected: '{}'. Parent paths are not allowed by default. To change this behavior replace {}.", path, PathValidator.class);

      requestContext.abortWith(status(BAD_REQUEST).build());
    }
  }
}
