/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.container;

import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

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
