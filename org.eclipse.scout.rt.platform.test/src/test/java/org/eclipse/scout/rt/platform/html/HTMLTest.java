/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html;

import static org.eclipse.scout.rt.platform.html.HTML.bold;
import static org.eclipse.scout.rt.platform.html.HTML.div;
import static org.eclipse.scout.rt.platform.html.HTML.italic;
import static org.eclipse.scout.rt.platform.html.HTML.link;
import static org.eclipse.scout.rt.platform.html.HTML.td;
import static org.eclipse.scout.rt.platform.html.HTML.tr;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CloneUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link HtmlBinds}
 */
public class HTMLTest {
  private static final String HTML_TEXT = "Test Last Name&";
  private static final String ESCAPED_HTML_TEXT = "Test Last Name&amp;";
  private static final String TEST_URL = "http://SCOUTBLABLA.com\"";

  private static final String sampleCSS = "p {"
      + "    text-align: center;"
      + "    color: red;"
      + "}";

  @Test
  public void testHtmlNoBinds() {
    assertEncodedText("h1", HTML.h1(HTML_TEXT).toHtml());
    assertEncodedText("h2", HTML.h2(HTML_TEXT).toHtml());
    assertEncodedText("h3", HTML.h3(HTML_TEXT).toHtml());
    assertEncodedText("h4", HTML.h4(HTML_TEXT).toHtml());
    assertEncodedText("h5", HTML.h5(HTML_TEXT).toHtml());
    assertEncodedText("h6", HTML.h6(HTML_TEXT).toHtml());
    assertEncodedText("b", bold(HTML_TEXT).toHtml());
    assertEncodedText("i", italic(HTML_TEXT).toHtml());
    assertEncodedText("td", td(HTML_TEXT).toHtml());
    assertEncodedText("div", div(HTML_TEXT).toHtml());
    assertEncodedText("p", HTML.p(HTML_TEXT).toHtml());
    assertEncodedText("span", HTML.span(HTML_TEXT).toHtml());
    assertEncodedText("li", HTML.li(HTML_TEXT).toHtml());
    assertEncodedText("head", HTML.head(HTML_TEXT).toHtml());
    assertEncodedText("body", HTML.body(HTML_TEXT).toHtml());
  }

  /**
   * Tests a link with URL and encoded text.
   */
  @Test
  public void testLinkNoBinds() {
    String html = HTML.link(TEST_URL, HTML_TEXT).toHtml();
    assertEquals("<a href=\"" + TEST_URL.replace("\"", "&quot;") + "\">" + escape(HTML_TEXT) + "</a>", html);
  }

  /**
   * Tests an image encoded source.
   */
  @Test
  public void testImageNoBinds() {
    String html = HTML.img("logo.png").toHtml();
    assertEquals("<img src=\"logo.png\">", html);
  }

  @Test
  public void testNullAttribute() {
    assertEquals("<a href=\"\"></a>", HTML.link(null, null).toHtml());
    assertEquals("<a href=\"\">&lt;a&gt;badlink&lt;&#47;a&gt;</a>", HTML.link(null, "<a>badlink</a>").toHtml());
  }

  /**
   * Tests for {@link HTML#br()}
   */
  @Test
  public void testBr() {
    IHtmlElement br = HTML.br();
    assertEquals("<br>", br.toHtml());
  }

  @Test
  public void testAddAttribute() {
    IHtmlElement span = HTML.span("text").addAttribute("name", "value");
    assertEquals("<span name=\"value\">text</span>", span.toHtml());
  }

  @Test
  public void testAddBooleanAttribute() {
    IHtmlElement span = HTML.span("text").addBooleanAttribute("name");
    assertEquals("<span name>text</span>", span.toHtml());
  }

  /**
   * Test for {@link IHtmlElement#appLink(CharSequence)}
   */
  @Test
  public void testAppLinkNoBinds() {
    final IHtmlElement html = HTML.appLink("domain=123&text=456", "Link Text&");
    assertEquals("<span class=\"app-link\" data-ref=\"domain=123&text=456\">Link Text&amp;</span>", html.toHtml());
  }

  @Test
  public void testAppLinkWithQuote() {
    final IHtmlElement html = HTML.appLink("domain=123\"text=456", "Link Text<a href=\"javascript:window.alert('bad');\">test</a>");
    assertEquals("<span class=\"app-link\" data-ref=\"domain=123&quot;text=456\">Link Text&lt;a href=&quot;javascript:window.alert(&#39;bad&#39;);&quot;&gt;test&lt;&#47;a&gt;</span>", html.toHtml());
  }

  @Test
  public void testBoldAppLink() {
    final IHtmlElement html = HTML.bold("asdf", HTML.appLink("domain=123&text=456", "Link Text&"));
    assertEquals("<b>asdf<span class=\"app-link\" data-ref=\"domain=123&text=456\">Link Text&amp;</span></b>", html.toHtml());
  }

  @Test
  public void testTableNoBinds() {
    String html = HTML.table(tr(td(HTML_TEXT))).toHtml();
    assertEquals("<table><tr><td>" + escape(HTML_TEXT) + "</td></tr></table>", html);
  }

  @Test
  public void testTableAttributesNoBinds() {
    final IHtmlTable table = HTML.table(tr(td(HTML_TEXT)));
    assertEquals("<table><tr><td>" + escape(HTML_TEXT) + "</td></tr></table>", table.toHtml());
  }

  @Test
  public void testLinkWithBoldNoBinds() {
    final IHtmlElement html = HTML.bold(HTML_TEXT, link(TEST_URL, HTML_TEXT));
    assertEquals("<b>Test Last Name&amp;<a href=\"http://SCOUTBLABLA.com&quot;\">Test Last Name&amp;</a></b>", html.toHtml());
  }

  private String escape(String text) {
    return BEANS.get(HtmlHelper.class).escape(text);
  }

  private void assertEncodedText(String tagName, String actualText) {
    assertEquals("<" + tagName + ">" + ESCAPED_HTML_TEXT + "</" + tagName + ">", actualText);
  }

  @Test
  public void testFragment() {
    assertEquals("", HTML.fragment((CharSequence) null).toHtml());

    final IHtmlContent fragment = HTML.fragment(HTML.div(HTML_TEXT), HTML_TEXT);
    assertEquals("<div>" + ESCAPED_HTML_TEXT + "</div>" + ESCAPED_HTML_TEXT, fragment.toHtml());
  }

  @Test
  public void testMultipleFragments() {
    final IHtmlContent fragment = HTML.fragment(HTML.div("1"), HTML.div("2"));
    assertEquals("<div>1</div><div>2</div>", fragment.toHtml());
  }

  @Test
  public void testRowWithMultipleBinds() {
    IHtmlTableRow row = HTML.tr(HTML.td("p1"), HTML.td("p2"), HTML.td("p4"));
    assertEquals("<tr><td>p1</td><td>p2</td><td>p4</td></tr>", row.toHtml());
    assertEquals("<tr><td>p1</td><td>p2</td><td>p4</td></tr>", row.toString());
  }

  @Test
  public void testMultipleCellsNoBinds() {
    IHtmlTableRow row1 = HTML.tr(HTML.td("p1"), HTML.td("p2"));
    assertEquals("<tr><td>p1</td><td>p2</td></tr>", row1.toString());
  }

  @Test
  public void testCellWithColspan() {
    IHtmlTableRow row = HTML.tr(HTML.td("1").colspan(2), HTML.td("2"));
    assertEquals("<tr><td colspan=\"2\">1</td><td>2</td></tr>", row.toHtml());
  }

  @Test
  public void testMultipleRowsNoBinds() {
    IHtmlTableRow row1 = HTML.tr(HTML.td("p1"), HTML.td("p2"));
    IHtmlTableRow row2 = HTML.tr(HTML.td("p3"), HTML.td("p4"));
    String row1String = "<tr><td>p1</td><td>p2</td></tr>";
    String row2String = "<tr><td>p3</td><td>p4</td></tr>";

    String res = HTML.table(row1, row2).toHtml();
    assertEquals("<table>" + row1String + row2String + "</table>", res);
  }

  @Test
  public void testComplexHtml() {
    final IHtmlElement html = HTML.div(link(TEST_URL, HTML_TEXT), HTML.table(tr(td(HTML_TEXT), td(HTML_TEXT), td(HTML_TEXT))));
    String expected = "<div><a href=\"http://SCOUTBLABLA.com&quot;\">Test Last Name&amp;</a><table><tr><td>Test Last Name&amp;</td><td>Test Last Name&amp;</td><td>Test Last Name&amp;</td></tr></table></div>";
    assertEquals(expected, html.toHtml());
  }

  @Test
  public void testUl() {
    String html = HTML.ul(HTML.li(HTML_TEXT)).toHtml();
    assertEquals("<ul><li>" + ESCAPED_HTML_TEXT + "</li></ul>", html);
  }

  @Test
  public void testMultipleUl() {
    String html = HTML.ul(HTML.li(HTML_TEXT), HTML.li("2")).toHtml();
    assertEquals("<ul><li>" + ESCAPED_HTML_TEXT + "</li><li>2</li></ul>", html);
  }

  @Test
  public void testMultipleOl() {
    String html = HTML.ol(HTML.li(HTML_TEXT), HTML.li("2")).toHtml();
    assertEquals("<ol><li>" + ESCAPED_HTML_TEXT + "</li><li>2</li></ol>", html);
  }

  @Test
  public void testHtmlCssStyle() {
    IHtmlContent head = HTML.head(HTML.cssStyle(sampleCSS));
    assertEquals("<head><style type=\"text/css\">" + sampleCSS + "</style></head>", head.toHtml());
  }

  @Test
  public void testFullHtml() {
    IHtmlDocument html = HTML.html(HTML.cssStyle(sampleCSS), HTML_TEXT);
    String expected = "<html><head><style type=\"text/css\">" + sampleCSS + "</style>" + "</head><body>" + ESCAPED_HTML_TEXT + "</body></html>";
    assertEquals(expected, html.toHtml());
  }

  @Test
  public void testFullHtmlDocType() {
    IHtmlDocument html = HTML.html5(HTML.cssStyle(sampleCSS), HTML_TEXT);
    String expected = "<!DOCTYPE html><html><head><style type=\"text/css\">" + sampleCSS + "</style>" + "</head><body>" + ESCAPED_HTML_TEXT + "</body></html>";
    assertEquals(expected, html.toHtml());
  }

  @Test
  public void testPlain() {
    assertEquals("", HTML.raw((CharSequence) null).toHtml());
    assertEquals(HTML_TEXT, HTML.raw(HTML_TEXT).toHtml());
    IHtmlContent plainLink = HTML.raw(HTML_TEXT, HTML.appLink("REF", HTML_TEXT));
    String plainLinkString = String.format("%s<span class=\"app-link\" data-ref=\"REF\">%s</span>", HTML_TEXT, ESCAPED_HTML_TEXT);
    assertEquals(String.format(plainLinkString, HTML_TEXT, ESCAPED_HTML_TEXT), plainLink.toHtml());
    assertEquals(String.format("<b>%s</b>", plainLinkString), HTML.bold(plainLink).toHtml());
    assertEquals(String.format("<i>%s</i>", plainLinkString), HTML.italic(plainLink).toHtml());
  }

  @Test
  public void testManyBinds() throws Exception {
    IHtmlElement h2 = HTML.h2("h2");
    IHtmlTable table = createTable("0");

    IHtmlElement html = HTML.div(h2, table);

    String exp = "<div><h2>h2</h2>" + createTableString("0") + "</div>";
    assertEquals(exp, html.toHtml());
  }

  @Test
  public void testNullValues() {
    String encodedNullDiv = HTML.div((String) null).toHtml();
    assertEquals("<div></div>", encodedNullDiv);
  }

  @Test
  public void testInput() {
    String expected = "<input id='lastName' name='Last name' class='person-data' maxlength='30' value='' type='text'>";
    expected = expected.replace("'", "\"");

    IHtmlInput htmlInput = HTML.input().id("lastName").name("Last name").cssClass("person-data").maxlength(30).value("").type("text");
    assertEquals(expected, htmlInput.toHtml());
  }

  @Test
  public void testSpecialCharacters() {
    assertEquals("<p>Test$Class</p>", HTML.p("Test$Class").toHtml());
    assertEquals("<p>C:\\Temp\\config.properties</p>", HTML.p("C:\\Temp\\config.properties").toHtml());
  }

  @Test
  public void testIconFromScoutFont() {
    assertEquals("<span class=\"font-icon\">\uE002</span>", HTML.icon("font:\uE002").toHtml());
  }

  @Test
  public void testIconFromCustomFont() {
    assertEquals("<span class=\"font-crmIcons\">\uE100</span>", HTML.icon("font:crmIcons \uE100").toHtml());
  }

  @Test
  public void testIconFromImage() {
    assertEquals("<img src=\"iconId:logo\">", HTML.icon("logo").toHtml());
  }

  private String createTableString(String prefix) {
    List<String> rows = new ArrayList<String>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRowString(prefix, i));
    }
    return "<table>" + CollectionUtility.format(rows, "") + "</table>";

  }

  private String createRowString(String prefix, int i) {
    return HTML.tr(HTML.td("A" + prefix + i), HTML.td("B" + prefix + i)).toHtml();
  }

  private IHtmlTable createTable(String prefix) {
    List<IHtmlTableRow> rows = new ArrayList<IHtmlTableRow>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRow(prefix, i));
    }
    return HTML.table(rows);
  }

  private IHtmlTableRow createRow(String prefix, int i) {
    return HTML.tr(HTML.td("A" + prefix + i), HTML.td("B" + prefix + i));
  }

  @Test
  public void testNewLineToBr() {
    assertEquals("one &amp; two<br>three &amp; four", HTML.fragment("one & two\nthree & four").toHtml());
    assertEquals("one &amp; two\nthree &amp; four", HTML.fragment("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals("<span>one &amp; two<br>three &amp; four</span>", HTML.span("one & two\nthree & four").toHtml());
    assertEquals("<span>one &amp; two\nthree &amp; four</span>", HTML.span("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals("<i>one &amp; two<br>three &amp; four</i>", HTML.italic("one & two\nthree & four").toHtml());
    assertEquals("<i>one &amp; two\nthree &amp; four</i>", HTML.italic("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals("<b>one &amp; two<br>three &amp; four</b>", HTML.bold("one & two\nthree & four").toHtml());
    assertEquals("<b>one &amp; two\nthree &amp; four</b>", HTML.bold("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals("<div>one &amp; two<br>three &amp; four</div>", HTML.div("one & two\nthree & four").toHtml());
    assertEquals("<div>one &amp; two\nthree &amp; four</div>", HTML.div("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals("<h1>one &amp; two<br>three &amp; four</h1>", HTML.h1("one & two\nthree & four").toHtml());
    assertEquals("<h1>one &amp; two\nthree &amp; four</h1>", HTML.h1("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals("<p>one &amp; two<br>three &amp; four</p>", HTML.p("one & two\nthree & four").toHtml());
    assertEquals("<p>one &amp; two\nthree &amp; four</p>", HTML.p("one & two\nthree & four").withNewLineToBr(false).toHtml());

    // for app links, only 2 argument (text) is escaped and new lines are replaced
    assertEquals("<span class=\"app-link\" data-ref=\"one & two\nthree & four\">one &amp; two<br>three &amp; four</span>", HTML.appLink("one & two\nthree & four", "one & two\nthree & four").toHtml());
    assertEquals("<span class=\"app-link\" data-ref=\"one & two\nthree & four\">one &amp; two\nthree &amp; four</span>", HTML.appLink("one & two\nthree & four", "one & two\nthree & four").withNewLineToBr(false).toHtml());
  }

  @Test
  public void testEquality() {
    assertEquals(BEANS.get(HtmlHelper.class).escapeAndNewLineToBr("one & two\nthree & four"), HTML.fragment("one & two\nthree & four").toHtml());
    assertEquals(BEANS.get(HtmlHelper.class).escape("one & two\nthree & four"), HTML.fragment("one & two\nthree & four").withNewLineToBr(false).toHtml());

    assertEquals(HTML.raw(BEANS.get(HtmlHelper.class).escapeAndNewLineToBr("one & two\nthree & four")).toHtml(), HTML.fragment("one & two\nthree & four").toHtml());
    assertEquals(HTML.raw(BEANS.get(HtmlHelper.class).escape("one & two\nthree & four")).toHtml(), HTML.fragment("one & two\nthree & four").withNewLineToBr(false).toHtml());
  }

  @Test(expected = ProcessingException.class)
  public void testNonSerializableRaw() {
    HTML.raw("1", "2", new NonSerializableCharSequence(), "3");
  }

  @Test(expected = ProcessingException.class)
  public void testNonSerializableFragment() {
    HTML.fragment("1", "2", new NonSerializableCharSequence(), "3");
  }

  @Test
  public void testSerializability() throws Exception {
    IHtmlDocument htmlDocument = HTML.html5(
        null,
        HTML.body(
            HTML.p("Lorem ipsum"),
            HTML.bold(" dolor sit amet,"),
            HTML.div(" consetetur sadipscing elitr,"),
            HTML.fragment(" sed", " diam", " nonumy", "eirmod"),
            HTML.italic(HTML.raw("<p>tempor invidunt ut labore et dolore magna aliquyam erat.</p>"))));

    String htmlBeforeSerialization = htmlDocument.toHtml();
    String plainTextBeforeSerialization = htmlDocument.toPlainText();

    IHtmlDocument clonedHtmlDocument = CloneUtility.createDeepCopyBySerializing(htmlDocument);

    Assert.assertEquals(htmlBeforeSerialization, clonedHtmlDocument.toHtml());
    Assert.assertEquals(plainTextBeforeSerialization, clonedHtmlDocument.toPlainText());
  }

  protected static class NonSerializableCharSequence implements CharSequence {

    @Override
    public int length() {
      return 0;
    }

    @Override
    public char charAt(int index) {
      return 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return null;
    }
  }
}
