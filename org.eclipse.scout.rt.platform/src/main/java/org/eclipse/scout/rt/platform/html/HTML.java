/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.html.internal.EmptyHtmlNodeBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlContentBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlDocumentBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlImageBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlInputBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlLinkBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlListElement;
import org.eclipse.scout.rt.platform.html.internal.HtmlNodeBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlPlainBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlTableBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlTableColgroupBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlTableColgroupColBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlTableDataBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlTableHeadBuilder;
import org.eclipse.scout.rt.platform.html.internal.HtmlTableRowBuilder;
import org.eclipse.scout.rt.platform.html.internal.StyleElementBuilder;
import org.eclipse.scout.rt.platform.resource.BinaryRefs;
import org.eclipse.scout.rt.platform.resource.BinaryResourceUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Convenience for building a HTML document or parts of it with encoded text.
 * <p>
 * Only the most common cases are supported, not intended to be complete.
 * <p>
 * Because {@link IHtmlContent} extends {@link Serializable}, all {@link CharSequence} provided as arguments must be
 * serializable (constraint verified by constructor of {@link HtmlPlainBuilder} and {@link HtmlContentBuilder}.
 */
public final class HTML {

  // pattern for Scout font icon 'font:CHAR', pattern for custom font icon: 'font:FONT-NAME CHAR'
  private static final Pattern FONT_ICON_PATTERN = Pattern.compile("font:([^ ]+)(?:$| (.+$))");

  /**
   * Utility class
   */
  private HTML() {
  }

  /**
   * Creates a <code>&lt;head&gt...&lt;/head&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.head("text").toHtml();</code>
   */
  public static IHtmlContent head(CharSequence... elements) {
    return new HtmlNodeBuilder("head", elements);
  }

  /**
   * Creates a <code>&lt;body&gt...&lt;/body&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.body("text").toHtml();</code>
   */
  public static IHtmlContent body(CharSequence... elements) {
    return new HtmlNodeBuilder("body", elements);
  }

  /**
   * Creates a <code>&lt;b&gt...&lt;/b&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.bold("text").toHtml();</code>
   */
  public static IHtmlElement bold(CharSequence... text) {
    return new HtmlNodeBuilder("b", text);
  }

  /**
   * Creates a <code>&lt;i&gt...&lt;/i&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.italic("text").toHtml();</code>
   */
  public static IHtmlElement italic(CharSequence... text) {
    return new HtmlNodeBuilder("i", text);
  }

  /**
   * Creates a <code>&lt;p&gt...&lt;/p&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.p("text").toHtml();</code>
   */
  public static IHtmlElement p(CharSequence... text) {
    return new HtmlNodeBuilder("p", text);
  }

  /**
   * Creates a <code>&lt;br&gt</code> element without content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.br().toHtml();</code>
   */
  public static IHtmlElement br() {
    return new EmptyHtmlNodeBuilder("br");
  }

  /**
   * Creates a <code>&lt;hr&gt</code> element without content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.hr().toHtml();</code>
   */
  public static IHtmlElement hr() {
    return new EmptyHtmlNodeBuilder("hr");
  }

  /**
   * Creates a <code>&lt;h1&gt...&lt;/h1&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.h1("text").toHtml();</code>
   */
  public static IHtmlElement h1(CharSequence... text) {
    return new HtmlNodeBuilder("h1", text);
  }

  /**
   * Creates a <code>&lt;h2&gt...&lt;/h2&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.h2("text").toHtml();</code>
   */
  public static IHtmlElement h2(CharSequence... text) {
    return new HtmlNodeBuilder("h2", text);
  }

  /**
   * Creates a <code>&lt;h3&gt...&lt;/h3&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.h3("text").toHtml();</code>
   */
  public static IHtmlElement h3(CharSequence... text) {
    return new HtmlNodeBuilder("h3", text);
  }

  /**
   * Creates a <code>&lt;h4&gt...&lt;/h4&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.h4("text").toHtml();</code>
   */
  public static IHtmlElement h4(CharSequence... text) {
    return new HtmlNodeBuilder("h4", text);
  }

  /**
   * Creates a <code>&lt;h5&gt...&lt;/h5&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.h5("text").toHtml();</code>
   */
  public static IHtmlElement h5(CharSequence... text) {
    return new HtmlNodeBuilder("h5", text);
  }

  /**
   * Creates a <code>&lt;h6&gt...&lt;/h6&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.h6("text").toHtml();</code>
   */
  public static IHtmlElement h6(CharSequence... text) {
    return new HtmlNodeBuilder("h6", text);
  }

  /**
   * Creates a <code>&lt;div&gt...&lt;/div&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.div("text").toHtml();</code>
   */
  public static IHtmlElement div(CharSequence... content) {
    return div(Arrays.asList(content));
  }

  /**
   * Creates a <code>&lt;div&gt...&lt;/div&gt</code> element with encoded content.
   */
  public static IHtmlElement div(List<CharSequence> contents) {
    return new HtmlNodeBuilder("div", contents);
  }

  /**
   * Creates a <code>&lt;span&gt...&lt;/span&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.span("text").toHtml();</code>
   */
  public static IHtmlElement span(CharSequence... text) {
    return new HtmlNodeBuilder("span", text);
  }

  /**
   * Creates a <code>&lt;a&gt...&lt;/a&gt</code> element with encoded text and attribute
   * <code>rel="noreferrer noopener"</code>.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.link("http://xyz.com","text").toHtml();</code>
   */
  public static IHtmlElement link(CharSequence url, CharSequence text) {
    return link(url, text, false);
  }

  /**
   * Creates a <code>&lt;a&gt...&lt;/a&gt</code> element with encoded text.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.link("http://xyz.com","text").toHtml();</code>
   * </p>
   *
   * @param preserveOpener
   *     Specifies if the opener and referrer should be available to the target browser window. Because of security
   *     reasons this should be disabled ({@code false}) in most cases. If disabled, the attribute
   *     {@code rel="noreferrer noopener"} is added to the link element.
   * @see "https://developer.mozilla.org/en-US/docs/Web/HTML/Link_types"
   * @see "https://mathiasbynens.github.io/rel-noopener/"
   */
  public static IHtmlElement link(CharSequence url, CharSequence text, boolean preserveOpener) {
    return new HtmlLinkBuilder(url, text, preserveOpener);
  }

  /**
   * Creates a <code>&lt;img src="..."&gt</code> element.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.img("image.png").toHtml();</code>
   *
   * @param src
   *     image source path
   */
  public static IHtmlElement img(CharSequence src) {
    return new HtmlImageBuilder(src);
  }

  /**
   * Creates a <code>&lt;img src="binaryResource:..."&gt</code> element.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.imgByBinaryResource("resourceName").toHtml();</code>
   *
   * @param binaryResource
   *     image source path
   */
  public static IHtmlElement imgByBinaryResource(CharSequence binaryResource) {
    return new HtmlImageBuilder(BinaryResourceUtility.createUrl(binaryResource.toString()));
  }

  /**
   * Creates a <code>&lt;img src="binref:..."&gt</code> element.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.imgByBinaryRef("/crm/customer/image/1234").toHtml();</code>
   *
   * @param binaryRef
   *     base path plus id
   */
  public static IHtmlElement imgByBinaryRef(CharSequence binaryRef) {
    if (binaryRef != null && StringUtility.startsWith(binaryRef.toString(), BinaryRefs.URI_SCHEME + ":")) {
      return new HtmlImageBuilder(binaryRef);
    }
    return new HtmlImageBuilder(BinaryRefs.URI_SCHEME + ":" + binaryRef);
  }

  /**
   * Creates a <code>&lt;img src="iconId:..."&gt</code> element.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.imgByIconId("icon_name").toHtml();</code>
   */
  public static IHtmlElement imgByIconId(CharSequence iconId) {
    return new HtmlImageBuilder("iconId:" + iconId);
  }

  /**
   * Creates an appropriate element for the given icon:
   * <ul>
   * <li>For a font icon, a <code>span</code> element is returned.
   * <li>For all other icons, an <code>img</code> element is returned.
   * </ul>
   *
   * @param icon
   *     icon like AbstractIcons#Info
   */
  public static IHtmlElement icon(final CharSequence icon) {
    final Matcher matcher = FONT_ICON_PATTERN.matcher(icon);
    if (matcher.find()) {
      // font icon
      if (matcher.group(2) == null) {
        return span(matcher.group(1)).cssClass("font-icon"); // icon from Scout font
      }
      else {
        return span(matcher.group(2)).cssClass("font-icon font-" + matcher.group(1)); // icon from custom font
      }
    }
    else {
      // image icon
      return imgByIconId(icon);
    }
  }

  /**
   * Creates a <code>&lt;li&gt...&lt;/li&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.li("text").toHtml();</code>
   */
  public static IHtmlListElement li(CharSequence text) {
    return new HtmlListElement(text);
  }

  /**
   * Creates a <code>&lt;ul&gt...&lt;/ul&gt</code> element with the given list elements.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.ul(HTML.li("text"),...).toHtml();</code>
   */
  public static IHtmlElement ul(IHtmlListElement... li) {
    return ul(Arrays.asList(li));
  }

  /**
   * Creates a <code>&lt;ul&gt...&lt;/ul&gt</code> element with the given list elements.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.ul(HTML.li("text"),...).toHtml();</code>
   */
  public static IHtmlElement ul(List<IHtmlListElement> li) {
    return new HtmlNodeBuilder("ul", li);
  }

  /**
   * Creates a <code>&lt;ol&gt...&lt;/ol&gt</code> element with the given list elements.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.ol(HTML.li("text"),...).toHtml();</code>
   */
  public static IHtmlContent ol(IHtmlListElement... li) {
    return new HtmlNodeBuilder("ol", li);
  }

  /**
   * Creates a <code>&lt;colgroup&gt...&lt;/colgroup&gt</code> element with the given <code>col</code> elements
   * <p>
   * Example see {@link #table(IHtmlTableColgroup, List)}.
   */
  public static IHtmlTableColgroup colgroup(IHtmlTableColgroupCol... cols) {
    return new HtmlTableColgroupBuilder(Arrays.asList(cols));
  }

  /**
   * Creates a <code>&lt;colgroup&gt...&lt;/colgroup&gt</code> element with the given <code>col</code> elements
   * <p>
   * Example see {@link #table(IHtmlTableColgroup, List)}.
   */
  public static IHtmlTableColgroup colgroup(List<IHtmlTableColgroupCol> cols) {
    return new HtmlTableColgroupBuilder(cols);
  }

  /**
   * Creates a <code>&lt;col&gt</code> element.
   * <p>
   * Example see {@link #table(IHtmlTableColgroup, List)}.
   */
  public static IHtmlTableColgroupCol col() {
    return new HtmlTableColgroupColBuilder();
  }

  /**
   * Creates a <code>&lt;th&gt...&lt;/th&gt</code> element with encoded content.
   * <p>
   * Example see {@link #table(IHtmlTableRow...)}.
   */
  public static IHtmlTableCell th(CharSequence... text) {
    return new HtmlTableHeadBuilder(Arrays.asList(text));
  }

  /**
   * Creates a <code>&lt;td&gt...&lt;/td&gt</code> element with encoded content.
   * <p>
   * Example see {@link #table(IHtmlTableRow...)}.
   */
  public static IHtmlTableCell td(CharSequence... text) {
    return new HtmlTableDataBuilder(Arrays.asList(text));
  }

  /**
   * Creates a <code>&lt;tr&gt...&lt;/tr&gt</code> element with encoded content.
   * <p>
   * Example see {@link #table(IHtmlTableRow...)}.
   */
  public static IHtmlTableRow tr(IHtmlTableCell... td) {
    return tr(Arrays.asList(td));
  }

  /**
   * Creates a <code>&lt;tr&gt...&lt;/tr&gt</code> element with encoded content.
   * <p>
   * Example see {@link #table(IHtmlTableRow...)}.
   */
  public static IHtmlTableRow tr(List<IHtmlTableCell> td) {
    return new HtmlTableRowBuilder(td);
  }

  /**
   * Creates a <code>&lt;table&gt...&lt;/table&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   *
   * <pre>
   * HTML.table(
   *   HTML.tr(
   *     HTML.th("header1"),
   *     HTML.th("header2")
   *   ),
   *   HTML.tr(
   *     HTML.td("cell1"),
   *     HTML.td("cell2")
   *   ),
   *   HTML.tr(
   *     HTML.td("cell3"),
   *     HTML.td("cell4")
   * )
   * </pre>
   */
  public static IHtmlTable table(IHtmlTableRow... rows) {
    return new HtmlTableBuilder(Arrays.asList(rows));
  }

  /**
   * Creates a <code>&lt;table&gt...&lt;/table&gt</code> element with encoded content.
   * <p>
   * Example see {@link #table(IHtmlTableRow...)}.
   */
  public static IHtmlTable table(List<IHtmlTableRow> rows) {
    return new HtmlTableBuilder(rows);
  }

  /**
   * Creates a <code>&lt;table&gt...&lt;/table&gt</code> element with encoded content.
   * <p>
   * <i>Example:</i><br>
   *
   * <pre>
   * HTML.table(
   *   HTML.colgroup(
   *     HTML.col(),
   *     HTML.col()
   *   ),
   *   HTML.tr(
   *     HTML.td("cell1"),
   *     HTML.td("cell2")
   *   ),
   *   HTML.tr(
   *     HTML.td("cell3"),
   *     HTML.td("cell4")
   * )
   * </pre>
   */
  public static IHtmlTable table(IHtmlTableColgroup colgroup, IHtmlTableRow... rows) {
    return new HtmlTableBuilder(colgroup, Arrays.asList(rows));
  }

  /**
   * Creates a <code>&lt;table&gt...&lt;/table&gt</code> element with encoded content.
   * <p>
   * Example see {@link #table(IHtmlTableColgroup, IHtmlTableRow...)}.
   */
  public static IHtmlTable table(IHtmlTableColgroup colgroup, List<IHtmlTableRow> rows) {
    return new HtmlTableBuilder(colgroup, rows);
  }

  /**
   * Creates an application local link with encoded text.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.appLink("path","text").toHtml();</code>
   *
   * @param ref
   *     what the link is referring to
   * @param text
   *     the link text
   */
  public static IHtmlElement appLink(CharSequence ref, CharSequence text) {
    return span(text).appLink(ref);
  }

  /**
   * Creates HTML content from multiple elements (concatenation). The fragment element itself has no representation in
   * the resulting HTML document.
   * <p>
   * <i>Example:</i><br>
   *
   * <pre>
   * String encodedHtml = HTML.fragment(
   *     HTML.bold("Attention: "),
   *     "This message is ",
   *     HTML.italic("not important.")).toHtml();
   *
   * // Result:
   * // &lt;b&gt;Attention: &lt;/b&gt; This message is &lt;i&gt;not important.&lt;i&gt;</code>
   * </pre>
   */
  public static IHtmlContent fragment(CharSequence... elements) {
    return new HtmlContentBuilder(elements);
  }

  /**
   * Creates HTML content from multiple elements (concatenation). The fragment element itself has no representation in
   * the resulting HTML document.
   * <p>
   * <i>Example:</i><br>
   *
   * <pre>
   * String encodedHtml = HTML.fragment(
   *     HTML.bold("Attention: "),
   *     "This message is ",
   *     HTML.italic("not important.")).toHtml();
   *
   * // Result:
   * // &lt;b&gt;Attention: &lt;/b&gt; This message is &lt;i&gt;not important.&lt;i&gt;</code>
   * </pre>
   */
  public static IHtmlContent fragment(List<? extends CharSequence> elements) {
    return new HtmlContentBuilder(elements);
  }

  /**
   * Creates a <code>&lt;style type="text/css"&gt...&lt;/style&gt</code> element with encoded content.
   */
  public static IHtmlElement cssStyle(CharSequence... cssContent) {
    return new StyleElementBuilder(cssContent).type("text/css");
  }

  /**
   * Creates a <code>&lt;html&gt...&lt;/html&gt</code> with HTML5 doctype.
   */
  public static IHtmlDocument html5(CharSequence head, CharSequence body) {
    return new HtmlDocumentBuilder(head(head), body(body)).doctype();
  }

  /**
   * Creates a <code>&lt;html&gt...&lt;/html&gt</code> without doctype.
   */
  public static IHtmlDocument html(CharSequence head, CharSequence body) {
    return new HtmlDocumentBuilder(head(head), body(body));
  }

  /**
   * Creates HTML content that will not be encoded with {@link IHtmlContent#toHtml()}. <b>Use with caution!</b>
   */
  public static IHtmlContent raw(CharSequence... text) {
    return new HtmlPlainBuilder(text);
  }

  /**
   * Creates a <code>&lt;input&gt</code> element without content.
   */
  public static IHtmlInput input() {
    return new HtmlInputBuilder();
  }

  /**
   * Creates a HTML element for the given tag name with encoded content.
   * <p>
   * Use this method, if no specific method is available.
   * <p>
   * <i>Example:</i><br>
   * <code>String encodedHtml = HTML.tag("my-special-tag").toHtml();</code>
   */
  public static IHtmlElement tag(String tag, CharSequence... text) {
    return new HtmlNodeBuilder(tag, text);
  }
}
