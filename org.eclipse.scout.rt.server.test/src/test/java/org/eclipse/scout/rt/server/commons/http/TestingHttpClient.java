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

import java.io.IOException;
import java.net.Socket;
import java.util.function.Supplier;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.HttpResponseInformationCallback;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.ApacheHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;

import com.google.api.client.http.HttpTransport;

/**
 * HTTP Client supporting socket level interception of http requests/responses. Used to trigger and force errors and
 * failures.
 */
@IgnoreBean
public class TestingHttpClient extends DefaultHttpTransportManager {

  @FunctionalInterface
  public interface IExecuteInterceptor {
    ClassicHttpResponse execute(ClassicHttpRequest request, HttpClientConnection conn, HttpResponseInformationCallback informationCallback, HttpContext context, IExecuteInterceptor superExecuteCall) throws IOException, HttpException;
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
              .build());
      return connManager;
    }
  }

  private IExecuteInterceptor m_executeInterceptor;

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
      public ClassicHttpResponse execute(ClassicHttpRequest request, HttpClientConnection conn, HttpResponseInformationCallback informationCallback, HttpContext context) throws IOException, HttpException {
        if (m_executeInterceptor != null) {
          return m_executeInterceptor.execute(request, conn, informationCallback, context, (request0, conn0, informationCallback0, context0, interceptor) -> super.execute(request0, conn0, informationCallback0, context0));
        }
        else {
          return super.execute(request, conn, informationCallback, context);
        }
      }
    });
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

  public TestingHttpClient withExecuteInterceptor(IExecuteInterceptor executeInterceptor) {
    m_executeInterceptor = executeInterceptor;
    return this;
  }
}
