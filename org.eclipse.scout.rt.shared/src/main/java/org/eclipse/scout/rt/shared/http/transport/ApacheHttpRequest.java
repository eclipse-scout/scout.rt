/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.RequestConfig.Builder;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.StreamingContent;

/**
 * <p>
 * Internal {@link LowLevelHttpRequest} for {@link ApacheHttpTransport}.
 * </p>
 *
 * @see ApacheHttpTransport
 */
public class ApacheHttpRequest extends LowLevelHttpRequest {

  private final HttpClient m_httpClient;

  private final HttpUriRequestBase m_request;

  public ApacheHttpRequest(HttpClient httpClient, HttpUriRequestBase request) {
    m_httpClient = httpClient;
    m_request = request;
  }

  @Override
  public void addHeader(String name, String value) throws IOException {
    m_request.addHeader(name, value);
  }

  @Override
  public void setTimeout(int connectTimeout, int readTimeout) throws IOException {
    super.setTimeout(connectTimeout, readTimeout);
    RequestConfig config = m_request.getConfig();
    Builder configBuilder = config != null ? RequestConfig.copy(config) : RequestConfig.custom();
    // Google HTTP client seems to still support connect timeout per request
    //noinspection deprecation
    configBuilder.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
    configBuilder.setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS);
    m_request.setConfig(configBuilder.build());
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    @SuppressWarnings("deprecation") final StreamingContent streamingContent = getStreamingContent();
    if (streamingContent != null) {
      AbstractHttpEntity entity = new AbstractHttpEntity(getContentType(), getContentEncoding()) {

        @Override
        public void close() {
          // implementations must not close stream, see javadoc of StreamingContent
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
          streamingContent.writeTo(outstream);
          outstream.flush();
        }

        @Override
        public boolean isStreaming() {
          return true;
        }

        @Override
        public boolean isRepeatable() {
          if (streamingContent instanceof HttpContent) {
            return ((HttpContent) streamingContent).retrySupported();
          }
          return false;
        }

        @Override
        public long getContentLength() {
          return ApacheHttpRequest.this.getContentLength();
        }

        @Override
        public InputStream getContent() {
          throw new UnsupportedOperationException("Streaming entity cannot be represented as an input stream.");
        }
      };
      m_request.setEntity(entity);
    }
    return createResponseInternal();
  }

  protected ApacheHttpResponse createResponseInternal() throws IOException {
    try {
      return new ApacheHttpResponse(m_request, m_httpClient.executeOpen(RoutingSupport.determineHost(m_request), m_request, null));
    }
    catch (HttpException e) {
      throw new IOException("Unable to determine host for request", e);
    }
  }
}
