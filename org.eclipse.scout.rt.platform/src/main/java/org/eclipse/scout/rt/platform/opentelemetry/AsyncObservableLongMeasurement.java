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
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.context.RunContext;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

/**
 * {@link ObservableLongMeasurement} to support "long-running" observable measurements that are independent of
 * OpenTelemetry's metrics export interval.
 *
 * @see PeriodicAsyncMeasurement
 */
public final class AsyncObservableLongMeasurement implements Consumer<ObservableLongMeasurement>, AutoCloseable {

  public static AsyncObservableLongMeasurement create(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier, Supplier<Boolean> leaderElectionSupplier) {
    return create(name, callable, runContextSupplier, leaderElectionSupplier, Attributes.empty());
  }

  public static AsyncObservableLongMeasurement create(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier, Supplier<Boolean> leaderElectionSupplier, Attributes attributes) {
    return new AsyncObservableLongMeasurement(name, callable, runContextSupplier, leaderElectionSupplier, attributes);
  }

  private final Attributes m_attributes;
  private final PeriodicAsyncMeasurement<Long> m_periodicAsyncMeasurement;

  private AsyncObservableLongMeasurement(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier, Supplier<Boolean> leaderElectionSupplier, Attributes attributes) {
    m_attributes = attributes;
    m_periodicAsyncMeasurement = new PeriodicAsyncMeasurement<>(name, callable, runContextSupplier, leaderElectionSupplier);
  }

  @Override
  public void accept(ObservableLongMeasurement measurement) {
    Long value = m_periodicAsyncMeasurement.getAndNext();
    if (value != null) {
      measurement.record(value, m_attributes);
    }
  }

  @Override
  public void close() throws Exception {
    m_periodicAsyncMeasurement.close();
  }
}
