/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client.proxy;

import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;

/**
 * REST client exception handler that extracts {@link ErrorDo} from the error {@link Response}.
 */
@ApplicationScoped
public class ErrorDoRestClientExceptionTransformer extends AbstractEntityRestClientExceptionTransformer {

  @Override
  protected RuntimeException transformEntityForbidden(RuntimeException cause, Response response) {
    ErrorDo error = response.readEntity(ErrorResponse.class).getError();
    return new VetoException(error.getMessage(), cause).withTitle(error.getTitle());
  }

  @Override
  protected RuntimeException transformEntityError(RuntimeException cause, Response response) {
    ErrorDo error = response.readEntity(ErrorResponse.class).getError();
    return new ProcessingException(error.getMessage(), cause).withTitle(error.getTitle());
  }
}
