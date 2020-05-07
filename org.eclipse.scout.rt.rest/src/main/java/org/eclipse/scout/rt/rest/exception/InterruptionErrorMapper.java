/*******************************************************************************
 * Copyright (c) 2020 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;
import org.eclipse.scout.rt.rest.error.ErrorResponseBuilder;

/**
 * Translates errors extending {@link AbstractInterruptionError} into a BAD REQUEST status code.
 */
public class InterruptionErrorMapper extends AbstractExceptionMapper<AbstractInterruptionError> {

  @Override
  protected Response toResponseImpl(AbstractInterruptionError exception) {
    return BEANS.get(ErrorResponseBuilder.class)
        .withStatus(Response.Status.BAD_REQUEST)
        .withTitle("Interrupted")
        .build();
  }
}
