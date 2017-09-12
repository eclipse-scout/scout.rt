/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.filter.gzip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.BufferedServletInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GzipServletRequestWrapper extends HttpServletRequestWrapper {
  private static final Logger LOG = LoggerFactory.getLogger(GzipServletRequestWrapper.class);

  private BufferedServletInputStream m_buf;
  private int m_compressedLength = -1;
  private int m_uncompressedLength = -1;

  public GzipServletRequestWrapper(HttpServletRequest req) {
    super(req);
  }

  protected BufferedServletInputStream ensureBufferedStream() throws IOException {
    if (m_buf == null) {
      byte[] gzipped = IOUtility.readBytes(super.getInputStream(), super.getContentLength());
      m_compressedLength = gzipped.length;
      m_buf = new BufferedServletInputStream(IOUtility.uncompressGzip(gzipped));
      m_uncompressedLength = m_buf.getLength();
    }
    return m_buf;
  }

  public int getUncompressedLength() {
    return m_uncompressedLength;
  }

  public int getCompressedLength() {
    return m_compressedLength;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return ensureBufferedStream();
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(ensureBufferedStream()));
  }

  /**
   * since servlet api 3.1
   */
  @Override
  public long getContentLengthLong() {
    return getContentLength();
  }

  @Override
  public int getContentLength() {
    try {
      return ensureBufferedStream().getLength();
    }
    catch (IOException ex) {
      LOG.warn("Error while reading GZIP content", ex);
      return -1;
    }
  }

  @Override
  public String getHeader(String name) {
    if (GzipServletFilter.CONTENT_ENCODING.equals(name)) {
      return null;
    }
    return super.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    if (GzipServletFilter.CONTENT_ENCODING.equals(name)) {
      return Collections.emptyEnumeration();
    }
    return super.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    Enumeration<String> names = super.getHeaderNames();
    if (names != null) {
      List<String> list = Collections.list(names);
      list.remove(GzipServletFilter.CONTENT_ENCODING);
      return Collections.enumeration(list);
    }
    return names;
  }
}
