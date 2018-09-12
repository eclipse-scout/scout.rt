/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class DownloadHttpResponseInterceptorTest {
  @Test
  public void testContentDispositionResponseHeader() {
    String actual = new DownloadHttpResponseInterceptor("simple.pdf").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "simple.pdf"
        + "\"; filename*=utf-8''"
        + "simple.pdf"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderNullFillsDefaultValue() {
    String actual = new DownloadHttpResponseInterceptor(null).getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderEmptyFillsDefaultValue() {
    String actual = new DownloadHttpResponseInterceptor("").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderDoubleQuotes() {
    String actual = new DownloadHttpResponseInterceptor("x\"x").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "xx"
        + "\"; filename*=utf-8''"
        + "x%22x"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderNewlinesTabsSpaces() {
    String actual = new DownloadHttpResponseInterceptor("x\r\f\n\tx\b x").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "xx x"
        + "\"; filename*=utf-8''"
        + "xx%20x"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace() {
    String actual = new DownloadHttpResponseInterceptor("   x   ").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "   x   "
        + "\"; filename*=utf-8''"
        + "x"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace2() {
    String actual = new DownloadHttpResponseInterceptor(" ").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + " "
        + "\"; filename*=utf-8''"
        + ""
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderUmlauts() {
    String actual = new DownloadHttpResponseInterceptor("TestäüöÄÜÖ.pdf").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "TestäüöÄÜÖ.pdf"
        + "\"; filename*=utf-8''"
        + "Test%C3%A4%C3%BC%C3%B6%C3%84%C3%9C%C3%96.pdf"
        + "", actual);
    Assert.assertEquals(new String(actual.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1), actual);
  }

  @Test
  public void testContentDispositionResponseHeaderEuroSignIsRemovedInIso88591() {
    String actual = new DownloadHttpResponseInterceptor("Test 5€.pdf").getContentDispositionHeaderValue();
    Assert.assertEquals("attachment; filename=\""
        + "Test 5.pdf"
        + "\"; filename*=utf-8''"
        + "Test%205%E2%82%AC.pdf"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderControlCharacters() throws UnsupportedEncodingException {
    String s = "test";
    for (int i = 0x00; i <= 0x1F; i++) {
      s = s + new String(new byte[]{(byte) i}, StandardCharsets.ISO_8859_1);
    }
    s = s + ".pdf";
    Assert.assertEquals("test.pdf".length() + 32, s.length());
    String actual = new DownloadHttpResponseInterceptor(s).getContentDispositionHeaderValue();

    Assert.assertEquals("attachment; filename=\""
        + "test.pdf"
        + "\"; filename*=utf-8''"
        + "test.pdf"
        + "", actual);
  }
}
