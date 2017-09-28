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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.Date;

import org.eclipse.scout.rt.platform.job.JobState;
import org.quartz.Calendar;
import org.quartz.Trigger;
import org.quartz.spi.OperableTrigger;

/**
 * Runner on a 'per-future' basis for delayed and/or repetitive jobs to control execution, and to compute the trigger's
 * firing times. This runner uses the 'misfire' policy as defined on the schedule to deal with missed firings.
 * <p>
 * Upon a 'misfire' but with no planned further firing (as computed by the schedule plan), this runner schedules a last
 * execution to enter done state.
 * <p>
 * A firing is called a 'misfire', if the trigger would like to fire, but is not allowed to because currently executing.
 *
 * @since 5.2
 */
public class FutureRunner<RESULT> implements IRejectableRunnable {

  private final JobFutureTask<RESULT> m_futureTask;
  private final JobManager m_jobManager;
  private final OperableTrigger m_trigger;

  public FutureRunner(final JobManager jobManager, final JobFutureTask<RESULT> futureTask) {
    m_jobManager = jobManager;
    m_futureTask = futureTask;
    m_trigger = futureTask.getTrigger();
  }

  @Override
  public void run() {
    // Run the task.
    m_futureTask.run();

    // Exit if done (completed or cancelled).
    if (m_futureTask.isDone()) {
      return;
    }

    // Prepare for the next execution.
    m_futureTask.changeState(JobState.PENDING);
    m_trigger.executionComplete(null, null);

    // Apply 'misfire' policy if the next firing time already elapsed.
    applyMisfire(m_futureTask.getCalendar(), m_trigger);

    // Schedule next execution.
    m_jobManager.getDelayedExecutor().schedule(() -> m_jobManager.competeForPermitAndExecute(m_futureTask, FutureRunner.this), m_trigger.getNextFireTime());
  }

  @Override
  public void reject() {
    m_futureTask.reject();
  }

  /**
   * Applies the 'misfire' policy in case the next firing time already elapsed.
   */
  protected void applyMisfire(final Calendar calendar, final OperableTrigger trigger) {
    // Check if a 'misfire' policy is configured.
    if (trigger.getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
      return;
    }

    // Check whether a 'misfire' situation exists, meaning that the next fire time already elapsed.
    final Date now = new Date();
    if (trigger.getNextFireTime().after(now)) {
      return;
    }

    // Let the trigger compute its next fire time based on its configured 'misfire' policy.
    trigger.updateAfterMisfire(calendar);

    // If the 'misfire' policy of the trigger prevents the job to be executed ever again, make it to fire immediately, so it can enter done state accordingly.
    // This may happen if the schedule is configured with an end-time, and that end-time has passed in the meantime (e.g. if using SimpleSchedule).
    if (trigger.getNextFireTime() == null) {
      trigger.setNextFireTime(now);
    }
  }
}
