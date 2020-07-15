/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.html.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.html.StyleHelper;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Builder for a html node with start tag, end tag and attributes.
 */
public class HtmlNodeBuilder extends HtmlContentBuilder implements IHtmlElement {

  private static final long serialVersionUID = 1L;

  private final List<IHtmlContent> m_attributes = new ArrayList<>();
  private final String m_tag;

  protected String getTag() {
    return m_tag;
  }

  public HtmlNodeBuilder(String tag, CharSequence... texts) {
    this(tag, Arrays.asList(texts));
  }

  public HtmlNodeBuilder(String tag) {
    this(tag, new ArrayList<String>());
  }

  public HtmlNodeBuilder(String tag, List<? extends CharSequence> texts) {
    super(texts);
    m_tag = tag;
  }

  @Override
  public void build() {
    appendStartTag();
    if (!getTexts().isEmpty()) {
      appendText();
    }
    appendEndTag();
  }

  protected void appendStartTag() {
    append("<", false);
    append(getTag(), true);
    appendAttributes();
    append(">", false);
  }

  protected void appendEndTag() {
    append("</", false);
    append(getTag(), true);
    append(">", false);
  }

  private void appendAttributes() {
    if (!m_attributes.isEmpty()) {
      append(" ", false);
      append(CollectionUtility.format(m_attributes, " "), false);
    }
  }

  protected void addAttribute(String name, int value) {
    addAttribute(name, Integer.toString(value));
  }

  @Override
  public IHtmlElement addAttribute(String name, CharSequence value) {
    String attribValue = null;
    final String doubleQuote = "\"";
    if (value == null) {
      attribValue = "";
    }
    else {
      attribValue = StringUtility.replace(value.toString(), doubleQuote, "&quot;");
    }

    HtmlContentBuilder content = new HtmlContentBuilder(
        new HtmlPlainBuilder(escape(name)),
        new HtmlPlainBuilder("=" + doubleQuote),
        new HtmlPlainBuilder(attribValue),
        new HtmlPlainBuilder(doubleQuote));
    m_attributes.add(content);
    return this;
  }

  /**
   * Removes the attribute with the given name and returns its value without quotes.
   */
  public String removeAttribute(String name) {
    ListIterator<IHtmlContent> iterator = m_attributes.listIterator();
    while (iterator.hasNext()) {
      IHtmlContent attr = iterator.next();
      String[] pair = attr.toHtml().split("=", 2);
      if (pair[0].equals(name)) {
        iterator.remove();
        if (pair.length != 2) {
          return null;
        }
        // Remove quotes at start and end
        return StringUtility.substring(pair[1], 1, pair[1].length() - 2);
      }
    }
    return null;
  }

  @Override
  public IHtmlElement addBooleanAttribute(String name) {
    IHtmlContent content = new HtmlPlainBuilder(escape(name));
    m_attributes.add(content);
    return this;
  }

  /// GLOBAL ATTRIBUTES
  @Override
  public IHtmlElement style(CharSequence value) {
    addAttribute("style", value);
    return this;
  }

  @Override
  public IHtmlElement cssClass(CharSequence cssClass) {
    addAttribute("class", cssClass);
    return this;
  }

  @Override
  public IHtmlElement addCssClass(CharSequence cssClass) {
    if (cssClass == null) {
      return this;
    }
    String value = removeAttribute("class");
    value = BEANS.get(StyleHelper.class).addCssClasses(value, cssClass.toString());
    addAttribute("class", value);
    return this;
  }

  @Override
  public IHtmlElement id(CharSequence id) {
    addAttribute("id", id);
    return this;
  }

  @Override
  public IHtmlElement appLink(CharSequence ref) {
    return appLink(ref, "app-link");
  }

  @Override
  public IHtmlElement appLink(CharSequence ref, CharSequence cssClass) {
    cssClass(cssClass);
    addAttribute("data-ref", ref);
    return this;
  }
}
