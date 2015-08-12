package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.Filter;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
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
    Object pollingRequestHint = jobInput.getProperty(POLLING_REQUEST_HINT);
    if (pollingRequestHint instanceof Boolean) {
      return ((Boolean) pollingRequestHint).booleanValue();
    }
    else {
      return false;
    }
  }

  /**
   * Runs the given job as the model thread, and blocks until the job completed or enters a blocking condition. Does NOT
   * catch any exception.
   *
   * @param callable
   *          {@link Callable} to be executed
   * @param jobInput
   *          input for the model job
   * @param throwableTranslatorClass
   *          exception translator class to be used
   * @return the job's result.
   */
  public static <RESULT, ERROR extends Throwable> RESULT runInModelJobAndAwait(final Callable<RESULT> callable, final JobInput jobInput, final Class<? extends IThrowableTranslator<ERROR>> throwableTranslatorClass) throws ERROR {
    Assertions.assertTrue(!ModelJobs.isModelThread());

    final IFuture<RESULT> future = ModelJobs.schedule(callable, jobInput);

    // Wait until the job is done (completed or cancelled), or enters a blocking condition.
    boolean timeout;
    try {
      timeout = !Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchFutures(future).andAreNotBlocked(), AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    catch (ProcessingException e) {
      // FIXME [dwi] use translator to not work with ProcessingException
      throw new UiException("Interrupted while waiting for a job to complete. [job=%s, future=%s]", e, callable.getClass().getName(), future);
    }

    if (timeout) {
      throw new UiException("Timeout elapsed while waiting for a job to complete [job=%s, timeout=%ss, future=%s]", new TimeoutException(), callable.getClass().getName(), TimeUnit.MILLISECONDS.toSeconds(AWAIT_TIMEOUT), future);
    }

    // Return immediately if the job is not done yet, e.g. because waiting for a blocking condition to fall.
    if (!future.isDone()) {
      return null;
    }

    // Return the future's result or processing exception.
    return future.awaitDoneAndGet(BEANS.get(throwableTranslatorClass));
  }

  /**
   * Calls {@link #runInModelJobAndAwait(Callable, JobInput, Class)} but uses the {@link ExceptionHandler} to handle the
   * exceptions.
   * <p>
   * Important: This method may only be used if a model messagebox may be displayed because the exception handler uses
   * waitFor to wait for a button click and therefore blocks the thread. If the messagebox cannot be displayed (e.g. on
   * session startup) nobody will release the waitFor lock.
   */
  public static <RESULT> RESULT runInModelJobAndAwaitAndHandleException(final Callable<RESULT> callable, final JobInput jobInput) {
    try {
      return runInModelJobAndAwait(callable, jobInput, ExceptionTranslator.class);
    }
    catch (final Exception e) {
      ModelJobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }, jobInput.withName("Handling exception"));
      return null;
    }
  }

  /**
   * @param jobName
   *          name to use for the model job (expect if the current thread is already the model job)
   * @param clientSession
   *          client session to run the job on behalf
   * @param pollingRequest
   *          whether the model job is started for a "polling request". Set this argument to <code>false</code> when
   *          handling a user context. A flag is put in the job input property map and may be retrieved again using the
   *          method {@link #isPollingRequestJob(JobInput)}. This flag is useful when listening for certain job manager
   *          events (see {@link UiSession#UiSession()}.
   */
  public static JobInput newModelJobInput(final String jobName, final IClientSession clientSession, final boolean pollingRequest) {
    ClientRunContext runContext = ClientRunContexts.copyCurrent()
        .withSession(clientSession, true)
        .withProperty(POLLING_REQUEST_HINT, pollingRequest);
    return ModelJobs.newInput(runContext).withName(jobName);
  }
}
