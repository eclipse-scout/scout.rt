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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ISpanAttributeMapper;
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
  public void wrapInSpan(Tracer tracer, String spanName, Runnable runnable) {
    Span span = tracer.spanBuilder(spanName).startSpan();
    try (Scope ignored = span.makeCurrent()) {
      runnable.run();
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

  @Override
  public void wrapInSpan(Tracer tracer, String spanName, Consumer<Span> consumer) {
    Span span = tracer.spanBuilder(spanName).startSpan();
    try (Scope ignored = span.makeCurrent()) {
      consumer.accept(span);
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

  @Override
  @SuppressWarnings("unchecked")
  public <T> void appendAttributes(Span span, T source) {
    BEANS.all(ISpanAttributeMapper.class).stream()
        .filter(mapper -> getGenericClassType(mapper).equals(source.getClass()))
        .forEach(mapper -> mapper.addAttribute(span, source));
  }

  @SuppressWarnings("unchecked")
  private <T> Class<T> getGenericClassType(ISpanAttributeMapper<T> mapper) {
    Type interfaceClass = mapper.getClass().getGenericInterfaces()[0];
    return ((Class<T>) ((ParameterizedType) interfaceClass).getActualTypeArguments()[0]);
  }
}
