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

import java.util.UUID;
import java.util.concurrent.FutureTask;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.KeyMatcher;
import org.quartz.listeners.TriggerListenerSupport;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner on a 'per-future' basis which prevents concurrent Future execution by suspending (pausing) the trigger. The
 * 'misfire' policy as defined on the schedule controls how to deal with missed firings.
 * <p>
 * Upon a 'misfire' but with no planned further firing (as computed by the schedule plan), this runner schedules a last
 * execution to enter done state.
 * <p>
 * A firing is called a 'misfire', if the trigger would like to fire, but is not allowed to because being paused, or
 * because the Quartz worker thread is currently not available.
 * <p>
 * If {@link #beforeExecute()} returns <code>true</code>, the caller must invoke {@link #run()} or {@link #reject()} in
 * order to release the mutex.
 * <p>
 * <em>If concurrent execution for the same Future would be allowed, that could result in a state and permit inconsistency,
 * and is further not allowed by Java {@link FutureTask}.</em>
 *
 * @since 5.2
 */
@Internal
public class SerialFutureRunner<RESULT> implements IFutureRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SerialFutureRunner.class);

  private final Scheduler m_quartz;
  private final JobFutureTask<RESULT> m_futureTask;

  public SerialFutureRunner(final Scheduler quartz, final JobFutureTask<RESULT> futureTask) {
    m_quartz = quartz;
    m_futureTask = futureTask;

    // Install 'misfire' listener.
    final String listenerIdentity = UUID.randomUUID().toString();
    final TriggerKey triggerIdentity = m_futureTask.getTriggerIdentity();
    installMisfireHandler(m_quartz, listenerIdentity, triggerIdentity);
    m_futureTask.whenDone(new IDoneHandler<RESULT>() {

      @Override
      public void onDone(final DoneEvent<RESULT> event) {
        uninstallMisfireHandler(quartz, listenerIdentity, triggerIdentity);
      }
    }, null);
  }

  @Override
  public boolean beforeExecute() {
    if (m_futureTask.isDone()) {
      return false;
    }

    // Suspend the trigger from firing while running this task.
    if (!suspendTrigger()) {
      m_futureTask.reject();
      return false;
    }

    return true;
  }

  @Override
  public void run() {
    // The following invariants applies:
    // - this method is never invoked concurrently for the same future, which is ensured by 'P_Mutex';
    // - if the future is assigned to an execution semaphore, the future owns a permit;
    // - after JobFutureTask.run(), the future's permit is released, meaning that the future is no longer a permit owner;

    m_futureTask.run();
    if (m_futureTask.isDone()) {
      return;
    }

    m_futureTask.changeState(JobState.PENDING);

    // Resume the trigger to fire for subsequent or missed (misfire) tasks.
    if (!resumeTrigger()) {
      m_futureTask.reject();
    }

    // Notify trigger that this round completed.
    if (!afterExecute()) {
      m_futureTask.reject();
      return;
    }
  }

  /**
   * Method invoked upon completion of a round, but only if the future is not in done state yet.
   */
  protected boolean afterExecute() {
    try {
      final Trigger trigger = m_quartz.getTrigger(m_futureTask.getTriggerIdentity());
      if (trigger instanceof IRoundCompletedListener) {
        ((IRoundCompletedListener) trigger).onRoundCompleted(m_quartz);
      }
      return true;
    }
    catch (final SchedulerException | RuntimeException e) {
      LOG.error("Failed to notify trigger about round completion", e);
      return false;
    }
  }

  /**
   * Method invoked once a 'misfire' occurs.
   *
   * @param trigger
   *          trigger with the 'misfire' policy applied.
   */
  protected void onMisfire(final OperableTrigger trigger) {
    // If the 'misfire' policy of the trigger prevents the job to be executed never again, execute it manually, so it can enter done state accordingly.
    // That may happen if the schedule is configured with an end-time, and that end-time has passed in the meantime (e.g. if using a SimpleSchedule).
    if (trigger.getNextFireTime() == null) {
      try {
        m_quartz.scheduleJob(TriggerBuilder.newTrigger()
            .forJob(QuartzExecutorJob.IDENTITY)
            .usingJobData(QuartzExecutorJob.newTriggerData(m_futureTask, this))
            .startNow()
            .build());
      }
      catch (final SchedulerException | RuntimeException e) {
        LOG.error("Failed to schedule last execution of a job after a misfire [future={}]", m_futureTask, e);
        m_futureTask.reject();
      }
    }
  }

  @Override
  public void reject() {
    m_futureTask.reject();
  }

  /**
   * Temporarily suspends the trigger from firing.
   */
  protected boolean suspendTrigger() {
    try {
      m_quartz.pauseTrigger(m_futureTask.getTriggerIdentity());
      return true;
    }
    catch (final SchedulerException | RuntimeException e) {
      LOG.error("Failed to suspend trigger", e);
      return false;
    }
  }

  /**
   * Resumes the trigger to start firing again. In case one or more firings were missed, that results in one
   * consolidated 'misfire' notification. See {@link #onMisfire(OperableTrigger)}
   */
  protected boolean resumeTrigger() {
    try {
      m_quartz.resumeTrigger(m_futureTask.getTriggerIdentity());
      return true;
    }
    catch (final SchedulerException | RuntimeException e) {
      LOG.error("Failed to resume trigger", e);
      return false;
    }
  }

  protected void installMisfireHandler(final Scheduler quartz, final String listenerIdentifier, final TriggerKey triggerIdentity) {
    try {
      quartz.getListenerManager().addTriggerListener(new TriggerListenerSupport() {

        @Override
        public void triggerMisfired(final Trigger trigger) {
          // Clone trigger to apply 'misfire' policy.
          final OperableTrigger operableTrigger = (OperableTrigger) ((OperableTrigger) trigger).clone();
          // Apply 'misfire' policy.
          operableTrigger.updateAfterMisfire(Jobs.getJobManager().getCalendar(trigger.getCalendarName()));
          // Handle 'misfire' notification.
          onMisfire(operableTrigger);
        }

        @Override
        public String getName() {
          return listenerIdentifier;
        }
      }, KeyMatcher.keyEquals(triggerIdentity));
    }
    catch (final SchedulerException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  protected void uninstallMisfireHandler(final Scheduler quartz, final String listenerIdentifier, final TriggerKey triggerIdentity) {
    try {
      quartz.getListenerManager().removeTriggerListenerMatcher(listenerIdentifier, KeyMatcher.keyEquals(triggerIdentity));
    }
    catch (final SchedulerException | RuntimeException e) {
      LOG.error("Failed to uninstall misfire handler", e);
    }
  }

  @Override
  public JobFutureTask<?> getFutureTask() {
    return m_futureTask;
  }
}
