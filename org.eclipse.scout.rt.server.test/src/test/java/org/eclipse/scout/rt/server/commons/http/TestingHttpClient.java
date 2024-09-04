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
import java.net.Socket;
import java.util.function.Supplier;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.ApacheHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;

import com.google.api.client.http.HttpTransport;

/**
 * HTTP Client supporting interception of http. Used to trigger and force errors and failures.
 */
@IgnoreBean
public class TestingHttpClient extends DefaultHttpTransportManager {
  public static final int BUFFER_SIZE = 32;

  @FunctionalInterface
  public interface IResponseProvider {
    HttpResponse call() throws IOException, HttpException;
  }

  @FunctionalInterface
  public interface IRequestInterceptor {
    HttpResponse intercept(HttpRequest request, HttpClientConnection conn, HttpContext context, IResponseProvider superCall) throws IOException, HttpException;
  }

  @FunctionalInterface
  public interface IResponseInterceptor {
    HttpResponse intercept(HttpRequest request, HttpClientConnection conn, HttpContext context, IResponseProvider superCall);
  }

  /**
   * Add interception on socket level
   */
  private class ApacheHttpTransportFactoryEx extends ApacheHttpTransportFactory {
    @Override
    protected PlainConnectionSocketFactory createPlainSocketFactory() {
      return new PlainConnectionSocketFactory() {
        @Override
        public Socket createSocket(HttpContext context) throws IOException {
          if (m_socketReadInterceptor == null && m_socketWriteInterceptor == null) {
            return super.createSocket(context);
          }
          SocketWithInterception socket = new SocketWithInterception();
          if (m_socketReadInterceptor != null) {
            socket.withInterceptRead(m_socketReadInterceptor.get());
          }
          if (m_socketWriteInterceptor != null) {
            socket.withInterceptWrite(m_socketWriteInterceptor.get());
          }
          return socket;
        }
      };
    }

    @Override
    protected HttpClientConnectionManager createHttpClientConnectionManager(IHttpTransportManager manager) {
      PoolingHttpClientConnectionManager connManager = (PoolingHttpClientConnectionManager) super.createHttpClientConnectionManager(manager);
      connManager.setDefaultConnectionConfig(
          ConnectionConfig
              .custom()
              .setFragmentSizeHint(BUFFER_SIZE)
              .setBufferSize(BUFFER_SIZE)
              .build());
      return connManager;
    }
  }

  private IRequestInterceptor m_requestInterceptor;
  private IResponseInterceptor m_responseInterceptor;

  private Supplier<SocketWithInterception.ISocketReadInterceptor> m_socketReadInterceptor;
  private Supplier<SocketWithInterception.ISocketWriteInterceptor> m_socketWriteInterceptor;

  @Override
  protected HttpTransport createHttpTransport() {
    return new ApacheHttpTransportFactoryEx().newHttpTransport(this);
  }

  @Override
  public void interceptNewHttpTransport(IHttpTransportBuilder builder) {
    ApacheHttpTransportBuilder builder0 = (ApacheHttpTransportBuilder) builder;
    builder0.getBuilder().setRequestExecutor(new HttpRequestExecutor() {
      @Override
      protected HttpResponse doSendRequest(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        if (m_requestInterceptor != null) {
          return m_requestInterceptor.intercept(request, conn, context, () -> super.doSendRequest(request, conn, context));
        }
        else {
          return super.doSendRequest(request, conn, context);
        }
      }

      @Override
      protected HttpResponse doReceiveResponse(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
        if (m_responseInterceptor != null) {
          return m_responseInterceptor.intercept(request, conn, context, () -> super.doReceiveResponse(request, conn, context));
        }
        else {
          return super.doReceiveResponse(request, conn, context);
        }
      }
    });
  }

  /**
   * Install a handler that intercepts all requests. Can be used to simulate network interruptions or socket errors.
   */
  public TestingHttpClient withRequestInterceptor(IRequestInterceptor requestInterceptor) {
    m_requestInterceptor = requestInterceptor;
    return this;
  }

  /**
   * Install a handler that intercepts all responses. Can be used to simulate network interruptions or socket errors.
   */
  public TestingHttpClient withResponseInterceptor(IResponseInterceptor responseInterceptor) {
    m_responseInterceptor = responseInterceptor;
    return this;
  }

  public TestingHttpClient withSocketReadInterceptor(Supplier<SocketWithInterception.ISocketReadInterceptor> socketReadInterceptor) {
    m_socketReadInterceptor = socketReadInterceptor;
    return this;
  }

  public TestingHttpClient withSocketWriteInterceptor(Supplier<SocketWithInterception.ISocketWriteInterceptor> socketWriteInterceptor) {
    m_socketWriteInterceptor = socketWriteInterceptor;
    return this;
  }

  public void stop() {
    removeHttpTransport();
  }
}
