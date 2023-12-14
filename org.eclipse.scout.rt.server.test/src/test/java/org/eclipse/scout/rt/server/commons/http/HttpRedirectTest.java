/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.http;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.impl.classic.RedirectExec;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;

/**
 * Test if the Google HTTP Client API (here called 'google level') together with the Apache HTTP Client (here called
 * 'apache level') works as expected by triggering redirects in the http transport layer.
 * <p>
 * The Google HTTP Client API is not only an API, it contains an execution loop that handles various retry scenarios.
 * However in its core it uses a http transport - hers it is Apache HTTP Client.
 * <p>
 * Apache HTTP client also handles various http retry scenarios in its exec loop.
 */

@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class HttpRedirectTest {
  private static final String CORRELATION_ID = "Correlation-Id";

  private TestingHttpClient m_client;
  private TestingHttpServer m_server;
  private final List<String> m_servletGetLog = Collections.synchronizedList(new ArrayList<>());
  private final List<String> m_servletPostLog = Collections.synchronizedList(new ArrayList<>());
  private final Queue<String> m_redirectUrls = new ArrayBlockingQueue<>(10);
  private int m_redirectCode = 302;

  @Before
  public void before() {
    m_redirectCode = 302;
    m_servletGetLog.clear();
    m_servletPostLog.clear();
    m_client = new TestingHttpClient();
    m_server = new TestingHttpServer(TestingHttpPorts.PORT_33006)
        .withServletGetHandler((req, resp) -> fixtureServletGet(req, resp))
        .withServletPostHandler((req, resp) -> fixtureServletPost(req, resp));
    m_server.start();
  }

  @After
  public void after() {
    m_client.stop();
    m_server.stop();
  }

  private void fixtureServletGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    m_servletGetLog.add(req.getHeader(CORRELATION_ID));
    String redirectUrl = m_redirectUrls.poll();
    if (redirectUrl != null) {
      resp.sendRedirect(redirectUrl);
      return;
    }
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getOutputStream().println("HTTP-GET:Hello " + req.getParameter("foo"));

  }

  private void fixtureServletPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    m_servletPostLog.add(req.getHeader(CORRELATION_ID));
    String redirectUrl = m_redirectUrls.poll();
    if (redirectUrl != null) {
      if (m_redirectCode == 302) {
        resp.sendRedirect(redirectUrl);
      }
      else {
        resp.setStatus(m_redirectCode);
        resp.addHeader("Location", redirectUrl);
      }
      return;
    }
    String arg = null;
    assertEquals("text/plain;charset=UTF-8", req.getContentType());
    assertEquals("UTF-8", req.getCharacterEncoding());
    assertEquals(3, req.getContentLength());
    arg = IOUtility.readString(req.getInputStream(), req.getCharacterEncoding(), req.getContentLength());
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getOutputStream().println("HTTP-POST:" + arg);
  }

  /**
   * Expect one redirect on apache level
   */
  @Test
  public void testGetRedirect() throws IOException {
    String url = m_server.getServletUrl() + "?foo=bar";
    m_redirectUrls.add(url);

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildGetRequest(new GenericUrl(url));
    req.getHeaders().set(CORRELATION_ID, "01");
    HttpResponse resp = req.execute();
    byte[] bytes;
    try (InputStream in = resp.getContent()) {
      bytes = IOUtility.readBytes(in);
    }
    assertEquals(200, resp.getStatusCode());
    String text = new String(bytes, StandardCharsets.UTF_8).trim();
    assertEquals("HTTP-GET:Hello bar", text);
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    assertEquals(new String(bytes), 20, bytes.length);//text + CR + LF
    //two calls due to redirect
    assertEquals(Arrays.asList("01", "01"), m_servletGetLog);
  }

  /**
   * Expect redirect on apache level due to {@link HttpContent#retrySupported()} true; however expect redirect to be a
   * GET request (see {@link RedirectExec} resp.
   * <a href="https://www.rfc-editor.org/rfc/rfc9110#status.302">specification</a>).
   */
  @Test
  public void testPostRedirectWithSupportedPostRetry_302() throws IOException {
    HttpResponse resp = testPostRedirectInternal(true);
    try (InputStream in = resp.getContent()) {
      byte[] bytes = IOUtility.readBytes(in);
      String text = new String(bytes, StandardCharsets.UTF_8).trim();
      assertEquals("HTTP-GET:Hello null", text); // http response 302 leads to a redirect as a GET request
      assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
      assertEquals(new String(bytes), 21, bytes.length);//text + CR + LF

    }
    assertEquals(200, resp.getStatusCode());
    //two calls due to redirect
    assertEquals(Collections.singletonList("05"), m_servletPostLog);
    assertEquals(Collections.singletonList(null), m_servletGetLog); // header correlation-id is also not resent for new GET request; therefore do not expect any correlation id
  }

  /**
   * Expect redirect on apache level due to {@link HttpContent#retrySupported()} true; expect redirect to repeat POST
   * request (see {@link RedirectExec} resp.
   * <a href="https://www.rfc-editor.org/rfc/rfc9110#status.307">specification</a>).
   */
  @Test
  public void testPostRedirectWithSupportedPostRetry_307() throws IOException {
    m_redirectCode = 307;
    HttpResponse resp = testPostRedirectInternal(true);
    try (InputStream in = resp.getContent()) {
      byte[] bytes = IOUtility.readBytes(in);
      String text = new String(bytes, StandardCharsets.UTF_8).trim();
      assertEquals("HTTP-POST:bar", text); // http response 302 leads to a redirect as a GET request
      assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
      assertEquals(new String(bytes), 15, bytes.length);//text + CR + LF

    }
    assertEquals(200, resp.getStatusCode());
    //two calls due to redirect
    assertEquals(List.of("05", "05"), m_servletPostLog);
  }

  /**
   * Expect no redirect on apache level due to {@link HttpContent#retrySupported()} false. This leads to a 302 post
   * response which fails.
   */
  @Test
  public void testPostRedirectWithUnsupportedPostRetry() throws IOException {
    HttpResponse resp = testPostRedirectInternal(false);
    try (InputStream in = resp.getContent()) {
      byte[] bytes = IOUtility.readBytes(in);
      assertArrayEquals(new byte[0], bytes);
    }
    assertEquals(302, resp.getStatusCode());
    // one calls due to non-repeatable
    assertEquals(Arrays.asList("05"), m_servletPostLog);
  }

  protected HttpResponse testPostRedirectInternal(boolean retrySupported) throws IOException {
    String url = m_server.getServletUrl().toExternalForm();
    m_redirectUrls.add(url);

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(url), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return retrySupported;
      }

      @Override
      public String getType() {
        return "text/plain;charset=UTF-8";
      }

      @Override
      public long getLength() {
        return 3;
      }
    });
    req.getHeaders().set(CORRELATION_ID, "05");
    HttpResponse resp = req.execute();
    return resp;
  }
}
