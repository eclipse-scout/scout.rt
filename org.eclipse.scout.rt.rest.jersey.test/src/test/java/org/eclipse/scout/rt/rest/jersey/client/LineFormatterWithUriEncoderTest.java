/*******************************************************************************
 * Copyright (c) 2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
