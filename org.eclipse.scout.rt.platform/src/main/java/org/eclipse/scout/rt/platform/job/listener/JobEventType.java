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
package org.eclipse.scout.rt.platform.job.listener;

import org.eclipse.scout.rt.platform.job.JobState;

/**
 * Describes the semantic of a {@link JobEvent}.
 *
 * @since 5.2
 */
public enum JobEventType {
  /**
   * Signals that a job transitioned to a new {@link JobState}, e.g. form {@link JobState#SCHEDULED} to
   * {@link JobState#RUNNING}.
   *
   * @see JobEventData#getState()
   * @see JobEventData#getFuture()
   * @see JobEventData#getBlockingCondition()
   */
  JOB_STATE_CHANGED,

  /**
   * Signals that an execution hint was added to a job.
   *
   * @see JobEventData#getExecutionHint()
   * @see JobEventData#getFuture()
   */
  JOB_EXECUTION_HINT_ADDED,

  /**
   * Signals that an execution hint was removed from a job.
   *
   * @see JobEventData#getExecutionHint()
   * @see JobEventData#getFuture()
   */
  JOB_EXECUTION_HINT_REMOVED,

  /**
   * Signals that the job manager was shutdown.
   */
  JOB_MANAGER_SHUTDOWN
}
