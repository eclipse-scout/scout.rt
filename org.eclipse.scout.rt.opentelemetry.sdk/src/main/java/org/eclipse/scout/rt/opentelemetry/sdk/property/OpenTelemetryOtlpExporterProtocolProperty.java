/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.property;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

public class OpenTelemetryOtlpExporterProtocolProperty extends AbstractStringConfigProperty {

  @Override
  public String getKey() {
    return "scout.otel.exporter.otlp.protocol";
  }

  @Override
  public String description() {
    return "Configures the protocol for exporting telemetry data with otlp."
        + " Possible values are 'http/protobuf', 'http/json' or 'grpc'."
        + " Default is 'http/protobuf'";
  }

  @Override
  public String getDefaultValue() {
    return "http/protobuf";
  }
}
