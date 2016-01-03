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
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.job.ExecutionTrigger;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFixedDelayTrigger;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner on a 'per-future' basis to run a {@link JobFutureTask} with a 'fixed-delay' schedule periodically with a fixed
 * delay between the termination of one execution and the commencement of the next execution.
 * <p>
 * Serialization is not required because the trigger does not fire concurrently.
 *
 * @since 5.1
 * @see FixedDelayScheduleBuilder
 * @see ExecutionTrigger#withSchedule(org.quartz.ScheduleBuilder)
 */
@Internal
public class FixedDelayFutureRunner implements IFutureRunner {

  private static final Logger LOG = LoggerFactory.getLogger(FixedDelayFutureRunner.class);

  private final Scheduler m_quartz;
  private final JobFutureTask<?> m_futureTask;

  public FixedDelayFutureRunner(final Scheduler quartz, final JobFutureTask<?> futureTask) {
    Assertions.assertTrue(futureTask.getJobInput().getExecutionTrigger().getSchedule() instanceof FixedDelayScheduleBuilder);
    m_quartz = quartz;
    m_futureTask = futureTask;
  }

  @Override
  public boolean accept() {
    return !m_futureTask.isDone();
  }

  @Override
  public void reject() {
    m_futureTask.reject();
  }

  @Override
  public JobFutureTask<?> getFutureTask() {
    return m_futureTask;
  }

  @Override
  public void run() {
    // The following invariants applies:
    // - this method is never invoked concurrently for the same future, because triggered via a 'one-shot' trigger upon completion of this method;
    // - if the future is assigned to a scheduling semaphore, the future owns a permit;
    // - after JobFutureTask.run(), the future's permit is released, meaning that the future is no longer a permit owner;

    m_futureTask.run();
    if (!m_futureTask.isDone()) {
      m_futureTask.changeState(JobState.PENDING);
      computeNextTriggerFireTime();
    }
  }

  private void computeNextTriggerFireTime() {
    m_futureTask.getCompletionPromise().getInternalLock().lock();
    try {
      if (m_futureTask.isDone()) {
        return;
      }

      final IFixedDelayTrigger fixedDelayTrigger = (IFixedDelayTrigger) m_quartz.getTrigger(m_futureTask.getTriggerIdentity());
      if (fixedDelayTrigger.computeNextTriggerFireTime()) {
        m_quartz.rescheduleJob(fixedDelayTrigger.getKey(), fixedDelayTrigger);
      }
    }
    catch (final SchedulerException | RuntimeException e) {
      LOG.error("Failed to update 'fixed delay' Quartz Trigger with next firing time [future={}]", m_futureTask, e);
      m_futureTask.reject();
    }
    finally {
      m_futureTask.getCompletionPromise().getInternalLock().unlock();
    }
  }
}
