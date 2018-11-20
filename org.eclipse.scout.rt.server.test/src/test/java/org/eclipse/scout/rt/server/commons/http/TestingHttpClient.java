/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.http;

import java.io.IOException;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.ApacheHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportBuilder;

/**
 * HTTP Client supporting interception of http. Used to trigger and force errors and failures.
 */
@IgnoreBean
public class TestingHttpClient extends DefaultHttpTransportManager {

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
    HttpResponse intercept(HttpRequest request, HttpClientConnection conn, HttpContext context, IResponseProvider superCall) throws HttpException, IOException;
  }

  private IRequestInterceptor m_requestInterceptor;
  private IResponseInterceptor m_responseInterceptor;

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
  public void setRequestInterceptor(IRequestInterceptor requestInterceptor) {
    m_requestInterceptor = requestInterceptor;
  }

  /**
   * Install a handler that intercepts all responses. Can be used to simulate network interruptions or socket errors.
   */
  public void setResponseInterceptor(IResponseInterceptor responseInterceptor) {
    m_responseInterceptor = responseInterceptor;
  }

  public void stop() {
    removeHttpTransport();
  }
}
