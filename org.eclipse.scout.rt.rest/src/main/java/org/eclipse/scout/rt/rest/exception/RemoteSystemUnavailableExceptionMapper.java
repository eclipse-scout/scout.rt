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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ExceptionMapper} transforming {@link RemoteSystemUnavailableException} into
 * {@link Response.Status#SERVICE_UNAVAILABLE}
 */
public class RemoteSystemUnavailableExceptionMapper extends AbstractExceptionMapper<RemoteSystemUnavailableException> {

  private static final Logger LOG = LoggerFactory.getLogger(RemoteSystemUnavailableExceptionMapper.class);

  @Override
  public Response toResponseImpl(RemoteSystemUnavailableException exception) {
    LOG.warn("{}: {}", exception.getClass().getSimpleName(), exception.getMessage());
    return createResponse(exception);
  }

  protected Response createResponse(RemoteSystemUnavailableException exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withHttpStatus(Response.Status.SERVICE_UNAVAILABLE)
        .withMessage(exception.getMessage())
        .build();
  }
}
