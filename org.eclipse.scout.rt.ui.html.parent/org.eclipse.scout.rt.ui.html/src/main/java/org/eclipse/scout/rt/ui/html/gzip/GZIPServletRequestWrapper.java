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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.StreamUtility;

class GZIPServletRequestWrapper extends HttpServletRequestWrapper {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(GZIPServletRequestWrapper.class);

  private BufferedServletInputStream m_buf;

  public GZIPServletRequestWrapper(HttpServletRequest req) throws IOException {
    super(req);
  }

  protected BufferedServletInputStream ensureBufferedStream() throws IOException {
    if (m_buf == null) {
      m_buf = new BufferedServletInputStream(StreamUtility.uncompressGZIP(StreamUtility.readStream(super.getInputStream(), super.getContentLength())));
    }
    return m_buf;
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

}
