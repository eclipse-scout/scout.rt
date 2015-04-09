/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.binds;

import static org.eclipse.scout.commons.html.HTML.bold;
import static org.eclipse.scout.commons.html.HTML.cell;
import static org.eclipse.scout.commons.html.HTML.div;
import static org.eclipse.scout.commons.html.HTML.link;
import static org.eclipse.scout.commons.html.HTML.row;
import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.html.HTML;
import org.eclipse.scout.commons.html.HtmlBinds;
import org.eclipse.scout.commons.html.IHtmlContent;
import org.eclipse.scout.commons.html.IHtmlElement;
import org.eclipse.scout.commons.html.IHtmlTable;
import org.junit.Test;
import org.w3c.dom.html.HTMLElement;

/**
 * Tests for {@link HtmlBinds}
 */
public class HtmlBindsTest {
  private static final String BIND_TEXT = "Test Last Name&";
  private static final String ENCODED_BIND_TEXT = "Test Last Name&amp;";

  private static final String TEST_URL = "http://SCOUTBLABLA.com";

  /**
   * Tests that the {@link HTMLElement} only contains bind variables, not actual values.
   */
  @Test
  public void testBinds() {
    HtmlBinds binds = new HtmlBinds();
    IHtmlElement boldBind = HTML.bold(binds.put(BIND_TEXT));
    assertEquals("<b>:b__0</b>", boldBind.toString());
  }

  /**
   * Tests that the encoded values are contained within the tags, when binds are applied.
   */
  @Test
  public void testHtmlTags() {
    HtmlBinds binds = new HtmlBinds();
    assertEncodedText("h1", binds.applyBindParameters(HTML.h1(binds.put(BIND_TEXT))));
    assertEncodedText("h2", binds.applyBindParameters(HTML.h2(binds.put(BIND_TEXT))));
    assertEncodedText("h3", binds.applyBindParameters(HTML.h3(binds.put(BIND_TEXT))));
    assertEncodedText("h4", binds.applyBindParameters(HTML.h4(binds.put(BIND_TEXT))));
    assertEncodedText("h5", binds.applyBindParameters(HTML.h5(binds.put(BIND_TEXT))));
    assertEncodedText("h6", binds.applyBindParameters(HTML.h6(binds.put(BIND_TEXT))));
    assertEncodedText("b", binds.applyBindParameters(bold(binds.put(BIND_TEXT))));
    assertEncodedText("td", binds.applyBindParameters(cell(binds.put(BIND_TEXT))));
    assertEncodedText("div", binds.applyBindParameters(div(binds.put(BIND_TEXT))));
    assertEncodedText("p", binds.applyBindParameters(HTML.p(binds.put(BIND_TEXT))));
    assertEncodedText("span", binds.applyBindParameters(HTML.span(binds.put(BIND_TEXT))));
  }

  @Test
  public void testHtmlNoBinds() {
    assertEncodedText("h1", HTML.h1(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h2", HTML.h2(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h3", HTML.h3(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h4", HTML.h4(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h5", HTML.h5(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h6", HTML.h6(BIND_TEXT).toEncodedHtml());
    assertEncodedText("b", bold(BIND_TEXT).toEncodedHtml());
    assertEncodedText("td", cell(BIND_TEXT).toEncodedHtml());
    assertEncodedText("div", div(BIND_TEXT).toEncodedHtml());
    assertEncodedText("p", HTML.p(BIND_TEXT).toEncodedHtml());
    assertEncodedText("span", HTML.span(BIND_TEXT).toEncodedHtml());
  }

  /**
   * Tests a link with URL and encoded text.
   */
  @Test
  public void testLink() {
    HtmlBinds binds = new HtmlBinds();
    IHtmlElement link = HTML.link(binds.put(TEST_URL), binds.put(BIND_TEXT));
    String html = binds.applyBindParameters(link);
    assertEquals("<a href=\"" + TEST_URL + "\">" + encode(BIND_TEXT) + "</a>", html);
  }

  /**
   * Tests a link with URL and encoded text.
   */
  @Test
  public void testLinkNoBinds() {
    String html = HTML.link(TEST_URL, BIND_TEXT).toEncodedHtml();
    assertEquals("<a href=\"" + TEST_URL + "\">" + encode(BIND_TEXT) + "</a>", html);
  }

  /**
   * Tests an image encoded source.
   */
  @Test
  public void testImageNoBinds() {
    String html = HTML.img("logo.png").toEncodedHtml();
    assertEquals("<img src=\"logo.png\">", html);
  }

  /**
   * Tests an image encoded source.
   */
  @Test
  public void testImage() {
    HtmlBinds binds = new HtmlBinds();
    String html = binds.applyBindParameters(HTML.img(binds.put("logo.png")));
    assertEquals("<img src=\"logo.png\">", html);
  }

  /**
   * Tests for {@link HTML#br()}
   */
  public void testBr() {
    HtmlBinds binds = new HtmlBinds();
    IHtmlElement br = HTML.br();
    assertEquals("<br>", binds.applyBindParameters(br));
    assertEquals("<br>", br.toEncodedHtml());

  }

  /**
   * Test for {@link IHtmlElement#appLink(CharSequence)}
   */
  @Test
  public void testAppLink() {
    HtmlBinds binds = new HtmlBinds();
    final IHtmlElement html = HTML.appLink("domain=123&text=456", binds.put("Link Text"));
    assertEquals("<span class=\"app-link\" data-ref=\"domain=123&text=456\">Link Text</span>", binds.applyBindParameters(html));
  }

  /**
   * Test for {@link IHtmlElement#appLink(CharSequence)}
   */
  @Test
  public void testAppLinkNoBinds() {
    final IHtmlElement html = HTML.appLink("domain=123&text=456", "Link Text&");
    assertEquals("<span class=\"app-link\" data-ref=\"domain=123&text=456\">Link Text&amp;</span>", html.toEncodedHtml());
  }

  @Test
  public void testTable() {
    HtmlBinds binds = new HtmlBinds();
    String html = binds.applyBindParameters(HTML.table(row(cell(binds.put(BIND_TEXT)))));
    assertEquals("<table><tr><td>" + encode(BIND_TEXT) + "</td></tr></table>", html);
  }

  @Test
  public void testTableNoBinds() {
    String html = HTML.table(row(cell(BIND_TEXT))).toEncodedHtml();
    assertEquals("<table><tr><td>" + encode(BIND_TEXT) + "</td></tr></table>", html);
  }

  @Test
  public void testTableAttributes() {
    HtmlBinds binds = new HtmlBinds();
    final IHtmlTable table = HTML.table(
        row(
        cell(binds.put(BIND_TEXT)))
        ).cellspacing(1).cellpadding(2);

    final String htmlString = binds.applyBindParameters(table);

    assertEquals("<table cellspacing=\"1\" cellpadding=\"2\"><tr><td>" + encode(BIND_TEXT) + "</td></tr></table>", htmlString);
  }

  @Test
  public void testTableAttributesNoBinds() {
    final IHtmlTable table = HTML.table(
        row(
        cell(BIND_TEXT))
        ).cellspacing(1).cellpadding(2);

    assertEquals("<table cellspacing=\"1\" cellpadding=\"2\"><tr><td>" + encode(BIND_TEXT) + "</td></tr></table>", table.toEncodedHtml());
  }

  @Test
  public void testLinkWithBold() {
    HtmlBinds binds = new HtmlBinds();
    final IHtmlElement html = HTML.bold(
        binds.put(BIND_TEXT),
        link(binds.put(TEST_URL), binds.put(BIND_TEXT))
        );
    assertEquals("<b>Test Last Name&amp;<a href=\"http://SCOUTBLABLA.com\">Test Last Name&amp;</a></b>", binds.applyBindParameters(html));
  }

  @Test
  public void testLinkWithBoldNoBinds() throws Exception {
    final IHtmlElement html = HTML.bold(
        BIND_TEXT,
        link(TEST_URL, BIND_TEXT)
        );
    assertEquals("<b>Test Last Name&amp;<a href=\"http://SCOUTBLABLA.com\">Test Last Name&amp;</a></b>", html.toEncodedHtml());

  }

  private String encode(String text) {
    return StringUtility.htmlEncode(text, false);
  }

  private void assertEncodedText(String tagName, String actualText) {
    assertEquals("<" + tagName + ">" + ENCODED_BIND_TEXT + "</" + tagName + ">", actualText);
  }

  @Test
  public void testFragment() {
    HtmlBinds binds = new HtmlBinds();
    final IHtmlContent fragment = HTML.fragment(HTML.div(binds.put(BIND_TEXT)), HTML.div(binds.put(BIND_TEXT)));
    assertEquals("<div>" + ENCODED_BIND_TEXT + "</div>" + "<div>" + ENCODED_BIND_TEXT + "</div>", binds.applyBindParameters(fragment));
  }
}
