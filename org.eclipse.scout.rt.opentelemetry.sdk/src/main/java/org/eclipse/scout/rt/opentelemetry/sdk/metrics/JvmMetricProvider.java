/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.metrics;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.opentelemetry.IMetricProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Classes;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Cpu;
import io.opentelemetry.instrumentation.runtimemetrics.java8.GarbageCollector;
import io.opentelemetry.instrumentation.runtimemetrics.java8.MemoryPools;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Threads;

/**
 * {@link IMetricProvider} which serves the default Java runtime environment metrics (jvm)
 *
 * @see <a href=
 *      "https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/runtime-telemetry-java8/library">JVM
 *      Metrics</a>
 */
public class JvmMetricProvider implements IMetricProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JvmMetricProvider.class);

  private final List<AutoCloseable> m_observables = new ArrayList<>();

  @Override
  public void register(OpenTelemetry openTelemetry) {
    // see https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/runtime-telemetry-java8/library#usage
    m_observables.addAll(Classes.registerObservers(openTelemetry));
    m_observables.addAll(Cpu.registerObservers(openTelemetry));
    m_observables.addAll(MemoryPools.registerObservers(openTelemetry));
    m_observables.addAll(Threads.registerObservers(openTelemetry));
    m_observables.addAll(GarbageCollector.registerObservers(openTelemetry));
  }

  @Override
  public void close() {
    for (AutoCloseable observable : m_observables) {
      try {
        observable.close();
      }
      catch (Exception e) {
        LOG.warn("Failed to close metric observable", e);
      }
    }
  }
}
