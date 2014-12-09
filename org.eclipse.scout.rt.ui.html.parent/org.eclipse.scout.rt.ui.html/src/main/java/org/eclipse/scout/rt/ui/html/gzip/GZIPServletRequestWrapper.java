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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.StreamUtility;

class GZIPServletRequestWrapper extends HttpServletRequestWrapper {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(GZIPServletRequestWrapper.class);

  private BufferedServletInputStream m_buf;
  private int m_compressedLength = -1;
  private int m_uncompressedLength = -1;

  public GZIPServletRequestWrapper(HttpServletRequest req) throws IOException {
    super(req);
  }

  protected BufferedServletInputStream ensureBufferedStream() throws IOException {
    if (m_buf == null) {
      byte[] gzipped = StreamUtility.readStream(super.getInputStream(), super.getContentLength());
      m_compressedLength = gzipped.length;
      m_buf = new BufferedServletInputStream(StreamUtility.uncompressGZIP(gzipped));
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
      LOG.warn("reading gzip content", ex);
      return -1;
    }
  }

  @Override
  public String getHeader(String name) {
    if (GZIPServletFilter.CONTENT_ENCODING.equals(name)) {
      return null;
    }
    return super.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    if (GZIPServletFilter.CONTENT_ENCODING.equals(name)) {
      return Collections.emptyEnumeration();
    }
    return super.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    Enumeration<String> names = super.getHeaderNames();
    if (names != null) {
      ArrayList<String> list = Collections.list(names);
      list.remove(GZIPServletFilter.CONTENT_ENCODING);
      return Collections.enumeration(list);
    }
    return names;
  }

}
