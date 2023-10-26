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
import org.apache.hc.core5.http.protocol.HttpContext;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;

import com.google.api.client.http.HttpTransport;

/**
 * HTTP Client supporting socket level interception of http requests/responses. Used to trigger and force errors and failures.
 */
@IgnoreBean
public class TestingHttpClient extends DefaultHttpTransportManager {

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

  private Supplier<SocketWithInterception.ISocketReadInterceptor> m_socketReadInterceptor;
  private Supplier<SocketWithInterception.ISocketWriteInterceptor> m_socketWriteInterceptor;

  @Override
  protected HttpTransport createHttpTransport() {
    return new ApacheHttpTransportFactoryEx().newHttpTransport(this);
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
