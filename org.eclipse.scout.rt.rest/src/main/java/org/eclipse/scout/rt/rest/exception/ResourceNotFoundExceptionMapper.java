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

import org.eclipse.scout.rt.dataobject.exception.ResourceNotFoundException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;

public class ResourceNotFoundExceptionMapper extends AbstractVetoExceptionMapper<ResourceNotFoundException> {

  @Override
  protected ErrorResponseBuilder createErrorResponseBuilder(ResourceNotFoundException exception) {
    return super.createErrorResponseBuilder(exception)
        .withHttpStatus(Response.Status.NOT_FOUND);
  }
}
