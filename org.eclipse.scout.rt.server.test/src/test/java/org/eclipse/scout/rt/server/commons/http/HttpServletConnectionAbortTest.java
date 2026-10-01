/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;

/**
 * Test what happens if the servlet is not reading the input stream content of a post request.
 * <code>java.net.SocketException: Software caused connection abort: socket write error</code>
 */
@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class HttpServletConnectionAbortTest {
  private TestingHttpClient m_client;
  private TestingHttpServer m_server;

  @Before
  public void before() {
    m_client = new TestingHttpClient();
    m_server = new TestingHttpServer(TestingHttpPorts.PORT_33007);
    m_server.start();
  }

  @After
  public void after() {
    m_client.stop();
    m_server.stop();
  }

  /**
   * Verifies that requests succeed when the servlet fully consumes the request body.
   */
  @Test
  public void testPostWithServerReadingInput() throws IOException {
    byte[] reqBytes = new byte[1000];
    byte[] respBytes = new byte[1000];

    m_server.withServletPostHandler((req, resp) -> {
      //MARKER: consume input to avoid java.net.SocketException: Software caused connection abort: socket write error
      IOUtility.readBytes(req.getInputStream(), req.getContentLength());

      resp.setContentType("application/octet-stream");
      //resp.setHeader("Transfer-Encoding", "chunked");
      resp.getOutputStream().write(respBytes, 0, respBytes.length);
      resp.getOutputStream().flush();
    });

    for (int i = 0; i < 100; i++) {
      System.out.println("ROUND " + i);
      HttpRequest req = m_client
          .getHttpRequestFactory()
          .buildPostRequest(new GenericUrl(m_server.getServletUrl()), new InputStreamContent("application/octet-stream", new ByteArrayInputStream(reqBytes)));
      req.getHeaders().setCacheControl("no-cache");
      req.getHeaders().setContentType("application/octet-stream");
      req.getHeaders().put("Pragma", "no-cache");

      HttpResponse resp = req.execute();
      byte[] respBytes2 = IOUtility.readBytes(resp.getContent());
      Assert.assertArrayEquals(respBytes, respBytes2);
    }
  }

  /**
   * Verifies that a response can be received even if the servlet does not consume the
   * request body before writing the response.
   */
  @Test
  public void testPostWithoutServerReadingInput() throws IOException {
    byte[] reqBytes = new byte[1000];
    byte[] respBytes = new byte[1000];

    m_server.withServletPostHandler((req, resp) -> {
      resp.setContentType("application/octet-stream");
      resp.getOutputStream().write(respBytes, 0, respBytes.length);
      resp.getOutputStream().flush();
    });

    for (int i = 0; i < 100; i++) {
      System.out.println("ROUND " + i);
      HttpRequest req = m_client
          .getHttpRequestFactory()
          .buildPostRequest(new GenericUrl(m_server.getServletUrl()), new InputStreamContent("application/octet-stream", new ByteArrayInputStream(reqBytes)));
      req.getHeaders().setCacheControl("no-cache");
      req.getHeaders().setContentType("application/octet-stream");
      req.getHeaders().put("Pragma", "no-cache");

      HttpResponse resp = req.execute();
      byte[] respBytes2 = IOUtility.readBytes(resp.getContent());
      Assert.assertArrayEquals(respBytes, respBytes2);
    }
  }
}
