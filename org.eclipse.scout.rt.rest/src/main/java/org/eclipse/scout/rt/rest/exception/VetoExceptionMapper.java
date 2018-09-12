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
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    return BEANS.get(ErrorResponseBuilder.class)
        .withStatus(Response.Status.FORBIDDEN)
        .withTitle(exception.getStatus().getTitle())
        .withMessage(exception.getStatus().getBody())
        .withCode(exception.getStatus().getCode())
        .build();
  }
}
