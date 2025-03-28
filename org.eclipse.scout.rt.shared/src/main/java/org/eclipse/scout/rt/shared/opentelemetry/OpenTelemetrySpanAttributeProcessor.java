/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.opentelemetry;

import static org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryContextProcessor.NOOP;

import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryProperties.OpenTelemetrySpanAttributeProcessorEnabledProperty;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryProperties.OpenTelemetryTracingEnabledProperty;
import org.eclipse.scout.rt.shared.ISession;

import io.opentelemetry.api.trace.Span;

/**
 * This Processor adds the user id to the current span. This processor does nothing if it is not explicitly enabled.
 *
 * @See OpenTelemetrySpanAttributeProcessorEnabledProperty
 * @see OpenTelemetryTracingEnabledProperty
 */
public class OpenTelemetrySpanAttributeProcessor implements ICallableDecorator {

  @Override
  public IUndecorator decorate() throws Exception {
    if (!CONFIG.getPropertyValue(OpenTelemetrySpanAttributeProcessorEnabledProperty.class).booleanValue()) {
      return NOOP;
    }

    Span current = Span.current();

    if (Span.getInvalid() == current) {
      return NOOP;
    }

    current.setAttribute(getUserIdKey(), getUserIdValue());

    return () -> current.setAttribute(getUserIdKey(), null);
  }

  /***
   * @see <a href="https://opentelemetry.io/docs/specs/semconv/attributes-registry/user/">OpenTelemetry Convention for User attributes</a>
   */
  protected String getUserIdKey() {
    return "user.name";
  }

  protected String getUserIdValue() {
    final ISession currentSession = ISession.CURRENT.get();
    return currentSession != null ? currentSession.getUserId() : null;
  }
}
