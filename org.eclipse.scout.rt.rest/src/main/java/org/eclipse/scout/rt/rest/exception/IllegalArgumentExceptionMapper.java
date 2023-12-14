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
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ExceptionMapper} transforming {@link IllegalArgumentException} into {@link Response.Status.BAD_REQUEST}
 */
public class IllegalArgumentExceptionMapper extends AbstractExceptionMapper<IllegalArgumentException> {

  private static final Logger LOG = LoggerFactory.getLogger(IllegalArgumentExceptionMapper.class);

  @Override
  protected Response toResponseImpl(IllegalArgumentException exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
    return BEANS.get(ErrorResponseBuilder.class)
        .withHttpStatus(Response.Status.BAD_REQUEST)
        .withMessage(Response.Status.BAD_REQUEST.getReasonPhrase()) // do not return internal exception message
        .build();
  }
}
