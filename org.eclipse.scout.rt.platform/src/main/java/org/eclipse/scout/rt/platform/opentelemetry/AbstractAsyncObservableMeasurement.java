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
import java.util.function.Consumer;
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
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.metrics.ObservableMeasurement;

/**
 * {@link ObservableMeasurement} to support "long-running" observable measurements that are independent of
 * OpenTelemetry's metrics export interval. "long-running" means measurements that are based on values accessing other
 * (external) systems, e.g. database queries, and can therefore take longer than few milliseconds. To avoid a
 * time-consuming metrics export process, the measurement is done asynchronously.<br/>
 * <b>Important:</b> Prefer to use in-memory metrics instead of this type of metrics if possible.
 * <p>
 * The observation is executed asynchronously with a predefined minimum interval (see
 * {@link AsyncObservationIntervalProperty}). The schedule of this async job is controlled during the execution of the
 * metrics export. As soon as the last job trigger is outside the specified interval, the job is re-triggered during the
 * metrics export (see {@link #accept(ObservableMeasurement)}).
 * </p>
 * <p>
 * {@link ConcurrentAsyncObservableJobProperty} is used to prevent too many parallel "long-running" observable
 * measurements, which could cause resource starvation (mainly with database connections).
 * </p>
 */
public abstract class AbstractAsyncObservableMeasurement<M extends ObservableMeasurement, V> implements Consumer<M> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractAsyncObservableMeasurement.class);

  static final String ASYNC_JOB_NAME_PATTERN = "[otel] Async measurement: {}";
  static final LazyValue<IExecutionSemaphore> ASYNC_JOB_EXECUTION_SEMAPHORE = new LazyValue<>(() -> Jobs.newExecutionSemaphore(CONFIG.getPropertyValue(ConcurrentAsyncObservableJobProperty.class)));
  static final String ASYNC_JOB_EXECUTION_HINT = AbstractAsyncObservableMeasurement.class.getSimpleName() + "$ASYNC_JOB";

  private final String m_name;
  private final Callable<V> m_callable;
  private final Supplier<RunContext> m_runContextSupplier;
  private final long m_asyncObservationIntervalInMs;

  private volatile V m_asyncMeasurementValue;
  private final AtomicReference<IFuture<V>> m_asyncJobRef = new AtomicReference<>();
  private long m_asyncJobLastTriggerTimestamp;

  protected AbstractAsyncObservableMeasurement(String name, Callable<V> callable, Supplier<RunContext> runContextSupplier) {
    this(name, callable, runContextSupplier, TimeUnit.SECONDS.toMillis(CONFIG.getPropertyValue(AsyncObservationIntervalProperty.class)));
  }

  protected AbstractAsyncObservableMeasurement(String name, Callable<V> callable, Supplier<RunContext> runContextSupplier, long asyncObservationIntervalInMs) {
    m_name = name;
    m_callable = callable;
    m_runContextSupplier = runContextSupplier;
    m_asyncObservationIntervalInMs = asyncObservationIntervalInMs;
  }

  @Override
  public void accept(M measurement) {
    if (requiresAsyncMeasurement()) {
      triggerAsyncMeasurement();
    }
    if (m_asyncMeasurementValue != null) {
      record(measurement, m_asyncMeasurementValue);
    }
  }

  protected abstract void record(M measurement, V asyncMeasurementValue);

  public void close() {
    IFuture<V> asyncJobRef = m_asyncJobRef.get();
    if (asyncJobRef == null) {
      return;
    }
    asyncJobRef.cancel(true);
  }

  boolean requiresAsyncMeasurement() {
    if (System.currentTimeMillis() - m_asyncJobLastTriggerTimestamp < m_asyncObservationIntervalInMs) {
      return false;
    }

    IFuture<V> asyncJobRef = m_asyncJobRef.get();
    if (asyncJobRef != null) {
      // async measurement job is running longer than one "interval" --> cancel and re-trigger the async measurement job
      LOG.warn("Cancel async measurement job '{}' which is running for more than {}s", m_name, TimeUnit.MILLISECONDS.toSeconds(m_asyncObservationIntervalInMs));
      asyncJobRef.cancel(true);
    }
    return true;
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
            .withExpirationTime(m_asyncObservationIntervalInMs, TimeUnit.SECONDS)); // prevent execution after waiting for execution during at least one whole async job interval
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
