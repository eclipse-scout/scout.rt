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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ISpanAttributeMapper;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

@RunWith(PlatformTestRunner.class)
public class TracingHelperTest {

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
  public void testWrapRunnableInSpan() {
    // Arrange
    Tracer mockTracer = mock(Tracer.class);
    mockSpan(mockTracer); // Also mocks the span builder
    Runnable mockRunnable = mock(Runnable.class);

    // Act
    BEANS.get(ITracingHelper.class).wrapInSpan(mockTracer, "testName", mockRunnable);

    // Assert
    verify(mockRunnable).run();
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
  public void testExceptionHandlingWhenWrappingInSpan() {
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
  public void appendAttributes() {
    // Arrange
    String sourceObject = "Lorem Ipsum";
    Span mockSpan = mock(Span.class);
    BEANS.getBeanManager().registerClass(TestSpanAttributeMapper.class);

    // Act
    BEANS.get(ITracingHelper.class).appendAttributes(mockSpan, sourceObject);

    // Assert
    verify(mockSpan).setAttribute("string.legth", sourceObject.length());
    verify(mockSpan).setAttribute("object.hash_code", sourceObject.hashCode());
  }

  private Span mockSpan(Tracer mockTracer) {
    Span mockSpan = mock(Span.class);
    SpanBuilder mockSpanBuilder = mock(SpanBuilder.class);
    when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
    when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
    return mockSpan;
  }

  public static class TestSpanAttributeMapper implements ISpanAttributeMapper<String> {
    @Override
    public void addAttribute(Span span, String source) {
      span.setAttribute("string.legth", source.length());
      span.setAttribute("object.hash_code", source.hashCode());
    }
  }
}
