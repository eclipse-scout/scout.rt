/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.exception;

import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingExceptionMapper extends AbstractExceptionMapper<ProcessingException> {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessingExceptionMapper.class);

  @Override
  public Response toResponseImpl(ProcessingException exception) {
    LOG.error("Exception occured while processing rest request", exception);
    return createResponse(exception);
  }

  protected Response createResponse(ProcessingException exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withStatus(Response.Status.INTERNAL_SERVER_ERROR)
        .withCode(exception.getStatus().getCode())
        .withMessage(defaultErrorMessage())
        .build();
  }

  protected String defaultErrorMessage() {
    return DefaultExceptionMapper.DEFAULT_ERROR_MESSAGE;
  }
}
