/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebApplicationExceptionMapper extends AbstractExceptionMapper<WebApplicationException> {

  private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

  @Override
  public Response toResponseImpl(WebApplicationException exception) {
    LOG.debug("{}: {}", exception.getClass().getSimpleName(), exception.getMessage());
    return createResponse(exception);
  }

  protected Response createResponse(WebApplicationException exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withHttpStatus(exception.getResponse().getStatus())
        .withMessage(exception.getMessage())
        .build();
  }
}
