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

import java.util.function.BiFunction;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.dataobject.exception.ResourceNotFoundException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;

/**
 * REST client exception handler that extracts {@link ErrorDo} from the error {@link Response} if available.
 * Furthermore, service unavailable, bad gateway and gateway timeout are transformed into a
 * {@link RemoteSystemUnavailableException}.
 */
@ApplicationScoped
public class ErrorDoRestClientExceptionTransformer extends AbstractEntityRestClientExceptionTransformer {

  @Override
  protected RuntimeException transformByResponseStatus(Response.Status status, RuntimeException e, Response response) {
    switch (status) {
      case FORBIDDEN:
        return transformClientError(e, response, AccessForbiddenException::new);
      case NOT_FOUND:
        return transformClientError(e, response, ResourceNotFoundException::new);
      case BAD_GATEWAY:
      case SERVICE_UNAVAILABLE:
      case GATEWAY_TIMEOUT:
        return transformUnavailableResponse(e, response);
    }
    return super.transformByResponseStatus(status, e, response);
  }

  @Override
  protected RuntimeException transformByResponseStatusFamily(Response.Status.Family family, RuntimeException e, Response response) {
    switch (family) {
      case CLIENT_ERROR:
        return transformClientError(e, response, VetoException::new);
    }
    return super.transformByResponseStatusFamily(family, e, response);
  }

  protected RuntimeException transformClientError(RuntimeException e, Response response, BiFunction<String, RuntimeException, VetoException> vetoExceptionFactory) {
    return safeTransformEntityErrorResponse(e, response,
        () -> buildProcessingException(e, response, vetoExceptionFactory, response.readEntity(ErrorResponse.class).getError()),
        () -> buildProcessingException(e, response, vetoExceptionFactory, null));
  }

  @Override
  protected RuntimeException defaultTransform(RuntimeException e, Response response) {
    return safeTransformEntityErrorResponse(e, response,
        () -> buildProcessingException(e, response, ProcessingException::new, response.readEntity(ErrorResponse.class).getError()),
        () -> buildProcessingException(e, response, ProcessingException::new, null));
  }

  protected ProcessingException buildProcessingException(RuntimeException e, Response response, BiFunction<String, RuntimeException, ? extends ProcessingException> exceptionFactory, ErrorDo error) {
    if (error != null) {
      return exceptionFactory.apply(error.getMessage(), e)
          .withTitle(error.getTitle())
          .withCode(error.getErrorCodeAsInt())
          .withSeverity(error.getSeverityAsInt());
    }

    // ErrorDo might be missing, if a ServletFilter aborts the request with an e.g. forbidden http status code.
    // The servlet container might then return a generic json response which does not contain a Scout ErrorDo.
    // Such a response could look like: {"servlet":"ServletName","message":"Forbidden","url":"/api/","status":"403"}
    StatusType statusInfo = response.getStatusInfo();
    return exceptionFactory.apply("REST call failed: " + statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase(), e);
  }

  protected RuntimeException transformUnavailableResponse(RuntimeException e, Response response) {
    return new RemoteSystemUnavailableException("Server temporarily not available", e);
  }
}
