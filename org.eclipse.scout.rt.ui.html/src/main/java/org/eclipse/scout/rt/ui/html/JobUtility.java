package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.Filter;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobException;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.ui.html.json.JsonException;

/**
 * Utility methods to work with the Job API.
 */
public final class JobUtility {
  private static final long AWAIT_TIMEOUT = TimeUnit.HOURS.toMillis(1);

  private JobUtility() {
    // static access only
  }

  /**
   * Blocks until all model jobs for the given session are done. Blocked jobs (e.g. waitFor) are ignored.
   * If the calling thread is already the model thread, an exception is thrown!
   *
   * @throws JsonException
   *           in case the current thread was interrupted, or the waiting timeout elapsed, or the current thread
   *           is the model thread.
   */
  public static void awaitAllModelJobs(final IClientSession clientSession) {
    Assertions.assertNotNull(clientSession, "Argument 'clientSession' must not be null");
    Assertions.assertFalse(ModelJobs.isModelThread(clientSession), "This method may not be called from the model thread");

    boolean timeout;
    try {
      Filter filter = ModelJobs.newFutureFilter().session(clientSession).notBlocked();
      timeout = !Jobs.getJobManager().awaitDone(filter, AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (JobException e) {
      throw new JsonException("Interrupted while waiting for model jobs [clientSession=%s]", e, clientSession);
    }

    if (timeout) {
      throw new JsonException("Timeout while waiting for model jobs [timeout=%ss, clientSession=%s]", new TimeoutException(), TimeUnit.MILLISECONDS.toSeconds(AWAIT_TIMEOUT), clientSession);
    }
  }

  /**
   * Runs the given job as the model thread, and blocks until the job completed or enters a blocking condition.
   * If the calling thread is already the model thread, the job is executed directly (without scheduling a new job).
   *
   * @param jobName
   *          name to use for the model job (expect if the current thread is already the model job)
   * @param clientSession
   *          client session to run the job on behalf
   * @param job
   *          job to be executed
   * @throws JsonException
   *           in case the current thread was interrupted, or the waiting timeout elapsed, or the Future returned with a
   *           processing exception.
   * @return the job's result.
   */
  public static <RESULT> RESULT runModelJobAndAwait(final String jobName, final IClientSession clientSession, final IExecutable<RESULT> job) {
    // If we are already in the model thread, execute the job directly (without scheduling a new job)
    if (ModelJobs.isModelThread(clientSession)) {
      try {
        return Callables.callable(job).call();
      }
      catch (Exception e) {
        throw new JsonException("Job failed [job=%s]", e, job.getClass().getName());
      }
    }

    // Otherwise, schedule a model job and wait for it to finish
    final IFuture<RESULT> future = ModelJobs.schedule(job, ModelJobs.newInput(ClientRunContexts.copyCurrent().session(clientSession)).name(jobName));
    boolean timeout;
    try {
      timeout = !Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().futures(future).notBlocked(), AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (JobException e) {
      throw new JsonException("Interrupted while waiting for a job to complete. [job=%s, future=%s]", e, job.getClass().getName(), future);
    }

    if (timeout) {
      throw new JsonException("Timeout elapsed while waiting for a job to complete [job=%s, timeout=%ss, future=%s]", new TimeoutException(), job.getClass().getName(), TimeUnit.MILLISECONDS.toSeconds(AWAIT_TIMEOUT), future);
    }

    // Query the future's 'done-state' to not block anew. That is if the job is waiting for a blocking condition to fall.
    if (!future.isDone()) {
      return null;
    }

    // Return the future's result or processing exception.
    try {
      return future.awaitDoneAndGet();
    }
    catch (ProcessingException e) {
      throw new JsonException("Job failed [job=%s]", e, job.getClass().getName());
    }
  }
}
