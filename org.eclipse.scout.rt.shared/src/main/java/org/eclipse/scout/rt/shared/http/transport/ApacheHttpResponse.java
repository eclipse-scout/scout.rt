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

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.eclipse.scout.rt.platform.util.StringUtility;

import com.google.api.client.http.LowLevelHttpResponse;

/**
 * <p>
 * Internal {@link LowLevelHttpResponse} for {@link ApacheHttpTransport}.
 * </p>
 *
 * @see ApacheHttpTransport
 */
public class ApacheHttpResponse extends LowLevelHttpResponse {

  private final HttpUriRequestBase m_request;
  private final ClassicHttpResponse m_response;

  public ApacheHttpResponse(HttpUriRequestBase request, ClassicHttpResponse response) {
    m_request = request;
    m_response = response;
  }

  @Override
  public InputStream getContent() throws IOException {
    HttpEntity entity = m_response.getEntity();
    return entity != null ? entity.getContent() : null;
  }

  @Override
  public String getContentEncoding() {
    HttpEntity entity = m_response.getEntity();
    String contentEncoding = entity != null ? entity.getContentEncoding() : null;
    return contentEncoding;
  }

  @Override
  public long getContentLength() throws IOException {
    HttpEntity entity = m_response.getEntity();
    return entity != null ? entity.getContentLength() : 0;
  }

  @Override
  public String getContentType() throws IOException {
    HttpEntity entity = m_response.getEntity();
    String contentType = entity != null ? entity.getContentType() : null;
    return contentType;
  }

  @Override
  public String getStatusLine() throws IOException {
    return StringUtility.join(" ", getStatusCode(), getReasonPhrase());
  }

  @Override
  public int getStatusCode() throws IOException {
    return m_response.getCode();
  }

  @Override
  public String getReasonPhrase() {
    return m_response.getReasonPhrase();
  }

  @Override
  public int getHeaderCount() {
    return m_response.getHeaders().length;
  }

  @Override
  public String getHeaderName(int index) {
    return m_response.getHeaders()[index].getName();
  }

  @Override
  public String getHeaderValue(int index) {
    return m_response.getHeaders()[index].getValue();
  }

  @Override
  public void disconnect() throws IOException {
    m_request.abort();
  }
}
