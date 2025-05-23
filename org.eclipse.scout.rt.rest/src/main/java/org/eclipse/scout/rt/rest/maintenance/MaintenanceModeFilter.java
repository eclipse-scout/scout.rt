/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.maintenance;

import java.io.IOException;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for maintenance mode, if maintenance mode is enabled for application (see {@link #isMaintenanceMode()}) and no special permission is granted (see {@link #isMaintenanceModeLoginPermissionGranted()}) the filter aborts the request
 * with {@link Response.Status#SERVICE_UNAVAILABLE}
 */
@Priority(Priorities.AUTHORIZATION)
public class MaintenanceModeFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(MaintenanceModeFilter.class);

  protected static final String DEFAULT_MAINTENANCE_MESSAGE = "Resources are temporarily unavailable due to maintenance.";

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (!isMaintenanceMode()) {
      // no maintenance mode
      return;
    }

    if (isMaintenanceModeLoginPermissionGranted()) {
      // still granted even though maintenance mode is active
      return;
    }

    LOG.debug("Abort HTTP request '{} {}' with status code {}, because application is in maintenance mode", requestContext.getMethod(), Optional.ofNullable(requestContext.getUriInfo()).map(UriInfo::getPath).orElse(null), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    requestContext.abortWith(BEANS.get(ErrorResponseBuilder.class).withHttpStatus(Response.Status.SERVICE_UNAVAILABLE).withMessage(getMaintenanceMessage()).build());
  }

  /**
   * @see IMaintenanceModeService#isActive()
   */
  protected boolean isMaintenanceMode() {
    return BEANS.get(IMaintenanceModeService.class).isActive();
  }

  /**
   * @see IMaintenanceModeService#isGrantOverride()
   */
  protected boolean isMaintenanceModeLoginPermissionGranted() {
    return BEANS.get(IMaintenanceModeService.class).isGrantOverride();
  }

  /**
   * Message for declined requests.
   */
  protected String getMaintenanceMessage() {
    return DEFAULT_MAINTENANCE_MESSAGE;
  }
}
