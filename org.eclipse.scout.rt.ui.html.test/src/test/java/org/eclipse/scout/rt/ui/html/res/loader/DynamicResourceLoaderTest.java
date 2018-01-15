package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResponseHeaderContributor;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DynamicResourceLoaderTest {

  @Test
  public void testContentDispositionResponseHeader() {
    String actual = getContentDispositionHeaderValue("simple.pdf");
    Assert.assertEquals("attachment; filename=\""
        + "simple.pdf"
        + "\"; filename*=utf-8''"
        + "simple.pdf"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderNullFillsDefaultValue() {
    String actual = getContentDispositionHeaderValue(null);
    Assert.assertEquals("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderEmptyFillsDefaultValue() {
    String actual = getContentDispositionHeaderValue("");
    Assert.assertEquals("attachment; filename=\""
        + "Download"
        + "\"; filename*=utf-8''"
        + "Download"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderDoubleQuotes() {
    String actual = getContentDispositionHeaderValue("x\"x");
    Assert.assertEquals("attachment; filename=\""
        + "xx"
        + "\"; filename*=utf-8''"
        + "x%22x"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderNewlinesTabsSpaces() {
    String actual = getContentDispositionHeaderValue("x\r\f\n\tx\b x");
    Assert.assertEquals("attachment; filename=\""
        + "xx x"
        + "\"; filename*=utf-8''"
        + "xx%20x"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace() {
    String actual = getContentDispositionHeaderValue("   x   ");
    Assert.assertEquals("attachment; filename=\""
        + "   x   "
        + "\"; filename*=utf-8''"
        + "x"
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderUrlEncodeTrimsWhitespace2() {
    String actual = getContentDispositionHeaderValue(" ");
    Assert.assertEquals("attachment; filename=\""
        + " "
        + "\"; filename*=utf-8''"
        + ""
        + "", actual);
  }

  @Test
  public void testContentDispositionResponseHeaderUmlauts() {
    String actual = getContentDispositionHeaderValue("TestäüöÄÜÖ.pdf");
    Assert.assertEquals("attachment; filename=\""
        + "TestäüöÄÜÖ.pdf"
        + "\"; filename*=utf-8''"
        + "Test%C3%A4%C3%BC%C3%B6%C3%84%C3%9C%C3%96.pdf"
        + "", actual);
    Assert.assertEquals(new String(actual.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1), actual);
  }

  @Test
  public void testContentDispositionResponseHeaderEuroSignIsRemovedInIso88591() {
    String actual = getContentDispositionHeaderValue("Test 5€.pdf");
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
    String actual = getContentDispositionHeaderValue(s);

    Assert.assertEquals("attachment; filename=\""
        + "test.pdf"
        + "\"; filename*=utf-8''"
        + "test.pdf"
        + "", actual);
  }

  /**
   * Helper method for testing {@link DynamicResourceLoader#addResponseHeaderForDownload}
   */
  private static String getContentDispositionHeaderValue(String filename) {
    DynamicResourceLoader resourceLoader = new DynamicResourceLoader(null);
    HttpCacheObject cacheMock = Mockito.mock(HttpCacheObject.class);
    final Holder<String> resultHolder = new Holder<String>();
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        HttpResponseHeaderContributor interceptor = invocation.getArgumentAt(0, HttpResponseHeaderContributor.class);
        resultHolder.setValue(interceptor.getValue());
        return null;
      }
    }).when(cacheMock).addHttpResponseInterceptor(Mockito.any(IHttpResponseInterceptor.class));
    resourceLoader.addResponseHeaderForDownload(cacheMock, filename);
    return resultHolder.getValue();
  }
}
