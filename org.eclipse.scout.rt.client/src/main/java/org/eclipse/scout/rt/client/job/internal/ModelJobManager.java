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
  public boolean isBlocked(final Future<?> future) {
    return m_blockedFutures.contains(future);
  }

  @Override
  public final boolean isModelThread() {
    return m_mutexSemaphore.isModelThread();
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
    Assertions.assertNotNull(input.getSession(), "ClientSession must not be null");
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
    return new Task<RESULT>(m_executor, input) {

      @Override
      protected ModelJobFuture<RESULT> interceptFuture(final ModelJobFuture<RESULT> future) {
        return ModelJobManager.this.interceptFuture(future);
      }

      @Override
      public void beforeExecute(final ModelJobFuture<RESULT> future) {
        m_mutexSemaphore.registerAsModelThread(); // the model-mutex was already acquired the time being scheduled.
      }

      @Override
      protected RESULT execute(final ModelJobFuture<RESULT> future) throws Exception {
        // Run the command on behalf of its Future.
        return new InitThreadLocalCallable<>(callable, IFuture.CURRENT, future).call();
      }

      @Override
      public void afterExecute(final ModelJobFuture<RESULT> future) {
        // Check, if this job was interrupted while waiting for a blocking condition to fall.
        final boolean interruptedWhileBlocking = m_blockedFutures.remove(future);

        if (interruptedWhileBlocking) {
          // NOOP: This job does not own the mutex and therefore must not pass the mutex to the next queued job.
        }
        else {
          final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
          if (nextTask != null) {
            nextTask.schedule();
          }
        }
      }

      @Override
      public void rejected(final ModelJobFuture<RESULT> future) {
        future.cancel(true); // to interrupt the submitter if waiting for the job to complete.

        final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
        if (nextTask != null) {
          nextTask.schedule();
        }
      }

      @Override
      protected boolean isMutexOwner() {
        return m_mutexSemaphore.getMutexOwner() == this;
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
    final Callable<RESULT> c8 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, input.getSession().getTexts());
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, NlsLocale.CURRENT, input.getSession().getLocale());
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, ISession.CURRENT, input.getSession());
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
      if (!isModelThread()) {
        throw new JobExecutionException(String.format("Wrong thread: A job can only be blocked on behalf of the model thread. [thread=%s]", Thread.currentThread().getName()));
      }

      final ModelJobFuture<?> currentFuture = (ModelJobFuture<?>) IFuture.CURRENT.get();
      final ClientJobInput input = currentFuture.getInput();

      // Pass the model-mutex to the next queued job or release the mutex.
      final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
      if (nextTask != null) {
        nextTask.schedule();
      }

      // [mutex] The following code is not synchronized with the model-mutex anymore.

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
        }
        m_blockedFutures.remove(currentFuture); // do not put into a 'finally'-block to not pass the mutex to the next job if being interrupted; see Task#afterExecute.
      }

      // [re-acquire] phase 1: Compete for the model-mutex anew.
      final CountDownLatch mutexReAcquiredLatch = new CountDownLatch(1);
      final AtomicBoolean rejectedByExecutor = new AtomicBoolean(false);

      final Task<Void> task = new Task<Void>(m_executor, input) {

        @Override
        protected Void execute(final ModelJobFuture<Void> future) throws Exception {
          rejectedByExecutor.set(false);
          mutexReAcquiredLatch.countDown(); // simply release the blocking thread.
          return null;
        }

        @Override
        public void rejected(final ModelJobFuture<Void> future) {
          rejectedByExecutor.set(true);
          mutexReAcquiredLatch.countDown(); // simply release the blocking thread.
        }

        @Override
        protected boolean isMutexOwner() {
          return m_mutexSemaphore.getMutexOwner() == this;
        }
      };
      boolean acquired = m_mutexSemaphore.tryAcquireElseOfferHead(task);

      // [re-acquire] phase 2: If not being the mutex-owner yet, wait until having re-acquired the model-mutex.
      while (!acquired && !m_executor.isShutdown()) {
        try {
          mutexReAcquiredLatch.await();
          acquired = true;
        }
        catch (final InterruptedException e) {
          // Ignore thread interruptions to not enter an inconsistent state of having multiple concurrent model-threads.
          LOG.warn(String.format("Ignored thread's interruption while waiting for the model-mutex to be re-acquired. [blockingCondition=%s, job=%s]", m_name, input.getIdentifier("n/a")), e);
        }
      }

      // Check if the model-mutex could be acquired successfully.
      if (rejectedByExecutor.get()) {
        currentFuture.cancel(true); // to interrupt the submitter if waiting for the job to complete.
        throw JobExecutionException.newRejectedJobExecutionException("Failed to re-acquire the model-mutex because being rejected by the executor. Maybe there are no more threads or queue slots available, or the executor was shutdown. [blockingCondition=%s, job=%s, shutdown=%s]", m_name, input.getIdentifier("n/a"), m_executor.isShutdown());
      }
      else {
        // [mutex] The following code is synchronized with the model-mutex anew.
        m_mutexSemaphore.registerAsModelThread();
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
