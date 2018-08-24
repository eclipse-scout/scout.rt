/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import javax.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract default implementation of a JAX-RS REST {@link ExceptionMapper}.
 * <p>
 * <b>NOTE:</b> Use {@link #notifyTransaction(Exception)} method to notify the Scout transaction before the exception
 * is mapped into a {@link Response} object and discarded as exception. If the Scout transaction is not notified about
 * the failure, the transaction will be committed afterwards.
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
   * <b>NOTE:</b> The returned {@link Response} instance should contain a {@link ErrorResponse} entity providing
   * further error information. Otherwise, clients relying on the entity would fail.
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
