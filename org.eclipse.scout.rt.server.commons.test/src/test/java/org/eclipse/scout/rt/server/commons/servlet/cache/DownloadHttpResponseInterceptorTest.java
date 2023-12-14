/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.DownloadResponseHelper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DownloadHttpResponseInterceptorTest {
  @Test
  public void testContentDispositionResponseHeader() {
    testIntercept("simple.pdf", "attachment; filename=\""
        + "simple.pdf"
        + "\"; filename*=utf-8''"
        + "simple.pdf"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderNullFillsDefaultValue() {
    testIntercept(null, "attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderEmptyFillsDefaultValue() {
    testIntercept("", "attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderContainsOnlyNonQuotesFillsDefaultValue() {
    testIntercept("\"", "attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "%22"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderDoubleQuotes() {
    testIntercept("x\"x", "attachment; filename=\""
        + "xx"
        + "\"; filename*=utf-8''"
        + "x%22x"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderNewlinesTabsSpaces() {
    testIntercept("x\r\f\n\tx\b x", "attachment; filename=\""
        + "xx x"
        + "\"; filename*=utf-8''"
        + "xx%20x"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace() {
    testIntercept("   x   ", "attachment; filename=\""
        + "   x   "
        + "\"; filename*=utf-8''"
        + "x"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace2() {
    testIntercept(" ", "attachment; filename=\""
        + " "
        + "\"; filename*=utf-8''"
        + ""
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderUmlauts() {
    testIntercept("TestäüöÄÜÖ.pdf", "attachment; filename=\""
        + "TestäüöÄÜÖ.pdf"
        + "\"; filename*=utf-8''"
        + "Test%C3%A4%C3%BC%C3%B6%C3%84%C3%9C%C3%96.pdf"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderEuroSignIsRemovedInIso88591() {
    testIntercept("Test 5€.pdf", "attachment; filename=\""
        + "Test 5.pdf"
        + "\"; filename*=utf-8''"
        + "Test%205%E2%82%AC.pdf"
        + "");
  }

  @Test
  public void testContentDispositionResponseHeaderControlCharacters() {
    String s = "test";
    for (int i = 0x00; i <= 0x1F; i++) {
      s = s + new String(new byte[]{(byte) i}, StandardCharsets.ISO_8859_1);
    }
    s = s + ".pdf";
    Assert.assertEquals("test.pdf".length() + 32, s.length());

    testIntercept(s, "attachment; filename=\""
        + "test.pdf"
        + "\"; filename*=utf-8''"
        + "test.pdf"
        + "");
  }

  protected void testIntercept(String filename, String expectedContentDispositionHeader) {
    DownloadHttpResponseInterceptor downloadHttpResponseInterceptor = new DownloadHttpResponseInterceptor(filename);
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    downloadHttpResponseInterceptor.intercept(null, responseMock);

    Mockito.verify(responseMock, times(1)).setHeader(DownloadResponseHelper.HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
    Mockito.verify(responseMock, times(1)).setHeader(DownloadResponseHelper.HEADER_CONTENT_DISPOSITION, expectedContentDispositionHeader);
  }
}
