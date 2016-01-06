package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;

/**
 * This global Job submits the Future associated with the firing trigger to {@link JobManager} for execution.
 * <p>
 * This job is installed in Quartz Scheduler as a durable job. Durable means, that this job's existence is not bound to
 * the existence of an associated trigger, which is typically true when {@link JobManager} is idle because of no jobs
 * are scheduled.
 * <p>
 * <strong>Important implementation detail</strong><br>
 * It is crucial for this job to be very short-lived, meaning no blocking action and quick completion.<br>
 * This is because this Quartz Job is configured to be executed in a serial manner (see
 * {@link DisallowConcurrentExecution}), even if triggers may fire concurrently. So for jobs with the same fire time,
 * this guarantees a potential permit acquisition to be done in respect to the scheduling order. For example, if
 * scheduling two jobs in a row, they very likely will have the same execution time (granularity in milliseconds).
 * However, priority-based firing and serial permit acquisition guarantee the first job to compete for a permit before
 * the second job does.
 * <p>
 * Also, the job's trigger is paused during the time of executing a job. That ensures no concurrent execution among the
 * same future. That is important for repetitive jobs firing while still executing a round.
 *
 * @since 5.2
 */
@Bean
@DisallowConcurrentExecution // priority based firing and serial execution guarantee an 'as-scheduled' permit acquisition.
public class QuartzExecutorJob implements Job {

  public static final JobKey IDENTITY = new JobKey("dispatcher", "scout.jobmanager.quartz");

  protected static final String PROP_JOB_MANAGER = "scout.jobmanager";
  protected static final String PROP_TRIGGER_FUTURE_TASK = "scout.jobmanager.futureTask";
  protected static final String PROP_TRIGGER_FUTURE_RUNNER = "scout.jobmanager.futureRunner";

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    final JobDataMap jobData = context.getJobDetail().getJobDataMap();
    final JobDataMap triggerData = context.getTrigger().getJobDataMap();

    JobFutureTask<?> futureTask = null;
    try {
      futureTask = Assertions.assertNotNull((JobFutureTask<?>) triggerData.get(PROP_TRIGGER_FUTURE_TASK), "JobFutureTask must not be null");
      final SerialFutureRunner<?> futureRunner = Assertions.assertNotNull((SerialFutureRunner<?>) triggerData.get(PROP_TRIGGER_FUTURE_RUNNER), "FutureRunner must not be null");
      final JobManager jobManager = Assertions.assertNotNull((JobManager) jobData.get(PROP_JOB_MANAGER), "JobManager must not be null");

      // Asynchronously run the job via executor service.
      if (futureRunner.beforeExecute()) {
        jobManager.submit(futureTask, futureRunner);
      }
    }
    catch (final Throwable t) {
      if (futureTask != null) {
        futureTask.reject();
      }

      final JobExecutionException jee = new JobExecutionException(String.format("Unexpected error while dispatching future [quartzJob=%s, future=%s]", getClass().getSimpleName(), futureTask), t);
      jee.unscheduleFiringTrigger();
      jee.setRefireImmediately(false);
      throw jee;
    }
  }

  // ==== Public Helper methods ==== //

  /**
   * Creates a {@link JobDataMap} to be given to {@link QuartzExecutorJob}.
   */
  public static JobDataMap newJobData(final JobManager jobManager) {
    final JobDataMap jobData = new JobDataMap();
    jobData.put(QuartzExecutorJob.PROP_JOB_MANAGER, jobManager);
    return jobData;
  }

  /**
   * Creates a {@link JobDataMap} to associate a {@link JobFutureTask} with a Quartz trigger.
   */
  public static JobDataMap newTriggerData(final JobFutureTask<?> futureTask, final SerialFutureRunner<?> futureRunner) {
    final JobDataMap triggerData = new JobDataMap();
    triggerData.put(QuartzExecutorJob.PROP_TRIGGER_FUTURE_TASK, futureTask);
    triggerData.put(QuartzExecutorJob.PROP_TRIGGER_FUTURE_RUNNER, futureRunner);
    return triggerData;
  }

  /**
   * Computes the firing priority for a {@link Trigger}.
   * <p>
   * For a non semaphore aware job, {@link Trigger#DEFAULT_PRIORITY} is returned. For a semaphore aware job, the next
   * lower priority compared to the most recently returned priority for that semaphore instead. Then priority based
   * firing guarantees an 'as-scheduled' permit acquisition for jobs with the same fire time.
   * <p>
   * For example, if scheduling two jobs in a row, they very likely will have the same execution time (granularity in
   * milliseconds). However, priority-based firing and serial permit acquisition guarantee the first job to compete for
   * a permit before the second job does.
   */
  public static int computePriority(final JobFutureTask<?> futureTask) {
    final ExecutionSemaphore executionSemaphore = futureTask.getExecutionSemaphore();
    if (executionSemaphore == null) {
      return Trigger.DEFAULT_PRIORITY;
    }
    else {
      return executionSemaphore.computeNextLowerPriority();
    }
  }
}
