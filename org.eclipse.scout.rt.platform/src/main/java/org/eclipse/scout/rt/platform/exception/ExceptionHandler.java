/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.exception;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationRuntimeException;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central point for exception handling.
 */
@ApplicationScoped
public class ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

  /**
   * Method invoked to handle the given {@link Throwable}.
   * <p>
   * This method must not throw an exception.
   */
  public void handle(final Throwable t) {
    if (t instanceof InterruptedRuntimeException) {
      handleInterruptedException((InterruptedRuntimeException) t);
    }
    else if (t instanceof CancellationRuntimeException) {
      handleCancelledException((CancellationRuntimeException) t);
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
   * Method invoked to handle an {@link InterruptedRuntimeException}.
   * <p>
   * The default implementation logs it with debug level.
   */
  protected void handleInterruptedException(final InterruptedRuntimeException e) {
    LOG.debug("Interruption", e);
  }

  /**
   * Method invoked to handle a {@link CancellationRuntimeException}. Typically, such an exception is thrown when waiting on a
   * cancelled job.
   * <p>
   * The default implementation logs it with debug level.
   */
  protected void handleCancelledException(final CancellationRuntimeException e) {
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
        LOG.debug("", e);
      }
      else {
        LOG.info("{}: {}", e.getClass().getSimpleName(), e.getMessage());
      }
    }
    else if (e instanceof ProcessingException) {
      switch (((ProcessingException) e).getStatus().getSeverity()) {
        case IProcessingStatus.INFO:
        case IProcessingStatus.OK:
          LOG.info("", e);
          break;
        case IProcessingStatus.WARNING:
          LOG.warn("", e);
          break;
        default:
          LOG.error("", e);
          break;
      }
    }
    else {
      LOG.error("", e);
    }
  }

  /**
   * Method invoked to handle a {@link Throwable} which is not of the type {@code PlatformException}, or
   * {@link InterruptedRuntimeException}, or {@link CancellationRuntimeException}.
   * <p>
   * The default implementation logs the throwable as <code>ERROR</code>.
   */
  protected void handleThrowable(final Throwable t) {
    LOG.error("{}:{}", t.getClass().getSimpleName(), StringUtility.nvl(t.getMessage(), "n/a"), t);
  }

  /**
   * Helper method to get the exception's root cause.
   */
  public static Throwable getRootCause(final Throwable e) {
    if (e == null) {
      return null;
    }

    Throwable cause = e;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }
}
