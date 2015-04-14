/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.exception;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * {@code ExceptionHandler} is the central point for handling exceptions.
 */
@ApplicationScoped
public class ExceptionHandler {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ExceptionHandler.class);

  /**
   * Method invoked to handle the given exception. This method must not throw an exception.
   */
  public void handle(final Exception e) {
    final Throwable rootCause = getRootCause(e);
    if (rootCause instanceof InterruptedException) {
      handleInterruptedException((InterruptedException) rootCause);
    }
    else if (e instanceof ProcessingException) {
      final ProcessingException pe = (ProcessingException) e;
      if (!pe.isConsumed()) {
        try {
          handleProcessingException(pe);
        }
        finally {
          pe.consume();
        }
      }
    }
    else {
      handleException(e);
    }
  }

  /**
   * Method invoked to handle a {@code InterruptedException}.<br/>
   * The default implementation does nothing.
   */
  protected void handleInterruptedException(final InterruptedException e) {
    // NOOP: InterruptedException is swallowed.
  }

  /**
   * Method invoked to handle a {@code ProcessingException}.<br/>
   * The default implementation logs the exception according to the severity of the status. In the case of a
   * {@code VetoException}, only it's message is logged as <code>INFO</code>.
   */
  protected void handleProcessingException(final ProcessingException e) {
    final IProcessingStatus status = e.getStatus();
    final String message = String.format("%s:%s", e.getClass().getSimpleName(), status);

    if (e instanceof VetoException) {
      LOG.info(message);
    }
    else {
      switch (status.getSeverity()) {
        case IProcessingStatus.INFO:
          LOG.info(message, e);
          break;
        case IProcessingStatus.WARNING:
          LOG.warn(message, e);
          break;
        default:
          LOG.error(message, e);
          break;
      }
    }
  }

  /**
   * Method invoked to handle an {@code Exception}.<br/>
   * The default implementation logs the exception as <code>ERROR</code>.
   */
  protected void handleException(final Exception e) {
    final String message = String.format("%s:%s", e.getClass().getSimpleName(), StringUtility.nvl(e.getMessage(), "n/a"));
    LOG.error(message, e);
  }

  /**
   * Helper method to get the exception's root cause.
   */
  protected Throwable getRootCause(final Throwable e) {
    if (e.getCause() != null) {
      return getRootCause(e.getCause());
    }
    return e;
  }
}
