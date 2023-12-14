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
import jakarta.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract default implementation of a JAX-RS REST {@link ExceptionMapper}.
 * <p>
 * <b>NOTE:</b> Use {@link #notifyTransaction(Throwable)} method to notify the Scout transaction before the exception is
 * mapped into a {@link Response} object and discarded as exception. If the Scout transaction is not notified about the
 * failure, the transaction will be committed afterwards.
 */
@Bean
public abstract class AbstractExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractExceptionMapper.class);

  /**
   * Default implementation of {@link ExceptionMapper#toResponse(Throwable)} notifying the running Scout transaction
   * about the failure exception. The concrete implementation is delegated to {@link #toResponseImpl(Throwable)} method.
   */
  @Override
  public Response toResponse(E exception) {
    notifyTransaction(exception);
    Response response = toResponseImpl(exception);
    checkResponseEntity(response);
    return response;
  }

  /**
   * Maps an exception to a {@link Response}.
   * <p>
   * <b>NOTE:</b> The returned {@link Response} instance should contain a {@link ErrorResponse} entity providing further
   * error information. Otherwise, clients relying on the entity would fail.
   *
   * @param exception
   *          the exception to map to a response.
   * @return a response mapped from the supplied exception.
   */
  protected abstract Response toResponseImpl(E exception);

  /**
   * Adds the {@code exception} as failure to the currently running Scout transaction
   */
  protected void notifyTransaction(E exception) {
    final ITransaction transaction = ITransaction.CURRENT.get();
    if (transaction != null) {
      transaction.addFailure(exception);
    }
  }

  /**
   * Checks if given {@link Response} contains an entity (e.g. {@link ErrorResponse}).
   */
  protected void checkResponseEntity(Response response) {
    if (!response.hasEntity()) {
      LOG.warn("REST response {} contains no entity! Check the ExceptionMapper implementation ({}) and return an {} entity.", response, getClass().getSimpleName(), ErrorResponse.class.getSimpleName());
    }
  }
}
