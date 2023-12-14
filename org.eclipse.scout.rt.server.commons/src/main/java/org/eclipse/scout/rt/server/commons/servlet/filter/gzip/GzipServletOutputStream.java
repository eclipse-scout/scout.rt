/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.gzip;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GzipServletOutputStream extends ServletOutputStream {

  private static final Logger LOG = LoggerFactory.getLogger(GzipServletOutputStream.class);

  private final HttpServletResponse m_response;
  private final ServletOutputStream m_servletOutputStream;

  private GZIPOutputStream m_gzipOutputStream;
  private byte[] m_buf;
  private int m_bufCount = 0;

  private volatile Object m_writeListener;

  public GzipServletOutputStream(int compressThreshold, HttpServletResponse response) throws IOException {
    super();
    m_response = response;
    m_servletOutputStream = response.getOutputStream();
    m_buf = createBuffer(compressThreshold);
  }

  protected byte[] createBuffer(int size) {
    return new byte[size];
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
    m_writeListener = Assertions.assertNotNull(writeListener);
    try {
      writeListener.onWritePossible();
    }
    catch (IOException e) {
      writeListener.onError(e);
    }
  }

  protected GZIPOutputStream ensureGzipOutStream() throws IOException {
    if (m_gzipOutputStream == null) {
      m_response.addHeader(GzipServletFilter.CONTENT_ENCODING, GzipServletFilter.GZIP);
      m_gzipOutputStream = new GZIPOutputStream(m_servletOutputStream);
    }
    return m_gzipOutputStream;
  }

  protected void flushBufferToGzipOutputStream() throws IOException {
    if (m_bufCount == 0) {
      return;
    }
    writeToGzipOutputStream(m_buf, 0, m_bufCount);
    m_bufCount = 0;
  }

  protected void writeToGzipOutputStream(byte[] buffer, int off, int len) throws IOException {
    //noinspection resource
    ensureGzipOutStream().write(buffer, off, len);
  }

  protected boolean fitsIntoBuffer(int len) {
    return (m_buf.length - m_bufCount) >= len;
  }

  @Override
  public void write(int b) throws IOException {
    if (m_gzipOutputStream == null && fitsIntoBuffer(1)) {
      m_buf[m_bufCount] = (byte) b;
      ++m_bufCount;
      return;
    }

    try {
      flushBufferToGzipOutputStream();
      writeToGzipOutputStream(new byte[]{(byte) b}, 0, 1);
    }
    catch (IOException e) {
      WriteListener listener = (WriteListener) m_writeListener;
      if (listener != null) {
        listener.onError(e);
      }
      throw e;
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (m_gzipOutputStream == null && fitsIntoBuffer(len)) { // enough space in buffer
      System.arraycopy(b, off, m_buf, m_bufCount, len);
      m_bufCount += len;
      return;
    }

    try {
      flushBufferToGzipOutputStream();
      writeToGzipOutputStream(b, off, len);
    }
    catch (Exception e) {
      WriteListener listener = (WriteListener) m_writeListener;
      if (listener != null) {
        listener.onError(e);
      }
      if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(e)) {
        // Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
        LOG.debug("Connection error detected: exception class={}, message={}.", e.getClass().getSimpleName(), e.getMessage(), e);
        return;
      }
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    if (m_gzipOutputStream != null) {
      m_gzipOutputStream.close();
      m_gzipOutputStream = null;
    }
    else if (m_bufCount > 0) {
      m_servletOutputStream.write(m_buf, 0, m_bufCount);
      m_bufCount = 0;
    }
  }

  /*
   * In case where  m_gzipOutputStream is null and m_bufCount contains some written data, calling this method will not
   * flush the data to the output stream. Data located in the memory buffer will be written to the output stream when
   * close is called.
   */
  @Override
  public void flush() throws IOException {
    if (m_gzipOutputStream != null) {
      m_gzipOutputStream.flush();
    }
  }
}
