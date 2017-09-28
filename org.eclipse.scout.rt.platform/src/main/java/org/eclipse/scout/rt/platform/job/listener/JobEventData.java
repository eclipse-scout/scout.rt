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
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Container for data associated with a {@link JobEvent}.
 * <p>
 * Depending on {@link JobEventType}, different data is set.
 *
 * @since 5.2
 */
public class JobEventData {

  private final FinalValue<IFuture<?>> m_future = new FinalValue<>();
  private final FinalValue<JobState> m_state = new FinalValue<>();
  private final FinalValue<IBlockingCondition> m_blockingCondition = new FinalValue<>();
  private final FinalValue<String> m_executionHint = new FinalValue<>();

  /**
   * Returns the {@link IFuture} if associated with the event. Is <code>null</code> for event type
   * {@link JobEventType#JOB_MANAGER_SHUTDOWN}.
   */
  public IFuture<?> getFuture() {
    return m_future.get();
  }

  public JobEventData withFuture(final IFuture<?> future) {
    m_future.set(future);
    return this;
  }

  /**
   * Returns the {@link JobState} if associated with the event, and is only set for event type
   * {@link JobEventType#JOB_STATE_CHANGED}.
   */
  public JobState getState() {
    return m_state.get();
  }

  public JobEventData withState(final JobState state) {
    m_state.set(state);
    return this;
  }

  /**
   * Returns the execution hint if associated with the event, and is only set for event type
   * {@link JobEventType#JOB_EXECUTION_HINT_ADDED} or {@link JobEventType#JOB_EXECUTION_HINT_REMOVED}.
   */
  public String getExecutionHint() {
    return m_executionHint.get();
  }

  public JobEventData withExecutionHint(final String executionHint) {
    m_executionHint.set(executionHint);
    return this;
  }

  /**
   * Returns the {@link IBlockingCondition} if associated with the event, and is only set for event type
   * {@link JobEventType#JOB_STATE_CHANGED} with state {@link JobState#WAITING_FOR_BLOCKING_CONDITION}.
   */
  public IBlockingCondition getBlockingCondition() {
    return m_blockingCondition.get();
  }

  public JobEventData withBlockingCondition(final IBlockingCondition blockingCondition) {
    m_blockingCondition.set(blockingCondition);
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("future", m_future.get());
    builder.ref("state", m_state.get());
    builder.ref("executionHint", m_executionHint.get());
    builder.attr("blockingCondition", m_blockingCondition.get());
    return builder.toString();
  }
}
