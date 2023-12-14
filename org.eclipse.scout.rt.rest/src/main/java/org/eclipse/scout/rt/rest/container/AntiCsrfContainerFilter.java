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
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

import java.io.IOException;
import java.net.URI;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.rest.csrf.AntiCsrfHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter prevents CSRF attacks on REST services.
 *
 * @see AntiCsrfHelper
 */
@Priority(Priorities.AUTHENTICATION) // should be one of the first post-matching filters to get executed
public class AntiCsrfContainerFilter implements IRestContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(AntiCsrfContainerFilter.class);
  private final LazyValue<AntiCsrfHelper> m_requestWithHelper = new LazyValue<>(AntiCsrfHelper.class);

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (m_requestWithHelper.get().isValidRequest(requestContext)) {
      return;
    }

    UriInfo uriInfo = requestContext.getUriInfo();
    URI path = uriInfo == null ? null : uriInfo.getRequestUri();
    LOG.info("Request '{}' blocked because no '{}' header was present (possible CSRF attack).", path, AntiCsrfHelper.REQUESTED_WITH_HEADER);

    requestContext.abortWith(status(FORBIDDEN).build());
  }
}
