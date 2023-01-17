/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ThreadNameDecorator} installed if the platform is running in development mode.
 * <p>
 * In additional to {@link ThreadNameDecorator}, this decorator appends the job name, and if blocked, the job state to
 * the thread name.
 * <p>
 * By default, this {@link ThreadNameDecorator} is <em>ignored</em>, meaning not registered automatically. However,
 * {@link DevelopmentRegisterer} registers this bean if running in development mode.
 *
 * @since 5.2
 */
@IgnoreBean // ignored by default
@Replace
public class DevelopmentThreadNameDecorator extends ThreadNameDecorator {

  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentThreadNameDecorator.class);

  @Override
  public IUndecorator decorate() {
    final ThreadInfo threadInfo = ThreadInfo.CURRENT.get();
    final IFuture<?> future = IFuture.CURRENT.get();

    // Update the thread name.
    threadInfo.updateThreadName(future.getJobInput().getThreadName(), buildExecutionInfo(future));

    // Decorate the thread name upon job state change.
    final IRegistrationHandle listenerRegistration = future.addListener(
        Jobs.newEventFilterBuilder()
            .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
            .andMatchState(
                JobState.RUNNING,
                JobState.WAITING_FOR_PERMIT,
                JobState.WAITING_FOR_BLOCKING_CONDITION)
            .toFilter(),
        event -> threadInfo.updateThreadName(future.getJobInput().getThreadName(), buildExecutionInfo(future)));

    return () -> {
      // Restore to the original thread name.
      listenerRegistration.dispose();
      threadInfo.reset();
    };
  }

  protected String buildExecutionInfo(final IFuture<?> future) {
    switch (future.getState()) {
      case WAITING_FOR_BLOCKING_CONDITION:
      case WAITING_FOR_PERMIT:
        return String.format("(%s) %s", future.getState().name(), future.getJobInput().getName()); // include state information
      default:
        return future.getJobInput().getName();
    }
  }

  /**
   * Registers {@link DevelopmentThreadNameDecorator} if running in development mode.
   */
  public static class DevelopmentRegisterer implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.BeanManagerPrepared && event.getSource().inDevelopmentMode()) {
        LOG.info("+++ Development thread name decoration");
        event.getSource().getBeanManager().registerClass(DevelopmentThreadNameDecorator.class);
      }
    }
  }
}
