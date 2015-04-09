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

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.html.internal.EmptyNodeBuilder;
import org.eclipse.scout.commons.html.internal.HtmlImageBuilder;
import org.eclipse.scout.commons.html.internal.HtmlLinkBuilder;
import org.eclipse.scout.commons.html.internal.HtmlNodeBuilder;
import org.eclipse.scout.commons.html.internal.HtmlTableBuilder;
import org.eclipse.scout.commons.html.internal.HtmlTableDataBuilder;
import org.eclipse.scout.commons.html.internal.HtmlTableRowBuilder;

/**
 * Convenience for building a html document or parts of it with encoded binds. <br>
 * Only the most common cases are supported, not intended to be complete.
 */
public final class HTML {

  /**
   * Utility class
   */
  private HTML() {
  }

  /**
   * Create a html element with encoded text for &lt;b&gt;text&lt;/b&gt;.
   * <p>
   * Example:<br>
   * HtmlBinds binds = new HtmlBinds(); <br>
   * IHtmlElement boldHtml = HTML.bold(binds.put(bindText)); <br>
   * String html = binds.applyBindParameters(boldHtml);
   * </p>
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement bold(IHtmlContent... text) {
    return new HtmlNodeBuilder("b", text);
  }

  /**
   * Create a html element with encoded text for &lt;p&gt;text&lt;/p&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement p(IHtmlContent text) {
    return new HtmlNodeBuilder("p", text);
  }

  /**
   * Create a html element for &lt;br&gt;.
   */
  public static IHtmlElement br() {
    return new HtmlNodeBuilder("br");
  }

  /**
   * Create a html element with encoded text for &lt;h1&gt;text&lt;/h1&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement h1(IHtmlContent text) {
    return new HtmlNodeBuilder("h1", text);
  }

  /**
   * Create a html element with encoded text for &lt;h2&gt;text&lt;/h2&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement h2(IHtmlContent text) {
    return new HtmlNodeBuilder("h2", text);
  }

  /**
   * Create a html element with encoded text for &lt;h3&gt;text&lt;/h3&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement h3(IHtmlContent text) {
    return new HtmlNodeBuilder("h3", text);
  }

  /**
   * Create a html element with encoded text for &lt;h4&gt;text&lt;/h4&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement h4(IHtmlContent text) {
    return new HtmlNodeBuilder("h4", text);
  }

  /**
   * Create a html element with encoded text for &lt;h5&gt;text&lt;/h5&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement h5(IHtmlContent text) {
    return new HtmlNodeBuilder("h5", text);
  }

  /**
   * Create a html element with encoded text for &lt;h6&gt;text&lt;/h6&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement h6(IHtmlContent text) {
    return new HtmlNodeBuilder("h6", text);
  }

  /**
   * Create a html element with encoded text for &lt;div&gt;content&lt;/div&gt;.
   *
   * @param content
   *          content as bind
   */
  public static IHtmlElement div(IHtmlContent... content) {
    return div(Arrays.asList(content));
  }

  /**
   * Create a html element with encoded text for &lt;div&gt;content&lt;/div&gt;.
   *
   * @param content
   *          content as bind
   */
  public static IHtmlElement div(List<IHtmlContent> contents) {
    return new HtmlNodeBuilder("div", contents);
  }

  /**
   * Create a html element with encoded text for &lt;span&gt;content&lt;/span&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement span(IHtmlContent text) {
    return new HtmlNodeBuilder("span", text);
  }

  /**
   * Create a html element with encoded text for a link &lt;a href="url"&gt;text&lt;/a&gt;.
   *
   * @param text
   *          text as bind
   */
  public static IHtmlElement link(CharSequence url, CharSequence text) {
    return new HtmlLinkBuilder(url, text);
  }

  /**
   * Create a html element for an image: &lt;img src="path"&gt;&lt;/img&gt;.
   *
   * @param src
   *          image source path as bind
   */
  public static IHtmlElement img(CharSequence src) {
    return new HtmlImageBuilder(src);
  }

  /**
   * Create a html element with encoded text for table data: &lt;td&gt;text&lt;/td&gt;.
   *
   * @param text
   *          text with binds
   */
  public static IHtmlTableCell cell(IHtmlContent... text) {
    return new HtmlTableDataBuilder(Arrays.asList(text));
  }

  /**
   * Create a html element with encoded text for a table row: &lt;tr&gt;...&lt;/tr&gt;.
   *
   * @param td
   *          table data within row
   */
  public static IHtmlTableRow row(IHtmlTableCell... td) {
    return new HtmlTableRowBuilder(Arrays.asList(td));
  }

  /**
   * Create a html element with encoded text for a table.
   */
  public static IHtmlTable table(IHtmlTableRow... rows) {
    return new HtmlTableBuilder(Arrays.asList(rows));
  }

  /**
   * Create a html element with encoded text for a table.
   */
  public static IHtmlTable table(List<IHtmlTableRow> rows) {
    return new HtmlTableBuilder(rows);
  }

  /**
   * Creates an application local link
   *
   * @param path
   *          path to identify what is the link referring to.
   * @param text
   *          the link text
   */
  public static IHtmlElement appLink(CharSequence path, IHtmlContent text) {
    return span(text).appLink(path);
  }

  /**
   * Creates HTML content from multiple elements. e.g. <b>Bold Text</b> Text <b> More bold text </b>
   */
  public static IHtmlContent fragment(IHtmlElement... elements) {
    return new EmptyNodeBuilder(elements);
  }

}
