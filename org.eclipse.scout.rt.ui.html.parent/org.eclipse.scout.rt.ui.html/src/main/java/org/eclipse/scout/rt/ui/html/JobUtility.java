package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
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
   * Waits for the given Future and for all model jobs associated with the Future's session to complete. Thereby,
   * blocked jobs are ignored.
   *
   * @param future
   *          the Future to wait for, and additionally all jobs associated with its session.
   * @throws JsonException
   *           in case the current thread was interrupted, or the waiting timeout elapsed, or the Future returned with a
   *           processing exception.
   */
  public static void awaitModelJobs(final IFuture<Void> future) {
    Assertions.assertTrue(ClientJobs.isClientJob(future) || ModelJobs.isModelJob(future), "Future must be a client- or model job Future");
    final IClientSession session = Assertions.assertNotNull(((ClientRunContext) future.getJobInput().runContext()).session(), "client session must not be null");

    boolean timeout;
    try {
      timeout = !Jobs.getJobManager().awaitDone(ModelJobs.newFutureFilter().session(session).notBlocked(), AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (final InterruptedException e) {
      throw new JsonException("Interrupted while waiting for all events to be processed. [future=%s]", e, future);
    }

    if (timeout) {
      throw new JsonException("Timeout elapsed while waiting for all events to be processed [timeout=%ss, future=%s]", new TimeoutException(), TimeUnit.MILLISECONDS.toSeconds(AWAIT_TIMEOUT), future);
    }

    // Query the future's 'done-state' to not block anew. That is if the job is waiting for a blocking condition to fall.
    if (!future.isDone()) {
      return;
    }

    // Query for the future's result to propagate a potential processing exception.
    try {
      future.awaitDoneAndGet();
    }
    catch (final ProcessingException e) {
      throw new JsonException("Event processing failed [future=%s]", e, future);
    }
  }

  /**
   * Runs the given job in the model thread, and blocks until the job completed or enters a blocking condition. The
   * calling thread must not be the model thread for the given session.
   *
   * @param clientSession
   *          client session to run the job on behalf
   * @param job
   *          job to be executed
   * @throws JsonException
   *           in case the current thread was interrupted, or the waiting timeout elapsed, or the Future returned with a
   *           processing exception.
   * @return the job's result.
   */
  public static <RESULT> RESULT runModelJobAndAwait(final IClientSession clientSession, final IExecutable<RESULT> job) {
    Assertions.assertFalse(ModelJobs.isModelThread(clientSession), "Wrong thread, must not be the model thread");

    final IFuture<RESULT> future = ModelJobs.schedule(job, ModelJobs.newInput(ClientRunContexts.copyCurrent().session(clientSession)));
    boolean timeout;
    try {
      timeout = !Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().futures(future).notBlocked(), AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (final InterruptedException e) {
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
    catch (final ProcessingException e) {
      throw new JsonException("Job failed [job=%s]", e, job.getClass().getName());
    }
  }
}
