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
        .withStatus(Response.Status.BAD_REQUEST)
        .withMessage(Response.Status.BAD_REQUEST.getReasonPhrase()) // do not return internal exception message
        .build();
  }
}
