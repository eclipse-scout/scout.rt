package org.eclipse.scout.rt.ui.html.res.loader;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HtmlDocumentParserTest {

  @BeforeClass
  public static void beforeClass() {
    BEANS.getBeanManager().registerClass(TestWebContentService.class);
    BEANS.getBeanManager().registerClass(TestTextProviderService.class);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.getBeanManager().unregisterClass(TestWebContentService.class);
    BEANS.getBeanManager().unregisterClass(TestTextProviderService.class);
  }

  private HtmlDocumentParser m_parser;

  @Before
  public void before() {
    HtmlDocumentParserParameters params = new HtmlDocumentParserParameters("html/path", "testTheme", false, false, "base-path");
    m_parser = new HtmlDocumentParser(params);
  }

  private byte[] read(String filename) throws IOException {
    return IOUtility.readFromUrl(HtmlDocumentParserTest.class.getResource(filename));
  }

  private void testParser(String inputFilename, String expectedResultFilename) throws IOException {
    byte[] input = read(inputFilename);
    String expectedResult = new String(read(expectedResultFilename), StandardCharsets.UTF_8)
        .replaceAll("\\$PROTECT-LINE-ENDING\\$", "");
    String result = new String(m_parser.parseDocument(input), StandardCharsets.UTF_8);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testHtmlDocumentParser_01() throws IOException {
    IBean<TestApplicationVersionProperty> bean = BEANS.getBeanManager().registerBean(new BeanMetaData(TestApplicationVersionProperty.class).withReplace(true));
    try {
      testParser("test01_input.html", "test01_output.html");
    }
    finally {
      BEANS.getBeanManager().unregisterBean(bean);
    }
  }

  @Test
  public void testHtmlDocumentParser_02() throws IOException {
    testParser("test02_input.html", "test02_output.html");
  }

  @Order(Double.MIN_VALUE)
  public static class TestWebContentService implements IWebContentService {

    @Override
    public URL getScriptSource(String path) {
      throw new AssertionError("This method should never be called during this test");
    }

    @Override
    public URL getWebContentResource(String path) {
      return HtmlDocumentParserTest.class.getResource("include.html");
    }
  }

  /**
   * TextProviderService that returns a stable result (<code>--key--</code>), independent from locale and other text
   * provider services.
   */
  @Order(Double.MIN_VALUE)
  public static class TestTextProviderService implements ITextProviderService {

    @Override
    public String getText(Locale locale, String key, String... messageArguments) {
      return "--" + key + "--";
    }

    @Override
    public Map<String, String> getTextMap(Locale locale) {
      return new HashMap<>();
    }
  }

  @IgnoreBean
  @Replace
  public static class TestApplicationVersionProperty extends ApplicationVersionProperty {

    @Override
    public synchronized String getValue(String namespace) {
      return "1.2.3.unit_test";
    }
  }
}
