/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.cancellation;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;

/**
 * Abstract REST resource providing cancellation support.
 *
 * @see RestRequestCancellationRegistry
 * @see RestRequestCancellationClientRequestFilter
 */
@Path("cancellation")
public abstract class AbstractCancellationResource implements IRestResource {

  @PUT
  @Path("{requestId}")
  public void cancel(@PathParam("requestId") String requestId) {
    BEANS.get(RestRequestCancellationRegistry.class).cancel(requestId, resolveCurrentUserId());
  }

  /**
   * Returns the user id of the current user. May be {@code null}.
   */
  protected abstract Object resolveCurrentUserId();
}
