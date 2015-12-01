/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.filter.gzip;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;

public class GzipServletResponseWrapper extends HttpServletResponseWrapper {

  private BufferedServletOutputStream m_buf;
  private int m_compressedLength = -1;
  private int m_uncompressedLength = -1;
  //one of these two is used
  private ServletOutputStream m_servletOut;
  private PrintWriter m_writer;

  public GzipServletResponseWrapper(HttpServletResponse resp) throws IOException {
    super(resp);
  }

  protected BufferedServletOutputStream ensureBufferedStream() throws IOException {
    if (m_buf == null) {
      m_buf = new BufferedServletOutputStream();
    }
    return m_buf;
  }

  /**
   * only valid after {@link #finish(int)} was called
   */
  public int getCompressedLength() {
    return m_compressedLength;
  }

  /**
   * only valid after {@link #finish(int)} was called
   */
  public int getUncompressedLength() {
    return m_uncompressedLength;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (m_writer != null) {
      throw new IllegalStateException("getWriter was previsouly called, getOutputStream is not available");
    }
    if (m_servletOut == null) {
      m_servletOut = ensureBufferedStream();
    }
    return m_servletOut;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (m_servletOut != null) {
      throw new IllegalStateException("getOutputStream was previsouly called, getWriter is not available");
    }
    if (m_writer == null) {
      m_writer = new PrintWriter(new OutputStreamWriter(ensureBufferedStream(), getResponse().getCharacterEncoding()));
    }
    return m_writer;
  }

  @Override
  public void setContentLength(int len) {
    //ignored
  }

  @Override
  public void flushBuffer() throws IOException {
    if (m_writer != null) {
      m_writer.flush();
    }
    if (m_buf != null) {
      m_buf.flush();
    }
    super.flushBuffer();
  }

  /**
   * @param minimumLengthToCompress
   *          is the minimum uncompressed size that is compressed, -1 disables compression
   * @return true if the content was compressed
   */
  public boolean finish(int minimumLengthToCompress) throws IOException {
    if (m_writer != null) {
      m_writer.close();
      m_writer = null;
    }
    boolean compressed = false;
    if (m_buf != null) {
      m_buf.close();
      byte[] raw = m_buf.getContent();
      m_uncompressedLength = raw.length;
      m_buf = null;

      HttpServletResponse res = (HttpServletResponse) getResponse();
      byte[] gzipped;
      if (minimumLengthToCompress >= 0 && m_uncompressedLength >= minimumLengthToCompress) {
        gzipped = IOUtility.compressGzip(raw);
        res.addHeader(GzipServletFilter.CONTENT_ENCODING, GzipServletFilter.GZIP);
        compressed = true;
      }
      else {
        gzipped = raw;
      }
      m_compressedLength = gzipped.length;
      raw = null;
      res.setContentLength(gzipped.length);
      res.getOutputStream().write(gzipped);
      super.flushBuffer();
    }
    return compressed;
  }
}
