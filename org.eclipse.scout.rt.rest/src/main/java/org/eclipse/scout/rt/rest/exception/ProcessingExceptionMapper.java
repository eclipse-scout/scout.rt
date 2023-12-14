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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingExceptionMapper extends AbstractExceptionMapper<ProcessingException> {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessingExceptionMapper.class);

  @Override
  public Response toResponseImpl(ProcessingException exception) {
    LOG.error("Exception occurred while processing REST request", exception);
    return createResponse(exception);
  }

  protected Response createResponse(ProcessingException exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withHttpStatus(Response.Status.INTERNAL_SERVER_ERROR)
        .withErrorCode(exception.getStatus().getCode())
        .withSeverity(exception.getStatus().getSeverity())
        .withMessage(defaultErrorMessage(exception))
        .build();
  }

  protected String defaultErrorMessage(ProcessingException exception) {
    return DefaultExceptionMapper.DEFAULT_ERROR_MESSAGE;
  }
}
