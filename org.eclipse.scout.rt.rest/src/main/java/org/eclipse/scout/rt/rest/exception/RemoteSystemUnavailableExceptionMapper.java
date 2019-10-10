/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ExceptionMapper} transforming {@link RemoteSystemUnavailableException} into
 * {@link Response.Status.SERVICE_UNAVAILABLE}
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
