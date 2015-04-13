/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.PlatformException;

/**
 * Technical exception thrown if interacting with jobs.
 *
 * @since 5.1
 */
public class JobException extends PlatformException {

  private static final long serialVersionUID = 1L;

  public JobException(final String message) {
    super(message);
  }

  public JobException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * @return <code>true</code> to indicate an interruption while waiting for a job or condition.
   */
  public boolean isInterruption() {
    return getCause() instanceof InterruptedException;
  }

  /**
   * @return <code>true</code> to indicate that the result of a job could not be retrieved because a timeout occurred
   *         while waiting for that job to complete.
   */
  public boolean isTimeout() {
    return getCause() instanceof TimeoutException;
  }

  /**
   * Returns <code>true</code> if the given exception describes that a timeout occurred while waiting for a job to
   * complete.
   */
  public static boolean isTimeout(final Exception e) {
    return e instanceof JobException && ((JobException) e).isTimeout();
  }

  /**
   * Returns <code>true</code> if the given exception describes an interruption, meaning that the current thread was
   * interrupted while interacting with the job manager.
   */
  public static boolean isInterruption(final Exception e) {
    return e instanceof JobException && ((JobException) e).isInterruption();
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("message", getLocalizedMessage(), false);
    builder.attr("status", isInterruption() ? "interruption" : isTimeout() ? "timeout" : "n/a");
    return builder.toString();
  }
}
