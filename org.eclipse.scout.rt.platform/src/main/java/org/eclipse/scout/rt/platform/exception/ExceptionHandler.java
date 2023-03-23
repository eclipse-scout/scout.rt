/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.exception;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.transaction.TransactionCancelledError;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central point for exception handling.
 */
@ApplicationScoped
public class ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

  protected static final String LOG_PATTERN = "{}: {}";

  /**
   * Method invoked to handle the given {@link Throwable}.
   * <p>
   * This method must not throw an exception.
   */
  public void handle(final Throwable t) {
    if (t instanceof TransactionCancelledError) {
      handleTransactionCancelledException((TransactionCancelledError) t);
    }
    else if (t instanceof ThreadInterruptedError) {
      handleInterruptedException((ThreadInterruptedError) t);
    }
    else if (t instanceof FutureCancelledError) {
      handleCancelledException((FutureCancelledError) t);
    }
    else if (t instanceof PlatformException) {
      final PlatformException pe = (PlatformException) t;
      if (!pe.isConsumed()) {
        try {
          handlePlatformException(pe);
        }
        finally {
          pe.consume();
        }
      }
    }
    else {
      handleThrowable(t);
    }
  }

  /**
   * Method invoked to handle a {@link TransactionCancelledError}. Typically, such an exception is thrown when accessing
   * an already cancelled transaction.
   * <p>
   * The default implementation logs it with debug level.
   */
  protected void handleTransactionCancelledException(final TransactionCancelledError e) {
    LOG.debug("Transaction cancelled", e);
  }

  /**
   * Method invoked to handle an {@link ThreadInterruptedError}.
   * <p>
   * The default implementation logs it with debug level.
   */
  protected void handleInterruptedException(final ThreadInterruptedError e) {
    LOG.debug("Interruption", e);
  }

  /**
   * Method invoked to handle a {@link FutureCancelledError}. Typically, such an exception is thrown when waiting on a
   * cancelled job.
   * <p>
   * The default implementation logs it with debug level.
   */
  protected void handleCancelledException(final FutureCancelledError e) {
    LOG.debug("Cancellation", e);
  }

  /**
   * Method invoked to handle a {@link PlatformException} unless already <em>consumed</em>.
   * <p>
   * The default implementation logs as following:
   * <ul>
   * <li>{@link PlatformException}: is logged as an <em>ERROR</em></li>
   * <li>{@link ProcessingException}: is logged according to its severity</li>
   * <li>{@link VetoException}: is logged as <em>INFO</em> without stacktrace, or <em>DEBUG</em> if
   * <em>debug-logging</em> is enabled</li>
   * </ul>
   */
  protected void handlePlatformException(final PlatformException e) {
    if (e instanceof VetoException) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(LOG_PATTERN, toLogArguments(e));
      }
      else {
        // do not add stacktrace for VetoException
        LOG.info(LOG_PATTERN, e.getClass().getSimpleName(), e.getMessage());
      }
    }
    else if (e instanceof ProcessingException) {
      switch (((ProcessingException) e).getStatus().getSeverity()) {
        case IProcessingStatus.INFO:
        case IProcessingStatus.OK:
          LOG.info(LOG_PATTERN, toLogArguments(e));
          break;
        case IProcessingStatus.WARNING:
          LOG.warn(LOG_PATTERN, toLogArguments(e));
          break;
        default:
          LOG.error(LOG_PATTERN, toLogArguments(e));
          break;
      }
    }
    else {
      LOG.error(LOG_PATTERN, toLogArguments(e));
    }
  }

  /**
   * Method invoked to handle a {@link Throwable} which is not of the type {@code PlatformException}, or
   * {@link ThreadInterruptedError}, or {@link FutureCancelledError}.
   * <p>
   * The default implementation logs the throwable as <code>ERROR</code>.
   */
  protected void handleThrowable(final Throwable t) {
    LOG.error(LOG_PATTERN, toLogArguments(t));
  }

  /**
   * @return Arguments matching {@link #LOG_PATTERN} for given {@link Throwable}.
   */
  protected Object[] toLogArguments(Throwable t) {
    return new Object[]{t.getClass().getSimpleName(), ObjectUtility.nvl(t.getMessage(), "n/a"), t};
  }
}
