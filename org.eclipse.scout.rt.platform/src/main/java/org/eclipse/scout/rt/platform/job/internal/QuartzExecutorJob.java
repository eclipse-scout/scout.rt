package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.ExecutorService;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.internal.SchedulingSemaphore.IPermitAcquiredCallback;
import org.eclipse.scout.rt.platform.job.internal.SchedulingSemaphore.QueuePosition;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * This global Job submits the Future associated with the firing trigger to {@link ExecutorService} for execution.
 * Additionally, for semaphore aware tasks, it asynchronously competes for an execution permit first.
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
 * the second job does.<br>
 * Basically, it would be possible to work with two Quartz Jobs and multiple Quartz worker threads, namely one job for
 * regular jobs (parallel processing), an another job for semaphore aware jobs (serial processing). However, tests with
 * lot of jobs showed up, that the performance benefit would be marginal.
 *
 * @since 5.2
 */
@Bean
@DisallowConcurrentExecution // priority based firing and serial execution guarantee an 'as-scheduled' permit acquisition.
public class QuartzExecutorJob implements Job {

  public static final JobKey IDENTITY = new JobKey("dispatcher", "scout.jobmanager.quartz");

  protected static final String PROP_JOB_EXECUTOR = "scout.jobmanager.executor";
  protected static final String PROP_TRIGGER_FUTURE_TASK = "scout.jobmanager.futureTask";
  protected static final String PROP_TRIGGER_FUTURE_RUNNER = "scout.jobmanager.futureRunner";

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    // Obtain Future to be executed.
    final JobFutureTask<?> futureTask = (JobFutureTask<?>) context.getTrigger().getJobDataMap().get(PROP_TRIGGER_FUTURE_TASK);
    if (futureTask == null) {
      throw newJobExecutionException("Unexpected: no FutureTask provided [quartzJob={}]", getClass().getSimpleName());
    }

    // Obtain Runner to run the Future.
    final IFutureRunner futureRunner = (IFutureRunner) context.getTrigger().getJobDataMap().get(PROP_TRIGGER_FUTURE_RUNNER);
    if (futureRunner == null) {
      futureTask.cancel(false);
      throw newJobExecutionException("Unexpected: no FutureRunner provided [quartzJob={}, future={}]", getClass().getSimpleName(), futureTask);
    }

    // Obtain Executor to schedule the Runner.
    final ExecutorService executor = (ExecutorService) context.getJobDetail().getJobDataMap().get(PROP_JOB_EXECUTOR);
    if (executor == null) {
      futureTask.cancel(false);
      throw newJobExecutionException("Unexpected: no Executor provided [quartzJob={}, future={}]", getClass().getSimpleName(), futureTask);
    }

    try {
      dispatch(futureTask, futureRunner, executor);
    }
    catch (final Throwable t) {
      futureTask.cancel(true);
      throw newJobExecutionException("Unexpected error while dispatching future [quartzJob={}, future={}]", getClass().getSimpleName(), futureTask, t);
    }
  }

  /**
   * Competes for an execution permit and executes the Future via ExecutorService.
   */
  protected void dispatch(final JobFutureTask<?> futureTask, final IFutureRunner futureRunner, final ExecutorService executor) {
    // Check whether FutureRunner is ready to run the job.
    if (!futureRunner.accept()) {
      return;
    }

    // Schedule FutureRunner via ExecutorService.
    final SchedulingSemaphore schedulingSemaphore = futureTask.getSchedulingSemaphore();
    if (schedulingSemaphore == null) {
      executor.execute(futureRunner);
    }
    else {
      futureTask.changeState(JobState.WAITING_FOR_PERMIT);
      schedulingSemaphore.compete(futureTask, QueuePosition.TAIL, new IPermitAcquiredCallback() {

        @Override
        public void onPermitAcquired() {
          executor.execute(futureRunner);
        }
      });
    }
    return;
  }

  protected JobExecutionException newJobExecutionException(final String message, final Object... args) {
    final FormattingTuple format = MessageFormatter.arrayFormat(message, args);

    final JobExecutionException jobException = new JobExecutionException(format.getMessage(), format.getThrowable());
    jobException.unscheduleFiringTrigger();
    jobException.setRefireImmediately(false);
    return jobException;
  }

  // ==== Public Helper methods ==== //

  /**
   * Creates a {@link JobDataMap} to be given to {@link QuartzExecutorJob}.
   */
  public static JobDataMap newJobData(final ExecutorService executor) {
    final JobDataMap jobData = new JobDataMap();
    jobData.put(QuartzExecutorJob.PROP_JOB_EXECUTOR, executor);
    return jobData;
  }

  /**
   * Creates a {@link JobDataMap} to associate a {@link JobFutureTask} with a Quartz trigger.
   */
  public static JobDataMap newTriggerData(final JobFutureTask<?> futureTask, final IFutureRunner futureRunner) {
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
    final SchedulingSemaphore schedulingSemaphore = futureTask.getSchedulingSemaphore();
    if (schedulingSemaphore == null) {
      return Trigger.DEFAULT_PRIORITY;
    }
    else {
      return schedulingSemaphore.computeNextLowerPriority();
    }
  }
}
