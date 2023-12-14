/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client.proxy;

import static org.eclipse.scout.rt.platform.util.Assertions.assertInstance;
import static org.eclipse.scout.rt.rest.jersey.EchoServletParameters.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.dataobject.exception.ResourceNotFoundException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.RequestSynchronizer;
import org.eclipse.scout.rt.rest.jersey.RestClientTestEchoResponse;
import org.eclipse.scout.rt.rest.jersey.TestingRestClientConfigFactory;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RestClientProxyInvocationTest {

  private WebTarget m_target;
  private JerseyTestRestClientHelper m_helper;

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  @Before
  public void before() {
    m_helper = BEANS.get(JerseyTestRestClientHelper.class);
    m_target = m_helper.target("echo");
  }

  // --- GET -> Response -------------------------------------------------------

  @Test
  public void testSyncGetOk() {
    Locale locale = Locale.GERMANY;
    String correlationId = UUID.randomUUID().toString();
    RunContexts.copyCurrent()
        .withLocale(locale)
        .withCorrelationId(correlationId)
        .run(() -> {
          try (Response response = webTargetGet(Response.Status.OK, Content.DEFAULT, Execution.SYNC)) {
            assertNotNull(response);
            RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
            assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), entity.getEcho().getCode());

            assertEquals(correlationId, response.getHeaderString(CorrelationId.HTTP_HEADER_NAME));
            assertEquals(locale.toLanguageTag(), response.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
          }
        });
  }

  @Test
  public void testSyncGetForbidden() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> webTargetGet(Status.FORBIDDEN, Content.DEFAULT, Execution.SYNC));
    assertEquals("REST Client Test: Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testSyncGetForbiddenEmptyBody() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> webTargetGet(Status.FORBIDDEN, Content.EMPTY_BODY, Execution.SYNC));
    assertEquals("Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testSyncGetNotFound() {
    ProcessingException pe = Assert.assertThrows(ProcessingException.class, () -> webTargetGet(Status.NOT_FOUND, Content.DEFAULT, Execution.SYNC));
    assertEquals("REST Client Test: Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testSyncGetNotFoundEmptyBody() {
    ProcessingException pe = Assert.assertThrows(ProcessingException.class, () -> webTargetGet(Status.NOT_FOUND, Content.EMPTY_BODY, Execution.SYNC));
    assertEquals("Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testAsyncGetOk() {
    Locale defaultLocale = NlsLocale.get();
    Locale locale = Locale.GERMANY;
    String correlationId = UUID.randomUUID().toString();
    RunContexts.copyCurrent()
        .withLocale(locale)
        .withCorrelationId(correlationId)
        .run(() -> {
          try (Response response = webTargetGet(Response.Status.OK, Content.DEFAULT, Execution.ASYNC)) {
            assertNotNull(response);
            RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
            assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), entity.getEcho().getCode());

            assertNull(response.getHeaderString(CorrelationId.HTTP_HEADER_NAME));
            assertEquals(defaultLocale.toLanguageTag(), response.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
          }
        });
  }

  @Test
  public void testAsyncGetForbidden() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> webTargetGet(Status.FORBIDDEN, Content.DEFAULT, Execution.ASYNC));
    assertEquals("REST Client Test: Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testAsyncGetForbiddenEmptyBody() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> webTargetGet(Status.FORBIDDEN, Content.EMPTY_BODY, Execution.ASYNC));
    assertEquals("Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testAsyncGetNotFound() {
    ResourceNotFoundException pe = Assert.assertThrows(ResourceNotFoundException.class, () -> webTargetGet(Status.NOT_FOUND, Content.DEFAULT, Execution.ASYNC));
    assertEquals("REST Client Test: Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testAsyncGetNotFoundEmptyBody() {
    ResourceNotFoundException pe = Assert.assertThrows(ResourceNotFoundException.class, () -> webTargetGet(Status.NOT_FOUND, Content.EMPTY_BODY, Execution.ASYNC));
    assertEquals("Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testSyncGetOkWithoutReadingEntityDefaultData() throws Exception {
    assertConnectionReleased(() -> {
      Response response = webTargetGet(Response.Status.OK, Content.DEFAULT, Execution.SYNC);
      response.close();
    });
  }

  @Test
  public void testSyncGetOkWithoutReadingEntityEmptyData() throws Exception {
    assertConnectionReleased(() -> {
      Response response = webTargetGet(Response.Status.OK, Content.EMPTY_BODY, Execution.SYNC);
      assertFalse(response.hasEntity());
      response.close();
    });
  }

  @Test
  public void testSyncGetOkWithoutReadingEntityLargeMessage() throws Exception {
    assertConnectionReleased(() -> {
      Response response = webTargetGet(Response.Status.OK, Content.LARGE_MESSAGE, Execution.SYNC);
      response.close();
    });
  }

  /**
   * Invoking a REST service starting with a {@link WebTarget}.
   */
  protected Response webTargetGet(Status status, Content content, Execution execution) throws Exception {
    WebTarget target = m_target.queryParam(STATUS, status.getStatusCode());
    if (content == Content.EMPTY_BODY) {
      target = target.queryParam(EMPTY_BODY, "true");
    }
    else if (status.getFamily() == Response.Status.Family.SUCCESSFUL && content == Content.LARGE_MESSAGE) {
      target = target.queryParam(LARGE_MESSAGE, "true");
    }

    Builder builder = target
        .request()
        .accept(MediaType.APPLICATION_JSON);

    if (execution == Execution.SYNC) {
      return builder.get();
    }
    return builder.async().get().get();
  }

  @Test
  public void testAsyncGetCancel() throws Exception {
    assertConnectionReleased(() -> {
      RequestSynchronizer requestSynchronizer = BEANS.get(RequestSynchronizer.class);
      final String requestId = requestSynchronizer.announceRequest();
      Future<Response> future = m_target
          .queryParam(STATUS, Response.Status.OK.getStatusCode())
          .queryParam(SLEEP_SEC, 2)
          .queryParam(REQUEST_ID, requestId)
          .request()
          .accept(MediaType.APPLICATION_JSON)
          .async()
          .get();
      requestSynchronizer.awaitRequest(requestId, 5);
      //noinspection resource
      Assert.assertThrows(TimeoutException.class, () -> future.get(300, TimeUnit.MILLISECONDS));
      future.cancel(true);
    });
  }

  @Test
  public void testSyncGetCancel() throws Exception {
    assertConnectionReleased(() -> {
      RequestSynchronizer requestSynchronizer = BEANS.get(RequestSynchronizer.class);
      final String requestId = requestSynchronizer.announceRequest();
      IFuture<Response> future = Jobs.schedule(() -> {
        try {
          return m_target
              .queryParam(STATUS, Response.Status.OK.getStatusCode())
              .queryParam(SLEEP_SEC, 2)
              .queryParam(REQUEST_ID, requestId)
              .request()
              .accept(MediaType.APPLICATION_JSON)
              .get();
        }
        catch (ThreadInterruptedError e) {
          // expected
        }
        return null;
      }, Jobs.newInput());
      requestSynchronizer.awaitRequest(requestId, 5);
      Assert.assertThrows(TimedOutError.class, () -> future.awaitDone(100, TimeUnit.MILLISECONDS));
      future.cancel(true);
    });
  }

  /**
   * Connection pooling provided by connectors require to completely consume the returned data of a REST service in
   * order to put leased connections back into the pool. Otherwise, resource leaks may occur and processes may even be
   * blocked.
   */
  protected void assertConnectionReleased(IRunnable runnable) throws Exception {
    Object manager = m_helper.client().getConfiguration().getProperty(TestingRestClientConfigFactory.PROP_CONNECTION_MANAGER);
    @SuppressWarnings("resource")
    PoolingHttpClientConnectionManager poolingConnectionManager = assertInstance(manager, PoolingHttpClientConnectionManager.class, "This test works with Apache HTTP client only. Adapt it for other libraries.");
    URI uri = URI.create(m_helper.getBaseUri());
    HttpHost target = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
    assertEquals(1, poolingConnectionManager.getMaxPerRoute(new HttpRoute(target)));

    // invoke actual service
    runnable.run();

    // verify that other REST services can be invoked (i.e. that the HTTP client was released to the pool)
    try {
      int statusCode = m_helper
          .rawClient()
          .target(m_helper.getBaseUri())
          .path("echo")
          .queryParam(STATUS, Response.Status.OK.getStatusCode())
          .request(MediaType.APPLICATION_JSON)
          .async()
          .get(RestClientTestEchoResponse.class)
          .get(5, TimeUnit.SECONDS)
          .getEcho()
          .getCode()
          .intValue();
      assertEquals(Response.Status.OK.getStatusCode(), statusCode);
    }
    catch (TimeoutException e) {
      fail("Subsequent REST service invocation failed. Most likely, the HTTP connection was not put back to the pool.");
    }
  }

  //--- GET -> Entity ----------------------------------------------------------

  @Test
  public void testSyncGetEntityOk() {
    int scOk = Response.Status.OK.getStatusCode();
    RestClientTestEchoResponse response = m_target
        .queryParam(STATUS, scOk)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(RestClientTestEchoResponse.class);
    assertNotNull(response);
    assertEquals(Integer.valueOf(scOk), response.getEcho().getCode());
  }

  @Test
  public void testSyncGetEntityForbidden() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> m_target
      .queryParam(STATUS, Status.FORBIDDEN.getStatusCode())
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .get(RestClientTestEchoResponse.class));
    assertEquals("REST Client Test: Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testSyncGetEntityForbiddenEmptyBody() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> m_target
      .queryParam(STATUS, Status.FORBIDDEN.getStatusCode())
      .queryParam(EMPTY_BODY, "true")
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .get(RestClientTestEchoResponse.class));
    assertEquals("Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testSyncGetEntityNotFound() {
    ProcessingException pe = Assert.assertThrows(ProcessingException.class, () -> m_target
      .queryParam(STATUS, Status.NOT_FOUND.getStatusCode())
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .get(RestClientTestEchoResponse.class));
    assertEquals("REST Client Test: Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testSyncGetEntityNotFoundEmptyBody() {
    ResourceNotFoundException pe = Assert.assertThrows(ResourceNotFoundException.class, () -> m_target
      .queryParam(STATUS, Status.NOT_FOUND.getStatusCode())
      .queryParam(EMPTY_BODY, "true")
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .get(RestClientTestEchoResponse.class));
    assertEquals("Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testAsyncGetEntityOk() throws Exception {
    int scOk = Response.Status.OK.getStatusCode();
    RestClientTestEchoResponse response = m_target
        .queryParam(STATUS, scOk)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .async()
        .get(RestClientTestEchoResponse.class)
        .get();
    assertNotNull(response);
    assertEquals(Integer.valueOf(scOk), response.getEcho().getCode());
  }

  @Test
  public void testAsyncGetEntityForbidden() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> m_target
      .queryParam(STATUS, Status.FORBIDDEN.getStatusCode())
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .async()
      .get(RestClientTestEchoResponse.class)
      .get());
    assertEquals(AccessForbiddenException.class, ee.getCause().getClass());
    assertEquals("REST Client Test: Forbidden", ((AccessForbiddenException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testAsyncGetEntityForbiddenEmptyBody() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> m_target
      .queryParam(STATUS, Status.FORBIDDEN.getStatusCode())
      .queryParam(EMPTY_BODY, "true")
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .async()
      .get(RestClientTestEchoResponse.class)
      .get());
    assertEquals(AccessForbiddenException.class, ee.getCause().getClass());
    assertEquals("Forbidden", ((AccessForbiddenException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testAsyncGetEntityNotFound() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> m_target
      .queryParam(STATUS, Status.NOT_FOUND.getStatusCode())
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .async()
      .get(RestClientTestEchoResponse.class)
      .get());
    assertEquals(ResourceNotFoundException.class, ee.getCause().getClass());
    assertEquals("REST Client Test: Not Found", ((ResourceNotFoundException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testAsyncGetEntityNotFoundEmptyBody() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> m_target
      .queryParam(STATUS, Status.NOT_FOUND.getStatusCode())
      .queryParam(EMPTY_BODY, "true")
      .request()
      .accept(MediaType.APPLICATION_JSON)
      .async()
      .get(RestClientTestEchoResponse.class)
      .get());
    assertEquals(ResourceNotFoundException.class, ee.getCause().getClass());
    assertEquals("Not Found", ((ResourceNotFoundException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testAsyncGetCallbackOk() throws Exception {
    RecordingInvocationCallback callback = webTargetAsyncGetCallback(Response.Status.OK, Content.DEFAULT);
    assertNull(callback.getThrowable());
    assertNotNull(callback.getResponse());
    assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), callback.getResponse().getEcho().getCode());
  }

  @Test
  public void testAsyncGetCallbackForbidden() throws Exception {
    RecordingInvocationCallback callback = webTargetAsyncGetCallback(Response.Status.FORBIDDEN, Content.DEFAULT);
    assertNull(callback.getResponse());
    ResponseProcessingException rpe = assertException(ResponseProcessingException.class, callback.getThrowable());
    assertException(ForbiddenException.class, rpe.getCause());
  }

  @Test
  public void testAsyncGetCallbackForbiddenEmptyBody() throws Exception {
    RecordingInvocationCallback callback = webTargetAsyncGetCallback(Response.Status.FORBIDDEN, Content.EMPTY_BODY);
    assertNull(callback.getResponse());
    ResponseProcessingException rpe = assertException(ResponseProcessingException.class, callback.getThrowable());
    assertException(ForbiddenException.class, rpe.getCause());
  }

  @Test
  public void testAsyncGetCallbackNotFound() throws Exception {
    RecordingInvocationCallback callback = webTargetAsyncGetCallback(Response.Status.NOT_FOUND, Content.DEFAULT);
    assertNull(callback.getResponse());
    ResponseProcessingException rpe = assertException(ResponseProcessingException.class, callback.getThrowable());
    assertException(NotFoundException.class, rpe.getCause());
  }

  @Test
  public void testAsyncGetCallbackNotFoundEmptyBody() throws Exception {
    RecordingInvocationCallback callback = webTargetAsyncGetCallback(Response.Status.NOT_FOUND, Content.EMPTY_BODY);
    assertNull(callback.getResponse());
    ResponseProcessingException rpe = assertException(ResponseProcessingException.class, callback.getThrowable());
    assertException(NotFoundException.class, rpe.getCause());
  }

  protected RecordingInvocationCallback webTargetAsyncGetCallback(Response.Status status, Content content) throws Exception {
    RecordingInvocationCallback callback = new RecordingInvocationCallback();
    WebTarget target = m_target.queryParam(STATUS, status.getStatusCode());
    if (content == Content.EMPTY_BODY) {
      target.queryParam(EMPTY_BODY, "true");
    }
    target
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .async()
        .get(callback);
    callback.await();
    return callback;
  }

  // --- client.invocation -> GET -> Entity ------------------------------------

  @Test
  public void testClientInvocationSyncGetEntityOk() throws Exception {
    RestClientTestEchoResponse response = clientInvocationGetEntity(Response.Status.OK, Content.DEFAULT, Execution.SYNC);
    assertNotNull(response);
    assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), response.getEcho().getCode());
  }

  @Test
  public void testClientInvocationSyncGetEntityForbidden() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> clientInvocationGetEntity(Status.FORBIDDEN, Content.DEFAULT, Execution.SYNC));
    assertEquals("REST Client Test: Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testClientInvocationSyncGetEntityForbiddenEmptyBody() {
    VetoException ve = Assert.assertThrows(VetoException.class, () -> clientInvocationGetEntity(Status.FORBIDDEN, Content.EMPTY_BODY, Execution.SYNC));
    assertEquals("Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testClientInvocationSyncGetEntityNotFound() {
    ProcessingException pe = Assert.assertThrows(ProcessingException.class, () -> clientInvocationGetEntity(Status.NOT_FOUND, Content.DEFAULT, Execution.SYNC));
    assertEquals("REST Client Test: Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testClientInvocationSyncGetEntityNotFoundEmptyBody() {
    ProcessingException pe = Assert.assertThrows(ProcessingException.class, () -> clientInvocationGetEntity(Status.NOT_FOUND, Content.EMPTY_BODY, Execution.SYNC));
    assertEquals("Not Found", pe.getDisplayMessage());
  }

  @Test
  public void testClientInvocationAsyncGetEntityOk() throws Exception {
    RestClientTestEchoResponse response = clientInvocationGetEntity(Response.Status.OK, Content.DEFAULT, Execution.ASYNC);
    assertNotNull(response);
    assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), response.getEcho().getCode());
  }

  @Test
  public void testClientInvocationAsyncGetEntityForbidden() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> clientInvocationGetEntity(Status.FORBIDDEN, Content.DEFAULT, Execution.ASYNC));
    assertEquals(AccessForbiddenException.class, ee.getCause().getClass());
    assertEquals("REST Client Test: Forbidden", ((AccessForbiddenException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testClientInvocationAsyncGetEntityForbiddenEmptyBody() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> clientInvocationGetEntity(Status.FORBIDDEN, Content.EMPTY_BODY, Execution.ASYNC));
    assertEquals(AccessForbiddenException.class, ee.getCause().getClass());
    assertEquals("Forbidden", ((AccessForbiddenException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testClientInvocationAsyncGetEntityNotFound() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> clientInvocationGetEntity(Status.NOT_FOUND, Content.DEFAULT, Execution.ASYNC));
    assertEquals(ResourceNotFoundException.class, ee.getCause().getClass());
    assertEquals("REST Client Test: Not Found", ((ResourceNotFoundException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testClientInvocationAsyncGetEntityNotFoundEmptyBody() {
    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> clientInvocationGetEntity(Status.NOT_FOUND, Content.EMPTY_BODY, Execution.ASYNC));
    assertEquals(ResourceNotFoundException.class, ee.getCause().getClass());
    assertEquals("Not Found", ((ResourceNotFoundException) ee.getCause()).getDisplayMessage());
  }

  protected RestClientTestEchoResponse clientInvocationGetEntity(Status status, Content content, Execution execution) throws Exception {
    UriBuilder uriBuilder = new UriBuilder(m_helper.getBaseUri())
        .addPath("echo")
        .parameter(STATUS, String.valueOf(status.getStatusCode()));

    if (content == Content.EMPTY_BODY) {
      uriBuilder.parameter(EMPTY_BODY, String.valueOf(true));
    }

    Builder invocationBuilder = m_helper.client()
        .invocation(Link.fromUri(uriBuilder.createURI()).build())
        .accept(MediaType.APPLICATION_JSON);

    if (execution == Execution.SYNC) {
      return invocationBuilder.get(RestClientTestEchoResponse.class);
    }
    return invocationBuilder.async().get(RestClientTestEchoResponse.class).get();
  }

  // --- invocation -> invoke -> Entity ----------------------------------------

  @Test
  public void testInvocationSyncGetEntityOk() {
    int scOk = Response.Status.OK.getStatusCode();
    Invocation invocation = m_target
        .queryParam(STATUS, scOk)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    RestClientTestEchoResponse response = invocation.invoke(RestClientTestEchoResponse.class);
    assertNotNull(response);
    assertEquals(Integer.valueOf(scOk), response.getEcho().getCode());
  }

  @Test
  public void testInvocationSyncGetEntityForbidden() {
    Invocation invocation = m_target
        .queryParam(STATUS, Response.Status.FORBIDDEN.getStatusCode())
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    VetoException ve = Assert.assertThrows(VetoException.class, () -> invocation.invoke(RestClientTestEchoResponse.class));
    assertEquals("REST Client Test: Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testInvocationSyncGetEntityForbiddenEmptyBody() {
    Invocation invocation = m_target
        .queryParam(STATUS, Response.Status.FORBIDDEN.getStatusCode())
        .queryParam(EMPTY_BODY, "true")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    VetoException ve = Assert.assertThrows(VetoException.class, () -> invocation.invoke(RestClientTestEchoResponse.class));
    assertEquals("Forbidden", ve.getDisplayMessage());
  }

  @Test
  public void testInvocationAsyncGetEntityOk() throws Exception {
    int scOk = Response.Status.OK.getStatusCode();
    Invocation invocation = m_target
        .queryParam(STATUS, scOk)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    RestClientTestEchoResponse response = invocation.submit(RestClientTestEchoResponse.class).get();
    assertNotNull(response);
    assertEquals(Integer.valueOf(scOk), response.getEcho().getCode());
  }

  @Test
  public void testInvocationAsyncGetEntityForbidden() {
    Invocation invocation = m_target
        .queryParam(STATUS, Response.Status.FORBIDDEN.getStatusCode())
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> invocation.submit(RestClientTestEchoResponse.class).get());
    assertEquals(AccessForbiddenException.class, ee.getCause().getClass());
    assertEquals("REST Client Test: Forbidden", ((AccessForbiddenException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testInvocationAsyncGetEntityForbiddenEmptyBody() {
    Invocation invocation = m_target
        .queryParam(STATUS, Response.Status.FORBIDDEN.getStatusCode())
        .queryParam(EMPTY_BODY, "true")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    ExecutionException ee = Assert.assertThrows(ExecutionException.class, () -> invocation.submit(RestClientTestEchoResponse.class).get());
    assertEquals(AccessForbiddenException.class, ee.getCause().getClass());
    assertEquals("Forbidden", ((AccessForbiddenException) ee.getCause()).getDisplayMessage());
  }

  @Test
  public void testInvocationAsyncGetCallbackOk() throws Exception {
    RecordingInvocationCallback callback = invocationAsyncGetCallback(Response.Status.OK, Content.DEFAULT);
    assertNotNull(callback.getResponse());
    assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), callback.getResponse().getEcho().getCode());
    assertNull(callback.getThrowable());
  }

  @Test
  public void testInvocationAsyncGetCallbackForbidden() throws Exception {
    RecordingInvocationCallback callback = invocationAsyncGetCallback(Response.Status.FORBIDDEN, Content.DEFAULT);
    assertNull(callback.getResponse());
    ResponseProcessingException rpe = assertException(ResponseProcessingException.class, callback.getThrowable());
    assertException(ForbiddenException.class, rpe.getCause());
  }

  @Test
  public void testInvocationAsyncGetCallbackEmptyBody() throws Exception {
    RecordingInvocationCallback callback = invocationAsyncGetCallback(Response.Status.FORBIDDEN, Content.EMPTY_BODY);
    assertNull(callback.getResponse());
    ResponseProcessingException rpe = assertException(ResponseProcessingException.class, callback.getThrowable());
    assertException(ForbiddenException.class, rpe.getCause());
  }

  protected RecordingInvocationCallback invocationAsyncGetCallback(Response.Status status, Content content) throws Exception {
    WebTarget target = m_target.queryParam(STATUS, status.getStatusCode());
    if (content == Content.EMPTY_BODY) {
      target.queryParam(EMPTY_BODY, "true");
    }

    Invocation invocation = target
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .buildGet();

    RecordingInvocationCallback callback = new RecordingInvocationCallback();
    invocation.submit(callback);
    callback.await();
    return callback;
  }

  protected enum Execution {
    SYNC, ASYNC
  }

  protected enum Content {
    DEFAULT, EMPTY_BODY, LARGE_MESSAGE
  }

  /**
   * {@link InvocationCallback} that just records the completed {@link Response} or the {@link Throwable}, if
   * unsuccessful.
   */
  protected static class RecordingInvocationCallback implements InvocationCallback<RestClientTestEchoResponse> {

    private final CountDownLatch m_latch = new CountDownLatch(1);
    private RestClientTestEchoResponse m_response;
    private Throwable m_throwable;

    @Override
    public void completed(RestClientTestEchoResponse response) {
      m_response = response;
      m_latch.countDown();
    }

    @Override
    public void failed(Throwable throwable) {
      m_throwable = throwable;
      m_latch.countDown();
    }

    public void await() throws InterruptedException {
      m_latch.await();
    }

    public RestClientTestEchoResponse getResponse() {
      return m_response;
    }

    public Throwable getThrowable() {
      return m_throwable;
    }
  }

  protected <T extends Throwable> T assertException(Class<T> expectedType, Throwable actualException) {
    if (actualException == null) {
      throw new AssertionError("Expecting [" + expectedType.getName() + "] but nothing was thrown");
    }
    if (expectedType.isInstance(actualException)) {
      return expectedType.cast(actualException);
    }
    throw new AssertionError("Expecting [" + expectedType.getName() + "] but a [" + actualException.getClass().getName() + "] was thrown", actualException);
  }
}
