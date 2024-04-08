/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.common;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

public class OpenTelemetryOtlpExporterEndpointProperty extends AbstractStringConfigProperty {

  @Override
  public String getKey() {
    return "scout.otel.expoter.otlp.endpoint";
  }

  @Override
  public String description() {
    return "Configures the endpoint for exporting telemetry data with otlp.";
  }

  @Override
  public String getDefaultValue() {
    return "http://localhost:4318";
  }
}
