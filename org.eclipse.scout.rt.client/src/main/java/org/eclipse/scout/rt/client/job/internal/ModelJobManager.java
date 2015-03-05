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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.job.IExecutable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.Executables;
import org.eclipse.scout.commons.job.internal.NamedThreadFactory;
import org.eclipse.scout.commons.job.internal.ProgressMonitor;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.internal.callable.SubjectCallable;
import org.eclipse.scout.commons.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IBlockingCondition;
import org.eclipse.scout.rt.client.job.IClientJobManager;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.job.internal.ModelFutureTask.IMutexAcquiredListener;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Default implementation of {@link IModelJobManager}.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ModelJobManager implements IModelJobManager {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobManager.class);

  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.model.corePoolSize";
  protected static final int DEFAULT_CORE_POOL_SIZE = 20; // The number of threads to keep in the pool, even if they are idle;
  protected static final String PROP_KEEP_ALIVE_TIME = "org.eclipse.scout.job.model.keepAliveTime";
  protected static final long DEFAULT_KEEP_ALIVE_TIME = 60L;

  @Internal
  protected final ExecutorService m_executor;
  @Internal
  protected final MutexSemaphores m_mutexSemaphores;
  @Internal
  protected final Set<IFuture<?>> m_blockedFutures;
  @Internal
  protected final Set<Thread> m_modelThreads; // The threads currently representing the model-thread. There is one model-thread per session.

  public ModelJobManager() {
    m_mutexSemaphores = Assertions.assertNotNull(createMutexSemaphores());
    m_executor = Assertions.assertNotNull(createExecutor());

    m_blockedFutures = new CopyOnWriteArraySet<>(); // CopyOnWriteArraySet because concurrent iteration if querying all Futures.
    m_modelThreads = new HashSet<>();
  }

  @Override
  public final <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return runNow(executable, createDefaultJobInput());
  }

  @Override
  public final <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final ClientJobInput input) throws ProcessingException {
    validateInput(input);

    Assertions.assertTrue(isModelThread(), "Wrong thread: The calling thread must be the model-thread to run model jobs in 'runNow' style. [thread=%s, job=%s]", Thread.currentThread().getName(), input.getIdentifier());
    Assertions.assertNotNull(IFuture.CURRENT.get(), "Unexpected inconsistency: No Future bound to current thread. [thread=%s, job=%s]", Thread.currentThread().getName(), input.getIdentifier());

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

    final ModelFutureTask<RESULT> task = interceptFuture(createModelFutureTask(command, input));

    if (m_mutexSemaphores.tryAcquireElseOfferTail(task)) {
      m_executor.execute(task);
    }

    return task.getFuture();
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit) {
    return schedule(executable, delay, delayUnit, createDefaultJobInput());
  }

  @Override
  public <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final ClientJobInput input) {
    validateInput(input);

    final Callable<RESULT> command = Assertions.assertNotNull(interceptCallable(Executables.callable(executable), input));

    final ModelFutureTask<RESULT> task = interceptFuture(createModelFutureTask(command, input));

    try {
      OBJ.one(IClientJobManager.class).schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          if (m_mutexSemaphores.tryAcquireElseOfferTail(task)) {
            m_executor.execute(task);
          }
        }
      }, delay, delayUnit, ClientJobInput.empty().sessionRequired(false));
    }
    catch (final JobExecutionException e) {
      LOG.error("Failed to delayed schedule the given executable.", e);
      task.getFuture().cancel(true);
    }

    return task.getFuture();
  }

  @Override
  public boolean isBlocked(final IFuture<?> future) {
    return m_blockedFutures.contains(future);
  }

  @Internal
  protected void registerModelThread(final Thread thread) {
    m_modelThreads.add(thread);
  }

  @Internal
  protected void unregisterModelThread(final Thread thread) {
    m_modelThreads.remove(thread);
  }

  @Override
  public final boolean isModelThread() {
    return m_modelThreads.contains(Thread.currentThread());
  }

  /**
   * @return all Futures managed by this job manager which is all blocked, scheduled and running Futures.
   */
  private Set<IFuture<?>> getFutures() {
    final Set<IFuture<?>> futures = new HashSet<>();
    futures.addAll(m_mutexSemaphores.getFutures());
    futures.addAll(m_blockedFutures);
    return futures;
  }

  @Override
  public boolean isDone(final IFilter<IFuture<?>> filter) {
    final IFilter<IFuture<?>> f = AlwaysFilter.ifNull(filter);

    for (final IFuture<?> future : getFutures()) {
      if (f.accept(future)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean waitUntilDone(final IFilter<IFuture<?>> filter, final long timeout, final TimeUnit unit) throws InterruptedException {
    final IFilter<IFuture<?>> f = AlwaysFilter.ifNull(filter);

    // Determine the absolute deadline.
    final Date deadline = new Date(System.currentTimeMillis() + unit.toMillis(timeout));

    // Wait until all jobs matching the filter are 'done' or the deadline is passed.
    m_mutexSemaphores.getMutexChangedLock().lockInterruptibly();
    try {
      while (!isDone(f)) {
        if (!m_mutexSemaphores.getMutexChangedCondition().awaitUntil(deadline)) {
          return false; // timeout expired
        }
      }
    }
    finally {
      m_mutexSemaphores.getMutexChangedLock().unlock();
    }
    return true;
  }

  @Override
  public boolean cancel(final IFilter<IFuture<?>> filter, final boolean interruptIfRunning) {
    final IFilter<IFuture<?>> f = AlwaysFilter.ifNull(filter);
    final Set<Boolean> success = new HashSet<>();

    visit(f, new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(final IFuture<?> future) {
        success.add(future.cancel(interruptIfRunning));
        return true;
      }
    });

    return Collections.singleton(Boolean.TRUE).equals(success);
  }

  @Override
  public final void shutdown() {
    cancel(new AlwaysFilter<IFuture<?>>(), true);
    m_mutexSemaphores.invalidate();
    m_executor.shutdownNow();
  }

  @Override
  public final void visit(final IFilter<IFuture<?>> filter, final IVisitor<IFuture<?>> visitor) {
    final IFilter<IFuture<?>> f = AlwaysFilter.ifNull(filter);

    for (final IFuture<?> future : getFutures()) {
      if (future.isDone() || !f.accept(future)) {
        continue;
      }
      if (!visitor.visit(future)) {
        return;
      }
    }
  }

  @Override
  public IBlockingCondition createBlockingCondition(final String name, final boolean blocking) {
    return new BlockingCondition(name, blocking);
  }

  /**
   * Method invoked prior scheduling a job to validate its input.
   */
  protected void validateInput(final ClientJobInput input) {
    Assertions.assertNotNull(input, "ClientJobInput must not be null");
    Assertions.assertNotNull(input.getSession(), "ClientSession must not be null; input-property 'sessionRequired' not applicable for model jobs");
  }

  /**
   * Use this method to pass the mutex to the next job in the queue. Thereby, the current model-thread is unregistered
   * and the next task scheduled.
   */
  @Internal
  protected void passMutexToNextJob(final ModelFutureTask<?> currentMutexOwner) {
    unregisterModelThread(Thread.currentThread());

    final ModelFutureTask<?> nextTask = m_mutexSemaphores.releaseAndPoll(currentMutexOwner);
    if (nextTask != null) {
      m_executor.execute(nextTask);
    }
  }

  /**
   * Creates a model-task representing the given {@link Callable} to be passed to the executor for serial execution
   * within the session.
   *
   * @param callable
   *          {@link Callable} to be executed.
   * @param input
   *          describes the {@link Callable} and contains execution instructions.
   * @return {@link ModelFutureTask} to be passed to the executor.
   */
  @Internal
  protected <RESULT> ModelFutureTask<RESULT> createModelFutureTask(final Callable<RESULT> callable, final ClientJobInput input) {
    return new ModelFutureTask<RESULT>(callable, input, this) {

      @Override
      protected void rejected(final IFuture<RESULT> future) {
        future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
        passMutexToNextJob(this);
      }

      @Override
      protected void beforeExecute(final IFuture<RESULT> future) {
        // Check, if the Future is expired and therefore should not be executed.
        if (isExpired()) {
          future.cancel(true);
        }

        registerModelThread(Thread.currentThread());

        IFuture.CURRENT.set(future);
        IProgressMonitor.CURRENT.set(future.getProgressMonitor());
      }

      @Override
      protected void afterExecute(final IFuture<RESULT> future) {
        IProgressMonitor.CURRENT.remove();
        IFuture.CURRENT.remove();

        if (isModelThread()) { // the current thread is not the model thread if being interrupted while waiting for a blocking condition to fall.
          passMutexToNextJob(this);
        }
      }
    };
  }

  /**
   * Creates a {@link MutexSemaphores} to manage acquisition of the model-mutex.
   */
  @Internal
  protected MutexSemaphores createMutexSemaphores() {
    return new MutexSemaphores();
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
        ((ModelFutureTask) runnable).reject();

        // Do not throw a 'RejectedExecutionException' because the invoker will not be the job's submitter if this task was queued.
        if (m_executor.isShutdown()) {
          LOG.debug("Job rejected because the job manager is shutdown.");
        }
        else {
          LOG.error("Job rejected because no more threads or queue slots available.");
        }
      }
    };

    return new ThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-model-thread"), rejectionHandler);
  }

  /**
   * Method invoked to create a {@link ClientJobInput} filled with default values.
   */
  protected ClientJobInput createDefaultJobInput() {
    return ClientJobInput.defaults();
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
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, UserAgent.CURRENT, input.getUserAgent());
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, ISession.CURRENT, input.getSession());
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, NlsLocale.CURRENT, input.getLocale());
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, JobContext.CURRENT, input.getContext());
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
  protected <RESULT> ModelFutureTask<RESULT> interceptFuture(final ModelFutureTask<RESULT> future) {
    return future;
  }

  /**
   * @see IBlockingCondition
   */
  @Internal
  protected class BlockingCondition implements IBlockingCondition {

    private volatile boolean m_blocking;
    private final String m_name;

    protected BlockingCondition(final String name, final boolean blocking) {
      m_name = StringUtility.nvl(name, "n/a");
      m_blocking = blocking;
    }

    @Override
    public boolean isBlocking() {
      return m_blocking;
    }

    @Override
    public void setBlocking(final boolean blocking) {
      if (m_blocking != blocking) {
        synchronized (BlockingCondition.this) {
          if (m_blocking != blocking) {
            m_blocking = blocking;
            if (!blocking) {
              BlockingCondition.this.notifyAll();
            }
          }
        }
      }
    }

    @Override
    public void waitFor() throws JobExecutionException {
      Assertions.assertTrue(isModelThread(), "Wrong thread: A job can only be blocked on behalf of the model thread. [thread=%s]", Thread.currentThread().getName());

      if (!m_blocking) { // the blocking condition is not armed yet.
        return;
      }

      final IFuture<?> currentFuture = IFuture.CURRENT.get();
      final ModelFutureTask<?> currentTask = ((ModelFutureTask<?>) currentFuture.getDelegate());

      passMutexToNextJob(currentTask);

      // NOT-SYNCHRONIZED-WITH-MUTEX anymore

      // Block the calling thread until the blocking condition falls.
      synchronized (BlockingCondition.this) {
        m_blockedFutures.add(currentFuture);
        while (m_blocking) { // guard for spurious-wakeups
          try {
            BlockingCondition.this.wait();
          }
          catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.

            throw new JobExecutionException(String.format("Interrupted while waiting for a blocking condition to fall. [blockingCondition=%s, job=%s]", m_name, currentFuture.getJobInput().getIdentifier()), e);
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

      currentTask.setMutexAcquiredListener(new IMutexAcquiredListener() {

        @Override
        public void onMutexAcquired() {
          currentTask.setMutexAcquiredListener(null);

          synchronized (blockedThreadInterrupted) {
            if (blockedThreadInterrupted.get()) {
              passMutexToNextJob(currentTask);
            }
            else {
              registerModelThread(blockedThread);
              mutexReAcquiredLatch.countDown();
            }
          }
        }
      });

      if (m_mutexSemaphores.tryAcquireElseOfferHead(currentTask)) {
        registerModelThread(blockedThread);
        currentTask.setMutexAcquiredListener(null);
      }
      else {
        // Wait until the model-mutex is re-acquired.
        while (!isModelThread()) { // guard for spurious-wakeups
          try {
            mutexReAcquiredLatch.await();
          }
          catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.

            synchronized (blockedThreadInterrupted) {
              blockedThreadInterrupted.set(true);
            }

            currentFuture.cancel(true);
            throw new JobExecutionException(String.format("Interrupted while re-acquiring the model-mutex [job=%s]", currentFuture.getJobInput().getIdentifier()), e);
          }
        }
      }
    }
  }

  // === IProgressMonitorProvider

  @Override
  public <RESULT> IProgressMonitor create(final IFuture<RESULT> future) {
    return new ProgressMonitor(future);
  }
}
