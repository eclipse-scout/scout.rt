/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.metrics;

import org.eclipse.scout.rt.platform.opentelemetry.IMetricProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.runtimetelemetry.RuntimeTelemetry;

/**
 * {@link IMetricProvider} which serves the default Java runtime environment metrics (jvm)
 *
 * @see <a href=
 * "https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/library">JVM
 * Metrics</a>
 */
public class JvmMetricProvider implements IMetricProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JvmMetricProvider.class);

  private RuntimeTelemetry m_runtimeTelemetry;

  @Override
  public void register(OpenTelemetry openTelemetry) {
    m_runtimeTelemetry = RuntimeTelemetry.create(openTelemetry);
  }

  @Override
  public void close() {
    if (m_runtimeTelemetry == null) {
      return;
    }
    try {
      m_runtimeTelemetry.close();
    }
    catch (Exception e) {
      LOG.warn("Failed to close metric observable", e);
    }
  }
}
