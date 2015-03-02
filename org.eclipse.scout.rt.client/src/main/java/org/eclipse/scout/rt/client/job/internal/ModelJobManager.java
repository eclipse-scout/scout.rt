/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.Executables;
import org.eclipse.scout.commons.job.Executables.IExecutable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.NamedThreadFactory;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.internal.callable.SubjectCallable;
import org.eclipse.scout.commons.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IBlockingCondition;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Default implementation of {@link IModelJobManager}.
 *
 * @since 5.1
 */
public class ModelJobManager implements IModelJobManager {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobManager.class);

  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.model.corePoolSize";
  protected static final int DEFAULT_CORE_POOL_SIZE = 0; // The number of threads to keep in the pool, even if they are idle; The default is 0 to have no worker-thread alive if idle to save resources if having inactive clients.
  protected static final String PROP_KEEP_ALIVE_TIME = "org.eclipse.scout.job.model.keepAliveTime";
  protected static final long DEFAULT_KEEP_ALIVE_TIME = 60L;

  @Internal
  protected final ExecutorService m_executor;
  @Internal
  protected final MutexSemaphore m_mutexSemaphore;
  @Internal
  protected final Set<Future<?>> m_blockedFutures = new HashSet<>();
  @Internal
  protected volatile Thread m_modelThread; // The thread currently representing the model-thread.

  public ModelJobManager() {
    m_mutexSemaphore = Assertions.assertNotNull(createMutexSemaphore());
    m_executor = Assertions.assertNotNull(createExecutor());
  }

  @Override
  public final <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return runNow(executable, createDefaultJobInput());
  }

  @Override
  public final <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final ClientJobInput input) throws ProcessingException {
    validateInput(input);

    Assertions.assertTrue(isModelThread(), "Wrong thread: The calling thread must be the model-thread to run model jobs in 'runNow' style. [thread=%s, job=%s]", Thread.currentThread().getName(), input.getIdentifier("n/a"));
    Assertions.assertNotNull(IFuture.CURRENT.get(), "Unexpected inconsistency: No Future bound to current thread. [thread=%s, job=%s]", Thread.currentThread().getName(), input.getIdentifier("n/a"));

    final Callable<RESULT> command = Assertions.assertNotNull(interceptCallable(Executables.callable(executable), input));
    try {
      // run the command on behalf of the current model-thread and Future.
      return command.call();
    }
    catch (final Exception e) {
      throw ExceptionTranslator.translate(e);
    }
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable) {
    return schedule(executable, createDefaultJobInput());
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final ClientJobInput input) {
    validateInput(input);

    final Callable<RESULT> command = Assertions.assertNotNull(interceptCallable(Executables.callable(executable), input));

    final Task<RESULT> task = createModelTask(command, input);

    if (m_mutexSemaphore.tryAcquireElseOfferTail(task)) {
      task.schedule();
    }

    return Executables.future(task.getFuture(), input.getIdentifier("n/a"));
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit) {
    return schedule(executable, delay, delayUnit, createDefaultJobInput());
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final ClientJobInput input) {
    // TODO [dwi] implement this method.
    return schedule(executable, input);
  }

  @Override
  public boolean isBlocked(final Future<?> future) {
    return m_blockedFutures.contains(future);
  }

  /**
   * Sets the given thread as model-thread.
   */
  @Internal
  protected void setModelThread(final Thread thread) {
    m_modelThread = thread;
  }

  @Override
  public final boolean isModelThread() {
    return Thread.currentThread() == m_modelThread;
  }

  @Override
  public final boolean isIdle() {
    return m_mutexSemaphore.isIdle();
  }

  @Override
  public final boolean waitForIdle(final long timeout, final TimeUnit unit) throws InterruptedException {
    return m_mutexSemaphore.waitForIdle(timeout, unit);
  }

  @Override
  public final void shutdown() {
    // 1. Cancel executing and pending futures.
    m_mutexSemaphore.clearAndCancel();
    // 2. Shutdown the job manager.
    m_executor.shutdownNow();
  }

  @Override
  public final void visit(final IFutureVisitor visitor) {
    m_mutexSemaphore.visit(visitor);
  }

  @Override
  public IBlockingCondition createBlockingCondition(final String name) {
    return new BlockingCondition(name);
  }

  /**
   * Method invoked prior scheduling a job to validate its input.
   */
  protected void validateInput(final ClientJobInput input) {
    Assertions.assertNotNull(input, "ClientJobInput must not be null");
    Assertions.assertNotNull(input.getSession(), "ClientSession must not be null; input-property 'sessionRequired' not applicable for model jobs");
  }

  /**
   * Use this method to pass the mutex to the next job in the queue:
   * <ol>
   * <li>sets the model-thread to <code>null</code></li>
   * <li>releases the mutex-owner</li>
   * <li>polls for the next pending job and schedules it if available</li>
   * </ol>
   */
  @Internal
  protected void passMutexToNextJob() {
    m_modelThread = null;

    final Task<?> nextTask = m_mutexSemaphore.releaseAndPoll();
    if (nextTask != null) {
      nextTask.schedule();
    }
  }

  /**
   * Creates a task representing the given {@link Callable} to be passed to the executor.
   *
   * @param callable
   *          {@link Callable} to be executed.
   * @param input
   *          describes the {@link Callable} and contains execution instructions.
   * @return {@link Task} to be passed to the executor.
   */
  @Internal
  protected <RESULT> Task<RESULT> createModelTask(final Callable<RESULT> callable, final ClientJobInput input) {
    return new Task<RESULT>(m_executor, m_mutexSemaphore, input) {

      @Override
      protected ModelJobFuture<RESULT> interceptFuture(final ModelJobFuture<RESULT> future) {
        return ModelJobManager.this.interceptFuture(future);
      }

      @Override
      public void rejected(final ModelJobFuture<RESULT> future) {
        future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
        passMutexToNextJob();
      }

      @Override
      public void beforeExecute(final ModelJobFuture<RESULT> future) {
        setModelThread(Thread.currentThread());
      }

      @Override
      protected RESULT execute(final ModelJobFuture<RESULT> future) throws Exception {
        return new InitThreadLocalCallable<>(callable, IFuture.CURRENT, future).call(); // Run the command on behalf of its Future.
      }

      @Override
      public void afterExecute(final ModelJobFuture<RESULT> future) {
        if (isModelThread()) { // the current thread is not the model thread if being interrupted while waiting for a blocking condition to fall.
          passMutexToNextJob();
        }
      }
    };
  }

  /**
   * @return Creates a {@link IProgressMonitor} to track the progress of an activity and query the job's
   *         cancellation state.
   */
  @Internal
  protected <RESULT> IProgressMonitor createProgressMonitor() {
    return new IProgressMonitor() {

      @Override
      public boolean isCancelled() {
        return IFuture.CURRENT.get().isCancelled();
      }
    };
  }

  /**
   * Creates a {@link MutexSemaphore} to manage acquisition of the model-mutex.
   */
  @Internal
  protected MutexSemaphore createMutexSemaphore() {
    return new MutexSemaphore();
  }

  /**
   * Creates the {@link ExecutorService} to run model jobs within this {@link ModelJobManager} in mutual-exclusion
   * manner.
   */
  @Internal
  protected ExecutorService createExecutor() {
    final int corePoolSize = ConfigIniUtility.getPropertyInt(PROP_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE);
    final long keepAliveTime = ConfigIniUtility.getPropertyLong(PROP_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME);

    final RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
        handleJobRejected((ModelJobFuture) runnable);
      }
    };

    return new ThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-model-thread"), rejectionHandler);
  }

  /**
   * Overwrite this method to contribute some behavior to the {@link Callable} given to the executor for execution.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Callable c1 = new YourInterceptor1(c2); // executed 2nd<br/>
   *   Callable head = <i>super.interceptCallable(c1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Callable c1 = new YourInterceptor2(c2); // executed 2nd<br/>
   *   Callable head = new YourInterceptor1(c1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent chain element which is typically the {@link Callable} to be executed.
   * @param input
   *          describes the {@link Callable} and contains execution instructions.
   * @return the head of the chain to be invoked first.
   */
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next, final ClientJobInput input) {
    final Callable<RESULT> c9 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, input.getSession().getTexts());
    final Callable<RESULT> c8 = new InitThreadLocalCallable<>(c9, UserAgent.CURRENT, input.getUserAgent());
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, ISession.CURRENT, input.getSession());
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, NlsLocale.CURRENT, input.getLocale());
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, JobContext.CURRENT, input.getContext());
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, IProgressMonitor.CURRENT, createProgressMonitor());
    final Callable<RESULT> c3 = new SubjectCallable<>(c4, input.getSubject());
    final Callable<RESULT> c2 = new ThreadNameDecorator<RESULT>(c3, input);
    final Callable<RESULT> c1 = new ExceptionTranslator<>(c2, input);

    return c1;
  }

  /**
   * Overwrite this method to adapt the Future representing a job to be executed.<br/>
   * The default implementation simply returns the given future.
   *
   * @param future
   *          Future to be adapted.
   * @return adapted Future.
   */
  protected <RESULT> ModelJobFuture<RESULT> interceptFuture(final ModelJobFuture<RESULT> future) {
    return future;
  }

  /**
   * Method invoked if a job was rejected from being scheduled.<br/>
   *
   * @param future
   *          rejected {@link Future}.
   */
  @Internal
  protected void handleJobRejected(final ModelJobFuture<?> future) {
    future.reject(); // see Task#rejected

    // Do not throw a 'RejectedExecutionException' because the invoker will not be the job's submitter if this task was queued.
    if (m_executor.isShutdown()) {
      LOG.debug("Job rejected because the job manager is shutdown.");
    }
    else {
      LOG.error("Job rejected because no more threads or queue slots available.");
    }
  }

  /**
   * Method invoked to create a {@link ClientJobInput} filled with default values.
   */
  protected ClientJobInput createDefaultJobInput() {
    return ClientJobInput.defaults();
  }

  /**
   * @see IBlockingCondition
   */
  @Internal
  protected class BlockingCondition implements IBlockingCondition {

    private final AtomicBoolean m_blocking = new AtomicBoolean(); // true if at least one thread is waiting for the condition to fall.
    private final String m_name;

    public BlockingCondition(final String name) {
      m_name = Assertions.assertNotNull(name);
    }

    @Override
    public boolean hasWaitingThreads() {
      return m_blocking.get();
    }

    @Override
    public void block() throws JobExecutionException {
      Assertions.assertTrue(isModelThread(), "Wrong thread: A job can only be blocked on behalf of the model thread. [thread=%s]", Thread.currentThread().getName());

      final ModelJobFuture<?> currentFuture = (ModelJobFuture<?>) IFuture.CURRENT.get();
      final ClientJobInput input = currentFuture.getInput();

      passMutexToNextJob();

      // NOT-SYNCHRONIZED-WITH-MUTEX anymore

      // Block the calling thread until the blocking condition falls (IBlockingCondition#signalAll).
      synchronized (m_blocking) {
        m_blocking.set(true);

        m_blockedFutures.add(currentFuture);
        while (m_blocking.get()) { // spurious-wakeup safe
          try {
            m_blocking.wait();
          }
          catch (final InterruptedException e) {
            throw new JobExecutionException(String.format("Interrupted while waiting for a blocking condition to fall. [blockingCondition=%s, job=%s]", m_name, input.getIdentifier("n/a")), e);
          }
          finally {
            m_blockedFutures.remove(currentFuture);
          }
        }
      }

      // Compete for the model-mutex anew.
      final Thread blockedThread = Thread.currentThread();
      final AtomicBoolean blockedThreadInterrupted = new AtomicBoolean(false);

      final CountDownLatch mutexReAcquiredLatch = new CountDownLatch(1);
      final Task<Void> mutexReAcquireTask = new Task<Void>(m_executor, m_mutexSemaphore, input) {

        @Override
        public void rejected(final ModelJobFuture<Void> future) {
          onMutexAcquired();
        }

        @Override
        protected Void execute(final ModelJobFuture<Void> future) throws Exception {
          // NOOP because only invoked if not cancelled.
          return null;
        }

        @Override
        protected void afterExecute(final ModelJobFuture<Void> future) {
          onMutexAcquired();
        }

        private void onMutexAcquired() {
          synchronized (blockedThreadInterrupted) {
            if (blockedThreadInterrupted.get()) {
              passMutexToNextJob();
            }
            else {
              setModelThread(blockedThread);
              mutexReAcquiredLatch.countDown();
            }
          }
        }
      };

      if (m_mutexSemaphore.tryAcquireElseOfferHead(mutexReAcquireTask)) {
        setModelThread(blockedThread);
      }
      else {
        // Wait until the model-mutex is re-acquired.
        while (!isModelThread()) { // spurious-wakeup safe
          try {
            mutexReAcquiredLatch.await();
          }
          catch (final InterruptedException e) {
            synchronized (blockedThreadInterrupted) {
              blockedThreadInterrupted.set(true);
            }

            currentFuture.cancel(true);
            throw new JobExecutionException(String.format("Interrupted while re-acquiring the model-mutex [job=%s]", input.getIdentifier("n/a")), e);
          }
        }
      }
    }

    @Override
    public void unblock() {
      synchronized (m_blocking) {
        m_blocking.set(false);
        m_blocking.notifyAll();
      }
    }
  }
}
