/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.text.ITextProviderService;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.shared.ui.webresource.AbstractWebResourceResolver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HtmlDocumentParserTest {

  private static final String SCRIPT1_NAME = "script1.js";
  private static final String SCRIPT2_NAME = "script2.js";
  private static final String SCRIPT3_NAME = "script3.js";
  private static final String SCRIPT4_NAME = "style.css";
  private static final String SCRIPT5_NAME = "vendors~style.css";
  private static final String SCRIPT6_NAME = "style.css";

  @BeforeClass
  public static void beforeClass() {
    BEANS.getBeanManager().registerClass(TestTextProviderService.class);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.getBeanManager().unregisterClass(TestTextProviderService.class);
  }

  protected HtmlDocumentParser newParser(String theme) {
    HtmlDocumentParserParameters params = new HtmlDocumentParserParameters("html/path", theme, false, false, "base-path");
    return new HtmlDocumentParser(params) {
      @Override
      protected URL resolveInclude(String includeName) {
        return HtmlDocumentParserTest.class.getResource("include.html");
      }

      @Override
      protected Stream<String> getAssetsForEntryPoint(String entryPoint) {
        if ("style".equals(entryPoint)) {
          return Stream.of(SCRIPT5_NAME, SCRIPT6_NAME);
        }
        return Stream.of(SCRIPT4_NAME, SCRIPT1_NAME, SCRIPT2_NAME, SCRIPT3_NAME);
      }

      @Override
      protected String createExternalPath(String internalPath) {
        return AbstractWebResourceResolver.getThemePath(internalPath, m_params.getTheme());
      }
    };
  }

  private byte[] read(String filename) throws IOException {
    return IOUtility.readFromUrl(HtmlDocumentParserTest.class.getResource(filename));
  }

  private void testParser(HtmlDocumentParser parser, String inputFilename, String expectedResultFilename) throws IOException {
    byte[] input = read(inputFilename);
    String expectedResult = new String(read(expectedResultFilename), StandardCharsets.UTF_8)
        .replaceAll("\\$PROTECT-LINE-ENDING\\$", "");
    String result = new String(parser.parseDocument(input), StandardCharsets.UTF_8);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testHtmlDocumentParser_01() throws IOException {
    HtmlDocumentParser parser = newParser(null);
    IBean<TestApplicationVersionProperty> bean = BEANS.getBeanManager().registerBean(new BeanMetaData(TestApplicationVersionProperty.class).withReplace(true));
    try {
      testParser(parser, "test01_input.html", "test01_output.html");
    }
    finally {
      BEANS.getBeanManager().unregisterBean(bean);
    }
  }

  @Test
  public void testHtmlDocumentParser_02() throws IOException {
    HtmlDocumentParser parser = newParser(null);
    testParser(parser, "test02_input.html", "test02_output.html");
  }

  @Test
  public void testHtmlDocumentParser_03() throws IOException {
    HtmlDocumentParser parser = newParser(null);
    testParser(parser, "test03_input.html", "test03_output.html");
  }

  @Test
  public void testHtmlDocumentParser_04() throws IOException {
    HtmlDocumentParser parser = newParser(null);
    testParser(parser, "test04_input.html", "test04_output.html");
  }

  @Test
  public void testHtmlDocumentParser_05() throws IOException {
    HtmlDocumentParser parser = newParser("mytheme");
    testParser(parser, "test05_input.html", "test05_output.html");
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
