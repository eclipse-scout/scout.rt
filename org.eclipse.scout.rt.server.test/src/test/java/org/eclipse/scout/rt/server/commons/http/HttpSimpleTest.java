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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.commons.http.SocketWithInterception.ISocketReadInterceptor;
import org.eclipse.scout.rt.server.commons.http.SocketWithInterception.ISocketWriteInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;

/**
 * Simple test if the Google HTTP Client API (here called 'google level') together with the Apache HTTP Client (here
 * called 'apache level') works as expected.
 * <p>
 * The Google HTTP Client API is not only an API, it contains an execution loop that handles various retry scenarios.
 * However in its core it uses a http transport - hers it is Apache HTTP Client.
 * <p>
 * Apache HTTP client also handles various http retry scenarios in its exec loop.
 */
public class HttpSimpleTest {
  private TestingHttpClient m_client;
  private TestingHttpServer m_server;

  private StringBuffer m_clientRead = new StringBuffer();
  private StringBuffer m_clientWrite = new StringBuffer();

  @Before
  public void before() {
    m_client = new TestingHttpClient()
        .withSocketReadInterceptor(this::createClientReadInterceptor)
        .withSocketWriteInterceptor(this::createClientWriteInterceptor);
    m_server = new TestingHttpServer(TestingHttpPorts.PORT_33001);
    m_server.start();
  }

  @After
  public void after() {
    m_client.stop();
    m_server.stop();
    System.out.println("# HttpClient.write\n" + m_clientWrite);
    System.out.println("# HttpClient.read\n" + m_clientRead);
  }

  private ISocketReadInterceptor createClientReadInterceptor() {
    return new ISocketReadInterceptor() {
      @Override
      public int read(InputStream in, byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        m_clientRead.append(new String(b, off, len));
        return n;
      }

      @Override
      public int read(InputStream in) throws IOException {
        int b = in.read();
        m_clientRead.append((char) b);
        return b;
      }
    };
  }

  private ISocketWriteInterceptor createClientWriteInterceptor() {
    return new ISocketWriteInterceptor() {
      @Override
      public void write(OutputStream out, byte[] b, int off, int len) throws IOException {
        m_clientWrite.append(new String(b, off, len));
        out.write(b, off, len);
      }

      @Override
      public void write(OutputStream out, int b) throws IOException {
        m_clientWrite.append((char) b);
        out.write(b);
      }
    };
  }

  @Test
  public void testGet() throws IOException {
    m_server.withServletGetHandler((req, resp) -> {
      resp.setContentType("text/plain;charset=UTF-8");
      resp.getOutputStream().println("Hello " + req.getParameter("foo"));
    });

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildGetRequest(new GenericUrl(m_server.getServletUrl() + "?foo=bar"));
    HttpResponse resp = req.execute();
    byte[] bytes;
    try (InputStream in = resp.getContent()) {
      bytes = IOUtility.readBytes(in, ObjectUtility.nvl(resp.getHeaders().getContentLength(), -1L).intValue());
    }
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    assertEquals(new String(bytes), 11, bytes.length);//text + CR + LF
    String text = new String(bytes, StandardCharsets.UTF_8).trim();
    assertEquals(text, "Hello bar");
  }

  @Test
  public void testPost() throws IOException {
    m_server.withServletPostHandler((req, resp) -> {
      assertEquals("text/plain;charset=UTF-8", req.getContentType());
      assertEquals("UTF-8", req.getCharacterEncoding());
      assertEquals(3, req.getContentLength());
      String arg = IOUtility.readString(req.getInputStream(), req.getCharacterEncoding(), req.getContentLength());
      resp.setContentType("text/plain;charset=UTF-8");
      resp.getOutputStream().println("Post " + arg);
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
    HttpResponse resp = req.execute();
    byte[] bytes;
    try (InputStream in = resp.getContent()) {
      bytes = IOUtility.readBytes(in);
    }
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    assertEquals(new String(bytes), 10, bytes.length);//text + CR + LF
    String text = new String(bytes, StandardCharsets.UTF_8).trim();
    assertEquals(text, "Post bar");
  }

  @Test
  public void testGetChunked() throws IOException {
    m_server.withServletGetHandler((req, resp) -> {
      resp.setContentType("text/plain;charset=UTF-8");
      resp.setHeader("Transfer-Encoding", "chunked");
      for (int i = 0; i < 100; i++) {
        resp.getWriter().write("Line of chunked data " + i + "\r\n");
        resp.getWriter().flush();
      }
    });

    HttpRequestFactory reqFactory = m_client.getHttpRequestFactory();
    HttpRequest req = reqFactory.buildGetRequest(new GenericUrl(m_server.getServletUrl()));
    HttpResponse resp = req.execute();
    assertEquals(StandardCharsets.UTF_8, resp.getContentCharset());
    String content = resp.parseAsString();
    assertEquals(2490, content.length());
  }
}
