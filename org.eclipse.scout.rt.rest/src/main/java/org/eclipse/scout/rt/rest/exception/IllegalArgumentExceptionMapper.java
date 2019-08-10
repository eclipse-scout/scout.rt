/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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
