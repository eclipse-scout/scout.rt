/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.exception;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

@Priority(1) // override Jackson JsonMappingExceptionMapper implementation registered in JacksonFeature
public class JsonMappingExceptionMapper extends AbstractExceptionMapper<JsonMappingException> {

  private static final Logger LOG = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

  @Override
  protected Response toResponseImpl(JsonMappingException exception) {
    LOG.info("{}: {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
    return Response.status(Response.Status.BAD_REQUEST).build(); // do not return internal exception message
  }
}
