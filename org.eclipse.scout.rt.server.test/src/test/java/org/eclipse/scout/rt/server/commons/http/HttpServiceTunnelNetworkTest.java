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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.server.commons.http.SocketWithInterception.ISocketReadInterceptor;
import org.eclipse.scout.rt.server.commons.http.SocketWithInterception.ISocketWriteInterceptor;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.http.RemoteServiceInvocationCallable;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple test if the Google HTTP Client API (here called 'google level') together with the Apache HTTP Client (here
 * called 'apache level') works as expected.
 * <p>
 * The Google HTTP Client API is not only an API, it contains an execution loop that handles various retry scenarios.
 * However in its core it uses a http transport - hers it is Apache HTTP Client.
 * <p>
 * Apache HTTP client also handles various http retry scenarios in its exec loop.
 */
@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class HttpServiceTunnelNetworkTest {
  private static final String X_TRUNCATE_BODY = "X-Truncate-Body";
  private static final String X_WAIT_AFTER_BYTES = "X-Wait-After-Bytes";

  private IBean<?> m_exceptionHandlerReplacement;
  private boolean m_sleepIsOver;
  private TestingHttpClient m_client;
  private TestingHttpServer m_server;

  private StringBuffer m_clientRead = new StringBuffer();
  private StringBuffer m_clientWrite = new StringBuffer();

  private static void sysout(Object o) {
    //System.out.println(o);
  }

  @Before
  public void before() {
    m_exceptionHandlerReplacement = BeanTestingHelper.get().registerBean(
        new BeanMetaData(UIExceptionHandler.class, new UIExceptionHandler())
            .withReplace(true));
    m_client = new TestingHttpClient();
    m_server = new TestingHttpServer(TestingHttpPorts.PORT_33005);
    m_server.start();
  }

  @After
  public void after() {
    m_sleepIsOver = true;
    BeanTestingHelper.get().unregisterBean(m_exceptionHandlerReplacement);
    m_client.stop();
    m_server.stop();
    sysout("# HttpClient.read\n" + m_clientRead + "\n");
    sysout("# HttpClient.write\n" + m_clientWrite + "\n");
  }

  @Test
  public void testPostWithChunkedResponseAndSuccess() throws SecurityException {
    m_client.withSocketReadInterceptor(() -> new ISocketReadInterceptor() {
      @Override
      public int read(InputStream in, byte[] buf, int off, int len) throws IOException {
        int n = in.read(buf, off, len);
        for (int i = 0; i < n; i++) {
          int b = buf[i] & 0xff;
          m_clientRead.append(b < 7 ? "#" + Integer.toHexString(b) : (char) b);
        }
        return n;
      }

      @Override
      public int read(InputStream in) throws IOException {
        int b = in.read();
        m_clientRead.append(b < 7 ? "#" + Integer.toHexString(b) : (char) b);
        return b;
      }
    });

    m_client.withSocketWriteInterceptor(() -> new ISocketWriteInterceptor() {
      @Override
      public void write(OutputStream out, byte[] buf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
          int b = buf[i] & 0xff;
          m_clientWrite.append(b < 7 ? "#" + Integer.toHexString(b) : (char) b);
        }
        out.write(buf, off, len);
      }

      @Override
      public void write(OutputStream out, int b) throws IOException {
        m_clientWrite.append(b < 7 ? "#" + Integer.toHexString(b) : (char) b);
        out.write(b);
      }
    });

    HttpServiceTunnel tunnel = new HttpServiceTunnel(m_server.getServletUrl()) {
      @Override
      protected IHttpTransportManager getHttpTransportManager() {
        return m_client;
      }
    };
    IServiceTunnelContentHandler contentHandler = new BinaryServiceTunnelContentHandler();
    contentHandler.initialize();
    tunnel.setContentHandler(contentHandler);

    m_server.withServletPostHandler((req, resp) -> {
      //consume input to avoid java.net.SocketException: Software caused connection abort: socket write error
      IOUtility.readBytes(req.getInputStream(), req.getContentLength());

      resp.setContentType(contentHandler.getContentType());
      resp.setHeader("Transfer-Encoding", "chunked");
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      ServiceTunnelResponse msg = new ServiceTunnelResponse("bar", null);
      contentHandler.writeResponse(buf, msg);
      byte[] bytes = buf.toByteArray();
      resp.getOutputStream().write(bytes, 0, bytes.length);
      resp.getOutputStream().flush();
    });

    String ret = RunContexts.empty().call(() -> (String) tunnel.invokeService(IFixturePageService.class, IFixturePageService.class.getMethod("getGlobalSearchRowCount", String.class), new Object[]{"foo"}));
    Assert.assertEquals("bar", ret);
  }

  /**
   * The chunked read is interrupted just in the moment when it is reading the chunk size
   * <p>
   * Test the following rather complex issue: When a backend service tunnel call is cancelled, the
   * {@link RunMonitor#cancel(boolean)} cancels all attached {@link ICancellable}. One of them is the
   * {@link RemoteServiceInvocationCallable} that is waiting for a rmeote http response. The invokation of
   * {@link RemoteServiceInvocationCallable#cancel()} starts a parallel backend http call - again through the service
   * tunnel - calling {@link IRunMonitorCancelService#cancel(long)}.
   */
  @Test
  public void testPostWithChunkedResponseThatIsCancelled() throws SecurityException {
    m_client.withSocketReadInterceptor(() -> new ISocketReadInterceptor() {
      int m_contentStart;
      int m_truncatedContentLength;
      int m_waitAfterContentBytes;
      final StringBuffer m_buf = new StringBuffer();

      private int intercept(int b) {
        m_clientRead.append((char) b);
        m_buf.append((char) b);

        Matcher m = Pattern.compile(Pattern.quote(X_TRUNCATE_BODY + ": ") + "([0-9]+)").matcher(m_buf.toString());
        if (m.find()) {
          m_truncatedContentLength = Integer.parseInt(m.group(1));
        }
        m = Pattern.compile(Pattern.quote(X_WAIT_AFTER_BYTES + ": ") + "([0-9]+)").matcher(m_buf.toString());
        if (m.find()) {
          m_waitAfterContentBytes = Integer.parseInt(m.group(1));
        }
        if (m_contentStart == 0 && m_buf.toString().endsWith("\r\n\r\n")) {
          m_contentStart = m_buf.length();
          sysout("--> Content start at " + m_contentStart);
        }
        //evaluate in reverse order, larger values first
        if (m_contentStart > 0 && m_truncatedContentLength > 0 && m_buf.length() == m_contentStart + m_truncatedContentLength) {
          //emulate that the server just finished sending this chunk and then got interrupted then
          sysout("--> Truncate stream on content position " + m_truncatedContentLength);
          return -1;
        }
        if (m_contentStart > 0 && m_waitAfterContentBytes > 0 && m_buf.length() == m_contentStart + m_waitAfterContentBytes) {
          sysout("--> Wait after " + m_waitAfterContentBytes + " body bytes");
          while (!m_sleepIsOver) {
            try {
              Thread.sleep(100L);
            }
            catch (InterruptedException e) {
              //nop
            }
          }
        }
        return b;
      }

      @Override
      public int read(InputStream in, byte[] buf, int off, int len) throws IOException {
        int n = in.read(buf, off, len);
        for (int i = 0; i < n; i++) {
          int b = intercept(buf[i]);
          if (b < 0) {
            return i;
          }
          m_clientRead.append(b < 30 ? "#" + Integer.toHexString(b) : (char) b);
        }
        return n;
      }

      @Override
      public int read(InputStream in) throws IOException {
        int b = intercept(in.read());
        if (b < 0) {
          return b;
        }
        m_clientRead.append(b < 30 ? "#" + Integer.toHexString(b) : (char) b);
        return b;
      }
    });

    m_client.withSocketWriteInterceptor(() -> new ISocketWriteInterceptor() {
      @Override
      public void write(OutputStream out, byte[] buf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
          int b = buf[i];
          m_clientWrite.append(b < 30 ? "#" + Integer.toHexString(b) : (char) b);
        }
        out.write(buf, off, len);
      }

      @Override
      public void write(OutputStream out, int b) throws IOException {
        m_clientWrite.append(b < 30 ? "#" + Integer.toHexString(b) : (char) b);
        out.write(b);
      }
    });

    HttpServiceTunnel tunnel = new HttpServiceTunnel(m_server.getServletUrl()) {
      @Override
      protected IHttpTransportManager getHttpTransportManager() {
        return m_client;
      }

      @Override
      protected void interceptRequest(ServiceTunnelRequest request) {
        request.setUserAgent(UserAgents.createDefault().createIdentifier());
        request.setSessionId("testSession123");
        request.setClientNodeId(NodeId.current());
      }
    };
    //emulate interrupted exception while reading the object
    IServiceTunnelContentHandler contentHandler = new BinaryServiceTunnelContentHandler();
    contentHandler.initialize();
    tunnel.setContentHandler(contentHandler);

    m_server.withServletPostHandler((req, resp) -> {
      ServiceTunnelRequest sreq;
      try {
        sreq = contentHandler.readRequest(req.getInputStream());
      }
      catch (ClassNotFoundException e) {
        throw new ProcessingException("class not found", e);
      }
      sysout("Service tunnel request: " + sreq);
      String op = sreq.getServiceInterfaceClassName() + "." + sreq.getOperation();
      resp.setContentType(contentHandler.getContentType());
      resp.setHeader("Transfer-Encoding", "chunked");
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      byte[] bytes;
      if (op.endsWith("IFixturePageService.getGlobalSearchRowCount")) {
        //let this one be last for ages
        ServiceTunnelResponse sresp = new ServiceTunnelResponse("bar", null);
        contentHandler.writeResponse(buf, sresp);
        bytes = buf.toByteArray();
        int chunkedBodyLength = 4/*chunk-len + crlf*/ + bytes.length + 2/*crlf*/;//the length of the chunked body without the final 0 eof marker
        resp.setIntHeader(X_WAIT_AFTER_BYTES, chunkedBodyLength);
        resp.getOutputStream().write(bytes, 0, bytes.length);
        resp.getOutputStream().flush();
      }
      else if (op.endsWith("IRunMonitorCancelService.cancel")) {
        //let this one be truncated
        ServiceTunnelResponse sresp = new ServiceTunnelResponse(true, null);
        contentHandler.writeResponse(buf, sresp);
        bytes = buf.toByteArray();
        //remove the chunked encoding eof marker in the response by truncating the response stream
        int chunkedBodyLength = 4/*chunk-len + crlf*/ + bytes.length + 2/*crlf*/;
        resp.setIntHeader(X_TRUNCATE_BODY, chunkedBodyLength);
        resp.getOutputStream().write(bytes, 0, bytes.length);
        resp.getOutputStream().flush();
      }
      else {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unit test: unknown service op '" + op + "'");
      }
    });

    RunMonitor runMonitor = new RunMonitor();
    IFuture<String> f = Jobs.schedule(
        () -> (String) tunnel.invokeService(IFixturePageService.class, IFixturePageService.class.getMethod("getGlobalSearchRowCount", String.class), new Object[]{"foo"}),
        Jobs.newInput()
            .withRunContext(RunContexts.empty().withRunMonitor(runMonitor)));

    try {
      runMonitor.cancel(true);
      f.awaitDoneAndGet();
      Assert.fail("must fail");
    }
    catch (Throwable e) {//NOSONAR
      e.printStackTrace();
    }
  }

  public interface IFixturePageService {
    public String getGlobalSearchRowCount(String arg);
  }

  @IgnoreBean
  private static class UIExceptionHandler extends ExceptionHandler {
    @Override
    protected void handlePlatformException(final PlatformException e) {
      super.handlePlatformException(e);
      showExceptionInternal(e);
    }

    @Override
    protected void handleThrowable(final Throwable t) {
      super.handleThrowable(t);
      showExceptionInternal(t);
    }

    protected void showExceptionInternal(final Throwable t) {
      sysout("MESSAGE_BOX: " + t);
    }
  }
}
