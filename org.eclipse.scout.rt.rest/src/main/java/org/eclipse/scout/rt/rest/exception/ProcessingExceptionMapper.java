/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
        .withMessage(defaultErrorMessage(exception))
        .build();
  }

  protected String defaultErrorMessage(ProcessingException exception) {
    return DefaultExceptionMapper.DEFAULT_ERROR_MESSAGE;
  }
}
