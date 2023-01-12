/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import org.apache.http.RequestLine;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.message.LineFormatter;
import org.apache.http.util.CharArrayBuffer;
import org.eclipse.scout.rt.rest.IRestHttpRequestUriEncoder;

/**
 * {@link LineFormatter} implementation that allows for customizing the request URI (as defined in
 * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>) encoding using a custom
 * {@link IRestHttpRequestUriEncoder}. Primarily useful for REST-APIs that define a custom URI encoding scheme, such as
 * S3.
 */
public class LineFormatterWithUriEncoder extends BasicLineFormatter {

  private final IRestHttpRequestUriEncoder m_requestUriEncoder;

  public LineFormatterWithUriEncoder(IRestHttpRequestUriEncoder requestUriEncoder) {
    m_requestUriEncoder = requestUriEncoder;
  }

  @Override
  protected void doFormatRequestLine(CharArrayBuffer buffer, RequestLine reqline) {
    final String method = reqline.getMethod();

    // <customized>
    String uri = m_requestUriEncoder.encodeRequestUri(reqline.getUri());
    // </customized>

    // room for "GET /index.html HTTP/1.1"
    final int len = method.length() + 1 + uri.length() + 1 + estimateProtocolVersionLen(reqline.getProtocolVersion());
    buffer.ensureCapacity(len);

    buffer.append(method);
    buffer.append(' ');
    buffer.append(uri);
    buffer.append(' ');
    appendProtocolVersion(buffer, reqline.getProtocolVersion());
  }
}
