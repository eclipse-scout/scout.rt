/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.rest.IRestResource;

/**
 * REST resource providing cancellation support.
 * <p>
 * The {@link User#currentUserId()} method is used to identify users, subclasses may overwrite the {@link #resolveCurrentUserId()} method to provide a different identifier.
 *
 * @see RestRequestCancellationRegistry
 * @see RestRequestCancellationClientRequestFilter
 */
@Path(CancellationResource.PATH)
public class CancellationResource implements IRestResource {

  public static final String PATH = "cancellation";

  @PUT
  @Path("{requestId}")
  @ApiExposed(false)
  public void cancel(@PathParam("requestId") String requestId) {
    BEANS.get(RestRequestCancellationRegistry.class).cancel(requestId, resolveCurrentUserId());
  }

  /**
   * Returns the user id of the current user. May be {@code null}.
   */
  protected String resolveCurrentUserId() {
    return User.currentUserId();
  }
}
