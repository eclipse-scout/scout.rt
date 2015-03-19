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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Handler to log exceptions related to job execution or job manager failures.
 */
@Priority(-1)
@ApplicationScoped
public class JobExceptionHandler {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobExceptionHandler.class);

  /**
   * Method invoked to handle a job execution exception. Any exception thrown by this method will be ignored. This
   * method is only invoked if {@link JobInput#isLogOnError()} is enabled.
   *
   * @param job
   *          job that caused the exception.
   * @param t
   *          exception thrown during job execution.
   */
  public void handleException(final JobInput job, final Throwable t) {
    LOG.error(String.format("Job execution failed: %s [%s]", StringUtility.nvl(t.getMessage(), "n/a"), job), t);
  }

  /**
   * Method invoked when the given thread terminates due to the given uncaught exception. Any exception thrown by this
   * method will be ignored.
   *
   * @param thread
   *          thread that terminated due to the given uncaught exception.
   * @param t
   *          uncaught exception.
   */
  public void handleUncaughtException(final Thread thread, final Throwable t) {
    LOG.error(String.format("Worker thread abruptly terminated due to an uncaught exception [worker-thread=%s]", thread.getName()), t);
  }
}
