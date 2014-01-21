/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext.browser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.scout.rt.ui.rap.ext.browser.BrowserExtension.HyperlinkProcessor;
import org.eclipse.scout.rt.ui.rap.html.HtmlAdapter;
import org.junit.Test;

public class BrowserExtensionHyperlinkProcessorTest {

  private HyperlinkProcessor createProcessor() {
    HyperlinkProcessor processor = new HyperlinkProcessor();
    processor.setHyperlinkFunctionName(BrowserExtension.HYPERLINK_FUNCTION_NAME);
    processor.setHyperlinkFunctionReturnType(BrowserExtension.HYPERLINK_FUNCTION_RETURN_TYPE);
    processor.setGeneratedMappings(new HashMap<String, String>());
    return processor;
  }

  private void testRewrite(String html, List<String> links) {
    HtmlAdapter htmlAdapter = new HtmlAdapter(null);
    HyperlinkProcessor processor = createProcessor();
    processor.setConvertExternalUrlsEnabled(true);

    String origHtml = html;
    for (int i = 0; i < links.size(); i++) {
      String link = links.get(i);
      origHtml = origHtml.replace("{" + i + "}", link);
    }
    String expectedHtml = html;
    for (int i = 0; i < links.size(); i++) {
      expectedHtml = expectedHtml.replace("{" + i + "}", "javascript:" + processor.getHyperlinkFunctionReturnType() + " " + processor.getHyperlinkFunctionName() + "('" + i + "');");
    }

    String result = htmlAdapter.processHyperlinks(origHtml, processor);
    assertEquals(expectedHtml, result);
    for (int i = 0; i < links.size(); i++) {
      processor.getGeneratedMappings().get("" + i).equals(links.get(i));
    }
  }

  private void testRewrite(String html, String link) {
    List<String> links = new ArrayList<String>();
    links.add(link);
    testRewrite(html, links);
  }

  @Test
  public void testLocal() {
    String link = "http://local/xy";
    String html = "<html><a href=\"{0}\">link</a></html>";

    testRewrite(html, link);
  }

  @Test
  public void testExternal() {
    String link = "https://www.google.ch/#q=test";
    String html = "<html><a href=\"{0}\">link</a></html>";

    testRewrite(html, link);
  }

  @Test
  public void testOnlyLocal() {
    HtmlAdapter htmlAdapter = new HtmlAdapter(null);
    HyperlinkProcessor processor = createProcessor();
    processor.setConvertExternalUrlsEnabled(false);

    String link = "http://local/xy";
    String html = "<html><a href=\"{0}\">link</a><a href=\"https://www.google.ch/#q=test\">link2</a></html>";
    String origHtml = html.replace("{0}", link);
    String expectedHtml = html.replace("{0}", "javascript:" + processor.getHyperlinkFunctionReturnType() + " " + processor.getHyperlinkFunctionName() + "('" + 0 + "');");

    String result = htmlAdapter.processHyperlinks(origHtml, processor);
    assertEquals(expectedHtml, result);
    processor.getGeneratedMappings().get("" + 0).equals(link);
  }

  @Test
  public void testOnlyLocalWithDefaultTarget() {
    HtmlAdapter htmlAdapter = new HtmlAdapter(null);
    HyperlinkProcessor processor = createProcessor();
    processor.setConvertExternalUrlsEnabled(false);
    processor.setDefaultTarget("_top");

    String link = "http://local/xy";
    String html = "<html><a href=\"{0}\">link</a><a href=\"https://www.google.ch/#q=test\">link2</a></html>";
    String expectedHtml = "<html><a href=\"{0}\">link</a><a href=\"https://www.google.ch/#q=test\" target=\"_top\">link2</a></html>";
    String origHtml = html.replace("{0}", link);
    expectedHtml = expectedHtml.replace("{0}", "javascript:" + processor.getHyperlinkFunctionReturnType() + " " + processor.getHyperlinkFunctionName() + "('" + 0 + "');");

    String result = htmlAdapter.processHyperlinks(origHtml, processor);
    assertEquals(expectedHtml, result);
    processor.getGeneratedMappings().get("" + 0).equals(link);
  }

  @Test
  public void testOnlyLocalWithDefaultTargetNoChange() {
    HtmlAdapter htmlAdapter = new HtmlAdapter(null);
    HyperlinkProcessor processor = createProcessor();
    processor.setConvertExternalUrlsEnabled(false);
    processor.setDefaultTarget("_top");

    String link = "http://local/xy";
    String html = "<html><a href=\"{0}\">link</a><a href=\"https://www.google.ch/#q=test\">link2</a><a href=\"https://www.google.ch/#q=test\" target=\"_blank\">link3</a></html>";
    String expectedHtml = "<html><a href=\"{0}\">link</a><a href=\"https://www.google.ch/#q=test\" target=\"_top\">link2</a><a href=\"https://www.google.ch/#q=test\" target=\"_blank\">link3</a></html>";
    String origHtml = html.replace("{0}", link);
    expectedHtml = expectedHtml.replace("{0}", "javascript:" + processor.getHyperlinkFunctionReturnType() + " " + processor.getHyperlinkFunctionName() + "('" + 0 + "');");

    String result = htmlAdapter.processHyperlinks(origHtml, processor);
    assertEquals(expectedHtml, result);
    processor.getGeneratedMappings().get("" + 0).equals(link);
  }

  @Test
  public void testImage() {
    String link = "https://www.google.ch/#q=test";
    String html = "<html><a href=\"{0}\"><img src=\"button.png\" width=\"50\" height=\"50\" border=\"0\" alt=\"Button\"></a></html>";

    testRewrite(html, link);
  }

  public void testSvg() {
    String link = "https://www.google.ch/#q=test";
    String html = "<a xlink:actuate=\"onRequest\" xlink:type=\"simple\" xlink:show=\"replace\" xlink:href=\"{0}\"><rect width=\"77.349\" x=\"473.651\" height=\"64.217\" id=\"b65\" y=\"377.103\" style=\"fill: rgb(238, 238, 238);&#10;stroke: rgb(192, 192, 192);&#10;stroke-miterlimit: 10;&#10;\"/></a>";

    testRewrite(html, link);
  }

  @Test
  public void testArea() {
    List<String> links = new ArrayList<String>();
    links.add("https://www.google.ch/#q=test");
    links.add("http://local/xy");
    String html = ""
        + "<html>"
        + "<img src=\"map.png\" width=\"300\" height=\"200\" usemap=\"#Map\">"
        + "<map name=\"Map\">"
        + "<area shape=\"rect\" coords=\"10,10,40,40\""
        + "    href=\"{0}\" alt=\"Google\">"
        + "<area shape=\"rect\" coords=\"50,50,40,40\""
        + "      href=\"{1}\" alt=\"Google\">"
        + "</map>"
        + "</html>";

    testRewrite(html, links);
  }

  @Test
  public void testAreaXhtml() {
    List<String> links = new ArrayList<String>();
    links.add("https://www.google.ch/#q=test");
    links.add("http://local/xy");
    String html = ""
        + "<html>"
        + "<img src=\"map.png\" width=\"300\" height=\"200\" usemap=\"#Map\"/>"
        + "<map name=\"Map\">"
        + "<area shape=\"rect\" coords=\"10,10,40,40\""
        + "    href=\"{0}\" alt=\"Google\"/>"
        + "<area shape=\"rect\" coords=\"50,50,40,40\""
        + "      href=\"{1}\" alt=\"Google\"/>"
        + "</map>"
        + "</html>";

    testRewrite(html, links);
  }

  @Test
  public void testMultiple() {
    List<String> links = new ArrayList<String>();
    links.add("https://www.google.ch/#q=test");
    links.add("http://local/xy");
    links.add("http://local/abc?abc=1234");
    links.add("http://www.google.ch/#q=area");
    String html = ""
        + "<html>"
        + "<ul>"
        + "<li><a "
        + "   href=\"{0}\">link</a>"
        + "</li>"
        + "<li>"
        + "<a href=\"{1}\">link2</a>"
        + "</li>"
        + "</b>"
        + "<img src=\"map.png\" width=\"300\" height=\"200\"  usemap=\"#Map\">"
        + "<map name=\"Map\">"
        + "<area shape=\"rect\" coords=\"10,10,40,40\""
        + "    href=\"{2}\" alt=\"Google\">"
        + "<area shape=\"rect\" coords=\"50,50,40,40\""
        + "    href=\"{3}\" alt=\"Google\">"
        + "</map>"
        + "</html>";

    testRewrite(html, links);
  }

}
