/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.retry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpRequest;

/**
 * A Proxy class for {@link org.apache.http.HttpEntity} that supports retry regardless of the enclosed
 * {@link HttpEntity#isRepeatable()}
 *
 * @since 7.0
 */
public class OneTimeRepeatableRequestEntityProxy implements HttpEntity {
  private final HttpEntity m_original;
  private boolean m_consumed;

  public static void installRetry(HttpRequest request) {
    if (request instanceof HttpEntityContainer) {
      final HttpEntity entity = ((HttpEntityContainer) request).getEntity();
      if (entity != null && !(entity instanceof OneTimeRepeatableRequestEntityProxy)) {
        ((HttpEntityContainer) request).setEntity(new OneTimeRepeatableRequestEntityProxy(entity));
      }
    }
  }

  public OneTimeRepeatableRequestEntityProxy(final HttpEntity original) {
    m_original = original;
  }

  public HttpEntity getOriginal() {
    return m_original;
  }

  @Override
  public boolean isRepeatable() {
    return !m_consumed;
  }

  @Override
  public boolean isChunked() {
    return m_original.isChunked();
  }

  @Override
  public Set<String> getTrailerNames() {
    return m_original.getTrailerNames();
  }

  @Override
  public long getContentLength() {
    return m_original.getContentLength();
  }

  @Override
  public String getContentType() {
    return m_original.getContentType();
  }

  @Override
  public String getContentEncoding() {
    return m_original.getContentEncoding();
  }

  @Override
  public InputStream getContent() throws IOException {
    return m_original.getContent();
  }

  @Override
  public void writeTo(final OutputStream outstream) throws IOException {
    m_consumed = true;
    m_original.writeTo(outstream);
  }

  @Override
  public boolean isStreaming() {
    return m_original.isStreaming();
  }

  @Override
  public Supplier<List<? extends Header>> getTrailers() {
    return m_original.getTrailers();
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName())
        .append("{")
        .append(m_original)
        .append('}')
        .toString();
  }

  @Override
  public void close() throws IOException {
    m_original.close();
  }
}
