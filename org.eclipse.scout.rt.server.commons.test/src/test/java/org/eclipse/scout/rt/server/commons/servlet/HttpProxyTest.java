/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import static java.util.Collections.*;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.hashMap;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EnumerationUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.testing.http.MockHttpTransport.Builder;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

@RunWith(PlatformTestRunner.class)
public class HttpProxyTest {

  private HttpProxy m_proxy;

  @Before
  public void before() {
    m_proxy = BEANS.get(HttpProxy.class);
  }

  @Test
  public void testRewriteUrl() {
    HttpProxy proxy = m_proxy
        .withRemoteBaseUrl("http://internal.example.com:1234/api");

    HttpProxyRequestOptions options = new HttpProxyRequestOptions()
        .withRewriteRule(new SimpleRegexRewriteRule("/my-api/", "/"));

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getPathInfo()).thenReturn("/my-api/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview");

    String url = proxy.rewriteUrl(req, options);
    assertEquals("http://internal.example.com:1234/api/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview", url);
  }

  @Test
  public void testShouldWriteParametersAsPayload() {
    Map<String, String[]> oneParameter = CollectionUtility.hashMap(ImmutablePair.of("maxRows", new String[]{"2"}));
    Map<String, String[]> multiParameters = CollectionUtility.hashMap(ImmutablePair.of("name", new String[]{"alice"}), ImmutablePair.of("pets", new String[]{"dog", "cat"}));

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
  public void testGetConnectionHeaderValuesHttpRequest() throws Exception {
    assertGetConnectionHeaderValues(null);
    assertGetConnectionHeaderValues("Keep-Alive", "keep-alive");
    assertGetConnectionHeaderValues("Keep-alive, trailers, Foo", "keep-alive", "trailers", "foo");
    assertGetConnectionHeaderValues("Keep-alive ,   trailers  ,Foo", "keep-alive", "trailers", "foo");
    assertGetConnectionHeaderValues("Keep-alive,, ,  , \t", "keep-alive");
  }

  protected void assertGetConnectionHeaderValues(String receivedHeaderValue, String... expectedValues) throws IOException {
    // verify HttpServletRequest
    HttpServletRequest req = mock(HttpServletRequest.class);
    if (receivedHeaderValue != null) {
      Stream<String> stream = Stream.of(StringUtility.split(receivedHeaderValue, ","))
          .filter(StringUtility::hasText)
          .map(StringUtility::trim);
      when(req.getHeaders("Connection")).thenReturn(EnumerationUtility.asEnumeration(stream.iterator()));
    }

    assertEquals(CollectionUtility.hashSet(expectedValues), m_proxy.getConnectionHeaderValues(req));
    verify(req).getHeaders("Connection");
    verifyNoMoreInteractions(req);

    // verify HttpResponse
    MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse();
    if (receivedHeaderValue != null) {
      mockResponse.addHeader("Connection", receivedHeaderValue);
    }
    HttpResponse httpResponse = new Builder()
        .setLowLevelHttpResponse(
            mockResponse)
        .build()
        .createRequestFactory()
        .buildGetRequest(new GenericUrl("http://www.example.org/test"))
        .execute();

    assertEquals(CollectionUtility.hashSet(expectedValues), m_proxy.getConnectionHeaderValues(httpResponse));
  }

  @Test
  public void testFilterHopByHopRequestHeaders() throws Exception {
    assertRewriteRequestHeaders(emptyMap(), emptyMap());
    assertRewriteRequestHeaders(singletonMap("foo", "bar"), singletonMap("foo", "bar"));

    assertRewriteRequestHeaders(emptyMap(), singletonMap("Connection", "close"));
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

  protected void assertRewriteRequestHeaders(Map<String, String> expectedHeaders, Map<String, String> receivedHeaders) throws IOException {
    // mock HttpServletRequest
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeaderNames()).thenReturn(Collections.enumeration(receivedHeaders.keySet()));
    when(req.getHeader(anyString())).thenAnswer(invocation -> receivedHeaders.get(invocation.getArgument(0)));
    when(req.getHeaders("Connection")).thenAnswer(invocation -> {
      Stream<String> stream = Stream.of(StringUtility.split(receivedHeaders.get("Connection"), ","))
          .filter(StringUtility::hasText)
          .map(StringUtility::trim);
      return EnumerationUtility.asEnumeration(stream.iterator());
    });

    HttpRequest httpReq = new Builder()
        .setLowLevelHttpRequest(new MockLowLevelHttpRequest())
        .build()
        .createRequestFactory()
        .buildGetRequest(new GenericUrl("http://www.example.org/test"));

    // perform actual operation
    BEANS.get(HttpProxy.class).writeRequestHeaders(req, httpReq);

    assertEquals(
        expectedHeaders.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> Arrays.asList(e.getValue()))),
        httpReq.getHeaders()
            .entrySet()
            .stream()
            // filter automatically added accept-encoding header unless it has been set explicitly
            .filter(e -> !"accept-encoding".equalsIgnoreCase(e.getKey()) || receivedHeaders.containsKey("accept-encoding"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

    verify(req).getHeaders("Connection");
    verify(req, atLeastOnce()).getHeaderNames();
    verify(req, atLeast(0)).getHeader(anyString());
    verifyNoMoreInteractions(req);
  }

  @Test
  public void testFilterHopByHopResponseHeaders() throws Exception {
    final String acceptEncodingHeaderName = "accept-encoding";
    assertRewriteResponseHeaders(emptyMap(), emptyMap());
    assertRewriteResponseHeaders(singletonMap("foo", "bar"), singletonMap("foo", "bar"));

    assertRewriteResponseHeaders(emptyMap(), singletonMap("Connection", "close"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Keep-Alive", "true"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Transfer-Encoding", "chunked"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Proxy-Authenticate", "Basic"));
    assertRewriteResponseHeaders(emptyMap(), singletonMap("Trailer", "Expires"));

    assertRewriteResponseHeaders(
        singletonMap(acceptEncodingHeaderName, "deflate"),
        hashMap(new ImmutablePair<>("Transfer-Encoding", "chunked"), new ImmutablePair<>(acceptEncodingHeaderName, "deflate")));

    assertRewriteResponseHeaders(
        singletonMap("foobar", "bar"),
        hashMap(new ImmutablePair<>("Connection", "foo, bar"), new ImmutablePair<>("Foo", "bar"), new ImmutablePair<>("foobar", "bar")));
  }

  protected void assertRewriteResponseHeaders(Map<String, String> expectedHeaders, Map<String, String> receivedHeaders) throws IOException {
    // mock HttpServletResponse
    HttpServletResponse res = mock(HttpServletResponse.class);
    Map<String, String> collectedHeaders = new HashMap<>();
    doAnswer(invocation -> collectedHeaders.put(invocation.getArgument(0), invocation.getArgument(1))).when(res).setHeader(anyString(), anyString());

    MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse();
    receivedHeaders.entrySet().forEach(e -> mockResponse.addHeader(e.getKey(), e.getValue()));

    HttpResponse httpResponse = new Builder()
        .setLowLevelHttpResponse(mockResponse)
        .build()
        .createRequestFactory()
        .buildGetRequest(new GenericUrl("http://www.example.org/test"))
        .execute();

    // perform actual operation
    BEANS.get(HttpProxy.class).writeResponseHeaders(res, httpResponse);

    assertEquals(expectedHeaders, collectedHeaders);
    verify(res, atLeast(0)).setHeader(anyString(), anyString());
    verifyNoMoreInteractions(res);
  }
}
