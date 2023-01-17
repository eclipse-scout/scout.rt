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

import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;

public class VetoExceptionMapper extends AbstractVetoExceptionMapper<VetoException> {

  @Override
  protected ErrorResponseBuilder createErrorResponseBuilder(VetoException exception) {
    return super.createErrorResponseBuilder(exception)
        .withHttpStatus(Response.Status.BAD_REQUEST);
  }
}
