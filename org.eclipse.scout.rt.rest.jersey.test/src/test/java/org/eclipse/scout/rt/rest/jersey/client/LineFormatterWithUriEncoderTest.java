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

import static org.junit.Assert.assertEquals;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.util.CharArrayBuffer;
import org.eclipse.scout.rt.rest.IRestHttpRequestUriEncoder;
import org.junit.Test;

public class LineFormatterWithUriEncoderTest {

  @Test
  public void testDoFormatRequestLine() {
    BasicRequestLine reqLine = new BasicRequestLine("GET", "/a,b", new ProtocolVersion("HTTP", 1, 1));
    LineFormatterWithUriEncoder lineFormatter = new LineFormatterWithUriEncoder(new MockUriEncoder());

    CharArrayBuffer buffer = new CharArrayBuffer(20);
    buffer = lineFormatter.formatRequestLine(buffer, reqLine);

    assertEquals("GET /a%2Cb HTTP/1.1", buffer.toString());
  }

  class MockUriEncoder implements IRestHttpRequestUriEncoder {

    @Override
    public String encodeRequestUri(String uri) {
      return uri.replace(",", "%2C");
    }
  }
}
