/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.error;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.function.Consumer;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ErrorResponseBuilderTest {

  @Test
  public void testBuildError() {
    ErrorDo error = createErrorDo().buildError();

    assertEquals("message", error.getMessage());
    assertEquals("title", error.getTitle());
    assertEquals("42", error.getErrorCode());
    assertEquals(Integer.valueOf(418), error.getHttpStatus());
    assertEquals("info", error.getSeverity());
    assertEquals(IStatus.INFO, error.getSeverityAsInt());
    assertEquals(CorrelationId.CURRENT.get(), error.getCorrelationId());
  }

  @Test
  public void testBuild() {
    testResponseBuilder(b -> {}, b -> verify(b, never()).header(any(), any()));
    testResponseBuilder(b -> b.addHeader("foo", "bar"), b -> verify(b, times(1)).header(any(), any()));
    testResponseBuilder(b -> b.addHeader("foo", "bar").addHeader("lorem", "ipsum"), b -> verify(b, times(2)).header(any(), any()));
    testResponseBuilder(b -> b.withHeaders(Collections.singletonMap("lorem", "ipsum")), b -> verify(b, times(1)).header(any(), any()));
    testResponseBuilder(b -> b.withHeaders(null), b -> verify(b, never()).header(any(), any()));
  }

  private void testResponseBuilder(Consumer<ErrorResponseBuilder> ErrorResponseBuilderModifier, Consumer<ResponseBuilder> responseBuilderExpectations) {
    ErrorResponseBuilder errorResponseBuilder = createErrorDo();
    RuntimeDelegate previousDelegate = RuntimeDelegate.getInstance();
    RuntimeDelegate spiedDelegate = spy(previousDelegate);
    ResponseBuilder mockedResponseBuilder = mock(ResponseBuilder.class, withSettings().defaultAnswer(RETURNS_SELF));
    doReturn(mockedResponseBuilder).when(spiedDelegate).createResponseBuilder();

    try {
      RuntimeDelegate.setInstance(spiedDelegate);

      ErrorResponseBuilderModifier.accept(errorResponseBuilder);
      try (Response ignored = errorResponseBuilder.build()) { // as FixtureRuntimeDelegate works with mocks we need to verify on the spy (instead on the result itself)
        responseBuilderExpectations.accept(mockedResponseBuilder);
      }

      verify(spiedDelegate, times(1)).createResponseBuilder();
    }
    finally {
      RuntimeDelegate.setInstance(previousDelegate);
    }
  }

  private ErrorResponseBuilder createErrorDo() {
    return BEANS.get(ErrorResponseBuilder.class)
        .withErrorCode(42)
        .withMessage("message")
        .withTitle("title")
        .withHttpStatus(418)
        .withSeverity(IStatus.INFO);
  }
}
