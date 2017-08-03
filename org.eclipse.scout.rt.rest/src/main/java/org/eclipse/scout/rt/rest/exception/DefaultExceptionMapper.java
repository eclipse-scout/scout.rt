/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the exceptions not mapped by another more specific exception mapper. Logs the exception with severity error
 * and returns HTTP status 500. Exceptions of type {@link WebApplicationException} are ignored.
 * <p>
 * Without this mapper the exception would not be logged and tomcat would return a HTML document containing the
 * exception message. It is true that exceptions provoked by the client (e.g. invalid json) should not be logged. This
 * is still the case, because such exceptions are handled by other exception mappers or are of type
 * {@link WebApplicationException} which won't be logged. Every other exception has to be logged in order to understand
 * the exception cause. But it must not be sent to the client due to security reason.
 */
@Bean
public class DefaultExceptionMapper extends AbstractExceptionMapper<Exception> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);

  @Override
  public Response toResponseImpl(Exception exception) {
    LOG.error("Exception occured while processing rest request", exception);
    return createResponse(exception);
  }

  protected Response createResponse(Exception exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }
}
