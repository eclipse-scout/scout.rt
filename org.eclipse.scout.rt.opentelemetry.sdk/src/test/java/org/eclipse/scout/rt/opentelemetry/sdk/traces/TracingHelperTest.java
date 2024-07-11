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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.IThrowingConsumer;
import org.eclipse.scout.rt.platform.opentelemetry.IThrowingFunction;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * Test class for {@link TracingHelper}
 */
@RunWith(PlatformTestRunner.class)
public class TracingHelperTest {

  @Test
  public void testCreateGlobalOpenTelemetryTracerCreation() {
    // Act
    Tracer tracer = BEANS.get(ITracingHelper.class).createTracer(this.getClass());

    // Assert
    assertNotNull(tracer);
  }

  @Test
  public void testCreateTracer() {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
    when(openTelemetry.getTracer(anyString())).thenReturn(mockTracer);

    // Act
    Tracer tracer = BEANS.get(ITracingHelper.class).createTracer(openTelemetry, this.getClass());

    // Assert
    assertEquals(mockTracer, tracer);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testWrapConsumerInSpan() {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    Span mockSpan = mockSpan(mockTracer);
    Consumer<Span> mockConsumer = mock(Consumer.class);

    // Act
    BEANS.get(ITracingHelper.class).wrapInSpan(mockTracer, "testName", mockConsumer);

    // Assert
    verify(mockConsumer).accept(mockSpan);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testWrapFunctionInSpan() {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    Span mockSpan = mockSpan(mockTracer);
    String functionReturnValue = "Sucess!";
    Function<Span, String> mockFunction = mock(Function.class);
    when(mockFunction.apply(any())).thenReturn(functionReturnValue);

    // Act
    String returnedValue = BEANS.get(ITracingHelper.class).wrapInSpan(mockTracer, "testName", mockFunction);

    // Assert
    assertEquals(functionReturnValue, returnedValue);
    verify(mockFunction).apply(mockSpan);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testWrapThrowingConsumerInSpan() throws Exception {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    Span mockSpan = mockSpan(mockTracer);
    IThrowingConsumer<Span> mockConsumer = mock(IThrowingConsumer.class);

    // Act
    BEANS.get(ITracingHelper.class).wrapInThrowingSpan(mockTracer, "testName", mockConsumer);

    // Assert
    verify(mockConsumer).accept(mockSpan);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testWrapThrowingFunctionInSpan() throws Exception {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    Span mockSpan = mockSpan(mockTracer);
    String functionReturnValue = "Sucess!";
    IThrowingFunction<Span, String> mockFunction = mock(IThrowingFunction.class);
    when(mockFunction.apply(any())).thenReturn(functionReturnValue);

    // Act
    String returnedValue = BEANS.get(ITracingHelper.class).wrapInThrowingSpan(mockTracer, "testName", mockFunction);

    // Assert
    assertEquals(functionReturnValue, returnedValue);
    verify(mockFunction).apply(mockSpan);
  }

  @Test
  public void testExceptionHandlingWhenWrappingConsumerInSpan() {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    Span mockSpan = mockSpan(mockTracer);
    Consumer<Span> mockConsumer = span -> {
      throw new RuntimeException();
    };

    // Act / Assert
    assertThrows(RuntimeException.class, () -> BEANS.get(ITracingHelper.class).wrapInSpan(mockTracer, "testName", mockConsumer));
    verify(mockSpan).setStatus(eq(StatusCode.ERROR), any());
    verify(mockSpan).recordException(any());
  }

  @Test
  public void testExceptionHandlingWhenWrappingFunctionInSpan() {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    Span mockSpan = mockSpan(mockTracer);
    Function<Span, Void> mockConsumer = span -> {
      throw new RuntimeException();
    };

    // Act / Assert
    assertThrows(RuntimeException.class, () -> BEANS.get(ITracingHelper.class).wrapInSpan(mockTracer, "testName", mockConsumer));
    verify(mockSpan).setStatus(eq(StatusCode.ERROR), any());
    verify(mockSpan).recordException(any());
  }

  private Span mockSpan(Tracer mockTracer) {
    Span mockSpan = mock(Span.class);
    SpanBuilder mockSpanBuilder = mock(SpanBuilder.class);
    when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
    when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
    return mockSpan;
  }

}
