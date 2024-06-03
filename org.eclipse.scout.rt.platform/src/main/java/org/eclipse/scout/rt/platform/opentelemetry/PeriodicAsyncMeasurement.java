/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to support "long-running" observable measurements that are independent of OpenTelemetry's metrics export
 * interval. "long-running" means measurements that are based on values accessing other (external) systems, e.g.
 * database queries, and can therefore take longer than few milliseconds. To avoid a time-consuming metrics export
 * process, the measurement is done asynchronously.<br/>
 * <b>Important:</b> Prefer to use in-memory metrics instead of this type of metrics if possible.
 * <p>
 * The observation is executed asynchronously with a predefined minimum interval (see
 * {@link AsyncObservationIntervalProperty}). The schedule of this async job is controlled during the execution of the
 * metrics export. As soon as the last job trigger is outside the specified interval, the job is re-triggered during the
 * metrics export (see {@link #getAndNext()}).
 * </p>
 * <p>
 * {@link ConcurrentAsyncObservableJobProperty} is used to prevent too many parallel "long-running" observable
 * measurements, which could cause resource starvation (mainly with database connections).
 * </p>
 */
public class PeriodicAsyncMeasurement<V> implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(PeriodicAsyncMeasurement.class);

  static final String ASYNC_JOB_NAME_PATTERN = "[otel] Async measurement: {}";
  static final LazyValue<IExecutionSemaphore> ASYNC_JOB_EXECUTION_SEMAPHORE = new LazyValue<>(() -> Jobs.newExecutionSemaphore(CONFIG.getPropertyValue(ConcurrentAsyncObservableJobProperty.class)));
  static final String ASYNC_JOB_EXECUTION_HINT = PeriodicAsyncMeasurement.class.getSimpleName() + "$ASYNC_JOB";
  private static final long ASYNC_JOB_MIN_EXECUTION_EXPIRATION_TIME_MILLIS = TimeUnit.SECONDS.toMillis(10);

  private final String m_name;
  private final Callable<V> m_callable;
  private final Supplier<RunContext> m_runContextSupplier;
  private final Supplier<Boolean> m_activeOnThisNodeSupplier;
  private final long m_asyncObservationIntervalMillis;

  // In OpenTelemetry the metric collections across all readers are sequential. Therefore, no specific concurrency handling
  // is required.
  // see https://opentelemetry.io/docs/specs/otel/metrics/sdk/#exportbatch
  // and io.opentelemetry.sdk.metrics.internal.state.MeterSharedState.collectAll(RegisteredReader, MeterProviderSharedState, long)
  private volatile V m_asyncMeasurementValue;
  private final AtomicReference<IFuture<V>> m_asyncJobRef = new AtomicReference<>();
  private volatile long m_asyncJobLastTriggerTimestamp;

  public PeriodicAsyncMeasurement(String name, Callable<V> callable, Supplier<RunContext> runContextSupplier, Supplier<Boolean> activeOnThisNodeSupplier) {
    this(name, callable, runContextSupplier, activeOnThisNodeSupplier, TimeUnit.SECONDS.toMillis(CONFIG.getPropertyValue(AsyncObservationIntervalProperty.class)));
  }

  public PeriodicAsyncMeasurement(String name, Callable<V> callable, Supplier<RunContext> runContextSupplier, Supplier<Boolean> activeOnThisNodeSupplier, long asyncObservationIntervalMillis) {
    Assertions.assertNotNull(name);
    Assertions.assertNotNull(callable);
    Assertions.assertNotNull(runContextSupplier);
    m_name = name;
    m_callable = callable;
    m_runContextSupplier = runContextSupplier;
    m_activeOnThisNodeSupplier = activeOnThisNodeSupplier;
    m_asyncObservationIntervalMillis = asyncObservationIntervalMillis;
  }

  /**
   * @return <code>null</code> or the currently available measurement and trigger the next async measurement if required
   *         (see interval).
   */
  public V getAndNext() {
    if (requiresAsyncMeasurement()) {
      triggerAsyncMeasurement();
    }
    return m_asyncMeasurementValue;
  }

  @Override
  public void close() {
    IFuture<V> asyncJobRef = m_asyncJobRef.get();
    if (asyncJobRef == null) {
      return;
    }
    asyncJobRef.cancel(true);
  }

  boolean requiresAsyncMeasurement() {
    if (!isActiveOnThisNode()) {
      return false;
    }

    if (System.currentTimeMillis() - m_asyncJobLastTriggerTimestamp < m_asyncObservationIntervalMillis) {
      return false;
    }

    IFuture<V> asyncJobRef = m_asyncJobRef.get();
    if (asyncJobRef != null) {
      // async measurement job is running longer than one "interval" --> cancel and re-trigger the async measurement job
      LOG.warn("Canceling async measurement job '{}' that is running longer than {}s", m_name, TimeUnit.MILLISECONDS.toSeconds(m_asyncObservationIntervalMillis));
      asyncJobRef.cancel(true);
    }
    return true;
  }

  protected boolean isActiveOnThisNode() {
    return m_activeOnThisNodeSupplier.get();
  }

  void triggerAsyncMeasurement() {
    IFuture<V> future = Jobs
        .schedule(m_callable, Jobs
            .newInput()
            .withName(ASYNC_JOB_NAME_PATTERN, m_name)
            .withExecutionHint(ASYNC_JOB_EXECUTION_HINT)
            .withRunContext(m_runContextSupplier.get())
            .withExecutionSemaphore(ASYNC_JOB_EXECUTION_SEMAPHORE.get())
            .withExceptionHandling(BEANS.get(ExceptionHandler.class), true)
            .withExpirationTime(Math.max(m_asyncObservationIntervalMillis, ASYNC_JOB_MIN_EXECUTION_EXPIRATION_TIME_MILLIS), TimeUnit.MILLISECONDS)); // prevent execution after waiting for execution during at least one complete async job interval
    if (m_asyncJobRef.compareAndSet(null, future)) {
      LOG.debug("Scheduled async measurement job '{}'", m_name);
    }
    future.whenDone(event -> {
      if (!m_asyncJobRef.compareAndSet(future, null)) {
        LOG.warn("Async measurement job '{}' was unexpectedly terminated", m_name);
      }
      if (event.getResult() != null) {
        m_asyncMeasurementValue = event.getResult();
      }
    }, RunContexts.empty());

    m_asyncJobLastTriggerTimestamp = System.currentTimeMillis();
  }

  public static class ConcurrentAsyncObservableJobProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.otel.asyncObservableConcurrentJobCount";
    }

    @Override
    public String description() {
      return "The number of concurrent jobs that are executing observations asynchronously. The default value is 5.\n"
          + "Note: This value should be smaller than the number of max DB connections and there should be a fair amount of DB connections "
          + "available for synchronous requests.";
    }

    @Override
    public Integer getDefaultValue() {
      return 5;
    }
  }

  public static class AsyncObservationIntervalProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.otel.asyncObservationInterval";
    }

    @Override
    public String description() {
      return "Minimum interval (in seconds) at which the observations are executed asynchronously.";
    }

    @Override
    public Integer getDefaultValue() {
      return 60;
    }
  }
}
