/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.transaction.ITransaction;

/**
 * Abstract default implementation of a REST {@link ExceptionMapper}.
 * <p>
 * <b>NOTE:</b> Use {@link #notifyTransaction(Exception)} method to notify the Scout transaction before the exception is
 * mapped into a {@link Response} object and discarded as exception. If the Scout transaction is not notified about the
 * failure, the transaction will be committed afterwards.
 */
public abstract class AbstractExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

  /**
   * Default implementation of {@link ExceptionMapper#toResponse(Throwable)} notifying the running Scout transaction
   * about the failure exception. The concrete implementation is delegated to {@link #toResponseImpl(Throwable)} method.
   */
  @Override
  public Response toResponse(E exception) {
    notifyTransaction(exception);
    return toResponseImpl(exception);
  }

  /**
   * Map an exception to a {@link javax.ws.rs.core.Response}. Returning {@code null} results in a
   * {@link javax.ws.rs.core.Response.Status#NO_CONTENT} response. Throwing a runtime exception results in a
   * {@link javax.ws.rs.core.Response.Status#INTERNAL_SERVER_ERROR} response.
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
}
