/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.dataobject.exception.ResourceNotFoundException;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@RunWith(PlatformTestRunner.class)
public class DefaultExceptionMapperTest {

  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void beforeClass() {
    // Setup mocked JAX-RS runtime (without a mock, a real JAX-RS runtime needs to be on the classpath during test execution)
    RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
    RuntimeDelegate.setInstance(runtimeDelegate);
    final ResponseBuilder responseBuilder = mock(ResponseBuilder.class);
    when(runtimeDelegate.createResponseBuilder()).thenReturn(responseBuilder);

    when((responseBuilder).status(Mockito.any(Response.Status.class))).then((Answer<ResponseBuilder>) invocation -> addStatus(responseBuilder, ((Status) invocation.getArgument(0)).getStatusCode()));

    when((responseBuilder.status(Mockito.any(StatusType.class)))).then((Answer<ResponseBuilder>) invocation -> addStatus(responseBuilder, ((StatusType) invocation.getArgument(0)).getStatusCode()));

    when((responseBuilder.status(Mockito.anyInt()))).then(invocation -> addStatus(responseBuilder, invocation.getArgument(0)));

    when((responseBuilder.entity(Mockito.any(ErrorResponse.class)))).then((Answer<ResponseBuilder>) invocation -> {
      try (Response response = responseBuilder.build()) {
        when(response.readEntity(Mockito.any(Class.class))).thenReturn(invocation.getArgument(0));
        return responseBuilder;
      }
    });

    when((responseBuilder.type(Mockito.anyString()))).then((Answer<ResponseBuilder>) invocation -> responseBuilder);
  }

  @SuppressWarnings("resource")
  protected static ResponseBuilder addStatus(ResponseBuilder responseBuilder, int statusCode) {
    Response response = mock(Response.class);
    StatusType statusType = mock(StatusType.class);
    when(response.getStatus()).thenReturn(statusCode);
    when(response.getStatusInfo()).thenReturn(statusType);
    when(statusType.getReasonPhrase()).thenReturn("mock");
    when((responseBuilder).build()).thenReturn(response);
    return responseBuilder;
  }

  @AfterClass
  public static void afterClass() {
    RuntimeDelegate.setInstance(null);
  }

  @Test
  public void testToResponseInternalServerError() {
    DefaultExceptionMapper mapper = new DefaultExceptionMapper();
    try (Response response = mapper.toResponse(new Exception())) {
      assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
  }

  @Test
  public void testToResponseWebApplicationException() {
    WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
    WebApplicationException webException = new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode());
    try (Response response = mapper.toResponse(webException)) {
      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
  }

  @Test
  public void testToResponseIllegalArgumentApplicationException() {
    IllegalArgumentExceptionMapper mapper = new IllegalArgumentExceptionMapper();
    IllegalArgumentException illegalArgException = new IllegalArgumentException("foo");
    try (Response response = mapper.toResponse(illegalArgException)) {
      assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
  }

  @Test
  public void testToResponseProcessingException() {
    ProcessingExceptionMapper mapper = new ProcessingExceptionMapper();
    ProcessingException exception = new ProcessingException("foo {}", "bar", new Exception()).withTitle("hagbard").withCode(37);
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
  }

  @Test
  public void testToResponseVetoException() {
    VetoExceptionMapper mapper = new VetoExceptionMapper();
    VetoException exception = new VetoException("foo {}", "bar", new Exception()).withTitle("hagbard").withCode(37).withSeverity(IStatus.WARNING);
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      assertEquals(exception.getStatus().getTitle(), error.getTitle());
      assertEquals(exception.getStatus().getBody(), error.getMessage());
      assertEquals(exception.getStatus().getCode(), error.getErrorCodeAsInt());
      assertEquals("warning", error.getSeverity());
      assertEquals(exception.getStatus().getSeverity(), error.getSeverityAsInt());
    }
  }

  @Test
  public void testToResponseAccessForbiddenException() {
    AccessForbiddenExceptionMapper mapper = new AccessForbiddenExceptionMapper();
    AccessForbiddenException exception = new AccessForbiddenException("foo {}", "bar", new Exception()).withTitle("hagbard").withCode(37);
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      assertEquals(exception.getStatus().getTitle(), error.getTitle());
      assertEquals(exception.getStatus().getBody(), error.getMessage());
      assertEquals(exception.getStatus().getCode(), error.getErrorCodeAsInt());
    }
  }

  @Test
  public void testToResponseResourceNotFoundException() {
    ResourceNotFoundExceptionMapper mapper = new ResourceNotFoundExceptionMapper();
    ResourceNotFoundException exception = (ResourceNotFoundException) new ResourceNotFoundException("foo {}", "bar", new Exception()).withTitle("hagbard").withCode(37);
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      assertEquals(exception.getStatus().getTitle(), error.getTitle());
      assertEquals(exception.getStatus().getBody(), error.getMessage());
      assertEquals(exception.getStatus().getCode(), error.getErrorCodeAsInt());
    }
  }

  @Test
  public void testToResponseRemoteSystemUnavailableException() {
    RemoteSystemUnavailableExceptionMapper mapper = new RemoteSystemUnavailableExceptionMapper();
    RemoteSystemUnavailableException exception = new RemoteSystemUnavailableException("unavailable");
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      assertEquals(exception.getMessage(), error.getMessage());
    }
  }

  @Test
  public void testNotifyTransaction() {
    RunContexts.empty().run(() -> {
      assertEquals(0, ITransaction.CURRENT.get().getFailures().length);

      DefaultExceptionMapper mapper = new DefaultExceptionMapper();
      Exception exception = new Exception();
      try (Response response = mapper.toResponse(exception)) {

        // expect that transaction was notified about failure due to the exception
        assertEquals(1, ITransaction.CURRENT.get().getFailures().length);
        assertEquals(exception, ITransaction.CURRENT.get().getFailures()[0]);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
      }
    });
  }
}
