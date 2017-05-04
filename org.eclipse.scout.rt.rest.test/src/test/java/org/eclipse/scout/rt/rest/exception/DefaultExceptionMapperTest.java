/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
  }

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
    Response response = mapper.toResponse(new Exception());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  @Test
  public void testToResponseWebApplicationException() {
    DefaultExceptionMapper mapper = new DefaultExceptionMapper();
    WebApplicationException webException = new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode());
    Response response = mapper.toResponse(webException);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void testNotifyTransaction() {
    RunContexts.empty().run(new IRunnable() {
      @Override
      public void run() throws Exception {
        assertEquals(0, ITransaction.CURRENT.get().getFailures().length);

        DefaultExceptionMapper mapper = new DefaultExceptionMapper();
        Exception exception = new Exception();
        Response response = mapper.toResponse(exception);

        // expect that transaction was notified about failure due to the exception
        assertEquals(1, ITransaction.CURRENT.get().getFailures().length);
        assertEquals(exception, ITransaction.CURRENT.get().getFailures()[0]);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
      }
    });
  }
}
