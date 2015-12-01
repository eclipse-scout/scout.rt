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
package org.eclipse.scout.rt.platform.html.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.html.IHtmlBind;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Builder for a html node with start tag, end tag and attributes.
 */
public class HtmlNodeBuilder extends HtmlContentBuilder implements IHtmlElement {

  private final List<IHtmlBind> m_attributes = new ArrayList<>();
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
    super(texts);
    m_tag = tag;
  }

  @Override
  public void build() {
    appendStartTag();
    if (getTexts().size() > 0) {
      appendText();
    }
    appendEndTag();
  }

  protected void appendStartTag() {
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

  @Override
  public IHtmlElement addAttribute(String name, CharSequence value) {
    HtmlContentBuilder content = new HtmlContentBuilder(
        getBinds().put(name),
        new HtmlPlainBuilder("=\""),
        getBinds().put(value),
        new HtmlPlainBuilder("\""));
    m_attributes.add(content);
    return this;
  }

  @Override
  public void replaceBinds(Map<String/*old Bind*/, String/*new Bind*/> bindMap) {
    super.replaceBinds(bindMap);
    for (IHtmlBind elem : m_attributes) {
      elem.replaceBinds(bindMap);
    }
    getBinds().replaceBinds(bindMap);
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
  public IHtmlElement appLink(CharSequence ref) {
    cssClass("app-link");
    addAttribute("data-ref", ref);
    return this;
  }

}
