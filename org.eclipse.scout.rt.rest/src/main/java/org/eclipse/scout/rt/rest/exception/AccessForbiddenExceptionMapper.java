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

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;

public class AccessForbiddenExceptionMapper extends AbstractVetoExceptionMapper<AccessForbiddenException> {

  @Override
  protected ErrorResponseBuilder createErrorResponseBuilder(AccessForbiddenException exception) {
    return super.createErrorResponseBuilder(exception)
        .withHttpStatus(Response.Status.FORBIDDEN);
  }
}
