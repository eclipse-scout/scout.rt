/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;

public final class OpenTelemetryProperties {

  private OpenTelemetryProperties() {
  }

  public static class OpenTelemetryTracingEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.otel.tracing.enabled";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String description() {
      return "Property to specify if the application is using the Scout OpenTelemetry Tracing. Default is false.";
    }
  }
}
