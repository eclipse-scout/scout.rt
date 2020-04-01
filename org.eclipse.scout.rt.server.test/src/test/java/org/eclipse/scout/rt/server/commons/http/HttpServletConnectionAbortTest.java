/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.http;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.http.SocketWithInterception.ISocketWriteInterceptor;
import org.eclipse.scout.rt.shared.servicetunnel.http.ByteArrayContentEx;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

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
   * This test must not fail, see "MARKER:"
   */
  @Test
  public void testPostWithServerReadingInput() throws IOException {
    m_client.withSocketWriteInterceptor(() -> new ISocketWriteInterceptor() {
      @Override
      public void write(OutputStream out, int b) throws IOException {
        out.write(b);
      }

      @Override
      public void write(OutputStream out, byte[] buf, int off, int len) throws IOException {
        out.write(buf, off, len);
      }
    });

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
          .buildPostRequest(new GenericUrl(m_server.getServletUrl()), new ByteArrayContentEx(null, reqBytes, false));
      req.getHeaders().setCacheControl("no-cache");
      req.getHeaders().setContentType("application/octet-stream");
      req.getHeaders().put("Pragma", "no-cache");

      HttpResponse resp = req.execute();
      byte[] respBytes2 = IOUtility.readBytes(resp.getContent());
      Assert.assertArrayEquals(respBytes, respBytes2);
    }
  }

  /**
   * This test may fail after some rounds because...
   * <ul>
   * <li>on the line marked with "MARKER1:" the socket output is split into smaller packets as may a proxy or firewall
   * do</li>
   * <li>on the line marked with "MARKER2:" the input stream is never consumed</li>
   * </ul>
   */
  @Ignore
  @Test
  public void testPostWithoutServerReadingInput() throws IOException {
    m_client.withSocketWriteInterceptor(() -> new ISocketWriteInterceptor() {
      @Override
      public void write(OutputStream out, int b) throws IOException {
        out.write(b);
      }

      @Override
      public void write(OutputStream out, byte[] buf, int off, int len) throws IOException {
        /**** MARKER1 ****/
        //out.write(b, off, len);

        for (int i = 0; i < len; i++) {
          out.write(buf[i]);
        }
      }
    });

    byte[] reqBytes = new byte[1000];
    byte[] respBytes = new byte[1000];

    m_server.withServletPostHandler((req, resp) -> {
      //consume input to avoid java.net.SocketException: Software caused connection abort: socket write error
      /**** MARKER2 ****/
      //IOUtility.readBytes(req.getInputStream(), req.getContentLength());

      resp.setContentType("application/octet-stream");
      //resp.setHeader("Transfer-Encoding", "chunked");
      resp.getOutputStream().write(respBytes, 0, respBytes.length);
      resp.getOutputStream().flush();
    });

    for (int i = 0; i < 100; i++) {
      System.out.println("ROUND " + i);
      HttpRequest req = m_client
          .getHttpRequestFactory()
          .buildPostRequest(new GenericUrl(m_server.getServletUrl()), new ByteArrayContentEx(null, reqBytes, false));
      req.getHeaders().setCacheControl("no-cache");
      req.getHeaders().setContentType("application/octet-stream");
      req.getHeaders().put("Pragma", "no-cache");

      HttpResponse resp = req.execute();
      byte[] respBytes2 = IOUtility.readBytes(resp.getContent());
      Assert.assertArrayEquals(respBytes, respBytes2);
    }
  }
}
