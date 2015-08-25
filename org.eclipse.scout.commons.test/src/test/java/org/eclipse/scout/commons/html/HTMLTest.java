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
package org.eclipse.scout.commons.html;

import static org.eclipse.scout.commons.html.HTML.bold;
import static org.eclipse.scout.commons.html.HTML.cell;
import static org.eclipse.scout.commons.html.HTML.div;
import static org.eclipse.scout.commons.html.HTML.italic;
import static org.eclipse.scout.commons.html.HTML.link;
import static org.eclipse.scout.commons.html.HTML.row;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.junit.Test;

/**
 * Tests for {@link HtmlBinds}
 */
public class HTMLTest {
  private static final String BIND_TEXT = "Test Last Name&";
  private static final String ENCODED_BIND_TEXT = "Test Last Name&amp;";
  private static final String TEST_URL = "http://SCOUTBLABLA.com";

  private static final String sampleCSS = "p {"
      + "    text-align: center;"
      + "    color: red;"
      + "}";

  @Test
  public void testHtmlNoBinds() {
    assertEncodedText("h1", HTML.h1(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h2", HTML.h2(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h3", HTML.h3(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h4", HTML.h4(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h5", HTML.h5(BIND_TEXT).toEncodedHtml());
    assertEncodedText("h6", HTML.h6(BIND_TEXT).toEncodedHtml());
    assertEncodedText("b", bold(BIND_TEXT).toEncodedHtml());
    assertEncodedText("i", italic(BIND_TEXT).toEncodedHtml());
    assertEncodedText("td", cell(BIND_TEXT).toEncodedHtml());
    assertEncodedText("div", div(BIND_TEXT).toEncodedHtml());
    assertEncodedText("p", HTML.p(BIND_TEXT).toEncodedHtml());
    assertEncodedText("span", HTML.span(BIND_TEXT).toEncodedHtml());
    assertEncodedText("li", HTML.li(BIND_TEXT).toEncodedHtml());
    assertEncodedText("head", HTML.head(BIND_TEXT).toEncodedHtml());
    assertEncodedText("body", HTML.body(BIND_TEXT).toEncodedHtml());
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
   * Tests for {@link HTML#br()}
   */
  @Test
  public void testBr() {
    IHtmlElement br = HTML.br();
    assertEquals("<br>", br.toEncodedHtml());
  }

  @Test
  public void testAddAttribute() {
    IHtmlElement span = HTML.span("text").addAttribute("name", "value");
    assertEquals("<span name=\"value\">text</span>", span.toEncodedHtml());
  }

  /**
   * Test for {@link IHtmlElement#appLink(CharSequence)}
   */
  @Test
  public void testAppLinkNoBinds() {
    final IHtmlElement html = HTML.appLink("domain=123&text=456", "Link Text&");
    assertEquals("<span class=\"app-link\" data-ref=\"domain=123&amp;text=456\">Link Text&amp;</span>", html.toEncodedHtml());
  }

  @Test
  public void testBoldAppLink() {
    final IHtmlElement html = HTML.bold("asdf", HTML.appLink("domain=123&text=456", "Link Text&"));
    assertEquals("<b>asdf<span class=\"app-link\" data-ref=\"domain=123&amp;text=456\">Link Text&amp;</span></b>", html.toEncodedHtml());
  }

  @Test
  public void testTableNoBinds() {
    String html = HTML.table(row(cell(BIND_TEXT))).toEncodedHtml();
    assertEquals("<table><tr><td>" + encode(BIND_TEXT) + "</td></tr></table>", html);
  }

  @Test
  public void testTableAttributesNoBinds() {
    final IHtmlTable table = HTML.table(row(cell(BIND_TEXT)));
    assertEquals("<table><tr><td>" + encode(BIND_TEXT) + "</td></tr></table>", table.toEncodedHtml());
  }

  @Test
  public void testLinkWithBoldNoBinds() {
    final IHtmlElement html = HTML.bold(BIND_TEXT, link(TEST_URL, BIND_TEXT));
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
    assertEquals("", HTML.fragment((CharSequence) null).toEncodedHtml());

    final IHtmlContent fragment = HTML.fragment(HTML.div(BIND_TEXT), BIND_TEXT);
    assertEquals("<div>" + ENCODED_BIND_TEXT + "</div>" + ENCODED_BIND_TEXT, fragment.toEncodedHtml());
  }

  @Test
  public void testMultipleFragments() {
    final IHtmlContent fragment = HTML.fragment(HTML.div("1"), HTML.div("2"));
    assertEquals("<div>1</div><div>2</div>", fragment.toEncodedHtml());
  }

  @Test
  public void testRowWithMultipleBinds() {
    IHtmlTableRow row = HTML.row(HTML.cell("p1"), HTML.cell("p2"), HTML.cell("p4"));
    assertEquals("<tr><td>p1</td><td>p2</td><td>p4</td></tr>", row.toEncodedHtml());
    assertEquals("<tr><td>:b__0</td><td>:b__1</td><td>:b__2</td></tr>", row.toString());
    assertEquals(3, row.getBinds().getBindMap().size());
  }

  @Test
  public void testMultipleCellsNoBinds() {
    IHtmlTableRow row1 = HTML.row(HTML.cell("p1"), HTML.cell("p2"));
    assertEquals("<tr><td>:b__0</td><td>:b__1</td></tr>", row1.toString());
  }

  @Test
  public void testCellWithColspan() {
    IHtmlTableRow row = HTML.row(HTML.cell("1").colspan(2), HTML.cell("2"));
    assertEquals("<tr><td colspan=\"2\">1</td><td>2</td></tr>", row.toEncodedHtml());
  }

  @Test
  public void testMultipleRowsNoBinds() {
    IHtmlTableRow row1 = HTML.row(HTML.cell("p1"), HTML.cell("p2"));
    IHtmlTableRow row2 = HTML.row(HTML.cell("p3"), HTML.cell("p4"));
    String row1String = "<tr><td>p1</td><td>p2</td></tr>";
    String row2String = "<tr><td>p3</td><td>p4</td></tr>";

    String res = HTML.table(row1, row2).toEncodedHtml();
    assertEquals("<table>" + row1String + row2String + "</table>", res);
  }

  @Test
  public void testComplexHtml() {
    final IHtmlElement html = HTML.div(link(TEST_URL, BIND_TEXT), HTML.table(row(cell(BIND_TEXT), cell(BIND_TEXT), cell(BIND_TEXT))));
    String expected = "<div><a href=\"http://SCOUTBLABLA.com\">Test Last Name&amp;</a><table><tr><td>Test Last Name&amp;</td><td>Test Last Name&amp;</td><td>Test Last Name&amp;</td></tr></table></div>";
    assertEquals(expected, html.toEncodedHtml());
  }

  @Test
  public void testUl() {
    String html = HTML.ul(HTML.li(BIND_TEXT)).toEncodedHtml();
    assertEquals("<ul><li>" + ENCODED_BIND_TEXT + "</li></ul>", html);
  }

  @Test
  public void testMultipleUl() {
    String html = HTML.ul(HTML.li(BIND_TEXT), HTML.li("2")).toEncodedHtml();
    assertEquals("<ul><li>" + ENCODED_BIND_TEXT + "</li><li>2</li></ul>", html);
  }

  @Test
  public void testMultipleOl() {
    String html = HTML.ol(HTML.li(BIND_TEXT), HTML.li("2")).toEncodedHtml();
    assertEquals("<ol><li>" + ENCODED_BIND_TEXT + "</li><li>2</li></ol>", html);
  }

  @Test
  public void testHtmlCssStyle() {
    IHtmlContent head = HTML.head(HTML.cssStyle(sampleCSS));
    assertEquals("<head><style type=\"text/css\">" + sampleCSS + "</style></head>", head.toEncodedHtml());
  }

  @Test
  public void testFullHtml() {
    IHtmlDocument html = HTML.html(HTML.cssStyle(sampleCSS), BIND_TEXT);
    String expected = "<html><head><style type=\"text/css\">" + sampleCSS + "</style>" + "</head><body>" + ENCODED_BIND_TEXT + "</body></html>";
    assertEquals(expected, html.toEncodedHtml());
  }

  @Test
  public void testFullHtmlDocType() {
    IHtmlDocument html = HTML.html5(HTML.cssStyle(sampleCSS), BIND_TEXT);
    String expected = "<!DOCTYPE html><html><head><style type=\"text/css\">" + sampleCSS + "</style>" + "</head><body>" + ENCODED_BIND_TEXT + "</body></html>";
    assertEquals(expected, html.toEncodedHtml());
  }

  @Test
  public void testPlain() {
    assertEquals("", HTML.plain((CharSequence) null).toEncodedHtml());
    assertEquals(BIND_TEXT, HTML.plain(BIND_TEXT).toEncodedHtml());
    IHtmlContent plainLink = HTML.plain(BIND_TEXT, HTML.appLink("REF", BIND_TEXT));
    String plainLinkString = String.format("%s<span class=\"app-link\" data-ref=\"REF\">%s</span>", BIND_TEXT, ENCODED_BIND_TEXT);
    assertEquals(String.format(plainLinkString, BIND_TEXT, ENCODED_BIND_TEXT), plainLink.toEncodedHtml());
    assertEquals(String.format("<b>%s</b>", plainLinkString), HTML.bold(plainLink).toEncodedHtml());
    assertEquals(String.format("<i>%s</i>", plainLinkString), HTML.italic(plainLink).toEncodedHtml());
  }

  @Test
  public void testManyBinds() throws Exception {
    IHtmlElement h2 = HTML.h2("h2");
    IHtmlTable table = createTable("0");

    IHtmlElement html = HTML.div(h2, table);

    String exp = "<div><h2>h2</h2>" + createTableString("0") + "</div>";
    assertEquals(exp, html.toEncodedHtml());
  }

  @Test
  public void testNullValues() {
    String encodedNullDiv = HTML.div((String) null).toEncodedHtml();
    assertEquals("<div></div>", encodedNullDiv);
  }

  @Test
  public void testInput() {
    String expected = "<input id='lastName' name='Last name' class='person-data' maxlength='30' value='' type='text'>";
    expected = expected.replace("'", "\"");

    IHtmlInput htmlInput = HTML.input().id("lastName").name("Last name").cssClass("person-data").maxlength(30).value("").type("text");
    assertEquals(expected, htmlInput.toEncodedHtml());
  }

  @Test
  public void testSpecialCharacters() {
    assertEquals("<p>Test$Class</p>", HTML.p("Test$Class").toEncodedHtml());
    assertEquals("<p>C:\\Temp\\config.properties</p>", HTML.p("C:\\Temp\\config.properties").toEncodedHtml());
  }

  private String createTableString(String prefix) {
    List<String> rows = new ArrayList<String>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRowString(prefix, i));
    }
    return "<table>" + CollectionUtility.format(rows, "") + "</table>";

  }

  private String createRowString(String prefix, int i) {
    return HTML.row(HTML.cell("A" + prefix + i), HTML.cell("B" + prefix + i)).toEncodedHtml();
  }

  private IHtmlTable createTable(String prefix) {
    List<IHtmlTableRow> rows = new ArrayList<IHtmlTableRow>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRow(prefix, i));
    }
    return HTML.table(rows);
  }

  private IHtmlTableRow createRow(String prefix, int i) {
    return HTML.row(HTML.cell("A" + prefix + i), HTML.cell("B" + prefix + i));
  }

}
