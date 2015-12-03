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
package org.eclipse.scout.rt.platform.job.listener;

import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Event to describe the job lifecycle change.
 *
 * @since 5.1
 */
public enum JobEventType {
  /**
   * Indicates that a job was scheduled.
   */
  SCHEDULED,
  /**
   * Indicates that a job was rejected for execution. This might happen if the job manager has been shutdown, or if no
   * more worker threads are available.
   */
  REJECTED,
  /**
   * Indicates that a job is about to commence execution.
   */
  ABOUT_TO_RUN,
  /**
   * Indicates that a job finished execution, either normally or because it was cancelled. Use
   * {@link IFuture#isCancelled()} to check for cancellation.
   */
  DONE,
  /**
   * Indicates that the job manager was shutdown.
   */
  SHUTDOWN,
  /**
   * Indicates that a job entered a blocking condition and is waiting for it to fall.
   *
   * @see IBlockingCondition
   */
  BLOCKED,
  /**
   * Indicates that a job was waiting for a blocking condition to fall and is now resuming execution.
   *
   * @see IBlockingCondition
   */
  UNBLOCKED,
  /**
   * Indicates that an unblocked job resumed execution. For jobs which do not operate on a mutex object, this event
   * corresponds to the 'unblock-event'. If being a mutex job, this event is fired once the mutex is acquired to
   * continue execution.
   *
   * @see IBlockingCondition
   */
  RESUMED,
  /**
   * Indicates a change in the execution hints of a job.
   */
  EXECUTION_HINT_CHANGED
}
