/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.proxy;

import static org.eclipse.scout.rt.platform.util.Assertions.assertInstance;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.dataobject.exception.ResourceNotFoundException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link ErrorDoRestClientExceptionTransformer} and {@link AbstractEntityRestClientExceptionTransformer}.
 */
public class ErrorDoRestClientExceptionTransformerTest {

  protected ErrorDoRestClientExceptionTransformer m_exceptionTransformer = BEANS.get(ErrorDoRestClientExceptionTransformer.class);

  @Test
  public void testTransformResponseNull() {
    RuntimeException rte = new RuntimeException("mock");
    assertEquals(rte, m_exceptionTransformer.transform(rte, null));
  }

  @Test
  public void testTransformJaxRsException() {
    javax.ws.rs.ProcessingException exception = new javax.ws.rs.ProcessingException("call nok");
    assertException(RemoteSystemUnavailableException.class, null, m_exceptionTransformer.transform(exception, null));

    RuntimeException mockCause = new RuntimeException("mock");
    javax.ws.rs.ProcessingException exception2 = new javax.ws.rs.ProcessingException("call nok", mockCause);
    assertException(RemoteSystemUnavailableException.class, mockCause, m_exceptionTransformer.transform(exception2, null));
  }

  @Test
  public void testTransformStatusSuccessful() {
    RuntimeException mockCause = new RuntimeException("mock");
    assertEquals(mockCause, m_exceptionTransformer.transform(mockCause, mockResponse(Status.OK)));
  }

  @Test
  public void testWithoutErrorDo() {
    String message = "mock";
    RuntimeException mockCause = new RuntimeException(message);
    ErrorResponse errorResponse = BEANS.get(ErrorResponse.class);
    RuntimeException actualException = m_exceptionTransformer.transform(mockCause, mockResponse(Status.MOVED_PERMANENTLY, errorResponse));
    assertProcessingException(ProcessingException.class, mockCause, null, 0, actualException);
    assertEquals("REST call failed: 301 Moved Permanently [severity=ERROR]", actualException.getMessage());
    assertEquals(message, actualException.getCause().getMessage());
  }

  @Test
  public void testWithoutException() {
    ErrorResponse errorResponse = BEANS.get(ErrorResponse.class);
    RuntimeException actualException = m_exceptionTransformer.transform(null, mockResponse(Status.FORBIDDEN, errorResponse));
    assertProcessingException(AccessForbiddenException.class, null, null, 0, actualException);
    assertEquals("REST call failed: 403 Forbidden [severity=ERROR]", actualException.getMessage());
  }

  @Test
  public void testTransformByResponseStatus() {
    RuntimeException mockCause = new RuntimeException("mock");
    int mockCode = 100;
    String mockTitle = "title";
    ErrorResponse errorResponse = BEANS.get(ErrorResponse.class)
        .withError(BEANS.get(ErrorDo.class)
            .withErrorCode(String.valueOf(mockCode))
            .withTitle(mockTitle)
            .withMessage("mock"));

    // Redirection (3xx)
    assertException(ProcessingException.class, mockCause, m_exceptionTransformer.transform(mockCause, mockResponse(Status.MOVED_PERMANENTLY)));
    assertProcessingException(ProcessingException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.MOVED_PERMANENTLY, errorResponse)));

    // Client errors - specially handled (4xx)
    assertProcessingException(AccessForbiddenException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.FORBIDDEN, errorResponse)));
    assertProcessingException(ResourceNotFoundException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.NOT_FOUND, errorResponse)));

    // Client errors -  commonly handled (4xx)
    assertProcessingException(VetoException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.BAD_REQUEST, errorResponse)));
    assertProcessingException(VetoException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.UNAUTHORIZED, errorResponse)));
    assertProcessingException(VetoException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.CONFLICT, errorResponse)));
    assertProcessingException(VetoException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.REQUEST_HEADER_FIELDS_TOO_LARGE, errorResponse)));

    // Client errors - commonly handled without error response (4xx)
    assertProcessingException(VetoException.class, mockCause, null, null, m_exceptionTransformer.transform(mockCause, mockResponse(Status.BAD_REQUEST)));

    // Server errors - specially handled (5xx)
    assertException(RemoteSystemUnavailableException.class, mockCause, m_exceptionTransformer.transform(mockCause, mockResponse(Status.BAD_GATEWAY)));
    assertException(RemoteSystemUnavailableException.class, mockCause, m_exceptionTransformer.transform(mockCause, mockResponse(Status.SERVICE_UNAVAILABLE)));
    assertException(RemoteSystemUnavailableException.class, mockCause, m_exceptionTransformer.transform(mockCause, mockResponse(Status.GATEWAY_TIMEOUT)));

    // Server errors -  commonly handled (5xx)
    assertException(ProcessingException.class, mockCause, m_exceptionTransformer.transform(mockCause, mockResponse(Status.INTERNAL_SERVER_ERROR)));
    assertProcessingException(ProcessingException.class, mockCause, mockTitle, mockCode, m_exceptionTransformer.transform(mockCause, mockResponse(Status.INTERNAL_SERVER_ERROR, errorResponse)));
  }

  protected Response mockResponse(Response.Status status) {
    return mockResponse(status, null);
  }

  protected Response mockResponse(Response.Status status, ErrorResponse errorResponse) {
    Response res = Mockito.mock(Response.class);
    when(res.getStatus()).thenReturn(status.getStatusCode());
    when(res.getStatusInfo()).thenReturn(status);
    when(res.readEntity(ErrorResponse.class)).thenReturn(errorResponse);
    when(res.hasEntity()).thenReturn(errorResponse != null);
    return res;
  }

  protected void assertException(Class<?> expected, Throwable cause, RuntimeException actual) {
    assertInstance(actual, expected);
    assertEquals(cause, actual.getCause());
  }

  protected void assertProcessingException(Class<? extends ProcessingException> expected, Throwable expectedCause, String expectedTitle, Integer expectedCode, RuntimeException actual) {
    ProcessingException processingException = assertInstance(actual, expected);
    assertEquals(expectedCause, actual.getCause());
    assertEquals(expectedTitle, processingException.getStatus().getTitle());
    assertEquals(expectedCode != null ? expectedCode.intValue() : 0, processingException.getStatus().getCode());
    assertEquals(0, processingException.getSuppressed().length); // nothing should be suppressed
  }
}
