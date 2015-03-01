package org.eclipse.scout.rt.client.job;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.Executables.IExecutable;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job manager to execute jobs interacting with the client model on behalf of the model-thread. There is one
 * {@link IModelJobManager} per {@link IClientSession}.
 * <p/>
 * <strong>If not interacting with the client-model, use {@link IClientJobManager}.</strong>
 * <p/>
 * Jobs are executed in sequence so that no more than one job will be active at any given time. If a model job gets
 * blocked by entering a {@link IBlockingCondition}, the model-mutex will be released which allows another model job to
 * run. When being unblocked, the job must compete for the model-mutex anew in order to continue execution.
 * <p/>
 * While running, jobs executed on behalf of this job manager comply with the following characteristics:
 * <ul>
 * <li>run in sequence among other model jobs (mutual exclusion);</li>
 * <li>are optionally executed on behalf of a {@link Subject};</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s; see {@link ExceptionTranslator} for more
 * information;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IFuture#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 *
 * @since 5.1
 */
public interface IModelJobManager {

  /**
   * Runs the given job immediately on behalf of the current model-thread. This call blocks the calling thread as long
   * as the job is running. The job manager will use a default {@link ClientJobInput} with values from the current
   * calling context.
   * <p/>
   * <strong>The calling thread must be the model-thread himself.</strong>
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @return computation result as returned by the callable; is <code>null</code> if operating on a runnable.
   * @throws ProcessingException
   *           if the executable throws an exception during execution.
   * @see #runNow(IExecutable, ClientJobInput)
   * @see ClientJobInput#defaults()
   */
  <RESULT> RESULT runNow(IExecutable<RESULT> executable) throws ProcessingException;

  /**
   * Runs the given job immediately on behalf of the current model-thread. This call blocks the calling thread as long
   * as the job is running.
   * <p/>
   * <strong>The calling thread must be the model-thread himself.</strong>
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          gives the executable a semantic meaning and contains instructions about its execution.
   * @return computation result as returned by the callable; is <code>null</code> if operating on a runnable.
   * @throws ProcessingException
   *           if the executable throws an exception during execution.
   * @see #runNow(IExecutable)
   */
  <RESULT> RESULT runNow(IExecutable<RESULT> executable, ClientJobInput input) throws ProcessingException;

  /**
   * Runs the given job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. The job manager will use a default {@link ClientJobInput} with values
   * from the current calling context.
   * <p/>
   * If the given job is rejected by the job manager the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the job manager.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).get();</code>.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          gives the callable a semantic and contains instructions about its execution.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @see #schedule(IExecutable, ClientJobInput)
   * @see ClientJobInput#defaults()
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable);

  /**
   * Runs the given job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the job manager the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the job manager.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).get();</code>.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          gives the executable a semantic meaning and contains instructions about its execution.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @see #schedule(IExecutable)
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, ClientJobInput input);

  /**
   * To visit the Futures of the running and pending model jobs.
   *
   * @param visitor
   *          {@link IFutureVisitor} called for each {@link Future}.
   */
  void visit(IFutureVisitor visitor);

  /**
   * Interrupts a possible running job, rejects pending jobs and interrupts jobs waiting for a blocking condition to
   * fall. After having shutdown, this job manager cannot be used anymore.
   */
  void shutdown();

  /**
   * @return <code>true</code> if the job belonging to the given Future is blocked because waiting for a
   *         {@link IBlockingCondition} to fall.
   * @see IBlockingCondition
   */
  boolean isBlocked(Future<?> future);

  /**
   * @return <code>true</code> if the calling thread is the model-thread.
   */
  boolean isModelThread();

  /**
   * @return <code>true</code> if the model-mutex is currently not acquired.
   */
  boolean isIdle();

  /**
   * Blocks the calling thread until the model-mutex gets available. Does not block if available at time of invocation.
   *
   * @param timeout
   *          the maximal time to wait for the model-mutex to become available.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   * @see {@link #isIdle()}
   */
  boolean waitForIdle(long timeout, TimeUnit unit) throws InterruptedException;

  /**
   * Creates a blocking condition to put a model job into waiting mode and let another job acquire the
   * model-mutex. This condition can be used across multiple model-jobs to wait for the same condition; this
   * condition is reusable upon unblocking.
   *
   * @param name
   *          the name of the blocking condition; primarily used for debugging purpose.
   * @return {@link IBlockingCondition}.
   */
  IBlockingCondition createBlockingCondition(String name);
}
