/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Wrapper around a {@link CompletionStage}, that provides scout-like behaviour for blocking operations (i.e. similar to
 * {@link IFuture}). The original {@link CompletionStage} can be obtained by {@link #unwrap()} and the
 * {@link CompletableFuture} by {@link #toCompletableFuture()}, respectively.
 *
 * @param <RESULT> the result type of values this instance provides
 */
public class Completable<RESULT> {

  private final CompletionStage<RESULT> m_completionStage;

  /**
   * Creates a new instance that is already completed with the given value.
   *
   * @see CompletableFuture#completedFuture(Object)
   */
  public static <RESULT> Completable<RESULT> completed(RESULT value) {
    return wrap(CompletableFuture.completedFuture(value));
  }

  /**
   * Creates a new instance for the given {@link CompletionStage}.
   */
  public static <RESULT> Completable<RESULT> wrap(CompletionStage<RESULT> cs) {
    return new Completable<>(cs);
  }

  /**
   * Returns a new instance for that is completed when all the given Completable instances complete.
   *
   * @see CompletableFuture#allOf(CompletableFuture[])
   */
  public static Completable<Void> allOf(Stream<Completable<?>> stream) {
    CompletableFuture<?>[] futures = stream.map(Completable::toCompletableFuture).toArray(i -> new CompletableFuture<?>[i]);
    return wrap(CompletableFuture.allOf(futures));
  }

  protected Completable(CompletionStage<RESULT> completionStage) {
    m_completionStage = assertNotNull(completionStage, "completionStage is required");
  }

  /**
   * @return the wrapped {@link CompletionStage}
   */
  public CompletionStage<RESULT> unwrap() {
    return m_completionStage;
  }

  /**
   * Converts the wrapped {@link CompletionStage} into a {@link CompletableFuture}.
   *
   * @see CompletionStage#toCompletableFuture()
   */
  public CompletableFuture<RESULT> toCompletableFuture() {
    return unwrap().toCompletableFuture();
  }

  // --- non-blocking api methods (like CompletionStage) ----------------------

  // Note: not all CompletionStage methods are mapped yet

  /**
   * @see CompletionStage#thenApply(Function)
   */
  public <U> Completable<U> thenApply(Function<? super RESULT, ? extends U> fn) {
    return wrap(unwrap().thenApply(fn));
  }

  /**
   * @see CompletionStage#thenAccept(Consumer)
   */
  public Completable<Void> thenAccept(Consumer<? super RESULT> action) {
    return wrap(unwrap().thenAccept(action));
  }

  /**
   * @see CompletionStage#thenRun(Runnable)
   */
  public Completable<Void> thenRun(Runnable action) {
    return wrap(unwrap().thenRun(action));
  }

  /**
   * @see CompletionStage#handle(BiFunction)
   */
  public <U> Completable<U> handle(BiFunction<? super RESULT, Throwable, ? extends U> fn) {
    return wrap(unwrap().handle(fn));
  }

  /**
   * @see CompletionStage#whenComplete(BiConsumer)
   */
  public Completable<RESULT> whenComplete(BiConsumer<? super RESULT, ? super Throwable> action) {
    return wrap(unwrap().whenComplete(action));
  }

  /**
   * @see CompletionStage#exceptionally(Function)
   */
  public Completable<RESULT> exceptionally(Function<Throwable, ? extends RESULT> fn) {
    return wrap(unwrap().exceptionally(fn));
  }

  // --- blocking api methods (like IFuture) ----------------------------------

  /**
   * @see Future#isDone()
   */
  public boolean isDone() {
    return toCompletableFuture().isDone();
  }

  /**
   * @see Future#isCancelled()
   */
  public boolean isCancelled() {
    return toCompletableFuture().isCancelled();
  }

  /**
   * Returns the result value if completed or throws the exception transformed by
   * {@link DefaultRuntimeExceptionTranslator}. If not yet completed, the given {@code valueIfAbsent} is returned.
   *
   * @param valueIfAbsent
   *     the value to return if not completed
   * @return the result value, if completed, else the given valueIfAbsent
   * @throws FutureCancelledError
   *     if the computation was cancelled
   * @throws RuntimeException
   *     if this future completed exceptionally or a completion computation threw an exception
   */
  public RESULT getNow(RESULT valueIfAbsent) {
    try {
      return toCompletableFuture().getNow(valueIfAbsent);
    }
    catch (final CompletionException e) {
      throw translateCompletionException(e, DefaultRuntimeExceptionTranslator.class);
    }
    catch (final CancellationException e) {
      throw translateCancellationException(e);
    }
  }

  /**
   * Waits if necessary for the completable or until it is cancelled. This method does not throw an exception in case
   * the computation failed or if it was cancelled.
   *
   * @throws ThreadInterruptedError
   *     if the current thread was interrupted while waiting.
   */
  public void awaitDone() {
    try {
      toCompletableFuture().get();
    }
    catch (final ExecutionException | CancellationException e) { // NOSONAR
      // nop: not interested in the actual result.
    }
    catch (final InterruptedException e) {
      restoreInterrupted();
      throw translateInterruptedException(e);
    }
  }

  /**
   * Waits if necessary for at most the given time for the completable or until it is cancelled. This method does not
   * throw an exception in case the computation failed or if it was cancelled.
   *
   * @param timeout
   *     the maximal time to wait for the completable.
   * @param unit
   *     unit of the timeout.
   * @throws ThreadInterruptedError
   *     if the current thread was interrupted while waiting.
   * @throws TimedOutError
   *     if the wait timed out.
   */
  public void awaitDone(long timeout, TimeUnit unit) {
    try {
      toCompletableFuture().get(timeout, unit);
    }
    catch (final ExecutionException | CancellationException e) {
      // nop: not interested in the actual result.
    }
    catch (final InterruptedException e) {
      restoreInterrupted();
      throw translateInterruptedException(e);
    }
    catch (final TimeoutException e) {
      throw translateTimeoutException(e, timeout, unit);
    }
  }

  /**
   * Waits if necessary for the completable and then returns its result, or throws the exception transformed by
   * {@link DefaultRuntimeExceptionTranslator}. A {@link FutureCancelledError} is thrown when cancelled or a
   * {@link ThreadInterruptedError} if the current thread was interrupted while waiting.
   *
   * @return the completable's result.
   * @throws ThreadInterruptedError
   *     if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *     if the completable was cancelled.
   * @throws RuntimeException
   *     if the completable threw an exception, translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  public RESULT awaitDoneAndGet() {
    return awaitDoneAndGet(DefaultRuntimeExceptionTranslator.class);
  }

  /**
   * Waits if necessary for the completable and then returns its result, or throws the exception transformed by the
   * given {@link IExceptionTranslator}. A {@link FutureCancelledError} is thrown when cancelled or a
   * {@link ThreadInterruptedError} if the current thread was interrupted while waiting.
   *
   * @param exceptionTranslator
   *     to translate the callable's exception, if any is thrown
   * @return the completable's result.
   * @throws ThreadInterruptedError
   *     if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *     if the completable was cancelled.
   * @throws EXCEPTION
   *     if the completable threw an exception, translated by {@link IExceptionTranslator}.
   */
  public <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    try {
      return toCompletableFuture().get();
    }
    catch (final ExecutionException e) {
      throw translateExecutionException(e, exceptionTranslator);
    }
    catch (final CancellationException e) {
      throw translateCancellationException(e);
    }
    catch (final InterruptedException e) {
      restoreInterrupted();
      throw translateInterruptedException(e);
    }
  }

  /**
   * Waits if necessary at most the given time for the completable and then returns its result, or throws the exception
   * transformed by {@link DefaultRuntimeExceptionTranslator}. A {@link FutureCancelledError} is thrown when cancelled
   * or a {@link ThreadInterruptedError} if the current thread was interrupted while waiting. A
   * {@link ThreadInterruptedError} is thrown if the current thread was interrupted while waiting.
   *
   * @param timeout
   *     the maximal time to wait for the completable.
   * @param unit
   *     unit of the timeout.
   * @return the completable's result.
   * @throws ThreadInterruptedError
   *     if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *     if the job was cancelled.
   * @throws TimedOutError
   *     if the wait timed out.
   * @throws RuntimeException
   *     if the completable threw an exception, translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  public RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit) {
    return awaitDoneAndGet(timeout, unit, DefaultRuntimeExceptionTranslator.class);
  }

  /**
   * Waits if necessary at most the given time for the completable and then returns its result, or throws the exception
   * transformed by the given {@link IExceptionTranslator}. A {@link FutureCancelledError} is thrown when cancelled or a
   * {@link ThreadInterruptedError} if the current thread was interrupted while waiting. A
   * {@link ThreadInterruptedError} is thrown if the current thread was interrupted while waiting.
   *
   * @param timeout
   *     the maximal time to wait for the completable.
   * @param unit
   *     unit of the timeout.
   * @return the completable's result.
   * @throws ThreadInterruptedError
   *     if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *     if the job was cancelled.
   * @throws TimedOutError
   *     if the wait timed out.
   * @throws EXCEPTION
   *     if the completable threw an exception, translated by {@link IExceptionTranslator}.
   */
  public <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit, final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    try {
      return toCompletableFuture().get(timeout, unit);
    }
    catch (final ExecutionException e) {
      throw translateExecutionException(e, exceptionTranslator);
    }
    catch (final CancellationException e) {
      throw translateCancellationException(e);
    }
    catch (final InterruptedException e) {
      restoreInterrupted();
      throw translateInterruptedException(e);
    }
    catch (final TimeoutException e) {
      throw translateTimeoutException(e, timeout, unit);
    }
  }

  // --- exception handling ---------------------------------------------------

  private FutureCancelledError translateCancellationException(CancellationException e) {
    return new FutureCancelledError("completable was cancelled", e);
  }

  private ThreadInterruptedError translateInterruptedException(final InterruptedException e) {
    return new ThreadInterruptedError("Interrupted while waiting for a completable", e);
  }

  private TimedOutError translateTimeoutException(final TimeoutException e, final long timeout, final TimeUnit unit) {
    return new TimedOutError("Failed to wait for a completable because the maximal wait time elapsed", e).withContextInfo("timeout", "{}ms", unit.toMillis(timeout));
  }

  private <EXCEPTION extends Throwable> EXCEPTION translateExecutionException(final ExecutionException e, final Class<? extends IExceptionTranslator<EXCEPTION>> translator) {
    return BEANS.get(translator).translate(e); // Do not unwrap ExecutionException here because to be done by translator, so that the submitter can also work with ExecutionException, e.g. by using NullExceptionTranslator.
  }

  private <EXCEPTION extends Throwable> EXCEPTION translateCompletionException(CompletionException e, Class<? extends IExceptionTranslator<EXCEPTION>> translator) {
    Throwable t = e;
    while (t instanceof CompletionException || t instanceof ExecutionException) {
      t = t.getCause();
    }
    if (t == null) {
      t = e;
    }
    return BEANS.get(translator).translate(t);
  }

  /**
   * Restores the thread's interrupted state which is cleared by catching {@link InterruptedException}.
   */
  private void restoreInterrupted() {
    Thread.currentThread().interrupt();
  }
}
