/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.traces;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

public class OpenTelemetryTracesExporterProperty extends AbstractStringConfigProperty {

  @Override
  public String getKey() {
    return "scout.otel.traces.exporter";
  }

  @Override
  public String description() {
    return "Configures the default exporter for traces. Default is 'none', so no traces are exported. Set value to "
        + "'otlp' to export using the OpenTelemetry protocol.";
  }

  @Override
  public String getDefaultValue() {
    return "none";
  }
}
