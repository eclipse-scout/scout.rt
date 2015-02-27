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
package org.eclipse.scout.commons.job.internal;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.job.IExecutable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobChangeEvent;
import org.eclipse.scout.commons.job.IJobManager;

/**
 * Job change event implementation
 */
public class JobChangeEvent implements IJobChangeEvent {

  /**
   * Event type indicating that a new {@link IExecutable} has been scheduled.
   */
  public static final int EVENT_TYPE_SCHEDULED = 1 << 0;

  /**
   * Event type indicating that the {@link IExecutable} has been rejected. This can happen if the {@link IJobManager}
   * has already been shutdown or if no more worker threads are available.
   */
  public static final int EVENT_TYPE_REJECTED = 1 << 1;

  /**
   * Event type indicating that the {@link IFuture} associated with the event is about to run.
   */
  public static final int EVENT_TYPE_ABOUT_TO_RUN = 1 << 2;

  /**
   * Event type indicating that the {@link IFuture} associated with the event has finished. This also happens if the
   * {@link IFuture} has been cancelled. Use {@link IFuture#isCancelled()} to check whether the future has completed
   * normally or because it was cancelled.
   */
  public static final int EVENT_TYPE_DONE = 1 << 3;

  /**
   * Event type indicating that the {@link IJobManager} associated with the event is about to shutdown.
   */
  public static final int EVENT_TYPE_SHUTDOWN = 1 << 4;

  /**
   * Event type indicating that the {@link IFuture} associated with the event has been blocked.
   */
  public static final int EVENT_TYPE_BLOCKED = 1 << 5;

  /**
   * Event type indicating that the {@link IFuture} associated with the event has been unblocked and will resume
   * execution.
   */
  public static final int EVENT_TYPE_UN_BLOCKED = 1 << 6;

  /**
   * Event mode indicating that the event occurred in sync mode (no other threads are involved).
   */
  public static final int EVENT_MODE_SYNC = 1 << 0;

  /**
   * Event mode indicating that the event occurred async (other thread are involved).
   */
  public static final int EVENT_MODE_ASYNC = 1 << 1;

  private final int m_type;
  private final int m_mode;
  private final IFuture<?> m_future;
  private final IJobManager<?> m_source;

  public JobChangeEvent(int type, int mode, IJobManager<?> source, IFuture<?> future) {
    if (!isValidMode(mode)) {
      throw new IllegalArgumentException("mode '" + mode + "' is not valid.");
    }
    if (!isValidType(type)) {
      throw new IllegalArgumentException("type '" + type + "' is not valid.");
    }
    m_type = type;
    m_mode = mode;
    m_source = Assertions.assertNotNull(source, "Source job manager may not be null.");
    m_future = future;
  }

  @Override
  public int getType() {
    return m_type;
  }

  @Override
  public int getMode() {
    return m_mode;
  }

  @Override
  public IFuture<?> getFuture() {
    return m_future;
  }

  @Override
  public IJobManager<?> getSourceManager() {
    return m_source;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_future == null) ? 0 : m_future.hashCode());
    result = prime * result + m_mode;
    result = prime * result + ((m_source == null) ? 0 : m_source.hashCode());
    result = prime * result + m_type;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JobChangeEvent)) {
      return false;
    }
    JobChangeEvent other = (JobChangeEvent) obj;
    if (m_future == null) {
      if (other.m_future != null) {
        return false;
      }
    }
    else if (!m_future.equals(other.m_future)) {
      return false;
    }
    if (m_mode != other.m_mode) {
      return false;
    }
    if (m_source == null) {
      if (other.m_source != null) {
        return false;
      }
    }
    else if (!m_source.equals(other.m_source)) {
      return false;
    }
    if (m_type != other.m_type) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("JobChangeEvent [type=").append(typeToString(m_type)).append(", mode=").append(modeToString(m_mode)).append(", future=").append(m_future).append(", manager=").append(m_source.getClass().getName()).append("]");
    return builder.toString();
  }

  public static boolean isValidMode(int mode) {
    return mode == EVENT_MODE_ASYNC
        || mode == EVENT_MODE_SYNC;
  }

  public static boolean isValidType(int type) {
    return type == EVENT_TYPE_SCHEDULED
        || type == EVENT_TYPE_REJECTED
        || type == EVENT_TYPE_ABOUT_TO_RUN
        || type == EVENT_TYPE_DONE
        || type == EVENT_TYPE_SHUTDOWN
        || type == EVENT_TYPE_BLOCKED
        || type == EVENT_TYPE_UN_BLOCKED;
  }

  public static String modeToString(int mode) {
    switch (mode) {
      case EVENT_MODE_ASYNC:
        return "async";
      case EVENT_MODE_SYNC:
        return "sync";
      default:
        return "unknown";
    }
  }

  public static String typeToString(int type) {
    switch (type) {
      case EVENT_TYPE_SCHEDULED:
        return "scheduled";
      case EVENT_TYPE_REJECTED:
        return "rejected";
      case EVENT_TYPE_ABOUT_TO_RUN:
        return "about to run";
      case EVENT_TYPE_DONE:
        return "done";
      case EVENT_TYPE_SHUTDOWN:
        return "shutdown";
      case EVENT_TYPE_BLOCKED:
        return "blocked";
      case EVENT_TYPE_UN_BLOCKED:
        return "unblocked";
      default:
        return "unknown";
    }
  }
}
