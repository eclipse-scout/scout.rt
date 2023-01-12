/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
