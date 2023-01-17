/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

/**
 * Enumeration describing the different job lifecycle states.
 *
 * @since 5.2
 */
public enum JobState {
  /**
   * Indicates that a job was created and not scheduled yet.
   */
  NEW,
  /**
   * Indicates that a job was given to the job manager for execution.
   */
  SCHEDULED,

  /**
   * Indicates that a job was rejected for execution. This might happen if the job manager has been shutdown, or if no
   * more worker threads are available.
   */
  REJECTED,

  /**
   * Indicates that a job's execution is pending, either because scheduled with a delay, or because of being a
   * repetitive job while waiting for the commencement of the next execution.
   */
  PENDING,

  /**
   * Indicates that a job is running.
   */
  RUNNING,

  /**
   * Indicates that a semaphore aware job is competing for a permit to become available.
   */
  WAITING_FOR_PERMIT,

  /**
   * Indicates that a job is blocked by a blocking condition, and is waiting for it to fall.
   */
  WAITING_FOR_BLOCKING_CONDITION,

  /**
   * Indicates that a job finished execution, either normally or because it was cancelled. Use
   * {@link IFuture#isCancelled()} to check for cancellation.
   */
  DONE
}
