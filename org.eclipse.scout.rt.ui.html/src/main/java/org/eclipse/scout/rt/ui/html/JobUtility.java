package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.Filter;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;

/**
 * Utility methods to work with the Job API.
 */
public final class JobUtility {
  private static final long AWAIT_TIMEOUT = TimeUnit.HOURS.toMillis(1);
  private static final String POLLING_REQUEST_HINT = "pollingRequest";

  private JobUtility() {
    // static access only
  }

  /**
   * Blocks until all model jobs for the given session are done. Blocked jobs (e.g. waitFor) are ignored.
   * If the calling thread is already the model thread, an exception is thrown!
   *
   * @throws UiException
   *           in case the current thread was interrupted, or the waiting timeout elapsed, or the current thread
   *           is the model thread.
   */
  public static void awaitAllModelJobs(final IClientSession clientSession) {
    Assertions.assertNotNull(clientSession, "Argument 'clientSession' must not be null");
    Assertions.assertFalse(ModelJobs.isModelThread(clientSession), "This method may not be called from the model thread");

    boolean timeout;
    try {
      Filter filter = ModelJobs.newFutureFilter().andMatchSession(clientSession).andAreNotBlocked();
      timeout = !Jobs.getJobManager().awaitDone(filter, AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (ProcessingException e) {
      throw new UiException("Interrupted while waiting for model jobs to complete [clientSession=%s]", e, clientSession);
    }
    if (timeout) {
      throw new UiException("Timeout while waiting for model jobs to complete [timeout=%ss, clientSession=%s]", new TimeoutException(), TimeUnit.MILLISECONDS.toSeconds(AWAIT_TIMEOUT), clientSession);
    }
  }

  public static boolean isPollingRequestJob(JobInput jobInput) {
    return jobInput.propertyMap().get(POLLING_REQUEST_HINT) != null;
  }

  /**
   * Runs the given job as the model thread, and blocks until the job completed or enters a blocking condition.
   * If the calling thread is already the model thread, the job is executed directly (without scheduling a new job).
   *
   * @param jobName
   *          name to use for the model job (expect if the current thread is already the model job)
   * @param clientSession
   *          client session to run the job on behalf
   * @param pollingRequest
   *          whether the model job is started for a "polling request". Set this argument to <code>false</code> when
   *          handling a user context. A flag is put in the job input property map and may be retrieved again using the
   *          method {@link #isPollingRequestJob(JobInput)}. This flag is useful when listening for certain job manager
   *          events (see {@link UiSession#UiSession()}.
   * @param callable
   *          {@link Callable} to be executed
   * @throws UiException
   *           in case the current thread was interrupted, or the waiting timeout elapsed, or the Future returned with a
   *           processing exception.
   * @return the job's result.
   */
  public static <RESULT> RESULT runModelJobAndAwait(final String jobName, final IClientSession clientSession, final boolean pollingRequest, final Callable<RESULT> callable) {
    // If we are already in the model thread, execute the job directly (without scheduling a new job)
    if (ModelJobs.isModelThread(clientSession)) {
      try {
        return callable.call();
      }
      catch (Exception e) {
        throw new UiException("Job failed [job=%s]", e, callable.getClass().getName());
      }
    }

    // Otherwise, schedule a model job and wait for it to finish
    JobInput jobInput = ModelJobs.newInput(ClientRunContexts.copyCurrent().withSession(clientSession, true)).name(jobName);
    if (pollingRequest) {
      jobInput.propertyMap().put(POLLING_REQUEST_HINT, pollingRequest);
    }
    final IFuture<RESULT> future = ModelJobs.schedule(callable, jobInput);
    boolean timeout;
    try {
      timeout = !Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchFutures(future).andAreNotBlocked(), AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (ProcessingException e) {
      throw new UiException("Interrupted while waiting for a job to complete. [job=%s, future=%s]", e, callable.getClass().getName(), future);
    }

    if (timeout) {
      throw new UiException("Timeout elapsed while waiting for a job to complete [job=%s, timeout=%ss, future=%s]", new TimeoutException(), callable.getClass().getName(), TimeUnit.MILLISECONDS.toSeconds(AWAIT_TIMEOUT), future);
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
      throw new UiException("Job failed [job=%s]", e, callable.getClass().getName());
    }
  }
}
