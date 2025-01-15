/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Bean to define custom explicit bucket distribution of specific histogram metrics.
 * <p>
 * Scout's {@link org.eclipse.scout.rt.opentelemetry.sdk.OpenTelemetryInitializer} registers for each hint an
 * OpenTelemetry view.
 * </p>
 * <p>
 * Such explicit bucket definitions will be obsolete as soon as
 * <a href="https://opentelemetry.io/docs/specs/otel/metrics/data-model/#exponentialhistogram">exponential
 * histograms</a> become widely adopted.
 * </p>
 * <p>
 * <b>Attention:</b> This feature is not supported when using a OpenTelemetry Java Agent.
 * </p>
 *
 * @see io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
 * @see io.opentelemetry.sdk.metrics.InstrumentSelector
 * @see io.opentelemetry.sdk.metrics.View
 * @see <a href="https://opentelemetry.io/docs/specs/otel/metrics/sdk/#view">OpenTelemetry Metrics: View</a>
 * @see <a href=
 * "https://opentelemetry.io/docs/specs/otel/metrics/sdk/#explicit-bucket-histogram-aggregation">OpenTelemetry
 * Metrics: Explicit Bucket Histogram Aggregation</a>
 */
@ApplicationScoped
public interface IHistogramViewHintProvider {

  /**
   * Select instruments with the given {@code name}.
   * <p>
   * Instrument name may contain the wildcard characters {@code *} and {@code ?} with the following matching criteria:
   * <ul>
   * <li>{@code *} matches 0 or more instances of any character
   * <li>{@code ?} matches exactly one instance of any character
   * </ul>
   * </p>
   *
   * @see io.opentelemetry.sdk.metrics.InstrumentSelector
   */
  String getInstrumentName();

  /**
   * @return A list of (inclusive) upper bounds for the histogram. Should be in order from lowest to highest.
   * @see io.opentelemetry.sdk.metrics.Aggregation#explicitBucketHistogram(List)
   */
  List<Double> getExplicitBuckets();
}
