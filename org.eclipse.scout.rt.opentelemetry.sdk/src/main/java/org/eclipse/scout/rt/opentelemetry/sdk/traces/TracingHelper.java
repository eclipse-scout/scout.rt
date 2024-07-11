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

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.opentelemetry.IThrowingConsumer;
import org.eclipse.scout.rt.platform.opentelemetry.IThrowingFunction;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class TracingHelper implements ITracingHelper {

  @Override
  public Tracer createTracer(Class<?> instrumentationClass) {
    return createTracer(GlobalOpenTelemetry.get(), instrumentationClass);
  }

  @Override
  public Tracer createTracer(OpenTelemetry openTelemetry, Class<?> instrumentationClass) {
    return openTelemetry.getTracer(instrumentationClass.getName());
  }

  @Override
  public void wrapInSpan(Tracer tracer, String spanName, Consumer<Span> consumer) {
    try {
      wrapInThrowingSpan(tracer, spanName, (IThrowingConsumer<Span>) span -> consumer.accept(span));
    }
    catch (Exception e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public <T> T wrapInSpan(Tracer tracer, String spanName, Function<Span, T> function) {
    try {
      return wrapInThrowingSpan(tracer, spanName, (IThrowingFunction<Span, T>) span -> function.apply(span));
    }
    catch (Exception e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public void wrapInThrowingSpan(Tracer tracer, String spanName, IThrowingConsumer<Span> consumer) throws Exception {
    wrapInThrowingSpan(tracer, spanName, span -> {
      consumer.accept(span);
      return null;
    });
  }

  @Override
  public <T> T wrapInThrowingSpan(Tracer tracer, String spanName, IThrowingFunction<Span, T> function) throws Exception {
    Span span = tracer.spanBuilder(spanName).startSpan();
    try (Scope ignored = span.makeCurrent()) {
      return function.apply(span);
    }
    catch (Throwable t) {
      span.setStatus(StatusCode.ERROR, t.getMessage());
      span.recordException(t);
      throw t;
    }
    finally {
      span.end();
    }
  }
}
