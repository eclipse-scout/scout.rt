/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.proxy;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;

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
