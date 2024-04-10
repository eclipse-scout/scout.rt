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
import java.util.function.Function;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.opentelemetry.ISpanAttributeMapper;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;
import org.eclipse.scout.rt.platform.opentelemetry.ThrowingConsumer;
import org.eclipse.scout.rt.platform.opentelemetry.ThrowingFunction;

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
    wrapInSpan(tracer, spanName, (ThrowingConsumer<Span>) span -> consumer.accept(span), BEANS.get(PlatformExceptionTranslator.class));
  }

  @Override
  public <T> T wrapInSpan(Tracer tracer, String spanName, Function<Span, T> function) {
    return wrapInSpan(tracer, spanName, (ThrowingFunction<Span, T>) span -> function.apply(span), BEANS.get(PlatformExceptionTranslator.class));
  }

  @Override
  public void wrapInSpan(Tracer tracer, String spanName, ThrowingConsumer<Span> consumer, PlatformExceptionTranslator exceptionTranslator) {
    wrapInSpan(tracer, spanName, span -> {
      consumer.accept(span);
      return null;
    }, exceptionTranslator);
  }

  @Override
  public <T> T wrapInSpan(Tracer tracer, String spanName, ThrowingFunction<Span, T> function, PlatformExceptionTranslator exceptionTranslator) {
    Span span = tracer.spanBuilder(spanName).startSpan();
    try (Scope ignored = span.makeCurrent()) {
      try {
        return function.apply(span);
      }
      catch (Exception e) {
        throw exceptionTranslator.translate(e);
      }
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
        .filter(mapper -> getGenericClass(mapper).equals(source.getClass()))
        .forEach(mapper -> mapper.addAttribute(span, source));
  }

  @SuppressWarnings("unchecked")
  private <T> Class<T> getGenericClass(ISpanAttributeMapper<T> mapper) {
    Type interfaceClass = mapper.getClass().getGenericInterfaces()[0];
    return ((Class<T>) ((ParameterizedType) interfaceClass).getActualTypeArguments()[0]);
  }
}
