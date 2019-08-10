/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link DownloadResponseHelper}
 */
public class DownloadResponseHelperTest {

  @Test
  public void testContentDispositionResponseHeader() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("simple.pdf");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "simple.pdf"
        + "\"; filename*=utf-8''"
        + "simple.pdf"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderNullFillsDefaultValue() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders(null);
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderEmptyFillsDefaultValue() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderContainsOnlyNonQuotesFillsDefaultValue() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("\"");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "%22"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderDoubleQuotes() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("x\"x");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "xx"
        + "\"; filename*=utf-8''"
        + "x%22x"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderNewlinesTabsSpaces() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("x\r\f\n\tx\b x");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "xx x"
        + "\"; filename*=utf-8''"
        + "xx%20x"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("   x   ");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "   x   "
        + "\"; filename*=utf-8''"
        + "x"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace2() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders(" ");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + " "
        + "\"; filename*=utf-8''"
        + ""
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderUmlauts() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("TestäüöÄÜÖ.pdf");
    Map<String, String> expected = getExpectedHeaders("attachment; filename=\""
        + "TestäüöÄÜÖ.pdf"
        + "\"; filename*=utf-8''"
        + "Test%C3%A4%C3%BC%C3%B6%C3%84%C3%9C%C3%96.pdf"
        + "");
    String actualContentDispositionHeader = actual.get(DownloadResponseHelper.HEADER_CONTENT_DISPOSITION);
    assertThat(actual, is(expected));
    Assert.assertEquals(new String(actualContentDispositionHeader.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1), actualContentDispositionHeader);
  }

  @Test
  public void testContentDispositionResponseHeaderEuroSignIsRemovedInIso88591() {
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders("Test 5€.pdf");
    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "Test 5.pdf"
        + "\"; filename*=utf-8''"
        + "Test%205%E2%82%AC.pdf"
        + "")));
  }

  @Test
  public void testContentDispositionResponseHeaderControlCharacters() throws UnsupportedEncodingException {
    String s = "test";
    for (int i = 0x00; i <= 0x1F; i++) {
      s = s + new String(new byte[]{(byte) i}, StandardCharsets.ISO_8859_1);
    }
    s = s + ".pdf";
    Assert.assertEquals("test.pdf".length() + 32, s.length());
    Map<String, String> actual = new DownloadResponseHelper().getDownloadHeaders(s);

    assertThat(actual, is(getExpectedHeaders("attachment; filename=\""
        + "test.pdf"
        + "\"; filename*=utf-8''"
        + "test.pdf"
        + "")));
  }

  protected Map<String, String> getExpectedHeaders(String expectedContentDispositionHeader) {
    Map<String, String> expectedHeaders = new HashMap<>();
    expectedHeaders.put(DownloadResponseHelper.HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
    expectedHeaders.put(DownloadResponseHelper.HEADER_CONTENT_DISPOSITION, expectedContentDispositionHeader);
    return expectedHeaders;
  }
}
