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
package org.eclipse.scout.commons.html.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.html.IHtmlElement;

/**
 * Builder for a html node with start tag, end tag and attributes.
 */
public class HtmlNodeBuilder extends AbstractExpressionBuilder implements IHtmlElement {
  private final List<? extends CharSequence> m_texts;
  private final List<String> m_attributes = new ArrayList<>();
  private String m_tag;

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
    m_tag = tag;
    m_texts = texts;
  }

  @Override
  public void build() {
    appendStartTag();
    if (m_texts.size() > 0) {
      appendText();
      appendEndTag();
    }
  }

  protected void appendText() {
    for (CharSequence t : m_texts) {
      append(t);
    }
  }

  private void appendStartTag() {
    append("<");
    append(getTag());
    appendAttributes();
    append(">");
  }

  protected void appendEndTag() {
    append("</");
    append(getTag());
    append(">");
  }

  private void appendAttributes() {
    if (m_attributes.size() > 0) {
      append(" ");
      append(CollectionUtility.format(m_attributes, " "));
    }
  }

  protected void addAttribute(String name, int value) {
    addAttribute(name, Integer.toString(value));
  }

  protected void addAttribute(String name, CharSequence value) {
    m_attributes.add(name + "=\"" + value + "\"");
  }

  /// GLOBAL ATTRIBUTES
  @SuppressWarnings("unchecked")
  @Override
  public <T extends IHtmlElement> T style(CharSequence value) {
    addAttribute("style", value);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IHtmlElement> T clazz(CharSequence clazz) {
    addAttribute("class", clazz);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IHtmlElement> T appLink(CharSequence path) {
    clazz("hyperlink");
    addAttribute("data-hyperlink", path);
    return (T) this;
  }

}
