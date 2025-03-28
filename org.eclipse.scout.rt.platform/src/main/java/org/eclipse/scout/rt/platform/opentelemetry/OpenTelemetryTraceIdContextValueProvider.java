/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryProperties.OpenTelemetryTracingEnabledProperty;
import org.slf4j.MDC;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

/**
 * This class provides the {@link SpanContext#getTraceId()} to be set into the <code>diagnostic context map</code> for
 * logging purpose. The trace id is only set if tracing is enabled and a valid span context is available.
 *
 * @see OpenTelemetryTracingEnabledProperty
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class OpenTelemetryTraceIdContextValueProvider implements IDiagnosticContextValueProvider {
  public static final String KEY = "opentelemetry.trace.id";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    if (CONFIG.getPropertyValue(OpenTelemetryTracingEnabledProperty.class).booleanValue() && isValidSpanContext()) {
      return Span.current().getSpanContext().getTraceId();
    }
    return null;
  }

  private boolean isValidSpanContext() {
    SpanContext spanContext = Span.current().getSpanContext();
    return spanContext != null && spanContext.isValid();
  }
}
