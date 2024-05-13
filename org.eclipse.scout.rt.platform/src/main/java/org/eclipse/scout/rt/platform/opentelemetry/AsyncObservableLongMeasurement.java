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
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.context.RunContext;

import io.opentelemetry.api.metrics.ObservableLongMeasurement;

public final class AsyncObservableLongMeasurement extends AbstractAsyncObservableMeasurement<ObservableLongMeasurement, Long> {

  public static AsyncObservableLongMeasurement create(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier) {
    return new AsyncObservableLongMeasurement(name, callable, runContextSupplier);
  }

  static AsyncObservableLongMeasurement create(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier, long asyncObservationIntervalInMs) {
    return new AsyncObservableLongMeasurement(name, callable, runContextSupplier, asyncObservationIntervalInMs);
  }

  private AsyncObservableLongMeasurement(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier) {
    super(name, callable, runContextSupplier);
  }

  private AsyncObservableLongMeasurement(String name, Callable<Long> callable, Supplier<RunContext> runContextSupplier, long asyncObservationIntervalInNs) {
    super(name, callable, runContextSupplier, asyncObservationIntervalInNs);
  }

  @Override
  protected void record(ObservableLongMeasurement measurement, Long asyncMeasurementValue) {
    measurement.record(asyncMeasurementValue);
  }
}
