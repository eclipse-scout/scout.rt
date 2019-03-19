/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http.retry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

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
    if (request instanceof HttpEntityEnclosingRequest) {
      final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
      if (entity != null && !(entity instanceof OneTimeRepeatableRequestEntityProxy)) {
        ((HttpEntityEnclosingRequest) request).setEntity(new OneTimeRepeatableRequestEntityProxy(entity));
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
  public long getContentLength() {
    return m_original.getContentLength();
  }

  @Override
  public Header getContentType() {
    return m_original.getContentType();
  }

  @Override
  public Header getContentEncoding() {
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

  @SuppressWarnings("deprecation")
  @Override
  public void consumeContent() throws IOException {
    m_consumed = true;
    m_original.consumeContent();
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName())
        .append("{")
        .append(m_original)
        .append('}')
        .toString();
  }
}
