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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.http.protocol.HTTP;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GzipServletResponseWrapper extends HttpServletResponseWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(GzipServletResponseWrapper.class);

  private PrintWriter m_writer;
  private ServletOutputStream m_outputStream;

  private final HttpServletRequest m_request;
  private final int m_compressThreshold;
  private final Set<String> m_contentTypes;
  private final boolean m_enableEmptyContentTypeLogging;

  public GzipServletResponseWrapper(HttpServletResponse response, HttpServletRequest request, int compressThreshold, Set<String> contentTypes, boolean enableEmptyContentTypeLogging) {
    super(response);
    m_request = request;
    m_compressThreshold = compressThreshold;
    m_contentTypes = contentTypes;
    m_enableEmptyContentTypeLogging = enableEmptyContentTypeLogging;
  }

  @Override
  public HttpServletResponse getResponse() {
    return (HttpServletResponse) super.getResponse();
  }

  protected GzipServletOutputStream createGzipServletOutputStream(int compressThreshold, HttpServletResponse response) throws IOException {
    return new GzipServletOutputStream(compressThreshold, response);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (m_writer != null) {
      throw new IllegalStateException("getWriter was previously called, getOutputStream is not available");
    }

    if (m_outputStream == null) {
      m_outputStream = getOrCreateServletOutputStream();
    }
    return m_outputStream;
  }

  protected ServletOutputStream getOrCreateServletOutputStream() throws IOException {
    if (!requiresGzipCompression(getContentType())) {
      return getResponse().getOutputStream();
    }
    return createGzipServletOutputStream(m_compressThreshold, getResponse());
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (m_writer != null) {
      return m_writer;
    }

    if (m_outputStream != null) {
      throw new IllegalStateException("getOutputStream was previously called, getWriter is not available");
    }

    m_outputStream = getOrCreateServletOutputStream();
    m_writer = new PrintWriter(new OutputStreamWriter(m_outputStream, getResponse().getCharacterEncoding()));

    return m_writer;
  }

  @Override
  public void setContentLength(int len) {
    // ignored: content length zipped content != content length unzipped content
  }

  @Override
  public void setHeader(String name, String value) {
    if (HTTP.CONTENT_LEN.equalsIgnoreCase(name)) {
      // see setContentLength
      return;
    }
    super.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    if (HTTP.CONTENT_LEN.equalsIgnoreCase(name)) {
      // see setContentLength
      return;
    }
    super.addHeader(name, value);
  }

  @Override
  public void flushBuffer() throws IOException {
    if (m_writer != null) {
      m_writer.flush();
    }
    if (m_outputStream != null) {
      m_outputStream.flush();
    }
    super.flushBuffer();
  }

  public void finish() throws IOException {
    if (m_writer != null) {
      m_writer.close();
      m_writer = null;
    }

    if (m_outputStream != null) {
      m_outputStream.close();
      m_outputStream = null;
    }
  }

  protected boolean requiresGzipCompression(String contentType) {
    if (!UrlHints.isCompressHint(m_request)) {
      return false;
    }
    if (m_request.isAsyncStarted()) {
      // GzipServletOutputStream does not work with async responses unfortunately
      return false;
    }
    if (contentType == null) {
      if (m_enableEmptyContentTypeLogging) {
        LOG.warn("Content type of response is not defined for request path info {}.", m_request.getPathInfo());
      }
      return false;
    }
    // Content type may contain the charset parameter separated by ; -> remove it
    contentType = contentType.split(";")[0];
    return m_contentTypes.contains(contentType);
  }
}
