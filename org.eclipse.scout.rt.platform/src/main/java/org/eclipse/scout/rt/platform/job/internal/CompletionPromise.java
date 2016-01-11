/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This promise represents a proxy for the future's final value, and allows to associate handlers to be notified
 * asynchronously upon success or failure, or to wait for the future to complete.
 * <p>
 * Besides, the 'done' parking mechanism of Java {@link FutureTask} cannot be used by {@link JobManager}, because
 * waiting threads are released before {@link FutureTask#done()} is invoked. That results in an inconsistent state,
 * because bookkeeping and event firing is to be done prior releasing any waiting thread.
 * <p>
 * However, the semantic of 'done' remains the same, meaning that a {@link Future} is done once cancelled or completed.
 *
 * @since 5.2
 */
class CompletionPromise<RESULT> {

  private static final Logger LOG = LoggerFactory.getLogger(CompletionPromise.class);

  private final ExecutorService m_executor;

  private final Lock m_lock = new ReentrantLock();
  private final Condition m_doneCondition = m_lock.newCondition();
  private final Condition m_finishedCondition = m_lock.newCondition();

  private final JobFutureTask<RESULT> m_future;
  private final List<PromiseHandler<RESULT>> m_handlers;

  private final FinalValue<DoneEvent<RESULT>> m_doneEvent = new FinalValue<>();
  private final FinalValue<Boolean> m_finished = new FinalValue<>();

  public CompletionPromise(final JobFutureTask<RESULT> future, final ExecutorService executor) {
    m_future = future;
    m_handlers = new ArrayList<>();
    m_executor = executor;
  }

  /**
   * Invoke to release threads waiting for done state.
   */
  public void done() {
    // Release any thread waiting for a future to become 'done'.
    m_lock.lock();
    try {
      m_doneEvent.set(CompletionPromise.newDoneEvent(m_future));
      m_doneCondition.signalAll();
    }
    finally {
      m_lock.unlock();
    }

    // Notify registered handlers asynchronously.
    // Notice: Do not notify via Jobs.schedule(...), because for every job, JobManager registers a whenDone handler as well
    //         in order to unregister the associated Quartz Trigger. Otherwise, that could cause an infinite recursion.
    if (!m_handlers.isEmpty()) {
      m_executor.execute(new Runnable() {

        @Override
        public void run() {
          final Iterator<PromiseHandler<RESULT>> iterator = m_handlers.iterator();
          while (iterator.hasNext()) {
            iterator.next().notifySafe(m_doneEvent.get());
            iterator.remove();
          }
        }
      });
    }
  }

  /**
   * Invoke to release threads waiting for finish state.
   */
  public void finish() {
    m_lock.lock();
    try {
      m_finished.set(Boolean.TRUE);
      m_finishedCondition.signalAll();
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Registers the given <code>callback</code> to be invoked once the Future enters 'done' state. The callback is
   * invoked immediately and in the calling thread, if being in 'done' state at the time of invocation. Otherwise, this
   * method returns immediately, and the callback invoked upon transition into 'done' state.
   *
   * @param handler
   *          handler invoked upon transition into 'done' state.
   * @param runContext
   *          optional {@link RunContext} to invoke the handler on behalf, or <code>null</code> to not invoke on a
   *          specific {@link RunContext}.
   */
  public void whenDone(final IDoneHandler<RESULT> handler, final RunContext runContext) {
    final PromiseHandler<RESULT> promiseHandler = new PromiseHandler<>(handler, runContext);

    final boolean done;
    m_lock.lock();
    try {
      done = isDone();
      if (!done) {
        m_handlers.add(promiseHandler);
      }
    }
    finally {
      m_lock.unlock();
    }

    if (done) {
      promiseHandler.notifySafe(m_doneEvent.get());
    }
  }

  /**
   * Waits if necessary for the execution to complete, or until cancelled, and then returns its result, if available.
   *
   * @return the computed result
   * @throws ExecutionException
   *           if the computation threw an exception.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws CancellationException
   *           if the computation was cancelled.
   * @see Future#get()
   */
  public RESULT awaitDoneAndGet() throws InterruptedException, ExecutionException {
    m_lock.lockInterruptibly();
    try {
      while (!isDone()) {
        m_doneCondition.await();
      }
    }
    finally {
      m_lock.unlock();
    }

    return CompletionPromise.retrieveFinalValue(m_future);
  }

  /**
   * Waits if necessary for at most the given time for the execution to complete, or until cancelled, and then returns
   * its result, if available.
   *
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the time unit of the timeout argument
   * @return the computed result
   * @throws ExecutionException
   *           if the computation threw an exception.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws CancellationException
   *           if the computation was cancelled.
   * @throws TimeoutException
   *           if the wait timed out.
   * @see Future#get(long, TimeUnit)
   */
  public RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    Assertions.assertGreater(timeout, 0L, "Invalid timeout; must be > 0 [timeout={}]", timeout);

    m_lock.lockInterruptibly();
    try {
      long nanos = unit.toNanos(timeout);
      while (!isDone() && nanos > 0L) {
        nanos = m_doneCondition.awaitNanos(nanos);
      }
      if (nanos <= 0L) {
        throw new TimeoutException(String.format("Waiting for the Future's final value timed out [timeout=%sms]", unit.toMillis(timeout)));
      }
    }
    finally {
      m_lock.unlock();
    }

    return CompletionPromise.retrieveFinalValue(m_future);
  }

  /**
   * Waits if necessary for at most the given time for the job to finish, meaning that the job either completes normally
   * or by an exception, or that it will never commence execution due to a premature cancellation.
   *
   * @param timeout
   *          the maximum time to wait
   * @param unit
   *          the time unit of the timeout argument
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws TimeoutException
   *           if the wait timed out.
   */
  public void awaitFinished(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
    Assertions.assertGreater(timeout, 0L, "Invalid timeout; must be > 0 [timeout={}]", timeout);

    m_lock.lockInterruptibly();
    try {
      long nanos = unit.toNanos(timeout);
      while (!isFinished() && nanos > 0L) {
        nanos = m_finishedCondition.awaitNanos(nanos);
      }
      if (nanos <= 0L) {
        throw new TimeoutException(String.format("Waiting for the Future to finish timed out [timeout=%sms]", unit.toMillis(timeout)));
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Returns the internal lock object.
   */
  Lock getInternalLock() {
    return m_lock;
  }

  // ==== Internal helper methods ==== //

  /**
   * Returns <code>true</code>, if this promise is in 'done' state.
   */
  private boolean isDone() {
    return m_doneEvent.isSet();
  }

  /**
   * Returns <code>true</code>, if this promise is in 'finished' state.
   */
  public boolean isFinished() {
    return m_finished.isSet();
  }

  /**
   * Retrieves the given future's final value. This method expects the future to be in 'done' state.
   *
   * @return the computed result
   * @throws IllegalStateException
   *           if the future in not in 'done' state yet.
   * @throws ExecutionException
   *           if the computation threw an exception.
   * @throws CancellationException
   *           if the computation was cancelled.
   */
  private static <RESULT> RESULT retrieveFinalValue(final Future<RESULT> future) throws ExecutionException {
    try {
      return future.get(0, TimeUnit.NANOSECONDS);
    }
    catch (final TimeoutException | InterruptedException e) {
      throw new IllegalStateException("Unexpected: future expected to be in 'done' state", e);
    }
  }

  /**
   * Creates the {@link DoneEvent} for the given future's final value. This method expects the future to be in 'done'
   * state.
   */
  private static <RESULT> DoneEvent<RESULT> newDoneEvent(final Future<RESULT> future) {
    try {
      return new DoneEvent<>(retrieveFinalValue(future), null, false);
    }
    catch (final ExecutionException e) {
      try {
        final Exception exception = BEANS.get(JobExceptionTranslator.class).translateExecutionException(e, DefaultExceptionTranslator.class);
        return new DoneEvent<>(null, exception, false);
      }
      catch (final Error error) {
        // Errors are re-thrown by exception translator.
        return new DoneEvent<>(null, error, false);
      }
    }
    catch (final CancellationException e) {
      return new DoneEvent<>(null, null, true);
    }
  }

  /**
   * Handler associated with its {@link RunContext}.
   */
  private static class PromiseHandler<RESULT> {
    private final RunContext m_runContext;
    private final IDoneHandler<RESULT> m_callback;

    public PromiseHandler(final IDoneHandler<RESULT> callback, final RunContext runContext) {
      m_runContext = runContext;
      m_callback = callback;
    }

    /**
     * Notifies the associated handler in the specified {@link RunContext}.
     * <p>
     * This method never throws an exception.
     */
    public void notifySafe(final DoneEvent<RESULT> doneEvent) {
      try {
        if (m_runContext == null) {
          m_callback.onDone(doneEvent);
        }
        else {
          m_runContext.run(new IRunnable() {

            @Override
            public void run() throws Exception {
              m_callback.onDone(doneEvent);
            }
          });
        }
      }
      catch (final Throwable t) {
        LOG.error("Failed to notify 'done-promise' callback", t);
      }
    }
  }

  // ==== Matchers ==== //

  /**
   * Matches futures in 'done' state.
   */
  static final IFilter<JobFutureTask<?>> FUTURE_DONE_MATCHER = new IFilter<JobFutureTask<?>>() {

    @Override
    public boolean accept(final JobFutureTask<?> future) {
      return future.isDone();
    }
  };

  /**
   * Matches futures in 'done' state, and for which the 'done' event was fired.
   */
  static final IFilter<JobFutureTask<?>> PROMISE_DONE_MATCHER = new IFilter<JobFutureTask<?>>() {

    @Override
    public boolean accept(final JobFutureTask<?> future) {
      return future.getCompletionPromise().isDone();
    }
  };
}
