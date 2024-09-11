/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
import java.lang.reflect.Proxy;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.server.commons.http.TestingHttpServer.IServletRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;

/**
 * Test if the Google HTTP Client API (here called 'google level') together with the Apache HTTP Client (here called
 * 'apache level') works as expected by triggering various failures in the http transport layer.
 * <p>
 * The Google HTTP Client API is not only an API, it contains an execution loop that handles various retry scenarios.
 * However in its core it uses a http transport - hers it is Apache HTTP Client.
 * <p>
 * Apache HTTP client also handles various http retry scenarios in its exec loop.
 */
public class HttpRetryTest {
  private static final String CORRELATION_ID = "Correlation-Id";

  private TestingHttpClient m_client;
  private TestingHttpServer m_server;
  private final List<String> servletGetLog = Collections.synchronizedList(new ArrayList<>());
  private final LinkedBlockingDeque<String> servletPostLog = new LinkedBlockingDeque<>();
  private IServletRequestHandler servletFailOnce;
  private Exception servletPostError;

  @Before
  public void before() {
    servletGetLog.clear();
    servletPostLog.clear();
    servletPostError = null;
    m_client = new TestingHttpClient();
    m_server = new TestingHttpServer(TestingHttpPorts.PORT_33000)
        .withServletGetHandler(this::fixtureServletGet)
        .withServletPostHandler(this::fixtureServletPost);
    m_server.start();
  }

  @After
  public void after() {
    m_client.stop();
    m_server.stop();
  }

  private void fixtureServletGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    servletGetLog.add(req.getHeader(CORRELATION_ID));
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getOutputStream().println("Hello " + req.getParameter("foo"));
  }

  private void fixtureServletPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    servletPostLog.add(req.getHeader(CORRELATION_ID));
    String arg = null;
    try {
      assertEquals("text/plain;charset=UTF-8", req.getContentType());
      assertEquals("UTF-8", req.getCharacterEncoding());
      assertEquals(3, req.getContentLength());
      arg = IOUtility.readString(req.getInputStream(), req.getCharacterEncoding(), req.getContentLength());
    }
    catch (Exception e) {
      servletPostError = e;
      throw e;
    }

    if (servletFailOnce != null) {
      try {
        servletFailOnce.handle(req, resp);
        return;
      }
      finally {
        servletFailOnce = null;
      }
    }

    resp.setContentType("text/plain;charset=UTF-8");
    resp.getOutputStream().println("Post " + arg);
  }

  /**
   * Expect retry on apache level
   */
  @Test
  public void testGetRetry() throws IOException {
    //emulate a socket close before data is received
    AtomicInteger count = new AtomicInteger(1);
    m_server.withChannelInterceptor((channel, superCall) -> {
      if (count.getAndIncrement() < 2) {
        channel.getHttpTransport().abort(new SocketException("TEST:cannot write"));
        return;
      }
      superCall.call();
    });

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildGetRequest(new GenericUrl(m_server.getServletUrl() + "?foo=bar"));
    req.getHeaders().set(CORRELATION_ID, "01");
    HttpResponse resp = req.execute();
    byte[] bytes;
    try (InputStream in = resp.getContent()) {
      bytes = IOUtility.readBytes(in);
    }
    String text = new String(bytes, StandardCharsets.UTF_8).trim();
    assertEquals(text, "Hello bar");
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    assertEquals(new String(bytes), 11, bytes.length);//text + CR + LF
    assertEquals(Arrays.asList("01"), servletGetLog);
  }

  /**
   * Expect retry on apache level
   */
  @Test
  public void testPostWithUnsupportedRetryAndFailureWhileRetrievingResponse() throws IOException, InterruptedException {
    //emulate a header write error
    AtomicInteger count = new AtomicInteger(1);
    m_client.withExecuteInterceptor(
        (request, conn, informationCallback, context, superCall) -> {
          if (count.getAndIncrement() < 2) {
            HttpClientConnection originalConnection = conn;
            conn = (HttpClientConnection) Proxy.newProxyInstance(
                HttpClientConnection.class.getClassLoader(),
                new Class[]{HttpClientConnection.class},
                (proxy, method, args) -> {
                  if ("receiveResponseHeader".equals(method.getName())) {
                    throw new NoHttpResponseException("foo");
                  }
                  return method.invoke(originalConnection, args);
                });
          }
          return superCall.execute(request, conn, informationCallback, context, null);
        });

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(m_server.getServletUrl()), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return false;
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
    req.getHeaders().set(CORRELATION_ID, "02");
    HttpResponse resp = req.execute();
    byte[] bytes;
    try (InputStream in = resp.getContent()) {
      bytes = IOUtility.readBytes(in);
    }
    String text = new String(bytes, StandardCharsets.UTF_8).trim();
    assertEquals(text, "Post bar");
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    assertEquals(new String(bytes), 10, bytes.length);//text + CR + LF
    String e1 = servletPostLog.poll(30, TimeUnit.SECONDS);
    String e2 = servletPostLog.poll(30, TimeUnit.SECONDS);
    assertTrue(servletPostLog.isEmpty());
    assertEquals(List.of("02", "02"), List.of(e1, e2));
    assertNull(servletPostError);
  }

  /**
   * Expect no-retry on apache and google level
   */
  @Test
  public void testPostWithUnsupportedRetryAndFailureWhileBodyIsSent() throws IOException {
    //emulate a request body write error
    AtomicInteger count = new AtomicInteger(1);

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(m_server.getServletUrl()), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        if (count.getAndIncrement() < 2) {
          out.write("ba".getBytes());
          throw new SocketException("TEST:cannot write");
        }
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return false;
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
    req.getHeaders().set(CORRELATION_ID, "03");
    try {
      req.execute();
    }
    catch (SocketException e) {
      //the request was partially sent, resulting in a servlet side org.eclipse.jetty.io.EofException: Early EOF
      // awaiting completion of that exception
      for (int i = 0; i < 100 && servletPostError == null; i++) {
        SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
      }
      assertArrayEquals(new String[]{"03"}, servletPostLog.toArray());
      assertNotNull(servletPostError);
      assertEquals(ProcessingException.class, servletPostError.getClass());
      assertEquals(org.eclipse.jetty.io.EofException.class, servletPostError.getCause().getClass());
      return;
    }
    fail("Expected to fail");
  }

  /**
   * Expect no-retry on apache and google level
   */
  @Test
  public void testPostWithUnsupportedRetryAndFailureAfterRequestIsSent() throws IOException {
    //emulate a socket close before data is received
    AtomicInteger count = new AtomicInteger(1);
    m_server.withChannelInterceptor((channel, superCall) -> {
      //2 failures in a row, the first would have been retried by the CustomHttpRequestRetryHandler
      if (count.getAndIncrement() < 3) {
        channel.getHttpTransport().abort(new IOException("TEST:cannot write"));
        return;
      }
      superCall.call();
    });

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(m_server.getServletUrl()), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return false;
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
    req.getHeaders().set(CORRELATION_ID, "04");
    try {
      req.execute();
    }
    catch (NoHttpResponseException e) {
      assertTrue(servletPostLog.isEmpty());
      assertNull(servletPostError);
      return;
    }
    fail("Expected to fail");
  }

  /**
   * Expect no-retry on apache but retry on google level
   */
  @Test
  public void testPostWithSupportedRetryAndFailureAfterRequestIsSent() throws IOException {
    //emulate a socket close before data is received
    AtomicInteger count = new AtomicInteger(1);
    m_server.withChannelInterceptor((channel, superCall) -> {
      if (count.getAndIncrement() < 2) {
        channel.getHttpTransport().abort(new SocketException("TEST:cannot write"));
        return;
      }
      superCall.call();
    });

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(m_server.getServletUrl()), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return true;
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
    byte[] bytes;
    try (InputStream in = resp.getContent()) {
      bytes = IOUtility.readBytes(in);
    }
    String text = new String(bytes, StandardCharsets.UTF_8).trim();
    assertEquals(text, "Post bar");
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    assertEquals(new String(bytes), 10, bytes.length);//text + CR + LF
    assertArrayEquals(new String[]{"05"}, servletPostLog.toArray());
    assertNull(servletPostError);
  }

  /**
   * Expect no-retry on apache and google level since a valid response (400) was received
   */
  @Test
  public void testPostWithSupportedRetryAndFailureWithValidHttpResponseCode() throws IOException {
    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(m_server.getServletUrl()), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        //emulate a failure in the servlet
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return true;
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
    req.getHeaders().set(CORRELATION_ID, "06");
    servletFailOnce = (hreq, hresp) -> hresp.sendError(400);
    req.setThrowExceptionOnExecuteError(false);
    HttpResponse resp = req.execute();
    assertEquals(400, resp.getStatusCode());
    assertArrayEquals(new String[]{"06"}, servletPostLog.toArray());
    assertNull(servletPostError);
  }

  /**
   * Expect no-retry on apache and google level since a valid response (500) was received
   */
  @Test
  public void testPostWithSupportedRetryAndFailureWithoutHttpResponse() throws IOException {
    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildPostRequest(new GenericUrl(m_server.getServletUrl()), new HttpContent() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        //emulate a failure in the servlet
        out.write("bar".getBytes());
      }

      @Override
      public boolean retrySupported() {
        return true;
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
    req.getHeaders().set(CORRELATION_ID, "07");
    servletFailOnce = (hreq, hresp) -> {
      throw new FutureCancelledError("TEST");
    };
    req.setThrowExceptionOnExecuteError(false);
    HttpResponse resp = req.execute();
    assertEquals(500, resp.getStatusCode());
    assertArrayEquals(new String[]{"07"}, servletPostLog.toArray());
    assertNull(servletPostError);
  }
}
