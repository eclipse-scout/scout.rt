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

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;

public final class AsyncObservableDoubleMeasurement extends AbstractAsyncObservableMeasurement<ObservableDoubleMeasurement, Double> {

  public static AsyncObservableDoubleMeasurement create(String name, Callable<Double> callable, Supplier<RunContext> runContextSupplier) {
    return new AsyncObservableDoubleMeasurement(name, callable, runContextSupplier);
  }

  static AsyncObservableDoubleMeasurement create(String name, Callable<Double> callable, Supplier<RunContext> runContextSupplier, long asyncObservationIntervalInMs) {
    return new AsyncObservableDoubleMeasurement(name, callable, runContextSupplier, asyncObservationIntervalInMs);
  }

  private AsyncObservableDoubleMeasurement(String name, Callable<Double> callable, Supplier<RunContext> runContextSupplier) {
    super(name, callable, runContextSupplier);
  }

  private AsyncObservableDoubleMeasurement(String name, Callable<Double> callable, Supplier<RunContext> runContextSupplier, long asyncObservationIntervalInNs) {
    super(name, callable, runContextSupplier, asyncObservationIntervalInNs);
  }

  @Override
  protected void record(ObservableDoubleMeasurement measurement, Double asyncMeasurementValue) {
    measurement.record(asyncMeasurementValue);
  }
}
