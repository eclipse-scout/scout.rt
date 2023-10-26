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

import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
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
  public void formatRequestLine(final CharArrayBuffer buffer, final RequestLine reqline) {
    Args.notNull(buffer, "Char array buffer");
    Args.notNull(reqline, "Request line");
    buffer.append(reqline.getMethod());
    buffer.append(' ');
    // <customized>
    String uri = m_requestUriEncoder.encodeRequestUri(reqline.getUri());
    buffer.append(uri);
    // </customized>
    buffer.append(' ');
    buffer.append(reqline.getProtocolVersion().format()); // super.formatProtocolVersion not visible..
  }
}
