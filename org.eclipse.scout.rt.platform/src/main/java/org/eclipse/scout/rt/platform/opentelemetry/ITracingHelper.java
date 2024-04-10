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

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.slf4j.Logger;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

/**
 * A helper class to provide utility methods regarding tracing.
 * <p>
 * A trace tracks a process through the system. This system may consist of several applications. A trace goes over the
 * boarder of an application.
 * </p>
 * <p>
 * A trace consists of one or several spans. A span is a unit of work. Important or interesting actions in the software
 * can be wrapped into a span. The start and end time of this action will then be recorded. Metadata can be attached to
 * a span using span attributes.
 * </p>
 */
@ApplicationScoped
public interface ITracingHelper {

  /**
   * Convenience method to create an instance of a {@link Tracer}.
   * <p>
   * A tracer is nescessary for creating spans. A tracer should be bound to the class, in which the spans are created.
   * It is like a {@link Logger} for traces. It uses the {@link GlobalOpenTelemetry} instance to create the tracer from.
   * </p>
   *
   * @param instrumentationClass
   *     class for which the {@link Tracer} should be created
   * @return the tracer instance
   */
  Tracer createTracer(Class<?> instrumentationClass);

  /**
   * Convenience method to create an instance of a {@link Tracer}.
   * <p>
   * A tracer is necessary for creating spans. A tracer should be bound to the class, in which the spans are created. It
   * is like a {@link Logger} for traces.
   * </p>
   *
   * @param openTelemetry
   *     custom {@link OpenTelemetry} instance to create the tracer from
   * @param instrumentationClass
   *     class for which the {@link Tracer} should be created
   * @return the tracer instance
   */
  Tracer createTracer(OpenTelemetry openTelemetry, Class<?> instrumentationClass);

  /**
   * Wraps the given code into a span.
   * <p>
   * Creates a new span (unit of work) with the given name and from the provided tracer. The code passed is being
   * executed within the span. The consumer accepts the span object. This gives the opportunity to add e.g. attributes
   * or events to the trace.
   * </p>
   *
   * @param tracer
   *     instance of the tracer where the span is created from
   * @param spanName
   *     name of the span
   * @param consumer
   *     code to be executed within the span, accepts the span object to add attributes or events
   */
  void wrapInSpan(Tracer tracer, String spanName, Consumer<Span> consumer);

  <T> T wrapInSpan(Tracer tracer, String spanName, Function<Span, T> function);

  void wrapInSpan(Tracer tracer, String spanName, ThrowingConsumer<Span> consumer, PlatformExceptionTranslator exceptionTranslator);

  <T> T wrapInSpan(Tracer tracer, String spanName, ThrowingFunction<Span, T> function, PlatformExceptionTranslator exceptionTranslator);

  /**
   * Adds attributes to the span from a source object.
   * <p>
   * This method provides a way to add metadata from a source object to the span, considering the correct semantic
   * conventions from OpenTelemetry. The implementation checks whether the object is an instance of a known object and
   * adds for tracing relevant data to the span.
   * </p>
   *
   * @param span
   *     span where attributes should be added
   * @param source
   *     object from where the attributes are read
   */
  <T> void appendAttributes(Span span, T source);
}
