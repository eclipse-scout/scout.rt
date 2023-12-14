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
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVetoExceptionMapper<E extends VetoException> extends AbstractExceptionMapper<E> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractVetoExceptionMapper.class);

  @Override
  public Response toResponseImpl(E exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage());
    return createErrorResponseBuilder(exception).build();
  }

  protected ErrorResponseBuilder createErrorResponseBuilder(E exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withHttpStatus(Response.Status.BAD_REQUEST)
        .withTitle(exception.getStatus().getTitle())
        .withMessage(exception.getStatus().getBody())
        .withErrorCode(exception.getStatus().getCode())
        .withSeverity(exception.getStatus().getSeverity());
  }
}
