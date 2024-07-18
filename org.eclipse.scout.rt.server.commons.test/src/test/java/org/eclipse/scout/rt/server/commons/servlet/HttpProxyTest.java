/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import static java.util.Collections.*;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.EnumerationUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.shared.http.async.AbstractAsyncHttpClientManager;
import org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager;
import org.eclipse.scout.rt.shared.http.async.ForceHttp2DefaultAsyncHttpClientManager;
import org.eclipse.scout.rt.shared.http.async.H2AsyncHttpClientManager;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.NonParameterized;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.ParameterizedPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(ParameterizedPlatformTestRunner.class)
public class HttpProxyTest {

  private HttpProxy m_proxy;
  private Server m_server;
  private HandlerCollection m_handlerCollection;

  private HttpProxyTestParameter m_httpProxyTestParameter;

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    return List.of(
        new HttpProxyTestParameter(BEANS.get(DefaultAsyncHttpClientManager.class), server -> new ServerConnector(server, new HttpConnectionFactory())),
        new HttpProxyTestParameter(BEANS.get(H2AsyncHttpClientManager.class), server -> new ServerConnector(server, new HTTP2CServerConnectionFactory(new HttpConfiguration()))),
        new HttpProxyTestParameter(BEANS.get(ForceHttp2DefaultAsyncHttpClientManager.class), server -> new ServerConnector(server, new HTTP2CServerConnectionFactory(new HttpConfiguration()))));
  }

  public HttpProxyTest(HttpProxyTestParameter testParameter) {
    m_httpProxyTestParameter = testParameter;
  }

  @Before
  public void before() {
    m_proxy = BEANS.get(HttpProxy.class);

    if (m_httpProxyTestParameter != null) {
      m_proxy.withHttpClientManager(m_httpProxyTestParameter.getClientManager());
    }
  }

  @Before
  public void createHttpServer() throws Exception {
    if (m_httpProxyTestParameter == null) {
      return;
    }

    m_server = new Server();
    m_handlerCollection = new HandlerCollection(true);
    m_server.setHandler(m_handlerCollection);
    @SuppressWarnings("resource")
    ServerConnector connector = m_httpProxyTestParameter.getServerConnectorFunction().apply(m_server);
    m_server.setConnectors(new Connector[]{connector});
    m_server.start();
  }

  @After
  public void uninstallHttpServer() throws Exception {
    if (m_server == null) {
      return;
    }

    m_server.stop();
    m_server = null;
  }

  @Test
  @NonParameterized
  public void testRewriteUrl() {
    HttpProxyRequestOptions options = new HttpProxyRequestOptions()
        .withRewriteRule(new SimpleRegexRewriteRule("/my-api/", "/"));

    testRewriteUrlInternal("http://internal.example.com:1234/api/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview",
        "/my-api/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview", null, options);
    testRewriteUrlInternal("http://internal.example.com:1234/api",
        null, null, options);
    testRewriteUrlInternal("http://internal.example.com:1234/api?foo",
        null, "foo", options);
    testRewriteUrlInternal("http://internal.example.com:1234/api/",
        "/", null, options);
    testRewriteUrlInternal("http://internal.example.com:1234/api/lorem",
        "/lorem", null, options);
    testRewriteUrlInternal("http://internal.example.com:1234/api/lorem?ipsum",
        "/lorem", "ipsum", new HttpProxyRequestOptions());
    testRewriteUrlInternal("http://internal.example.com:1234/api/lorem?foo=%25bar%25+lorem+ipsum", "/lorem", "foo=%25bar%25+lorem+ipsum", new HttpProxyRequestOptions());
    testRewriteUrlInternal("http://internal.example.com:1234/api/lorem%20ipsum/%C3%84hnliche%20Dateien.html",
        "/lorem ipsum/Ähnliche Dateien.html", null, new HttpProxyRequestOptions());
  }

  protected void testRewriteUrlInternal(String expectedResult, String pathInfo, String queryString, HttpProxyRequestOptions options) {
    HttpProxy proxy = m_proxy
        .withRemoteBaseUrl("http://internal.example.com:1234/api");

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getPathInfo()).thenReturn(pathInfo);
    when(req.getQueryString()).thenReturn(queryString);

    String url = proxy.rewriteUrl(req, options);
    assertEquals(expectedResult, url);
  }

  @Test
  @NonParameterized
  public void testWithRemoteBaseUrl() {
    m_proxy.withRemoteBaseUrl("http://internal.example.com:1234/api");
    assertEquals("http://internal.example.com:1234/api", m_proxy.getRemoteBaseUrl());

    m_proxy.withRemoteBaseUrl("http://internal.example.com:1234/foo/");
    assertEquals("http://internal.example.com:1234/foo", m_proxy.getRemoteBaseUrl());
  }

  @Test
  @NonParameterized
  public void testShouldWriteParametersAsPayload() {
    Map<String, String[]> oneParameter = hashMap(ImmutablePair.of("maxRows", new String[]{"2"}));
    Map<String, String[]> multiParameters = hashMap(ImmutablePair.of("name", new String[]{"alice"}), ImmutablePair.of("pets", new String[]{"dog", "cat"}));

    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", null, null)));
    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", null, oneParameter)));
    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", null, multiParameters)));

    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/json", null)));
    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/json", oneParameter)));
    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/json", multiParameters)));

    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/json; \"this is not application/x-www-form-urlencoded\"", null)));
    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/json; \"this is not application/x-www-form-urlencoded\"", oneParameter)));
    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/json; \"this is not application/x-www-form-urlencoded\"", multiParameters)));

    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/x-www-form-urlencoded", null)));
    assertTrue(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/x-www-form-urlencoded", oneParameter)));
    assertTrue(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/x-www-form-urlencoded", multiParameters)));

    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/x-www-form-urlencoded; charset=utf-8", null)));
    assertTrue(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/x-www-form-urlencoded; charset=utf-8", oneParameter)));
    assertTrue(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "application/x-www-form-urlencoded; charset=utf-8", multiParameters)));

    assertFalse(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "APplicATIon/X-www-fOrM-urlencoded; charset=\"UTF-8\"", null)));
    assertTrue(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "APplicATIon/X-www-fOrM-urlencoded; charset=\"UTF-8\"", oneParameter)));
    assertTrue(m_proxy.shouldWriteParametersAsPayload(mockRequest("POST", "APplicATIon/X-www-fOrM-urlencoded; charset=\"UTF-8\"", multiParameters)));
  }

  private HttpServletRequest mockRequest(String method, String contentType, Map<String, String[]> parameterMap) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    if (method != null) {
      when(req.getMethod()).thenReturn(method);
    }
    if (contentType != null) {
      when(req.getContentType()).thenReturn(contentType);
    }
    if (parameterMap != null) {
      when(req.getParameterMap()).thenReturn(parameterMap);
    }
    return req;
  }

  @Test
  @NonParameterized
  public void testGetConnectionHeaderValuesHttpRequest() {
    assertGetConnectionHeaderValues(null);
    assertGetConnectionHeaderValues("Keep-Alive", "keep-alive");
    assertGetConnectionHeaderValues("Keep-alive, trailers, Foo", "keep-alive", "trailers", "foo");
    assertGetConnectionHeaderValues("Keep-alive ,   trailers  ,Foo", "keep-alive", "trailers", "foo");
    assertGetConnectionHeaderValues("Keep-alive,, ,  , \t", "keep-alive");
  }

  protected void assertGetConnectionHeaderValues(String receivedHeaderValue, String... expectedValues) {
    // verify HttpServletRequest
    HttpServletRequest req = mock(HttpServletRequest.class);
    if (receivedHeaderValue != null) {
      Stream<String> stream = Stream.of(StringUtility.split(receivedHeaderValue, ","))
          .filter(StringUtility::hasText)
          .map(StringUtility::trim);
      when(req.getHeaders("Connection")).thenReturn(EnumerationUtility.asEnumeration(stream.iterator()));
    }

    assertEquals(hashSet(expectedValues), m_proxy.getConnectionHeaderValues(req));
    verify(req).getHeaders("Connection");
    verifyNoMoreInteractions(req);

    // verify HttpResponse
    HttpResponse httpResponse = new BasicHttpResponse(HttpStatus.SC_OK);
    httpResponse.addHeader(new BasicHeader("Connection", receivedHeaderValue));

    assertEquals(hashSet(expectedValues), m_proxy.getConnectionHeaderValues(httpResponse));
  }

  @Test
  @NonParameterized
  public void testFilterHopByHopRequestHeaders() {
    assertRewriteRequestHeaders(emptyMap(), emptyMap());
    assertRewriteRequestHeaders(singletonMap("foo", "bar"), singletonMap("foo", "bar"));

    assertRewriteRequestHeaders(emptyMap(), singletonMap("Connection", "close"));
    assertRewriteRequestHeaders(emptyMap(), singletonMap("Upgrade", "h2c"));
    assertRewriteRequestHeaders(emptyMap(), singletonMap("Keep-Alive", "true"));
    assertRewriteRequestHeaders(emptyMap(), singletonMap("Transfer-Encoding", "chunked"));
    assertRewriteRequestHeaders(emptyMap(), singletonMap("Proxy-Authorization", "Basic YWxhZGRpbjpvcGVuc2VzYW1l"));
    assertRewriteRequestHeaders(emptyMap(), singletonMap("TE", "trailers, deflate;q=0.5"));

    assertRewriteRequestHeaders(
        singletonMap("accept-encoding", "deflate"),
        hashMap(new ImmutablePair<>("Transfer-Encoding", "chunked"), new ImmutablePair<>("accept-encoding", "deflate")));

    assertRewriteRequestHeaders(
        singletonMap("foobar", "bar"),
        hashMap(new ImmutablePair<>("Connection", "foo, bar"), new ImmutablePair<>("Foo", "bar"), new ImmutablePair<>("foobar", "bar")));
  }

  protected void assertRewriteRequestHeaders(Map<String, String> expectedHeaders, Map<String, String> receivedHeaders) {
    // mock HttpServletRequest
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeaderNames()).thenReturn(enumeration(receivedHeaders.keySet()));
    //noinspection SuspiciousMethodCalls
    when(req.getHeader(anyString())).thenAnswer(invocation -> receivedHeaders.get(invocation.getArgument(0)));
    when(req.getHeaders("Connection")).thenAnswer(invocation -> {
      Stream<String> stream = Stream.of(StringUtility.split(receivedHeaders.get("Connection"), ","))
          .filter(StringUtility::hasText)
          .map(StringUtility::trim);
      return EnumerationUtility.asEnumeration(stream.iterator());
    });

    BasicRequestBuilder httpReq = BasicRequestBuilder.get();

    // perform actual operation
    BEANS.get(HttpProxy.class).writeRequestHeaders(req, httpReq);

    assertEquals(
        expectedHeaders,
        Arrays.stream(ObjectUtility.nvlOpt(httpReq.getHeaders(), () -> new Header[]{}))
            // filter automatically added accept-encoding header unless it has been set explicitly
            .filter(e -> !"accept-encoding".equalsIgnoreCase(e.getName()) || receivedHeaders.containsKey("accept-encoding"))
            .collect(Collectors.toMap(Header::getName, Header::getValue)));

    verify(req).getHeaders("Connection");
    verify(req, atLeastOnce()).getHeaderNames();
    verify(req, atLeast(0)).getHeader(anyString());
    verifyNoMoreInteractions(req);
  }

  @Test
  @NonParameterized
  public void testFilterHopByHopResponseHeaders() {
    assertRewriteResponseHeaders(emptyMap(), emptyMap());
    assertRewriteResponseHeaders(singletonMap("foo", "bar"), singletonMap("foo", "bar"));

    assertRewriteResponseHeaders(emptyMap(), singletonMap("Connection", "close"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Keep-Alive", "true"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Transfer-Encoding", "chunked"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Proxy-Authenticate", "Basic"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Trailer", "Expires"));

    assertRewriteResponseHeaders(
        singletonMap("accept-encoding", "deflate"),
        hashMap(new ImmutablePair<>("Transfer-Encoding", "chunked"), new ImmutablePair<>("accept-encoding", "deflate")));

    assertRewriteResponseHeaders(
        singletonMap("foobar", "bar"),
        hashMap(new ImmutablePair<>("Connection", "foo, bar"), new ImmutablePair<>("Foo", "bar"), new ImmutablePair<>("foobar", "bar")));
  }

  protected void assertRewriteResponseHeaders(Map<String, String> expectedHeaders, Map<String, String> receivedHeaders) {
    // mock HttpServletResponse
    HttpServletResponse res = mock(HttpServletResponse.class);
    Map<String, String> collectedHeaders = new HashMap<>();
    doAnswer(invocation -> collectedHeaders.put(invocation.getArgument(0), invocation.getArgument(1))).when(res).setHeader(anyString(), anyString());

    BasicHttpResponse httpResponse = new BasicHttpResponse(HttpStatus.SC_OK);
    receivedHeaders.forEach((k, v) -> httpResponse.addHeader(k, v));

    // perform actual operation
    BEANS.get(HttpProxy.class).writeResponseHeaders(res, httpResponse);

    assertEquals(expectedHeaders, collectedHeaders);
    verify(res, atLeast(0)).setHeader(anyString(), anyString());
    verifyNoMoreInteractions(res);
  }

  @Test
  @NonParameterized
  public void testWriteCustomRequestHeaders() {
    BasicRequestBuilder httpReq = BasicRequestBuilder.get()
        .setHeaders(
            new BasicHeader("Foo", "123"),
            new BasicHeader("Bar", "xyz"),
            new BasicHeader("x-foobar", "test"),
            new BasicHeader("Bar", "zyz"), // duplicate!
            new BasicHeader("Baz", "H0"));

    HttpProxyRequestOptions requestOptions = new HttpProxyRequestOptions()
        .withCustomRequestHeader(null, null) // invalid name, should be ignored
        .withCustomRequestHeader("myHeader", "H1") // new header
        .withCustomRequestHeader("FOO", "H2") // overwrite single header (case-insensitive)
        .withCustomRequestHeader("bar", "H3") // overwrite multi-header (case-insensitive)
        .withCustomRequestHeader("X-FooBar", null) // remove existing header (case-insensitive)
        .withCustomRequestHeader("X-BarFoo", null); // remove non-existing header

    BEANS.get(HttpProxy.class).writeCustomRequestHeaders(httpReq, requestOptions.getCustomRequestHeaders());

    var expectedHeaders = List.of(
        new ImmutablePair<>("Baz", "H0"),
        new ImmutablePair<>("myHeader", "H1"),
        new ImmutablePair<>("FOO", "H2"),
        new ImmutablePair<>("bar", "H3"));
    var actualHeaders = Arrays.stream(ObjectUtility.nvlOpt(httpReq.getHeaders(), () -> new Header[0]))
        .map(header -> new ImmutablePair<>(header.getName(), header.getValue()))
        .collect(Collectors.toList());
    assertEquals(expectedHeaders, actualHeaders);
  }

  @Test
  public void testProxyRequest_noContent() throws IOException {
    testProxyRequestWithStatusCodeAndContent_Internal(200, new byte[]{}, true);
    testProxyRequestWithStatusCodeAndContent_Internal(200, new byte[]{}, false);
  }

  @Test
  public void testProxyRequest_200() throws IOException {
    testProxyRequestWithStatusCodeAndContent_Internal(200, new byte[]{0x01}, true);
  }

  @Test
  public void testProxyRequest_spaceInPath() throws IOException {
    testProxyRequestWithStatusCodeAndContent_Internal(200, new byte[]{0x02}, true, 1, "/a b/c d/lorem ipsum.htm");
  }

  @Test
  public void testProxyRequest_numerousRequests() throws IOException {
    // keep number of requests low on CI (at least as long as we do not run tests in parallel), however can be increased for testing locally
    testProxyRequestWithStatusCodeAndContent_Internal(200, new byte[]{0x01}, true, 25, null);
  }

  @Test
  public void testProxyRequest_404() throws IOException {
    testProxyRequestWithStatusCodeAndContent_Internal(404, new byte[]{0x02}, true);
  }

  @Test
  public void testProxyRequest_500() throws IOException {
    testProxyRequestWithStatusCodeAndContent_Internal(500, new byte[]{0x02}, true);
  }

  @Test
  public void testProxyRequest_largeContent() throws IOException {
    // keep size pseudo-large (could be larger) on CI (at least as long as we do not run tests in parallel), however can be increased for testing locally
    byte[] content = new byte[65536 * 32];
    new Random().nextBytes(content); // for tests alright not to use SecureRandom
    testProxyRequestWithStatusCodeAndContent_Internal(200, content, true);
    testProxyRequestWithStatusCodeAndContent_Internal(200, content, false);
  }

  protected void testProxyRequestWithStatusCodeAndContent_Internal(int statusCode, byte[] content, boolean specifyContentLength) throws IOException {
    testProxyRequestWithStatusCodeAndContent_Internal(statusCode, content, specifyContentLength, 1, null);
  }

  protected void testProxyRequestWithStatusCodeAndContent_Internal(int statusCode, byte[] content, boolean specifyContentLength, int numberOfRequests, String pathInfo) throws IOException {
    AbstractHandler handler = new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(statusCode);
        if (specifyContentLength) {
          response.setContentLength(content.length);
        }
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(content);
        outputStream.flush();
        baseRequest.setHandled(true);
        response.flushBuffer();
      }
    };

    HttpServletResponse resp = testProxyRequestInternal(handler, numberOfRequests, pathInfo);

    verify(resp, times(numberOfRequests)).setStatus(statusCode);
    byte[] expectedContent = new byte[numberOfRequests * content.length];
    IntStream.range(0, numberOfRequests).forEach(i -> System.arraycopy(content, 0, expectedContent, i * content.length, content.length));
    assertArrayEquals(expectedContent, ((BufferedServletOutputStream) resp.getOutputStream()).getContent());
  }

  @Ignore // do not run this (long) test on CI (at least as long as we do not run tests in parallel), however can be used for testing locally
  @Test
  public void testProxyRequest_longDuration() throws IOException {
    AbstractHandler handler = new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SC_OK);
        SleepUtil.sleepElseLog(3, TimeUnit.MINUTES);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(0x42);
        outputStream.flush();
        baseRequest.setHandled(true);
        response.flushBuffer();
      }
    };

    HttpServletResponse resp = testProxyRequestInternal(handler, 6 * 60 * 1000L, 1, null);

    verify(resp).setStatus(200);
    assertArrayEquals(new byte[]{0x42}, ((BufferedServletOutputStream) resp.getOutputStream()).getContent());
  }

  @Test
  public void testProxyRequest_Failure() {
    HttpServletResponse resp = testProxyRequestInternal(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        throw new IOException();
      }
    }, 1, null);

    verify(resp).setStatus(500);

    List<Throwable> handledThrowables = BEANS.get(JUnitExceptionHandler.class).getErrors();
    assertTrue(handledThrowables.stream().allMatch(t -> t instanceof ConnectionClosedException));
    handledThrowables.clear(); // do not fail test because there were handled exceptions (see JUnitExceptionHandler)
  }

  public HttpServletResponse testProxyRequestInternal(Handler handler, int numberOfRequests, String pathInfo) {
    return testProxyRequestInternal(handler, 30 * 1000L, numberOfRequests, pathInfo);
  }

  public HttpServletResponse testProxyRequestInternal(Handler handler, long timeoutUntilCompletion, int numberOfRequests, String pathInfo) {
    try {
      m_handlerCollection.addHandler(handler);
      m_proxy.withRemoteBaseUrl(m_server.getURI().toString());

      AsyncContext asyncContext = mock(AsyncContext.class);

      HttpServletRequest httpReq = mock(HttpServletRequest.class);
      when(httpReq.getMethod()).thenReturn(Method.GET.toString());
      when(httpReq.getPathInfo()).thenReturn(pathInfo != null ? pathInfo : "/");
      when(httpReq.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
      when(httpReq.startAsync(any(), any())).thenReturn(asyncContext);

      HttpServletResponse httpResp = mock(HttpServletResponse.class);
      BufferedServletOutputStream outputStream = new BufferedServletOutputStream();
      when(httpResp.getOutputStream()).thenReturn(outputStream);

      IntStream.range(0, numberOfRequests).parallel().forEach(i -> {
        try {
          m_proxy.proxy(httpReq, httpResp, mock(HttpProxyRequestOptions.class));
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      verify(asyncContext, timeout(timeoutUntilCompletion).times(numberOfRequests)).complete();
      return httpResp;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      m_handlerCollection.removeHandler(handler);
    }
  }

  public static class HttpProxyTestParameter implements IScoutTestParameter {

    private final AbstractAsyncHttpClientManager<?> m_clientManager;
    private final Function<Server, ServerConnector> m_serverConnectorFunction;

    public HttpProxyTestParameter(AbstractAsyncHttpClientManager<?> clientManager, Function<Server, ServerConnector> serverConnectorFunction) {
      m_clientManager = clientManager;
      m_serverConnectorFunction = serverConnectorFunction;
    }

    public AbstractAsyncHttpClientManager<?> getClientManager() {
      return m_clientManager;
    }

    public Function<Server, ServerConnector> getServerConnectorFunction() {
      return m_serverConnectorFunction;
    }

    @Override
    public String getName() {
      return m_clientManager.getClass().getSimpleName();
    }
  }
}
