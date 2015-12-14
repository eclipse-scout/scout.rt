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

import java.util.EventObject;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Event object describing a {@link IFuture} or {@link IJobManager} change.
 *
 * @since 5.1
 */
public class JobEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private final JobEventType m_type;

  private Object m_data;
  private IFuture<?> m_future;

  public JobEvent(final IJobManager jobManager, final JobEventType type) {
    super(jobManager);
    m_type = Assertions.assertNotNull(type);
  }

  @Override
  public IJobManager getSource() {
    return (IJobManager) super.getSource();
  }

  /**
   * Returns the type which describes this event; is never <code>null</code>.
   */
  public JobEventType getType() {
    return m_type;
  }

  /**
   * @return Future that is associated with this event. Is always set, unless for
   *         {@link JobEventType#JOB_MANAGER_SHUTDOWN}.
   */
  public IFuture<?> getFuture() {
    return m_future;
  }

  /**
   * To associate this event with a {@link IFuture}.
   */
  public JobEvent withFuture(final IFuture<?> future) {
    m_future = future;
    return this;
  }

  /**
   * Returns data associated with this event, and is specific to the respective {@link JobEventType}.
   * <p>
   * <table>
   * <tr>
   * <td><strong>event type<strong></td>
   * <td><strong>event data</strong></td>
   * </tr>
   * <tr>
   * <td>{@link JobEventType#JOB_STATE_CHANGED}</td>
   * <td>{@link JobState}, which the job transitioned into.</td>
   * </tr>
   * <tr>
   * <td>{@link JobEventType#JOB_EXECUTION_HINT_ADDED}</td>
   * <td>Execution hint which was added to the Future.</td>
   * </tr>
   * <tr>
   * <td>{@link JobEventType#JOB_EXECUTION_HINT_REMOVED}</td>
   * <td>Execution hint which was removed from the Future.</td>
   * </tr>
   * <tr>
   * <td>{@link JobEventType#JOB_MANAGER_SHUTDOWN}</td>
   * <td>Not supported, is always <code>null</code>.</td>
   * </tr>
   */
  public Object getData() {
    return m_data;
  }

  public JobEvent withData(final Object data) {
    m_data = data;
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("type", m_type.name());
    builder.attr("data", m_data);
    builder.attr("future", m_future);
    builder.ref("source", getSource());
    return builder.toString();
  }
}
