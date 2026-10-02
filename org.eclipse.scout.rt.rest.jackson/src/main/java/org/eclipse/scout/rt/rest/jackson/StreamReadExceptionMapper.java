/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jackson;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.eclipse.scout.rt.rest.exception.AbstractExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.exc.StreamReadException;

/**
 * Override Jackson StreamReadExceptionMapper implementation registered in JacksonFeature
 *
 * @see <a href="https://github.com/FasterXML/jackson-jaxrs-providers/issues/22">jackson-jaxrs-providers - Issue #22</a>
 */
@Priority(1)
public class StreamReadExceptionMapper extends AbstractExceptionMapper<StreamReadException> {

  private static final Logger LOG = LoggerFactory.getLogger(StreamReadExceptionMapper.class);

  @Override
  protected Response toResponseImpl(StreamReadException exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
    return BEANS.get(ErrorResponseBuilder.class)
        .withHttpStatus(Response.Status.BAD_REQUEST)
        .withMessage(Response.Status.BAD_REQUEST.getReasonPhrase()) // do not return internal exception message
        .build();
  }
}
