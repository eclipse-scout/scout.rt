/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.container;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.scout.rt.platform.util.PathValidator;

/**
 * Container request filter that validates the path of the request using the {@link PathValidator}. The filter is
 * applied globally, but only if the incoming request has been matched to a particular resource by JAX-RS runtime.
 */
@Priority(Priorities.AUTHENTICATION) // should be one of the first post-matching filters to get executed
public class PathValidationFilter implements IRestContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    UriInfo uriInfo = requestContext.getUriInfo();
    if (uriInfo == null) {
      return;
    }

    if (!PathValidator.isValid(uriInfo.getPath())) {
      requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
    }
  }
}
