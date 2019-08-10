/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client.proxy;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * REST client exception handler that transforms {@link Response.Status#FORBIDDEN} into a {@link VetoException} and all
 * other unsuccessful status into a {@link ProcessingException}.
 */
@ApplicationScoped
public class BasicRestClientExceptionTransformer implements IRestClientExceptionTransformer {

  @Override
  public RuntimeException transform(RuntimeException e, Response response) {
    if (response != null) {
      if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
        return transformForbiddenResponse(e, response);
      }
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        return transformErrorResponse(e, response);
      }
    }
    return e;
  }

  protected RuntimeException transformForbiddenResponse(RuntimeException e, Response response) {
    return new VetoException(response.getStatusInfo().getReasonPhrase())
        .withCode(response.getStatus());
  }

  protected RuntimeException transformErrorResponse(RuntimeException e, Response response) {
    StatusType statusInfo = response.getStatusInfo();
    return new ProcessingException("REST call failed: {} {}", statusInfo.getStatusCode(), statusInfo.getReasonPhrase(), e)
        .withCode(response.getStatus());
  }
}
