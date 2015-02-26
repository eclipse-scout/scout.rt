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
package org.eclipse.scout.commons.job.internal;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.Executables;
import org.eclipse.scout.commons.job.Executables.CallableWithJobInput;
import org.eclipse.scout.commons.job.Executables.IExecutable;
import org.eclipse.scout.commons.job.Executables.RunnableWithJobInput;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.commons.job.internal.FutureSet.FutureSupplier;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.internal.callable.SubjectCallable;
import org.eclipse.scout.commons.job.internal.callable.ThreadNameDecorator;

/**
 * Default implementation of {@link IJobManager}.
 *
 * @since 5.1
 */
public class JobManager<INPUT extends IJobInput> implements IJobManager<INPUT> {

  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.corePoolSize";
  protected static final int DEFAULT_CORE_POOL_SIZE = 5; // The number of threads to keep in the pool, even if they are idle.

  @Internal
  protected final String m_threadName;
  @Internal
  protected final ScheduledExecutorService m_executor;
  @Internal
  protected final FutureSet m_futures = new FutureSet();

  public JobManager(final String threadName) {
    m_threadName = threadName;
    m_executor = Assertions.assertNotNull(createExecutor());
  }

  @Override
  public <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return runNow(executable, createDefaultJobInput());
  }

  @Override
  public final <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final INPUT input) throws ProcessingException {
    validateInput(input);

    final Callable<RESULT> command = Assertions.assertNotNull(interceptCallable(Executables.callable(executable), input));
    if (IFuture.CURRENT.get() == null) {
      final IFuture<RESULT> future = m_futures.add(input.getIdentifier("n/a"), new FutureSupplier<RESULT>() {

        @Override
        public Future<RESULT> get() {
          return interceptFuture(new JobFuture<>(new JobRunNowFuture<RESULT>(Thread.currentThread()), input));
        }
      });

      try {
        // Run the command on behalf of the Future created.
        return new InitThreadLocalCallable<>(command, IFuture.CURRENT, future.getDelegate()).call();
      }
      catch (final Exception e) {
        throw ExceptionTranslator.translate(e);
      }
      finally {
        m_futures.remove(future.getDelegate());
      }
    }
    else {
      try {
        // run the command on behalf of the current Future (nested job).
        return command.call();
      }
      catch (final Exception e) {
        throw ExceptionTranslator.translate(e);
      }
    }
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable) throws JobExecutionException {
    return schedule(executable, createDefaultJobInput());
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final INPUT input) throws JobExecutionException {
    validateInput(input);

    final Callable<RESULT> command = Assertions.assertNotNull(interceptCallable(Executables.callable(executable), input));
    return m_futures.add(input.getIdentifier("n/a"), new FutureSupplier<RESULT>() {

      @Override
      public Future<RESULT> get() {
        return m_executor.submit(Executables.callableWithJobInput(command, input));
      }
    });
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final INPUT input) throws JobExecutionException {
    validateInput(input);

    final Callable<RESULT> command = Assertions.assertNotNull(interceptCallable(Executables.callable(executable), input));
    return m_futures.add(input.getIdentifier("n/a"), new FutureSupplier<RESULT>() {

      @Override
      public Future<RESULT> get() {
        return m_executor.schedule(Executables.callableWithJobInput(command, input), delay, delayUnit);
      }
    });
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit) throws JobExecutionException {
    return schedule(executable, delay, delayUnit, createDefaultJobInput());
  }

  @Override
  public final IFuture<Void> scheduleAtFixedRate(final IRunnable runnable, final long initialDelay, final long period, final TimeUnit unit, final INPUT input) throws JobExecutionException {
    validateInput(input);

    final Callable<Void> command = Assertions.assertNotNull(interceptCallable(Executables.callable(runnable), input));
    return m_futures.add(input.getIdentifier("n/a"), new FutureSupplier<Void>() {

      @Override
      @SuppressWarnings("unchecked")
      public Future<Void> get() {
        return (Future<Void>) m_executor.scheduleAtFixedRate(Executables.runnableWithJobInput(command, input), initialDelay, period, unit);
      }
    });
  }

  @Override
  public final IFuture<Void> scheduleWithFixedDelay(final IRunnable runnable, final long initialDelay, final long delay, final TimeUnit unit, final INPUT input) throws JobExecutionException {
    validateInput(input);

    final Callable<Void> command = Assertions.assertNotNull(interceptCallable(Executables.callable(runnable), input));
    return m_futures.add(input.getIdentifier("n/a"), new FutureSupplier<Void>() {

      @Override
      @SuppressWarnings("unchecked")
      public Future<Void> get() {
        return (Future<Void>) m_executor.scheduleWithFixedDelay(Executables.runnableWithJobInput(command, input), initialDelay, delay, unit);
      }
    });
  }

  @Override
  public final void shutdown() {
    // 1. Cancel executing futures.
    final Set<Future<?>> futures = m_futures.clear();
    for (final Future future : futures) {
      future.cancel(true);
    }

    // 2. Shutdown the job manager.
    m_executor.shutdownNow();
  }

  @Override
  public final void visit(final IFutureVisitor visitor) {
    m_futures.visit(visitor);
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
   * Creates the {@link ScheduledExecutorService} to run jobs in parallel.
   */
  @Internal
  protected ScheduledExecutorService createExecutor() {
    final int corePoolSize = ConfigIniUtility.getPropertyInt(PROP_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE);

    final RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnableFuture, final ThreadPoolExecutor executor) {
        handleJobRejected((Future<?>) runnableFuture);
      }
    };

    return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(m_threadName), rejectionHandler) {

      @Override
      protected <RESULT> RunnableScheduledFuture<RESULT> decorateTask(final Callable<RESULT> callable, final RunnableScheduledFuture<RESULT> future) {
        return interceptFuture(new JobFuture<>(future, ((CallableWithJobInput<?>) callable).getInput()));
      }

      @Override
      protected <RESULT> RunnableScheduledFuture<RESULT> decorateTask(final Runnable runnable, final RunnableScheduledFuture<RESULT> future) {
        return interceptFuture(new JobFuture<>(future, ((RunnableWithJobInput) runnable).getInput()));
      }

      @Override
      protected void beforeExecute(final Thread thread, final Runnable runnableFuture) {
        IFuture.CURRENT.set((Future<?>) runnableFuture);
      }

      @Override
      protected void afterExecute(final Runnable runnableFuture, final Throwable t) {
        IFuture.CURRENT.remove();

        final RunnableScheduledFuture<?> future = (RunnableScheduledFuture<?>) runnableFuture;
        if (future.isPeriodic() && !future.isDone()) {
          // NOOP: periodic action which is not finished yet but scheduled for a next execution.
        }
        else {
          handleJobCompleted(future);
        }
      }

    };
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
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next, final INPUT input) {
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(next, JobContext.CURRENT, input.getContext());
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
  protected <RESULT> JobFuture<RESULT> interceptFuture(final JobFuture<RESULT> future) {
    return future;
  }

  /**
   * Method invoked if a job was rejected from being scheduled.
   *
   * @param future
   *          rejected {@link Future}.
   */
  @Internal
  protected void handleJobRejected(final Future<?> future) {
    future.cancel(true); // to indicate the submitter that the job was not executed.

    if (m_executor.isShutdown()) {
      throw new RejectedExecutionException("Job rejected because the job manager is shutdown.");
    }
    else {
      throw new RejectedExecutionException("Job rejected because no more threads or queue slots available.");
    }
  }

  /**
   * Method invoked if a job completed execution.
   *
   * @param future
   *          associated {@link Future}.
   */
  @Internal
  protected void handleJobCompleted(final Future<?> future) {
    m_futures.remove(future); // Remove the job from the map to allow the job to run again.
  }

  /**
   * Method invoked prior scheduling a job to validate its input.
   */
  protected void validateInput(final INPUT input) {
  }

  /**
   * Method invoked to create a {@link IJobInput} filled with default values.
   */
  protected INPUT createDefaultJobInput() {
    @SuppressWarnings("unchecked")
    final INPUT input = (INPUT) JobInput.defaults();
    return input;
  }
}
