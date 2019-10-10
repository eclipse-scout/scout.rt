/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.dataobject.exception.ResourceNotFoundException;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(PlatformTestRunner.class)
public class DefaultExceptionMapperTest {

  @BeforeClass
  public static void beforeClass() {
    // Setup mocked JAX-RS runtime (without a mock, a real JAX-RS runtime needs to be on the classpath during test execution)
    RuntimeDelegate runtimeDelegate = mock(RuntimeDelegate.class);
    RuntimeDelegate.setInstance(runtimeDelegate);
    final ResponseBuilder responseBuilder = mock(ResponseBuilder.class);
    when(runtimeDelegate.createResponseBuilder()).thenReturn(responseBuilder);

    when((responseBuilder).status(Mockito.any(Response.Status.class))).then(new Answer<ResponseBuilder>() {
      @Override
      public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
        return addStatus(responseBuilder, ((Response.Status) invocation.getArgument(0)).getStatusCode());
      }
    });

    when((responseBuilder.status(Mockito.any(StatusType.class)))).then(new Answer<ResponseBuilder>() {
      @Override
      public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
        return addStatus(responseBuilder, ((Response.StatusType) invocation.getArgument(0)).getStatusCode());
      }
    });

    when((responseBuilder.status(Mockito.anyInt()))).then(new Answer<ResponseBuilder>() {
      @Override
      public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
        return addStatus(responseBuilder, ((int) invocation.getArgument(0)));
      }
    });

    when((responseBuilder.entity(Mockito.any(ErrorResponse.class)))).then(new Answer<ResponseBuilder>() {
      @SuppressWarnings("unchecked")
      @Override
      public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
        try (Response response = responseBuilder.build()) {
          when(response.readEntity(Mockito.any(Class.class))).thenReturn(invocation.getArgument(0));
          return responseBuilder;
        }
      }
    });

    when((responseBuilder.type(Mockito.anyString()))).then(new Answer<ResponseBuilder>() {
      @Override
      public ResponseBuilder answer(InvocationOnMock invocation) throws Throwable {
        return responseBuilder;
      }
    });
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
    VetoException exception = new VetoException("foo {}", "bar", new Exception()).withTitle("hagbard").withCode(37);
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      assertEquals(exception.getStatus().getTitle(), error.getTitle());
      assertEquals(exception.getStatus().getBody(), error.getMessage());
      assertEquals(exception.getStatus().getCode(), error.getCodeAsInt());
    }
  }

  @Test
  public void testToResponseAccessForbiddenException() {
    AccessForbiddenExceptionMapper mapper = new AccessForbiddenExceptionMapper();
    AccessForbiddenException exception = (AccessForbiddenException) new AccessForbiddenException("foo {}", "bar", new Exception()).withTitle("hagbard").withCode(37);
    try (Response response = mapper.toResponse(exception)) {
      assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      assertEquals(exception.getStatus().getTitle(), error.getTitle());
      assertEquals(exception.getStatus().getBody(), error.getMessage());
      assertEquals(exception.getStatus().getCode(), error.getCodeAsInt());
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
      assertEquals(exception.getStatus().getCode(), error.getCodeAsInt());
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
    RunContexts.empty().run(new IRunnable() {
      @Override
      public void run() throws Exception {
        assertEquals(0, ITransaction.CURRENT.get().getFailures().length);

        DefaultExceptionMapper mapper = new DefaultExceptionMapper();
        Exception exception = new Exception();
        try (Response response = mapper.toResponse(exception)) {

          // expect that transaction was notified about failure due to the exception
          assertEquals(1, ITransaction.CURRENT.get().getFailures().length);
          assertEquals(exception, ITransaction.CURRENT.get().getFailures()[0]);
          assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
      }
    });
  }
}
