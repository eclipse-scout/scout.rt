/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bean
public class VetoExceptionMapper extends AbstractExceptionMapper<VetoException> {
  private static final Logger LOG = LoggerFactory.getLogger(VetoExceptionMapper.class);

  @Override
  public Response toResponseImpl(VetoException exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage());
    return createResponse(exception);
  }

  protected Response createResponse(VetoException exception) {
    // Veto Exception is thrown if access is denied, but may also in other circumstances (like failed validation, missing item, etc.).
    // Since we cannot distinguish them at the moment, always use forbidden status code.
    // We should consider using status codes for veto exceptions so they can be mapped to a HTTP status code.
    return Response.status(Status.FORBIDDEN).build();
  }
}
