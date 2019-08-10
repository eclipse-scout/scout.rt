/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.Preconditions;

/**
 * Implementation of {@link AbstractInputStreamContent} based on a byte array that is repeatable depending on the flag
 * retrySupported.
 *
 * @since 9.0
 */
public class ByteArrayContentEx extends AbstractInputStreamContent {
  private final byte[] m_bytes;
  private final int m_offset;
  private final int m_length;
  private boolean m_retrySupported;

  public ByteArrayContentEx(String type, byte[] array, boolean retrySupported) {
    this(type, array, 0, array.length, retrySupported);
  }

  public ByteArrayContentEx(String type, byte[] array, int offset, int length, boolean retrySupported) {
    super(type);
    Preconditions.checkArgument(offset >= 0 && length >= 0 && offset + length <= array.length, "offset %s, length %s, array length %s", offset, length, array.length);
    m_bytes = Preconditions.checkNotNull(array);
    m_offset = offset;
    m_length = length;
    m_retrySupported = retrySupported;
  }

  @Override
  public long getLength() {
    return m_length;
  }

  @Override
  public boolean retrySupported() {
    return m_retrySupported;
  }

  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(m_bytes, m_offset, m_length);
  }

  public ByteArrayContentEx setRetrySupported(boolean retrySupported) {
    m_retrySupported = retrySupported;
    return this;
  }

  @Override
  public ByteArrayContentEx setType(String type) {
    super.setType(type);
    return this;
  }

  @Override
  public ByteArrayContentEx setCloseInputStream(boolean closeInputStream) {
    super.setCloseInputStream(closeInputStream);
    return this;
  }
}
