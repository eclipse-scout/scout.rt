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

import java.util.function.Supplier;

import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;

/**
 * Base implementation for reading the entity of an unsuccessful REST {@link Response}.
 */
@ApplicationScoped
public abstract class AbstractEntityRestClientExceptionTransformer implements IRestClientExceptionTransformer {

  @Override
  public RuntimeException transform(RuntimeException e, Response response) {
    if (response == null) {
      if (e instanceof javax.ws.rs.ProcessingException) {
        return transformJaxRsProcessingException((javax.ws.rs.ProcessingException) e);
      }
      return e;
    }

    RuntimeException result = transformByResponseStatus(Response.Status.fromStatusCode(response.getStatusInfo().getStatusCode()), e, response);
    if (result == null) {
      result = transformByResponseStatusFamily(response.getStatusInfo().getFamily(), e, response);
    }
    return result != null ? result : defaultTransform(e, response);
  }

  /**
   * Transforms {@link javax.ws.rs.ProcessingException} into a {@link RemoteSystemUnavailableException}.
   * <p>
   * In case there is no response at all, a {@link javax.ws.rs.ProcessingException} indicates that something went wrong
   * while trying to reach the remote system, like the remote system host is down or configuration parameters are wrong.
   */
  protected RuntimeException transformJaxRsProcessingException(javax.ws.rs.ProcessingException e) {
    Throwable cause = e.getCause();
    return new RemoteSystemUnavailableException(cause.getMessage(), cause);
  }

  /**
   * Transform logic for a given {@link Response.Status}.
   * <p>
   * Implementation should simply use a switch statement on status parameter:
   *
   * <pre>
   * switch (status) {
   *   case BAD_GATEWAY:
   *   case SERVICE_UNAVAILABLE:
   *   case GATEWAY_TIMEOUT:
   *     return transformUnavailableResponse(e, response);
   *   case FORBIDDEN:
   *     return transformForbidden(e, response);
   *   case NOT_FOUND:
   *     return transformNotFound(e, response);
   * }
   * return super.transformByResponseStatus(status, e, response);
   * </pre>
   *
   * @return {@code null} if there is no special transformation for given status required else transformed exception
   */
  protected RuntimeException transformByResponseStatus(Response.Status status, RuntimeException e, Response response) {
    return null;
  }

  /**
   * Transform logic if there is no special logic for current {@link Response.Status}
   *
   * @return {@code null} if there is no special transformation for given status family required else transformed
   *         exception
   */
  protected RuntimeException transformByResponseStatusFamily(Response.Status.Family family, RuntimeException e, Response response) {
    switch (family) {
      case SUCCESSFUL:
        return e;
    }
    return null;
  }

  /**
   * Default transform logic if there is no specific handling for current {@link Response.Status} nor
   * {@link Response.Status.Family}
   *
   * @return transformed exception
   */
  protected abstract RuntimeException defaultTransform(RuntimeException e, Response response);

  /**
   * First tries to extract a error from a response by calling {@link Response#readEntity}. If this fails, a fallback
   * logic is called to transform the exception.
   *
   * @param entityExceptionSupplier
   *          responsible to read entity and then providing new exception
   * @param fallbackExceptionSupplier
   *          creates fallback exception in case entity reading fails
   * @return transformed exception
   */
  protected RuntimeException safeTransformEntityErrorResponse(RuntimeException e, Response response, Supplier<RuntimeException> entityExceptionSupplier, Supplier<RuntimeException> fallbackExceptionSupplier) {
    RuntimeException suppressedException = null;
    try {
      if (response.hasEntity()) {
        return entityExceptionSupplier.get();
      }
    }
    catch (RuntimeException ex) {
      suppressedException = ex;
    }

    RuntimeException exception = fallbackExceptionSupplier.get();
    if (suppressedException != null) {
      exception.addSuppressed(suppressedException);
    }
    return exception;
  }
}
