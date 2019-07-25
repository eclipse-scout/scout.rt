/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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

public abstract class AbstractVetoExceptionMapper<E extends VetoException> extends AbstractExceptionMapper<E> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractVetoExceptionMapper.class);

  @Override
  public Response toResponseImpl(E exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage());
    return createErrorResponseBuilder(exception).build();
  }

  protected ErrorResponseBuilder createErrorResponseBuilder(E exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withStatus(Response.Status.BAD_REQUEST)
        .withTitle(exception.getStatus().getTitle())
        .withMessage(exception.getStatus().getBody())
        .withCode(exception.getStatus().getCode());
  }
}
