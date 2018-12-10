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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * Base implementation for reading the entity of an unsuccessful REST {@link Response}.
 */
@ApplicationScoped
public abstract class AbstractEntityRestClientExceptionTransformer implements IRestClientExceptionTransformer {

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
    RuntimeException suppressedException = null;
    try {
      if (response.hasEntity()) {
        return transformEntityForbidden(e, response);
      }
    }
    catch (@SuppressWarnings("squid:S1166") javax.ws.rs.ProcessingException | IllegalStateException ex) {
      suppressedException = ex;
    }

    VetoException vetoException = new VetoException(response.getStatusInfo().getReasonPhrase(), e);
    if (suppressedException != null) {
      vetoException.addSuppressed(suppressedException);
    }
    return vetoException;
  }

  protected RuntimeException transformErrorResponse(RuntimeException e, Response response) {
    RuntimeException suppressedException = null;
    try {
      if (response.hasEntity() && MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
        return transformEntityError(e, response);
      }
    }
    catch (@SuppressWarnings("squid:S1166") javax.ws.rs.ProcessingException | IllegalStateException ex) {
      suppressedException = ex;
    }

    StatusType statusInfo = response.getStatusInfo();
    ProcessingException processingException = new ProcessingException("REST call failed: {} {}", statusInfo.getStatusCode(), statusInfo.getReasonPhrase(), e);
    if (suppressedException != null) {
      processingException.addSuppressed(suppressedException);
    }
    return processingException;
  }

  protected abstract RuntimeException transformEntityForbidden(RuntimeException cause, Response response);

  protected abstract RuntimeException transformEntityError(RuntimeException cause, Response response);
}
