/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html.internal;

import org.eclipse.scout.rt.platform.html.IHtmlInput;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Builder for a HTML input element (&lt;input&gt;).
 */
public class HtmlInputBuilder extends EmptyHtmlNodeBuilder implements IHtmlInput {

  private static final long serialVersionUID = 1L;

  public HtmlInputBuilder() {
    super("input");
  }

  @Override
  public IHtmlInput id(String id) {
    addAttribute("id", id);
    return this;
  }

  @Override
  public IHtmlInput name(String name) {
    addAttribute("name", name);
    return this;
  }

  @Override
  public IHtmlInput type(String type) {
    addAttribute("type", type);
    return this;
  }

  @Override
  public IHtmlInput value(Object value) {
    addAttribute("value", StringUtility.emptyIfNull(value));
    return this;
  }

  @Override
  public IHtmlInput maxlength(int maxlength) {
    addAttribute("maxlength", maxlength);
    return this;
  }

  @Override
  public IHtmlInput checked() {
    addAttribute("checked", "checked");
    return this;
  }

  @Override
  public IHtmlInput cssClass(CharSequence cssClass) {
    return (IHtmlInput) super.cssClass(cssClass);
  }

  @Override
  public IHtmlInput style(CharSequence style) {
    return (IHtmlInput) super.style(style);
  }

  @Override
  public IHtmlInput appLink(CharSequence ref) {
    return (IHtmlInput) super.appLink(ref);
  }

  @Override
  public IHtmlInput addAttribute(String name, CharSequence value) {
    return (IHtmlInput) super.addAttribute(name, value);
  }
}
