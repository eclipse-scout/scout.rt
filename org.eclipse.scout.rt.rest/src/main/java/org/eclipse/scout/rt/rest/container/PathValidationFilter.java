/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.container;

import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

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
