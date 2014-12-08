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
package org.eclipse.scout.rt.ui.html.gzip;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.scout.rt.ui.html.StreamUtility;

class GZIPServletResponseWrapper extends HttpServletResponseWrapper {
  private BufferedServletOutputStream m_buf;
  //one of these two is used
  private ServletOutputStream m_servletOut;
  private PrintWriter m_writer;

  public GZIPServletResponseWrapper(HttpServletResponse resp) throws IOException {
    super(resp);
  }

  protected BufferedServletOutputStream ensureBufferedStream() throws IOException {
    if (m_buf == null) {
      m_buf = new BufferedServletOutputStream(getResponse().getOutputStream());
    }
    return m_buf;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (m_writer != null) {
      throw new IllegalStateException("getWriter was called, getOutputStream is not available");
    }
    if (m_servletOut == null) {
      m_servletOut = ensureBufferedStream();
    }
    return m_servletOut;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (m_servletOut != null) {
      throw new IllegalStateException("getOutputStream was called, getWriter is not available");
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

  public void finish() throws IOException {
    if (m_writer != null) {
      m_writer.close();
    }
    if (m_buf != null) {
      m_buf.close();
      HttpServletResponse res = (HttpServletResponse) getResponse();
      byte[] bytes = StreamUtility.compressGZIP(m_buf.getContent());
      res.addHeader(GZIPServletFilter.CONTENT_ENCODING, GZIPServletFilter.GZIP);
      res.setContentLength(bytes.length);
      res.getOutputStream().write(bytes);
      super.flushBuffer();
    }
  }

}
